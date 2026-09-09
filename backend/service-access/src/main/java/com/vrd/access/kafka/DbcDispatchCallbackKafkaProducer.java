package com.vrd.access.kafka;

import com.alibaba.fastjson2.JSON;
import com.vrd.access.config.KafkaTopicProperties;
import com.vrd.common.message.DbcDispatchCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * DBC 下发回调 Kafka 生产者（service-access -> Kafka -> service-dbc）
 *
 * <p>service-access 在 MQTT 发送到车端成功/失败后，通过此生产者发送回调消息到
 * Kafka topic dbc-dispatch-callback，service-dbc 消费后更新 dispatch_log 状态。
 */
@Component
public class DbcDispatchCallbackKafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(DbcDispatchCallbackKafkaProducer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaTopicProperties topicProperties;

    public DbcDispatchCallbackKafkaProducer(KafkaTemplate<String, String> kafkaTemplate,
                                            KafkaTopicProperties topicProperties) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicProperties = topicProperties;
    }

    /**
     * 发送 DBC 下发回调消息
     *
     * @param callback 回调消息体（traceId、vin、status=SENT/FAILED）
     */
    public void sendCallback(DbcDispatchCallback callback) {
        try {
            String payload = JSON.toJSONString(callback);
            kafkaTemplate.send(topicProperties.getDbcDispatchCallback(), callback.getVin(), payload);
            log.info("DBC dispatch callback sent to Kafka: traceId={}, vin={}, status={}, topic={}",
                    callback.getTraceId(), callback.getVin(), callback.getStatus(),
                    topicProperties.getDbcDispatchCallback());
        } catch (Exception e) {
            log.error("Failed to send DBC dispatch callback: traceId={}, vin={}",
                    callback.getTraceId(), callback.getVin(), e);
        }
    }
}
