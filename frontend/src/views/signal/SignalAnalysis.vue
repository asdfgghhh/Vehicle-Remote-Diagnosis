<template>
  <div class="signal-analysis">
    <el-card style="margin-bottom: 16px">
      <el-form inline :model="query" label-width="80px">
        <el-form-item label="车辆 VIN">
          <el-select v-model="query.vin" filterable placeholder="选择车辆" style="width: 260px" @change="resolveVehicle">
            <el-option v-for="v in vehicleOptions" :key="v.vin" :label="`${v.plateNumber || v.vin} - ${v.vin}`" :value="v.vin" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            :default-time="[new Date(2000, 1, 1, 0, 0, 0), new Date(2000, 1, 1, 23, 59, 59)]"
            style="width: 380px"
          />
        </el-form-item>
        <el-form-item label="信号">
          <el-select
            v-model="selectedSignals"
            multiple
            filterable
            collapse-tags
            collapse-tags-tooltip
            placeholder="从 DBC 选择信号（最多 8 个）"
            style="width: 320px"
          >
            <el-option v-for="s in signalOptions" :key="s.name" :label="`${s.name} (${s.messageName || '-'})`" :value="s.name" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleQuery">分析</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card v-if="hasData">
      <template #header><span>📈 信号时间序列对比</span></template>
      <div ref="chartRef" class="chart-box"></div>
    </el-card>

    <el-card v-if="hasData" style="margin-top: 16px">
      <template #header><span>📊 数据统计</span></template>
      <el-table :data="statRows" stripe size="small">
        <el-table-column prop="name" label="信号" />
        <el-table-column prop="count" label="采样数" width="100" />
        <el-table-column label="平均值" width="120">
          <template #default="{ row }">{{ row.avg != null ? row.avg.toFixed(2) : '-' }}</template>
        </el-table-column>
        <el-table-column label="最小值" width="120">
          <template #default="{ row }">{{ row.min != null ? row.min.toFixed(2) : '-' }}</template>
        </el-table-column>
        <el-table-column label="最大值" width="120">
          <template #default="{ row }">{{ row.max != null ? row.max.toFixed(2) : '-' }}</template>
        </el-table-column>
        <el-table-column label="方差" width="120">
          <template #default="{ row }">{{ row.variance != null ? row.variance.toFixed(2) : '-' }}</template>
        </el-table-column>
        <el-table-column label="异常建议" min-width="200">
          <template #default="{ row }">
            <el-tag v-if="row.anomaly" type="danger" size="small">{{ row.anomaly }}</el-tag>
            <span v-else style="color: #67C23A">正常波动</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import dayjs from 'dayjs'
import { ElMessage } from 'element-plus'
import { getSignalTimeline } from '@/api/signal'
import { getVehiclePage } from '@/api/vehicle'
import { getDbcFilePage, getDbcSignals } from '@/api/dbc'

const CHART_COLORS = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#845EC2', '#00C9A7', '#D65DB1', '#FF6F91']

const vehicleOptions = ref([])
const signalOptions = ref([])
const query = reactive({ vin: '' })
const dateRange = ref([])
const selectedSignals = ref([])
const loading = ref(false)
const chartRef = ref(null)
const signalData = ref([])
let chartInstance = null
let vehicleId = null

const hasData = computed(() => signalData.value.length > 0 && selectedSignals.value.length > 0)

const statRows = computed(() => {
  return selectedSignals.value.map(name => {
    const points = signalData.value.filter(p => p.signalName === name).map(p => Number(p.numericValue ?? p.signalValue) || 0)
    if (!points.length) return { name, count: 0 }
    const sum = points.reduce((a, b) => a + b, 0)
    const avg = sum / points.length
    const min = Math.min(...points)
    const max = Math.max(...points)
    const variance = points.reduce((a, b) => a + (b - avg) ** 2, 0) / points.length
    const range = max - min
    let anomaly = null
    if (variance > 1000 && range > 100) anomaly = '波动异常'
    else if (range === 0 && points.length > 10) anomaly = '信号恒定'
    return { name, count: points.length, avg, min, max, variance, anomaly }
  })
})

const loadVehicles = async () => {
  try {
    const res = await getVehiclePage({ current: 1, size: 200 })
    vehicleOptions.value = (res.data?.records || []).map(r => ({ vin: r.vin, plateNumber: r.plateNumber || r.licensePlate }))
    if (vehicleOptions.value.length) {
      query.vin = vehicleOptions.value[0].vin
      await resolveVehicle()
    }
  } catch (e) {
    console.error('加载车辆列表失败', e)
  }
}

const resolveVehicle = async () => {
  if (!query.vin) return
  try {
    const res = await getVehiclePage({ current: 1, size: 10, vin: query.vin })
    const first = (res.data?.records || []).find(r => r.vin === query.vin)
    vehicleId = first?.id || null
    await loadSignalOptions()
  } catch (e) {
    console.error('解析车辆失败', e)
  }
}

const loadSignalOptions = async () => {
  if (!vehicleId) return
  try {
    const res = await getDbcFilePage({ current: 1, size: 1, status: 1 })
    const file = res.data?.records?.[0]
    if (file) {
      const sr = await getDbcSignals(file.id)
      signalOptions.value = (sr.data || []).map(s => ({ name: s.signalName || s.name, messageName: s.messageName }))
    }
  } catch (e) {
    console.error('加载 DBC 信号失败', e)
  }
}

const handleQuery = async () => {
  if (!query.vin) {
    ElMessage.warning('请选择车辆')
    return
  }
  if (!selectedSignals.value.length) {
    ElMessage.warning('请至少选择一个信号')
    return
  }
  if (!dateRange.value?.length) {
    ElMessage.warning('请选择时间范围')
    return
  }
  if (!vehicleId) {
    ElMessage.warning('未找到该车辆')
    return
  }
  loading.value = true
  try {
    const res = await getSignalTimeline({
      vehicleId,
      vin: query.vin,
      startTime: dateRange.value[0],
      endTime: dateRange.value[1]
    })
    const selected = new Set(selectedSignals.value)
    signalData.value = (res.data?.signals || []).filter(p => selected.has(p.signalName))
    await nextTick()
    renderChart()
    if (!signalData.value.length) {
      ElMessage.info('所选时间范围内暂无信号数据')
    }
  } catch (e) {
    ElMessage.error('信号分析查询失败')
    console.error(e)
  } finally {
    loading.value = false
  }
}

const renderChart = () => {
  if (!chartRef.value) return
  if (!hasData.value) {
    chartInstance?.clear()
    return
  }
  const series = selectedSignals.value
    .map((name, idx) => {
      const points = signalData.value
        .filter(p => p.signalName === name)
        .map(p => [p.signalTime, Number(p.numericValue ?? p.signalValue) || 0])
      return {
        name,
        type: 'line',
        showSymbol: false,
        smooth: true,
        lineStyle: { width: 1.5, color: CHART_COLORS[idx % CHART_COLORS.length] },
        itemStyle: { color: CHART_COLORS[idx % CHART_COLORS.length] },
        data: points
      }
    })
    .filter(s => s.data.length > 0)

  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }
  chartInstance.setOption({
    tooltip: { trigger: 'axis' },
    legend: { type: 'scroll', top: 0 },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    dataZoom: [
      { type: 'inside', start: 0, end: 100 },
      { type: 'slider', start: 0, end: 100, height: 18 }
    ],
    xAxis: { type: 'time' },
    yAxis: { type: 'value', scale: true },
    series
  }, true)
}

const handleResize = () => chartInstance?.resize()

onMounted(async () => {
  await loadVehicles()
  const end = dayjs()
  dateRange.value = [end.subtract(2, 'hour').format('YYYY-MM-DD HH:mm:ss'), end.format('YYYY-MM-DD HH:mm:ss')]
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance?.dispose()
})
</script>

<style scoped>
.chart-box {
  width: 100%;
  height: 420px;
}
</style>
