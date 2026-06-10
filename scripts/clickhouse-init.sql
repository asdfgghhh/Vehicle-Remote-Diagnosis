-- ClickHouse 初始化（远程执行示例）
-- curl -u default:密码 "http://124.221.104.56:8123/" --data "CREATE DATABASE IF NOT EXISTS vrd_bigdata"

CREATE DATABASE IF NOT EXISTS vrd_bigdata;

CREATE TABLE IF NOT EXISTS vrd_bigdata.ecu_log_records (    id UInt64,
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

CREATE TABLE IF NOT EXISTS vrd_bigdata.vehicle_signal_records (
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
SETTINGS index_granularity = 8192;
