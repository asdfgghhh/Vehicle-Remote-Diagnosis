# 车辆远程诊断系统 (Vehicle Remote Diagnosis System)

## 项目简介

这是一个基于SpringCloud微服务架构和Vue3前端的企业级车辆远程诊断系统，支持车辆管理、ECU日志上传、DBC文件管理、信号监控和大数据存储等核心功能。

## 系统架构

### 后端微服务架构 (SpringCloud)

```
┌─────────────────────────────────────────────────────────────────────┐
│                        API Gateway (8080)                           │
│              ┌──────────────────────────────────────────┐          │
│              │   AuthFilter: JWT验证 → 用户信息注入       │          │
│              │   请求头: X-User-Id, X-Username, X-Roles  │          │
│              └──────────────────────────────────────────┘          │
└───────────────────────────┬─────────────────────────────────────────┘
                            │
        ┌───────────────────┼───────────────────┐
        ▼                   ▼                   ▼
┌───────────────┐   ┌───────────────┐   ┌───────────────┐
│ service-auth  │   │service-vehicle│   │ service-dbc   │
│ 认证服务      │   │ 车辆管理      │   │ DBC文件管理   │
│ JWT签发/验证  │   │ Kafka消费     │   │ 消息解析      │
└───────────────┘   └───────────────┘   └───────────────┘
        │                   │                   │
        ▼                   ▼                   ▼
┌───────────────┐   ┌───────────────┐   ┌───────────────┐
│service-ecu-log│   │service-signal │   │ service-access│
│ ECU日志上传   │   │ 信号监控      │   │ 数据接入      │
│ 断点续传      │   │ MQTT/Kafka    │   │ MQTT→Kafka    │
└───────────────┘   └───────────────┘   └───────────────┘
                            │
                            ▼
┌────────────────────────────────────────────────────────┐
│                      common 模块                        │
│  ┌───────────────────┐  ┌───────────────────────────┐  │
│  │   BigDataClient   │  │       UserContext         │  │
│  │   ├─ ClickHouse   │  │   请求头用户信息提取工具   │  │
│  │   ├─ Doris        │  │                           │  │
│  │   └─ TDengine     │  │                           │  │
│  │   HTTP客户端/重试  │  └───────────────────────────┘  │
│  └───────────────────┘                                 │
└────────────────────────────────────────────────────────┘
```

### 前端架构 (Vue3)

```
frontend/
├── src/
│   ├── api/                 # API接口定义
│   ├── views/               # 页面组件
│   │   ├── Vehicle/         # 车辆管理
│   │   ├── EcuLog/          # ECU日志
│   │   ├── DbcFile/         # DBC文件
│   │   └── Signal/          # 信号监控
│   ├── router/              # 路由配置（动态路由）
│   ├── stores/              # Pinia状态管理
│   ├── utils/               # 工具函数
│   ├── styles/              # 全局样式
│   └── layouts/             # 布局组件
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

用户请求
    │
    └── service-gateway ──► AuthFilter验证JWT ──► 下游服务(信任网关身份)
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

## 技术栈

### 后端技术
- **框架**: Spring Boot 3.2.0, Spring Cloud 2023.0.0
- **注册配置中心**: Nacos
- **网关**: Spring Cloud Gateway
- **数据库**: MySQL 8.0, Redis
- **消息队列**: Apache Kafka
- **物联网**: MQTT (Eclipse Mosquitto)
- **大数据存储**: ClickHouse / Doris / TDengine (可配置切换)
- **ORM**: MyBatis-Plus 3.5.5
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
- **消息队列**: Apache Kafka 7.5.0
- **物联网Broker**: Eclipse Mosquitto 2
- **时序数据库**: ClickHouse (默认), Doris, TDengine (可配置)

## 项目结构

```
Vehicle-Remote-Diagnosis/
├── backend/                           # 后端微服务
│   ├── pom.xml                        # 父POM
│   ├── service-gateway/               # API网关
│   ├── service-auth/                  # 认证服务
│   ├── service-vehicle/               # 车辆管理
│   ├── service-ecu-log/               # ECU日志
│   ├── service-dbc/                   # DBC文件
│   ├── service-signal/                # 信号采集
│   ├── service-access/                # 数据接入
│   ├── nacos-configs/                 # Nacos配置文件
│   └── common/                        # 公共模块
├── frontend/                          # 前端应用
│   ├── src/
│   │   ├── api/                      # API接口
│   │   ├── views/                    # 页面组件
│   │   ├── router/                   # 路由配置
│   │   └── styles/                   # 样式
│   └── package.json
├── scripts/                           # 部署脚本
│   ├── build.sh                       # 构建并推送镜像
│   ├── start.sh                       # 启动服务
│   └── stop.sh                        # 停止服务
├── docs/                              # 文档
│   └── kafka-integration.md           # Kafka集成文档
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

### 3. 启动服务（从镜像仓库拉取并启动）

```bash
# 赋予脚本执行权限
chmod +x scripts/*.sh

# 一键启动所有服务
./scripts/start.sh
```

**start.sh 自动执行以下操作：**
- 检查 Docker 环境
- 创建数据目录 `/data/vrd/`
- 从镜像仓库拉取最新镜像
- 启动所有 Docker 容器

### 4. 停止服务

```bash
./scripts/stop.sh
```

### 5. 访问系统

- **前端地址**: http://localhost:3000
- **API网关**: http://localhost:8080
- **Nacos控制台**: http://localhost:8848/nacos (用户名: nacos, 密码: nacos)
- **ClickHouse**: http://localhost:8123
- **Kafka**: localhost:9092
- **MQTT**: localhost:1883

### 6. 查看服务状态

```bash
# 查看所有服务状态
docker-compose ps

# 查看服务日志
docker-compose logs -f service-gateway
docker-compose logs -f service-auth

# 查看特定服务日志
docker logs vrd-service-vehicle -f
```

### 7. 手动部署（可选）

如果需要分步操作：

```bash
# 1. 创建数据目录
mkdir -p /data/vrd/{uploads,logs,temp,dbc,signals,data}

# 2. 构建后端
cd backend
./mvnw clean package -DskipTests

# 3. 构建前端
cd ../frontend
npm install --legacy-peer-deps
npm run build

# 4. 构建 Docker 镜像
cd ..
docker-compose build

# 5. 启动服务（基础设施服务需提前启动）
docker-compose up -d service-gateway service-auth service-vehicle service-ecu-log service-dbc service-signal service-access
sleep 20
docker-compose up -d frontend
```

**注意**：基础设施服务（MySQL、Redis、Kafka、MQTT、ClickHouse、Nacos）需提前启动，脚本不再包含这些服务的启动。

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
    introspect-url: http://service-auth/auth/introspect
    ignore-paths:
      - /auth/login
      - /auth/register
      - /auth/validate
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
- `POST /dbc/{id}/dispatch/{vehicleId}` - 下发到车辆
- `GET /dbc/{id}/download` - 下载DBC文件

### 信号监控
- `GET /signal/timeline/{vehicleId}` - 时间轴查询
- `GET /signal/page/{vehicleId}` - 分页查询
- `GET /signal/signal-name/{vehicleId}` - 按信号名查询

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
1. **实时信号**: MQTT → service-access → Kafka → service-signal → BigDataStorage
2. **ECU日志**: HTTP → service-ecu-log → BigDataStorage
3. **车辆数据**: API → service-vehicle → MySQL → Kafka → service-access → BigDataStorage

**大数据存储**: 通过 `bigdata.type` 配置切换 ClickHouse/Doris/TDengine

### MQTT主题
- `vehicle/signal/+` - 车辆信号数据
- `vehicle/logs/+` - 车辆日志数据
- `vehicle/dtc/+` - 故障诊断码

### 认证流程
1. 用户登录 → service-auth 签发 JWT
2. 请求经过网关 → AuthFilter 验证 JWT (调用 introspect 接口)
3. 网关将 userId/roles 注入请求头 → 下游服务直接从请求头获取用户信息
4. 下游服务不再各自解析 JWT，只信任网关转发的身份

## 监控运维

### 服务监控
- Nacos Console: http://localhost:8848
- 查看服务注册状态
- 查看配置管理

### Kafka监控
- Kafka Manager: http://localhost:9000
- 监控主题
- 监控消费者组
- 监控消息延迟

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
- 确认Zookeeper启动
- 检查网络连接
- 验证端口配置

### 3. MQTT连接失败
- 确认Mosquitto启动
- 检查认证信息
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
