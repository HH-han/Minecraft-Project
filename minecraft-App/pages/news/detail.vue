<template>
  <div class="app-page app-page--plain">
    <div class="shell article-shell">
      <article class="article">
        <div class="article-top">
          <span v-if="news.source" class="source-chip">{{ news.source }}</span>
          <span class="article-date">{{ shortDate(news.createTime) }}</span>
        </div>

        <h1 class="article-title">{{ news.title || t('common.loading') }}</h1>

        <div class="article-meta">
          <span><uni-icons type="eye" size="13" color="currentColor" />{{ news.viewCount || 0 }} {{ t('newsDetail.views') }}</span>
          <span><uni-icons type="heart" size="13" color="currentColor" />{{ news.likeCount || 0 }} {{ t('newsDetail.likes') }}</span>
          <span><uni-icons type="chat" size="13" color="currentColor" />{{ news.commentCount || 0 }} {{ t('newsDetail.comments') }}</span>
          <span class="read-time">{{ t('common.minutesRead', { n: readingTime }) }}</span>
        </div>

        <div v-if="news.coverImage" class="article-cover">
          <img :src="news.coverImage" :alt="news.title">
        </div>

        <div class="article-body">
          <p v-for="(paragraph, index) in paragraphs" :key="index">{{ paragraph }}</p>
          <p v-if="!paragraphs.length" class="article-empty">{{ t('newsDetail.noContent') }}</p>
        </div>

        <div v-if="news.description" class="article-summary">
          <div class="summary-label">{{ t('newsDetail.summary') }}</div>
          <p>{{ news.description }}</p>
        </div>

        <div class="article-actions">
          <button class="pill-action" :class="{ 'is-active': liked }" @click="liked = !liked">
            <uni-icons :type="liked ? 'heart-filled' : 'heart'" size="16" color="currentColor" />
            {{ liked ? t('newsDetail.liked') : t('newsDetail.like') }}
          </button>
          <button class="pill-action" :class="{ 'is-active-gold': collected }" @click="collected = !collected">
            <uni-icons :type="collected ? 'star-filled' : 'star'" size="16" color="currentColor" />
            {{ collected ? t('common.saved') : t('common.save') }}
          </button>
        </div>
      </article>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { getNewsDetail } from '@/api/news'
import UniIcons from '@/uni_modules/uni-icons/components/uni-icons/uni-icons.vue'
import { t, usePageTitle } from '@/utils/i18n'

usePageTitle('page.newsDetail')

const news = ref({})
const liked = ref(false)
const collected = ref(false)

const shortDate = (value) => (value ? String(value).replace('T', ' ').slice(0, 16) : '')

const paragraphs = computed(() => {
  const content = news.value.content || ''
  return String(content)
    .split(/\n+/)
    .map((item) => item.trim())
    .filter(Boolean)
})

const readingTime = computed(() => {
  const length = (news.value.content || '').length
  return Math.max(1, Math.ceil(length / 400))
})

onMounted(async () => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const { id } = currentPage.options || {}
  if (id) {
    try {
      const response = await getNewsDetail(id)
      news.value = response.data || {}
    } catch (error) {
      console.error('获取新闻详情失败:', error)
    }
  }
})
</script>

<style scoped>
.article-shell {
  max-width: 860px;
}

.article {
  padding: 20px 18px 26px;
  background: var(--card-background);
  border: 1px solid var(--card-border);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-sm);
}

.article-top {
  display: flex;
  align-items: center;
  gap: 10px;
}

.source-chip {
  padding: 3px 10px;
  border-radius: var(--radius-pill);
  background: var(--primary-soft);
  color: var(--primary-color);
  font-size: 11px;
  font-weight: 700;
}

.article-date {
  font-size: 12px;
  color: var(--text-tertiary-color);
  font-variant-numeric: tabular-nums;
}

.article-title {
  margin-top: 12px;
  font-size: 24px;
  font-weight: 800;
  line-height: 1.35;
  letter-spacing: -0.4px;
  color: var(--text-color);
}

.article-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 14px;
  margin-top: 12px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border-color);
  font-size: 12px;
  color: var(--text-tertiary-color);
}

.article-meta span {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.read-time {
  margin-left: auto;
  padding: 2px 9px;
  border-radius: var(--radius-pill);
  background: var(--surface-2);
  font-size: 11px;
}

.article-cover {
  margin-top: 16px;
  border-radius: var(--radius-lg);
  overflow: hidden;
  background: var(--surface-2);
}

.article-cover img {
  width: 100%;
  max-height: 420px;
  object-fit: cover;
}

.article-body {
  margin-top: 18px;
  font-size: 15px;
  line-height: 1.9;
  color: var(--text-color);
}

.article-body p {
  margin-bottom: 14px;
  white-space: pre-wrap;
  word-break: break-word;
}

.article-empty {
  color: var(--text-tertiary-color);
  font-size: 14px;
}

.article-summary {
  margin-top: 8px;
  padding: 14px 16px;
  border-radius: var(--radius-md);
  background: var(--surface-2);
  border-left: 4px solid var(--primary-color);
}

.summary-label {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.6px;
  color: var(--primary-color);
}

.article-summary p {
  margin-top: 6px;
  font-size: 13px;
  line-height: 1.7;
  color: var(--text-secondary-color);
}

.article-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 24px;
}

.pill-action {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 22px;
  border-radius: var(--radius-pill);
  background: var(--card-background);
  border: 1px solid var(--border-color);
  color: var(--text-secondary-color);
  font-size: 13px;
  font-weight: 600;
  transition: all 0.28s var(--ease-out);
}

.pill-action:hover {
  border-color: var(--primary-color);
  color: var(--primary-color);
}

.pill-action.is-active {
  background: rgba(255, 90, 60, 0.1);
  border-color: rgba(255, 90, 60, 0.4);
  color: #C4613C;
}

.pill-action.is-active-gold {
  background: rgba(192, 138, 46, 0.12);
  border-color: rgba(192, 138, 46, 0.45);
  color: var(--gold-color);
}

@media (min-width: 768px) {
  .article {
    padding: 36px 44px 40px;
  }

  .article-title {
    font-size: 32px;
  }

  .article-body {
    font-size: 16px;
  }
}
</style>
