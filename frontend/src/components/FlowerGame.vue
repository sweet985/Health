<template>
  <div class="flower-game" @mousemove="handleMouseMove" @mouseleave="handleMouseLeave" :class="{ 'hide-cursor': isHovering }">
    <div class="game-header">
      <h3>耐心浇花</h3>
      <p>移动水壶到花朵上方，按住鼠标给小花浇水吧</p>
    </div>
    
    <div class="garden-area">
      <!-- 浇水壶 -->
      <div 
        class="watering-can" 
        :class="{ 'is-watering': isWatering, 'is-visible': isHovering }"
        :style="{ left: `${mouseX}px`, top: `${mouseY}px` }"
      >
        🚿
        <div class="water-drops" v-if="isWatering">
          <div class="drop d1"></div>
          <div class="drop d2"></div>
          <div class="drop d3"></div>
        </div>
      </div>

      <!-- 花朵成长容器 -->
      <div 
        class="flower-container"
        @mousedown="startWatering"
        @mouseup="stopWatering"
        @mouseleave="stopWatering"
      >
        <div class="flower-hitbox"></div>
        <div 
          class="flower-emoji" 
          :style="{ 
            transform: `scale(${flowerScale}) translateY(${flowerOffsetY}px)`, 
            opacity: flowerOpacity 
          }"
        >
          {{ currentFlower }}
        </div>
        <!-- 花盆已移除 -->
      </div>

      <!-- 感谢提示条 -->
      <transition name="fade">
        <div class="thanks-msg" v-if="showThanks">
          {{ thanksMessage }}
        </div>
      </transition>
    </div>

    <!-- 进度条 -->
    <div class="progress-section">
      <el-progress 
        :percentage="growthProgress" 
        :stroke-width="15"
        status="success" 
        :show-text="false"
      />
      <div class="level-text">成长阶段: {{ growthLevel }}/5</div>
    </div>

    <div class="controls">
      <el-button 
        v-if="growthLevel >= 5"
        type="warning" 
        round 
        size="large" 
        @click="resetGame"
        plain
      >
        重新种一盆
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

// 游戏状态
const growthProgress = ref(0)
const isWatering = ref(false)
const showThanks = ref(false)
const mouseX = ref(-100)
const mouseY = ref(-100)
const isHovering = ref(false)
let wateringInterval = null

// 不同阶段的花朵形态
const flowerStages = ['🌰', '🌱', '🌿', '🌷', '🌸', '🌺']
const thanksMessages = [
  "好渴呀，谢谢你的水~",
  "感觉充满力量了！",
  "我在努力长大哦~",
  "看到花骨朵了吗？",
  "谢谢你的照顾，我开花啦！"
]

// 计算属性
const growthLevel = computed(() => {
  return Math.floor(growthProgress.value / 20)
})

const currentFlower = computed(() => {
  return flowerStages[growthLevel.value] || flowerStages[0]
})

const flowerScale = computed(() => {
  // 基础缩放 0.8，每次升级增加 0.2，最大不超过 1.8
  return 0.8 + (growthLevel.value * 0.2)
})

const flowerOpacity = computed(() => {
  return 0.7 + (growthLevel.value * 0.06)
})

const flowerOffsetY = computed(() => {
  // Y 轴偏移量：直接长在地上，不需要深埋
  // 阶段 0: 0px, 阶段 1: -10px, 阶段 2: -20px, 阶段 3: -30px, 阶段 4: -40px, 阶段 5: -50px
  return -(growthLevel.value * 10)
})

const thanksMessage = computed(() => {
  const index = Math.min(growthLevel.value - 1, thanksMessages.length - 1)
  return thanksMessages[index >= 0 ? index : 0]
})

// 动作方法
const handleMouseMove = (e) => {
  // 直接使用浏览器视口绝对坐标 (fixed定位)
  mouseX.value = e.clientX
  mouseY.value = e.clientY
  isHovering.value = true
}

const handleMouseLeave = () => {
  isHovering.value = false
  stopWatering()
}

const startWatering = () => {
  if (growthLevel.value >= 5) return
  isWatering.value = true
  showThanks.value = false
  
  // 持续浇水增加进度
  if (!wateringInterval) {
    wateringInterval = setInterval(() => {
      if (growthProgress.value < 100) {
        growthProgress.value = Math.min(100, growthProgress.value + 2)
      } else {
        stopWatering()
      }
    }, 100)
  }
}

const stopWatering = () => {
  if (isWatering.value) {
    isWatering.value = false
    if (wateringInterval) {
      clearInterval(wateringInterval)
      wateringInterval = null
    }
    
    // 显示感谢信息
    if (growthProgress.value > 0 && growthLevel.value < 5) {
      showThanks.value = true
      setTimeout(() => {
        showThanks.value = false
      }, 2000)
    } else if (growthLevel.value >= 5) {
      showThanks.value = true
    }
  }
}

const resetGame = () => {
  growthProgress.value = 0
  showThanks.value = false
  stopWatering()
}
</script>

<style scoped>
.flower-game {
  height: 500px;
  display: flex;
  flex-direction: column;
  align-items: center;
  background: linear-gradient(180deg, #e0f7fa 0%, #ffffff 100%);
  border-radius: 12px;
  padding: 20px;
  position: relative;
  overflow: hidden;
}

.hide-cursor {
  cursor: none; /* 隐藏鼠标，使用水壶代替 */
}

.game-header {
  text-align: center;
  margin-bottom: 20px;
}
.game-header h3 {
  color: #2c3e50;
  font-size: 1.5rem;
  margin-bottom: 5px;
}
.game-header p {
  color: #7f8c8d;
  font-size: 1rem;
}

.garden-area {
  flex: 1;
  width: 100%;
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  align-items: center;
  padding-bottom: 30px;
}

/* 浇水壶动画 */
.watering-can {
  position: fixed; /* 使用 fixed 定位，直接对齐鼠标坐标 */
  font-size: 60px;
  transform-origin: bottom right;
  z-index: 100;
  pointer-events: none; /* 让鼠标事件穿透水壶，作用在下方的花朵上 */
  opacity: 0;
  /* 调整初始位置，让壶嘴在光标左上方一点点 */
  transform: translate(-60px, -40px);
}
.watering-can.is-visible {
  opacity: 1;
}
.watering-can.is-watering {
  /* 浇水时倾斜水壶，壶嘴稍微下移，模拟倒水动作 */
  transform: rotate(-45deg) translate(-50px, 0px);
  transition: transform 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

/* 水滴动画 */
.water-drops {
  position: absolute;
  bottom: -30px;
  left: -20px;
  width: 40px;
  height: 60px;
}
.drop {
  position: absolute;
  width: 8px;
  height: 12px;
  background: #4facfe;
  border-radius: 50%;
  opacity: 0;
}
.d1 { left: 5px; animation: dropAnim 0.6s infinite 0.1s; }
.d2 { left: 20px; animation: dropAnim 0.6s infinite 0.3s; }
.d3 { left: 35px; animation: dropAnim 0.6s infinite 0.5s; }

@keyframes dropAnim {
  0% { transform: translateY(0); opacity: 1; }
  100% { transform: translateY(50px); opacity: 0; }
}

/* 花朵与花盆 */
.flower-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  z-index: 5;
  cursor: pointer;
}
.flower-hitbox {
  position: absolute;
  width: 150px;
  height: 250px;
  bottom: 0;
  z-index: 10;
  border-radius: 50%;
  /* background: rgba(255,0,0,0.1); for debugging */
}
.flower-emoji {
  font-size: 80px;
  transition: transform 1s cubic-bezier(0.34, 1.56, 0.64, 1), opacity 1s;
  transform-origin: bottom center;
  line-height: 1; /* 确保高度计算准确 */
}
.pot {
  font-size: 100px;
  z-index: 10; /* 花盆必须在花朵（z-index 5）上方 */
  margin-top: -15px; /* 与 wrapper 稍微重叠 */
}

/* 感谢提示条 */
.thanks-msg {
  position: absolute;
  top: 100px;
  background: rgba(255, 255, 255, 0.9);
  padding: 8px 20px;
  border-radius: 20px;
  color: #ff7675;
  font-weight: bold;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  font-size: 1.1rem;
  z-index: 20;
}
.thanks-msg::after {
  content: '';
  position: absolute;
  bottom: -8px;
  left: 50%;
  transform: translateX(-50%);
  border-width: 8px 8px 0;
  border-style: solid;
  border-color: rgba(255, 255, 255, 0.9) transparent transparent transparent;
}

.fade-enter-active,
.fade-leave-active {
  transition: all 0.3s ease;
}
.fade-enter-from {
  opacity: 0;
  transform: translateY(10px);
}
.fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* 进度与控制区 */
.progress-section {
  width: 80%;
  margin: 20px 0;
  text-align: center;
}
.level-text {
  margin-top: 8px;
  color: #606266;
  font-size: 0.9rem;
}

.controls {
  display: flex;
  gap: 15px;
  margin-bottom: 10px;
}
</style>