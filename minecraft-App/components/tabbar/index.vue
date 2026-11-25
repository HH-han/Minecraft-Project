<template>
  <view class="float-tabbar">
    <view class="float-tabbar__bar">
      <view v-for="item in tabList" :key="item.key" class="tab-item"
        :class="{ 'is-active': current === item.key, 'is-center': item.center }" @click="handleTap(item)">
        <view v-if="item.center" class="tab-center">
          <uni-icons type="scan" size="24" color="#FFFFFF" />
        </view>
        <uni-icons v-else :type="iconType(item)" size="22" color="currentColor" />
        <text class="tab-label">{{ t(item.label) }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { onMounted } from 'vue'
import UniIcons from '@/uni_modules/uni-icons/components/uni-icons/uni-icons.vue'
import { t } from '@/utils/i18n'

const props = defineProps({
  // 当前页面标识：home / discover / news / profile
  current: {
    type: String,
    default: 'home'
  }
})

// 底部导航项：中间为扫码登录，独立页面，不参与 Tab 切换
const tabList = [
  { key: 'home', label: 'tabbar.home', icon: 'home', activeIcon: 'home-filled', path: '/pages/index/index' },
  { key: 'discover', label: 'tabbar.discover', icon: 'map', activeIcon: 'map-filled', path: '/pages/discover/index' },
  { key: 'scan', label: 'tabbar.scan', icon: 'scan', path: '/pages/login/scan', center: true },
  { key: 'news', label: 'tabbar.news', icon: 'paperplane', activeIcon: 'paperplane-filled', path: '/pages/news/index' },
  { key: 'profile', label: 'tabbar.profile', icon: 'person', activeIcon: 'person-filled', path: '/pages/personalcenter/index' }
]

const iconType = (item) => (props.current === item.key && item.activeIcon ? item.activeIcon : item.icon)

// 隐藏原生 TabBar，由悬浮导航接管
const hideNativeTabBar = () => {
  try {
    uni.hideTabBar({ animation: false, fail: () => {} })
  } catch (error) {
    // 非 TabBar 环境忽略
  }
}

const handleTap = (item) => {
  if (item.center) {
    uni.navigateTo({ url: item.path })
    return
  }
  if (props.current === item.key) {
    return
  }
  uni.switchTab({
    url: item.path,
    fail: () => {
      uni.reLaunch({ url: item.path })
    }
  })
}

onMounted(() => {
  hideNativeTabBar()
  // 部分平台 TabBar 渲染晚于组件挂载，延迟兜底
  setTimeout(hideNativeTabBar, 60)
})
</script>

<style scoped>
/* 悬浮导航容器：移动端通栏留边，PC 端居中限宽 */
.float-tabbar {
  position: fixed;
  left: 50%;
  bottom: calc(12px + env(safe-area-inset-bottom, 0px));
  transform: translateX(-50%);
  z-index: 900;
  width: calc(100% - 28px);
  max-width: 560px;
}

/* 导航条 */
.float-tabbar__bar {
  position: relative;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  height: 62px;
  padding: 0 10px;
  border-radius: 24px;
  background: var(--glass-background);
  -webkit-backdrop-filter: blur(18px);
  backdrop-filter: blur(18px);
  border: 1px solid var(--card-border);
  box-shadow: var(--shadow-lg);
}

/* 导航项 */
.tab-item {
  flex: 1;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-end;
  gap: 4px;
  padding: 9px 0 10px;
  color: var(--text-tertiary-color);
  cursor: pointer;
  transition: color 0.25s var(--ease-out), transform 0.25s var(--ease-out);
}

.tab-item:active {
  transform: scale(0.92);
}

.tab-item.is-active {
  color: var(--primary-color);
}

.tab-label {
  font-size: 11px;
  font-weight: 500;
  line-height: 1.1;
  white-space: nowrap;
}

.tab-item.is-active .tab-label {
  font-weight: 700;
}

/* 中间扫码：上浮圆形按钮 */
.tab-item.is-center {
  padding: 0 0 10px;
}

.tab-item.is-center .tab-label {
  color: var(--primary-color);
  font-weight: 600;
}

.tab-center {
  width: 52px;
  height: 52px;
  margin-top: -26px;
  margin-bottom: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  color: #fff;
  background: var(--primary-color);
  border: 3px solid var(--card-background);
  box-shadow: var(--shadow-brand);
  transition: transform 0.3s var(--ease-spring);
}

.tab-item.is-center:active .tab-center {
  transform: scale(0.9);
}

/* PC 端：更紧凑的悬浮胶囊 */
@media (min-width: 768px) {
  .float-tabbar {
    bottom: calc(18px + env(safe-area-inset-bottom, 0px));
    max-width: 520px;
  }

  .float-tabbar__bar {
    height: 64px;
    padding: 0 14px;
    border-radius: 26px;
  }

  .tab-label {
    font-size: 12px;
  }
}
</style>
