package com.vrd.access.config;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.vrd.access.dto.EcuLogRecord;
import com.vrd.access.entity.VehicleSignal;
import com.vrd.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
@ConditionalOnProperty(name = "clickhouse.enabled", havingValue = "true", matchIfMissing = true)
public class ClickHouseHttpClient {

    private static final DateTimeFormatter CH_DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ClickHouseProperties properties;
    private final HttpClient httpClient;

    public ClickHouseHttpClient(ClickHouseProperties properties) {
        this.properties = properties;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(properties.getConnectionTimeout()))
                .build();
    }

    public void execute(String sql) {
        postSql(sql, properties.getDatabase());
    }

    public long queryCount(String sql) {
        String body = postSql(sql, properties.getDatabase());
        if (!StringUtils.hasText(body)) {
            return 0L;
        }
        return Long.parseLong(body.trim());
    }

    public void insertEcuLogRecord(EcuLogRecord record) {
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
        execute(sql);
    }

    public boolean existsByMd5(String fileMd5) {
        if (!StringUtils.hasText(fileMd5)) {
            return false;
        }
        return queryCount("SELECT count() FROM ecu_log_records WHERE file_md5 = " + literal(fileMd5.trim())) > 0;
    }

    public void insertSignals(List<VehicleSignal> signals) {
        if (signals.isEmpty()) {
            return;
        }
        int batchSize = Math.max(properties.getInsertBatchSize(), 1);
        for (int i = 0; i < signals.size(); i += batchSize) {
            insertSignalChunk(signals.subList(i, Math.min(i + batchSize, signals.size())));
        }
    }

    private void insertSignalChunk(List<VehicleSignal> signals) {
        StringBuilder rows = new StringBuilder();
        for (VehicleSignal signal : signals) {
            if (signal.getId() == null) {
                signal.setId(generateId());
            }
            JSONObject row = new JSONObject();
            row.put("id", signal.getId());
            row.put("vin", signal.getVin() != null ? signal.getVin() : "");
            row.put("vehicle_id", signal.getVehicleId() != null ? signal.getVehicleId() : 0L);
            row.put("signal_name", signal.getSignalName() != null ? signal.getSignalName() : "");
            row.put("signal_value", signal.getSignalValue() != null ? signal.getSignalValue() : "");
            row.put("numeric_value", signal.getNumericValue() != null ? signal.getNumericValue().doubleValue() : 0D);
            row.put("unit", signal.getUnit() != null ? signal.getUnit() : "");
            row.put("timestamp", signal.getTimestamp() != null ? signal.getTimestamp() : 0L);
            row.put("signal_time", formatDateTime(signal.getSignalTime()));
            row.put("message_name", signal.getMessageName() != null ? signal.getMessageName() : "");
            row.put("message_id", signal.getMessageId() != null ? signal.getMessageId() : 0);
            rows.append(row.toJSONString()).append('\n');
        }
        postSql("INSERT INTO vehicle_signal_records FORMAT JSONEachRow\n" + rows, properties.getDatabase());
    }

    private String postSql(String sql, String database) {
        try {
            String db = StringUtils.hasText(database) ? database : "default";
            URI uri = URI.create(String.format("http://%s:%d/?database=%s",
                    properties.getHost(), properties.getPort(), db));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .timeout(Duration.ofMillis(properties.getSocketTimeout()))
                    .header("Authorization", basicAuth())
                    .header("Content-Type", "text/plain; charset=utf-8")
                    .POST(HttpRequest.BodyPublishers.ofString(sql, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new BusinessException("ClickHouse 执行失败: " + response.body());
            }
            return response.body();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("ClickHouse HTTP request failed", e);
            throw new BusinessException("ClickHouse 连接失败: " + e.getMessage());
        }
    }

    private String basicAuth() {
        String credentials = properties.getUsername() + ":" + properties.getPassword();
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }

    private String dateTimeLiteral(LocalDateTime dateTime) {
        return "toDateTime(" + literal(formatDateTime(dateTime)) + ")";
    }

    private String literal(String value) {
        if (value == null) {
            return "''";
        }
        return "'" + value.replace("\\", "\\\\").replace("'", "\\'") + "'";
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return LocalDateTime.now().format(CH_DATETIME);
        }
        return dateTime.format(CH_DATETIME);
    }

    private long generateId() {
        return System.currentTimeMillis() * 1000L + ThreadLocalRandom.current().nextInt(1000);
    }
}
