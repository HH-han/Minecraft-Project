package com.minecraft.service.qr;

import com.alibaba.fastjson2.JSONObject;
import com.minecraft.enums.QrLoginStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 扫码登录 WebSocket 定向推送。
 *
 * <p>Web 端通过 /ws/qr/{ticket} 订阅 ticket 状态；服务端只推送状态与时间戳，
 * 绝不推送 token / userId，最终凭据由 Web 主动 consume 拉取。</p>
 */
@Slf4j
@Component
public class QrWsNotifier {

    private final Map<String, Set<WebSocketSession>> sessions = new ConcurrentHashMap<>();

    public void register(String ticket, WebSocketSession session) {
        if (ticket == null || session == null) {
            return;
        }
        sessions.computeIfAbsent(ticket, key -> ConcurrentHashMap.newKeySet()).add(session);
    }

    public void unregister(String ticket, WebSocketSession session) {
        if (ticket == null || session == null) {
            return;
        }
        Set<WebSocketSession> set = sessions.get(ticket);
        if (set == null) {
            return;
        }
        set.remove(session);
        if (set.isEmpty()) {
            sessions.remove(ticket);
        }
    }

    public void push(String ticket, QrLoginStatus status) {
        Set<WebSocketSession> set = sessions.get(ticket);
        if (set == null || set.isEmpty()) {
            return;
        }
        JSONObject message = new JSONObject();
        message.put("type", "qr_status");
        message.put("ticket", ticket);
        message.put("status", status.name());
        message.put("ts", System.currentTimeMillis());
        String payload = message.toJSONString();

        for (WebSocketSession session : set) {
            if (session == null || !session.isOpen()) {
                unregister(ticket, session);
                continue;
            }
            try {
                synchronized (session) {
                    if (session.isOpen()) {
                        session.sendMessage(new TextMessage(payload));
                    }
                }
            } catch (Exception e) {
                log.debug("扫码登录状态推送失败: {}", e.getMessage());
                unregister(ticket, session);
            }
        }
    }
}
