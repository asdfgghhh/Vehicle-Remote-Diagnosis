<template>
  <div class="vehicle-sync-log">
    <el-card>
      <template #header>
        <span>车辆同步记录</span>
      </template>

      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="同步方式">
          <el-select v-model="queryForm.syncType" placeholder="全部" clearable style="width: 120px;">
            <el-option label="Kafka" value="KAFKA" />
            <el-option label="API" value="API" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 120px;">
            <el-option label="成功" value="SUCCESS" />
            <el-option label="失败" value="FAILED" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="queryForm.keyword" placeholder="VIN/来源/失败原因" clearable style="width: 220px;" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="syncType" label="同步方式" width="90">
          <template #default="{ row }">
            <el-tag v-if="row.syncType === 'KAFKA'" type="warning">Kafka</el-tag>
            <el-tag v-else type="info">API</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="source" label="来源" min-width="180" show-overflow-tooltip />
        <el-table-column prop="vin" label="VIN" width="180" show-overflow-tooltip />
        <el-table-column prop="action" label="动作" width="90" />
        <el-table-column prop="recordCount" label="记录数" width="80" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 'SUCCESS' ? 'success' : 'danger'">
              {{ row.status === 'SUCCESS' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="message" label="失败原因" min-width="160" show-overflow-tooltip />
        <el-table-column prop="startTime" label="开始时间" width="170">
          <template #default="{ row }">{{ formatTime(row.startTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleViewDetail(row)">明细</el-button>
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

    <el-dialog v-model="detailVisible" title="同步记录明细" width="760px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="同步方式">{{ currentRow.syncType || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentRow.status === 'SUCCESS' ? 'success' : 'danger'">
            {{ currentRow.status === 'SUCCESS' ? '成功' : '失败' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="来源" :span="2">{{ currentRow.source || '-' }}</el-descriptions-item>
        <el-descriptions-item label="VIN">{{ currentRow.vin || '-' }}</el-descriptions-item>
        <el-descriptions-item label="动作">{{ currentRow.action || '-' }}</el-descriptions-item>
        <el-descriptions-item label="记录数">{{ currentRow.recordCount ?? 0 }}</el-descriptions-item>
        <el-descriptions-item label="耗时">{{ calcDuration(currentRow) }}</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ formatTime(currentRow.startTime) }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ formatTime(currentRow.endTime) }}</el-descriptions-item>
        <el-descriptions-item label="失败原因" :span="2">
          {{ currentRow.message || '-' }}
        </el-descriptions-item>
      </el-descriptions>

      <div class="payload-section">
        <div class="payload-title">原始同步车辆信息</div>
        <pre class="payload-content">{{ formatPayload(currentRow.payload) }}</pre>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getVehicleSyncLogPage, getVehicleSyncLog } from '@/api/vehicle'

const loading = ref(false)
const tableData = ref([])
const detailVisible = ref(false)
const currentRow = ref({})

const queryForm = reactive({
  syncType: '',
  status: '',
  keyword: ''
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const formatTime = (time) => {
  if (!time) return '-'
  return String(time).replace('T', ' ').slice(0, 19)
}

const calcDuration = (row) => {
  if (!row.startTime || !row.endTime) return '-'
  const start = new Date(String(row.startTime).replace(' ', 'T')).getTime()
  const end = new Date(String(row.endTime).replace(' ', 'T')).getTime()
  if (Number.isNaN(start) || Number.isNaN(end)) return '-'
  const ms = end - start
  return ms < 1000 ? `${ms} ms` : `${(ms / 1000).toFixed(2)} s`
}

const formatPayload = (payload) => {
  if (!payload) return '无原始数据'
  try {
    return JSON.stringify(JSON.parse(payload), null, 2)
  } catch {
    return payload
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size,
      syncType: queryForm.syncType || undefined,
      status: queryForm.status || undefined,
      keyword: queryForm.keyword || undefined
    }
    const res = await getVehicleSyncLogPage(params)
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    ElMessage.error('加载同步记录失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadData()
}

const handleReset = () => {
  queryForm.syncType = ''
  queryForm.status = ''
  queryForm.keyword = ''
  handleSearch()
}

const handleViewDetail = async (row) => {
  try {
    const res = await getVehicleSyncLog(row.id)
    currentRow.value = res.data || row
    detailVisible.value = true
  } catch (error) {
    ElMessage.error('加载明细失败')
  }
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
})
</script>

<style scoped>
.vehicle-sync-log {
  padding: 20px;
}

.search-form {
  margin-bottom: 20px;
}

.payload-section {
  margin-top: 20px;
}

.payload-title {
  font-weight: 600;
  margin-bottom: 8px;
}

.payload-content {
  margin: 0;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
  max-height: 360px;
  overflow: auto;
  font-family: Consolas, Monaco, monospace;
  font-size: 13px;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
