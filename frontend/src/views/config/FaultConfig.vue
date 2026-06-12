<template>
  <div class="fault-config">
    <el-card>
      <template #header>
        <div class="header-actions">
          <span>故障配置</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增配置
          </el-button>
        </div>
      </template>

      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="关键词">
          <el-input v-model="queryForm.keyword" placeholder="故障码/DTC/告警名称/ECU" clearable />
        </el-form-item>
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
        <el-form-item label="告警等级">
          <el-select v-model="queryForm.alarmLevel" placeholder="全部" clearable style="width: 120px;">
            <el-option label="严重" :value="1" />
            <el-option label="警告" :value="2" />
            <el-option label="提示" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="faultCode" label="故障码" width="110" />
        <el-table-column prop="dtc" label="DTC" width="110" />
        <el-table-column prop="alarmName" label="告警名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="modelName" label="车型" width="140" show-overflow-tooltip />
        <el-table-column prop="ecuType" label="ECU 部件" width="110" />
        <el-table-column prop="componentCode" label="部件简称" width="100" />
        <el-table-column prop="alarmLevel" label="告警等级" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="alarmLevelTag(row.alarmLevel)" size="small">
              {{ alarmLevelText(row.alarmLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="说明" min-width="180" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="车型" prop="modelId">
          <el-select v-model="form.modelId" placeholder="请选择车型" style="width: 100%;">
            <el-option
              v-for="item in modelOptions"
              :key="item.id"
              :label="item.modelName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="故障码" prop="faultCode">
          <el-input v-model="form.faultCode" placeholder="如 F001" />
        </el-form-item>
        <el-form-item label="DTC" prop="dtc">
          <el-input v-model="form.dtc" placeholder="如 P0420" />
        </el-form-item>
        <el-form-item label="告警名称" prop="alarmName">
          <el-input v-model="form.alarmName" />
        </el-form-item>
        <el-form-item label="ECU 部件" prop="ecuType">
          <el-input v-model="form.ecuType" placeholder="如 EMS、BCM" />
        </el-form-item>
        <el-form-item label="部件简称">
          <el-input v-model="form.componentCode" placeholder="如 EMS" />
        </el-form-item>
        <el-form-item label="告警等级" prop="alarmLevel">
          <el-select v-model="form.alarmLevel" style="width: 100%;">
            <el-option label="严重" :value="1" />
            <el-option label="警告" :value="2" />
            <el-option label="提示" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="说明">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  getFaultConfigPage,
  createFaultConfig,
  updateFaultConfig,
  deleteFaultConfig
} from '@/api/faultConfig'
import { getVehicleModelPage } from '@/api/vehicle'

const loading = ref(false)
const tableData = ref([])
const modelOptions = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增配置')
const formRef = ref(null)
const editingId = ref(null)

const queryForm = reactive({
  keyword: '',
  modelId: null,
  alarmLevel: null
})

const pagination = reactive({ current: 1, size: 10, total: 0 })

const defaultForm = () => ({
  modelId: null,
  faultCode: '',
  dtc: '',
  alarmName: '',
  ecuType: '',
  componentCode: '',
  alarmLevel: 2,
  description: '',
  status: 1
})

const form = reactive(defaultForm())

const rules = {
  modelId: [{ required: true, message: '请选择车型', trigger: 'change' }],
  faultCode: [{ required: true, message: '请输入故障码', trigger: 'blur' }],
  alarmName: [{ required: true, message: '请输入告警名称', trigger: 'blur' }],
  ecuType: [{ required: true, message: '请输入 ECU 部件', trigger: 'blur' }],
  alarmLevel: [{ required: true, message: '请选择告警等级', trigger: 'change' }]
}

const alarmLevelText = (level) => ({ 1: '严重', 2: '警告', 3: '提示' }[level] ?? '-')
const alarmLevelTag = (level) => ({ 1: 'danger', 2: 'warning', 3: 'info' }[level] ?? 'info')

const loadModels = async () => {
  try {
    const res = await getVehicleModelPage({ current: 1, size: 200 })
    modelOptions.value = res.data?.records || []
  } catch {
    modelOptions.value = []
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getFaultConfigPage({
      current: pagination.current,
      size: pagination.size,
      keyword: queryForm.keyword || undefined,
      modelId: queryForm.modelId || undefined,
      alarmLevel: queryForm.alarmLevel ?? undefined
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
  queryForm.alarmLevel = null
  handleSearch()
}

const resetForm = () => {
  Object.assign(form, defaultForm())
  editingId.value = null
}

const handleAdd = () => {
  resetForm()
  dialogTitle.value = '新增配置'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  editingId.value = row.id
  Object.assign(form, {
    modelId: row.modelId,
    faultCode: row.faultCode,
    dtc: row.dtc,
    alarmName: row.alarmName,
    ecuType: row.ecuType,
    componentCode: row.componentCode,
    alarmLevel: row.alarmLevel,
    description: row.description,
    status: row.status ?? 1
  })
  dialogTitle.value = '编辑配置'
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value?.validate()
  try {
    if (editingId.value) {
      await updateFaultConfig(editingId.value, { ...form })
      ElMessage.success('更新成功')
    } else {
      await createFaultConfig({ ...form })
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch {
    ElMessage.error('保存失败')
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除故障配置「${row.alarmName}」吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await deleteFaultConfig(row.id)
      ElMessage.success('删除成功')
      loadData()
    })
    .catch(() => {})
}

onMounted(() => {
  loadModels()
  loadData()
})
</script>

<style scoped>
.fault-config {
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
