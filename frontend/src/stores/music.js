import { defineStore } from 'pinia'

export const useMusicStore = defineStore('music', {
  state: () => ({
    currentMusic: null,
    isPlaying: false,
    isVisible: false
  }),
  actions: {
    play(music) {
      if (this.currentMusic && this.currentMusic.id === music.id) {
        this.isPlaying = true
      } else {
        this.currentMusic = music
        this.isPlaying = true
      }
      this.isVisible = true
    },
    pause() {
      this.isPlaying = false
    },
    togglePlay(music) {
      if (this.currentMusic && this.currentMusic.id === music.id) {
        this.isPlaying = !this.isPlaying
      } else {
        this.play(music)
      }
    },
    close() {
      this.isVisible = false
      this.isPlaying = false
      this.currentMusic = null
    }
  }
})
