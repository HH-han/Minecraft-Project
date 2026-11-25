<template>
  <div class="app-page">
    <div class="shell">

      <!-- 页头 -->
      <header class="page-head reveal">
        <div class="page-head__glow"></div>
        <div class="page-eyebrow">{{ t('discover.eyebrow') }}</div>
        <h1 class="page-title">{{ t('discover.title') }}</h1>
        <p class="page-sub">{{ t('discover.sub') }}</p>
      </header>

      <!-- 分类筛选 -->
      <div class="chip-row category-row reveal reveal-delay-1">
        <button v-for="category in categories" :key="category.value" class="chip"
          :class="{ 'is-active': selectedCategory === category.value }" @click="selectCategory(category.value)">
          {{ t(category.labelKey) }}
        </button>
      </div>

      <!-- 骨架屏 -->
      <div v-if="loading" class="world-grid">
        <div v-for="i in 6" :key="i" class="world-card world-card--skeleton">
          <div class="skeleton world-media-skeleton"></div>
          <div class="world-body">
            <div class="skeleton skeleton-line skeleton-line--lg"></div>
            <div class="skeleton skeleton-line"></div>
            <div class="skeleton skeleton-line skeleton-line--sm"></div>
          </div>
        </div>
      </div>

      <!-- 世界特征列表 -->
      <div v-else-if="characteristics.length" class="world-grid">
        <article v-for="item in characteristics" :key="item.id" class="world-card">
          <div class="world-media">
            <img :src="item.image" :alt="item.name" loading="lazy">
            <span v-if="item.category" class="world-cat"
              :style="item.color ? { backgroundColor: item.color + '26', color: item.color } : {}">
              {{ item.category }}
            </span>
          </div>
          <div class="world-body">
            <div class="world-head">
              <h3 class="world-name">{{ item.name }}</h3>
              <span v-if="item.country" class="world-country">
                <uni-icons type="location" size="12" color="currentColor" />
                {{ item.country }}
              </span>
            </div>
            <p class="world-desc clamp-2">{{ item.description }}</p>

            <div v-if="item.culture || item.history" class="world-facts">
              <div v-if="item.culture" class="world-fact">
                <span class="fact-label">{{ t('discover.culture') }}</span>
                <span class="fact-text clamp-1">{{ item.culture }}</span>
              </div>
              <div v-if="item.history" class="world-fact">
                <span class="fact-label">{{ t('discover.history') }}</span>
                <span class="fact-text clamp-1">{{ item.history }}</span>
              </div>
            </div>

            <div v-if="item.features" class="world-feature">
              <uni-icons type="fire-filled" size="13" color="#C4613C" />
              <span class="clamp-1">{{ item.features }}</span>
            </div>

            <div v-if="item.tags" class="world-tags">
              <span v-for="tag in (item.tags ? item.tags.split(',') : [])" :key="tag" class="tag-pill">{{ tag }}</span>
            </div>
          </div>
        </article>
      </div>

      <!-- 空状态 -->
      <div v-else class="empty-state">
        <div class="empty-state__icon"><uni-icons type="map-filled" size="26" color="#1E5B48" /></div>
        {{ t('discover.empty') }}
      </div>

      <!-- 分页 -->
      <div v-if="!loading && characteristics.length > 0" class="pager">
        <button class="pager-btn" :disabled="currentPage === 1" @click="changePage(currentPage - 1)">{{ t('common.prev') }}</button>
        <span class="pager-info">{{ currentPage }} / {{ totalPages }}</span>
        <button class="pager-btn" :disabled="currentPage === totalPages" @click="changePage(currentPage + 1)">{{ t('common.next') }}</button>
      </div>
    </div>

    <TabBar current="discover" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getWorldCharacteristicsPage, getWorldCharacteristicsByCategory } from '@/api/worldcharacteristics'
import UniIcons from '@/uni_modules/uni-icons/components/uni-icons/uni-icons.vue'
import TabBar from '@/components/tabbar/index.vue'
import { t, usePageTitle } from '@/utils/i18n'

usePageTitle('page.discover')

const categories = ref([
  { labelKey: 'discover.catAll', value: '全部' },
  { labelKey: 'discover.catNature', value: '自然景观' },
  { labelKey: 'discover.catHeritage', value: '文化遗产' },
  { labelKey: 'discover.catHistoric', value: '历史建筑' },
  { labelKey: 'discover.catFood', value: '特色美食' },
  { labelKey: 'discover.catCustom', value: '传统习俗' }
])
const selectedCategory = ref('全部')

const currentPage = ref(1)
const pageSize = ref(10)
const totalPages = ref(1)

const characteristics = ref([])
const loading = ref(true)

const calculateTotalPages = (total) => Math.ceil(total / pageSize.value) || 1

const fetchCharacteristics = async () => {
  loading.value = true
  try {
    if (selectedCategory.value === '全部') {
      const response = await getWorldCharacteristicsPage(currentPage.value, pageSize.value)
      characteristics.value = response.data.records || []
      totalPages.value = calculateTotalPages(response.data.total || 0)
    } else {
      const response = await getWorldCharacteristicsByCategory(selectedCategory.value)
      characteristics.value = response.data || []
      totalPages.value = 1
    }
  } catch (error) {
    console.error('获取世界特征失败:', error)
    characteristics.value = []
  } finally {
    loading.value = false
  }
}

const selectCategory = (category) => {
  if (selectedCategory.value === category) return
  selectedCategory.value = category
  currentPage.value = 1
  fetchCharacteristics()
  uni.pageScrollTo({ scrollTop: 0, duration: 240 })
}

const changePage = (page) => {
  if (page < 1 || page > totalPages.value) return
  currentPage.value = page
  fetchCharacteristics()
  uni.pageScrollTo({ scrollTop: 0, duration: 240 })
}

onMounted(fetchCharacteristics)
</script>

<style scoped>
/* 页头 */
.page-head {
  position: relative;
  overflow: hidden;
  padding: 24px 22px;
  border-radius: var(--radius-xl);
  background: var(--card-background);
  border: 1px solid var(--card-border);
  box-shadow: var(--shadow-sm);
}

.page-head__glow {
  position: absolute;
  top: -80px;
  right: -30px;
  width: 210px;
  height: 210px;
  border-radius: 50%;
  background: var(--primary-soft);
  filter: blur(40px);
  pointer-events: none;
}

.page-eyebrow {
  position: relative;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 1.6px;
  color: var(--primary-color);
}

.page-title {
  position: relative;
  margin-top: 8px;
  font-size: 26px;
  font-weight: 800;
  letter-spacing: -0.5px;
  color: var(--text-color);
}

.page-sub {
  position: relative;
  margin-top: 6px;
  font-size: 13px;
  line-height: 1.6;
  color: var(--text-secondary-color);
  max-width: 520px;
}

.category-row {
  margin-top: 16px;
}

/* 网格 */
.world-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 14px;
  margin-top: 6px;
}

.world-card {
  display: flex;
  flex-direction: column;
  background: var(--card-background);
  border: 1px solid var(--card-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.4s var(--ease-out), box-shadow 0.4s var(--ease-out),
    border-color 0.4s var(--ease-out);
}

.world-card:hover {
  transform: translateY(-5px);
  box-shadow: var(--shadow-md);
  border-color: var(--border-strong);
}

.world-media {
  position: relative;
  height: 168px;
  overflow: hidden;
  background: var(--surface-2);
}

.world-media img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.7s var(--ease-out);
}

.world-card:hover .world-media img {
  transform: scale(1.06);
}

.world-cat {
  position: absolute;
  top: 12px;
  left: 12px;
  padding: 4px 10px;
  border-radius: var(--radius-pill);
  background: rgba(255, 255, 255, 0.92);
  color: var(--primary-color);
  font-size: 11px;
  font-weight: 700;
  -webkit-backdrop-filter: blur(8px);
  backdrop-filter: blur(8px);
}

.world-body {
  display: flex;
  flex-direction: column;
  gap: 9px;
  padding: 14px 15px 16px;
}

.world-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 10px;
}

.world-name {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-color);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.world-country {
  flex: 0 0 auto;
  display: inline-flex;
  align-items: center;
  gap: 2px;
  font-size: 11px;
  color: var(--text-tertiary-color);
}

.world-desc {
  font-size: 13px;
  line-height: 1.6;
  color: var(--text-secondary-color);
}

.world-facts {
  display: flex;
  flex-direction: column;
  gap: 5px;
  padding: 9px 11px;
  border-radius: var(--radius-sm);
  background: var(--surface-2);
}

.world-fact {
  display: flex;
  align-items: center;
  gap: 7px;
  font-size: 12px;
  color: var(--text-secondary-color);
  min-width: 0;
}

.fact-label {
  flex: 0 0 auto;
  padding: 1px 7px;
  border-radius: var(--radius-pill);
  background: var(--primary-soft);
  color: var(--primary-color);
  font-size: 10px;
  font-weight: 700;
}

.fact-text {
  min-width: 0;
}

.world-feature {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  font-weight: 500;
  color: var(--text-secondary-color);
  min-width: 0;
}

.world-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

/* 骨架 */
.world-card--skeleton {
  pointer-events: none;
}

.world-media-skeleton {
  height: 168px;
  border-radius: 0;
}

.skeleton-line {
  height: 10px;
  width: 72%;
}

.skeleton-line--lg {
  width: 100%;
  height: 14px;
}

.skeleton-line--sm {
  width: 40%;
}

/* PC */
@media (min-width: 768px) {
  .page-head {
    padding: 34px 38px;
  }

  .page-title {
    font-size: 34px;
  }

  .world-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 18px;
  }
}

@media (min-width: 1100px) {
  .world-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .world-media {
    height: 190px;
  }
}
</style>
