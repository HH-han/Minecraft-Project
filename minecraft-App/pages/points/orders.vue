<template>
  <view class="orders-container">
    <view class="header">
      <text class="title">{{ t('page.pointsOrders') }}</text>
    </view>
    
    <view class="orders-list">
      <view 
        v-for="order in orders" 
        :key="order.id" 
        class="order-item"
      >
        <view class="order-header">
          <text class="order-no">{{ t('points.orderNo', { no: order.orderNo }) }}</text>
          <text :class="['order-status', order.status]" :style="{ backgroundColor: getStatusColor(order.status) }">
            {{ getStatusText(order.status) }}
          </text>
        </view>
        <view class="order-content">
          <text class="product-name">{{ order.productName }}</text>
          <text class="order-info">{{ t('points.qtyLabel', { n: order.quantity }) }}</text>
          <text class="order-info">{{ t('points.used', { n: order.pointsUsed }) }}</text>
          <text class="order-info">{{ t('points.receiverLabel', { name: order.receiver }) }}</text>
          <text class="order-info">{{ t('points.phoneLabel', { phone: order.phone }) }}</text>
          <text class="order-info">{{ t('points.addressLabel', { addr: order.address }) }}</text>
        </view>
        <view class="order-footer">
          <text class="order-time">{{ formatTime(order.createTime) }}</text>
        </view>
      </view>
      <view v-if="orders.length === 0" class="empty">
        <text>{{ t('points.emptyOrders') }}</text>
      </view>
    </view>
  </view>
</template>

<script>
import { getExchangeOrders } from '@/api/points'
import { t } from '@/utils/i18n'

export default {
  data() {
    return {
      orders: []
    }
  },
  onLoad() {
    this.loadOrders()
  },
  onShow() {
    uni.setNavigationBarTitle({ title: t('page.pointsOrders') })
  },
  methods: {
    t,
    async loadOrders() {
      try {
        const res = await getExchangeOrders({ page: 1, size: 100 })
        this.orders = res.records
      } catch (error) {
        console.error('获取订单列表失败', error)
      }
    },
    getStatusText(status) {
      const statusMap = {
        'PENDING': t('points.statusPending'),
        'SUCCESS': t('points.statusSuccess'),
        'FAILED': t('points.statusFailed'),
        'CANCELLED': t('points.statusCancelled')
      }
      return statusMap[status] || status
    },
    getStatusColor(status) {
      const colorMap = {
        'PENDING': '#C08A2E',
        'SUCCESS': '#2F7A55',
        'FAILED': '#B5482F',
        'CANCELLED': '#8C948B'
      }
      return colorMap[status] || '#8C948B'
    },
    formatTime(time) {
      if (!time) return ''
      const date = new Date(time)
      return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
    }
  }
}
</script>

<style scoped>
.orders-container {
  min-height: 100vh;
  background-color: var(--bg-color);
}

.header {
  background-color: var(--card-background);
  padding: 20rpx;
  border-bottom: 1rpx solid var(--border-color);
  position: sticky;
  top: 0;
  z-index: 10;
}

.title {
  font-size: 32rpx;
  font-weight: bold;
  color: var(--text-color);
}

.orders-list {
  padding: 20rpx;
}

.order-item {
  background-color: var(--card-background);
  border-radius: 12rpx;
  padding: 20rpx;
  margin-bottom: 15rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15rpx;
  padding-bottom: 15rpx;
  border-bottom: 1rpx solid var(--border-color);
}

.order-no {
  font-size: 24rpx;
  color: var(--text-secondary-color);
}

.order-status {
  font-size: 22rpx;
  color: white;
  padding: 5rpx 15rpx;
  border-radius: 15rpx;
}

.order-content {
  margin-bottom: 15rpx;
}

.product-name {
  font-size: 28rpx;
  font-weight: bold;
  color: var(--text-color);
  margin-bottom: 10rpx;
  display: block;
}

.order-info {
  font-size: 24rpx;
  color: var(--text-secondary-color);
  margin-bottom: 5rpx;
  display: block;
}

.order-footer {
  padding-top: 15rpx;
  border-top: 1rpx solid var(--border-color);
}

.order-time {
  font-size: 20rpx;
  color: var(--text-tertiary-color);
}

.empty {
  text-align: center;
  padding: 100rpx 0;
  color: var(--text-tertiary-color);
  font-size: 24rpx;
}
</style>
