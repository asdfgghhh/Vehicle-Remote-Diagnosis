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