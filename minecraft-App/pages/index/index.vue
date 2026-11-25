<template>
  <div class="app-page">
    <div class="shell">

      <!-- Hero -->
      <header class="hero reveal">
        <div class="hero-glow"></div>
        <div class="hero-content">
          <div class="hero-eyebrow">
            <span class="hero-dot"></span>
            {{ t('home.eyebrow') }}
          </div>
          <h1 class="hero-title">{{ t('home.titleLine1') }}<br /><span>{{ t('home.titleLine2') }}</span></h1>
          <p class="hero-desc">{{ t('home.desc') }}</p>
        </div>
        <div class="hero-ring hero-ring--1"></div>
        <div class="hero-ring hero-ring--2"></div>
      </header>

      <!-- 轮播图 -->
      <div class="hero-visual reveal reveal-delay-1">
        <Carousel :items="carouselList" :interval="4000" :transitionDuration="600" :showArrows="true"
          :showIndicators="true" :aspectRatio="'16/9'" />
      </div>

      <!-- 功能导航 -->
      <Index class="reveal reveal-delay-2" />

      <!-- 推荐景点 -->
      <section class="block">
        <div class="section-head">
          <div class="section-title">{{ t('home.attractions') }}<span class="section-sub">{{ t('home.attractionsSub') }}</span></div>
          <button class="section-more" @click="navigateTo('/pages/attraction/index')">
            {{ t('common.viewAll') }}
            <uni-icons type="right" size="12" color="currentColor" />
          </button>
        </div>

        <div v-if="loading" class="card-row">
          <div v-for="i in 4" :key="i" class="trip-card trip-card--skeleton">
            <div class="skeleton skeleton-media"></div>
            <div class="trip-body">
              <div class="skeleton skeleton-line skeleton-line--lg"></div>
              <div class="skeleton skeleton-line"></div>
            </div>
          </div>
        </div>

        <div v-else-if="recommendAttractions.length" class="card-row">
          <article v-for="attraction in recommendAttractions" :key="attraction.id" class="trip-card"
            @click="navigateTo(`/pages/attraction/detail?id=${attraction.id}`)">
            <div class="trip-media">
              <img :src="attraction.coverImage" :alt="attraction.name" loading="lazy">
              <span v-if="attraction.season" class="trip-badge">{{ attraction.season }}</span>
              <span v-if="attraction.rating" class="trip-rating">
                <uni-icons type="star-filled" size="11" color="#FFFFFF" />
                {{ attraction.rating }}
              </span>
            </div>
            <div class="trip-body">
              <h3 class="trip-name">{{ attraction.name }}</h3>
              <div class="trip-meta">
                <uni-icons type="location" size="12" color="currentColor" />
                {{ attraction.city }}
              </div>
              <div class="trip-foot">
                <span v-if="attraction.price > 0" class="trip-price"><small>¥</small>{{ attraction.price }}</span>
                <span v-else class="trip-price is-free">{{ t('common.free') }}</span>
                <div class="trip-stats">
                  <span><uni-icons type="heart" size="12" color="currentColor" />{{ compact(attraction.likeCount) }}</span>
                  <span><uni-icons type="chat" size="12" color="currentColor" />{{ compact(attraction.commentCount) }}</span>
                </div>
              </div>
            </div>
          </article>
        </div>

        <div v-else class="empty-state">
          <div class="empty-state__icon"><uni-icons type="map" size="26" color="#1E5B48" /></div>
          {{ t('home.emptyAttractions') }}
        </div>
      </section>

      <!-- 推荐酒店 -->
      <section class="block">
        <div class="section-head">
          <div class="section-title">{{ t('home.hotels') }}<span class="section-sub">{{ t('home.hotelsSub') }}</span></div>
          <button class="section-more" @click="navigateTo('/pages/hotel/index')">
            {{ t('common.viewAll') }}
            <uni-icons type="right" size="12" color="currentColor" />
          </button>
        </div>

        <div v-if="loading" class="card-row">
          <div v-for="i in 4" :key="i" class="trip-card trip-card--skeleton">
            <div class="skeleton skeleton-media"></div>
            <div class="trip-body">
              <div class="skeleton skeleton-line skeleton-line--lg"></div>
              <div class="skeleton skeleton-line"></div>
            </div>
          </div>
        </div>

        <div v-else-if="recommendHotels.length" class="card-row">
          <article v-for="hotel in recommendHotels" :key="hotel.id" class="trip-card"
            @click="navigateTo(`/pages/hotel/detail?id=${hotel.id}`)">
            <div class="trip-media">
              <img :src="hotel.coverImage" :alt="hotel.name" loading="lazy">
              <span v-if="hotel.starLevel" class="trip-badge trip-badge--gold">
                <uni-icons type="medal-filled" size="11" color="#8A5A00" />
                {{ t('detail.starLevel', { n: hotel.starLevel }) }}
              </span>
            </div>
            <div class="trip-body">
              <h3 class="trip-name">{{ hotel.name }}</h3>
              <div class="trip-meta">
                <uni-icons type="location" size="12" color="currentColor" />
                {{ hotel.city }}
              </div>
              <div class="trip-foot">
                <span class="trip-price"><small>¥</small>{{ hotel.price }}<em>{{ t('common.perNight') }}</em></span>
                <div class="trip-stats">
                  <span><uni-icons type="heart" size="12" color="currentColor" />{{ compact(hotel.likeCount) }}</span>
                  <span><uni-icons type="star" size="12" color="currentColor" />{{ compact(hotel.collectCount) }}</span>
                </div>
              </div>
            </div>
          </article>
        </div>

        <div v-else class="empty-state">
          <div class="empty-state__icon"><uni-icons type="shop" size="26" color="#1E5B48" /></div>
          {{ t('home.emptyHotels') }}
        </div>
      </section>

      <!-- 推荐美食 -->
      <section class="block">
        <div class="section-head">
          <div class="section-title">{{ t('home.foods') }}<span class="section-sub">{{ t('home.foodsSub') }}</span></div>
          <button class="section-more" @click="navigateTo('/pages/food/index')">
            {{ t('common.viewAll') }}
            <uni-icons type="right" size="12" color="currentColor" />
          </button>
        </div>

        <div v-if="loading" class="card-row">
          <div v-for="i in 4" :key="i" class="trip-card trip-card--skeleton">
            <div class="skeleton skeleton-media"></div>
            <div class="trip-body">
              <div class="skeleton skeleton-line skeleton-line--lg"></div>
              <div class="skeleton skeleton-line"></div>
            </div>
          </div>
        </div>

        <div v-else-if="recommendFoods.length" class="card-row">
          <article v-for="food in recommendFoods" :key="food.id" class="trip-card"
            @click="navigateTo(`/pages/food/detail?id=${food.id}`)">
            <div class="trip-media">
              <img :src="food.coverImage" :alt="food.name" loading="lazy">
              <span v-if="food.cuisineType" class="trip-badge trip-badge--coral">{{ food.cuisineType }}</span>
              <span v-if="food.rating" class="trip-rating">
                <uni-icons type="star-filled" size="11" color="#FFFFFF" />
                {{ food.rating }}
              </span>
            </div>
            <div class="trip-body">
              <h3 class="trip-name">{{ food.name }}</h3>
              <div class="trip-meta">
                <uni-icons type="location" size="12" color="currentColor" />
                {{ food.city }}
              </div>
              <div class="trip-foot">
                <span class="trip-price"><small>¥</small>{{ food.price }}</span>
                <div class="trip-stats">
                  <span><uni-icons type="heart" size="12" color="currentColor" />{{ compact(food.likeCount) }}</span>
                  <span><uni-icons type="chat" size="12" color="currentColor" />{{ compact(food.commentCount) }}</span>
                </div>
              </div>
            </div>
          </article>
        </div>

        <div v-else class="empty-state">
          <div class="empty-state__icon"><uni-icons type="fire" size="26" color="#C4613C" /></div>
          {{ t('home.emptyFoods') }}
        </div>
      </section>

      <!-- 热门新闻 -->
      <section class="block">
        <div class="section-head">
          <div class="section-title">{{ t('home.news') }}<span class="section-sub">{{ t('home.newsSub') }}</span></div>
          <button class="section-more" @click="navigateTo('/pages/news/index')">
            {{ t('common.viewAll') }}
            <uni-icons type="right" size="12" color="currentColor" />
          </button>
        </div>

        <div v-if="loading" class="news-feed">
          <div v-for="i in 3" :key="i" class="news-card">
            <div class="news-text">
              <div class="skeleton skeleton-line skeleton-line--sm"></div>
              <div class="skeleton skeleton-line skeleton-line--lg"></div>
              <div class="skeleton skeleton-line"></div>
            </div>
            <div class="skeleton news-thumb-skeleton"></div>
          </div>
        </div>

        <div v-else-if="hotNews.length" class="news-feed">
          <article v-for="news in hotNews" :key="news.id" class="news-card"
            @click="navigateTo(`/pages/news/detail?id=${news.id}`)">
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
                <span><uni-icons type="star" size="12" color="currentColor" />{{ compact(news.collectCount) }}</span>
              </div>
            </div>
            <div v-if="news.coverImage" class="news-thumb">
              <img :src="news.coverImage" :alt="news.title" loading="lazy">
            </div>
          </article>
        </div>

        <div v-else class="empty-state">
          <div class="empty-state__icon"><uni-icons type="list" size="26" color="#1E5B48" /></div>
          {{ t('home.emptyNews') }}
        </div>
      </section>

    </div>

    <TabBar current="home" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onPullDownRefresh } from '@dcloudio/uni-app'
import { getHotAttractions } from '@/api/attraction'
import { getRecommendHotels } from '@/api/hotel'
import { getRecommendFoods } from '@/api/food'
import { getHotNews } from '@/api/news'
import carouselApi from '@/api/carousel'
import Carousel from '@/pages/carousel/Carousel.vue'
import Index from '@/components/index/index.vue'
import TabBar from '@/components/tabbar/index.vue'
import UniIcons from '@/uni_modules/uni-icons/components/uni-icons/uni-icons.vue'
import { t, usePageTitle } from '@/utils/i18n'

usePageTitle('page.home')

const carouselList = ref([])
const recommendAttractions = ref([])
const recommendHotels = ref([])
const recommendFoods = ref([])
const hotNews = ref([])
const loading = ref(true)

const navigateTo = (url) => {
  uni.navigateTo({ url })
}

// 数量压缩展示：1200 -> 1.2k
const compact = (value) => {
  const num = Number(value) || 0
  if (num >= 10000) return (num / 10000).toFixed(1).replace('.0', '') + 'w'
  if (num >= 1000) return (num / 1000).toFixed(1).replace('.0', '') + 'k'
  return String(num)
}

const shortDate = (value) => (value ? String(value).replace('T', ' ').slice(0, 10) : '')

const getCarouselData = async () => {
  try {
    const response = await carouselApi.getHomeTopCarousels()
    carouselList.value = response.data || []
  } catch (error) {
    console.error('获取轮播图数据失败:', error)
  }
}

const getRecommendAttractions = async () => {
  try {
    const response = await getHotAttractions(4)
    recommendAttractions.value = response.data || []
  } catch (error) {
    console.error('获取推荐景点失败:', error)
  }
}

const getRecommendHotelsList = async () => {
  try {
    const response = await getRecommendHotels('', 4)
    recommendHotels.value = response.data || []
  } catch (error) {
    console.error('获取推荐酒店失败:', error)
  }
}

const getRecommendFoodsList = async () => {
  try {
    const response = await getRecommendFoods('', 4)
    recommendFoods.value = response.data || []
  } catch (error) {
    console.error('获取推荐美食失败:', error)
  }
}

const getHotNewsList = async () => {
  try {
    const response = await getHotNews({ limit: 4 })
    hotNews.value = response.data || []
  } catch (error) {
    console.error('获取热门新闻失败:', error)
  }
}

const loadAll = async () => {
  await Promise.all([
    getCarouselData(),
    getRecommendAttractions(),
    getRecommendHotelsList(),
    getRecommendFoodsList(),
    getHotNewsList()
  ])
  loading.value = false
}

onMounted(loadAll)

onPullDownRefresh(async () => {
  await loadAll()
  uni.stopPullDownRefresh()
})
</script>

<style scoped>
/* ---------- Hero ---------- */
.hero {
  position: relative;
  overflow: hidden;
  margin: 2px 0 0;
  padding: 26px 22px 78px;
  border-radius: var(--radius-xl);
  background: var(--primary-color);
  box-shadow: var(--shadow-brand);
  color: #fff;
}

.hero-content {
  position: relative;
  z-index: 2;
}

.hero-eyebrow {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 5px 12px;
  border-radius: var(--radius-pill);
  background: rgba(255, 255, 255, 0.16);
  border: 1px solid rgba(255, 255, 255, 0.22);
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.4px;
  color: #fff;
}

.hero-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #7CF2C2;
  box-shadow: 0 0 0 3px rgba(124, 242, 194, 0.28);
}

.hero-title {
  margin-top: 14px;
  font-size: 30px;
  line-height: 1.22;
  font-weight: 800;
  letter-spacing: -0.6px;
}

.hero-title span {
  color: #F3E7CE;
}

.hero-desc {
  margin-top: 10px;
  font-size: 13px;
  line-height: 1.6;
  color: rgba(255, 255, 255, 0.82);
}

.hero-glow {
  position: absolute;
  top: -60px;
  right: -40px;
  width: 230px;
  height: 230px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.16);
  filter: blur(40px);
  pointer-events: none;
}

.hero-ring {
  position: absolute;
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: 50%;
  pointer-events: none;
}

.hero-ring--1 {
  width: 180px;
  height: 180px;
  right: -50px;
  bottom: -80px;
}

.hero-ring--2 {
  width: 110px;
  height: 110px;
  right: 20px;
  bottom: -46px;
}

/* ---------- 轮播 ---------- */
.hero-visual {
  position: relative;
  z-index: 3;
  margin: -62px 6px 0;
  border-radius: var(--radius-lg);
  overflow: hidden;
  background: var(--card-background);
  border: 4px solid var(--card-background);
  box-shadow: var(--shadow-lg);
}

/* ---------- 区块 ---------- */
.block {
  margin-top: 6px;
}

/* ---------- 卡片横向滚动 ---------- */
.card-row {
  display: flex;
  gap: 12px;
  overflow-x: auto;
  padding: 4px 2px 10px;
  scrollbar-width: none;
  -webkit-overflow-scrolling: touch;
}

.card-row::-webkit-scrollbar {
  display: none;
}

.trip-card {
  flex: 0 0 216px;
  background: var(--card-background);
  border: 1px solid var(--card-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.4s var(--ease-out), box-shadow 0.4s var(--ease-out),
    border-color 0.4s var(--ease-out);
}

.trip-card:hover {
  transform: translateY(-6px);
  box-shadow: var(--shadow-md);
  border-color: var(--border-strong);
}

.trip-card:active {
  transform: scale(0.985);
}

.trip-media {
  position: relative;
  height: 132px;
  overflow: hidden;
  background: var(--surface-2);
}

.trip-media img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.6s var(--ease-out);
}

.trip-card:hover .trip-media img {
  transform: scale(1.07);
}

.trip-media::after {
  content: '';
  position: absolute;
  inset: auto 0 0;
  height: 46%;
  background: linear-gradient(to top, rgba(6, 16, 33, 0.55), transparent);
  pointer-events: none;
}

.trip-badge {
  position: absolute;
  top: 10px;
  left: 10px;
  z-index: 2;
  display: inline-flex;
  align-items: center;
  gap: 3px;
  padding: 4px 9px;
  border-radius: var(--radius-pill);
  background: rgba(255, 255, 255, 0.9);
  -webkit-backdrop-filter: blur(6px);
  backdrop-filter: blur(6px);
  color: #2F7A55;
  font-size: 11px;
  font-weight: 700;
}

.trip-badge--gold {
  color: #8A5A00;
  background: rgba(255, 214, 130, 0.92);
}

.trip-badge--coral {
  color: #C2410C;
  background: rgba(255, 214, 200, 0.94);
}

.trip-rating {
  position: absolute;
  right: 10px;
  bottom: 10px;
  z-index: 2;
  display: inline-flex;
  align-items: center;
  gap: 3px;
  font-size: 12px;
  font-weight: 700;
  color: #fff;
  text-shadow: 0 1px 4px rgba(0, 0, 0, 0.35);
}

.trip-body {
  padding: 12px 13px 13px;
}

.trip-name {
  font-size: 15px;
  font-weight: 700;
  line-height: 1.35;
  color: var(--text-color);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.trip-meta {
  display: flex;
  align-items: center;
  gap: 3px;
  margin-top: 5px;
  font-size: 12px;
  color: var(--text-tertiary-color);
}

.trip-foot {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 8px;
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px dashed var(--border-color);
}

.trip-price {
  display: inline-flex;
  align-items: baseline;
  gap: 1px;
  color: #C4613C;
  font-size: 18px;
  font-weight: 800;
  font-variant-numeric: tabular-nums;
}

.trip-price small {
  font-size: 11px;
  font-weight: 700;
}

.trip-price em {
  font-style: normal;
  font-size: 11px;
  font-weight: 500;
  color: var(--text-tertiary-color);
  margin-left: 2px;
}

.trip-price.is-free {
  font-size: 14px;
  color: var(--success-color);
}

.trip-stats {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 11px;
  color: var(--text-tertiary-color);
}

.trip-stats span {
  display: inline-flex;
  align-items: center;
  gap: 3px;
}

/* ---------- 骨架 ---------- */
.trip-card--skeleton {
  pointer-events: none;
}

.skeleton-media {
  height: 132px;
  border-radius: 0;
}

.skeleton-line {
  height: 10px;
  margin-top: 9px;
  width: 62%;
}

.skeleton-line--lg {
  width: 100%;
  height: 13px;
}

.skeleton-line--sm {
  width: 34%;
}

/* ---------- 新闻 ---------- */
.news-feed {
  display: flex;
  flex-direction: column;
  gap: 12px;
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
  transition: transform 0.35s var(--ease-out), box-shadow 0.35s var(--ease-out);
}

.news-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-md);
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
  margin-bottom: 7px;
}

.news-source {
  padding: 2px 8px;
  border-radius: var(--radius-pill);
  background: var(--primary-soft);
  color: var(--primary-color);
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.2px;
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
  margin-top: 6px;
  font-size: 12px;
  line-height: 1.55;
  color: var(--text-secondary-color);
}

.news-stats {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-top: 10px;
  font-size: 11px;
  color: var(--text-tertiary-color);
}

.news-stats span {
  display: inline-flex;
  align-items: center;
  gap: 3px;
}

.news-thumb {
  flex: 0 0 112px;
  height: 86px;
  border-radius: var(--radius-sm);
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

.news-thumb-skeleton {
  flex: 0 0 112px;
  height: 86px;
}

/* ---------- PC 端 ---------- */
@media (min-width: 768px) {
  .hero {
    padding: 40px 44px 96px;
  }

  .hero-title {
    font-size: 42px;
  }

  .hero-desc {
    font-size: 15px;
  }

  .hero-visual {
    margin: -74px 16px 0;
  }

  /* 桌面端改为完整网格，避免横向滚动 */
  .card-row {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: 18px;
    overflow: visible;
    padding: 4px 0 8px;
  }

  .trip-card {
    flex: initial;
  }

  .trip-media {
    height: 168px;
  }

  .trip-name {
    font-size: 16px;
  }

  .news-feed {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 16px;
  }

  .news-thumb {
    flex-basis: 140px;
    height: 100px;
  }
}
</style>
