import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
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
        name: 'Signal',
        component: () => import('@/views/Signal.vue')
      },
      {
        path: 'bigdata',
        name: 'Bigdata',
        component: () => import('@/views/Bigdata.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
