package com.minecraft.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 扫码登录配置。
 *
 * <p>对应 application.yml 中的 qr-login 节点；qr-login.enabled=false 时可秒级关闭扫码登录。</p>
 */
@Data
@Component
@ConfigurationProperties(prefix = "qr-login")
public class QrLoginProperties {

    /** 是否启用扫码登录 */
    private boolean enabled = true;

    /** 二维码有效期（秒） */
    private long ttlSeconds = 120;

    /** APP 确认后 Web 端必须在该时间内消费（秒） */
    private long confirmedTtlSeconds = 30;

    /** WebSocket 不可用时是否启用轮询兜底 */
    private boolean pollFallback = true;

    /** WebSocket 心跳间隔（秒），前端预留 */
    private int wsHeartbeatSeconds = 25;

    /** 二维码内容协议头 */
    private String qrProtocol = "minecraft://qrLogin";

    /** 限流配置 */
    private RateLimit rateLimit = new RateLimit();

    @Data
    public static class RateLimit {

        /** 单 IP 每分钟生成二维码上限 */
        private int generatePerIpPerMin = 10;

        /** 单用户每分钟扫码上限 */
        private int scanPerUserPerMin = 20;
    }
}
