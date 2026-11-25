<template>
  <view class="records-container">
    <view class="header">
      <text class="title">{{ t('page.pointsRecords') }}</text>
    </view>
    
    <view class="records-list">
      <view 
        v-for="record in records" 
        :key="record.id" 
        class="record-item"
      >
        <view class="record-info">
          <text class="record-type">{{ getTypeText(record.type) }}</text>
          <text class="record-remark">{{ record.remark }}</text>
          <text class="record-time">{{ formatTime(record.createTime) }}</text>
        </view>
        <view class="record-points" :class="record.type === 'INCOME' ? 'income' : 'expense'">
          {{ record.type === 'INCOME' ? '+' : '-' }}{{ record.points }}
        </view>
      </view>
      <view v-if="records.length === 0" class="empty">
        <text>{{ t('points.emptyRecords') }}</text>
      </view>
    </view>
  </view>
</template>

<script>
import { getPointsRecords } from '@/api/points'
import { t } from '@/utils/i18n'

export default {
  data() {
    return {
      records: []
    }
  },
  onLoad() {
    this.loadRecords()
  },
  onShow() {
    uni.setNavigationBarTitle({ title: t('page.pointsRecords') })
  },
  methods: {
    t,
    async loadRecords() {
      try {
        const res = await getPointsRecords()
        this.records = res.data
      } catch (error) {
        console.error('获取积分记录失败', error)
      }
    },
    getTypeText(type) {
      const typeMap = {
        'INCOME': t('points.typeIncome'),
        'EXCHANGE': t('points.typeExchange'),
        'REFUND': t('points.typeRefund')
      }
      return typeMap[type] || type
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
.records-container {
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

.records-list {
  padding: 20rpx;
}

.record-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: var(--card-background);
  border-radius: 12rpx;
  padding: 20rpx;
  margin-bottom: 15rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
}

.record-info {
  flex: 1;
}

.record-type {
  font-size: 28rpx;
  font-weight: bold;
  color: var(--text-color);
  margin-bottom: 5rpx;
  display: block;
}

.record-remark {
  font-size: 24rpx;
  color: var(--text-secondary-color);
  margin-bottom: 5rpx;
  display: block;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.record-time {
  font-size: 20rpx;
  color: var(--text-tertiary-color);
  display: block;
}

.record-points {
  font-size: 32rpx;
  font-weight: bold;
  min-width: 120rpx;
  text-align: right;
}

.income {
  color: var(--success-color);
}

.expense {
  color: var(--accent-color);
}

.empty {
  text-align: center;
  padding: 100rpx 0;
  color: var(--text-tertiary-color);
  font-size: 24rpx;
}
</style>
