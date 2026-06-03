<template>
  <div class="fault-monitor">
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card>
          <template #header>
            <span>故障监控</span>
          </template>
          <el-table :data="alerts" stripe v-loading="loading">
            <el-table-column prop="time" label="时间" width="180" />
            <el-table-column prop="vin" label="VIN" width="200" />
            <el-table-column prop="type" label="类型" width="120" />
            <el-table-column prop="componentCode" label="部件" width="100" />
            <el-table-column prop="message" label="告警信息" min-width="200" />
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
      <el-col :span="8">
        <el-card>
          <template #header>
            <span>故障描述分布</span>
          </template>
          <div ref="pieChartRef" class="pie-chart"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import { getVehicleDashboardStats } from '@/api/vehicle'

const loading = ref(false)
const alerts = ref([])
const faultByCode = ref([])
const pieChartRef = ref(null)
let pieChart = null

const loadData = async () => {
  loading.value = true
  try {
    const res = await getVehicleDashboardStats()
    const data = res.data || {}
    alerts.value = data.recentAlerts || []
    faultByCode.value = data.faultByCode || []
    updatePieChart()
  } catch (error) {
    console.error('加载故障监控数据失败', error)
  } finally {
    loading.value = false
  }
}

const updatePieChart = () => {
  if (!pieChart) return
  const pieData = faultByCode.value.map(item => ({
    name: item.faultName || item.faultCode,
    value: item.faultCount ?? 0
  }))
  pieChart.setOption({
    tooltip: { trigger: 'item', confine: true },
    legend: { type: 'scroll', orient: 'vertical', right: 0, top: 'middle', height: '80%' },
    series: [{
      name: '故障描述',
      type: 'pie',
      radius: ['40%', '65%'],
      center: ['40%', '50%'],
      label: { show: false },
      data: pieData.length ? pieData : [{ value: 0, name: '暂无数据' }]
    }]
  })
}

onMounted(async () => {
  if (pieChartRef.value) {
    pieChart = echarts.init(pieChartRef.value)
  }
  await loadData()
})

onBeforeUnmount(() => {
  if (pieChart) {
    pieChart.dispose()
    pieChart = null
  }
})
</script>

<style scoped>
.fault-monitor {
  padding: 20px;
}

.pie-chart {
  width: 100%;
  height: 420px;
}
</style>
