<template>
  <div>
    <el-card style="margin-bottom: 16px">
      <template #header>
        <span>🔔 告警规则管理</span>
        <el-button type="primary" size="small" @click="openCreateDialog" style="float: right">+ 新建规则</el-button>
      </template>

      <el-alert
        title="规则引擎说明"
        type="info"
        :closable="false"
        style="margin-bottom: 16px"
      >
        支持三种规则类型：<strong>阈值规则</strong>（上下限阈值 + 防抖计数）、<strong>趋势规则</strong>（上升/下降趋势 + 变化率）、<strong>组合规则</strong>（MVEL 表达式）。规则启用后，信号数据到达时自动评估。
      </el-alert>

      <el-table :data="rules" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="50" />
        <el-table-column prop="ruleName" label="规则名称" min-width="160" />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="ruleTypeTag(row.ruleType)" size="small">{{ ruleTypeText(row.ruleType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="signalName" label="监测信号" width="150" />
        <el-table-column label="阈值/条件" min-width="200">
          <template #default="{ row }">
            <template v-if="row.ruleType === 'THRESHOLD'">
              上限: {{ row.upperThreshold || '-' }}, 下限: {{ row.lowerThreshold || '-' }},
              连续 {{ row.consecutiveCount }} 次
            </template>
            <template v-else-if="row.ruleType === 'TREND'">
              {{ row.trendDirection === 'UP' ? '上升' : '下降' }} &gt; {{ row.trendChangeRate }}%
            </template>
            <template v-else>
              {{ row.conditionExpr }}
            </template>
          </template>
        </el-table-column>
        <el-table-column label="级别" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="levelType(row.alertLevel)" size="small">{{ levelText(row.alertLevel) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.status" :active-value="1" :inactive-value="0"
              @change="toggleRule(row)" size="small" />
          </template>
        </el-table-column>
        <el-table-column label="冷却" width="80" align="center">
          <template #default="{ row }">{{ row.cooldownSec }}s</template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="editRule(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteRule(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新建/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEditing ? '编辑规则' : '新建规则'" width="650px">
      <el-form :model="form" label-width="130px">
        <el-form-item label="规则名称" required>
          <el-input v-model="form.ruleName" placeholder="如: 发动机水温过高" />
        </el-form-item>
        <el-form-item label="规则类型" required>
          <el-select v-model="form.ruleType" style="width: 100%" @change="onRuleTypeChange">
            <el-option label="阈值规则 (THRESHOLD)" value="THRESHOLD" />
            <el-option label="趋势规则 (TREND)" value="TREND" />
            <el-option label="组合规则 (COMBINATION)" value="COMBINATION" />
          </el-select>
        </el-form-item>
        <el-form-item label="监测信号" required>
          <el-input v-model="form.signalName" placeholder="如: EngineCoolantTemp" />
        </el-form-item>
        <el-form-item label="告警级别" required>
          <el-radio-group v-model="form.alertLevel">
            <el-radio :value="1">严重</el-radio>
            <el-radio :value="2">警告</el-radio>
            <el-radio :value="3">提示</el-radio>
          </el-radio-group>
        </el-form-item>

        <template v-if="form.ruleType === 'THRESHOLD'">
          <el-form-item label="阈值上限">
            <el-input-number v-model="form.upperThreshold" :precision="2" style="width: 100%" />
          </el-form-item>
          <el-form-item label="阈值下限">
            <el-input-number v-model="form.lowerThreshold" :precision="2" style="width: 100%" />
          </el-form-item>
          <el-form-item label="连续超阈次数">
            <el-input-number v-model="form.consecutiveCount" :min="1" :max="10" />
            <span style="margin-left: 8px; color: #999">连续超阈 N 次才触发，防抖</span>
          </el-form-item>
        </template>

        <template v-if="form.ruleType === 'TREND'">
          <el-form-item label="趋势方向">
            <el-radio-group v-model="form.trendDirection">
              <el-radio value="UP">上升</el-radio>
              <el-radio value="DOWN">下降</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="变化率阈值 (%)">
            <el-input-number v-model="form.trendChangeRate" :min="0" :max="100" :precision="1" />
          </el-form-item>
          <el-form-item label="趋势窗口 (秒)">
            <el-input-number v-model="form.trendWindowSec" :min="10" :max="3600" />
          </el-form-item>
        </template>

        <template v-if="form.ruleType === 'COMBINATION'">
          <el-form-item label="条件表达式 (MVEL)">
            <el-input v-model="form.conditionExpr" type="textarea" :rows="3"
              placeholder="如: signal.currentValue > 100 && signal.trendChangeRate > 10" />
          </el-form-item>
        </template>

        <el-form-item label="告警消息模板">
          <el-input v-model="form.alertMessage" placeholder="支持 {signalName} {currentValue} {threshold} 等变量" />
        </el-form-item>
        <el-form-item label="部件代码">
          <el-input v-model="form.componentCode" placeholder="如: EMS, BCM" />
        </el-form-item>
        <el-form-item label="冷却时间 (秒)">
          <el-input-number v-model="form.cooldownSec" :min="10" :max="3600" />
        </el-form-item>
        <el-form-item label="优先级">
          <el-input-number v-model="form.priority" :min="1" :max="999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveRule">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import api from '../utils/api'

const loading = ref(false)
const rules = ref([])

const dialogVisible = ref(false)
const isEditing = ref(false)
const editingId = ref(null)

const form = reactive({
  ruleName: '',
  ruleType: 'THRESHOLD',
  signalName: '',
  upperThreshold: null,
  lowerThreshold: null,
  consecutiveCount: 1,
  trendDirection: 'UP',
  trendChangeRate: 10,
  trendWindowSec: 60,
  conditionExpr: '',
  alertLevel: 2,
  alertMessage: '',
  componentCode: '',
  cooldownSec: 60,
  priority: 100
})

onMounted(() => fetchRules())

async function fetchRules() {
  loading.value = true
  try {
    const res = await api.get('/vehicle/alert/rules')
    if (res?.code === 200) rules.value = res.data?.records || []
  } finally {
    loading.value = false
  }
}

function onRuleTypeChange() {
  form.upperThreshold = null
  form.lowerThreshold = null
  form.trendChangeRate = 10
  form.conditionExpr = ''
}

function openCreateDialog() {
  isEditing.value = false
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

function editRule(row) {
  isEditing.value = true
  editingId.value = row.id
  Object.assign(form, {
    ruleName: row.ruleName,
    ruleType: row.ruleType,
    signalName: row.signalName,
    upperThreshold: row.upperThreshold,
    lowerThreshold: row.lowerThreshold,
    consecutiveCount: row.consecutiveCount || 1,
    trendDirection: row.trendDirection || 'UP',
    trendChangeRate: row.trendChangeRate || 10,
    trendWindowSec: row.trendWindowSec || 60,
    conditionExpr: row.conditionExpr || '',
    alertLevel: row.alertLevel,
    alertMessage: row.alertMessage || '',
    componentCode: row.componentCode || '',
    cooldownSec: row.cooldownSec || 60,
    priority: row.priority || 100
  })
  dialogVisible.value = true
}

async function saveRule() {
  try {
    if (isEditing.value) {
      await api.put(`/vehicle/alert/rules/${editingId.value}`, { ...form })
    } else {
      await api.post('/vehicle/alert/rules', { ...form })
    }
    dialogVisible.value = false
    fetchRules()
    ElMessage.success('保存成功')
  } catch (e) {
    console.error('Save failed:', e)
  }
}

async function deleteRule(row) {
  await api.delete(`/vehicle/alert/rules/${row.id}`)
  fetchRules()
  ElMessage.success('删除成功')
}

async function toggleRule(row) {
  await api.put(`/vehicle/alert/rules/${row.id}/status?status=${row.status}`)
  ElMessage.success(row.status === 1 ? '已启用' : '已禁用')
}

function resetForm() {
  Object.assign(form, {
    ruleName: '',
    ruleType: 'THRESHOLD',
    signalName: '',
    upperThreshold: null,
    lowerThreshold: null,
    consecutiveCount: 1,
    trendDirection: 'UP',
    trendChangeRate: 10,
    trendWindowSec: 60,
    conditionExpr: '',
    alertLevel: 2,
    alertMessage: '',
    componentCode: '',
    cooldownSec: 60,
    priority: 100
  })
}

function ruleTypeTag(type) {
  return { THRESHOLD: 'warning', TREND: 'primary', COMBINATION: 'success' }[type] || 'info'
}
function ruleTypeText(type) {
  return { THRESHOLD: '阈值', TREND: '趋势', COMBINATION: '组合' }[type] || type
}
function levelType(level) {
  return { 1: 'danger', 2: 'warning', 3: 'info' }[level] || 'info'
}
function levelText(level) {
  return { 1: '严重', 2: '警告', 3: '提示' }[level] || '未知'
}

const ElMessage = {
  success(msg) { if (window.$message) window.$message.success(msg) }
}
</script>
