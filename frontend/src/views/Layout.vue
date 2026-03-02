<template>
  <el-container class="layout-container">
    <el-header class="header">
      <div class="logo">心理健康平台</div>
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
      </el-menu>
      <div class="user-actions">
        <span>{{ userStore.userInfo.username }}</span>
        <el-button link type="danger" @click="logout">退出</el-button>
      </div>
    </el-header>
    <el-main>
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { useUserStore } from '../stores/user'
import { useRouter } from 'vue-router'

const userStore = useUserStore()
const router = useRouter()

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
  font-size: 20px;
  font-weight: bold;
  color: #409eff;
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
