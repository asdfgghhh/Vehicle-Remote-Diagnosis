# 车辆远程诊断平台 · 详细设计说明书（VHR 对齐版）

> **文档版本**：v2.0（VHR 对齐）
> **编写日期**：2026-08-18
> **设计依据**：`vrd-ui-design.html`（16 页面 UI 原型）+ 现有后端代码（14 Controller / 97 HTTP 接口 / 1 WebSocket Handler）+ 现有数据库脚本（22 张 MySQL 表 + 2 张 ClickHouse 表）
> **图例**：✅ 已有接口/表（无需改动）｜🟡 需修改接口/表（含差异说明）｜🔴 需新增接口/表

---

## 第 1 章 设计依据与范围

### 1.1 UI 原型页面清单（16 页面）

| # | 页面 | 功能概述 | 对应用例 |
|---|------|---------|---------|
| 1 | 系统首页 dashboard | 车队健康总览（健康指数/七大域分值）、接入/在线车辆统计、在线趋势折线图、告警趋势折线图、实时告警列表 | WebSocket 实时推送刷新 |
| 2 | 车辆健康 vehicle-health | 车辆数字孪生（SVG 车身 + 七大域标注点）、七大域健康卡片、部件级状态 | VHR 数字孪生 |
| 3 | AI 诊断 ai-diagnosis | 诊断会话列表、AI 聊天交互、快捷提问、诊断报告 | AI 智能诊断 |
| 4 | 智能维保 maintenance | 维保建议、保养计划时间线（里程+时间双维度）、电池 SOH | 预测性维护 |
| 5 | 车型管理 vehicle-model | 车型 CRUD、车型列表、状态启停 | 基础管理 |
| 6 | 车辆管理 vehicle-list | 车辆 CRUD、VIN 查询、ECU 管理、同步记录 | 基础管理 |
| 7 | DBC 管理 dbc-manage | DBC 文件上传/解析/发布/分发/下载、报文与信号浏览 | DBC 生命周期 |
| 8 | 主动监控 monitor | 实时信号监控、信号曲线、信号订阅 | WebSocket 实时信号 |
| 9 | 故障信息 fault-info | 故障统计、智能值守上下文、故障树场景筛选、故障列表、一键远程诊断 | VHR 场景化诊断 |
| 10 | 远程诊断 diagnosis | 整车/域/部件三级诊断流程、UDS 服务（读故障码/DID/日志/快照等） | ISO 14229 |
| 11 | 日志分析 log-analysis | ECU 日志文件列表、日志下载、日志内容查看/筛选 | 日志管理 |
| 12 | 信号分析 signal-analysis | 信号时间序列曲线、多信号对比、数据统计 | 时序分析 |
| 13 | 信号回放 signal-playback | 历史信号选择、回放控制（播放/暂停/倍速）、AI 异常检测 | 时序回放 |
| 14 | 故障分析 fault-analysis | 故障统计分布、故障码 TOP、按域/场景统计 | 统计分析 |
| 15 | 用户管理 user-manage | 用户 CRUD、角色分配、最后登录时间 | 账号管理 |
| 16 | 权限管理 role-manage | 角色 CRUD、权限点分配、角色-权限关系 | 权限管理 |

### 1.2 现有模块与 UI 页面映射

| 后端服务 | 现有 Controller | RequestMapping | 主要支撑页面 |
|---------|----------------|----------------|-------------|
| service-auth (9081) | AuthController / UserManageController / RoleManageController | `/auth` | 登录、用户管理、权限管理 |
| service-vehicle (9082) | VehicleController / VehicleModelController / FaultConfigController / SyncLogController / AlertRuleController | `/vehicle`、`/alert` | 首页、车辆管理、车型管理、故障信息、故障分析 |
| service-ecu-log (9083) | EcuLogController | `/ecu-log` | 日志分析 |
| service-dbc (9084) | DbcFileController | `/dbc` | DBC 管理 |
| service-signal (9085) | SignalController | `/signal` | 信号分析、信号回放 |
| service-access (9086) | VehicleLogController / VehicleSignalController | `/ecu-log/vehicle`、`/signal/vehicle` | 数据接入（MQTT） |
| service-diagnosis (9087) | UdsDiagnosisController | `/diagnosis` | 远程诊断 |
| service-gateway (9080) | — | 路由 + JWT 过滤 | 全局 |

### 1.3 文档约定

- **接口编号**：`M{服务}-{序号}`，M 表示模块；例如 `M-AUTH-05`。
- **差异标记**：🟡 修改表示"接口已存在但返回字段/入参需扩展"，🔴 新增表示"接口当前不存在，需新建"。
- **数据库差异**：🟡 修改表示"表已存在但需加字段"，🔴 新增表示"表不存在需新建"。

---

## 第 2 章 接口设计

### 2.1 接口全景总览

| 服务 | 已有接口 | 需修改 | 需新增 | 说明 |
|-----|---------|--------|--------|------|
| service-auth | 16 | 3 | 4 | userinfo 缺失、用户分页补字段、权限点 |
| service-vehicle | 33 | 4 | 10 | 健康/风险/故障场景/故障分析 |
| service-ecu-log | 2 | 1 | 2 | 日志内容查看/解析 |
| service-dbc | 21 | 0 | 2 | 批量导入/版本对比 |
| service-signal | 4 | 1 | 4 | 回放控制/AI 分析/统计 |
| service-access | 5 | 0 | 0 | 数据接入不动 |
| service-diagnosis | 16 | 1 | 4 | 故障树场景/三级诊断 |
| **合计** | **97** | **10** | **26** | — |

> 另含 1 个 WebSocket 端点（`/ws/signal`，service-access 9086），支撑首页实时推送与主动监控。

---

### 2.2 service-auth（认证授权服务，9081）

#### 2.2.1 已有接口（✅ 16 个）

**AuthController — 认证（4 个）**

| 编号 | 方法 | 路径 | 说明 | 状态 |
|-----|------|------|------|------|
| M-AUTH-01 | POST | `/auth/login` | 账号密码登录，返回 JWT token | ✅ |
| M-AUTH-02 | POST | `/auth/register` | 用户注册 | ✅ |
| M-AUTH-03 | GET | `/auth/validate` | 校验 token 有效性 | ✅ |
| M-AUTH-04 | POST | `/auth/introspect` | token 解析/用户信息 | ✅ |

**UserManageController — 用户管理（6 个）**

| 编号 | 方法 | 路径 | 说明 | 状态 |
|-----|------|------|------|------|
| M-AUTH-05 | GET | `/auth/user/page` | 用户分页（支持 username/status 过滤） | 🟡 需补 `lastLoginTime` 返回字段 |
| M-AUTH-06 | GET | `/auth/user/{id}` | 用户详情 | ✅ |
| M-AUTH-07 | POST | `/auth/user` | 新增用户 | ✅ |
| M-AUTH-08 | PUT | `/auth/user/{id}` | 修改用户 | ✅ |
| M-AUTH-09 | DELETE | `/auth/user/{id}` | 删除用户 | ✅ |
| M-AUTH-10 | PUT | `/auth/user/{id}/roles` | 分配用户角色 | ✅ |

**RoleManageController — 角色管理（6 个）**

| 编号 | 方法 | 路径 | 说明 | 状态 |
|-----|------|------|------|------|
| M-AUTH-11 | GET | `/auth/role/page` | 角色分页 | ✅ |
| M-AUTH-12 | GET | `/auth/role/list` | 角色列表（不分页） | ✅ |
| M-AUTH-13 | GET | `/auth/role/{id}` | 角色详情 | ✅ |
| M-AUTH-14 | POST | `/auth/role` | 新增角色 | ✅ |
| M-AUTH-15 | PUT | `/auth/role/{id}` | 修改角色 | ✅ |
| M-AUTH-16 | DELETE | `/auth/role/{id}` | 删除角色 | ✅ |

#### 2.2.2 需修改接口（🟡 3 个）

| 编号 | 方法 | 路径 | 差异项说明 |
|-----|------|------|-----------|
| M-AUTH-17 | GET | `/auth/userinfo` | **前端已引用但后端缺失**（`frontend/src/api/auth.js` 调用 `GET /auth/userinfo`，AuthController 无此方法）。需补建：根据 token 返回当前用户信息（id/username/realName/roles/permissions）。→ 归属"需新增"亦成立，此处标记为**前端引用缺失接口**。 |
| M-AUTH-05 | GET | `/auth/user/page` | 返回字段补充 `lastLoginTime`（对应 sys_user 新字段），供用户管理页"最后登录"列展示。 |
| M-AUTH-15 | PUT | `/auth/role/{id}` | 入参扩展支持 `permissionIds`（角色-权限点绑定），或在角色详情 M-AUTH-13 返回 `permissionIds`。 |

#### 2.2.3 需新增接口（🔴 4 个）

| 编号 | 方法 | 路径 | 说明 | 支撑页面 |
|-----|------|------|------|---------|
| M-AUTH-17 | GET | `/auth/userinfo` | 补建用户信息接口（前端已引用），返回当前登录用户信息+权限码 | 全局/布局 |
| M-AUTH-18 | GET | `/auth/perm/list` | 权限点列表（菜单/按钮级权限树） | 权限管理 |
| M-AUTH-19 | GET | `/auth/role/{id}/perms` | 查询角色已分配权限点 ID 列表 | 权限管理 |
| M-AUTH-20 | PUT | `/auth/role/{id}/perms` | 保存角色权限点（全量覆盖） | 权限管理 |

> **前端引用核对**：`auth.js` 引用了 `/auth/userinfo`、`system.js` 引用了 `/auth/user/*`、`/auth/role/*` 全部 10 个接口 — 除 `/auth/userinfo` 外均已存在。**`/auth/userinfo` 是本次唯一的前端引用缺失接口，优先级最高。**

---

### 2.3 service-vehicle（车辆服务，9082）

#### 2.3.1 已有接口（✅ 33 个）

**VehicleController — 车辆（14 个）**

| 编号 | 方法 | 路径 | 说明 | 状态 |
|-----|------|------|------|------|
| M-VEH-01 | GET | `/vehicle/stats` | 首页统计（车辆数/在线数/告警数/车型分布/部件告警/最近告警/故障分布） | 🟡 扩展 |
| M-VEH-02 | GET | `/vehicle/stats/online-trend` | 在线趋势折线数据 | ✅ |
| M-VEH-03 | GET | `/vehicle/stats/alert-long-trend` | 告警长趋势折线数据 | ✅ |
| M-VEH-04 | GET | `/vehicle/page` | 车辆分页（支持 VIN/车型/状态过滤） | 🟡 扩展 |
| M-VEH-05 | GET | `/vehicle/{id}` | 车辆详情 | ✅ |
| M-VEH-06 | GET | `/vehicle/vin/{vin}` | 按 VIN 查询 | ✅ |
| M-VEH-07 | POST | `/vehicle` | 新增车辆 | ✅ |
| M-VEH-08 | PUT | `/vehicle/{id}` | 修改车辆 | ✅ |
| M-VEH-09 | DELETE | `/vehicle/{id}` | 删除车辆 | ✅ |
| M-VEH-10 | POST | `/vehicle/sync/kafka` | 触发 Kafka 数据同步 | ✅ |
| M-VEH-11 | POST | `/vehicle/sync/api` | 触发 API 数据同步 | ✅ |
| M-VEH-12 | GET | `/vehicle/{id}/ecu` | 车辆 ECU 列表 | ✅ |
| M-VEH-13 | POST | `/vehicle/{id}/ecu` | 添加车辆 ECU | ✅ |
| M-VEH-14 | PUT | `/vehicle/ecu/{ecuId}` | 修改 ECU | ✅ |

**VehicleModelController — 车型（5 个）**

| 编号 | 方法 | 路径 | 说明 | 状态 |
|-----|------|------|------|------|
| M-VEH-15 | GET | `/vehicle/model/page` | 车型分页 | ✅ |
| M-VEH-16 | GET | `/vehicle/model/{id}` | 车型详情 | ✅ |
| M-VEH-17 | POST | `/vehicle/model` | 新增车型 | ✅ |
| M-VEH-18 | PUT | `/vehicle/model/{id}` | 修改车型 | ✅ |
| M-VEH-19 | DELETE | `/vehicle/model/{id}` | 删除车型 | ✅ |

**FaultConfigController — 故障配置（5 个）**

| 编号 | 方法 | 路径 | 说明 | 状态 |
|-----|------|------|------|------|
| M-VEH-20 | GET | `/vehicle/fault-config/page` | 故障码配置分页 | ✅ |
| M-VEH-21 | GET | `/vehicle/fault-config/{id}` | 故障配置详情 | ✅ |
| M-VEH-22 | POST | `/vehicle/fault-config` | 新增故障配置 | ✅ |
| M-VEH-23 | PUT | `/vehicle/fault-config/{id}` | 修改故障配置 | ✅ |
| M-VEH-24 | DELETE | `/vehicle/fault-config/{id}` | 删除故障配置 | ✅ |

**SyncLogController — 同步记录（2 个）**

| 编号 | 方法 | 路径 | 说明 | 状态 |
|-----|------|------|------|------|
| M-VEH-25 | GET | `/vehicle/sync-record/page` | 同步记录分页 | ✅ |
| M-VEH-26 | GET | `/vehicle/sync-record/{id}` | 同步记录详情 | ✅ |

**AlertRuleController — 告警规则（7 个）**

| 编号 | 方法 | 路径 | 说明 | 状态 |
|-----|------|------|------|------|
| M-VEH-27 | GET | `/alert/rules` | 告警规则列表 | ✅ |
| M-VEH-28 | POST | `/alert/rules` | 新增告警规则 | ✅ |
| M-VEH-29 | PUT | `/alert/rules/{id}` | 修改告警规则 | ✅ |
| M-VEH-30 | DELETE | `/alert/rules/{id}` | 删除告警规则 | ✅ |
| M-VEH-31 | PUT | `/alert/rules/{id}/status` | 启停规则 | ✅ |
| M-VEH-32 | POST | `/alert/rules/refresh` | 刷新规则引擎缓存 | ✅ |
| M-VEH-33 | GET | `/alert/logs` | 告警触发日志分页 | ✅ |

#### 2.3.2 需修改接口（🟡 4 个）

| 编号 | 方法 | 路径 | 差异项说明 |
|-----|------|------|-----------|
| M-VEH-01 | GET | `/vehicle/stats` | 返回体扩展：新增 `fleetHealthScore`（车队健康指数）、`domainHealth`（七大域分值：智驾/座舱/动力/底盘/车身/三电/网联）、`onlineVehicleCount` 已有保留。支撑首页"车队健康总览"横幅。 |
| M-VEH-04 | GET | `/vehicle/page` | 返回行补充 `healthScore`（健康分）、`riskLevel`（风险等级）、`batterySoh`（电池 SOH）、`lastOnlineTime`（最近在线时间），支撑车辆管理页状态列。 |
| M-VEH-05 | GET | `/vehicle/{id}` | 详情补充 `domainHealth`、`healthScore`、`riskLevel` 汇总，支撑车辆健康页切换车辆。 |
| M-VEH-20 | GET | `/vehicle/fault-config/page` | 返回行补充 `faultSceneId`/`faultSceneName`（故障树场景关联），支撑故障信息页场景列。 |

#### 2.3.3 需新增接口（🔴 10 个）

| 编号 | 方法 | 路径 | 说明 | 支撑页面 |
|-----|------|------|------|---------|
| M-VEH-34 | GET | `/vehicle/health/{vin}` | 车辆七大域健康状态（每域：分值/状态/关键部件列表/告警数） | 车辆健康 |
| M-VEH-35 | GET | `/vehicle/health/{vin}/trend` | 健康分历史趋势（近 30/90 天） | 车辆健康 |
| M-VEH-36 | GET | `/vehicle/health/domain/{vin}/{domainCode}` | 指定域详情（部件级健康、信号采样、风险项） | 车辆健康 |
| M-VEH-37 | GET | `/vehicle/fault/page` | 故障列表分页（支持 VIN/故障码/级别/状态/场景筛选） | 故障信息 |
| M-VEH-38 | GET | `/vehicle/fault/standby` | 智能值守上下文（选中车辆近 90 天维保/未处理故障/AI 推荐优先级） | 故障信息 |
| M-VEH-39 | GET | `/fault-scene/list` | 故障树场景列表（车辆趴窝/电池热失控/制动力不足/碰撞/OTA 失败/通讯异常等） | 故障信息、远程诊断 |
| M-VEH-40 | POST | `/vehicle/fault/{id}/diagnose` | 一键发起远程诊断（关联故障树场景，调用 service-diagnosis） | 故障信息 |
| M-VEH-41 | GET | `/vehicle/fault-analysis/trend` | 故障趋势统计（按日/周） | 故障分析 |
| M-VEH-42 | GET | `/vehicle/fault-analysis/distribution` | 故障分布（按级别/域/场景/故障码 TOP N） | 故障分析 |
| M-VEH-43 | GET | `/vehicle/risk/list` | 高风险车辆列表（按风险等级排序，含风险原因） | 首页、车辆列表 |

---

### 2.4 service-ecu-log（日志服务，9083）

#### 2.4.1 已有接口（✅ 2 个）

| 编号 | 方法 | 路径 | 说明 | 状态 |
|-----|------|------|------|------|
| M-ECU-01 | GET | `/ecu-log/page` | 日志文件分页（支持 VIN/时间段/文件名过滤） | 🟡 扩展 |
| M-ECU-02 | GET | `/ecu-log/download/{id}` | 下载日志文件 | ✅ |

#### 2.4.2 需修改接口（🟡 1 个）

| 编号 | 方法 | 路径 | 差异项说明 |
|-----|------|------|-----------|
| M-ECU-01 | GET | `/ecu-log/page` | 返回行补充 `fileSize`、`recordCount`（解析记录数）、`uploadSource`（车载上传/远程抓取）、`status`（待解析/已解析），支撑日志分析页表格列。 |

#### 2.4.3 需新增接口（🔴 2 个）

| 编号 | 方法 | 路径 | 说明 | 支撑页面 |
|-----|------|------|------|---------|
| M-ECU-03 | GET | `/ecu-log/content/{id}` | 日志内容查看（分页读取，支持关键字高亮筛选） | 日志分析 |
| M-ECU-04 | POST | `/ecu-log/parse/{id}` | 触发日志解析（异步，解析为结构化信号/故障记录） | 日志分析 |

---

### 2.5 service-dbc（DBC 服务，9084）

#### 2.5.1 已有接口（✅ 21 个）

| 编号 | 方法 | 路径 | 说明 | 状态 |
|-----|------|------|------|------|
| M-DBC-01 | GET | `/dbc/page` | DBC 文件分页 | ✅ |
| M-DBC-02 | GET | `/dbc/{id}` | DBC 详情 | ✅ |
| M-DBC-03 | POST | `/dbc/upload` | 上传 DBC 文件 | ✅ |
| M-DBC-04 | GET | `/dbc/{id}/messages` | 报文列表（含信号数/周期） | ✅ |
| M-DBC-05 | GET | `/dbc/{id}/signals` | 信号列表 | ✅ |
| M-DBC-06 | GET | `/dbc/{id}/signal-details` | 信号详情（含多路复用/位序/缩放） | ✅ |
| M-DBC-07 | PUT | `/dbc/{id}` | 修改 DBC 元信息 | ✅ |
| M-DBC-08 | POST | `/dbc/{id}/publish` | 发布 DBC | ✅ |
| M-DBC-09 | POST | `/dbc/{id}/revoke` | 撤销发布 | ✅ |
| M-DBC-10 | DELETE | `/dbc/{id}` | 删除 DBC | ✅ |
| M-DBC-11 | GET | `/dbc/{id}/download` | 下载 DBC 原文件 | ✅ |
| M-DBC-12 | POST | `/dbc/{id}/dispatch/{vehicleId}` | 分发到单台车辆 | ✅ |
| M-DBC-13 | POST | `/dbc/{id}/dispatch` | 批量分发（vehicleIds 数组） | ✅ |
| M-DBC-14 | GET | `/dbc/{id}/structured` | 结构化 DBC 内容 | ✅ |
| M-DBC-15 | GET | `/dbc/{id}/message/{messageKey}` | 单报文详情 | ✅ |
| M-DBC-16 | GET | `/dbc/{id}/signals-native` | 原生解析信号（纯 Java DbcParser） | ✅ |
| M-DBC-17 | POST | `/dbc/{id}/decode` | CAN 帧解码（Intel/Motorola 位运算） | ✅ |
| M-DBC-18 | POST | `/dbc/{id}/encode` | 信号编码为 CAN 帧 | ✅ |
| M-DBC-19 | GET | `/dbc/{id}/generate/java-constants` | 生成 Java 常量 | ✅ |
| M-DBC-20 | GET | `/dbc/{id}/generate/json-schema` | 生成 JSON Schema | ✅ |
| M-DBC-21 | DELETE | `/dbc/{id}/cache` | 清理解析缓存 | ✅ |

#### 2.5.2 需新增接口（🔴 2 个）

| 编号 | 方法 | 路径 | 说明 | 支撑页面 |
|-----|------|------|------|---------|
| M-DBC-22 | POST | `/dbc/batch-import` | 批量上传导入多个 DBC 文件（zip 或多文件 multipart） | DBC 管理 |
| M-DBC-23 | GET | `/dbc/compare/{idA}/{idB}` | DBC 版本对比（报文/信号差异清单，支撑发布前变更评估） | DBC 管理 |

> DBC 模块接口最完整，与 UI 页面（上传/解析/发布/分发/下载/报文/信号浏览）完全对齐，**无需修改现有接口**。

---

### 2.6 service-signal（信号服务，9085）

#### 2.6.1 已有接口（✅ 4 个）

| 编号 | 方法 | 路径 | 说明 | 状态 |
|-----|------|------|------|------|
| M-SIG-01 | GET | `/signal/timeline/{vehicleId}` | 信号时间序列（vin/startTime/endTime 参数，ClickHouse 查询） | 🟡 扩展 |
| M-SIG-02 | GET | `/signal/page/{vehicleId}` | 信号分页 | ✅ |
| M-SIG-03 | GET | `/signal/signal-name/{vehicleId}` | 按信号名查询 | ✅ |
| M-SIG-04 | GET | `/signal/{id}` | 信号详情 | ✅ |

#### 2.6.2 需修改接口（🟡 1 个）

| 编号 | 方法 | 路径 | 差异项说明 |
|-----|------|------|-----------|
| M-SIG-01 | GET | `/signal/timeline/{vehicleId}` | 入参扩展支持 `signalNames`（多信号批量对比，逗号分隔）与 `interval`（采样间隔聚合秒数，默认 1s），支撑信号分析页多曲线对比与信号回放数据源。 |

#### 2.6.3 需新增接口（🔴 4 个）

| 编号 | 方法 | 路径 | 说明 | 支撑页面 |
|-----|------|------|------|---------|
| M-SIG-05 | POST | `/signal/analysis/statistics` | 信号统计（max/min/avg/方差/越限次数，指定时间窗） | 信号分析 |
| M-SIG-06 | GET | `/signal/playback/range` | 可回放时间范围与信号集查询（按 VIN） | 信号回放 |
| M-SIG-07 | POST | `/signal/playback/control` | 回放控制指令（start/pause/resume/speed/seek/stop），返回回放帧流（或复用 WebSocket 推送） | 信号回放 |
| M-SIG-08 | POST | `/signal/analysis/anomaly` | AI 异常检测（基于选定时间窗信号，返回异常点/置信度/原因建议） | 信号回放 |

---

### 2.7 service-access（接入服务，9086）

#### 2.7.1 已有接口（✅ 5 个）

| 编号 | 方法 | 路径 | 说明 | 状态 |
|-----|------|------|------|------|
| M-ACC-01 | POST | `/ecu-log/vehicle/init` | 日志上传初始化（获取 uploadId） | ✅ |
| M-ACC-02 | POST | `/ecu-log/vehicle/chunk` | 日志分片上传 | ✅ |
| M-ACC-03 | POST | `/ecu-log/vehicle/complete` | 日志上传完成合并 | ✅ |
| M-ACC-04 | POST | `/ecu-log/vehicle/report` | 车辆主动上报日志记录 | ✅ |
| M-ACC-05 | POST | `/signal/vehicle/receive` | 车载信号上报（MQTT 入口） | ✅ |

#### 2.7.2 需新增接口

**无。** 接入层面向车载设备，接口稳定，不因 UI 变化调整。

> **WebSocket 端点**：`ws://{host}:9086/ws/signal`（支持全局广播 `/ws/signal` 与按 VIN 点对点 `/ws/signal/{vin}`）。首页实时告警、主动监控实时信号均复用此通道，无需新增。若信号回放需要实时帧推送，可复用该通道扩展消息类型（`PLAYBACK_FRAME`）。

---

### 2.8 service-diagnosis（诊断服务，9087）

#### 2.8.1 已有接口（✅ 16 个）

| 编号 | 方法 | 路径 | 说明 | 状态 |
|-----|------|------|------|------|
| M-DIA-01 | POST | `/diagnosis/uds` | 通用 UDS 服务执行 | ✅ |
| M-DIA-02 | POST | `/diagnosis/session/control` | 诊断会话控制（0x10） | ✅ |
| M-DIA-03 | POST | `/diagnosis/ecu/reset` | ECU 复位（0x11） | ✅ |
| M-DIA-04 | POST | `/diagnosis/security/request-seed` | 安全访问种子（0x27） | ✅ |
| M-DIA-05 | POST | `/diagnosis/security/send-key` | 安全访问密钥（0x27） | ✅ |
| M-DIA-06 | POST | `/diagnosis/data/read` | 读取 DID（0x22） | ✅ |
| M-DIA-07 | POST | `/diagnosis/data/write` | 写入 DID（0x2E） | ✅ |
| M-DIA-08 | POST | `/diagnosis/dtc/read` | 读取故障码（0x19） | ✅ |
| M-DIA-09 | POST | `/diagnosis/dtc/clear` | 清除故障码（0x14） | ✅ |
| M-DIA-10 | POST | `/diagnosis/routine/control` | 例程控制（0x31） | ✅ |
| M-DIA-11 | POST | `/diagnosis/memory/read` | 内存读取（0x23） | ✅ |
| M-DIA-12 | POST | `/diagnosis/memory/write` | 内存写入（0x3D） | ✅ |
| M-DIA-13 | POST | `/diagnosis/io/control` | IO 控制（0x2F） | ✅ |
| M-DIA-14 | POST | `/diagnosis/tester-present` | 测试仪在线（0x3E） | ✅ |
| M-DIA-15 | GET | `/diagnosis/sessions` | 诊断会话记录列表 | ✅ |
| M-DIA-16 | GET | `/diagnosis/services` | 支持的服务清单 | ✅ |

#### 2.8.2 需修改接口（🟡 1 个）

| 编号 | 方法 | 路径 | 差异项说明 |
|-----|------|------|-----------|
| M-DIA-15 | GET | `/diagnosis/sessions` | 返回行补充 `diagLevel`（整车/域/部件三级）、`sceneId/sceneName`（故障树场景）、`faultTreeResult`（扫描结果摘要），支撑远程诊断页会话历史与三级诊断流程展示。 |

#### 2.8.3 需新增接口（🔴 4 个）

| 编号 | 方法 | 路径 | 说明 | 支撑页面 |
|-----|------|------|------|---------|
| M-DIA-17 | GET | `/diagnosis/fault-tree/scenarios` | 故障树场景列表（与 M-VEH-39 同源，供诊断前置选择） | 远程诊断 |
| M-DIA-18 | POST | `/diagnosis/level` | 发起三级诊断（整车扫描 → 域定位 → 部件深度检测），返回诊断任务 ID | 远程诊断 |
| M-DIA-19 | GET | `/diagnosis/task/{taskId}` | 查询诊断任务进度与中间结果（整车/域/部件逐级状态） | 远程诊断 |
| M-DIA-20 | GET | `/diagnosis/report/{sessionId}` | 诊断报告（含 DTC 列表、DID 快照、日志摘要、结论建议） | 远程诊断、AI 诊断 |

---

### 2.9 AI 诊断（🔴 新增模块，建议落在 service-diagnosis 或独立 service-ai）

| 编号 | 方法 | 路径 | 说明 | 支撑页面 |
|-----|------|------|------|---------|
| M-AI-01 | POST | `/ai/session` | 新建诊断会话（绑定 VIN） | AI 诊断 |
| M-AI-02 | GET | `/ai/session/page` | 会话历史分页 | AI 诊断 |
| M-AI-03 | GET | `/ai/session/{id}/messages` | 会话消息列表 | AI 诊断 |
| M-AI-04 | POST | `/ai/chat` | 发送诊断提问，返回 AI 回复（可触发自动诊断） | AI 诊断 |
| M-AI-05 | GET | `/ai/report/{sessionId}` | 生成/获取 AI 诊断报告 | AI 诊断 |

---

## 第 3 章 数据库设计

### 3.1 数据库总览

**MySQL（业务库，共 22 张表）**

| 库 | 已有表 | 需新增 |
|----|--------|--------|
| vrd_auth | sys_user、sys_role、sys_user_role | sys_permission、sys_role_permission |
| vrd_vehicle | vehicle_model、vehicle、vehicle_ecu、vehicle_alert、fault_config、vehicle_fault、vehicle_online_stat、vehicle_alert_trend_stat、sync_log | vehicle_health、fault_scene、maintenance_plan、maintenance_record |
| vrd_ecu_log | ecu_log_file、upload_chunk | — |
| vrd_dbc | dbc_file、dispatch_log | — |
| vrd_signal | vehicle_signal、signal_batch | — |
| vrd_diagnosis | uds_diagnosis_session、uds_dtc_record | ai_diagnosis_session、ai_diagnosis_message |

**ClickHouse（时序库，2 张表，无需改动）**：`ecu_log_records`、`vehicle_signal_records`

---

### 3.2 已有表（✅ 无改动）

| 库 | 表名 | 说明 |
|----|------|------|
| vrd_auth | sys_user | 用户（🟡 见 3.4） |
| vrd_auth | sys_role | 角色 |
| vrd_auth | sys_user_role | 用户-角色关联 |
| vrd_vehicle | vehicle_model | 车型（🟡 见 3.4） |
| vrd_vehicle | vehicle | 车辆（🟡 见 3.4） |
| vrd_vehicle | vehicle_ecu | 车辆 ECU |
| vrd_vehicle | vehicle_alert | 车辆告警 |
| vrd_vehicle | fault_config | 故障码配置 |
| vrd_vehicle | vehicle_fault | 车辆故障 |
| vrd_vehicle | vehicle_online_stat | 在线统计 |
| vrd_vehicle | vehicle_alert_trend_stat | 告警趋势统计 |
| vrd_vehicle | sync_log | 同步记录 |
| vrd_ecu_log | ecu_log_file | ECU 日志文件 |
| vrd_ecu_log | upload_chunk | 上传分片 |
| vrd_dbc | dbc_file | DBC 文件 |
| vrd_dbc | dispatch_log | DBC 分发日志 |
| vrd_signal | vehicle_signal | 信号元数据 |
| vrd_signal | signal_batch | 信号批次 |
| vrd_diagnosis | uds_diagnosis_session | UDS 诊断会话 |
| vrd_diagnosis | uds_dtc_record | UDS 故障码记录 |
| vrd_alert | alert_rule | 告警规则 |
| vrd_alert | alert_trigger_log | 告警触发日志 |

---

### 3.3 需新增表（🔴 8 张）

#### 3.3.1 `sys_permission`（权限点表）

```sql
CREATE TABLE sys_permission (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    parent_id   BIGINT DEFAULT 0 COMMENT '父权限 ID（0=顶级）',
    perm_code   VARCHAR(100) NOT NULL UNIQUE COMMENT '权限码，如 vehicle:list',
    perm_name   VARCHAR(100) NOT NULL COMMENT '权限名称',
    perm_type   TINYINT DEFAULT 1 COMMENT '1-菜单 2-按钮',
    route_path  VARCHAR(200) COMMENT '前端路由',
    icon        VARCHAR(100),
    sort_no     INT DEFAULT 0,
    status      TINYINT DEFAULT 1 COMMENT '1-启用 0-禁用',
    deleted     TINYINT DEFAULT 0,
    create_time DATETIME,
    update_time DATETIME,
    INDEX idx_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限点表';
```

#### 3.3.2 `sys_role_permission`（角色-权限关联表）

```sql
CREATE TABLE sys_role_permission (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id       BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    create_time   DATETIME,
    UNIQUE KEY uk_role_perm (role_id, permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';
```

#### 3.3.3 `vehicle_health`（车辆健康状态表）

```sql
CREATE TABLE vehicle_health (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    vin           VARCHAR(50) NOT NULL,
    domain_code   VARCHAR(30) NOT NULL COMMENT '域编码：ADAS/COCKPIT/POWERTRAIN/CHASSIS/BODY/BATTERY/TELEMATICS',
    domain_name   VARCHAR(50) COMMENT '域名称：智驾/座舱/动力/底盘/车身/三电/网联',
    health_score  INT DEFAULT 100 COMMENT '域健康分 0-100',
    status        VARCHAR(20) DEFAULT 'NORMAL' COMMENT 'NORMAL/ATTENTION/WARNING/DANGER',
    component_json JSON COMMENT '部件级健康 JSON [{name,score,status}]',
    alert_count   INT DEFAULT 0 COMMENT '本域活跃告警数',
    risk_level    VARCHAR(20) DEFAULT 'LOW' COMMENT 'LOW/MEDIUM/HIGH',
    update_time   DATETIME,
    create_time   DATETIME,
    UNIQUE KEY uk_vin_domain (vin, domain_code),
    INDEX idx_vin (vin)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车辆七大域健康状态表';
```

#### 3.3.4 `fault_scene`（故障树场景表）

```sql
CREATE TABLE fault_scene (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    scene_code    VARCHAR(50) NOT NULL UNIQUE COMMENT '场景编码：SCENE_THERMAL_RUNWAY 等',
    scene_name    VARCHAR(100) NOT NULL COMMENT '场景名称：电池热失控/车辆趴窝/制动力不足/碰撞/OTA失败/通讯异常',
    description   TEXT COMMENT '场景描述',
    fault_codes   VARCHAR(500) COMMENT '关联故障码集合，逗号分隔',
    diag_sequence TEXT COMMENT '诊断序列 JSON（故障树步骤）',
    priority      INT DEFAULT 1 COMMENT '优先级',
    ai_confidence DECIMAL(5,2) DEFAULT 0 COMMENT 'AI 默认置信度',
    status        TINYINT DEFAULT 1,
    deleted       TINYINT DEFAULT 0,
    create_time   DATETIME,
    update_time   DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='故障树场景表';
```

#### 3.3.5 `maintenance_plan`（保养计划表）

```sql
CREATE TABLE maintenance_plan (
    id               BIGINT PRIMARY KEY AUTO_INCREMENT,
    vin              VARCHAR(50) NOT NULL,
    plan_name        VARCHAR(100) NOT NULL COMMENT '保养项名称',
    plan_type        TINYINT DEFAULT 1 COMMENT '1-按里程 2-按时间 3-双维度',
    due_mileage      INT COMMENT '建议保养里程 km',
    due_date         DATE COMMENT '建议保养日期',
    last_done_mileage INT COMMENT '上次保养里程',
    last_done_date   DATE COMMENT '上次保养日期',
    status           TINYINT DEFAULT 0 COMMENT '0-未到期 1-即将到期 2-已到期 3-已完成',
    advice           TEXT COMMENT '保养建议描述',
    create_time      DATETIME,
    update_time      DATETIME,
    INDEX idx_vin (vin)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='保养计划表';
```

#### 3.3.6 `maintenance_record`（维保记录表）

```sql
CREATE TABLE maintenance_record (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    vin           VARCHAR(50) NOT NULL,
    record_type   TINYINT DEFAULT 1 COMMENT '1-保养 2-维修 3-检测',
    title         VARCHAR(200) COMMENT '记录标题',
    content       TEXT COMMENT '维保内容明细',
    mileage       INT COMMENT '维保时里程',
    record_date   DATE COMMENT '维保日期',
    cost          DECIMAL(10,2) COMMENT '费用',
    operator      VARCHAR(50) COMMENT '操作人',
    create_time   DATETIME,
    INDEX idx_vin (vin)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='维保记录表';
```

#### 3.3.7 `ai_diagnosis_session`（AI 诊断会话表）

```sql
CREATE TABLE ai_diagnosis_session (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    vin           VARCHAR(50),
    user_id       BIGINT,
    title         VARCHAR(200) COMMENT '会话标题（首问自动生成）',
    status        VARCHAR(20) DEFAULT 'RUNNING' COMMENT 'RUNNING/COMPLETED/FAILED',
    summary       TEXT COMMENT '会话结论摘要',
    report_json   JSON COMMENT '诊断报告 JSON',
    create_time   DATETIME,
    update_time   DATETIME,
    INDEX idx_vin (vin),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 诊断会话表';
```

#### 3.3.8 `ai_diagnosis_message`（AI 诊断消息表）

```sql
CREATE TABLE ai_diagnosis_message (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id   BIGINT NOT NULL,
    role         VARCHAR(20) COMMENT 'USER/ASSISTANT/SYSTEM',
    content      TEXT COMMENT '消息内容',
    msg_type     VARCHAR(30) DEFAULT 'TEXT' COMMENT 'TEXT/DIAG_CMD/REPORT/QUICK_QUESTION',
    ref_data     JSON COMMENT '关联诊断数据（DTC/信号/报告引用）',
    create_time  DATETIME,
    INDEX idx_session (session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 诊断消息表';
```

---

### 3.4 需修改表（🟡 4 张，标明差异项）

#### 3.4.1 `sys_user` — 新增 1 字段

| 变更 | 字段 | 类型 | 说明 |
|------|------|------|------|
| 🔴 新增列 | `last_login_time` | DATETIME NULL | 最后登录时间，支撑用户管理页"最后登录"列（现有表无此字段） |

```sql
ALTER TABLE sys_user ADD COLUMN last_login_time DATETIME NULL COMMENT '最后登录时间' AFTER update_time;
```

#### 3.4.2 `vehicle` — 新增 4 字段

| 变更 | 字段 | 类型 | 说明 |
|------|------|------|------|
| 🔴 新增列 | `health_score` | INT DEFAULT 100 | 整车健康分 0-100，支撑车辆管理页健康列 |
| 🔴 新增列 | `risk_level` | VARCHAR(20) DEFAULT 'LOW' | 风险等级 LOW/MEDIUM/HIGH |
| 🔴 新增列 | `battery_soh` | DECIMAL(5,2) | 电池健康度 SOH（%），支撑智能维保页 |
| 🔴 新增列 | `last_online_time` | DATETIME | 最近在线时间，支撑在线状态展示 |

```sql
ALTER TABLE vehicle
    ADD COLUMN health_score INT DEFAULT 100 COMMENT '整车健康分' AFTER data_source,
    ADD COLUMN risk_level VARCHAR(20) DEFAULT 'LOW' COMMENT '风险等级' AFTER health_score,
    ADD COLUMN battery_soh DECIMAL(5,2) COMMENT '电池SOH%' AFTER risk_level,
    ADD COLUMN last_online_time DATETIME COMMENT '最近在线时间' AFTER battery_soh;
```

#### 3.4.3 `vehicle_model` — 新增 1 字段

| 变更 | 字段 | 类型 | 说明 |
|------|------|------|------|
| 🔴 新增列 | `domain_coverage` | VARCHAR(200) | 车型覆盖的域列表 JSON/逗号分隔，支撑车型管理页域覆盖展示 |

```sql
ALTER TABLE vehicle_model ADD COLUMN domain_coverage VARCHAR(200) DEFAULT NULL COMMENT '覆盖域集合' AFTER description;
```

#### 3.4.4 `fault_config` — 新增 2 字段

| 变更 | 字段 | 类型 | 说明 |
|------|------|------|------|
| 🔴 新增列 | `fault_scene_id` | BIGINT NULL | 关联故障树场景（fault_scene.id），支撑故障信息页场景列 |
| 🔴 新增列 | `ai_badge` | TINYINT DEFAULT 0 | 是否 AI 场景识别（1-是），支撑 AI 徽标展示 |

```sql
ALTER TABLE fault_config
    ADD COLUMN fault_scene_id BIGINT NULL COMMENT '故障树场景ID' AFTER status,
    ADD COLUMN ai_badge TINYINT DEFAULT 0 COMMENT 'AI场景识别标记' AFTER fault_scene_id;
```

#### 3.4.5 `uds_diagnosis_session` — 新增 3 字段

| 变更 | 字段 | 类型 | 说明 |
|------|------|------|------|
| 🔴 新增列 | `diag_level` | VARCHAR(20) DEFAULT 'VEHICLE' | 诊断层级 VEHICLE/DOMAIN/PART，支撑三级诊断 |
| 🔴 新增列 | `scene_id` | BIGINT NULL | 故障树场景 ID |
| 🔴 新增列 | `fault_tree_result` | JSON | 故障树扫描结果摘要 |

```sql
ALTER TABLE uds_diagnosis_session
    ADD COLUMN diag_level VARCHAR(20) DEFAULT 'VEHICLE' COMMENT '诊断层级' AFTER status,
    ADD COLUMN scene_id BIGINT NULL COMMENT '故障树场景ID' AFTER diag_level,
    ADD COLUMN fault_tree_result JSON NULL COMMENT '故障树扫描结果' AFTER scene_id;
```

---

### 3.5 数据库差异项汇总表

| 表 | 变更类型 | 变更内容 | 支撑 UI 页面 |
|----|---------|---------|-------------|
| sys_user | 🟡 修改 | + `last_login_time` | 用户管理（最后登录列） |
| vehicle | 🟡 修改 | + `health_score`、`risk_level`、`battery_soh`、`last_online_time` | 车辆管理、智能维保、车辆健康 |
| vehicle_model | 🟡 修改 | + `domain_coverage` | 车型管理 |
| fault_config | 🟡 修改 | + `fault_scene_id`、`ai_badge` | 故障信息 |
| uds_diagnosis_session | 🟡 修改 | + `diag_level`、`scene_id`、`fault_tree_result` | 远程诊断 |
| sys_permission | 🔴 新增 | 权限点表 | 权限管理 |
| sys_role_permission | 🔴 新增 | 角色权限关联 | 权限管理 |
| vehicle_health | 🔴 新增 | 七大域健康状态 | 车辆健康、首页 |
| fault_scene | 🔴 新增 | 故障树场景 | 故障信息、远程诊断 |
| maintenance_plan | 🔴 新增 | 保养计划 | 智能维保 |
| maintenance_record | 🔴 新增 | 维保记录 | 智能维保 |
| ai_diagnosis_session | 🔴 新增 | AI 诊断会话 | AI 诊断 |
| ai_diagnosis_message | 🔴 新增 | AI 诊断消息 | AI 诊断 |

> **无需改动**：vehicle_ecu、vehicle_alert、vehicle_fault、vehicle_online_stat、vehicle_alert_trend_stat、sync_log、ecu_log_file、upload_chunk、dbc_file、dispatch_log、vehicle_signal、signal_batch、uds_dtc_record、alert_rule、alert_trigger_log（15 张），以及 ClickHouse 全部 2 张时序表。

---

## 第 4 章 接口差异项汇总（实施清单）

### 4.1 优先级 P0（前端已引用/页面已就绪，缺失将直接报错或空白）

| # | 差异项 | 类型 | 说明 |
|---|--------|------|------|
| 1 | `GET /auth/userinfo` | 🔴 新增 | 前端 `auth.js` 已调用，后端缺失，**必须补建** |
| 2 | `GET /vehicle/stats` 扩展 | 🟡 修改 | 首页"车队健康总览"横幅需要 fleetHealthScore + 七大域分值 |

### 4.2 优先级 P1（核心页面功能所需）

| # | 差异项 | 类型 | 说明 |
|---|--------|------|------|
| 3 | `/vehicle/health/*`（3 个） | 🔴 新增 | 车辆健康页（数字孪生 + 七大域） |
| 4 | `/vehicle/fault/page`、`/vehicle/fault/standby` | 🔴 新增 | 故障信息页（智能值守 + 故障列表） |
| 5 | `/fault-scene/list`、`/diagnosis/fault-tree/scenarios` | 🔴 新增 | 故障树场景（故障信息 + 远程诊断） |
| 6 | `/vehicle/fault-analysis/*`（2 个） | 🔴 新增 | 故障分析页 |
| 7 | `/diagnosis/level`、`/diagnosis/task/{id}`、`/diagnosis/report/{id}` | 🔴 新增 | 远程诊断三级流程 + 报告 |
| 8 | `/vehicle/user/page` 返回 lastLoginTime | 🟡 修改 | 用户管理页最后登录列 |

### 4.3 优先级 P2（增强功能，可迭代实现）

| # | 差异项 | 类型 | 说明 |
|---|--------|------|------|
| 9 | `/ai/*`（5 个） | 🔴 新增 | AI 诊断会话/聊天/报告 |
| 10 | `/maintenance/*` 数据（靠 vehicle_health + maintenance 表） | 🔴 新增 | 智能维保页 |
| 11 | `/signal/playback/*`（3 个） | 🔴 新增 | 信号回放 |
| 12 | `/signal/analysis/*`（2 个） | 🔴 新增 | 信号分析统计/AI 异常 |
| 13 | `/ecu-log/content/{id}`、`/ecu-log/parse/{id}` | 🔴 新增 | 日志分析 |
| 14 | `/auth/perm/*`、`/auth/role/{id}/perms` | 🔴 新增 | 权限管理权限点 |
| 15 | `/dbc/batch-import`、`/dbc/compare` | 🔴 新增 | DBC 批量导入/版本对比 |

---

## 第 5 章 实施建议

1. **先修 P0**：补建 `/auth/userinfo`（auth 服务），扩展 `/vehicle/stats` 返回体（vehicle 服务），同步执行 sys_user/vehicle 表 ALTER。
2. **WebSocket 复用**：首页实时推送与主动监控复用现有 `/ws/signal`，避免新建通道；信号回放帧流可扩展消息类型。
3. **健康分计算**：`vehicle_health` 表建议由定时任务（每小时）基于告警/信号统计生成七大域分值，整车健康分 = 各域加权平均。
4. **故障树场景**：`fault_scene` 初始数据（6 类场景）随 09_seed 脚本补充；故障码与场景的关联由 fault_config.fault_scene_id 维护。
5. **权限模型**：sys_permission 种子数据按现有 16 页面 + 按钮级操作生成权限码，避免前端路由与权限码脱节。
