<template>
  <div class="app-page app-page--plain">
    <div class="shell">
      <header class="head reveal">
        <div class="page-eyebrow">{{ t('list.foodEyebrow') }}</div>
        <h1 class="page-title">{{ t('page.food') }}</h1>
        <p class="page-sub">{{ t('list.foodSub') }}</p>
      </header>

      <!-- 搜索 -->
      <div class="search-field search-row reveal reveal-delay-1">
        <uni-icons type="search" size="17" color="#8C948B" />
        <input v-model="searchQuery" type="text" :placeholder="t('list.searchFood')" confirm-type="search"
          @confirm="handleFilterChange" />
        <button class="search-submit" @click="handleFilterChange">{{ t('common.search') }}</button>
      </div>

      <!-- 筛选 -->
      <div class="filter-group">
        <div class="chip-row">
          <button v-for="item in cityOptions" :key="item.value" class="chip"
            :class="{ 'is-active': filterCity === item.value }" @click="selectCity(item.value)">
            {{ t(item.labelKey) }}
          </button>
        </div>
        <div class="chip-row">
          <button v-for="item in cuisineOptions" :key="item.value" class="chip"
            :class="{ 'is-active': filterCuisine === item.value }" @click="selectCuisine(item.value)">
            {{ t(item.labelKey) }}
          </button>
        </div>
      </div>

      <!-- 骨架 -->
      <div v-if="loading" class="grid">
        <div v-for="i in 6" :key="i" class="card card--skeleton">
          <div class="skeleton media-skeleton"></div>
          <div class="card-body">
            <div class="skeleton skeleton-line skeleton-line--lg"></div>
            <div class="skeleton skeleton-line"></div>
            <div class="skeleton skeleton-line skeleton-line--sm"></div>
          </div>
        </div>
      </div>

      <!-- 列表 -->
      <div v-else-if="foods.length" class="grid">
        <article v-for="food in foods" :key="food.id" class="card" @click="navigateToDetail(food.id)">
          <div class="card-media">
            <img :src="food.coverImage" :alt="food.name" loading="lazy">
            <span v-if="food.cuisineType" class="media-badge">{{ food.cuisineType }}</span>
            <span v-if="food.rating" class="media-rating">
              <uni-icons type="star-filled" size="11" color="#FFFFFF" />
              {{ food.rating }}
            </span>
          </div>
          <div class="card-body">
            <h3 class="card-title">{{ food.name }}</h3>
            <div class="card-meta">
              <uni-icons type="location" size="12" color="currentColor" />
              {{ food.city }}
            </div>
            <p v-if="food.address" class="card-address clamp-1">{{ food.address }}</p>
            <div v-if="food.tags" class="tags">
              <span v-for="tag in food.tags.split(',')" :key="tag" class="tag-pill">{{ tag }}</span>
            </div>
            <div class="card-foot">
              <span class="price"><small>¥</small>{{ food.price }}</span>
              <div class="stats">
                <span><uni-icons type="heart" size="12" color="currentColor" />{{ compact(food.likeCount) }}</span>
                <span><uni-icons type="chat" size="12" color="currentColor" />{{ compact(food.commentCount) }}</span>
              </div>
            </div>
          </div>
        </article>
      </div>

      <!-- 空状态 -->
      <div v-else class="empty-state">
        <div class="empty-state__icon"><uni-icons type="fire" size="26" color="#C4613C" /></div>
        {{ t('list.emptyFood') }}
      </div>

      <!-- 分页 -->
      <div v-if="!loading && foods.length > 0" class="pager">
        <button class="pager-btn" :disabled="currentPage === 1" @click="changePage(currentPage - 1)">{{ t('common.prev') }}</button>
        <span class="pager-info">{{ currentPage }} / {{ totalPages }}</span>
        <button class="pager-btn" :disabled="currentPage === totalPages" @click="changePage(currentPage + 1)">{{ t('common.next') }}</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getFoodList } from '@/api/food'
import UniIcons from '@/uni_modules/uni-icons/components/uni-icons/uni-icons.vue'
import { t, usePageTitle } from '@/utils/i18n'

usePageTitle('page.food')

const searchQuery = ref('')
const filterCity = ref('')
const filterCuisine = ref('')

const currentPage = ref(1)
const pageSize = ref(10)
const totalPages = ref(1)
const foods = ref([])
const loading = ref(true)

const cityOptions = [
  { labelKey: 'list.allCities', value: '' },
  { labelKey: 'list.cityBeijing', value: '北京' },
  { labelKey: 'list.cityShanghai', value: '上海' },
  { labelKey: 'list.cityGuangzhou', value: '广州' },
  { labelKey: 'list.cityShenzhen', value: '深圳' }
]

const cuisineOptions = [
  { labelKey: 'list.allCuisines', value: '' },
  { labelKey: 'list.cuisineSichuan', value: '川菜' },
  { labelKey: 'list.cuisineCantonese', value: '粤菜' },
  { labelKey: 'list.cuisineShandong', value: '鲁菜' },
  { labelKey: 'list.cuisineHuaiyang', value: '淮扬菜' }
]

const compact = (value) => {
  const num = Number(value) || 0
  if (num >= 10000) return (num / 10000).toFixed(1).replace('.0', '') + 'w'
  if (num >= 1000) return (num / 1000).toFixed(1).replace('.0', '') + 'k'
  return String(num)
}

const calculateTotalPages = (total) => Math.ceil(total / pageSize.value) || 1

const fetchFoods = async () => {
  loading.value = true
  try {
    const response = await getFoodList({
      page: currentPage.value,
      pageSize: pageSize.value,
      city: filterCity.value,
      cuisine: filterCuisine.value,
      keyword: searchQuery.value
    })
    foods.value = response.data.records || []
    totalPages.value = calculateTotalPages(response.data.total || 0)
  } catch (error) {
    console.error('获取美食列表失败:', error)
    foods.value = []
  } finally {
    loading.value = false
  }
}

const handleFilterChange = () => {
  currentPage.value = 1
  fetchFoods()
}

const selectCity = (value) => {
  if (filterCity.value === value) return
  filterCity.value = value
  handleFilterChange()
}

const selectCuisine = (value) => {
  if (filterCuisine.value === value) return
  filterCuisine.value = value
  handleFilterChange()
}

const navigateToDetail = (id) => {
  uni.navigateTo({ url: `/pages/food/detail?id=${id}` })
}

const changePage = (page) => {
  if (page < 1 || page > totalPages.value) return
  currentPage.value = page
  fetchFoods()
  uni.pageScrollTo({ scrollTop: 0, duration: 240 })
}

onMounted(fetchFoods)
</script>

<style scoped>
.head {
  padding: 6px 2px 0;
}

.page-eyebrow {
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 1.6px;
  color: var(--primary-color);
}

.page-title {
  margin-top: 7px;
  font-size: 26px;
  font-weight: 800;
  letter-spacing: -0.5px;
  color: var(--text-color);
}

.page-sub {
  margin-top: 5px;
  font-size: 13px;
  line-height: 1.6;
  color: var(--text-secondary-color);
}

.search-row {
  margin-top: 16px;
}

.search-submit {
  flex: 0 0 auto;
  padding: 6px 14px;
  border-radius: var(--radius-pill);
  background: var(--primary-color);
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  transition: transform 0.2s var(--ease-out);
}

.search-submit:active {
  transform: scale(0.95);
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 14px;
}

.grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 14px;
  margin-top: 16px;
}

.card {
  background: var(--card-background);
  border: 1px solid var(--card-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.4s var(--ease-out), box-shadow 0.4s var(--ease-out),
    border-color 0.4s var(--ease-out);
}

.card:hover {
  transform: translateY(-5px);
  box-shadow: var(--shadow-md);
  border-color: var(--border-strong);
}

.card:active {
  transform: scale(0.99);
}

.card-media {
  position: relative;
  height: 180px;
  overflow: hidden;
  background: var(--surface-2);
}

.card-media img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.7s var(--ease-out);
}

.card:hover .card-media img {
  transform: scale(1.06);
}

.card-media::after {
  content: '';
  position: absolute;
  inset: auto 0 0;
  height: 44%;
  background: linear-gradient(to top, rgba(6, 16, 33, 0.48), transparent);
  pointer-events: none;
}

.media-badge {
  position: absolute;
  top: 12px;
  left: 12px;
  z-index: 2;
  padding: 4px 10px;
  border-radius: var(--radius-pill);
  background: rgba(255, 214, 200, 0.94);
  color: #C2410C;
  font-size: 11px;
  font-weight: 700;
  -webkit-backdrop-filter: blur(8px);
  backdrop-filter: blur(8px);
}

.media-rating {
  position: absolute;
  right: 12px;
  bottom: 10px;
  z-index: 2;
  display: inline-flex;
  align-items: center;
  gap: 3px;
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  text-shadow: 0 1px 4px rgba(0, 0, 0, 0.35);
}

.card-body {
  padding: 14px 15px 15px;
}

.card-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-color);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-meta {
  display: flex;
  align-items: center;
  gap: 3px;
  margin-top: 5px;
  font-size: 12px;
  color: var(--text-tertiary-color);
}

.card-address {
  margin-top: 6px;
  font-size: 12px;
  color: var(--text-secondary-color);
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 9px;
}

.card-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px dashed var(--border-color);
}

.price {
  display: inline-flex;
  align-items: baseline;
  gap: 1px;
  color: #C4613C;
  font-size: 18px;
  font-weight: 800;
  font-variant-numeric: tabular-nums;
}

.price small {
  font-size: 11px;
}

.stats {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 11px;
  color: var(--text-tertiary-color);
}

.stats span {
  display: inline-flex;
  align-items: center;
  gap: 3px;
}

.card--skeleton {
  pointer-events: none;
}

.media-skeleton {
  height: 180px;
  border-radius: 0;
}

.skeleton-line {
  height: 10px;
  margin-top: 8px;
  width: 72%;
}

.skeleton-line--lg {
  width: 100%;
  height: 14px;
}

.skeleton-line--sm {
  width: 44%;
}

@media (min-width: 768px) {
  .page-title {
    font-size: 32px;
  }

  .grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 18px;
  }

  .card-media,
  .media-skeleton {
    height: 210px;
  }
}

@media (min-width: 1100px) {
  .grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}
</style>
