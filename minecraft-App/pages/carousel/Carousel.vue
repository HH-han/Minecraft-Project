<template>
  <div ref="carouselRef" class="carousel" @mouseenter="pauseAutoPlay" @mouseleave="startAutoPlay">
    <div class="carousel-viewport">
      <div class="carousel-track" :style="trackStyle" @touchstart="handleTouchStart" @touchmove="handleTouchMove" @touchend="handleTouchEnd">
        <!-- 克隆最后一个项目放在前面 -->
        <div class="carousel-slide" v-if="items.length > 1">
          <div class="slide-content">
            <img :src="items[items.length - 1].imageUrl" class="slide-image" alt="" />
            <div class="slide-overlay"></div>
            <div class="slide-caption" v-if="items[items.length - 1].title">
              <h3>{{ items[items.length - 1].title }}</h3>
              <p v-if="items[items.length - 1].subtitle">{{ items[items.length - 1].subtitle }}</p>
            </div>
          </div>
        </div>

        <!-- 正常项目 -->
        <div class="carousel-slide" v-for="(item, index) in items" :key="index">
          <div class="slide-content">
            <img :src="item.imageUrl" class="slide-image" alt="" />
            <div class="slide-overlay"></div>
            <div class="slide-caption" v-if="item.title">
              <h3>{{ item.title }}</h3>
              <p v-if="item.subtitle">{{ item.subtitle }}</p>
            </div>
          </div>
        </div>

        <!-- 克隆第一个项目放在后面 -->
        <div class="carousel-slide" v-if="items.length > 1">
          <div class="slide-content">
            <img :src="items[0].imageUrl" class="slide-image" alt="" />
            <div class="slide-overlay"></div>
            <div class="slide-caption" v-if="items[0].title">
              <h3>{{ items[0].title }}</h3>
              <p v-if="items[0].subtitle">{{ items[0].subtitle }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 导航指示器 -->
    <div class="carousel-pagination" v-if="showIndicators && items.length > 1">
      <button v-for="(item, index) in items" :key="index" :class="{ 'active': currentIndex === index }" @click="goTo(index)" :aria-label="`Go to slide ${index + 1}`">
        <span class="progress-bar" v-if="currentIndex === index"></span>
      </button>
    </div>

    <!-- 幻灯片计数 -->
    <div class="carousel-counter" v-if="items.length > 1">
      <span class="carousel-counter__current">{{ currentLabel }}</span>
      <span class="carousel-counter__slash">/</span>
      <span>{{ totalLabel }}</span>
    </div>

    <!-- 导航箭头 -->
    <button class="carousel-nav prev" @click="prev" v-if="showArrows && items.length > 1" aria-label="Previous slide">
      <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
        <path d="M15 18L9 12L15 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
      </svg>
    </button>
    <button class="carousel-nav next" @click="next" v-if="showArrows && items.length > 1" aria-label="Next slide">
      <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
        <path d="M9 6L15 12L9 18" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
      </svg>
    </button>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  items: {
    type: Array,
    default: () => [],
    validator: value => value.every(item => 'imageUrl' in item),
  },
  interval: {
    type: Number,
    default: 3000
  },
  transitionDuration: {
    type: Number,
    default: 600
  },
  showArrows: {
    type: Boolean,
    default: true
  },
  showIndicators: {
    type: Boolean,
    default: true
  },
  aspectRatio: {
    type: String,
    default: '16/9'
  }
})

const currentIndex = ref(0)
const carouselRef = ref(null)
const autoPlayTimer = ref(null)
const isTransitioning = ref(false)
const touchStartX = ref(0)
const touchEndX = ref(0)

// 计算轨道偏移量
const trackStyle = computed(() => {
  const offset = props.items.length > 1 ? -(currentIndex.value + 1) * 100 : -currentIndex.value * 100
  return {
    transform: `translateX(${offset}%)`,
    transition: isTransitioning.value ? `transform ${props.transitionDuration}ms cubic-bezier(0.16, 1, 0.3, 1)` : 'none'
  }
})

// 幻灯片计数：01 / 05
const currentLabel = computed(() => String(currentIndex.value + 1).padStart(2, '0'))
const totalLabel = computed(() => String(props.items.length).padStart(2, '0'))

// 自动播放控制
const startAutoPlay = () => {
  if (props.items.length <= 1) return
  stopAutoPlay()
  autoPlayTimer.value = setInterval(() => {
    next()
  }, props.interval)

  // 进度条动画
  startProgressBar()
}

const pauseAutoPlay = () => {
  stopAutoPlay()
  clearProgressBar()
}

const stopAutoPlay = () => {
  if (autoPlayTimer.value) {
    clearInterval(autoPlayTimer.value)
    autoPlayTimer.value = null
  }
}

// 进度条动画
let progressRaf = null

const startProgressBar = () => {
  clearProgressBar()
  nextTick(() => {
    if (progressRaf) cancelAnimationFrame(progressRaf)
    progressRaf = requestAnimationFrame(() => {
      progressRaf = null
      const root = carouselRef.value || document
      const bar = root.querySelector('.progress-bar')
      if (!bar) return
      // 强制回流，确保新挂载的进度条能触发宽度过渡
      bar.style.transition = 'none'
      bar.style.width = '0%'
      void bar.offsetWidth
      bar.style.transition = `width ${props.interval}ms linear`
      bar.style.width = '100%'
    })
  })
}

const clearProgressBar = () => {
  if (progressRaf) {
    cancelAnimationFrame(progressRaf)
    progressRaf = null
  }
  const root = carouselRef.value || document
  const progressBars = root.querySelectorAll('.progress-bar')
  progressBars.forEach(bar => {
    bar.style.transition = 'none'
    bar.style.width = '0%'
  })
}

// 自动播放切到下一张时，重新计时当前指示器进度
watch(currentIndex, () => {
  if (autoPlayTimer.value) startProgressBar()
})

// 导航方法
const goTo = (index) => {
  if (isTransitioning.value || index === currentIndex.value) return

  isTransitioning.value = true
  currentIndex.value = index

  setTimeout(() => {
    isTransitioning.value = false
    startAutoPlay()
  }, props.transitionDuration)
}

const next = () => {
  if (isTransitioning.value) return

  isTransitioning.value = true
  currentIndex.value = (currentIndex.value + 1) % props.items.length

  setTimeout(() => {
    isTransitioning.value = false

    // 如果到达克隆的第一个项目（实际是最后一个），无动画跳转到真实位置
    if (currentIndex.value === props.items.length - 1) {
      setTimeout(() => {
        isTransitioning.value = false
        currentIndex.value = 0
      }, 50)
    }
  }, props.transitionDuration)
}

const prev = () => {
  if (isTransitioning.value) return

  isTransitioning.value = true
  currentIndex.value = (currentIndex.value - 1 + props.items.length) % props.items.length

  setTimeout(() => {
    isTransitioning.value = false

    // 如果到达克隆的最后一个项目（实际是第一个），无动画跳转到真实位置
    if (currentIndex.value === 0) {
      setTimeout(() => {
        isTransitioning.value = false
        currentIndex.value = props.items.length - 1
      }, 50)
    }
  }, props.transitionDuration)
}

// 触摸事件处理
const handleTouchStart = (e) => {
  touchStartX.value = e.touches[0].clientX
  pauseAutoPlay()
}

const handleTouchMove = (e) => {
  touchEndX.value = e.touches[0].clientX
}

const handleTouchEnd = () => {
  const diff = touchStartX.value - touchEndX.value
  if (Math.abs(diff) > 50) { // 滑动阈值
    if (diff > 0) {
      next()
    } else {
      prev()
    }
  }
  // 手指离开后恢复自动播放与进度计时
  startAutoPlay()
}

// 生命周期
onMounted(() => {
  startAutoPlay()
})

onUnmounted(() => {
  stopAutoPlay()
  clearProgressBar()
})
</script>

<style scoped>
.carousel {
  --carousel-aspect-ratio: v-bind('aspectRatio');
  --carousel-ink: #ffffff;
  --carousel-radius: var(--radius-lg, 20px);

  position: relative;
  width: 100%;
  max-height: 100vh;
  aspect-ratio: var(--carousel-aspect-ratio);
  overflow: hidden;
  border-radius: var(--carousel-radius);
  background: var(--surface-2, #f4efe6);
  isolation: isolate;
  touch-action: pan-y;
  transition: box-shadow 0.4s var(--ease-out, ease);
}

.carousel:hover {
  box-shadow: var(--shadow-lg, 0 20px 44px rgba(38, 45, 36, 0.12));
}

.carousel-viewport {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.carousel-track {
  display: flex;
  height: 100%;
  width: 100%;
  will-change: transform;
}

.carousel-slide {
  flex: 0 0 100%;
  min-width: 100%;
  position: relative;
  overflow: hidden;
}

.slide-content {
  position: relative;
  width: 100%;
  height: 100%;
}

.slide-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  user-select: none;
  -webkit-user-drag: none;
  transition: transform 0.9s var(--ease-out, ease);
}

.carousel:hover .slide-image {
  transform: scale(1.035);
}

.slide-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg,
      rgba(15, 21, 18, 0.34) 0%,
      rgba(15, 21, 18, 0.06) 30%,
      rgba(15, 21, 18, 0.1) 55%,
      rgba(15, 21, 18, 0.78) 100%);
  pointer-events: none;
}

.slide-caption {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 2;
  max-width: min(88%, 760px);
  padding: clamp(20px, 4%, 40px) clamp(20px, 5%, 48px) clamp(52px, 8%, 72px);
  color: var(--carousel-ink);
  text-align: left;
}

.slide-caption h3 {
  margin: 0 0 10px;
  font-size: clamp(1.25rem, 3vw, 2.4rem);
  font-weight: 800;
  line-height: 1.15;
  letter-spacing: -0.02em;
  text-shadow: 0 2px 12px rgba(15, 21, 18, 0.45);
}

.slide-caption h3::before {
  content: '';
  display: block;
  width: 24px;
  height: 3px;
  margin-bottom: 12px;
  border-radius: 2px;
  background: var(--accent-color, #c4613c);
  box-shadow: 0 1px 4px rgba(15, 21, 18, 0.35);
}

.slide-caption p {
  margin: 0;
  max-width: 46ch;
  font-size: clamp(0.875rem, 1.5vw, 1.0625rem);
  line-height: 1.6;
  color: rgba(255, 255, 255, 0.86);
  text-shadow: 0 1px 6px rgba(15, 21, 18, 0.5);
}

/* 右上角幻灯片计数 */
.carousel-counter {
  position: absolute;
  top: 14px;
  right: 14px;
  z-index: 10;
  display: inline-flex;
  align-items: baseline;
  gap: 4px;
  padding: 5px 11px;
  border-radius: var(--radius-pill, 999px);
  border: 1px solid rgba(255, 255, 255, 0.18);
  background: rgba(15, 21, 18, 0.34);
  -webkit-backdrop-filter: blur(10px);
  backdrop-filter: blur(10px);
  color: rgba(255, 255, 255, 0.7);
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.06em;
  font-variant-numeric: tabular-nums;
  pointer-events: none;
}

.carousel-counter__current {
  color: #ffffff;
  font-weight: 700;
}

.carousel-counter__slash {
  opacity: 0.5;
}

.carousel-nav {
  position: absolute;
  top: 50%;
  z-index: 10;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  transform: translateY(-50%);
  border: 1px solid rgba(255, 255, 255, 0.26);
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.16);
  -webkit-backdrop-filter: blur(10px);
  backdrop-filter: blur(10px);
  color: #ffffff;
  cursor: pointer;
  opacity: 0;
  visibility: hidden;
  transition: opacity 0.3s var(--ease-out, ease), background-color 0.3s var(--ease-out, ease),
    border-color 0.3s var(--ease-out, ease), transform 0.3s var(--ease-out, ease);
}

.carousel:hover .carousel-nav,
.carousel-nav:focus-visible {
  opacity: 1;
  visibility: visible;
}

.carousel-nav:hover {
  background: var(--primary-color, #1e5b48);
  border-color: var(--primary-color, #1e5b48);
  transform: translateY(-50%) scale(1.06);
}

.carousel-nav:focus-visible {
  outline: 2px solid #ffffff;
  outline-offset: 2px;
}

.carousel-nav svg {
  width: 20px;
  height: 20px;
}

.prev {
  left: 14px;
}

.next {
  right: 14px;
}

.carousel-pagination {
  position: absolute;
  left: clamp(20px, 5%, 48px);
  bottom: clamp(16px, 2.6%, 22px);
  z-index: 10;
  display: flex;
  align-items: center;
  gap: 10px;
}

.carousel-pagination button {
  position: relative;
  width: 8px;
  height: 8px;
  padding: 0;
  border: none;
  border-radius: var(--radius-pill, 999px);
  background: rgba(255, 255, 255, 0.45);
  box-shadow: 0 1px 4px rgba(15, 21, 18, 0.3);
  cursor: pointer;
  transition: width 0.35s var(--ease-out, ease), background-color 0.3s var(--ease-out, ease);
}

/* 扩大触控热区 */
.carousel-pagination button::before {
  content: '';
  position: absolute;
  inset: -5px;
  border-radius: 999px;
}

.carousel-pagination button:hover {
  background: rgba(255, 255, 255, 0.85);
}

.carousel-pagination button:focus-visible {
  outline: 2px solid #ffffff;
  outline-offset: 4px;
}

.carousel-pagination button.active {
  width: 26px;
  background: rgba(255, 255, 255, 0.9);
}

.carousel-pagination button.active .progress-bar {
  position: absolute;
  top: 0;
  left: 0;
  width: 0;
  height: 100%;
  border-radius: var(--radius-pill, 999px);
  background: var(--primary-color, #1e5b48);
}

/* 触摸设备：隐藏箭头，保留滑动与指示器 */
@media (hover: none) {
  .carousel-nav {
    display: none;
  }

  .carousel:hover .slide-image {
    transform: none;
  }

  .carousel-pagination button:hover {
    background: rgba(255, 255, 255, 0.45);
  }
}

@media (max-width: 768px) {
  .carousel {
    --carousel-radius: var(--radius-md, 16px);
  }

  .carousel-counter {
    top: 10px;
    right: 10px;
    padding: 4px 9px;
    font-size: 10px;
  }

  .slide-caption {
    max-width: 100%;
    padding: 12px 16px 40px;
  }

  .slide-caption h3 {
    margin-bottom: 4px;
    font-size: clamp(1.05rem, 4.8vw, 1.5rem);
  }

  .slide-caption h3::before {
    width: 18px;
    margin-bottom: 7px;
  }

  .slide-caption p {
    max-width: 100%;
    font-size: 12px;
    line-height: 1.5;
  }

  .carousel-pagination {
    left: 16px;
    bottom: 14px;
  }

  .carousel-pagination button.active {
    width: 22px;
  }
}
</style>