/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.stereotype.Component
 *  org.springframework.web.socket.WebSocketSession
 */
package com.vrd.access.websocket;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

/**
 * WebSocket 会话管理器
 * <p>
 * 维护两类会话集合（均为线程安全）：
 * <ul>
 *   <li>全局广播会话（allSessions）：接收全部车辆的告警推送</li>
 *   <li>VIN 订阅会话（vinSessions）：按车架号分组，接收对应车辆的信号/告警推送</li>
 * </ul>
 * sessionVinMap 记录会话当前订阅的 VIN，用于会话断开或切换模式时正确注销。
 */
@Component
public class WebSocketSessionManager {
    private static final Logger log = LoggerFactory.getLogger(WebSocketSessionManager.class);
    private final Set<WebSocketSession> allSessions = new CopyOnWriteArraySet<WebSocketSession>();
    private final Map<String, Set<WebSocketSession>> vinSessions = new ConcurrentHashMap<String, Set<WebSocketSession>>();
    private final Map<String, String> sessionVinMap = new ConcurrentHashMap<String, String>();

    public void addSession(WebSocketSession session) {
        this.allSessions.add(session);
        log.info("WebSocket session added: {}, total: {}", (Object)session.getId(), (Object)this.allSessions.size());
    }

    public void addVinSession(String vin, WebSocketSession session) {
        this.vinSessions.computeIfAbsent(vin, k -> new CopyOnWriteArraySet()).add(session);
        this.sessionVinMap.put(session.getId(), vin);
        log.info("WebSocket session subscribed to VIN {}: {}, VIN subscribers: {}", new Object[]{vin, session.getId(), this.getVinSessionCount(vin)});
    }

    public void removeSession(WebSocketSession session) {
        Set<WebSocketSession> sessions;
        this.allSessions.remove(session);
        String vin = this.sessionVinMap.remove(session.getId());
        if (vin != null && (sessions = this.vinSessions.get(vin)) != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                this.vinSessions.remove(vin);
            }
        }
        log.info("WebSocket session removed: {}, remaining: {}", (Object)session.getId(), (Object)this.allSessions.size());
    }

    public Set<WebSocketSession> getSessionsByVin(String vin) {
        return this.vinSessions.getOrDefault(vin, Set.of());
    }

    public Set<WebSocketSession> getAllSessions() {
        return this.allSessions;
    }

    public int getVinSessionCount(String vin) {
        Set<WebSocketSession> sessions = this.vinSessions.get(vin);
        return sessions != null ? sessions.size() : 0;
    }

    public int getTotalSessionCount() {
        return this.allSessions.size();
    }

    public int getVinSubscriptionCount() {
        return this.vinSessions.size();
    }
}

