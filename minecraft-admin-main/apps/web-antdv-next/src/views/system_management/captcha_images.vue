<script lang="ts" setup>
import type { TableColumnsType } from 'antdv-next';

import type { CaptchaImageItem } from '#/api/management/system/captcha';

import { computed, onMounted, ref } from 'vue';

import { Page } from '@vben/common-ui';
import { Search } from '@vben/icons';
import { $t } from '@vben/locales';

import {
  Button,
  Card,
  Image,
  Input,
  message,
  Popconfirm,
  Result,
  Skeleton,
  Table,
  Tag,
  Upload,
} from 'antdv-next';

import {
  deleteCaptchaImage,
  getCaptchaImageList,
  uploadCaptchaImage,
} from '#/api/management/system/captcha';

defineOptions({ name: 'CaptchaImages' });

/** 与后端 CaptchaImageServiceImpl.MAX_FILE_SIZE 保持一致 */
const MAX_FILE_SIZE = 5 * 1024 * 1024;

// =========================
// 状态
// =========================
const loading = ref(false);
const uploading = ref(false);
const errorMsg = ref('');

const images = ref<CaptchaImageItem[]>([]);
const pageNum = ref(1);
const pageSize = ref(12);
const pages = ref(0);
const total = ref(0);
const keyword = ref('');

// =========================
// 工具
// =========================
function displayValue(value: any): string {
  if (value === null || value === undefined || value === '') return '-';
  return String(value);
}

function formatTime(value?: null | string): string {
  if (!value) return '-';
  return String(value).replace('T', ' ');
}

function toNumber(value: any): number {
  const n = Number(value);
  return Number.isFinite(n) ? n : 0;
}

function formatFileSize(bytes?: null | number): string {
  const size = toNumber(bytes);
  if (size <= 0) return '-';
  if (size < 1024) return `${size} B`;
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`;
  return `${(size / 1024 / 1024).toFixed(2)} MB`;
}

/** 相对路径补全为同源静态资源地址（兼容历史 localhost / 物理目录地址） */
function resolveImageUrl(url?: null | string): string {
  if (!url) return '';
  let raw = String(url).trim();
  if (!raw || raw === 'null' || raw === 'undefined') return '';
  raw = raw
    .replace(/^https?:\/\/(?:localhost|127\.0\.0\.1)(?::\d+)?/i, '')
    .replace('/www/wwwroot/springboot/upload/images/', '/upload/');
  if (/^https?:\/\//i.test(raw)) return raw;
  return raw.startsWith('/') ? raw : `/${raw}`;
}

function statusInfo(value?: null | number): { color: string; text: string } {
  return Number(value) === 1
    ? { color: 'green', text: $t('system.captcha_image_page.enabled') }
    : { color: 'default', text: $t('system.captcha_image_page.disabled') };
}

// =========================
// 列定义
// =========================
const columns = computed<TableColumnsType>(() => [
  {
    title: $t('system.captcha_image_page.fields.preview'),
    key: 'preview',
    width: 130,
  },
  {
    title: $t('system.captcha_image_page.fields.fileName'),
    key: 'fileName',
    width: 220,
    ellipsis: true,
  },
  {
    title: $t('system.captcha_image_page.fields.dimension'),
    key: 'dimension',
    width: 120,
  },
  {
    title: $t('system.captcha_image_page.fields.fileSize'),
    key: 'fileSize',
    width: 110,
  },
  {
    title: $t('system.captcha_image_page.fields.md5Hash'),
    key: 'md5Hash',
    width: 220,
    ellipsis: true,
  },
  {
    title: $t('system.captcha_image_page.fields.status'),
    key: 'status',
    width: 100,
  },
  {
    title: $t('system.captcha_image_page.fields.createTime'),
    key: 'createTime',
    width: 170,
  },
  {
    title: $t('system.captcha_image_page.fields.action_col'),
    key: 'action',
    width: 90,
    fixed: 'right',
  },
]);

// =========================
// 数据加载
// =========================
async function fetchData() {
  loading.value = true;
  errorMsg.value = '';
  try {
    const res = await getCaptchaImageList({
      page: pageNum.value,
      size: pageSize.value,
      keyword: keyword.value.trim() || undefined,
    });
    images.value = res?.records ?? [];
    total.value = toNumber(res?.total);
    pages.value = toNumber(res?.pages);
  } catch (error: any) {
    errorMsg.value = error?.message || $t('system.captcha_image_page.error_desc');
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  fetchData();
});

function handleFilterChange() {
  pageNum.value = 1;
  fetchData();
}

function handlePageChange(page: number, size: number) {
  pageNum.value = page;
  pageSize.value = size;
  fetchData();
}

function handlePageSizeChange(_page: number, size: number) {
  pageSize.value = size;
  pageNum.value = 1;
  fetchData();
}

// =========================
// 上传
// =========================
async function handleBeforeUpload(file: any): Promise<boolean> {
  const isAllowed = /\.(jpe?g|png)$/i.test(String(file?.name ?? ''));
  if (!isAllowed) {
    message.warning($t('system.captcha_image_page.invalid_type'));
    return false;
  }
  if (toNumber(file?.size) > MAX_FILE_SIZE) {
    message.warning($t('system.captcha_image_page.too_large'));
    return false;
  }

  uploading.value = true;
  try {
    await uploadCaptchaImage(file);
    message.success($t('system.captcha_image_page.upload_success'));
    pageNum.value = 1;
    await fetchData();
  } catch {
    // 请求层已统一提示错误信息
  } finally {
    uploading.value = false;
  }
  return false;
}

// =========================
// 删除
// =========================
const deletingId = ref<null | string>(null);

async function handleDelete(record: any) {
  deletingId.value = record.id;
  try {
    await deleteCaptchaImage(record.id);
    message.success($t('system.captcha_image_page.delete_success'));
    if (pages.value > 0 && pageNum.value > 1 && images.value.length <= 1) {
      pageNum.value -= 1;
    }
    await fetchData();
  } catch {
    // 请求层已统一提示错误信息
  } finally {
    deletingId.value = null;
  }
}
</script>

<template>
  <Page :auto-content-height="true">
    <div class="mx-auto w-full">
      <!-- 加载中：骨架屏（仅首次加载） -->
      <Card
        v-if="loading && images.length === 0"
        :title="$t('system.captcha_image_page.title')"
        :styles="{ body: { padding: '24px' } }"
      >
        <Skeleton :active="true" :paragraph="{ rows: 8 }" />
      </Card>

      <!-- 加载失败 -->
      <Card
        v-else-if="errorMsg && images.length === 0"
        :title="$t('system.captcha_image_page.title')"
        :styles="{ body: { padding: '24px' } }"
      >
        <Result
          status="error"
          :title="$t('system.captcha_image_page.error_title')"
          :sub-title="errorMsg"
        >
          <template #extra>
            <Button type="primary" @click="fetchData">
              {{ $t('system.captcha_image_page.retry') }}
            </Button>
          </template>
        </Result>
      </Card>

      <!-- 列表 -->
      <Card
        v-else
        :title="$t('system.captcha_image_page.title')"
        :styles="{ body: { padding: '0' } }"
      >
        <template #extra>
          <Button @click="handleFilterChange">
            {{ $t('system.captcha_image_page.refresh') }}
          </Button>
        </template>

        <div
          class="flex flex-wrap items-center justify-between gap-2 border-b border-border px-4 py-3"
        >
          <div class="flex flex-wrap items-center gap-2">
            <Input
              v-model:value="keyword"
              :placeholder="$t('system.captcha_image_page.search_placeholder')"
              allow-clear
              class="w-60"
              @pressEnter="handleFilterChange"
            >
              <template #prefix>
                <Search class="size-4 text-muted-foreground" />
              </template>
            </Input>
            <Button type="primary" @click="handleFilterChange">
              {{ $t('system.captcha_image_page.search') }}
            </Button>
          </div>

          <div class="flex items-center gap-2">
            <span class="text-xs text-muted-foreground">
              {{ $t('system.captcha_image_page.upload_hint') }}
            </span>
            <Upload
              accept=".jpg,.jpeg,.png"
              :show-upload-list="false"
              :before-upload="handleBeforeUpload"
              :disabled="uploading"
            >
              <Button type="primary" :loading="uploading">
                {{ $t('system.captcha_image_page.upload') }}
              </Button>
            </Upload>
          </div>
        </div>

        <Table
          :columns="columns"
          :data-source="images"
          :loading="loading"
          :pagination="
            pages === 0
              ? { pageSize: 12, showSizeChanger: false }
              : {
                  current: pageNum,
                  pageSize: pageSize,
                  showSizeChanger: true,
                  pageSizeOptions: ['12', '24', '48'],
                  total: total,
                  showTotal: (t: number) =>
                    `${t} ${$t('system.captcha_image_page.total_suffix')}`,
                  onChange: handlePageChange,
                  onShowSizeChange: handlePageSizeChange,
                }
          "
          :scroll="{ x: 1200 }"
          size="small"
          row-key="id"
        >
          <template #bodyCell="{ column, record }">
            <!-- 预览 -->
            <template v-if="column.key === 'preview'">
              <Image
                v-if="record.url"
                :src="resolveImageUrl(record.url)"
                :width="96"
                :height="54"
                class="rounded border border-border object-cover"
                :preview="{ src: resolveImageUrl(record.url) }"
              />
              <span v-else class="text-xs text-muted-foreground">-</span>
            </template>

            <!-- 文件名 -->
            <template v-else-if="column.key === 'fileName'">
              <span class="text-xs">{{ displayValue(record.fileName) }}</span>
            </template>

            <!-- 尺寸 -->
            <template v-else-if="column.key === 'dimension'">
              <span class="tabular-nums">
                {{ toNumber(record.width) }} × {{ toNumber(record.height) }}
              </span>
            </template>

            <!-- 文件大小 -->
            <template v-else-if="column.key === 'fileSize'">
              <span class="tabular-nums">{{ formatFileSize(record.fileSize) }}</span>
            </template>

            <!-- MD5 -->
            <template v-else-if="column.key === 'md5Hash'">
              <code class="text-xs">{{ displayValue(record.md5Hash) }}</code>
            </template>

            <!-- 状态 -->
            <template v-else-if="column.key === 'status'">
              <Tag :color="statusInfo(record.status).color">
                {{ statusInfo(record.status).text }}
              </Tag>
            </template>

            <!-- 上传时间 -->
            <template v-else-if="column.key === 'createTime'">
              <span class="text-xs text-muted-foreground">
                {{ formatTime(record.createTime) }}
              </span>
            </template>

            <!-- 操作 -->
            <template v-else-if="column.key === 'action'">
              <Popconfirm
                :title="$t('system.captcha_image_page.delete_confirm_title')"
                :description="$t('system.captcha_image_page.delete_confirm')"
                :ok-text="$t('system.captcha_image_page.delete')"
                :cancel-text="$t('system.captcha_image_page.cancel')"
                :ok-button-props="{ danger: true }"
                @confirm="handleDelete(record)"
              >
                <Button
                  type="link"
                  size="small"
                  danger
                  :loading="deletingId === record.id"
                >
                  {{ $t('system.captcha_image_page.delete') }}
                </Button>
              </Popconfirm>
            </template>
          </template>

          <template #emptyText>
            <span class="text-sm text-muted-foreground">
              {{ $t('system.captcha_image_page.empty') }}
            </span>
          </template>
        </Table>
      </Card>
    </div>
  </Page>
</template>
