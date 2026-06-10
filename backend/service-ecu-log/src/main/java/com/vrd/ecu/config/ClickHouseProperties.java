package com.vrd.ecu.config;

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
    private int connectionTimeout = 3000;
    private int socketTimeout = 2000;
    private int queryTimeoutSeconds = 2;
    private int defaultDays = 7;
    private int maxPageSize = 100;
}
