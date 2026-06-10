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
VALUES ('admin', '$2a$10$9sCn.a.mx9Xm/bvr6Dytm.RTNkwAggG334aleN7n7e9WiJKOKhBxa', 'admin@vrd.com', '系统管理员', 1, 0, NOW(), NOW());

INSERT INTO sys_role (role_code, role_name, description, status, deleted, create_time, update_time)
VALUES ('ADMIN', '系统管理员', '拥有系统全部权限', 1, 0, NOW(), NOW()),
       ('OPERATOR', '操作员', '业务操作权限', 1, 0, NOW(), NOW()),
       ('VIEWER', '只读用户', '只读查看权限', 1, 0, NOW(), NOW());

INSERT INTO sys_user_role (user_id, role_id, create_time)
VALUES (1, 1, NOW());

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
    config_word VARCHAR(255) COMMENT '配置字',
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

-- 已有库升级: ALTER TABLE vehicle ADD COLUMN config_word VARCHAR(255) COMMENT '配置字' AFTER body_number;

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

CREATE TABLE IF NOT EXISTS vehicle_alert (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    vin VARCHAR(50),
    vehicle_id BIGINT,
    component_code VARCHAR(50) COMMENT '部件英文简称',
    ecu_type VARCHAR(50) COMMENT '控制器类型(兼容)',
    alert_type VARCHAR(50) COMMENT '告警类型',
    message TEXT COMMENT '告警信息',
    status INT DEFAULT 0 COMMENT '0-未处理 1-已处理',
    alert_time DATETIME,
    deleted INT DEFAULT 0,
    create_time DATETIME,
    update_time DATETIME,
    INDEX idx_vin (vin),
    INDEX idx_component_code (component_code),
    INDEX idx_ecu_type (ecu_type),
    INDEX idx_alert_time (alert_time),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO vehicle_alert (vin, vehicle_id, component_code, ecu_type, alert_type, message, status, alert_time, deleted, create_time, update_time) VALUES
('LSVAG4189ES123456', 1, 'EMS', 'EMS', '温度过高', '发动机温度超过阈值', 0, DATE_SUB(NOW(), INTERVAL 2 HOUR), 0, NOW(), NOW()),
('LSVBG6189ES234567', 2, 'BCM', 'BCM', '电瓶电压', '电瓶电压过低', 1, DATE_SUB(NOW(), INTERVAL 3 HOUR), 0, NOW(), NOW()),
('LSVAH4189ES345678', 3, 'ABS', 'ABS', '刹车系统', '刹车片磨损警告', 0, DATE_SUB(NOW(), INTERVAL 5 HOUR), 0, NOW(), NOW()),
('LSVAJ6189ES456789', 4, 'TPMS', 'TPMS', '轮胎压力', '左前轮胎压不足', 1, DATE_SUB(NOW(), INTERVAL 6 HOUR), 0, NOW(), NOW()),
('LSVAK6189ES567890', 5, 'EMS', 'EMS', '温度过高', '冷却液温度异常', 0, DATE_SUB(NOW(), INTERVAL 8 HOUR), 0, NOW(), NOW()),
('LSVAL6189ES678901', 6, 'BMS', 'BMS', '电池异常', '动力电池SOC过低', 0, DATE_SUB(NOW(), INTERVAL 10 HOUR), 0, NOW(), NOW()),
('LSVAM6189ES789012', 7, 'BCM', 'BCM', '灯光故障', '近光灯通信中断', 1, DATE_SUB(NOW(), INTERVAL 12 HOUR), 0, NOW(), NOW()),
('LSVAN6189ES890123', 8, 'TCU', 'TCU', '变速箱', '换挡顿挫告警', 0, DATE_SUB(NOW(), INTERVAL 1 DAY), 0, NOW(), NOW()),
('LSVAO6189ES901234', 9, 'EMS', 'EMS', '排放异常', 'OBD故障码P0420', 1, DATE_SUB(NOW(), INTERVAL 1 DAY), 0, NOW(), NOW()),
('LSVAP6189ES012345', 10, 'ABS', 'ABS', '制动压力', '制动压力传感器异常', 0, DATE_SUB(NOW(), INTERVAL 2 DAY), 0, NOW(), NOW()),
('LSVAQ6189ES123457', 11, 'EPS', 'EPS', '转向助力', '转向助力电机过温', 0, DATE_SUB(NOW(), INTERVAL 2 DAY), 0, NOW(), NOW()),
('LSVAR6189ES234568', 12, 'BMS', 'BMS', '充电异常', '快充接口温度过高', 1, DATE_SUB(NOW(), INTERVAL 3 DAY), 0, NOW(), NOW());

CREATE TABLE IF NOT EXISTS vehicle_fault (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    vin VARCHAR(50),
    vehicle_id BIGINT,
    fault_code VARCHAR(20) NOT NULL COMMENT '故障编码/DTC',
    fault_name VARCHAR(100) COMMENT '故障描述',
    component_code VARCHAR(50) COMMENT '部件英文简称',
    ecu_type VARCHAR(50) COMMENT '控制器类型(兼容)',
    status INT DEFAULT 0 COMMENT '0-未处理 1-已处理',
    fault_time DATETIME,
    deleted INT DEFAULT 0,
    create_time DATETIME,
    update_time DATETIME,
    INDEX idx_vin (vin),
    INDEX idx_fault_code (fault_code),
    INDEX idx_component_code (component_code),
    INDEX idx_fault_time (fault_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO vehicle_fault (vin, vehicle_id, fault_code, fault_name, component_code, ecu_type, status, fault_time, deleted, create_time, update_time) VALUES
('LSVAG4189ES123456', 1, 'P0420', '催化器效率低于阈值', 'EMS', 'EMS', 0, DATE_SUB(NOW(), INTERVAL 1 HOUR), 0, NOW(), NOW()),
('LSVAK6189ES567890', 5, 'P0420', '催化器效率低于阈值', 'EMS', 'EMS', 0, DATE_SUB(NOW(), INTERVAL 4 HOUR), 0, NOW(), NOW()),
('LSVAO6189ES901234', 9, 'P0420', '催化器效率低于阈值', 'EMS', 'EMS', 1, DATE_SUB(NOW(), INTERVAL 1 DAY), 0, NOW(), NOW()),
('LSVBG6189ES234567', 2, 'P0562', '系统电压过低', 'BCM', 'BCM', 1, DATE_SUB(NOW(), INTERVAL 2 HOUR), 0, NOW(), NOW()),
('LSVAH4189ES345678', 3, 'C1201', 'ABS泵电机故障', 'ABS', 'ABS', 0, DATE_SUB(NOW(), INTERVAL 5 HOUR), 0, NOW(), NOW()),
('LSVAP6189ES012345', 10, 'C1201', 'ABS泵电机故障', 'ABS', 'ABS', 0, DATE_SUB(NOW(), INTERVAL 2 DAY), 0, NOW(), NOW()),
('LSVAN6189ES890123', 8, 'P0700', '变速箱控制系统故障', 'TCU', 'TCU', 0, DATE_SUB(NOW(), INTERVAL 1 DAY), 0, NOW(), NOW()),
('LSVAL6189ES678901', 6, 'P0300', '随机/多缸失火', 'EMS', 'EMS', 0, DATE_SUB(NOW(), INTERVAL 8 HOUR), 0, NOW(), NOW()),
('LSVAQ6189ES123457', 11, 'P0300', '随机/多缸失火', 'EMS', 'EMS', 0, DATE_SUB(NOW(), INTERVAL 2 DAY), 0, NOW(), NOW()),
('LSVAM6189ES789012', 7, 'U0100', '与EMS通信丢失', 'BCM', 'BCM', 1, DATE_SUB(NOW(), INTERVAL 12 HOUR), 0, NOW(), NOW()),
('LSVAJ6189ES456789', 4, 'P0096', '进气温度传感器范围异常', 'EMS', 'EMS', 1, DATE_SUB(NOW(), INTERVAL 6 HOUR), 0, NOW(), NOW()),
('LSVAR6189ES234568', 12, 'P0A80', '动力电池更换需求', 'BMS', 'BMS', 0, DATE_SUB(NOW(), INTERVAL 3 DAY), 0, NOW(), NOW());

CREATE TABLE IF NOT EXISTS vehicle_online_stat (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    stat_time DATETIME NOT NULL,
    stat_granularity VARCHAR(10) NOT NULL COMMENT 'hour/day',
    online_count INT NOT NULL DEFAULT 0,
    create_time DATETIME,
    UNIQUE KEY uk_stat_time_granularity (stat_time, stat_granularity),
    INDEX idx_granularity_time (stat_granularity, stat_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO vehicle_online_stat (stat_time, stat_granularity, online_count, create_time) VALUES
(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 23 HOUR), '%Y-%m-%d %H:00:00'), 'hour', 86, NOW()),
(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 22 HOUR), '%Y-%m-%d %H:00:00'), 'hour', 88, NOW()),
(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 21 HOUR), '%Y-%m-%d %H:00:00'), 'hour', 90, NOW()),
(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 20 HOUR), '%Y-%m-%d %H:00:00'), 'hour', 92, NOW()),
(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 19 HOUR), '%Y-%m-%d %H:00:00'), 'hour', 94, NOW()),
(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 18 HOUR), '%Y-%m-%d %H:00:00'), 'hour', 96, NOW()),
(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 17 HOUR), '%Y-%m-%d %H:00:00'), 'hour', 98, NOW()),
(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 16 HOUR), '%Y-%m-%d %H:00:00'), 'hour', 100, NOW()),
(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 15 HOUR), '%Y-%m-%d %H:00:00'), 'hour', 102, NOW()),
(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 14 HOUR), '%Y-%m-%d %H:00:00'), 'hour', 105, NOW()),
(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 13 HOUR), '%Y-%m-%d %H:00:00'), 'hour', 108, NOW()),
(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 12 HOUR), '%Y-%m-%d %H:00:00'), 'hour', 110, NOW()),
(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 11 HOUR), '%Y-%m-%d %H:00:00'), 'hour', 112, NOW()),
(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 10 HOUR), '%Y-%m-%d %H:00:00'), 'hour', 115, NOW()),
(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 9 HOUR), '%Y-%m-%d %H:00:00'), 'hour', 118, NOW()),
(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 8 HOUR), '%Y-%m-%d %H:00:00'), 'hour', 120, NOW()),
(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 7 HOUR), '%Y-%m-%d %H:00:00'), 'hour', 118, NOW()),
(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 6 HOUR), '%Y-%m-%d %H:00:00'), 'hour', 116, NOW()),
(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 5 HOUR), '%Y-%m-%d %H:00:00'), 'hour', 114, NOW()),
(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 4 HOUR), '%Y-%m-%d %H:00:00'), 'hour', 112, NOW()),
(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 3 HOUR), '%Y-%m-%d %H:00:00'), 'hour', 110, NOW()),
(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 2 HOUR), '%Y-%m-%d %H:00:00'), 'hour', 108, NOW()),
(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 1 HOUR), '%Y-%m-%d %H:00:00'), 'hour', 106, NOW()),
(DATE_FORMAT(NOW(), '%Y-%m-%d %H:00:00'), 'hour', 104, NOW()),
(DATE_SUB(CURDATE(), INTERVAL 6 DAY), 'day', 82, NOW()),
(DATE_SUB(CURDATE(), INTERVAL 5 DAY), 'day', 88, NOW()),
(DATE_SUB(CURDATE(), INTERVAL 4 DAY), 'day', 95, NOW()),
(DATE_SUB(CURDATE(), INTERVAL 3 DAY), 'day', 102, NOW()),
(DATE_SUB(CURDATE(), INTERVAL 2 DAY), 'day', 108, NOW()),
(DATE_SUB(CURDATE(), INTERVAL 1 DAY), 'day', 112, NOW()),
(CURDATE(), 'day', 104, NOW());

CREATE TABLE IF NOT EXISTS vehicle_alert_trend_stat (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    stat_time DATETIME NOT NULL,
    stat_granularity VARCHAR(10) NOT NULL COMMENT 'hour/day/week/month',
    fault_count INT NOT NULL DEFAULT 0 COMMENT '故障数',
    fault_vehicle_count INT NOT NULL DEFAULT 0 COMMENT '故障车辆数',
    create_time DATETIME,
    UNIQUE KEY uk_alert_trend (stat_time, stat_granularity),
    INDEX idx_alert_trend_granularity (stat_granularity, stat_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO vehicle_alert_trend_stat (stat_time, stat_granularity, fault_count, fault_vehicle_count, create_time) VALUES
(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 23 HOUR), '%Y-%m-%d %H:00:00'), 'hour', 12, 8, NOW()),
(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 22 HOUR), '%Y-%m-%d %H:00:00'), 'hour', 15, 9, NOW()),
(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 21 HOUR), '%Y-%m-%d %H:00:00'), 'hour', 18, 11, NOW()),
(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 20 HOUR), '%Y-%m-%d %H:00:00'), 'hour', 14, 10, NOW()),
(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 19 HOUR), '%Y-%m-%d %H:00:00'), 'hour', 20, 12, NOW()),
(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 18 HOUR), '%Y-%m-%d %H:00:00'), 'hour', 22, 13, NOW()),
(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 17 HOUR), '%Y-%m-%d %H:00:00'), 'hour', 25, 14, NOW()),
(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 16 HOUR), '%Y-%m-%d %H:00:00'), 'hour', 28, 15, NOW()),
(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 15 HOUR), '%Y-%m-%d %H:00:00'), 'hour', 30, 16, NOW()),
(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 14 HOUR), '%Y-%m-%d %H:00:00'), 'hour', 27, 15, NOW()),
(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 13 HOUR), '%Y-%m-%d %H:00:00'), 'hour', 32, 17, NOW()),
(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 12 HOUR), '%Y-%m-%d %H:00:00'), 'hour', 35, 18, NOW()),
(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 11 HOUR), '%Y-%m-%d %H:00:00'), 'hour', 33, 17, NOW()),
(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 10 HOUR), '%Y-%m-%d %H:00:00'), 'hour', 38, 19, NOW()),
(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 9 HOUR), '%Y-%m-%d %H:00:00'), 'hour', 40, 20, NOW()),
(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 8 HOUR), '%Y-%m-%d %H:00:00'), 'hour', 36, 18, NOW()),
(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 7 HOUR), '%Y-%m-%d %H:00:00'), 'hour', 34, 17, NOW()),
(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 6 HOUR), '%Y-%m-%d %H:00:00'), 'hour', 31, 16, NOW()),
(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 5 HOUR), '%Y-%m-%d %H:00:00'), 'hour', 29, 15, NOW()),
(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 4 HOUR), '%Y-%m-%d %H:00:00'), 'hour', 26, 14, NOW()),
(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 3 HOUR), '%Y-%m-%d %H:00:00'), 'hour', 24, 13, NOW()),
(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 2 HOUR), '%Y-%m-%d %H:00:00'), 'hour', 21, 12, NOW()),
(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 1 HOUR), '%Y-%m-%d %H:00:00'), 'hour', 19, 11, NOW()),
(DATE_FORMAT(NOW(), '%Y-%m-%d %H:00:00'), 'hour', 17, 10, NOW()),
(DATE_SUB(CURDATE(), INTERVAL 29 DAY), 'day', 45, 22, NOW()),
(DATE_SUB(CURDATE(), INTERVAL 28 DAY), 'day', 48, 23, NOW()),
(DATE_SUB(CURDATE(), INTERVAL 27 DAY), 'day', 52, 25, NOW()),
(DATE_SUB(CURDATE(), INTERVAL 26 DAY), 'day', 50, 24, NOW()),
(DATE_SUB(CURDATE(), INTERVAL 25 DAY), 'day', 55, 26, NOW()),
(DATE_SUB(CURDATE(), INTERVAL 24 DAY), 'day', 58, 27, NOW()),
(DATE_SUB(CURDATE(), INTERVAL 23 DAY), 'day', 60, 28, NOW()),
(DATE_SUB(CURDATE(), INTERVAL 22 DAY), 'day', 57, 27, NOW()),
(DATE_SUB(CURDATE(), INTERVAL 21 DAY), 'day', 62, 29, NOW()),
(DATE_SUB(CURDATE(), INTERVAL 20 DAY), 'day', 65, 30, NOW()),
(DATE_SUB(CURDATE(), INTERVAL 19 DAY), 'day', 63, 29, NOW()),
(DATE_SUB(CURDATE(), INTERVAL 18 DAY), 'day', 68, 31, NOW()),
(DATE_SUB(CURDATE(), INTERVAL 17 DAY), 'day', 70, 32, NOW()),
(DATE_SUB(CURDATE(), INTERVAL 16 DAY), 'day', 67, 31, NOW()),
(DATE_SUB(CURDATE(), INTERVAL 15 DAY), 'day', 72, 33, NOW()),
(DATE_SUB(CURDATE(), INTERVAL 14 DAY), 'day', 75, 34, NOW()),
(DATE_SUB(CURDATE(), INTERVAL 13 DAY), 'day', 73, 33, NOW()),
(DATE_SUB(CURDATE(), INTERVAL 12 DAY), 'day', 78, 35, NOW()),
(DATE_SUB(CURDATE(), INTERVAL 11 DAY), 'day', 80, 36, NOW()),
(DATE_SUB(CURDATE(), INTERVAL 10 DAY), 'day', 77, 35, NOW()),
(DATE_SUB(CURDATE(), INTERVAL 9 DAY), 'day', 82, 37, NOW()),
(DATE_SUB(CURDATE(), INTERVAL 8 DAY), 'day', 85, 38, NOW()),
(DATE_SUB(CURDATE(), INTERVAL 7 DAY), 'day', 83, 37, NOW()),
(DATE_SUB(CURDATE(), INTERVAL 6 DAY), 'day', 88, 39, NOW()),
(DATE_SUB(CURDATE(), INTERVAL 5 DAY), 'day', 90, 40, NOW()),
(DATE_SUB(CURDATE(), INTERVAL 4 DAY), 'day', 87, 39, NOW()),
(DATE_SUB(CURDATE(), INTERVAL 3 DAY), 'day', 92, 41, NOW()),
(DATE_SUB(CURDATE(), INTERVAL 2 DAY), 'day', 95, 42, NOW()),
(DATE_SUB(CURDATE(), INTERVAL 1 DAY), 'day', 93, 41, NOW()),
(CURDATE(), 'day', 91, 40, NOW()),
(DATE_SUB(DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY), INTERVAL 11 WEEK), 'week', 320, 145, NOW()),
(DATE_SUB(DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY), INTERVAL 10 WEEK), 'week', 335, 150, NOW()),
(DATE_SUB(DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY), INTERVAL 9 WEEK), 'week', 348, 155, NOW()),
(DATE_SUB(DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY), INTERVAL 8 WEEK), 'week', 360, 160, NOW()),
(DATE_SUB(DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY), INTERVAL 7 WEEK), 'week', 372, 165, NOW()),
(DATE_SUB(DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY), INTERVAL 6 WEEK), 'week', 385, 170, NOW()),
(DATE_SUB(DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY), INTERVAL 5 WEEK), 'week', 398, 175, NOW()),
(DATE_SUB(DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY), INTERVAL 4 WEEK), 'week', 410, 180, NOW()),
(DATE_SUB(DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY), INTERVAL 3 WEEK), 'week', 425, 185, NOW()),
(DATE_SUB(DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY), INTERVAL 2 WEEK), 'week', 438, 190, NOW()),
(DATE_SUB(DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY), INTERVAL 1 WEEK), 'week', 450, 195, NOW()),
(DATE_SUB(DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY), INTERVAL 0 WEEK), 'week', 465, 200, NOW()),
(DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 5 MONTH), '%Y-%m-01'), 'month', 1200, 520, NOW()),
(DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 4 MONTH), '%Y-%m-01'), 'month', 1280, 545, NOW()),
(DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 3 MONTH), '%Y-%m-01'), 'month', 1350, 570, NOW()),
(DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 2 MONTH), '%Y-%m-01'), 'month', 1420, 595, NOW()),
(DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 1 MONTH), '%Y-%m-01'), 'month', 1500, 620, NOW()),
(DATE_FORMAT(CURDATE(), '%Y-%m-01'), 'month', 1580, 645, NOW());

CREATE TABLE IF NOT EXISTS sync_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sync_type VARCHAR(50) COMMENT 'KAFKA/API',
    source VARCHAR(255) COMMENT 'Kafka主题或API地址',
    target VARCHAR(255) DEFAULT 'database',
    vin VARCHAR(50) COMMENT '同步车辆VIN',
    action VARCHAR(20) COMMENT 'CREATE/UPDATE/BATCH',
    record_count INT DEFAULT 0,
    status VARCHAR(20) COMMENT 'SUCCESS/FAILED/PROCESSING',
    message TEXT COMMENT '失败原因或备注',
    payload TEXT COMMENT '原始同步车辆信息(JSON)',
    start_time DATETIME,
    end_time DATETIME,
    create_time DATETIME,
    INDEX idx_sync_type (sync_type),
    INDEX idx_status (status),
    INDEX idx_vin (vin),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 已有库升级:
-- ALTER TABLE sync_log ADD COLUMN vin VARCHAR(50) COMMENT '同步车辆VIN' AFTER target;
-- ALTER TABLE sync_log ADD COLUMN action VARCHAR(20) COMMENT 'CREATE/UPDATE/BATCH' AFTER vin;
-- ALTER TABLE sync_log ADD COLUMN payload TEXT COMMENT '原始同步车辆信息(JSON)' AFTER message;

INSERT INTO sync_log (sync_type, source, target, vin, action, record_count, status, message, payload, start_time, end_time, create_time) VALUES
('KAFKA', 'vehicle-data', 'database', 'LSVAG4189ES123456', 'CREATE', 1, 'SUCCESS', NULL,
 '{"action":"CREATE","data":{"vin":"LSVAG4189ES123456","modelId":1,"plateNumber":"沪A12345","color":"白","productionYear":2024,"configWord":"A1B2C3D4"}}',
 DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 2 HOUR)),
('KAFKA', 'vehicle-data', 'database', 'LSVBG6189ES234567', 'UPDATE', 1, 'SUCCESS', NULL,
 '{"action":"UPDATE","data":{"id":2,"vin":"LSVBG6189ES234567","plateNumber":"沪B67890","configWord":"E5F6G7H8"}}',
 DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_SUB(NOW(), INTERVAL 1 HOUR)),
('KAFKA', 'vehicle-data', 'database', NULL, 'CREATE', 0, 'FAILED', 'VIN码不能为空',
 '{"action":"CREATE","data":{"modelId":1,"color":"黑"}}',
 DATE_SUB(NOW(), INTERVAL 30 MINUTE), DATE_SUB(NOW(), INTERVAL 30 MINUTE), DATE_SUB(NOW(), INTERVAL 30 MINUTE)),
('API', 'http://example.com/api/vehicles', 'database', NULL, 'BATCH', 3, 'SUCCESS', NULL,
 '[{"vin":"LSVAH4189ES345678","modelId":1,"plateNumber":"沪C11111","configWord":"11223344"},{"vin":"LSVAJ6189ES456789","modelId":1,"plateNumber":"沪D22222","configWord":"55667788"},{"vin":"LSVAK6189ES567890","modelId":1,"plateNumber":"沪E33333","configWord":"99AABBCC"}]',
 DATE_SUB(NOW(), INTERVAL 20 MINUTE), DATE_SUB(NOW(), INTERVAL 20 MINUTE), DATE_SUB(NOW(), INTERVAL 20 MINUTE)),
('API', 'http://example.com/api/vehicles', 'database', NULL, 'BATCH', 0, 'FAILED', 'Connection refused: connect',
 NULL,
 DATE_SUB(NOW(), INTERVAL 10 MINUTE), DATE_SUB(NOW(), INTERVAL 10 MINUTE), DATE_SUB(NOW(), INTERVAL 10 MINUTE));

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
