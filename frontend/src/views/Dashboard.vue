<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6">
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
      
      <el-col :span="6">
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
      
      <el-col :span="6">
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
      
      <el-col :span="6">
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
      <el-col :span="16">
        <el-card>
          <template #header>
            <span>信号趋势</span>
          </template>
          <div ref="chartRef" style="width: 100%; height: 300px;"></div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card>
          <template #header>
            <span>车辆状态分布</span>
          </template>
          <div ref="pieChartRef" style="width: 100%; height: 300px;"></div>
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
import { ref, onMounted, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import {
  Van,
  SuccessFilled,
  WarningFilled,
  CircleCloseFilled
} from '@element-plus/icons-vue'

const chartRef = ref(null)
const pieChartRef = ref(null)
let chart = null
let pieChart = null

const stats = ref({
  totalVehicles: 152,
  onlineVehicles: 128,
  alerts: 23,
  faults: 8
})

const alerts = ref([
  { time: '2024-01-15 14:30:22', vin: 'LSVAG4189ES123456', type: '温度过高', message: '发动机温度超过阈值', status: '未处理' },
  { time: '2024-01-15 13:45:10', vin: 'LSVBG6189ES234567', type: '电瓶电压', message: '电瓶电压过低', status: '已处理' },
  { time: '2024-01-15 12:20:35', vin: 'LSVAH4189ES345678', type: '刹车系统', message: '刹车片磨损警告', status: '未处理' },
  { time: '2024-01-15 11:15:45', vin: 'LSVAJ6189ES456789', type: '轮胎压力', message: '左前轮胎压不足', status: '已处理' }
])

onMounted(() => {
  initCharts()
})

onBeforeUnmount(() => {
  if (chart) chart.dispose()
  if (pieChart) pieChart.dispose()
})

const initCharts = () => {
  chart = echarts.init(chartRef.value)
  const chartOption = {
    tooltip: { trigger: 'axis' },
    legend: { data: ['车速', '转速', '水温'] },
    xAxis: {
      type: 'category',
      data: ['00:00', '02:00', '04:00', '06:00', '08:00', '10:00', '12:00']
    },
    yAxis: [
      { type: 'value', name: '车速(km/h)' },
      { type: 'value', name: '转速(rpm)' }
    ],
    series: [
      { name: '车速', type: 'line', data: [45, 52, 38, 65, 78, 82, 75] },
      { name: '转速', type: 'line', yAxisIndex: 1, data: [1200, 1500, 1100, 2000, 2500, 2800, 2400] },
      { name: '水温', type: 'line', yAxisIndex: 1, data: [85, 88, 82, 90, 95, 92, 89] }
    ]
  }
  chart.setOption(chartOption)

  pieChart = echarts.init(pieChartRef.value)
  const pieOption = {
    tooltip: { trigger: 'item' },
    legend: { orient: 'vertical', left: 'left' },
    series: [{
      name: '车辆状态',
      type: 'pie',
      radius: '60%',
      data: [
        { value: 128, name: '在线' },
        { value: 15, name: '离线' },
        { value: 7, name: '故障' },
        { value: 2, name: '维护中' }
      ]
    }]
  }
  pieChart.setOption(pieOption)
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
</style>
