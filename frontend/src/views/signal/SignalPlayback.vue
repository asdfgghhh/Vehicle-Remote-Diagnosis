<template>
  <div class="signal-playback">
    <div class="page-title">信号回放</div>

    <div class="top-bar">
      <el-form ref="queryFormRef" :inline="true" :model="queryForm" :rules="queryRules" class="top-form">
        <el-form-item label="VIN" prop="vin">
          <el-autocomplete
            v-model="queryForm.vin"
            class="vin-input"
            :fetch-suggestions="queryVinSuggestions"
            :trigger-on-focus="true"
            placeholder="请输入VIN号"
            clearable
            @select="handleVinSelect"
            @blur="loadVehicleInfo"
          />
        </el-form-item>
        <el-form-item v-if="vehicleTags.length" class="vehicle-tags">
          <el-tag v-for="tag in vehicleTags" :key="tag" size="small" type="info">{{ tag }}</el-tag>
        </el-form-item>
        <el-form-item label="数据时间">
          <el-date-picker
            v-model="dateRange"
            class="date-range"
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
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleQuery">查询回放</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="main-body">
      <aside class="signal-sidebar">
        <div class="sidebar-header">
          <el-select v-model="signalType" class="signal-type" size="small">
            <el-option label="CAN信号" value="CAN" />
          </el-select>
          <el-button type="primary" size="small" @click="openAddSignalDialog">
            <el-icon><Plus /></el-icon>
            添加信号
          </el-button>
        </div>

        <div class="sidebar-search">
          <el-input
            v-model="signalKeyword"
            placeholder="请输入搜索关键字"
            clearable
            size="small"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>

        <div class="sidebar-toolbar">
          <span class="signal-count">已添加信号({{ addedSignals.length }}/{{ MAX_SIGNALS }})</span>
          <div class="toolbar-actions">
            <el-tooltip content="全部显示/隐藏">
              <el-button link @click="toggleAllVisible">
                <el-icon><View /></el-icon>
              </el-button>
            </el-tooltip>
            <el-tooltip content="刷新图表">
              <el-button link @click="handleQuery">
                <el-icon><RefreshRight /></el-icon>
              </el-button>
            </el-tooltip>
            <el-tooltip content="清空信号">
              <el-button link @click="clearSignals">
                <el-icon><Delete /></el-icon>
              </el-button>
            </el-tooltip>
          </div>
        </div>

        <el-table
          :data="filteredSignals"
          size="small"
          height="100%"
          class="signal-table"
          @row-click="handleSignalRowClick"
        >
          <el-table-column width="40">
            <template #default="{ row }">
              <el-checkbox v-model="row.visible" @change="renderChart" @click.stop />
            </template>
          </el-table-column>
          <el-table-column label="信号名称" min-width="120">
            <template #default="{ row }">
              <span class="signal-name">
                <i class="color-dot" :style="{ backgroundColor: row.color }" />
                {{ row.name }}
              </span>
            </template>
          </el-table-column>
          <el-table-column width="40" fixed="right">
            <template #default="{ row }">
              <el-button link type="danger" @click.stop="removeSignal(row.name)">
                <el-icon><Close /></el-icon>
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </aside>

      <section class="chart-panel">
        <div class="chart-toolbar">
          <el-button size="small" disabled>+ 添加条件 (0/5)</el-button>
          <div class="chart-toolbar-right">
            <span v-if="dateRangeLabel" class="time-label">{{ dateRangeLabel }}</span>
            <el-button link @click="renderChart"><el-icon><RefreshRight /></el-icon></el-button>
            <el-button link @click="toggleFullscreen"><el-icon><FullScreen /></el-icon></el-button>
          </div>
        </div>

        <div ref="chartContainerRef" class="chart-container" v-loading="loading">
          <div v-if="!hasChartData" class="chart-empty">
            <el-empty description="暂无数据，请先选择信号" />
          </div>
          <div v-show="hasChartData" ref="chartRef" class="chart-area" />
        </div>

        <div class="playback-bar">
          <div class="playback-scale">
            <span>{{ playbackStartLabel }}</span>
            <span>{{ playbackEndLabel }}</span>
          </div>
          <el-slider
            v-model="playbackRange"
            range
            :min="0"
            :max="100"
            :disabled="!hasChartData"
            @change="renderChart"
          />
        </div>
      </section>
    </div>

    <el-dialog v-model="addDialogVisible" title="添加信号" width="720px" destroy-on-close>
      <div class="add-dialog-toolbar">
        <el-select
          v-model="selectedDbcId"
          placeholder="选择DBC配置"
          filterable
          style="width: 260px;"
          @change="loadDbcSignals"
        >
          <el-option
            v-for="item in dbcFiles"
            :key="item.id"
            :label="`${item.fileName} (${item.signalCount || 0})`"
            :value="item.id"
          />
        </el-select>
        <el-input
          v-model="dbcSignalKeyword"
          placeholder="搜索信号名称/报文"
          clearable
          style="width: 220px;"
        />
        <span class="add-quota">还可添加 {{ remainingSlots }} 个</span>
      </div>

      <el-table
        ref="dbcSignalTableRef"
        :data="filteredDbcSignals"
        height="360"
        v-loading="dbcLoading"
        @selection-change="handleDbcSelectionChange"
      >
        <el-table-column type="selection" width="48" :selectable="isDbcSignalSelectable" />
        <el-table-column prop="name" label="信号名称" min-width="160" />
        <el-table-column prop="messageName" label="报文名称" min-width="160" />
      </el-table>

      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" :disabled="!pendingSelections.length" @click="confirmAddSignals">
          添加选中 ({{ pendingSelections.length }})
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Search, View, RefreshRight, Delete, Close, FullScreen
} from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import { getSignalTimeline } from '@/api/signal'
import { getVehiclePage, getVehicleByVin } from '@/api/vehicle'
import { getDbcFilePage, getDbcSignals } from '@/api/dbc'

const MAX_SIGNALS = 50
const MAX_RANGE_MONTHS = 1
const RECENT_VIN_KEY = 'signal-playback-recent-vins'
const MAX_RECENT_VINS = 5
const CHART_COLORS = [
  '#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399',
  '#00C9A7', '#845EC2', '#D65DB1', '#FF6F91', '#FFC75F'
]

const loading = ref(false)
const dbcLoading = ref(false)
const queryFormRef = ref(null)
const chartRef = ref(null)
const chartContainerRef = ref(null)
const dbcSignalTableRef = ref(null)

const queryForm = reactive({ vin: '' })
const queryRules = {
  vin: [{ required: true, message: '请输入VIN号', trigger: 'blur' }]
}

const dateRange = ref([])
const rangeAnchor = ref(null)
const vehicleInfo = ref(null)
const signalType = ref('CAN')
const signalKeyword = ref('')
const addedSignals = ref([])
const signalData = ref([])
const playbackRange = ref([0, 100])

const addDialogVisible = ref(false)
const dbcFiles = ref([])
const selectedDbcId = ref(null)
const dbcSignals = ref([])
const dbcSignalKeyword = ref('')
const pendingSelections = ref([])

let chartInstance = null
let vehicleId = null

const vehicleTags = computed(() => {
  if (!vehicleInfo.value) return []
  const tags = []
  if (vehicleInfo.value.plateNumber) tags.push(vehicleInfo.value.plateNumber)
  if (vehicleInfo.value.color) tags.push(vehicleInfo.value.color)
  tags.push('已入库')
  return tags
})

const filteredSignals = computed(() => {
  const keyword = signalKeyword.value.trim().toLowerCase()
  if (!keyword) return addedSignals.value
  return addedSignals.value.filter((item) =>
    item.name.toLowerCase().includes(keyword) ||
    item.messageName.toLowerCase().includes(keyword)
  )
})

const filteredDbcSignals = computed(() => {
  const keyword = dbcSignalKeyword.value.trim().toLowerCase()
  if (!keyword) return dbcSignals.value
  return dbcSignals.value.filter((item) =>
    item.name.toLowerCase().includes(keyword) ||
    (item.messageName || '').toLowerCase().includes(keyword)
  )
})

const remainingSlots = computed(() => Math.max(0, MAX_SIGNALS - addedSignals.value.length))

const hasChartData = computed(() =>
  signalData.value.length > 0 && addedSignals.value.some((item) => item.visible)
)

const dateRangeLabel = computed(() => {
  if (!dateRange.value?.length) return ''
  return `${dateRange.value[0]} ~ ${dateRange.value[1]}`
})

const queryTimeBounds = computed(() => {
  if (!dateRange.value?.length) return null
  return {
    start: dayjs(dateRange.value[0]),
    end: dayjs(dateRange.value[1])
  }
})

const playbackStartLabel = computed(() => {
  if (!queryTimeBounds.value) return '00:00'
  const { start, end } = queryTimeBounds.value
  const [minPct] = playbackRange.value
  return start.add(end.diff(start) * minPct / 100, 'millisecond').format('HH:mm:ss')
})

const playbackEndLabel = computed(() => {
  if (!queryTimeBounds.value) return '23:59'
  const { start, end } = queryTimeBounds.value
  const [, maxPct] = playbackRange.value
  return start.add(end.diff(start) * maxPct / 100, 'millisecond').format('HH:mm:ss')
})

onMounted(() => {
  initDefaultDateRange()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance?.dispose()
})

const initDefaultDateRange = () => {
  const end = dayjs()
  const start = end.subtract(2, 'hour')
  dateRange.value = [start.format('YYYY-MM-DD HH:mm:ss'), end.format('YYYY-MM-DD HH:mm:ss')]
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
  if (!trimmed) return
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
  loadVehicleInfo()
}

const loadVehicleInfo = async () => {
  const vin = queryForm.vin?.trim()
  if (!vin) {
    vehicleInfo.value = null
    vehicleId = null
    return
  }
  try {
    const res = await getVehicleByVin(vin)
    vehicleInfo.value = res.data
    vehicleId = res.data?.id || null
  } catch {
    vehicleInfo.value = null
    vehicleId = null
  }
}

const buildDisabledTime = (date) => {
  const now = dayjs()
  const selected = dayjs(date)
  if (!selected.isSame(now, 'day')) return {}
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
  if (day.isAfter(today)) return true
  if (rangeAnchor.value) {
    const anchor = dayjs(rangeAnchor.value).startOf('day')
    const minDay = anchor.subtract(MAX_RANGE_MONTHS, 'month').startOf('day')
    const maxDay = anchor.add(MAX_RANGE_MONTHS, 'month').startOf('day')
    const cappedMaxDay = maxDay.isAfter(today) ? today : maxDay
    return day.isBefore(minDay) || day.isAfter(cappedMaxDay)
  }
  return false
}

const disabledTime = (date) => (date ? buildDisabledTime(date) : {})

const handleCalendarChange = (dates) => {
  const [start, end] = dates || []
  rangeAnchor.value = start && !end ? start : null
}

const normalizeDateRange = (val, showMessage = true) => {
  if (!val || val.length !== 2) return val
  const now = dayjs()
  let start = dayjs(val[0])
  let end = dayjs(val[1])
  let adjusted = false
  if (end.isAfter(now)) { end = now; adjusted = true }
  if (start.isAfter(now)) { start = now; adjusted = true }
  if (end.isBefore(start)) { start = end; adjusted = true }
  const maxEnd = start.add(MAX_RANGE_MONTHS, 'month')
  if (end.isAfter(maxEnd)) {
    end = maxEnd.isAfter(now) ? now : maxEnd
    adjusted = true
    if (showMessage) ElMessage.warning('时间范围最多选择1个月')
  }
  return adjusted
    ? [start.format('YYYY-MM-DD HH:mm:ss'), end.format('YYYY-MM-DD HH:mm:ss')]
    : val
}

const handleDateRangeChange = (val) => {
  rangeAnchor.value = null
  if (!val || val.length !== 2) return
  const normalized = normalizeDateRange(val)
  if (normalized !== val) dateRange.value = normalized
}

const validateDateRange = () => {
  if (!dateRange.value?.length || dateRange.value.length !== 2) {
    ElMessage.warning('请选择数据时间范围')
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

const resolveVehicleId = async () => {
  if (vehicleId) return vehicleId
  await loadVehicleInfo()
  return vehicleId
}

const openAddSignalDialog = async () => {
  if (addedSignals.value.length >= MAX_SIGNALS) {
    ElMessage.warning(`最多添加 ${MAX_SIGNALS} 个信号`)
    return
  }
  addDialogVisible.value = true
  pendingSelections.value = []
  dbcSignalKeyword.value = ''
  if (!dbcFiles.value.length) {
    try {
      const res = await getDbcFilePage({ current: 1, size: 50 })
      dbcFiles.value = res.data?.records || []
      if (dbcFiles.value.length && !selectedDbcId.value) {
        selectedDbcId.value = dbcFiles.value[0].id
      }
    } catch {
      ElMessage.error('加载DBC配置失败')
    }
  }
  if (selectedDbcId.value) {
    await loadDbcSignals()
  }
}

const loadDbcSignals = async () => {
  if (!selectedDbcId.value) return
  dbcLoading.value = true
  try {
    const res = await getDbcSignals(selectedDbcId.value)
    dbcSignals.value = res.data || []
  } catch {
    dbcSignals.value = []
    ElMessage.error('加载信号列表失败')
  } finally {
    dbcLoading.value = false
  }
}

const isDbcSignalSelectable = (row) => {
  if (addedSignals.value.some((item) => item.name === row.name)) return false
  const selectedCount = pendingSelections.value.length
  const alreadyAdded = addedSignals.value.length
  const isPending = pendingSelections.value.some((item) => item.name === row.name)
  if (isPending) return true
  return alreadyAdded + selectedCount < MAX_SIGNALS
}

const handleDbcSelectionChange = (rows) => {
  const maxPick = remainingSlots.value
  if (rows.length > maxPick) {
    ElMessage.warning(`最多还能添加 ${maxPick} 个信号`)
    const trimmed = rows.slice(0, maxPick)
    pendingSelections.value = trimmed
    nextTick(() => {
      dbcSignalTableRef.value?.clearSelection()
      trimmed.forEach((row) => dbcSignalTableRef.value?.toggleRowSelection(row, true))
    })
    return
  }
  pendingSelections.value = rows
}

const pickColor = (index) => CHART_COLORS[index % CHART_COLORS.length]

const confirmAddSignals = () => {
  const existing = new Set(addedSignals.value.map((item) => item.name))
  const toAdd = pendingSelections.value.filter((item) => !existing.has(item.name))
  const allowed = toAdd.slice(0, remainingSlots.value)
  allowed.forEach((item, index) => {
    addedSignals.value.push({
      name: item.name,
      messageName: item.messageName || '',
      visible: true,
      color: pickColor(addedSignals.value.length + index)
    })
  })
  addDialogVisible.value = false
  ElMessage.success(`已添加 ${allowed.length} 个信号`)
}

const removeSignal = (name) => {
  addedSignals.value = addedSignals.value.filter((item) => item.name !== name)
  renderChart()
}

const clearSignals = async () => {
  if (!addedSignals.value.length) return
  try {
    await ElMessageBox.confirm('确认清空已添加的信号？', '提示', { type: 'warning' })
    addedSignals.value = []
    signalData.value = []
    renderChart()
  } catch {
    // cancelled
  }
}

const toggleAllVisible = () => {
  if (!addedSignals.value.length) return
  const allVisible = addedSignals.value.every((item) => item.visible)
  addedSignals.value.forEach((item) => { item.visible = !allVisible })
  renderChart()
}

const handleSignalRowClick = (row) => {
  row.visible = !row.visible
  renderChart()
}

const handleQuery = async () => {
  try {
    await queryFormRef.value?.validate()
  } catch {
    return
  }
  if (!validateDateRange()) return
  if (!addedSignals.value.length) {
    ElMessage.warning('请先添加信号')
    return
  }

  const id = await resolveVehicleId()
  if (!id) {
    ElMessage.warning('未找到该VIN对应车辆')
    return
  }

  loading.value = true
  try {
    const res = await getSignalTimeline({
      vehicleId: id,
      vin: queryForm.vin.trim(),
      startTime: dateRange.value[0],
      endTime: dateRange.value[1]
    })
    const selectedNames = new Set(addedSignals.value.map((item) => item.name))
    signalData.value = (res.data?.signals || []).filter((item) => selectedNames.has(item.signalName))
    saveRecentVin(queryForm.vin)
    playbackRange.value = [0, 100]
    await nextTick()
    renderChart()
    if (!signalData.value.length) {
      ElMessage.info('所选时间范围内暂无信号数据')
    }
  } catch {
    ElMessage.error('信号回放查询失败')
  } finally {
    loading.value = false
  }
}

const getPlaybackWindow = () => {
  if (!queryTimeBounds.value) return null
  const { start, end } = queryTimeBounds.value
  const totalMs = end.diff(start)
  const [minPct, maxPct] = playbackRange.value
  return {
    start: start.add(totalMs * minPct / 100, 'millisecond'),
    end: start.add(totalMs * maxPct / 100, 'millisecond')
  }
}

const renderChart = () => {
  if (!chartRef.value) return
  if (!hasChartData.value) {
    chartInstance?.clear()
    return
  }

  const window = getPlaybackWindow()

  const series = addedSignals.value
    .filter((item) => item.visible)
    .map((item) => {
      const data = signalData.value
        .filter((point) => point.signalName === item.name)
        .filter((point) => {
          if (!window) return true
          const time = dayjs(point.signalTime)
          return !time.isBefore(window.start) && !time.isAfter(window.end)
        })
        .map((point) => [point.signalTime, Number(point.numericValue ?? point.signalValue) || 0])
      return {
        name: item.name,
        type: 'line',
        showSymbol: false,
        smooth: true,
        lineStyle: { width: 1.5, color: item.color },
        itemStyle: { color: item.color },
        data
      }
    })
    .filter((item) => item.data.length > 0)

  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }

  chartInstance.setOption({
    tooltip: { trigger: 'axis' },
    legend: {
      type: 'scroll',
      top: 0,
      data: series.map((item) => item.name)
    },
    grid: { left: 48, right: 24, top: 48, bottom: 24 },
    xAxis: { type: 'time', boundaryGap: false },
    yAxis: { type: 'value', scale: true },
    series
  }, true)
}

const handleResize = () => chartInstance?.resize()

const toggleFullscreen = () => {
  const el = chartContainerRef.value
  if (!el) return
  if (document.fullscreenElement) {
    document.exitFullscreen()
  } else {
    el.requestFullscreen?.()
  }
}
</script>

<style scoped>
.signal-playback {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 120px);
  padding: 16px 20px 20px;
  background: #f5f7fa;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
}

.top-bar {
  background: #fff;
  border-radius: 4px;
  padding: 12px 16px 4px;
  margin-bottom: 12px;
}

.top-form {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
}

.vin-input {
  width: 220px;
}

.date-range {
  width: 360px;
}

.vehicle-tags {
  margin-bottom: 0;
}

.vehicle-tags :deep(.el-tag) {
  margin-right: 6px;
}

.main-body {
  flex: 1;
  min-height: 0;
  display: flex;
  gap: 12px;
}

.signal-sidebar {
  width: 300px;
  background: #fff;
  border-radius: 4px;
  padding: 12px;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.sidebar-header {
  display: flex;
  gap: 8px;
  margin-bottom: 10px;
}

.signal-type {
  flex: 1;
}

.sidebar-search {
  margin-bottom: 10px;
}

.sidebar-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 13px;
  color: #606266;
}

.toolbar-actions {
  display: flex;
  gap: 2px;
}

.signal-table {
  flex: 1;
}

.signal-name {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.color-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.chart-panel {
  flex: 1;
  min-width: 0;
  background: #fff;
  border-radius: 4px;
  padding: 12px;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.chart-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.chart-toolbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.time-label {
  font-size: 12px;
  color: #909399;
}

.chart-container {
  flex: 1;
  min-height: 0;
  position: relative;
}

.chart-empty,
.chart-area {
  width: 100%;
  height: 100%;
}

.chart-empty {
  display: flex;
  align-items: center;
  justify-content: center;
}

.playback-bar {
  margin-top: 12px;
  padding-top: 8px;
  border-top: 1px solid #ebeef5;
}

.playback-scale {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.add-dialog-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.add-quota {
  margin-left: auto;
  font-size: 13px;
  color: #909399;
}
</style>
