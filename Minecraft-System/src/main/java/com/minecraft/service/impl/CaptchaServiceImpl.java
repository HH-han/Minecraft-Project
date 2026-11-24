package com.minecraft.service.impl;

import cloud.tianai.captcha.application.ImageCaptchaApplication;
import cloud.tianai.captcha.application.vo.ImageCaptchaVO;
import cloud.tianai.captcha.common.AnyMap;
import cloud.tianai.captcha.common.constant.CaptchaTypeConstant;
import cloud.tianai.captcha.common.response.ApiResponse;
import cloud.tianai.captcha.validator.common.model.dto.ImageCaptchaTrack;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minecraft.dto.request.CaptchaRecordQuery;
import com.minecraft.dto.response.CaptchaRecordVO;
import com.minecraft.dto.response.CaptchaResponse;
import com.minecraft.dto.response.CaptchaStatisticsVO;
import com.minecraft.dto.response.PageResponse;
import com.minecraft.dto.response.VerifyResult;
import com.minecraft.entity.VerifyRecord;
import com.minecraft.mapper.VerifyRecordMapper;
import com.minecraft.service.CaptchaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class CaptchaServiceImpl implements CaptchaService {

    private static final Logger logger = LoggerFactory.getLogger(CaptchaServiceImpl.class);

    /** captcha_verify_record.image_id 字段长度 */
    private static final int IMAGE_ID_MAX_LENGTH = 36;
    /** captcha_verify_record.trace_id 字段长度 */
    private static final int TRACE_ID_MAX_LENGTH = 64;
    /** captcha_verify_record.client_ip 字段长度 */
    private static final int CLIENT_IP_MAX_LENGTH = 45;
    /** 天爱验证码缓存中保存目标位置百分比的 key */
    private static final String CACHE_PERCENTAGE_KEY = "percentage";
    private static final String UNKNOWN = "unknown";

    @Autowired
    private ImageCaptchaApplication imageCaptchaApplication;

    @Autowired
    private VerifyRecordMapper recordMapper;

    /** 天爱验证码缓存前缀，需与 captcha.prefix 配置保持一致 */
    @Value("${captcha.prefix:captcha}")
    private String captchaCachePrefix;

    @Override
    public String generateAndCacheImage() {
        ApiResponse<ImageCaptchaVO> response = imageCaptchaApplication.generateCaptcha(CaptchaTypeConstant.SLIDER);
        if (response.isSuccess()) {
            return response.getData().getId();
        }
        throw new RuntimeException("Failed to generate captcha");
    }

    @Override
    public CaptchaResponse getCaptcha(String traceId) {
        try {
            logger.info("Generating captcha for traceId: {}", traceId);

            ApiResponse<ImageCaptchaVO> response = imageCaptchaApplication.generateCaptcha(CaptchaTypeConstant.SLIDER);

            if (!response.isSuccess()) {
                throw new RuntimeException("Failed to generate captcha: " + response.getMsg());
            }

            ImageCaptchaVO captchaVO = response.getData();

            CaptchaResponse captchaResponse = CaptchaResponse.builder()
                    .traceId(traceId)
                    .imageId(captchaVO.getId())
                    .imageData(captchaVO.getBackgroundImage())
                    .sliderImage(captchaVO.getTemplateImage())
                    .type(captchaVO.getType())
                    .build();

            logger.info("Successfully generated captcha for traceId: {}, imageId: {}", traceId, captchaVO.getId());
            return captchaResponse;

        } catch (Exception e) {
            logger.error("Failed to generate captcha for traceId: {}", traceId, e);
            throw new RuntimeException("获取验证码失败", e);
        }
    }

    @Override
    public VerifyResult verify(String traceId, Integer sliderPosition, String userAgent, String clientIp) {
        ImageCaptchaTrack track = new ImageCaptchaTrack();
        track.setLeft(sliderPosition);
        track.setTop(0);
        return verify(traceId, track, userAgent, clientIp);
    }

    @Override
    public VerifyResult verify(String captchaId, ImageCaptchaTrack track, String userAgent, String clientIp) {
        long startTime = System.currentTimeMillis();
        ImageCaptchaTrack safeTrack = track == null ? new ImageCaptchaTrack() : track;

        if (!StringUtils.hasText(captchaId)) {
            persistRecord(captchaId, safeTrack.getLeft(), safeTrack.getLeft(), false, startTime, userAgent, clientIp);
            return VerifyResult.fail("验证会话已失效，请刷新验证码后重试");
        }

        // 目标位置需在 matching 之前读取：matching 会移除验证码缓存
        Integer targetOffset = resolveTargetOffset(captchaId, safeTrack);

        boolean passed;
        String failMessage = "验证失败，请重试";
        try {
            ApiResponse<?> response = imageCaptchaApplication.matching(captchaId, safeTrack);
            passed = response != null && response.isSuccess();
            if (!passed && response != null && StringUtils.hasText(response.getMsg())) {
                failMessage = response.getMsg();
            }
        } catch (Exception e) {
            logger.error("Failed to verify captcha for id: {}", captchaId, e);
            passed = false;
            failMessage = "验证服务异常";
        }

        persistRecord(captchaId, safeTrack.getLeft(), targetOffset, passed, startTime, userAgent, clientIp);

        if (passed) {
            logger.info("Captcha verification passed for id: {}", captchaId);
            return VerifyResult.builder().passed(true).message("验证成功").build();
        }
        logger.warn("Captcha verification failed for id: {}", captchaId);
        return VerifyResult.fail(failMessage);
    }

    @Override
    public PageResponse<CaptchaRecordVO> pageRecords(CaptchaRecordQuery query) {
        int pageNum = query.getPage() == null ? 1 : query.getPage();
        int pageSize = query.getSize() == null ? 10 : query.getSize();

        LambdaQueryWrapper<VerifyRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(query.getPassed() != null, VerifyRecord::getIsPassed, query.getPassed());
        wrapper.ge(query.getStartTime() != null, VerifyRecord::getCreateTime, query.getStartTime());
        wrapper.le(query.getEndTime() != null, VerifyRecord::getCreateTime, query.getEndTime());
        if (StringUtils.hasText(query.getKeyword())) {
            String keyword = query.getKeyword().trim();
            wrapper.and(w -> w.like(VerifyRecord::getTraceId, keyword)
                    .or().like(VerifyRecord::getImageId, keyword)
                    .or().like(VerifyRecord::getClientIp, keyword));
        }
        wrapper.orderByDesc(VerifyRecord::getId);

        Page<VerifyRecord> page = recordMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<CaptchaRecordVO> records = page.getRecords().stream().map(this::toVO).toList();
        return new PageResponse<>(records, page.getTotal(), pageNum, pageSize);
    }

    @Override
    public CaptchaStatisticsVO statistics() {
        CaptchaStatisticsVO statistics = recordMapper.selectStatistics();
        if (statistics == null) {
            statistics = new CaptchaStatisticsVO();
        }
        long total = statistics.getTotal() == null ? 0L : statistics.getTotal();
        long passed = statistics.getPassed() == null ? 0L : statistics.getPassed();
        statistics.setTotal(total);
        statistics.setPassed(passed);
        statistics.setFailed(statistics.getFailed() == null ? 0L : statistics.getFailed());
        statistics.setTodayTotal(statistics.getTodayTotal() == null ? 0L : statistics.getTodayTotal());
        statistics.setTodayPassed(statistics.getTodayPassed() == null ? 0L : statistics.getTodayPassed());
        statistics.setTodayFailed(statistics.getTodayFailed() == null ? 0L : statistics.getTodayFailed());
        statistics.setAvgCostTime(statistics.getAvgCostTime() == null ? 0D : statistics.getAvgCostTime());
        statistics.setPassRate(total == 0 ? 0D : Math.round(passed * 10000D / total) / 100D);
        return statistics;
    }

    @Override
    public String getImageFromPool() {
        return "";
    }

    @Override
    public void cacheSession(String traceId, com.minecraft.dto.request.CaptchaSession session) {
    }

    @Override
    public com.minecraft.dto.request.CaptchaSession getAndRemoveSession(String traceId) {
        return null;
    }

    @Override
    public com.minecraft.entity.CaptchaImage getImage(String imageId) {
        return null;
    }

    @Override
    public com.minecraft.entity.PuzzleConfig getPuzzleConfig(String imageId, Integer version) {
        return null;
    }

    /**
     * 从验证码缓存中解析目标偏移量（百分比 * 背景图宽度），失败时退化为用户拖动位置。
     */
    private Integer resolveTargetOffset(String captchaId, ImageCaptchaTrack track) {
        try {
            String cacheKey = captchaCachePrefix + ":" + captchaId;
            AnyMap cache = imageCaptchaApplication.getCacheStore().getCache(cacheKey);
            Float percentage = cache == null ? null : cache.getFloat(CACHE_PERCENTAGE_KEY);
            Integer backgroundWidth = track.getBgImageWidth();
            if (percentage != null && backgroundWidth != null && backgroundWidth > 0) {
                return Math.round(percentage * backgroundWidth);
            }
        } catch (Exception e) {
            logger.debug("Failed to resolve target offset for captcha id: {}", captchaId, e);
        }
        return track.getLeft();
    }

    /**
     * 持久化验证记录，异常仅记录日志，不影响验证码校验结果。
     */
    private void persistRecord(String captchaId, Integer sliderOffset, Integer targetOffset,
                               boolean passed, long startTime, String userAgent, String clientIp) {
        try {
            VerifyRecord record = new VerifyRecord();
            record.setTraceId(truncate(StringUtils.hasText(captchaId) ? captchaId : UNKNOWN, TRACE_ID_MAX_LENGTH));
            record.setImageId(resolveImageId(captchaId));
            record.setClientIp(truncate(StringUtils.hasText(clientIp) ? clientIp : UNKNOWN, CLIENT_IP_MAX_LENGTH));
            record.setUserAgent(userAgent);
            record.setSliderOffset(sliderOffset == null ? 0 : sliderOffset);
            record.setTargetOffset(targetOffset == null ? (sliderOffset == null ? 0 : sliderOffset) : targetOffset);
            record.setIsPassed(passed ? 1 : 0);
            record.setCostTime((int) Math.max(0, System.currentTimeMillis() - startTime));
            recordMapper.insert(record);
        } catch (Exception e) {
            logger.error("Failed to persist captcha verify record, captchaId: {}", captchaId, e);
        }
    }

    /**
     * 天爱验证码 id 形如 SLIDER_xxx，image_id 字段长度有限，优先截取 UUID 部分。
     */
    private String resolveImageId(String captchaId) {
        if (!StringUtils.hasText(captchaId)) {
            return UNKNOWN;
        }
        int separatorIndex = captchaId.indexOf('_');
        String candidate = separatorIndex >= 0 && separatorIndex < captchaId.length() - 1
                ? captchaId.substring(separatorIndex + 1)
                : captchaId;
        return truncate(candidate, IMAGE_ID_MAX_LENGTH);
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() > maxLength ? value.substring(0, maxLength) : value;
    }

    private CaptchaRecordVO toVO(VerifyRecord record) {
        CaptchaRecordVO vo = new CaptchaRecordVO();
        vo.setId(record.getId());
        vo.setTraceId(record.getTraceId());
        vo.setImageId(record.getImageId());
        vo.setClientIp(record.getClientIp());
        vo.setUserAgent(record.getUserAgent());
        vo.setSliderOffset(record.getSliderOffset());
        vo.setTargetOffset(record.getTargetOffset());
        vo.setIsPassed(record.getIsPassed());
        vo.setCostTime(record.getCostTime());
        vo.setCreateTime(record.getCreateTime());
        return vo;
    }
}
