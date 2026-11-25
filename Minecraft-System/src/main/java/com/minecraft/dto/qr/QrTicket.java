package com.minecraft.dto.qr;

import com.minecraft.enums.QrLoginStatus;
import com.minecraft.vo.QrUserInfoVO;
import lombok.Data;

/**
 * 扫码登录 ticket 在存储中的完整快照。
 *
 * <p>该对象只保存在 Redis（或 Redis 不可用时的本地内存），不直接返回给前端。</p>
 */
@Data
public class QrTicket {

    private QrLoginStatus status;

    private String ticket;

    /** 生成方（Web）信息 */
    private String genIp;

    private String genRegion;

    private String genUserAgent;

    private String genBrowser;

    private String genOs;

    private Long genAt;

    private Long expireAt;

    /** 扫码方（APP）信息 */
    private Long scannerUserId;

    private String scannerIp;

    private Long scannedAt;

    private String appVersion;

    private String deviceModel;

    /** APP 确认后签发的 Web token，仅供 Web 主动 consume 读取 */
    private String webToken;

    private QrUserInfoVO webUserInfo;

    private Long confirmedAt;

    private Long canceledAt;
}
