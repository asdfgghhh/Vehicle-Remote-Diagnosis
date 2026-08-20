<template>
  <div class="uds-diagnosis">
    <el-tabs v-model="activeTab">
      <!-- Tab1: 三级智能诊断 -->
      <el-tab-pane label="三级智能诊断" name="level">
        <el-card>
          <template #header><span>🧭 整车 / 域 / 部件 三级诊断</span></template>

          <el-form :model="levelForm" label-width="110px" inline>
            <el-form-item label="车辆 VIN">
              <el-select v-model="levelForm.vin" filterable placeholder="选择车辆" style="width: 260px">
                <el-option v-for="v in vehicleOptions" :key="v.vin" :label="`${v.plateNumber || v.vin} - ${v.vin}`" :value="v.vin" />
              </el-select>
            </el-form-item>
            <el-form-item label="故障树场景">
              <el-select v-model="levelForm.sceneCode" placeholder="选择场景（默认全车扫描）" clearable style="width: 240px">
                <el-option v-for="s in scenarios" :key="s.sceneCode" :label="`${s.sceneName} (AI ${(s.aiConfidence * 100).toFixed(0)}%)`" :value="s.sceneCode" />
              </el-select>
            </el-form-item>
            <el-form-item label="诊断级别">
              <el-select v-model="levelForm.diagLevel" style="width: 180px">
                <el-option v-for="l in levels" :key="l.code" :label="l.name" :value="l.code" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="creating" @click="createTask">发起诊断</el-button>
            </el-form-item>
          </el-form>

          <!-- 级别说明 -->
          <el-row :gutter="12" style="margin-top: 8px">
            <el-col v-for="l in levels" :key="l.code" :span="8">
              <div class="level-card">
                <div class="lv-head">
                  <b>{{ l.name }}</b>
                  <el-tag size="mini" type="info">{{ l.code }}</el-tag>
                </div>
                <div class="lv-desc">{{ l.description }}</div>
                <div class="lv-svc">支持服务: {{ (l.supportedServices || []).join(' / ') }}</div>
              </div>
            </el-col>
          </el-row>

          <!-- 任务进度 -->
          <el-card v-if="currentTask" shadow="never" style="margin-top: 16px; background: #f8fafc">
            <template #header>
              <span>⏳ 诊断任务 #{{ currentTask.id }}</span>
              <el-tag :type="taskTag(currentTask.sessionStatus)" style="margin-left: 12px">{{ currentTask.sessionStatus }}</el-tag>
            </template>
            <el-descriptions :column="3" size="small" border>
              <el-descriptions-item label="traceId">{{ currentTask.traceId }}</el-descriptions-item>
              <el-descriptions-item label="VIN">{{ currentTask.vin }}</el-descriptions-item>
              <el-descriptions-item label="级别">{{ currentTask.diagLevel }}</el-descriptions-item>
              <el-descriptions-item label="请求时间">{{ fmtTime(currentTask.requestTime) }}</el-descriptions-item>
              <el-descriptions-item label="响应时间">{{ fmtTime(currentTask.responseTime) }}</el-descriptions-item>
              <el-descriptions-item label="结果">{{ currentTask.success === 1 ? '成功' : currentTask.success === 0 ? '执行中' : '-' }}</el-descriptions-item>
            </el-descriptions>
            <div style="margin-top: 12px; text-align: right">
              <el-button size="small" @click="refreshTask">刷新进度</el-button>
              <el-button size="small" type="primary" @click="showReport">查看诊断报告</el-button>
            </div>
          </el-card>
        </el-card>
      </el-tab-pane>

      <!-- Tab2: UDS 服务执行 -->
      <el-tab-pane label="UDS 服务执行" name="uds">
        <el-card style="margin-bottom: 16px">
          <template #header><span>🔧 UDS 远程诊断 - ISO 14229</span></template>

          <el-form :model="udsForm" label-width="120px" inline>
            <el-form-item label="车辆 VIN">
              <el-input v-model="udsForm.vin" placeholder="如: LSVAA0000XXXXXXX" style="width: 200px" />
            </el-form-item>
            <el-form-item label="ECU 类型">
              <el-select v-model="udsForm.ecuType" placeholder="选择 ECU" style="width: 180px">
                <el-option label="EMS 发动机" value="EMS" />
                <el-option label="BCM 车身" value="BCM" />
                <el-option label="ABS 制动" value="ABS" />
                <el-option label="BMS 电池" value="BMS" />
                <el-option label="TCU 变速箱" value="TCU" />
                <el-option label="IC 仪表" value="IC" />
              </el-select>
            </el-form-item>
            <el-form-item label="诊断服务">
              <el-select v-model="udsForm.serviceId" placeholder="选择服务" style="width: 220px" @change="onServiceChange">
                <el-option v-for="svc in udsServices" :key="svc.id" :label="svc.label" :value="svc.id" />
              </el-select>
            </el-form-item>
          </el-form>

          <el-form :model="udsForm" label-width="120px" v-if="showExtraParams">
            <el-form-item v-if="udsForm.serviceId === '0x10'" label="会话类型">
              <el-select v-model="udsForm.sessionType" style="width: 200px">
                <el-option label="默认会话 (0x01)" :value="1" />
                <el-option label="编程会话 (0x02)" :value="2" />
                <el-option label="扩展会话 (0x03)" :value="3" />
              </el-select>
            </el-form-item>
            <el-form-item v-if="udsForm.serviceId === '0x11'" label="复位类型">
              <el-select v-model="udsForm.resetType" style="width: 200px">
                <el-option label="硬复位 (0x01)" :value="1" />
                <el-option label="钥匙复位 (0x02)" :value="2" />
                <el-option label="软复位 (0x03)" :value="3" />
              </el-select>
            </el-form-item>
            <el-form-item v-if="udsForm.serviceId === '0x22'" label="数据标识符 DID">
              <el-input v-model="udsForm.dataIdentifier" placeholder="如: F190 (VIN)" style="width: 200px" />
            </el-form-item>
            <el-form-item v-if="udsForm.serviceId === '0x27'" label="安全级别">
              <el-select v-model="udsForm.securityLevel" style="width: 200px">
                <el-option label="级别 1" :value="1" />
                <el-option label="级别 3" :value="3" />
                <el-option label="级别 5" :value="5" />
              </el-select>
            </el-form-item>
            <el-form-item v-if="udsForm.serviceId === '0x31'" label="例程 ID">
              <el-input v-model="udsForm.routineId" placeholder="如: E001" style="width: 200px" />
            </el-form-item>
          </el-form>

          <el-button type="primary" @click="executeDiagnosis" :loading="executing">执行诊断</el-button>
          <el-button @click="readDtc" :loading="executing">读取 DTC (0x19)</el-button>
          <el-button @click="testerPresent" :loading="executing">测试在线 (0x3E)</el-button>
        </el-card>

        <el-card v-if="lastResult" style="margin-bottom: 16px">
          <template #header>
            <span>📋 诊断结果</span>
            <el-tag :type="lastResult.success ? 'success' : 'danger'" style="margin-left: 12px">
              {{ lastResult.success ? '成功' : '失败' }}
            </el-tag>
            <span style="margin-left: 12px; color: #999">耗时: {{ lastResult.responseTimeMs }}ms</span>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="追踪 ID">{{ lastResult.traceId }}</el-descriptions-item>
            <el-descriptions-item label="VIN">{{ lastResult.vin }}</el-descriptions-item>
            <el-descriptions-item label="服务 ID">{{ lastResult.serviceIdHex }}</el-descriptions-item>
            <el-descriptions-item label="会话状态">{{ lastResult.sessionStatus || '-' }}</el-descriptions-item>
            <el-descriptions-item label="安全状态">{{ lastResult.securityStatus || '-' }}</el-descriptions-item>
            <el-descriptions-item label="否定响应码">
              {{ lastResult.negativeResponseCode ? '0x' + lastResult.negativeResponseCode.toString(16) : '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="响应数据" :span="2">
              <pre style="background: #f5f7fa; padding: 8px; border-radius: 4px; max-height: 200px; overflow: auto">{{ lastResult.responseData || '-' }}</pre>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <el-card>
          <template #header><span>📜 诊断历史</span></template>
          <el-table :data="sessionHistory" style="width: 100%" max-height="400">
            <el-table-column prop="traceId" label="追踪 ID" width="120" />
            <el-table-column label="服务" width="200">
              <template #default="{ row }">
                0x{{ row.serviceId?.toString(16)?.toUpperCase() }}
                {{ getServiceName(row.serviceId) }}
              </template>
            </el-table-column>
            <el-table-column prop="vin" label="VIN" width="160" />
            <el-table-column label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.success === 1 ? 'success' : 'danger'" size="small">
                  {{ row.success === 1 ? '成功' : '失败' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="requestTime" label="时间" width="180" />
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 诊断报告抽屉 -->
    <el-drawer v-model="reportDrawer" title="诊断报告" size="520px">
      <div v-if="report">
        <el-descriptions :column="1" border style="margin-bottom: 16px">
          <el-descriptions-item label="任务 ID">{{ report.taskId }}</el-descriptions-item>
          <el-descriptions-item label="VIN">{{ report.vin }}</el-descriptions-item>
          <el-descriptions-item label="诊断级别">{{ report.levelName || report.diagLevel }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="report.success === 1 ? 'success' : 'warning'">{{ report.status }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="开始时间">{{ fmtTime(report.startTime) }}</el-descriptions-item>
          <el-descriptions-item label="结束时间">{{ fmtTime(report.endTime) }}</el-descriptions-item>
        </el-descriptions>
        <h4>诊断结论</h4>
        <el-table :data="report.summary || []" size="small" stripe>
          <el-table-column prop="key" label="项目" width="110" />
          <el-table-column prop="value" label="内容" />
        </el-table>
        <h4 v-if="report.faultTree">故障树扫描结果</h4>
        <pre v-if="report.faultTree" class="report-json">{{ JSON.stringify(report.faultTree, null, 2) }}</pre>
      </div>
      <el-empty v-else description="暂无报告数据" :image-size="60" />
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getDiagnosisLevels,
  getFaultTreeScenarios,
  createDiagnosisTask,
  getDiagnosisTask,
  getDiagnosisReport,
  executeUds,
  getDiagnosisSessions
} from '@/api/diagnosis'
import { getVehiclePage } from '@/api/vehicle'

const activeTab = ref('level')

// ===== 三级诊断 =====
const vehicleOptions = ref([])
const levels = ref([])
const scenarios = ref([])
const levelForm = reactive({ vin: '', sceneCode: '', diagLevel: 'VEHICLE' })
const creating = ref(false)
const currentTask = ref(null)
const reportDrawer = ref(false)
const report = ref(null)

const taskTag = (status) => {
  const map = { COMPLETED: 'success', SUCCESS: 'success', PENDING: 'warning', RUNNING: 'primary', FAILED: 'danger' }
  return map[status] || 'info'
}

const fmtTime = (t) => {
  if (!t) return '-'
  return new Date(t).toLocaleString('zh-CN', { hour12: false })
}

const loadLevels = async () => {
  try {
    const res = await getDiagnosisLevels()
    levels.value = res.data?.levels || []
  } catch (e) {
    console.error('加载诊断级别失败', e)
  }
}

const loadScenarios = async () => {
  try {
    const res = await getFaultTreeScenarios()
    scenarios.value = res.data || []
  } catch (e) {
    console.error('加载故障树场景失败', e)
  }
}

const loadVehicles = async () => {
  try {
    const res = await getVehiclePage({ current: 1, size: 200 })
    vehicleOptions.value = (res.data?.records || []).map(r => ({ vin: r.vin, plateNumber: r.plateNumber || r.licensePlate }))
    if (vehicleOptions.value.length) {
      levelForm.vin = vehicleOptions.value[0].vin
    }
  } catch (e) {
    console.error('加载车辆列表失败', e)
  }
}

const createTask = async () => {
  if (!levelForm.vin) {
    ElMessage.warning('请选择车辆')
    return
  }
  creating.value = true
  try {
    const scene = scenarios.value.find(s => s.sceneCode === levelForm.sceneCode)
    const res = await createDiagnosisTask({
      vin: levelForm.vin,
      sceneId: scene?.id || null,
      diagLevel: levelForm.diagLevel
    })
    currentTask.value = { id: res.data.taskId, traceId: res.data.traceId, vin: res.data.vin, diagLevel: res.data.diagLevel, sessionStatus: res.data.status }
    ElMessage.success(`诊断任务已创建：#${res.data.taskId}`)
  } catch (e) {
    ElMessage.error('创建诊断任务失败')
    console.error(e)
  } finally {
    creating.value = false
  }
}

const refreshTask = async () => {
  if (!currentTask.value?.id) return
  try {
    const res = await getDiagnosisTask(currentTask.value.id)
    currentTask.value = res.data
  } catch (e) {
    console.error('刷新任务失败', e)
  }
}

const showReport = async () => {
  if (!currentTask.value?.id) {
    ElMessage.warning('请先发起诊断')
    return
  }
  try {
    const res = await getDiagnosisReport(currentTask.value.id)
    report.value = res.data
    reportDrawer.value = true
  } catch (e) {
    ElMessage.error('加载诊断报告失败')
  }
}

// ===== UDS 服务执行 =====
const udsForm = reactive({
  vin: '',
  ecuType: 'EMS',
  serviceId: '0x10',
  sessionType: 1,
  resetType: 1,
  dataIdentifier: 'F190',
  securityLevel: 1,
  routineId: 'E001'
})

const showExtraParams = ref(true)
const executing = ref(false)
const lastResult = ref(null)
const sessionHistory = ref([])

const udsServices = [
  { id: '0x10', label: '0x10 诊断会话控制' },
  { id: '0x11', label: '0x11 ECU 复位' },
  { id: '0x27', label: '0x27 安全访问' },
  { id: '0x22', label: '0x22 按 ID 读取数据' },
  { id: '0x2E', label: '0x2E 按 ID 写入数据' },
  { id: '0x19', label: '0x19 读取 DTC' },
  { id: '0x14', label: '0x14 清除诊断信息' },
  { id: '0x31', label: '0x31 例程控制' },
  { id: '0x23', label: '0x23 读内存' },
  { id: '0x3D', label: '0x3D 写内存' },
  { id: '0x2F', label: '0x2F IO 控制' },
  { id: '0x3E', label: '0x3E 测试在线' },
  { id: '0x34', label: '0x34 请求下载' },
  { id: '0x35', label: '0x35 请求上传' }
]

function onServiceChange() {
  showExtraParams.value = ['0x10', '0x11', '0x22', '0x27', '0x31'].includes(udsForm.serviceId)
}

function getServiceName(id) {
  const svc = udsServices.find(s => parseInt(s.id) === id)
  return svc ? svc.label.replace(/^0x\w+\s/, '') : ''
}

async function executeDiagnosis() {
  executing.value = true
  try {
    const req = {
      vin: udsForm.vin,
      ecuType: udsForm.ecuType,
      serviceId: parseInt(udsForm.serviceId),
      sessionType: udsForm.sessionType,
      resetType: udsForm.resetType,
      dataIdentifier: parseInt(udsForm.dataIdentifier, 16) || null,
      securityLevel: udsForm.securityLevel,
      routineId: parseInt(udsForm.routineId, 16) || null
    }
    const res = await executeUds(req)
    if (res.code === 200) {
      lastResult.value = {
        ...res.data,
        serviceIdHex: '0x' + res.data.serviceId?.toString(16).toUpperCase()
      }
    }
  } catch (e) {
    console.error('Diagnosis failed:', e)
  } finally {
    executing.value = false
  }
}

async function readDtc() {
  udsForm.serviceId = '0x19'
  await executeDiagnosis()
}

async function testerPresent() {
  udsForm.serviceId = '0x3E'
  await executeDiagnosis()
}

const loadSessions = async () => {
  try {
    const res = await getDiagnosisSessions()
    sessionHistory.value = Array.isArray(res.data) ? res.data : (res.data?.records || [])
  } catch (e) {
    console.error('加载诊断历史失败', e)
  }
}

onMounted(() => {
  loadLevels()
  loadScenarios()
  loadVehicles()
  loadSessions()
})
</script>

<style scoped>
.level-card {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 12px;
  background: #fafbfc;
  height: 100%;
}

.lv-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.lv-desc {
  font-size: 12px;
  color: #606266;
  line-height: 1.6;
  margin-bottom: 6px;
}

.lv-svc {
  font-size: 11px;
  color: #909399;
}

.report-json {
  background: #f5f7fa;
  border-radius: 6px;
  padding: 10px;
  max-height: 400px;
  overflow: auto;
  font-size: 12px;
}
</style>
