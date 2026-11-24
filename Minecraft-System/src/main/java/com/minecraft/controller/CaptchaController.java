package com.minecraft.controller;

import jakarta.validation.Valid;

import cloud.tianai.captcha.application.ImageCaptchaApplication;
import cloud.tianai.captcha.application.vo.ImageCaptchaVO;
import cloud.tianai.captcha.common.constant.CaptchaTypeConstant;
import cloud.tianai.captcha.common.response.ApiResponse;
import cloud.tianai.captcha.validator.common.model.dto.ImageCaptchaTrack;
import com.minecraft.dto.request.VerifyRequest;
import com.minecraft.dto.response.CaptchaResponse;
import com.minecraft.dto.response.VerifyResult;
import com.minecraft.service.CaptchaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@Tag(name = "验证码管理")
@RestController
@RequestMapping("/api/captcha")
public class CaptchaController {

    private static final Logger logger = LoggerFactory.getLogger(CaptchaController.class);

    @Autowired
    private ImageCaptchaApplication imageCaptchaApplication;

    @Autowired
    private CaptchaService captchaService;

    @Operation(summary = "获取滑块验证码")
    @GetMapping("/get")
    public com.minecraft.dto.response.ApiResponse<CaptchaResponse> getCaptcha(@RequestParam String traceId) {
        try {
            return com.minecraft.dto.response.ApiResponse.success("获取成功", captchaService.getCaptcha(traceId));
        } catch (Exception e) {
            logger.error("Failed to generate captcha for traceId: {}", traceId, e);
            return com.minecraft.dto.response.ApiResponse.error("获取验证码失败");
        }
    }

    @Operation(summary = "验证滑块位置")
    @PostMapping("/verify")
    public com.minecraft.dto.response.ApiResponse<VerifyResult> verify(@Valid @RequestBody VerifyRequest request,
                                                                      HttpServletRequest httpRequest) {
        try {
            VerifyResult result = captchaService.verify(
                    request.getTraceId(),
                    request.getSliderPosition(),
                    resolveUserAgent(request.getUserAgent(), httpRequest),
                    getClientIP(httpRequest));
            return com.minecraft.dto.response.ApiResponse.success(
                    Boolean.TRUE.equals(result.getPassed()) ? "验证成功" : "验证失败", result);
        } catch (Exception e) {
            logger.error("Failed to verify captcha for traceId: {}", request.getTraceId(), e);
            return com.minecraft.dto.response.ApiResponse.error("验证服务异常");
        }
    }

    @Operation(summary = "TIANAI-CAPTCHA标准生成接口")
    @PostMapping("/gen")
    public ApiResponse<ImageCaptchaVO> genCaptcha() {
        return imageCaptchaApplication.generateCaptcha(CaptchaTypeConstant.SLIDER);
    }

    @Operation(summary = "TIANAI-CAPTCHA标准验证接口（成功后持久化验证记录）")
    @PostMapping("/check")
    public ApiResponse<?> checkCaptcha(@RequestBody VerifyData data, HttpServletRequest httpRequest) {
        VerifyResult result = captchaService.verify(
                data.getId(),
                data.getData(),
                httpRequest.getHeader("User-Agent"),
                getClientIP(httpRequest));
        if (Boolean.TRUE.equals(result.getPassed())) {
            return ApiResponse.ofSuccess(Collections.singletonMap("id", data.getId()));
        }
        return ApiResponse.ofError(result.getMessage());
    }

    private String resolveUserAgent(String userAgent, HttpServletRequest request) {
        return userAgent != null && !userAgent.isBlank() ? userAgent : request.getHeader("User-Agent");
    }

    private String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    @lombok.Data
    public static class VerifyData {
        private String id;
        private ImageCaptchaTrack data;
    }
}
