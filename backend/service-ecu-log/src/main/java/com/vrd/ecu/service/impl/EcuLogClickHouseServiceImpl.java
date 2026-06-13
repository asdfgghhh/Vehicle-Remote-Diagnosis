package com.vrd.ecu.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.vrd.common.bigdata.BigDataClient;
import com.vrd.common.exception.BusinessException;
import com.vrd.ecu.dto.EcuLogRecord;
import com.vrd.ecu.dto.PageResult;
import com.vrd.ecu.service.EcuLogClickHouseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
public class EcuLogClickHouseServiceImpl implements EcuLogClickHouseService {

    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final String SELECT_COLUMNS = """
            SELECT id, vin, ecu_type, log_start_time, log_end_time,
                   upload_start_time, upload_end_time, storage_address, storage_key,
                   storage_type, file_name, file_size, file_md5
            FROM ecu_log_records
            """;

    @Autowired
    private BigDataClient bigDataClient;

    @Override
    public PageResult<EcuLogRecord> search(Integer current, Integer size, String vin, String ecuType,
                                           LocalDateTime startTime, LocalDateTime endTime) {
        long startMs = System.currentTimeMillis();
        int pageSize = Math.min(size == null ? 10 : size, 500);
        int pageCurrent = current == null || current < 1 ? 1 : current;

        LocalDateTime start = startTime != null ? startTime : LocalDateTime.now().minusDays(30);
        LocalDateTime end = endTime != null ? endTime : LocalDateTime.now();
        if (start.isAfter(end)) {
            throw new BusinessException("开始时间不能晚于结束时间");
        }

        String where = buildWhereClause(start, end, vin, ecuType);
        int offset = (pageCurrent - 1) * pageSize;

        try {
            long total = bigDataClient.queryCount("SELECT count(*) FROM ecu_log_records" + where);
            List<EcuLogRecord> records = bigDataClient.queryForList(
                    SELECT_COLUMNS + where + " ORDER BY upload_start_time DESC LIMIT "
                            + pageSize + " OFFSET " + offset, EcuLogRecord.class);

            long elapsed = System.currentTimeMillis() - startMs;
            log.info("BigData log record search {}ms, total={}", elapsed, total);
            return PageResult.of(records, total, pageCurrent, pageSize);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("BigData log record search failed, where={}", where, e);
            throw new BusinessException("日志查询失败: " + e.getMessage());
        }
    }

    @Override
    public void insertRecord(EcuLogRecord record) {
        JSONObject json = new JSONObject();
        json.put("id", record.getId());
        json.put("vin", record.getVin());
        json.put("ecu_type", record.getEcuType());
        json.put("log_start_time", formatDateTime(record.getLogStartTime()));
        json.put("log_end_time", formatDateTime(record.getLogEndTime()));
        json.put("upload_start_time", formatDateTime(record.getUploadStartTime()));
        json.put("upload_end_time", formatDateTime(record.getUploadEndTime()));
        json.put("storage_address", record.getStorageAddress());
        json.put("storage_key", record.getStorageKey());
        json.put("storage_type", record.getStorageType());
        json.put("file_name", record.getFileName());
        json.put("file_size", record.getFileSize() != null ? record.getFileSize() : 0L);
        json.put("file_md5", record.getFileMd5());
        bigDataClient.insertJson("ecu_log_records", List.of(json));
    }

    @Override
    public EcuLogRecord getById(Long id) {
        List<EcuLogRecord> list = bigDataClient.queryForList(
                SELECT_COLUMNS + " WHERE id = " + id + " LIMIT 1", EcuLogRecord.class);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public boolean existsByMd5(String fileMd5) {
        if (!StringUtils.hasText(fileMd5)) {
            return false;
        }
        return bigDataClient.queryCount(
                "SELECT count(*) FROM ecu_log_records WHERE file_md5 = '" + fileMd5.trim() + "'") > 0;
    }

    private String buildWhereClause(LocalDateTime start, LocalDateTime end, String vin, String ecuType) {
        StringBuilder where = new StringBuilder(" WHERE upload_start_time >= '")
                .append(start.format(DATETIME_FORMAT))
                .append("' AND upload_start_time <= '")
                .append(end.format(DATETIME_FORMAT))
                .append("'");

        if (StringUtils.hasText(vin)) {
            where.append(" AND vin LIKE '%").append(vin.trim()).append("%'");
        }
        if (StringUtils.hasText(ecuType)) {
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