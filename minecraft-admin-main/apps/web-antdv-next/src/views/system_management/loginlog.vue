<script lang="ts" setup>
import type { TableColumnsType } from 'antdv-next';

import type {
  LoginLog,
  LoginLogStatistics,
} from '#/api/management/system/loginlog';

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
  InputNumber,
  message,
  Modal,
  Result,
  Select,
  Skeleton,
  Table,
  Tag,
} from 'antdv-next';

import {
  blockIPAddress,
  getLoginLogDetail,
  getLoginLogList,
} from '#/api/management/system/loginlog';

defineOptions({ name: 'LoginLogs' });

// =========================
// 状态
// =========================
const loading = ref(false);
const errorMsg = ref('');

const records = ref<LoginLog[]>([]);
const pageNum = ref(1);
const pageSize = ref(10);
const total = ref(0);

const statistics = ref<LoginLogStatistics>({
  total: 0,
  success: 0,
  fail: 0,
  uniqueUsers: 0,
});

// 过滤条件
const statusFilter = ref<null | string>(null);
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

/** 登录状态 → Tag */
function statusInfo(value?: null | string): { color: string; text: string } {
  return String(value ?? '') === '1'
    ? { color: 'green', text: $t('system.loginlog_page.success') }
    : { color: 'red', text: $t('system.loginlog_page.failed') };
}

const emptyStatistics: LoginLogStatistics = {
  total: 0,
  success: 0,
  fail: 0,
  uniqueUsers: 0,
};

const successRate = computed(() => {
  const totalCount = toNumber(statistics.value.total);
  if (totalCount === 0) return 0;
  return Number(
    ((toNumber(statistics.value.success) / totalCount) * 100).toFixed(2),
  );
});

const statisticCards = computed(() => [
  {
    key: 'total',
    label: $t('system.loginlog_page.stat_total'),
    value: statistics.value.total ?? 0,
    suffix: '',
    className: '',
  },
  {
    key: 'success',
    label: $t('system.loginlog_page.stat_success'),
    value: statistics.value.success ?? 0,
    suffix: '',
    className: 'text-green-600',
  },
  {
    key: 'fail',
    label: $t('system.loginlog_page.stat_fail'),
    value: statistics.value.fail ?? 0,
    suffix: '',
    className: 'text-red-500',
  },
  {
    key: 'successRate',
    label: $t('system.loginlog_page.stat_success_rate'),
    value: successRate.value,
    suffix: '%',
    className: '',
  },
  {
    key: 'uniqueUsers',
    label: $t('system.loginlog_page.stat_unique_users'),
    value: statistics.value.uniqueUsers ?? 0,
    suffix: '',
    className: '',
  },
]);

const statusOptions = computed(() => [
  { label: $t('system.loginlog_page.status_success'), value: '1' },
  { label: $t('system.loginlog_page.status_failed'), value: '0' },
]);

// =========================
// 列定义
// =========================
const columns = computed<TableColumnsType>(() => [
  { title: 'ID', dataIndex: 'id', width: 70 },
  {
    title: $t('system.loginlog_page.fields.username'),
    key: 'username',
    width: 140,
  },
  {
    title: $t('system.loginlog_page.fields.account'),
    key: 'account',
    width: 150,
  },
  {
    title: $t('system.loginlog_page.fields.ipaddr'),
    key: 'ipaddr',
    width: 150,
  },
  {
    title: $t('system.loginlog_page.fields.loginLocation'),
    key: 'loginLocation',
    width: 160,
    ellipsis: true,
  },
  {
    title: $t('system.loginlog_page.fields.browser'),
    key: 'browser',
    width: 130,
    ellipsis: true,
  },
  {
    title: $t('system.loginlog_page.fields.os'),
    key: 'os',
    width: 130,
    ellipsis: true,
  },
  {
    title: $t('system.loginlog_page.fields.status'),
    key: 'status',
    width: 100,
  },
  {
    title: $t('system.loginlog_page.fields.msg'),
    key: 'msg',
    width: 160,
    ellipsis: true,
  },
  {
    title: $t('system.loginlog_page.fields.loginTime'),
    key: 'loginTime',
    width: 170,
  },
  {
    title: $t('system.loginlog_page.fields.action_col'),
    key: 'action',
    width: 160,
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
    const res = await getLoginLogList({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      status: statusFilter.value ?? undefined,
      keyword: keyword.value.trim() || undefined,
      startTime: dateRange.value?.[0] || undefined,
      endTime: dateRange.value?.[1] || undefined,
    });
    records.value = res?.list ?? [];
    total.value = toNumber(res?.total);
    statistics.value = { ...emptyStatistics, ...(res?.statistics ?? {}) };
  } catch (error: any) {
    errorMsg.value = error?.message || $t('system.loginlog_page.error_desc');
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

function handleReset() {
  statusFilter.value = null;
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
const detailLoading = ref(false);
const detailRecord = ref<LoginLog | null>(null);

async function openDetail(record: any) {
  detailRecord.value = record;
  detailVisible.value = true;
  detailLoading.value = true;
  try {
    const res = await getLoginLogDetail(record.id);
    if (res) detailRecord.value = res;
  } catch {
    // 详情接口失败时使用列表数据展示
  } finally {
    detailLoading.value = false;
  }
}

// =========================
// 封禁 IP
// =========================
const blockVisible = ref(false);
const blockRecord = ref<LoginLog | null>(null);
const blockDuration = ref<number>(24);
const blockReason = ref('');
const blockSubmitting = ref(false);

function openBlock(record: any) {
  if (!record.ipaddr) {
    message.warning($t('system.loginlog_page.block_ip_missing'));
    return;
  }
  blockRecord.value = record;
  blockDuration.value = 24;
  blockReason.value = '';
  blockVisible.value = true;
}

async function handleBlock() {
  if (!blockRecord.value?.ipaddr) {
    message.warning($t('system.loginlog_page.block_ip_missing'));
    return;
  }
  if (!blockDuration.value || blockDuration.value <= 0) {
    message.warning($t('system.loginlog_page.block_duration_required'));
    return;
  }
  blockSubmitting.value = true;
  try {
    await blockIPAddress({
      ip: blockRecord.value.ipaddr,
      duration: blockDuration.value,
      reason: blockReason.value.trim() || undefined,
    });
    message.success($t('system.loginlog_page.block_success'));
    blockVisible.value = false;
  } catch (error: any) {
    message.error(error?.message || $t('system.loginlog_page.block_failed'));
  } finally {
    blockSubmitting.value = false;
  }
}
</script>

<template>
  <Page :auto-content-height="true">
    <div class="mx-auto w-full">
      <!-- 数据概览 -->
      <div class="mb-4 grid grid-cols-2 gap-3 md:grid-cols-3 xl:grid-cols-5">
        <Card v-for="card in statisticCards" :key="card.key" size="small">
          <Skeleton v-if="loading && records.length === 0" :active="true" :paragraph="false" />
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
        :title="$t('system.loginlog_page.title')"
        :styles="{ body: { padding: '24px' } }"
      >
        <Skeleton :active="true" :paragraph="{ rows: 8 }" />
      </Card>

      <!-- 加载失败 -->
      <Card
        v-else-if="errorMsg && records.length === 0"
        :title="$t('system.loginlog_page.title')"
        :styles="{ body: { padding: '24px' } }"
      >
        <Result
          status="error"
          :title="$t('system.loginlog_page.error_title')"
          :sub-title="errorMsg"
        >
          <template #extra>
            <Button type="primary" @click="fetchData">
              {{ $t('system.loginlog_page.retry') }}
            </Button>
          </template>
        </Result>
      </Card>

      <!-- 列表 -->
      <Card
        v-else
        :title="$t('system.loginlog_page.title')"
        :styles="{ body: { padding: '0' } }"
      >
        <template #extra>
          <Button @click="handleFilterChange">
            {{ $t('system.loginlog_page.refresh') }}
          </Button>
        </template>

        <div
          class="flex flex-wrap items-center gap-2 border-b border-border px-4 py-3"
        >
          <Select
            v-model:value="statusFilter"
            :placeholder="$t('system.loginlog_page.status_all')"
            allow-clear
            class="w-32"
            :options="statusOptions"
            @change="handleFilterChange"
          ></Select>
          <Input
            v-model:value="keyword"
            :placeholder="$t('system.loginlog_page.search_placeholder')"
            allow-clear
            class="w-60"
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
            {{ $t('system.loginlog_page.search') }}
          </Button>
          <Button @click="handleReset">
            {{ $t('system.loginlog_page.reset') }}
          </Button>
        </div>

        <Table
          :columns="columns"
          :data-source="records"
          :loading="loading"
          :pagination="{
            current: pageNum,
            pageSize: pageSize,
            showSizeChanger: true,
            pageSizeOptions: ['10', '20', '50'],
            total: total,
            showTotal: (t: number) =>
              `${t} ${$t('system.loginlog_page.total_suffix')}`,
            onChange: handlePageChange,
            onShowSizeChange: handlePageSizeChange,
          }"
          :scroll="{ x: 1400 }"
          size="small"
          row-key="id"
        >
          <template #bodyCell="{ column, record }">
            <!-- 用户名 -->
            <template v-if="column.key === 'username'">
              <span>{{ displayValue(record.username) }}</span>
            </template>

            <!-- 账号 -->
            <template v-else-if="column.key === 'account'">
              <code class="text-xs">{{ displayValue(record.account) }}</code>
            </template>

            <!-- IP 地址 -->
            <template v-else-if="column.key === 'ipaddr'">
              <code class="text-xs">{{ displayValue(record.ipaddr) }}</code>
            </template>

            <!-- 登录地点 -->
            <template v-else-if="column.key === 'loginLocation'">
              <span>{{ displayValue(record.loginLocation) }}</span>
            </template>

            <!-- 浏览器 -->
            <template v-else-if="column.key === 'browser'">
              <span>{{ displayValue(record.browser) }}</span>
            </template>

            <!-- 操作系统 -->
            <template v-else-if="column.key === 'os'">
              <span>{{ displayValue(record.os) }}</span>
            </template>

            <!-- 登录状态 -->
            <template v-else-if="column.key === 'status'">
              <Tag :color="statusInfo(record.status).color">
                {{ statusInfo(record.status).text }}
              </Tag>
            </template>

            <!-- 提示信息 -->
            <template v-else-if="column.key === 'msg'">
              <span>{{ displayValue(record.msg) }}</span>
            </template>

            <!-- 登录时间 -->
            <template v-else-if="column.key === 'loginTime'">
              <span class="text-xs text-muted-foreground">
                {{ formatTime(record.loginTime) }}
              </span>
            </template>

            <!-- 操作 -->
            <template v-else-if="column.key === 'action'">
              <div class="flex items-center gap-1">
                <Button type="link" size="small" @click="openDetail(record)">
                  {{ $t('system.loginlog_page.detail') }}
                </Button>
                <Button
                  type="link"
                  size="small"
                  danger
                  :disabled="!record.ipaddr"
                  @click="openBlock(record)"
                >
                  {{ $t('system.loginlog_page.block_ip') }}
                </Button>
              </div>
            </template>
          </template>

          <template #emptyText>
            <span class="text-sm text-muted-foreground">
              {{ $t('system.loginlog_page.empty') }}
            </span>
          </template>
        </Table>
      </Card>
    </div>

    <!-- 详情弹窗 -->
    <Modal
      v-model:open="detailVisible"
      :title="$t('system.loginlog_page.detail_title')"
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
      <Skeleton v-if="detailLoading" :active="true" :paragraph="{ rows: 6 }" />
      <template v-else-if="detailRecord">
        <div class="mb-4 flex flex-wrap items-center gap-2">
          <Tag :color="statusInfo(detailRecord.status).color">
            {{ statusInfo(detailRecord.status).text }}
          </Tag>
          <Tag color="cyan">
            {{ displayValue(detailRecord.browser) }}
          </Tag>
          <Tag color="purple">
            {{ displayValue(detailRecord.os) }}
          </Tag>
        </div>

        <Descriptions :column="2" size="small" bordered class="mb-3">
          <DescriptionsItem :label="$t('system.loginlog_page.fields.id')">
            {{ detailRecord.id }}
          </DescriptionsItem>
          <DescriptionsItem :label="$t('system.loginlog_page.fields.userId')">
            {{ displayValue(detailRecord.userId) }}
          </DescriptionsItem>
          <DescriptionsItem :label="$t('system.loginlog_page.fields.username')">
            {{ displayValue(detailRecord.username) }}
          </DescriptionsItem>
          <DescriptionsItem :label="$t('system.loginlog_page.fields.account')">
            <code class="text-xs">{{ displayValue(detailRecord.account) }}</code>
          </DescriptionsItem>
          <DescriptionsItem :label="$t('system.loginlog_page.fields.ipaddr')">
            <code class="text-xs">{{ displayValue(detailRecord.ipaddr) }}</code>
          </DescriptionsItem>
          <DescriptionsItem
            :label="$t('system.loginlog_page.fields.loginLocation')"
          >
            {{ displayValue(detailRecord.loginLocation) }}
          </DescriptionsItem>
          <DescriptionsItem :label="$t('system.loginlog_page.fields.browser')">
            {{ displayValue(detailRecord.browser) }}
          </DescriptionsItem>
          <DescriptionsItem :label="$t('system.loginlog_page.fields.os')">
            {{ displayValue(detailRecord.os) }}
          </DescriptionsItem>
          <DescriptionsItem :label="$t('system.loginlog_page.fields.status')">
            <Tag :color="statusInfo(detailRecord.status).color">
              {{ statusInfo(detailRecord.status).text }}
            </Tag>
          </DescriptionsItem>
          <DescriptionsItem
            :label="$t('system.loginlog_page.fields.loginTime')"
          >
            {{ formatTime(detailRecord.loginTime) }}
          </DescriptionsItem>
          <DescriptionsItem
            :label="$t('system.loginlog_page.fields.msg')"
            :span="2"
          >
            {{ displayValue(detailRecord.msg) }}
          </DescriptionsItem>
        </Descriptions>
      </template>
    </Modal>

    <!-- 封禁 IP 弹窗 -->
    <Modal
      v-model:open="blockVisible"
      :title="$t('system.loginlog_page.block_title')"
      :confirm-loading="blockSubmitting"
      :ok-text="$t('system.loginlog_page.block_confirm')"
      :cancel-text="$t('system.loginlog_page.cancel')"
      @ok="handleBlock"
    >
      <div class="space-y-4 py-2">
        <div>
          <div class="mb-1 text-sm text-muted-foreground">
            {{ $t('system.loginlog_page.fields.ipaddr') }}
          </div>
          <code class="text-sm">{{ displayValue(blockRecord?.ipaddr) }}</code>
        </div>
        <div>
          <div class="mb-1 text-sm text-muted-foreground">
            {{ $t('system.loginlog_page.block_duration') }}
          </div>
          <InputNumber
            v-model:value="blockDuration"
            :min="1"
            :max="8760"
            :addon-after="$t('system.loginlog_page.block_duration_unit')"
            class="w-full"
          />
        </div>
        <div>
          <div class="mb-1 text-sm text-muted-foreground">
            {{ $t('system.loginlog_page.block_reason') }}
          </div>
          <Input
            v-model:value="blockReason"
            :placeholder="$t('system.loginlog_page.block_reason_placeholder')"
            allow-clear
          />
        </div>
      </div>
    </Modal>
  </Page>
</template>
