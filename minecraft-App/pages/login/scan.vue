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
      <text class="scan-hero__title">{{ t('scan.title') }}</text>
      <text class="scan-hero__desc">{{ t('scan.desc') }}</text>
    </view>

    <view class="scan-actions">
      <button class="scan-btn" :loading="scanning" :disabled="scanning || loading" @click="startScan">
        {{ scanning ? t('scan.opening') : t('scan.start') }}
      </button>
      <text class="scan-tip">{{ t('scan.tip') }}</text>
    </view>

    <view class="scan-steps">
      <view class="scan-step">
        <text class="scan-step__index">1</text>
        <text class="scan-step__text">{{ t('scan.step1') }}</text>
      </view>
      <view class="scan-step">
        <text class="scan-step__index">2</text>
        <text class="scan-step__text">{{ t('scan.step2') }}</text>
      </view>
      <view class="scan-step">
        <text class="scan-step__index">3</text>
        <text class="scan-step__text">{{ t('scan.step3') }}</text>
      </view>
    </view>

    <!-- 确认登录弹窗 -->
    <view v-if="popupVisible" class="device-mask">
      <view class="device-card" @click.stop>
        <text class="device-title">{{ t('scan.confirmTitle') }}</text>
        <text class="device-subtitle">{{ t('scan.confirmDesc') }}</text>

        <view class="device-list">
          <view class="device-row">
            <text class="device-label">{{ t('scan.browser') }}</text>
            <text class="device-value">{{ detail.browser || t('scan.unknownBrowser') }}</text>
          </view>
          <view class="device-row">
            <text class="device-label">{{ t('scan.os') }}</text>
            <text class="device-value">{{ detail.os || t('scan.unknownOs') }}</text>
          </view>
          <view class="device-row">
            <text class="device-label">{{ t('scan.ip') }}</text>
            <text class="device-value">{{ detail.ip || t('scan.unknown') }}</text>
          </view>
          <view class="device-row">
            <text class="device-label">{{ t('scan.region') }}</text>
            <text class="device-value">{{ detail.region || t('scan.unknown') }}</text>
          </view>
        </view>

        <text class="device-warning">{{ t('scan.warning') }}</text>

        <view class="device-actions">
          <button class="device-btn device-btn--ghost" :disabled="submitting" @click="onCancel">{{ t('scan.cancel') }}</button>
          <button class="device-btn device-btn--primary" :loading="submitting" @click="onConfirm">{{ t('scan.confirm') }}</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { cancelQr, confirmQr, scanQr } from '../../api/auth'
import { t, usePageTitle } from '@/utils/i18n'

usePageTitle('page.scan')

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

    showToast(response?.message || t('scan.expired'))
  } catch (error) {
    showToast(t('scan.networkError'))
  } finally {
    loading.value = false
  }
}

const handleRawResult = (raw) => {
  const value = extractTicket(raw)
  if (!value) {
    showToast(t('scan.noQr'))
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
  showToast(t('scan.useInApp'))
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
        showToast(t('scan.failed'))
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
      uni.showToast({ title: t('scan.success'), icon: 'success' })
      setTimeout(() => {
        uni.navigateBack()
      }, 1200)
      return
    }
    showToast(response?.message || t('scan.loginFailed'))
  } catch (error) {
    showToast(t('scan.networkError'))
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
  uni.showToast({ title: t('scan.canceled'), icon: 'none' })
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
  background: var(--bg-color);
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
  color: var(--primary-color);
  background: var(--card-background);
  box-shadow: var(--shadow-sm);
}

.scan-hero__title {
  margin-top: 28rpx;
  font-size: 40rpx;
  font-weight: 700;
  color: var(--text-color);
}

.scan-hero__desc {
  margin-top: 14rpx;
  max-width: 560rpx;
  font-size: 26rpx;
  line-height: 1.6;
  color: var(--text-secondary-color);
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
  background: var(--primary-color);
  box-shadow: var(--shadow-brand);
  border: none;
}

.scan-btn[disabled] {
  opacity: 0.7;
}

.scan-tip {
  margin-top: 20rpx;
  font-size: 24rpx;
  color: var(--text-tertiary-color);
}

.scan-steps {
  width: 100%;
  margin-top: 48rpx;
  padding: 32rpx;
  box-sizing: border-box;
  border-radius: 28rpx;
  background: var(--card-background);
  box-shadow: var(--shadow-sm);
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
  color: var(--primary-color);
  background: var(--primary-soft);
}

.scan-step__text {
  margin-left: 18rpx;
  font-size: 26rpx;
  color: var(--text-secondary-color);
}

.device-mask {
  position: fixed;
  inset: 0;
  z-index: 999;
  background: rgba(15, 21, 18, 0.45);
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
  background: var(--card-background);
  box-shadow: var(--shadow-lg);
}

.device-title {
  display: block;
  font-size: 34rpx;
  font-weight: 700;
  color: var(--text-color);
}

.device-subtitle {
  display: block;
  margin-top: 12rpx;
  font-size: 24rpx;
  line-height: 1.5;
  color: var(--text-secondary-color);
}

.device-list {
  margin-top: 28rpx;
  padding: 24rpx;
  border-radius: 20rpx;
  background: var(--surface-2);
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
  color: var(--text-tertiary-color);
}

.device-value {
  font-size: 26rpx;
  color: var(--text-color);
  max-width: 400rpx;
  text-align: right;
  word-break: break-all;
}

.device-warning {
  display: block;
  margin-top: 24rpx;
  font-size: 24rpx;
  color: var(--gold-color);
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
  color: var(--text-secondary-color);
  background: var(--background-soft);
}

.device-btn--primary {
  color: #ffffff;
  background: var(--primary-color);
}
</style>
