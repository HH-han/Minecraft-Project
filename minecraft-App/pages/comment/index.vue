<template>
  <div class="app-page app-page--plain">
    <div class="shell">
      <header class="simple-head">
        <div class="page-eyebrow">{{ t('comment.eyebrow') }}</div>
        <h1 class="page-title">{{ t('comment.title') }}</h1>
        <p class="page-sub">{{ t('comment.sub') }}</p>
      </header>

      <div v-if="comments.length" class="comment-list">
        <article v-for="item in comments" :key="item.id" class="comment-card">
          <div class="comment-head">
            <div class="user-avatar">
              <img :src="item.avatar" :alt="item.userName">
            </div>
            <div class="user-info">
              <h3 class="user-name">{{ t('profile.guestName') }}</h3>
              <p class="comment-time">{{ item.time }}</p>
            </div>
            <div class="rating">
              <uni-icons v-for="i in 5" :key="i" :type="i <= item.rating ? 'star-filled' : 'star'" size="13"
                :color="i <= item.rating ? '#C08A2E' : '#D6CFC0'" />
            </div>
          </div>

          <p class="comment-text">{{ t(item.contentKey) }}</p>

          <div class="comment-target">
            <span class="target-label">{{ t('comment.target') }}</span>
            <span class="target-name">{{ t(item.targetKey) }}</span>
          </div>

          <div class="comment-actions">
            <button class="btn btn--ghost btn--sm" @click="handleEdit">{{ t('comment.edit') }}</button>
            <button class="btn btn--danger btn--sm" @click="removeComment(item.id)">{{ t('comment.delete') }}</button>
          </div>
        </article>
      </div>

      <div v-else class="empty-state">
        <div class="empty-state__icon"><uni-icons type="chatbubble" size="26" color="#1E5B48" /></div>
        {{ t('comment.empty') }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import UniIcons from '@/uni_modules/uni-icons/components/uni-icons/uni-icons.vue'
import { t, usePageTitle } from '@/utils/i18n'

usePageTitle('page.comment')

const avatar = 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=user%20avatar%20portrait&image_size=square'

const comments = ref([
  {
    id: 1,
    avatar,
    time: '2026-03-28 14:30',
    rating: 5,
    contentKey: 'comment.item1',
    targetKey: 'comment.item1Target'
  },
  {
    id: 2,
    avatar,
    time: '2026-03-27 10:15',
    rating: 4,
    contentKey: 'comment.item2',
    targetKey: 'comment.item2Target'
  },
  {
    id: 3,
    avatar,
    time: '2026-03-26 18:45',
    rating: 5,
    contentKey: 'comment.item3',
    targetKey: 'comment.item3Target'
  }
])

const handleEdit = () => {
  uni.showToast({ title: t('common.building', { name: t('comment.edit') }), icon: 'none', position: 'bottom' })
}

const removeComment = (id) => {
  uni.showModal({
    title: t('comment.deleteTitle'),
    content: t('comment.deleteContent'),
    success: (res) => {
      if (res.confirm) {
        comments.value = comments.value.filter((item) => item.id !== id)
        uni.showToast({ title: t('comment.deleted'), icon: 'none', position: 'bottom' })
      }
    }
  })
}
</script>

<style scoped>
.simple-head {
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
  color: var(--text-secondary-color);
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
  margin-top: 18px;
}

.comment-card {
  padding: 16px;
  background: var(--card-background);
  border: 1px solid var(--card-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  transition: transform 0.35s var(--ease-out), box-shadow 0.35s var(--ease-out);
}

.comment-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-md);
}

.comment-head {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-avatar {
  flex: 0 0 40px;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  background: var(--surface-2);
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-info {
  flex: 1;
  min-width: 0;
}

.user-name {
  font-size: 14px;
  font-weight: 700;
  color: var(--text-color);
}

.comment-time {
  margin-top: 2px;
  font-size: 11px;
  color: var(--text-tertiary-color);
}

.rating {
  display: flex;
  align-items: center;
  gap: 1px;
}

.comment-text {
  margin-top: 12px;
  font-size: 14px;
  line-height: 1.7;
  color: var(--text-color);
}

.comment-target {
  display: flex;
  align-items: center;
  gap: 7px;
  margin-top: 12px;
  padding: 8px 11px;
  border-radius: var(--radius-sm);
  background: var(--surface-2);
}

.target-label {
  font-size: 11px;
  color: var(--text-tertiary-color);
}

.target-name {
  font-size: 12px;
  font-weight: 600;
  color: var(--primary-color);
}

.comment-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 12px;
}

.btn--sm {
  padding: 7px 15px;
  font-size: 12px;
}

.btn--danger {
  background: rgba(240, 68, 56, 0.08);
  border: 1px solid rgba(240, 68, 56, 0.24);
  color: #F04438;
}

.btn--danger:hover {
  background: rgba(240, 68, 56, 0.14);
}

@media (min-width: 768px) {
  .page-title {
    font-size: 32px;
  }

  .comment-list {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 18px;
  }
}
</style>
