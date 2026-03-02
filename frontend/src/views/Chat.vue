<template>
  <div class="chat-container">
    <div class="user-list">
      <div class="search-box">
        <el-input prefix-icon="Search" placeholder="搜索好友..." v-model="searchText" />
      </div>
      <div v-for="friend in filteredFriends" :key="friend.id" 
           class="user-item" :class="{ active: currentFriend?.id === friend.id }"
           @click="selectFriend(friend)">
        <el-avatar :size="40" :src="friend.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
        <div class="user-info">
          <span class="username">{{ friend.username }}</span>
          <span class="last-msg">点击开始聊天</span>
        </div>
        <div class="user-actions">
           <el-button type="danger" link size="small" @click.stop="deleteConversation(friend)">
             <el-icon><Delete /></el-icon>
           </el-button>
        </div>
      </div>
    </div>
    
    <div class="chat-main" v-if="currentFriend">
      <div class="chat-header">
        <div class="header-info pointer" @click="showUserProfile(currentFriend)">
          <el-avatar :size="36" :src="currentFriend.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" style="margin-right: 10px;" />
          <span class="friend-name">{{ currentFriend.username }}</span>
          <span class="status-dot"></span>
        </div>
        <div class="header-actions">
          <el-button icon="MoreFilled" circle text />
        </div>
      </div>
      
      <div class="messages-area" ref="msgArea">
        <div v-for="msg in messages" :key="msg.id" 
             class="msg-row" :class="{ 'msg-mine': msg.senderId === userStore.userInfo.id }">
           <el-avatar v-if="msg.senderId !== userStore.userInfo.id" :size="36" :src="currentFriend.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" class="msg-avatar pointer" @click="showUserProfile(currentFriend)" />
           
           <div class="msg-content-wrapper">
             <div class="msg-bubble" v-if="!msg.isWithdrawn">
               {{ msg.content }}
               <div class="msg-actions">
                 <el-popconfirm title="删除这条消息？" @confirm="deleteMessage(msg)">
                   <template #reference>
                     <span class="action-link">删除</span>
                   </template>
                 </el-popconfirm>
               </div>
             </div>
             <div v-else class="withdrawn-tip">
               <span>消息已撤回</span>
               <div class="msg-actions">
                 <el-popconfirm title="删除这条提示？" @confirm="deleteMessage(msg)">
                   <template #reference>
                     <span class="action-link">删除</span>
                   </template>
                 </el-popconfirm>
               </div>
             </div>
             <div v-if="msg.senderId === userStore.userInfo.id && !msg.isWithdrawn" class="msg-meta">
               <span class="withdraw-link" @click="withdraw(msg)">撤回</span>
             </div>
           </div>
           
           <el-avatar v-if="msg.senderId === userStore.userInfo.id" :size="36" :src="userStore.userInfo.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" class="msg-avatar" />
        </div>
      </div>
      
      <div class="input-area">
        <el-input 
          v-model="content" 
          type="textarea" 
          :rows="3"
          resize="none"
          placeholder="输入消息，Enter 发送..." 
          @keyup.enter.exact="send"
        />
        <div class="input-toolbar">
          <el-button type="primary" round @click="send" :disabled="!content.trim()">发送</el-button>
        </div>
      </div>
    </div>
    
    <div v-else class="empty-state">
      <el-icon :size="60" color="#dcdfe6"><ChatDotRound /></el-icon>
      <p>选择一个好友，开启温暖对话</p>
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
  </div>
</template>

<script setup>
import { ref, onMounted, computed, nextTick } from 'vue'
import request from '../utils/request'
import { useUserStore } from '../stores/user'
import { useRoute } from 'vue-router'
import { ChatDotRound, Search, MoreFilled, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const friends = ref([])
const currentFriend = ref(null)
const messages = ref([])
const content = ref('')
const searchText = ref('')
const userStore = useUserStore()
const route = useRoute()
const msgArea = ref(null)

// Profile Dialog
const profileVisible = ref(false)
const currentUser = ref(null)

const filteredFriends = computed(() => {
  return friends.value.filter(f => f.username.includes(searchText.value))
})

const loadFriends = async () => {
  try {
    // This returns List<Friendship> but with friendInfo populated.
    // However, the previous code in FriendController.getFriends() returns List<Friendship>.
    // In Chat.vue we treat it as user list.
    // Let's check how FindFriend.vue handles it. 
    // FindFriend.vue uses friend.friendInfo.
    // So here we need to map it or use friend.friendInfo.
    
    const res = await request.get('/friend/list')
    // Transform to user list format for Chat view
    friends.value = res.map(f => ({
      id: f.friendInfo.id,
      username: f.friendInfo.username,
      avatar: f.friendInfo.avatar,
      mbti: f.friendInfo.mbti,
      bio: f.friendInfo.bio
    }))
    
    if (route.query.id) {
      const friend = friends.value.find(f => f.id == route.query.id)
      if (friend) selectFriend(friend)
    }
  } catch (e) {
    console.error(e)
  }
}

const selectFriend = async (friend) => {
  currentFriend.value = friend
  await loadMessages()
}

const loadMessages = async () => {
  if (!currentFriend.value) return
  messages.value = await request.get(`/chat/history`, { params: { otherId: currentFriend.value.id } })
  scrollToBottom()
}

const send = async () => {
  if (!content.value.trim()) return
  await request.post('/chat/send', { receiverId: currentFriend.value.id, content: content.value })
  content.value = ''
  await loadMessages()
}

const withdraw = async (msg) => {
  try {
    await request.post(`/chat/withdraw/${msg.id}`)
    await loadMessages()
  } catch (e) {}
}

const deleteMessage = async (msg) => {
  try {
    await request.post(`/chat/delete/${msg.id}`)
    ElMessage.success('已删除')
    // Remove locally
    const index = messages.value.findIndex(m => m.id === msg.id)
    if (index !== -1) messages.value.splice(index, 1)
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

// const deleteWithdrawnTip = async (msg) => { ... } // Removed

const showUserProfile = (user) => {
  currentUser.value = user
  profileVisible.value = true
}

const deleteConversation = (friend) => {
  ElMessageBox.confirm(
    '确定要删除与该好友的对话记录吗？删除后您将无法查看，但对方仍可见。',
    '删除对话',
    {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    try {
      await request.post(`/chat/delete_conversation/${friend.id}`)
      ElMessage.success('对话已删除')
      
      // Clear messages if current friend is selected
      if (currentFriend.value && currentFriend.value.id === friend.id) {
        messages.value = []
      }
      
      // Optionally reload friends or messages
      // await loadFriends() 
    } catch (e) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

const scrollToBottom = () => {
  nextTick(() => {
    if (msgArea.value) {
      msgArea.value.scrollTop = msgArea.value.scrollHeight
    }
  })
}

onMounted(loadFriends)
</script>

<style scoped>
.chat-container {
  display: flex;
  height: calc(100vh - 80px);
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.05);
  overflow: hidden;
  max-width: 1200px;
  margin: 10px auto;
}

.user-list {
  width: 280px;
  background: #f7f9fc;
  border-right: 1px solid #ebedf0;
  display: flex;
  flex-direction: column;
}

.search-box {
  padding: 20px;
}

.user-item {
  padding: 15px 20px;
  display: flex;
  align-items: center;
  cursor: pointer;
  transition: all 0.2s;
  border-left: 3px solid transparent;
}

.user-item:hover {
  background: #ecf5ff;
}

.user-item.active {
  background: #fff;
  border-left-color: #409eff;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.user-info {
  margin-left: 12px;
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.user-actions {
  display: none;
}

.user-item:hover .user-actions {
  display: block;
}

.username {
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.last-msg {
  font-size: 12px;
  color: #999;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #fff;
}

.chat-header {
  height: 60px;
  padding: 0 20px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-info {
  display: flex;
  align-items: center;
}

.friend-name {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.status-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  background: #67c23a;
  border-radius: 50%;
  margin-left: 8px;
}

.messages-area {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background: #fcfcfc;
}

.msg-row {
  display: flex;
  margin-bottom: 20px;
  align-items: flex-start;
}

.msg-mine {
  flex-direction: row-reverse;
}

.msg-avatar {
  margin: 0 10px;
}

.msg-content-wrapper {
  max-width: 60%;
  display: flex;
  flex-direction: column;
}

.msg-mine .msg-content-wrapper {
  align-items: flex-end;
}

.msg-bubble {
  padding: 12px 16px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.5;
  position: relative;
  word-break: break-word;
  background: #fff;
  color: #333;
  box-shadow: 0 2px 5px rgba(0,0,0,0.05);
  border-top-left-radius: 2px;
}

/* Ensure actions are visible when hovering anywhere in the row OR on the actions themselves */
.msg-row:hover .msg-actions,
.msg-actions:hover {
  display: block;
}

.msg-actions {
  display: none;
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  font-size: 12px;
  background: rgba(255, 255, 255, 0.9);
  padding: 2px 6px;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  z-index: 10; /* Ensure it stays on top */
  white-space: nowrap; /* Prevent wrapping */
}

/* Increase hit area invisibly to bridge the gap */
.msg-actions::before {
  content: '';
  position: absolute;
  top: -10px;
  bottom: -10px;
  left: -10px;
  right: -10px;
  z-index: -1;
}

/* For received messages, actions on the right */
.msg-row:not(.msg-mine) .msg-actions {
  right: -55px; /* Move slightly further out */
  left: auto;
}

/* For sent messages, actions on the left */
.msg-row.msg-mine .msg-actions {
  left: -55px; /* Move slightly further out */
  right: auto;
}

.action-link {
  color: #f56c6c;
  cursor: pointer;
}

.action-link:hover {
  text-decoration: underline;
}

.msg-mine .msg-bubble {
  background: #409eff;
  color: #fff;
  border-top-left-radius: 12px;
  border-top-right-radius: 2px;
}

.withdrawn-tip {
  background: #f0f0f0;
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 12px;
  color: #999;
  align-self: center;
  display: flex;
  align-items: center;
  gap: 10px;
  position: relative; /* Needed for absolute positioning of actions */
}

.withdrawn-tip:hover .msg-actions {
  display: block;
}

.delete-tip-link {
  color: #409eff;
  cursor: pointer;
}

.delete-tip-link:hover {
  text-decoration: underline;
}

.msg-meta {
  margin-top: 4px;
  font-size: 12px;
}

.withdraw-link {
  color: #999;
  cursor: pointer;
  opacity: 0;
  transition: opacity 0.2s;
}

.msg-row:hover .withdraw-link {
  opacity: 1;
}

.input-area {
  padding: 20px;
  border-top: 1px solid #f0f0f0;
  background: #fff;
}

.input-toolbar {
  margin-top: 10px;
  text-align: right;
}

.empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #909399;
  background: #fcfcfc;
}

.empty-state p {
  margin-top: 15px;
  font-size: 16px;
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

/* Custom Scrollbar */
::-webkit-scrollbar {
  width: 6px;
}
::-webkit-scrollbar-thumb {
  background: #e0e0e0;
  border-radius: 3px;
}
::-webkit-scrollbar-track {
  background: transparent;
}
</style>
