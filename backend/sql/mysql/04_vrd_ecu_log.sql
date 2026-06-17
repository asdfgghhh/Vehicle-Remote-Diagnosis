USE vrd_ecu_log;

CREATE TABLE IF NOT EXISTS ecu_log_file (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500),
    file_size BIGINT,
    md5 VARCHAR(64),
    vehicle_id BIGINT,
    vin VARCHAR(50),
    ecu_type VARCHAR(50),
    upload_status INT DEFAULT 1 COMMENT '1-上传中 2-完成 3-失败',
    uploaded_size BIGINT DEFAULT 0,
    deleted INT DEFAULT 0,
    create_time DATETIME,
    update_time DATETIME,
    INDEX idx_vin (vin),
    INDEX idx_vehicle_id (vehicle_id),
    INDEX idx_upload_status (upload_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS upload_chunk (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    chunk_id VARCHAR(100),
    file_md5 VARCHAR(64),
    chunk_number INT,
    chunk_size BIGINT,
    chunk_path VARCHAR(500),
    status INT DEFAULT 1,
    create_time DATETIME,
    update_time DATETIME,
    INDEX idx_chunk_id (chunk_id),
    INDEX idx_file_md5 (file_md5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;