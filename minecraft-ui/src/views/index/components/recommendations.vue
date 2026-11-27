<template>
  <div class="recommendations-container">
    <div class="section-header">
      <h2 class="section-title">{{ $t('homeSections.recommendations.title') }}</h2>
      <span class="section-subtitle">{{ $t('homeSections.recommendations.subtitle') }}</span>
    </div>

    <div class="filter-bar">
      <div class="filter-row">
        <span class="filter-label">{{ $t('homeSections.recommendations.filterCategory') }}</span>
        <div class="category-list">
          <button v-for="category in categories" :key="String(category.id)"
            :class="['category-btn', { active: selectedCategory === category.id }]"
            :aria-pressed="selectedCategory === category.id" @click="filterByCategory(category.id)">
            {{ category.name }}
          </button>
        </div>
      </div>
      <div class="filter-row">
        <span class="filter-label">{{ $t('homeSections.recommendations.filterPrice') }}</span>
        <div class="category-list">
          <button v-for="priceCategory in priceCategories" :key="String(priceCategory.id)"
            :class="['category-btn', { active: selectedPriceCategory === priceCategory.id }]"
            :aria-pressed="selectedPriceCategory === priceCategory.id"
            @click="filterByPriceCategory(priceCategory.id)">
            {{ priceCategory.name }}
          </button>
        </div>
      </div>
    </div>

    <div v-if="displayedRecommendations.length" class="recommendations-grid">
      <div v-for="(item, index) in displayedRecommendations" :key="item.id" class="card"
        :class="{ 'card--expanded': isExpanded && index >= DEFAULT_DISPLAY_COUNT }"
        :style="{ animationDelay: `${Math.min(index, DEFAULT_DISPLAY_COUNT) * 0.05}s` }"
        @click="openRecommendationDetail(item)">
        <div class="card-image-wrapper">
          <img :src="item.coverImageUrl" :alt="item.name" class="card-image" loading="lazy">
          <div class="card-overlay"></div>
          <span class="category-tag">{{ item.categoryName }}</span>
          <span v-if="item.isFree" class="free-badge">{{ $t('homeSections.recommendations.free') }}</span>
        </div>

        <div class="card-content">
          <h3 class="card-title">{{ item.name }}</h3>
          <p class="card-description">{{ excerpt(item.summary || item.recommendationReason) }}</p>

          <div class="card-meta">
            <span class="card-location">{{ item.city }} · {{ item.address }}</span>
            <span class="card-price">{{ item.isFree ? $t('homeSections.common.free') : `¥${item.minPrice || 0}`
              }}</span>
          </div>

          <div class="card-actions">
            <button class="primary-cta">
              {{ $t('homeSections.common.viewDetail') }}
              <svg class="cta-arrow" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                aria-hidden="true">
                <path d="M5 12h14M12 5l7 7-7 7" />
              </svg>
            </button>
            <button class="secondary-cta" :aria-label="$t('homeSections.common.favorite')"
              @click.stop="toggleFavorite(item.id)">
              <svg class="heart-icon" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
                <path
                  d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z" />
              </svg>
            </button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="hasMoreData" class="expand-actions">
      <button class="expand-btn" @click="toggleExpand">
        <span>{{ isExpanded ? $t('homeSections.common.collapse') : $t('homeSections.common.expandAll') }}</span>
        <svg class="expand-arrow" :class="{ 'expand-arrow--up': isExpanded }" viewBox="0 0 24 24" fill="none"
          stroke="currentColor" stroke-width="2" aria-hidden="true">
          <polyline points="6 9 12 15 18 9" />
        </svg>
      </button>
    </div>

    <div v-if="recommendations.length === 0" class="empty-state">
      <div class="empty-icon">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <circle cx="12" cy="12" r="10" />
          <path d="M2 12h20M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z" />
        </svg>
      </div>
      <p class="empty-text">{{ $t('homeSections.recommendations.empty') }}</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { getHotRecommendations } from '@/api/homeRecommendations'
import { useAutoRefresh } from '@/hooks/useAutoRefresh'

const emit = defineEmits(['open-recommendation-detail'])
const { t } = useI18n()

// 默认展示的数据条数
const DEFAULT_DISPLAY_COUNT = 4;

const recommendations = ref([])
const allRecommendations = ref([])
const selectedCategory = ref(null)
const selectedPriceCategory = ref(null)
const isExpanded = ref(false)

// 默认仅展示前若干条，展开后显示全部
const displayedRecommendations = computed(() => {
  if (isExpanded.value || recommendations.value.length <= DEFAULT_DISPLAY_COUNT) {
    return recommendations.value
  }
  return recommendations.value.slice(0, DEFAULT_DISPLAY_COUNT)
})

// 是否存在折叠数据（用于控制按钮显示）
const hasMoreData = computed(() => recommendations.value.length > DEFAULT_DISPLAY_COUNT)

// 筛选项（随语言切换响应式更新）
const categories = computed(() => [
  { id: null, name: t('homeSections.recommendations.all') },
  { id: 1, name: t('homeSections.recommendations.categories.nature') },
  { id: 2, name: t('homeSections.recommendations.categories.history') },
  { id: 3, name: t('homeSections.recommendations.categories.park') },
  { id: 4, name: t('homeSections.recommendations.categories.food') },
  { id: 5, name: t('homeSections.recommendations.categories.resort') }
])

const priceCategories = computed(() => [
  { id: null, name: t('homeSections.recommendations.all') },
  { id: true, name: t('homeSections.recommendations.free') },
  { id: false, name: t('homeSections.recommendations.paid') }
])

onMounted(() => {
  loadHotRecommendations()
})

const loadHotRecommendations = async () => {
  try {
    const response = await getHotRecommendations(20)
    if (response.code === 200 && response.data) {
      allRecommendations.value = response.data
      recommendations.value = response.data
    }
  } catch (error) {
    console.error('Failed to load hot recommendations:', error)
  }
}

// 数据变更后自动刷新
useAutoRefresh(loadHotRecommendations)

const filterByCategory = (categoryId) => {
  selectedCategory.value = categoryId
  applyFilters()
}

const filterByPriceCategory = (priceCategoryId) => {
  selectedPriceCategory.value = priceCategoryId
  applyFilters()
}

const applyFilters = () => {
  recommendations.value = allRecommendations.value.filter(item => {
    const categoryMatch = selectedCategory.value === null || item.categoryId === selectedCategory.value
    const priceMatch = selectedPriceCategory.value === null || item.isFree === selectedPriceCategory.value
    return categoryMatch && priceMatch
  })
  // 切换筛选条件时重置为折叠状态
  isExpanded.value = false
}

const toggleExpand = () => {
  isExpanded.value = !isExpanded.value
}

/** 截断简介，避免空描述报错 */
const excerpt = (text) => {
  const value = (text || '').trim() || t('homeSections.recommendations.noSummary')
  return value.length > 100 ? `${value.slice(0, 100)}…` : value
}

const openRecommendationDetail = (item) => {
  emit('open-recommendation-detail', item)
}

const toggleFavorite = (id) => {
  console.log('Toggle favorite:', id)
}
</script>

<style scoped>
.recommendations-container {
  width: 100%;
}

.section-header {
  margin-bottom: 0;
}

.section-title {
  font-size: clamp(24px, 2.6vw, 34px);
  font-weight: 700;
  color: var(--theme-text-1);
  margin: 0;
  letter-spacing: -0.03em;
  line-height: 1.2;
}

.section-subtitle {
  display: block;
  font-size: 14.5px;
  color: var(--theme-text-2);
  margin-top: 6px;
  font-weight: 400;
}

/* 紧凑筛选条 */
.filter-bar {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 12px 14px;
  margin: 20px 0 26px;
  background: var(--theme-bg-surface);
  border: 1px solid var(--theme-hairline);
  border-radius: 18px;
}

.filter-row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.filter-label {
  flex: none;
  padding-top: 8px;
  font-size: 12.5px;
  font-weight: 600;
  letter-spacing: 0.06em;
  color: var(--theme-text-3);
  min-width: 32px;
}

.category-list {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  min-width: 0;
}

.category-btn {
  padding: 8px 16px;
  background: var(--theme-bg-surface-2);
  border: 1px solid transparent;
  border-radius: 980px;
  font-size: 13px;
  font-weight: 500;
  color: var(--theme-text-2);
  cursor: pointer;
  transition: color 0.24s ease, background 0.24s ease, border-color 0.24s ease;
}

.category-btn:hover {
  background: var(--theme-bg-hover);
  color: var(--theme-text-1);
}

.category-btn.active {
  background: var(--theme-gradient);
  border-color: transparent;
  color: var(--theme-primary-text);
  box-shadow: var(--theme-shadow-sm);
}

.recommendations-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(min(100%, 290px), 1fr));
  gap: 26px;
  transition: opacity 0.4s ease;
}

.expand-actions {
  display: flex;
  justify-content: center;
  margin-top: 40px;
}

.expand-btn {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 12px 32px;
  background: var(--theme-bg-surface);
  border: 1px solid var(--theme-border);
  border-radius: 980px;
  font-size: 14.5px;
  font-weight: 500;
  color: var(--theme-text-1);
  cursor: pointer;
  transition: all 0.3s var(--theme-ease, ease);
  box-shadow: var(--theme-shadow-sm);
}

.expand-btn:hover {
  background: var(--theme-primary-soft);
  border-color: var(--theme-primary);
  color: var(--theme-primary);
  transform: translateY(-2px);
  box-shadow: var(--theme-shadow-md);
}

.expand-btn:active {
  transform: translateY(0);
}

.expand-arrow {
  width: 18px;
  height: 18px;
  transition: transform 0.3s ease;
}

.expand-arrow--up {
  transform: rotate(180deg);
}

.card {
  min-width: 0;
  background: var(--theme-bg-surface);
  border: 1px solid var(--theme-hairline);
  border-radius: 26px;
  overflow: hidden;
  box-shadow: var(--theme-shadow-md);
  transition: transform 0.5s cubic-bezier(0.22, 1, 0.36, 1), box-shadow 0.5s ease,
    border-color 0.5s ease;
  cursor: pointer;
  opacity: 0;
  display: flex;
  flex-direction: column;
  animation: fadeInUp 0.5s ease-out forwards;
}

.card:hover {
  transform: translateY(-6px);
  border-color: var(--theme-primary);
  box-shadow: var(--theme-shadow-lg), 0 0 0 1px var(--theme-primary-soft);
}

/* 展开时新增卡片使用更轻量的淡入动画 */
.card--expanded {
  animation: fadeInExpand 0.5s ease-out forwards;
}

@keyframes fadeInExpand {
  from {
    opacity: 0;
    transform: translateY(20px) scale(0.98);
  }

  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

@keyframes fadeInUp {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.card-image-wrapper {
  position: relative;
  height: 224px;
  overflow: hidden;
  background: var(--theme-bg-surface-2);
}

.card-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.6s cubic-bezier(0.22, 1, 0.36, 1);
}

.card:hover .card-image {
  transform: scale(1.06);
}

.card-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(to bottom, transparent 45%, var(--theme-bg-glass) 100%);
  pointer-events: none;
}

.category-tag {
  position: absolute;
  top: 14px;
  left: 14px;
  padding: 6px 12px;
  background: var(--theme-bg-glass-strong);
  border: 1px solid var(--theme-border);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border-radius: 980px;
  font-size: 11.5px;
  font-weight: 600;
  color: var(--theme-text-1);
  letter-spacing: 0.04em;
}

.free-badge {
  position: absolute;
  top: 14px;
  right: 14px;
  padding: 6px 12px;
  background: var(--theme-success);
  border-radius: 980px;
  font-size: 11.5px;
  font-weight: 600;
  color: var(--theme-primary-text);
}

.card-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 22px;
}

.card-title {
  font-size: 19px;
  font-weight: 600;
  color: var(--theme-text-1);
  margin: 0 0 10px;
  line-height: 1.35;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-description {
  font-size: 14px;
  color: var(--theme-text-2);
  margin: 0 0 18px;
  line-height: 1.65;
  min-height: 46px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.card-location {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 13px;
  color: var(--theme-text-3);
}

.card-price {
  flex: 0 0 auto;
  white-space: nowrap;
  font-size: 18px;
  font-weight: 700;
  background: var(--theme-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.card-actions {
  display: flex;
  gap: 10px;
  justify-content: space-between;
  margin-top: auto;
}

.primary-cta {
  flex: 1;
  padding: 11px 18px;
  background: var(--theme-gradient);
  color: var(--theme-primary-text);
  border: none;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.3s var(--theme-ease, ease);
  box-shadow: var(--theme-shadow-sm);
}

.primary-cta:hover {
  transform: translateY(-2px);
  box-shadow: var(--theme-shadow-md);
}

.cta-arrow {
  width: 17px;
  height: 17px;
  transition: transform 0.3s ease;
}

.primary-cta:hover .cta-arrow {
  transform: translateX(4px);
}

.secondary-cta {
  width: 44px;
  height: 44px;
  padding: 0;
  flex: 0 0 44px;
  background: var(--theme-bg-surface-2);
  color: var(--theme-text-3);
  border: none;
  border-radius: 12px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.secondary-cta:hover {
  background: var(--theme-danger-soft);
  color: var(--theme-danger);
  transform: scale(1.05);
}

.heart-icon {
  width: 19px;
  height: 19px;
  transition: transform 0.3s ease;
}

.secondary-cta:hover .heart-icon {
  transform: scale(1.1);
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 64px 0;
  background: var(--theme-bg-surface);
  border: 1px dashed var(--theme-border);
  border-radius: 24px;
}

.empty-icon {
  width: 56px;
  height: 56px;
  color: var(--theme-text-3);
  margin-bottom: 16px;
}

.empty-text {
  margin: 0;
  font-size: 14.5px;
  color: var(--theme-text-2);
}

@media (max-width: 768px) {
  .section-title {
    font-size: 24px;
  }

  .section-subtitle {
    font-size: 13.5px;
  }

  .filter-bar {
    padding: 12px;
    margin: 16px 0 22px;
    border-radius: 16px;
  }

  .filter-row {
    flex-direction: column;
    gap: 6px;
  }

  .filter-label {
    padding-top: 0;
  }

  .category-list {
    gap: 6px;
  }

  .category-btn {
    padding: 7px 13px;
    font-size: 12.5px;
  }

  .recommendations-grid {
    grid-template-columns: minmax(0, 1fr);
    gap: 20px;
  }

  .expand-actions {
    margin-top: 30px;
  }

  .card-image-wrapper {
    height: 200px;
  }

  .card-content {
    padding: 20px;
  }

  .card-title {
    font-size: 18px;
  }

  .card-description {
    font-size: 13.5px;
    min-height: 0;
  }

  .card-price {
    font-size: 17px;
  }
}

@media (max-width: 375px) {
  .section-title {
    font-size: 22px;
  }

  .card-image-wrapper {
    height: 184px;
  }

  .card-content {
    padding: 18px;
  }

  .card-title {
    font-size: 17px;
  }

  .primary-cta {
    padding: 10px 14px;
    font-size: 13.5px;
  }

  .secondary-cta {
    flex: 0 0 40px;
    width: 40px;
    height: 40px;
  }
}
</style>
