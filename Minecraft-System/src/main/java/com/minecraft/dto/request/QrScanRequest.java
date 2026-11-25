package com.minecraft.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * APP 扫码请求。
 */
@Data
public class QrScanRequest {

    @NotBlank(message = "ticket 不能为空")
    private String ticket;

    private String appVersion;

    private String deviceModel;
}
