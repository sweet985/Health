<template>
  <div class="bubble-game">
    <div class="game-header">
      <h3>解压戳泡泡</h3>
      <p>当前得分: {{ score }}</p>
    </div>
    <div class="play-area" ref="playArea" @mousedown="createBubbleOnEvent">
      <transition-group name="pop">
        <div
          v-for="b in bubbles"
          :key="b.id"
          class="bubble-wrapper"
          :style="{ left: b.x + '%', bottom: b.y + 'px', animationDuration: b.speed + 's' }"
        >
          <div 
            class="bubble"
            :class="{ 'popping': b.isPopping }"
            :style="{ width: b.size + 'px', height: b.size + 'px' }"
            @mousedown.stop="popBubble(b)"
          ></div>
        </div>
      </transition-group>
      <div class="hint">点击空白处生成泡泡，点击泡泡戳破它</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const score = ref(0)
const bubbles = ref([])
const playArea = ref(null)
let bubbleId = 0
let autoTimer = null

const createBubble = (x = Math.random() * 90) => {
  const size = Math.random() * 40 + 30
  const speed = Math.random() * 3 + 4 // 4-7 seconds
  const id = bubbleId++
  bubbles.value.push({ id, x, y: -size, size, speed, isPopping: false })
  
  // Auto remove after animation
  setTimeout(() => {
    bubbles.value = bubbles.value.filter(b => b.id !== id)
  }, speed * 1000)
}

const createBubbleOnEvent = (e) => {
  if (!playArea.value) return
  const rect = playArea.value.getBoundingClientRect()
  const x = ((e.clientX - rect.left) / rect.width) * 100
  createBubble(x)
}

const popBubble = (bubble) => {
  if (bubble.isPopping) return
  
  bubble.isPopping = true
  score.value++
  
  // Remove element after pop animation finishes (200ms)
  setTimeout(() => {
    bubbles.value = bubbles.value.filter(b => b.id !== bubble.id)
  }, 200)
}

onMounted(() => {
  autoTimer = setInterval(() => createBubble(), 1000)
})

onUnmounted(() => {
  clearInterval(autoTimer)
})
</script>

<style scoped>
.bubble-game { height: 500px; display: flex; flex-direction: column; }
.game-header { text-align: center; margin-bottom: 15px; }
.game-header h3 { color: #2c3e50; font-size: 1.5rem; margin-bottom: 5px; }
.game-header p { color: #409eff; font-weight: bold; font-size: 1.2rem; }
.play-area { flex: 1; background: linear-gradient(180deg, #e0c3fc 0%, #8ec5fc 100%); border-radius: 12px; position: relative; overflow: hidden; cursor: crosshair; box-shadow: inset 0 0 20px rgba(0,0,0,0.1); }
.hint { position: absolute; top: 20px; width: 100%; text-align: center; color: rgba(255,255,255,0.7); pointer-events: none; font-size: 0.9rem; }
.bubble-wrapper {
  position: absolute;
  animation: floatUp linear forwards;
}
.bubble {
  border-radius: 50%;
  background: radial-gradient(circle at 30% 30%, rgba(255, 255, 255, 0.9), rgba(255, 255, 255, 0.1));
  box-shadow: inset 0 0 20px rgba(255,255,255,0.5);
  border: 1px solid rgba(255,255,255,0.4);
  cursor: pointer;
  transition: transform 0.1s;
}
@keyframes floatUp {
  0% { transform: translateY(0); opacity: 0; }
  10% { opacity: 1; }
  90% { opacity: 1; }
  100% { transform: translateY(-600px); opacity: 0; }
}

.bubble.popping {
  animation: popEffect 0.2s ease-out forwards;
  pointer-events: none;
}

@keyframes popEffect {
  0% { transform: scale(1); opacity: 1; }
  40% { transform: scale(1.4); opacity: 0.8; box-shadow: inset 0 0 50px rgba(255,255,255,0.9); }
  100% { transform: scale(2); opacity: 0; border: none; }
}

.pop-leave-active { transition: all 0.2s ease-out; }
.pop-leave-to { transform: scale(1.5) !important; opacity: 0 !important; }
</style>