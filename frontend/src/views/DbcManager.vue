<template>
  <div>
    <el-card style="margin-bottom: 16px">
      <template #header>
        <span>📊 DBC 文件管理</span>
        <el-tag :type="cantoolsAvailable ? 'success' : 'warning'" style="margin-left: 12px">
          Cantools: {{ cantoolsAvailable ? '可用' : '不可用（已降级为正则解析）' }}
        </el-tag>
      </template>

      <el-row :gutter="16">
        <el-col :span="12">
          <el-select
            v-model="selectedModelId"
            placeholder="请先选择车型（上传必填）"
            style="width: 260px; margin-right: 12px"
            filterable
            clearable
          >
            <el-option
              v-for="m in vehicleModels"
              :key="m.id"
              :label="m.modelName"
              :value="m.id"
            />
          </el-select>
          <el-upload
            ref="uploadRef"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :data="uploadData"
            :before-upload="beforeUpload"
            :on-success="onUploadSuccess"
            :on-error="onUploadError"
            accept=".dbc"
            :limit="1"
          >
            <el-button type="primary" :disabled="!selectedModelId">上传 DBC 文件</el-button>
            <template #tip>
              <div style="color: #999; margin-top: 8px">
                支持 .dbc 格式文件，必须先选择车型后再上传
              </div>
            </template>
          </el-upload>
        </el-col>
        <el-col :span="12" style="text-align: right">
          <el-input v-model="searchKeyword" placeholder="搜索文件名" style="width: 250px" clearable>
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
        </el-col>
      </el-row>
    </el-card>

    <!-- DBC 文件列表 -->
    <el-table :data="dbcFiles" style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="fileName" label="文件名" min-width="200" />
      <el-table-column prop="modelName" label="车型" width="120" />
      <el-table-column prop="version" label="版本" width="100" />
      <el-table-column prop="messageCount" label="消息数" width="80" align="center" />
      <el-table-column prop="signalCount" label="信号数" width="80" align="center" />
      <el-table-column prop="fileSize" label="大小" width="80" align="center">
        <template #default="{ row }">
          {{ formatFileSize(row.fileSize) }}
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="上传时间" width="160" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="viewDetails(row)">详情</el-button>
          <el-button size="small" type="success" @click="publishDbc(row)" v-if="row.status !== 2">发布</el-button>
          <el-button size="small" type="danger" @click="deleteDbc(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="currentPage"
      :total="total"
      :page-size="pageSize"
      layout="total, prev, pager, next"
      style="margin-top: 16px; justify-content: center"
      @current-change="fetchDbcFiles"
    />

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="DBC 信号详情" width="70%">
      <el-table :data="signalDetails" style="width: 100%" max-height="500" stripe>
        <el-table-column prop="name" label="信号名" width="150" />
        <el-table-column prop="messageName" label="所属消息" width="150" />
        <el-table-column prop="startBit" label="起始位" width="80" />
        <el-table-column prop="length" label="长度" width="70" />
        <el-table-column prop="byteOrder" label="字节序" width="80" />
        <el-table-column prop="factor" label="因子" width="80" />
        <el-table-column prop="offset" label="偏移" width="80" />
        <el-table-column prop="min" label="最小值" width="80" />
        <el-table-column prop="max" label="最大值" width="80" />
        <el-table-column prop="unit" label="单位" width="70" />
        <el-table-column prop="comment" label="注释" min-width="150" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getDbcFilePage,
  getDbcSignals,
  publishDbcFile,
  deleteDbcFile
} from '../api/dbc'
import { getVehicleModelPage } from '../api/vehicle'

// 上传地址与鉴权 header
const uploadUrl = '/api/dbc/upload'
const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token')
  return token ? { Authorization: `Bearer ${token}` } : {}
})

// 上传时附带的表单字段（modelId 是后端必填项）
const uploadData = computed(() => {
  const model = vehicleModels.value.find(m => m.id === selectedModelId.value)
  return {
    modelId: selectedModelId.value || '',
    modelName: model?.modelName || ''
  }
})

const cantoolsAvailable = ref(false)
const loading = ref(false)
const dbcFiles = ref([])
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const detailVisible = ref(false)
const signalDetails = ref([])

// 车型下拉数据
const selectedModelId = ref(null)
const vehicleModels = ref([])

onMounted(() => {
  loadVehicleModels()
  checkCantoolsHealth()
  fetchDbcFiles()
})

async function loadVehicleModels() {
  try {
    const res = await getVehicleModelPage({ current: 1, size: 1000 })
    if (res?.code === 200) {
      vehicleModels.value = res.data?.records || []
    }
  } catch (e) {
    console.error('Failed to load vehicle models:', e)
  }
}

async function checkCantoolsHealth() {
  try {
    const res = await fetch('/api/dbc/health')
    const data = await res.json()
    cantoolsAvailable.value = data?.status === 'ok'
  } catch {
    cantoolsAvailable.value = false
  }
}

async function fetchDbcFiles() {
  loading.value = true
  try {
    const res = await getDbcFilePage({
      current: currentPage.value,
      size: pageSize.value,
      keyword: searchKeyword.value
    })
    if (res?.code === 200) {
      dbcFiles.value = res.data?.records || []
      total.value = res.data?.total || 0
    }
  } catch (e) {
    console.error('Failed to load DBC files:', e)
  } finally {
    loading.value = false
  }
}

function beforeUpload(file) {
  const isDbc = file.name.toLowerCase().endsWith('.dbc')
  if (!isDbc) {
    ElMessage.error('仅支持 .dbc 文件')
    return false
  }
  if (!selectedModelId.value) {
    ElMessage.error('请先选择车型后再上传')
    return false
  }
  return true
}

function onUploadSuccess() {
  ElMessage.success('上传成功')
  fetchDbcFiles()
}

function onUploadError() {
  ElMessage.error('上传失败')
}

async function viewDetails(row) {
  try {
    const res = await getDbcSignals(row.id)
    signalDetails.value = res?.data || []
    detailVisible.value = true
  } catch (e) {
    console.error('Failed to load signal details:', e)
  }
}

async function publishDbc(row) {
  await publishDbcFile(row.id)
  ElMessage.success('发布成功')
  fetchDbcFiles()
}

async function deleteDbc(row) {
  await deleteDbcFile(row.id)
  ElMessage.success('删除成功')
  fetchDbcFiles()
}

function formatFileSize(bytes) {
  if (!bytes) return '-'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1048576) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / 1048576).toFixed(1) + ' MB'
}

function statusType(status) {
  return { 0: 'info', 1: 'warning', 2: 'success' }[status] || 'info'
}

function statusText(status) {
  return { 0: '草稿', 1: '就绪', 2: '已发布' }[status] || '未知'
}
</script>
