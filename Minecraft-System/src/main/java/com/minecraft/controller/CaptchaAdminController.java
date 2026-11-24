package com.minecraft.controller;

import com.minecraft.dto.request.CaptchaImageQuery;
import com.minecraft.dto.request.CaptchaRecordQuery;
import com.minecraft.dto.response.ApiResponse;
import com.minecraft.dto.response.CaptchaImageVO;
import com.minecraft.dto.response.CaptchaRecordVO;
import com.minecraft.dto.response.CaptchaStatisticsVO;
import com.minecraft.dto.response.PageResponse;
import com.minecraft.recommendation.service.AdminAuthorizationService;
import com.minecraft.service.CaptchaImageService;
import com.minecraft.service.CaptchaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 滑块验证码管理端接口（管理员，permissions='0'）
 */
@Tag(name = "滑块验证管理")
@RestController
@RequestMapping("/api/admin/captcha")
@RequiredArgsConstructor
public class CaptchaAdminController {

    private final CaptchaService captchaService;
    private final CaptchaImageService captchaImageService;
    private final AdminAuthorizationService adminAuthorizationService;

    @Operation(summary = "分页查询滑块验证记录")
    @GetMapping("/records")
    @PreAuthorize("@perm.has('sys:captcha:list')")
    public ApiResponse<PageResponse<CaptchaRecordVO>> pageRecords(@Valid CaptchaRecordQuery query) {
        adminAuthorizationService.requireAdmin();
        return ApiResponse.success(captchaService.pageRecords(query));
    }

    @Operation(summary = "查询滑块验证数据概览")
    @GetMapping("/statistics")
    @PreAuthorize("@perm.has('sys:captcha:list')")
    public ApiResponse<CaptchaStatisticsVO> statistics() {
        adminAuthorizationService.requireAdmin();
        return ApiResponse.success(captchaService.statistics());
    }

    @Operation(summary = "分页查询滑块验证码图片")
    @GetMapping("/images")
    @PreAuthorize("@perm.has('sys:captcha:list')")
    public ApiResponse<PageResponse<CaptchaImageVO>> pageImages(@Valid CaptchaImageQuery query) {
        adminAuthorizationService.requireAdmin();
        return ApiResponse.success(captchaImageService.pageImages(query));
    }

    @Operation(summary = "上传滑块验证码图片（JPG/PNG，最大 5MB）")
    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@perm.has('sys:captcha:add')")
    public ApiResponse<CaptchaImageVO> uploadImage(@RequestParam("file") MultipartFile file) {
        adminAuthorizationService.requireAdmin();
        return ApiResponse.success("上传成功", captchaImageService.upload(file));
    }

    @Operation(summary = "删除滑块验证码图片")
    @DeleteMapping("/images/{id}")
    @PreAuthorize("@perm.has('sys:captcha:delete')")
    public ApiResponse<Void> deleteImage(@PathVariable String id) {
        adminAuthorizationService.requireAdmin();
        captchaImageService.delete(id);
        return ApiResponse.success("删除成功", null);
    }
}
