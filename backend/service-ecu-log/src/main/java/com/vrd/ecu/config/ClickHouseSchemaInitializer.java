package com.vrd.ecu.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 启动时自动创建 ClickHouse 库与表
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "clickhouse.enabled", havingValue = "true", matchIfMissing = true)
public class ClickHouseSchemaInitializer {

    private static final String CREATE_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS %s.ecu_log_records (
                id UInt64,
                vin String,
                ecu_type LowCardinality(String),
                log_start_time DateTime,
                log_end_time DateTime,
                upload_start_time DateTime,
                upload_end_time DateTime,
                storage_address String,
                storage_key String,
                storage_type LowCardinality(String),
                file_name String,
                file_size UInt64,
                file_md5 String,
                create_time DateTime DEFAULT now()
            ) ENGINE = MergeTree()
            PARTITION BY toYYYYMM(upload_start_time)
            ORDER BY (ecu_type, vin, upload_start_time)
            SETTINGS index_granularity = 8192
            """;

    private final ClickHouseProperties properties;
    private final ClickHouseHttpClient clickHouseHttpClient;

    public ClickHouseSchemaInitializer(ClickHouseProperties properties,
                                       ClickHouseHttpClient clickHouseHttpClient) {
        this.properties = properties;
        this.clickHouseHttpClient = clickHouseHttpClient;
    }

    @PostConstruct
    public void init() {
        try {
            String db = properties.getDatabase();
            clickHouseHttpClient.execute("CREATE DATABASE IF NOT EXISTS " + db);
            clickHouseHttpClient.execute(String.format(CREATE_TABLE_SQL, db));
            log.info("ClickHouse schema ready: {}.ecu_log_records", db);
        } catch (Exception e) {
            log.warn("ClickHouse schema init failed: {}", e.getMessage());
        }
    }
}
