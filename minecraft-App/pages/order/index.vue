<template>
  <div class="app-page app-page--plain">
    <div class="shell">
      <header class="simple-head">
        <div class="page-eyebrow">{{ t('order.eyebrow') }}</div>
        <h1 class="page-title">{{ t('order.title') }}</h1>
        <p class="page-sub">{{ t('order.sub') }}</p>
      </header>

      <!-- 状态筛选 -->
      <div class="chip-row tab-row">
        <button v-for="tab in tabs" :key="tab.value" class="chip" :class="{ 'is-active': activeTab === tab.value }"
          @click="activeTab = tab.value">
          {{ t(tab.labelKey) }}
        </button>
      </div>

      <!-- 订单列表 -->
      <div v-if="filteredOrders.length" class="order-list">
        <article v-for="order in filteredOrders" :key="order.id" class="order-card">
          <div class="order-top">
            <span class="order-id">{{ t('order.orderNo', { id: order.id }) }}</span>
            <span class="status" :class="`status--${order.status}`">{{ t(order.statusKey) }}</span>
          </div>

          <div class="order-body">
            <div class="order-thumb">
              <img :src="order.image" :alt="order.title" loading="lazy">
            </div>
            <div class="order-info">
              <h3 class="order-title">{{ t(order.titleKey) }}</h3>
              <p class="order-meta">{{ t(order.cityKey) }} · {{ t('order.qty', { n: order.count }) }}</p>
              <p class="order-date">{{ order.createTime }}</p>
            </div>
            <div class="order-price"><small>¥</small>{{ order.price }}</div>
          </div>

          <div class="order-actions">
            <button class="btn btn--ghost btn--sm" @click="handleAction('order.cancel')"
              v-if="order.status === 'pending'">{{ t('order.cancel') }}</button>
            <button class="btn btn--primary btn--sm" @click="handleAction('order.pay')"
              v-if="order.status === 'pending'">{{ t('order.pay') }}</button>
            <button class="btn btn--ghost btn--sm" @click="handleAction('order.view')"
              v-if="order.status !== 'pending'">{{ t('order.view') }}</button>
            <button class="btn btn--soft btn--sm" @click="handleAction('order.again')"
              v-if="order.status === 'done'">{{ t('order.again') }}</button>
          </div>
        </article>
      </div>

      <div v-else class="empty-state">
        <div class="empty-state__icon"><uni-icons type="list" size="26" color="#1E5B48" /></div>
        {{ t('order.empty') }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { t, usePageTitle } from '@/utils/i18n'

usePageTitle('page.order')

const tabs = [
  { labelKey: 'order.tabAll', value: 'all' },
  { labelKey: 'order.tabPending', value: 'pending' },
  { labelKey: 'order.tabUnused', value: 'unused' },
  { labelKey: 'order.tabDone', value: 'done' },
  { labelKey: 'order.tabCanceled', value: 'canceled' }
]

const orders = ref([
  {
    id: '20260329001',
    titleKey: 'order.itemTicket',
    cityKey: 'list.cityBeijing',
    count: 2,
    price: '198.00',
    status: 'pending',
    statusKey: 'order.statusPending',
    createTime: '2026-03-29 10:24',
    image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=great%20wall%20beijing%20scenery&image_size=landscape_4_3'
  },
  {
    id: '20260328002',
    titleKey: 'order.itemHotel',
    cityKey: 'list.cityShanghai',
    count: 1,
    price: '398.00',
    status: 'done',
    statusKey: 'order.statusDone',
    createTime: '2026-03-28 18:06',
    image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=luxury%20hotel%20room%20interior&image_size=landscape_4_3'
  },
  {
    id: '20260326005',
    titleKey: 'order.itemFood',
    cityKey: 'list.cityBeijing',
    count: 3,
    price: '268.00',
    status: 'unused',
    statusKey: 'order.statusUnused',
    createTime: '2026-03-26 12:40',
    image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=peking%20duck%20dish&image_size=landscape_4_3'
  }
])

const activeTab = ref('all')

const filteredOrders = computed(() =>
  activeTab.value === 'all' ? orders.value : orders.value.filter((item) => item.status === activeTab.value)
)

const handleAction = (actionKey) => {
  uni.showToast({
    title: t('common.building', { name: t(actionKey) }),
    icon: 'none',
    position: 'bottom'
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

.order-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
  margin-top: 14px;
}

.order-card {
  padding: 16px;
  background: var(--card-background);
  border: 1px solid var(--card-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  transition: transform 0.35s var(--ease-out), box-shadow 0.35s var(--ease-out);
}

.order-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-md);
}

.order-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding-bottom: 12px;
  border-bottom: 1px dashed var(--border-color);
}

.order-id {
  font-size: 12px;
  color: var(--text-tertiary-color);
  font-variant-numeric: tabular-nums;
}

.status {
  padding: 3px 10px;
  border-radius: var(--radius-pill);
  font-size: 11px;
  font-weight: 700;
}

.status--pending {
  background: rgba(255, 138, 61, 0.14);
  color: #F97316;
}

.status--unused {
  background: var(--primary-soft);
  color: var(--primary-color);
}

.status--done {
  background: rgba(16, 185, 129, 0.14);
  color: var(--success-color);
}

.status--canceled {
  background: var(--background-soft);
  color: var(--text-tertiary-color);
}

.order-body {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 0 4px;
}

.order-thumb {
  flex: 0 0 72px;
  width: 72px;
  height: 72px;
  border-radius: var(--radius-sm);
  overflow: hidden;
  background: var(--surface-2);
}

.order-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.order-info {
  flex: 1;
  min-width: 0;
}

.order-title {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-color);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.order-meta,
.order-date {
  margin-top: 4px;
  font-size: 12px;
  color: var(--text-tertiary-color);
}

.order-price {
  flex: 0 0 auto;
  display: inline-flex;
  align-items: baseline;
  color: #C4613C;
  font-size: 19px;
  font-weight: 800;
  font-variant-numeric: tabular-nums;
}

.order-price small {
  font-size: 11px;
}

.order-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding-top: 12px;
}

.btn--sm {
  padding: 8px 15px;
  font-size: 12px;
}

@media (min-width: 768px) {
  .page-title {
    font-size: 32px;
  }

  .order-list {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 18px;
  }
}
</style>
