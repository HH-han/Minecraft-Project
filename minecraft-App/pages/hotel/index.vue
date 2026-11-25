<template>
  <div class="app-page app-page--plain">
    <div class="shell">
      <header class="head reveal">
        <div class="page-eyebrow">{{ t('list.hotelEyebrow') }}</div>
        <h1 class="page-title">{{ t('page.hotel') }}</h1>
        <p class="page-sub">{{ t('list.hotelSub') }}</p>
      </header>

      <!-- 搜索 -->
      <div class="search-field search-row reveal reveal-delay-1">
        <uni-icons type="search" size="17" color="#8C948B" />
        <input v-model="searchQuery" type="text" :placeholder="t('list.searchHotel')" confirm-type="search"
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
          <button v-for="item in priceOptions" :key="item.value" class="chip"
            :class="{ 'is-active': filterPrice === item.value }" @click="selectPrice(item.value)">
            {{ item.labelKey ? t(item.labelKey) : item.label }}
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
      <div v-else-if="hotels.length" class="grid">
        <article v-for="hotel in hotels" :key="hotel.id" class="card" @click="navigateToDetail(hotel.id)">
          <div class="card-media">
            <img :src="hotel.coverImage" :alt="hotel.name" loading="lazy">
            <span v-if="hotel.starLevel" class="media-badge">
              <uni-icons type="medal-filled" size="11" color="#8A5A00" />
              {{ hotel.starLevel }}星
            </span>
          </div>
          <div class="card-body">
            <h3 class="card-title">{{ hotel.name }}</h3>
            <div class="card-meta">
              <uni-icons type="location" size="12" color="currentColor" />
              {{ hotel.city }}
            </div>
            <p v-if="hotel.address" class="card-address clamp-1">{{ hotel.address }}</p>
            <div v-if="hotel.facilities" class="facilities">
              <span v-for="facility in hotel.facilities.split(',')" :key="facility" class="tag-pill">{{ facility }}</span>
            </div>
            <div class="card-foot">
              <span class="price"><small>¥</small>{{ hotel.price }}<em>{{ t('common.perNight') }}</em></span>
              <div class="stats">
                <span><uni-icons type="heart" size="12" color="currentColor" />{{ compact(hotel.likeCount) }}</span>
                <span><uni-icons type="star" size="12" color="currentColor" />{{ compact(hotel.collectCount) }}</span>
              </div>
            </div>
          </div>
        </article>
      </div>

      <!-- 空状态 -->
      <div v-else class="empty-state">
        <div class="empty-state__icon"><uni-icons type="shop" size="26" color="#1E5B48" /></div>
        {{ t('list.emptyHotel') }}
      </div>

      <!-- 分页 -->
      <div v-if="!loading && hotels.length > 0" class="pager">
        <button class="pager-btn" :disabled="currentPage === 1" @click="changePage(currentPage - 1)">{{ t('common.prev') }}</button>
        <span class="pager-info">{{ currentPage }} / {{ totalPages }}</span>
        <button class="pager-btn" :disabled="currentPage === totalPages" @click="changePage(currentPage + 1)">{{ t('common.next') }}</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getHotelList } from '@/api/hotel'
import UniIcons from '@/uni_modules/uni-icons/components/uni-icons/uni-icons.vue'
import { t, usePageTitle } from '@/utils/i18n'

usePageTitle('page.hotel')

const searchQuery = ref('')
const filterCity = ref('')
const filterPrice = ref('')

const currentPage = ref(1)
const pageSize = ref(10)
const totalPages = ref(1)
const hotels = ref([])
const loading = ref(true)

const cityOptions = [
  { labelKey: 'list.allCities', value: '' },
  { labelKey: 'list.cityBeijing', value: '北京' },
  { labelKey: 'list.cityShanghai', value: '上海' },
  { labelKey: 'list.cityGuangzhou', value: '广州' },
  { labelKey: 'list.cityShenzhen', value: '深圳' }
]

const priceOptions = [
  { labelKey: 'list.anyPrice', value: '' },
  { label: '¥0-200', value: '0-200' },
  { label: '¥200-500', value: '200-500' },
  { label: '¥500-1000', value: '500-1000' },
  { label: '¥1000+', value: '1000+' }
]

const compact = (value) => {
  const num = Number(value) || 0
  if (num >= 10000) return (num / 10000).toFixed(1).replace('.0', '') + 'w'
  if (num >= 1000) return (num / 1000).toFixed(1).replace('.0', '') + 'k'
  return String(num)
}

const calculateTotalPages = (total) => Math.ceil(total / pageSize.value) || 1

const fetchHotels = async () => {
  loading.value = true
  try {
    const response = await getHotelList({
      page: currentPage.value,
      pageSize: pageSize.value,
      city: filterCity.value,
      priceRange: filterPrice.value,
      keyword: searchQuery.value
    })
    hotels.value = response.data.records || []
    totalPages.value = calculateTotalPages(response.data.total || 0)
  } catch (error) {
    console.error('获取酒店列表失败:', error)
    hotels.value = []
  } finally {
    loading.value = false
  }
}

const handleFilterChange = () => {
  currentPage.value = 1
  fetchHotels()
}

const selectCity = (value) => {
  if (filterCity.value === value) return
  filterCity.value = value
  handleFilterChange()
}

const selectPrice = (value) => {
  if (filterPrice.value === value) return
  filterPrice.value = value
  handleFilterChange()
}

const navigateToDetail = (id) => {
  uni.navigateTo({ url: `/pages/hotel/detail?id=${id}` })
}

const changePage = (page) => {
  if (page < 1 || page > totalPages.value) return
  currentPage.value = page
  fetchHotels()
  uni.pageScrollTo({ scrollTop: 0, duration: 240 })
}

onMounted(fetchHotels)
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

.media-badge {
  position: absolute;
  top: 12px;
  left: 12px;
  z-index: 2;
  display: inline-flex;
  align-items: center;
  gap: 3px;
  padding: 4px 10px;
  border-radius: var(--radius-pill);
  background: rgba(255, 214, 130, 0.92);
  color: #8A5A00;
  font-size: 11px;
  font-weight: 700;
  -webkit-backdrop-filter: blur(8px);
  backdrop-filter: blur(8px);
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

.facilities {
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

.price em {
  font-style: normal;
  margin-left: 2px;
  font-size: 11px;
  font-weight: 500;
  color: var(--text-tertiary-color);
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
