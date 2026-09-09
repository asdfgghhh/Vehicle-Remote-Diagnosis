package com.vrd.access.kafka;

import com.alibaba.fastjson2.JSON;
import com.vrd.access.config.KafkaTopicProperties;
import com.vrd.common.message.DbcDispatchCallback;
import com.vrd.common.message.DbcDispatchMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * DBC 下发指令 Kafka 消费者 → MQTT 下行桥接
 *
 * <p>消费 service-dbc 发来的 dbc-dispatch topic 消息，转发到车端 MQTT topic: vrd/{vin}/config/dispatch
 * <p>遵循"MQTT 指令 + HTTPS 下载"分离模式：MQTT 只传小指令，DBC 文件由车端从对象存储下载。
 *
 * <p>MQTT 发送（QoS=1 同步）成功/失败后，通过 {@link DbcDispatchCallbackKafkaProducer} 发送回调消息，
 * service-dbc 消费回调后更新 dispatch_log 状态为 SENT / FAILED。
 */
@Component
public class DbcDispatchKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(DbcDispatchKafkaConsumer.class);

    private final KafkaTopicProperties topicProperties;
    private final MessageChannel mqttOutboundChannel;
    private final DbcDispatchCallbackKafkaProducer callbackProducer;

    @Value("${mqtt.dispatch-topic-prefix:vrd}")
    private String mqttDispatchPrefix;

    @Autowired
    public DbcDispatchKafkaConsumer(KafkaTopicProperties topicProperties,
                                    MessageChannel mqttOutboundChannel,
                                    DbcDispatchCallbackKafkaProducer callbackProducer) {
        this.topicProperties = topicProperties;
        this.mqttOutboundChannel = mqttOutboundChannel;
        this.callbackProducer = callbackProducer;
    }

    @KafkaListener(topics = "#{@kafkaTopicProperties.dbcDispatch}", groupId = "access-dbc-dispatch-bridge")
    public void consume(String message) {
        DbcDispatchMessage command = null;
        try {
            command = JSON.parseObject(message, DbcDispatchMessage.class);
            if (command == null) {
                log.warn("Skip invalid DBC dispatch message: {}", message);
                return;
            }
            String vin = command.getVin();
            if (!StringUtils.hasText(vin)) {
                log.warn("Skip DBC dispatch message without vin: {}", message);
                return;
            }

            // 转发到车端 MQTT topic: vrd/{vin}/config/dispatch
            String mqttTopic = mqttDispatchPrefix + "/" + vin + "/config/dispatch";
            Message<String> mqttMessage = MessageBuilder.withPayload(message)
                    .setHeader(MqttHeaders.TOPIC, mqttTopic)
                    .setHeader(MqttHeaders.QOS, 1)
                    .build();

            mqttOutboundChannel.send(mqttMessage);

            log.info("DBC dispatch command bridged to MQTT: traceId={}, vin={}, dbcFileId={}, topic={}",
                    command.getTraceId(), vin, command.getDbcFileId(), mqttTopic);

            // MQTT 发送成功 → 回调通知 service-dbc 标记 SENT
            this.sendCallback(command, "SENT", "MQTT broker ACK confirmed");
        } catch (Exception e) {
            log.error("Failed to bridge DBC dispatch command to MQTT: {}", e.getMessage());
            // MQTT 发送失败 → 回调通知 service-dbc 标记 FAILED
            if (command != null) {
                this.sendCallback(command, "FAILED", "MQTT send failed: " + e.getMessage());
            }
        }
    }

    /**
     * 发送回调消息到 Kafka，通知 service-dbc 更新 dispatch_log 状态
     *
     * @param command 原始下发指令
     * @param status  回调状态：SENT / FAILED
     * @param msg     结果描述
     */
    private void sendCallback(DbcDispatchMessage command, String status, String msg) {
        try {
            DbcDispatchCallback callback = new DbcDispatchCallback();
            callback.setTraceId(command.getTraceId());
            callback.setVin(command.getVin());
            callback.setDbcFileId(command.getDbcFileId());
            callback.setStatus(status);
            callback.setMessage(msg);
            callback.setTimestamp(System.currentTimeMillis());
            this.callbackProducer.sendCallback(callback);
        } catch (Exception ex) {
            log.error("Failed to send DBC dispatch callback: traceId={}, vin={}",
                    command.getTraceId(), command.getVin(), ex);
        }
    }
}
