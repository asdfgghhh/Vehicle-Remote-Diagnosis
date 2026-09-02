/**
 * 车辆在线状态 WebSocket 连接管理器
 * 连接 service-vehicle（端口 9082）的 /ws/online 端点
 * 用于实时接收车辆在线状态变更通知
 */
class OnlineStatusWebSocketManager {
  constructor() {
    this.ws = null
    this.listeners = new Map()
    this.reconnectTimer = null
    this.reconnectInterval = 5000
    this.maxReconnect = 10
    this.reconnectCount = 0
  }

  /**
   * 连接 WebSocket
   * @param {string} vin - 可选，订阅指定 VIN（车辆详情页用）；不传则全局广播
   */
  connect(vin = null) {
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    const host = window.location.hostname || 'localhost'
    const port = '9082' // service-vehicle 端口

    const url = vin
      ? `${protocol}//${host}:${port}/ws/online/${vin}`
      : `${protocol}//${host}:${port}/ws/online`

    console.log('[OnlineStatusWS] Connecting to:', url)
    this.ws = new WebSocket(url)

    this.ws.onopen = () => {
      console.log('[OnlineStatusWS] Connected')
      this.reconnectCount = 0
      this.emit('connected', { timestamp: Date.now() })
    }

    this.ws.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data)
        if (data.type === 'onlineStatus') {
          this.emit('onlineStatus', data)
        }
      } catch (e) {
        console.warn('[OnlineStatusWS] Failed to parse message:', event.data)
      }
    }

    this.ws.onclose = (event) => {
      console.log('[OnlineStatusWS] Disconnected:', event.code, event.reason)
      this.emit('disconnected', { code: event.code })
      this.tryReconnect(vin)
    }

    this.ws.onerror = (error) => {
      console.error('[OnlineStatusWS] Error:', error)
      this.emit('error', error)
    }
  }

  send(data) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.ws.send(typeof data === 'string' ? data : JSON.stringify(data))
    }
  }

  on(event, callback) {
    if (!this.listeners.has(event)) {
      this.listeners.set(event, [])
    }
    this.listeners.get(event).push(callback)
  }

  off(event, callback) {
    const callbacks = this.listeners.get(event)
    if (callbacks) {
      const idx = callbacks.indexOf(callback)
      if (idx > -1) callbacks.splice(idx, 1)
    }
  }

  emit(event, data) {
    const callbacks = this.listeners.get(event)
    if (callbacks) {
      callbacks.forEach(cb => cb(data))
    }
  }

  disconnect() {
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }
    if (this.ws) {
      this.ws.close()
      this.ws = null
    }
  }

  tryReconnect(vin) {
    if (this.reconnectCount >= this.maxReconnect) {
      console.log('[OnlineStatusWS] Max reconnect attempts reached')
      return
    }
    this.reconnectCount++
    console.log(`[OnlineStatusWS] Reconnecting in ${this.reconnectInterval / 1000}s (attempt ${this.reconnectCount}/${this.maxReconnect})`)
    this.reconnectTimer = setTimeout(() => {
      this.connect(vin)
    }, this.reconnectInterval)
  }
}

export default new OnlineStatusWebSocketManager()
