# Apache Doris 大数据查询服务

## 概述

车辆远程诊断系统 - Doris OLAP查询引擎，提供海量数据的快速查询和分析能力。

## 架构

```
Kafka (实时数据流
    │
    ├─► service-bigdata (消费
    │    │
    │    ├─► HDFS (原始数据存储
    │    │
    │    └─► Doris (OLAP查询引擎 ← 本项目
    │
    └─► 查询 API (查询结果)
```

## 快速开始

### 前置条件

- Docker 20.10+
- Docker Compose 2.0+
- 足够的磁盘空间（建议至少 10GB+

### 启动Doris

#### 1. 启动Doris服务

```bash
# 在项目根目录下执行
./scripts/start-doris.sh
```

#### 2. 等待Doris启动完成
需要等待1-2分钟，查看日志：
```bash
# 查看Doris FE日志
docker logs -f vrd-doris-fe
```

#### 3. 初始化数据库
等待FE完全启动后（看到 "start to work" 日志），执行：
```bash
./scripts/init-doris-db.sh
```

#### 4. 访问Web控制台
打开浏览器访问：
http://localhost:8030

### 停止Doris

```bash
./scripts/stop-doris.sh
```

## Doris服务访问

| 服务 | 地址 | 说明 |
|------|------|------|
| FE Web | http://localhost:8030 | Web控制台 |
| FE MySQL | localhost:9030 | MySQL协议端口 |
| BE Web | http://localhost:8040 | BE Web管理 |
| BE Heartbeat | localhost:9050 | BE心跳端口 |
| BE RPC | localhost:9060 | BE RPC端口 |

### 数据库连接

```bash
# 使用MySQL客户端连接
mysql -h localhost -P 9030 -u root
```

数据库连接信息：
- 数据库: vrd_bigdata
- 用户名: root
- 密码: (空)

## 数据表说明

### vehicle_signals - 车辆信号表

存储车辆实时信号数据，包含：
- 信号名称、值、单位
- 位置信息
- 时间戳
- 按日期分区
- 车辆ID分桶

### ecu_logs - ECU日志表

存储ECU故障和诊断日志，包含：
- 故障码、错误级别
- 错误描述和日志内容
- 位置和状态信息

### diagnostics - 诊断数据表

存储车辆健康诊断报告，包含：
- 健康评分
- 故障和警告统计
- 诊断详情

## 查询接口

### API端点

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /bigdata/signals | 查询车辆信号 |
| GET | /bigdata/logs | 查询ECU日志 |
| GET | /bigdata/diagnostics | 查询诊断数据 |
| GET | /bigdata/aggregate | 聚合统计 |
| GET | /bigdata/trend | 信号趋势 |
| GET | /bigdata/health-report | 健康报告 |
| GET | /bigdata/top-errors | 故障码排行 |
| GET | /bigdata/dates | 可用数据日期 |
| GET | /bigdata/statistics | 数据统计 |

### 示例查询

```bash
# 查询车辆信号
curl "http://localhost:8086/bigdata/signals?vehicle_id=1&start_time=2026-01-01&end_time=2026-12-31"

# 车辆健康报告
curl "http://localhost:8086/bigdata/health-report?vehicle_id=1&start_time=2026-01-01&end_time=2026-12-31"

# 故障码排行
curl "http://localhost:8086/bigdata/top-errors?start_time=2026-01-01&end_time=2026-12-31"
```

## SQL查询示例

```sql
-- 信号数据统计
SELECT 
    signal_name,
    COUNT(*) AS count,
    AVG(CAST(signal_value AS DOUBLE)) AS avg_value
FROM vrd_bigdata.vehicle_signals
WHERE dt = '2026-06-01'
GROUP BY signal_name;

-- 健康评分趋势
SELECT 
    dt,
    AVG(overall_health_score) AS avg_score,
    COUNT(*) AS diagnostic_count
FROM vrd_bigdata.diagnostics
WHERE dt BETWEEN '2026-05-01' AND '2026-06-01'
GROUP BY dt
ORDER BY dt;
```

## 配置说明

### Doris配置

```yaml
# service-bigdata的配置
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:9030/vrd_bigdata
    username: root
    password: ''
```

### Kafka同步配置

```yaml
bigdata:
  sync:
    doris:
      enabled: true  # true=启用Kafka到Doris的实时同步
```

## 常见问题

### FE无法启动
检查日志：
```bash
docker logs vrd-doris-fe
docker logs vrd-doris-be
```

### BE无法加入集群
确保BE容器能访问FE：
```bash
docker exec vrd-doris-be ping doris-fe
```

### 查询超时
增加查询超时时间：
```yaml
doris:
  query:
    timeout: 120
```

### 分区管理
定期清理旧分区：
```sql
ALTER TABLE vrd_bigdata.vehicle_signals
DROP PARTITION p_2024_01;
```

## 性能优化建议

1. 分区裁剪：总是在WHERE条件中包含dt字段
2. 物化视图：预聚合统计查询
3. 数据保留策略：定期清理过期数据
4. 分桶优化：调整BUCKETS数量适配数据规模
