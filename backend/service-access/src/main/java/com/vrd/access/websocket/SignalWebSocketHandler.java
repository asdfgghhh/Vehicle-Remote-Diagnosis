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

@Component
public class SignalWebSocketHandler
extends TextWebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(SignalWebSocketHandler.class);
    private final WebSocketSessionManager sessionManager;

    public SignalWebSocketHandler(WebSocketSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public void afterConnectionEstablished(WebSocketSession session) {
        String vin = this.extractVinFromUri(session);
        if (vin != null) {
            this.sessionManager.addVinSession(vin, session);
            this.sendMessage(session, this.buildWelcome(vin));
        } else {
            this.sessionManager.addSession(session);
            this.sendMessage(session, this.buildWelcome(null));
        }
        log.info("WebSocket connected: sessionId={}, vin={}", (Object)session.getId(), (Object)vin);
    }

    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        this.sessionManager.removeSession(session);
        log.info("WebSocket disconnected: sessionId={}, status={}", (Object)session.getId(), (Object)status);
    }

    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String payload = (String)message.getPayload();
        log.debug("Received WebSocket message from {}: {}", (Object)session.getId(), (Object)payload);
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
            log.error("Error handling WebSocket message: {}", (Object)e.getMessage());
        }
    }

    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("WebSocket transport error: sessionId={}", (Object)session.getId(), (Object)exception);
        this.sessionManager.removeSession(session);
    }

    public void broadcastSignal(String vin, String signalJson) {
        for (WebSocketSession session : this.sessionManager.getSessionsByVin(vin)) {
            this.sendMessage(session, signalJson);
        }
    }

    public void broadcastToAll(String message) {
        for (WebSocketSession session : this.sessionManager.getAllSessions()) {
            this.sendMessage(session, message);
        }
    }

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
                log.error("Failed to send WebSocket message to {}", (Object)session.getId(), (Object)e);
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
        welcome.put((Object)"type", (Object)"connected");
        welcome.put((Object)"vin", (Object)vin);
        welcome.put((Object)"mode", (Object)(vin != null ? "VIN\u8ba2\u9605" : "\u5168\u5c40\u5e7f\u64ad"));
        welcome.put((Object)"message", (Object)"\u5df2\u8fde\u63a5\u5230\u8f66\u8f86\u4fe1\u53f7\u5b9e\u65f6\u63a8\u9001\u670d\u52a1");
        welcome.put((Object)"timestamp", (Object)System.currentTimeMillis());
        return welcome.toJSONString(new JSONWriter.Feature[0]);
    }

    private String buildResponse(String action, String message) {
        JSONObject response = new JSONObject();
        response.put((Object)"type", (Object)action);
        response.put((Object)"message", (Object)message);
        response.put((Object)"timestamp", (Object)System.currentTimeMillis());
        return response.toJSONString(new JSONWriter.Feature[0]);
    }
}

