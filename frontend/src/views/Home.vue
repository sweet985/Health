<template>
  <div class="home">
    <div class="hero-section">
      <div class="hero-content">
        <h1 class="fade-in-up">心灵的港湾</h1>
        <p class="fade-in-up delay-1">在这里，倾听内心的声音，遇见温暖的灵魂。</p>
        <div class="hero-decoration"></div>
      </div>
    </div>
    
    <div class="features-container">
      <div class="carousel-wrapper">
        <el-button class="carousel-btn prev-btn" @click="scrollPrev" circle>
          <el-icon><ArrowLeft /></el-icon>
        </el-button>
        
        <div class="feature-track" ref="trackRef">
          <div class="feature-card treehole-card" @click="$router.push('/treehole')">
            <div class="card-icon"><el-icon><ChatDotRound /></el-icon></div>
            <h3>匿名树洞</h3>
            <p>戴上面具，卸下防备，在这里你可以诉说任何秘密。</p>
            <div class="card-bg-shape"></div>
          </div>
          
          <div class="feature-card treasure-card" @click="$router.push('/treasure')">
            <div class="card-icon"><el-icon><Star /></el-icon></div>
            <h3>治愈宝库</h3>
            <p>温暖的文字、图片与旋律，是抚慰心灵的良药。</p>
            <div class="card-bg-shape"></div>
          </div>
          
          <div class="feature-card community-card" @click="$router.push('/community')">
            <div class="card-icon"><el-icon><User /></el-icon></div>
            <h3>共鸣社区</h3>
            <p>分享生活点滴，在茫茫人海中找到懂你的那个TA。</p>
            <div class="card-bg-shape"></div>
          </div>
          
          <div class="feature-card assessment-card" @click="$router.push('/assessment')">
            <div class="card-icon"><el-icon><DataAnalysis /></el-icon></div>
            <h3>心理测评</h3>
            <p>专业量表，科学探索内心，发现更好的自己。</p>
            <div class="card-bg-shape"></div>
          </div>
          
          <div class="feature-card contact-card" @click="$router.push('/contact')">
            <div class="card-icon"><el-icon><PhoneFilled /></el-icon></div>
            <h3>联系我们</h3>
            <p>随时倾听您的声音，为您提供最贴心的支持。</p>
            <div class="card-bg-shape"></div>
          </div>
        </div>

        <el-button class="carousel-btn next-btn" @click="scrollNext" circle>
          <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ChatDotRound, Star, User, DataAnalysis, PhoneFilled, ArrowLeft, ArrowRight } from '@element-plus/icons-vue'

const trackRef = ref(null)

const scrollPrev = () => {
  if (trackRef.value) {
    trackRef.value.scrollBy({ left: -340, behavior: 'smooth' })
  }
}

const scrollNext = () => {
  if (trackRef.value) {
    trackRef.value.scrollBy({ left: 340, behavior: 'smooth' })
  }
}
</script>

<style scoped>
.home {
  min-height: calc(100vh - 60px);
  background: #fdfbf7;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
}

.hero-section {
  height: 400px;
  background: linear-gradient(135deg, #e0c3fc 0%, #8ec5fc 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  position: relative;
  overflow: hidden;
  clip-path: ellipse(150% 100% at 50% 0%);
}

.hero-content {
  z-index: 2;
  color: #fff;
}

.hero-content h1 {
  font-size: 3.5rem;
  margin-bottom: 1rem;
  text-shadow: 0 4px 6px rgba(0,0,0,0.1);
  font-weight: 300;
  letter-spacing: 2px;
}

.hero-content p {
  font-size: 1.5rem;
  opacity: 0.9;
  font-weight: 300;
}

.features-container {
  max-width: 1200px;
  margin: -60px auto 0;
  padding: 0 20px;
  position: relative;
  z-index: 3;
}

.carousel-wrapper {
  display: flex;
  align-items: center;
  position: relative;
  margin: 20px 0;
  padding: 20px 0;
}

.feature-track {
  display: flex;
  gap: 30px;
  overflow-x: auto;
  scroll-behavior: smooth;
  padding: 20px 10px;
  /* Hide scrollbar */
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.feature-track::-webkit-scrollbar {
  display: none;
}

.feature-card {
  flex: 0 0 310px; /* Fixed width for each card */
  background: #fff;
  border-radius: 20px;
  padding: 30px 20px;
  text-align: center;
  height: 280px;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  box-shadow: 0 10px 30px rgba(0,0,0,0.05);
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  box-sizing: border-box;
}

.feature-card:hover {
  transform: scale(1.08);
  box-shadow: 0 20px 40px rgba(0,0,0,0.15);
  z-index: 10;
}

.carousel-btn {
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  background: #fff;
  border: 1px solid #ebeef5;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  z-index: 10;
  transition: all 0.3s;
}

.carousel-btn:hover {
  color: #409eff;
  border-color: #c6e2ff;
  background-color: #ecf5ff;
}

.prev-btn {
  margin-right: -15px;
}

.next-btn {
  margin-left: -15px;
}

.card-icon {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
  font-size: 36px;
  transition: transform 0.3s ease;
}

.feature-card:hover .card-icon {
  transform: scale(1.1) rotate(10deg);
}

.treehole-card .card-icon { background: #e8f4fd; color: #409eff; }
.treasure-card .card-icon { background: #f0f9eb; color: #67c23a; }
.community-card .card-icon { background: #fdf6ec; color: #e6a23c; }

.feature-card h3 {
  font-size: 1.5rem;
  margin-bottom: 15px;
  color: #333;
}

.feature-card p {
  color: #666;
  line-height: 1.6;
  font-size: 1rem;
}

/* Background decorations */
.card-bg-shape {
  position: absolute;
  top: -50px;
  right: -50px;
  width: 150px;
  height: 150px;
  border-radius: 50%;
  opacity: 0.1;
  transition: transform 0.5s ease;
}

.treehole-card .card-bg-shape { background: #409eff; }
.treasure-card .card-bg-shape { background: #67c23a; }
.community-card .card-bg-shape { background: #e6a23c; }
.assessment-card .card-bg-shape { background: #9c27b0; }
.contact-card .card-bg-shape { background: #ff5722; }

.assessment-card .card-icon { background: #f3e5f5; color: #9c27b0; }
.contact-card .card-icon { background: #fbe9e7; color: #ff5722; }

.feature-card:hover .card-bg-shape {
  transform: scale(1.5);
}

/* Animations */
.fade-in-up {
  opacity: 0;
  animation: fadeInUp 1s ease-out forwards;
}

.delay-1 {
  animation-delay: 0.3s;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
