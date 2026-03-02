<template>
  <div class="login-page">
    <div class="background-shapes">
      <div class="shape shape-1"></div>
      <div class="shape shape-2"></div>
      <div class="shape shape-3"></div>
    </div>
    
    <div class="login-container glass-effect">
      <div class="left-panel">
        <div class="brand-content">
          <div class="logo-circle">
            <span class="logo-icon">🌿</span>
          </div>
          <h1>Health Mind</h1>
          <p class="slogan">聆听心灵的声音，遇见更好的自己</p>
          <div class="illustration">
            <!-- Simple CSS illustration or just shapes -->
            <div class="circle-orbit">
              <div class="orbit-item">💖</div>
              <div class="orbit-item delay-1">✨</div>
              <div class="orbit-item delay-2">🌱</div>
            </div>
          </div>
        </div>
      </div>
      
      <div class="right-panel">
        <div class="form-wrapper">
          <div class="form-header">
            <h2>{{ activeTab === 'login' ? '欢迎回来' : '加入我们要' }}</h2>
            <p>{{ activeTab === 'login' ? '请登录您的账号以继续' : '创建一个新账号开启旅程' }}</p>
          </div>
          
          <el-tabs v-model="activeTab" class="custom-tabs">
            <el-tab-pane label="登录" name="login">
              <el-form :model="loginForm" class="auth-form" @keyup.enter="handleLogin">
                <el-form-item>
                  <el-input 
                    v-model="loginForm.username" 
                    placeholder="用户名" 
                    prefix-icon="User"
                    class="custom-input"
                  />
                </el-form-item>
                <el-form-item>
                  <el-input 
                    v-model="loginForm.password" 
                    type="password" 
                    placeholder="密码" 
                    prefix-icon="Lock"
                    show-password
                    class="custom-input"
                  />
                </el-form-item>
                <el-button type="primary" :loading="loading" class="submit-btn" @click="handleLogin">
                  登录
                  <el-icon class="el-icon--right"><ArrowRight /></el-icon>
                </el-button>
              </el-form>
            </el-tab-pane>
            
            <el-tab-pane label="注册" name="register">
              <el-form :model="registerForm" class="auth-form" @keyup.enter="handleRegister">
                <el-form-item>
                  <el-input 
                    v-model="registerForm.username" 
                    placeholder="设置用户名" 
                    prefix-icon="User"
                    class="custom-input"
                  />
                </el-form-item>
                <el-form-item>
                  <el-input 
                    v-model="registerForm.password" 
                    type="password" 
                    placeholder="设置密码" 
                    prefix-icon="Lock"
                    show-password
                    class="custom-input"
                  />
                </el-form-item>
                <el-button type="success" :loading="loading" class="submit-btn register-btn" @click="handleRegister">
                  立即注册
                  <el-icon class="el-icon--right"><Check /></el-icon>
                </el-button>
              </el-form>
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import request from '../utils/request'
import { useUserStore } from '../stores/user'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, ArrowRight, Check } from '@element-plus/icons-vue'

const activeTab = ref('login')
const loginForm = ref({ username: '', password: '' })
const registerForm = ref({ username: '', password: '' })
const loading = ref(false)
const userStore = useUserStore()
const router = useRouter()

const handleLogin = async () => {
  if (!loginForm.value.username || !loginForm.value.password) {
    return ElMessage.warning('请输入用户名和密码')
  }
  loading.value = true
  try {
    const token = await request.post('/auth/login', loginForm.value)
    userStore.setToken(token)
    const info = await request.get('/user/info')
    userStore.setUserInfo(info)
    ElMessage.success('登录成功')
    router.push('/')
  } catch (e) {
    // Handled by interceptor
  } finally {
    loading.value = false
  }
}

const handleRegister = async () => {
  if (!registerForm.value.username || !registerForm.value.password) {
    return ElMessage.warning('请输入用户名和密码')
  }
  loading.value = true
  try {
    await request.post('/auth/register', registerForm.value)
    ElMessage.success('注册成功，请登录')
    activeTab.value = 'login'
    loginForm.value.username = registerForm.value.username
    registerForm.value = { username: '', password: '' }
  } catch (e) {
    // Handled by interceptor
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  position: relative;
  overflow: hidden;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
}

/* Background Shapes */
.background-shapes .shape {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.6;
  animation: float 10s infinite ease-in-out;
}

.shape-1 {
  width: 400px;
  height: 400px;
  background: #a1c4fd;
  top: -100px;
  left: -100px;
}

.shape-2 {
  width: 300px;
  height: 300px;
  background: #c2e9fb;
  bottom: -50px;
  right: -50px;
  animation-delay: -2s;
}

.shape-3 {
  width: 200px;
  height: 200px;
  background: #ffecd2;
  top: 40%;
  left: 60%;
  animation-delay: -5s;
}

@keyframes float {
  0%, 100% { transform: translateY(0) scale(1); }
  50% { transform: translateY(-20px) scale(1.05); }
}

.login-container {
  display: flex;
  width: 900px;
  height: 550px;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0,0,0,0.1);
  z-index: 10;
  background: rgba(255, 255, 255, 0.85);
}

.glass-effect {
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.5);
}

/* Left Panel */
.left-panel {
  flex: 1;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 40px;
  position: relative;
  overflow: hidden;
}

.left-panel::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='0.1'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
}

.brand-content {
  z-index: 2;
  text-align: center;
}

.logo-circle {
  width: 80px;
  height: 80px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 0 auto 20px;
  backdrop-filter: blur(5px);
  border: 2px solid rgba(255, 255, 255, 0.3);
}

.logo-icon {
  font-size: 40px;
}

.left-panel h1 {
  font-size: 2.5rem;
  margin-bottom: 10px;
  font-weight: 700;
  text-shadow: 0 2px 4px rgba(0,0,0,0.2);
}

.slogan {
  font-size: 1.1rem;
  opacity: 0.9;
  margin-bottom: 40px;
  font-weight: 300;
}

.illustration {
  height: 150px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.circle-orbit {
  position: relative;
  width: 120px;
  height: 120px;
  border: 2px dashed rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  animation: rotate 20s linear infinite;
}

.orbit-item {
  position: absolute;
  font-size: 24px;
  top: -12px;
  left: 50%;
  transform: translateX(-50%);
}

.delay-1 {
  top: auto;
  bottom: -12px;
  left: 50%;
  transform: translateX(-50%) rotate(180deg); /* Simple positioning fix */
}

.delay-2 {
  top: 50%;
  left: -12px;
  transform: translateY(-50%) rotate(-90deg);
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* Right Panel */
.right-panel {
  flex: 1.2;
  padding: 50px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  background: #fff;
}

.form-wrapper {
  max-width: 320px;
  margin: 0 auto;
  width: 100%;
}

.form-header {
  margin-bottom: 30px;
}

.form-header h2 {
  font-size: 1.8rem;
  color: #333;
  margin-bottom: 5px;
}

.form-header p {
  color: #999;
  font-size: 0.9rem;
}

/* Custom Tabs */
:deep(.custom-tabs .el-tabs__nav-wrap::after) {
  height: 2px;
  background-color: #f0f0f0;
}

:deep(.custom-tabs .el-tabs__active-bar) {
  height: 2px;
  background-color: #764ba2;
}

:deep(.custom-tabs .el-tabs__item) {
  font-size: 16px;
  color: #666;
  font-weight: 500;
}

:deep(.custom-tabs .el-tabs__item.is-active) {
  color: #764ba2;
}

/* Form Styles */
.auth-form {
  margin-top: 20px;
}

:deep(.custom-input .el-input__wrapper) {
  background-color: #f7f9fc;
  border-radius: 8px;
  padding: 8px 15px;
  box-shadow: none;
  border: 1px solid transparent;
  transition: all 0.3s;
}

:deep(.custom-input .el-input__wrapper:hover),
:deep(.custom-input .el-input__wrapper.is-focus) {
  background-color: #fff;
  border-color: #a1c4fd;
  box-shadow: 0 0 0 3px rgba(161, 196, 253, 0.2);
}

:deep(.custom-input .el-input__inner) {
  height: 40px;
}

.submit-btn {
  width: 100%;
  height: 48px;
  border-radius: 8px;
  font-size: 16px;
  margin-top: 10px;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  border: none;
  transition: transform 0.2s, box-shadow 0.2s;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(118, 75, 162, 0.4);
}

.register-btn {
  background: linear-gradient(90deg, #11998e 0%, #38ef7d 100%);
}

.register-btn:hover {
  box-shadow: 0 5px 15px rgba(56, 239, 125, 0.4);
}
</style>
