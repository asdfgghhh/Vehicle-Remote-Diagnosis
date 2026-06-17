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