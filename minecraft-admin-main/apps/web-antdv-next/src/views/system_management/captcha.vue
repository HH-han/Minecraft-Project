<script lang="ts" setup>
import type { TableColumnsType } from 'antdv-next';

import type { CaptchaRecord, CaptchaStatistics } from '#/api/management/system/captcha';

import { computed, onMounted, ref } from 'vue';

import { Page } from '@vben/common-ui';
import { Search } from '@vben/icons';
import { $t } from '@vben/locales';

import {
  Button,
  Card,
  DatePicker,
  Descriptions,
  DescriptionsItem,
  Input,
  Modal,
  Result,
  Select,
  Skeleton,
  Table,
  Tag,
} from 'antdv-next';

import {
  getCaptchaRecordList,
  getCaptchaStatistics,
} from '#/api/management/system/captcha';

defineOptions({ name: 'CaptchaRecords' });

// =========================
// 状态
// =========================
const loading = ref(false);
const statisticsLoading = ref(false);
const errorMsg = ref('');

const records = ref<CaptchaRecord[]>([]);
const pageNum = ref(1);
const pageSize = ref(10);
const pages = ref(0);
const total = ref(0);

const statistics = ref<CaptchaStatistics>({
  total: 0,
  passed: 0,
  failed: 0,
  passRate: 0,
  avgCostTime: 0,
  todayTotal: 0,
  todayPassed: 0,
  todayFailed: 0,
});

// 过滤条件
const passedFilter = ref<null | number>(null);
const keyword = ref('');
const dateRange = ref<any>(null);

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

/** 拖动位置与目标位置的偏差（像素） */
function deviation(record: any): number {
  const slider = toNumber(record.sliderOffset);
  const target = toNumber(record.targetOffset);
  return Math.abs(slider - target);
}

/** 验证结果 → Tag */
function passedInfo(value?: any): { color: string; text: string } {
  return Number(value) === 1
    ? { color: 'green', text: $t('system.captcha_page.passed') }
    : { color: 'red', text: $t('system.captcha_page.failed') };
}

const emptyStatistics: CaptchaStatistics = {
  total: 0,
  passed: 0,
  failed: 0,
  passRate: 0,
  avgCostTime: 0,
  todayTotal: 0,
  todayPassed: 0,
  todayFailed: 0,
};

const statisticCards = computed(() => [
  {
    key: 'total',
    label: $t('system.captcha_page.stat_total'),
    value: statistics.value.total ?? 0,
    suffix: '',
    className: '',
  },
  {
    key: 'passed',
    label: $t('system.captcha_page.stat_passed'),
    value: statistics.value.passed ?? 0,
    suffix: '',
    className: 'text-green-600',
  },
  {
    key: 'failed',
    label: $t('system.captcha_page.stat_failed'),
    value: statistics.value.failed ?? 0,
    suffix: '',
    className: 'text-red-500',
  },
  {
    key: 'passRate',
    label: $t('system.captcha_page.stat_pass_rate'),
    value: statistics.value.passRate ?? 0,
    suffix: '%',
    className: '',
  },
  {
    key: 'avgCostTime',
    label: $t('system.captcha_page.stat_avg_cost'),
    value: statistics.value.avgCostTime ?? 0,
    suffix: 'ms',
    className: '',
  },
  {
    key: 'todayTotal',
    label: $t('system.captcha_page.stat_today'),
    value: statistics.value.todayTotal ?? 0,
    suffix: '',
    className: '',
  },
]);

const resultOptions = computed(() => [
  { label: $t('system.captcha_page.result_passed'), value: 1 },
  { label: $t('system.captcha_page.result_failed'), value: 0 },
]);

// =========================
// 列定义
// =========================
const columns = computed<TableColumnsType>(() => [
  { title: 'ID', dataIndex: 'id', width: 70 },
  {
    title: $t('system.captcha_page.fields.traceId'),
    key: 'traceId',
    width: 240,
    ellipsis: true,
  },
  {
    title: $t('system.captcha_page.fields.imageId'),
    key: 'imageId',
    width: 260,
    ellipsis: true,
  },
  {
    title: $t('system.captcha_page.fields.clientIp'),
    key: 'clientIp',
    width: 140,
  },
  {
    title: $t('system.captcha_page.fields.sliderOffset'),
    key: 'sliderOffset',
    width: 110,
  },
  {
    title: $t('system.captcha_page.fields.targetOffset'),
    key: 'targetOffset',
    width: 110,
  },
  {
    title: $t('system.captcha_page.fields.deviation'),
    key: 'deviation',
    width: 100,
  },
  {
    title: $t('system.captcha_page.fields.isPassed'),
    key: 'isPassed',
    width: 100,
  },
  {
    title: $t('system.captcha_page.fields.costTime'),
    key: 'costTime',
    width: 100,
  },
  {
    title: $t('system.captcha_page.fields.createTime'),
    key: 'createTime',
    width: 170,
  },
  {
    title: $t('system.captcha_page.fields.action_col'),
    key: 'action',
    width: 90,
    fixed: 'right',
  },
]);

// =========================
// 数据加载
// =========================
async function fetchStatistics() {
  statisticsLoading.value = true;
  try {
    const res = await getCaptchaStatistics();
    statistics.value = { ...emptyStatistics, ...res };
  } catch {
    statistics.value = { ...emptyStatistics };
  } finally {
    statisticsLoading.value = false;
  }
}

async function fetchData() {
  loading.value = true;
  errorMsg.value = '';
  try {
    const res = await getCaptchaRecordList({
      page: pageNum.value,
      size: pageSize.value,
      passed: passedFilter.value ?? undefined,
      keyword: keyword.value.trim() || undefined,
      startTime: dateRange.value?.[0] || undefined,
      endTime: dateRange.value?.[1] || undefined,
    });
    records.value = res?.records ?? [];
    total.value = toNumber(res?.total);
    pages.value = toNumber(res?.pages);
  } catch (error: any) {
    errorMsg.value = error?.message || $t('system.captcha_page.error_desc');
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  fetchData();
  fetchStatistics();
});

function handleFilterChange() {
  pageNum.value = 1;
  fetchData();
}

function handleReset() {
  passedFilter.value = null;
  keyword.value = '';
  dateRange.value = null;
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
// 详情弹窗
// =========================
const detailVisible = ref(false);
const detailRecord = ref<any>(null);

function openDetail(record: any) {
  detailRecord.value = record;
  detailVisible.value = true;
}
</script>

<template>
  <Page :auto-content-height="true">
    <div class="mx-auto w-full">
      <!-- 数据概览 -->
      <div class="mb-4 grid grid-cols-2 gap-3 md:grid-cols-3 xl:grid-cols-6">
        <Card v-for="card in statisticCards" :key="card.key" size="small">
          <Skeleton v-if="statisticsLoading" :active="true" :paragraph="false" />
          <template v-else>
            <div class="text-xs text-muted-foreground">{{ card.label }}</div>
            <div
              class="mt-1 text-2xl font-semibold tabular-nums"
              :class="card.className"
            >
              {{ card.value }}<span class="ml-0.5 text-xs font-normal text-muted-foreground">{{ card.suffix }}</span>
            </div>
          </template>
        </Card>
      </div>

      <!-- 加载中：骨架屏（仅首次加载） -->
      <Card
        v-if="loading && records.length === 0"
        :title="$t('system.captcha_page.title')"
        :styles="{ body: { padding: '24px' } }"
      >
        <Skeleton :active="true" :paragraph="{ rows: 8 }" />
      </Card>

      <!-- 加载失败 -->
      <Card
        v-else-if="errorMsg && records.length === 0"
        :title="$t('system.captcha_page.title')"
        :styles="{ body: { padding: '24px' } }"
      >
        <Result
          status="error"
          :title="$t('system.captcha_page.error_title')"
          :sub-title="errorMsg"
        >
          <template #extra>
            <Button type="primary" @click="fetchData">
              {{ $t('system.captcha_page.retry') }}
            </Button>
          </template>
        </Result>
      </Card>

      <!-- 列表 -->
      <Card
        v-else
        :title="$t('system.captcha_page.title')"
        :styles="{ body: { padding: '0' } }"
      >
        <template #extra>
          <Button @click="handleFilterChange">
            {{ $t('system.captcha_page.refresh') }}
          </Button>
        </template>

        <div
          class="flex flex-wrap items-center gap-2 border-b border-border px-4 py-3"
        >
          <Select
            v-model:value="passedFilter"
            :placeholder="$t('system.captcha_page.result_all')"
            allow-clear
            class="w-32"
            :options="resultOptions"
            @change="handleFilterChange"
          ></Select>
          <Input
            v-model:value="keyword"
            :placeholder="$t('system.captcha_page.search_placeholder')"
            allow-clear
            class="w-56"
            @pressEnter="handleFilterChange"
          >
            <template #prefix>
              <Search class="size-4 text-muted-foreground" />
            </template>
          </Input>
          <DatePicker.RangePicker
            v-model:value="dateRange"
            show-time
            value-format="YYYY-MM-DD HH:mm:ss"
            class="w-80"
            @change="handleFilterChange"
          />
          <Button type="primary" @click="handleFilterChange">
            {{ $t('system.captcha_page.search') }}
          </Button>
          <Button @click="handleReset">
            {{ $t('system.captcha_page.reset') }}
          </Button>
        </div>

        <Table
          :columns="columns"
          :data-source="records"
          :loading="loading"
          :pagination="
            pages === 0
              ? { pageSize: 10, showSizeChanger: false }
              : {
                  current: pageNum,
                  pageSize: pageSize,
                  showSizeChanger: true,
                  pageSizeOptions: ['10', '20', '50'],
                  total: total,
                  showTotal: (t: number) =>
                    `${t} ${$t('system.captcha_page.total_suffix')}`,
                  onChange: handlePageChange,
                  onShowSizeChange: handlePageSizeChange,
                }
          "
          :scroll="{ x: 1500 }"
          size="small"
          row-key="id"
        >
          <template #bodyCell="{ column, record }">
            <!-- 验证会话ID -->
            <template v-if="column.key === 'traceId'">
              <code class="text-xs">{{ displayValue(record.traceId) }}</code>
            </template>

            <!-- 图片ID -->
            <template v-else-if="column.key === 'imageId'">
              <code class="text-xs">{{ displayValue(record.imageId) }}</code>
            </template>

            <!-- 客户端IP -->
            <template v-else-if="column.key === 'clientIp'">
              <code class="text-xs">{{ displayValue(record.clientIp) }}</code>
            </template>

            <!-- 拖动偏移量 -->
            <template v-else-if="column.key === 'sliderOffset'">
              <span class="tabular-nums">{{ toNumber(record.sliderOffset) }}</span>
            </template>

            <!-- 目标偏移量 -->
            <template v-else-if="column.key === 'targetOffset'">
              <span class="tabular-nums">{{ toNumber(record.targetOffset) }}</span>
            </template>

            <!-- 偏差 -->
            <template v-else-if="column.key === 'deviation'">
              <span
                class="tabular-nums"
                :class="deviation(record) <= 10 ? 'text-green-600' : 'text-orange-500'"
              >
                {{ deviation(record) }}
              </span>
            </template>

            <!-- 验证结果 -->
            <template v-else-if="column.key === 'isPassed'">
              <Tag :color="passedInfo(record.isPassed).color">
                {{ passedInfo(record.isPassed).text }}
              </Tag>
            </template>

            <!-- 耗时 -->
            <template v-else-if="column.key === 'costTime'">
              <span class="tabular-nums">
                {{ toNumber(record.costTime) }}
                {{ $t('system.captcha_page.ms_unit') }}
              </span>
            </template>

            <!-- 记录时间 -->
            <template v-else-if="column.key === 'createTime'">
              <span class="text-xs text-muted-foreground">
                {{ formatTime(record.createTime) }}
              </span>
            </template>

            <!-- 操作 -->
            <template v-else-if="column.key === 'action'">
              <Button type="link" size="small" @click="openDetail(record)">
                {{ $t('system.captcha_page.detail') }}
              </Button>
            </template>
          </template>

          <template #emptyText>
            <span class="text-sm text-muted-foreground">
              {{ $t('system.captcha_page.empty') }}
            </span>
          </template>
        </Table>
      </Card>
    </div>

    <!-- 详情弹窗 -->
    <Modal
      v-model:open="detailVisible"
      :title="$t('system.captcha_page.detail_title')"
      :width="720"
      :footer="null"
      :styles="{
        body: {
          maxHeight: 'calc(100vh - 220px)',
          overflowY: 'auto',
          padding: '20px 24px',
        },
      }"
    >
      <template v-if="detailRecord">
        <div class="mb-4 flex flex-wrap items-center gap-2">
          <Tag :color="passedInfo(detailRecord.isPassed).color">
            {{ passedInfo(detailRecord.isPassed).text }}
          </Tag>
          <Tag color="cyan">
            {{ $t('system.captcha_page.fields.costTime') }}:
            {{ toNumber(detailRecord.costTime) }}
            {{ $t('system.captcha_page.ms_unit') }}
          </Tag>
          <Tag color="purple">
            {{ $t('system.captcha_page.fields.deviation') }}:
            {{ deviation(detailRecord) }}
          </Tag>
        </div>

        <Descriptions :column="2" size="small" bordered class="mb-3">
          <DescriptionsItem :label="$t('system.captcha_page.fields.id')">
            {{ detailRecord.id }}
          </DescriptionsItem>
          <DescriptionsItem :label="$t('system.captcha_page.fields.clientIp')">
            <code class="text-xs">{{ displayValue(detailRecord.clientIp) }}</code>
          </DescriptionsItem>
          <DescriptionsItem
            :label="$t('system.captcha_page.fields.traceId')"
            :span="2"
          >
            <code class="text-xs">{{ displayValue(detailRecord.traceId) }}</code>
          </DescriptionsItem>
          <DescriptionsItem
            :label="$t('system.captcha_page.fields.imageId')"
            :span="2"
          >
            <code class="text-xs">{{ displayValue(detailRecord.imageId) }}</code>
          </DescriptionsItem>
          <DescriptionsItem
            :label="$t('system.captcha_page.fields.sliderOffset')"
          >
            {{ toNumber(detailRecord.sliderOffset) }}
          </DescriptionsItem>
          <DescriptionsItem
            :label="$t('system.captcha_page.fields.targetOffset')"
          >
            {{ toNumber(detailRecord.targetOffset) }}
          </DescriptionsItem>
          <DescriptionsItem
            :label="$t('system.captcha_page.fields.createTime')"
            :span="2"
          >
            {{ formatTime(detailRecord.createTime) }}
          </DescriptionsItem>
        </Descriptions>

        <div v-if="detailRecord.userAgent">
          <div class="mb-1 text-xs font-medium text-muted-foreground">
            {{ $t('system.captcha_page.fields.userAgent') }}
          </div>
          <div
            class="whitespace-pre-wrap break-all rounded-md border border-border bg-accent/40 p-3 text-xs leading-5"
          >
            {{ detailRecord.userAgent }}
          </div>
        </div>
      </template>
    </Modal>
  </Page>
</template>
