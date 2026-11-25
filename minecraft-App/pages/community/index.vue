<template>
  <div class="app-page community-page">
    <div class="shell">

      <!-- 页头 -->
      <header class="community-head reveal">
        <div class="community-head__glow"></div>
        <div class="community-head__text">
          <div class="page-eyebrow">{{ t('community.eyebrow') }}</div>
          <h1 class="page-title">{{ t('community.title') }}</h1>
          <p class="page-sub">{{ t('community.sub') }}</p>
        </div>
        <button class="compose-btn" @click="createPost">
          <uni-icons type="compose" size="16" color="#FFFFFF" />
          {{ t('community.compose') }}
        </button>
      </header>

      <!-- 搜索 -->
      <div class="search-field search-row reveal reveal-delay-1">
        <uni-icons type="search" size="17" color="#8C948B" />
        <input v-model="searchQuery" type="text" :placeholder="t('community.searchPlaceholder')" confirm-type="search"
          @confirm="handleSearch" />
        <button class="search-submit" @click="handleSearch">{{ t('common.search') }}</button>
      </div>

      <!-- 骨架 -->
      <div v-if="loading" class="post-list">
        <div v-for="i in 3" :key="i" class="post-card">
          <div class="post-author">
            <div class="skeleton avatar-skeleton"></div>
            <div class="author-text">
              <div class="skeleton skeleton-line skeleton-line--sm"></div>
              <div class="skeleton skeleton-line skeleton-line--xs"></div>
            </div>
          </div>
          <div class="skeleton skeleton-line skeleton-line--lg"></div>
          <div class="skeleton skeleton-line"></div>
          <div class="skeleton post-image-skeleton"></div>
        </div>
      </div>

      <!-- 帖子列表 -->
      <div v-else-if="posts.length" class="post-list">
        <article v-for="post in posts" :key="post.id" class="post-card" @click="navigateToDetail(post.id)">
          <div class="post-author">
            <div class="author-avatar">
              <img :src="post.userAvatar || defaultAvatar" :alt="post.userName">
            </div>
            <div class="author-text">
              <h4 class="author-name">{{ post.userName }}</h4>
              <p class="post-time">{{ shortDate(post.createTime) }}</p>
            </div>
            <span v-if="post.tag" class="tag-pill">{{ post.tag }}</span>
          </div>

          <div class="post-content">
            <h3 class="post-title">{{ post.title }}</h3>
            <p class="post-desc clamp-2">{{ post.content }}</p>
          </div>

          <div v-if="post.images && post.images.length > 0" class="post-image">
            <img :src="post.images[0]" :alt="post.title" loading="lazy">
          </div>

          <div class="post-footer">
            <span class="stat"><uni-icons type="heart" size="14" color="currentColor" />{{ compact(post.likeCount) }}</span>
            <span class="stat"><uni-icons type="chat" size="14" color="currentColor" />{{ compact(post.commentCount) }}</span>
            <span class="stat"><uni-icons type="eye" size="14" color="currentColor" />{{ compact(post.viewCount) }}</span>
            <span class="stat stat--more"><uni-icons type="more-filled" size="15" color="currentColor" /></span>
          </div>
        </article>
      </div>

      <!-- 空状态 -->
      <div v-else class="empty-state">
        <div class="empty-state__icon"><uni-icons type="chatboxes" size="26" color="#1E5B48" /></div>
        {{ searchQuery ? t('community.emptySearch') : t('community.empty') }}
      </div>

      <!-- 分页 -->
      <div v-if="!loading && posts.length > 0" class="pager">
        <button class="pager-btn" :disabled="currentPage === 1" @click="changePage(currentPage - 1)">{{ t('common.prev') }}</button>
        <span class="pager-info">{{ currentPage }} / {{ totalPages }}</span>
        <button class="pager-btn" :disabled="currentPage === totalPages" @click="changePage(currentPage + 1)">{{ t('common.next') }}</button>
      </div>
    </div>

    <!-- 发布按钮 -->
    <button class="fab" @click="createPost" aria-label="发布帖子">
      <uni-icons type="plusempty" size="24" color="#FFFFFF" />
    </button>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getPostList } from '@/api/community'
import UniIcons from '@/uni_modules/uni-icons/components/uni-icons/uni-icons.vue'
import { t, usePageTitle } from '@/utils/i18n'

usePageTitle('page.community')

const defaultAvatar = 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=user%20avatar%20portrait&image_size=square'

const searchQuery = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const totalPages = ref(1)
const posts = ref([])
const loading = ref(true)

const calculateTotalPages = (total) => Math.ceil(total / pageSize.value) || 1

const compact = (value) => {
  const num = Number(value) || 0
  if (num >= 10000) return (num / 10000).toFixed(1).replace('.0', '') + 'w'
  if (num >= 1000) return (num / 1000).toFixed(1).replace('.0', '') + 'k'
  return String(num)
}

const shortDate = (value) => (value ? String(value).replace('T', ' ').slice(0, 16) : '')

const fetchPosts = async () => {
  loading.value = true
  try {
    const response = await getPostList({
      page: currentPage.value,
      pageSize: pageSize.value,
      keyword: searchQuery.value
    })
    posts.value = response.data.records || []
    totalPages.value = calculateTotalPages(response.data.total || 0)
  } catch (error) {
    console.error('获取帖子列表失败:', error)
    posts.value = []
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchPosts()
  uni.pageScrollTo({ scrollTop: 0, duration: 240 })
}

const navigateToDetail = (id) => {
  uni.navigateTo({ url: `/pages/community/detail?id=${id}` })
}

const changePage = (page) => {
  if (page < 1 || page > totalPages.value) return
  currentPage.value = page
  fetchPosts()
  uni.pageScrollTo({ scrollTop: 0, duration: 240 })
}

const createPost = () => {
  uni.navigateTo({ url: '/pages/community/create' })
}

onMounted(fetchPosts)
</script>

<style scoped>
.community-page {
  position: relative;
}

/* 页头 */
.community-head {
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 24px 22px;
  border-radius: var(--radius-xl);
  background: var(--primary-color);
  box-shadow: var(--shadow-brand);
  color: #fff;
}

.community-head__glow {
  position: absolute;
  right: -40px;
  top: -70px;
  width: 220px;
  height: 220px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.14);
  filter: blur(40px);
  pointer-events: none;
}

.community-head__text {
  position: relative;
  z-index: 1;
}

.page-eyebrow {
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 1.5px;
  color: rgba(255, 255, 255, 0.78);
}

.page-title {
  margin-top: 7px;
  font-size: 26px;
  font-weight: 800;
  letter-spacing: -0.5px;
}

.page-sub {
  margin-top: 5px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.82);
}

.compose-btn {
  position: relative;
  z-index: 1;
  flex: 0 0 auto;
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 10px 16px;
  border-radius: var(--radius-pill);
  background: rgba(255, 255, 255, 0.18);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: #fff;
  font-size: 13px;
  font-weight: 700;
  -webkit-backdrop-filter: blur(8px);
  backdrop-filter: blur(8px);
  transition: all 0.3s var(--ease-out);
}

.compose-btn:hover {
  background: rgba(255, 255, 255, 0.28);
  transform: translateY(-1px);
}

.compose-btn:active {
  transform: scale(0.96);
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
.post-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
  margin-top: 16px;
}

.post-card {
  background: var(--card-background);
  border: 1px solid var(--card-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  padding: 16px;
  cursor: pointer;
  transition: transform 0.35s var(--ease-out), box-shadow 0.35s var(--ease-out),
    border-color 0.35s var(--ease-out);
}

.post-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-md);
  border-color: var(--border-strong);
}

.post-author {
  display: flex;
  align-items: center;
  gap: 10px;
}

.author-avatar {
  flex: 0 0 42px;
  width: 42px;
  height: 42px;
  border-radius: 50%;
  overflow: hidden;
  background: var(--surface-2);
  border: 2px solid var(--card-background);
  box-shadow: 0 0 0 1px var(--border-color);
}

.author-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.author-text {
  flex: 1;
  min-width: 0;
}

.author-name {
  font-size: 14px;
  font-weight: 700;
  color: var(--text-color);
}

.post-time {
  margin-top: 2px;
  font-size: 11px;
  color: var(--text-tertiary-color);
  font-variant-numeric: tabular-nums;
}

.post-content {
  margin-top: 12px;
}

.post-title {
  font-size: 17px;
  font-weight: 700;
  line-height: 1.4;
  color: var(--text-color);
}

.post-desc {
  margin-top: 6px;
  font-size: 13px;
  line-height: 1.6;
  color: var(--text-secondary-color);
}

.post-image {
  margin-top: 12px;
  border-radius: var(--radius-md);
  overflow: hidden;
  background: var(--surface-2);
  max-height: 300px;
}

.post-image img {
  width: 100%;
  height: 100%;
  max-height: 300px;
  object-fit: cover;
  transition: transform 0.6s var(--ease-out);
}

.post-card:hover .post-image img {
  transform: scale(1.04);
}

.post-footer {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-top: 14px;
  padding-top: 12px;
  border-top: 1px solid var(--border-color);
}

.stat {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  font-weight: 500;
  color: var(--text-tertiary-color);
  transition: color 0.25s var(--ease-out);
}

.post-card:hover .stat {
  color: var(--primary-color);
}

.stat--more {
  margin-left: auto;
}

/* 悬浮按钮 */
.fab {
  position: fixed;
  right: 20px;
  bottom: calc(78px + env(safe-area-inset-bottom));
  z-index: 90;
  width: 54px;
  height: 54px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: var(--primary-color);
  box-shadow: var(--shadow-brand);
  transition: transform 0.3s var(--ease-spring), box-shadow 0.3s var(--ease-out);
}

.fab:hover {
  transform: translateY(-3px) scale(1.05);
}

.fab:active {
  transform: scale(0.92);
}

/* 骨架 */
.avatar-skeleton {
  width: 42px;
  height: 42px;
  border-radius: 50%;
}

.skeleton-line {
  height: 10px;
  margin-top: 8px;
  width: 72%;
}

.skeleton-line--lg {
  width: 90%;
  height: 14px;
}

.skeleton-line--sm {
  width: 42%;
}

.skeleton-line--xs {
  width: 26%;
}

.post-image-skeleton {
  height: 150px;
  margin-top: 12px;
}

/* PC */
@media (min-width: 768px) {
  .community-head {
    padding: 34px 38px;
  }

  .page-title {
    font-size: 34px;
  }

  .post-list {
    gap: 18px;
  }

  .post-card {
    padding: 20px 22px;
  }

  .post-title {
    font-size: 19px;
  }
}
</style>
