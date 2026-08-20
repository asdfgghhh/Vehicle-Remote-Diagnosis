<template>
  <div class="vehicle-health">
    <!-- 车辆选择 + 概览 -->
    <el-card style="margin-bottom: 16px">
      <div class="health-header">
        <div class="vehicle-select">
          <el-select
            v-model="selectedVin"
            filterable
            placeholder="选择车辆（VIN）"
            style="width: 320px"
            @change="loadHealth"
          >
            <el-option
              v-for="v in vehicleOptions"
              :key="v.vin"
              :label="`${v.plateNumber || v.vin} - ${v.vin}`"
              :value="v.vin"
            />
          </el-select>
          <el-button :loading="loading" @click="loadHealth" style="margin-left: 8px">刷新</el-button>
        </div>
        <div v-if="health" class="health-overview">
          <div class="overview-item">
            <div class="overview-label">整车健康分</div>
            <el-progress
              type="dashboard"
              :percentage="health.healthScore || 0"
              :color="scoreColor(health.healthScore)"
              :width="90"
            />
          </div>
          <div class="overview-item info">
            <div class="info-row"><span class="label">车牌号</span><span>{{ health.plateNumber || '-' }}</span></div>
            <div class="info-row"><span class="label">车型</span><span>{{ health.modelName || '-' }}</span></div>
            <div class="info-row"><span class="label">风险等级</span>
              <el-tag :type="riskTagType(health.riskLevel)" size="small">{{ health.riskLevel || '-' }}</el-tag>
            </div>
            <div class="info-row"><span class="label">电池 SOH</span><span>{{ health.batterySoh != null ? health.batterySoh + '%' : '-' }}</span></div>
            <div class="info-row"><span class="label">最近在线</span><span>{{ fmtTime(health.lastOnlineTime) }}</span></div>
          </div>
        </div>
        <el-empty v-else-if="!loading" description="请选择车辆查看健康状态" :image-size="80" />
      </div>
    </el-card>

    <el-row v-if="health" :gutter="16">
      <!-- 七大域健康卡片 -->
      <el-col v-for="d in health.domains" :key="d.domainCode" :span="6" style="margin-bottom: 16px">
        <el-card class="domain-card" :class="'status-' + (d.status || '').toLowerCase()" @click="openDomain(d)">
          <div class="domain-head">
            <span class="domain-name">{{ d.domainName }}</span>
            <el-tag :type="statusTagType(d.status)" size="small">{{ d.status || '-' }}</el-tag>
          </div>
          <el-progress
            type="circle"
            :percentage="d.healthScore || 0"
            :width="72"
            :color="scoreColor(d.healthScore)"
            style="margin: 8px auto; display: block"
          />
          <div class="domain-meta">
            <span>告警 <b :style="{ color: d.alertCount ? '#F56C6C' : '#67C23A' }">{{ d.alertCount || 0 }}</b></span>
            <span>风险 <el-tag :type="riskTagType(d.riskLevel)" size="mini">{{ d.riskLevel || '-' }}</el-tag></span>
          </div>
          <div class="component-list">
            <div v-for="c in (d.components || []).slice(0, 5)" :key="c.name" class="component-row">
              <span class="c-name">{{ c.name }}</span>
              <span class="c-score" :style="{ color: scoreTextColor(c.score) }">{{ c.score }}</span>
            </div>
            <div v-if="!(d.components || []).length" class="empty-tip">暂无部件数据</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 健康趋势 -->
    <el-card v-if="health">
      <template #header>
        <div class="trend-header">
          <span>健康分趋势</span>
          <el-radio-group v-model="trendDays" size="small" @change="loadTrend">
            <el-radio-button :value="30">近 30 天</el-radio-button>
            <el-radio-button :value="90">近 90 天</el-radio-button>
          </el-radio-group>
        </div>
      </template>
      <div ref="trendRef" class="trend-chart"></div>
    </el-card>

    <!-- 域详情抽屉 -->
    <el-drawer v-model="domainDrawer" :title="domainDetail ? domainDetail.domainName + ' 详情' : '域详情'" size="420px">
      <div v-if="domainDetail">
        <el-descriptions :column="1" border style="margin-bottom: 16px">
          <el-descriptions-item label="健康分">
            <b :style="{ color: scoreTextColor(domainDetail.healthScore) }">{{ domainDetail.healthScore }}</b>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTagType(domainDetail.status)">{{ domainDetail.status }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="告警数">{{ domainDetail.alertCount || 0 }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ fmtTime(domainDetail.updateTime) }}</el-descriptions-item>
        </el-descriptions>
        <h4>部件级健康</h4>
        <el-table :data="domainDetail.components || []" size="small" stripe>
          <el-table-column prop="name" label="部件" />
          <el-table-column label="分值" width="100">
            <template #default="{ row }">
              <b :style="{ color: scoreTextColor(row.score) }">{{ row.score }}</b>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="statusTagType(row.status)" size="small">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!(domainDetail.components || []).length" description="暂无部件数据" :image-size="60" />
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getVehicleHealth, getVehicleHealthTrend, getVehicleDomainHealth } from '@/api/health'
import { getVehiclePage } from '@/api/vehicle'
import { ElMessage } from 'element-plus'

const selectedVin = ref('')
const vehicleOptions = ref([])
const health = ref(null)
const loading = ref(false)
const trendDays = ref(30)
const trendRef = ref(null)
let trendChart = null
const domainDrawer = ref(false)
const domainDetail = ref(null)

const scoreColor = (score) => {
  if (score == null) return '#909399'
  if (score >= 90) return '#67C23A'
  if (score >= 75) return '#409EFF'
  if (score >= 60) return '#E6A23C'
  return '#F56C6C'
}

const scoreTextColor = (score) => {
  if (score == null) return '#909399'
  if (score >= 90) return '#67C23A'
  if (score >= 75) return '#409EFF'
  if (score >= 60) return '#E6A23C'
  return '#F56C6C'
}

const statusTagType = (status) => {
  const map = { NORMAL: 'success', ATTENTION: 'primary', WARNING: 'warning', DANGER: 'danger' }
  return map[status] || 'info'
}

const riskTagType = (level) => {
  const map = { LOW: 'success', MEDIUM: 'warning', HIGH: 'danger' }
  return map[level] || 'info'
}

const fmtTime = (t) => {
  if (!t) return '-'
  return new Date(t).toLocaleString('zh-CN', { hour12: false })
}

const loadVehicles = async () => {
  try {
    const res = await getVehiclePage({ current: 1, size: 200 })
    const records = res.data?.records || []
    vehicleOptions.value = records.map(r => ({ vin: r.vin, plateNumber: r.plateNumber || r.licensePlate }))
    if (records.length && !selectedVin.value) {
      selectedVin.value = records[0].vin
      await loadHealth()
    }
  } catch (e) {
    console.error('加载车辆列表失败', e)
  }
}

const loadHealth = async () => {
  if (!selectedVin.value) return
  loading.value = true
  try {
    const res = await getVehicleHealth(selectedVin.value)
    health.value = res.data
    await nextTick()
    initTrend()
    loadTrend()
  } catch (e) {
    ElMessage.error('加载车辆健康数据失败')
    console.error(e)
  } finally {
    loading.value = false
  }
}

const loadTrend = async () => {
  if (!selectedVin.value || !trendChart) return
  try {
    const res = await getVehicleHealthTrend(selectedVin.value, trendDays.value)
    const points = res.data?.points || []
    trendChart.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: points.map(p => p.timeLabel),
        axisLabel: { interval: Math.ceil(points.length / 12) }
      },
      yAxis: { type: 'value', name: '健康分', min: 0, max: 100 },
      series: [{
        name: '健康分',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 5,
        data: points.map(p => p.healthScore),
        itemStyle: { color: '#409EFF' },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
              { offset: 1, color: 'rgba(64, 158, 255, 0.03)' }
            ]
          }
        }
      }]
    }, true)
  } catch (e) {
    console.error('加载健康趋势失败', e)
  }
}

const initTrend = () => {
  if (!trendRef.value) return
  if (trendChart) trendChart.dispose()
  trendChart = echarts.init(trendRef.value)
}

const openDomain = async (d) => {
  domainDrawer.value = true
  domainDetail.value = d
  try {
    const res = await getVehicleDomainHealth(selectedVin.value, d.domainCode)
    domainDetail.value = res.data || d
  } catch (e) {
    console.error('加载域详情失败', e)
  }
}

const handleResize = () => trendChart?.resize()

onMounted(async () => {
  await loadVehicles()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  if (trendChart) {
    trendChart.dispose()
    trendChart = null
  }
})
</script>

<style scoped>
.health-header {
  display: flex;
  align-items: flex-start;
  gap: 32px;
  flex-wrap: wrap;
}

.vehicle-select {
  display: flex;
  align-items: center;
}

.health-overview {
  display: flex;
  gap: 32px;
  align-items: center;
  flex-wrap: wrap;
}

.overview-item.info {
  flex: 1;
  min-width: 320px;
}

.info-row {
  display: flex;
  gap: 8px;
  padding: 4px 0;
  font-size: 13px;
}

.info-row .label {
  color: #909399;
  width: 72px;
  text-align: right;
}

.domain-card {
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.domain-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.domain-card.status-danger {
  border-top: 3px solid #F56C6C;
}

.domain-card.status-warning {
  border-top: 3px solid #E6A23C;
}

.domain-card.status-normal {
  border-top: 3px solid #67C23A;
}

.domain-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.domain-name {
  font-weight: 600;
  font-size: 14px;
}

.domain-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
  margin: 4px 0 8px;
}

.component-list {
  border-top: 1px dashed #ebeef5;
  padding-top: 8px;
}

.component-row {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  padding: 2px 0;
}

.c-name {
  color: #606266;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 70%;
}

.c-score {
  font-weight: 600;
}

.empty-tip {
  color: #c0c4cc;
  font-size: 12px;
  text-align: center;
  padding: 4px 0;
}

.trend-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.trend-chart {
  width: 100%;
  height: 300px;
}
</style>
