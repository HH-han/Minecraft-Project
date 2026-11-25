package com.minecraft.config;

import com.minecraft.handler.QrWebSocketHandler;
import com.minecraft.handler.WebSocketHandler;
import com.minecraft.handler.WebSocketInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final WebSocketHandler webSocketHandler;

    private final QrWebSocketHandler qrWebSocketHandler;

    public WebSocketConfig(WebSocketHandler webSocketHandler, QrWebSocketHandler qrWebSocketHandler) {
        this.webSocketHandler = webSocketHandler;
        this.qrWebSocketHandler = qrWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketHandler, "/ws")
                .addInterceptors(new WebSocketInterceptor())
                .setAllowedOriginPatterns("*");

        // 扫码登录状态通道：无需登录态，ticket 即凭据
        registry.addHandler(qrWebSocketHandler, "/ws/qr/*")
                .setAllowedOriginPatterns("*");
    }
}
