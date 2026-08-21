package com.vrd.access.kafka;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.vrd.access.config.KafkaTopicProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * P0-1: uds-commands 消费者 → MQTT 下行桥接
 *
 * service-diagnosis 将 UDS 命令发布到 Kafka uds-commands，
 * 本组件消费后转发到车端 MQTT topic: vrd/{vin}/uds/request
 */
@Component
public class UdsCommandKafkaConsumer {
    private static final Logger log = LoggerFactory.getLogger(UdsCommandKafkaConsumer.class);

    private final KafkaTopicProperties topicProperties;
    private final MessageChannel mqttOutboundChannel;

    @Value("${mqtt.uds-request-topic-prefix:vrd}")
    private String mqttRequestPrefix;

    public UdsCommandKafkaConsumer(KafkaTopicProperties topicProperties, MessageChannel mqttOutboundChannel) {
        this.topicProperties = topicProperties;
        this.mqttOutboundChannel = mqttOutboundChannel;
    }

    @KafkaListener(topics = "#{@kafkaTopicProperties.udsCommands}", groupId = "access-uds-command-bridge")
    public void consume(String message) {
        try {
            JSONObject command = JSON.parseObject(message);
            if (command == null) {
                log.warn("Skip invalid UDS command message: {}", message);
                return;
            }
            String vin = command.getString("vin");
            String traceId = command.getString("traceId");
            if (!StringUtils.hasText(vin)) {
                log.warn("Skip UDS command without vin: {}", message);
                return;
            }
            String mqttTopic = mqttRequestPrefix + "/" + vin + "/uds/request";
            Message<String> mqttMessage = MessageBuilder.withPayload(message)
                    .setHeader(MqttHeaders.TOPIC, mqttTopic)
                    .setHeader(MqttHeaders.QOS, 1)
                    .build();
            mqttOutboundChannel.send(mqttMessage);
            log.info("UDS command bridged to MQTT: traceId={}, topic={}, serviceId={}", traceId, mqttTopic, command.getString("serviceId"));
        } catch (Exception e) {
            log.error("Failed to bridge UDS command to MQTT", e);
        }
    }
}
