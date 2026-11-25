<template>
  <view class="scan-page">
    <view class="scan-hero">
      <view class="scan-hero__icon">
        <svg viewBox="0 0 1024 1024" width="72" height="72" aria-hidden="true">
          <path
            d="M117.368 487.445h371.92v-371.92h-371.92v371.92z m92.981-278.94h185.96v185.96h-185.96v-185.96z m325.43-92.98v371.92h371.92v-371.92H535.78z m278.94 278.94h-185.96v-185.96h185.96v185.96z m-557.88-46.49h92.98v-92.98h-92.98v92.98z m-139.47 557.88h371.92v-371.92h-371.92v371.92z m92.98-278.94h185.96v185.96h-185.96v-185.96z m557.88-371.92h-92.98v92.98h92.98v-92.98z m-232.45 650.86h92.98v-92.98h-92.98v92.98z m92.98-278.94v185.96h92.98v-185.96h-92.98z m185.96 185.96h-92.98v92.98h185.96v-185.96h-92.98v92.98z m0-185.96h92.98v-92.98h-92.98v92.98z m-278.94-92.98v92.98h92.98v-92.98h-92.98z m-278.94 232.45h92.98v-92.98h-92.98v92.98z"
            fill="currentColor"></path>
        </svg>
      </view>
      <text class="scan-hero__title">扫一扫登录</text>
      <text class="scan-hero__desc">扫描电脑端登录页的二维码，确认后电脑端自动登录</text>
    </view>

    <view class="scan-actions">
      <button class="scan-btn" :loading="scanning" :disabled="scanning || loading" @click="startScan">
        {{ scanning ? '正在打开相机…' : '开始扫描' }}
      </button>
      <text class="scan-tip">请确保当前 App 已登录本人账号</text>
    </view>

    <view class="scan-steps">
      <view class="scan-step">
        <text class="scan-step__index">1</text>
        <text class="scan-step__text">在电脑端登录页打开扫码登录</text>
      </view>
      <view class="scan-step">
        <text class="scan-step__index">2</text>
        <text class="scan-step__text">用本页扫一扫对准二维码</text>
      </view>
      <view class="scan-step">
        <text class="scan-step__index">3</text>
        <text class="scan-step__text">核对设备信息并确认登录</text>
      </view>
    </view>

    <!-- 确认登录弹窗 -->
    <view v-if="popupVisible" class="device-mask">
      <view class="device-card" @click.stop>
        <text class="device-title">确认登录电脑端？</text>
        <text class="device-subtitle">请核对以下设备信息，确认是本人操作</text>

        <view class="device-list">
          <view class="device-row">
            <text class="device-label">浏览器</text>
            <text class="device-value">{{ detail.browser || '未知浏览器' }}</text>
          </view>
          <view class="device-row">
            <text class="device-label">操作系统</text>
            <text class="device-value">{{ detail.os || '未知系统' }}</text>
          </view>
          <view class="device-row">
            <text class="device-label">IP 地址</text>
            <text class="device-value">{{ detail.ip || '未知' }}</text>
          </view>
          <view class="device-row">
            <text class="device-label">登录地点</text>
            <text class="device-value">{{ detail.region || '未知地点' }}</text>
          </view>
        </view>

        <text class="device-warning">若非本人操作，请点击取消</text>

        <view class="device-actions">
          <button class="device-btn device-btn--ghost" :disabled="submitting" @click="onCancel">取消</button>
          <button class="device-btn device-btn--primary" :loading="submitting" @click="onConfirm">确认登录</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { cancelQr, confirmQr, scanQr } from '../../api/auth'

const ticket = ref('')
const scanning = ref(false)
const loading = ref(false)
const submitting = ref(false)
const popupVisible = ref(false)
const detail = reactive({
  browser: '',
  os: '',
  ip: '',
  region: ''
})

const showToast = (title) => {
  uni.showToast({ title, icon: 'none' })
}

const extractTicket = (raw) => {
  const text = String(raw || '').trim()
  const matched = text.match(/[?&]t=([A-Za-z0-9]{32,64})/)
  if (matched) {
    return matched[1]
  }
  if (/^[A-Za-z0-9]{32,64}$/.test(text)) {
    return text
  }
  return ''
}

const goLogin = (value) => {
  const redirect = encodeURIComponent(`/pages/login/scan?t=${value}`)
  uni.navigateTo({ url: `/pages/login/login?redirect=${redirect}` })
}

const getDeviceInfo = () => {
  try {
    return uni.getSystemInfoSync() || {}
  } catch (error) {
    return {}
  }
}

const loadDetail = async (value) => {
  loading.value = true
  try {
    const systemInfo = getDeviceInfo()
    const response = await scanQr({
      ticket: value,
      appVersion: systemInfo.appVersion || '1.0.0',
      deviceModel: systemInfo.deviceModel || systemInfo.model || systemInfo.platform || 'unknown'
    })

    if (response && response.code === 200) {
      const webClient = response.data?.webClient || {}
      detail.browser = webClient.browser || ''
      detail.os = webClient.os || ''
      detail.ip = webClient.ip || ''
      detail.region = webClient.region || ''
      popupVisible.value = true
      return
    }

    if (response && response.code === 401) {
      goLogin(value)
      return
    }

    showToast(response?.message || '二维码已失效，请刷新后重试')
  } catch (error) {
    showToast('网络异常，请稍后重试')
  } finally {
    loading.value = false
  }
}

const handleRawResult = (raw) => {
  const value = extractTicket(raw)
  if (!value) {
    showToast('未识别到登录二维码')
    return
  }
  ticket.value = value

  if (!uni.getStorageSync('token')) {
    goLogin(value)
    return
  }
  loadDetail(value)
}

const startScan = () => {
  if (scanning.value || loading.value) {
    return
  }

  // #ifdef H5
  showToast('请在 App 中使用扫一扫')
  return
  // #endif

  scanning.value = true
  uni.scanCode({
    scanType: ['qrCode'],
    success: (result) => {
      handleRawResult(result.result)
    },
    fail: (error) => {
      const message = error?.errMsg || ''
      if (!message.includes('cancel')) {
        showToast('扫码失败，请重试')
      }
    },
    complete: () => {
      scanning.value = false
    }
  })
}

const onConfirm = async () => {
  if (submitting.value) {
    return
  }
  submitting.value = true
  try {
    const response = await confirmQr({ ticket: ticket.value })
    if (response && response.code === 200) {
      popupVisible.value = false
      uni.showToast({ title: '登录成功', icon: 'success' })
      setTimeout(() => {
        uni.navigateBack()
      }, 1200)
      return
    }
    showToast(response?.message || '登录失败，请重试')
  } catch (error) {
    showToast('网络异常，请稍后重试')
  } finally {
    submitting.value = false
  }
}

const onCancel = async () => {
  try {
    await cancelQr({ ticket: ticket.value })
  } catch (error) {
    // 取消失败不影响关闭弹窗
  }
  popupVisible.value = false
  uni.showToast({ title: '已取消登录', icon: 'none' })
}

onLoad((options) => {
  const value = options?.t ? String(options.t) : ''
  if (value) {
    handleRawResult(value)
    return
  }
  startScan()
})
</script>

<style scoped>
.scan-page {
  min-height: 100vh;
  box-sizing: border-box;
  padding: 48rpx 40rpx calc(48rpx + env(safe-area-inset-bottom, 0px));
  background: linear-gradient(180deg, #eef6ff 0%, #f8faff 40%, #f8faff 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
}

.scan-hero {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  margin-top: 24rpx;
}

.scan-hero__icon {
  width: 140rpx;
  height: 140rpx;
  border-radius: 36rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #3a80d2;
  background: #ffffff;
  box-shadow: 0 16rpx 40rpx rgba(58, 128, 210, 0.16);
}

.scan-hero__title {
  margin-top: 28rpx;
  font-size: 40rpx;
  font-weight: 700;
  color: #1f2d3d;
}

.scan-hero__desc {
  margin-top: 14rpx;
  max-width: 560rpx;
  font-size: 26rpx;
  line-height: 1.6;
  color: #7a8798;
}

.scan-actions {
  width: 100%;
  margin-top: 56rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.scan-btn {
  width: 100%;
  height: 96rpx;
  line-height: 96rpx;
  border-radius: 48rpx;
  font-size: 32rpx;
  font-weight: 600;
  color: #ffffff;
  background: linear-gradient(135deg, #4a90e2, #2f6fc4);
  box-shadow: 0 16rpx 32rpx rgba(58, 128, 210, 0.28);
  border: none;
}

.scan-btn[disabled] {
  opacity: 0.7;
}

.scan-tip {
  margin-top: 20rpx;
  font-size: 24rpx;
  color: #98a4b3;
}

.scan-steps {
  width: 100%;
  margin-top: 48rpx;
  padding: 32rpx;
  box-sizing: border-box;
  border-radius: 28rpx;
  background: #ffffff;
  box-shadow: 0 12rpx 32rpx rgba(31, 45, 61, 0.06);
}

.scan-step {
  display: flex;
  align-items: center;
}

.scan-step + .scan-step {
  margin-top: 24rpx;
}

.scan-step__index {
  width: 40rpx;
  height: 40rpx;
  line-height: 40rpx;
  flex: 0 0 40rpx;
  border-radius: 50%;
  text-align: center;
  font-size: 22rpx;
  font-weight: 700;
  color: #3a80d2;
  background: #e8f2ff;
}

.scan-step__text {
  margin-left: 18rpx;
  font-size: 26rpx;
  color: #4a5568;
}

.device-mask {
  position: fixed;
  inset: 0;
  z-index: 999;
  background: rgba(17, 24, 39, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48rpx;
}

.device-card {
  width: 100%;
  max-width: 620rpx;
  box-sizing: border-box;
  padding: 40rpx 36rpx 32rpx;
  border-radius: 28rpx;
  background: #ffffff;
  box-shadow: 0 24rpx 60rpx rgba(17, 24, 39, 0.2);
}

.device-title {
  display: block;
  font-size: 34rpx;
  font-weight: 700;
  color: #1f2d3d;
}

.device-subtitle {
  display: block;
  margin-top: 12rpx;
  font-size: 24rpx;
  line-height: 1.5;
  color: #7a8798;
}

.device-list {
  margin-top: 28rpx;
  padding: 24rpx;
  border-radius: 20rpx;
  background: #f5f9ff;
}

.device-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.device-row + .device-row {
  margin-top: 18rpx;
}

.device-label {
  font-size: 24rpx;
  color: #8896a6;
}

.device-value {
  font-size: 26rpx;
  color: #1f2d3d;
  max-width: 400rpx;
  text-align: right;
  word-break: break-all;
}

.device-warning {
  display: block;
  margin-top: 24rpx;
  font-size: 24rpx;
  color: #e6a23c;
}

.device-actions {
  margin-top: 32rpx;
  display: flex;
  gap: 24rpx;
}

.device-btn {
  flex: 1;
  height: 84rpx;
  line-height: 84rpx;
  border-radius: 42rpx;
  font-size: 28rpx;
  font-weight: 600;
  border: none;
}

.device-btn--ghost {
  color: #4a5568;
  background: #eef2f7;
}

.device-btn--primary {
  color: #ffffff;
  background: linear-gradient(135deg, #4a90e2, #2f6fc4);
}
</style>
