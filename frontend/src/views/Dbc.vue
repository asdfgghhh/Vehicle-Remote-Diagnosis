<template>
  <div class="dbc-management">
    <el-card>
      <template #header>
        <div class="header-actions">
          <span>DBC文件管理</span>
          <el-button type="primary" @click="uploadDialogVisible = true">
            <el-icon><Upload /></el-icon>
            上传DBC文件
          </el-button>
        </div>
      </template>
      
      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="fileName" label="文件名" width="200" />
        <el-table-column prop="version" label="版本" width="100" />
        <el-table-column prop="messageCount" label="消息数" width="100" />
        <el-table-column prop="signalCount" label="信号数" width="100" />
        <el-table-column prop="fileSize" label="文件大小" width="120">
          <template #default="{ row }">
            {{ formatSize(row.fileSize) }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="上传时间" width="180" />
        <el-table-column label="操作" width="350" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button type="success" link @click="handleViewMessages(row)">消息列表</el-button>
            <el-button type="info" link @click="handleDispatch(row)">下发</el-button>
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
    
    <el-dialog v-model="uploadDialogVisible" title="上传DBC文件" width="500px">
      <el-upload
        ref="uploadRef"
        drag
        :auto-upload="false"
        :on-change="handleFileChange"
        :file-list="fileList"
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">
          拖拽.dbc文件到此处或<em>点击上传</em>
        </div>
      </el-upload>
      
      <el-form :model="uploadForm" style="margin-top: 20px;" label-width="80px">
        <el-form-item label="版本">
          <el-input v-model="uploadForm.version" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="uploadForm.description" type="textarea" rows="3" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="uploadDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleUpload">上传</el-button>
      </template>
    </el-dialog>
    
    <el-dialog v-model="dispatchDialogVisible" title="下发DBC文件" width="600px">
      <el-form :model="dispatchForm" label-width="100px">
        <el-form-item label="选择车辆">
          <el-select v-model="dispatchForm.vehicleIds" multiple placeholder="请选择车辆">
            <el-option
              v-for="vehicle in vehicles"
              :key="vehicle.id"
              :label="vehicle.vin"
              :value="vehicle.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dispatchDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmDispatch">确认下发</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Upload, UploadFilled } from '@element-plus/icons-vue'
import { getDbcFilePage, uploadDbcFile, dispatchToVehicles, getDbcMessages, deleteDbcFile } from '@/api/dbc'
import { getVehiclePage } from '@/api/vehicle'

const loading = ref(false)
const tableData = ref([])
const vehicles = ref([])
const uploadDialogVisible = ref(false)
const dispatchDialogVisible = ref(false)
const uploadRef = ref(null)
const fileList = ref([])
const currentFile = ref(null)

const uploadForm = reactive({
  version: '',
  description: ''
})

const dispatchForm = reactive({
  dbcFileId: null,
  vehicleIds: []
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
      size: pagination.size
    }
    const res = await getDbcFilePage(params)
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const loadVehicles = async () => {
  try {
    const res = await getVehiclePage({ current: 1, size: 100 })
    vehicles.value = res.data.records
  } catch (error) {
    console.error('加载车辆列表失败', error)
  }
}

const handleFileChange = (file) => {
  currentFile.value = file
}

const handleUpload = async () => {
  if (!currentFile.value) {
    ElMessage.warning('请选择文件')
    return
  }
  
  const formData = new FormData()
  formData.append('file', currentFile.value.raw)
  formData.append('version', uploadForm.version)
  formData.append('description', uploadForm.description)
  
  try {
    await uploadDbcFile(formData)
    ElMessage.success('上传成功')
    uploadDialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('上传失败')
  }
}

const handleView = (row) => {
  ElMessageBox.alert(row.parseResult || '暂无解析结果', 'DBC文件内容')
}

const handleViewMessages = async (row) => {
  try {
    const res = await getDbcMessages(row.id)
    ElMessageBox.alert(res.data.join('<br>'), '消息列表')
  } catch (error) {
    ElMessage.error('加载消息列表失败')
  }
}

const handleDispatch = (row) => {
  dispatchForm.dbcFileId = row.id
  dispatchForm.vehicleIds = []
  dispatchDialogVisible.value = true
}

const confirmDispatch = async () => {
  if (dispatchForm.vehicleIds.length === 0) {
    ElMessage.warning('请选择至少一辆车')
    return
  }
  
  try {
    await dispatchToVehicles(dispatchForm.dbcFileId, dispatchForm.vehicleIds)
    ElMessage.success('下发成功')
    dispatchDialogVisible.value = false
  } catch (error) {
    ElMessage.error('下发失败')
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该DBC文件吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await deleteDbcFile(row.id)
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
  loadVehicles()
})
</script>

<style scoped>
.dbc-management {
  padding: 20px;
}

.header-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
