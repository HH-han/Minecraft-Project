<template>
  <Teleport to="body">
    <div
      v-if="visible"
      class="captcha-modal"
      role="dialog"
      aria-modal="true"
      :aria-label="t('login.captcha.title')"
      @click.self="handleClose"
    >
      <div ref="cardEl" class="captcha-card" :class="{ 'captcha-card--shake': shaking }" @animationend="handleCardAnimationEnd">
        <!-- 左侧插画区（移动端隐藏） -->
        <aside class="captcha-aside">
          <img
            v-for="(image, index) in asideImages"
            :key="image"
            class="aside-illustration"
            :class="{ 'aside-illustration--active': index === activeImageIndex }"
            :src="image"
            alt=""
          />
          <span class="aside-scrim"></span>
          <div class="aside-overlay">
            <span class="aside-brand">Minecraft 旅行</span>
            <span class="aside-slogan">{{ t('login.captcha.brandSlogan') }}</span>
          </div>
        </aside>

        <!-- 右侧验证区 -->
        <section class="captcha-main">
          <header class="captcha-header">
            <div class="captcha-title">
              <span class="title-icon"><Lock /></span>
              <div class="title-text">
                <h3>{{ t('login.captcha.title') }}</h3>
                <p>{{ t('login.captcha.subtitle') }}</p>
              </div>
            </div>
            <button class="modal-close" type="button" :aria-label="t('common.cancel')" @click="handleClose">
              <Close />
            </button>
          </header>

          <div class="captcha-body">
            <!-- 加载中 -->
            <div v-if="loadState === 'loading'" class="captcha-status">
              <div class="status-spinner"></div>
              <p>{{ t('login.captcha.loading') }}</p>
            </div>

            <!-- 加载失败 -->
            <div v-else-if="loadState === 'error'" class="captcha-status error">
              <span class="status-icon"><WarningFilled /></span>
              <p>{{ t('login.captcha.error') }}</p>
              <button class="retry-btn" type="button" @click="initCaptcha">
                <Refresh />
                <span>{{ t('login.captcha.retry') }}</span>
              </button>
            </div>

            <!-- TAC 验证码挂载点 -->
            <div
              v-show="loadState === 'ready'"
              ref="captchaBox"
              class="captcha-container"
              :style="captchaHintStyle"
            ></div>
          </div>

          <footer v-if="loadState === 'ready'" class="captcha-footer">
            <p class="footer-hint">
              <InfoFilled />
              <span>{{ t('login.captcha.hint') }}</span>
            </p>
            <button class="ghost-btn" type="button" @click="refreshCaptcha">
              <Refresh />
              <span>{{ t('login.captcha.refresh') }}</span>
            </button>
          </footer>
        </section>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { ref, computed, watch, nextTick, onUnmounted } from 'vue';
import { useI18n } from 'vue-i18n';
import { Lock, Close, Refresh, WarningFilled, InfoFilled } from '@element-plus/icons-vue';
import asideImage1 from '@/assets/defaultimage/1.png';
import asideImage2 from '@/assets/defaultimage/2.png';
import asideImage3 from '@/assets/defaultimage/3.png';

// 左侧插画轮播
const asideImages = [asideImage1, asideImage2, asideImage3];
const activeImageIndex = ref(0);
const CAROUSEL_INTERVAL = 4000;
let carouselTimer = null;

const stopCarousel = () => {
  clearInterval(carouselTimer);
  carouselTimer = null;
};

const startCarousel = () => {
  stopCarousel();
  carouselTimer = setInterval(() => {
    activeImageIndex.value = (activeImageIndex.value + 1) % asideImages.length;
  }, CAROUSEL_INTERVAL);
};

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  mode: {
    type: String,
    default: 'image'
  }
});

const emit = defineEmits(['close', 'success', 'fail']);

const { t } = useI18n();

// 拖动提示文案通过 CSS 变量注入 TAC 轨道（TAC 模板内无法直接渲染文本）
const captchaHintStyle = computed(() => ({
  '--captcha-drag-hint': `'${t('login.captcha.dragHint')}'`
}));

const captchaBox = ref(null);
const cardEl = ref(null);
const loadState = ref('loading'); // loading | ready | error
const shaking = ref(false);
let tacInstance = null;
let shakeTimer = null;

const triggerShake = async () => {
  shaking.value = false;
  clearTimeout(shakeTimer);
  // 等待一次渲染并强制重排，保证连续失败时动画可以重新触发
  await nextTick();
  if (!cardEl.value) return;
  void cardEl.value.offsetWidth;
  shaking.value = true;
  shakeTimer = setTimeout(() => {
    shaking.value = false;
  }, 520);
};

const initCaptcha = async () => {
  if (tacInstance) return;

  loadState.value = 'loading';
  try {
    await loadTACScript();
  } catch {
    loadState.value = 'error';
    return;
  }

  await nextTick();
  if (!captchaBox.value) return;

  const captchaConfig = {
    requestCaptchaDataUrl: '/api/captcha/gen',
    validCaptchaUrl: '/api/captcha/check',
    bindEl: captchaBox.value,
    style: {
      // 传 null：TAC loadStyle 会将 logo 置为 display:none
      logoUrl: null,
      i18n: {
        slider_title: t('login.captcha.sliderTitle'),
        slider_title_size: '13px',
        tips_success: t('login.captcha.success'),
        tips_error: t('login.captcha.fail')
      }
    },
    validSuccess: (res, c, tInstance) => {
      emit('success');
      tInstance.destroyWindow();
      tacInstance = null;
    },
    validFail: (res, c, tInstance) => {
      emit('fail');
      triggerShake();
      tInstance.reloadCaptcha();
    },
    btnRefreshFun: (el, tac) => {
      tac.reloadCaptcha();
    },
    btnCloseFun: (el, tac) => {
      tac.destroyWindow();
      tacInstance = null;
      emit('close');
    }
  };

  tacInstance = new TAC(captchaConfig);
  tacInstance.init();
  loadState.value = 'ready';
};

const loadTACScript = () => {
  return new Promise((resolve, reject) => {
    if (window.TAC) {
      resolve();
      return;
    }

    const script = document.createElement('script');
    script.src = '/tac/js/tac.min.js';
    script.onload = resolve;
    script.onerror = () => reject(new Error('TAC script load failed'));
    document.head.appendChild(script);
  });
};

const refreshCaptcha = () => {
  tacInstance?.reloadCaptcha();
};

const destroyCaptcha = () => {
  if (tacInstance) {
    tacInstance.destroyWindow();
    tacInstance = null;
  }
};

const handleClose = () => {
  destroyCaptcha();
  emit('close');
};

const handleCardAnimationEnd = () => {
  shaking.value = false;
};

watch(
  () => props.visible,
  (newVal) => {
    if (newVal) {
      activeImageIndex.value = 0;
      startCarousel();
      nextTick(() => {
        initCaptcha();
      });
    } else {
      stopCarousel();
      destroyCaptcha();
    }
  },
  { immediate: true }
);

onUnmounted(() => {
  stopCarousel();
  clearTimeout(shakeTimer);
  destroyCaptcha();
});
</script>

<style scoped>
/* 遮罩：叠加主色光晕，避免纯灰蒙层 */
.captcha-modal {
  position: fixed;
  inset: 0;
  z-index: 2000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background:
    radial-gradient(
      720px 420px at 50% -10%,
      color-mix(in srgb, var(--theme-primary) 30%, transparent),
      transparent 70%
    ),
    var(--theme-overlay);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  animation: modal-fade 0.25s ease-out;
}

@keyframes modal-fade {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

/* 验证卡片：左插画 + 右验证 */
.captcha-card {
  display: grid;
  grid-template-columns: 232px auto;
  max-width: 100%;
  background: var(--theme-bg-elevated);
  border: 1px solid var(--theme-hairline);
  border-radius: var(--theme-radius-xl);
  overflow: hidden;
  box-shadow: var(--theme-shadow-lg);
  animation: card-pop 0.28s cubic-bezier(0.34, 1.4, 0.64, 1);
}

@keyframes card-pop {
  from {
    opacity: 0;
    transform: translateY(16px) scale(0.96);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* 拖动失败抖动反馈 */
.captcha-card--shake {
  animation: card-shake 0.45s ease;
}

@keyframes card-shake {
  0%,
  100% {
    transform: translateX(0);
  }
  20% {
    transform: translateX(-7px);
  }
  40% {
    transform: translateX(6px);
  }
  60% {
    transform: translateX(-4px);
  }
  80% {
    transform: translateX(3px);
  }
}

/* 插画区 */
.captcha-aside {
  position: relative;
  overflow: hidden;
  background: var(--theme-bg-surface-2);
}

/* 轮播图叠层淡入淡出 */
.aside-illustration {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  opacity: 0;
  transform: scale(1.06);
  transition: opacity 0.9s ease, transform 6s ease;
}

.aside-illustration--active {
  opacity: 1;
  transform: scale(1.03);
}

@media (prefers-reduced-motion: reduce) {
  .aside-illustration {
    transition: opacity 0.3s ease;
  }
}

.aside-scrim {
  position: absolute;
  inset: 0;
  background:
    linear-gradient(180deg, rgba(2, 6, 23, 0) 40%, rgba(2, 6, 23, 0.62) 74%, rgba(2, 6, 23, 0.86) 100%),
    linear-gradient(90deg, rgba(2, 6, 23, 0.14), transparent 45%);
}

.aside-overlay {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 18px 18px 20px;
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.aside-brand {
  color: #ffffff;
  font-size: 16px;
  font-weight: 800;
  letter-spacing: 0.5px;
  text-shadow: 0 2px 14px rgba(2, 6, 23, 0.45);
}

.aside-slogan {
  color: rgba(255, 255, 255, 0.82);
  font-size: 12px;
  letter-spacing: 0.3px;
}

/* 验证主区 */
.captcha-main {
  display: flex;
  flex-direction: column;
  padding: 22px 26px 20px;
  background:
    radial-gradient(
      320px 180px at 100% 0%,
      color-mix(in srgb, var(--theme-primary) 10%, transparent),
      transparent 72%
    ),
    var(--theme-bg-elevated);
}

.captcha-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}

.captcha-title {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.title-icon {
  flex: 0 0 auto;
  width: 42px;
  height: 42px;
  display: grid;
  place-items: center;
  border-radius: 13px;
  color: var(--theme-primary);
  background: var(--theme-primary-soft);
  box-shadow: inset 0 0 0 1px color-mix(in srgb, var(--theme-primary) 20%, transparent);
}

.title-icon svg {
  width: 21px;
  height: 21px;
}

.title-text h3 {
  margin: 0;
  font-size: 17px;
  font-weight: 700;
  line-height: 1.3;
  letter-spacing: 0.2px;
  color: var(--theme-text-1);
}

.title-text p {
  margin: 4px 0 0;
  font-size: 12px;
  line-height: 1.45;
  color: var(--theme-text-3);
}

.modal-close {
  flex: 0 0 auto;
  width: 32px;
  height: 32px;
  display: grid;
  place-items: center;
  padding: 0;
  border: none;
  border-radius: 50%;
  background: var(--theme-bg-surface-2);
  color: var(--theme-text-3);
  cursor: pointer;
  transition: background-color var(--theme-duration-fast) ease, color var(--theme-duration-fast) ease,
    transform var(--theme-duration-fast) ease;
}

.modal-close svg {
  width: 14px;
  height: 14px;
}

.modal-close:hover {
  background: var(--theme-bg-hover);
  color: var(--theme-text-1);
  transform: rotate(90deg);
}

.captcha-body {
  display: flex;
  justify-content: center;
}

/* TAC 挂载容器：覆盖 TAC 默认皮肤 */
.captcha-container {
  width: 318px;
  max-width: 100%;
}

.captcha-container :deep(#tianai-captcha-parent) {
  width: 318px;
  height: auto;
  padding: 8px;
  border: 1px solid var(--theme-hairline);
  border-radius: 16px;
  background: var(--theme-bg-surface-2);
  box-shadow: none;
  overflow: hidden;
}

/* 隐藏 TAC 默认的白色底图占位层，让容器主题底色生效 */
.captcha-container :deep(#tianai-captcha-parent #tianai-captcha-bg-img) {
  background-color: transparent;
}

/* 隐藏 TAC 默认的绿色/红色圆形按钮，改用卡片内的主题化操作 */
.captcha-container :deep(#tianai-captcha-parent .slider-bottom) {
  display: none;
}

.captcha-container :deep(#tianai-captcha-parent #tianai-captcha-box) {
  height: 260px;
  border-radius: 10px;
}

.captcha-container :deep(#tianai-captcha-parent #tianai-captcha-box .loading) {
  background: linear-gradient(var(--theme-primary) 0 0) 0 / 0% no-repeat var(--theme-bg-surface);
  border-radius: 999px;
}

.captcha-container :deep(#tianai-captcha .slider-tip) {
  margin: 0 2px 8px;
  line-height: 1.4;
  font-weight: 600;
  color: var(--theme-text-1);
}

.captcha-container :deep(#tianai-captcha .slider-tip span) {
  font-size: 13px !important;
}

.captcha-container :deep(#tianai-captcha .content) {
  border-radius: 10px;
  overflow: hidden;
  background: var(--theme-bg-surface);
}

.captcha-container :deep(#tianai-captcha-slider-bg-img) {
  border-radius: 10px;
}

/* ---------- 滑块轨道 ---------- */
.captcha-container :deep(#tianai-captcha .slider-move) {
  height: 42px;
  line-height: 42px;
}

.captcha-container :deep(#tianai-captcha .slider-move-track) {
  position: relative;
  /* TAC 为 content-box，减去左右描边避免总宽超出 300px 画布 */
  width: calc(100% - 2px);
  height: 30px;
  line-height: 40px;
  border: 1px solid var(--theme-border);
  border-radius: 13px;
  background: var(--theme-bg-surface);
  box-shadow: inset 0 1px 3px color-mix(in srgb, var(--theme-overlay) 8%, transparent);
  color: var(--theme-text-3);
  font-size: 12px;
  overflow: hidden;
  transition: border-color var(--theme-duration-fast) ease, box-shadow var(--theme-duration-fast) ease;
}

.captcha-container :deep(#tianai-captcha .slider-move-track:hover) {
  border-color: color-mix(in srgb, var(--theme-primary) 45%, var(--theme-border));
  box-shadow:
    inset 0 1px 3px color-mix(in srgb, var(--theme-overlay) 8%, transparent),
    0 0 0 3px color-mix(in srgb, var(--theme-primary) 10%, transparent);
}

/* 居中拖动提示：文案由 i18n 通过 CSS 变量注入，跟随语言切换 */
.captcha-container :deep(#tianai-captcha .slider-move-track::after) {
  content: var(--captcha-drag-hint, '');
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  letter-spacing: 1px;
  color: var(--theme-text-3);
  pointer-events: none;
  user-select: none;
}

/* 拖动进度：主题渐变填充 */
.captcha-container :deep(#tianai-captcha-parent #tianai-captcha-slider-move-track-mask) {
  top: 0;
  left: 0;
  height: 40px;
  border: none;
  border-radius: 12px;
  background-color: transparent !important;
  background-image: linear-gradient(
    90deg,
    color-mix(in srgb, var(--theme-primary) 24%, transparent),
    color-mix(in srgb, var(--theme-accent-2) 30%, transparent)
  ) !important;
  opacity: 1 !important;
}

/* ---------- 拖拽滑块 ---------- */
.captcha-container :deep(#tianai-captcha-parent #tianai-captcha .slider-move-btn) {
  top: -2px;
  /* 58px：与 TAC 内部 242px 的最大拖动距离相加正好等于轨道宽度 300px */
  width: 58px;
  height: 36px;
  border-radius: 13px;
  /* 覆盖 TAC 通过行内样式写入的默认按钮图 */
  background-color: transparent !important;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='%23ffffff' stroke-width='2.2' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpath d='M7 6l6 6-6 6'/%3E%3Cpath d='M13 6l5 6-5 6'/%3E%3C/svg%3E"),
    var(--theme-gradient) !important;
  background-size: 18px 18px, 100% 100% !important;
  background-position: center, center !important;
  background-repeat: no-repeat !important;
  box-shadow:
    0 10px 20px -10px color-mix(in srgb, var(--theme-primary) 75%, transparent),
    inset 0 1px 0 rgba(255, 255, 255, 0.35) !important;
  transition: filter var(--theme-duration-fast) ease, box-shadow var(--theme-duration-fast) ease;
}

.captcha-container :deep(#tianai-captcha-parent #tianai-captcha .slider-move-btn:hover) {
  filter: brightness(1.06);
}

.captcha-container :deep(#tianai-captcha-parent #tianai-captcha .slider-move-btn:active) {
  filter: brightness(0.97);
  box-shadow:
    0 6px 12px -8px color-mix(in srgb, var(--theme-primary) 80%, transparent),
    inset 0 1px 0 rgba(255, 255, 255, 0.28) !important;
}

/* TAC 默认的移动光带会在轨道中间留下一道竖线，直接隐藏 */
.captcha-container :deep(#tianai-captcha .slider-move-shadow) {
  display: none;
}

.captcha-container :deep(#tianai-captcha .tianai-captcha-tips) {
  /* 保留 TAC 的上下滑入逻辑，仅调整字体与圆角 */
  font-size: 12px;
  border-radius: 0 0 10px 10px;
}

.captcha-container :deep(#tianai-captcha .tianai-captcha-tips.tianai-captcha-tips-error) {
  background-color: var(--theme-danger);
}

.captcha-container :deep(#tianai-captcha .tianai-captcha-tips.tianai-captcha-tips-success) {
  background-color: var(--theme-success);
}

/* 加载 / 错误状态 */
.captcha-status {
  width: 318px;
  max-width: 100%;
  height: 278px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 14px;
  padding: 24px;
  text-align: center;
  border: 1px dashed var(--theme-border);
  border-radius: 16px;
  background: var(--theme-bg-surface-2);
}

.captcha-status p {
  margin: 0;
  font-size: 13px;
  line-height: 1.5;
  color: var(--theme-text-3);
}

.captcha-status .status-icon {
  display: grid;
  place-items: center;
  width: 46px;
  height: 46px;
  border-radius: 14px;
  color: var(--theme-warning);
  background: var(--theme-warning-soft);
}

.captcha-status.error .status-icon {
  color: var(--theme-danger);
  background: var(--theme-danger-soft);
}

.captcha-status .status-icon svg {
  width: 24px;
  height: 24px;
}

.status-spinner {
  width: 34px;
  height: 34px;
  border: 3px solid var(--theme-border);
  border-top-color: var(--theme-primary);
  border-radius: 50%;
  animation: spin 0.9s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.retry-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 9px 20px;
  border: none;
  border-radius: var(--theme-radius-pill);
  background: var(--theme-gradient);
  color: var(--theme-primary-text);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 8px 18px -10px color-mix(in srgb, var(--theme-primary) 85%, transparent);
  transition: transform var(--theme-duration-fast) ease, box-shadow var(--theme-duration-fast) ease;
}

.retry-btn svg {
  width: 14px;
  height: 14px;
}

.retry-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 10px 22px -10px color-mix(in srgb, var(--theme-primary) 90%, transparent);
}

/* 底部操作栏 */
.captcha-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  width: 100%;
  margin-top: 14px;
  padding-top: 12px;
  border-top: 1px dashed var(--theme-hairline);
}

.footer-hint {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 0;
  min-width: 0;
  font-size: 11px;
  line-height: 1.4;
  color: var(--theme-text-3);
}

.footer-hint svg {
  flex: 0 0 auto;
  width: 13px;
  height: 13px;
}

.ghost-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  flex: 0 0 auto;
  padding: 6px 12px;
  border: 1px solid var(--theme-border);
  border-radius: var(--theme-radius-pill);
  background: transparent;
  color: var(--theme-text-2);
  font-size: 12px;
  cursor: pointer;
  transition: color var(--theme-duration-fast) ease, border-color var(--theme-duration-fast) ease,
    background-color var(--theme-duration-fast) ease;
}

.ghost-btn svg {
  width: 13px;
  height: 13px;
}

.ghost-btn:hover {
  color: var(--theme-primary);
  border-color: var(--theme-primary);
  background: var(--theme-primary-soft);
}

/* 移动端：隐藏插画，卡片收窄 */
@media (max-width: 720px) {
  .captcha-modal {
    padding: 16px;
  }

  .captcha-card {
    grid-template-columns: minmax(0, 1fr);
  }

  .captcha-aside {
    display: none;
  }

  .captcha-main {
    padding: 18px 16px 16px;
  }

  .captcha-footer {
    flex-direction: column;
    align-items: stretch;
    gap: 8px;
  }

  .ghost-btn {
    justify-content: center;
  }
}

@media (max-width: 400px) {
  .captcha-modal {
    padding: 8px;
  }

  .captcha-main {
    padding: 14px 10px;
  }
}
</style>
