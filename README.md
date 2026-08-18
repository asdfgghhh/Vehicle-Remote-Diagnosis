# 车辆远程诊断系统 (Vehicle Remote Diagnosis System)

## 项目简介

这是一个基于SpringCloud微服务架构和Vue3前端的企业级车辆远程诊断系统，支持车辆管理、ECU日志上传、DBC文件管理、信号监控和大数据存储等核心功能。

## 系统架构

### 后端微服务架构 (SpringCloud)

```
┌─────────────────────────────────────────────────────────────────────┐
│                        API Gateway (9080)                           │
│              ┌──────────────────────────────────────────┐          │
│              │   AuthFilter: JWT校验(内省) → 用户信息注入 │          │
│              │   请求头: X-User-Id, X-Username, X-Roles  │          │
│              └──────────────────────────────────────────┘          │
└───────────────────────────┬─────────────────────────────────────────┘
                            │
        ┌───────────────────┼───────────────────┬───────────────────┐
        ▼                   ▼                   ▼                   ▼
┌───────────────┐   ┌───────────────┐   ┌───────────────┐   ┌───────────────┐
│ service-auth  │   │service-vehicle│   │ service-dbc   │   │service-       │
│ 认证服务      │   │ 车辆管理      │   │ DBC文件管理   │   │diagnosis      │
│ JWT签发/校验  │   │ 告警规则引擎  │   │ CAN帧编解码   │   │ UDS远程诊断   │
└───────────────┘   └───────────────┘   └───────────────┘   └───────────────┘
        │                   │                   │                   │
        ▼                   ▼                   ▼                   ▼
┌───────────────┐   ┌───────────────┐   ┌───────────────┐   ┌───────────────┐
│service-ecu-log│   │service-signal │   │ service-access│   │  common 模块  │
│ ECU日志查询   │   │ 信号监控      │   │ 数据接入      │   │ 统一响应/存储  │
│ 断点续传      │   │ MQTT/Kafka    │   │ MQTT→Kafka    │   │ 抽象/MyBatis  │
└───────────────┘   └───────────────┘   └───────────────┘   └───────────────┘
```

### 前端架构 (Vue3)

```
frontend/
├── src/
│   ├── api/                 # API接口定义
│   ├── views/               # 页面组件
│   │   ├── Login.vue        # 登录页
│   │   ├── Layout.vue       # 主布局（侧边栏导航）
│   │   ├── Dashboard.vue    # 仪表盘
│   │   ├── Vehicle*.vue     # 车辆管理（车型/车辆/同步记录）
│   │   ├── EcuLog.vue       # ECU日志
│   │   ├── config/          # DBC配置（CAN模型/故障配置）
│   │   ├── signal/          # 信号监控（故障监控/回放）
│   │   ├── settings/        # 系统设置（用户/角色）
│   │   └── UdsDiagnosis.vue # UDS远程诊断
│   ├── router/              # 路由配置
│   ├── utils/               # 工具函数（request/websocket）
│   └── styles/              # 全局样式
└── package.json
```

### 数据流架构

```
车端设备
    │
    ├── MQTT ──► service-access ──► Kafka ──► service-signal ──► BigDataStorage
    │                     │                                    (ClickHouse/Doris/TDengine)
    ├── HTTP ──► service-ecu-log ──────────────────────────────► BigDataStorage
    │
    └── API ──► service-gateway ──► service-vehicle ──► MySQL
                                    └──► Kafka ──► service-access ──► BigDataStorage

诊断指令
    │
    └── 管理端 ──► service-gateway ──► service-diagnosis ──► Kafka(uds-commands) ──► 车端

用户请求
    │
    └── service-gateway ──► AuthFilter校验JWT(内省) ──► 下游服务(信任网关身份)
                                    │
                                    └──► service-auth /service-vehicle /service-dbc /...
```

## 核心功能

### 1. 车型管理
- 车型信息的增删改查
- 品牌、厂商、车辆类型管理
- 发动机功率、变速箱类型等参数配置

### 2. 车辆管理
- 车辆基本信息管理（VIN、ECU版本等）
- 手动录入车辆信息
- Kafka数据同步
- API接口同步
- 车辆ECU零部件管理

### 3. ECU日志管理
- HTTPS接口上传日志文件
- 断点续传支持
- 文件下载功能
- 日志状态跟踪

### 4. DBC文件管理
- DBC文件上传
- 文件解析（消息和信号提取）
- 文件下发到车端
- 版本管理

### 5. 信号监控
- MQTT协议接收车辆信号
- 实时信号解析和存储
- 时间轴查询
- 信号趋势分析
- 历史数据查询

### 6. 数据接入
- MQTT消息接收
- Kafka消息队列
- ClickHouse大数据存储

### 7. UDS远程诊断
- 基于 ISO 14229 的 UDS 诊断协议（service-diagnosis）
- 诊断会话控制、ECU复位、安全访问（种子/密钥）
- DID数据读写、DTC故障码读取/清除、例程控制
- 内存读写、IO控制、Tester Present
- 诊断指令经 Kafka（uds-commands）下发车端
- 诊断会话历史查询（MySQL vrd_diagnosis）

## 技术栈

### 后端技术
- **框架**: Spring Boot 3.2.0, Spring Cloud 2023.0.0
- **注册配置中心**: Nacos 2.3
- **网关**: Spring Cloud Gateway
- **数据库**: MySQL 8.0, Redis
- **消息队列**: Apache Kafka 3.7
- **物联网**: MQTT (EMQX)
- **大数据存储**: ClickHouse / Doris / TDengine (可配置切换)
- **ORM**: MyBatis-Plus 3.5.9
- **诊断协议**: UDS (ISO 14229)
- **规则引擎**: Easy Rules 4.1
- **安全**: JWT (jjwt 0.12.6)
- **工具**: Hutool, FastJSON2

### 前端技术
- **框架**: Vue 3.4.0
- **路由**: Vue Router 4.2.5
- **状态管理**: Pinia 2.1.7
- **UI框架**: Element Plus 2.4.4
- **图表**: ECharts 5.4.3
- **构建工具**: Vite 5.0.8
- **HTTP**: Axios 1.6.2

### 基础设施
- **容器化**: Docker, Docker Compose
- **数据库**: MySQL 8.0
- **缓存**: Redis 7
- **消息队列**: Apache Kafka 3.7
- **物联网Broker**: EMQX 5
- **时序数据库**: ClickHouse (默认), Doris, TDengine (可配置)

## 项目结构

```
Vehicle-Remote-Diagnosis/
├── backend/                           # 后端微服务
│   ├── pom.xml                        # 父POM
│   ├── service-gateway/               # API网关 (9080)
│   ├── service-auth/                  # 认证服务 (9081)
│   ├── service-vehicle/               # 车辆管理+告警规则引擎 (9082)
│   ├── service-ecu-log/               # ECU日志 (9083)
│   ├── service-dbc/                   # DBC文件+原生解析器 (9084)
│   ├── service-signal/                # 信号采集 (9085)
│   ├── service-access/                # 数据接入 (9086)
│   ├── service-diagnosis/             # UDS远程诊断 (9087)
│   ├── nacos-configs/                 # Nacos配置文件
│   ├── sql/                           # MySQL/ClickHouse初始化脚本
│   ├── Dockerfile                     # 统一多阶段构建
│   └── common/                        # 公共模块
├── frontend/                          # 前端应用
│   ├── src/
│   │   ├── api/                      # API接口
│   │   ├── views/                    # 页面组件
│   │   ├── router/                   # 路由配置
│   │   └── styles/                   # 样式
│   ├── Dockerfile                     # 前端镜像 (Nginx)
│   └── package.json
├── dbc-parser-service/                # 遗留Python解析服务(已被Java原生替代)
├── deploy.sh                          # 一键构建部署脚本
├── docs/                              # 文档
├── docker-compose.yml                 # Docker编排
└── README.md                          # 项目说明

```

## 快速开始

### 环境要求
- Docker & Docker Compose
- 基础设施服务：MySQL、Redis、Kafka、MQTT、Nacos

### 1. 克隆项目

```bash
git clone https://github.com/your-repo/Vehicle-Remote-Diagnosis.git
cd Vehicle-Remote-Diagnosis
```

### 2. 登录镜像仓库

```bash
docker login 124.221.104.56:8211
```

### 3. 构建并推送镜像（可选，镜像已就绪可跳过）

```bash
# 本地一键构建并推送全部 8 个后端镜像 + 前端镜像
./deploy.sh [tag] [registry]
# 示例: ./deploy.sh v1.2.0 124.221.104.56:8211
```

**deploy.sh 自动执行以下操作：**
- Maven 打包全部后端微服务
- 构建前端 dist
- 并行构建 8 个后端 Docker 镜像 + 前端镜像
- 推送镜像到 Harbor 镜像仓库

### 4. 在目标服务器启动服务（从镜像仓库拉取并启动）

```bash
# 在服务器上拉取并启动
export TAG=latest REGISTRY=124.221.104.56:8211
docker compose pull
docker compose up -d --remove-orphans
```

**docker compose up 自动执行以下操作：**
- 创建 Docker 网络 `vrd-network`
- 按依赖顺序启动 8 个微服务容器 + 前端容器
- 自动重启策略（restart: always）

### 5. 停止服务

```bash
docker compose down
```

### 6. 访问系统

- **前端地址**: http://localhost:3000
- **API网关**: http://localhost:9080
- **Nacos控制台**: http://localhost:8848/nacos (用户名: nacos, 密码: nacos)
- **ClickHouse**: http://localhost:8123
- **Kafka**: localhost:9092
- **MQTT**: localhost:1883

### 7. 查看服务状态

```bash
# 查看所有服务状态
docker compose ps

# 查看服务日志
docker compose logs -f service-gateway
docker compose logs -f service-auth

# 查看特定服务日志（容器名）
docker logs vrd-vehicle -f
```

### 8. 手动部署（可选）

如果需要分步操作：

```bash
# 1. 构建后端（注意：本机需 JAVA_HOME 指向 JDK 17+，target 为 17）
cd backend
./mvnw clean package -DskipTests

# 2. 构建前端
cd ../frontend
npm install --legacy-peer-deps
npm run build

# 3. 构建 Docker 镜像（统一多阶段 Dockerfile，按服务传入 build-arg）
cd ..
docker build --build-arg SERVICE_NAME=service-diagnosis -f backend/Dockerfile -t 124.221.104.56:8211/vrd/service-diagnosis:latest backend

# 4. 启动服务（基础设施服务需提前启动）
export REGISTRY=124.221.104.56:8211 TAG=latest
docker compose up -d service-gateway service-auth service-vehicle service-ecu-log service-dbc service-signal service-access service-diagnosis
sleep 20
docker compose up -d frontend
```

**注意**：基础设施服务（MySQL、Redis、Kafka、EMQX、ClickHouse、Nacos）部署在宿主机（124.221.104.56），不在 docker-compose 编排内，需提前启动。

### 9. 更新单个服务（增量发布）

日常迭代无需全量重建，只需更新变更的服务即可。以下分别以**后端 service-dbc（:9084）**和**前端 frontend（:3000）**为例，其他服务替换服务名即可。

#### 9.1 更新后端服务（以 service-dbc 为例）

**① 本地构建并推送镜像**

```bash
cd Vehicle-Remote-Diagnosis

# 登录镜像仓库（未登录时）
docker login 124.221.104.56:8211

# 构建单个服务镜像（统一多阶段 Dockerfile，通过 SERVICE_NAME 指定服务）
docker build \
  --build-arg SERVICE_NAME=service-dbc \
  -t 124.221.104.56:8211/vrd/service-dbc:v1.2.1 \
  -t 124.221.104.56:8211/vrd/service-dbc:latest \
  -f backend/Dockerfile backend

# 推送镜像到 Harbor
docker push 124.221.104.56:8211/vrd/service-dbc:v1.2.1
docker push 124.221.104.56:8211/vrd/service-dbc:latest
```

> 注：统一 Dockerfile 内部会执行 Maven 全量编译，但最终镜像只包含 `service-dbc` 的 JAR，产物是单服务镜像。

**② 服务器拉取并重建该服务**

```bash
ssh root@124.221.104.56
cd /data/vrd   # docker-compose.yml 所在目录

# 拉取新镜像
docker compose pull service-dbc

# 仅重建该服务容器（--no-deps 避免连带重启其他服务）
docker compose up -d --no-deps service-dbc
```

**③ 验证**

```bash
docker ps | grep vrd-dbc
docker logs -f vrd-dbc
curl -s http://localhost:9084/actuator/health
```

#### 9.2 更新前端服务

**① 本地构建并推送镜像**

```bash
cd Vehicle-Remote-Diagnosis

docker build \
  -t 124.221.104.56:8211/vrd/frontend:v1.2.1 \
  -t 124.221.104.56:8211/vrd/frontend:latest \
  -f frontend/Dockerfile frontend

docker push 124.221.104.56:8211/vrd/frontend:v1.2.1
docker push 124.221.104.56:8211/vrd/frontend:latest
```

**② 服务器拉取并重建**

```bash
ssh root@124.221.104.56
cd /data/vrd

docker compose pull frontend
docker compose up -d --no-deps frontend
```

**③ 验证**

```bash
docker ps | grep vrd-frontend
docker logs -f vrd-frontend
# 浏览器访问 http://124.221.104.56:3000 确认页面已更新
```

#### 9.3 仅更新配置（不重新构建镜像）

若只修改了 Nacos 配置（如 `backend/nacos-configs/` 下的 yml），无需构建镜像：

```bash
# 在 Nacos 控制台修改并发布配置后，重启服务使其生效
docker restart vrd-dbc
```

#### 9.4 回滚到上一版本

```bash
cd /data/vrd
TAG=v1.2.0 docker compose up -d --no-deps service-dbc
```

#### 9.5 服务名 / 容器名 / 端口对照

| compose 服务名 | 容器名 | 端口 |
|---|---|---|
| service-gateway | vrd-gateway | 9080 |
| service-auth | vrd-auth | 9081 |
| service-vehicle | vrd-vehicle | 9082 |
| service-ecu-log | vrd-ecu-log | 9083 |
| service-dbc | vrd-dbc | 9084 |
| service-signal | vrd-signal | 9085 |
| service-access | vrd-access | 9086 |
| service-diagnosis | vrd-diagnosis | 9087 |
| frontend | vrd-frontend | 3000 |

> **注意**：`docker compose` 命令使用**服务名**（如 `service-dbc`），`docker ps / logs / restart` 使用**容器名**（如 `vrd-dbc`），两者不要混用。

## 配置说明

### 数据库配置
修改各服务的 `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/vrd_vehicle
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:root123}
```

### Kafka配置
```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
```

### MQTT配置
```yaml
mqtt:
  url: tcp://localhost:1883
  username: admin
  password: public
  topic: vehicle/signal/+
```

### 大数据存储配置 (Nacos shared-config: storage.yml)
支持通过配置切换大数据存储类型，默认使用ClickHouse

```yaml
bigdata:
  type: CLICKHOUSE  # 可选值: CLICKHOUSE, DORIS, TDENGINE
  clickhouse:
    host: localhost
    port: 8123
    database: vrd_bigdata
    username: default
    password:
  doris:
    host: localhost
    port: 8030
    database: vrd_bigdata
    username: root
    password:
  tdengine:
    host: localhost
    port: 6041
    database: vrd_bigdata
    username: root
    password:
```

### 网关认证配置
```yaml
gateway:
  auth:
    introspect-url: http://service-auth/auth/introspect   # 网关内省校验 Token
    white-list:
      - /api/auth/login
      - /api/auth/register
      - /api/auth/introspect
      - /actuator/**
```

## API接口

### 认证服务
- `POST /auth/login` - 用户登录
- `POST /auth/register` - 用户注册
- `GET /auth/validate` - 验证Token
- `POST /auth/introspect` - Token内省（网关调用）

### 车辆管理
- `GET /vehicle/model/page` - 车型分页查询
- `POST /vehicle/model` - 创建车型
- `PUT /vehicle/model/{id}` - 更新车型
- `DELETE /vehicle/model/{id}` - 删除车型
- `GET /vehicle/page` - 车辆分页查询
- `POST /vehicle` - 创建车辆
- `PUT /vehicle/{id}` - 更新车辆
- `POST /vehicle/sync/kafka` - Kafka同步
- `POST /vehicle/sync/api` - API同步

### ECU日志
- `GET /ecu-log/page` - 日志分页查询
- `POST /ecu-log/init-upload` - 初始化上传
- `POST /ecu-log/upload-chunk` - 上传分片
- `POST /ecu-log/merge-chunks` - 合并分片
- `GET /ecu-log/check-upload` - 检查上传状态
- `GET /ecu-log/download/{id}` - 下载日志

### DBC文件
- `GET /dbc/page` - 文件分页查询
- `POST /dbc/upload` - 上传DBC文件
- `GET /dbc/{id}/messages` - 获取消息列表
- `GET /dbc/{id}/signals` - 获取信号定义
- `GET /dbc/{id}/structured` - 结构化解析结果查询
- `GET /dbc/{id}/message/{messageId}` - 报文详情
- `GET /dbc/{id}/signal/{signalName}` - 信号详情
- `POST /dbc/{id}/decode` - CAN帧实时解码
- `POST /dbc/{id}/encode` - CAN帧编码测试
- `GET /dbc/{id}/java-constants` - Java常量类生成
- `GET /dbc/{id}/json-schema` - JSON Schema生成
- `POST /dbc/{id}/dispatch/{vehicleId}` - 下发到车辆
- `POST /dbc/{id}/dispatch` - 批量下发
- `DELETE /dbc/{id}` - 逻辑删除
- `GET /dbc/{id}/download` - 下载DBC文件

### 信号监控
- `GET /signal/timeline/{vehicleId}` - 时间轴查询
- `GET /signal/page/{vehicleId}` - 分页查询
- `GET /signal/signal-name/{vehicleId}` - 按信号名查询
- `GET /signal/{id}` - 单条信号详情

### UDS远程诊断
- `GET /diagnosis/services` - 支持的服务列表
- `POST /diagnosis/uds` - 通用UDS指令执行
- `POST /diagnosis/session/control` - 诊断会话控制
- `POST /diagnosis/ecu/reset` - ECU复位
- `POST /diagnosis/security/request-seed` - 安全访问种子
- `POST /diagnosis/security/send-key` - 安全访问密钥
- `POST /diagnosis/data/read` - 按ID读取数据
- `POST /diagnosis/data/write` - 按ID写入数据
- `POST /diagnosis/dtc/read` - 读取DTC故障码
- `POST /diagnosis/dtc/clear` - 清除DTC
- `POST /diagnosis/routine/control` - 例程控制
- `POST /diagnosis/memory/read` - 按地址读内存
- `POST /diagnosis/memory/write` - 按地址写内存
- `POST /diagnosis/io/control` - IO控制
- `POST /diagnosis/tester-present` - Tester Present保活
- `GET /diagnosis/sessions` - 诊断会话历史查询

## 开发指南

### 后端开发
```bash
# 编译单个模块
cd backend/service-vehicle
mvn clean package

# 运行单个服务
java -jar target/service-vehicle-1.0.0-SNAPSHOT.jar

# 运行测试
mvn test
```

### 前端开发
```bash
cd frontend

# 安装依赖
npm install

# 开发模式
npm run dev

# 构建生产版本
npm run build

# 预览
npm run preview
```

## 数据同步

### 数据流架构

**车端数据流向：**
1. **实时信号**: MQTT/HTTP → service-access → Kafka → service-signal → BigDataStorage
2. **ECU日志**: HTTP 分片/直传 → service-access → 对象存储 + ClickHouse → service-ecu-log 查询
3. **车辆数据**: API → service-vehicle → MySQL → Kafka → service-access → BigDataStorage
4. **诊断指令**: 管理端 → service-diagnosis → Kafka (uds-commands) → 车端

**大数据存储**: 通过 `bigdata.type` 配置切换 ClickHouse/Doris/TDengine

### Kafka 主题
- `vehicle-data` - 车辆主数据同步（service-vehicle 生产/消费）
- `vehicle-signals` - 车辆信号数据（service-access 生产，service-access/service-vehicle 消费）
- `uds-commands` - UDS 诊断指令下发（service-diagnosis 生产）
- `uds-responses` - UDS 诊断响应回传（预留）

### MQTT主题
- `vehicle/signal/+` - 车辆信号数据（`+` 为 VIN 通配）

### 认证流程
1. 用户登录 → service-auth 签发 JWT
2. 请求经过网关 → AuthFilter 调用 service-auth `/auth/introspect` 校验 Token
3. 网关将 userId/roles 注入请求头 → 下游服务直接从请求头获取用户信息
4. 下游服务不再各自解析 JWT，只信任网关转发的身份

## 监控运维

### 服务监控
- Nacos Console: http://localhost:8848
- 查看服务注册状态
- 查看配置管理

### Kafka监控
- 通过 Kafka 命令行工具（宿主机部署）监控主题与消费者组
- 各服务日志查看: `docker compose logs -f <service>`

### 日志管理
- 应用日志: `/var/log/vrd/`
- Kafka日志: Docker容器日志
- Nginx日志: `/var/log/nginx/`

## 性能优化

### 数据库优化
- 使用连接池 (HikariCP)
- 索引优化
- 读写分离

### 缓存优化
- Redis缓存热点数据
- 本地缓存
- 缓存失效策略

### 消息队列优化
- Kafka分区策略
- 消费者组配置
- 消息压缩

## 安全配置

### JWT配置
```yaml
jwt:
  secret: your-secret-key
  expiration: 86400000
```

### API网关安全
- Token验证（统一在网关层）
- 限流策略
- 跨域配置

## 常见问题

### 1. 服务启动失败
- 检查端口占用
- 检查数据库连接
- 查看日志文件

### 2. Kafka连接失败
- 检查网络连接
- 验证端口配置（默认 9092，地址经 Nacos 配置下发）

### 3. MQTT连接失败
- 确认 EMQX 已启动
- 检查认证信息（service-access.yml 的 mqtt.username/password）
- 验证主题权限

## 扩展功能

### 1. 添加新微服务
1. 创建模块目录
2. 编写pom.xml
3. 实现业务代码
4. 添加Dockerfile
5. 更新docker-compose.yml

### 2. 添加新功能
1. 修改后端API
2. 添加前端组件
3. 更新路由配置
4. 编写测试用例

## 性能指标

- 支持10000+车辆同时在线
- 实时信号延迟 < 100ms
- 日志上传速度 > 10MB/s
- 系统可用性 > 99.9%

## 许可证

MIT License

## 技术支持

- 文档: docs/
- 问题反馈: Issues
- 技术讨论: Discussions

## 版本历史

### v1.2.0 (2026-08)
- UDS 远程诊断服务（service-diagnosis，ISO 14229）
- DBC 解析改为纯 Java 原生实现（DbcParser + CanFrameCodec），移除 Python cantools 依赖
- 告警规则引擎（Easy Rules：阈值/趋势/组合规则）
- WebSocket 实时信号推送
- CI/CD 流水线 + Harbor 镜像仓库部署

### v1.1.0 (2026-06)
- 网关认证升级为 Token 内省（introspect）模式
- ClickHouse 时序存储落地
- 前端完整版管理系统（登录 + Layout + 14 路由）

### v1.0.0 (2024-01-15)
- 初始版本
- 支持车辆管理、ECU日志、DBC文件、信号监控、数据接入
- 微服务架构
- Docker容器化部署

## 贡献者

欢迎提交Issue和Pull Request！

## 致谢

- Spring Cloud Team
- Vue.js Team
- Element Plus Team
- 所有开源社区贡献者
