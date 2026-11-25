package com.minecraft.vo;

import lombok.Data;

/**
 * 扫码后展示给 APP 的 Web 端设备信息，用于用户确认是否本人操作。
 */
@Data
public class QrWebClientVO {

    private String ip;

    private String region;

    private String userAgent;

    private String browser;

    private String os;

    private Long genAt;
}
