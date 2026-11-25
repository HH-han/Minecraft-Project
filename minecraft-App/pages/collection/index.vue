<template>
  <div class="app-page app-page--plain">
    <div class="shell">
      <header class="simple-head">
        <div class="page-eyebrow">{{ t('collection.eyebrow') }}</div>
        <h1 class="page-title">{{ t('collection.title') }}</h1>
        <p class="page-sub">{{ t('collection.sub') }}</p>
      </header>

      <div class="chip-row tab-row">
        <button v-for="tab in tabs" :key="tab.value" class="chip" :class="{ 'is-active': activeTab === tab.value }"
          @click="activeTab = tab.value">
          {{ t(tab.labelKey) }}
        </button>
      </div>

      <div v-if="filteredCollections.length" class="collection-grid">
        <article v-for="item in filteredCollections" :key="item.id" class="collection-card">
          <div class="collection-media">
            <img :src="item.image" :alt="item.title" loading="lazy">
            <span class="type-badge">{{ t(item.typeKey) }}</span>
            <button class="remove-btn" @click.stop="removeCollection(item.id)" :aria-label="t('collection.removeTitle')">
              <uni-icons type="heart-filled" size="15" color="#C4613C" />
            </button>
          </div>
          <div class="collection-info">
            <h3 class="collection-title">{{ t(item.titleKey) }}</h3>
            <p class="collection-desc clamp-2">{{ t(item.descKey) }}</p>
            <div class="collection-meta">
              <span><uni-icons type="calendar" size="12" color="currentColor" />{{ item.time }}</span>
              <span class="go-link">{{ t('collection.viewDetail') }}<uni-icons type="right" size="11" color="currentColor" /></span>
            </div>
          </div>
        </article>
      </div>

      <div v-else class="empty-state">
        <div class="empty-state__icon"><uni-icons type="star" size="26" color="#1E5B48" /></div>
        {{ t('collection.empty') }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import UniIcons from '@/uni_modules/uni-icons/components/uni-icons/uni-icons.vue'
import { t, usePageTitle } from '@/utils/i18n'

usePageTitle('page.collection')

const tabs = [
  { labelKey: 'common.all', value: 'all' },
  { labelKey: 'collection.typeAttraction', value: 'attraction' },
  { labelKey: 'collection.typeHotel', value: 'hotel' },
  { labelKey: 'collection.typeFood', value: 'food' }
]

const activeTab = ref('all')

const collections = ref([
  {
    id: 1,
    type: 'attraction',
    typeKey: 'collection.typeAttraction',
    titleKey: 'collection.itemGreatWall',
    descKey: 'collection.itemGreatWallDesc',
    time: '2026-03-28',
    image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=beautiful%20scenic%20landscape%20great%20wall&image_size=landscape_4_3'
  },
  {
    id: 2,
    type: 'hotel',
    typeKey: 'collection.typeHotel',
    titleKey: 'collection.itemHotel',
    descKey: 'collection.itemHotelDesc',
    time: '2026-03-27',
    image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=luxury%20hotel%20exterior%20mountain%20view&image_size=landscape_4_3'
  },
  {
    id: 3,
    type: 'food',
    typeKey: 'collection.typeFood',
    titleKey: 'collection.itemFood',
    descKey: 'collection.itemFoodDesc',
    time: '2026-03-26',
    image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=chinese%20roast%20duck%20dish&image_size=landscape_4_3'
  }
])

const filteredCollections = computed(() =>
  activeTab.value === 'all' ? collections.value : collections.value.filter((item) => item.type === activeTab.value)
)

const removeCollection = (id) => {
  uni.showModal({
    title: t('collection.removeTitle'),
    content: t('collection.removeContent'),
    success: (res) => {
      if (res.confirm) {
        collections.value = collections.value.filter((item) => item.id !== id)
        uni.showToast({ title: t('collection.removed'), icon: 'none', position: 'bottom' })
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

.tab-row {
  margin-top: 16px;
}

.collection-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 14px;
  margin-top: 14px;
}

.collection-card {
  background: var(--card-background);
  border: 1px solid var(--card-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
  transition: transform 0.35s var(--ease-out), box-shadow 0.35s var(--ease-out);
}

.collection-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-md);
}

.collection-media {
  position: relative;
  height: 168px;
  overflow: hidden;
  background: var(--surface-2);
}

.collection-media img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.7s var(--ease-out);
}

.collection-card:hover .collection-media img {
  transform: scale(1.06);
}

.type-badge {
  position: absolute;
  top: 12px;
  left: 12px;
  padding: 4px 10px;
  border-radius: var(--radius-pill);
  background: rgba(255, 255, 255, 0.9);
  color: var(--primary-color);
  font-size: 11px;
  font-weight: 700;
  -webkit-backdrop-filter: blur(8px);
  backdrop-filter: blur(8px);
}

.remove-btn {
  position: absolute;
  top: 10px;
  right: 10px;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 4px 12px rgba(38, 45, 36, 0.16);
  transition: transform 0.25s var(--ease-spring);
}

.remove-btn:hover {
  transform: scale(1.1);
}

.collection-info {
  padding: 14px 15px 15px;
}

.collection-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-color);
}

.collection-desc {
  margin-top: 6px;
  font-size: 12px;
  line-height: 1.6;
  color: var(--text-secondary-color);
}

.collection-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-top: 12px;
  padding-top: 11px;
  border-top: 1px dashed var(--border-color);
  font-size: 11px;
  color: var(--text-tertiary-color);
}

.collection-meta span {
  display: inline-flex;
  align-items: center;
  gap: 3px;
}

.go-link {
  color: var(--primary-color);
  font-weight: 600;
}

@media (min-width: 768px) {
  .page-title {
    font-size: 32px;
  }

  .collection-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 18px;
  }

  .collection-media {
    height: 200px;
  }
}

@media (min-width: 1100px) {
  .collection-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}
</style>
