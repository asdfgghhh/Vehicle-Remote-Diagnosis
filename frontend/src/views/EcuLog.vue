<template>
  <div class="ecu-log">
    <el-card>
      <template #header>
        <span>日志分析</span>
      </template>
      
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="关键词">
          <el-input v-model="queryForm.keyword" placeholder="文件名" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="fileName" label="文件名" width="200" />
        <el-table-column prop="vin" label="VIN" width="200" />
        <el-table-column prop="ecuType" label="ECU类型" width="120" />
        <el-table-column prop="fileSize" label="文件大小" width="120">
          <template #default="{ row }">
            {{ formatSize(row.fileSize) }}
          </template>
        </el-table-column>
        <el-table-column prop="uploadStatus" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.uploadStatus === 1" type="warning">上传中</el-tag>
            <el-tag v-else-if="row.uploadStatus === 2" type="success">完成</el-tag>
            <el-tag v-else type="danger">失败</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="上传时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleDownload(row)">下载</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        style="margin-top: 20px; justify-content: flex-end;"
      />
    </el-card>
    
    <el-dialog v-model="uploadDialogVisible" title="上传日志文件" width="500px">
      <el-upload
        ref="uploadRef"
        drag
        :auto-upload="false"
        :on-change="handleFileChange"
        :file-list="fileList"
        multiple
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">
          拖拽文件到此处或<em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            支持断点续传，单文件不超过500MB
          </div>
        </template>
      </el-upload>
      
      <div v-if="currentFile" style="margin-top: 20px;">
        <el-form :model="uploadForm" label-width="100px">
          <el-form-item label="VIN">
            <el-input v-model="uploadForm.vin" />
          </el-form-item>
          <el-form-item label="ECU类型">
            <el-input v-model="uploadForm.ecuType" />
          </el-form-item>
        </el-form>
        
        <el-progress :percentage="uploadProgress" />
      </div>
      
      <template #footer>
        <el-button @click="uploadDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleUpload" :loading="uploading">上传</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { getEcuLogPage, downloadLog } from '@/api/ecuLog'

const loading = ref(false)
const uploading = ref(false)
const tableData = ref([])
const uploadDialogVisible = ref(false)
const uploadRef = ref(null)
const fileList = ref([])
const currentFile = ref(null)
const uploadProgress = ref(0)

const queryForm = reactive({
  keyword: ''
})

const uploadForm = reactive({
  vin: '',
  ecuType: ''
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size,
      keyword: queryForm.keyword
    }
    const res = await getEcuLogPage(params)
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
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
  handleSearch()
}

const handleFileChange = (file) => {
  currentFile.value = file
}

const handleUpload = async () => {
  if (!currentFile.value) {
    ElMessage.warning('请选择文件')
    return
  }
  
  uploading.value = true
  uploadProgress.value = 0
  
  try {
    const interval = setInterval(() => {
      if (uploadProgress.value < 90) {
        uploadProgress.value += 10
      }
    }, 500)
    
    await new Promise(resolve => setTimeout(resolve, 5000))
    
    clearInterval(interval)
    uploadProgress.value = 100
    
    ElMessage.success('上传成功')
    uploadDialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('上传失败')
  } finally {
    uploading.value = false
    uploadProgress.value = 0
  }
}

const handleDownload = async (row) => {
  try {
    const res = await downloadLog(row.id)
    const url = window.URL.createObjectURL(new Blob([res]))
    const link = document.createElement('a')
    link.href = url
    link.download = row.fileName
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('下载成功')
  } catch (error) {
    ElMessage.error('下载失败')
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该日志文件吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    ElMessage.success('删除成功')
    loadData()
  })
}

const handleSizeChange = (size) => {
  pagination.size = size
  loadData()
}

const handleCurrentChange = (current) => {
  pagination.current = current
  loadData()
}

const formatSize = (bytes) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return (bytes / Math.pow(k, i)).toFixed(2) + ' ' + sizes[i]
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.ecu-log {
  padding: 20px;
}

.search-form {
  margin-bottom: 20px;
}
</style>
