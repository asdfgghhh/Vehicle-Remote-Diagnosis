<template>
  <div class="vehicle-detail" v-loading="loading">
    <el-card>
      <template #header>
        <div class="header-actions">
          <div class="title-row">
            <el-button link type="primary" @click="goBack">
              <el-icon><ArrowLeft /></el-icon>
              返回列表
            </el-button>
            <span class="page-title">车辆详情</span>
          </div>
          <el-button type="primary" @click="handleEdit">编辑</el-button>
        </div>
      </template>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="VIN码">{{ vehicle.vin || '-' }}</el-descriptions-item>
        <el-descriptions-item label="车牌号">{{ vehicle.plateNumber || '-' }}</el-descriptions-item>
        <el-descriptions-item label="车型">{{ modelName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="颜色">{{ vehicle.color || '-' }}</el-descriptions-item>
        <el-descriptions-item label="生产年份">{{ vehicle.productionYear || '-' }}</el-descriptions-item>
        <el-descriptions-item label="发动机号">{{ vehicle.engineNumber || '-' }}</el-descriptions-item>
        <el-descriptions-item label="车架号">{{ vehicle.bodyNumber || '-' }}</el-descriptions-item>
        <el-descriptions-item label="数据来源">
          <el-tag v-if="vehicle.dataSource === 1" type="success">手动</el-tag>
          <el-tag v-else-if="vehicle.dataSource === 2" type="warning">Kafka</el-tag>
          <el-tag v-else-if="vehicle.dataSource === 3" type="info">API</el-tag>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="配置字" :span="2">
          <span class="config-word">{{ vehicle.configWord || '-' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatTime(vehicle.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ formatTime(vehicle.updateTime) }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card class="ecu-card">
      <template #header>
        <span>ECU 信息</span>
      </template>
      <el-table :data="ecuList" stripe empty-text="暂无 ECU 信息">
        <el-table-column prop="ecuType" label="ECU类型" width="120" />
        <el-table-column prop="ecuPartNumber" label="零部件号" width="150" />
        <el-table-column prop="hardwareVersion" label="硬件版本" width="120" />
        <el-table-column prop="softwareVersion" label="软件版本" width="120" />
        <el-table-column prop="supplier" label="供应商" width="120" />
        <el-table-column prop="serialNumber" label="序列号" min-width="150" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getVehicle, getVehicleEcus, getVehicleModelPage } from '@/api/vehicle'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const vehicle = ref({})
const ecuList = ref([])
const vehicleModels = ref([])

const modelName = computed(() => {
  const model = vehicleModels.value.find(item => item.id === vehicle.value.modelId)
  if (!model) return ''
  return model.modelCode ? `${model.modelCode} - ${model.modelName}` : model.modelName
})

const formatTime = (time) => {
  if (!time) return '-'
  return String(time).replace('T', ' ').slice(0, 19)
}

const loadDetail = async () => {
  const id = route.params.id
  if (!id) return

  loading.value = true
  try {
    const [vehicleRes, ecuRes, modelRes] = await Promise.all([
      getVehicle(id),
      getVehicleEcus(id),
      getVehicleModelPage({ current: 1, size: 100 })
    ])
    vehicle.value = vehicleRes.data || {}
    ecuList.value = ecuRes.data || []
    vehicleModels.value = modelRes.data?.records || []
  } catch (error) {
    ElMessage.error('加载车辆详情失败')
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.push('/vehicle/list')
}

const handleEdit = () => {
  router.push({ path: '/vehicle/list', query: { editId: vehicle.value.id } })
}

onMounted(() => {
  loadDetail()
})
</script>

<style scoped>
.vehicle-detail {
  padding: 20px;
}

.header-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-title {
  font-size: 16px;
  font-weight: 600;
}

.config-word {
  font-family: Consolas, Monaco, monospace;
  word-break: break-all;
}

.ecu-card {
  margin-top: 20px;
}
</style>
