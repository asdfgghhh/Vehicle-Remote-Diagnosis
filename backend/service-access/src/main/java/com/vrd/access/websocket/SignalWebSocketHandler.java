/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.alibaba.fastjson2.JSON
 *  com.alibaba.fastjson2.JSONObject
 *  com.alibaba.fastjson2.JSONWriter$Feature
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.stereotype.Component
 *  org.springframework.web.socket.CloseStatus
 *  org.springframework.web.socket.TextMessage
 *  org.springframework.web.socket.WebSocketMessage
 *  org.springframework.web.socket.WebSocketSession
 *  org.springframework.web.socket.handler.TextWebSocketHandler
 */
package com.vrd.access.websocket;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.vrd.access.websocket.WebSocketSessionManager;
import java.io.IOException;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * 车辆信号实时推送 WebSocket 处理器（service-access，端口 9086）
 * <p>
 * 对外 WebSocket 端点（经网关/Nginx 反向代理路径为 /ws/signal[/&lt;vin&gt;]）：
 * <ul>
 *   <li>连接 /ws/signal/{vin}：VIN 订阅模式，仅接收该车的信号与告警推送</li>
 *   <li>连接 /ws/signal：全局广播模式，接收全部车辆的告警推送</li>
 * </ul>
 * 客户端可在连接后发送 JSON 消息动态切换模式：
 * <pre>
 * {"action": "subscribe",   "vin": "LSV123..."}  // 切换到指定车辆订阅
 * {"action": "unsubscribe"}                        // 切回全局广播
 * {"action": "ping"}                               // 心跳，服务端回 {"type":"pong"}
 * </pre>
 * 服务端推送的消息类型：connected（欢迎）、signal（实时信号）、alert（告警）等。
 */
@Component
public class SignalWebSocketHandler
extends TextWebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(SignalWebSocketHandler.class);
    private final WebSocketSessionManager sessionManager;

    public SignalWebSocketHandler(WebSocketSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    /**
     * 连接建立回调：从 URI 提取 VIN 注册会话并发送欢迎消息
     * <p>路径末段为 VIN 则注册为 VIN 订阅会话，否则注册为全局广播会话。
     */
    public void afterConnectionEstablished(WebSocketSession session) {
        String vin = this.extractVinFromUri(session);
        if (vin != null) {
            this.sessionManager.addVinSession(vin, session);
            this.sendMessage(session, this.buildWelcome(vin));
        } else {
            this.sessionManager.addSession(session);
            this.sendMessage(session, this.buildWelcome(null));
        }
        log.info("WebSocket connected: sessionId={}, vin={}", session.getId(), vin);
    }

    /**
     * 连接关闭回调：注销会话并记录日志
     */
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        this.sessionManager.removeSession(session);
        log.info("WebSocket disconnected: sessionId={}, status={}", session.getId(), status);
    }

    /**
     * 处理客户端下行 JSON 消息
     * <p>支持 action：subscribe（订阅指定 VIN）、unsubscribe（切回全局广播）、ping（心跳）。
     */
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String payload = (String)message.getPayload();
        log.debug("Received WebSocket message from {}: {}", session.getId(), payload);
        try {
            JSONObject request = JSON.parseObject((String)payload);
            String action = request.getString("action");
            if ("subscribe".equals(action)) {
                String vin = request.getString("vin");
                if (vin != null && !vin.isEmpty()) {
                    this.sessionManager.removeSession(session);
                    this.sessionManager.addVinSession(vin, session);
                    this.sendMessage(session, this.buildResponse("subscribed", "\u5df2\u8ba2\u9605\u8f66\u8f86 " + vin));
                }
            } else if ("unsubscribe".equals(action)) {
                this.sessionManager.removeSession(session);
                this.sessionManager.addSession(session);
                this.sendMessage(session, this.buildResponse("unsubscribed", "\u5df2\u5207\u6362\u4e3a\u5168\u5c40\u5e7f\u64ad\u6a21\u5f0f"));
            } else if ("ping".equals(action)) {
                this.sendMessage(session, this.buildResponse("pong", "ok"));
            }
        }
        catch (Exception e) {
            log.error("Error handling WebSocket message: {}", e.getMessage());
        }
    }

    /**
     * 传输层异常回调：注销异常会话
     */
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("WebSocket transport error: sessionId={}", session.getId(), exception);
        this.sessionManager.removeSession(session);
    }

    /**
     * 向订阅指定 VIN 的客户端推送实时信号消息
     *
     * @param vin        车架号
     * @param signalJson 信号 JSON 报文
     */
    public void broadcastSignal(String vin, String signalJson) {
        for (WebSocketSession session : this.sessionManager.getSessionsByVin(vin)) {
            this.sendMessage(session, signalJson);
        }
    }

    /**
     * 向全部全局广播会话推送消息
     *
     * @param message JSON 报文
     */
    public void broadcastToAll(String message) {
        for (WebSocketSession session : this.sessionManager.getAllSessions()) {
            this.sendMessage(session, message);
        }
    }

    /**
     * 推送告警消息：VIN 订阅会话与全局广播会话均会收到
     *
     * @param vin       车架号
     * @param alertJson 告警 JSON 报文
     */
    public void broadcastAlert(String vin, String alertJson) {
        for (WebSocketSession session : this.sessionManager.getSessionsByVin(vin)) {
            this.sendMessage(session, alertJson);
        }
        for (WebSocketSession session : this.sessionManager.getAllSessions()) {
            this.sendMessage(session, alertJson);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void sendMessage(WebSocketSession session, String message) {
        if (session != null && session.isOpen()) {
            try {
                WebSocketSession webSocketSession = session;
                synchronized (webSocketSession) {
                    session.sendMessage((WebSocketMessage)new TextMessage((CharSequence)message));
                }
            }
            catch (IOException e) {
                log.error("Failed to send WebSocket message to {}", session.getId(), e);
            }
        }
    }

    private String extractVinFromUri(WebSocketSession session) {
        URI uri = session.getUri();
        if (uri == null) {
            return null;
        }
        String path = uri.getPath();
        String[] segments = path.split("/");
        if (segments.length >= 3 && !"signal".equals(segments[segments.length - 1])) {
            return segments[segments.length - 1];
        }
        return null;
    }

    private String buildWelcome(String vin) {
        JSONObject welcome = new JSONObject();
        welcome.put("type", "connected");
        welcome.put("vin", vin);
        welcome.put("mode", (vin != null ? "VIN\u8ba2\u9605" : "\u5168\u5c40\u5e7f\u64ad"));
        welcome.put("message", "\u5df2\u8fde\u63a5\u5230\u8f66\u8f86\u4fe1\u53f7\u5b9e\u65f6\u63a8\u9001\u670d\u52a1");
        welcome.put("timestamp", System.currentTimeMillis());
        return welcome.toJSONString(new JSONWriter.Feature[0]);
    }

    private String buildResponse(String action, String message) {
        JSONObject response = new JSONObject();
        response.put("type", action);
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());
        return response.toJSONString(new JSONWriter.Feature[0]);
    }
}

