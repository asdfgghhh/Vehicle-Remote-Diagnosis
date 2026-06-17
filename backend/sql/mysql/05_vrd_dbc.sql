USE vrd_dbc;

CREATE TABLE IF NOT EXISTS dbc_file (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    model_id BIGINT COMMENT '关联车型ID',
    model_name VARCHAR(100) COMMENT '车型名称',
    storage_key VARCHAR(500) COMMENT '对象存储key',
    storage_address VARCHAR(1000) COMMENT '对象存储访问地址',
    storage_type VARCHAR(32) COMMENT '存储类型',
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) COMMENT '兼容字段，通常与storage_key一致',
    file_size BIGINT,
    version VARCHAR(50),
    description TEXT,
    parse_result TEXT,
    message_count INT DEFAULT 0,
    signal_count INT DEFAULT 0,
    status INT DEFAULT 1,
    deleted INT DEFAULT 0,
    create_time DATETIME,
    update_time DATETIME,
    INDEX idx_model_id (model_id),
    INDEX idx_file_name (file_name),
    INDEX idx_version (version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS dispatch_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    dbc_file_id BIGINT,
    vehicle_id BIGINT,
    vin VARCHAR(50),
    dispatch_type VARCHAR(20),
    status INT,
    result TEXT,
    dispatch_time DATETIME,
    create_time DATETIME,
    INDEX idx_dbc_file_id (dbc_file_id),
    INDEX idx_vehicle_id (vehicle_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;