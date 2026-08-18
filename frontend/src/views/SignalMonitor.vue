<template>
  <div>
    <el-card style="margin-bottom: 16px">
      <template #header>
        <span>🔌 WebSocket 实时信号监控</span>
        <el-tag :type="wsStatus === 'connected' ? 'success' : 'danger'" style="margin-left: 12px">
          {{ wsStatusText }}
        </el-tag>
      </template>

      <el-row :gutter="16">
        <el-col :span="6">
          <el-input v-model="vinInput" placeholder="输入 VIN 订阅（留空为全局广播）" clearable>
            <template #append>
              <el-button @click="subscribeVin" :disabled="wsStatus !== 'connected'">订阅</el-button>
            </template>
          </el-input>
        </el-col>
        <el-col :span="6">
          <el-button @click="unsubscribeVin" :disabled="!subscribedVin">取消订阅</el-button>
          <el-button @click="reconnect">重新连接</el-button>
        </el-col>
        <el-col :span="6" style="text-align: right">
          <el-statistic title="已接收信号数" :value="signalCount" />
        </el-col>
        <el-col :span="6" style="text-align: right">
          <el-statistic title="当前 VIN" :value="subscribedVin || '全局广播'" />
        </el-col>
      </el-row>
    </el-card>

    <!-- 实时信号表格 -->
    <el-card>
      <template #header>
        <span>📊 实时信号数据</span>
        <el-button size="small" type="danger" @click="clearSignals" style="float: right">清空</el-button>
      </template>

      <el-table :data="recentSignals" style="width: 100%" max-height="500" stripe highlight-current-row>
        <el-table-column prop="timestamp" label="时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.timestamp) }}
          </template>
        </el-table-column>
        <el-table-column prop="vin" label="VIN" width="160" />
        <el-table-column prop="messageName" label="消息" width="150" />
        <el-table-column prop="signalCount" label="信号数量" width="100" align="center" />
        <el-table-column label="信号详情">
          <template #default="{ row }">
            <div v-for="(sig, idx) in row.signals" :key="idx" style="display: inline-block; margin-right: 16px; margin-bottom: 4px">
              <el-tag size="small" type="info">{{ sig.name }}</el-tag>
              <span style="font-weight: bold; margin: 0 4px">{{ sig.value }}</span>
              <span v-if="sig.unit" style="color: #999; font-size: 12px">{{ sig.unit }}</span>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="recentSignals.length === 0" description="等待信号数据..." />
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import wsManager from '../utils/websocket'

const wsStatus = ref('disconnected')
const wsStatusText = computed(() => wsStatus.value === 'connected' ? '已连接' : '已断开')
const vinInput = ref('')
const subscribedVin = ref(null)
const signalCount = ref(0)
const recentSignals = ref([])
const MAX_SIGNALS = 100

onMounted(() => {
  wsManager.on('connected', () => {
    wsStatus.value = 'connected'
  })
  wsManager.on('disconnected', () => {
    wsStatus.value = 'disconnected'
  })
  wsManager.on('signal', handleSignal)
  wsManager.on('subscribed', (data) => {
    console.log('[WS] Subscribed:', data)
  })
  wsManager.connect()
})

onUnmounted(() => {
  wsManager.disconnect()
})

function handleSignal(data) {
  signalCount.value++
  const record = {
    vin: data.vin,
    timestamp: data.timestamp,
    messageName: data.signals?.[0]?.messageName || '',
    signalCount: data.signals?.length || 0,
    signals: data.signals || []
  }
  recentSignals.value.unshift(record)
  if (recentSignals.value.length > MAX_SIGNALS) {
    recentSignals.value = recentSignals.value.slice(0, MAX_SIGNALS)
  }
}

function subscribeVin() {
  const vin = vinInput.value.trim()
  if (vin) {
    wsManager.subscribeVin(vin)
    subscribedVin.value = vin
  }
}

function unsubscribeVin() {
  wsManager.unsubscribe()
  subscribedVin.value = null
}

function reconnect() {
  wsManager.disconnect()
  wsManager.connect(subscribedVin.value)
}

function clearSignals() {
  recentSignals.value = []
  signalCount.value = 0
}

function formatTime(ts) {
  if (!ts) return ''
  return new Date(ts).toLocaleTimeString('zh-CN', { hour12: false })
}
</script>
