<template>
  <div class="app-page app-page--plain detail-page">
    <div class="shell">
      <!-- 头图 -->
      <div class="hero reveal">
        <img v-if="attraction.coverImage" class="hero-img" :src="attraction.coverImage" :alt="attraction.name">
        <div class="hero-shade"></div>
        <div class="hero-content">
          <div class="hero-chips">
            <span v-if="attraction.season" class="hero-chip">{{ attraction.season }}</span>
            <span v-if="attraction.rating" class="hero-chip hero-chip--gold">
              <uni-icons type="star-filled" size="11" color="#FFD98A" />
              {{ attraction.rating }}
            </span>
            <span v-if="attraction.price > 0" class="hero-chip hero-chip--coral">¥{{ attraction.price }}</span>
            <span v-else class="hero-chip hero-chip--green">{{ t('detail.freeEntry') }}</span>
          </div>
          <h1 class="hero-title">{{ attraction.name || t('common.loading') }}</h1>
          <p class="hero-loc">
            <uni-icons type="location" size="13" color="rgba(255,255,255,0.85)" />
            {{ attraction.city }}
          </p>
          <div class="hero-stats">
            <span><uni-icons type="heart" size="13" color="rgba(255,255,255,0.9)" />{{ attraction.likeCount || 0 }}</span>
            <span><uni-icons type="chat" size="13" color="rgba(255,255,255,0.9)" />{{ attraction.commentCount || 0 }}</span>
            <span><uni-icons type="star" size="13" color="rgba(255,255,255,0.9)" />{{ attraction.collectCount || 0 }}</span>
          </div>
        </div>
      </div>

      <!-- 介绍 -->
      <section class="panel">
        <div class="panel-title">
          <span class="title-bar"></span>
          {{ t('detail.introAttraction') }}
        </div>
        <p class="panel-text">{{ attraction.description || t('detail.noIntro') }}</p>
      </section>

      <!-- 实用信息 -->
      <section class="panel">
        <div class="panel-title">
          <span class="title-bar"></span>
          {{ t('detail.info') }}
        </div>
        <div class="info-item">
          <span class="info-icon info-icon--blue"><uni-icons type="location" size="15" color="#1E5B48" /></span>
          <div class="info-body">
            <div class="info-label">{{ t('detail.address') }}</div>
            <div class="info-value">{{ attraction.address || t('detail.noAddress') }}</div>
          </div>
        </div>
        <div class="info-item">
          <span class="info-icon info-icon--green"><uni-icons type="calendar" size="15" color="#2F7A55" /></span>
          <div class="info-body">
            <div class="info-label">{{ t('detail.openTime') }}</div>
            <div class="info-value">{{ attraction.openTime || t('detail.noOpenTime') }}</div>
          </div>
        </div>
        <div class="info-item">
          <span class="info-icon info-icon--gold"><uni-icons type="star-filled" size="15" color="#C08A2E" /></span>
          <div class="info-body">
            <div class="info-label">{{ t('detail.rating') }}</div>
            <div class="info-value">{{ attraction.rating || t('detail.noRating') }}</div>
          </div>
        </div>
      </section>
    </div>

    <!-- 底部操作栏 -->
    <div class="action-bar">
      <div class="action-price">
        <template v-if="attraction.price > 0">
          <small>¥</small>{{ attraction.price }}
        </template>
        <template v-else>{{ t('common.free') }}</template>
      </div>
      <button class="btn btn--ghost action-collect" :class="{ 'is-active': collected }" @click="toggleCollect">
        <uni-icons :type="collected ? 'star-filled' : 'star'" size="15" color="currentColor" />
        {{ collected ? t('common.saved') : t('common.save') }}
      </button>
      <button class="btn btn--primary action-book" @click="book">{{ t('detail.bookNow') }}</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAttractionDetail } from '@/api/attraction'
import UniIcons from '@/uni_modules/uni-icons/components/uni-icons/uni-icons.vue'
import { t, usePageTitle } from '@/utils/i18n'

usePageTitle('page.attractionDetail')

const attraction = ref({})
const collected = ref(false)

const toggleCollect = () => {
  collected.value = !collected.value
  uni.showToast({
    title: collected.value ? t('detail.collectAdded') : t('detail.collectRemoved'),
    icon: 'none',
    position: 'bottom'
  })
}

const book = () => {
  uni.showToast({ title: t('detail.bookBuilding'), icon: 'none', position: 'bottom' })
}

onMounted(async () => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const { id } = currentPage.options || {}
  if (id) {
    try {
      const response = await getAttractionDetail(id)
      attraction.value = response.data || {}
    } catch (error) {
      console.error('获取景点详情失败:', error)
    }
  }
})
</script>

<style scoped>
.detail-page {
  padding-bottom: calc(104px + env(safe-area-inset-bottom));
}

/* 头图 */
.hero {
  position: relative;
  height: 320px;
  border-radius: var(--radius-xl);
  overflow: hidden;
  background: var(--surface-2);
  box-shadow: var(--shadow-lg);
}

.hero-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.hero-shade {
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, rgba(4, 12, 26, 0.86) 4%, rgba(4, 12, 26, 0.32) 46%, rgba(4, 12, 26, 0.08) 100%);
}

.hero-content {
  position: absolute;
  inset: auto 0 0;
  padding: 20px;
  color: #fff;
}

.hero-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 7px;
  margin-bottom: 10px;
}

.hero-chip {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  padding: 4px 10px;
  border-radius: var(--radius-pill);
  background: rgba(255, 255, 255, 0.18);
  border: 1px solid rgba(255, 255, 255, 0.26);
  -webkit-backdrop-filter: blur(10px);
  backdrop-filter: blur(10px);
  font-size: 11px;
  font-weight: 700;
  color: #fff;
}

.hero-chip--gold {
  background: rgba(192, 138, 46, 0.9);
  border-color: transparent;
  color: #3B2500;
}

.hero-chip--coral {
  background: rgba(255, 90, 60, 0.9);
  border-color: transparent;
}

.hero-chip--green {
  background: rgba(16, 185, 129, 0.9);
  border-color: transparent;
}

.hero-title {
  font-size: 26px;
  font-weight: 800;
  line-height: 1.25;
  letter-spacing: -0.4px;
  text-shadow: 0 2px 12px rgba(0, 0, 0, 0.28);
}

.hero-loc {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 8px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.86);
}

.hero-stats {
  display: flex;
  gap: 18px;
  margin-top: 12px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.86);
}

.hero-stats span {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

/* 内容面板 */
.panel {
  margin-top: 14px;
  padding: 18px 18px 6px;
  background: var(--card-background);
  border: 1px solid var(--card-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}

.panel-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  font-size: 16px;
  font-weight: 700;
  color: var(--text-color);
}

.title-bar {
  width: 4px;
  height: 16px;
  border-radius: 4px;
  background: var(--primary-color);
}

.panel-text {
  padding-bottom: 14px;
  font-size: 14px;
  line-height: 1.75;
  color: var(--text-secondary-color);
  white-space: pre-wrap;
}

.info-item {
  display: flex;
  gap: 12px;
  padding: 12px 0;
  border-top: 1px solid var(--border-color);
}

.info-item:first-of-type {
  border-top: none;
}

.info-icon {
  flex: 0 0 auto;
  width: 34px;
  height: 34px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-sm);
}

.info-icon--blue {
  background: var(--primary-soft);
}

.info-icon--green {
  background: rgba(16, 185, 129, 0.13);
}

.info-icon--gold {
  background: rgba(192, 138, 46, 0.15);
}

.info-body {
  flex: 1;
  min-width: 0;
}

.info-label {
  font-size: 11px;
  font-weight: 600;
  color: var(--text-tertiary-color);
}

.info-value {
  margin-top: 2px;
  font-size: 14px;
  line-height: 1.6;
  color: var(--text-color);
}

/* 底部操作栏 */
.action-bar {
  position: fixed;
  left: 50%;
  bottom: 0;
  z-index: 80;
  transform: translateX(-50%);
  width: 100%;
  max-width: 1200px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px calc(12px + env(safe-area-inset-bottom));
  background: var(--glass-background);
  -webkit-backdrop-filter: blur(20px);
  backdrop-filter: blur(20px);
  border-top: 1px solid var(--card-border);
}

.action-price {
  flex: 1;
  display: inline-flex;
  align-items: baseline;
  gap: 1px;
  color: #C4613C;
  font-size: 22px;
  font-weight: 800;
}

.action-price small {
  font-size: 12px;
}

.action-collect {
  gap: 4px;
  padding: 11px 16px;
}

.action-collect.is-active {
  color: var(--gold-color);
  border-color: rgba(192, 138, 46, 0.5);
  background: rgba(192, 138, 46, 0.1);
}

.action-book {
  flex: 0 0 auto;
}

@media (min-width: 768px) {
  .hero {
    height: 420px;
  }

  .hero-content {
    padding: 32px 36px;
  }

  .hero-title {
    font-size: 36px;
  }

  .panel {
    padding: 24px 26px 10px;
  }

  .action-bar {
    left: 50%;
    width: min(100%, 1200px);
    border-radius: 20px 20px 0 0;
    padding: 14px 28px calc(14px + env(safe-area-inset-bottom));
  }
}
</style>
