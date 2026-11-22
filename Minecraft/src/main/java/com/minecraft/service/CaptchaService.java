package com.minecraft.service;

import cloud.tianai.captcha.validator.common.model.dto.ImageCaptchaTrack;
import com.minecraft.dto.request.CaptchaRecordQuery;
import com.minecraft.dto.request.CaptchaSession;
import com.minecraft.dto.response.CaptchaRecordVO;
import com.minecraft.dto.response.CaptchaResponse;
import com.minecraft.dto.response.CaptchaStatisticsVO;
import com.minecraft.dto.response.PageResponse;
import com.minecraft.dto.response.VerifyResult;
import com.minecraft.entity.CaptchaImage;
import com.minecraft.entity.PuzzleConfig;

public interface CaptchaService {

    String generateAndCacheImage();

    CaptchaImage getImage(String imageId);

    PuzzleConfig getPuzzleConfig(String imageId, Integer version);

    void cacheSession(String traceId, CaptchaSession session);

    CaptchaSession getAndRemoveSession(String traceId);

    CaptchaResponse getCaptcha(String traceId);

    VerifyResult verify(String traceId, Integer sliderPosition, String userAgent, String clientIp);

    /**
     * 校验滑块验证码并将验证记录持久化到数据库
     *
     * @param captchaId  验证码ID（天爱验证码生成的 id）
     * @param track      用户拖动轨迹，至少包含 left 与背景图宽度
     * @param userAgent  客户端 User-Agent
     * @param clientIp   客户端 IP
     */
    VerifyResult verify(String captchaId, ImageCaptchaTrack track, String userAgent, String clientIp);

    /**
     * 分页查询滑块验证记录（管理端）
     */
    PageResponse<CaptchaRecordVO> pageRecords(CaptchaRecordQuery query);

    /**
     * 查询滑块验证数据概览（管理端）
     */
    CaptchaStatisticsVO statistics();

    String getImageFromPool();
}
