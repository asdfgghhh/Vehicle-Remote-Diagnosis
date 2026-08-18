/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.alibaba.fastjson2.JSONObject
 *  com.vrd.common.bigdata.BigDataClient
 *  com.vrd.common.exception.BusinessException
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.stereotype.Service
 *  org.springframework.util.StringUtils
 */
package com.vrd.ecu.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.vrd.common.bigdata.BigDataClient;
import com.vrd.common.exception.BusinessException;
import com.vrd.ecu.dto.EcuLogRecord;
import com.vrd.ecu.dto.PageResult;
import com.vrd.ecu.service.EcuLogClickHouseService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class EcuLogClickHouseServiceImpl
implements EcuLogClickHouseService {
    private static final Logger log = LoggerFactory.getLogger(EcuLogClickHouseServiceImpl.class);
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String SELECT_COLUMNS = "SELECT id, vin, ecu_type, log_start_time, log_end_time,\n       upload_start_time, upload_end_time, storage_address, storage_key,\n       storage_type, file_name, file_size, file_md5\nFROM ecu_log_records\n";
    @Autowired
    private BigDataClient bigDataClient;

    @Override
    public PageResult<EcuLogRecord> search(Integer current, Integer size, String vin, String ecuType, LocalDateTime startTime, LocalDateTime endTime) {
        LocalDateTime end;
        long startMs = System.currentTimeMillis();
        int pageSize = Math.min(size == null ? 10 : size, 500);
        int pageCurrent = current == null || current < 1 ? 1 : current;
        LocalDateTime start = startTime != null ? startTime : LocalDateTime.now().minusDays(30L);
        LocalDateTime localDateTime = end = endTime != null ? endTime : LocalDateTime.now();
        if (start.isAfter(end)) {
            throw new BusinessException("\u5f00\u59cb\u65f6\u95f4\u4e0d\u80fd\u665a\u4e8e\u7ed3\u675f\u65f6\u95f4");
        }
        String where = this.buildWhereClause(start, end, vin, ecuType);
        int offset = (pageCurrent - 1) * pageSize;
        try {
            long total = this.bigDataClient.queryCount("SELECT count(*) FROM ecu_log_records" + where);
            List records = this.bigDataClient.queryForList(SELECT_COLUMNS + where + " ORDER BY upload_start_time DESC LIMIT " + pageSize + " OFFSET " + offset, EcuLogRecord.class);
            long elapsed = System.currentTimeMillis() - startMs;
            log.info("BigData log record search {}ms, total={}", (Object)elapsed, (Object)total);
            return PageResult.of(records, total, pageCurrent, pageSize);
        }
        catch (BusinessException e) {
            throw e;
        }
        catch (Exception e) {
            log.error("BigData log record search failed, where={}", (Object)where, (Object)e);
            throw new BusinessException("\u65e5\u5fd7\u67e5\u8be2\u5931\u8d25: " + e.getMessage());
        }
    }

    @Override
    public void insertRecord(EcuLogRecord record) {
        JSONObject json = new JSONObject();
        json.put((Object)"id", (Object)record.getId());
        json.put((Object)"vin", (Object)record.getVin());
        json.put((Object)"ecu_type", (Object)record.getEcuType());
        json.put((Object)"log_start_time", (Object)this.formatDateTime(record.getLogStartTime()));
        json.put((Object)"log_end_time", (Object)this.formatDateTime(record.getLogEndTime()));
        json.put((Object)"upload_start_time", (Object)this.formatDateTime(record.getUploadStartTime()));
        json.put((Object)"upload_end_time", (Object)this.formatDateTime(record.getUploadEndTime()));
        json.put((Object)"storage_address", (Object)record.getStorageAddress());
        json.put((Object)"storage_key", (Object)record.getStorageKey());
        json.put((Object)"storage_type", (Object)record.getStorageType());
        json.put((Object)"file_name", (Object)record.getFileName());
        json.put((Object)"file_size", (Object)(record.getFileSize() != null ? record.getFileSize() : 0L));
        json.put((Object)"file_md5", (Object)record.getFileMd5());
        this.bigDataClient.insertJson("ecu_log_records", List.of(json));
    }

    @Override
    public EcuLogRecord getById(Long id) {
        List list = this.bigDataClient.queryForList("SELECT id, vin, ecu_type, log_start_time, log_end_time,\n       upload_start_time, upload_end_time, storage_address, storage_key,\n       storage_type, file_name, file_size, file_md5\nFROM ecu_log_records\n WHERE id = " + id + " LIMIT 1", EcuLogRecord.class);
        return list.isEmpty() ? null : (EcuLogRecord)list.get(0);
    }

    @Override
    public boolean existsByMd5(String fileMd5) {
        if (!StringUtils.hasText((String)fileMd5)) {
            return false;
        }
        return this.bigDataClient.queryCount("SELECT count(*) FROM ecu_log_records WHERE file_md5 = '" + fileMd5.trim() + "'") > 0L;
    }

    private String buildWhereClause(LocalDateTime start, LocalDateTime end, String vin, String ecuType) {
        StringBuilder where = new StringBuilder(" WHERE upload_start_time >= '").append(start.format(DATETIME_FORMAT)).append("' AND upload_start_time <= '").append(end.format(DATETIME_FORMAT)).append("'");
        if (StringUtils.hasText((String)vin)) {
            where.append(" AND vin LIKE '%").append(vin.trim()).append("%'");
        }
        if (StringUtils.hasText((String)ecuType)) {
            where.append(" AND ecu_type = '").append(ecuType.trim()).append("'");
        }
        return where.toString();
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return LocalDateTime.now().format(DATETIME_FORMAT);
        }
        return dateTime.format(DATETIME_FORMAT);
    }
}

