<template>
  <div class="find-friend-container">
    <div class="search-hero">
      <h2>寻找志同道合的伙伴</h2>
      <p>输入用户名，开启一段新的友谊</p>
      <div class="search-box">
        <el-input 
          v-model="keyword" 
          placeholder="搜索用户名..." 
          class="custom-input"
          size="large"
          @keyup.enter="search"
        >
          <template #prefix>
            <el-icon class="search-icon"><Search /></el-icon>
          </template>
          <template #append>
            <el-button type="primary" class="search-btn" @click="search">搜索</el-button>
          </template>
        </el-input>
      </div>
    </div>
    
    <div v-if="searchResult" class="result-section fade-in-up">
      <div class="result-card">
        <el-avatar :size="80" :src="searchResult.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" class="result-avatar pointer" @click="showUserProfile(searchResult)" />
        <div class="result-info">
          <h3 class="pointer" @click="showUserProfile(searchResult)">{{ searchResult.username }}</h3>
          <div v-if="searchResult.mbti" class="mbti-tag">{{ searchResult.mbti }}</div>
          <p class="user-bio">{{ searchResult.bio || '这个人很神秘，没有写简介~' }}</p>
          <el-button type="primary" round class="add-btn" @click="openAddDialog(searchResult)">
            <el-icon><Plus /></el-icon> 添加好友
          </el-button>
        </div>
      </div>
    </div>
    
    <div class="main-content">
      <el-row :gutter="40">
        <!-- 好友申请 -->
        <el-col :span="12">
          <div class="panel-card request-panel">
            <div class="panel-header">
              <div class="header-icon icon-bell">
                <el-icon><Bell /></el-icon>
              </div>
              <h3>好友申请</h3>
              <span class="badge" v-if="requests.length > 0">{{ requests.length }}</span>
            </div>
            
            <div class="panel-body">
              <el-empty v-if="requests.length === 0" description="暂无新的申请" :image-size="80" />
              <div v-else class="list-container">
                <div v-for="req in requests" :key="req.id" class="list-item">
                  <div class="req-left pointer" @click="showUserProfile(req.friendInfo)">
                    <el-avatar :size="50" :src="req.friendInfo.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
                    <div class="item-info">
                      <span class="name">{{ req.friendInfo.username }}</span>
                      <span class="desc" v-if="req.message">留言: {{ req.message }}</span>
                      <span class="desc" v-else>请求添加你为好友</span>
                    </div>
                  </div>
                  <div class="req-actions">
                    <el-button type="success" size="small" circle icon="Check" @click="accept(req.friendInfo.id)" />
                    <el-button type="danger" size="small" circle icon="Close" @click="openRejectDialog(req.friendInfo.id)" />
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-col>
        
        <!-- 我的好友 -->
        <el-col :span="12">
          <div class="panel-card friend-panel">
            <div class="panel-header">
              <div class="header-icon icon-user">
                <el-icon><User /></el-icon>
              </div>
              <h3>我的好友</h3>
              <span class="count">{{ friends.length }} 位好友</span>
            </div>
            
            <div class="panel-body">
              <el-empty v-if="friends.length === 0" description="还没有好友，快去添加吧" :image-size="80" />
              <div v-else class="list-container">
                <div v-for="friend in friends" :key="friend.id" class="list-item friend-item-row">
                  <div class="friend-left" @click="showUserProfile(friend.friendInfo)">
                    <el-avatar :size="50" :src="friend.friendInfo.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" class="friend-avatar" />
                    <div class="item-info">
                      <div class="name-row">
                        <span class="name">{{ friend.friendInfo.username }}</span>
                        <span v-if="friend.friendInfo.mbti" class="mini-mbti">{{ friend.friendInfo.mbti }}</span>
                      </div>
                      <span class="status-indicator" v-if="friend.friendInfo.bio">{{ friend.friendInfo.bio.substring(0, 15) + (friend.friendInfo.bio.length > 15 ? '...' : '') }}</span>
                      <span class="status-indicator" v-else>在线</span>
                    </div>
                  </div>
                  <div class="actions">
                    <el-tooltip content="发消息" placement="top">
                      <el-button type="primary" circle plain icon="ChatDotRound" @click="chat(friend.friendInfo)" />
                    </el-tooltip>
                    <el-popconfirm 
                      title="确定要删除这位好友吗？" 
                      confirm-button-text="删除" 
                      cancel-button-text="取消"
                      confirm-button-type="danger"
                      @confirm="deleteFriend(friend.friendInfo.id)"
                    >
                      <template #reference>
                        <el-button type="danger" circle plain icon="Delete" />
                      </template>
                    </el-popconfirm>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- User Profile Dialog -->
    <el-dialog
      v-model="profileVisible"
      title="用户资料"
      width="400px"
      center
      class="profile-dialog"
    >
      <div v-if="currentUser" class="dialog-profile">
        <el-avatar :size="80" :src="currentUser.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" class="dialog-avatar" />
        <h3 class="dialog-name">{{ currentUser.username }}</h3>
        <div v-if="currentUser.mbti" class="dialog-mbti">{{ currentUser.mbti }}</div>
        <p class="dialog-bio">{{ currentUser.bio || '这个人很神秘，什么都没写~' }}</p>
      </div>
    </el-dialog>

    <!-- Add Friend Dialog -->
    <el-dialog v-model="addDialogVisible" title="添加好友" width="400px">
      <el-input v-model="addMessage" placeholder="捎句话（相当于私信）" type="textarea" :rows="3" />
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="addDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmAddFriend">发送申请</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- Reject Friend Dialog -->
    <el-dialog v-model="rejectDialogVisible" title="拒绝申请" width="400px">
      <el-input v-model="rejectReason" placeholder="拒绝原因（相当于私信）" type="textarea" :rows="3" />
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="rejectDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="confirmReject">拒绝</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../utils/request'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { Search, Bell, User, Plus, ChatDotRound, Delete, Check, Close } from '@element-plus/icons-vue'

const keyword = ref('')
const searchResult = ref(null)
const requests = ref([])
const friends = ref([])
const router = useRouter()

// Dialog states
const profileVisible = ref(false)
const currentUser = ref(null)
const addDialogVisible = ref(false)
const addMessage = ref('')
const targetUserId = ref(null)
const rejectDialogVisible = ref(false)
const rejectReason = ref('')
const rejectTargetId = ref(null)

const search = async () => {
  if (!keyword.value.trim()) return
  try {
    const res = await request.get('/user/search', { params: { username: keyword.value } })
    searchResult.value = res
  } catch (e) {
    searchResult.value = null
    ElMessage.warning('未找到该用户')
  }
}

const showUserProfile = (user) => {
  currentUser.value = user
  profileVisible.value = true
}

const openAddDialog = (user) => {
  targetUserId.value = user.id
  addMessage.value = ''
  addDialogVisible.value = true
}

const confirmAddFriend = async () => {
  if (!targetUserId.value) return
  await request.post(`/friend/add/${targetUserId.value}`, { message: addMessage.value })
  ElMessage.success('申请已发送')
  addDialogVisible.value = false
  searchResult.value = null
  keyword.value = ''
}

const loadData = async () => {
  requests.value = await request.get('/friend/requests')
  friends.value = await request.get('/friend/list')
}

const accept = async (id) => {
  await request.post(`/friend/accept/${id}`)
  ElMessage.success('已添加好友')
  loadData()
}

const openRejectDialog = (id) => {
  rejectTargetId.value = id
  rejectReason.value = ''
  rejectDialogVisible.value = true
}

const confirmReject = async () => {
  if (!rejectTargetId.value) return
  await request.post(`/friend/reject/${rejectTargetId.value}`, { reason: rejectReason.value })
  ElMessage.success('已拒绝申请')
  rejectDialogVisible.value = false
  loadData()
}

const deleteFriend = async (id) => {
  await request.post(`/friend/delete/${id}`)
  ElMessage.success('好友已删除')
  loadData()
}

const chat = (friend) => {
  router.push({ path: '/chat', query: { id: friend.id, name: friend.username } })
}

onMounted(loadData)
</script>

<style scoped>
.find-friend-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 40px 20px;
  min-height: calc(100vh - 60px);
  background: #f9fbfd;
}

.search-hero {
  text-align: center;
  margin-bottom: 50px;
  background: #fff;
  padding: 40px;
  border-radius: 20px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.03);
}

.search-hero h2 {
  font-size: 2rem;
  color: #333;
  margin-bottom: 10px;
}

.search-hero p {
  color: #888;
  margin-bottom: 30px;
}

.search-box {
  max-width: 500px;
  margin: 0 auto;
}

:deep(.custom-input .el-input__wrapper) {
  border-radius: 50px;
  padding-left: 20px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.05);
}

:deep(.custom-input .el-input-group__append) {
  border-top-right-radius: 50px;
  border-bottom-right-radius: 50px;
  background: #409eff;
  color: #fff;
  border: none;
}

:deep(.custom-input .search-btn) {
  color: #fff;
}

.result-section {
  max-width: 400px;
  margin: 0 auto 40px;
}

.result-card {
  background: #fff;
  padding: 30px;
  border-radius: 16px;
  text-align: center;
  box-shadow: 0 10px 25px rgba(64, 158, 255, 0.15);
  display: flex;
  flex-direction: column;
  align-items: center;
}

.result-avatar {
  border: 4px solid #eef5ff;
  margin-bottom: 15px;
}

.result-info h3 {
  margin: 0 0 5px;
  color: #333;
}

.mbti-tag {
  display: inline-block;
  background: #f0f9eb;
  color: #67c23a;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
  margin-bottom: 10px;
}

.user-bio {
  color: #666;
  font-size: 0.95rem;
  margin-bottom: 20px;
  line-height: 1.5;
  background: #f9f9f9;
  padding: 10px;
  border-radius: 8px;
  width: 100%;
}

.add-btn {
  padding: 10px 30px;
}

.panel-card {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 5px 20px rgba(0,0,0,0.03);
  height: 500px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.panel-header {
  padding: 20px;
  border-bottom: 1px solid #f5f5f5;
  display: flex;
  align-items: center;
}

.header-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  font-size: 20px;
}

.icon-bell {
  background: #fef0f0;
  color: #f56c6c;
}

.icon-user {
  background: #ecf5ff;
  color: #409eff;
}

.panel-header h3 {
  flex: 1;
  margin: 0;
  font-size: 1.1rem;
  color: #333;
}

.badge {
  background: #f56c6c;
  color: #fff;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: bold;
}

.count {
  color: #999;
  font-size: 0.9rem;
}

.panel-body {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
}

.list-container {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.list-item {
  display: flex;
  align-items: center;
  padding: 15px;
  border-radius: 12px;
  transition: background 0.2s;
}

.list-item:hover {
  background: #f9fafe;
}

.req-left {
  display: flex;
  align-items: center;
  flex: 1;
}

.item-info {
  flex: 1;
  margin-left: 15px;
  display: flex;
  flex-direction: column;
}

.item-info .name {
  font-weight: 600;
  color: #333;
}

.item-info .desc {
  font-size: 0.8rem;
  color: #999;
  margin-top: 2px;
}

.req-actions {
  display: flex;
  gap: 5px;
}

.friend-item-row {
  justify-content: space-between;
}

.friend-left {
  display: flex;
  align-items: center;
  flex: 1;
  cursor: pointer;
}

.friend-avatar {
  border: 2px solid #fff;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.name-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.mini-mbti {
  background: #ecf5ff;
  color: #409eff;
  font-size: 10px;
  padding: 1px 4px;
  border-radius: 4px;
}

.status-indicator {
  font-size: 0.8rem;
  color: #999; 
  margin-top: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 150px;
}

.actions {
  display: flex;
  gap: 8px;
  opacity: 0.8;
}

.list-item:hover .actions {
  opacity: 1;
}

.fade-in-up {
  animation: fadeInUp 0.5s ease-out forwards;
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.pointer {
  cursor: pointer;
}

/* Dialog Styles */
.dialog-profile {
  text-align: center;
  padding: 20px 0;
}

.dialog-avatar {
  border: 4px solid #f0f2f5;
  margin-bottom: 15px;
}

.dialog-name {
  font-size: 1.5rem;
  color: #333;
  margin: 0 0 10px;
}

.dialog-mbti {
  display: inline-block;
  background: #f0f9eb;
  color: #67c23a;
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: bold;
  margin-bottom: 15px;
}

.dialog-bio {
  color: #666;
  background: #f9f9f9;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 25px;
  line-height: 1.6;
}
</style>
