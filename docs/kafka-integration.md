# Kafka Integration Configuration for Vehicle Remote Diagnosis System

## Kafka Topics

### 1. Vehicle Data Topic
Topic: `vehicle-data`
- Purpose: Synchronize vehicle data between systems
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

### 2. ECU Log Topic
Topic: `ecu-logs`
- Purpose: Receive ECU log files from vehicles
- Partitions: 3
- Replication Factor: 1
- Retention: 30 days

Message Format:
```json
{
  "vin": "LSVAG4189ES123456",
  "ecuType": "VCU",
  "logType": "ERROR|WARN|INFO|DEBUG",
  "logContent": "Base64 encoded log content",
  "fileSize": 1024000,
  "md5": "d41d8cd98f00b204e9800998ecf8427e",
  "timestamp": 1704067200000
}
```

### 3. Signal Data Topic
Topic: `vehicle-signals`
- Purpose: Real-time signal data from vehicles
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

### 4. Diagnostics Topic
Topic: `diagnostics`
- Purpose: Diagnostic trouble codes and fault data
- Partitions: 3
- Replication Factor: 1
- Retention: 14 days

Message Format:
```json
{
  "vin": "LSVAG4189ES123456",
  "dtcCodes": [
    {
      "code": "P0301",
      "description": "Cylinder 1 Misfire Detected",
      "severity": "HIGH",
      "timestamp": 1704067200000
    }
  ],
  "diagnosisResults": [],
  "timestamp": 1704067200000
}
```

## Kafka Producer Configuration

### Service-Vehicle Producer
```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
      acks: all
      retries: 3
      properties:
        enable.idempotence: true
        max.in.flight.requests.per.connection: 5
```

### Service-Bigdata Consumer
```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: bigdata-group
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      enable-auto-commit: false
      properties:
        session.timeout.ms: 30000
        heartbeat.interval.ms: 10000
```

## Data Flow

1. **Vehicle → Kafka → Service-Vehicle**
   - Vehicle data changes trigger Kafka messages
   - Service-Vehicle processes and stores in MySQL

2. **Service-Vehicle → Kafka → Service-Bigdata**
   - Processed data is forwarded to big data cluster
   - Service-Bigdata stores in HDFS for long-term storage

3. **Vehicle → Kafka → Service-Bigdata**
   - Real-time signals and logs go directly to big data
   - Low-latency processing for critical data

## Monitoring

### Kafka Manager
- URL: http://localhost:9000
- Purpose: Monitor topic health, consumer groups, message lag

### Metrics
- Messages per second
- Consumer lag
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
