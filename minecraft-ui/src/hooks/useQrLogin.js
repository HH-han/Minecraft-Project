import { computed, onUnmounted, ref } from 'vue'
import QRCode from 'qrcode'
import { wsBaseUrl } from '@/config'
import { consumeQr, generateQr, getQrStatus } from '@/api/qrLogin'
import { useAuthStore } from '@/stores/auth'
import { usePermissionStore } from '@/stores/permissionStore'

const FINAL_STATUSES = ['EXPIRED', 'CANCELED', 'CONSUMED']

/**
 * 构建带业务码的错误对象（后端业务错误以 HTTP 200 + code 返回）
 */
const toBusinessError = (response, fallback) => {
  const error = new Error(response?.message || fallback)
  error.code = response?.code
  return error
}

/**
 * APP 扫码授权 Web 登录组合式函数。
 *
 * 流程：生成 ticket → 渲染二维码 → WebSocket 订阅（轮询兜底）→
 * APP 确认 → Web 主动 consume → 写入登录态与权限。
 */
export function useQrLogin({ onSuccess } = {}) {
  const status = ref('WAITING')
  const qrDataUrl = ref('')
  const countdown = ref(0)
  const loading = ref(false)
  const errorCode = ref(null)
  const errorMessage = ref('')

  let ticket = ''
  let expireAt = 0
  let socket = null
  let tickTimer = null
  let pollTimer = null
  let reconnectTimer = null
  let consuming = false
  let stopped = true

  const isFinal = computed(() => FINAL_STATUSES.includes(status.value))

  const clearTimers = () => {
    if (tickTimer) {
      clearInterval(tickTimer)
      tickTimer = null
    }
    if (pollTimer) {
      clearInterval(pollTimer)
      pollTimer = null
    }
    if (reconnectTimer) {
      clearTimeout(reconnectTimer)
      reconnectTimer = null
    }
  }

  const closeSocket = () => {
    if (!socket) return
    const current = socket
    socket = null
    try {
      current.onopen = null
      current.onmessage = null
      current.onerror = null
      current.onclose = null
      current.close()
    } catch {
      // 忽略关闭异常
    }
  }

  const stopNetwork = () => {
    stopped = true
    clearTimers()
    closeSocket()
  }

  const stop = () => {
    stopNetwork()
    ticket = ''
    qrDataUrl.value = ''
    countdown.value = 0
    loading.value = false
    consuming = false
  }

  const updateCountdown = () => {
    if (!expireAt) return
    const remain = Math.max(0, Math.ceil((expireAt - Date.now()) / 1000))
    countdown.value = remain
    if (remain === 0 && !FINAL_STATUSES.includes(status.value) && status.value !== 'CONFIRMED') {
      status.value = 'EXPIRED'
      stopNetwork()
    }
  }

  const applyStatus = (next) => {
    if (!next) return
    status.value = next
    if (next === 'CONFIRMED') {
      consume()
      return
    }
    if (FINAL_STATUSES.includes(next)) {
      stopNetwork()
    }
  }

  const consume = async () => {
    if (consuming || !ticket) return
    consuming = true
    try {
      const response = await consumeQr(ticket)
      if (response?.code !== 200 || !response.data?.token) {
        throw toBusinessError(response, '登录失败，请刷新二维码重试')
      }

      const data = response.data
      const userInfo = data.userInfo || {}
      const normalized = {
        ...userInfo,
        token: data.token,
        id: userInfo.userId,
        username: userInfo.username || userInfo.account
      }

      useAuthStore().setUserInfo(normalized)
      usePermissionStore().$patch({
        permissions: data.permissions || [],
        roles: data.roles || [],
        menus: data.menus || [],
        superAdmin: !!data.superAdmin,
        dataScope: data.dataScope ?? 4,
        loaded: true
      })

      localStorage.setItem('tokenTimestamp', String(Date.now()))
      if (normalized.username) {
        localStorage.setItem('rememberedUsername', normalized.username)
      }

      status.value = 'CONFIRMED'
      stopNetwork()
      onSuccess?.(data)
    } catch (error) {
      errorCode.value = error.code || null
      errorMessage.value = error.message || '登录失败，请刷新二维码重试'
      status.value = 'ERROR'
      stopNetwork()
    } finally {
      consuming = false
    }
  }

  const connectSocket = () => {
    if (stopped || !ticket) return
    const base = wsBaseUrl || `${window.location.protocol === 'https:' ? 'wss' : 'ws'}://${window.location.host}`
    const url = `${String(base).replace(/^http/, 'ws')}/ws/qr/${ticket}`

    try {
      socket = new WebSocket(url)
    } catch {
      socket = null
      return
    }

    socket.onmessage = (event) => {
      try {
        const message = JSON.parse(event.data)
        if (message?.type !== 'qr_status' || message.ticket !== ticket) return
        applyStatus(message.status)
      } catch {
        // 忽略无法解析的消息
      }
    }

    socket.onerror = () => {
      try {
        socket?.close()
      } catch {
        // 忽略
      }
    }

    socket.onclose = () => {
      socket = null
      if (stopped || isFinal.value || status.value === 'CONFIRMED' || status.value === 'ERROR') return
      reconnectTimer = setTimeout(() => {
        reconnectTimer = null
        connectSocket()
      }, 2000)
    }
  }

  const startPolling = () => {
    pollTimer = setInterval(async () => {
      if (stopped || consuming || !ticket) return
      if (isFinal.value || status.value === 'CONFIRMED' || status.value === 'ERROR') return
      try {
        const response = await getQrStatus(ticket)
        if (response?.code === 200 && response.data?.status) {
          applyStatus(response.data.status)
        }
      } catch {
        // 网络抖动时等待下一次轮询
      }
    }, 2000)
  }

  const start = async () => {
    stop()
    stopped = false
    loading.value = true
    errorCode.value = null
    errorMessage.value = ''
    status.value = 'WAITING'

    try {
      const response = await generateQr()
      if (response?.code !== 200 || !response.data?.ticket) {
        throw toBusinessError(response, '二维码生成失败，请重试')
      }

      const data = response.data
      ticket = data.ticket
      expireAt = data.expireAt || Date.now() + (data.ttl || 120) * 1000

      qrDataUrl.value = await QRCode.toDataURL(data.qrContent, {
        width: 260,
        margin: 1,
        errorCorrectionLevel: 'M',
        color: { dark: '#111827', light: '#ffffff' }
      })

      updateCountdown()
      tickTimer = setInterval(updateCountdown, 1000)
      connectSocket()
      startPolling()
    } catch (error) {
      errorCode.value = error.code || null
      errorMessage.value = error.message || '二维码生成失败，请重试'
      status.value = 'ERROR'
      stopNetwork()
    } finally {
      loading.value = false
    }
  }

  onUnmounted(stop)

  return {
    status,
    qrDataUrl,
    countdown,
    loading,
    errorCode,
    errorMessage,
    isFinal,
    start,
    refresh: start,
    stop
  }
}
