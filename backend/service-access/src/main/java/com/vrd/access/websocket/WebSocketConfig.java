/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.context.annotation.Bean
 *  org.springframework.context.annotation.Configuration
 *  org.springframework.web.socket.WebSocketHandler
 *  org.springframework.web.socket.config.annotation.EnableWebSocket
 *  org.springframework.web.socket.config.annotation.WebSocketConfigurer
 *  org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry
 *  org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean
 */
package com.vrd.access.websocket;

import com.vrd.access.websocket.SignalWebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

/**
 * WebSocket 配置
 * <p>
 * 注册对外 WebSocket 端点（文本缓冲区 64KB，会话空闲超时 10 分钟）：
 * <ul>
 *   <li>/ws/signal —— 全局广播模式，接收全部车辆告警推送</li>
 *   <li>/ws/signal/{vin} —— VIN 订阅模式，仅接收指定车辆的信号与告警推送</li>
 * </ul>
 * 生产环境前端经 Nginx 将 /ws/ 反向代理到 service-access（9086）。
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig
implements WebSocketConfigurer {
    private final SignalWebSocketHandler signalWebSocketHandler;

    public WebSocketConfig(SignalWebSocketHandler signalWebSocketHandler) {
        this.signalWebSocketHandler = signalWebSocketHandler;
    }

    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler((WebSocketHandler)this.signalWebSocketHandler, new String[]{"/ws/signal"}).setAllowedOrigins(new String[]{"*"});
        registry.addHandler((WebSocketHandler)this.signalWebSocketHandler, new String[]{"/ws/signal/{vin}"}).setAllowedOrigins(new String[]{"*"});
    }

    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(Integer.valueOf(65536));
        container.setMaxBinaryMessageBufferSize(Integer.valueOf(65536));
        container.setMaxSessionIdleTimeout(Long.valueOf(600000L));
        return container;
    }
}

