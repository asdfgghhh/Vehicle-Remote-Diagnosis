-- 创建数据库
CREATE DATABASE IF NOT EXISTS vrd_auth CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS vrd_vehicle CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS vrd_ecu_log CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS vrd_dbc CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS vrd_signal CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE vrd_auth;

CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    real_name VARCHAR(50),
    status INT DEFAULT 1 COMMENT '1-启用 0-禁用',
    deleted INT DEFAULT 0,
    create_time DATETIME,
    update_time DATETIME,
    INDEX idx_username (username),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_code VARCHAR(50) NOT NULL UNIQUE,
    role_name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    status INT DEFAULT 1,
    deleted INT DEFAULT 0,
    create_time DATETIME,
    update_time DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    create_time DATETIME,
    UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO sys_user (username, password, email, real_name, status, deleted, create_time, update_time)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'admin@vrd.com', '系统管理员', 1, 0, NOW(), NOW());

USE vrd_vehicle;

CREATE TABLE IF NOT EXISTS vehicle_model (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    model_code VARCHAR(50) NOT NULL UNIQUE,
    model_name VARCHAR(100) NOT NULL,
    brand VARCHAR(50),
    manufacturer VARCHAR(100),
    vehicle_type VARCHAR(50),
    engine_power DECIMAL(10,2),
    transmission_type VARCHAR(50),
    fuel_type VARCHAR(50),
    emission_standard VARCHAR(50),
    year INT,
    description TEXT,
    status INT DEFAULT 1,
    deleted INT DEFAULT 0,
    create_time DATETIME,
    update_time DATETIME,
    INDEX idx_model_code (model_code),
    INDEX idx_model_name (model_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS vehicle (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    vin VARCHAR(50) NOT NULL UNIQUE,
    model_id BIGINT,
    plate_number VARCHAR(20),
    color VARCHAR(20),
    production_year INT,
    engine_number VARCHAR(50),
    body_number VARCHAR(50),
    status INT DEFAULT 1,
    current_ecu_version VARCHAR(50),
    data_source INT DEFAULT 1 COMMENT '1-手动 2-Kafka 3-API',
    external_id VARCHAR(100),
    deleted INT DEFAULT 0,
    create_time DATETIME,
    update_time DATETIME,
    INDEX idx_vin (vin),
    INDEX idx_model_id (model_id),
    INDEX idx_plate_number (plate_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS vehicle_ecu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    vehicle_id BIGINT NOT NULL,
    ecu_type VARCHAR(50),
    ecu_part_number VARCHAR(100),
    hardware_version VARCHAR(50),
    software_version VARCHAR(50),
    supplier VARCHAR(100),
    serial_number VARCHAR(100),
    install_date DATE,
    status INT DEFAULT 1,
    deleted INT DEFAULT 0,
    create_time DATETIME,
    update_time DATETIME,
    INDEX idx_vehicle_id (vehicle_id),
    INDEX idx_ecu_type (ecu_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sync_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sync_type VARCHAR(50),
    source VARCHAR(255),
    target VARCHAR(255),
    record_count INT DEFAULT 0,
    status VARCHAR(20),
    message TEXT,
    start_time DATETIME,
    end_time DATETIME,
    create_time DATETIME,
    INDEX idx_sync_type (sync_type),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

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

USE vrd_dbc;

CREATE TABLE IF NOT EXISTS dbc_file (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500),
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

USE vrd_signal;

CREATE TABLE IF NOT EXISTS vehicle_signal (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    vin VARCHAR(50),
    vehicle_id BIGINT,
    signal_name VARCHAR(100),
    signal_value VARCHAR(255),
    numeric_value DECIMAL(20,6),
    unit VARCHAR(20),
    timestamp BIGINT,
    signal_time DATETIME,
    message_name VARCHAR(100),
    message_id INT,
    deleted INT DEFAULT 0,
    create_time DATETIME,
    INDEX idx_vin (vin),
    INDEX idx_vehicle_id (vehicle_id),
    INDEX idx_signal_name (signal_name),
    INDEX idx_signal_time (signal_time),
    INDEX idx_vin_signal_time (vin, signal_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS signal_batch (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    vin VARCHAR(50),
    vehicle_id BIGINT,
    signal_count INT DEFAULT 0,
    raw_data TEXT,
    parsed_data TEXT,
    status INT DEFAULT 1,
    deleted INT DEFAULT 0,
    create_time DATETIME,
    update_time DATETIME,
    INDEX idx_vin (vin),
    INDEX idx_vehicle_id (vehicle_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SELECT '数据库初始化完成' AS status;
