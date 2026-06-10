package com.vrd.ecu.service.impl;

import com.vrd.common.exception.BusinessException;
import com.vrd.ecu.config.ClickHouseHttpClient;
import com.vrd.ecu.config.ClickHouseProperties;
import com.vrd.ecu.dto.EcuLogRecord;
import com.vrd.ecu.dto.PageResult;
import com.vrd.ecu.service.EcuLogClickHouseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@ConditionalOnProperty(name = "clickhouse.enabled", havingValue = "true", matchIfMissing = true)
public class EcuLogClickHouseServiceImpl implements EcuLogClickHouseService {

    private static final DateTimeFormatter CH_DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final String SELECT_COLUMNS = """
            SELECT id, vin, ecu_type, log_start_time, log_end_time,
                   upload_start_time, upload_end_time, storage_address, storage_key,
                   storage_type, file_name, file_size, file_md5
            FROM ecu_log_records
            """;

    @Autowired
    private ClickHouseHttpClient clickHouseHttpClient;

    @Autowired
    private ClickHouseProperties properties;

    @Override
    public PageResult<EcuLogRecord> search(Integer current, Integer size, String vin, String ecuType,
                                           LocalDateTime startTime, LocalDateTime endTime) {
        long startMs = System.currentTimeMillis();
        int pageSize = Math.min(size == null ? 10 : size, properties.getMaxPageSize());
        int pageCurrent = current == null || current < 1 ? 1 : current;

        LocalDateTime start = startTime != null ? startTime : LocalDateTime.now().minusDays(properties.getDefaultDays());
        LocalDateTime end = endTime != null ? endTime : LocalDateTime.now();
        if (start.isAfter(end)) {
            throw new BusinessException("开始时间不能晚于结束时间");
        }

        String where = buildWhereClause(start, end, vin, ecuType);
        int offset = (pageCurrent - 1) * pageSize;

        try {
            long total = clickHouseHttpClient.queryCount("SELECT count() FROM ecu_log_records" + where);
            List<EcuLogRecord> records = clickHouseHttpClient.queryRecords(
                    SELECT_COLUMNS + where + " ORDER BY upload_start_time DESC LIMIT "
                            + pageSize + " OFFSET " + offset);

            long elapsed = System.currentTimeMillis() - startMs;
            log.info("ClickHouse log record search {}ms, total={}", elapsed, total);
            return PageResult.of(records, total, pageCurrent, pageSize);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("ClickHouse log record search failed, where={}", where, e);
            throw new BusinessException("日志查询失败: " + e.getMessage());
        }
    }

    @Override
    public void insertRecord(EcuLogRecord record) {
        String sql = """
                INSERT INTO ecu_log_records
                (id, vin, ecu_type, log_start_time, log_end_time, upload_start_time, upload_end_time,
                 storage_address, storage_key, storage_type, file_name, file_size, file_md5)
                VALUES (%d, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %d, %s)
                """.formatted(
                record.getId(),
                literal(record.getVin()),
                literal(record.getEcuType()),
                dateTimeLiteral(record.getLogStartTime()),
                dateTimeLiteral(record.getLogEndTime()),
                dateTimeLiteral(record.getUploadStartTime()),
                dateTimeLiteral(record.getUploadEndTime()),
                literal(record.getStorageAddress()),
                literal(record.getStorageKey()),
                literal(record.getStorageType()),
                literal(record.getFileName()),
                record.getFileSize() != null ? record.getFileSize() : 0L,
                literal(record.getFileMd5()));
        clickHouseHttpClient.execute(sql);
    }

    @Override
    public EcuLogRecord getById(Long id) {
        List<EcuLogRecord> list = clickHouseHttpClient.queryRecords(
                SELECT_COLUMNS + " WHERE id = " + id + " LIMIT 1");
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public boolean existsByMd5(String fileMd5) {
        if (!StringUtils.hasText(fileMd5)) {
            return false;
        }
        return clickHouseHttpClient.queryCount(
                "SELECT count() FROM ecu_log_records WHERE file_md5 = " + literal(fileMd5.trim())) > 0;
    }

    private String buildWhereClause(LocalDateTime start, LocalDateTime end, String vin, String ecuType) {
        StringBuilder where = new StringBuilder(" WHERE upload_start_time >= ")
                .append(dateTimeLiteral(start))
                .append(" AND upload_start_time <= ")
                .append(dateTimeLiteral(end));

        if (StringUtils.hasText(vin)) {
            where.append(" AND positionCaseInsensitive(vin, ").append(literal(vin.trim())).append(") > 0");
        }
        if (StringUtils.hasText(ecuType)) {
            where.append(" AND ecu_type = ").append(literal(ecuType.trim()));
        }
        return where.toString();
    }

    private String dateTimeLiteral(LocalDateTime dateTime) {
        return "toDateTime(" + literal(dateTime.format(CH_DATETIME)) + ")";
    }

    private String literal(String value) {
        if (value == null) {
            return "''";
        }
        return "'" + value.replace("\\", "\\\\").replace("'", "\\'") + "'";
    }
}
