package com.vrd.diagnosis.kafka;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.vrd.diagnosis.dto.UdsResponse;
import com.vrd.diagnosis.service.UdsDiagnosisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * P0-3: uds-responses 消费者
 *
 * service-access 将车端 MQTT 响应转发到 Kafka uds-responses，
 * 本组件按 traceId 关联回等待中的 CompletableFuture，唤醒 Service 层返回真实结果。
 *
 * 消息格式（service-access 封装的 envelope）:
 *   {"vin":"...","source":"mqtt","payload":"{\"traceId\":\"...\",\"serviceId\":25,...}"}
 */
@Component
public class UdsResponseHandler {
    private static final Logger log = LoggerFactory.getLogger(UdsResponseHandler.class);

    private final UdsDiagnosisService udsDiagnosisService;

    public UdsResponseHandler(UdsDiagnosisService udsDiagnosisService) {
        this.udsDiagnosisService = udsDiagnosisService;
    }

    @KafkaListener(topics = "${kafka.topics.uds-responses:uds-responses}", groupId = "diagnosis-service")
    public void consume(String message) {
        try {
            JSONObject envelope = JSON.parseObject(message);
            if (envelope == null) {
                log.warn("Skip invalid UDS response envelope: {}", message);
                return;
            }
            String payload = envelope.getString("payload");
            if (!StringUtils.hasText(payload)) {
                log.warn("Skip UDS response without payload: {}", message);
                return;
            }
            UdsResponse response = JSON.parseObject(payload, UdsResponse.class);
            if (response == null || !StringUtils.hasText(response.getTraceId())) {
                log.warn("Skip UDS response without traceId: {}", payload);
                return;
            }
            this.udsDiagnosisService.completeResponse(response);
        }
        catch (Exception e) {
            log.error("Failed to consume UDS response", e);
        }
    }
}
