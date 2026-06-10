<template>
  <div class="vehicle-list">
    <el-card>
      <template #header>
        <div class="header-actions">
          <span>车辆列表</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增车辆
          </el-button>
        </div>
      </template>
      
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="关键词">
          <el-input v-model="queryForm.keyword" placeholder="VIN/车牌号" clearable />
        </el-form-item>
        <el-form-item label="车型">
          <el-select
            v-model="queryForm.modelId"
            class="model-select"
            placeholder="请选择车型"
            clearable
            filterable
          >
            <el-option
              v-for="model in vehicleModels"
              :key="model.id"
              :label="formatModelLabel(model)"
              :value="model.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="vin" label="VIN码" width="200">
          <template #default="{ row }">
            <el-link type="primary" :underline="false" @click="goToDetail(row)">
              {{ row.vin }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="plateNumber" label="车牌号" width="120" />
        <el-table-column prop="color" label="颜色" width="80" />
        <el-table-column prop="productionYear" label="生产年份" width="100" />
        <el-table-column prop="engineNumber" label="发动机号" width="150" />
        <el-table-column prop="configWord" label="配置字" min-width="180" show-overflow-tooltip />
        <el-table-column prop="dataSource" label="数据来源" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.dataSource === 1" type="success">手动</el-tag>
            <el-tag v-else-if="row.dataSource === 2" type="warning">Kafka</el-tag>
            <el-tag v-else type="info">API</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="primary" link @click="handleViewEcu(row)">ECU信息</el-button>
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
    
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="VIN码" prop="vin">
          <el-input v-model="form.vin" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="车型" prop="modelId">
          <el-select
            v-model="form.modelId"
            class="model-select-full"
            placeholder="请选择车型"
            filterable
          >
            <el-option
              v-for="model in vehicleModels"
              :key="model.id"
              :label="formatModelLabel(model)"
              :value="model.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="车牌号" prop="plateNumber">
          <el-input v-model="form.plateNumber" />
        </el-form-item>
        <el-form-item label="颜色" prop="color">
          <el-input v-model="form.color" />
        </el-form-item>
        <el-form-item label="生产年份" prop="productionYear">
          <el-input-number v-model="form.productionYear" :min="2000" :max="2030" />
        </el-form-item>
        <el-form-item label="发动机号" prop="engineNumber">
          <el-input v-model="form.engineNumber" />
        </el-form-item>
        <el-form-item label="车架号" prop="bodyNumber">
          <el-input v-model="form.bodyNumber" />
        </el-form-item>
        <el-form-item label="配置字" prop="configWord">
          <el-input v-model="form.configWord" placeholder="请输入配置字" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
    
    <el-dialog v-model="ecuDialogVisible" title="ECU信息" width="800px">
      <el-button type="primary" @click="handleAddEcu" style="margin-bottom: 10px;">
        <el-icon><Plus /></el-icon>
        添加ECU
      </el-button>
      <el-table :data="ecuList" stripe>
        <el-table-column prop="ecuType" label="ECU类型" width="120" />
        <el-table-column prop="ecuPartNumber" label="零部件号" width="150" />
        <el-table-column prop="hardwareVersion" label="硬件版本" width="100" />
        <el-table-column prop="softwareVersion" label="软件版本" width="100" />
        <el-table-column prop="supplier" label="供应商" width="120" />
        <el-table-column prop="serialNumber" label="序列号" width="150" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button type="primary" link>编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getVehiclePage, getVehicle, createVehicle, updateVehicle, deleteVehicle, getVehicleModelPage, getVehicleEcus } from '@/api/vehicle'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const tableData = ref([])
const vehicleModels = ref([])
const dialogVisible = ref(false)
const ecuDialogVisible = ref(false)
const dialogTitle = ref('新增车辆')
const formRef = ref(null)
const ecuList = ref([])
const currentVehicleId = ref(null)

const queryForm = reactive({
  keyword: '',
  modelId: null
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const form = reactive({
  id: null,
  vin: '',
  modelId: null,
  plateNumber: '',
  color: '',
  productionYear: new Date().getFullYear(),
  engineNumber: '',
  bodyNumber: '',
  configWord: ''
})

const rules = {
  vin: [{ required: true, message: '请输入VIN码', trigger: 'blur' }],
  modelId: [{ required: true, message: '请选择车型', trigger: 'change' }]
}

const formatModelLabel = (model) => {
  if (model.modelCode) {
    return `${model.modelCode} - ${model.modelName}`
  }
  return model.modelName
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size,
      keyword: queryForm.keyword,
      modelId: queryForm.modelId
    }
    const res = await getVehiclePage(params)
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const loadVehicleModels = async () => {
  try {
    const res = await getVehicleModelPage({ current: 1, size: 100 })
    vehicleModels.value = res.data.records
  } catch (error) {
    console.error('加载车型列表失败', error)
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

const handleAdd = () => {
  dialogTitle.value = '新增车辆'
  Object.keys(form).forEach(key => {
    if (key !== 'productionYear') form[key] = ''
  })
  form.productionYear = new Date().getFullYear()
  dialogVisible.value = true
}

const goToDetail = (row) => {
  router.push(`/vehicle/detail/${row.id}`)
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑车辆'
  Object.assign(form, row)
  dialogVisible.value = true
}

const openEditById = async (id) => {
  try {
    const res = await getVehicle(id)
    if (res.data) {
      handleEdit(res.data)
    }
  } catch (error) {
    ElMessage.error('加载车辆信息失败')
  }
}

const handleViewEcu = async (row) => {
  currentVehicleId.value = row.id
  try {
    const res = await getVehicleEcus(row.id)
    ecuList.value = res.data || []
    ecuDialogVisible.value = true
  } catch (error) {
    ElMessage.error('加载ECU信息失败')
  }
}

const handleAddEcu = () => {
  ElMessage.info('添加ECU功能')
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该车辆吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await deleteVehicle(row.id)
    ElMessage.success('删除成功')
    loadData()
  })
}

const handleSubmit = async () => {
  await formRef.value.validate(async (valid) => {
    if (valid) {
      if (form.id) {
        await updateVehicle(form.id, form)
        ElMessage.success('更新成功')
      } else {
        await createVehicle(form)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      loadData()
    }
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

onMounted(() => {
  loadData()
  loadVehicleModels()
  if (route.query.editId) {
    openEditById(route.query.editId)
  }
})
</script>

<style scoped>
.vehicle-list {
  padding: 20px;
}

.header-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}

.model-select {
  width: 280px;
}

.model-select-full {
  width: 100%;
}
</style>
