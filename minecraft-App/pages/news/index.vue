<template>
  <div class="app-page">
    <div class="shell">

      <!-- 页头 -->
      <header class="news-head reveal">
        <div class="news-head__glow"></div>
        <div class="page-eyebrow">{{ t('news.eyebrow') }}</div>
        <h1 class="page-title">{{ t('news.title') }}</h1>
        <p class="page-sub">{{ t('news.sub') }}</p>
      </header>

      <!-- 搜索 -->
      <div class="search-field search-row reveal reveal-delay-1">
        <uni-icons type="search" size="17" color="#8C948B" />
        <input v-model="searchQuery" type="text" :placeholder="t('news.searchPlaceholder')" confirm-type="search"
          @confirm="handleSearch" />
        <button class="search-submit" @click="handleSearch">{{ t('common.search') }}</button>
      </div>

      <!-- 骨架 -->
      <div v-if="loading" class="news-list">
        <div v-for="i in 4" :key="i" class="news-card">
          <div class="skeleton news-thumb-skeleton"></div>
          <div class="news-text">
            <div class="skeleton skeleton-line skeleton-line--sm"></div>
            <div class="skeleton skeleton-line skeleton-line--lg"></div>
            <div class="skeleton skeleton-line"></div>
          </div>
        </div>
      </div>

      <!-- 列表 -->
      <div v-else-if="newsList.length" class="news-list">
        <article v-for="(news, index) in newsList" :key="news.id" class="news-card"
          :class="{ 'news-card--featured': index === 0 && news.coverImage }"
          @click="navigateToDetail(news.id)">
          <div v-if="news.coverImage" class="news-thumb">
            <img :src="news.coverImage" :alt="news.title" loading="lazy">
            <span v-if="index === 0" class="featured-flag">{{ t('news.featured') }}</span>
          </div>
          <div class="news-text">
            <div class="news-top">
              <span v-if="news.source" class="news-source">{{ news.source }}</span>
              <span class="news-date">{{ shortDate(news.createTime) }}</span>
            </div>
            <h3 class="news-title">{{ news.title }}</h3>
            <p class="news-desc clamp-2">{{ news.description }}</p>
            <div class="news-stats">
              <span><uni-icons type="eye" size="12" color="currentColor" />{{ compact(news.viewCount) }}</span>
              <span><uni-icons type="heart" size="12" color="currentColor" />{{ compact(news.likeCount) }}</span>
              <span><uni-icons type="chat" size="12" color="currentColor" />{{ compact(news.commentCount) }}</span>
              <span><uni-icons type="star" size="12" color="currentColor" />{{ compact(news.collectCount) }}</span>
            </div>
          </div>
        </article>
      </div>

      <!-- 空状态 -->
      <div v-else class="empty-state">
        <div class="empty-state__icon"><uni-icons type="list" size="26" color="#1E5B48" /></div>
        {{ searchQuery ? t('news.emptySearch') : t('news.empty') }}
      </div>

      <!-- 分页 -->
      <div v-if="!loading && newsList.length > 0" class="pager">
        <button class="pager-btn" :disabled="currentPage === 1" @click="changePage(currentPage - 1)">{{ t('common.prev') }}</button>
        <span class="pager-info">{{ currentPage }} / {{ totalPages }}</span>
        <button class="pager-btn" :disabled="currentPage === totalPages" @click="changePage(currentPage + 1)">{{ t('common.next') }}</button>
      </div>
    </div>

    <TabBar current="news" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getNewsList } from '@/api/news'
import UniIcons from '@/uni_modules/uni-icons/components/uni-icons/uni-icons.vue'
import TabBar from '@/components/tabbar/index.vue'
import { t, usePageTitle } from '@/utils/i18n'

usePageTitle('page.news')

const searchQuery = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const totalPages = ref(1)
const newsList = ref([])
const loading = ref(true)

const calculateTotalPages = (total) => Math.ceil(total / pageSize.value) || 1

const compact = (value) => {
  const num = Number(value) || 0
  if (num >= 10000) return (num / 10000).toFixed(1).replace('.0', '') + 'w'
  if (num >= 1000) return (num / 1000).toFixed(1).replace('.0', '') + 'k'
  return String(num)
}

const shortDate = (value) => (value ? String(value).replace('T', ' ').slice(0, 10) : '')

const fetchNewsList = async () => {
  loading.value = true
  try {
    const response = await getNewsList({
      page: currentPage.value,
      pageSize: pageSize.value,
      keyword: searchQuery.value
    })
    newsList.value = response.data.records || []
    totalPages.value = calculateTotalPages(response.data.total || 0)
  } catch (error) {
    console.error('获取新闻列表失败:', error)
    newsList.value = []
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchNewsList()
  uni.pageScrollTo({ scrollTop: 0, duration: 240 })
}

const navigateToDetail = (id) => {
  uni.navigateTo({ url: `/pages/news/detail?id=${id}` })
}

const changePage = (page) => {
  if (page < 1 || page > totalPages.value) return
  currentPage.value = page
  fetchNewsList()
  uni.pageScrollTo({ scrollTop: 0, duration: 240 })
}

onMounted(fetchNewsList)
</script>

<style scoped>
/* 页头 */
.news-head {
  position: relative;
  overflow: hidden;
  padding: 24px 22px;
  border-radius: var(--radius-xl);
  background: var(--card-background);
  border: 1px solid var(--card-border);
  box-shadow: var(--shadow-sm);
}

.news-head__glow {
  position: absolute;
  top: -80px;
  right: -40px;
  width: 220px;
  height: 220px;
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
}

/* 搜索 */
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

/* 列表 */
.news-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 16px;
}

.news-card {
  display: flex;
  gap: 14px;
  padding: 14px;
  background: var(--card-background);
  border: 1px solid var(--card-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  cursor: pointer;
  transition: transform 0.35s var(--ease-out), box-shadow 0.35s var(--ease-out),
    border-color 0.35s var(--ease-out);
}

.news-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-md);
  border-color: var(--border-strong);
}

.news-thumb {
  position: relative;
  flex: 0 0 112px;
  height: 92px;
  border-radius: var(--radius-md);
  overflow: hidden;
  background: var(--surface-2);
}

.news-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.6s var(--ease-out);
}

.news-card:hover .news-thumb img {
  transform: scale(1.06);
}

.featured-flag {
  position: absolute;
  top: 8px;
  left: 8px;
  padding: 3px 8px;
  border-radius: var(--radius-pill);
  background: var(--accent-color);
  color: #fff;
  font-size: 10px;
  font-weight: 700;
  box-shadow: 0 4px 10px rgba(196, 97, 60, 0.32);
}

.news-text {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.news-top {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.news-source {
  padding: 2px 8px;
  border-radius: var(--radius-pill);
  background: var(--primary-soft);
  color: var(--primary-color);
  font-size: 10px;
  font-weight: 700;
}

.news-date {
  font-size: 11px;
  color: var(--text-tertiary-color);
  font-variant-numeric: tabular-nums;
}

.news-title {
  font-size: 15px;
  font-weight: 700;
  line-height: 1.42;
  color: var(--text-color);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.news-desc {
  margin-top: 5px;
  font-size: 12px;
  line-height: 1.55;
  color: var(--text-secondary-color);
}

.news-stats {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-top: auto;
  padding-top: 9px;
  font-size: 11px;
  color: var(--text-tertiary-color);
}

.news-stats span {
  display: inline-flex;
  align-items: center;
  gap: 3px;
}

/* 骨架 */
.skeleton-line {
  height: 10px;
  margin-top: 8px;
  width: 70%;
}

.skeleton-line--lg {
  width: 96%;
  height: 15px;
}

.skeleton-line--sm {
  width: 38%;
}

.news-thumb-skeleton {
  flex: 0 0 112px;
  height: 92px;
}

/* PC */
@media (min-width: 768px) {
  .news-head {
    padding: 34px 38px;
  }

  .page-title {
    font-size: 34px;
  }

  .news-list {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 16px;
  }

  /* 头条跨两列，图片在左、信息在右 */
  .news-card--featured {
    grid-column: 1 / -1;
    padding: 18px;
  }

  .news-card--featured .news-thumb {
    flex-basis: 340px;
    height: 210px;
  }

  .news-card--featured .news-title {
    font-size: 22px;
    line-height: 1.35;
  }

  .news-card--featured .news-desc {
    font-size: 14px;
    -webkit-line-clamp: 3;
  }

  .news-thumb {
    flex-basis: 140px;
    height: 104px;
  }

  .news-title {
    font-size: 16px;
  }

  .news-thumb-skeleton {
    flex-basis: 140px;
    height: 104px;
  }
}
</style>
