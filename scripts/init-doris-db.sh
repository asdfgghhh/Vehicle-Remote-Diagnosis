#!/bin/bash

# Doris数据库初始化脚本
# 使用方法: ./init-doris-db.sh

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DORIS_DIR="$(dirname "$SCRIPT_DIR")"

echo "============================================"
echo "  初始化Doris数据库"
echo "============================================"
echo ""

# 等待FE就绪
echo "等待Doris FE启动..."
MAX_WAIT=120
WAIT=0
while true; do
    if docker logs vrd-doris-fe 2>/dev/null | grep -q "start to work"; then
        echo "✅ Doris FE 已启动"
        break
    fi
    if [ $WAIT -ge $MAX_WAIT ]; then
        echo "❌ 等待超时，请检查Doris日志"
        exit 1
    fi
    sleep 5
    WAIT=$((WAIT + 5))
    echo "等待FE启动... (${WAIT}s)"
done

# 添加BE到集群
echo ""
echo "添加BE节点到集群..."
sleep 10
docker exec -i vrd-doris-fe mysql -h localhost -P 9030 -u root <<END_SQL
ALTER SYSTEM ADD BACKEND 'vrd-doris-be:9050';
END_SQL
echo "✅ BE节点已添加"

# 等待BE加入集群
echo ""
echo "等待BE加入集群..."
sleep 20

# 创建数据库和表
echo ""
echo "创建数据库和表..."
docker exec -i vrd-doris-fe mysql -h localhost -P 9030 -u root <<END_SQL
CREATE DATABASE IF NOT EXISTS vrd_bigdata;
USE vrd_bigdata;

-- 车辆信号表
CREATE TABLE IF NOT EXISTS vehicle_signals (
    id BIGINT,
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
    id BIGINT,
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
    id BIGINT,
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

SHOW TABLES;
END_SQL

echo ""
echo "✅ 数据库和表创建完成"

# 验证
echo ""
echo "============================================"
echo "  验证Doris集群状态"
echo "============================================"
docker exec -i vrd-doris-fe mysql -h localhost -P 9030 -u root <<END_SQL
SHOW FRONTENDS;
SHOW BACKENDS;
USE vrd_bigdata;
SHOW TABLES;
END_SQL

echo ""
echo "============================================"
echo "  初始化完成！"
echo "============================================"
echo ""
echo "访问地址："
echo "  FE Web控制台: http://localhost:8030"
echo "  MySQL连接: localhost:9030"
echo ""
echo "数据库: vrd_bigdata"
echo "用户名: root"
echo "密码: (空)"
echo ""
