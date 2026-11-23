<template>
  <!-- 通用数据展示切换：卡片 / 列表 -->
  <div class="view-switch" role="group" :aria-label="ariaLabel">
    <button
      type="button"
      class="view-switch__btn"
      :class="{ 'is-active': modelValue === 'card' }"
      :aria-pressed="modelValue === 'card'"
      :title="cardLabel"
      @click="update('card')"
    >
      <el-icon><Grid /></el-icon>
      <span>{{ cardLabel }}</span>
    </button>
    <button
      type="button"
      class="view-switch__btn"
      :class="{ 'is-active': modelValue === 'list' }"
      :aria-pressed="modelValue === 'list'"
      :title="listLabel"
      @click="update('list')"
    >
      <el-icon><List /></el-icon>
      <span>{{ listLabel }}</span>
    </button>
  </div>
</template>

<script setup>
import { Grid, List } from '@element-plus/icons-vue'

defineProps({
  /** 当前展示模式：list / card */
  modelValue: { type: String, default: 'list' },
  /** 无障碍分组说明 */
  ariaLabel: { type: String, default: '' },
  /** 卡片模式文案（由调用方传入国际化后的文本） */
  cardLabel: { type: String, default: '' },
  /** 列表模式文案 */
  listLabel: { type: String, default: '' }
})

const emit = defineEmits(['update:modelValue'])

const update = (value) => emit('update:modelValue', value)
</script>

<style scoped>
.view-switch {
  display: inline-flex;
  gap: 2px;
  padding: 3px;
  border-radius: 12px;
  background: var(--theme-bg-surface-2);
  border: 1px solid var(--theme-hairline);
}

.view-switch__btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border: 0;
  border-radius: 9px;
  background: transparent;
  color: var(--theme-text-2);
  font-family: inherit;
  font-size: 12.5px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.18s ease, color 0.18s ease, box-shadow 0.18s ease;
}

.view-switch__btn:hover {
  color: var(--theme-primary);
}

.view-switch__btn.is-active {
  background: var(--theme-bg-surface);
  color: var(--theme-primary);
  box-shadow: var(--theme-shadow-sm);
}

.view-switch__btn:focus-visible {
  outline: 2px solid var(--theme-primary);
  outline-offset: 2px;
}

@media (max-width: 720px) {
  .view-switch__btn {
    padding: 6px 10px;
  }
}
</style>
