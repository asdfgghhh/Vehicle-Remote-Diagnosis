-- ============================================================
-- ClickHouse 车辆在线状态变更日志表
-- 执行库: vrd_bigdata (与 ecu_log_records 保持一致)
-- 方言注意 (经验 #948492):
--   1. 不要写 CREATE INDEX，CH 依赖 PARTITION BY + ORDER BY + Skip Index 加速
--   2. 主键列必须在 ORDER BY 前缀里 (列存稀疏索引)
--   3. String 类型统一用 String/LowCardinality(String)；UNSIGNED 用 UInt64
--   4. 支持 TTL 自动清理过期数据（默认保留12个月）
-- ============================================================

CREATE TABLE IF NOT EXISTS vrd_bigdata.vehicle_online_log (
    id               UInt64,                         -- 日志ID(雪花ID)
    vin              String,                         -- 车架号（核心过滤维度）
    vehicle_id       UInt64,                         -- 车辆ID
    status_from      Int8,                           -- 变更前：0-离线 1-在线 2-未知
    status_to        Int8,                           -- 变更后：0-离线 1-在线 2-未知
    reason           LowCardinality(String),         -- 触发原因：mqtt_online / mqtt_offline / timeout_check / scheduler / api_update
    source           LowCardinality(String),         -- 来源：MQTT / KAFKA / SCHEDULER / API / BROKER_WEBHOOK
    ext_info         String   DEFAULT '',            -- 扩展信息(JSON)
    event_time       DateTime64(3, 'Asia/Shanghai'), -- 事件发生时间(毫秒精度)
    create_time      DateTime DEFAULT now()          -- 落库时间
) ENGINE = MergeTree()
-- 按月分区，便于 TTL 物理清理 + 分区裁剪
PARTITION BY toYYYYMM(event_time)
-- 查询主要按 vin + 时间范围 → vin 放最左可命中主键稀疏索引；时间做 secondary order
ORDER BY (vin, event_time, status_to)
-- 主键 (CH里 PRIMARY KEY 必须是 ORDER BY 前缀，可省略默认与 ORDER BY 相同)
PRIMARY KEY (vin, event_time)
-- 跳数索引：按 reason / source 高频过滤时可直接跳过 granule
-- 经验 #948492: 不要用 CREATE INDEX，必须写在建表 SETTINGS 之前或 USING 子句里
,INDEX idx_status_to status_to TYPE set(100) GRANULARITY 4
,INDEX idx_reason    reason    TYPE bloom_filter GRANULARITY 2
-- TTL：12个月后自动清理，避免人工 DELETE 产生 part 合并压力
TTL event_time + INTERVAL 12 MONTH
SETTINGS index_granularity = 8192;

-- ============================================================
-- SQL 使用样例
-- ============================================================
-- a. 写入 (通过 BigDataClient.insertJson -> 内部 INSERT SELECT / VALUES)
-- INSERT INTO vrd_bigdata.vehicle_online_log
--   (id, vin, vehicle_id, status_from, status_to, reason, source, event_time, ext_info)
-- VALUES
--   (1234567890123456789, 'LSVAG4189ES123456', 1, 0, 1, 'mqtt_online', 'MQTT',
--    toDateTime64('2026-08-31 14:30:00.000', 3, 'Asia/Shanghai'), '{"signal":-75}');
--
-- b. 单辆车最近30天状态变更
-- SELECT id, vin, status_from, status_to, reason, source, event_time, create_time
--   FROM vrd_bigdata.vehicle_online_log
--  WHERE vin = 'LSVAG4189ES123456'
--    AND event_time >= now() - INTERVAL 30 DAY
--  ORDER BY event_time DESC
--  LIMIT 500;
--
-- c. 近7天每日在线/离线事件数
-- SELECT toDate(event_time) AS dt,
--        status_to,
--        count() AS cnt
--   FROM vrd_bigdata.vehicle_online_log
--  WHERE event_time >= now() - INTERVAL 7 DAY
--  GROUP BY dt, status_to
--  ORDER BY dt, status_to;
--
-- d. 近30天上线次数Top10车辆
-- SELECT vin, count() AS online_count
--   FROM vrd_bigdata.vehicle_online_log
--  WHERE status_to = 1
--    AND event_time >= now() - INTERVAL 30 DAY
--  GROUP BY vin
--  ORDER BY online_count DESC
--  LIMIT 10;
