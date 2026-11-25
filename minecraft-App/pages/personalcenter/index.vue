<template>
  <div class="app-page">
    <div class="shell">
      <view v-if="loading" class="top-loading"></view>

      <!-- 未登录 -->
      <view v-if="!isLoggedIn" class="guest-card reveal">
        <view class="guest-glow"></view>
        <view class="guest-logo">
          <uni-icons type="map-pin-ellipse" size="26" color="#FFFDF8" />
        </view>
        <text class="guest-title">{{ t('personal.loginTipTitle') }}</text>
        <text class="guest-desc">{{ t('personal.loginTipDesc') }}</text>
        <button class="guest-btn" @click="goLogin">{{ t('personal.loginNow') }}</button>
        <view class="guest-links">
          <text class="guest-link" @click="go('/pages/profile/settings')">{{ t('profile.settings') }}</text>
          <text class="guest-dot">·</text>
          <text class="guest-link" @click="go('/pages/profile/about')">{{ t('profile.about') }}</text>
        </view>
      </view>

      <template v-else>
        <!-- 用户头部 -->
        <header class="hero reveal">
          <view class="hero-glow"></view>
          <view class="hero-ring hero-ring--1"></view>
          <view class="hero-ring hero-ring--2"></view>

          <view class="hero-main">
            <view class="avatar" @click="changeAvatar">
              <image v-if="avatar" :src="avatar" class="avatar-img" mode="aspectFill" />
              <view v-else class="avatar-fallback">
                <uni-icons type="person-filled" size="28" color="rgba(255,253,248,0.9)" />
              </view>
              <view class="avatar-cam">
                <uni-icons type="camera-filled" size="11" color="#FFFFFF" />
              </view>
            </view>

            <view class="hero-info">
              <view class="name-row">
                <text class="name">{{ displayName }}</text>
                <text v-if="isPaid" class="vip-chip">{{ t('personal.paid') }}</text>
              </view>
              <text class="sign">{{ signature }}</text>
              <view class="hero-chips">
                <text v-if="levelName" class="level-chip">
                  <uni-icons type="medal-filled" size="11" color="#F3D9A4" />
                  {{ levelName }}
                </text>
                <text v-if="signContinuous" class="streak-chip">
                  {{ t('personal.signStreak', { n: signContinuous }) }}
                </text>
              </view>
            </view>

            <view class="edit-btn" :aria-label="t('personal.editProfile')" @click="openEdit">
              <uni-icons type="compose" size="15" color="#FFFDF8" />
            </view>
          </view>

          <view class="hero-foot">
            <view class="hero-growth">
              <text class="hero-growth-label">{{ t('personal.growth') }}</text>
              <text class="hero-growth-value">{{ compact(growth) }}</text>
            </view>
            <button class="sign-btn" :class="{ 'is-done': todaySigned }" :loading="signing" @click="handleSign">
              <uni-icons :type="todaySigned ? 'checkmarkempty' : 'calendar'" size="14" color="currentColor" />
              {{ todaySigned ? t('personal.signed') : t('personal.signIn') }}
            </button>
          </view>
        </header>

        <!-- 数据概览 -->
        <view class="stats-card reveal reveal-delay-1">
          <view class="stat" @click="go('/pages/points/index')">
            <view class="stat-icon tint-gold">
              <uni-icons type="wallet" size="16" color="currentColor" />
            </view>
            <text class="stat-num">{{ compact(points) }}</text>
            <text class="stat-label">{{ t('personal.points') }}</text>
          </view>
          <view class="stat" @click="go('/pages/collection/index')">
            <view class="stat-icon tint-coral">
              <uni-icons type="star" size="16" color="currentColor" />
            </view>
            <text class="stat-num">{{ compact(favoriteTotal) }}</text>
            <text class="stat-label">{{ t('personal.favorites') }}</text>
          </view>
          <view class="stat" @click="go('/pages/travelNote/mine')">
            <view class="stat-icon tint-green">
              <uni-icons type="compose" size="16" color="currentColor" />
            </view>
            <text class="stat-num">{{ compact(noteTotal) }}</text>
            <text class="stat-label">{{ t('personal.notes') }}</text>
          </view>
          <view class="stat" @click="go('/pages/order/index')">
            <view class="stat-icon tint-sand">
              <uni-icons type="list" size="16" color="currentColor" />
            </view>
            <text class="stat-num">{{ compact(orderTotal) }}</text>
            <text class="stat-label">{{ t('personal.orders') }}</text>
          </view>
        </view>

        <!-- 会员成长 -->
        <view class="panel member-panel reveal reveal-delay-1">
          <view class="panel-head">
            <view class="level-badge">
              <uni-icons type="medal-filled" size="22" color="#C08A2E" />
            </view>
            <view class="panel-titles">
              <text class="panel-title">{{ t('personal.memberCard') }}</text>
              <text class="panel-sub">{{ t('personal.level') }} · {{ levelName || '--' }}</text>
            </view>
            <view class="growth">
              <text class="growth-num">{{ compact(growth) }}</text>
              <text class="growth-unit">{{ t('personal.growth') }}</text>
            </view>
          </view>
          <view class="progress">
            <view class="progress-fill" :style="{ width: growthPercent + '%' }"></view>
          </view>
          <view class="member-foot">
            <text class="member-tip">{{ nextLevelText }}</text>
            <view class="member-chips">
              <text v-if="discount !== null" class="member-chip">
                {{ t('personal.discountLabel') }} {{ discount }}
              </text>
              <text v-if="pointsRate !== null" class="member-chip">
                {{ t('personal.pointsRateLabel') }} ×{{ pointsRate }}
              </text>
            </view>
          </view>
          <view class="sign-tip">
            <uni-icons type="info" size="12" color="currentColor" />
            <text>{{ t('personal.signTip') }}</text>
          </view>
        </view>

        <!-- 订单入口 -->
        <view class="section-head">
          <div class="section-title">{{ t('profile.order') }}</div>
          <button class="section-more" @click="go('/pages/order/index')">
            {{ t('common.viewAll') }}
            <uni-icons type="right" size="12" color="currentColor" />
          </button>
        </view>

        <view class="order-shortcuts reveal">
          <view v-for="item in orderShortcuts" :key="item.key" class="shortcut"
            @click="go('/pages/order/index')">
            <view class="shortcut-icon" :class="'tint-' + item.tint">
              <uni-icons :type="item.icon" size="18" color="currentColor" />
            </view>
            <text class="shortcut-label">{{ t(item.labelKey) }}</text>
          </view>
        </view>

        <!-- 常用服务 -->
        <view class="section-head">
          <div class="section-title">{{ t('personal.services') }}</div>
        </view>
        <view class="service-grid">
          <view v-for="item in services" :key="item.key" class="service-item" @click="go(item.path)">
            <view class="service-icon" :class="'tint-' + item.tint">
              <uni-icons :type="item.icon" size="20" color="currentColor" />
            </view>
            <text class="service-label">{{ t(item.labelKey) }}</text>
          </view>
        </view>

        <button class="logout-btn" @click="handleLogout">{{ t('profile.logout') }}</button>
      </template>
    </div>

    <!-- 编辑资料 -->
    <view v-if="editVisible" class="sheet-mask" @click="editVisible = false">
      <view class="sheet" @click.stop>
        <view class="sheet-handle"></view>
        <view class="sheet-head">
          <text class="sheet-title">{{ t('personal.editTitle') }}</text>
          <view class="sheet-close" @click="editVisible = false">
            <uni-icons type="closeempty" size="18" color="#8C948B" />
          </view>
        </view>

        <view class="sheet-field">
          <text class="sheet-label">{{ t('personal.nickname') }}</text>
          <view class="sheet-input-wrap">
            <input v-model="editForm.nickname" class="sheet-input"
              :placeholder="t('personal.nicknamePlaceholder')" maxlength="30" />
          </view>
        </view>

        <view class="sheet-field">
          <text class="sheet-label">{{ t('personal.signature') }}</text>
          <view class="sheet-input-wrap">
            <input v-model="editForm.signature" class="sheet-input"
              :placeholder="t('personal.signaturePlaceholder')" maxlength="60" />
          </view>
        </view>

        <view class="sheet-field">
          <text class="sheet-label">{{ t('personal.bio') }}</text>
          <view class="sheet-input-wrap sheet-input-wrap--area">
            <textarea v-model="editForm.bio" class="sheet-textarea"
              :placeholder="t('personal.bioPlaceholder')" maxlength="200" />
          </view>
        </view>

        <view class="sheet-actions">
          <button class="sheet-btn sheet-btn--ghost" @click="editVisible = false">{{ t('points.cancel') }}</button>
          <button class="sheet-btn sheet-btn--primary" :loading="saving" @click="saveProfile">
            {{ t('personal.save') }}
          </button>
        </view>
      </view>
    </view>

    <TabBar current="profile" />
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { onPullDownRefresh, onShow } from '@dcloudio/uni-app'
import { getUserInfo, updateUserInfo, logout as logoutApi } from '@/api/user'
import { getMemberInfo, signMember } from '@/api/member'
import { getFavoriteStats } from '@/api/favorite'
import { getOrderList } from '@/api/order'
import { getMyNotes } from '@/api/travelNote'
import { BASE_URL } from '@/utils/config'
import UniIcons from '@/uni_modules/uni-icons/components/uni-icons/uni-icons.vue'
import TabBar from '@/components/tabbar/index.vue'
import { t, usePageTitle } from '@/utils/i18n'

usePageTitle('page.personalCenter')

const isLoggedIn = ref(!!uni.getStorageSync('token'))
const loading = ref(false)
const signing = ref(false)

const userInfo = ref({})
const memberInfo = ref(null)
const orderTotal = ref(0)
const noteTotal = ref(0)
const favoriteTotal = ref(0)

const editVisible = ref(false)
const saving = ref(false)
const editForm = reactive({
  nickname: '',
  signature: '',
  bio: ''
})

const orderShortcuts = [
  { key: 'all', labelKey: 'personal.orderAll', icon: 'list', tint: 'green' },
  { key: 'pending', labelKey: 'personal.orderPending', icon: 'wallet', tint: 'gold' },
  { key: 'paid', labelKey: 'personal.orderPaid', icon: 'checkbox', tint: 'coral' },
  { key: 'completed', labelKey: 'personal.orderCompleted', icon: 'checkmarkempty', tint: 'sand' }
]

const services = [
  { key: 'notes', labelKey: 'profile.notes', icon: 'compose', tint: 'green', path: '/pages/travelNote/mine' },
  { key: 'favorites', labelKey: 'profile.collection', icon: 'star', tint: 'gold', path: '/pages/collection/index' },
  { key: 'comments', labelKey: 'profile.comments', icon: 'chatbubble', tint: 'coral', path: '/pages/comment/index' },
  { key: 'points', labelKey: 'profile.points', icon: 'gift', tint: 'green', path: '/pages/points/index' },
  { key: 'pointsRecords', labelKey: 'page.pointsRecords', icon: 'list', tint: 'sand', path: '/pages/points/records' },
  { key: 'support', labelKey: 'profile.support', icon: 'headphones', tint: 'gold', path: '/pages/chat/index' },
  { key: 'settings', labelKey: 'profile.settings', icon: 'settings', tint: 'coral', path: '/pages/profile/settings' },
  { key: 'about', labelKey: 'profile.about', icon: 'info', tint: 'green', path: '/pages/profile/about' }
]

const displayName = computed(() =>
  userInfo.value.nickname || userInfo.value.realName || userInfo.value.username || t('profile.guestName')
)
const signature = computed(() => userInfo.value.signature || userInfo.value.bio || t('personal.noSignature'))
const avatar = computed(() => userInfo.value.avatar || '')
const points = computed(() => Number(userInfo.value.points) || 0)
const levelName = computed(() => memberInfo.value?.levelName || '')
const growth = computed(() => Number(memberInfo.value?.growth) || 0)
const isPaid = computed(() => Number(memberInfo.value?.isPaid) === 1)
const todaySigned = computed(() => Number(memberInfo.value?.todaySigned) === 1)
const signContinuous = computed(() => Number(memberInfo.value?.signContinuous) || 0)
const discount = computed(() => (memberInfo.value?.discount != null ? memberInfo.value.discount : null))
const pointsRate = computed(() => (memberInfo.value?.pointsRate != null ? memberInfo.value.pointsRate : null))

const growthPercent = computed(() => {
  const member = memberInfo.value
  if (!member) return 0
  const current = Number(member.growth) || 0
  const min = Number(member.levelMinGrowth) || 0
  const remain = Number(member.nextLevelGrowth) || 0
  if (remain <= 0) return 100
  const span = current - min + remain
  if (span <= 0) return 0
  return Math.max(4, Math.min(100, Math.round(((current - min) / span) * 100)))
})

const nextLevelText = computed(() => {
  const remain = Number(memberInfo.value?.nextLevelGrowth) || 0
  return remain > 0 ? t('personal.nextLevel', { n: remain }) : t('personal.maxLevel')
})

const compact = (value) => {
  const num = Number(value) || 0
  if (num >= 10000) return (num / 10000).toFixed(1).replace('.0', '') + 'w'
  if (num >= 1000) return (num / 1000).toFixed(1).replace('.0', '') + 'k'
  return String(num)
}

const go = (url) => {
  uni.navigateTo({ url })
}

const goLogin = () => {
  uni.navigateTo({ url: '/pages/login/index' })
}

const extract = (response) => (response && response.code === 200 ? response.data : null)

const loadUser = async () => {
  try {
    const data = extract(await getUserInfo())
    if (data) {
      userInfo.value = data
      uni.setStorageSync('userInfo', data)
    }
  } catch (error) {
    console.error('获取用户信息失败', error)
  }
}

const loadMember = async () => {
  try {
    const data = extract(await getMemberInfo())
    if (data) memberInfo.value = data
  } catch (error) {
    console.error('获取会员信息失败', error)
  }
}

const loadOrders = async () => {
  try {
    const data = extract(await getOrderList({ pageNum: 1, pageSize: 1 }))
    if (data) orderTotal.value = Number(data.total) || 0
  } catch (error) {
    console.error('获取订单失败', error)
  }
}

const loadNotes = async () => {
  try {
    const data = extract(await getMyNotes({ pageNum: 1, pageSize: 1 }))
    if (data) noteTotal.value = Number(data.total) || 0
  } catch (error) {
    console.error('获取笔记数量失败', error)
  }
}

const loadFavorites = async (userId) => {
  if (!userId) return
  try {
    const data = extract(await getFavoriteStats(userId))
    if (data) favoriteTotal.value = Number(data.total) || 0
  } catch (error) {
    console.error('获取收藏统计失败', error)
  }
}

const loadAll = async () => {
  isLoggedIn.value = !!uni.getStorageSync('token')
  if (!isLoggedIn.value) {
    loading.value = false
    return
  }

  const cached = uni.getStorageSync('userInfo')
  if (cached && typeof cached === 'object') userInfo.value = cached

  loading.value = true
  await Promise.all([loadUser(), loadMember(), loadOrders(), loadNotes()])
  await loadFavorites(userInfo.value.id || userInfo.value.userId)
  loading.value = false
}

onShow(loadAll)

onPullDownRefresh(async () => {
  await loadAll()
  uni.stopPullDownRefresh()
})

const changeAvatar = () => {
  if (!isLoggedIn.value) return goLogin()
  uni.chooseImage({
    count: 1,
    success: (result) => {
      const filePath = result.tempFilePaths && result.tempFilePaths[0]
      if (!filePath) return
      const token = uni.getStorageSync('token')
      uni.uploadFile({
        url: `${BASE_URL}/user/avatar`,
        filePath,
        name: 'avatar',
        header: token ? { Authorization: `Bearer ${token}` } : {},
        success: (uploadResult) => {
          try {
            const body = JSON.parse(uploadResult.data)
            if (body.code === 200 && body.data) {
              userInfo.value = { ...userInfo.value, avatar: body.data }
              uni.setStorageSync('userInfo', userInfo.value)
              uni.showToast({ title: t('personal.avatarUpdated'), icon: 'none' })
            } else {
              uni.showToast({ title: body.message || t('personal.avatarFailed'), icon: 'none' })
            }
          } catch (error) {
            uni.showToast({ title: t('personal.avatarFailed'), icon: 'none' })
          }
        },
        fail: () => {
          uni.showToast({ title: t('personal.avatarFailed'), icon: 'none' })
        }
      })
    }
  })
}

const openEdit = () => {
  editForm.nickname = userInfo.value.nickname || userInfo.value.username || ''
  editForm.signature = userInfo.value.signature || ''
  editForm.bio = userInfo.value.bio || ''
  editVisible.value = true
}

const saveProfile = async () => {
  if (saving.value) return
  saving.value = true
  try {
    const response = await updateUserInfo({
      id: userInfo.value.id,
      nickname: editForm.nickname,
      signature: editForm.signature,
      bio: editForm.bio
    })
    if (response && response.code === 200) {
      userInfo.value = {
        ...userInfo.value,
        nickname: editForm.nickname,
        signature: editForm.signature,
        bio: editForm.bio
      }
      uni.setStorageSync('userInfo', userInfo.value)
      editVisible.value = false
      uni.showToast({ title: t('personal.saved'), icon: 'none' })
    } else {
      uni.showToast({ title: response?.message || t('personal.saveFailed'), icon: 'none' })
    }
  } catch (error) {
    uni.showToast({ title: t('personal.saveFailed'), icon: 'none' })
  } finally {
    saving.value = false
  }
}

const handleSign = async () => {
  if (!isLoggedIn.value) return goLogin()
  if (todaySigned.value || signing.value) return
  signing.value = true
  try {
    const response = await signMember()
    if (response && response.code === 200) {
      if (response.data) memberInfo.value = response.data
      await loadUser()
      uni.showToast({ title: t('personal.signSuccess'), icon: 'none' })
    } else {
      uni.showToast({ title: response?.message || t('personal.signSuccess'), icon: 'none' })
    }
  } catch (error) {
    console.error('签到失败', error)
  } finally {
    signing.value = false
  }
}

const handleLogout = () => {
  uni.showModal({
    title: t('profile.logoutTitle'),
    content: t('profile.logoutContent'),
    success: async (result) => {
      if (!result.confirm) return
      try {
        await logoutApi()
      } catch (error) {
        // 退出接口失败也清理本地登录态
      }
      uni.removeStorageSync('token')
      uni.removeStorageSync('userInfo')
      isLoggedIn.value = false
      userInfo.value = {}
      memberInfo.value = null
      uni.reLaunch({ url: '/pages/login/index' })
    }
  })
}
</script>

<style scoped>
/* ============ 未登录 ============ */
.guest-card {
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 38px 24px 28px;
  border-radius: var(--radius-xl);
  background: linear-gradient(155deg, #235F4B 0%, #1A4C3D 55%, #113528 100%);
  box-shadow: var(--shadow-brand);
  color: #FFFDF8;
  text-align: center;
}

.guest-glow {
  position: absolute;
  top: -70px;
  right: -50px;
  width: 230px;
  height: 230px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255, 253, 248, 0.26), transparent 66%);
  pointer-events: none;
}

.guest-logo {
  position: relative;
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 18px;
  background: rgba(255, 253, 248, 0.16);
  border: 1px solid rgba(255, 253, 248, 0.26);
  -webkit-backdrop-filter: blur(10px);
  backdrop-filter: blur(10px);
}

.guest-title {
  position: relative;
  margin-top: 18px;
  font-size: 20px;
  font-weight: 800;
}

.guest-desc {
  position: relative;
  margin-top: 8px;
  font-size: 13px;
  line-height: 1.6;
  color: rgba(255, 253, 248, 0.74);
}

.guest-btn {
  position: relative;
  width: 100%;
  max-width: 280px;
  height: 48px;
  line-height: 48px;
  margin-top: 22px;
  border: none;
  border-radius: 999px;
  background: #FFFDF8;
  color: #1E5B48;
  font-size: 14px;
  font-weight: 700;
}

.guest-btn::after {
  border: none;
}

.guest-links {
  position: relative;
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 18px;
  font-size: 12px;
  color: rgba(255, 253, 248, 0.6);
}

.guest-link {
  color: rgba(255, 253, 248, 0.8);
}

/* ============ 用户头部 ============ */
.hero {
  position: relative;
  overflow: hidden;
  padding: 22px 20px 20px;
  border-radius: var(--radius-xl);
  background: linear-gradient(152deg, #235F4B 0%, #1A4C3D 58%, #103226 100%);
  box-shadow: var(--shadow-brand);
  color: #FFFDF8;
}

.hero-ring {
  position: absolute;
  border-radius: 50%;
  border: 1px solid rgba(255, 253, 248, 0.14);
  pointer-events: none;
}

.hero-ring--1 {
  width: 230px;
  height: 230px;
  right: -100px;
  top: -90px;
  background: radial-gradient(circle, rgba(255, 253, 248, 0.16), transparent 66%);
  border: none;
}

.hero-ring--2 {
  width: 170px;
  height: 170px;
  right: 10px;
  bottom: -110px;
}

.hero-main {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 14px;
}

.avatar {
  position: relative;
  flex: 0 0 auto;
  width: 68px;
  height: 68px;
  border-radius: 50%;
  padding: 3px;
  background: rgba(255, 253, 248, 0.22);
}

.avatar-img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid rgba(255, 253, 248, 0.7);
}

.avatar-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: rgba(255, 253, 248, 0.16);
  border: 2px solid rgba(255, 253, 248, 0.5);
}

.avatar-cam {
  position: absolute;
  right: -1px;
  bottom: 0;
  width: 22px;
  height: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #FFFDF8;
  border: 2px solid #1A4C3D;
  color: #1E5B48;
}

.hero-info {
  flex: 1;
  min-width: 0;
}

.name-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 7px;
}

.name {
  max-width: 100%;
  font-size: 20px;
  font-weight: 800;
  letter-spacing: -0.2px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.vip-chip,
.level-chip {
  padding: 2px 8px;
  border-radius: var(--radius-pill);
  font-size: 10px;
  font-weight: 700;
  white-space: nowrap;
}

.vip-chip {
  background: linear-gradient(135deg, #E9C46A, #C08A2E);
  color: #4A3400;
}

.level-chip {
  background: rgba(255, 253, 248, 0.18);
  border: 1px solid rgba(255, 253, 248, 0.26);
  color: #FFFDF8;
}

.sign {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-top: 6px;
  font-size: 12.5px;
  line-height: 1.5;
  color: rgba(255, 253, 248, 0.76);
}

.streak {
  display: inline-block;
  margin-top: 8px;
  padding: 2px 9px;
  border-radius: var(--radius-pill);
  background: rgba(255, 253, 248, 0.14);
  font-size: 10.5px;
  color: rgba(255, 253, 248, 0.86);
}

.hero-actions {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-top: 16px;
}

.edit-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 8px 14px;
  border-radius: var(--radius-pill);
  background: rgba(255, 253, 248, 0.14);
  border: 1px solid rgba(255, 253, 248, 0.24);
  font-size: 12px;
  font-weight: 600;
  color: #FFFDF8;
  transition: background-color 0.25s var(--ease-out);
}

.edit-btn:hover {
  background: rgba(255, 253, 248, 0.24);
}

.sign-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  height: 38px;
  margin: 0;
  padding: 0 20px;
  border: 1px solid transparent;
  border-radius: var(--radius-pill);
  background: #FFFDF8;
  color: #1E5B48;
  font-size: 13px;
  font-weight: 700;
  line-height: 38px;
  transition: transform 0.2s var(--ease-out), opacity 0.2s var(--ease-out);
}

.sign-btn::after {
  border: none;
}

.sign-btn:active {
  transform: scale(0.96);
}

.sign-btn.is-done {
  background: rgba(255, 253, 248, 0.16);
  border-color: rgba(255, 253, 248, 0.26);
  color: rgba(255, 253, 248, 0.8);
}

/* ============ 数据概览 ============ */
.stats-card {
  position: relative;
  z-index: 2;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  margin: -22px 10px 0;
  padding: 16px 8px;
  border-radius: var(--radius-lg);
  background: var(--card-background);
  border: 1px solid var(--card-border);
  box-shadow: var(--shadow-md);
}

.stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 3px;
  padding: 2px 4px;
  cursor: pointer;
  border-right: 1px solid var(--border-color);
  transition: transform 0.25s var(--ease-out);
}

.stat:last-child {
  border-right: none;
}

.stat:active {
  transform: scale(0.94);
}

.stat-num {
  font-size: 20px;
  font-weight: 800;
  color: var(--text-color);
  font-variant-numeric: tabular-nums;
}

.stat-label {
  font-size: 11px;
  color: var(--text-tertiary-color);
}

/* ============ 会员成长 ============ */
.panel {
  margin-top: 16px;
  padding: 18px;
  border-radius: var(--radius-lg);
  background: var(--card-background);
  border: 1px solid var(--card-border);
  box-shadow: var(--shadow-sm);
}

.panel-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.panel-titles {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.panel-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-color);
}

.panel-sub {
  font-size: 11.5px;
  color: var(--text-tertiary-color);
}

.growth {
  display: inline-flex;
  align-items: baseline;
  gap: 2px;
  color: var(--primary-color);
}

.growth-num {
  font-size: 22px;
  font-weight: 800;
  font-variant-numeric: tabular-nums;
}

.growth-unit {
  font-size: 10.5px;
  color: var(--text-tertiary-color);
}

.progress {
  height: 8px;
  margin-top: 14px;
  border-radius: var(--radius-pill);
  background: var(--background-soft);
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  border-radius: var(--radius-pill);
  background: linear-gradient(90deg, #1E5B48, #4F8A74);
  transition: width 0.6s var(--ease-out);
}

.member-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-top: 10px;
}

.member-tip {
  font-size: 11.5px;
  color: var(--text-tertiary-color);
}

.member-meta {
  display: flex;
  gap: 12px;
  font-size: 11px;
  font-weight: 600;
  color: var(--accent-color);
}

.sign-tip {
  display: block;
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px dashed var(--border-color);
  font-size: 11px;
  color: var(--text-tertiary-color);
}

/* ============ 订单 ============ */
.order-shortcuts {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
}

.shortcut {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 7px;
  padding: 14px 6px;
  border-radius: var(--radius-md);
  background: var(--card-background);
  border: 1px solid var(--card-border);
  box-shadow: var(--shadow-sm);
  cursor: pointer;
  transition: transform 0.3s var(--ease-out), box-shadow 0.3s var(--ease-out);
}

.shortcut:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-md);
}

.shortcut-icon {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-sm);
  background: var(--primary-soft);
  color: var(--primary-color);
}

.shortcut-label {
  font-size: 11.5px;
  font-weight: 500;
  color: var(--text-secondary-color);
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 12px;
}

.order-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: var(--radius-md);
  background: var(--card-background);
  border: 1px solid var(--card-border);
  box-shadow: var(--shadow-sm);
  cursor: pointer;
  transition: transform 0.3s var(--ease-out), box-shadow 0.3s var(--ease-out);
}

.order-item:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.order-thumb {
  flex: 0 0 48px;
  width: 48px;
  height: 48px;
  border-radius: var(--radius-sm);
  overflow: hidden;
  background: var(--surface-2);
}

.order-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.order-thumb-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--primary-color);
}

.order-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.order-name {
  font-size: 13.5px;
  font-weight: 600;
  color: var(--text-color);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.order-date {
  font-size: 10.5px;
  color: var(--text-tertiary-color);
}

.order-right {
  flex: 0 0 auto;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
}

.order-amount {
  font-size: 14px;
  font-weight: 800;
  color: var(--text-color);
  font-variant-numeric: tabular-nums;
}

.order-status {
  padding: 2px 8px;
  border-radius: var(--radius-pill);
  font-size: 10px;
  font-weight: 700;
}

.order-status.is-pending {
  background: rgba(192, 138, 46, 0.14);
  color: #A87218;
}

.order-status.is-paid {
  background: var(--primary-soft);
  color: var(--primary-color);
}

.order-status.is-shipped {
  background: var(--accent-soft);
  color: var(--accent-color);
}

.order-status.is-completed {
  background: rgba(47, 122, 85, 0.14);
  color: var(--success-color);
}

.order-status.is-canceled,
.order-status.is-refunded {
  background: var(--background-soft);
  color: var(--text-tertiary-color);
}

/* ============ 积分动态 ============ */
.points-list {
  padding: 4px 16px;
}

.points-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px dashed var(--border-color);
}

.points-item:last-child {
  border-bottom: none;
}

.points-main {
  display: flex;
  flex-direction: column;
  gap: 3px;
  min-width: 0;
}

.points-remark {
  font-size: 13px;
  color: var(--text-color);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.points-date {
  font-size: 10.5px;
  color: var(--text-tertiary-color);
}

.points-value {
  flex: 0 0 auto;
  font-size: 15px;
  font-weight: 800;
  color: var(--accent-color);
  font-variant-numeric: tabular-nums;
}

.points-value.is-income {
  color: var(--success-color);
}

/* ============ 常用服务 ============ */
.service-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
}

.service-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 14px 6px;
  border-radius: var(--radius-md);
  background: var(--card-background);
  border: 1px solid var(--card-border);
  box-shadow: var(--shadow-sm);
  cursor: pointer;
  transition: transform 0.3s var(--ease-out), box-shadow 0.3s var(--ease-out);
}

.service-item:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-md);
}

.service-icon {
  width: 42px;
  height: 42px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-sm);
}

.tint-green {
  background: var(--primary-soft);
  color: var(--primary-color);
}

.tint-gold {
  background: rgba(192, 138, 46, 0.14);
  color: #A87218;
}

.tint-coral {
  background: var(--accent-soft);
  color: var(--accent-color);
}

.service-label {
  font-size: 11.5px;
  color: var(--text-secondary-color);
  text-align: center;
}

/* ============ 空态 / 退出 ============ */
.empty-mini {
  margin-top: 12px;
  padding: 20px 16px;
  text-align: center;
  font-size: 12px;
  color: var(--text-tertiary-color);
}

.logout-btn {
  width: 100%;
  height: 48px;
  margin-top: 24px;
  border-radius: var(--radius-pill);
  background: var(--card-background);
  border: 1px solid rgba(240, 68, 56, 0.28);
  color: #F04438;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.28s var(--ease-out);
}

.logout-btn:hover {
  background: rgba(240, 68, 56, 0.08);
}

/* ============ 编辑资料弹层 ============ */
.sheet-mask {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  background: rgba(15, 21, 18, 0.45);
  -webkit-backdrop-filter: blur(4px);
  backdrop-filter: blur(4px);
}

.sheet {
  width: 100%;
  max-width: 480px;
  padding: 20px 20px calc(20px + env(safe-area-inset-bottom));
  border-radius: 24px 24px 0 0;
  background: var(--card-background);
  box-shadow: var(--shadow-lg);
  animation: sheetUp 0.32s var(--ease-out) both;
}

@keyframes sheetUp {
  from {
    transform: translateY(40px);
    opacity: 0;
  }

  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.sheet-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;
}

.sheet-title {
  font-size: 17px;
  font-weight: 800;
  color: var(--text-color);
}

.sheet-close {
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: var(--surface-2);
}

.sheet-field {
  margin-top: 14px;
}

.sheet-label {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-secondary-color);
}

.sheet-input-wrap {
  display: flex;
  align-items: center;
  height: 46px;
  margin-top: 7px;
  padding: 0 14px;
  border-radius: 13px;
  background: var(--surface-2);
  border: 1px solid var(--border-color);
  transition: border-color 0.25s var(--ease-out), box-shadow 0.25s var(--ease-out);
}

.sheet-input-wrap:focus-within {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px var(--primary-soft);
}

.sheet-input-wrap--area {
  height: auto;
  padding: 10px 14px;
}

.sheet-input {
  flex: 1;
  min-width: 0;
  height: 100%;
  border: none;
  outline: none;
  background: transparent;
  font-size: 14px;
  color: var(--text-color);
}

.sheet-textarea {
  width: 100%;
  height: 76px;
  border: none;
  outline: none;
  background: transparent;
  font-size: 14px;
  line-height: 1.6;
  color: var(--text-color);
  resize: none;
}

.sheet-actions {
  display: flex;
  gap: 10px;
  margin-top: 20px;
}

.sheet-btn {
  flex: 1;
  height: 46px;
  line-height: 46px;
  border: none;
  border-radius: var(--radius-pill);
  font-size: 14px;
  font-weight: 700;
}

.sheet-btn::after {
  border: none;
}

.sheet-btn--ghost {
  background: var(--surface-2);
  color: var(--text-secondary-color);
}

.sheet-btn--primary {
  background: linear-gradient(135deg, #235F4B, #1E5B48 55%, #2F7A55);
  color: #FFFDF8;
  box-shadow: var(--shadow-brand);
}

/* ============ PC ============ */
@media (min-width: 768px) {
  .hero {
    padding: 28px 30px 24px;
  }

  .avatar {
    width: 84px;
    height: 84px;
  }

  .name {
    font-size: 24px;
  }

  .panel {
    padding: 22px 24px;
  }

  .order-list {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 14px;
  }

  .service-grid {
    grid-template-columns: repeat(8, 1fr);
  }

  .sheet-mask {
    align-items: center;
  }

  .sheet {
    border-radius: 24px;
  }
}

/* ============ 细节与交互增强 ============ */
.top-loading {
  height: 3px;
  margin: 0 2px 10px;
  border-radius: var(--radius-pill);
  background: linear-gradient(90deg, transparent, var(--primary-color), transparent);
  background-size: 200% 100%;
  animation: loadingSlide 1.2s linear infinite;
}

@keyframes loadingSlide {
  0% {
    background-position: 200% 0;
  }

  100% {
    background-position: -200% 0;
  }
}

.hero {
  padding-bottom: 34px;
}

.hero-glow {
  position: absolute;
  inset: 0;
  pointer-events: none;
  background:
    radial-gradient(340px 190px at 12% 0%, rgba(255, 253, 248, 0.18), transparent 70%),
    linear-gradient(118deg, transparent 54%, rgba(255, 253, 248, 0.05) 54.4%, transparent 55%);
}

.hero-main {
  align-items: flex-start;
}

.avatar {
  transition: transform 0.35s var(--ease-spring);
}

.avatar:hover {
  transform: scale(1.04);
}

.avatar-cam {
  transition: transform 0.3s var(--ease-spring);
}

.avatar:hover .avatar-cam {
  transform: scale(1.16);
}

.edit-btn {
  flex: 0 0 auto;
  width: 34px;
  height: 34px;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: rgba(255, 253, 248, 0.14);
  border: 1px solid rgba(255, 253, 248, 0.24);
  transition: transform 0.3s var(--ease-spring), background-color 0.25s var(--ease-out);
}

.edit-btn:hover {
  transform: rotate(8deg) scale(1.08);
  background: rgba(255, 253, 248, 0.26);
}

.hero-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 9px;
}

.level-chip {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  padding: 2px 9px;
  border-radius: var(--radius-pill);
  background: rgba(255, 253, 248, 0.16);
  border: 1px solid rgba(255, 253, 248, 0.24);
  font-size: 10.5px;
  font-weight: 700;
  color: #FFFDF8;
}

.streak-chip {
  padding: 2px 9px;
  border-radius: var(--radius-pill);
  background: rgba(233, 196, 106, 0.18);
  border: 1px solid rgba(233, 196, 106, 0.32);
  color: #F3D9A4;
  font-size: 10.5px;
  font-weight: 600;
}

.hero-foot {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 16px;
  padding-top: 14px;
  border-top: 1px solid rgba(255, 253, 248, 0.14);
}

.hero-growth {
  display: flex;
  align-items: baseline;
  gap: 5px;
}

.hero-growth-label {
  font-size: 10.5px;
  color: rgba(255, 253, 248, 0.62);
}

.hero-growth-value {
  font-size: 17px;
  font-weight: 800;
  color: #FFFDF8;
  font-variant-numeric: tabular-nums;
}

.sign-btn {
  box-shadow: 0 6px 18px rgba(6, 24, 18, 0.18);
}

.sign-btn:not(.is-done) {
  animation: signGlow 2.8s ease-in-out infinite;
}

@keyframes signGlow {

  0%,
  100% {
    box-shadow: 0 6px 18px rgba(6, 24, 18, 0.18);
  }

  50% {
    box-shadow: 0 6px 24px rgba(255, 253, 248, 0.36);
  }
}

/* 数据概览 */
.stats-card {
  margin: -22px 8px 0;
  padding: 14px 4px;
  -webkit-backdrop-filter: blur(10px);
  backdrop-filter: blur(10px);
}

.stat {
  gap: 5px;
  padding: 4px 2px;
}

.stat:active {
  transform: scale(0.92);
}

.stat-icon {
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
}

.stat-num {
  font-size: 18px;
  letter-spacing: -0.3px;
}

/* 会员成长 */
.panel-head {
  align-items: center;
  gap: 10px;
}

.level-badge {
  flex: 0 0 auto;
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  background: linear-gradient(140deg, rgba(192, 138, 46, 0.24), rgba(192, 138, 46, 0.06));
  border: 1px solid rgba(192, 138, 46, 0.24);
}

.panel-titles {
  flex: 1;
  min-width: 0;
}

.progress {
  position: relative;
  height: 9px;
}

.progress-fill {
  position: relative;
  background: linear-gradient(90deg, #1E5B48, #4F8A74 60%, #8FC3AE);
  box-shadow: 0 2px 10px rgba(30, 91, 72, 0.26);
}

.progress-fill::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.42), transparent);
  animation: progressSheen 2.8s ease-in-out infinite;
}

@keyframes progressSheen {
  0% {
    transform: translateX(-100%);
  }

  60%,
  100% {
    transform: translateX(100%);
  }
}

.member-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  justify-content: flex-end;
}

.member-foot {
  flex-wrap: wrap;
}

.empty-mini {
  border-style: dashed;
  background: transparent;
  box-shadow: none;
}

.member-chip {
  padding: 2px 9px;
  border-radius: var(--radius-pill);
  background: var(--accent-soft);
  color: var(--accent-color);
  font-size: 10px;
  font-weight: 700;
  white-space: nowrap;
}

.sign-tip {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--text-tertiary-color);
}

/* 订单 */
.shortcut:active {
  transform: scale(0.94);
}

.shortcut-icon {
  width: 38px;
  height: 38px;
  border-radius: 13px;
}

.order-item {
  position: relative;
  overflow: hidden;
  border-left: 3px solid transparent;
}

.order-item:active {
  transform: scale(0.99);
}

.order-item.is-pending {
  border-left-color: #C08A2E;
}

.order-item.is-paid {
  border-left-color: var(--primary-color);
}

.order-item.is-shipped {
  border-left-color: var(--accent-color);
}

.order-item.is-completed {
  border-left-color: var(--success-color);
}

.order-item.is-canceled,
.order-item.is-refunded {
  border-left-color: var(--border-strong);
}

.order-img {
  transition: transform 0.5s var(--ease-out);
}

.order-item:hover .order-img {
  transform: scale(1.08);
}

/* 积分动态 */
.points-item {
  gap: 12px;
}

.points-icon {
  flex: 0 0 auto;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: var(--accent-soft);
  color: var(--accent-color);
}

.points-icon.is-income {
  background: rgba(47, 122, 85, 0.14);
  color: var(--success-color);
}

.points-main {
  flex: 1;
}

/* 常用服务 */
.tint-sand {
  background: var(--background-soft);
  color: var(--text-secondary-color);
}

.service-item:active {
  transform: scale(0.94);
}

.service-icon {
  transition: transform 0.3s var(--ease-spring);
}

.service-item:hover .service-icon {
  transform: scale(1.1);
}

/* 弹层与按钮 */
.sheet-handle {
  width: 38px;
  height: 4px;
  margin: 0 auto 12px;
  border-radius: var(--radius-pill);
  background: var(--border-strong);
  opacity: 0.7;
}

.logout-btn:active {
  transform: scale(0.99);
}

@media (prefers-reduced-motion: reduce) {

  .top-loading,
  .sign-btn:not(.is-done),
  .progress-fill::after {
    animation: none;
  }
}
</style>
