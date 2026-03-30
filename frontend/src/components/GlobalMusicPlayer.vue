<template>
  <div 
    class="global-music-player" 
    v-if="musicStore.isVisible"
    :class="{ 'is-expanded': isExpanded }"
    @mouseenter="isExpanded = true"
    @mouseleave="isExpanded = false"
  >
    <div class="player-icon" :class="{ 'is-playing': musicStore.isPlaying }">
      <el-icon class="spin-icon"><Headset /></el-icon>
    </div>
    
    <div class="player-content" v-show="isExpanded">
      <div class="music-info">
        <div class="music-title" :title="musicStore.currentMusic?.title">{{ musicStore.currentMusic?.title }}</div>
      </div>
      <div class="player-controls">
        <el-button circle size="small" type="primary" @click="togglePlay">
          <el-icon><VideoPause v-if="musicStore.isPlaying" /><VideoPlay v-else /></el-icon>
        </el-button>
        <el-button circle size="small" type="info" plain @click="closePlayer">
          <el-icon><Close /></el-icon>
        </el-button>
      </div>
    </div>
    
    <!-- Hidden audio element -->
    <audio 
      ref="audioRef" 
      :src="musicStore.currentMusic?.content" 
      @ended="onEnded"
      @play="onPlay"
      @pause="onPause"
      loop
    ></audio>
  </div>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue'
import { useMusicStore } from '../stores/music'
import { Headset, VideoPlay, VideoPause, Close } from '@element-plus/icons-vue'

const musicStore = useMusicStore()
const audioRef = ref(null)
const isExpanded = ref(false)

// Handle play state changes from store
watch(() => musicStore.isPlaying, async (newVal) => {
  await nextTick()
  if (!audioRef.value) return
  
  if (newVal) {
    audioRef.value.play().catch(e => console.error('Audio play error:', e))
  } else {
    audioRef.value.pause()
  }
})

// Handle track changes from store
watch(() => musicStore.currentMusic, async (newVal) => {
  await nextTick()
  if (newVal && audioRef.value && musicStore.isPlaying) {
    audioRef.value.play().catch(e => console.error('Audio play error:', e))
  }
})

const togglePlay = () => {
  if (musicStore.isPlaying) {
    musicStore.pause()
  } else {
    musicStore.play(musicStore.currentMusic)
  }
}

const closePlayer = () => {
  musicStore.close()
  isExpanded.value = false
}

const onEnded = () => {
  // If not looping, we could pause or play next. Currently setting to loop.
}

const onPlay = () => {
  musicStore.isPlaying = true
}

const onPause = () => {
  musicStore.isPlaying = false
}
</script>

<style scoped>
.global-music-player {
  position: fixed;
  top: 80px; /* Below the top header */
  left: 0;
  height: 50px;
  width: 50px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-left: none;
  border-radius: 0 25px 25px 0;
  box-shadow: 2px 4px 15px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  padding: 5px;
  z-index: 2000;
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
  overflow: hidden;
  box-sizing: border-box;
}

.global-music-player.is-expanded {
  width: 260px;
  padding-right: 15px;
  background: rgba(255, 255, 255, 0.95);
}

.player-icon {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #a1c4fd 0%, #c2e9fb 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
  margin-left: 0;
  box-shadow: 0 2px 8px rgba(161, 196, 253, 0.5);
  transition: background 0.3s;
}

.player-icon.is-playing {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.5);
}

.spin-icon {
  font-size: 20px;
  animation: spin 4s linear infinite;
  animation-play-state: paused;
}

.is-playing .spin-icon {
  animation-play-state: running;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.player-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex: 1;
  margin-left: 12px;
  opacity: 0;
  animation: fadeIn 0.3s forwards;
  animation-delay: 0.15s;
  min-width: 0; /* Ensures child text-overflow works */
}

@keyframes fadeIn {
  to { opacity: 1; }
}

.music-info {
  flex: 1;
  overflow: hidden;
  margin-right: 10px;
}

.music-title {
  font-size: 14px;
  color: #333;
  font-weight: 600;
  white-space: nowrap;
  text-overflow: ellipsis;
  overflow: hidden;
}

.player-controls {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}
</style>
