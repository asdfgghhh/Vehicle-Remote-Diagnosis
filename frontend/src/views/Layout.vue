<template>
  <div class="layout-container">
    <el-container>
      <el-aside width="200px">
        <div class="logo">
          <h3>VRD系统</h3>
        </div>
        <el-menu
          :default-active="activeMenu"
          router
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
        >
          <el-menu-item index="/dashboard">
            <el-icon><DataAnalysis /></el-icon>
            <span>仪表盘</span>
          </el-menu-item>
          
          <el-sub-menu index="/vehicle">
            <template #title>
              <el-icon><Van /></el-icon>
              <span>车辆管理</span>
            </template>
            <el-menu-item index="/vehicle/model">车型管理</el-menu-item>
            <el-menu-item index="/vehicle/list">车辆列表</el-menu-item>
            <el-menu-item index="/vehicle/sync-record">同步记录</el-menu-item>
          </el-sub-menu>
          
          <el-menu-item index="/ecu-log">
            <el-icon><Document /></el-icon>
            <span>日志分析</span>
          </el-menu-item>
          
          <el-sub-menu index="/config">
            <template #title>
              <el-icon><FolderOpened /></el-icon>
              <span>配置管理</span>
            </template>
            <el-menu-item index="/config/can-model">车辆 CAN 模型</el-menu-item>
            <el-menu-item index="/config/fault">故障配置</el-menu-item>
          </el-sub-menu>
          
          <el-sub-menu index="/signal">
            <template #title>
              <el-icon><DataLine /></el-icon>
              <span>主动监控</span>
            </template>
            <el-menu-item index="/signal/fault">故障监控</el-menu-item>
            <el-menu-item index="/signal/playback">信号回放</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="/settings">
            <template #title>
              <el-icon><Setting /></el-icon>
              <span>系统设置</span>
            </template>
            <el-menu-item index="/settings/user">账号管理</el-menu-item>
            <el-menu-item index="/settings/role">权限管理</el-menu-item>
          </el-sub-menu>
        </el-menu>
      </el-aside>
      
      <el-container>
        <el-header>
          <div class="header-left">
            <el-breadcrumb separator="/">
              <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
              <el-breadcrumb-item v-if="currentRoute">{{ currentRoute }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          <div class="header-right">
            <el-dropdown @command="handleCommand">
              <span class="user-info">
                <el-icon><User /></el-icon>
                {{ username }}
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                  <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>
        
        <el-main>
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import {
  DataAnalysis,
  Van,
  Document,
  FolderOpened,
  DataLine,
  Setting,
  User
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const activeMenu = computed(() => route.path)
const username = ref('Admin')

const currentRoute = computed(() => {
  if (route.path.startsWith('/vehicle/detail')) {
    return '车辆详情'
  }
  if (route.path.startsWith('/config/can-model/') && route.params.id) {
    return '配置管理 / 车辆 CAN 模型 / 信号详情'
  }
  const routeMap = {
    '/dashboard': '仪表盘',
    '/vehicle/model': '车辆管理 / 车型管理',
    '/vehicle/list': '车辆管理 / 车辆列表',
    '/vehicle/sync-record': '车辆管理 / 同步记录',
    '/ecu-log': '日志分析',
    '/config/can-model': '配置管理 / 车辆 CAN 模型',
    '/config/fault': '配置管理 / 故障配置',
    '/signal/fault': '主动监控 / 故障监控',
    '/signal/playback': '主动监控 / 信号回放',
    '/settings/user': '账号管理',
    '/settings/role': '权限管理'
  }
  return routeMap[route.path]
})

onMounted(() => {
  const userInfo = localStorage.getItem('userInfo')
  if (userInfo) {
    const user = JSON.parse(userInfo)
    username.value = user.username || 'Admin'
  }
})

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
  } else if (command === 'profile') {
    router.push('/profile')
  }
}
</script>

<style scoped>
.layout-container {
  width: 100%;
  height: 100vh;
}

.el-container {
  height: 100%;
}

.el-aside {
  background-color: #304156;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #2b3a4a;
}

.logo h3 {
  margin: 0;
  color: #fff;
  font-size: 18px;
}

.el-header {
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
}

.el-main {
  background-color: #f0f2f5;
  padding: 20px;
}
</style>
