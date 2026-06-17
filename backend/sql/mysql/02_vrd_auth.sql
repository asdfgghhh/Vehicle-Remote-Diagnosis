USE vrd_auth;

CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    real_name VARCHAR(50),
    status INT DEFAULT 1 COMMENT '1-启用 0-禁用',
    deleted INT DEFAULT 0,
    create_time DATETIME,
    update_time DATETIME,
    INDEX idx_username (username),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_code VARCHAR(50) NOT NULL UNIQUE,
    role_name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    status INT DEFAULT 1,
    deleted INT DEFAULT 0,
    create_time DATETIME,
    update_time DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    create_time DATETIME,
    UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO sys_user (username, password, email, real_name, status, deleted, create_time, update_time)
VALUES ('admin', '$2a$10$9sCn.a.mx9Xm/bvr6Dytm.RTNkwAggG334aleN7n7e9WiJKOKhBxa', 'admin@vrd.com', '系统管理员', 1, 0, NOW(), NOW());

INSERT INTO sys_role (role_code, role_name, description, status, deleted, create_time, update_time)
VALUES ('ADMIN', '系统管理员', '拥有系统全部权限', 1, 0, NOW(), NOW()),
       ('OPERATOR', '操作员', '业务操作权限', 1, 0, NOW(), NOW()),
       ('VIEWER', '只读用户', '只读查看权限', 1, 0, NOW(), NOW());

INSERT INTO sys_user_role (user_id, role_id, create_time)
VALUES (1, 1, NOW());