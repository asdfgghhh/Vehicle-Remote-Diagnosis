package com.vrd.dbc.kafka;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vrd.common.message.DbcDispatchMessage;
import com.vrd.common.storage.StorageKeyUtils;
import com.vrd.common.storage.StorageService;
import com.vrd.dbc.entity.DbcFile;
import com.vrd.dbc.entity.DispatchLog;
import com.vrd.dbc.mapper.DispatchLogMapper;
import com.vrd.dbc.service.DbcFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DBC 下发补偿定时任务
 *
 * <p>定时扫描 PENDING 状态超时的 dispatch_log 记录（默认5分钟未收到回调），
 * 重新构建指令并通过 Kafka 发送，实现最终一致性。
 *
 * <p>最大重试次数默认5次，超过后标记 FAILED。
 */
@Component
public class DispatchRetryScheduler {

    private static final Logger log = LoggerFactory.getLogger(DispatchRetryScheduler.class);

    @Autowired
    private DispatchLogMapper dispatchLogMapper;

    @Autowired
    private DbcDispatchKafkaProducer dbcDispatchKafkaProducer;

    @Autowired
    private StorageService storageService;

    @Autowired
    private DbcFileService dbcFileService;

    /** PENDING 超时阈值（分钟） */
    @Value("${dispatch.retry.timeout-minutes:5}")
    private int timeoutMinutes;

    /** 最大重试次数 */
    @Value("${dispatch.retry.max-count:5}")
    private int maxRetryCount;

    /** 单次扫描批量大小 */
    @Value("${dispatch.retry.batch-size:200}")
    private int batchSize;

    /**
     * 每60秒扫描一次 PENDING 超时记录
     */
    @Scheduled(fixedDelayString = "${dispatch.retry.scan-interval-ms:60000}")
    public void retryPendingDispatches() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(timeoutMinutes);

        LambdaQueryWrapper<DispatchLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DispatchLog::getStatus, 1)             // PENDING
               .lt(DispatchLog::getDispatchTime, cutoff)   // 超时
               .lt(DispatchLog::getRetryCount, maxRetryCount) // 未超过最大重试
               .orderByAsc(DispatchLog::getDispatchTime)
               .last("LIMIT " + batchSize);

        List<DispatchLog> pendingLogs = this.dispatchLogMapper.selectList(wrapper);
        if (pendingLogs.isEmpty()) {
            return;
        }

        log.info("Dispatch retry scan found {} pending records", pendingLogs.size());

        for (DispatchLog dispatchLog : pendingLogs) {
            try {
                // 重试次数已达上限 → 标记 FAILED
                if (dispatchLog.getRetryCount() != null && dispatchLog.getRetryCount() >= maxRetryCount) {
                    dispatchLog.setStatus(3);
                    dispatchLog.setResult("FAILED: max retries exceeded (" + maxRetryCount + ")");
                    dispatchLog.setUpdateTime(LocalDateTime.now());
                    this.dispatchLogMapper.updateById(dispatchLog);
                    log.warn("Dispatch log marked FAILED after max retries: id={}, vin={}",
                            dispatchLog.getId(), dispatchLog.getVin());
                    continue;
                }

                // 重新构建并发送指令
                DbcFile dbcFile = this.dbcFileService.getById(dispatchLog.getDbcFileId());
                if (dbcFile == null) {
                    dispatchLog.setStatus(3);
                    dispatchLog.setResult("FAILED: DBC file not found");
                    dispatchLog.setUpdateTime(LocalDateTime.now());
                    this.dispatchLogMapper.updateById(dispatchLog);
                    continue;
                }

                DbcDispatchMessage message = this.buildRetryMessage(dbcFile, dispatchLog.getVin());
                this.dbcDispatchKafkaProducer.sendDispatchCommand(message);

                // 更新重试次数和结果
                int newRetryCount = (dispatchLog.getRetryCount() == null ? 0 : dispatchLog.getRetryCount()) + 1;
                dispatchLog.setRetryCount(newRetryCount);
                dispatchLog.setResult("PENDING(retry " + newRetryCount + "): traceId=" + message.getTraceId());
                dispatchLog.setDispatchTime(LocalDateTime.now());
                this.dispatchLogMapper.updateById(dispatchLog);

                log.info("Dispatch retried: id={}, vin={}, retryCount={}, newTraceId={}",
                        dispatchLog.getId(), dispatchLog.getVin(), newRetryCount, message.getTraceId());
            } catch (Exception e) {
                log.error("Dispatch retry failed: id={}, vin={}", dispatchLog.getId(), dispatchLog.getVin(), e);
            }
        }
    }

    /**
     * 构建重试指令消息
     */
    private DbcDispatchMessage buildRetryMessage(DbcFile dbcFile, String vin) throws Exception {
        String objectKey = StorageKeyUtils.resolveObjectKey(
                dbcFile.getStorageKey(), dbcFile.getFilePath(), dbcFile.getStorageAddress(), this.storageService);
        String downloadUrl = this.storageService.getUrl(objectKey);

        DbcDispatchMessage message = new DbcDispatchMessage();
        message.setTraceId(java.util.UUID.randomUUID().toString().replace("-", ""));
        message.setVin(vin);
        message.setDbcFileId(dbcFile.getId());
        message.setVersion(dbcFile.getVersion());
        message.setDownloadUrl(downloadUrl);
        message.setFileSize(dbcFile.getFileSize());
        message.setTargetPath("/etc/vrd/dbc/" + (dbcFile.getModelName() != null ? dbcFile.getModelName() : "default") + ".dbc");
        message.setImmediateApply(Boolean.TRUE);
        message.setTimestamp(System.currentTimeMillis());
        return message;
    }
}
