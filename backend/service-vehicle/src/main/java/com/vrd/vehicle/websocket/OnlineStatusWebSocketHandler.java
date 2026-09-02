package com.vrd.vehicle.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 车辆在线状态 WebSocket Handler（service-vehicle）
 * <p>
 * 端点：/ws/online（全局广播）和 /ws/online/{vin}（按 VIN 订阅）
 * <p>
 * service-vehicle 更新数据库车辆状态后，直接通过本 Handler 推送前端，
 * 无需经过 service-access 中转。
 */
@Slf4j
@Component
public class OnlineStatusWebSocketHandler extends TextWebSocketHandler {

    /** 全局广播会话集合（车辆列表页、仪表盘页连接） */
    private final Set<WebSocketSession> globalSessions = new CopyOnWriteArraySet<>();

    /** 按 VIN 订阅的会话映射（车辆详情页连接） */
    private final Map<String, Set<WebSocketSession>> vinSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String vin = extractVinFromPath(session);
        if (vin != null) {
            vinSessions.computeIfAbsent(vin, k -> new CopyOnWriteArraySet<>()).add(session);
            log.info("WebSocket online-status connected: session={}, vin={}", session.getId(), vin);
        } else {
            globalSessions.add(session);
            log.info("WebSocket online-status connected: session={}, mode=global", session.getId());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String vin = extractVinFromPath(session);
        if (vin != null) {
            Set<WebSocketSession> sessions = vinSessions.get(vin);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    vinSessions.remove(vin);
                }
            }
        } else {
            globalSessions.remove(session);
        }
        log.info("WebSocket online-status disconnected: session={}, status={}", session.getId(), status);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // 处理客户端 ping 心跳
        String payload = message.getPayload();
        if ("ping".equalsIgnoreCase(payload)) {
            try {
                session.sendMessage(new TextMessage("pong"));
            } catch (IOException e) {
                log.warn("Failed to send pong to session={}", session.getId());
            }
        }
    }

    /**
     * 推送在线状态变更通知给前端
     *
     * @param vin     车架号
     * @param jsonMsg JSON 消息（包含 type=onlineStatus, status, statusLabel 等）
     */
    public void broadcast(String vin, String jsonMsg) {
        // 1) 全局广播（车辆列表页、仪表盘）
        sendToSessions(globalSessions, jsonMsg);
        // 2) 按 VIN 订阅推送（车辆详情页）
        Set<WebSocketSession> sessions = vinSessions.get(vin);
        if (sessions != null) {
            sendToSessions(sessions, jsonMsg);
        }
    }

    private void sendToSessions(Set<WebSocketSession> sessions, String jsonMsg) {
        if (sessions == null || sessions.isEmpty()) return;
        TextMessage message = new TextMessage(jsonMsg);
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(message);
                } catch (IOException e) {
                    log.warn("Failed to send WebSocket message to session={}: {}", session.getId(), e.getMessage());
                }
            }
        }
    }

    private String extractVinFromPath(WebSocketSession session) {
        String path = session.getUri().getPath();
        // /ws/online/{vin} → vin；/ws/online → null（全局）
        if (path == null) return null;
        String[] parts = path.split("/");
        if (parts.length > 3) {
            return parts[3]; // /ws/online/{vin}
        }
        return null;
    }
}
