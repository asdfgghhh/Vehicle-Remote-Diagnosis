# 车辆远程诊断系统 - 架构优化实施记录 (Phase II)

> 实施日期：2026-08-10  
> 实施内容：Java 原生 DBC 解析器替换 + CI/CD 自动化流水线

> **📌 文档修订说明（2026-08-17）**
> - `.github/workflows/ci.yml`、`.github/workflows/deploy.yml` 仅存在于**服务器端 GitHub 仓库**（由远端触发 Actions 流水线），本地工作区不含 `.github/` 目录；本地保留的 CI/CD 相关产物为 `backend/Dockerfile`、`frontend/Dockerfile`、`frontend/nginx.conf`、`backend/.dockerignore`、`deploy.sh`；
> - 第三章 docker-compose 变更中 **nacos 不在 docker-compose 内**（Nacos 与 MySQL/Redis/Kafka/EMQX/ClickHouse 一样部署于服务器宿主机），实际 compose 仅编排 8 个微服务 + frontend + vrd-network。

---

## 一、Java 原生 DBC 解析器 (替代 Python cantools)

### 1.1 架构变更

```
优化前:
  service-dbc → CantoolsClient (HTTP) → dbc-parser-service (Python FastAPI + cantools)
  需单独部署 Python 服务，链路长，部署复杂

优化后:
  service-dbc → DbcParserService (纯 Java 内存调用) → DbcParser + CanFrameCodec
  零外部依赖，纯 Java 实现，无需额外部署
```

### 1.2 新增核心文件

| 文件 | 说明 |
|---|------|
| `service-dbc/.../parser/DbcParser.java` | 纯 Java DBC 文件解析引擎，完整支持 BO_、SG_、CM_、VAL_、BA_、SG_MUL_VAL_ 等 DBC 规范关键字 |
| `service-dbc/.../parser/CanFrameCodec.java` | CAN 帧编解码器，精确位运算实现 Intel (Little Endian) 和 Motorola (Big Endian) 两种字节序的信号提取和写入 |
| `service-dbc/.../parser/DbcDatabase.java` | DBC 数据库模型，维护报文/信号/节点/值表的完整索引（按 ID 和名称双向索引） |
| `service-dbc/.../parser/DbcMessage.java` | CAN 报文模型，含 CAN ID、DLC、信号列表、发送节点、周期属性 |
| `service-dbc/.../parser/DbcSignal.java` | CAN 信号模型，完整定义：起始位、长度、字节序、有符号、缩放因子、偏移量、范围、单位、值表 |
| `service-dbc/.../parser/DbcNode.java` | 网络节点模型 |
| `service-dbc/.../parser/DbcValueTable.java` | 值表模型 (信号枚举值映射) |
| `service-dbc/.../service/DbcParserService.java` | DBC 解析服务包装器，含内存缓存、代码生成（Java常量类、JSON Schema）、健康检查 |

### 1.3 修改文件

| 文件 | 修改内容 |
|---|------|
| `service-dbc/.../impl/DbcFileServiceImpl.java` | **重大重构**：移除 CantoolsClient 依赖，改为注入 DbcParserService；新增 `parseWithNativeParser()` 方法；新增 CAN 帧编解码、结构化数据查询、代码生成等方法 |
| `service-dbc/.../controller/DbcFileController.java` | 新增 7 个 API 端点：结构化数据查询、报文详情、信号详情（结构化）、CAN帧实时解码、CAN帧编码测试、Java常量生成、JSON Schema生成、缓存清除 |

### 1.4 删除文件

| 文件 | 说明 |
|---|------|
| `service-dbc/.../service/CantoolsClient.java` | 已删除，不再需要 Python 服务 HTTP 客户端 |
| `dbc-parser-service/` | Python 微服务目录，不再需要（保留备份） |

### 1.5 技术特性

- **零外部依赖**：纯 Java 17 实现，无需 Python/cantools
- **完整 DBC 规范支持**：消息、信号、多路复用、值表、注释、属性周期
- **高精度位运算**：Intel/Motorola 两种字节序精确编解码
- **有符号扩展处理**：支持 64 位及以下任意位宽信号
- **内存缓存**：ConcurrentHashMap 缓存，避免重复解析
- **代码生成**：支持自动生成 Java 信号常量类和 JSON Schema
- **降级策略**：解析异常时自动降级为正则解析器

---

## 二、CI/CD 自动化流水线

### 2.1 新增文件

| 文件 | 说明 |
|---|------|
| `.github/workflows/ci.yml` | CI 流水线：代码检出 → JDK 17 构建 → Maven 测试 → 前端 npm 构建 → Lint → 制品上传（**仅服务器端仓库**，本地不含 `.github/`） |
| `.github/workflows/deploy.yml` | CD 流水线：Docker 镜像并行构建 → 推送镜像仓库 → SSH 远程部署 → 健康检查（**仅服务器端仓库**） |
| `backend/Dockerfile` | **统一多阶段 Dockerfile**：Stage1 Maven 构建（缓存优化）→ Stage2 JRE 运行时（安全加固、健康检查、JVM 调优） |
| `frontend/Dockerfile` | 前端多阶段构建：Node.js 构建 → Nginx 运行时（含 Gzip、缓存、安全头） |
| `frontend/nginx.conf` | Nginx 配置：API 反向代理、WebSocket 代理、SPA fallback、安全头 |
| `backend/.dockerignore` | Docker 构建排除规则 |
| `deploy.sh` | 本地一键构建部署脚本 |

### 2.2 CI 流水线流程

```
Push/PR → [Backend: Maven Build + Test] → [Frontend: npm Build + Lint]
              ↘                          ↙
                  Upload Artifacts
                  Status Report
```

### 2.3 CD 流水线流程

```
Push to main → [Parallel: Build 8 service images + Frontend image]
                      ↓
                  Docker Buildx (BuildKit cache)
                      ↓
                  Push to Registry (124.221.104.56:8211)
                      ↓
                  SSH to Deploy Server
                      ↓
                  docker compose pull + up -d (滚动更新)
                      ↓
                  Health Check (curl gateway)
                      ↓
                  Image Cleanup (prune old images)
```

### 2.4 多阶段 Dockerfile 特性

| 特性 | 说明 |
|---|------|
| **构建缓存** | 先复制 pom.xml，利用 Docker 层缓存加速依赖下载 |
| **非 root 运行** | 创建 vrd 用户，以非特权用户运行应用 |
| **JVM 调优** | G1GC、MaxGCPauseMillis=200ms、HeapDumpOnOOM |
| **健康检查** | HEALTHCHECK curl /actuator/health 自动监控 |
| **安全头** | X-Frame-Options、X-Content-Type-Options、X-XSS-Protection |
| **镜像清理** | 自动清理 72h 前的旧镜像 |

### 2.5 所需 GitHub Secrets

| Secret | 说明 |
|---|------|
| `REGISTRY_USERNAME` | 镜像仓库用户名 |
| `REGISTRY_PASSWORD` | 镜像仓库密码 |
| `DEPLOY_SSH_KEY` | 部署服务器 SSH 私钥 |

### 2.6 所需 GitHub Variables

| Variable | 说明 |
|---|------|
| `DEPLOY_HOST` | 部署服务器地址 |
| `DEPLOY_USER` | 部署服务器用户 |
| `DEPLOY_PATH` | 部署目录（默认 /opt/vrd） |

---

## 三、docker-compose 变更

### 3.1 新增服务

| 服务 | 端口 | 说明 |
|---|------|------|
| `service-diagnosis` | 9087 | UDS 诊断服务 |

> **修订**：`nacos`（8848）不在 docker-compose 中——Nacos 与 MySQL/Redis/Kafka/EMQX/ClickHouse 均部署于服务器宿主机（124.221.104.56），compose 仅编排 8 个微服务 + frontend + vrd-network。

### 3.2 移除服务

| 服务 | 说明 |
|---|------|
| `dbc-parser-service` | Python cantools 微服务，已由 Java 原生解析器替代 |

### 3.3 优化项

- 所有镜像标签支持 `${TAG}` 变量（默认 latest）
- 镜像仓库地址支持 `${REGISTRY}` 变量
- 添加 `healthcheck` 健康检查配置
- 添加 `depends_on` service_started 条件依赖

---

## 四、架构对比

```
Phase I (上次优化):
  ┌──────────────┐    HTTP     ┌──────────────────┐
  │ service-dbc  │ ─────────→  │ dbc-parser-svc   │
  │ CantoolsClient│            │ (Python cantools) │
  └──────────────┘             └──────────────────┘
  外部依赖 Python 服务

Phase II (本次优化):
  ┌──────────────────────────────┐
  │ service-dbc                  │
  │ ├── DbcParserService         │
  │ ├── DbcParser (解析引擎)     │
  │ ├── CanFrameCodec (编解码)   │
  │ └── DbcDatabase (数据模型)   │
  └──────────────────────────────┘
  纯 Java 原生，零外部依赖
```

---

## 五、部署说明

### 5.1 CI/CD 自动化（推荐）
向 main 分支推送代码即自动触发构建和部署。

### 5.2 手动部署
```bash
# 本地构建并推送镜像
./deploy.sh v1.2.0

# 服务器上部署
export TAG=v1.2.0 REGISTRY=124.221.104.56:8211
docker compose pull
docker compose up -d --remove-orphans
```

### 5.3 验证
```bash
# 网关健康检查
curl http://<host>:9080/actuator/health

# DBC 解析测试
curl http://<host>:9084/dbc/{id}/structured

# CAN 帧解码测试
curl -X POST "http://<host>:9084/dbc/{id}/decode?messageId=0x123&dataHex=AABBCCDDEEFF0011"
```
