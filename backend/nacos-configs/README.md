# Nacos 配置中心使用说明

## 配置文件说明

本目录包含以下配置文件：
- `application.yml` - 公共配置，所有服务共享
- `service-gateway.yml` - 网关服务配置
- `service-auth.yml` - 认证服务配置
- `service-vehicle.yml` - 车辆管理服务配置
- `service-ecu-log.yml` - ECU日志服务配置
- `service-dbc.yml` - DBC文件服务配置
- `service-signal.yml` - 信号采集服务配置
- `service-bigdata.yml` - 大数据服务配置

## 配置导入方法

### 方法1：通过Nacos Web界面导入

1. 启动Nacos服务：
   ```bash
   docker-compose up -d nacos
   ```

2. 访问Nacos控制台：http://localhost:8848/nacos
   - 默认账号：nacos
   - 默认密码：nacos

3. 点击左侧菜单「配置管理」→「配置列表」

4. 点击「+」创建配置，依次导入以下配置：

   **公共配置：**
   - Data ID: `application.yml`
   - Group: `DEFAULT_GROUP`
   - 配置格式: `YAML`
   - 配置内容: 复制 `application.yml` 内容

   **各服务配置：**
   - Data ID: `service-gateway.yml` / `service-auth.yml` 等
   - Group: `DEFAULT_GROUP`
   - 配置格式: `YAML`
   - 配置内容: 复制对应服务配置文件内容

### 方法2：通过API导入（推荐）

使用curl命令批量导入配置：

```bash
#!/bin/bash

# Nacos地址
NACOS_URL="http://localhost:8848/nacos/v1/cs/configs"

# 导入公共配置
curl -X POST "$NACOS_URL" \
  -d "dataId=application.yml" \
  -d "group=DEFAULT_GROUP" \
  -d "content=$(cat application.yml)" \
  -d "type=yaml"

# 导入各服务配置
for file in service-*.yml; do
  dataId=$(basename "$file")
  curl -X POST "$NACOS_URL" \
    -d "dataId=$dataId" \
    -d "group=DEFAULT_GROUP" \
    -d "content=$(cat "$file")" \
    -d "type=yaml"
done

echo "配置导入完成！"
```

## 配置热更新

### 方式一：Nacos 控制台（推荐，修改后自动生效）

1. 在 Nacos 控制台修改 `application.yml` 或 `service-xxx.yml` 后点击「发布」
2. 各服务会自动收到配置变更并刷新（`refresh-enabled: true`）
3. 使用了 `@RefreshScope` 的配置 Bean 会立即生效，**无需重启服务**

适用配置示例：`storage.*`、`vrd.vehicle.kafka.*`、`bigdata.storage.*` 等。

### 方式二：手动触发刷新

若修改后未自动生效，可调用对应服务的 actuator 端点：

```bash
# 车辆服务（8082）
curl -X POST http://localhost:8082/actuator/refresh

# 网关（8080）
curl -X POST http://localhost:8080/actuator/refresh

# 认证服务（8081）
curl -X POST http://localhost:8081/actuator/refresh
```

### 本地开发说明

- `profile=dev` 时已启用 Nacos 配置中心（`bootstrap-dev.yml`）
- 数据库等仍可在各服务本地 `application.yml` / `application-dev.yml` 中配置
- Nacos 公共配置 `application.yml` 仅包含存储、上传等通用项，**不含数据库连接**，避免覆盖本地数据源
- **Kafka 消费主题、MQTT 等运行时连接**变更后，部分组件可能仍需重启才能完全生效

### 注意事项

1. **配置优先级**：Nacos 服务配置 > Nacos 公共配置 > 本地 application.yml

2. **命名空间**：如需使用命名空间，需要在bootstrap.yml中配置 `spring.cloud.nacos.config.namespace`

3. **环境隔离**：通过不同的Group或Namespace实现开发、测试、生产环境的配置隔离

4. **敏感信息**：生产环境建议使用Nacos的加密配置功能或配置中心的密钥管理功能
