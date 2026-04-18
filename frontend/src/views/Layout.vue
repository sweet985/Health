<template>
  <el-container class="layout-container">
    <el-header class="header">
      <div class="logo" @click="$router.push('/home')" style="cursor: pointer;">
        <img src="/logo.png" alt="logo" class="logo-img" @error="handleImageError" />
        心愈空间
      </div>
      <el-menu mode="horizontal" router :default-active="$route.path" class="menu">
        <el-menu-item index="/home">首页</el-menu-item>
        <el-menu-item index="/treehole">匿名树洞</el-menu-item>
        <el-menu-item index="/treasure">治愈宝库</el-menu-item>
        <el-menu-item index="/community">共鸣社区</el-menu-item>
        <el-menu-item index="/assessment">心理测评</el-menu-item>
        <el-menu-item index="/diary">心理日记</el-menu-item>
        <el-menu-item index="/find">寻找他人</el-menu-item>
        <el-menu-item index="/chat">消息中心</el-menu-item>
        <el-menu-item index="/ai-chat">心理助手</el-menu-item>
        <el-menu-item index="/profile">个人中心</el-menu-item>
        <el-menu-item index="/contact">联系我们</el-menu-item>
      </el-menu>
      <div class="user-actions">
        <el-avatar :key="userStore.userInfo?.avatar" :size="32" :src="userStore.userInfo?.avatar" style="margin-right: 8px;">
          {{ userStore.userInfo?.username?.charAt(0)?.toUpperCase() || 'U' }}
        </el-avatar>
        <span>{{ userStore.userInfo?.username || '用户' }}</span>
        <el-button link type="danger" @click="logout" style="margin-left: 10px;">退出</el-button>
      </div>
    </el-header>
    <el-main>
      <router-view />
    </el-main>
    <GlobalMusicPlayer />
  </el-container>
</template>

<script setup>
import { useUserStore } from '../stores/user'
import { useRouter } from 'vue-router'
import GlobalMusicPlayer from '../components/GlobalMusicPlayer.vue'
import { wsManager } from '../utils/websocket'
import { onMounted, onUnmounted } from 'vue'

const userStore = useUserStore()

const router = useRouter()

onMounted(() => {
  wsManager.connect()
})

onUnmounted(() => {
  wsManager.disconnect()
})

const handleImageError = (e) => {
  e.target.style.display = 'none'
}

const logout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #dcdfe6;
}
.logo {
  display: flex;
  align-items: center;
  font-size: 20px;
  font-weight: bold;
  color: #ff9a9e; /* Changed to match the logo color */
}
.logo-img {
  width: 40px;
  height: 40px;
  margin-right: 10px;
  border-radius: 8px;
  object-fit: cover;
}
.menu {
  flex: 1;
  margin: 0 20px;
  border-bottom: none;
}
.user-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}
</style>
