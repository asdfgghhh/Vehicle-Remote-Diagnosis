-- ============================================
-- VRD Vehicle Remote Diagnosis - Doris数据库设计
-- 数据库名: vrd_bigdata
-- ============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS vrd_bigdata;

USE vrd_bigdata;

-- ============================================
-- 1. 车辆信号表 (vehicle_signals)
-- 存储车辆的实时信号数据
-- ============================================
DROP TABLE IF EXISTS vehicle_signals;
CREATE TABLE vehicle_signals (
    id BIGINT AUTO_INCREMENT,
    vehicle_id BIGINT NOT NULL COMMENT '车辆ID',
    vin VARCHAR(17) NOT NULL COMMENT '车辆VIN码',
    signal_name VARCHAR(64) NOT NULL COMMENT '信号名称',
    signal_value VARCHAR(128) COMMENT '信号值',
    signal_unit VARCHAR(32) COMMENT '信号单位',
    signal_type TINYINT COMMENT '信号类型: 1-模拟量 2-数字量 3-状态量',
    longitude DECIMAL(10, 6) COMMENT '经度',
    latitude DECIMAL(10, 6) COMMENT '纬度',
    speed DECIMAL(6, 2) COMMENT '车速(km/h)',
    collect_time DATETIME NOT NULL COMMENT '采集时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    -- 分区和索引设计
    dt DATE NOT NULL COMMENT '分区字段: 日期',
    dt_hour TINYINT NOT NULL COMMENT '分区字段: 小时'
)
DUPLICATE KEY(id, vehicle_id, collect_time)
PARTITION BY RANGE (dt) (
    PARTITION p_2024_01 VALUES LESS THAN ('2024-02-01'),
    PARTITION p_2024_02 VALUES LESS THAN ('2024-03-01'),
    PARTITION p_2024_03 VALUES LESS THAN ('2024-04-01'),
    PARTITION p_2024_04 VALUES LESS THAN ('2024-05-01'),
    PARTITION p_2024_05 VALUES LESS THAN ('2024-06-01'),
    PARTITION p_2024_06 VALUES LESS THAN ('2024-07-01'),
    PARTITION p_2024_07 VALUES LESS THAN ('2024-08-01'),
    PARTITION p_2024_08 VALUES LESS THAN ('2024-09-01'),
    PARTITION p_2024_09 VALUES LESS THAN ('2024-10-01'),
    PARTITION p_2024_10 VALUES LESS THAN ('2024-11-01'),
    PARTITION p_2024_11 VALUES LESS THAN ('2024-12-01'),
    PARTITION p_2024_12 VALUES LESS THAN ('2025-01-01'),
    PARTITION p_2025_01 VALUES LESS THAN ('2025-02-01'),
    PARTITION p_2025_02 VALUES LESS THAN ('2025-03-01'),
    PARTITION p_2025_03 VALUES LESS THAN ('2025-04-01'),
    PARTITION p_2025_04 VALUES LESS THAN ('2025-05-01'),
    PARTITION p_2025_05 VALUES LESS THAN ('2025-06-01'),
    PARTITION p_2025_06 VALUES LESS THAN ('2025-07-01'),
    PARTITION p_2025_07 VALUES LESS THAN ('2025-08-01'),
    PARTITION p_2025_08 VALUES LESS THAN ('2025-09-01'),
    PARTITION p_2025_09 VALUES LESS THAN ('2025-10-01'),
    PARTITION p_2025_10 VALUES LESS THAN ('2025-11-01'),
    PARTITION p_2025_11 VALUES LESS THAN ('2025-12-01'),
    PARTITION p_2025_12 VALUES LESS THAN ('2026-01-01'),
    PARTITION p_2026_01 VALUES LESS THAN ('2026-02-01'),
    PARTITION p_2026_02 VALUES LESS THAN ('2026-03-01'),
    PARTITION p_2026_03 VALUES LESS THAN ('2026-04-01'),
    PARTITION p_2026_04 VALUES LESS THAN ('2026-05-01'),
    PARTITION p_2026_05 VALUES LESS THAN ('2026-06-01'),
    PARTITION p_2026_06 VALUES LESS THAN ('2026-07-01'),
    PARTITION p_future VALUES LESS THAN (MAXVALUE)
)
DISTRIBUTED BY HASH(vehicle_id) BUCKETS 10
PROPERTIES (
    "replication_num" = "1",
    "storage_medium" = "SSD",
    "enable_unique_keys" = "false"
);

-- 创建索引
CREATE INDEX idx_vehicle_id ON vehicle_signals(vehicle_id);
CREATE INDEX idx_vin ON vehicle_signals(vin);
CREATE INDEX idx_signal_name ON vehicle_signals(signal_name);
CREATE INDEX idx_collect_time ON vehicle_signals(collect_time);
CREATE INDEX idx_dt ON vehicle_signals(dt);


-- ============================================
-- 2. ECU日志表 (ecu_logs)
-- 存储ECU错误日志和诊断数据
-- ============================================
DROP TABLE IF EXISTS ecu_logs;
CREATE TABLE ecu_logs (
    id BIGINT AUTO_INCREMENT,
    vehicle_id BIGINT NOT NULL COMMENT '车辆ID',
    vin VARCHAR(17) NOT NULL COMMENT '车辆VIN码',
    ecu_type VARCHAR(32) NOT NULL COMMENT 'ECU类型: ECM, BCM, TCM等',
    error_code VARCHAR(16) COMMENT '故障码',
    error_level TINYINT COMMENT '错误级别: 1-提示 2-警告 3-严重',
    error_desc VARCHAR(512) COMMENT '错误描述',
    log_content TEXT COMMENT '日志内容',
    longitude DECIMAL(10, 6) COMMENT '经度',
    latitude DECIMAL(10, 6) COMMENT '纬度',
    speed DECIMAL(6, 2) COMMENT '故障时车速',
    rpm INT COMMENT '故障时转速',
    collect_time DATETIME NOT NULL COMMENT '采集时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    dt DATE NOT NULL COMMENT '分区字段: 日期'
)
DUPLICATE KEY(id, vehicle_id, collect_time)
PARTITION BY RANGE (dt) (
    PARTITION p_2024_01 VALUES LESS THAN ('2024-02-01'),
    PARTITION p_2024_02 VALUES LESS THAN ('2024-03-01'),
    PARTITION p_2024_03 VALUES LESS THAN ('2024-04-01'),
    PARTITION p_2024_04 VALUES LESS THAN ('2024-05-01'),
    PARTITION p_2024_05 VALUES LESS THAN ('2024-06-01'),
    PARTITION p_2024_06 VALUES LESS THAN ('2024-07-01'),
    PARTITION p_2024_07 VALUES LESS THAN ('2024-08-01'),
    PARTITION p_2024_08 VALUES LESS THAN ('2024-09-01'),
    PARTITION p_2024_09 VALUES LESS THAN ('2024-10-01'),
    PARTITION p_2024_10 VALUES LESS THAN ('2024-11-01'),
    PARTITION p_2024_11 VALUES LESS THAN ('2024-12-01'),
    PARTITION p_2024_12 VALUES LESS THAN ('2025-01-01'),
    PARTITION p_2025_01 VALUES LESS THAN ('2025-02-01'),
    PARTITION p_2025_02 VALUES LESS THAN ('2025-03-01'),
    PARTITION p_2025_03 VALUES LESS THAN ('2025-04-01'),
    PARTITION p_2025_04 VALUES LESS THAN ('2025-05-01'),
    PARTITION p_2025_05 VALUES LESS THAN ('2025-06-01'),
    PARTITION p_2025_06 VALUES LESS THAN ('2025-07-01'),
    PARTITION p_2025_07 VALUES LESS THAN ('2025-08-01'),
    PARTITION p_2025_08 VALUES LESS THAN ('2025-09-01'),
    PARTITION p_2025_09 VALUES LESS THAN ('2025-10-01'),
    PARTITION p_2025_10 VALUES LESS THAN ('2025-11-01'),
    PARTITION p_2025_11 VALUES LESS THAN ('2025-12-01'),
    PARTITION p_2025_12 VALUES LESS THAN ('2026-01-01'),
    PARTITION p_2026_01 VALUES LESS THAN ('2026-02-01'),
    PARTITION p_2026_02 VALUES LESS THAN ('2026-03-01'),
    PARTITION p_2026_03 VALUES LESS THAN ('2026-04-01'),
    PARTITION p_2026_04 VALUES LESS THAN ('2026-05-01'),
    PARTITION p_2026_05 VALUES LESS THAN ('2026-06-01'),
    PARTITION p_2026_06 VALUES LESS THAN ('2026-07-01'),
    PARTITION p_future VALUES LESS THAN (MAXVALUE)
)
DISTRIBUTED BY HASH(vehicle_id) BUCKETS 10
PROPERTIES (
    "replication_num" = "1",
    "storage_medium" = "SSD"
);

CREATE INDEX idx_ecu_vehicle_id ON ecu_logs(vehicle_id);
CREATE INDEX idx_ecu_vin ON ecu_logs(vin);
CREATE INDEX idx_ecu_type ON ecu_logs(ecu_type);
CREATE INDEX idx_error_code ON ecu_logs(error_code);
CREATE INDEX idx_ecu_collect_time ON ecu_logs(collect_time);


-- ============================================
-- 3. 诊断数据表 (diagnostics)
-- 存储车辆诊断报告和健康状态
-- ============================================
DROP TABLE IF EXISTS diagnostics;
CREATE TABLE diagnostics (
    id BIGINT AUTO_INCREMENT,
    vehicle_id BIGINT NOT NULL COMMENT '车辆ID',
    vin VARCHAR(17) NOT NULL COMMENT '车辆VIN码',
    diagnostic_type VARCHAR(32) NOT NULL COMMENT '诊断类型',
    diagnostic_result VARCHAR(16) NOT NULL COMMENT '诊断结果: PASS, FAIL, WARNING',
    overall_health_score DECIMAL(5, 2) COMMENT '综合健康评分(0-100)',
    system_count INT COMMENT '检测系统数量',
    error_count INT COMMENT '错误数量',
    warning_count INT COMMENT '警告数量',
    diagnostic_detail TEXT COMMENT '诊断详情JSON',
    technician_id BIGINT COMMENT '技师ID',
    diagnostic_time DATETIME NOT NULL COMMENT '诊断时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    dt DATE NOT NULL COMMENT '分区字段: 日期'
)
DUPLICATE KEY(id, vehicle_id, diagnostic_time)
PARTITION BY RANGE (dt) (
    PARTITION p_2024_01 VALUES LESS THAN ('2024-02-01'),
    PARTITION p_2024_02 VALUES LESS THAN ('2024-03-01'),
    PARTITION p_2024_03 VALUES LESS THAN ('2024-04-01'),
    PARTITION p_2024_04 VALUES LESS THAN ('2024-05-01'),
    PARTITION p_2024_05 VALUES LESS THAN ('2024-06-01'),
    PARTITION p_2024_06 VALUES LESS THAN ('2024-07-01'),
    PARTITION p_2024_07 VALUES LESS THAN ('2024-08-01'),
    PARTITION p_2024_08 VALUES LESS THAN ('2024-09-01'),
    PARTITION p_2024_09 VALUES LESS THAN ('2024-10-01'),
    PARTITION p_2024_10 VALUES LESS THAN ('2024-11-01'),
    PARTITION p_2024_11 VALUES LESS THAN ('2024-12-01'),
    PARTITION p_2024_12 VALUES LESS THAN ('2025-01-01'),
    PARTITION p_2025_01 VALUES LESS THAN ('2025-02-01'),
    PARTITION p_2025_02 VALUES LESS THAN ('2025-03-01'),
    PARTITION p_2025_03 VALUES LESS THAN ('2025-04-01'),
    PARTITION p_2025_04 VALUES LESS THAN ('2025-05-01'),
    PARTITION p_2025_05 VALUES LESS THAN ('2025-06-01'),
    PARTITION p_2025_06 VALUES LESS THAN ('2025-07-01'),
    PARTITION p_2025_07 VALUES LESS THAN ('2025-08-01'),
    PARTITION p_2025_08 VALUES LESS THAN ('2025-09-01'),
    PARTITION p_2025_09 VALUES LESS THAN ('2025-10-01'),
    PARTITION p_2025_10 VALUES LESS THAN ('2025-11-01'),
    PARTITION p_2025_11 VALUES LESS THAN ('2025-12-01'),
    PARTITION p_2025_12 VALUES LESS THAN ('2026-01-01'),
    PARTITION p_2026_01 VALUES LESS THAN ('2026-02-01'),
    PARTITION p_2026_02 VALUES LESS THAN ('2026-03-01'),
    PARTITION p_2026_03 VALUES LESS THAN ('2026-04-01'),
    PARTITION p_2026_04 VALUES LESS THAN ('2026-05-01'),
    PARTITION p_2026_05 VALUES LESS THAN ('2026-06-01'),
    PARTITION p_2026_06 VALUES LESS THAN ('2026-07-01'),
    PARTITION p_future VALUES LESS THAN (MAXVALUE)
)
DISTRIBUTED BY HASH(vehicle_id) BUCKETS 10
PROPERTIES (
    "replication_num" = "1",
    "storage_medium" = "SSD"
);

CREATE INDEX idx_diag_vehicle_id ON diagnostics(vehicle_id);
CREATE INDEX idx_diag_vin ON diagnostics(vin);
CREATE INDEX idx_diag_type ON diagnostics(diagnostic_type);
CREATE INDEX idx_diag_result ON diagnostics(diagnostic_result);
CREATE INDEX idx_diag_time ON diagnostics(diagnostic_time);


-- ============================================
-- 4. 物化视图 - 信号聚合表
-- 用于加速实时统计查询
-- ============================================
DROP MATERIALIZED VIEW IF EXISTS mv_signal_hourly_stats;
CREATE MATERIALIZED VIEW mv_signal_hourly_stats
BUILD DEFERRED
AS
SELECT
    vehicle_id,
    signal_name,
    dt,
    dt_hour,
    COUNT(*) as signal_count,
    AVG(CAST(signal_value AS DOUBLE)) as avg_value,
    MIN(CAST(signal_value AS DOUBLE)) as min_value,
    MAX(CAST(signal_value AS DOUBLE)) as max_value,
    AVG(speed) as avg_speed
FROM vehicle_signals
GROUP BY vehicle_id, signal_name, dt, dt_hour;

-- ============================================
-- 5. 示例查询 - 常用统计
-- ============================================

-- 5.1 查询指定车辆的信号趋势 (毫秒级响应)
-- EXPLAIN SELECT 
--     collect_time,
--     signal_value
-- FROM vehicle_signals
-- WHERE vehicle_id = 1
--     AND signal_name = 'battery_voltage'
--     AND dt >= '2026-05-01' AND dt <= '2026-05-30'
-- ORDER BY collect_time;

-- 5.2 统计每日错误数量
-- SELECT 
--     dt,
--     ecu_type,
--     error_level,
--     COUNT(*) as error_count
-- FROM ecu_logs
-- WHERE dt >= '2026-05-01'
-- GROUP BY dt, ecu_type, error_level
-- ORDER BY dt DESC;

-- 5.3 车辆健康趋势
-- SELECT 
--     dt,
--     AVG(overall_health_score) as avg_health_score,
--     SUM(error_count) as total_errors,
--     SUM(warning_count) as total_warnings
-- FROM diagnostics
-- WHERE vehicle_id = 1 AND dt >= '2026-05-01'
-- GROUP BY dt
-- ORDER BY dt;
