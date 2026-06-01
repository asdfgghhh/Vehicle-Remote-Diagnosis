# Apache Doris 大数据查询方案

## 概述

本项目采用 **Apache Doris** 作为OLAP查询引擎，配合 **Kafka** 实现实时数据同步，提供高速的大数据查询能力。

## 架构设计

```
┌─────────────┐
│   Kafka     │
│  数据流     │
└──────┬──────┘
       │
       ▼
┌─────────────────────────────────┐
│       service-bigdata           │
│  ┌───────────────────────────┐  │
│  │   Kafka Data Consumer     │  │
│  └───────────┬───────────────┘  │
│              │                  │
│      ┌───────┴───────┐          │
│      ▼               ▼          │
│  ┌────────┐    ┌─────────┐      │
│  │  HDFS  │    │  Doris  │      │
│  │ (原始) │    │ (查询)  │      │
│  └────────┘    └─────────┘      │
└─────────────────────────────────┘
       │
       ▼
┌─────────────────────────────────┐
│       DorisQueryService         │
│  ┌───────────────────────────┐  │
│  │ 毫秒级查询 API            │  │
│  │ - 车辆信号查询            │  │
│  │ - ECU日志查询             │  │
│  │ - 健康报告生成            │  │
│  └───────────────────────────┘  │
└─────────────────────────────────┘
```

## 核心特性

### 1. 实时数据同步
- Kafka消费者自动将数据同步到Doris
- 支持信号数据、日志数据、诊断数据
- 可通过配置开关控制同步功能

### 2. 高速查询
- 百亿级数据秒级响应
- 支持多维度聚合查询
- 内置物化视图加速统计

### 3. 分区管理
- 按日期分区存储
- 支持动态分区管理
- 冷热数据分离

## 快速启动

### 1. 启动Doris服务

```bash
# 启动Doris服务
cd /workspace
docker-compose -f docker-compose-doris.yml up -d

# 查看日志
docker-compose -f docker-compose-doris.yml logs -f
```

### 2. 初始化Doris表结构

```bash
# 等待初始化完成
docker-compose -f docker-compose-doris.yml logs -f doris-init

# 手动初始化（如需要）
docker exec -it vrd-doris-fe mysql -h localhost -P 9030 -u root < doris/sql/01_create_database.sql
```

### 3. 验证Doris状态

```bash
# 连接Doris
docker exec -it vrd-doris-fe mysql -h localhost -P 9030 -u root

# 查看数据库
mysql> SHOW DATABASES;
mysql> USE vrd_bigdata;
mysql> SHOW TABLES;
```

### 4. 启动大数据服务

```bash
cd /workspace/backend
mvn clean package -DskipTests
cd /workspace
docker-compose up -d service-bigdata
```

## API接口

### 1. 车辆信号查询
```bash
GET /bigdata/signals?vehicleId=1&startTime=2026-05-01&endTime=2026-05-30&signalName=battery_voltage
```

### 2. ECU日志查询
```bash
GET /bigdata/logs?vehicleId=1&startTime=2026-05-01&endTime=2026-05-30&ecuType=ECM
```

### 3. 诊断数据查询
```bash
GET /bigdata/diagnostics?vehicleId=1&startTime=2026-05-01&endTime=2026-05-30
```

### 4. 信号趋势分析
```bash
GET /bigdata/trend?vehicleId=1&signalName=battery_voltage&startTime=2026-05-01&endTime=2026-05-30
```

### 5. 车辆健康报告
```bash
GET /bigdata/health-report?vehicleId=1&startTime=2026-05-01&endTime=2026-05-30
```

### 6. Top错误码统计
```bash
GET /bigdata/top-errors?startTime=2026-05-01&endTime=2026-05-30&limit=10
```

### 7. 聚合统计
```bash
GET /bigdata/aggregate?vehicleId=1&signalName=battery_voltage&startTime=2026-05-01&endTime=2026-05-30
```

### 8. 可用数据日期
```bash
GET /bigdata/dates?dataType=signals
```

## 性能优化

### 1. 查询优化
```sql
-- 利用分区裁剪
SELECT * FROM vehicle_signals 
WHERE dt = '2026-05-15'  -- 只扫描该分区

-- 创建物化视图
CREATE MATERIALIZED VIEW mv_hourly AS
SELECT vehicle_id, dt, dt_hour, AVG(signal_value)
FROM vehicle_signals
GROUP BY vehicle_id, dt, dt_hour;
```

### 2. 数据导入优化
```sql
-- 批量导入
INSERT INTO vehicle_signals VALUES (...);

-- Stream Load
curl --location-trusted -u root: -T data.json -H "format: json" \
  http://doris-fe:8030/api/vrd_bigdata/vehicle_signals/_stream_load
```

## 配置说明

### Doris连接配置
```yaml
spring:
  datasource:
    url: jdbc:mysql://doris-fe:9030/vrd_bigdata
    username: root
    password: 
```

### Kafka同步开关
```yaml
bigdata:
  sync:
    doris:
      enabled: true  # true-启用 false-禁用
```

## 监控与运维

### Doris Web控制台
- 地址: http://localhost:8030
- 查看FE/BE状态
- 查看查询日志

### 常用运维命令
```bash
# 查看FE状态
mysql> SHOW FRONTENDS;

# 查看BE状态
mysql> SHOW BACKENDS;

# 查看tablet分布
mysql> SHOW TABLETS FROM vrd_bigdata.vehicle_signals;

# 查看正在执行的查询
mysql> SHOW PROC '/frontends/127.0.0.1_9030/running_queries';
```

## 注意事项

1. **首次启动**: Doris首次启动需要等待BE节点添加到集群并完成数据迁移
2. **资源配置**: 生产环境建议FE配置4核8G，BE配置8核16G
3. **数据备份**: 定期备份Doris的doris_fe_data和doris_be_data卷
4. **监控告警**: 建议配置FE/BE的CPU、内存、磁盘监控

## 故障排除

### BE节点未添加
```bash
mysql -h doris-fe -P 9030 -u root -e "ALTER SYSTEM ADD BACKEND 'doris-be:9050';"
```

### 查询超时
调整 `doris.query.timeout` 配置或SQL中增加 `timeout` 设置

### 数据不同步
1. 检查Kafka消费者日志
2. 确认 `bigdata.sync.doris.enabled=true`
3. 验证Doris连接配置正确
