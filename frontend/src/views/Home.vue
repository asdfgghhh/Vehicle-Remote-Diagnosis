<template>
  <div>
    <el-row :gutter="20" style="margin-bottom: 20px">
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header><span>🔌 WebSocket 实时推送</span></template>
          <p>基于 WebSocket 的车辆信号实时推送，支持按 VIN 订阅和全局广播两种模式，实现毫秒级信号传输。</p>
          <el-tag type="success">已完成</el-tag>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header><span>🔧 UDS 诊断协议</span></template>
          <p>ISO 14229 标准诊断服务实现，支持会话控制、安全访问、DID 读写、DTC 读取、例程控制等 15+ 诊断服务。</p>
          <el-tag type="success">已完成</el-tag>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header><span>📊 DBC 专业解析</span></template>
          <p>使用 Python cantools 库替代正则解析，支持 CAN 报文编解码、值表解析、精确的信号位布局计算。</p>
          <el-tag type="success">已完成</el-tag>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header><span>🔔 规则引擎告警</span></template>
          <p>基于 Easy Rules 的动态规则引擎，支持阈值/趋势/组合三种告警规则，含防抖、冷却、MVEL 表达式评估。</p>
          <el-tag type="success">已完成</el-tag>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header><span>📡 MQTT + Kafka</span></template>
          <p>车端 MQTT 协议接入，Kafka 高吞吐消息分发，ClickHouse 时序数据存储，支持海量车辆并发接入。</p>
          <el-tag type="info">已有</el-tag>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header><span>🛡️ 安全认证</span></template>
          <p>Spring Cloud Gateway JWT 认证、UDS 安全访问（种子/密钥）、RBAC 权限管理、API 白名单控制。</p>
          <el-tag type="info">已有</el-tag>
        </el-card>
      </el-col>
    </el-row>

    <el-divider />

    <el-alert
      title="优化实施概要"
      type="success"
      :closable="false"
      style="margin-bottom: 20px"
    >
      <ul style="margin: 8px 0; padding-left: 20px; line-height: 2">
        <li><strong>WebSocket 实时推送</strong>：service-access 新增 WebSocket 配置、SignalWebSocketHandler、WebSocketSessionManager；前端新增 WebSocket 连接管理和实时信号监控页面</li>
        <li><strong>UDS 诊断协议</strong>：新增 service-diagnosis 微服务模块，实现 ISO 14229 全部核心诊断服务（SID 0x10-0x3E），通过 Kafka 下发诊断指令</li>
        <li><strong>DBC 专业解析库</strong>：新增 Python cantools 微服务 (dbc-parser-service)，Java 端新增 CantoolsClient，DbcFileServiceImpl 优先使用 cantools 解析</li>
        <li><strong>规则引擎告警</strong>：service-vehicle 集成 Easy Rules，新增 AlertRuleEngine（阈值/趋势/组合规则），AlertEvaluationConsumer 实时评估，alert_rule/alert_trigger_log 表</li>
      </ul>
    </el-alert>
  </div>
</template>

<script setup>
</script>
