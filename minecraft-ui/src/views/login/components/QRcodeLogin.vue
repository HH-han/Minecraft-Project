<template>
  <div class="qr-panel">
    <button class="qr-panel__close" type="button" :aria-label="t('login.qr.back')" @click="$emit('close')">
      <el-icon><Close /></el-icon>
    </button>

    <header class="qr-panel__header">
      <h2 class="qr-panel__title">{{ t('login.qr.title') }}</h2>
      <p class="qr-panel__subtitle">{{ t('login.qr.appTip') }}</p>
    </header>

    <div class="qr-stage">
      <div class="qr-box" :class="{ 'qr-box--pulse': isWaiting && !loading }">
        <img v-if="qrDataUrl && !loading" :src="qrDataUrl" class="qr-image" alt="QR Code" />
        <div v-else class="qr-placeholder"></div>

        <div v-if="loading" class="qr-mask">
          <el-icon class="qr-mask__icon is-spinning"><Loading /></el-icon>
          <p class="qr-mask__text">{{ t('login.qr.loading') }}</p>
        </div>

        <div v-else-if="maskText" class="qr-mask" :class="`qr-mask--${maskTone}`">
          <el-icon v-if="status === 'SCANNED'" class="qr-mask__icon is-success"><CircleCheckFilled /></el-icon>
          <el-icon v-else-if="status === 'CONFIRMED'" class="qr-mask__icon is-spinning"><Loading /></el-icon>
          <el-icon v-else class="qr-mask__icon"><WarningFilled /></el-icon>
          <p class="qr-mask__text">{{ maskText }}</p>
          <button v-if="canRefresh" class="qr-mask__action" type="button" @click="refresh">
            <el-icon><RefreshRight /></el-icon>
            <span>{{ t('login.qr.refresh') }}</span>
          </button>
        </div>
      </div>

      <p v-if="isWaiting" class="qr-hint">{{ t('login.qr.hint') }}</p>
      <p v-if="isWaiting" class="qr-countdown">{{ t('login.qr.countdown', { seconds: countdown }) }}</p>
    </div>

    <ol class="qr-steps">
      <li class="qr-step">
        <span class="qr-step__index">1</span>
        <span class="qr-step__text">{{ t('login.qr.step1') }}</span>
      </li>
      <li class="qr-step">
        <span class="qr-step__index">2</span>
        <span class="qr-step__text">{{ t('login.qr.step2') }}</span>
      </li>
      <li class="qr-step">
        <span class="qr-step__index">3</span>
        <span class="qr-step__text">{{ t('login.qr.step3') }}</span>
      </li>
    </ol>

    <p class="qr-security">
      <el-icon><Lock /></el-icon>
      <span>{{ t('login.qr.securityTip') }}</span>
    </p>

    <button class="qr-back" type="button" @click="$emit('close')">
      <el-icon><Back /></el-icon>
      <span>{{ t('login.qr.back') }}</span>
    </button>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { useQrLogin } from '@/hooks/useQrLogin'

const emit = defineEmits(['success', 'close'])

const { t } = useI18n()

const { status, qrDataUrl, countdown, loading, errorCode, errorMessage, start, refresh } = useQrLogin({
  onSuccess: (data) => {
    ElMessage.success(t('login.qr.success'))
    emit('success', data)
  }
})

const isWaiting = computed(() => status.value === 'WAITING')

const maskText = computed(() => {
  switch (status.value) {
    case 'SCANNED':
      return t('login.qr.scanned')
    case 'CONFIRMED':
      return t('login.qr.confirming')
    case 'EXPIRED':
      return t('login.qr.expired')
    case 'CANCELED':
      return t('login.qr.canceled')
    case 'CONSUMED':
      return t('login.qr.consumed')
    case 'ERROR':
      return errorCode.value === 4008
        ? t('login.qr.disabled')
        : errorMessage.value || t('login.qr.error')
    default:
      return ''
  }
})

const maskTone = computed(() => {
  if (status.value === 'SCANNED' || status.value === 'CONFIRMED') return 'success'
  if (status.value === 'ERROR' && errorCode.value === 4008) return 'disabled'
  return 'warn'
})

const canRefresh = computed(
  () => ['EXPIRED', 'CANCELED', 'CONSUMED', 'ERROR'].includes(status.value) && errorCode.value !== 4008
)

onMounted(start)
</script>

<style scoped>
.qr-panel {
  position: relative;
  box-sizing: border-box;
  width: min(92vw, 420px);
  padding: 28px 28px 24px;
  border-radius: 18px;
  background: var(--theme-bg-elevated);
  box-shadow: var(--theme-shadow-lg);
  color: var(--theme-text-1);
  display: flex;
  flex-direction: column;
  align-items: center;
}

.qr-panel__close {
  position: absolute;
  top: 14px;
  right: 14px;
  width: 32px;
  height: 32px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 50%;
  background: transparent;
  color: var(--theme-text-3);
  cursor: pointer;
  transition: background 0.2s ease, color 0.2s ease;
}

.qr-panel__close:hover {
  background: var(--theme-bg-surface-2);
  color: var(--theme-text-1);
}

.qr-panel__header {
  text-align: center;
  margin-bottom: 20px;
}

.qr-panel__title {
  font-size: 22px;
  font-weight: 700;
  margin: 0 0 8px;
  color: var(--theme-text-1);
}

.qr-panel__subtitle {
  margin: 0;
  font-size: 13px;
  line-height: 1.6;
  color: var(--theme-text-2);
}

.qr-stage {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
}

.qr-box {
  position: relative;
  width: 220px;
  height: 220px;
  padding: 12px;
  box-sizing: border-box;
  border-radius: 16px;
  background: #ffffff;
  border: 1px solid var(--theme-hairline);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  transition: box-shadow 0.3s ease, transform 0.3s ease;
}

.qr-box--pulse {
  animation: qr-pulse 2.6s ease-in-out infinite;
}

@keyframes qr-pulse {
  0%,
  100% {
    box-shadow: 0 0 0 0 color-mix(in srgb, var(--theme-primary) 26%, transparent);
  }
  50% {
    box-shadow: 0 0 0 10px transparent;
  }
}

.qr-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
  border-radius: 8px;
}

.qr-placeholder {
  width: 100%;
  height: 100%;
  border-radius: 8px;
  background: var(--theme-bg-surface-2);
}

.qr-mask {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 16px;
  text-align: center;
  background: color-mix(in srgb, var(--theme-bg-elevated) 90%, transparent);
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
}

.qr-mask--success {
  background: color-mix(in srgb, var(--theme-primary) 88%, #000 4%);
  color: var(--theme-primary-text);
}

.qr-mask__icon {
  font-size: 34px;
  color: var(--theme-primary);
}

.qr-mask--warn .qr-mask__icon {
  color: var(--theme-warning, #e6a23c);
}

.qr-mask--success .qr-mask__icon {
  color: var(--theme-primary-text);
}

.qr-mask__text {
  margin: 0;
  font-size: 14px;
  line-height: 1.6;
  color: inherit;
}

.qr-mask--warn .qr-mask__text {
  color: var(--theme-text-1);
}

.is-success {
  color: var(--theme-primary-text);
}

.is-spinning {
  animation: qr-spin 1s linear infinite;
}

@keyframes qr-spin {
  to {
    transform: rotate(360deg);
  }
}

.qr-mask__action {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-top: 4px;
  padding: 8px 14px;
  border: none;
  border-radius: 999px;
  background: var(--theme-primary);
  color: var(--theme-primary-text);
  font-size: 13px;
  cursor: pointer;
  transition: transform 0.2s ease, opacity 0.2s ease;
}

.qr-mask__action:hover {
  transform: translateY(-1px);
  opacity: 0.92;
}

.qr-hint {
  margin: 16px 0 4px;
  font-size: 14px;
  font-weight: 600;
  color: var(--theme-text-1);
}

.qr-countdown {
  margin: 0;
  font-size: 12px;
  color: var(--theme-text-3);
  font-variant-numeric: tabular-nums;
}

.qr-steps {
  width: 100%;
  margin: 20px 0 0;
  padding: 14px 16px;
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 10px;
  border-radius: 12px;
  background: var(--theme-bg-surface-2);
  box-sizing: border-box;
}

.qr-step {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
  color: var(--theme-text-2);
}

.qr-step__index {
  width: 20px;
  height: 20px;
  flex: 0 0 20px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: color-mix(in srgb, var(--theme-primary) 16%, transparent);
  color: var(--theme-primary);
  font-size: 12px;
  font-weight: 700;
}

.qr-security {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 16px 0 0;
  font-size: 12px;
  line-height: 1.5;
  color: var(--theme-text-3);
  text-align: center;
}

.qr-back {
  margin-top: 18px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  border: none;
  background: transparent;
  color: var(--theme-text-2);
  font-size: 13px;
  cursor: pointer;
  transition: color 0.2s ease;
}

.qr-back:hover {
  color: var(--theme-primary);
}

@media (max-width: 480px) {
  .qr-panel {
    width: calc(100vw - 32px);
    padding: 24px 18px 20px;
  }

  .qr-box {
    width: 200px;
    height: 200px;
  }

  .qr-panel__title {
    font-size: 20px;
  }

  .qr-steps {
    margin-top: 16px;
  }
}
</style>
