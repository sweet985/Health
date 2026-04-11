<template>
  <div class="breathe-game">
    <div class="header">
      <h3>正念呼吸训练</h3>
      <p>跟随圆环的节奏，深呼吸，放松身心</p>
    </div>
    <div class="breathe-area">
      <div class="breathe-circle" :class="phase">
        {{ instruction }}
      </div>
    </div>
    <div class="controls">
      <el-button @click="toggle" type="primary" round size="large" :color="isRunning ? '#f56c6c' : '#42b983'">
        {{ isRunning ? '停止训练' : '开始训练' }}
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onUnmounted } from 'vue'

const isRunning = ref(false)
const phase = ref('')
const instruction = ref('准备')
let timer1, timer2, timer3

const startCycle = () => {
  if (!isRunning.value) return
  
  phase.value = 'inhale'
  instruction.value = '吸气...'
  
  timer1 = setTimeout(() => {
    if (!isRunning.value) return
    phase.value = 'hold'
    instruction.value = '屏息...'
    
    timer2 = setTimeout(() => {
      if (!isRunning.value) return
      phase.value = 'exhale'
      instruction.value = '呼气...'
      
      timer3 = setTimeout(() => {
        if (!isRunning.value) return
        startCycle()
      }, 4000)
    }, 2000)
  }, 4000)
}

const toggle = () => {
  if (isRunning.value) {
    isRunning.value = false
    clearTimeout(timer1)
    clearTimeout(timer2)
    clearTimeout(timer3)
    phase.value = ''
    instruction.value = '准备'
  } else {
    isRunning.value = true
    startCycle()
  }
}

onUnmounted(() => {
  isRunning.value = false
  clearTimeout(timer1)
  clearTimeout(timer2)
  clearTimeout(timer3)
})
</script>

<style scoped>
.breathe-game { height: 450px; display: flex; flex-direction: column; align-items: center; }
.header { text-align: center; margin-bottom: 40px; }
.header h3 { color: #2c3e50; margin-bottom: 10px; font-size: 1.5rem; }
.header p { color: #7f8c8d; }
.breathe-area { flex: 1; display: flex; justify-content: center; align-items: center; width: 100%; position: relative; }
.breathe-circle {
  width: 150px;
  height: 150px;
  border-radius: 50%;
  background: linear-gradient(135deg, #84fab0 0%, #8fd3f4 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #fff;
  font-weight: bold;
  box-shadow: 0 0 30px rgba(132, 250, 176, 0.5);
  transition: transform 4s cubic-bezier(0.4, 0, 0.2, 1), background 2s;
}
.breathe-circle.inhale {
  transform: scale(1.8);
  background: linear-gradient(135deg, #a1c4fd 0%, #c2e9fb 100%);
  box-shadow: 0 0 50px rgba(161, 196, 253, 0.6);
}
.breathe-circle.hold {
  transform: scale(1.8);
  transition: transform 2s linear;
}
.breathe-circle.exhale {
  transform: scale(1);
  background: linear-gradient(135deg, #84fab0 0%, #8fd3f4 100%);
  box-shadow: 0 0 20px rgba(132, 250, 176, 0.4);
}
.controls { margin-top: 30px; margin-bottom: 20px; }
</style>