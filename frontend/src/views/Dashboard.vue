<template>
  <div>
    <!-- 页面标题 -->
    <div class="page-title">系统首页</div>
    <div class="page-desc">
      车辆远程诊断平台总览 · VHR 数据驱动 · 实时监控全量车辆状态
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
          <div class="stat-trend down">↓ 实时监控</div>
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

// ---- 图表 ----
const chartRef = ref(null)
const pieChartRef = ref(null)
const trendChartRef = ref(null)
const alertLongTrendChartRef = ref(null)
let chart = null
let pieChart = null
let trendChart = null
let alertLongTrendChart = null
let slowTimer = null

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
  faults: 0
})

const onlineRate = computed(() => {
  if (!stats.value.totalVehicles) return 0
  return Math.round((stats.value.onlineVehicles / stats.value.totalVehicles) * 100)
})

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

  // 首次加载所有数据
  await Promise.all([loadStats(), loadOnlineTrend(), loadAlertLongTrend()])

  // 定时刷新（30 秒）：统计计数 + 趋势图
  slowTimer = setInterval(() => {
    loadStats()
    loadOnlineTrend()
    loadAlertLongTrend()
  }, 30_000)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  if (slowTimer) clearInterval(slowTimer)
  if (chart) { chart.dispose(); chart = null }
  if (pieChart) { pieChart.dispose(); pieChart = null }
  if (trendChart) { trendChart.dispose(); trendChart = null }
  if (alertLongTrendChart) { alertLongTrendChart.dispose(); alertLongTrendChart = null }
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
