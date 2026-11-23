<script lang="ts" setup>
import type { UploadChangeParam } from 'antdv-next';

import { computed, ref, watch } from 'vue';

import { ImagePlus, LoaderCircle } from '@vben/icons';

import { Upload } from 'antdv-next';

interface Props {
  /** 已上传图片地址（为空表示未上传） */
  src?: string;
  /** 未上传时的主文案 */
  uploadText: string;
  /** 已上传时的主文案 */
  changeText: string;
  /** 辅助说明文案 */
  tip?: string;
  /** 图片 alt */
  alt?: string;
  /** 上传中 */
  loading?: boolean;
  /** 禁用 */
  disabled?: boolean;
  /** 允许的文件类型 */
  accept?: string;
  /** 自定义上传前置处理，默认只选择文件不自动上传 */
  beforeUpload?: (file: any) => boolean | Promise<boolean>;
  /** 展示形态：card 卡片（主图字段）/ button 紧凑按钮（工具栏、行内） */
  variant?: 'button' | 'card';
}

const props = withDefaults(defineProps<Props>(), {
  accept: 'image/*',
  alt: '',
  beforeUpload: () => false,
  disabled: false,
  loading: false,
  src: '',
  tip: '',
  variant: 'card',
});

const emit = defineEmits<{
  change: [event: UploadChangeParam];
}>();

/** 图片加载失败时退回占位图标 */
const broken = ref(false);
const hasImage = computed(() => Boolean(props.src));

watch(
  () => props.src,
  () => {
    broken.value = false;
  },
);
</script>

<template>
  <Upload
    class="image-upload-field"
    :class="`image-upload-field--${variant}`"
    :accept="accept"
    :before-upload="beforeUpload"
    :disabled="disabled || loading"
    :max-count="1"
    :show-upload-list="false"
    @change="emit('change', $event)"
  >
    <!-- 卡片形态：主图 / 封面上传 -->
    <div
      v-if="variant === 'card'"
      class="iuf-card"
      :class="{ 'iuf-card--disabled': disabled, 'iuf-card--loading': loading }"
    >
      <div class="iuf-preview" :class="{ 'iuf-preview--empty': !hasImage }">
        <img
          v-if="hasImage && !broken"
          :src="src"
          :alt="alt"
          class="iuf-img"
          @error="broken = true"
        />
        <ImagePlus v-else class="iuf-placeholder" />
        <span v-if="hasImage && !broken" class="iuf-mask">
          <ImagePlus class="size-5" />
        </span>
      </div>
      <div class="iuf-body">
        <span class="iuf-title">
          {{ hasImage ? changeText : uploadText }}
        </span>
        <span v-if="tip" class="iuf-desc">{{ tip }}</span>
      </div>
      <span class="iuf-action">
        <LoaderCircle v-if="loading" class="iuf-spin size-4" />
        <ImagePlus v-else class="size-4" />
      </span>
    </div>

    <!-- 紧凑按钮形态：工具栏 / 行内上传 -->
    <div
      v-else
      class="iuf-button"
      :class="{ 'iuf-button--disabled': disabled, 'iuf-button--loading': loading }"
    >
      <LoaderCircle v-if="loading" class="iuf-spin size-4" />
      <ImagePlus v-else class="size-4" />
      <span>{{ hasImage ? changeText : uploadText }}</span>
    </div>
  </Upload>
</template>

<style scoped>
/* 卡片形态需要撑满整行（Upload 默认 inline-block 会收缩宽度） */
.image-upload-field--card :deep(.ant-upload-select) {
  display: block;
  width: 100%;
}

.iuf-card {
  display: flex;
  gap: 14px;
  align-items: center;
  width: 100%;
  padding: 12px 16px;
  cursor: pointer;
  background:
    radial-gradient(
      120% 160% at 0% 0%,
      hsl(var(--primary) / 10%),
      transparent 60%
    ),
    hsl(var(--muted) / 35%);
  border: 1px dashed hsl(var(--border));
  border-radius: 12px;
  transition:
    border-color 0.25s ease,
    background-color 0.25s ease,
    box-shadow 0.25s ease;
}

.iuf-card:hover {
  background:
    radial-gradient(
      120% 160% at 0% 0%,
      hsl(var(--primary) / 14%),
      transparent 60%
    ),
    hsl(var(--primary) / 4%);
  border-color: hsl(var(--primary) / 55%);
  box-shadow: 0 8px 20px -12px hsl(var(--primary) / 60%);
}

.iuf-card--loading {
  cursor: progress;
}

.iuf-card--disabled,
.iuf-card--disabled:hover {
  cursor: not-allowed;
  opacity: 0.65;
  border-color: hsl(var(--border));
  box-shadow: none;
}

/* 图片预览 + 渐变描边 + 悬浮遮罩 */
.iuf-preview {
  position: relative;
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  width: 60px;
  height: 60px;
  padding: 2px;
  background: linear-gradient(
    140deg,
    hsl(var(--primary) / 60%),
    hsl(var(--primary) / 8%)
  );
  border-radius: 50%;
  transition: transform 0.25s ease;
}

.iuf-preview--empty {
  background: hsl(var(--muted) / 70%);
  border: 1px dashed hsl(var(--border));
}

.iuf-card:hover .iuf-preview {
  transform: scale(1.05);
}

.iuf-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
}

.iuf-placeholder {
  width: 24px;
  height: 24px;
  color: hsl(var(--muted-foreground));
}

.iuf-mask {
  position: absolute;
  inset: 2px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: hsl(0deg 0% 100%);
  pointer-events: none;
  background: hsl(0deg 0% 0% / 45%);
  border-radius: 50%;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.iuf-card:hover .iuf-mask {
  opacity: 1;
}

.iuf-body {
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
  text-align: left;
}

.iuf-title {
  font-size: 14px;
  font-weight: 600;
  color: hsl(var(--foreground));
  transition: color 0.2s ease;
}

.iuf-card:hover .iuf-title {
  color: hsl(var(--primary));
}

.iuf-desc {
  font-size: 12px;
  line-height: 1.5;
  color: hsl(var(--muted-foreground));
}

/* 右侧圆形图标按钮 */
.iuf-action {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  color: hsl(var(--muted-foreground));
  background: hsl(var(--card));
  border: 1px solid hsl(var(--border));
  border-radius: 50%;
  transition:
    color 0.25s ease,
    border-color 0.25s ease,
    background-color 0.25s ease,
    transform 0.25s ease;
}

.iuf-card:hover .iuf-action {
  color: hsl(var(--primary));
  background: hsl(var(--primary) / 12%);
  border-color: hsl(var(--primary) / 55%);
  transform: translateX(2px);
}

/* 紧凑按钮形态 */
.iuf-button {
  display: inline-flex;
  gap: 6px;
  align-items: center;
  height: 32px;
  padding: 0 14px;
  font-size: 14px;
  font-weight: 500;
  line-height: 1;
  color: hsl(var(--primary));
  white-space: nowrap;
  cursor: pointer;
  background: hsl(var(--primary) / 6%);
  border: 1px dashed hsl(var(--primary) / 45%);
  border-radius: 8px;
  transition:
    color 0.25s ease,
    background-color 0.25s ease,
    border-color 0.25s ease,
    box-shadow 0.25s ease;
}

.iuf-button:hover {
  background: hsl(var(--primary) / 12%);
  border-color: hsl(var(--primary) / 70%);
  box-shadow: 0 4px 12px -6px hsl(var(--primary) / 60%);
}

.iuf-button--loading {
  cursor: progress;
}

.iuf-button--disabled,
.iuf-button--disabled:hover {
  color: hsl(var(--muted-foreground));
  cursor: not-allowed;
  background: hsl(var(--muted) / 50%);
  border-color: hsl(var(--border));
  box-shadow: none;
}

.iuf-spin {
  animation: iuf-spin 1s linear infinite;
}

@keyframes iuf-spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
