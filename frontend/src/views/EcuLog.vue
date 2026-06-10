<template>
  <div class="ecu-log">
    <el-card>
      <template #header>
        <span>日志分析</span>
      </template>
      
      <el-form ref="queryFormRef" :inline="true" :model="queryForm" :rules="queryRules" class="search-form">
        <el-form-item label="VIN号" prop="vin">
          <el-autocomplete
            v-model="queryForm.vin"
            class="search-vin"
            :fetch-suggestions="queryVinSuggestions"
            :trigger-on-focus="true"
            placeholder="请输入VIN号"
            clearable
            @select="handleVinSelect"
          />
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            class="search-date-range"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            :disabled-date="disabledDate"
            :disabled-time="disabledTime"
            @calendar-change="handleCalendarChange"
            @change="handleDateRangeChange"
          />
        </el-form-item>
        <el-form-item label="控制器部件">
          <el-select
            v-model="queryForm.ecuType"
            class="search-ecu-type"
            placeholder="请选择"
            clearable
          >
            <el-option label="EMS" value="EMS" />
            <el-option label="BCM" value="BCM" />
            <el-option label="ABS" value="ABS" />
            <el-option label="TCU" value="TCU" />
            <el-option label="BMS" value="BMS" />
            <el-option label="TPMS" value="TPMS" />
            <el-option label="EPS" value="EPS" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="vin" label="VIN" width="170" show-overflow-tooltip />
        <el-table-column prop="ecuType" label="控制器" width="90" />
        <el-table-column prop="logStartTime" label="日志开始" width="165" />
        <el-table-column prop="logEndTime" label="日志结束" width="165" />
        <el-table-column prop="uploadStartTime" label="上传开始" width="165" />
        <el-table-column prop="uploadEndTime" label="上传结束" width="165" />
        <el-table-column prop="fileName" label="文件名" width="150" show-overflow-tooltip />
        <el-table-column prop="fileSize" label="大小" width="100">
          <template #default="{ row }">{{ formatSize(row.fileSize) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleDownload(row)">下载</el-button>
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
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import { getEcuLogPage, downloadLog } from '@/api/ecuLog'
import { getVehiclePage } from '@/api/vehicle'

const MAX_RANGE_MONTHS = 1
const RECENT_VIN_KEY = 'ecu-log-recent-vins'
const MAX_RECENT_VINS = 5

const loading = ref(false)
const tableData = ref([])
const rangeAnchor = ref(null)
const queryFormRef = ref(null)

const queryForm = reactive({
  vin: '',
  ecuType: ''
})

const queryRules = {
  vin: [{ required: true, message: '请输入VIN号', trigger: 'blur' }]
}

const dateRange = ref([])

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const buildDisabledTime = (date) => {
  const now = dayjs()
  const selected = dayjs(date)
  if (!selected.isSame(now, 'day')) {
    return {}
  }
  const hour = now.hour()
  const minute = now.minute()
  const second = now.second()
  return {
    disabledHours: () => Array.from({ length: 24 - hour - 1 }, (_, i) => hour + 1 + i),
    disabledMinutes: (selectedHour) =>
      selectedHour === hour ? Array.from({ length: 60 - minute - 1 }, (_, i) => minute + 1 + i) : [],
    disabledSeconds: (selectedHour, selectedMinute) =>
      selectedHour === hour && selectedMinute === minute
        ? Array.from({ length: 60 - second - 1 }, (_, i) => second + 1 + i)
        : []
  }
}

const disabledDate = (time) => {
  const day = dayjs(time).startOf('day')
  const today = dayjs().startOf('day')
  if (day.isAfter(today)) {
    return true
  }
  if (rangeAnchor.value) {
    const anchor = dayjs(rangeAnchor.value).startOf('day')
    const minDay = anchor.subtract(MAX_RANGE_MONTHS, 'month').startOf('day')
    const maxDay = anchor.add(MAX_RANGE_MONTHS, 'month').startOf('day')
    const cappedMaxDay = maxDay.isAfter(today) ? today : maxDay
    return day.isBefore(minDay) || day.isAfter(cappedMaxDay)
  }
  return false
}

const disabledTime = (date) => {
  if (!date) {
    return {}
  }
  return buildDisabledTime(date)
}

const handleCalendarChange = (dates) => {
  const [start, end] = dates || []
  rangeAnchor.value = start && !end ? start : null
}

const normalizeDateRange = (val, showMessage = true) => {
  if (!val || val.length !== 2) {
    return val
  }

  const now = dayjs()
  let start = dayjs(val[0])
  let end = dayjs(val[1])
  let adjusted = false

  if (end.isAfter(now)) {
    end = now
    adjusted = true
  }
  if (start.isAfter(now)) {
    start = now
    adjusted = true
  }
  if (end.isBefore(start)) {
    start = end
    adjusted = true
  }

  const maxEnd = start.add(MAX_RANGE_MONTHS, 'month')
  if (end.isAfter(maxEnd)) {
    end = maxEnd.isAfter(now) ? now : maxEnd
    adjusted = true
    if (showMessage) {
      ElMessage.warning('时间范围最多选择1个月')
    }
  }

  if (!adjusted) {
    return val
  }
  return [start.format('YYYY-MM-DD HH:mm:ss'), end.format('YYYY-MM-DD HH:mm:ss')]
}

const handleDateRangeChange = (val) => {
  rangeAnchor.value = null
  if (!val || val.length !== 2) {
    return
  }
  const normalized = normalizeDateRange(val)
  if (normalized !== val) {
    dateRange.value = normalized
  }
}

const validateDateRange = () => {
  if (!dateRange.value?.length) {
    return true
  }
  if (dateRange.value.length !== 2) {
    ElMessage.warning('请选择完整的时间范围')
    return false
  }
  const normalized = normalizeDateRange(dateRange.value, false)
  if (normalized !== dateRange.value) {
    dateRange.value = normalized
    ElMessage.warning('时间范围不能超过当前时间，且最多选择1个月')
    return false
  }
  return true
}

const getRecentVins = () => {
  try {
    const list = JSON.parse(localStorage.getItem(RECENT_VIN_KEY) || '[]')
    return Array.isArray(list) ? list.filter(Boolean) : []
  } catch {
    return []
  }
}

const saveRecentVin = (vin) => {
  const trimmed = vin?.trim()
  if (!trimmed) {
    return
  }
  const list = getRecentVins().filter((item) => item !== trimmed)
  list.unshift(trimmed)
  localStorage.setItem(RECENT_VIN_KEY, JSON.stringify(list.slice(0, MAX_RECENT_VINS)))
}

const queryVinSuggestions = async (queryString, callback) => {
  const keyword = (queryString || '').trim()

  if (!keyword) {
    callback(getRecentVins().slice(0, MAX_RECENT_VINS).map((vin) => ({ value: vin })))
    return
  }

  const lowerKeyword = keyword.toLowerCase()
  const recentMatches = getRecentVins()
    .filter((vin) => vin.toLowerCase().includes(lowerKeyword))
    .map((vin) => ({ value: vin }))

  try {
    const res = await getVehiclePage({ current: 1, size: 10, keyword })
    const records = res.data?.records || []
    const seen = new Set(recentMatches.map((item) => item.value))
    const apiMatches = records
      .map((item) => item.vin)
      .filter((vin) => vin && !seen.has(vin) && vin.toLowerCase().includes(lowerKeyword))
      .map((vin) => ({ value: vin }))

    callback([...recentMatches, ...apiMatches].slice(0, 10))
  } catch {
    callback(recentMatches)
  }
}

const handleVinSelect = (item) => {
  queryForm.vin = item.value
}

const validateQueryForm = async () => {
  if (!queryFormRef.value) {
    return false
  }
  try {
    await queryFormRef.value.validate()
    return true
  } catch {
    return false
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size,
      vin: queryForm.vin.trim(),
      ecuType: queryForm.ecuType || undefined
    }
    if (dateRange.value?.length === 2) {
      params.startTime = dateRange.value[0]
      params.endTime = dateRange.value[1]
    }
    const res = await getEcuLogPage(params)
    tableData.value = res.data.records
    pagination.total = res.data.total
    saveRecentVin(queryForm.vin)
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '日志查询失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = async () => {
  if (!(await validateQueryForm())) {
    return
  }
  if (!validateDateRange()) {
    return
  }
  pagination.current = 1
  loadData()
}

const handleReset = () => {
  queryForm.vin = ''
  queryForm.ecuType = ''
  dateRange.value = []
  rangeAnchor.value = null
  tableData.value = []
  pagination.total = 0
  pagination.current = 1
  queryFormRef.value?.clearValidate()
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

const handleSizeChange = async (size) => {
  pagination.size = size
  if (!(await validateQueryForm()) || !validateDateRange()) {
    return
  }
  loadData()
}

const handleCurrentChange = async (current) => {
  pagination.current = current
  if (!(await validateQueryForm()) || !validateDateRange()) {
    return
  }
  loadData()
}

const formatSize = (bytes) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return (bytes / Math.pow(k, i)).toFixed(2) + ' ' + sizes[i]
}
</script>

<style scoped>
.ecu-log {
  padding: 20px;
}

.search-form {
  margin-bottom: 12px;
}

.search-vin {
  width: 200px;
}

.search-date-range {
  width: 360px;
}

.search-ecu-type {
  width: 140px;
}
</style>
