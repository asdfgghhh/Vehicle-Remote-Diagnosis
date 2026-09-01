/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vrd.common.result.Result
 *  org.springframework.web.bind.annotation.PostMapping
 *  org.springframework.web.bind.annotation.RequestBody
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RequestParam
 *  org.springframework.web.bind.annotation.RestController
 */
package com.vrd.access.controller;

import com.vrd.access.kafka.KafkaMessageProducer;
import com.vrd.common.result.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 车端信号 HTTP 接入接口（服务端口 9086，网关路由前缀 /api/signal/vehicle）
 * <p>
 * 为不支持 MQTT 的车端/模拟设备提供 HTTP 方式的信号上报入口，
 * 收到的信号经 Kafka（vehicle-signals）进入后续存储与告警链路。
 * 正常车端链路走 MQTT topic vehicle/signal/{vin}，本接口为补充通道。
 */
@RestController
@RequestMapping(value={"/signal/vehicle"})
public class VehicleSignalController {
    private final KafkaMessageProducer kafkaMessageProducer;

    public VehicleSignalController(KafkaMessageProducer kafkaMessageProducer) {
        this.kafkaMessageProducer = kafkaMessageProducer;
    }

    /**
     * 接收车端上报的信号数据（HTTP 通道）
     * <p>POST /signal/vehicle/receive?vin={vin}
     * <p>信号原文转发到 Kafka vehicle-signals 主题（source=http），由后端链路统一消费。
     *
     * @param vin     车架号
     * @param payload 信号数据原文（JSON 字符串，请求体）
     * @return 空结果
     */
    @PostMapping(value={"/receive"})
    public Result<Void> receiveSignal(@RequestParam String vin, @RequestBody String payload) {
        this.kafkaMessageProducer.publishVehicleSignal(vin, "http", payload);
        return Result.success();
    }
}

