<template>
  <div class="fault-analysis">
    <!-- 页面头部 -->
    <div class="page-title">故障分析</div>
    <div class="page-desc">VHR 趋势分析与风险预测 · 故障码排名 · AI 根因定位 · 改进闭环</div>
    <!-- 故障趋势 -->
    <el-card style="margin-bottom: 16px">
      <template #header>
        <div class="chart-head">
          <span>📈 故障趋势</span>
          <el-radio-group v-model="granularity" size="small" @change="loadTrend">
            <el-radio-button value="day">按天</el-radio-button>
            <el-radio-button value="week">按周</el-radio-button>
            <el-radio-button value="month">按月</el-radio-button>
          </el-radio-group>
        </div>
      </template>
      <div ref="trendRef" class="chart-box"></div>
    </el-card>

    <el-row :gutter="16">
      <el-col :span="12">
        <el-card>
          <template #header><span>📊 故障级别分布</span></template>
          <div ref="levelRef" class="chart-box"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header><span>🔧 故障域分布</span></template>
          <div ref="domainRef" class="chart-box"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="12">
        <el-card>
          <template #header><span>🌳 场景分布</span></template>
          <div ref="sceneRef" class="chart-box"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header><span>🏆 故障码 TOP N</span></template>
          <div ref="codeRef" class="chart-box"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getFaultAnalysisTrend, getFaultAnalysisDistribution } from '@/api/fault'

const granularity = ref('day')
const trendRef = ref(null)
const levelRef = ref(null)
const domainRef = ref(null)
const sceneRef = ref(null)
const codeRef = ref(null)

let trendChart = null
let levelChart = null
let domainChart = null
let sceneChart = null
let codeChart = null

const loadTrend = async () => {
  try {
    const res = await getFaultAnalysisTrend(granularity.value)
    const points = res.data?.points || []
    if (!trendChart) return
    trendChart.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: points.map(p => p.timeLabel),
        axisLabel: { interval: Math.ceil(points.length / 10), rotate: points.length > 15 ? 30 : 0 }
      },
      yAxis: { type: 'value', name: '故障数', minInterval: 1 },
      series: [{
        name: '故障数',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        data: points.map(p => p.faultCount ?? 0),
        itemStyle: { color: '#F56C6C' },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(245, 108, 108, 0.3)' },
              { offset: 1, color: 'rgba(245, 108, 108, 0.03)' }
            ]
          }
        }
      }]
    }, true)
  } catch (e) {
    console.error('加载故障趋势失败', e)
  }
}

const pieOption = (title, data, colors) => ({
  tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
  legend: { type: 'scroll', orient: 'vertical', right: 0, top: 'middle', itemWidth: 10, itemHeight: 10, textStyle: { fontSize: 11 } },
  color: colors,
  series: [{
    type: 'pie',
    radius: ['40%', '65%'],
    center: ['38%', '50%'],
    label: { show: false },
    emphasis: { label: { show: true, formatter: '{b}\n{c}' } },
    data: data.length ? data.map(d => ({ name: d.name, value: d.count })) : [{ name: '暂无数据', value: 0 }]
  }]
})

const barOption = (title, data, color) => ({
  tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
  grid: { left: '3%', right: '10%', bottom: '3%', containLabel: true },
  xAxis: { type: 'value', minInterval: 1 },
  yAxis: {
    type: 'category',
    data: data.length ? data.map(d => d.name) : ['暂无数据'],
    axisLabel: { width: 90, overflow: 'truncate' }
  },
  series: [{
    type: 'bar',
    barMaxWidth: 22,
    data: data.length ? data.map(d => d.count) : [0],
    itemStyle: { color, borderRadius: [0, 4, 4, 0] }
  }]
})

const loadDistribution = async () => {
  try {
    const res = await getFaultAnalysisDistribution()
    const data = res.data || {}
    levelChart?.setOption(pieOption('故障级别分布', data.byLevel || [], ['#F56C6C', '#E6A23C', '#409EFF']), true)
    domainChart?.setOption(pieOption('故障域分布', data.byDomain || [], ['#67C23A', '#409EFF', '#E6A23C', '#F56C6C', '#909399', '#b37feb', '#13c2c2']), true)
    sceneChart?.setOption(barOption('场景分布', (data.byScene || []).slice(0, 8), '#E6A23C'), true)
    codeChart?.setOption(barOption('故障码 TOP N', (data.topFaultCodes || []).slice(0, 10), '#F56C6C'), true)
  } catch (e) {
    console.error('加载故障分布失败', e)
  }
}

const initCharts = () => {
  trendChart = echarts.init(trendRef.value)
  levelChart = echarts.init(levelRef.value)
  domainChart = echarts.init(domainRef.value)
  sceneChart = echarts.init(sceneRef.value)
  codeChart = echarts.init(codeRef.value)
}

const handleResize = () => {
  trendChart?.resize()
  levelChart?.resize()
  domainChart?.resize()
  sceneChart?.resize()
  codeChart?.resize()
}

onMounted(async () => {
  await nextTick()
  initCharts()
  window.addEventListener('resize', handleResize)
  await Promise.all([loadTrend(), loadDistribution()])
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  ;[trendChart, levelChart, domainChart, sceneChart, codeChart].forEach(c => { if (c) { c.dispose(); c = null } })
})
</script>

<style scoped>
.chart-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-box {
  width: 100%;
  height: 320px;
}
</style>
