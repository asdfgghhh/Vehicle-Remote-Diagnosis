package com.vrd.dbc.kafka;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vrd.common.message.DbcDispatchCallback;
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
 * DBC 下发回调 Kafka 消费者（service-access -> service-dbc）
 *
 * <p>消费 dbc-dispatch-callback topic 消息，更新 dispatch_log 状态：
 * <ul>
 *   <li>SENT   -> status=2（MQTT broker 已接收，QoS1 PUBACK 确认）</li>
 *   <li>FAILED -> status=3（MQTT 发送失败，等待补偿任务重试）</li>
 * </ul>
 *
 * <p>通过 traceId 精确匹配 dispatch_log 记录，避免误更新。
 */
@Component
public class DbcDispatchCallbackKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(DbcDispatchCallbackKafkaConsumer.class);

    @Autowired
    private DispatchLogMapper dispatchLogMapper;

    @Value("${kafka.topics.dbc-dispatch-callback:dbc-dispatch-callback}")
    private String dbcDispatchCallbackTopic;

    @KafkaListener(topics = "${kafka.topics.dbc-dispatch-callback:dbc-dispatch-callback}", groupId = "dbc-dispatch-callback")
    public void consume(String message) {
        try {
            DbcDispatchCallback callback = JSON.parseObject(message, DbcDispatchCallback.class);
            if (callback == null || callback.getTraceId() == null) {
                log.warn("Skip invalid DBC dispatch callback: {}", message);
                return;
            }

            // 通过 traceId 精确匹配 dispatch_log
            LambdaQueryWrapper<DispatchLog> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DispatchLog::getVin, callback.getVin());
            wrapper.like(DispatchLog::getResult, "traceId=" + callback.getTraceId());
            wrapper.orderByDesc(DispatchLog::getCreateTime);
            wrapper.last("LIMIT 1");
            DispatchLog dispatchLog = this.dispatchLogMapper.selectOne(wrapper);
            if (dispatchLog == null) {
                log.warn("No dispatch_log found for callback: traceId={}, vin={}",
                        callback.getTraceId(), callback.getVin());
                return;
            }

            // 仅处理 PENDING 状态的记录，避免重复更新
            if (dispatchLog.getStatus() != null && dispatchLog.getStatus() != 1) {
                log.info("Dispatch_log already processed, skip callback: traceId={}, currentStatus={}",
                        callback.getTraceId(), dispatchLog.getStatus());
                return;
            }

            String status = callback.getStatus();
            if ("SENT".equalsIgnoreCase(status)) {
                dispatchLog.setStatus(2);
                dispatchLog.setResult("SENT: " + callback.getMessage() + " (traceId=" + callback.getTraceId() + ")");
            } else if ("FAILED".equalsIgnoreCase(status)) {
                dispatchLog.setStatus(3);
                dispatchLog.setResult("FAILED: " + callback.getMessage() + " (traceId=" + callback.getTraceId() + ")");
            } else {
                log.info("Skip unknown callback status: traceId={}, status={}",
                        callback.getTraceId(), status);
                return;
            }
            dispatchLog.setUpdateTime(LocalDateTime.now());
            this.dispatchLogMapper.updateById(dispatchLog);

            log.info("DBC dispatch callback processed: traceId={}, vin={}, status={}",
                    callback.getTraceId(), callback.getVin(), status);
        } catch (Exception e) {
            log.error("Failed to process DBC dispatch callback", e);
        }
    }
}
