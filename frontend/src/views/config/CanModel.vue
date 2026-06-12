<template>
  <div class="can-model">
    <el-card>
      <template #header>
        <div class="header-actions">
          <span>车辆 CAN 模型</span>
          <el-button type="primary" @click="uploadDialogVisible = true">
            <el-icon><Upload /></el-icon>
            上传 DBC
          </el-button>
        </div>
      </template>

      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="车型">
          <el-select v-model="queryForm.modelId" placeholder="全部" clearable style="width: 180px;">
            <el-option
              v-for="item in modelOptions"
              :key="item.id"
              :label="item.modelName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="queryForm.keyword" placeholder="文件名" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="modelName" label="车型" width="140" show-overflow-tooltip />
        <el-table-column prop="fileName" label="DBC 文件" min-width="200" show-overflow-tooltip />
        <el-table-column prop="version" label="版本" width="100" />
        <el-table-column prop="messageCount" label="报文数" width="90" align="center" />
        <el-table-column prop="signalCount" label="信号数" width="90" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="fileSize" label="文件大小" width="110">
          <template #default="{ row }">{{ formatSize(row.fileSize) }}</template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="160" show-overflow-tooltip />
        <el-table-column prop="createTime" label="上传时间" width="170" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="goDetail(row)">详情</el-button>
            <el-button type="primary" link @click="openEdit(row)">编辑</el-button>
            <el-button
              v-if="row.status !== 2"
              type="success"
              link
              @click="handlePublish(row)"
            >发布</el-button>
            <el-button
              v-if="row.status === 2"
              type="warning"
              link
              @click="handleRevoke(row)"
            >撤销</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadData"
        @current-change="loadData"
        style="margin-top: 20px; justify-content: flex-end;"
      />
    </el-card>

    <el-dialog v-model="uploadDialogVisible" title="上传 DBC 文件" width="520px" @closed="resetUpload">
      <el-upload
        drag
        :auto-upload="false"
        :limit="1"
        :on-change="handleFileChange"
        :file-list="fileList"
      >
        <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
        <div class="el-upload__text">拖拽 .dbc 文件到此处或<em>点击上传</em></div>
      </el-upload>
      <el-form ref="uploadFormRef" :model="uploadForm" :rules="uploadRules" label-width="80px" style="margin-top: 16px;">
        <el-form-item label="车型" prop="modelId">
          <el-select v-model="uploadForm.modelId" placeholder="请选择车型" style="width: 100%;">
            <el-option
              v-for="item in modelOptions"
              :key="item.id"
              :label="item.modelName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="版本">
          <el-input v-model="uploadForm.version" placeholder="如 v1.0" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="uploadForm.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="uploadDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="uploading" @click="handleUpload">上传</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="editDialogVisible" title="编辑 CAN 模型" width="480px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="文件名">
          <el-input :model-value="editForm.fileName" disabled />
        </el-form-item>
        <el-form-item label="版本">
          <el-input v-model="editForm.version" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="editForm.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Upload, UploadFilled } from '@element-plus/icons-vue'
import {
  getDbcFilePage,
  uploadDbcFile,
  updateDbcFile,
  publishDbcFile,
  revokeDbcFile
} from '@/api/dbc'
import { getVehicleModelPage } from '@/api/vehicle'

const router = useRouter()
const loading = ref(false)
const uploading = ref(false)
const tableData = ref([])
const modelOptions = ref([])
const uploadDialogVisible = ref(false)
const editDialogVisible = ref(false)
const fileList = ref([])
const currentFile = ref(null)
const uploadFormRef = ref(null)

const queryForm = reactive({ keyword: '', modelId: null })
const uploadForm = reactive({ modelId: null, version: '', description: '' })
const uploadRules = {
  modelId: [{ required: true, message: '请选择车型', trigger: 'change' }]
}
const editForm = reactive({ id: null, fileName: '', version: '', description: '' })
const pagination = reactive({ current: 1, size: 10, total: 0 })

const statusText = (status) => ({ 0: '已撤销', 1: '草稿', 2: '已发布' }[status] ?? '未知')
const statusTagType = (status) => ({ 0: 'info', 1: 'warning', 2: 'success' }[status] ?? 'info')

const loadData = async () => {
  loading.value = true
  try {
    const res = await getDbcFilePage({
      current: pagination.current,
      size: pagination.size,
      keyword: queryForm.keyword || undefined,
      modelId: queryForm.modelId || undefined
    })
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadData()
}

const handleReset = () => {
  queryForm.keyword = ''
  queryForm.modelId = null
  handleSearch()
}

const loadModels = async () => {
  try {
    const res = await getVehicleModelPage({ current: 1, size: 200 })
    modelOptions.value = res.data?.records || []
  } catch {
    modelOptions.value = []
  }
}

const getModelName = (modelId) => {
  const model = modelOptions.value.find((item) => item.id === modelId)
  return model?.modelName || ''
}

const goDetail = (row) => {
  router.push(`/config/can-model/${row.id}`)
}

const openEdit = (row) => {
  editForm.id = row.id
  editForm.fileName = row.fileName
  editForm.version = row.version || ''
  editForm.description = row.description || ''
  editDialogVisible.value = true
}

const confirmEdit = async () => {
  try {
    await updateDbcFile(editForm.id, {
      version: editForm.version,
      description: editForm.description
    })
    ElMessage.success('保存成功')
    editDialogVisible.value = false
    loadData()
  } catch {
    ElMessage.error('保存失败')
  }
}

const handlePublish = (row) => {
  ElMessageBox.confirm(`确定发布「${row.fileName}」吗？`, '发布确认', { type: 'info' })
    .then(async () => {
      await publishDbcFile(row.id)
      ElMessage.success('发布成功')
      loadData()
    })
    .catch(() => {})
}

const handleRevoke = (row) => {
  ElMessageBox.confirm(`确定撤销「${row.fileName}」的发布状态吗？`, '撤销确认', { type: 'warning' })
    .then(async () => {
      await revokeDbcFile(row.id)
      ElMessage.success('已撤销')
      loadData()
    })
    .catch(() => {})
}

const handleFileChange = (file) => {
  currentFile.value = file
  fileList.value = [file]
}

const resetUpload = () => {
  fileList.value = []
  currentFile.value = null
  uploadForm.modelId = null
  uploadForm.version = ''
  uploadForm.description = ''
  uploadFormRef.value?.clearValidate()
}

const handleUpload = async () => {
  if (!currentFile.value?.raw) {
    ElMessage.warning('请选择 DBC 文件')
    return
  }
  await uploadFormRef.value?.validate()
  uploading.value = true
  const formData = new FormData()
  formData.append('file', currentFile.value.raw)
  formData.append('modelId', uploadForm.modelId)
  formData.append('modelName', getModelName(uploadForm.modelId))
  formData.append('version', uploadForm.version)
  formData.append('description', uploadForm.description)
  try {
    await uploadDbcFile(formData)
    ElMessage.success('上传成功')
    uploadDialogVisible.value = false
    loadData()
  } catch {
    ElMessage.error('上传失败')
  } finally {
    uploading.value = false
  }
}

const formatSize = (bytes) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return (bytes / Math.pow(k, i)).toFixed(2) + ' ' + sizes[i]
}

onMounted(() => {
  loadModels()
  loadData()
})
</script>

<style scoped>
.can-model {
  padding: 0;
}

.header-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 16px;
}
</style>
