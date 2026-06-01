#!/bin/bash

echo "============================================"
echo "  等待Doris FE启动..."
echo "============================================"

max_attempts=30
attempt=0
while [ $attempt -lt $max_attempts ]; do
    if mysql -h doris-fe -P 9030 -u root --connect-timeout 5 -e "SHOW FRONTENDS;" > /dev/null 2>&1; then
        echo "Doris FE 已启动"
        break
    fi
    attempt=$((attempt + 1))
    echo "等待Doris FE启动... ($attempt/$max_attempts)"
    sleep 5
done

if [ $attempt -eq $max_attempts ]; then
    echo "Doris FE 启动超时"
    exit 1
fi

echo ""
echo "============================================"
echo "  添加BE节点到集群..."
echo "============================================"

mysql -h doris-fe -P 9030 -u root <<EOF
ADD BACKEND 'doris-be:9050';
EOF

echo ""
echo "============================================"
echo "  创建数据库和表..."
echo "============================================"

mysql -h doris-fe -P 9030 -u root <<EOF

CREATE DATABASE IF NOT EXISTS vrd_bigdata;

USE vrd_bigdata;

-- 车辆信号表
CREATE TABLE IF NOT EXISTS vehicle_signals (
    id BIGINT AUTO_INCREMENT,
    vehicle_id BIGINT NOT NULL,
    vin VARCHAR(17) NOT NULL,
    signal_name VARCHAR(64) NOT NULL,
    signal_value VARCHAR(128),
    signal_unit VARCHAR(32),
    signal_type TINYINT,
    longitude DECIMAL(10, 6),
    latitude DECIMAL(10, 6),
    speed DECIMAL(6, 2),
    collect_time DATETIME NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    dt DATE NOT NULL,
    dt_hour TINYINT NOT NULL
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

-- ECU日志表
CREATE TABLE IF NOT EXISTS ecu_logs (
    id BIGINT AUTO_INCREMENT,
    vehicle_id BIGINT NOT NULL,
    vin VARCHAR(17) NOT NULL,
    ecu_type VARCHAR(32) NOT NULL,
    error_code VARCHAR(16),
    error_level TINYINT,
    error_desc VARCHAR(512),
    log_content TEXT,
    longitude DECIMAL(10, 6),
    latitude DECIMAL(10, 6),
    speed DECIMAL(6, 2),
    rpm INT,
    collect_time DATETIME NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    dt DATE NOT NULL
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

-- 诊断数据表
CREATE TABLE IF NOT EXISTS diagnostics (
    id BIGINT AUTO_INCREMENT,
    vehicle_id BIGINT NOT NULL,
    vin VARCHAR(17) NOT NULL,
    diagnostic_type VARCHAR(32) NOT NULL,
    diagnostic_result VARCHAR(16) NOT NULL,
    overall_health_score DECIMAL(5, 2),
    system_count INT,
    error_count INT,
    warning_count INT,
    diagnostic_detail TEXT,
    technician_id BIGINT,
    diagnostic_time DATETIME NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    dt DATE NOT NULL
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

-- 创建索引
CREATE INDEX idx_vs_vehicle ON vehicle_signals(vehicle_id);
CREATE INDEX idx_vs_vin ON vehicle_signals(vin);
CREATE INDEX idx_vs_signal ON vehicle_signals(signal_name);
CREATE INDEX idx_vs_time ON vehicle_signals(collect_time);

CREATE INDEX idx_el_vehicle ON ecu_logs(vehicle_id);
CREATE INDEX idx_el_vin ON ecu_logs(vin);
CREATE INDEX idx_el_error ON ecu_logs(error_code);

CREATE INDEX idx_dg_vehicle ON diagnostics(vehicle_id);
CREATE INDEX idx_dg_vin ON diagnostics(vin);

EOF

echo ""
echo "============================================"
echo "  初始化完成！"
echo "============================================"
echo ""
echo "Doris FE: localhost:9030"
echo "Doris FE Web: localhost:8030"
echo "数据库: vrd_bigdata"
echo "用户名: root"
echo "密码: (空)"
echo ""
