# Kafka Integration Configuration for Vehicle Remote Diagnosis System

> 本文档基于当前代码实际使用的 Kafka 主题整理（核实自各服务源码与 Nacos 配置）。
> Kafka 3.7 部署于服务器 124.221.104.56:9092（宿主机，非 docker-compose 内）。

## Kafka Topics

### 1. Vehicle Data Topic
Topic: `vehicle-data`
- Purpose: 车辆主数据变更同步（新增/修改/删除）
- Producer/Consumer: service-vehicle（`VehicleKafkaConsumer`，group `vehicle-processor`，topic 可在 `vrd.vehicle.kafka.consumer-topic` / `producer-topic` 配置）
- Partitions: 6
- Replication Factor: 1
- Retention: 7 days

Message Format:
```json
{
  "action": "CREATE|UPDATE|DELETE",
  "data": {
    "vin": "LSVAG4189ES123456",
    "modelId": 1,
    "plateNumber": "京A12345",
    "color": "黑色",
    "productionYear": 2023,
    "engineNumber": "ENG123456",
    "bodyNumber": "BDY789012",
    "currentEcuVersion": "V2.1.0"
  },
  "timestamp": 1704067200000
}
```

### 2. Signal Data Topic
Topic: `vehicle-signals`
- Purpose: 实时信号数据（车辆信号采集与告警规则评估）
- Producer: service-access（`KafkaMessageProducer`，由 MQTT `vehicle/signal/+` 接入后转发）
- Consumer: service-signal（ClickHouse 存储）、service-vehicle `AlertEvaluationConsumer`（group `vehicle-alert-evaluator`，规则引擎评估）
- Partitions: 10
- Replication Factor: 1
- Retention: 3 days

Message Format:
```json
{
  "vin": "LSVAG4189ES123456",
  "vehicleId": 1,
  "signals": [
    {
      "name": "VehicleSpeed",
      "value": "65.5",
      "unit": "km/h",
      "timestamp": 1704067200000,
      "messageName": "VehicleData",
      "messageId": 1024
    }
  ]
}
```

### 3. UDS Commands Topic
Topic: `uds-commands`
- Purpose: UDS 远程诊断指令下发（会话控制、读写 DID、DTC 操作等）
- Producer: service-diagnosis（`UdsCommandProducer`，topic 可在 `kafka.topics.uds-commands` 配置）
- Consumer: 车辆端诊断网关（车端订阅执行）
- Partitions: 3
- Replication Factor: 1
- Retention: 7 days

Message Format:
```json
{
  "traceId": "8f14e45fceea167a5a36dedd4bea2543",
  "vin": "LSVAG4189ES123456",
  "serviceId": "0x22",
  "subFunction": null,
  "sessionType": "EXTENDED",
  "securityLevel": "NONE",
  "ecuType": "VCU",
  "requestData": "F1 90"
}
```

### 4. UDS Responses Topic
Topic: `uds-responses`
- Purpose: 车端 UDS 诊断响应回传（topic 已在 `service-diagnosis.yml` 配置为 `kafka.topics.uds-responses`）
- Consumer: **待建设** — service-diagnosis 尚未实现 `uds-responses` 消费者，响应落库（`uds_diagnosis_session`）功能暂未打通
- Partitions: 3
- Replication Factor: 1
- Retention: 7 days

Message Format:
```json
{
  "traceId": "8f14e45fceea167a5a36dedd4bea2543",
  "vin": "LSVAG4189ES123456",
  "serviceId": "0x22",
  "success": true,
  "nrc": null,
  "responseData": "62 F1 90 01 02 03"
}
```

## Kafka Producer Configuration

### Service-Vehicle Producer（vehicle-data）
```yaml
spring:
  kafka:
    bootstrap-servers: ${KAFKA_SERVERS:124.221.104.56:9092}
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
      acks: all
      retries: 3
      properties:
        enable.idempotence: true
        max.in.flight.requests.per.connection: 5
```

### Service-Access Producer（vehicle-signals）
```yaml
spring:
  kafka:
    bootstrap-servers: ${KAFKA_SERVERS:124.221.104.56:9092}
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
```

### Service-Vehicle Consumer（AlertEvaluationConsumer，vehicle-signals）
```yaml
spring:
  kafka:
    bootstrap-servers: ${KAFKA_SERVERS:124.221.104.56:9092}
    consumer:
      group-id: vehicle-alert-evaluator
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
```

### Service-Diagnosis Producer（uds-commands）
```yaml
spring:
  kafka:
    bootstrap-servers: ${KAFKA_SERVERS:124.221.104.56:9092}
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer

kafka:
  topics:
    uds-commands: uds-commands
    uds-responses: uds-responses
```

## Data Flow

1. **Vehicle → MQTT → service-access → Kafka vehicle-signals → service-signal / service-vehicle**
   - 车端信号经 MQTT 主题 `vehicle/signal/+` 接入 service-access
   - service-access 转发到 Kafka `vehicle-signals`
   - service-signal 消费并写入 ClickHouse（时序存储）
   - service-vehicle `AlertEvaluationConsumer` 消费并提交规则引擎评估告警

2. **Vehicle data changes → service-vehicle → Kafka vehicle-data**
   - 车辆主数据变更（增删改）发布到 `vehicle-data`
   - service-vehicle 自身消费者（`VehicleKafkaConsumer`）同步处理，外部系统亦可订阅

3. **service-diagnosis → Kafka uds-commands → 车端诊断网关**
   - 远程诊断指令下发到 `uds-commands`
   - 车端执行后经 `uds-responses` 回传（消费者待建设）

## Monitoring

### 现状说明
- 未部署 Kafka Manager / Kafka UI 等 Web 管理界面
- 推荐使用 Kafka 自带 CLI 工具在服务器（124.221.104.56）上监控：

```bash
# 查看消费者组与 lag
kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --all-groups

# 查看主题列表
kafka-topics.sh --bootstrap-server localhost:9092 --list

# 查看主题详情（分区/副本）
kafka-topics.sh --bootstrap-server localhost:9092 --describe --topic vehicle-signals
```

### Metrics
- Messages per second
- Consumer lag（重点关注 vehicle-alert-evaluator、vehicle-processor 两个消费组）
- Producer error rate
- Disk usage per topic

## Performance Tuning

### Producer Settings
- `batch.size`: 16384 (16KB)
- `linger.ms`: 5
- `buffer.memory`: 33554432 (32MB)
- `compression.type`: snappy

### Consumer Settings
- `fetch.min.bytes`: 1
- `fetch.max.wait.ms`: 500
- `max.poll.records`: 500
- `max.partition.fetch.bytes`: 1048576 (1MB)
