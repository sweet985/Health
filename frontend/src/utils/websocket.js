import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'

let ws = null
let reconnectTimer = null
const eventListeners = new Map()

export const wsManager = {
  connect() {
    const userStore = useUserStore()
    if (!userStore.token) return

    if (ws) {
      this.disconnect()
    }

    // Replace http/https with ws/wss
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    const host = window.location.hostname
    // If running dev server (5173), API usually at 8081. In prod, same port.
    const port = window.location.port === '5173' ? '8081' : window.location.port
    const wsUrl = `${protocol}//${host}:${port}/ws/${userStore.token}`

    ws = new WebSocket(wsUrl)

    ws.onopen = () => {
      console.log('WebSocket connected')
      if (reconnectTimer) {
        clearTimeout(reconnectTimer)
        reconnectTimer = null
      }
    }

    ws.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data)
        this.triggerEvent(data)
      } catch (e) {
        this.triggerEvent(event.data)
      }
    }

    ws.onclose = () => {
      console.log('WebSocket disconnected')
      // Auto reconnect
      if (userStore.token) {
        reconnectTimer = setTimeout(() => {
          this.connect()
        }, 5000)
      }
    }

    ws.onerror = (error) => {
      console.error('WebSocket error:', error)
    }
  },

  disconnect() {
    if (ws) {
      ws.close()
      ws = null
    }
  },

  // Event bus pattern
  on(event, callback) {
    if (!eventListeners.has(event)) {
      eventListeners.set(event, [])
    }
    eventListeners.get(event).push(callback)
  },

  off(event, callback) {
    if (eventListeners.has(event)) {
      const callbacks = eventListeners.get(event)
      eventListeners.set(event, callbacks.filter(cb => cb !== callback))
    }
  },

  triggerEvent(message) {
    if (typeof message === 'string') {
      const callbacks = eventListeners.get(message)
      if (callbacks) {
        callbacks.forEach(cb => cb(message))
      }
    } else {
       // if object event, etc
       const callbacks = eventListeners.get(message.type || 'DATA')
       if (callbacks) {
         callbacks.forEach(cb => cb(message))
       }
    }
  }
}
