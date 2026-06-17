USE vrd_bigdata;

CREATE TABLE IF NOT EXISTS ecu_log_records (
    ts TIMESTAMP,
    id BIGINT,
    vin NCHAR(50),
    ecu_type NCHAR(50),
    log_start_time TIMESTAMP,
    log_end_time TIMESTAMP,
    upload_start_time TIMESTAMP,
    upload_end_time TIMESTAMP,
    storage_address NCHAR(500),
    storage_key NCHAR(500),
    storage_type NCHAR(50),
    file_name NCHAR(255),
    file_size BIGINT,
    file_md5 NCHAR(64)
) TAGS (
    vin NCHAR(50)
);