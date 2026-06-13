package com.vrd.common.bigdata.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.vrd.common.bigdata.BigDataClient;
import com.vrd.common.bigdata.BigDataProperties;
import com.vrd.common.bigdata.BigDataStorageType;
import com.vrd.common.exception.BusinessException;
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
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component("clickhouseBigDataClient")
@ConditionalOnProperty(name = "bigdata.type", havingValue = "CLICKHOUSE", matchIfMissing = true)
public class ClickHouseBigDataClient implements BigDataClient {

    private final BigDataProperties properties;
    private final HttpClient httpClient;

    public ClickHouseBigDataClient(BigDataProperties properties) {
        this.properties = properties;
        BigDataProperties.ClickHouseConfig config = properties.getClickhouse();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(config.getConnectionTimeout()))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    @Override
    public void execute(String sql) {
        execute(sql, properties.getClickhouse().getDatabase());
    }

    @Override
    public void execute(String sql, String database) {
        postSql(sql, database);
    }

    @Override
    public long queryCount(String sql) {
        String body = postSql(sql, properties.getClickhouse().getDatabase());
        if (!StringUtils.hasText(body)) {
            return 0L;
        }
        return Long.parseLong(body.trim());
    }

    @Override
    public String query(String sql) {
        return postSql(sql, properties.getClickhouse().getDatabase());
    }

    @Override
    public String query(String sql, String database) {
        return postSql(sql, database);
    }

    @Override
    public <T> List<T> queryForList(String sql, Class<T> clazz) {
        String body = postSql(sql + " FORMAT JSONEachRow", properties.getClickhouse().getDatabase());
        if (!StringUtils.hasText(body)) {
            return List.of();
        }
        return JSON.parseArray(body, clazz);
    }

    @Override
    public JSONObject queryForJson(String sql) {
        String body = postSql(sql + " FORMAT JSON", properties.getClickhouse().getDatabase());
        if (!StringUtils.hasText(body)) {
            return new JSONObject();
        }
        return JSON.parseObject(body);
    }

    @Override
    public boolean exists(String tableName, String condition) {
        String sql = String.format("SELECT count() FROM %s WHERE %s", tableName, condition);
        return queryCount(sql) > 0;
    }

    @Override
    public void insertJson(String tableName, List<JSONObject> rows) {
        if (rows.isEmpty()) {
            return;
        }
        BigDataProperties.ClickHouseConfig config = properties.getClickhouse();
        int batchSize = Math.max(config.getInsertBatchSize(), 1);
        for (int i = 0; i < rows.size(); i += batchSize) {
            int end = Math.min(i + batchSize, rows.size());
            StringBuilder sb = new StringBuilder();
            for (int j = i; j < end; j++) {
                sb.append(rows.get(j).toJSONString()).append('\n');
            }
            postSql("INSERT INTO " + tableName + " FORMAT JSONEachRow\n" + sb, config.getDatabase());
        }
    }

    @Override
    public BigDataStorageType getStorageType() {
        return BigDataStorageType.CLICKHOUSE;
    }

    private String postSql(String sql, String database) {
        BigDataProperties.ClickHouseConfig config = properties.getClickhouse();
        int retries = config.getMaxRetries();
        long delayMs = config.getRetryDelayMs();

        for (int attempt = 1; attempt <= retries; attempt++) {
            try {
                String db = StringUtils.hasText(database) ? database : "default";
                URI uri = URI.create(String.format("http://%s:%d/?database=%s",
                        config.getHost(), config.getPort(), db));

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(uri)
                        .timeout(Duration.ofMillis(config.getSocketTimeout()))
                        .header("Authorization", basicAuth(config))
                        .header("Content-Type", "text/plain; charset=utf-8")
                        .POST(HttpRequest.BodyPublishers.ofString(sql, StandardCharsets.UTF_8))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() >= 400) {
                    String errorMsg = "ClickHouse 执行失败: " + response.body();
                    if (attempt < retries && isRetryable(response.statusCode())) {
                        log.warn("ClickHouse request failed (attempt {} of {}), retrying...", attempt, retries);
                        TimeUnit.MILLISECONDS.sleep(delayMs * attempt);
                        continue;
                    }
                    throw new BusinessException(errorMsg);
                }
                return response.body();
            } catch (BusinessException e) {
                throw e;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new BusinessException("ClickHouse 请求被中断");
            } catch (Exception e) {
                if (attempt < retries) {
                    log.warn("ClickHouse request failed (attempt {} of {}): {}", attempt, retries, e.getMessage());
                    try {
                        TimeUnit.MILLISECONDS.sleep(delayMs * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    log.error("ClickHouse HTTP request failed after {} attempts", retries, e);
                    throw new BusinessException("ClickHouse 连接失败: " + e.getMessage());
                }
            }
        }
        throw new BusinessException("ClickHouse 请求失败，已达到最大重试次数");
    }

    private boolean isRetryable(int statusCode) {
        return statusCode >= 500 || statusCode == 429;
    }

    private String basicAuth(BigDataProperties.ClickHouseConfig config) {
        String credentials = config.getUsername() + ":" + config.getPassword();
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }
}