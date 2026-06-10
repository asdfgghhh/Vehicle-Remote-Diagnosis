# VRD 系统架构图

浏览器打开：**[architecture.html](./architecture.html)**

## 分层结构（自上而下）

| 序号 | 层次 | 组件 |
|------|------|------|
| ① | 展现层 | frontend（Vue3 管理端） |
| ② | 网关层 | service-gateway |
| ③ | 服务层 | auth · vehicle · ecu-log · dbc · signal · bigdata |
| ④ | 数据层 | MySQL · Redis · ClickHouse · 对象存储 · HDFS |
| ⑤ | 中间件 | Kafka · MQTT Broker |
| ⑥ | 接入层 | HTTP 日志上报 · MQTT 信号接入 |
| ⑦ | 车端 | ECU 控制器 · 车载网关 |

**注册中心**：Nacos 独立侧栏，虚线关联网关与各微服务，不在业务主链路中。

## 数据流向

- **管理端**：① → ② → ③ → ④（请求下行）
- **车端**：⑦ → ⑥ → ⑤ / ② → ③ → ④（数据上行）
