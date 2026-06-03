<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <h2>车辆远程诊断系统</h2>
          <p>Vehicle Remote Diagnosis System</p>
        </div>
      </template>
      
      <el-form :model="loginForm" :rules="rules" ref="loginFormRef">
        <el-form-item prop="username">
          <el-input 
            v-model="loginForm.username" 
            placeholder="用户名"
            :prefix-icon="User"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input 
            v-model="loginForm.password" 
            type="password" 
            placeholder="密码"
            :prefix-icon="Lock"
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item class="remember-item">
          <el-checkbox v-model="rememberMe">记住账号密码</el-checkbox>
        </el-form-item>
        
        <el-form-item>
          <el-button 
            type="primary" 
            :loading="loading" 
            style="width: 100%"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { login } from '@/api/auth'

const REMEMBER_KEY = 'vrd_login_remember'
const USERNAME_KEY = 'vrd_login_username'
const PASSWORD_KEY = 'vrd_login_password'

const router = useRouter()
const loginFormRef = ref(null)
const loading = ref(false)
const rememberMe = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const saveRememberedCredentials = () => {
  if (rememberMe.value) {
    localStorage.setItem(REMEMBER_KEY, '1')
    localStorage.setItem(USERNAME_KEY, loginForm.username)
    localStorage.setItem(PASSWORD_KEY, loginForm.password)
    return
  }
  localStorage.removeItem(REMEMBER_KEY)
  localStorage.removeItem(USERNAME_KEY)
  localStorage.removeItem(PASSWORD_KEY)
}

const loadRememberedCredentials = () => {
  if (localStorage.getItem(REMEMBER_KEY) !== '1') {
    return
  }
  rememberMe.value = true
  loginForm.username = localStorage.getItem(USERNAME_KEY) || ''
  loginForm.password = localStorage.getItem(PASSWORD_KEY) || ''
}

onMounted(() => {
  loadRememberedCredentials()
})

const handleLogin = async () => {
  if (!loginFormRef.value) return

  try {
    await loginFormRef.value.validate()
  } catch {
    return
  }

  loading.value = true
  try {
    const res = await login(loginForm)
    const loginData = res.data
    if (!loginData?.token) {
      throw new Error('登录响应异常，未获取到 token')
    }
    localStorage.setItem('token', loginData.token)
    localStorage.setItem('userInfo', JSON.stringify(loginData))
    saveRememberedCredentials()
    ElMessage.success('登录成功')
    await router.replace('/dashboard')
  } catch (error) {
    ElMessage.error(error.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  width: 100%;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 400px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.card-header {
  text-align: center;
}

.card-header h2 {
  margin: 0 0 8px 0;
  color: #333;
}

.card-header p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.remember-item {
  margin-bottom: 8px;
}

.remember-item :deep(.el-form-item__content) {
  line-height: 1;
}
</style>
