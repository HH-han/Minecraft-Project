package com.minecraft.controller;

import com.minecraft.dto.request.CaptchaRecordQuery;
import com.minecraft.dto.response.ApiResponse;
import com.minecraft.dto.response.CaptchaRecordVO;
import com.minecraft.dto.response.CaptchaStatisticsVO;
import com.minecraft.dto.response.PageResponse;
import com.minecraft.recommendation.service.AdminAuthorizationService;
import com.minecraft.service.CaptchaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 滑块验证码管理端接口（管理员，permissions='0'）
 */
@Tag(name = "滑块验证管理")
@RestController
@RequestMapping("/api/admin/captcha")
@RequiredArgsConstructor
public class CaptchaAdminController {

    private final CaptchaService captchaService;
    private final AdminAuthorizationService adminAuthorizationService;

    @Operation(summary = "分页查询滑块验证记录")
    @GetMapping("/records")
    public ApiResponse<PageResponse<CaptchaRecordVO>> pageRecords(@Valid CaptchaRecordQuery query) {
        adminAuthorizationService.requireAdmin();
        return ApiResponse.success(captchaService.pageRecords(query));
    }

    @Operation(summary = "查询滑块验证数据概览")
    @GetMapping("/statistics")
    public ApiResponse<CaptchaStatisticsVO> statistics() {
        adminAuthorizationService.requireAdmin();
        return ApiResponse.success(captchaService.statistics());
    }
}
