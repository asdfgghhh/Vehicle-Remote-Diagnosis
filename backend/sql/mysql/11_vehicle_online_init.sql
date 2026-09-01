-- ============================================================
-- 车辆在线状态管理 MySQL 初始化脚本（vrd_vehicle 库）
-- 依赖：03_vrd_vehicle.sql / 10_vrd_vhr_migration.sql 已执行
-- ============================================================
USE vrd_vehicle;

-- 1) 确保 vehicle.last_online_time 字段存在（10_vrd_vhr_migration 已包含，这里兼容首次部署场景）
ALTER TABLE vehicle
    ADD COLUMN IF NOT EXISTS last_online_time DATETIME COMMENT '最近在线时间' AFTER data_source;

-- 2) vehicle_online_stat 唯一键与状态查询索引（兜底扫描/统计查询依赖）
ALTER TABLE vehicle_online_stat
    ADD UNIQUE KEY IF NOT EXISTS uk_stat_time_granularity (stat_time, stat_granularity);
CREATE INDEX IF NOT EXISTS idx_vehicle_status_deleted ON vehicle(status, deleted);
