<template>
  <div class="app">
    <!-- ===== 侧边栏 ===== -->
    <aside class="sidebar">
      <div class="sidebar-logo" @click="router.push('/dashboard')">
        <div class="logo-icon">🚗</div>
        <div class="logo-text">VRD 诊断平台</div>
      </div>
      <nav class="sidebar-nav">
        <div class="nav-group">
          <router-link class="nav-item" :class="{ active: isActive('/dashboard') }" to="/dashboard">
            <span class="nav-icon">📊</span>系统首页
          </router-link>
        </div>

        <div class="nav-group">
          <div class="nav-group-title">健康管理</div>
          <router-link class="nav-item" :class="{ active: isActive('/vehicle/health') }" to="/vehicle/health">
            <span class="nav-icon">🧬</span>车辆健康
          </router-link>
          <router-link class="nav-item" :class="{ active: isActive('/diagnosis/ai') }" to="/diagnosis/ai">
            <span class="nav-icon">🤖</span>AI 诊断<span class="nav-badge">AI</span>
          </router-link>
          <router-link class="nav-item" :class="{ active: isActive('/maintenance') }" to="/maintenance">
            <span class="nav-icon">🛠️</span>智能维保
          </router-link>
        </div>

        <div class="nav-group">
          <div class="nav-group-title">车辆管理</div>
          <router-link class="nav-item" :class="{ active: isActive('/vehicle/model') }" to="/vehicle/model">
            <span class="nav-icon">🏷️</span>车型管理
          </router-link>
          <router-link class="nav-item" :class="{ active: isActive('/vehicle/list') || isActive('/vehicle/detail') }" to="/vehicle/list">
            <span class="nav-icon">🚙</span>车辆管理
          </router-link>
          <router-link class="nav-item" :class="{ active: isActive('/dbc') || isActive('/config/can-model') }" to="/dbc">
            <span class="nav-icon">📁</span>DBC 管理
          </router-link>
        </div>

        <div class="nav-group">
          <div class="nav-group-title">监控诊断</div>
          <router-link class="nav-item" :class="{ active: isActive('/signal') }" to="/signal">
            <span class="nav-icon">📡</span>主动监控
          </router-link>
          <router-link class="nav-item" :class="{ active: isActive('/fault/info') }" to="/fault/info">
            <span class="nav-icon">⚠️</span>故障信息
          </router-link>
          <router-link class="nav-item" :class="{ active: isActive('/diagnosis') && !isActive('/diagnosis/ai') }" to="/diagnosis">
            <span class="nav-icon">🔧</span>远程诊断
          </router-link>
        </div>

        <div class="nav-group">
          <div class="nav-group-title">数据分析</div>
          <router-link class="nav-item" :class="{ active: isActive('/ecu-log') }" to="/ecu-log">
            <span class="nav-icon">📋</span>日志分析
          </router-link>
          <router-link class="nav-item" :class="{ active: isActive('/signal/analysis') }" to="/signal/analysis">
            <span class="nav-icon">📈</span>信号分析
          </router-link>
          <router-link class="nav-item" :class="{ active: isActive('/signal/playback') }" to="/signal/playback">
            <span class="nav-icon">⏯️</span>信号回放
          </router-link>
          <router-link class="nav-item" :class="{ active: isActive('/fault/analysis') }" to="/fault/analysis">
            <span class="nav-icon">🔍</span>故障分析
          </router-link>
        </div>

        <div class="nav-group">
          <div class="nav-group-title">系统管理</div>
          <router-link class="nav-item" :class="{ active: isActive('/settings/user') }" to="/settings/user">
            <span class="nav-icon">👤</span>用户管理
          </router-link>
          <router-link class="nav-item" :class="{ active: isActive('/settings/role') }" to="/settings/role">
            <span class="nav-icon">🛡️</span>权限管理
          </router-link>
        </div>
      </nav>
      <div class="sidebar-user" @click="showUserMenu">
        <div class="avatar">{{ avatarText }}</div>
        <div>
          <div class="user-name">{{ username }}</div>
          <div class="user-role">超级管理员</div>
        </div>
      </div>
    </aside>

    <!-- ===== 主区域 ===== -->
    <div class="main">
      <header class="header">
        <div class="header-breadcrumb">
          <span style="color: var(--text-secondary)">{{ breadcrumb }}</span>
        </div>
        <div class="header-actions">
          <div class="icon-btn" title="全屏" @click="toggleFullscreen">⛶</div>
          <el-dropdown trigger="click" @command="handleCommand">
            <span class="user-info" style="cursor: pointer; display: flex; align-items: center; gap: 8px">
              <span style="color: var(--text-secondary); font-size: 15px">👤</span>
              <span style="font-size: 13px; font-weight: 500">{{ username }}</span>
              <span style="font-size: 11px; color: var(--text-light)">▾</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>
      <div class="content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox } from 'element-plus'

const router = useRouter()
const route = useRoute()

const username = ref('admin')

const routeMap = {
  '/dashboard': '首页',
  '/vehicle/health': '健康管理 / 车辆健康',
  '/diagnosis/ai': '健康管理 / AI 诊断',
  '/maintenance': '健康管理 / 智能维保',
  '/vehicle/model': '车辆管理 / 车型管理',
  '/vehicle/list': '车辆管理 / 车辆管理',
  '/dbc': '车辆管理 / DBC 管理',
  '/signal': '监控诊断 / 主动监控',
  '/fault/info': '监控诊断 / 故障信息',
  '/diagnosis': '监控诊断 / 远程诊断',
  '/ecu-log': '数据分析 / 日志分析',
  '/signal/analysis': '数据分析 / 信号分析',
  '/signal/playback': '数据分析 / 信号回放',
  '/fault/analysis': '数据分析 / 故障分析',
  '/settings/user': '系统管理 / 用户管理',
  '/settings/role': '系统管理 / 权限管理'
}

const isActive = (path) => route.path === path || route.path.startsWith(path + '/')

const breadcrumb = computed(() => {
  if (route.path.startsWith('/vehicle/detail')) return '车辆管理 / 车辆详情'
  if (routeMap[route.path]) return routeMap[route.path]
  if (route.path.startsWith('/config/can-model')) return '车辆管理 / DBC 管理'
  if (route.path.startsWith('/config/fault')) return '配置管理 / 故障配置'
  if (route.path.startsWith('/signal/fault')) return '监控诊断 / 主动监控'
  return route.path
})

const avatarText = computed(() => (username.value ? username.value.charAt(0).toUpperCase() : 'A'))

const initUser = () => {
  const info = localStorage.getItem('userInfo')
  if (info) {
    try {
      const user = JSON.parse(info)
      username.value = user.username || 'admin'
    } catch (e) {
      /* ignore */
    }
  }
}
initUser()

const showUserMenu = () => {
  handleCommand('logout')
}

const handleCommand = (command) => {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      router.push('/login')
    })
  }
}

const toggleFullscreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen && document.documentElement.requestFullscreen()
  } else {
    document.exitFullscreen && document.exitFullscreen()
  }
}
</script>

<style scoped>
.user-info:hover {
  color: var(--primary);
}
</style>
