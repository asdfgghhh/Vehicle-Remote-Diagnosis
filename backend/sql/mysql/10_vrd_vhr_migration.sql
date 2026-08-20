-- ============================================================
-- VRD VHR 对齐版 · 数据库迁移脚本 v2.0
-- 说明：新增 8 张表 + 修改 5 张表
-- 适用：MySQL 8.x；幂等设计（重复执行不报错）
-- 库分布：vrd_auth / vrd_vehicle / vrd_diagnosis
-- ============================================================

-- ################ vrd_auth ################

-- 1. sys_user 增加 last_login_time（支撑用户管理页"最后登录"列）
ALTER TABLE vrd_auth.sys_user
    ADD COLUMN IF NOT EXISTS last_login_time DATETIME NULL COMMENT '最后登录时间' AFTER update_time;

-- 2. 权限点表（权限管理页）
CREATE TABLE IF NOT EXISTS vrd_auth.sys_permission (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    parent_id   BIGINT DEFAULT 0 COMMENT '父权限 ID（0=顶级）',
    perm_code   VARCHAR(100) NOT NULL UNIQUE COMMENT '权限码，如 vehicle:list',
    perm_name   VARCHAR(100) NOT NULL COMMENT '权限名称',
    perm_type   TINYINT DEFAULT 1 COMMENT '1-菜单 2-按钮',
    route_path  VARCHAR(200) COMMENT '前端路由',
    icon        VARCHAR(100),
    sort_no     INT DEFAULT 0,
    status      TINYINT DEFAULT 1 COMMENT '1-启用 0-禁用',
    deleted     TINYINT DEFAULT 0,
    create_time DATETIME,
    update_time DATETIME,
    INDEX idx_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限点表';

-- 3. 角色-权限关联表
CREATE TABLE IF NOT EXISTS vrd_auth.sys_role_permission (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id       BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    create_time   DATETIME,
    UNIQUE KEY uk_role_perm (role_id, permission_id),
    INDEX idx_perm (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- 4. 权限点种子数据（16 页面 + 按钮级）
INSERT IGNORE INTO vrd_auth.sys_permission (perm_code, perm_name, perm_type, route_path, sort_no, create_time, update_time) VALUES
('dashboard:view',    '系统首页',     1, '/dashboard',                1,  NOW(), NOW()),
('vehicle:health',    '车辆健康',     1, '/vehicle-health',           2,  NOW(), NOW()),
('ai:diagnosis',      'AI 诊断',     1, '/ai-diagnosis',             3,  NOW(), NOW()),
('maintenance:view',  '智能维保',     1, '/maintenance',              4,  NOW(), NOW()),
('vehicle:model',     '车型管理',     1, '/vehicle/model',            5,  NOW(), NOW()),
('vehicle:list',      '车辆管理',     1, '/vehicle/list',             6,  NOW(), NOW()),
('dbc:manage',        'DBC 管理',     1, '/dbc',                      7,  NOW(), NOW()),
('monitor:signal',    '主动监控',     1, '/signal/monitor',           8,  NOW(), NOW()),
('fault:info',        '故障信息',     1, '/fault-info',               9,  NOW(), NOW()),
('diagnosis:remote',  '远程诊断',     1, '/diagnosis',               10,  NOW(), NOW()),
('log:analysis',      '日志分析',     1, '/ecu-log',                 11,  NOW(), NOW()),
('signal:analysis',   '信号分析',     1, '/signal/analysis',         12,  NOW(), NOW()),
('signal:playback',   '信号回放',     1, '/signal/playback',         13,  NOW(), NOW()),
('fault:analysis',    '故障分析',     1, '/fault-analysis',          14,  NOW(), NOW()),
('user:manage',       '用户管理',     1, '/settings/user',           15,  NOW(), NOW()),
('role:manage',       '权限管理',     1, '/settings/role',           16,  NOW(), NOW()),
('vehicle:add',       '新增车辆',     2, NULL,                        17,  NOW(), NOW()),
('vehicle:edit',      '编辑车辆',     2, NULL,                        18,  NOW(), NOW()),
('vehicle:delete',    '删除车辆',     2, NULL,                        19,  NOW(), NOW()),
('dbc:upload',        'DBC 上传',     2, NULL,                        20,  NOW(), NOW()),
('dbc:dispatch',      'DBC 分发',     2, NULL,                        21,  NOW(), NOW()),
('diagnosis:execute', '发起诊断',     2, NULL,                        22,  NOW(), NOW()),
('ai:chat',           'AI 对话',      2, NULL,                        23,  NOW(), NOW());

-- ################ vrd_vehicle ################

-- 5. vehicle 增加健康/风险字段（车辆管理页 + 智能维保页）
ALTER TABLE vrd_vehicle.vehicle
    ADD COLUMN IF NOT EXISTS health_score    INT DEFAULT 100 COMMENT '整车健康分 0-100' AFTER external_id,
    ADD COLUMN IF NOT EXISTS risk_level      VARCHAR(20) DEFAULT 'LOW' COMMENT '风险等级 LOW/MEDIUM/HIGH' AFTER health_score,
    ADD COLUMN IF NOT EXISTS battery_soh     DECIMAL(5,2) COMMENT '电池健康度 SOH%' AFTER risk_level,
    ADD COLUMN IF NOT EXISTS last_online_time DATETIME COMMENT '最近在线时间' AFTER battery_soh;

-- 6. vehicle_model 增加域覆盖字段
ALTER TABLE vrd_vehicle.vehicle_model
    ADD COLUMN IF NOT EXISTS domain_coverage VARCHAR(200) DEFAULT NULL COMMENT '覆盖域集合（逗号分隔）' AFTER description;

-- 7. fault_config 增加故障树场景关联字段
ALTER TABLE vrd_vehicle.fault_config
    ADD COLUMN IF NOT EXISTS fault_scene_id BIGINT NULL COMMENT '故障树场景ID' AFTER status,
    ADD COLUMN IF NOT EXISTS ai_badge       TINYINT DEFAULT 0 COMMENT 'AI场景识别标记' AFTER fault_scene_id;

-- 8. 车辆七大域健康状态表（车辆健康页）
CREATE TABLE IF NOT EXISTS vrd_vehicle.vehicle_health (
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    vin            VARCHAR(50) NOT NULL,
    domain_code    VARCHAR(30) NOT NULL COMMENT '域编码 ADAS/COCKPIT/POWERTRAIN/CHASSIS/BODY/BATTERY/TELEMATICS',
    domain_name    VARCHAR(50) COMMENT '域名称',
    health_score   INT DEFAULT 100 COMMENT '域健康分 0-100',
    status         VARCHAR(20) DEFAULT 'NORMAL' COMMENT 'NORMAL/ATTENTION/WARNING/DANGER',
    component_json JSON COMMENT '部件级健康 [{name,score,status}]',
    alert_count    INT DEFAULT 0 COMMENT '本域活跃告警数',
    risk_level     VARCHAR(20) DEFAULT 'LOW' COMMENT 'LOW/MEDIUM/HIGH',
    update_time    DATETIME,
    create_time    DATETIME,
    UNIQUE KEY uk_vin_domain (vin, domain_code),
    INDEX idx_vin (vin)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车辆七大域健康状态表';

-- 9. 故障树场景表（故障信息页/远程诊断页）
CREATE TABLE IF NOT EXISTS vrd_vehicle.fault_scene (
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    scene_code     VARCHAR(50) NOT NULL UNIQUE COMMENT '场景编码',
    scene_name     VARCHAR(100) NOT NULL COMMENT '场景名称',
    description    TEXT,
    fault_codes    VARCHAR(500) COMMENT '关联故障码集合',
    diag_sequence  TEXT COMMENT '诊断序列 JSON',
    priority       INT DEFAULT 1,
    ai_confidence  DECIMAL(5,2) DEFAULT 0,
    status         TINYINT DEFAULT 1,
    deleted        TINYINT DEFAULT 0,
    create_time    DATETIME,
    update_time    DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='故障树场景表';

-- 10. 故障树场景种子数据
INSERT IGNORE INTO vrd_vehicle.fault_scene (scene_code, scene_name, description, fault_codes, priority, ai_confidence, status, create_time, update_time) VALUES
('SCENE_ALL',          '全车通用扫描',   '全车 320+ 故障树扫描', NULL, 0, 0.90, 1, NOW(), NOW()),
('SCENE_PARK',         '车辆趴窝',       '动力中断无法行驶', 'P0217,P0301,P0562', 1, 0.93, 1, NOW(), NOW()),
('SCENE_THERMAL',      '电池热失控',     '电池温度异常/热失控风险', 'P0A7E,P0AA6,BMS_TEMP', 1, 0.96, 1, NOW(), NOW()),
('SCENE_BRAKE',        '制动力不足',     '制动系统效能下降', 'C0035,C0040', 1, 0.91, 1, NOW(), NOW()),
('SCENE_COLLISION',    '碰撞',           '碰撞事件检测', 'B0010,B0011', 1, 0.88, 1, NOW(), NOW()),
('SCENE_OTA_FAIL',     'OTA 升级失败',   'OTA 升级失败/回滚', 'U1A00,U1A01', 1, 0.90, 1, NOW(), NOW()),
('SCENE_COMM_LOST',    '通讯异常',       'ECU 通讯丢失', 'U0140,U0100,U0121', 1, 0.92, 1, NOW(), NOW());

-- 11. 保养计划表（智能维保页）
CREATE TABLE IF NOT EXISTS vrd_vehicle.maintenance_plan (
    id                 BIGINT PRIMARY KEY AUTO_INCREMENT,
    vin                VARCHAR(50) NOT NULL,
    plan_name          VARCHAR(100) NOT NULL COMMENT '保养项名称',
    plan_type          TINYINT DEFAULT 1 COMMENT '1-按里程 2-按时间 3-双维度',
    due_mileage        INT COMMENT '建议保养里程 km',
    due_date           DATE COMMENT '建议保养日期',
    last_done_mileage  INT COMMENT '上次保养里程',
    last_done_date     DATE COMMENT '上次保养日期',
    status             TINYINT DEFAULT 0 COMMENT '0-未到期 1-即将到期 2-已到期 3-已完成',
    advice             TEXT COMMENT '保养建议描述',
    create_time        DATETIME,
    update_time        DATETIME,
    INDEX idx_vin (vin)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='保养计划表';

-- 12. 维保记录表（智能维保页）
CREATE TABLE IF NOT EXISTS vrd_vehicle.maintenance_record (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    vin           VARCHAR(50) NOT NULL,
    record_type   TINYINT DEFAULT 1 COMMENT '1-保养 2-维修 3-检测',
    title         VARCHAR(200),
    content       TEXT,
    mileage       INT COMMENT '维保时里程',
    record_date   DATE,
    cost          DECIMAL(10,2),
    operator      VARCHAR(50),
    create_time   DATETIME,
    INDEX idx_vin (vin)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='维保记录表';

-- ################ vrd_diagnosis ################

-- 13. uds_diagnosis_session 增加三级诊断/故障树字段
ALTER TABLE vrd_diagnosis.uds_diagnosis_session
    ADD COLUMN IF NOT EXISTS diag_level       VARCHAR(20) DEFAULT 'VEHICLE' COMMENT '诊断层级 VEHICLE/DOMAIN/PART' AFTER session_status,
    ADD COLUMN IF NOT EXISTS scene_id         BIGINT NULL COMMENT '故障树场景ID' AFTER diag_level,
    ADD COLUMN IF NOT EXISTS fault_tree_result JSON NULL COMMENT '故障树扫描结果' AFTER scene_id;

-- 14. AI 诊断会话表（AI 诊断页）
CREATE TABLE IF NOT EXISTS vrd_diagnosis.ai_diagnosis_session (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    vin           VARCHAR(50),
    user_id       BIGINT,
    title         VARCHAR(200) COMMENT '会话标题',
    status        VARCHAR(20) DEFAULT 'RUNNING' COMMENT 'RUNNING/COMPLETED/FAILED',
    summary       TEXT COMMENT '会话结论摘要',
    report_json   JSON COMMENT '诊断报告 JSON',
    create_time   DATETIME,
    update_time   DATETIME,
    INDEX idx_vin (vin),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 诊断会话表';

-- 15. AI 诊断消息表
CREATE TABLE IF NOT EXISTS vrd_diagnosis.ai_diagnosis_message (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id   BIGINT NOT NULL,
    role         VARCHAR(20) COMMENT 'USER/ASSISTANT/SYSTEM',
    content      TEXT,
    msg_type     VARCHAR(30) DEFAULT 'TEXT' COMMENT 'TEXT/DIAG_CMD/REPORT/QUICK_QUESTION',
    ref_data     JSON COMMENT '关联诊断数据',
    create_time  DATETIME,
    INDEX idx_session (session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 诊断消息表';
