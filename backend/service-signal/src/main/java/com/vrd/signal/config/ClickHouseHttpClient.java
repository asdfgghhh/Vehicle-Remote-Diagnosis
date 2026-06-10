package com.vrd.signal.config;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.vrd.common.exception.BusinessException;
import com.vrd.signal.entity.VehicleSignal;
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

    public List<VehicleSignal> querySignals(String sql) {
        String body = postSql(sql + " FORMAT JSONEachRow", properties.getDatabase());
        if (!StringUtils.hasText(body)) {
            return List.of();
        }
        List<VehicleSignal> records = new ArrayList<>();
        for (String line : body.split("\n")) {
            if (!StringUtils.hasText(line)) {
                continue;
            }
            records.add(mapSignal(JSON.parseObject(line)));
        }
        return records;
    }

    public void insertSignals(List<VehicleSignal> signals) {
        if (signals.isEmpty()) {
            return;
        }
        int batchSize = Math.max(properties.getInsertBatchSize(), 1);
        for (int i = 0; i < signals.size(); i += batchSize) {
            int end = Math.min(i + batchSize, signals.size());
            insertSignalChunk(signals.subList(i, end));
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
            log.error("ClickHouse HTTP request failed: {}", sql.length() > 200 ? sql.substring(0, 200) + "..." : sql, e);
            throw new BusinessException("ClickHouse 连接失败: " + e.getMessage());
        }
    }

    private String basicAuth() {
        String credentials = properties.getUsername() + ":" + properties.getPassword();
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }

    private VehicleSignal mapSignal(JSONObject json) {
        VehicleSignal signal = new VehicleSignal();
        signal.setId(json.getLong("id"));
        signal.setVin(json.getString("vin"));
        signal.setVehicleId(json.getLong("vehicle_id"));
        signal.setSignalName(json.getString("signal_name"));
        signal.setSignalValue(json.getString("signal_value"));
        signal.setNumericValue(BigDecimal.valueOf(json.getDoubleValue("numeric_value")));
        signal.setUnit(json.getString("unit"));
        signal.setTimestamp(json.getLong("timestamp"));
        signal.setSignalTime(parseDateTime(json.getString("signal_time")));
        signal.setMessageName(json.getString("message_name"));
        signal.setMessageId(json.getInteger("message_id"));
        signal.setCreateTime(parseDateTime(json.getString("create_time")));
        return signal;
    }

    private LocalDateTime parseDateTime(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return LocalDateTime.parse(value.replace('T', ' ').substring(0, 19), CH_DATETIME);
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
