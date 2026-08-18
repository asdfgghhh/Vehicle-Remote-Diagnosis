# Nacos 配置中心使用说明

## 配置文件说明

本目录包含以下配置文件：

- `application.yml` - 公共配置，所有服务共享（存储/上传等通用项，不含数据库连接）
- `storage.yml` - 存储相关配置（Redis/ClickHouse/Doris/TDengine 等连接地址）
- `service-gateway.yml` - 网关服务配置（:9080，路由 + JWT 认证白名单）
- `service-auth.yml` - 认证服务配置（:9081）
- `service-vehicle.yml` - 车辆管理服务配置（:9082，告警规则引擎）
- `service-ecu-log.yml` - ECU 日志服务配置（:9083）
- `service-dbc.yml` - DBC 文件服务配置（:9084，纯 Java 解析器）
- `service-signal.yml` - 信号采集服务配置（:9085，ClickHouse 存储）
- `service-access.yml` - MQTT 接入服务配置（:9086，MQTT + WebSocket）
- `service-diagnosis.yml` - UDS 诊断服务配置（:9087，Kafka 指令下发）
- `import-configs.sh` - 配置批量导入脚本（见下文「方法2」）

> 注：`application.yml` 与 `service-*.yml` 中的中间件地址默认指向服务器 `124.221.104.56`（MySQL/Redis/Kafka/EMQX/Nacos/ClickHouse 均部署于宿主机），本地开发可通过环境变量（如 `DB_HOST`、`KAFKA_SERVERS`、`NACOS_ADDR`）覆盖。

## 配置导入方法

### 方法1：通过 Nacos Web 界面导入

1. 访问 Nacos 控制台：http://124.221.104.56:8848/nacos
   - 默认账号：nacos
   - 默认密码：nacos
   - Nacos 部署于服务器宿主机（非 docker-compose 内）

2. 如需导入到指定命名空间（生产配置建议使用命名空间 `57f964ac-059c-4e2b-8138-47bba2b9afb0`），在控制台右上角切换到对应命名空间

3. 点击左侧菜单「配置管理」→「配置列表」

4. 点击「+」创建配置，依次导入以下配置：

   **公共配置：**
   - Data ID: `application.yml` / `storage.yml`
   - Group: `DEFAULT_GROUP`
   - 配置格式: `YAML`
   - 配置内容: 复制对应文件内容

   **各服务配置：**
   - Data ID: `service-gateway.yml` / `service-auth.yml` / `service-vehicle.yml` / `service-ecu-log.yml` / `service-dbc.yml` / `service-signal.yml` / `service-access.yml` / `service-diagnosis.yml`
   - Group: `DEFAULT_GROUP`
   - 配置格式: `YAML`
   - 配置内容: 复制对应服务配置文件内容

### 方法2：通过导入脚本（推荐）

本目录已提供 `import-configs.sh` 批量导入脚本（自动登录获取 token、依次导入 `application.yml` + 全部 `service-*.yml`）：

```bash
# 导入到服务器 Nacos（默认命名空间）
./import-configs.sh -a 124.221.104.56:8848

# 导入到指定命名空间
./import-configs.sh -a 124.221.104.56:8848 -n 57f964ac-059c-4e2b-8138-47bba2b9afb0

# 指定账号密码
./import-configs.sh -a 124.221.104.56:8848 -u nacos -p your-password
```

或使用 curl 手工导入：

```bash
NACOS_URL="http://124.221.104.56:8848/nacos/v1/cs/configs"

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

适用配置示例：`storage.*`、`vrd.vehicle.kafka.*` 等。

### 方式二：手动触发刷新

若修改后未自动生效，可调用对应服务的 actuator 端点：

```bash
# 车辆服务（9082）
curl -X POST http://localhost:9082/actuator/refresh

# 网关（9080）
curl -X POST http://localhost:9080/actuator/refresh

# 认证服务（9081）
curl -X POST http://localhost:9081/actuator/refresh
```

### 本地开发说明

- `profile=dev` 时已启用 Nacos 配置中心（`bootstrap-dev.yml`）
- 数据库等仍可在各服务本地 `application.yml` / `application-dev.yml` 中配置
- Nacos 公共配置 `application.yml` 仅包含存储、上传等通用项，**不含数据库连接**，避免覆盖本地数据源
- **Kafka 消费主题、MQTT 等运行时连接**变更后，部分组件可能仍需重启才能完全生效

### 注意事项

1. **配置优先级**：Nacos 服务配置 > Nacos 公共配置 > 本地 application.yml

2. **命名空间**：如需使用命名空间，需要在 bootstrap.yml 中配置 `spring.cloud.nacos.config.namespace`

3. **环境隔离**：通过不同的 Group 或 Namespace 实现开发、测试、生产环境的配置隔离

4. **敏感信息**：生产环境建议使用 Nacos 的加密配置功能或配置中心的密钥管理功能
