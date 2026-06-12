package com.vrd.common.clickhouse;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "clickhouse")
public class ClickHouseProperties {

    private boolean enabled = true;
    private String host = "localhost";
    private int port = 8123;
    private String database = "vrd_bigdata";
    private String username = "default";
    private String password = "";
    
    private int connectionTimeout = 10000;
    private int socketTimeout = 30000;
    private int queryTimeoutSeconds = 10;
    
    private int maxTotalConnections = 20;
    private int maxIdleConnections = 10;
    private int minIdleConnections = 5;
    private int connectionMaxIdleTimeMs = 300000;
    
    private int maxRetries = 3;
    private long retryDelayMs = 1000;
    
    private int insertBatchSize = 500;
    private int maxPageSize = 500;
}
