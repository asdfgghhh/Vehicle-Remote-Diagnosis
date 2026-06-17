USE vrd_bigdata;

CREATE TABLE IF NOT EXISTS vehicle_signal_records (
    ts TIMESTAMP,
    id BIGINT,
    vehicle_id BIGINT,
    signal_name NCHAR(100),
    signal_value NCHAR(255),
    numeric_value DOUBLE,
    unit NCHAR(20),
    timestamp BIGINT,
    signal_time TIMESTAMP,
    message_name NCHAR(100),
    message_id INT
) TAGS (
    vin NCHAR(50)
);