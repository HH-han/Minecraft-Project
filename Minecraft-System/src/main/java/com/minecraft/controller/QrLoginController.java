package com.minecraft.controller;

import com.minecraft.dto.request.QrScanRequest;
import com.minecraft.dto.request.QrTicketRequest;
import com.minecraft.dto.response.ApiResponse;
import com.minecraft.exception.BusinessException;
import com.minecraft.service.QrLoginService;
import com.minecraft.utils.SecurityUtils;
import com.minecraft.vo.QrConsumeVO;
import com.minecraft.vo.QrGenerateVO;
import com.minecraft.vo.QrScanDetailVO;
import com.minecraft.vo.QrStatusVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * APP 扫码授权 Web 登录接口。
 *
 * <p>Web 端接口（generate / status / consume）公开访问，ticket 即凭据；
 * APP 端接口（scan / detail / confirm / cancel）必须携带 APP 登录态。</p>
 */
@Tag(name = "扫码登录")
@RestController
@RequestMapping("/api/auth/qr")
@RequiredArgsConstructor
public class QrLoginController {

    private final QrLoginService qrLoginService;

    @Operation(summary = "生成登录二维码（Web）")
    @PostMapping("/generate")
    public ApiResponse<QrGenerateVO> generate(HttpServletRequest request) {
        return ApiResponse.success(qrLoginService.generate(request));
    }

    @Operation(summary = "查询二维码状态（Web，WebSocket 兜底）")
    @GetMapping("/status/{ticket}")
    public ApiResponse<QrStatusVO> status(@PathVariable String ticket) {
        return ApiResponse.success(qrLoginService.status(ticket));
    }

    @Operation(summary = "消费 ticket 换取 JWT（Web）")
    @PostMapping("/consume")
    public ApiResponse<QrConsumeVO> consume(@Valid @RequestBody QrTicketRequest request) {
        return ApiResponse.success("登录成功", qrLoginService.consume(request.getTicket()));
    }

    @Operation(summary = "扫码后获取 Web 端设备信息（APP）")
    @GetMapping("/detail/{ticket}")
    public ApiResponse<QrScanDetailVO> detail(@PathVariable String ticket) {
        return ApiResponse.success(qrLoginService.detail(ticket, requireLogin()));
    }

    @Operation(summary = "标记已扫描（APP）")
    @PostMapping("/scan")
    public ApiResponse<QrScanDetailVO> scan(@Valid @RequestBody QrScanRequest request,
                                            HttpServletRequest httpRequest) {
        return ApiResponse.success(qrLoginService.scan(request, requireLogin(), httpRequest));
    }

    @Operation(summary = "确认登录（APP）")
    @PostMapping("/confirm")
    public ApiResponse<QrStatusVO> confirm(@Valid @RequestBody QrTicketRequest request) {
        return ApiResponse.success(qrLoginService.confirm(request.getTicket(), requireLogin()));
    }

    @Operation(summary = "取消登录（APP）")
    @PostMapping("/cancel")
    public ApiResponse<QrStatusVO> cancel(@Valid @RequestBody QrTicketRequest request) {
        return ApiResponse.success(qrLoginService.cancel(request.getTicket(), requireLogin()));
    }

    private Long requireLogin() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(401, "请先在 APP 中登录");
        }
        return userId;
    }
}
