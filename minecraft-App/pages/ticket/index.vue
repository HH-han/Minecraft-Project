<template>
  <div class="app-page app-page--plain">
    <div class="shell">
      <div class="empty-state" style="padding-top: 72px">
        <div class="empty-state__icon">
          <uni-icons type="wallet" size="26" color="#2F6BFF" />
        </div>
        {{ t('placeholder.ticket') }}
      </div>
    </div>
  </div>
</template>

<script setup>
import UniIcons from '@/uni_modules/uni-icons/components/uni-icons/uni-icons.vue'
import { t, usePageTitle } from '@/utils/i18n'

usePageTitle('page.ticket')
</script>
