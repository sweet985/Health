<template>
  <div class="profile-container">
    <div class="profile-layout">
      <!-- 左侧：个人信息卡片 -->
      <div class="left-panel">
        <el-card class="user-card" shadow="hover">
          <div class="avatar-section">
            <div class="avatar-wrapper">
              <el-avatar :size="100" :src="form.avatar" class="user-avatar">
                {{ form.username?.charAt(0)?.toUpperCase() }}
              </el-avatar>
              <div class="edit-avatar-btn" @click="showAvatarDialog">
                <el-icon><Camera /></el-icon>
              </div>
            </div>
            <h2 class="username">{{ form.username }}</h2>
            <p class="user-bio">{{ form.bio || '这个人很懒，什么都没写~' }}</p>
            <div class="user-tags">
              <el-tag v-if="form.mbti" type="warning" effect="dark" round class="mbti-tag">
                {{ form.mbti }}
              </el-tag>
              <el-tag type="info" effect="plain" round>
                注册于 {{ formatDate(userStore.userInfo.createTime) }}
              </el-tag>
            </div>
          </div>
          
          <div class="info-actions">
            <el-button type="primary" plain round @click="dialogVisible = true" class="edit-btn">
              <el-icon><Edit /></el-icon> 编辑资料
            </el-button>
            <el-button type="danger" link @click="deleteAccount" class="logout-btn">
              注销账号
            </el-button>
          </div>
        </el-card>

        <!-- 统计数据卡片 (Mock data for now) -->
        <el-card class="stat-card" shadow="hover">
          <div class="stat-grid">
            <div class="stat-item">
              <span class="stat-num">{{ myPosts.length }}</span>
              <span class="stat-label">发布</span>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item">
              <span class="stat-num">{{ totalLikes }}</span>
              <span class="stat-label">获赞</span>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item">
              <span class="stat-num">{{ moodScore }}</span>
              <span class="stat-label">心情分</span>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 右侧：内容管理 -->
      <div class="right-panel">
        <el-card class="content-card" shadow="never">
          <template #header>
            <div class="content-header">
              <span class="title">我的发布</span>
              <el-tabs v-model="activeTab" @tab-click="filterPosts" class="custom-tabs">
                <el-tab-pane label="全部" name="all"></el-tab-pane>
                <el-tab-pane label="树洞" name="treehole"></el-tab-pane>
                <el-tab-pane label="社区" name="community"></el-tab-pane>
              </el-tabs>
            </div>
          </template>

          <div class="posts-list" v-loading="loading">
            <el-empty v-if="filteredPosts.length === 0" description="暂无发布内容" :image-size="150" />
            
            <div v-else v-for="post in filteredPosts" :key="post.id" class="post-item animate-in">
              <div class="post-main" @click="goToPost(post)">
                <div class="post-top">
                  <el-tag size="small" :type="post.type === 1 ? 'info' : 'success'" effect="light" round>
                    {{ post.type === 1 ? '匿名树洞' : '共鸣社区' }}
                  </el-tag>
                  <span class="post-time">{{ formatTime(post.createTime) }}</span>
                </div>
                <h4 class="post-title" v-if="post.title">{{ post.title }}</h4>
                <p class="post-excerpt">{{ post.content }}</p>
              </div>
              
              <div class="post-footer">
                <div class="post-stats">
                  <span class="stat"><el-icon><Star /></el-icon> {{ post.likeCount || 0 }}</span>
                  <span class="stat"><el-icon><ChatDotRound /></el-icon> {{ post.commentCount || 0 }}</span>
                </div>
                <div class="post-actions">
                  <el-tooltip content="删除" placement="top">
                    <el-button type="danger" circle size="small" plain @click.stop="deleteMyPost(post.id)">
                      <el-icon><Delete /></el-icon>
                    </el-button>
                  </el-tooltip>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </div>
    </div>

    <!-- Edit Profile Dialog -->
    <el-dialog v-model="dialogVisible" title="编辑个人资料" width="500px" center destroy-on-close class="custom-dialog">
      <el-form :model="form" label-position="top">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="给自己起个好听的名字" />
        </el-form-item>
        
        <el-form-item label="个人简介">
          <el-input 
            v-model="form.bio" 
            type="textarea" 
            :rows="3"
            maxlength="50" 
            show-word-limit 
            placeholder="介绍一下自己吧..."
          />
        </el-form-item>

        <el-form-item label="MBTI 人格">
          <el-select v-model="form.mbti" placeholder="选择你的MBTI人格" style="width: 100%">
            <el-option v-for="type in mbtiOptions" :key="type" :label="type" :value="type">
              <span style="float: left">{{ type }}</span>
              <span style="float: right; color: #8492a6; font-size: 13px">{{ getMbtiDesc(type) }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="修改密码 (留空则不修改)">
          <el-input v-model="password" type="password" placeholder="输入新密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false" round>取消</el-button>
          <el-button type="primary" @click="saveProfile" round>保存修改</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- Avatar Edit Dialog -->
    <el-dialog v-model="avatarDialogVisible" title="修改头像" width="400px" center>
      <el-input v-model="form.avatar" placeholder="输入头像图片链接 URL" />
      <div class="avatar-preview" v-if="form.avatar">
        <p>预览：</p>
        <el-avatar :size="80" :src="form.avatar" />
      </div>
      <template #footer>
        <el-button @click="avatarDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveAvatar">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import request from '../utils/request'
import { useUserStore } from '../stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { Edit, Camera, Delete, Star, ChatDotRound } from '@element-plus/icons-vue'

const userStore = useUserStore()
const router = useRouter()

const dialogVisible = ref(false)
const avatarDialogVisible = ref(false)
const loading = ref(false)
const activeTab = ref('all')

const form = ref({ 
  username: '',
  avatar: '',
  bio: '',
  mbti: ''
})
const password = ref('')
const myPosts = ref([])
const moodScore = ref(100) // Mock score, ideally from userStore

const mbtiOptions = [
  'ESTJ', 'ISTJ', 'ENTJ', 'INTJ', 
  'ESFJ', 'ISFJ', 'ENFJ', 'INFJ', 
  'ESTP', 'ISTP', 'ENTP', 'INTP', 
  'ESFP', 'ISFP', 'ENFP', 'INFP'
]

// Initialize
if (userStore.userInfo) {
  form.value = { ...userStore.userInfo }
  moodScore.value = userStore.userInfo.moodScore || 100
}

const totalLikes = computed(() => {
  return myPosts.value.reduce((sum, post) => sum + (post.likeCount || 0), 0)
})

const filteredPosts = computed(() => {
  if (activeTab.value === 'all') return myPosts.value
  const type = activeTab.value === 'treehole' ? 1 : 2
  return myPosts.value.filter(post => post.type === type)
})

const loadMyPosts = async () => {
  loading.value = true
  try {
    const res = await request.get('/post/my') // Assuming this API exists and returns all posts
    // If the API only supports paging, we might need to handle that.
    // For now assuming it returns a list or Page object.
    // Based on previous code it returns Page object.
    // Let's assume we want to show all or first page.
    // Previous code: res.records
    myPosts.value = res.records || []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const saveProfile = async () => {
  try {
    // Parallel updates
    const updates = []
    if (form.value.username !== userStore.userInfo.username) {
      updates.push(request.post('/user/update/username', { username: form.value.username }))
    }
    if (form.value.bio !== userStore.userInfo.bio) {
      updates.push(request.post('/user/update/bio', { bio: form.value.bio }))
    }
    if (form.value.mbti !== userStore.userInfo.mbti) {
      updates.push(request.post('/user/update/mbti', { mbti: form.value.mbti }))
    }
    if (password.value) {
      updates.push(request.post('/user/update/password', { password: password.value }))
    }

    await Promise.all(updates)
    ElMessage.success('资料已更新')
    dialogVisible.value = false
    password.value = ''
    await refreshUserInfo()
  } catch (e) {
    // Error handled
  }
}

const showAvatarDialog = () => {
  avatarDialogVisible.value = true
}

const saveAvatar = async () => {
  try {
    await request.post('/user/update/avatar', { avatar: form.value.avatar })
    ElMessage.success('头像已更新')
    avatarDialogVisible.value = false
    await refreshUserInfo()
  } catch (e) {}
}

const refreshUserInfo = async () => {
  const info = await request.get('/user/info')
  userStore.setUserInfo(info)
  form.value = { ...info }
}

const deleteAccount = () => {
  ElMessageBox.confirm('确定要注销账号吗？所有数据将被删除且无法恢复！', '警告', {
    confirmButtonText: '确定注销',
    cancelButtonText: '我再想想',
    type: 'warning',
    center: true
  }).then(async () => {
    await request.post('/user/delete')
    userStore.logout()
    router.push('/login')
    ElMessage.success('账号已注销')
  })
}

const deleteMyPost = (id) => {
  ElMessageBox.confirm('确定要删除这条内容吗？', '提示', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning',
    center: true
  }).then(async () => {
    try {
      await request.post(`/post/delete/${id}`)
      ElMessage.success('删除成功')
      loadMyPosts()
    } catch (e) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

const goToPost = (post) => {
  // Navigation logic
  // Since we don't have direct post links yet, we redirect to module
  // Ideally should navigate to detail anchor
  if (post.type === 1) {
    router.push('/treehole')
  } else {
    router.push('/community')
  }
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getFullYear()}-${(date.getMonth()+1).toString().padStart(2,'0')}-${date.getDate().toString().padStart(2,'0')}`
}

const formatDate = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleDateString()
}

const getMbtiDesc = (type) => {
  const map = {
    'INFJ': '提倡者', 'INFP': '调停者', 'ENFJ': '主人公', 'ENFP': '竞选者',
    'ISTJ': '物流师', 'ISFJ': '守卫者', 'ESTJ': '总经理', 'ESFJ': '执政官',
    'ISTP': '鉴赏家', 'ISFP': '探险家', 'ESTP': '企业家', 'ESFP': '表演者',
    'INTJ': '建筑师', 'INTP': '逻辑学家', 'ENTJ': '指挥官', 'ENTP': '辩论家'
  }
  return map[type] || ''
}

onMounted(() => {
  loadMyPosts()
  refreshUserInfo()
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Nunito:wght@400;600;700&display=swap');

.profile-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
  min-height: 90vh;
  font-family: 'Nunito', sans-serif;
  background-color: #fcfcfc;
}

.profile-layout {
  display: flex;
  gap: 30px;
}

/* Left Panel */
.left-panel {
  width: 350px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.user-card {
  border-radius: 20px;
  border: none;
  background: #fff;
  box-shadow: 0 10px 30px rgba(0,0,0,0.05);
  text-align: center;
  overflow: visible;
}

.avatar-section {
  position: relative;
  margin-bottom: 25px;
}

.avatar-wrapper {
  position: relative;
  width: 100px;
  height: 100px;
  margin: 0 auto 20px;
}

.user-avatar {
  border: 4px solid #fff;
  box-shadow: 0 8px 20px rgba(0,0,0,0.1);
  background: #f0f2f5;
  color: #909399;
  font-size: 40px;
  font-weight: bold;
}

.edit-avatar-btn {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 32px;
  height: 32px;
  background: #409eff;
  border-radius: 50%;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border: 2px solid #fff;
  transition: transform 0.2s;
}

.edit-avatar-btn:hover {
  transform: scale(1.1);
}

.username {
  font-size: 1.8rem;
  color: #2c3e50;
  margin: 0 0 10px;
  font-weight: 700;
}

.user-bio {
  color: #606266;
  font-size: 0.95rem;
  line-height: 1.6;
  margin: 0 0 20px;
  padding: 0 10px;
}

.user-tags {
  display: flex;
  justify-content: center;
  gap: 10px;
  flex-wrap: wrap;
}

.mbti-tag {
  background: linear-gradient(135deg, #f6d365 0%, #fda085 100%);
  border: none;
  font-weight: bold;
}

.info-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding-top: 20px;
  border-top: 1px solid #f5f5f5;
}

.edit-btn {
  width: 100%;
}

.logout-btn {
  color: #909399;
  font-size: 0.9rem;
}

.logout-btn:hover {
  color: #f56c6c;
}

/* Stat Card */
.stat-card {
  border-radius: 20px;
  border: none;
  box-shadow: 0 10px 30px rgba(0,0,0,0.05);
}

.stat-grid {
  display: flex;
  justify-content: space-around;
  align-items: center;
  padding: 10px 0;
}

.stat-item {
  text-align: center;
}

.stat-num {
  display: block;
  font-size: 1.5rem;
  font-weight: 800;
  color: #2c3e50;
}

.stat-label {
  font-size: 0.85rem;
  color: #909399;
}

.stat-divider {
  width: 1px;
  height: 30px;
  background: #f0f0f0;
}

/* Right Panel */
.right-panel {
  flex: 1;
}

.content-card {
  border-radius: 20px;
  border: none;
  box-shadow: 0 10px 30px rgba(0,0,0,0.05);
  min-height: 600px;
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.content-header .title {
  font-size: 1.2rem;
  font-weight: 700;
  color: #2c3e50;
}

.custom-tabs :deep(.el-tabs__nav-wrap::after) {
  display: none;
}

.custom-tabs :deep(.el-tabs__item) {
  font-size: 1rem;
}

.posts-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.post-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 20px;
  background: #f9fafc;
  border-radius: 12px;
  transition: all 0.3s;
  cursor: pointer;
  border: 1px solid transparent;
}

.post-item:hover {
  background: #fff;
  box-shadow: 0 5px 15px rgba(0,0,0,0.05);
  border-color: #e0e0e0;
  transform: translateY(-2px);
}

.post-main {
  flex: 1;
  padding-right: 20px;
}

.post-top {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.post-time {
  font-size: 0.85rem;
  color: #999;
}

.post-title {
  margin: 0 0 8px;
  font-size: 1.1rem;
  color: #333;
}

.post-excerpt {
  color: #666;
  font-size: 0.95rem;
  line-height: 1.6;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.post-footer {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 15px;
}

.post-stats {
  display: flex;
  gap: 15px;
  color: #999;
  font-size: 0.9rem;
}

.stat {
  display: flex;
  align-items: center;
  gap: 5px;
}

.post-actions {
  opacity: 0;
  transition: opacity 0.3s;
}

.post-item:hover .post-actions {
  opacity: 1;
}

.avatar-preview {
  margin-top: 20px;
  text-align: center;
}

.animate-in {
  animation: fadeIn 0.5s ease-out forwards;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

@media (max-width: 768px) {
  .profile-layout {
    flex-direction: column;
  }
  .left-panel {
    width: 100%;
  }
}
</style>
