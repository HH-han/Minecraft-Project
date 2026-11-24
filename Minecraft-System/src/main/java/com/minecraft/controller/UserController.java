package com.minecraft.controller;

import jakarta.validation.Valid;

import com.minecraft.common.exception.RateLimitException;
import com.minecraft.dto.request.LoginRequest;
import com.minecraft.dto.response.ApiResponse;
import com.minecraft.dto.response.LoginResponse;
import com.minecraft.entity.User;
import com.minecraft.vo.UserAvatarVO;
import com.minecraft.vo.UserVO;
import org.springframework.beans.BeanUtils;
import com.minecraft.entity.PointsRecord;
import com.minecraft.mapper.PointsRecordMapper;
import com.minecraft.service.LoginLogService;
import com.minecraft.service.SysUserService;
import com.minecraft.service.UserService;
import com.minecraft.utils.DeviceInfoUtils;
import com.minecraft.utils.ImageUtils;
import com.minecraft.utils.RedisUtil;
import com.minecraft.utils.SecurityUtils;
import com.minecraft.vo.UserRoleRowVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Tag(name = "用户管理")
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private LoginLogService loginLogService;
    
    @Autowired
    private PointsRecordMapper pointsRecordMapper;

    @Autowired
    private ImageUtils imageUtils;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private DeviceInfoUtils deviceInfoUtils;

    @Autowired
    private RedisUtil redisUtil;

    /** 头像查询限流窗口（秒） */
    private static final long AVATAR_QUERY_WINDOW_SECONDS = 60L;
    /** 单 IP 单个窗口内允许的最大头像查询次数 */
    private static final long AVATAR_QUERY_MAX_PER_WINDOW = 30L;
    /** 账号合法字符：字母、数字、下划线、点、@、中划线，长度 1-64 */
    private static final Pattern ACCOUNT_PATTERN = Pattern.compile("^[A-Za-z0-9_.@-]{1,64}$");

    @Operation(summary = "上传图片")
    @PostMapping("/upload")
    public ApiResponse<String> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            return ApiResponse.success("上传成功", imageUtils.processMultipartFile(file));
        } catch (Exception e) {
            return ApiResponse.error("上传失败: " + e.getMessage());
        }
    }

    @Operation(summary ="获取用户信息")
    @GetMapping("/info")
    public ApiResponse<UserVO> getUserInfo() {
        Long userId = SecurityUtils.getCurrentUserId();
        UserVO vo = toUserVO(userService.getUserInfo(userId));
        com.minecraft.security.core.LoginUser loginUser = SecurityUtils.getLoginUser();
        if (vo != null && loginUser != null) {
            vo.setRoles(java.util.List.copyOf(loginUser.getRoleCodes()));
            vo.setSuperAdmin(loginUser.isSuperAdmin());
        }
        return ApiResponse.success(vo);
    }

    @Operation(summary ="更新用户信息")
    @PutMapping("/update")
    public ApiResponse<Void> updateUserInfo(@RequestBody User user) {
        userService.updateUserInfo(user);
        return ApiResponse.success("更新成功", null);
    }

    @Operation(summary ="修改密码")
    @PutMapping("/password")
    public ApiResponse<Void> updatePassword(@RequestParam String oldPassword, @RequestParam String newPassword) {
        Long userId = SecurityUtils.getCurrentUserId();
        userService.updatePassword(userId, oldPassword, newPassword);
        return ApiResponse.success("修改成功", null);
    }

    @Operation(summary ="上传头像")
    @PostMapping("/avatar")
    public ApiResponse<String> uploadAvatar(@RequestParam("avatar") MultipartFile file) {
        Long userId = SecurityUtils.getCurrentUserId();
        String avatarUrl = userService.uploadAvatar(userId, file);
        return ApiResponse.success(avatarUrl);
    }

    @Operation(summary ="删除用户")
    @DeleteMapping("/{id}")
    @PreAuthorize("@perm.has('sys:user:delete')")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ApiResponse.success("删除成功", null);
    }

    @Operation(summary ="获取所有用户")
    @GetMapping("/list")
    @PreAuthorize("@perm.has('sys:user:list')")
    public ApiResponse<List<UserVO>> getAllUsers(@RequestParam(required = false) Integer limit) {
        List<User> users = userService.getAllUsers(limit);
        java.util.Map<Long, List<UserRoleRowVO>> roleMap = sysUserService.getRolesByUserIds(
                users.stream().map(User::getId).toList());
        List<UserVO> result = users.stream().map(user -> {
            UserVO vo = toUserVO(user);
            List<UserRoleRowVO> roles = roleMap.getOrDefault(user.getId(), List.of());
            vo.setRoles(roles.stream().map(UserRoleRowVO::getRoleCode).toList());
            vo.setRoleNames(roles.stream().map(UserRoleRowVO::getRoleName).toList());
            return vo;
        }).collect(Collectors.toList());
        return ApiResponse.success(result);
    }

    @Operation(summary ="根据账号获取用户信息")
    @GetMapping("/by-account")
    @PreAuthorize("@perm.hasAny('sys:user:query','sys:user:add','sys:user:edit')")
    public ApiResponse<UserVO> getUserByAccount(@RequestParam String account) {
        return ApiResponse.success(toUserVO(userService.getUserByAccount(account)));
    }

    /**
     * 公开头像查询：登录页在未登录状态下回显头像使用。
     * <p>安全措施：</p>
     * <ul>
     *   <li>只返回头像字段，不返回账号、手机号、邮箱等任何其他用户信息；</li>
     *   <li>账号不存在、无头像或参数非法时统一返回空字符串，不暴露账号是否存在；</li>
     *   <li>账号格式与长度白名单校验，防止 SQL 注入 / 超长参数；</li>
     *   <li>按客户端 IP 做固定窗口限流，防止被批量枚举账号；</li>
     *   <li>响应标记为私有缓存，避免中间代理共享缓存。</li>
     * </ul>
     */
    @Operation(summary ="根据账号查询用户头像（公开接口，仅返回头像）")
    @GetMapping("/avatar")
    public ApiResponse<UserAvatarVO> getUserAvatar(@RequestParam(required = false) String account,
                                                   HttpServletRequest request,
                                                   HttpServletResponse response) {
        enforceAvatarQueryRateLimit(request);

        String avatar = "";
        String normalizedAccount = account == null ? "" : account.trim();
        if (ACCOUNT_PATTERN.matcher(normalizedAccount).matches()) {
            User user = userService.getUserByAccount(normalizedAccount);
            if (user != null && user.getAvatar() != null) {
                avatar = user.getAvatar().trim();
            }
        }

        // 头像为低敏感数据，可短时缓存；private 防止被代理/CDN 共享
        response.setHeader("Cache-Control", "private, max-age=300");
        return ApiResponse.success(new UserAvatarVO(avatar));
    }

    /**
     * 按 IP 对头像查询做固定窗口限流。
     * 限流组件异常时放行（头像仅用于展示），避免基础设施抖动影响登录页可用性。
     */
    private void enforceAvatarQueryRateLimit(HttpServletRequest request) {
        String ip = deviceInfoUtils.getClientIP(request);
        String key = "security:user:avatar:rate:" + (ip == null ? "unknown" : ip);
        try {
            Long count = redisUtil.increment(key, 1L);
            if (count != null && count == 1L) {
                redisUtil.expire(key, AVATAR_QUERY_WINDOW_SECONDS, TimeUnit.SECONDS);
            }
            if (count != null && count > AVATAR_QUERY_MAX_PER_WINDOW) {
                throw new RateLimitException("头像查询过于频繁，请稍后再试");
            }
        } catch (RateLimitException e) {
            throw e;
        } catch (Exception e) {
            log.warn("头像查询限流检查失败，已放行：{}", e.getMessage());
        }
    }

    /**
     * 实体转脱敏 VO，避免密码等敏感字段返回前端
     */
    private UserVO toUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        vo.setUserId(user.getId());
        vo.setRealName(user.getNickname() != null && !user.getNickname().isBlank()
                ? user.getNickname()
                : user.getUsername());
        vo.setSuperAdmin(user.getSuperAdmin() != null && user.getSuperAdmin() == 1);
        return vo;
    }

    @Operation(summary ="退出登录")
    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        Long userId = SecurityUtils.getCurrentUserId();
        userService.updateOnlineStatus(userId, false);
        // 清除用户的登录日志
        loginLogService.clearLoginLogs(userId);
        return ApiResponse.success("退出成功", null);
    }

    @Operation(summary ="管理员登录")
    @PostMapping("/admin/login")
    public ApiResponse<LoginResponse> adminLogin(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        LoginResponse response = userService.adminLogin(request, httpRequest);
        return ApiResponse.success(response);
    }

    @Operation(summary ="获取用户总数")
    @GetMapping("/count")
    @PreAuthorize("@perm.has('sys:user:list')")
    public ApiResponse<Long> getUserCount() {
        long count = userService.getUserCount();
        return ApiResponse.success(count);
    }
    
    @Operation(summary ="获取积分记录")
    @GetMapping("/points/records")
    public ApiResponse<List<PointsRecord>> getPointsRecords() {
        Long userId = SecurityUtils.getCurrentUserId();
        
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PointsRecord> wrapper =
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(PointsRecord::getUserId, userId);
        wrapper.orderByDesc(PointsRecord::getCreateTime);
        wrapper.last("limit 50");
        
        List<PointsRecord> records = pointsRecordMapper.selectList(wrapper);
        return ApiResponse.success(records);
    }
    
    @Operation(summary ="获取用户积分")
    @GetMapping("/points")
    public ApiResponse<Integer> getUserPoints() {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userService.getUserInfo(userId);
        return ApiResponse.success(user.getPoints());
    }
}
