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

1. 在Nacos控制台修改配置后，点击「发布」

2. 使用了 `@RefreshScope` 注解的Bean会自动刷新配置

3. 对于没有使用 `@RefreshScope` 的配置，可以通过以下方式刷新：
   ```bash
   # 调用actuator端点刷新配置（需要引入spring-boot-starter-actuator）
   curl -X POST http://localhost:8080/actuator/refresh
   ```

## 注意事项

1. **配置优先级**：服务配置 > 公共配置 > 本地配置

2. **命名空间**：如需使用命名空间，需要在bootstrap.yml中配置 `spring.cloud.nacos.config.namespace`

3. **环境隔离**：通过不同的Group或Namespace实现开发、测试、生产环境的配置隔离

4. **敏感信息**：生产环境建议使用Nacos的加密配置功能或配置中心的密钥管理功能
