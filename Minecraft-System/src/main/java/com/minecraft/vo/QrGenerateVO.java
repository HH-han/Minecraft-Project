package com.minecraft.vo;

import lombok.Data;

/**
 * 生成扫码登录二维码的响应。
 */
@Data
public class QrGenerateVO {

    private String ticket;

    /** 二维码承载内容，例如 minecraft://qrLogin?t=xxx */
    private String qrContent;

    private Long expireAt;

    /** 有效期（秒） */
    private Long ttl;
}
