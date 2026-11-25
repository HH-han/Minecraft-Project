<template>
  <view class="note-page">
    <!-- 顶部 Tab -->
    <view class="tabs">
      <view
        v-for="tab in tabs"
        :key="tab.value"
        class="tab-item"
        :class="{ active: query.tab === tab.value }"
        @click="switchTab(tab.value)"
      >
        {{ t(tab.labelKey) }}
      </view>
    </view>

    <!-- 搜索 -->
    <view class="search-bar">
      <input
        v-model="keyword"
        class="search-input"
        type="text"
        :placeholder="t('note.searchPlaceholder')"
        confirm-type="search"
        @confirm="handleSearch"
      />
      <button class="search-btn" @click="handleSearch">{{ t('common.search') }}</button>
    </view>

    <!-- 热门标签 -->
    <scroll-view v-if="hotTags.length" class="tag-scroll" scroll-x>
      <view class="tag-list">
        <view
          v-for="tag in hotTags"
          :key="tag.id"
          class="tag-item"
          :class="{ active: query.tagId === tag.id }"
          @click="toggleTag(tag)"
        >
          # {{ tag.name }}
        </view>
      </view>
    </scroll-view>

    <!-- 笔记列表 -->
    <view class="note-list">
      <view v-for="note in notes" :key="note.id" class="note-item" @click="goDetail(note.id)">
        <image v-if="note.coverUrl" class="note-cover" :src="note.coverUrl" mode="aspectFill" />
        <view v-else class="note-cover note-cover--empty">{{ t('note.noCover') }}</view>

        <view class="note-body">
          <text class="note-title">{{ note.title }}</text>
          <text class="note-summary">{{ note.summary || t('note.noSummary') }}</text>
          <view class="note-meta">
            <text class="note-dest">{{ note.destination || t('note.noDestination') }}</text>
            <text v-if="note.travelDays" class="note-days">{{ t('note.days', { n: note.travelDays }) }}</text>
          </view>
          <view class="note-tags">
            <text v-for="tag in (note.tags || []).slice(0, 3)" :key="tag" class="note-tag">#{{ tag }}</text>
          </view>
          <view class="note-footer">
            <text class="note-author">{{ note.username || t('note.anonymous') }}</text>
            <view class="note-stats">
              <view class="stat">
                <uni-icons type="eye" size="12" color="currentColor" />
                <text>{{ note.viewCount || 0 }}</text>
              </view>
              <view class="stat">
                <uni-icons type="heart" size="12" color="currentColor" />
                <text>{{ note.likeCount || 0 }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>
    </view>

    <view v-if="!loading && notes.length === 0" class="empty-tip">{{ t('note.empty') }}</view>
    <view v-if="notes.length" class="load-more" @click="loadMore">
      {{ noMore ? t('note.noMore') : t('note.loadMore') }}
    </view>

    <!-- 悬浮操作 -->
    <view class="float-actions">
      <view class="float-btn float-btn--secondary" @click="goMine">{{ t('note.mine') }}</view>
      <view class="float-btn float-btn--primary" @click="goEditor">+</view>
    </view>
  </view>
</template>

<script>
import { getHotTags, getNotePage } from '@/api/travelNote'
import UniIcons from '@/uni_modules/uni-icons/components/uni-icons/uni-icons.vue'
import { t } from '@/utils/i18n'

export default {
  components: { UniIcons },
  data() {
    return {
      tabs: [
        { labelKey: 'note.tabRecommend', value: 'recommend' },
        { labelKey: 'note.tabLatest', value: 'latest' },
        { labelKey: 'note.tabFollow', value: 'follow' },
        { labelKey: 'note.tabCity', value: 'city' }
      ],
      keyword: '',
      loading: false,
      noMore: false,
      notes: [],
      hotTags: [],
      query: {
        pageNum: 1,
        pageSize: 10,
        tab: 'recommend',
        keyword: '',
        tagId: null
      }
    }
  },
  onLoad() {
    this.fetchNotes()
    this.fetchHotTags()
  },
  onShow() {
    uni.setNavigationBarTitle({ title: this.t('page.travelNote') })
  },
  onPullDownRefresh() {
    this.query.pageNum = 1
    this.noMore = false
    this.fetchNotes().finally(() => uni.stopPullDownRefresh())
  },
  methods: {
    t,
    async fetchNotes() {
      this.loading = true
      try {
        const params = {}
        Object.keys(this.query).forEach((key) => {
          const value = this.query[key]
          if (value !== null && value !== undefined && value !== '') {
            params[key] = value
          }
        })
        const res = await getNotePage(params)
        if (res.code === 200) {
          const records = (res.data && res.data.records) || []
          this.notes = this.query.pageNum === 1 ? records : this.notes.concat(records)
          this.noMore = this.notes.length >= Number((res.data && res.data.total) || 0)
        }
      } catch (e) {
        console.error('获取笔记失败', e)
      } finally {
        this.loading = false
      }
    },
    async fetchHotTags() {
      try {
        const res = await getHotTags(12)
        if (res.code === 200) {
          this.hotTags = res.data || []
        }
      } catch (e) {
        console.error('获取标签失败', e)
      }
    },
    switchTab(tab) {
      this.query.tab = tab
      this.query.pageNum = 1
      this.noMore = false
      this.fetchNotes()
    },
    handleSearch() {
      this.query.keyword = this.keyword.trim()
      this.query.pageNum = 1
      this.noMore = false
      this.fetchNotes()
    },
    toggleTag(tag) {
      this.query.tagId = this.query.tagId === tag.id ? null : tag.id
      this.query.pageNum = 1
      this.noMore = false
      this.fetchNotes()
    },
    loadMore() {
      if (this.noMore) return
      this.query.pageNum += 1
      this.fetchNotes()
    },
    goDetail(id) {
      uni.navigateTo({ url: `/pages/travelNote/detail?id=${id}` })
    },
    goEditor() {
      uni.navigateTo({ url: '/pages/travelNote/editor' })
    },
    goMine() {
      uni.navigateTo({ url: '/pages/travelNote/mine' })
    }
  }
}
</script>

<style scoped>
.note-page {
  min-height: 100vh;
  background-color: var(--bg-color);
  padding: 24rpx;
  padding-bottom: calc(180rpx + env(safe-area-inset-bottom));
  position: relative;
}

/* Tab */
.tabs {
  display: inline-flex;
  gap: 8rpx;
  padding: 8rpx;
  border-radius: 999rpx;
  background: var(--card-background);
  border: 1rpx solid var(--card-border);
  box-shadow: var(--shadow-sm);
}

.tab-item {
  padding: 12rpx 28rpx;
  border-radius: 999rpx;
  font-size: 26rpx;
  color: var(--text-secondary-color);
  transition: all 0.25s var(--ease-out);
}

.tab-item.active {
  background: var(--primary-color);
  color: #fff;
  font-weight: 600;
  box-shadow: var(--shadow-brand);
}

/* 搜索 */
.search-bar {
  display: flex;
  gap: 14rpx;
  margin-top: 24rpx;
  margin-bottom: 20rpx;
}

.search-input {
  flex: 1;
  height: 84rpx;
  padding: 0 30rpx;
  background-color: var(--card-background);
  border: 1rpx solid var(--border-color);
  border-radius: 999rpx;
  font-size: 27rpx;
  color: var(--text-color);
}

.search-btn {
  height: 84rpx;
  padding: 0 34rpx;
  background: var(--primary-color);
  color: #fff;
  border: none;
  border-radius: 999rpx;
  font-size: 27rpx;
  font-weight: 600;
  box-shadow: var(--shadow-brand);
}

/* 标签 */
.tag-scroll {
  white-space: nowrap;
  margin-bottom: 20rpx;
}

.tag-list {
  display: inline-flex;
  gap: 14rpx;
}

.tag-item {
  padding: 10rpx 26rpx;
  background-color: var(--card-background);
  border: 1rpx solid var(--border-color);
  border-radius: 999rpx;
  font-size: 24rpx;
  color: var(--text-secondary-color);
  transition: all 0.25s var(--ease-out);
}

.tag-item.active {
  background: var(--primary-soft);
  border-color: transparent;
  color: var(--primary-color);
  font-weight: 600;
}

/* 列表 */
.note-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.note-item {
  display: flex;
  gap: 22rpx;
  background-color: var(--card-background);
  border: 1rpx solid var(--card-border);
  border-radius: 28rpx;
  padding: 22rpx;
  box-shadow: var(--shadow-sm);
  transition: transform 0.35s var(--ease-out), box-shadow 0.35s var(--ease-out);
}

.note-item:active {
  transform: scale(0.99);
}

.note-cover {
  width: 240rpx;
  height: 190rpx;
  border-radius: 20rpx;
  flex-shrink: 0;
  background-color: var(--surface-2);
}

.note-cover--empty {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  color: var(--text-tertiary-color);
}

.note-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.note-title {
  font-size: 30rpx;
  font-weight: 700;
  color: var(--text-color);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.note-summary {
  margin-top: 8rpx;
  font-size: 24rpx;
  line-height: 1.6;
  color: var(--text-secondary-color);
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.note-meta {
  margin-top: 10rpx;
  display: flex;
  gap: 14rpx;
  font-size: 22rpx;
  color: var(--text-tertiary-color);
}

.note-dest,
.note-days {
  padding: 2rpx 14rpx;
  border-radius: 999rpx;
  background: var(--primary-soft);
  color: var(--primary-color);
}

.note-tags {
  margin-top: 10rpx;
  display: flex;
  gap: 12rpx;
  overflow: hidden;
}

.note-tag {
  font-size: 22rpx;
  color: var(--primary-color);
  white-space: nowrap;
}

.note-footer {
  margin-top: auto;
  padding-top: 12rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1rpx dashed var(--border-color);
}

.note-author {
  font-size: 22rpx;
  color: var(--text-tertiary-color);
}

.note-stats {
  display: flex;
  gap: 18rpx;
}

.stat {
  display: inline-flex;
  align-items: center;
  gap: 4rpx;
  font-size: 22rpx;
  color: var(--text-tertiary-color);
}

/* 空状态与加载 */
.empty-tip {
  text-align: center;
  padding: 100rpx 0;
  color: var(--text-tertiary-color);
  font-size: 26rpx;
}

.load-more {
  margin: 32rpx auto 0;
  width: 260rpx;
  text-align: center;
  padding: 20rpx 0;
  border-radius: 999rpx;
  background: var(--card-background);
  border: 1rpx solid var(--border-color);
  color: var(--primary-color);
  font-size: 26rpx;
  font-weight: 600;
  box-shadow: var(--shadow-sm);
}

/* 悬浮操作 */
.float-actions {
  position: fixed;
  right: 36rpx;
  bottom: calc(150rpx + env(safe-area-inset-bottom));
  display: flex;
  flex-direction: column;
  gap: 20rpx;
  align-items: center;
  z-index: 80;
}

.float-btn {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 32rpx;
  box-shadow: var(--shadow-brand);
  transition: transform 0.25s var(--ease-spring);
}

.float-btn:active {
  transform: scale(0.92);
}

.float-btn--primary {
  background: var(--primary-color);
  font-size: 52rpx;
}

.float-btn--secondary {
  width: 88rpx;
  height: 88rpx;
  background: var(--card-background);
  border: 1rpx solid var(--border-color);
  color: var(--text-color);
  font-size: 24rpx;
  box-shadow: var(--shadow-md);
}
</style>
