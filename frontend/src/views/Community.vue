<template>
  <div class="community-container">
    <div class="hero-section">
      <div class="hero-content">
        <h2>共鸣社区</h2>
        <p>每一个故事，都值得被倾听</p>
      </div>
      <div class="hero-wave"></div>
    </div>

    <div class="main-layout">
      <!-- Left: Feed -->
      <div class="feed-container">
        <!-- Search Bar -->
        <div class="search-bar">
          <el-input 
            v-model="keyword" 
            placeholder="搜索感兴趣的话题..." 
            prefix-icon="Search"
            clearable
            @keyup.enter="loadPosts(1)"
            @clear="loadPosts(1)"
            class="search-input"
          >
            <template #append>
              <el-button @click="loadPosts(1)">搜索</el-button>
            </template>
          </el-input>
        </div>

        <!-- Publish Box -->
        <div class="create-post-card">
          <div class="card-title">分享新鲜事</div>
          <div class="input-wrapper">
            <div class="user-avatar-area">
              <el-avatar :size="50" :src="userStore.userInfo.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
            </div>
            <div class="input-area">
              <el-input 
                v-model="newPost.title" 
                placeholder="请输入标题（选填）" 
                class="title-input"
              />
              <el-input 
                v-model="newPost.content" 
                type="textarea" 
                placeholder="此刻你想说什么？" 
                :rows="3" 
                resize="none"
                class="community-input"
              />
              <div class="publish-bar">
                <div class="publish-tools">
                  <el-button circle size="small"><el-icon><Picture /></el-icon></el-button>
                  <el-button circle size="small"><el-icon><Location /></el-icon></el-button>
                  <el-button circle size="small"><el-icon><Link /></el-icon></el-button>
                </div>
                <el-button type="primary" round class="pub-btn" @click="publish">发布动态</el-button>
              </div>
            </div>
          </div>
        </div>
        
        <!-- Posts List -->
        <div v-for="post in posts" :key="post.id" class="feed-card fade-up">
          <div class="card-header">
            <div class="avatar-wrapper" @click="showUserProfile(post.userId)">
              <el-avatar :size="48" :src="post.userAvatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" class="post-avatar pointer" />
            </div>
            <div class="header-info">
              <span class="nickname pointer" @click="showUserProfile(post.userId)">{{ post.username || '用户' }}</span>
              <span class="post-time">{{ formatTime(post.createTime) }}</span>
            </div>
            <el-dropdown v-if="post.userId === userStore.userInfo.id" trigger="click" @command="handleCommand($event, post)">
              <span class="el-dropdown-link">
                <el-icon><MoreFilled /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="delete" style="color: #f56c6c;">删除动态</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
          
          <div class="card-content-wrapper">
            <h3 class="post-title">{{ post.title || '无题' }}</h3>
            <div class="card-content">
              {{ post.content }}
            </div>
          </div>
          
          <div class="card-actions">
            <div class="action-btn" :class="{ active: post.isLiked }" @click="like(post)">
              <div class="icon-bg">
                <el-icon><Star /></el-icon>
              </div>
              <span>{{ post.likeCount || 0 }}</span>
            </div>
            <div class="action-btn" :class="{ active: post.showComments }" @click="toggleComments(post)">
               <div class="icon-bg">
                <el-icon><ChatDotRound /></el-icon>
               </div>
              <span>{{ post.replyCount || 0 }}</span>
            </div>
            <div class="action-btn">
               <div class="icon-bg">
                <el-icon><Share /></el-icon>
               </div>
              <span>分享</span>
            </div>
          </div>
          
          <!-- Comments Section -->
          <transition name="el-zoom-in-top">
            <div v-if="post.showComments" class="comments-box">
              <div class="comment-list">
                <div v-for="comment in post.comments" :key="comment.id" class="comment-row">
                  <div class="c-avatar-wrapper" @click="showUserProfile(comment.userId)">
                    <el-avatar :size="32" :src="comment.userAvatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" class="pointer" />
                  </div>
                  <div class="comment-body">
                    <div class="comment-bubble">
                      <span class="c-user pointer" @click="showUserProfile(comment.userId)">{{ comment.username }}</span>
                      <span class="c-content">{{ comment.content }}</span>
                    </div>
                    <div class="comment-footer">
                      <span class="c-time">{{ formatTime(comment.createTime) }}</span>
                      <span 
                        v-if="comment.userId === userStore.userInfo.id" 
                        class="c-delete" 
                        @click="deleteComment(post, comment.id)"
                      >删除</span>
                    </div>
                  </div>
                </div>
              </div>
              
              <div class="write-comment">
                <el-input 
                  v-model="post.newComment" 
                  placeholder="写下你的评论..." 
                  class="comment-input"
                  @keyup.enter="submitComment(post)"
                >
                  <template #suffix>
                    <el-button type="primary" link @click="submitComment(post)">发送</el-button>
                  </template>
                </el-input>
              </div>
            </div>
          </transition>
        </div>
        
        <el-pagination 
          layout="prev, pager, next" 
          :total="total" 
          @current-change="loadPosts" 
          class="community-pagination"
        />
      </div>

      <!-- Right: Sidebar -->
      <div class="sidebar">
        <div class="sidebar-card rules-card">
          <div class="card-header">
            <el-icon class="header-icon"><Collection /></el-icon>
            <h3>社区公约</h3>
          </div>
          <div class="rules-list">
            <div class="rule-item">
              <span class="rule-num">1</span>
              <span class="rule-text">尊重他人，文明发言，禁止人身攻击。</span>
            </div>
            <div class="rule-item">
              <span class="rule-num">2</span>
              <span class="rule-text">保护隐私，不随意透露他人个人信息。</span>
            </div>
            <div class="rule-item">
              <span class="rule-num">3</span>
              <span class="rule-text">鼓励分享真实经历，传递正能量。</span>
            </div>
            <div class="rule-item">
              <span class="rule-num">4</span>
              <span class="rule-text">禁止发布广告、垃圾信息或违法内容。</span>
            </div>
            <div class="rule-item">
              <span class="rule-num">5</span>
              <span class="rule-text">共同维护社区环境，发现违规请举报。</span>
            </div>
          </div>
        </div>

        <div class="sidebar-card topic-card">
          <div class="card-header">
            <el-icon class="header-icon"><TrendCharts /></el-icon>
            <h3>热门话题</h3>
          </div>
          <div class="topic-list">
            <div class="topic-item"># 今天的治愈瞬间</div>
            <div class="topic-item"># 战胜焦虑小技巧</div>
            <div class="topic-item"># 晚安计划</div>
            <div class="topic-item"># 书籍推荐</div>
          </div>
        </div>
      </div>
    </div>

    <!-- User Profile Dialog -->
    <el-dialog
      v-model="dialogVisible"
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
        
        <div class="dialog-actions" v-if="currentUser.id !== userStore.userInfo.id">
          <el-button 
            v-if="isFriend" 
            type="danger" 
            round 
            @click="deleteFriend(currentUser.id)"
          >
            <el-icon><Delete /></el-icon> 删除好友
          </el-button>
          <el-button 
            v-else 
            type="primary" 
            round 
            @click="addFriend(currentUser.id)"
          >
            <el-icon><Plus /></el-icon> 加为好友
          </el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import request from '../utils/request'
import { useUserStore } from '../stores/user'
import { wsManager } from '../utils/websocket'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Star, ChatDotRound, Share, MoreFilled, Picture, Location, Link, Search, Collection, TrendCharts, Plus, Delete } from '@element-plus/icons-vue'

const newPost = ref({ title: '', content: '' })
const posts = ref([])
const total = ref(0)
const keyword = ref('')
const userStore = useUserStore()

// Profile Dialog State
const dialogVisible = ref(false)
const currentUser = ref(null)
const isFriend = ref(false)
const friendList = ref([])

const loadPosts = async (page = 1) => {
  try {
    const res = await request.get('/post/list', { 
      params: { 
        page, 
        size: 10, 
        type: 2,
        keyword: keyword.value
      } 
    })
    posts.value = res.records.map(p => ({ 
      ...p, 
      showComments: false, 
      comments: [], 
      newComment: '' 
    }))
    total.value = res.total
  } catch (e) {
    console.error(e)
  }
}

const loadFriends = async () => {
  try {
    const res = await request.get('/friend/list')
    friendList.value = res || []
  } catch (e) {
    console.error(e)
  }
}

const showUserProfile = async (userId) => {
  try {
    // We can use the search endpoint to get user info by ID indirectly or implement a new endpoint
    // Here I'll assume we fetch by username if available, or just fetch post user info if backend supports getById
    // Since we don't have getById, let's search by username from the post data if available. 
    // Actually, searching by username is risky if duplicates exist (though unlikely). 
    // Best way: check if we have the user object in post. If not full info, we might need a new endpoint.
    // For now, let's try to find the post that triggered this and use its user info + extra call if needed.
    
    // Better approach: Since we need MBTI and Bio, and post list might not have it all (backend update needed?), 
    // let's assume we update the backend to return user details or use a new endpoint.
    // Wait, the backend post list only sets username and avatar.
    
    // Let's implement a getInfoById helper or search by username if unique.
    // Actually, let's use the `search` endpoint if we have the username.
    const post = posts.value.find(p => p.userId === userId)
    if (!post && !userId) return
    
    // Fallback: search by username
    const username = post ? post.username : ''
    const res = await request.get('/user/search', { params: { username } })
    
    if (res) {
      currentUser.value = res
      // Check if is friend
      isFriend.value = friendList.value.some(f => f.id === res.id)
      dialogVisible.value = true
    }
  } catch (e) {
    ElMessage.error('无法获取用户信息')
  }
}

const addFriend = async (id) => {
  try {
    await request.post(`/friend/add/${id}`)
    ElMessage.success('好友申请已发送')
    dialogVisible.value = false
  } catch (e) {
    // handled
  }
}

const deleteFriend = async (id) => {
  ElMessageBox.confirm('确定要删除这位好友吗？', '提示', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    confirmButtonClass: 'el-button--danger'
  }).then(async () => {
    await request.post(`/friend/delete/${id}`)
    ElMessage.success('好友已删除')
    dialogVisible.value = false
    loadFriends() // Refresh list
  }).catch(() => {})
}

const publish = async () => {
  if (!newPost.value.content.trim()) return ElMessage.warning('内容不能为空')
  await request.post('/post/create', { 
    type: 2, 
    title: newPost.value.title, 
    content: newPost.value.content 
  })
  ElMessage.success('发布成功')
  newPost.value = { title: '', content: '' }
  loadPosts()
}

const handleCommand = (command, post) => {
  if (command === 'delete') {
    ElMessageBox.confirm('确定要删除这条动态吗？', '提示', {
      type: 'warning'
    }).then(async () => {
      await request.post(`/post/delete/${post.id}`)
      ElMessage.success('已删除')
      loadPosts()
    }).catch(() => {})
  }
}

const like = async (post) => {
  await request.post(`/post/like/${post.id}`)
  loadPosts()
}

const toggleComments = async (post) => {
  if (!post.showComments) {
    const res = await request.get(`/post/comments/${post.id}`)
    post.comments = res
    post.showComments = true
  } else {
    post.showComments = false
  }
}

const submitComment = async (post) => {
  if (!post.newComment.trim()) return
  await request.post('/post/comment', { postId: post.id, content: post.newComment })
  post.newComment = ''
  const res = await request.get(`/post/comments/${post.id}`)
  post.comments = res
  post.replyCount++
}

const deleteComment = async (post, commentId) => {
  try {
    await request.post(`/post/comment/delete/${commentId}`)
    ElMessage.success('已删除')
    const res = await request.get(`/post/comments/${post.id}`)
    post.comments = res
    post.replyCount--
  } catch (e) {
    // Handled
  }
}

const formatTime = (time) => {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 16)
}

const handleNewPost = () => {
  // Option 1: auto reload. Option 2: show a "new posts" button. Here we auto reload.
  loadPosts()
}

let pollTimer = null

onMounted(() => {
  loadPosts()
  loadFriends()
  wsManager.on('NEW_POST_2', handleNewPost)
  
  pollTimer = setInterval(() => {
    loadPosts()
  }, 5000)
})

onUnmounted(() => {
  wsManager.off('NEW_POST_2', handleNewPost)
  if (pollTimer) clearInterval(pollTimer)
})
</script>

<style scoped>
.community-container {
  background-color: #f4f6f9;
  min-height: calc(100vh - 60px);
}

.hero-section {
  background: linear-gradient(120deg, #a1c4fd 0%, #c2e9fb 100%);
  padding: 60px 0 100px;
  text-align: center;
  position: relative;
  margin-bottom: -60px;
}

.hero-content h2 {
  font-size: 2.5rem;
  color: #fff;
  margin-bottom: 10px;
  text-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.hero-content p {
  color: #fff;
  font-size: 1.2rem;
  opacity: 0.9;
}

.main-layout {
  max-width: 1100px;
  margin: 0 auto;
  padding: 0 20px 40px;
  position: relative;
  z-index: 10;
  display: flex;
  gap: 30px;
}

/* Left Feed */
.feed-container {
  flex: 1;
  max-width: 740px;
}

.search-bar {
  margin-bottom: 20px;
}

:deep(.search-input .el-input__wrapper) {
  border-radius: 50px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.05);
  padding-left: 15px;
}

:deep(.search-input .el-input-group__append) {
  border-top-right-radius: 50px;
  border-bottom-right-radius: 50px;
  background-color: #fff;
  border: none;
  box-shadow: 0 4px 15px rgba(0,0,0,0.05);
}

.create-post-card {
  background: #fff;
  border-radius: 16px;
  padding: 25px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.05);
  margin-bottom: 30px;
}

.card-title {
  font-weight: 600;
  color: #333;
  margin-bottom: 15px;
  font-size: 1.1rem;
}

.input-wrapper {
  display: flex;
  gap: 15px;
}

.input-area {
  flex: 1;
}

:deep(.title-input .el-input__wrapper) {
  box-shadow: none;
  border-bottom: 1px solid #eee;
  border-radius: 0;
  padding-left: 0;
  margin-bottom: 10px;
  font-weight: 600;
  font-size: 16px;
}

:deep(.title-input .el-input__wrapper:focus-within) {
  box-shadow: none;
  border-bottom-color: #a1c4fd;
}

:deep(.community-input .el-textarea__inner) {
  border: none;
  background: #f9f9f9;
  border-radius: 12px;
  padding: 15px;
  font-size: 15px;
  transition: all 0.3s;
}

:deep(.community-input .el-textarea__inner:focus) {
  background: #fff;
  box-shadow: 0 0 0 2px #a1c4fd;
}

.publish-bar {
  margin-top: 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.publish-tools .el-button {
  color: #666;
  border: none;
  background: #f5f5f5;
}

.publish-tools .el-button:hover {
  color: #409eff;
  background: #ecf5ff;
}

.pub-btn {
  padding: 10px 30px;
  background: linear-gradient(90deg, #a1c4fd 0%, #c2e9fb 100%);
  border: none;
  box-shadow: 0 4px 15px rgba(161, 196, 253, 0.4);
}

.pub-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(161, 196, 253, 0.5);
}

.feed-card {
  background: #fff;
  border-radius: 16px;
  padding: 25px;
  margin-bottom: 25px;
  box-shadow: 0 5px 20px rgba(0,0,0,0.03);
  transition: all 0.3s;
  border: 1px solid rgba(0,0,0,0.02);
}

.feed-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 30px rgba(0,0,0,0.06);
}

.card-header {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.post-avatar {
  border: 2px solid #fff;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.header-info {
  flex: 1;
  margin-left: 15px;
  display: flex;
  flex-direction: column;
}

.nickname {
  font-weight: 700;
  font-size: 16px;
  color: #333;
}

.post-time {
  font-size: 12px;
  color: #999;
  margin-top: 3px;
}

.el-dropdown-link {
  cursor: pointer;
  color: #ccc;
  padding: 5px;
  border-radius: 50%;
  transition: all 0.2s;
}

.el-dropdown-link:hover {
  background: #f5f5f5;
  color: #666;
}

.card-content-wrapper {
  padding-left: 63px;
  margin-bottom: 20px;
}

.post-title {
  font-size: 18px;
  font-weight: 700;
  color: #333;
  margin-bottom: 8px;
  line-height: 1.4;
}

.card-content {
  font-size: 15px;
  line-height: 1.7;
  color: #555;
  white-space: pre-wrap;
}

.card-actions {
  display: flex;
  justify-content: flex-end; /* Right align actions */
  gap: 20px;
  padding-top: 15px;
  border-top: 1px solid #f9f9f9;
  padding-left: 63px;
}

.action-btn {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #888;
  font-size: 14px;
  transition: all 0.2s;
}

.icon-bg {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f9f9f9;
  transition: all 0.2s;
}

.action-btn:hover .icon-bg {
  background: #ecf5ff;
  color: #409eff;
}

.action-btn.active .icon-bg {
  background: #fef0f0;
  color: #f56c6c;
}

.action-btn.active span {
  color: #f56c6c;
}

.comments-box {
  margin-top: 20px;
  padding-left: 63px;
}

.comment-row {
  display: flex;
  gap: 12px;
  margin-bottom: 15px;
}

.comment-body {
  flex: 1;
}

.comment-bubble {
  background: #f4f6f9;
  padding: 10px 15px;
  border-radius: 12px;
  border-top-left-radius: 2px;
  display: inline-block;
}

.c-user {
  font-weight: 700;
  color: #333;
  margin-right: 8px;
  font-size: 13px;
}

.c-content {
  font-size: 14px;
  color: #555;
}

.comment-footer {
  margin-top: 5px;
  font-size: 12px;
  color: #aaa;
  padding-left: 5px;
}

.c-delete {
  color: #f56c6c;
  cursor: pointer;
  margin-left: 10px;
  opacity: 0;
  transition: opacity 0.2s;
}

.comment-row:hover .c-delete {
  opacity: 1;
}

.write-comment {
  margin-top: 15px;
}

:deep(.comment-input .el-input__wrapper) {
  border-radius: 20px;
  background: #f4f6f9;
  box-shadow: none;
}

:deep(.comment-input .el-input__wrapper:focus-within) {
  background: #fff;
  box-shadow: 0 0 0 1px #a1c4fd;
}

/* Sidebar */
.sidebar {
  width: 300px;
}

.sidebar-card {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 5px 20px rgba(0,0,0,0.03);
}

.rules-card .card-header, .topic-card .card-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #f0f0f0;
}

.header-icon {
  font-size: 20px;
  color: #409eff;
  margin-right: 10px;
}

.rules-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.rule-item {
  display: flex;
  align-items: flex-start;
  font-size: 14px;
  color: #666;
  line-height: 1.5;
}

.rule-num {
  display: inline-block;
  width: 20px;
  height: 20px;
  background: #ecf5ff;
  color: #409eff;
  border-radius: 50%;
  text-align: center;
  line-height: 20px;
  font-size: 12px;
  margin-right: 10px;
  flex-shrink: 0;
}

.topic-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.topic-item {
  background: #f4f6f9;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 13px;
  color: #555;
  cursor: pointer;
  transition: all 0.2s;
}

.topic-item:hover {
  background: #ecf5ff;
  color: #409eff;
}

.fade-up {
  animation: fadeUp 0.6s ease-out;
}

@keyframes fadeUp {
  from { opacity: 0; transform: translateY(30px); }
  to { opacity: 1; transform: translateY(0); }
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

.pointer {
  cursor: pointer;
}
</style>
