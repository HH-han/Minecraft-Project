<template>
  <div class="app-page app-page--plain">
    <div class="shell">
      <header class="head reveal">
        <div class="page-eyebrow">{{ t('settings.eyebrow') }}</div>
        <h1 class="page-title">{{ t('settings.title') }}</h1>
        <p class="page-sub">{{ t('settings.sub') }}</p>
      </header>

      <div class="section-head">
        <div class="section-title">{{ t('settings.general') }}</div>
      </div>
      <div class="panel reveal reveal-delay-1">
        <!-- 深色模式 -->
        <div class="settings-item" @click="toggleThemeMode">
          <div class="item-left">
            <span class="item-icon item-icon--violet">
              <uni-icons type="color-filled" size="17" color="#7A8B5E" />
            </span>
            <span class="settings-label">{{ t('settings.darkMode') }}</span>
          </div>
          <div class="item-right">
            <div class="switch" :class="{ 'is-on': isDark }">
              <span class="switch-dot"></span>
            </div>
          </div>
        </div>

        <!-- 语言切换 -->
        <div class="settings-item" @click="changeLocale">
          <div class="item-left">
            <span class="item-icon item-icon--blue">
              <uni-icons type="font" size="17" color="#1E5B48" />
            </span>
            <span class="settings-label">{{ t('settings.language') }}</span>
          </div>
          <div class="item-right">
            <span class="settings-desc">{{ currentLocaleLabel }}</span>
            <uni-icons type="right" size="15" color="#8C948B" />
          </div>
        </div>

        <div class="settings-item" @click="handlePlaceholder(t('settings.preferences'))">
          <div class="item-left">
            <span class="item-icon item-icon--blue">
              <uni-icons type="settings" size="17" color="#1E5B48" />
            </span>
            <span class="settings-label">{{ t('settings.preferences') }}</span>
          </div>
          <div class="item-right">
            <span class="settings-desc">{{ t('settings.preferencesDesc') }}</span>
            <uni-icons type="right" size="15" color="#8C948B" />
          </div>
        </div>

        <div class="settings-item" @click="clearCache">
          <div class="item-left">
            <span class="item-icon item-icon--orange">
              <uni-icons type="trash" size="17" color="#C4613C" />
            </span>
            <span class="settings-label">{{ t('settings.clearCache') }}</span>
          </div>
          <div class="item-right">
            <span class="settings-desc">{{ t('settings.clearCacheDesc') }}</span>
            <uni-icons type="right" size="15" color="#8C948B" />
          </div>
        </div>

        <div class="settings-item" @click="handlePlaceholder(t('settings.checkUpdate'))">
          <div class="item-left">
            <span class="item-icon item-icon--green">
              <uni-icons type="refresh" size="17" color="#2F7A55" />
            </span>
            <span class="settings-label">{{ t('settings.checkUpdate') }}</span>
          </div>
          <div class="item-right">
            <span class="settings-desc">v1.0.0</span>
            <uni-icons type="right" size="15" color="#8C948B" />
          </div>
        </div>
      </div>

      <div class="section-head">
        <div class="section-title">{{ t('settings.account') }}</div>
      </div>
      <div class="panel reveal reveal-delay-2">
        <div class="settings-item" @click="handlePlaceholder(t('settings.accountSecurity'))">
          <div class="item-left">
            <span class="item-icon item-icon--blue">
              <uni-icons type="locked" size="17" color="#1E5B48" />
            </span>
            <span class="settings-label">{{ t('settings.accountSecurity') }}</span>
          </div>
          <div class="item-right">
            <span class="settings-desc">{{ t('settings.accountSecurityDesc') }}</span>
            <uni-icons type="right" size="15" color="#8C948B" />
          </div>
        </div>

        <div class="settings-item" @click="handlePlaceholder(t('settings.privacy'))">
          <div class="item-left">
            <span class="item-icon item-icon--violet">
              <uni-icons type="notification" size="17" color="#7A8B5E" />
            </span>
            <span class="settings-label">{{ t('settings.privacy') }}</span>
          </div>
          <div class="item-right">
            <span class="settings-desc">{{ t('settings.privacyDesc') }}</span>
            <uni-icons type="right" size="15" color="#8C948B" />
          </div>
        </div>

        <div class="settings-item" @click="handlePlaceholder(t('settings.feedback'))">
          <div class="item-left">
            <span class="item-icon item-icon--coral">
              <uni-icons type="chat" size="17" color="#C4613C" />
            </span>
            <span class="settings-label">{{ t('settings.feedback') }}</span>
          </div>
          <div class="item-right">
            <span class="settings-desc">{{ t('settings.feedbackDesc') }}</span>
            <uni-icons type="right" size="15" color="#8C948B" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { getTheme, setTheme } from '@/utils/theme'
import { t, usePageTitle, locale, toggleLocale } from '@/utils/i18n'
import UniIcons from '@/uni_modules/uni-icons/components/uni-icons/uni-icons.vue'

usePageTitle('page.settings')

const isDark = ref(getTheme() === 'dark')

const currentLocaleLabel = computed(() =>
  locale.value === 'en-US' ? t('settings.languageValueEn') : t('settings.languageValueZh')
)

const toggleThemeMode = () => {
  const next = isDark.value ? 'light' : 'dark'
  isDark.value = next === 'dark'
  setTheme(next)
  uni.showToast({
    title: isDark.value ? t('settings.darkOn') : t('settings.darkOff'),
    icon: 'none',
    position: 'bottom'
  })
}

const changeLocale = () => {
  toggleLocale()
  uni.showToast({
    title: t('settings.localeChanged'),
    icon: 'none',
    position: 'bottom'
  })
}

const clearCache = () => {
  uni.showModal({
    title: t('settings.cacheTitle'),
    content: t('settings.cacheContent'),
    success: (res) => {
      if (res.confirm) {
        uni.showToast({ title: t('settings.cacheDone'), icon: 'none', position: 'bottom' })
      }
    }
  })
}

const handlePlaceholder = (name) => {
  uni.showToast({ title: t('common.building', { name }), icon: 'none', position: 'bottom' })
}
</script>

<style scoped>
.head {
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

.panel {
  background: var(--card-background);
  border: 1px solid var(--card-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
}

.settings-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 16px;
  border-bottom: 1px solid var(--border-color);
  cursor: pointer;
  transition: background-color 0.25s var(--ease-out);
}

.settings-item:last-child {
  border-bottom: none;
}

.settings-item:hover {
  background: var(--surface-2);
}

.item-left {
  display: flex;
  align-items: center;
  gap: 11px;
  min-width: 0;
}

.item-icon {
  flex: 0 0 auto;
  width: 34px;
  height: 34px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-sm);
}

.item-icon--blue {
  background: var(--primary-soft);
}

.item-icon--violet {
  background: rgba(122, 139, 94, 0.16);
}

.item-icon--orange {
  background: rgba(196, 97, 60, 0.14);
}

.item-icon--green {
  background: rgba(47, 122, 85, 0.14);
}

.item-icon--coral {
  background: rgba(196, 97, 60, 0.12);
}

.settings-label {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-color);
}

.item-right {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.settings-desc {
  font-size: 12px;
  color: var(--text-tertiary-color);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 开关 */
.switch {
  position: relative;
  width: 46px;
  height: 26px;
  border-radius: var(--radius-pill);
  background: var(--background-soft);
  border: 1px solid var(--border-color);
  transition: background 0.3s var(--ease-out), border-color 0.3s var(--ease-out);
}

.switch.is-on {
  background: var(--primary-color);
  border-color: transparent;
  box-shadow: 0 6px 16px rgba(30, 91, 72, 0.28);
}

.switch-dot {
  position: absolute;
  top: 2px;
  left: 2px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: #fff;
  box-shadow: 0 2px 6px rgba(38, 45, 36, 0.25);
  transition: transform 0.32s var(--ease-spring);
}

.switch.is-on .switch-dot {
  transform: translateX(20px);
}

@media (min-width: 768px) {
  .page-title {
    font-size: 32px;
  }

  .settings-item {
    padding: 16px 22px;
  }
}
</style>
