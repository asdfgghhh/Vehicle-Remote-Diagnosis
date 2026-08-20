-- ============================================================
-- VRD VHR 对齐版 · 健康数据种子脚本 v1.0
-- 说明：为已有车辆生成七大域健康数据 + 整车健康分初始化
-- 适用：MySQL 8.x；幂等设计（INSERT IGNORE / 条件 UPDATE 可重复执行）
-- ============================================================

-- 1. 整车健康分/风险等级初始化（仅当为空时）
UPDATE vrd_vehicle.vehicle
SET health_score = 95, risk_level = 'LOW'
WHERE deleted = 0 AND health_score IS NULL;

-- 2. 为已有车辆生成七大域健康数据（每车 7 行）
INSERT IGNORE INTO vrd_vehicle.vehicle_health
    (vin, domain_code, domain_name, health_score, status, component_json, alert_count, risk_level, update_time, create_time)
SELECT v.vin,
       d.domain_code,
       d.domain_name,
       CASE d.domain_code
           WHEN 'ADAS'       THEN 95
           WHEN 'COCKPIT'    THEN 92
           WHEN 'POWERTRAIN' THEN 98
           WHEN 'CHASSIS'    THEN 97
           WHEN 'BODY'       THEN 96
           WHEN 'BATTERY'    THEN 99
           WHEN 'TELEMATICS' THEN 94
           ELSE 100 END,
       'NORMAL',
       NULL,
       0,
       'LOW',
       NOW(),
       NOW()
FROM vrd_vehicle.vehicle v
CROSS JOIN (
    SELECT 'ADAS'       AS domain_code, '智能驾驶域' AS domain_name UNION ALL
    SELECT 'COCKPIT',    '智能座舱域' UNION ALL
    SELECT 'POWERTRAIN', '动力域'     UNION ALL
    SELECT 'CHASSIS',    '底盘域'     UNION ALL
    SELECT 'BODY',       '车身域'     UNION ALL
    SELECT 'BATTERY',    '三电域'     UNION ALL
    SELECT 'TELEMATICS', '网联域'
) d
WHERE v.deleted = 0;

-- 3. 按故障情况动态下调健康分（演示数据更真实：每 2 个未处理故障降 1 分，最低 60）
UPDATE vrd_vehicle.vehicle_health h
SET h.health_score = GREATEST(60, 100 - (
        SELECT COUNT(*) FROM vrd_vehicle.vehicle_fault f
        WHERE f.vin = h.vin AND f.deleted = 0 AND f.status = 1 AND f.component_code = h.domain_code
    ) * 5),
    h.status = CASE
        WHEN h.health_score >= 90 THEN 'NORMAL'
        WHEN h.health_score >= 75 THEN 'ATTENTION'
        WHEN h.health_score >= 60 THEN 'WARNING'
        ELSE 'DANGER' END,
    h.update_time = NOW()
WHERE EXISTS (
    SELECT 1 FROM vrd_vehicle.vehicle_fault f
    WHERE f.vin = h.vin AND f.deleted = 0 AND f.status = 1 AND f.component_code = h.domain_code
);

-- 4. 整车健康分同步（取七大域平均）
UPDATE vrd_vehicle.vehicle v
SET v.health_score = (
        SELECT ROUND(AVG(h.health_score)) FROM vrd_vehicle.vehicle_health h WHERE h.vin = v.vin
    ),
    v.risk_level = CASE
        WHEN (SELECT AVG(h.health_score) FROM vrd_vehicle.vehicle_health h WHERE h.vin = v.vin) >= 90 THEN 'LOW'
        WHEN (SELECT AVG(h.health_score) FROM vrd_vehicle.vehicle_health h WHERE h.vin = v.vin) >= 75 THEN 'MEDIUM'
        ELSE 'HIGH' END,
    v.update_time = NOW()
WHERE v.deleted = 0
  AND (SELECT AVG(h.health_score) FROM vrd_vehicle.vehicle_health h WHERE h.vin = v.vin) IS NOT NULL;
