<template>
  <div class="maintenance">
    <!-- 页面头部 -->
    <div class="page-title">智能维保</div>
    <div class="page-desc">基于 VHR 数据的预测性维护 · 从「坏了再修」到「提前预防」</div>
    <!-- 车辆选择 -->
    <el-card style="margin-bottom: 16px">
      <div class="maint-head">
        <el-select v-model="vin" filterable placeholder="选择车辆（VIN）" style="width: 320px" @change="loadData">
          <el-option v-for="v in vehicleOptions" :key="v.vin" :label="`${v.plateNumber || v.vin} - ${v.vin}`" :value="v.vin" />
        </el-select>
        <el-button style="margin-left: 8px" @click="loadData">刷新</el-button>
        <span v-if="health" class="soh-tip">电池 SOH：<b>{{ health.batterySoh ?? '-' }}%</b></span>
      </div>
    </el-card>

    <el-row v-if="health" :gutter="16">
      <!-- 左侧：电池 SOH + 健康概览 -->
      <el-col :span="8">
        <el-card>
          <template #header><span>🔋 电池健康</span></template>
          <div class="soh-box">
            <el-progress type="dashboard" :percentage="health.healthScore || 0" :width="140" :color="scoreColor(health.healthScore)" />
            <div class="soh-info">
              <div>整车健康分 <b>{{ health.healthScore }}</b></div>
              <div>电池 SOH <b>{{ health.batterySoh ?? '-' }}%</b></div>
              <div>风险等级
                <el-tag :type="riskTagType(health.riskLevel)" size="small">{{ health.riskLevel }}</el-tag>
              </div>
              <div>最近在线 {{ fmtTime(health.lastOnlineTime) }}</div>
            </div>
          </div>
          <el-divider />
          <h4>七大域健康</h4>
          <div v-for="d in health.domains" :key="d.domainCode" class="domain-row">
            <span class="d-name">{{ d.domainName }}</span>
            <el-progress :percentage="d.healthScore || 0" :color="scoreColor(d.healthScore)" :stroke-width="10" style="flex: 1" />
            <span class="d-score">{{ d.healthScore }}</span>
          </div>
        </el-card>
      </el-col>

      <!-- 中间：维保建议 -->
      <el-col :span="8">
        <el-card>
          <template #header><span>🛠️ 维保建议</span></template>
          <div v-for="(item, i) in suggestions" :key="i" class="suggest-item">
            <div class="s-head">
              <el-tag :type="item.type" size="small">{{ item.tag }}</el-tag>
              <b>{{ item.title }}</b>
            </div>
            <div class="s-desc">{{ item.desc }}</div>
            <div class="s-meta">建议时间：{{ item.due }} · 预估里程 {{ item.mileage }}</div>
          </div>
          <el-empty v-if="!suggestions.length" description="暂无维保建议" :image-size="60" />
        </el-card>
      </el-col>

      <!-- 右侧：保养记录时间线 -->
      <el-col :span="8">
        <el-card>
          <template #header><span>📅 保养计划时间线</span></template>
          <el-timeline v-if="(standby?.maintenanceRecords || []).length">
            <el-timeline-item
              v-for="m in standby.maintenanceRecords"
              :key="m.id"
              :timestamp="m.recordDate || ''"
              :type="m.recordType === 1 ? 'primary' : m.recordType === 2 ? 'warning' : 'success'"
            >
              <b>{{ m.title }}</b>
              <div class="tl-text">{{ m.content }}</div>
              <div class="tl-text">里程 {{ m.mileage || '-' }} km · {{ m.operator || '-' }} · 费用 ¥{{ m.cost || 0 }}</div>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无保养记录" :image-size="60" />
        </el-card>
      </el-col>
    </el-row>

    <el-card v-if="!health && !loading" style="margin-top: 16px">
      <el-empty description="选择车辆查看智能维保信息" :image-size="80" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import dayjs from 'dayjs'
import { getVehicleHealth } from '@/api/health'
import { getFaultStandby } from '@/api/fault'
import { getVehiclePage } from '@/api/vehicle'

const vehicleOptions = ref([])
const vin = ref('')
const health = ref(null)
const standby = ref(null)
const loading = ref(false)

const scoreColor = (score) => {
  if (score == null) return '#909399'
  if (score >= 90) return '#67C23A'
  if (score >= 75) return '#409EFF'
  if (score >= 60) return '#E6A23C'
  return '#F56C6C'
}

const riskTagType = (level) => ({ LOW: 'success', MEDIUM: 'warning', HIGH: 'danger' }[level] || 'info')

const fmtTime = (t) => {
  if (!t) return '-'
  return new Date(t).toLocaleString('zh-CN', { hour12: false })
}

const suggestions = computed(() => {
  const list = []
  const h = health.value
  if (!h) return list
  const score = h.healthScore || 0
  const soh = h.batterySoh

  if (soh != null && soh < 80) {
    list.push({ tag: '三电', type: 'danger', title: '电池健康度偏低', desc: `电池 SOH ${soh}%，建议预约 4S 店做电池容量标定与均衡维护。`, due: '1 周内', mileage: '—' })
  } else if (soh != null && soh < 90) {
    list.push({ tag: '三电', type: 'warning', title: '电池健康度下降', desc: `电池 SOH ${soh}%，建议关注充电效率与续航衰减趋势。`, due: '1 个月内', mileage: '—' })
  }
  if (score < 60) {
    list.push({ tag: '整车', type: 'danger', title: '整车健康分过低', desc: `健康分 ${score}，建议立即进店全面检测，重点排查 DANGER 域。`, due: '尽快', mileage: '—' })
  } else if (score < 75) {
    list.push({ tag: '整车', type: 'warning', title: '整车健康分预警', desc: `健康分 ${score}，建议 2 周内安排保养与故障复检。`, due: '2 周内', mileage: '—' })
  }
  if (h.domains?.some(d => d.status === 'WARNING' || d.status === 'DANGER')) {
    const bad = h.domains.filter(d => d.status === 'WARNING' || d.status === 'DANGER').map(d => d.domainName).join('、')
    list.push({ tag: '域', type: 'warning', title: `${bad} 存在异常`, desc: `请结合故障分析页定位具体部件，安排远程诊断或进店检测。`, due: '1 个月内', mileage: '—' })
  }
  const records = standby.value?.maintenanceRecords || []
  if (records.length) {
    const last = records[0]
    if (last.mileage != null) {
      list.push({ tag: '周期', type: 'info', title: '下次保养提示', desc: `上次保养里程 ${last.mileage} km，建议按保养手册在后续 5000-10000 km 内进行下次保养。`, due: dayjs(last.recordDate).add(6, 'month').format('YYYY-MM-DD'), mileage: `${last.mileage + 10000} km` })
    }
  } else {
    list.push({ tag: '周期', type: 'info', title: '首保提醒', desc: '暂无保养记录，新车建议 5000 km / 6 个月内完成首次保养。', due: dayjs().add(6, 'month').format('YYYY-MM-DD'), mileage: '5000 km' })
  }
  return list
})

const loadVehicles = async () => {
  try {
    const res = await getVehiclePage({ current: 1, size: 200 })
    vehicleOptions.value = (res.data?.records || []).map(r => ({ vin: r.vin, plateNumber: r.plateNumber || r.licensePlate }))
    if (vehicleOptions.value.length) {
      vin.value = vehicleOptions.value[0].vin
      await loadData()
    }
  } catch (e) {
    console.error('加载车辆列表失败', e)
  }
}

const loadData = async () => {
  if (!vin.value) return
  loading.value = true
  try {
    const [h, s] = await Promise.all([getVehicleHealth(vin.value), getFaultStandby(vin.value)])
    health.value = h.data
    standby.value = s.data
  } catch (e) {
    console.error('加载维保数据失败', e)
  } finally {
    loading.value = false
  }
}

onMounted(loadVehicles)
</script>

<style scoped>
.maint-head {
  display: flex;
  align-items: center;
}

.soh-tip {
  margin-left: 24px;
  font-size: 14px;
  color: #606266;
}

.soh-box {
  display: flex;
  gap: 24px;
  align-items: center;
}

.soh-info {
  font-size: 13px;
  color: #606266;
  line-height: 2;
}

.domain-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.d-name {
  width: 60px;
  font-size: 12px;
  color: #606266;
}

.d-score {
  width: 32px;
  text-align: right;
  font-weight: 600;
  color: #333;
}

.suggest-item {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 10px 12px;
  margin-bottom: 10px;
}

.s-head {
  display: flex;
  gap: 8px;
  align-items: center;
  font-size: 13px;
}

.s-desc {
  font-size: 12px;
  color: #606266;
  margin: 6px 0 2px;
  line-height: 1.6;
}

.s-meta {
  font-size: 12px;
  color: #909399;
}

.tl-text {
  font-size: 12px;
  color: #909399;
}
</style>
