package com.vrd.bigdata.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "bigdata.storage")
public class BigDataStorageProperties {

    private StorageType type = StorageType.CLICKHOUSE;

    private String basePath = "/vrd/data";

    private ClickHouseProperties clickhouse = new ClickHouseProperties();

    @Data
    public static class ClickHouseProperties {
        private String host = "localhost";
        private int port = 8123;
        private String database = "vrd_bigdata";
        private String username = "default";
        private String password = "";
        private int connectionTimeout = 30000;
        private int socketTimeout = 60000;
    }
}
