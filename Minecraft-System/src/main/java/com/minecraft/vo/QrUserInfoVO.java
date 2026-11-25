package com.minecraft.vo;

import lombok.Data;

/**
 * 扫码登录返回给 Web 端的用户信息（与账号密码登录返回结构保持一致）。
 */
@Data
public class QrUserInfoVO {

    private Long userId;

    private String username;

    private String nickname;

    private String avatar;

    private String email;

    private String account;

    private String phone;

    private String signature;

    private String experience;
}
