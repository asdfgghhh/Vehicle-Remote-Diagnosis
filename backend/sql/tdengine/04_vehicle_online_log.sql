-- ============================================================
-- TDengine 车辆在线状态变更日志（超级表）
-- 执行库: vrd_bigdata (与 ecu_log_records 保持一致)
-- 方言注意:
--   1. TSDB 不支持传统 BTree 索引，依赖 TIMESTAMP 主键 + TAGS 分区剪枝
--   2. 写入时使用 INSERT INTO tbl USING stb TAGS(vin) VALUES(...) 自动建子表
--   3. 保留策略默认 365 天；如需更长可修改 KEEP
-- ============================================================
USE vrd_bigdata;

-- 1) 超级表：事件时间作为第一主键列
CREATE STABLE IF NOT EXISTS stb_vehicle_online_log (
    event_time       TIMESTAMP,       -- 事件发生时间(TSDB 主键时间戳，业务事件真实时间)
    id               BIGINT,          -- 日志ID(雪花ID，用于链路追踪)
    vehicle_id       BIGINT,          -- 车辆ID（冗余，方便与 MySQL vehicle 表关联）
    status_from      INT,             -- 变更前状态：0-离线 1-在线 2-未知
    status_to        INT,             -- 变更后状态：0-离线 1-在线 2-未知
    reason           NCHAR(50),       -- 触发原因：mqtt_online / mqtt_offline / timeout_check / scheduler / api_update
    source           NCHAR(20),       -- 来源：MQTT / KAFKA / SCHEDULER / API / BROKER_WEBHOOK
    ext_info         NCHAR(500),      -- 扩展信息(JSON 字符串，可选：信号强度、电压等)
    create_time      TIMESTAMP        -- 落库时间(仅用于写入追踪)
) TAGS (
    vin              NCHAR(50)        -- TAG: 车架号，每辆车一张子表，关键剪枝维度
);

-- 2) 可选：创建保留策略(默认365天；按实际业务调整)
-- CREATE KEEP 365 ON DATABASE vrd_bigdata;  -- 如果整个库的 KEEP 需要单独设置则执行

-- 3) SQL 使用样例
-- a. 写入：自动按 VIN 建子表（若子表已存在则直接写）
-- INSERT INTO vehicle_online_log_LSVAG4189ES123456
--   USING stb_vehicle_online_log TAGS ('LSVAG4189ES123456')
--   VALUES (NOW, 1234567890123456789, 1, 0, 1, 'mqtt_online', 'MQTT', '{"signal":-75}', NOW);
--
-- b. 查询某车最近30天记录
-- SELECT event_time, status_from, status_to, reason, source, create_time
--   FROM stb_vehicle_online_log
--  WHERE vin = 'LSVAG4189ES123456'
--    AND event_time >= NOW - 30d
--  ORDER BY event_time DESC;
--
-- c. 统计每日上下线事件数量
-- SELECT COUNT(*) as cnt, status_to
--   FROM stb_vehicle_online_log
--  WHERE event_time >= NOW - 7d
--  INTERVAL(1d) PARTITION BY status_to;
