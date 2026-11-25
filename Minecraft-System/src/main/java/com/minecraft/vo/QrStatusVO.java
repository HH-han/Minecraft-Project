package com.minecraft.vo;

import com.minecraft.enums.QrLoginStatus;
import lombok.Data;

/**
 * 二维码状态轮询响应。
 */
@Data
public class QrStatusVO {

    private String ticket;

    private QrLoginStatus status;

    private Long expireAt;
}
