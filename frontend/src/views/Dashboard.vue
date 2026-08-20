<template>
  <div>
    <!-- 页面标题 -->
    <div class="page-title">系统首页</div>
    <div class="page-desc">
      车辆远程诊断平台总览 · VHR 数据驱动 · 实时监控全量车辆健康状态 ·
      <span class="ws-live-badge" style="display:inline-flex;align-items:center;gap:4px">
        <span class="dot"></span>WebSocket 实时推送 · 数据自动刷新
      </span>
    </div>

    <!-- 车队健康总览横幅 -->
    <div class="health-banner">
      <div>
        <div class="hb-title">🚗 车队健康总览</div>
        <div class="hb-sub">VHR 数据闭环 · 覆盖七大域核心部件 · 实时更新</div>
      </div>
      <div class="hb-divider"></div>
      <div class="hb-stat">
        <div class="hb-num">{{ stats.fleetHealthScore || 0 }}<small>/100</small></div>
        <div class="hb-label">车队健康指数</div>
      </div>
      <div class="hb-stat">
        <div class="hb-num">{{ stats.totalVehicles }}</div>
        <div class="hb-label">接入车辆</div>
      </div>
      <div class="hb-stat">
        <div class="hb-num" style="color:#4ade80">{{ stats.onlineVehicles }}</div>
        <div class="hb-label">在线车辆</div>
      </div>
      <div class="hb-stat">
        <div class="hb-num" style="color:#fbbf24">{{ stats.alerts }}</div>
        <div class="hb-label">活跃告警</div>
      </div>
      <div class="hb-domains">
        <div v-for="d in domainList" :key="d.code" class="hb-domain" :class="hbDomainClass(d.score)" :title="d.name">
          <span class="hd-name">{{ d.name }}</span>
          <span class="hd-val">{{ d.score }}</span>
        </div>
      </div>
    </div>

    <!-- 统计卡 -->
    <div class="stat-grid">
      <div class="stat-card">
        <div class="stat-icon" style="background:var(--primary-bg);color:var(--primary)">🏷️</div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.connectedModelCount }}</div>
          <div class="stat-label">接入车型</div>
          <div class="stat-trend up">↑ VHR 车型矩阵</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background:var(--primary-bg);color:var(--primary)">🚙</div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.totalVehicles }}</div>
          <div class="stat-label">接入车辆总数</div>
          <div class="stat-trend up">↑ 实时接入</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background:var(--success-bg);color:var(--success)">🟢</div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.onlineVehicles }}</div>
          <div class="stat-label">在线车辆</div>
          <div class="stat-trend up">↑ 在线率 {{ onlineRate }}%</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background:var(--warning-bg);color:var(--warning)">⚠️</div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.alerts }}</div>
          <div class="stat-label">活跃告警</div>
          <div class="stat-trend down">↓ 实时推送</div>
        </div>
      </div>
    </div>

    <!-- 趋势图 + 实时告警 -->
    <div class="grid-7-5">
      <div class="lc-stack">
        <div class="card" style="margin-bottom:0">
          <div class="card-header">
            <div class="card-title">📈 车辆在线趋势</div>
            <div class="lc-legend">
              <el-radio-group v-model="trendGranularity" size="small" @change="loadOnlineTrend">
                <el-radio-button value="hour">按小时</el-radio-button>
                <el-radio-button value="day">按天</el-radio-button>
              </el-radio-group>
            </div>
          </div>
          <div class="card-body">
            <div ref="trendChartRef" class="trend-chart"></div>
          </div>
        </div>
        <div class="card" style="margin-bottom:0">
          <div class="card-header">
            <div class="card-title">⚠️ 告警数趋势</div>
            <div class="lc-legend">
              <el-select v-model="alertMetric" size="small" style="width:150px" @change="loadAlertLongTrend">
                <el-option label="故障数" value="faultCount" />
                <el-option label="故障车辆数" value="faultVehicleCount" />
                <el-option label="车辆平均故障数" value="avgFaultPerVehicle" />
              </el-select>
              <el-radio-group v-model="alertGranularity" size="small" @change="loadAlertLongTrend">
                <el-radio-button value="hour">时</el-radio-button>
                <el-radio-button value="day">天</el-radio-button>
                <el-radio-button value="week">周</el-radio-button>
                <el-radio-button value="month">月</el-radio-button>
              </el-radio-group>
            </div>
          </div>
          <div class="card-body">
            <div ref="alertLongTrendChartRef" class="trend-chart"></div>
          </div>
        </div>
      </div>

      <div class="card">
        <div class="card-header">
          <div class="card-title">实时告警列表</div>
          <span class="ws-live-badge"><span class="dot"></span>实时推送</span>
        </div>
        <div class="card-body" style="padding:14px 16px 16px">
          <div class="alert-list-stack">
            <div v-for="(a, i) in alerts.slice(0, 6)" :key="i" class="alert-item">
              <div class="alert-severity" :class="alertSeverity(a)"></div>
              <div class="alert-content">
                <div class="alert-title">{{ a.message || a.type }}</div>
                <div class="alert-row">
                  <span class="label">故障编码：</span>
                  <span class="value">{{ a.faultCode || a.componentCode || '-' }}</span>
                </div>
                <div class="alert-row">
                  <span class="label">车辆VIN：</span>
                  <span class="vin-link">{{ a.vin }}</span>
                </div>
                <div class="alert-time">{{ a.time }}</div>
              </div>
            </div>
            <div v-if="!alerts.length" class="empty">
              <div class="empty-icon">📭</div>
              暂无实时告警
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- WebSocket 实时推送（保留功能） -->
    <div class="card">
      <div class="card-header">
        <div class="card-title">🔌 WebSocket 实时推送</div>
        <div>
          <el-tag :type="wsConnected ? 'success' : 'danger'" size="small">
            {{ wsConnected ? '已连接' : '已断开' }}
          </el-tag>
          <el-button size="small" style="margin-left:8px" @click="wsReconnect" :disabled="wsConnected">重新连接</el-button>
        </div>
      </div>
      <div class="card-body">
        <div style="display:flex;gap:12px;margin-bottom:12px;flex-wrap:wrap;align-items:center">
          <el-input v-model="wsVin" placeholder="输入 VIN 订阅（留空为全局广播）" size="small" style="width:280px" clearable>
            <template #append>
              <el-button @click="wsSubscribe" :disabled="!wsConnected">订阅</el-button>
            </template>
          </el-input>
          <span style="font-size:12px;color:var(--text-secondary)">已接收推送：<b style="color:var(--primary)">{{ wsCount }}</b></span>
          <span style="font-size:12px;color:var(--text-secondary)">当前订阅：<b style="color:var(--primary)">{{ wsSubscribedVin || '全局广播' }}</b></span>
        </div>
        <el-table :data="wsSignals" size="small" max-height="240" stripe>
          <el-table-column prop="time" label="时间" width="170" />
          <el-table-column prop="vin" label="VIN" width="160" />
          <el-table-column prop="messageName" label="消息" width="140" />
          <el-table-column label="信号">
            <template #default="{ row }">
              <el-tag v-for="(s, i) in row.signals.slice(0, 5)" :key="i" size="small" style="margin-right:6px">
                {{ s.name }}={{ s.value }}{{ s.unit || '' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="wsSignals.length === 0" description="等待实时信号推送..." :image-size="60" style="padding:12px 0" />
      </div>
    </div>

    <!-- 部件故障统计 + 故障描述统计 -->
    <div class="grid-7-5">
      <div class="card">
        <div class="card-header"><div class="card-title">📊 部件故障统计</div></div>
        <div class="card-body"><div ref="chartRef" style="width:100%;height:300px"></div></div>
      </div>
      <div class="card">
        <div class="card-header"><div class="card-title">🥧 故障描述统计</div></div>
        <div class="card-body"><div ref="pieChartRef" class="pie-chart"></div></div>
      </div>
    </div>

    <!-- 最近告警表格 -->
    <div class="card">
      <div class="card-header"><div class="card-title">最近告警</div></div>
      <div class="card-body" style="padding:0">
        <el-table :data="alerts" stripe>
          <el-table-column prop="time" label="时间" width="180" />
          <el-table-column prop="vin" label="VIN" width="200" />
          <el-table-column prop="type" label="类型" width="120" />
          <el-table-column prop="componentCode" label="部件" width="100" />
          <el-table-column prop="message" label="告警信息" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === '未处理' ? 'danger' : 'success'">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getVehicleDashboardStats, getVehicleOnlineTrend, getVehicleAlertLongTrend } from '@/api/vehicle'
import wsManager from '../utils/websocket'

// ---- WebSocket ----
const wsConnected = ref(false)
const wsVin = ref('')
const wsSubscribedVin = ref('')
const wsCount = ref(0)
const wsSignals = ref([])

const wsHandleSignal = (data) => {
  wsCount.value++
  wsSignals.value.unshift({
    vin: data.vin,
    time: data.timestamp
      ? new Date(data.timestamp).toLocaleString('zh-CN', { hour12: false })
      : new Date().toLocaleString('zh-CN', { hour12: false }),
    messageName: data.signals?.[0]?.messageName || '',
    signals: data.signals || []
  })
  if (wsSignals.value.length > 50) {
    wsSignals.value = wsSignals.value.slice(0, 50)
  }
}

const wsSubscribe = () => {
  const vin = wsVin.value.trim()
  if (vin) {
    wsManager.subscribeVin(vin)
    wsSubscribedVin.value = vin
  }
}

const wsReconnect = () => {
  wsManager.disconnect()
  wsManager.connect(wsSubscribedVin.value || null)
}

// ---- 图表 ----
const chartRef = ref(null)
const pieChartRef = ref(null)
const trendChartRef = ref(null)
const alertLongTrendChartRef = ref(null)
let chart = null
let pieChart = null
let trendChart = null
let alertLongTrendChart = null

const trendGranularity = ref('hour')
const onlineTrend = ref([])

const alertMetric = ref('faultCount')
const alertGranularity = ref('hour')
const alertLongTrend = ref([])

const metricLabels = {
  faultCount: '故障数',
  faultVehicleCount: '故障车辆数',
  avgFaultPerVehicle: '车辆平均故障数'
}

// ---- 统计数据 ----
const stats = ref({
  connectedModelCount: 0,
  totalVehicles: 0,
  onlineVehicles: 0,
  alerts: 0,
  faults: 0,
  fleetHealthScore: 0,
  domainHealth: []
})

const onlineRate = computed(() => {
  if (!stats.value.totalVehicles) return 0
  return Math.round((stats.value.onlineVehicles / stats.value.totalVehicles) * 100)
})

const domainNames = {
  ADAS: '智驾域',
  COCKPIT: '座舱域',
  POWERTRAIN: '动力域',
  CHASSIS: '底盘域',
  BODY: '车身域',
  BATTERY: '三电域',
  TELEMATICS: '网联域'
}

const domainList = computed(() => {
  const list = (stats.value.domainHealth || []).map(d => ({
    code: d.domainCode,
    name: d.domainName || domainNames[d.domainCode] || d.domainCode,
    score: d.healthScore ?? 0
  }))
  if (!list.length) {
    return Object.entries(domainNames).map(([code, name]) => ({ code, name, score: 0 }))
  }
  return list
})

const hbDomainClass = (score) => {
  if (score >= 90) return 'ok'
  if (score >= 75) return 'warn'
  return 'bad'
}

const alertSeverity = (a) => {
  if (a.level === 'HIGH' || a.level === 'CRITICAL') return 'critical'
  if (a.level === 'MEDIUM' || a.level === 'WARNING') return 'warning'
  if (a.status === '未处理') return 'warning'
  return 'info'
}

const alertByComponent = ref([])
const faultByCode = ref([])
const alerts = ref([])

const loadStats = async () => {
  try {
    const res = await getVehicleDashboardStats()
    const data = res.data || {}
    stats.value.connectedModelCount = data.connectedModelCount ?? 0
    stats.value.totalVehicles = data.totalVehicles ?? 0
    stats.value.onlineVehicles = data.onlineVehicles ?? 0
    stats.value.alerts = data.totalAlertCount ?? 0
    stats.value.faults = data.totalFaultCount ?? 0
    stats.value.fleetHealthScore = data.fleetHealthScore ?? 0
    stats.value.domainHealth = data.domainHealth || []
    alertByComponent.value = data.alertByComponent || []
    faultByCode.value = data.faultByCode || []
    alerts.value = data.recentAlerts || []
    updateComponentChart()
    updatePieChart()
  } catch (error) {
    console.error('加载仪表盘统计失败', error)
  }
}

const loadOnlineTrend = async () => {
  try {
    const res = await getVehicleOnlineTrend({ granularity: trendGranularity.value })
    onlineTrend.value = res.data?.points || []
    updateTrendChart()
  } catch (error) {
    console.error('加载在线车辆趋势失败', error)
  }
}

const loadAlertLongTrend = async () => {
  try {
    const res = await getVehicleAlertLongTrend({
      granularity: alertGranularity.value,
      metric: alertMetric.value
    })
    alertLongTrend.value = res.data?.points || []
    updateAlertLongTrendChart()
  } catch (error) {
    console.error('加载告警长周期趋势失败', error)
  }
}

onMounted(async () => {
  await nextTick()
  initCharts()
  window.addEventListener('resize', handleResize)
  await Promise.all([loadStats(), loadOnlineTrend(), loadAlertLongTrend()])
  wsManager.on('connected', () => {
    wsConnected.value = true
  })
  wsManager.on('disconnected', () => {
    wsConnected.value = false
  })
  wsManager.on('signal', wsHandleSignal)
  wsManager.connect()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  wsManager.disconnect()
  if (chart) {
    chart.dispose()
    chart = null
  }
  if (pieChart) {
    pieChart.dispose()
    pieChart = null
  }
  if (trendChart) {
    trendChart.dispose()
    trendChart = null
  }
  if (alertLongTrendChart) {
    alertLongTrendChart.dispose()
    alertLongTrendChart = null
  }
})

const updateComponentChart = () => {
  if (!chart) return
  const labels = alertByComponent.value.map(item => item.componentCode)
  const counts = alertByComponent.value.map(item => item.alertCount ?? 0)
  chart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: labels.length ? labels : ['暂无数据'],
      axisLabel: { interval: 0 }
    },
    yAxis: { type: 'value', name: '告警数', minInterval: 1 },
    series: [{
      name: '告警数量',
      type: 'bar',
      barMaxWidth: 48,
      data: counts.length ? counts : [0],
      itemStyle: { color: '#d97706', borderRadius: [4, 4, 0, 0] }
    }]
  })
}

const truncateText = (text, maxLen = 10) => {
  if (!text) return ''
  return text.length > maxLen ? `${text.slice(0, maxLen)}…` : text
}

const buildPieChartOption = (pieData) => ({
  tooltip: {
    trigger: 'item',
    confine: true,
    formatter: params => {
      const data = params.data || {}
      const lines = [`故障描述: ${data.faultName || params.name}`]
      if (data.faultCode) lines.push(`故障编码: ${data.faultCode}`)
      if (data.componentCode) lines.push(`部件: ${data.componentCode}`)
      lines.push(`数量: ${params.value} (${params.percent}%)`)
      return lines.join('<br/>')
    }
  },
  legend: {
    type: 'scroll',
    orient: 'vertical',
    right: 0,
    top: 'middle',
    height: '85%',
    itemWidth: 10,
    itemHeight: 10,
    itemGap: 8,
    textStyle: { fontSize: 11, lineHeight: 14 },
    formatter: name => truncateText(name, 8)
  },
  series: [{
    name: '故障描述',
    type: 'pie',
    radius: ['42%', '68%'],
    center: ['38%', '50%'],
    avoidLabelOverlap: true,
    label: { show: false },
    labelLine: { show: false },
    emphasis: {
      label: { show: true, fontSize: 11, formatter: '{b}' }
    },
    data: pieData.length ? pieData : [{ value: 0, name: '暂无数据' }]
  }]
})

const updatePieChart = () => {
  if (!pieChart) return
  const pieData = faultByCode.value.map(item => ({
    name: item.faultName || item.faultCode || '未知故障',
    value: item.faultCount ?? 0,
    faultCode: item.faultCode,
    componentCode: item.componentCode,
    faultName: item.faultName
  }))
  pieChart.setOption(buildPieChartOption(pieData), true)
}

const updateTrendChart = () => {
  if (!trendChart) return
  const labels = onlineTrend.value.map(item => item.timeLabel)
  const counts = onlineTrend.value.map(item => item.onlineCount ?? 0)
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: labels.length ? labels : ['暂无数据'],
      axisLabel: { interval: trendGranularity.value === 'hour' ? 1 : 0 }
    },
    yAxis: { type: 'value', name: '在线车辆', minInterval: 1 },
    series: [{
      name: '在线车辆',
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 6,
      data: counts.length ? counts : [0],
      itemStyle: { color: '#0050d8' },
      lineStyle: { width: 2.5 },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(0, 80, 216, 0.16)' },
            { offset: 1, color: 'rgba(0, 80, 216, 0.02)' }
          ]
        }
      }
    }]
  }, true)
}

const updateAlertLongTrendChart = () => {
  if (!alertLongTrendChart) return
  const labels = alertLongTrend.value.map(item => item.timeLabel)
  const values = alertLongTrend.value.map(item => item.value ?? 0)
  const seriesName = metricLabels[alertMetric.value] || '指标值'
  const isAvg = alertMetric.value === 'avgFaultPerVehicle'
  alertLongTrendChart.setOption({
    tooltip: {
      trigger: 'axis',
      formatter: params => {
        const idx = params[0]?.dataIndex
        const point = alertLongTrend.value[idx] || {}
        const lines = [`${params[0]?.axisValue}`, `${seriesName}: ${params[0]?.value}`]
        if (point.faultCount != null) lines.push(`故障数: ${point.faultCount}`)
        if (point.faultVehicleCount != null) lines.push(`故障车辆数: ${point.faultVehicleCount}`)
        return lines.join('<br/>')
      }
    },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: labels.length ? labels : ['暂无数据'],
      axisLabel: {
        interval: alertGranularity.value === 'hour' ? 2 : 0,
        rotate: labels.length > 12 ? 30 : 0
      }
    },
    yAxis: { type: 'value', name: seriesName, minInterval: isAvg ? 0 : 1 },
    series: [{
      name: seriesName,
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 6,
      data: values.length ? values : [0],
      itemStyle: { color: '#f59e0b' },
      lineStyle: { width: 2.5 },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(245, 158, 11, 0.18)' },
            { offset: 1, color: 'rgba(245, 158, 11, 0.02)' }
          ]
        }
      }
    }]
  }, true)
}

const initCharts = () => {
  if (!chartRef.value || !pieChartRef.value || !trendChartRef.value || !alertLongTrendChartRef.value) return
  chart = echarts.init(chartRef.value)
  chart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: [] },
    yAxis: { type: 'value', name: '告警数', minInterval: 1 },
    series: [{ name: '告警数量', type: 'bar', data: [] }]
  })
  pieChart = echarts.init(pieChartRef.value)
  pieChart.setOption(buildPieChartOption([]))
  trendChart = echarts.init(trendChartRef.value)
  trendChart.setOption({
    xAxis: { type: 'category', data: [] },
    yAxis: { type: 'value', name: '在线车辆' },
    series: [{ name: '在线车辆', type: 'line', data: [] }]
  })
  alertLongTrendChart = echarts.init(alertLongTrendChartRef.value)
  alertLongTrendChart.setOption({
    xAxis: { type: 'category', data: [] },
    yAxis: { type: 'value', name: '故障数' },
    series: [{ name: '故障数', type: 'line', data: [] }]
  })
}

const handleResize = () => {
  chart?.resize()
  pieChart?.resize()
  trendChart?.resize()
  alertLongTrendChart?.resize()
}
</script>

<style scoped>
.trend-chart {
  width: 100%;
  height: 300px;
}

.pie-chart {
  width: 100%;
  height: 320px;
}
</style>
