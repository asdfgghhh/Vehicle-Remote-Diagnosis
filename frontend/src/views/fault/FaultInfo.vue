<template>
  <div class="fault-info">
    <!-- 页面头部 -->
    <div class="page-title">故障信息</div>
    <div class="page-desc">VHR 故障树场景化诊断 · 智能值守上下文关联 · 按级别/车型/VIN 筛选</div>
    <!-- 智能值守上下文 -->
    <el-card style="margin-bottom: 16px">
      <template #header>
        <div class="card-head">
          <span>🤖 智能值守</span>
          <div class="vin-select">
            <el-select
              v-model="standbyVin"
              filterable
              placeholder="选择车辆（VIN）"
              style="width: 300px"
              @change="loadStandby"
            >
              <el-option v-for="v in vehicleOptions" :key="v.vin" :label="`${v.plateNumber || v.vin} - ${v.vin}`" :value="v.vin" />
            </el-select>
            <el-button size="small" style="margin-left: 8px" @click="loadStandby">刷新</el-button>
          </div>
        </div>
      </template>

      <div v-if="standby" class="standby-overview">
        <div class="standby-item">
          <div class="sb-label">健康分</div>
          <b :style="{ color: scoreTextColor(standby.healthScore) }" class="sb-value">{{ standby.healthScore ?? '-' }}</b>
          <el-tag :type="riskTagType(standby.riskLevel)" size="small">{{ standby.riskLevel || '-' }}</el-tag>
        </div>
        <div class="standby-item">
          <div class="sb-label">未处理故障</div>
          <b style="color: #F56C6C" class="sb-value">{{ standby.pendingFaultCount ?? 0 }}</b>
        </div>
        <div class="standby-item">
          <div class="sb-label">车型</div>
          <span class="sb-text">{{ standby.modelName || '-' }}</span>
        </div>
        <div class="standby-item">
          <div class="sb-label">车牌号</div>
          <span class="sb-text">{{ standby.plateNumber || '-' }}</span>
        </div>
      </div>

      <el-row v-if="standby" :gutter="16" style="margin-top: 8px">
        <el-col :span="8">
          <h4 class="sub-title">近 90 天维保记录</h4>
          <el-timeline v-if="(standby.maintenanceRecords || []).length">
            <el-timeline-item
              v-for="m in standby.maintenanceRecords"
              :key="m.id"
              :timestamp="m.recordDate || ''"
              :type="m.recordType === 1 ? 'primary' : m.recordType === 2 ? 'warning' : 'success'"
              size="small"
            >
              <b>{{ m.title }}</b>
              <div class="tl-text">{{ m.content }}</div>
              <div class="tl-text">里程: {{ m.mileage || '-' }} km · {{ m.operator || '' }}</div>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无维保记录" :image-size="50" />
        </el-col>

        <el-col :span="8">
          <h4 class="sub-title">未处理故障</h4>
          <el-table :data="standby.pendingFaults || []" size="small" stripe max-height="240">
            <el-table-column prop="faultCode" label="故障码" width="90" />
            <el-table-column prop="faultName" label="描述" show-overflow-tooltip />
            <el-table-column label="状态" width="70">
              <template #default="{ row }">
                <el-tag :type="row.status === 0 ? 'danger' : 'success'" size="small">{{ row.status === 0 ? '未处理' : '已处理' }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!(standby.pendingFaults || []).length" description="无未处理故障" :image-size="50" />
        </el-col>

        <el-col :span="8">
          <h4 class="sub-title">AI 推荐处理优先级</h4>
          <div v-for="p in (standby.aiPriority || [])" :key="p.priority" class="ai-priority">
            <div class="pri-head">
              <el-tag :type="p.priority === 1 ? 'danger' : p.priority === 2 ? 'warning' : 'info'" size="small">P{{ p.priority }}</el-tag>
              <b>{{ p.sceneName }}</b>
            </div>
            <div class="pri-reason">{{ p.reason }}</div>
            <div class="pri-action">建议: {{ p.action }}</div>
          </div>
          <el-empty v-if="!(standby.aiPriority || []).length" description="暂无推荐" :image-size="50" />
        </el-col>
      </el-row>
    </el-card>

    <!-- 故障树场景 -->
    <el-card style="margin-bottom: 16px">
      <template #header><span>🌳 故障树场景</span></template>
      <el-row :gutter="12">
        <el-col v-for="s in scenes" :key="s.id || s.sceneCode" :span="6" style="margin-bottom: 12px">
          <div class="scene-card" :class="{ active: query.sceneId === s.id }" @click="toggleScene(s)">
            <div class="scene-name">{{ s.sceneName }}</div>
            <div class="scene-desc">{{ s.description }}</div>
            <div class="scene-meta">
              <el-tag size="mini" :type="(s.priority || 0) > 0 ? 'warning' : 'info'">优先级 {{ s.priority }}</el-tag>
              <span v-if="s.aiConfidence" class="conf">AI {{ (s.aiConfidence * 100).toFixed(0) }}%</span>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 故障列表 -->
    <el-card>
      <template #header>
        <div class="card-head">
          <span>📋 故障列表</span>
          <div class="filter-bar">
            <el-input v-model="query.vin" placeholder="VIN" clearable style="width: 180px" />
            <el-input v-model="query.faultCode" placeholder="故障码" clearable style="width: 120px" />
            <el-select v-model="query.level" placeholder="级别" clearable style="width: 110px">
              <el-option label="致命" value="FATAL" />
              <el-option label="严重" value="MAJOR" />
              <el-option label="一般" value="MINOR" />
            </el-select>
            <el-select v-model="query.status" placeholder="状态" clearable style="width: 110px">
              <el-option label="未处理" :value="0" />
              <el-option label="已处理" :value="1" />
            </el-select>
            <el-button type="primary" size="default" @click="searchFaults">查询</el-button>
          </div>
        </div>
      </template>

      <el-table :data="faultRecords" stripe>
        <el-table-column prop="faultCode" label="故障码" width="100" />
        <el-table-column prop="faultName" label="故障描述" show-overflow-tooltip />
        <el-table-column prop="componentCode" label="部件" width="110" />
        <el-table-column label="级别" width="80">
          <template #default="{ row }">
            <el-tag :type="levelTag(row.level)" size="small">{{ row.level || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="场景" width="130">
          <template #default="{ row }">{{ sceneName(row.sceneId) }}</template>
        </el-table-column>
        <el-table-column prop="vin" label="VIN" width="170" show-overflow-tooltip />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'danger' : 'success'" size="small">{{ row.status === 0 ? '未处理' : '已处理' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="faultTime" label="发生时间" width="170" />
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" type="primary" size="small" @click="doDiagnose(row)">一键诊断</el-button>
            <span v-else style="color: #c0c4cc">已处理</span>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="query.current"
        v-model:page-size="query.size"
        :total="total"
        layout="total, prev, pager, next, sizes"
        :page-sizes="[10, 20, 50]"
        style="margin-top: 12px; justify-content: flex-end"
        @change="loadFaults"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getFaultPage, getFaultStandby, getFaultSceneList, diagnoseFault } from '@/api/fault'
import { getVehiclePage } from '@/api/vehicle'

const vehicleOptions = ref([])
const standbyVin = ref('')
const standby = ref(null)
const scenes = ref([])
const faultRecords = ref([])
const total = ref(0)
const query = reactive({ current: 1, size: 10, vin: '', faultCode: '', level: '', status: null, sceneId: null })

const scoreTextColor = (score) => {
  if (score == null) return '#909399'
  if (score >= 90) return '#67C23A'
  if (score >= 75) return '#409EFF'
  if (score >= 60) return '#E6A23C'
  return '#F56C6C'
}

const riskTagType = (level) => ({ LOW: 'success', MEDIUM: 'warning', HIGH: 'danger' }[level] || 'info')
const levelTag = (level) => ({ FATAL: 'danger', MAJOR: 'warning', MINOR: 'info' }[level] || 'info')

const loadVehicles = async () => {
  try {
    const res = await getVehiclePage({ current: 1, size: 200 })
    const records = res.data?.records || []
    vehicleOptions.value = records.map(r => ({ vin: r.vin, plateNumber: r.plateNumber || r.licensePlate }))
    if (records.length) {
      standbyVin.value = records[0].vin
      query.vin = records[0].vin
      await Promise.all([loadStandby(), loadFaults()])
    }
  } catch (e) {
    console.error('加载车辆列表失败', e)
  }
}

const loadStandby = async () => {
  if (!standbyVin.value) return
  try {
    const res = await getFaultStandby(standbyVin.value)
    standby.value = res.data
  } catch (e) {
    console.error('加载智能值守失败', e)
  }
}

const loadScenes = async () => {
  try {
    const res = await getFaultSceneList()
    scenes.value = res.data || []
  } catch (e) {
    console.error('加载故障树场景失败', e)
  }
}

const loadFaults = async () => {
  try {
    const res = await getFaultPage({
      current: query.current,
      size: query.size,
      vin: query.vin || undefined,
      faultCode: query.faultCode || undefined,
      level: query.level || undefined,
      status: query.status ?? undefined,
      sceneId: query.sceneId ?? undefined
    })
    faultRecords.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {
    console.error('加载故障列表失败', e)
  }
}

const searchFaults = () => {
  query.current = 1
  loadFaults()
}

const toggleScene = (s) => {
  const sceneId = s.id
  query.sceneId = query.sceneId === sceneId ? null : sceneId
  searchFaults()
}

const sceneName = (sceneId) => {
  if (sceneId == null) return '-'
  const s = scenes.value.find(x => x.id === sceneId || x.sceneCode === String(sceneId))
  return s ? s.sceneName : `场景${sceneId}`
}

const doDiagnose = async (row) => {
  try {
    await ElMessageBox.confirm(
      `将针对故障「${row.faultName || row.faultCode}」发起远程诊断（整车级），是否继续？`,
      '一键远程诊断',
      { confirmButtonText: '发起诊断', cancelButtonText: '取消', type: 'warning' }
    )
  } catch (e) {
    return
  }
  try {
    const res = await diagnoseFault(row.id)
    ElMessage.success(`诊断任务已创建（taskId: ${res.data}）`)
  } catch (e) {
    ElMessage.error('发起诊断失败')
  }
}

onMounted(() => {
  loadScenes()
  loadVehicles()
})
</script>

<style scoped>
.card-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.vin-select {
  display: flex;
  align-items: center;
}

.standby-overview {
  display: flex;
  gap: 40px;
  padding: 8px 0;
  flex-wrap: wrap;
}

.standby-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.sb-label {
  color: #909399;
  font-size: 13px;
}

.sb-value {
  font-size: 20px;
}

.sb-text {
  font-size: 13px;
}

.sub-title {
  margin: 8px 0 12px;
  color: #606266;
}

.tl-text {
  font-size: 12px;
  color: #909399;
}

.ai-priority {
  border: 1px solid #ebeef5;
  border-radius: 6px;
  padding: 8px 10px;
  margin-bottom: 8px;
  background: #fafafa;
}

.pri-head {
  display: flex;
  gap: 6px;
  align-items: center;
}

.pri-reason {
  font-size: 12px;
  color: #606266;
  margin-top: 4px;
}

.pri-action {
  font-size: 12px;
  color: #409EFF;
  margin-top: 2px;
}

.scene-card {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 10px 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.scene-card:hover {
  border-color: #409EFF;
}

.scene-card.active {
  border-color: #409EFF;
  background: rgba(64, 158, 255, 0.06);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.15);
}

.scene-name {
  font-weight: 600;
  font-size: 13px;
}

.scene-desc {
  font-size: 12px;
  color: #909399;
  margin: 4px 0;
  min-height: 32px;
}

.scene-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.conf {
  font-size: 12px;
  color: #E6A23C;
}

.filter-bar {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
</style>
