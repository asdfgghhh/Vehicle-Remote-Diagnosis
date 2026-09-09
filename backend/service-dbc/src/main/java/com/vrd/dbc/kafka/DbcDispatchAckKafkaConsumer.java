package com.vrd.dbc.kafka;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vrd.dbc.entity.DispatchLog;
import com.vrd.dbc.mapper.DispatchLogMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * DBC 下发 ACK Kafka 消费者（service-access -> service-dbc）
 *
 * <p>消费 dbc-dispatch-ack topic 消息，更新 dispatch_log 状态：
 * <p>DOWNLOADED -> 4（已下载），APPLIED -> 5（已应用），FAILED -> 6（车端应用失败）
 *
 * <p>消息体格式：
 * <pre>
 * {
 *   "vin": "...",
 *   "source": "mqtt",
 *   "payload": "{\"traceId\":\"abc\",\"vin\":\"...\",\"status\":\"APPLIED\",\"message\":\"...\",\"timestamp\":...}"
 * }
 * </pre>
 */
@Component
public class DbcDispatchAckKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(DbcDispatchAckKafkaConsumer.class);

    @Autowired
    private DispatchLogMapper dispatchLogMapper;

    @Value("${kafka.topics.dbc-dispatch-ack:dbc-dispatch-ack}")
    private String dbcDispatchAckTopic;

    @KafkaListener(topics = "${kafka.topics.dbc-dispatch-ack:dbc-dispatch-ack}", groupId = "dbc-dispatch-ack")
    public void consume(String message) {
        try {
            JSONObject envelope = JSON.parseObject(message);
            if (envelope == null) {
                return;
            }
            String payloadStr = envelope.getString("payload");
            JSONObject payload = JSON.parseObject(payloadStr);
            if (payload == null) {
                log.warn("Skip invalid DBC dispatch ACK: {}", message);
                return;
            }
            String traceId = payload.getString("traceId");
            String vin = payload.getString("vin");
            String status = payload.getString("status");
            String result = payload.getString("message");

            // 根据 traceId 查找 dispatch_log
            LambdaQueryWrapper<DispatchLog> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DispatchLog::getVin, vin);
            wrapper.like(DispatchLog::getResult, "traceId=" + traceId);
            wrapper.orderByDesc(DispatchLog::getCreateTime);
            wrapper.last("LIMIT 1");
            DispatchLog dispatchLog = this.dispatchLogMapper.selectOne(wrapper);
            if (dispatchLog == null) {
                log.warn("No dispatch_log found for traceId={}, vin={}", traceId, vin);
                return;
            }

            // 根据车端上报状态映射到 dispatch_log.status
            int newStatus;
            String statusDesc;
            if ("DOWNLOADED".equalsIgnoreCase(status)) {
                newStatus = 4;
                statusDesc = "DOWNLOADED";
            } else if ("APPLIED".equalsIgnoreCase(status)) {
                newStatus = 5;
                statusDesc = "APPLIED";
            } else if ("FAILED".equalsIgnoreCase(status)) {
                newStatus = 6;
                statusDesc = "FAILED";
            } else {
                log.info("Skip non-terminal DBC dispatch ACK: traceId={}, status={}", traceId, status);
                return;
            }
            dispatchLog.setStatus(newStatus);
            dispatchLog.setResult(statusDesc + ": " + (result != null ? result : "") + " (traceId=" + traceId + ")");
            dispatchLog.setUpdateTime(LocalDateTime.now());
            this.dispatchLogMapper.updateById(dispatchLog);
            log.info("DBC dispatch ACK processed: traceId={}, vin={}, status={}, dispatchLogId={}",
                    traceId, vin, statusDesc, dispatchLog.getId());
        } catch (Exception e) {
            log.error("Failed to process DBC dispatch ACK", e);
        }
    }
}
