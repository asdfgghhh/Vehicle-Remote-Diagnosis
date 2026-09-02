package com.vrd.vehicle.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket 配置（service-vehicle）
 * <p>
 * 端点：
 * <ul>
 *   <li>/ws/online — 全局广播，接收所有车辆在线状态变更（车辆列表页、仪表盘页）</li>
 *   <li>/ws/online/{vin} — 按 VIN 订阅，仅接收指定车辆的状态变更（车辆详情页）</li>
 * </ul>
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class OnlineStatusWebSocketConfig implements WebSocketConfigurer {

    private final OnlineStatusWebSocketHandler onlineStatusWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(onlineStatusWebSocketHandler, "/ws/online", "/ws/online/*")
                .setAllowedOrigins("*");
    }
}
