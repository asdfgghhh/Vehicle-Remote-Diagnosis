<template>
  <div class="can-model-detail">
    <el-card v-loading="loading">
      <template #header>
        <div class="header-actions">
          <div class="title-row">
            <el-button link type="primary" @click="goBack">
              <el-icon><ArrowLeft /></el-icon>
              返回
            </el-button>
            <span class="title">{{ dbcInfo.fileName || '信号详情' }}</span>
            <el-tag :type="statusTagType(dbcInfo.status)" size="small">{{ statusText(dbcInfo.status) }}</el-tag>
          </div>
        </div>
      </template>

      <el-descriptions :column="4" border class="meta-desc">
        <el-descriptions-item label="车型">{{ dbcInfo.modelName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="版本">{{ dbcInfo.version || '-' }}</el-descriptions-item>
        <el-descriptions-item label="报文数">{{ dbcInfo.messageCount ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="信号数">{{ dbcInfo.signalCount ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="上传时间">{{ dbcInfo.createTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="描述" :span="4">{{ dbcInfo.description || '-' }}</el-descriptions-item>
      </el-descriptions>

      <el-form :inline="true" class="search-form">
        <el-form-item label="信号/报文">
          <el-input
            v-model="keyword"
            placeholder="信号名或报文名"
            clearable
            style="width: 240px;"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="applyFilter">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="pagedSignals" stripe max-height="560">
        <el-table-column prop="name" label="信号名" min-width="160" fixed="left" show-overflow-tooltip />
        <el-table-column prop="messageName" label="报文名" min-width="140" show-overflow-tooltip />
        <el-table-column prop="messageId" label="报文 ID" width="90" />
        <el-table-column prop="startBit" label="起始位" width="80" align="center" />
        <el-table-column prop="length" label="长度(bit)" width="90" align="center" />
        <el-table-column prop="byteOrder" label="字节序" width="90" />
        <el-table-column prop="factor" label="系数" width="80" />
        <el-table-column prop="offset" label="偏移" width="80" />
        <el-table-column prop="min" label="最小值" width="80" />
        <el-table-column prop="max" label="最大值" width="80" />
        <el-table-column prop="unit" label="单位" width="80" />
        <el-table-column prop="samplePeriod" label="采样周期" width="100" align="center">
          <template #default="{ row }">
            {{ row.samplePeriod || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="valueDesc" label="值解释" min-width="180" show-overflow-tooltip />
        <el-table-column prop="comment" label="备注" min-width="160" show-overflow-tooltip />
        <el-table-column prop="receiver" label="接收节点" width="100" />
      </el-table>

      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="filteredSignals.length"
        :page-sizes="[20, 50, 100, 200]"
        layout="total, sizes, prev, pager, next"
        style="margin-top: 16px; justify-content: flex-end;"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getDbcFile, getDbcSignalDetails } from '@/api/dbc'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const dbcInfo = ref({})
const allSignals = ref([])
const keyword = ref('')
const activeKeyword = ref('')

const pagination = reactive({ current: 1, size: 50 })

const statusText = (status) => ({ 0: '已撤销', 1: '草稿', 2: '已发布' }[status] ?? '未知')
const statusTagType = (status) => ({ 0: 'info', 1: 'warning', 2: 'success' }[status] ?? 'info')

const filteredSignals = computed(() => {
  const kw = activeKeyword.value.trim().toLowerCase()
  if (!kw) return allSignals.value
  return allSignals.value.filter((item) =>
    (item.name || '').toLowerCase().includes(kw) ||
    (item.messageName || '').toLowerCase().includes(kw)
  )
})

const pagedSignals = computed(() => {
  const start = (pagination.current - 1) * pagination.size
  return filteredSignals.value.slice(start, start + pagination.size)
})

const loadDetail = async () => {
  const id = route.params.id
  if (!id) return
  loading.value = true
  try {
    const [infoRes, signalRes] = await Promise.all([
      getDbcFile(id),
      getDbcSignalDetails(id)
    ])
    dbcInfo.value = infoRes.data || {}
    allSignals.value = signalRes.data || []
  } catch {
    ElMessage.error('加载信号详情失败')
  } finally {
    loading.value = false
  }
}

const applyFilter = () => {
  activeKeyword.value = keyword.value
  pagination.current = 1
}

const resetFilter = () => {
  keyword.value = ''
  activeKeyword.value = ''
  pagination.current = 1
}

const goBack = () => {
  router.push('/config/can-model')
}

onMounted(loadDetail)
</script>

<style scoped>
.can-model-detail {
  padding: 0;
}

.header-actions {
  display: flex;
  align-items: center;
}

.title-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.title {
  font-size: 16px;
  font-weight: 600;
}

.meta-desc {
  margin-bottom: 16px;
}

.search-form {
  margin-bottom: 12px;
}
</style>
