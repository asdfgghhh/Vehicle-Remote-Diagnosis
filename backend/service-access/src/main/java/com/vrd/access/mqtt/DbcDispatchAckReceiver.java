package com.vrd.access.mqtt;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.vrd.access.config.KafkaTopicProperties;
import com.vrd.access.kafka.KafkaMessageProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

/**
 * 车端 DBC 下发 ACK 接收器
 *
 * <p>订阅 MQTT topic vrd/+/config/ack，接收车端上报的下发结果（已下载/已应用/失败），
 * <p>转发到 Kafka topic dbc-dispatch-ack，由 service-dbc 消费后更新 dispatch_log 状态。
 *
 * <p>车端上报的 ACK 消息格式：
 * <pre>
 * {
 *   "traceId": "abc123",
 *   "vin": "VIN12345",
 *   "dbcFileId": 123,
 *   "status": "APPLIED",   // DOWNLOADING / DOWNLOADED / APPLIED / FAILED
 *   "message": "success",
 *   "timestamp": 1700000000000
 * }
 * </pre>
 */
@Component
public class DbcDispatchAckReceiver {

    private static final Logger log = LoggerFactory.getLogger(DbcDispatchAckReceiver.class);
    private static final String MQTT_TOPIC_HEADER = "mqtt_receivedTopic";

    @Autowired
    private KafkaMessageProducer kafkaMessageProducer;

    @Autowired
    private KafkaTopicProperties topicProperties;

    @ServiceActivator(inputChannel = "mqttDispatchAckInputChannel")
    public void handleMessage(Message<?> message) {
        try {
            String topic = (String) message.getHeaders().get(MQTT_TOPIC_HEADER, String.class);
            String payload = String.valueOf(message.getPayload());
            JSONObject ack = JSON.parseObject(payload);
            if (ack == null || !ack.containsKey("vin")) {
                log.warn("Skip invalid DBC dispatch ACK: topic={}, payload={}", topic, payload);
                return;
            }
            String vin = ack.getString("vin");
            String traceId = ack.getString("traceId");
            String status = ack.getString("status");
            // 转发到 Kafka dbc-dispatch-ack topic，service-dbc 消费
            kafkaMessageProducer.publishDbcDispatchAck(vin, payload);
            log.info("DBC dispatch ACK bridged to Kafka: traceId={}, vin={}, status={}, topic={}",
                    traceId, vin, status, topicProperties.getDbcDispatchAck());
        } catch (Exception e) {
            log.error("Failed to process DBC dispatch ACK", e);
        }
    }
}
