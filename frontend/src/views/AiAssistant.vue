<template>
  <div class="ai-chat-container">
    <div class="chat-wrapper">
      <!-- 侧边栏/头部信息 -->
      <div class="chat-sidebar" :class="{ 'sidebar-xiaoxin': currentAssistant === 'xiaoxin' }">
        <div class="assistant-switcher">
          <div 
            class="switch-btn" 
            :class="{ active: currentAssistant === 'xiaonuan' }"
            @click="switchAssistant('xiaonuan')"
          >
            小暖
          </div>
          <div 
            class="switch-btn" 
            :class="{ active: currentAssistant === 'xiaoxin' }"
            @click="switchAssistant('xiaoxin')"
          >
            小馨
          </div>
        </div>

        <div class="assistant-profile" v-if="currentAssistant === 'xiaonuan'">
          <div class="avatar-wrapper">
            <img src="https://api.dicebear.com/7.x/avataaars/svg?seed=Coco&backgroundColor=b6e3f4" alt="AI Avatar" class="assistant-avatar" />
            <div class="status-dot"></div>
          </div>
          <h3 class="assistant-name">小暖</h3>
          <p class="assistant-desc">我是你的专属心理伙伴，<br>随时准备倾听你的心声。</p>
        </div>

        <div class="assistant-profile" v-else>
          <div class="avatar-wrapper">
            <img src="https://api.dicebear.com/7.x/avataaars/svg?seed=Lily&backgroundColor=ffdfba&mouth=smile&eyes=happy&eyebrows=default&clothing=collarAndSweater&hair=longHair" alt="AI Avatar" class="assistant-avatar" />
            <div class="status-dot"></div>
          </div>
          <h3 class="assistant-name">小馨</h3>
          <p class="assistant-desc">我是温柔的邻家姐姐，<br>愿用温暖治愈你的疲惫。</p>
        </div>

        <div class="chat-tips">
          <h4>你可以和我说：</h4>
          <ul>
            <li @click="setInput('最近工作压力好大，感觉喘不过气...')">"最近工作压力好大..."</li>
            <li @click="setInput('虽然没什么事，但就是觉得不开森')">"莫名其妙不开心..."</li>
            <li @click="setInput('今晚失眠了，想聊聊天')">"今晚失眠了..."</li>
          </ul>
        </div>
      </div>

      <!-- 聊天主区域 -->
      <div class="chat-main">
        <div class="chat-header-mobile">
          <span class="title">心理小助手 · {{ currentAssistant === 'xiaonuan' ? '小暖' : '小馨' }}</span>
          <span class="status">在线</span>
        </div>

        <div class="messages-container" ref="messagesContainer">
          <div v-if="currentMessages.length === 0" class="empty-state">
            <img 
              :src="currentAssistant === 'xiaonuan' ? 'https://api.dicebear.com/7.x/avataaars/svg?seed=Coco&backgroundColor=b6e3f4&mouth=smile' : 'https://api.dicebear.com/7.x/avataaars/svg?seed=Lily&backgroundColor=ffdfba&mouth=smile&eyes=happy&eyebrows=default&clothing=collarAndSweater&hair=longHair'" 
              class="welcome-avatar" 
            />
            <p class="welcome-text" v-if="currentAssistant === 'xiaonuan'">
              你好呀！我是小暖 ☀️<br>今天过得怎么样？无论开心还是难过，我都在这里陪着你。
            </p>
            <p class="welcome-text" v-else>
              嗨~ 我是小馨 🌸<br>累了吧？坐下来，我们可以慢慢聊。
            </p>
          </div>
          
          <div v-for="(msg, index) in currentMessages" :key="index" :class="['message-item', msg.role === 'user' ? 'user-message' : 'ai-message']">
            <div class="avatar" v-if="msg.role === 'ai'">
              <img 
                :src="currentAssistant === 'xiaonuan' ? 'https://api.dicebear.com/7.x/avataaars/svg?seed=Coco&backgroundColor=b6e3f4' : 'https://api.dicebear.com/7.x/avataaars/svg?seed=Lily&backgroundColor=ffdfba&mouth=smile&eyes=happy&eyebrows=default&clothing=collarAndSweater&hair=longHair'" 
                alt="AI" 
              />
            </div>
            <div class="content">
              <div class="bubble" :class="{ 'xiaoxin-bubble': currentAssistant === 'xiaoxin' && msg.role === 'user' }">
                <span v-if="msg.role === 'ai'" v-html="formatMessage(msg.content)"></span>
                <span v-else>{{ msg.content }}</span>
              </div>
              <span class="time">{{ formatTime(new Date()) }}</span>
            </div>
            <div class="avatar" v-if="msg.role === 'user'">
              <el-avatar :src="userStore.userInfo.avatar" :size="40">{{ userStore.userInfo.username?.charAt(0) }}</el-avatar>
            </div>
          </div>
          
          <div v-if="loading" class="message-item ai-message">
            <div class="avatar">
              <img 
                :src="currentAssistant === 'xiaonuan' ? 'https://api.dicebear.com/7.x/avataaars/svg?seed=Coco&backgroundColor=b6e3f4' : 'https://api.dicebear.com/7.x/avataaars/svg?seed=Lily&backgroundColor=ffdfba&mouth=smile&eyes=happy&eyebrows=default&clothing=collarAndSweater&hair=longHair'" 
                alt="AI" 
              />
            </div>
            <div class="content">
              <div class="bubble loading-bubble">
                <div class="typing-indicator">
                  <span></span><span></span><span></span>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <div class="input-area">
          <div class="input-wrapper" :class="{ 'xiaoxin-input': currentAssistant === 'xiaoxin' }">
            <el-input
              v-model="inputMessage"
              type="textarea"
              :rows="1"
              :autosize="{ minRows: 1, maxRows: 4 }"
              :placeholder="currentAssistant === 'xiaonuan' ? '告诉小暖你的心事吧...' : '和小馨说说悄悄话...'"
              @keyup.enter.ctrl="sendMessage"
              class="custom-textarea"
              resize="none"
            />
            <el-button type="primary" circle class="send-btn" :class="{ 'xiaoxin-btn': currentAssistant === 'xiaoxin' }" @click="sendMessage" :loading="loading" :disabled="!inputMessage.trim()">
              <el-icon><Promotion /></el-icon>
            </el-button>
          </div>
          <div class="input-footer">
            <span class="tip">按 Ctrl + Enter 发送</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted, computed } from 'vue'
import { useUserStore } from '../stores/user'
import request from '../utils/request'
import { Promotion } from '@element-plus/icons-vue'

const userStore = useUserStore()
const currentAssistant = ref('xiaonuan') // xiaonuan or xiaoxin
const messagesMap = ref({
  xiaonuan: [],
  xiaoxin: []
})

const currentMessages = computed(() => messagesMap.value[currentAssistant.value])

const inputMessage = ref('')
const loading = ref(false)
const messagesContainer = ref(null)

const switchAssistant = (assistant) => {
  currentAssistant.value = assistant
  scrollToBottom()
}

const formatMessage = (content) => {
  return content.replace(/\n/g, '<br>')
}

const formatTime = (date) => {
  return date.getHours().toString().padStart(2, '0') + ':' + date.getMinutes().toString().padStart(2, '0')
}

const setInput = (text) => {
  inputMessage.value = text
}

const scrollToBottom = async () => {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

const sendMessage = async () => {
  const content = inputMessage.value.trim()
  if (!content) return
  
  const assistantId = currentAssistant.value
  messagesMap.value[assistantId].push({ role: 'user', content: content })
  
  inputMessage.value = ''
  loading.value = true
  scrollToBottom()
  
  try {
    const res = await request.post('/ai/chat', { 
      message: content,
      assistantId: assistantId
    })
    messagesMap.value[assistantId].push({ role: 'ai', content: res })
  } catch (error) {
    console.error('AI Chat Error:', error)
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

onMounted(() => {
  scrollToBottom()
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Nunito:wght@400;600;700&display=swap');

.ai-chat-container {
  max-width: 1000px;
  margin: 20px auto;
  height: calc(100vh - 100px);
  font-family: 'Nunito', sans-serif;
  display: flex;
  justify-content: center;
}

.chat-wrapper {
  width: 100%;
  background: #fff;
  border-radius: 24px;
  box-shadow: 0 10px 40px rgba(0,0,0,0.05);
  display: flex;
  overflow: hidden;
  border: 1px solid #f0f0f0;
}

/* Sidebar */
.chat-sidebar {
  width: 280px;
  background: linear-gradient(135deg, #e0c3fc 0%, #8ec5fc 100%);
  padding: 40px 20px;
  color: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  transition: background 0.5s;
}

.chat-sidebar.sidebar-xiaoxin {
  background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 99%, #fecfef 100%);
}

.assistant-switcher {
  display: flex;
  background: rgba(255,255,255,0.2);
  border-radius: 20px;
  padding: 4px;
  margin-bottom: 30px;
  width: 100%;
}

.switch-btn {
  flex: 1;
  padding: 8px 0;
  border-radius: 16px;
  cursor: pointer;
  font-size: 0.9rem;
  font-weight: 600;
  transition: all 0.3s;
}

.switch-btn.active {
  background: #fff;
  color: #333;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.assistant-profile {
  margin-bottom: 40px;
  animation: fadeIn 0.5s;
}

.avatar-wrapper {
  position: relative;
  width: 100px;
  height: 100px;
  margin: 0 auto 15px;
}

.assistant-avatar {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  border: 4px solid rgba(255,255,255,0.3);
  background: #fff;
}

.status-dot {
  position: absolute;
  bottom: 5px;
  right: 5px;
  width: 18px;
  height: 18px;
  background: #67c23a;
  border: 3px solid #fff;
  border-radius: 50%;
}

.assistant-name {
  font-size: 1.5rem;
  margin: 0 0 10px;
  font-weight: 700;
}

.assistant-desc {
  font-size: 0.9rem;
  opacity: 0.9;
  line-height: 1.5;
}

.chat-tips {
  width: 100%;
  background: rgba(255,255,255,0.1);
  padding: 20px;
  border-radius: 16px;
  text-align: left;
}

.chat-tips h4 {
  margin: 0 0 15px;
  font-size: 0.95rem;
  opacity: 0.9;
}

.chat-tips ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.chat-tips li {
  background: rgba(255,255,255,0.2);
  margin-bottom: 10px;
  padding: 8px 12px;
  border-radius: 8px;
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.2s;
}

.chat-tips li:hover {
  background: rgba(255,255,255,0.3);
  transform: translateX(5px);
}

/* Main Chat Area */
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #fcfcfc;
  position: relative;
}

.chat-header-mobile {
  display: none; /* Only show on mobile if needed */
  padding: 15px 20px;
  border-bottom: 1px solid #f0f0f0;
  align-items: center;
  justify-content: space-between;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 30px;
  scroll-behavior: smooth;
}

.empty-state {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  opacity: 0.8;
}

.welcome-avatar {
  width: 120px;
  margin-bottom: 20px;
  animation: float 3s ease-in-out infinite;
}

.welcome-text {
  color: #888;
  font-size: 1.1rem;
  line-height: 1.6;
}

.message-item {
  display: flex;
  margin-bottom: 25px;
  align-items: flex-end;
}

.user-message {
  flex-direction: row-reverse;
}

.avatar img, .avatar .el-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  box-shadow: 0 4px 10px rgba(0,0,0,0.1);
}

.ai-message .avatar { margin-right: 15px; }
.user-message .avatar { margin-left: 15px; }

.content {
  max-width: 70%;
  display: flex;
  flex-direction: column;
}

.user-message .content { align-items: flex-end; }

.bubble {
  padding: 12px 18px;
  border-radius: 18px;
  font-size: 15px;
  line-height: 1.6;
  position: relative;
  box-shadow: 0 2px 5px rgba(0,0,0,0.03);
}

.ai-message .bubble {
  background: #fff;
  color: #333;
  border-bottom-left-radius: 4px;
  border: 1px solid #f0f0f0;
}

.user-message .bubble {
  background: linear-gradient(135deg, #8ec5fc 0%, #e0c3fc 100%);
  color: #fff;
  border-bottom-right-radius: 4px;
}

.user-message .bubble.xiaoxin-bubble {
  background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 99%);
}

.time {
  font-size: 11px;
  color: #ccc;
  margin-top: 5px;
  padding: 0 5px;
}

/* Input Area */
.input-area {
  padding: 20px 30px;
  background: #fff;
  border-top: 1px solid #f5f5f5;
}

.input-wrapper {
  display: flex;
  align-items: flex-end;
  background: #f8f9fa;
  border-radius: 24px;
  padding: 8px;
  border: 1px solid transparent;
  transition: all 0.3s;
}

.input-wrapper:focus-within {
  background: #fff;
  border-color: #e0c3fc;
  box-shadow: 0 0 0 3px rgba(224, 195, 252, 0.2);
}

.input-wrapper.xiaoxin-input:focus-within {
  border-color: #ff9a9e;
  box-shadow: 0 0 0 3px rgba(255, 154, 158, 0.2);
}

.custom-textarea :deep(.el-textarea__inner) {
  box-shadow: none !important;
  background: transparent !important;
  border: none !important;
  padding: 10px 15px;
  font-size: 15px;
  color: #333;
}

.send-btn {
  margin-left: 10px;
  background: linear-gradient(135deg, #e0c3fc 0%, #8ec5fc 100%);
  border: none;
  width: 40px;
  height: 40px;
  font-size: 18px;
}

.send-btn.xiaoxin-btn {
  background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 99%);
}

.send-btn:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 10px rgba(142, 197, 252, 0.4);
}

.input-footer {
  text-align: right;
  margin-top: 8px;
}

.input-footer .tip {
  font-size: 12px;
  color: #bbb;
}

/* Loading Animation */
.typing-indicator {
  display: flex;
  align-items: center;
  height: 20px;
}

.typing-indicator span {
  display: block;
  width: 6px;
  height: 6px;
  background-color: #bbb;
  border-radius: 50%;
  margin: 0 2px;
  animation: bounce 1.4s infinite ease-in-out both;
}

.typing-indicator span:nth-child(1) { animation-delay: -0.32s; }
.typing-indicator span:nth-child(2) { animation-delay: -0.16s; }

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

@keyframes float {
  0% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
  100% { transform: translateY(0); }
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

/* Responsive */
@media (max-width: 768px) {
  .chat-sidebar { display: none; }
  .chat-header-mobile { display: flex; }
  .ai-chat-container { height: calc(100vh - 60px); margin: 0; }
  .chat-wrapper { border-radius: 0; border: none; }
  .messages-container { padding: 15px; }
  .input-area { padding: 15px; }
}
</style>
