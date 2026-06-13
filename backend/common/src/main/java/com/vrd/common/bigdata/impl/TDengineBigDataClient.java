package com.vrd.common.bigdata.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
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
@Component("tdengineBigDataClient")
@ConditionalOnProperty(name = "bigdata.type", havingValue = "TDENGINE")
public class TDengineBigDataClient implements BigDataClient {

    private final BigDataProperties properties;
    private final HttpClient httpClient;

    public TDengineBigDataClient(BigDataProperties properties) {
        this.properties = properties;
        BigDataProperties.TDengineConfig config = properties.getTdengine();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(config.getConnectionTimeout()))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    @Override
    public void execute(String sql) {
        execute(sql, properties.getTdengine().getDatabase());
    }

    @Override
    public void execute(String sql, String database) {
        postSql(sql, database);
    }

    @Override
    public long queryCount(String sql) {
        String body = postSql(sql, properties.getTdengine().getDatabase());
        if (!StringUtils.hasText(body)) {
            return 0L;
        }
        JSONObject result = JSON.parseObject(body);
        if (result.containsKey("data") && result.getJSONArray("data").size() > 0) {
            JSONArray row = result.getJSONArray("data").getJSONArray(0);
            return row.getLongValue(0);
        }
        return 0L;
    }

    @Override
    public String query(String sql) {
        return postSql(sql, properties.getTdengine().getDatabase());
    }

    @Override
    public String query(String sql, String database) {
        return postSql(sql, database);
    }

    @Override
    public <T> List<T> queryForList(String sql, Class<T> clazz) {
        String body = postSql(sql, properties.getTdengine().getDatabase());
        if (!StringUtils.hasText(body)) {
            return List.of();
        }
        JSONObject result = JSON.parseObject(body);
        JSONArray data = result.getJSONArray("data");
        if (data == null || data.isEmpty()) {
            return List.of();
        }
        return data.toJavaList(clazz);
    }

    @Override
    public JSONObject queryForJson(String sql) {
        String body = postSql(sql, properties.getTdengine().getDatabase());
        if (!StringUtils.hasText(body)) {
            return new JSONObject();
        }
        return JSON.parseObject(body);
    }

    @Override
    public boolean exists(String tableName, String condition) {
        String sql = String.format("SELECT count(*) FROM %s WHERE %s", tableName, condition);
        return queryCount(sql) > 0;
    }

    @Override
    public void insertJson(String tableName, List<JSONObject> rows) {
        if (rows.isEmpty()) {
            return;
        }
        BigDataProperties.TDengineConfig config = properties.getTdengine();
        int batchSize = Math.max(config.getInsertBatchSize(), 1);
        for (int i = 0; i < rows.size(); i += batchSize) {
            int end = Math.min(i + batchSize, rows.size());
            StringBuilder sb = new StringBuilder();
            for (int j = i; j < end; j++) {
                JSONObject row = rows.get(j);
                List<String> keys = row.keySet().stream().toList();
                List<Object> values = row.values().stream().toList();
                
                sb.append("INSERT INTO ").append(tableName);
                sb.append("(").append(String.join(",", keys)).append(") ");
                sb.append("VALUES (");
                for (int k = 0; k < values.size(); k++) {
                    Object val = values.get(k);
                    if (val == null) {
                        sb.append("NULL");
                    } else if (val instanceof String) {
                        sb.append("'").append(val.toString().replace("'", "\\'")).append("'");
                    } else {
                        sb.append(val);
                    }
                    if (k < values.size() - 1) {
                        sb.append(",");
                    }
                }
                sb.append(")");
                if (j < end - 1) {
                    sb.append(" ");
                }
            }
            postSql(sb.toString(), config.getDatabase());
        }
    }

    @Override
    public BigDataStorageType getStorageType() {
        return BigDataStorageType.TDENGINE;
    }

    private String postSql(String sql, String database) {
        BigDataProperties.TDengineConfig config = properties.getTdengine();
        int retries = config.getMaxRetries();
        long delayMs = config.getRetryDelayMs();

        for (int attempt = 1; attempt <= retries; attempt++) {
            try {
                URI uri = URI.create(String.format("http://%s:%d/rest/sql",
                        config.getHost(), config.getPort()));

                String fullSql = StringUtils.hasText(database) 
                        ? "USE " + database + "; " + sql 
                        : sql;

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(uri)
                        .timeout(Duration.ofMillis(config.getSocketTimeout()))
                        .header("Authorization", basicAuth(config))
                        .header("Content-Type", "text/plain; charset=utf-8")
                        .POST(HttpRequest.BodyPublishers.ofString(fullSql, StandardCharsets.UTF_8))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() >= 400) {
                    String errorMsg = "TDengine 执行失败: " + response.body();
                    if (attempt < retries && isRetryable(response.statusCode())) {
                        log.warn("TDengine request failed (attempt {} of {}), retrying...", attempt, retries);
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
                throw new BusinessException("TDengine 请求被中断");
            } catch (Exception e) {
                if (attempt < retries) {
                    log.warn("TDengine request failed (attempt {} of {}): {}", attempt, retries, e.getMessage());
                    try {
                        TimeUnit.MILLISECONDS.sleep(delayMs * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    log.error("TDengine HTTP request failed after {} attempts", retries, e);
                    throw new BusinessException("TDengine 连接失败: " + e.getMessage());
                }
            }
        }
        throw new BusinessException("TDengine 请求失败，已达到最大重试次数");
    }

    private boolean isRetryable(int statusCode) {
        return statusCode >= 500 || statusCode == 429;
    }

    private String basicAuth(BigDataProperties.TDengineConfig config) {
        String credentials = config.getUsername() + ":" + config.getPassword();
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }
}