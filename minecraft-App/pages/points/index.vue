<template>
  <view class="points-container">
    <view class="points-header">
      <view class="points-info">
        <text class="points-label">{{ t('points.myPoints') }}</text>
        <text class="points-value">{{ userInfo?.points || 0 }}</text>
      </view>
    </view>
    
    <view class="products-section">
      <text class="section-title">{{ t('points.products') }}</text>
      <view class="products-list">
        <view 
          v-for="product in products" 
          :key="product.id" 
          class="product-item"
          @click="exchangeProduct(product)"
        >
          <image :src="product.imageUrl || 'https://via.placeholder.com/300x200'" class="product-image"></image>
          <view class="product-info">
            <text class="product-name">{{ product.name }}</text>
            <text class="product-description">{{ product.description }}</text>
            <view class="product-footer">
              <text class="points-price">{{ t('points.price', { n: product.pointsPrice }) }}</text>
              <text class="stock">{{ t('points.stock', { n: product.stock }) }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>
    
    <!-- 兑换弹窗 -->
    <uni-popup ref="exchangePopup" type="center" :mask-click="false">
      <view class="exchange-popup">
        <text class="popup-title">{{ t('points.exchangeTitle') }}</text>
        <view class="popup-content">
          <view class="form-item">
            <text class="label">{{ t('points.receiver') }}</text>
            <input v-model="exchangeForm.receiver" :placeholder="t('points.receiverPlaceholder')" class="input" />
          </view>
          <view class="form-item">
            <text class="label">{{ t('points.phone') }}</text>
            <input v-model="exchangeForm.phone" :placeholder="t('points.phonePlaceholder')" class="input" />
          </view>
          <view class="form-item">
            <text class="label">{{ t('points.address') }}</text>
            <textarea v-model="exchangeForm.address" :placeholder="t('points.addressPlaceholder')" class="textarea"></textarea>
          </view>
          <view class="form-item">
            <text class="label">{{ t('points.qty') }}</text>
            <view class="quantity-control">
              <button @click="decreaseQuantity" class="quantity-btn">-</button>
              <text class="quantity">{{ exchangeForm.quantity }}</text>
              <button @click="increaseQuantity" class="quantity-btn">+</button>
            </view>
          </view>
          <view class="form-item">
            <text class="label">{{ t('points.cost') }}</text>
            <text class="total-points">{{ t('points.price', { n: (currentProduct?.pointsPrice || 0) * exchangeForm.quantity }) }}</text>
          </view>
        </view>
        <view class="popup-footer">
          <button @click="closePopup" class="cancel-btn">{{ t('points.cancel') }}</button>
          <button @click="submitExchange" class="confirm-btn">{{ t('points.confirm') }}</button>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script>
import { getPointsProducts, exchangeProduct } from '@/api/points'
import { getUserInfo } from '@/api/user'
import { t } from '@/utils/i18n'

export default {
  data() {
    return {
      userInfo: null,
      products: [],
      currentProduct: null,
      exchangeForm: {
        receiver: '',
        phone: '',
        address: '',
        quantity: 1
      }
    }
  },
  onLoad() {
    this.loadUserInfo()
    this.loadProducts()
  },
  onShow() {
    uni.setNavigationBarTitle({ title: t('page.points') })
  },
  methods: {
    t,
    async loadUserInfo() {
      try {
        const res = await getUserInfo()
        this.userInfo = res.data
      } catch (error) {
        console.error('获取用户信息失败', error)
      }
    },
    async loadProducts() {
      try {
        const res = await getPointsProducts({ page: 1, size: 100 })
        this.products = res.records
      } catch (error) {
        console.error('获取商品列表失败', error)
      }
    },
    exchangeProduct(product) {
      this.currentProduct = product
      this.exchangeForm.quantity = 1
      this.$refs.exchangePopup.open()
    },
    closePopup() {
      this.$refs.exchangePopup.close()
    },
    decreaseQuantity() {
      if (this.exchangeForm.quantity > 1) {
        this.exchangeForm.quantity--
      }
    },
    increaseQuantity() {
      if (this.currentProduct && this.exchangeForm.quantity < this.currentProduct.stock) {
        this.exchangeForm.quantity++
      }
    },
    async submitExchange() {
      if (!this.exchangeForm.receiver || !this.exchangeForm.phone || !this.exchangeForm.address) {
        uni.showToast({ title: t('points.fillAll'), icon: 'none' })
        return
      }
      
      try {
        await exchangeProduct({
          productId: this.currentProduct.id,
          quantity: this.exchangeForm.quantity,
          address: this.exchangeForm.address,
          phone: this.exchangeForm.phone,
          receiver: this.exchangeForm.receiver
        })
        
        uni.showToast({ title: t('points.success'), icon: 'success' })
        this.$refs.exchangePopup.close()
        this.loadProducts()
        this.loadUserInfo()
      } catch (error) {
        uni.showToast({ title: error.response?.data?.message || t('points.failed'), icon: 'none' })
      }
    }
  }
}
</script>

<style scoped>
.points-container {
  min-height: 100vh;
  background-color: var(--bg-color);
}

.points-header {
  background: var(--primary-color);
  padding: 30rpx;
  color: white;
  text-align: center;
}

.points-info {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.points-label {
  font-size: 28rpx;
  margin-bottom: 10rpx;
}

.points-value {
  font-size: 60rpx;
  font-weight: bold;
}

.products-section {
  padding: 20rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  margin-bottom: 20rpx;
  color: var(--text-color);
}

.products-list {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
}

.product-item {
  width: 48%;
  background-color: var(--card-background);
  border-radius: 12rpx;
  margin-bottom: 20rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
  transition: transform 0.3s;
}

.product-item:hover {
  transform: translateY(-5rpx);
}

.product-image {
  width: 100%;
  height: 200rpx;
  object-fit: cover;
}

.product-info {
  padding: 15rpx;
}

.product-name {
  font-size: 28rpx;
  font-weight: bold;
  margin-bottom: 10rpx;
  color: var(--text-color);
}

.product-description {
  font-size: 24rpx;
  color: var(--text-secondary-color);
  margin-bottom: 15rpx;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.points-price {
  font-size: 26rpx;
  font-weight: bold;
  color: var(--accent-color);
}

.stock {
  font-size: 22rpx;
  color: var(--text-tertiary-color);
}

.exchange-popup {
  width: 90%;
  max-width: 600rpx;
  background-color: var(--card-background);
  border-radius: 12rpx;
  padding: 30rpx;
}

.popup-title {
  font-size: 32rpx;
  font-weight: bold;
  text-align: center;
  margin-bottom: 30rpx;
  color: var(--text-color);
}

.form-item {
  margin-bottom: 25rpx;
}

.label {
  display: block;
  font-size: 26rpx;
  color: var(--text-color);
  margin-bottom: 10rpx;
}

.input {
  width: 100%;
  padding: 15rpx;
  border: 1rpx solid var(--border-color);
  border-radius: 6rpx;
  font-size: 24rpx;
}

.textarea {
  width: 100%;
  padding: 15rpx;
  border: 1rpx solid var(--border-color);
  border-radius: 6rpx;
  font-size: 24rpx;
  min-height: 120rpx;
}

.quantity-control {
  display: flex;
  align-items: center;
}

.quantity-btn {
  width: 60rpx;
  height: 60rpx;
  border: 1rpx solid var(--border-color);
  background-color: var(--bg-color);
  border-radius: 6rpx;
  font-size: 30rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.quantity {
  margin: 0 30rpx;
  font-size: 26rpx;
  font-weight: bold;
}

.total-points {
  font-size: 28rpx;
  font-weight: bold;
  color: var(--accent-color);
}

.popup-footer {
  display: flex;
  justify-content: space-between;
  margin-top: 40rpx;
}

.cancel-btn {
  flex: 1;
  height: 80rpx;
  margin-right: 20rpx;
  border: 1rpx solid var(--border-color);
  background-color: var(--card-background);
  border-radius: 6rpx;
  font-size: 28rpx;
  color: var(--text-color);
}

.confirm-btn {
  flex: 1;
  height: 80rpx;
  background-color: var(--primary-color);
  border: none;
  border-radius: 6rpx;
  font-size: 28rpx;
  color: white;
}
</style>
