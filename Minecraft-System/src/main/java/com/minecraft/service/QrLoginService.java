package com.minecraft.service;

import com.minecraft.dto.request.QrScanRequest;
import com.minecraft.vo.QrConsumeVO;
import com.minecraft.vo.QrGenerateVO;
import com.minecraft.vo.QrScanDetailVO;
import com.minecraft.vo.QrStatusVO;

import jakarta.servlet.http.HttpServletRequest;

/**
 * APP 扫码授权 Web 登录服务。
 */
public interface QrLoginService {

    /** Web 生成登录二维码 */
    QrGenerateVO generate(HttpServletRequest request);

    /** Web 轮询 ticket 状态（WebSocket 兜底） */
    QrStatusVO status(String ticket);

    /** APP 扫码：WAITING → SCANNED（幂等） */
    QrScanDetailVO scan(QrScanRequest request, Long appUserId, HttpServletRequest httpRequest);

    /** APP 获取 Web 端设备信息 */
    QrScanDetailVO detail(String ticket, Long appUserId);

    /** APP 确认登录：SCANNED → CONFIRMED，签发 Web token */
    QrStatusVO confirm(String ticket, Long appUserId);

    /** APP 取消登录 */
    QrStatusVO cancel(String ticket, Long appUserId);

    /** Web 消费 ticket，换取 JWT 与权限快照 */
    QrConsumeVO consume(String ticket);
}
