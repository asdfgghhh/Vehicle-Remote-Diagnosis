<template>
  <div>
    <el-card>
      <template #header><span>⚠️ 告警触发记录</span></template>

      <el-form inline style="margin-bottom: 16px">
        <el-form-item label="VIN">
          <el-input v-model="filterVin" placeholder="输入 VIN" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="告警级别">
          <el-select v-model="filterLevel" placeholder="全部" clearable style="width: 120px">
            <el-option label="严重" :value="1" />
            <el-option label="警告" :value="2" />
            <el-option label="提示" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchLogs">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="logs" style="width: 100%" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="ruleName" label="规则名称" min-width="160" />
        <el-table-column prop="vin" label="VIN" width="160" />
        <el-table-column prop="signalName" label="信号" width="140" />
        <el-table-column prop="signalValue" label="信号值" width="100" />
        <el-table-column label="级别" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="levelType(row.alertLevel)" size="small">{{ levelText(row.alertLevel) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="alertMessage" label="告警消息" min-width="250" show-overflow-tooltip />
        <el-table-column prop="triggerTime" label="触发时间" width="170" />
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.notified === 1 ? 'success' : 'info'" size="small">
              {{ row.notified === 1 ? '已通知' : '未通知' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="currentPage"
        :total="total"
        :page-size="pageSize"
        layout="total, prev, pager, next"
        style="margin-top: 16px; justify-content: center"
        @current-change="fetchLogs"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../utils/api'

const loading = ref(false)
const logs = ref([])
const filterVin = ref('')
const filterLevel = ref(null)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

onMounted(() => fetchLogs())

async function fetchLogs() {
  loading.value = true
  try {
    const res = await api.get('/vehicle/alert/logs', {
      params: {
        page: currentPage.value,
        size: pageSize.value,
        vin: filterVin.value || undefined,
        alertLevel: filterLevel.value || undefined
      }
    })
    if (res?.code === 200) {
      logs.value = res.data?.records || []
      total.value = res.data?.total || 0
    }
  } finally {
    loading.value = false
  }
}

function levelType(level) {
  return { 1: 'danger', 2: 'warning', 3: 'info' }[level] || 'info'
}
function levelText(level) {
  return { 1: '严重', 2: '警告', 3: '提示' }[level] || '未知'
}
</script>
