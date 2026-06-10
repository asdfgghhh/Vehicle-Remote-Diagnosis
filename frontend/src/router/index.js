import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { public: true }
  },
  {
    path: '/register',
    redirect: '/login'
  },
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue')
      },
      {
        path: 'vehicle/model',
        name: 'VehicleModel',
        component: () => import('@/views/VehicleModel.vue')
      },
      {
        path: 'vehicle/list',
        name: 'VehicleList',
        component: () => import('@/views/VehicleList.vue')
      },
      {
        path: 'vehicle/detail/:id',
        name: 'VehicleDetail',
        component: () => import('@/views/VehicleDetail.vue')
      },
      {
        path: 'vehicle/sync-record',
        name: 'VehicleSyncLog',
        component: () => import('@/views/VehicleSyncLog.vue')
      },
      {
        path: 'ecu-log',
        name: 'EcuLog',
        component: () => import('@/views/EcuLog.vue')
      },
      {
        path: 'dbc',
        name: 'Dbc',
        component: () => import('@/views/Dbc.vue')
      },
      {
        path: 'signal',
        redirect: '/signal/fault'
      },
      {
        path: 'signal/fault',
        name: 'FaultMonitor',
        component: () => import('@/views/signal/FaultMonitor.vue')
      },
      {
        path: 'signal/playback',
        name: 'SignalPlayback',
        component: () => import('@/views/signal/SignalPlayback.vue')
      },
      {
        path: 'settings/user',
        name: 'UserManage',
        component: () => import('@/views/settings/UserManage.vue')
      },
      {
        path: 'settings/role',
        name: 'RoleManage',
        component: () => import('@/views/settings/RoleManage.vue')
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: to => (getToken() ? '/dashboard' : '/login')
  }
]

function getToken() {
  const token = localStorage.getItem('token')
  if (!token || token === 'undefined' || token === 'null') {
    return null
  }
  return token
}

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = getToken()

  if (to.path === '/login') {
    next(token ? '/dashboard' : undefined)
    return
  }

  if (to.meta.public) {
    next()
    return
  }

  if (!token) {
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    next('/login')
    return
  }

  next()
})

export default router
