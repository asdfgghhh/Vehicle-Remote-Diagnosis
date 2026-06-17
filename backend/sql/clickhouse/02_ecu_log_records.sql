CREATE TABLE IF NOT EXISTS vrd_bigdata.ecu_log_records (
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
SETTINGS index_granularity = 8192;