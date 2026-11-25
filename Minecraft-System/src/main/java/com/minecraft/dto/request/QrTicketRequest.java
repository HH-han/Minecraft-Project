package com.minecraft.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 仅携带 ticket 的扫码登录请求（确认 / 取消 / 消费）。
 */
@Data
public class QrTicketRequest {

    @NotBlank(message = "ticket 不能为空")
    private String ticket;
}
