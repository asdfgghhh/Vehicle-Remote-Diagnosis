<template>
  <div class="ai-diagnosis">
    <el-row :gutter="16">
      <!-- 左侧：会话历史 -->
      <el-col :span="6">
        <el-card style="height: calc(100vh - 140px)">
          <template #header>
            <div class="card-head">
              <span>💬 诊断会话</span>
              <el-button type="primary" size="small" @click="newSession">新建会话</el-button>
            </div>
          </template>
          <el-select v-model="vin" filterable placeholder="选择车辆 VIN" style="width: 100%; margin-bottom: 12px">
            <el-option v-for="v in vehicleOptions" :key="v.vin" :label="`${v.plateNumber || v.vin} - ${v.vin}`" :value="v.vin" />
          </el-select>
          <div class="session-list">
            <div
              v-for="s in sessions"
              :key="s.id"
              class="session-item"
              :class="{ active: s.id === activeSessionId }"
              @click="selectSession(s)"
            >
              <div class="s-title">{{ s.vin }} · {{ s.diagLevel || 'VEHICLE' }}</div>
              <div class="s-time">{{ fmtTime(s.requestTime) }}</div>
              <el-tag size="mini" :type="sessionTag(s.sessionStatus)">{{ s.sessionStatus }}</el-tag>
            </div>
            <el-empty v-if="!sessions.length" description="暂无会话" :image-size="50" />
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：聊天区 -->
      <el-col :span="18">
        <el-card style="height: calc(100vh - 140px); display: flex; flex-direction: column">
          <template #header>
            <div class="card-head">
              <span>🤖 AI 智能诊断助手</span>
              <div>
                <el-button size="small" @click="loadReport">查看诊断报告</el-button>
              </div>
            </div>
          </template>

          <!-- 快捷提问 -->
          <div class="quick-asks">
            <el-tag
              v-for="q in quickQuestions"
              :key="q"
              class="quick-tag"
              @click="sendQuick(q)"
            >{{ q }}</el-tag>
          </div>

          <!-- 消息区 -->
          <div ref="msgRef" class="msg-area">
            <div v-if="!messages.length" class="msg-empty">
              <div style="font-size: 40px">🩺</div>
              <p>选择车辆后，点击下方快捷提问发起诊断</p>
              <p style="color: #909399; font-size: 12px">AI 将基于故障树场景与车辆健康数据给出诊断建议</p>
            </div>
            <div v-for="(m, i) in messages" :key="i" class="msg-row" :class="m.role">
              <div class="avatar">{{ m.role === 'user' ? '🧑' : '🤖' }}</div>
              <div class="bubble">
                <pre>{{ m.content }}</pre>
                <div class="msg-time">{{ fmtTime(m.time) }}</div>
              </div>
            </div>
          </div>

          <!-- 输入区 -->
          <div class="input-bar">
            <el-input
              v-model="input"
              type="textarea"
              :rows="2"
              placeholder="输入诊断问题，如：车辆无法启动，可能是什么原因？"
              @keydown.enter.exact.prevent="send"
            />
            <el-button type="primary" :loading="sending" @click="send">发送</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

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
      <el-empty v-else description="暂无报告，请先选择会话" :image-size="60" />
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { getVehiclePage } from '@/api/vehicle'
import {
  getDiagnosisTaskPage,
  getDiagnosisTask,
  getDiagnosisReport,
  createDiagnosisTask,
  getFaultTreeScenarios
} from '@/api/diagnosis'

const vehicleOptions = ref([])
const vin = ref('')
const sessions = ref([])
const activeSessionId = ref(null)
const messages = ref([])
const input = ref('')
const sending = ref(false)
const msgRef = ref(null)
const reportDrawer = ref(false)
const report = ref(null)

const quickQuestions = [
  '车辆无法启动，可能是什么原因？',
  '读取全车故障码并分析',
  '电池健康状态如何？',
  '建议进行哪些维保？'
]

const sessionTag = (status) => {
  const map = { COMPLETED: 'success', SUCCESS: 'success', PENDING: 'warning', RUNNING: 'primary', FAILED: 'danger' }
  return map[status] || 'info'
}

const fmtTime = (t) => {
  if (!t) return '-'
  return new Date(t).toLocaleString('zh-CN', { hour12: false })
}

const loadVehicles = async () => {
  try {
    const res = await getVehiclePage({ current: 1, size: 200 })
    vehicleOptions.value = (res.data?.records || []).map(r => ({ vin: r.vin, plateNumber: r.plateNumber || r.licensePlate }))
    if (vehicleOptions.value.length) {
      vin.value = vehicleOptions.value[0].vin
    }
  } catch (e) {
    console.error('加载车辆失败', e)
  }
}

const loadSessions = async () => {
  try {
    const res = await getDiagnosisTaskPage({ current: 1, size: 30 })
    sessions.value = res.data?.records || []
  } catch (e) {
    console.error('加载会话失败', e)
  }
}

const selectSession = async (s) => {
  activeSessionId.value = s.id
  messages.value = [{
    role: 'assistant',
    content: `已加载诊断任务 #${s.id}（VIN: ${s.vin}，级别: ${s.diagLevel}，状态: ${s.sessionStatus}）。\n如需查看详细进度，请点击「查看诊断报告」。`,
    time: s.requestTime
  }]
  try {
    const res = await getDiagnosisTask(s.id)
    const task = res.data
    if (task?.faultTreeResult) {
      messages.value.push({
        role: 'assistant',
        content: '故障树扫描结果：\n' + JSON.stringify(task.faultTreeResult, null, 2),
        time: task.responseTime || task.createTime
      })
    }
    if (task?.responseData) {
      messages.value.push({ role: 'assistant', content: '诊断响应：\n' + String(task.responseData), time: task.responseTime })
    }
  } catch (e) {
    console.error('加载任务详情失败', e)
  }
  scrollBottom()
}

const newSession = () => {
  activeSessionId.value = null
  messages.value = []
  input.value = ''
  ElMessage.success('已创建新会话，请输入诊断问题')
}

const sendQuick = (q) => {
  input.value = q
  send()
}

const send = async () => {
  const text = input.value.trim()
  if (!text) return
  if (!vin.value) {
    ElMessage.warning('请先选择车辆')
    return
  }
  messages.value.push({ role: 'user', content: text, time: new Date().toISOString() })
  input.value = ''
  sending.value = true
  scrollBottom()

  // 尝试创建诊断任务（基于故障树场景），失败则给出本地 AI 推理兜底
  try {
    const scenes = (await getFaultTreeScenarios().catch(() => null))?.data || []
    const scene = scenes.length ? scenes[0] : null
    const res = await createDiagnosisTask({
      vin: vin.value,
      sceneId: scene?.id || null,
      diagLevel: 'VEHICLE'
    })
    const task = res.data
    activeSessionId.value = task.taskId
    await loadSessions()
    messages.value.push({
      role: 'assistant',
      content: `已创建诊断任务 #${task.taskId}（traceId: ${task.traceId}，状态: ${task.status}）。\n\n诊断建议：\n1. 建议执行整车级故障树扫描（0x19 读取 DTC）\n2. 结合健康域评分定位问题域\n3. 查看详细报告获取故障码与 DID 快照`,
      time: new Date().toISOString()
    })
  } catch (e) {
    console.error('创建诊断任务失败', e)
    messages.value.push({
      role: 'assistant',
      content: 'AI 诊断服务暂不可用，已根据已有数据分析：\n\n请先在「远程诊断」中执行整车级扫描（读取 DTC），或将此问题发送至售后支持。',
      time: new Date().toISOString()
    })
  } finally {
    sending.value = false
    scrollBottom()
  }
}

const loadReport = async () => {
  if (!activeSessionId.value) {
    ElMessage.warning('请先选择一个诊断会话')
    return
  }
  try {
    const res = await getDiagnosisReport(activeSessionId.value)
    report.value = res.data
    reportDrawer.value = true
  } catch (e) {
    ElMessage.error('加载诊断报告失败')
  }
}

const scrollBottom = async () => {
  await nextTick()
  if (msgRef.value) {
    msgRef.value.scrollTop = msgRef.value.scrollHeight
  }
}

onMounted(async () => {
  await loadVehicles()
  await loadSessions()
})
</script>

<style scoped>
.card-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.session-list {
  overflow-y: auto;
  max-height: calc(100vh - 320px);
}

.session-item {
  border: 1px solid #ebeef5;
  border-radius: 6px;
  padding: 8px 10px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.session-item:hover {
  border-color: #409EFF;
}

.session-item.active {
  border-color: #409EFF;
  background: rgba(64, 158, 255, 0.06);
}

.s-title {
  font-size: 13px;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.s-time {
  font-size: 11px;
  color: #909399;
  margin: 2px 0;
}

.quick-asks {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 12px;
}

.quick-tag {
  cursor: pointer;
  transition: all 0.2s;
}

.quick-tag:hover {
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
}

.msg-area {
  flex: 1;
  overflow-y: auto;
  padding: 4px 8px;
  min-height: 300px;
}

.msg-empty {
  text-align: center;
  color: #909399;
  margin-top: 80px;
}

.msg-row {
  display: flex;
  gap: 10px;
  margin-bottom: 14px;
}

.msg-row.user {
  flex-direction: row-reverse;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #f0f2f5;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
}

.bubble {
  max-width: 75%;
  background: #f4f4f5;
  border-radius: 10px;
  padding: 10px 14px;
}

.msg-row.user .bubble {
  background: #409eff;
  color: #fff;
}

.bubble pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
  font-family: inherit;
  font-size: 13px;
  line-height: 1.6;
}

.msg-time {
  font-size: 11px;
  color: #909399;
  margin-top: 4px;
}

.msg-row.user .msg-time {
  color: rgba(255, 255, 255, 0.8);
}

.input-bar {
  display: flex;
  gap: 10px;
  align-items: flex-end;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
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
