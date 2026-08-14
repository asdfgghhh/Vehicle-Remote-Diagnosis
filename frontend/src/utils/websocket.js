/**
 * WebSocket 连接管理器
 * 用于实时接收车辆信号推送
 */
class WebSocketManager {
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
   * @param {string} vin - 可选，订阅指定 VIN
   */
  connect(vin = null) {
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    const host = window.location.hostname || 'localhost'
    const port = '9086' // service-access 端口

    const url = vin
      ? `${protocol}//${host}:${port}/ws/signal/${vin}`
      : `${protocol}//${host}:${port}/ws/signal`

    console.log('[WebSocket] Connecting to:', url)
    this.ws = new WebSocket(url)

    this.ws.onopen = () => {
      console.log('[WebSocket] Connected')
      this.reconnectCount = 0
      this.emit('connected', { timestamp: Date.now() })
    }

    this.ws.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data)
        this.emit(data.type || 'message', data)
      } catch (e) {
        console.warn('[WebSocket] Failed to parse message:', event.data)
      }
    }

    this.ws.onclose = (event) => {
      console.log('[WebSocket] Disconnected:', event.code, event.reason)
      this.emit('disconnected', { code: event.code })
      this.tryReconnect(vin)
    }

    this.ws.onerror = (error) => {
      console.error('[WebSocket] Error:', error)
      this.emit('error', error)
    }
  }

  /**
   * 发送消息
   */
  send(data) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.ws.send(typeof data === 'string' ? data : JSON.stringify(data))
    }
  }

  /**
   * 订阅指定 VIN
   */
  subscribeVin(vin) {
    this.send({ action: 'subscribe', vin })
  }

  /**
   * 取消订阅，切换到全局广播
   */
  unsubscribe() {
    this.send({ action: 'unsubscribe' })
  }

  /**
   * 注册事件监听
   */
  on(event, callback) {
    if (!this.listeners.has(event)) {
      this.listeners.set(event, [])
    }
    this.listeners.get(event).push(callback)
  }

  /**
   * 移除事件监听
   */
  off(event, callback) {
    const callbacks = this.listeners.get(event)
    if (callbacks) {
      const idx = callbacks.indexOf(callback)
      if (idx > -1) callbacks.splice(idx, 1)
    }
  }

  /**
   * 触发事件
   */
  emit(event, data) {
    const callbacks = this.listeners.get(event)
    if (callbacks) {
      callbacks.forEach(cb => cb(data))
    }
  }

  /**
   * 断开连接
   */
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

  /**
   * 尝试重连
   */
  tryReconnect(vin) {
    if (this.reconnectCount >= this.maxReconnect) {
      console.log('[WebSocket] Max reconnect attempts reached')
      return
    }
    this.reconnectCount++
    console.log(`[WebSocket] Reconnecting in ${this.reconnectInterval / 1000}s (attempt ${this.reconnectCount}/${this.maxReconnect})`)
    this.reconnectTimer = setTimeout(() => {
      this.connect(vin)
    }, this.reconnectInterval)
  }
}

export default new WebSocketManager()
