# VRD 系统架构图

## 在线查看

浏览器打开：**[architecture.html](./architecture.html)**

## 架构图文件

| 文件 | 格式 | 说明 |
|------|------|------|
| [architecture.html](./architecture.html) | HTML | 可交互分层架构图（推荐） |
| [images/system-architecture.png](./images/system-architecture.png) | PNG | 系统总体架构图 |
| [images/data-flow-architecture.png](./images/data-flow-architecture.png) | PNG | 数据流架构图 |
| [images/deployment-architecture.png](./images/deployment-architecture.png) | PNG | 部署架构图 |

重新生成 PNG：`python scripts/generate_arch_diagrams.py`

## 分层结构（自上而下）

| 序号 | 层次 | 组件 |
|------|------|------|
| ① | 展现层 | frontend（Vue3 管理端 :3000） |
| ② | 网关层 | service-gateway（:8080，路由 + JWT） |
| ③ | 服务层 | auth · vehicle · ecu-log · dbc · signal · **access** |
| ④ | 数据层 | MySQL · Redis · ClickHouse · 对象存储 |
| ⑤ | 中间件 | Kafka · MQTT Broker |
| ⑥ | 接入层 | service-access（HTTP 日志/信号 + MQTT 信号） |
| ⑦ | 车端 | ECU 控制器 · 车载网关 |

**注册中心**：Nacos（:8848）独立侧栏，虚线关联网关与各微服务。

## 数据流向

- **管理端下行**：① frontend → ② gateway → ③ 微服务 → ④ 持久化存储
- **车端上行**：⑦ 车端 → ⑥ service-access → ⑤ Kafka/MQTT → ③ 微服务 → ④ ClickHouse / 对象存储

## 核心业务链路

| 业务 | 写入路径 | 查询路径 |
|------|----------|----------|
| 车辆主数据 | Kafka vehicle-data → service-vehicle → MySQL | service-vehicle |
| 实时信号 | MQTT/HTTP → service-access → Kafka → ClickHouse | service-signal |
| ECU 日志 | HTTP 分片/直传 → service-access → 存储 + ClickHouse | service-ecu-log |
| DBC 配置 | 管理端上传 → service-dbc → MySQL + 文件存储 | service-dbc |
| 认证授权 | service-auth → MySQL + JWT | service-auth |
