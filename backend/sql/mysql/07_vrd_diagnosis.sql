USE vrd_diagnosis;

CREATE TABLE IF NOT EXISTS uds_diagnosis_session (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    trace_id VARCHAR(64) NOT NULL COMMENT '请求追踪ID',
    vin VARCHAR(32) NOT NULL COMMENT '车辆VIN',
    vehicle_id BIGINT COMMENT '车辆ID',
    ecu_type VARCHAR(64) COMMENT 'ECU类型/地址',
    service_id INT NOT NULL COMMENT 'UDS服务ID(SID)',
    sub_function INT COMMENT '子功能码',
    request_data TEXT COMMENT '请求数据(十六进制)',
    response_data TEXT COMMENT '响应数据(十六进制)',
    success TINYINT NOT NULL DEFAULT 1 COMMENT '是否成功: 1-成功 0-失败',
    negative_response_code INT COMMENT '否定响应码(NRC)',
    session_status VARCHAR(32) COMMENT '会话状态',
    response_time_ms BIGINT COMMENT '响应耗时(毫秒)',
    operator VARCHAR(64) COMMENT '操作员',
    remark VARCHAR(512) COMMENT '备注',
    request_time DATETIME COMMENT '请求时间',
    response_time DATETIME COMMENT '响应时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_vin (vin),
    INDEX idx_trace_id (trace_id),
    INDEX idx_service_id (service_id),
    INDEX idx_request_time (request_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='UDS诊断会话记录';

CREATE TABLE IF NOT EXISTS uds_dtc_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    trace_id VARCHAR(64) NOT NULL COMMENT '会话追踪ID',
    vin VARCHAR(32) NOT NULL COMMENT '车辆VIN',
    vehicle_id BIGINT COMMENT '车辆ID',
    ecu_type VARCHAR(64) COMMENT 'ECU类型',
    dtc_code VARCHAR(16) NOT NULL COMMENT 'DTC码(如P0300,U0100)',
    dtc_status INT COMMENT 'DTC状态字节',
    dtc_description VARCHAR(256) COMMENT 'DTC描述',
    severity TINYINT DEFAULT 2 COMMENT '严重级别: 1-严重 2-警告 3-提示',
    fault_status TINYINT DEFAULT 0 COMMENT '故障状态: 0-未确认 1-已确认 2-历史',
    detection_time DATETIME COMMENT '检测时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_vin (vin),
    INDEX idx_trace_id (trace_id),
    INDEX idx_dtc_code (dtc_code),
    INDEX idx_detection_time (detection_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='UDS DTC故障码记录';
