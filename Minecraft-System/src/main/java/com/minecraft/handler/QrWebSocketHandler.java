package com.minecraft.handler;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.minecraft.service.qr.QrWsNotifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * 扫码登录 WebSocket：Web 端通过 /ws/qr/{ticket} 订阅 ticket 状态变更。
 *
 * <p>该通道无需登录态，ticket 本身即凭据；服务端只推送状态，不推送 token。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QrWebSocketHandler extends TextWebSocketHandler {

    private static final String TICKET_ATTR = "qrTicket";

    private final QrWsNotifier wsNotifier;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String ticket = extractTicket(session);
        if (ticket == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("invalid ticket"));
            return;
        }
        session.getAttributes().put(TICKET_ATTR, ticket);
        wsNotifier.register(ticket, session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            JSONObject json = JSON.parseObject(message.getPayload());
            if ("HEARTBEAT".equals(json.getString("type"))) {
                JSONObject pong = new JSONObject();
                pong.put("type", "HEARTBEAT");
                pong.put("status", "success");
                pong.put("ts", System.currentTimeMillis());
                synchronized (session) {
                    if (session.isOpen()) {
                        session.sendMessage(new TextMessage(pong.toJSONString()));
                    }
                }
            }
        } catch (Exception e) {
            log.debug("扫码登录 WebSocket 消息处理失败: {}", e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Object ticket = session.getAttributes().get(TICKET_ATTR);
        if (ticket != null) {
            wsNotifier.unregister(String.valueOf(ticket), session);
        }
    }

    private String extractTicket(WebSocketSession session) {
        if (session.getUri() == null) {
            return null;
        }
        String path = session.getUri().getPath();
        if (path == null) {
            return null;
        }
        String prefix = "/ws/qr/";
        int index = path.indexOf(prefix);
        if (index < 0) {
            return null;
        }
        String ticket = path.substring(index + prefix.length());
        if (ticket.isEmpty() || ticket.contains("/") || !ticket.matches("[A-Za-z0-9]{32,64}")) {
            return null;
        }
        return ticket;
    }
}
