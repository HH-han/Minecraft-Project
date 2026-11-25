package com.minecraft.vo;

import lombok.Data;

/**
 * APP 扫码后返回的 Web 端设备信息。
 */
@Data
public class QrScanDetailVO {

    private QrWebClientVO webClient;

    private Long expireAt;
}
