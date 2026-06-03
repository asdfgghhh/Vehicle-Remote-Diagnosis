<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="4">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #909399;">
              <el-icon><Collection /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.connectedModelCount }}</div>
              <div class="stat-label">接入车型</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="5">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #409EFF;">
              <el-icon><Van /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalVehicles }}</div>
              <div class="stat-label">车辆总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="5">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #67C23A;">
              <el-icon><SuccessFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.onlineVehicles }}</div>
              <div class="stat-label">在线车辆</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="5">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #E6A23C;">
              <el-icon><WarningFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.alerts }}</div>
              <div class="stat-label">告警数量</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="5">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #F56C6C;">
              <el-icon><CircleCloseFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.faults }}</div>
              <div class="stat-label">故障数量</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="trend-header">
              <span>在线车辆趋势</span>
              <el-radio-group v-model="trendGranularity" size="small" @change="loadOnlineTrend">
                <el-radio-button value="hour">按小时</el-radio-button>
                <el-radio-button value="day">按天</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="trendChartRef" class="trend-chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="trend-header">
              <span>告警长周期趋势分析</span>
              <div class="trend-controls">
                <el-select v-model="alertMetric" size="small" style="width: 160px;" @change="loadAlertLongTrend">
                  <el-option label="故障数" value="faultCount" />
                  <el-option label="故障车辆数" value="faultVehicleCount" />
                  <el-option label="车辆平均故障数" value="avgFaultPerVehicle" />
                </el-select>
                <el-radio-group v-model="alertGranularity" size="small" @change="loadAlertLongTrend">
                  <el-radio-button value="hour">按小时</el-radio-button>
                  <el-radio-button value="day">按天</el-radio-button>
                  <el-radio-button value="week">按周</el-radio-button>
                  <el-radio-button value="month">按月</el-radio-button>
                </el-radio-group>
              </div>
            </div>
          </template>
          <div ref="alertLongTrendChartRef" class="trend-chart"></div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="16">
        <el-card>
          <template #header>
            <span>部件故障统计</span>
          </template>
          <div ref="chartRef" style="width: 100%; height: 300px;"></div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card>
          <template #header>
            <span>故障描述统计</span>
          </template>
          <div ref="pieChartRef" class="pie-chart"></div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card>
          <template #header>
            <span>最近告警</span>
          </template>
          <el-table :data="alerts" stripe>
            <el-table-column prop="time" label="时间" width="180" />
            <el-table-column prop="vin" label="VIN" width="200" />
            <el-table-column prop="type" label="类型" width="120" />
            <el-table-column prop="componentCode" label="部件" width="100" />
            <el-table-column prop="message" label="告警信息" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === '未处理' ? 'danger' : 'success'">
                  {{ row.status }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getVehicleDashboardStats, getVehicleOnlineTrend, getVehicleAlertLongTrend } from '@/api/vehicle'
import {
  Van,
  Collection,
  SuccessFilled,
  WarningFilled,
  CircleCloseFilled
} from '@element-plus/icons-vue'

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

const stats = ref({
  connectedModelCount: 0,
  totalVehicles: 0,
  onlineVehicles: 0,
  alerts: 0,
  faults: 0
})

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
  await Promise.all([loadStats(), loadOnlineTrend(), loadAlertLongTrend()])
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
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
  if (!chart) {
    return
  }
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
      itemStyle: { color: '#E6A23C' }
    }]
  })
}

const truncateText = (text, maxLen = 10) => {
  if (!text) {
    return ''
  }
  return text.length > maxLen ? `${text.slice(0, maxLen)}…` : text
}

const buildPieChartOption = (pieData) => ({
  tooltip: {
    trigger: 'item',
    confine: true,
    formatter: params => {
      const data = params.data || {}
      const lines = [`故障描述: ${data.faultName || params.name}`]
      if (data.faultCode) {
        lines.push(`故障编码: ${data.faultCode}`)
      }
      if (data.componentCode) {
        lines.push(`部件: ${data.componentCode}`)
      }
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
      label: {
        show: true,
        fontSize: 11,
        formatter: '{b}'
      }
    },
    data: pieData.length ? pieData : [{ value: 0, name: '暂无数据' }]
  }]
})

const updatePieChart = () => {
  if (!pieChart) {
    return
  }
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
  if (!trendChart) {
    return
  }
  const labels = onlineTrend.value.map(item => item.timeLabel)
  const counts = onlineTrend.value.map(item => item.onlineCount ?? 0)
  const xName = trendGranularity.value === 'day' ? '日期' : '时间'
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      name: xName,
      boundaryGap: false,
      data: labels.length ? labels : ['暂无数据'],
      axisLabel: { interval: trendGranularity.value === 'hour' ? 1 : 0 }
    },
    yAxis: {
      type: 'value',
      name: '在线车辆',
      minInterval: 1
    },
    series: [{
      name: '在线车辆',
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 6,
      data: counts.length ? counts : [0],
      itemStyle: { color: '#67C23A' },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(103, 194, 58, 0.35)' },
            { offset: 1, color: 'rgba(103, 194, 58, 0.05)' }
          ]
        }
      }
    }]
  }, true)
}

const updateAlertLongTrendChart = () => {
  if (!alertLongTrendChart) {
    return
  }
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
        if (point.faultCount != null) {
          lines.push(`故障数: ${point.faultCount}`)
        }
        if (point.faultVehicleCount != null) {
          lines.push(`故障车辆数: ${point.faultVehicleCount}`)
        }
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
    yAxis: {
      type: 'value',
      name: seriesName,
      minInterval: isAvg ? 0 : 1
    },
    series: [{
      name: seriesName,
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 6,
      data: values.length ? values : [0],
      itemStyle: { color: '#F56C6C' },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(245, 108, 108, 0.35)' },
            { offset: 1, color: 'rgba(245, 108, 108, 0.05)' }
          ]
        }
      }
    }]
  }, true)
}

const initCharts = () => {
  if (!chartRef.value || !pieChartRef.value || !trendChartRef.value || !alertLongTrendChartRef.value) {
    return
  }

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
.dashboard {
  padding: 20px;
}

.stat-card {
  cursor: pointer;
  transition: transform 0.2s;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 28px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #333;
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin-top: 4px;
}

.pie-chart {
  width: 100%;
  height: 320px;
}

.trend-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
}

.trend-controls {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.trend-chart {
  width: 100%;
  height: 300px;
}
</style>
