package com.minecraft.service.impl;

import com.alibaba.fastjson2.JSON;
import com.minecraft.config.QrLoginProperties;
import com.minecraft.dto.qr.QrTicket;
import com.minecraft.dto.request.QrScanRequest;
import com.minecraft.entity.LoginLog;
import com.minecraft.entity.User;
import com.minecraft.enums.QrLoginStatus;
import com.minecraft.exception.BusinessException;
import com.minecraft.mapper.UserMapper;
import com.minecraft.security.core.LoginUser;
import com.minecraft.service.LoginLogService;
import com.minecraft.service.QrLoginService;
import com.minecraft.service.UserPermissionService;
import com.minecraft.service.qr.QrTicketStore;
import com.minecraft.service.qr.QrTicketStore.ConsumeResult;
import com.minecraft.service.qr.QrTicketStore.ConsumeStatus;
import com.minecraft.service.qr.QrWsNotifier;
import com.minecraft.utils.DeviceInfoUtils;
import com.minecraft.utils.IpLocationUtils;
import com.minecraft.utils.JwtUtil;
import com.minecraft.vo.QrConsumeVO;
import com.minecraft.vo.QrGenerateVO;
import com.minecraft.vo.QrScanDetailVO;
import com.minecraft.vo.QrStatusVO;
import com.minecraft.vo.QrUserInfoVO;
import com.minecraft.vo.QrWebClientVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

/**
 * 扫码登录核心服务：ticket 生成、状态机流转、Web token 签发与消费。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QrLoginServiceImpl implements QrLoginService {

    private static final int ERR_NOT_FOUND = 4001;
    private static final int ERR_EXPIRED = 4002;
    private static final int ERR_USED = 4003;
    private static final int ERR_OCCUPIED = 4004;
    private static final int ERR_FORBIDDEN = 4005;
    private static final int ERR_STATE = 4006;
    private static final int ERR_RATE_LIMIT = 4007;
    private static final int ERR_DISABLED = 4008;

    private static final String GEN_RATE_KEY = "mc:qr:rate:gen:";
    private static final String SCAN_RATE_KEY = "mc:qr:rate:scan:";
    private static final String INVALID_RATE_KEY = "mc:qr:rate:invalid:";

    private static final int INVALID_LIMIT = 20;
    private static final Duration INVALID_WINDOW = Duration.ofMinutes(5);
    private static final Duration BLOCK_TTL = Duration.ofMinutes(10);
    private static final Duration USED_TTL = Duration.ofSeconds(60);

    private final QrLoginProperties properties;
    private final QrTicketStore ticketStore;
    private final QrWsNotifier wsNotifier;
    private final JwtUtil jwtUtil;
    private final UserPermissionService permissionService;
    private final LoginLogService loginLogService;
    private final UserMapper userMapper;
    private final DeviceInfoUtils deviceInfoUtils;
    private final IpLocationUtils ipLocationUtils;

    @Override
    public QrGenerateVO generate(HttpServletRequest request) {
        ensureEnabled();
        String ip = deviceInfoUtils.getClientIP(request);
        if (ticketStore.isBlocked(ip)) {
            throw new BusinessException(ERR_RATE_LIMIT, "扫码请求过于频繁，请稍后再试");
        }
        Long count = ticketStore.increment(GEN_RATE_KEY + ip, Duration.ofMinutes(1));
        if (count != null && count > properties.getRateLimit().getGeneratePerIpPerMin()) {
            throw new BusinessException(ERR_RATE_LIMIT, "生成二维码过于频繁，请稍后再试");
        }

        long now = System.currentTimeMillis();
        long ttlSeconds = properties.getTtlSeconds();
        String userAgent = deviceInfoUtils.getUserAgent(request);

        QrTicket ticket = new QrTicket();
        ticket.setTicket(randomTicket());
        ticket.setStatus(QrLoginStatus.WAITING);
        ticket.setGenIp(ip);
        ticket.setGenRegion(ipLocationUtils.getLocation(ip));
        ticket.setGenUserAgent(userAgent);
        ticket.setGenBrowser(deviceInfoUtils.getBrowser(userAgent));
        ticket.setGenOs(deviceInfoUtils.getOS(userAgent));
        ticket.setGenAt(now);
        ticket.setExpireAt(now + ttlSeconds * 1000);

        ticketStore.save(ticket.getTicket(), ticket, Duration.ofSeconds(ttlSeconds));

        QrGenerateVO vo = new QrGenerateVO();
        vo.setTicket(ticket.getTicket());
        vo.setQrContent(buildQrContent(ticket.getTicket()));
        vo.setExpireAt(ticket.getExpireAt());
        vo.setTtl(ttlSeconds);
        return vo;
    }

    @Override
    public QrStatusVO status(String ticket) {
        QrTicket data = ticketStore.get(ticket);
        if (data != null) {
            return statusVO(ticket, data.getStatus(), data.getExpireAt());
        }
        QrLoginStatus status = ticketStore.isUsed(ticket) ? QrLoginStatus.CONSUMED : QrLoginStatus.EXPIRED;
        return statusVO(ticket, status, null);
    }

    @Override
    public QrScanDetailVO scan(QrScanRequest request, Long appUserId, HttpServletRequest httpRequest) {
        ensureLogin(appUserId);
        Long count = ticketStore.increment(SCAN_RATE_KEY + appUserId, Duration.ofMinutes(1));
        if (count != null && count > properties.getRateLimit().getScanPerUserPerMin()) {
            throw new BusinessException(ERR_RATE_LIMIT, "扫码过于频繁，请稍后再试");
        }

        QrTicket current = requireTicket(request.getTicket());
        if (current.getStatus().isFinal()) {
            throw new BusinessException(ERR_EXPIRED, "二维码已失效，请刷新");
        }

        if (current.getStatus() == QrLoginStatus.SCANNED) {
            if (!Objects.equals(current.getScannerUserId(), appUserId)) {
                throw new BusinessException(ERR_OCCUPIED, "二维码已被占用");
            }
            return buildDetail(current);
        }

        if (current.getStatus() != QrLoginStatus.WAITING) {
            throw new BusinessException(ERR_STATE, "二维码状态异常，请刷新");
        }

        String scannerIp = deviceInfoUtils.getClientIP(httpRequest);
        QrTicket updated = copy(current);
        updated.setStatus(QrLoginStatus.SCANNED);
        updated.setScannerUserId(appUserId);
        updated.setScannerIp(scannerIp);
        updated.setScannedAt(System.currentTimeMillis());
        updated.setAppVersion(request.getAppVersion());
        updated.setDeviceModel(request.getDeviceModel());

        if (!ticketStore.cas(current.getTicket(), QrLoginStatus.WAITING, updated)) {
            QrTicket latest = ticketStore.get(current.getTicket());
            if (latest == null) {
                recordInvalidTicket(scannerIp);
                throw new BusinessException(ERR_EXPIRED, "二维码已失效，请刷新");
            }
            if (latest.getStatus() == QrLoginStatus.SCANNED
                    && Objects.equals(latest.getScannerUserId(), appUserId)) {
                return buildDetail(latest);
            }
            throw new BusinessException(ERR_OCCUPIED, "二维码已被占用");
        }

        wsNotifier.push(updated.getTicket(), QrLoginStatus.SCANNED);
        return buildDetail(updated);
    }

    @Override
    public QrScanDetailVO detail(String ticket, Long appUserId) {
        ensureLogin(appUserId);
        QrTicket current = requireTicket(ticket);
        if (current.getStatus().isFinal()) {
            throw new BusinessException(ERR_EXPIRED, "二维码已失效，请刷新");
        }
        if (current.getScannerUserId() != null && !Objects.equals(current.getScannerUserId(), appUserId)) {
            throw new BusinessException(ERR_OCCUPIED, "二维码已被占用");
        }
        return buildDetail(current);
    }

    @Override
    public QrStatusVO confirm(String ticket, Long appUserId) {
        ensureLogin(appUserId);
        QrTicket current = requireTicket(ticket);

        if (current.getStatus() == QrLoginStatus.CONSUMED) {
            throw new BusinessException(ERR_USED, "二维码已被使用，请重新生成");
        }
        if (current.getStatus() == QrLoginStatus.CANCELED || current.getStatus() == QrLoginStatus.EXPIRED) {
            throw new BusinessException(ERR_EXPIRED, "二维码已失效，请刷新");
        }
        if (current.getStatus() != QrLoginStatus.SCANNED) {
            throw new BusinessException(ERR_STATE, "请先扫码后再确认登录");
        }
        if (!Objects.equals(current.getScannerUserId(), appUserId)) {
            throw new BusinessException(ERR_FORBIDDEN, "非本人扫码，无权确认");
        }

        LoginUser loginUser = permissionService.loadUserAuth(appUserId);
        if (loginUser == null) {
            throw new BusinessException(401, "登录状态已失效，请重新登录");
        }
        User user = userMapper.selectById(appUserId);

        QrTicket updated = copy(current);
        updated.setStatus(QrLoginStatus.CONFIRMED);
        updated.setWebToken(jwtUtil.generateToken(appUserId, loginUser.getUsername()));
        updated.setWebUserInfo(buildUserInfo(user, loginUser));
        updated.setConfirmedAt(System.currentTimeMillis());

        if (!ticketStore.cas(ticket, QrLoginStatus.SCANNED, updated)) {
            throw new BusinessException(ERR_STATE, "二维码状态已变化，请刷新");
        }

        // CONFIRMED 状态独立短 TTL，防止 Web 已关闭时 ticket 长期可被消费
        ticketStore.expire(ticket, Duration.ofSeconds(properties.getConfirmedTtlSeconds()));
        wsNotifier.push(ticket, QrLoginStatus.CONFIRMED);
        recordLoginLog(updated, user, loginUser);
        return statusVO(ticket, QrLoginStatus.CONFIRMED, updated.getExpireAt());
    }

    @Override
    public QrStatusVO cancel(String ticket, Long appUserId) {
        ensureLogin(appUserId);
        QrTicket current = requireTicket(ticket);

        if (current.getStatus() == QrLoginStatus.CANCELED) {
            return statusVO(ticket, QrLoginStatus.CANCELED, current.getExpireAt());
        }
        if (current.getStatus() == QrLoginStatus.CONSUMED
                || current.getStatus() == QrLoginStatus.CONFIRMED) {
            throw new BusinessException(ERR_STATE, "登录已确认，无法取消");
        }
        if (current.getStatus() == QrLoginStatus.WAITING) {
            throw new BusinessException(ERR_STATE, "二维码尚未扫描");
        }
        if (!Objects.equals(current.getScannerUserId(), appUserId)) {
            throw new BusinessException(ERR_FORBIDDEN, "非本人扫码，无权取消");
        }

        QrTicket updated = copy(current);
        updated.setStatus(QrLoginStatus.CANCELED);
        updated.setCanceledAt(System.currentTimeMillis());

        if (!ticketStore.cas(ticket, QrLoginStatus.SCANNED, updated)) {
            throw new BusinessException(ERR_STATE, "二维码状态已变化，请刷新");
        }

        wsNotifier.push(ticket, QrLoginStatus.CANCELED);
        return statusVO(ticket, QrLoginStatus.CANCELED, updated.getExpireAt());
    }

    @Override
    public QrConsumeVO consume(String ticket) {
        ConsumeResult result = ticketStore.consumeConfirmed(ticket);
        if (result.getStatus() == ConsumeStatus.MISSING) {
            if (ticketStore.isUsed(ticket)) {
                throw new BusinessException(ERR_USED, "二维码已被使用，请重新生成");
            }
            throw new BusinessException(ERR_NOT_FOUND, "二维码已失效，请刷新");
        }
        if (result.getStatus() == ConsumeStatus.NOT_CONFIRMED) {
            throw new BusinessException(ERR_STATE, "尚未确认登录");
        }

        QrTicket data = result.getTicket();
        ticketStore.markUsed(ticket, USED_TTL);
        wsNotifier.push(ticket, QrLoginStatus.CONSUMED);

        LoginUser loginUser = permissionService.loadUserAuth(data.getScannerUserId());
        if (loginUser == null) {
            throw new BusinessException(401, "登录状态已失效，请重新登录");
        }

        QrConsumeVO vo = new QrConsumeVO();
        vo.setToken(data.getWebToken());
        vo.setUserInfo(data.getWebUserInfo());
        vo.setRoles(new ArrayList<>(loginUser.getRoleCodes()));
        vo.setPermissions(new ArrayList<>(loginUser.getPermissions()));
        vo.setMenus(permissionService.getMenus(data.getScannerUserId()));
        vo.setDataScope(loginUser.getDataScope());
        vo.setSuperAdmin(loginUser.isSuperAdmin());
        return vo;
    }

    private QrTicket requireTicket(String ticket) {
        QrTicket data = ticketStore.get(ticket);
        if (data == null) {
            if (ticketStore.isUsed(ticket)) {
                throw new BusinessException(ERR_USED, "二维码已被使用，请重新生成");
            }
            throw new BusinessException(ERR_NOT_FOUND, "二维码已失效，请刷新");
        }
        return data;
    }

    private void ensureEnabled() {
        if (!properties.isEnabled()) {
            throw new BusinessException(ERR_DISABLED, "暂不支持扫码登录");
        }
    }

    private void ensureLogin(Long userId) {
        if (userId == null) {
            throw new BusinessException(401, "请先在 APP 中登录");
        }
    }

    private QrScanDetailVO buildDetail(QrTicket data) {
        QrWebClientVO webClient = new QrWebClientVO();
        webClient.setIp(data.getGenIp());
        webClient.setRegion(data.getGenRegion());
        webClient.setUserAgent(data.getGenUserAgent());
        webClient.setBrowser(data.getGenBrowser());
        webClient.setOs(data.getGenOs());
        webClient.setGenAt(data.getGenAt());

        QrScanDetailVO vo = new QrScanDetailVO();
        vo.setWebClient(webClient);
        vo.setExpireAt(data.getExpireAt());
        return vo;
    }

    private QrStatusVO statusVO(String ticket, QrLoginStatus status, Long expireAt) {
        QrStatusVO vo = new QrStatusVO();
        vo.setTicket(ticket);
        vo.setStatus(status);
        vo.setExpireAt(expireAt);
        return vo;
    }

    private QrUserInfoVO buildUserInfo(User user, LoginUser loginUser) {
        QrUserInfoVO info = new QrUserInfoVO();
        info.setUserId(loginUser.getUserId());
        info.setUsername(loginUser.getUsername());
        info.setNickname(user != null ? user.getNickname() : loginUser.getNickname());
        info.setAccount(user != null ? user.getAccount() : loginUser.getUsername());
        if (user != null) {
            info.setAvatar(user.getAvatar());
            info.setEmail(user.getEmail());
            info.setPhone(user.getPhone());
            info.setSignature(user.getSignature());
            info.setExperience(user.getExperience());
        }
        return info;
    }

    private void recordLoginLog(QrTicket data, User user, LoginUser loginUser) {
        try {
            LoginLog loginLog = new LoginLog();
            loginLog.setUserId(loginUser.getUserId());
            if (user != null) {
                loginLog.setUsername(user.getUsername());
                loginLog.setAccount(user.getAccount());
            } else {
                loginLog.setUsername(loginUser.getUsername());
                loginLog.setAccount(loginUser.getUsername());
            }
            loginLog.setIpaddr(data.getGenIp());
            loginLog.setLoginLocation(data.getGenRegion());
            loginLog.setBrowser(data.getGenBrowser());
            loginLog.setOs(data.getGenOs());
            loginLog.setStatus("1");
            loginLog.setMsg("扫码登录（APP 确认）");
            loginLog.setLoginTime(LocalDateTime.now());
            loginLogService.save(loginLog);
        } catch (Exception e) {
            log.warn("记录扫码登录日志失败: {}", e.getMessage());
        }
    }

    private void recordInvalidTicket(String ip) {
        if (ip == null || ip.isBlank()) {
            return;
        }
        Long count = ticketStore.increment(INVALID_RATE_KEY + ip, INVALID_WINDOW);
        if (count != null && count > INVALID_LIMIT) {
            ticketStore.block(ip, BLOCK_TTL);
        }
    }

    private String buildQrContent(String ticket) {
        String protocol = properties.getQrProtocol();
        if (protocol == null || protocol.isBlank()) {
            protocol = "minecraft://qrLogin";
        }
        return protocol + (protocol.contains("?") ? "&" : "?") + "t=" + ticket;
    }

    private String randomTicket() {
        return UUID.randomUUID().toString().replace("-", "") + RandomStringUtils.randomAlphanumeric(16);
    }

    private QrTicket copy(QrTicket source) {
        return JSON.parseObject(JSON.toJSONString(source), QrTicket.class);
    }
}
