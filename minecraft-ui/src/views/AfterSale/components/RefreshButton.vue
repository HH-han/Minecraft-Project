<template>
  <!-- 售后模块统一刷新按钮：列表 / 订单区共用同一视觉与交互 -->
  <el-button round :loading="loading" @click="handleClick">
    <el-icon class="as-refresh-btn__icon"><Refresh /></el-icon>
    <span>{{ text }}</span>
  </el-button>
</template>

<script setup>
import { Refresh } from '@element-plus/icons-vue'

defineProps({
  /** 按钮文案（由调用方传入国际化后的文本） */
  text: { type: String, required: true },
  /** 加载状态 */
  loading: { type: Boolean, default: false }
})

const emit = defineEmits(['click'])

const handleClick = () => emit('click')
</script>

<style scoped>
.as-refresh-btn__icon {
  margin-right: 4px;
}
</style>
