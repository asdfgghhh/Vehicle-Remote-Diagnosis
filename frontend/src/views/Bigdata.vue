<template>
  <div class="bigdata-view">
    <el-card>
      <template #header>
        <span>大数据存储</span>
      </template>
      
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="数据类型">
          <el-select v-model="queryForm.dataType" placeholder="请选择数据类型">
            <el-option label="信号数据" value="signals" />
            <el-option label="日志数据" value="logs" />
          </el-select>
        </el-form-item>
        <el-form-item label="可用日期">
          <el-select v-model="queryForm.date" placeholder="请选择日期">
            <el-option
              v-for="date in availableDates"
              :key="date"
              :label="date"
              :value="date"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button type="success" @click="handleRefresh">刷新</el-button>
        </el-form-item>
      </el-form>
      
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>统计信息</span>
            </template>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="数据类型">{{ queryForm.dataType }}</el-descriptions-item>
              <el-descriptions-item label="日期">{{ queryForm.date }}</el-descriptions-item>
              <el-descriptions-item label="文件数量">{{ statistics.totalFiles }}</el-descriptions-item>
              <el-descriptions-item label="总大小">{{ formatSize(statistics.totalSize) }}</el-descriptions-item>
              <el-descriptions-item label="记录数量">{{ statistics.recordCount }}</el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>HDFS文件列表</span>
            </template>
            <el-list>
              <el-list-item v-for="file in hdfsFiles" :key="file">
                <el-icon><Document /></el-icon>
                {{ file }}
              </el-list-item>
            </el-list>
          </el-card>
        </el-col>
      </el-row>
      
      <el-card style="margin-top: 20px;">
        <template #header>
          <span>数据预览</span>
        </template>
        <el-table :data="previewData" stripe height="300">
          <el-table-column v-for="col in previewColumns" :key="col" :prop="col" :label="col" />
        </el-table>
      </el-card>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Document } from '@element-plus/icons-vue'
import { getAvailableDates, getStatistics, listFiles, queryBigdataSignals } from '@/api/bigdata'

const availableDates = ref([])
const hdfsFiles = ref([])
const previewData = ref([])
const previewColumns = ref([])

const queryForm = reactive({
  dataType: 'signals',
  date: ''
})

const statistics = reactive({
  totalFiles: 0,
  totalSize: 0,
  recordCount: 0
})

onMounted(() => {
  loadAvailableDates()
})

const loadAvailableDates = async () => {
  try {
    const res = await getAvailableDates(queryForm.dataType)
    availableDates.value = res.data || []
    if (availableDates.value.length > 0) {
      queryForm.date = availableDates.value[0]
      handleQuery()
    }
  } catch (error) {
    console.error('加载可用日期失败', error)
  }
}

const handleQuery = async () => {
  try {
    const statsRes = await getStatistics({
      dataType: queryForm.dataType,
      startTime: queryForm.date,
      endTime: queryForm.date
    })
    Object.assign(statistics, statsRes.data)
    
    const path = queryForm.dataType === 'signals' 
      ? `/vrd/data/signals/${queryForm.date}`
      : `/vrd/data/logs/${queryForm.date}`
    
    const filesRes = await listFiles(path)
    hdfsFiles.value = filesRes.data || []
    
    if (filesRes.data && filesRes.data.length > 0) {
      loadPreviewData()
    }
  } catch (error) {
    console.error('查询失败', error)
  }
}

const loadPreviewData = async () => {
  try {
    const res = await queryBigdataSignals({
      vehicleId: 1,
      startTime: queryForm.date,
      endTime: queryForm.date
    })
    
    if (res.data && res.data.data) {
      previewData.value = res.data.data.slice(0, 100)
      if (previewData.value.length > 0) {
        previewColumns.value = Object.keys(previewData.value[0])
      }
    }
  } catch (error) {
    console.error('加载预览数据失败', error)
  }
}

const handleRefresh = () => {
  loadAvailableDates()
}

const formatSize = (bytes) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return (bytes / Math.pow(k, i)).toFixed(2) + ' ' + sizes[i]
}
</script>

<style scoped>
.bigdata-view {
  padding: 20px;
}

.search-form {
  margin-bottom: 20px;
}
</style>
