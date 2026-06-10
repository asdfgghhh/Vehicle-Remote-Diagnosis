package com.vrd.ecu.config;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.vrd.common.exception.BusinessException;
import com.vrd.ecu.dto.EcuLogRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

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

/**
 * 通过 ClickHouse HTTP 接口访问，规避 JDBC 压缩/RowBinary 兼容问题。
 */
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
        postSql(sql, null);
    }

    public long queryCount(String sql) {
        String body = postSql(sql, properties.getDatabase());
        if (!StringUtils.hasText(body)) {
            return 0L;
        }
        return Long.parseLong(body.trim());
    }

    public List<EcuLogRecord> queryRecords(String sql) {
        String body = postSql(sql + " FORMAT JSONEachRow", properties.getDatabase());
        if (!StringUtils.hasText(body)) {
            return List.of();
        }
        List<EcuLogRecord> records = new ArrayList<>();
        for (String line : body.split("\n")) {
            if (!StringUtils.hasText(line)) {
                continue;
            }
            records.add(mapRecord(JSON.parseObject(line)));
        }
        return records;
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
            log.error("ClickHouse HTTP request failed: {}", sql, e);
            throw new BusinessException("ClickHouse 连接失败: " + e.getMessage());
        }
    }

    private String basicAuth() {
        String credentials = properties.getUsername() + ":" + properties.getPassword();
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }

    private EcuLogRecord mapRecord(JSONObject json) {
        EcuLogRecord record = new EcuLogRecord();
        record.setId(json.getLong("id"));
        record.setVin(json.getString("vin"));
        record.setEcuType(json.getString("ecu_type"));
        record.setLogStartTime(parseDateTime(json.getString("log_start_time")));
        record.setLogEndTime(parseDateTime(json.getString("log_end_time")));
        record.setUploadStartTime(parseDateTime(json.getString("upload_start_time")));
        record.setUploadEndTime(parseDateTime(json.getString("upload_end_time")));
        record.setStorageAddress(json.getString("storage_address"));
        record.setStorageKey(json.getString("storage_key"));
        record.setStorageType(json.getString("storage_type"));
        record.setFileName(json.getString("file_name"));
        record.setFileSize(json.getLong("file_size"));
        record.setFileMd5(json.getString("file_md5"));
        return record;
    }

    private LocalDateTime parseDateTime(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return LocalDateTime.parse(value.replace('T', ' ').substring(0, 19), CH_DATETIME);
    }
}
