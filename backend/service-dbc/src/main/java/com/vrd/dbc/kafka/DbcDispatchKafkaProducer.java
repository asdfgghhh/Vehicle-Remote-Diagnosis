package com.vrd.dbc.kafka;

import com.alibaba.fastjson2.JSON;
import com.vrd.common.message.DbcDispatchMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * DBC 下发指令 Kafka 生产者
 *
 * <p>service-dbc 在 dispatchToVehicle 时将 DBC 下发指令发往 Kafka topic dbc-dispatch，
 * service-access 消费后桥接到 MQTT topic vrd/{vin}/config/dispatch 下发到车端。
 */
@Component
public class DbcDispatchKafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(DbcDispatchKafkaProducer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${kafka.topics.dbc-dispatch:dbc-dispatch}")
    private String dbcDispatchTopic;

    public DbcDispatchKafkaProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * 发送 DBC 下发指令到 Kafka
     *
     * @param message 下发指令消息体
     */
    public void sendDispatchCommand(DbcDispatchMessage message) {
        try {
            String payload = JSON.toJSONString(message);
            kafkaTemplate.send(dbcDispatchTopic, message.getVin(), payload);
            log.info("DBC dispatch command sent to Kafka: traceId={}, vin={}, dbcFileId={}, topic={}",
                    message.getTraceId(), message.getVin(), message.getDbcFileId(), dbcDispatchTopic);
        } catch (Exception e) {
            log.error("Failed to send DBC dispatch command to Kafka: traceId={}, vin={}",
                    message.getTraceId(), message.getVin(), e);
            throw new RuntimeException("Kafka 发送失败: " + e.getMessage(), e);
        }
    }
}
