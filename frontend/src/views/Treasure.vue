<template>
  <div class="treasure-container">
    <div class="header-section">
      <h2>治愈宝库</h2>
      <p>发现美好，疗愈心灵</p>
    </div>

    <el-tabs v-model="activeName" @tab-click="handleClick" class="custom-tabs">
      <!-- 治愈金句 -->
      <el-tab-pane label="治愈金句" name="1">
        <div class="refresh-bar" v-if="activeName === '1'">
          <el-button type="primary" round class="refresh-btn" @click="load('1')">
            <el-icon><Refresh /></el-icon> 换一批
          </el-button>
        </div>
        <div class="quote-grid" v-if="activeName === '1' && list.length > 0">
          <div v-for="item in list" :key="item.id" class="quote-card fade-in-up">
            <div class="quote-inner">
              <el-icon class="quote-mark left"><ChatLineSquare /></el-icon>
              <h3>{{ item.content }}</h3>
              <el-icon class="quote-mark right"><ChatLineSquare /></el-icon>
              <div class="quote-author" v-if="item.title">—— {{ item.title }}</div>
            </div>
          </div>
        </div>
        <el-empty v-else-if="activeName === '1' && !loading" description="暂无数据" />
        <div v-else-if="activeName === '1' && loading" class="loading-container">
          <el-skeleton :rows="3" animated />
        </div>
      </el-tab-pane>

      <!-- 治愈图片 -->
      <el-tab-pane label="治愈图片" name="2">
        <div class="refresh-bar" v-if="activeName === '2'">
          <el-button type="primary" round class="refresh-btn" @click="load('2')">
            <el-icon><Refresh /></el-icon> 换一批
          </el-button>
        </div>
        <div class="image-grid" v-if="activeName === '2' && list.length > 0">
          <div v-for="item in list" :key="item.id" class="image-item fade-in-up">
            <el-image 
              :src="item.content" 
              :preview-src-list="[item.content]"
              fit="cover" 
              class="healing-image"
              loading="lazy"
            >
              <template #placeholder>
                <div class="image-slot">Loading...</div>
              </template>
            </el-image>
            <div class="image-overlay">
              <span class="image-title">{{ item.title }}</span>
            </div>
          </div>
        </div>
        <el-empty v-else-if="activeName === '2' && !loading" description="暂无数据" />
        <div v-else-if="activeName === '2' && loading" class="loading-container">
          <el-skeleton :rows="3" animated />
        </div>
      </el-tab-pane>

      <!-- 治愈音乐 -->
      <el-tab-pane label="治愈音乐" name="3">
         <div class="music-list" v-if="activeName === '3' && list.length > 0">
            <div v-for="item in list" :key="item.id" class="music-item fade-in-up">
              <div class="music-cover">
                <el-icon :size="30" color="#fff"><Headset /></el-icon>
              </div>
              <div class="music-info">
                <span class="music-title">{{ item.title || '未知曲目' }}</span>
                <span class="music-desc">治愈心灵的旋律</span>
              </div>
              <audio controls :src="item.content" class="audio-player"></audio>
            </div>
         </div>
         <el-empty v-else-if="activeName === '3' && !loading" description="暂无数据" />
         <div v-else-if="activeName === '3' && loading" class="loading-container">加载中...</div>
      </el-tab-pane>

      <!-- 治愈视频 -->
      <el-tab-pane label="治愈视频" name="4">
        <div class="video-grid" v-if="activeName === '4' && list.length > 0">
           <div v-for="item in list" :key="item.id" class="video-card fade-in-up">
             <div class="video-wrapper">
               <video controls :src="item.content" class="video-player"></video>
             </div>
             <div class="video-info">
               <span>{{ item.title }}</span>
             </div>
           </div>
        </div>
        <el-empty v-else-if="activeName === '4' && !loading" description="暂无数据" />
        <div v-else-if="activeName === '4' && loading" class="loading-container">加载中...</div>
      </el-tab-pane>

      <!-- 小游戏 -->
      <el-tab-pane label="小游戏" name="5">
        <div class="game-list" v-if="activeName === '5' && list.length > 0">
           <div v-for="item in list" :key="item.id" class="game-card fade-in-up" @click="openGame(item.content)">
             <div class="game-icon">🎮</div>
             <div class="game-content">
               <h3>{{ item.title }}</h3>
               <p>点击开始游戏</p>
             </div>
             <el-button type="primary" round size="small">PLAY</el-button>
           </div>
        </div>
        <el-empty v-else-if="activeName === '5' && !loading" description="暂无数据" />
        <div v-else-if="activeName === '5' && loading" class="loading-container">加载中...</div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../utils/request'
import { ChatLineSquare, Headset, Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const activeName = ref('1')
const list = ref([])
const loading = ref(false)
let currentRequestId = 0

const load = async (specificType) => {
  const requestId = ++currentRequestId
  const type = specificType || activeName.value
  
  loading.value = true
  list.value = []
  
  try {
    console.log(`[Req:${requestId}] Loading treasure list for type:`, type)
    const endpoint = (type === '1' || type === '2') ? `/healing/random/${type}?limit=6` : `/healing/list/${type}`
    const res = await request.get(endpoint)
    
    if (requestId === currentRequestId) {
        list.value = res || []
    }
  } catch (error) {
    if (requestId === currentRequestId) {
        ElMessage.error('加载失败，请检查网络或登录状态')
    }
  } finally {
    if (requestId === currentRequestId) {
        loading.value = false
    }
  }
}

const handleClick = (tab) => {
  load(tab.props.name)
}

const openGame = (url) => {
  window.open(url, '_blank')
}

onMounted(() => {
  load()
})
</script>

<style scoped>
.treasure-container {
  padding: 40px;
  max-width: 1200px;
  margin: 0 auto;
  min-height: calc(100vh - 60px);
  background: linear-gradient(to bottom right, #fff1eb, #ace0f9);
}

.header-section {
  text-align: center;
  margin-bottom: 40px;
}
.header-section h2 {
  font-size: 2.5rem;
  color: #2c3e50;
  margin-bottom: 10px;
  font-weight: 300;
}
.header-section p {
  color: #7f8c8d;
  font-size: 1.1rem;
}

/* Custom Tabs */
:deep(.custom-tabs .el-tabs__nav-wrap::after) {
  height: 1px;
  background-color: rgba(0,0,0,0.05);
}
:deep(.custom-tabs .el-tabs__item) {
  font-size: 16px;
  font-weight: 500;
  color: #555;
}
:deep(.custom-tabs .el-tabs__item.is-active) {
  color: #409eff;
  font-weight: 600;
}

.loading-container {
  padding: 40px;
}

.refresh-bar {
  text-align: right;
  margin-bottom: 20px;
}
.refresh-btn {
  background: linear-gradient(90deg, #4facfe 0%, #00f2fe 100%);
  border: none;
  box-shadow: 0 4px 10px rgba(79, 172, 254, 0.3);
  transition: transform 0.2s;
}
.refresh-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 15px rgba(79, 172, 254, 0.4);
}

/* Quote Styles */
.quote-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 30px;
}
.quote-card {
  background: #fff;
  border-radius: 16px;
  padding: 30px;
  box-shadow: 0 10px 25px rgba(0,0,0,0.05);
  transition: all 0.3s;
  position: relative;
  overflow: hidden;
}
.quote-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 35px rgba(0,0,0,0.1);
}
.quote-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 6px;
  background: linear-gradient(90deg, #ff9a9e 0%, #fecfef 100%);
}
.quote-inner {
  text-align: center;
}
.quote-mark {
  font-size: 24px;
  color: #fad0c4;
}
.quote-mark.left { float: left; }
.quote-mark.right { float: right; transform: rotate(180deg); }
.quote-inner h3 {
  margin: 20px 0;
  font-size: 1.2rem;
  color: #444;
  line-height: 1.6;
  font-family: "Songti SC", serif;
}
.quote-author {
  text-align: right;
  color: #999;
  font-style: italic;
  margin-top: 15px;
}

/* Image Styles */
.image-grid {
  column-count: 3;
  column-gap: 20px;
}
.image-item {
  break-inside: avoid;
  margin-bottom: 20px;
  border-radius: 12px;
  overflow: hidden;
  position: relative;
  cursor: pointer;
  box-shadow: 0 5px 15px rgba(0,0,0,0.1);
}
.image-item:hover .healing-image {
  transform: scale(1.05);
}
.healing-image {
  width: 100%;
  display: block;
  transition: transform 0.5s ease;
}
.image-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  padding: 20px;
  background: linear-gradient(to top, rgba(0,0,0,0.6), transparent);
  color: #fff;
  opacity: 0;
  transition: opacity 0.3s;
}
.image-item:hover .image-overlay {
  opacity: 1;
}
.image-title {
  font-size: 1.1rem;
  font-weight: 500;
}

/* Music Styles */
.music-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.music-item {
  background: #fff;
  border-radius: 50px;
  padding: 15px 30px;
  display: flex;
  align-items: center;
  box-shadow: 0 5px 15px rgba(0,0,0,0.05);
  transition: transform 0.2s;
}
.music-item:hover {
  transform: scale(1.01);
}
.music-cover {
  width: 50px;
  height: 50px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
  box-shadow: 0 4px 10px rgba(118, 75, 162, 0.3);
}
.music-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}
.music-title {
  font-weight: 600;
  color: #333;
  font-size: 1.1rem;
}
.music-desc {
  font-size: 0.9rem;
  color: #999;
}
.audio-player {
  height: 36px;
  width: 300px;
}

/* Video Styles */
.video-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 30px;
}
.video-card {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 10px 25px rgba(0,0,0,0.05);
  transition: transform 0.3s;
}
.video-card:hover {
  transform: translateY(-5px);
}
.video-wrapper {
  position: relative;
  background: #000;
}
.video-player {
  width: 100%;
  height: 220px;
  object-fit: cover;
  display: block;
}
.video-info {
  padding: 20px;
  font-weight: 600;
  color: #333;
  background: #fff;
}

/* Game Styles */
.game-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 30px;
}
.game-card {
  background: #fff;
  border-radius: 20px;
  padding: 30px;
  text-align: center;
  box-shadow: 0 10px 20px rgba(0,0,0,0.05);
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.game-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 20px 40px rgba(0,0,0,0.1);
}
.game-icon {
  font-size: 48px;
  margin-bottom: 20px;
  background: #fdf6ec;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.game-content h3 {
  margin-bottom: 10px;
  color: #333;
}
.game-content p {
  color: #999;
  margin-bottom: 20px;
}

/* Animations */
.fade-in-up {
  animation: fadeInUp 0.5s ease-out forwards;
}
@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
