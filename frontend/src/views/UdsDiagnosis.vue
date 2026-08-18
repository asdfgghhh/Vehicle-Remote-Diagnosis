<template>
  <div>
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

      <!-- 动态参数区 -->
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

      <el-button type="primary" @click="executeDiagnosis" :loading="executing">
        执行诊断
      </el-button>
      <el-button @click="readDtc" :loading="executing">读取 DTC (0x19)</el-button>
      <el-button @click="testerPresent" :loading="executing">测试在线 (0x3E)</el-button>
    </el-card>

    <!-- 诊断结果 -->
    <el-card v-if="lastResult">
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

    <!-- 诊断历史 -->
    <el-card style="margin-top: 16px">
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
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import api from '../utils/api'

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
    const request = {
      vin: udsForm.vin,
      ecuType: udsForm.ecuType,
      serviceId: parseInt(udsForm.serviceId),
      sessionType: udsForm.sessionType,
      resetType: udsForm.resetType,
      dataIdentifier: parseInt(udsForm.dataIdentifier, 16) || null,
      securityLevel: udsForm.securityLevel,
      routineId: parseInt(udsForm.routineId, 16) || null
    }
    const res = await api.post('/diagnosis/uds', request)
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
</script>
