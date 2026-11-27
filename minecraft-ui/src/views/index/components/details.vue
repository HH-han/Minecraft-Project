<template>
  <div v-if="visible" class="modal-overlay" @click="closeModal">
    <div class="modal-content" role="dialog" aria-modal="true" :aria-label="title" @click.stop>
      <div class="modal-header">
        <h2 class="modal-title">{{ title }}</h2>
        <button class="close-button" :aria-label="$t('homeSections.details.close')" @click="closeModal">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.5">
            <line x1="18" y1="6" x2="6" y2="18" />
            <line x1="6" y1="6" x2="18" y2="18" />
          </svg>
        </button>
      </div>
      <div class="modal-body">
        <div v-if="loading" class="loading">{{ $t('homeSections.common.loading') }}</div>
        <div v-else-if="error" class="error">{{ error }}</div>
        <div v-else>
          <div v-if="type === 'news'" class="detail-content">
            <div class="detail-image">
              <img :src="data.coverImage || defaultNewsImage" :alt="data.title" />
            </div>
            <div class="detail-info">
              <h3 class="detail-title">{{ data.title }}</h3>
              <div class="detail-meta">
                <span class="detail-date">{{ formatDate(data.createTime) }}</span>
                <span class="detail-view">{{ $t('homeSections.details.readCount', { count: data.viewCount || 0 }) }}</span>
              </div>
              <div class="detail-text">{{ data.content }}</div>
            </div>
          </div>

          <div v-if="type === 'hotel'" class="detail-content">
            <div class="detail-image">
              <img :src="data.coverImage || defaultHotelImage" :alt="data.name" />
            </div>
            <div class="detail-info">
              <div class="info-header">
                <h3 class="detail-title">{{ data.name }}</h3>
                <span class="detail-price">¥{{ data.price }}<span class="price-unit">{{
                  $t('homeSections.details.perNight') }}</span></span>
              </div>
              <div class="detail-meta">
                <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="1.5">
                  <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z" />
                  <circle cx="12" cy="10" r="3" />
                </svg>
                <span>{{ data.city }} {{ data.address }}</span>
              </div>
              <div class="detail-text">{{ data.description }}</div>
            </div>
          </div>

          <div v-if="type === 'food'" class="detail-content">
            <div class="detail-image">
              <img :src="data.coverImage || defaultFoodImage" :alt="data.name" />
            </div>
            <div class="detail-info">
              <div class="info-header">
                <h3 class="detail-title">{{ data.name }}</h3>
                <span class="detail-price">¥{{ data.price }}</span>
              </div>
              <div class="detail-meta">
                <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="1.5">
                  <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z" />
                  <circle cx="12" cy="10" r="3" />
                </svg>
                <span>{{ data.city }} {{ data.restaurant }}</span>
              </div>
              <div class="detail-text">{{ data.description }}</div>
            </div>
          </div>

          <div v-if="type === 'attraction'" class="detail-content">
            <div class="detail-image">
              <img :src="data.coverImage || defaultAttractionImage" :alt="data.name" />
            </div>
            <div class="detail-info">
              <div class="info-header">
                <h3 class="detail-title">{{ data.name }}</h3>
                <span class="detail-price">¥{{ data.ticketPrice || 0 }}</span>
              </div>
              <div class="detail-meta">
                <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="1.5">
                  <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z" />
                  <circle cx="12" cy="10" r="3" />
                </svg>
                <span>{{ data.city }} {{ data.address }}</span>
              </div>
              <div class="detail-text">{{ data.description }}</div>
            </div>
          </div>

          <div v-if="type === 'safety'" class="detail-content">
            <div class="detail-image" v-if="data.imageUrl">
              <img :src="data.imageUrl" :alt="data.title" />
            </div>
            <div class="detail-info">
              <h3 class="detail-title">{{ data.title }}</h3>
              <div class="detail-text">{{ data.description }}</div>
            </div>
          </div>

          <div v-if="type === 'recommendation'" class="detail-content">
            <div class="detail-image">
              <img :src="data.coverImageUrl || defaultRecommendationImage" :alt="data.name" />
            </div>
            <div class="detail-info">
              <div class="info-header">
                <h3 class="detail-title">{{ data.name }}</h3>
                <span class="detail-price">{{ data.isFree ? $t('homeSections.common.free') : `¥${data.minPrice || 0}`
                  }}</span>
              </div>
              <div class="detail-meta">
                <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="1.5">
                  <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z" />
                  <circle cx="12" cy="10" r="3" />
                </svg>
                <span>{{ data.city }} · {{ data.address }}</span>
              </div>
              <div class="detail-section">
                <h4 class="section-title">{{ $t('homeSections.details.recommendReason') }}</h4>
                <p>{{ data.recommendationReason }}</p>
              </div>
              <div class="detail-section">
                <h4 class="section-title">{{ $t('homeSections.details.highlights') }}</h4>
                <p>{{ data.highlights }}</p>
              </div>
              <div class="detail-grid">
                <div class="grid-item">
                  <span class="grid-label">{{ $t('homeSections.details.category') }}</span>
                  <span class="grid-value">{{ data.categoryName }}</span>
                </div>
                <div class="grid-item">
                  <span class="grid-label">{{ $t('homeSections.details.bestTime') }}</span>
                  <span class="grid-value">{{ data.bestTravelTime }}</span>
                </div>
                <div class="grid-item">
                  <span class="grid-label">{{ $t('homeSections.details.duration') }}</span>
                  <span class="grid-value">{{ data.visitDuration }}</span>
                </div>
                <div class="grid-item">
                  <span class="grid-label">{{ $t('homeSections.details.ticket') }}</span>
                  <span class="grid-value">{{ data.ticketPriceDescription }}</span>
                </div>
                <div class="grid-item">
                  <span class="grid-label">{{ $t('homeSections.details.openingHours') }}</span>
                  <span class="grid-value">{{ data.openingHoursWeekday }}</span>
                </div>
                <div class="grid-item">
                  <span class="grid-label">{{ $t('homeSections.details.transport') }}</span>
                  <span class="grid-value">{{ data.transportation }}</span>
                </div>
              </div>
              <div class="detail-section">
                <h4 class="section-title">{{ $t('homeSections.details.tips') }}</h4>
                <p>{{ data.tips }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';

const props = defineProps({
    visible: {
        type: Boolean,
        default: false
    },
    type: {
        type: String,
        default: ''
    },
    data: {
        type: Object,
        default: () => ({})
    },
    loading: {
        type: Boolean,
        default: false
    },
    error: {
        type: String,
        default: ''
    }
});

const emit = defineEmits(['close']);
const { t, locale } = useI18n();

const DETAIL_TYPES = ['news', 'hotel', 'food', 'attraction', 'safety', 'recommendation'];

const title = computed(() => {
    const key = DETAIL_TYPES.includes(props.type) ? props.type : 'default';
    return t(`homeSections.details.titles.${key}`);
});

const defaultNewsImage = 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=news%20article%20placeholder%2C%20simple%20clean%20design&image_size=square'
const defaultHotelImage = 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=hotel%20exterior%20placeholder%2C%20modern%20design&image_size=square'
const defaultFoodImage = 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=delicious%20food%20placeholder%2C%20appetizing%20presentation&image_size=square'
const defaultAttractionImage = 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=tourist%20attraction%20placeholder%2C%20scenic%20view&image_size=square'
const defaultRecommendationImage = 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=travel%20recommendation%20placeholder%2C%20beautiful%20destination&image_size=square'

const closeModal = () => {
    emit('close');
};

const formatDate = (dateString) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString(locale.value === 'en-US' ? 'en-US' : 'zh-CN');
};
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background: var(--theme-overlay);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  padding: clamp(12px, 3vw, 32px);
}

.modal-content {
  background: var(--theme-bg-elevated);
  border: 1px solid var(--theme-border);
  border-radius: 26px;
  max-width: 820px;
  width: 100%;
  max-height: min(90vh, 900px);
  overflow-y: auto;
  overscroll-behavior: contain;
  animation: modalSlideUp 0.3s cubic-bezier(0.22, 1, 0.36, 1);
  scrollbar-width: none;
  -ms-overflow-style: none;
  box-shadow: var(--theme-shadow-lg);
}

.modal-content::-webkit-scrollbar {
  display: none;
}

@keyframes modalSlideUp {
  from {
    opacity: 0;
    transform: translateY(20px) scale(0.98);
  }

  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* 顶部栏吸附 + 毛玻璃，长内容滚动时保持层级 */
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  padding: 18px clamp(18px, 2.4vw, 28px);
  border-bottom: 1px solid var(--theme-hairline);
  position: sticky;
  top: 0;
  z-index: 10;
  background: var(--theme-bg-glass-strong);
  backdrop-filter: blur(18px) saturate(160%);
  -webkit-backdrop-filter: blur(18px) saturate(160%);
}

.modal-title {
  font-size: 17px;
  font-weight: 600;
  margin: 0;
  color: var(--theme-text-1);
  letter-spacing: -0.01em;
}

.close-button {
  flex: none;
  background: var(--theme-bg-surface-2);
  border: none;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: var(--theme-text-2);
  transition: background 0.2s ease, color 0.2s ease, transform 0.2s ease;
}

.close-button:hover {
  background: var(--theme-bg-hover);
  color: var(--theme-text-1);
  transform: rotate(90deg);
}

.modal-body {
  padding: 0;
}

.loading,
.error {
  text-align: center;
  padding: 72px 24px;
  font-size: 14.5px;
}

.loading {
  color: var(--theme-text-2);
}

.error {
  color: var(--theme-danger);
}

.detail-content {
  display: flex;
  flex-direction: column;
}

.detail-image {
  width: 100%;
  height: clamp(200px, 30vw, 320px);
  overflow: hidden;
  background: var(--theme-bg-surface-2);
}

.detail-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.detail-info {
  padding: clamp(18px, 2.4vw, 28px);
}

.info-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 14px;
}

.detail-title {
  font-size: clamp(20px, 2.2vw, 24px);
  font-weight: 700;
  margin: 0;
  line-height: 1.3;
  color: var(--theme-text-1);
}

.detail-price {
  flex: none;
  display: inline-flex;
  align-items: baseline;
  gap: 2px;
  padding: 6px 14px;
  border-radius: 12px;
  background: var(--theme-primary-soft);
  font-size: clamp(18px, 2vw, 22px);
  font-weight: 700;
  color: var(--theme-primary);
  white-space: nowrap;
}

.price-unit {
  font-size: 13px;
  font-weight: 400;
  color: var(--theme-text-2);
}

.detail-meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  font-size: 13.5px;
  color: var(--theme-text-2);
  margin-bottom: 18px;
}

.detail-date,
.detail-view {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.detail-text {
  font-size: 15px;
  line-height: 1.75;
  color: var(--theme-text-2);
  white-space: pre-line;
}

.detail-section {
  margin-bottom: 22px;
}

.detail-section .section-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--theme-text-1);
  margin: 0 0 10px;
}

.detail-section p {
  font-size: 14px;
  line-height: 1.7;
  color: var(--theme-text-2);
  margin: 0;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 10px;
  margin-bottom: 22px;
}

.grid-item {
  background: var(--theme-bg-surface-2);
  border: 1px solid var(--theme-hairline);
  padding: 14px 16px;
  border-radius: 14px;
}

.grid-label {
  display: block;
  font-size: 11.5px;
  letter-spacing: 0.04em;
  color: var(--theme-text-3);
  font-weight: 500;
  margin-bottom: 5px;
}

.grid-value {
  font-size: 14px;
  color: var(--theme-text-1);
  font-weight: 500;
}

/* 小屏：切换为底部上滑面板，更符合移动端操作习惯 */
@media (max-width: 640px) {
  .modal-overlay {
    align-items: flex-end;
    padding: 0;
  }

  .modal-content {
    max-width: none;
    max-height: 92dvh;
    border-radius: 24px 24px 0 0;
    border-bottom: none;
    animation: sheetSlideUp 0.32s cubic-bezier(0.22, 1, 0.36, 1);
    padding-bottom: env(safe-area-inset-bottom);
  }

  @keyframes sheetSlideUp {
    from {
      opacity: 0;
      transform: translateY(40px);
    }

    to {
      opacity: 1;
      transform: translateY(0);
    }
  }

  .modal-header {
    border-radius: 24px 24px 0 0;
  }

  .detail-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 8px;
  }

  .grid-item {
    padding: 12px 14px;
  }

  .info-header {
    flex-direction: column;
    gap: 10px;
  }

  .detail-price {
    align-self: flex-start;
  }

  .detail-meta {
    margin-bottom: 14px;
  }

  .detail-text {
    font-size: 14px;
    line-height: 1.7;
  }
}

@media (max-width: 420px) {
  .detail-grid {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
