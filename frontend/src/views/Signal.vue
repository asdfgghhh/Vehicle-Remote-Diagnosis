<template>
  <div class="signal-monitoring">
    <el-card>
      <template #header>
        <span>信号监控</span>
      </template>
      
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="选择车辆">
          <el-select v-model="queryForm.vehicleId" placeholder="请选择车辆" @change="handleVehicleChange">
            <el-option
              v-for="vehicle in vehicles"
              :key="vehicle.id"
              :label="vehicle.vin"
              :value="vehicle.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
        </el-form-item>
      </el-form>
      
      <el-tabs v-model="activeTab" type="border-card">
        <el-tab-pane label="时间轴视图" name="timeline">
          <div ref="timelineChartRef" style="width: 100%; height: 400px;"></div>
        </el-tab-pane>
        <el-tab-pane label="信号列表" name="table">
          <el-table :data="signalData" stripe height="400">
            <el-table-column prop="signalName" label="信号名称" width="150" />
            <el-table-column prop="signalValue" label="信号值" width="120" />
            <el-table-column prop="unit" label="单位" width="80" />
            <el-table-column prop="messageName" label="消息名称" width="150" />
            <el-table-column prop="signalTime" label="时间" />
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="信号详情" name="detail">
          <div v-if="selectedSignal" class="signal-detail">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="信号名称">{{ selectedSignal.signalName }}</el-descriptions-item>
              <el-descriptions-item label="信号值">{{ selectedSignal.signalValue }}</el-descriptions-item>
              <el-descriptions-item label="单位">{{ selectedSignal.unit }}</el-descriptions-item>
              <el-descriptions-item label="消息名称">{{ selectedSignal.messageName }}</el-descriptions-item>
              <el-descriptions-item label="时间">{{ selectedSignal.signalTime }}</el-descriptions-item>
            </el-descriptions>
            
            <div ref="signalChartRef" style="width: 100%; height: 300px; margin-top: 20px;"></div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, watch } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { getSignalTimeline, getSignalByName } from '@/api/signal'
import { getVehiclePage } from '@/api/vehicle'

const vehicles = ref([])
const signalData = ref([])
const selectedSignal = ref(null)
const activeTab = ref('timeline')
const dateRange = ref([])
const timelineChartRef = ref(null)
const signalChartRef = ref(null)
let timelineChart = null
let signalChart = null

const queryForm = reactive({
  vehicleId: null
})

onMounted(() => {
  loadVehicles()
  
  const now = new Date()
  const start = new Date(now.getTime() - 3600000)
  dateRange.value = [start, now]
})

onBeforeUnmount(() => {
  if (timelineChart) timelineChart.dispose()
  if (signalChart) signalChart.dispose()
})

const loadVehicles = async () => {
  try {
    const res = await getVehiclePage({ current: 1, size: 100 })
    vehicles.value = res.data.records
    if (vehicles.value.length > 0) {
      queryForm.vehicleId = vehicles.value[0].id
      handleQuery()
    }
  } catch (error) {
    console.error('加载车辆列表失败', error)
  }
}

const handleVehicleChange = () => {
  if (queryForm.vehicleId) {
    handleQuery()
  }
}

const handleQuery = async () => {
  if (!queryForm.vehicleId) {
    ElMessage.warning('请选择车辆')
    return
  }
  
  if (!dateRange.value || dateRange.value.length !== 2) {
    ElMessage.warning('请选择时间范围')
    return
  }
  
  try {
    const params = {
      vehicleId: queryForm.vehicleId,
      startTime: dateRange.value[0].toISOString(),
      endTime: dateRange.value[1].toISOString()
    }
    
    const res = await getSignalTimeline(params)
    signalData.value = res.data.signals || []
    
    if (activeTab.value === 'timeline') {
      initTimelineChart()
    }
  } catch (error) {
    ElMessage.error('查询失败')
  }
}

const initTimelineChart = () => {
  if (!timelineChartRef.value) return
  
  if (timelineChart) timelineChart.dispose()
  timelineChart = echarts.init(timelineChartRef.value)
  
  const categories = [...new Set(signalData.value.map(s => s.signalName))]
  const series = categories.map(name => {
    const data = signalData.value
      .filter(s => s.signalName === name)
      .map(s => [s.signalTime, parseFloat(s.signalValue) || 0])
    
    return { name, type: 'line', data }
  })
  
  const option = {
    tooltip: { trigger: 'axis' },
    legend: { data: categories },
    xAxis: { type: 'time', boundaryGap: false },
    yAxis: { type: 'value' },
    series
  }
  
  timelineChart.setOption(option)
}

watch(activeTab, (newVal) => {
  if (newVal === 'timeline') {
    setTimeout(initTimelineChart, 100)
  } else if (newVal === 'detail' && signalData.value.length > 0) {
    selectedSignal.value = signalData.value[0]
    initSignalChart()
  }
})

const initSignalChart = () => {
  if (!signalChartRef.value || !selectedSignal.value) return
  
  if (signalChart) signalChart.dispose()
  signalChart = echarts.init(signalChartRef.value)
  
  const data = signalData.value
    .filter(s => s.signalName === selectedSignal.value.signalName)
    .map(s => [s.signalTime, parseFloat(s.signalValue) || 0])
  
  const option = {
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'time', boundaryGap: false },
    yAxis: { type: 'value' },
    series: [{
      type: 'line',
      data,
      areaStyle: { opacity: 0.3 }
    }]
  }
  
  signalChart.setOption(option)
}
</script>

<style scoped>
.signal-monitoring {
  padding: 20px;
}

.search-form {
  margin-bottom: 20px;
}

.signal-detail {
  padding: 20px;
}
</style>
