package com.vrd.access.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "clickhouse.enabled", havingValue = "true", matchIfMissing = true)
public class ClickHouseSchemaInitializer {

    private static final String CREATE_ECU_LOG_TABLE = """
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

    private static final String CREATE_SIGNAL_TABLE = """
            CREATE TABLE IF NOT EXISTS %s.vehicle_signal_records (
                id UInt64,
                vin String,
                vehicle_id UInt64 DEFAULT 0,
                signal_name LowCardinality(String),
                signal_value String,
                numeric_value Float64,
                unit LowCardinality(String),
                timestamp UInt64,
                signal_time DateTime,
                message_name String,
                message_id UInt32 DEFAULT 0,
                create_time DateTime DEFAULT now()
            ) ENGINE = MergeTree()
            PARTITION BY toYYYYMM(signal_time)
            ORDER BY (vin, signal_name, signal_time)
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
            clickHouseHttpClient.execute(String.format(CREATE_ECU_LOG_TABLE, db));
            clickHouseHttpClient.execute(String.format(CREATE_SIGNAL_TABLE, db));
            log.info("ClickHouse schema ready: {}.ecu_log_records, {}.vehicle_signal_records", db, db);
        } catch (Exception e) {
            log.warn("ClickHouse schema init failed: {}", e.getMessage());
        }
    }
}
