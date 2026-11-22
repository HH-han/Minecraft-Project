<script lang="ts" setup>
import type { TableColumnsType } from 'antdv-next';

import type { LoginInfo } from '#/api/management/user/loginInfo';

import { computed, onMounted, ref } from 'vue';

import { Page } from '@vben/common-ui';
import { Search } from '@vben/icons';
import { $t } from '@vben/locales';

import {
  Button,
  Card,
  Descriptions,
  DescriptionsItem,
  Input,
  message,
  Modal,
  Popconfirm,
  Result,
  Skeleton,
  Table,
  Tag,
} from 'antdv-next';

import {
  deleteBatchLoginInfo,
  deleteLoginInfo,
  getLoginInfoList,
} from '#/api/management/user/loginInfo';

defineOptions({ name: 'LoginInfo' });

// =========================
// 状态
// =========================
const loading = ref(false);
const errorMsg = ref('');

const records = ref<LoginInfo[]>([]);
const pageNum = ref(1);
const pageSize = ref(10);
const total = ref(0);

const keyword = ref('');
const selectedRowKeys = ref<Array<number | string>>([]);
const deletingId = ref<null | number | string>(null);
const batchDeleting = ref(false);

// =========================
// 工具
// =========================
function displayValue(value: any): string {
  if (value === null || value === undefined || value === '') {
    return $t('user.login_page.unknown');
  }
  return String(value);
}

function formatTime(value?: null | string): string {
  if (!value) return $t('user.login_page.unknown');
  return String(value).replace('T', ' ');
}

function toNumber(value: any): number {
  const n = Number(value);
  return Number.isFinite(n) ? n : 0;
}

/** 登录状态 → Tag */
function statusInfo(value?: null | string): { color: string; text: string } {
  const status = String(value ?? '');
  if (status === '1') {
    return { color: 'green', text: $t('user.login_page.success') };
  }
  if (status === '0') {
    return { color: 'red', text: $t('user.login_page.failed') };
  }
  return { color: 'default', text: $t('user.login_page.unknown') };
}

// =========================
// 列定义
// =========================
const columns = computed<TableColumnsType>(() => [
  {
    title: $t('user.login_page.fields.id'),
    dataIndex: 'id',
    width: 80,
  },
  {
    title: $t('user.login_page.fields.userId'),
    key: 'userId',
    width: 100,
  },
  {
    title: $t('user.login_page.fields.username'),
    key: 'username',
    width: 140,
  },
  {
    title: $t('user.login_page.fields.ipaddr'),
    key: 'ipaddr',
    width: 150,
  },
  {
    title: $t('user.login_page.fields.loginLocation'),
    key: 'loginLocation',
    width: 160,
    ellipsis: true,
  },
  {
    title: $t('user.login_page.fields.browser'),
    key: 'browser',
    width: 130,
    ellipsis: true,
  },
  {
    title: $t('user.login_page.fields.os'),
    key: 'os',
    width: 130,
    ellipsis: true,
  },
  {
    title: $t('user.login_page.fields.status'),
    key: 'status',
    width: 100,
  },
  {
    title: $t('user.login_page.fields.msg'),
    key: 'msg',
    width: 160,
    ellipsis: true,
  },
  {
    title: $t('user.login_page.fields.loginTime'),
    key: 'loginTime',
    width: 170,
  },
  {
    title: $t('user.login_page.fields.action_col'),
    key: 'action',
    width: 130,
    fixed: 'right',
  },
]);

const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: Array<number | string>) => {
    selectedRowKeys.value = keys;
  },
}));

// =========================
// 数据加载
// =========================
async function fetchData() {
  loading.value = true;
  errorMsg.value = '';
  try {
    const res = await getLoginInfoList({
      page: pageNum.value,
      pageSize: pageSize.value,
      keyword: keyword.value.trim() || undefined,
    });
    records.value = res?.records ?? [];
    total.value = toNumber(res?.total);
    selectedRowKeys.value = [];
  } catch (error: any) {
    errorMsg.value = error?.message || $t('user.login_page.error_desc');
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  fetchData();
});

function handleSearch() {
  pageNum.value = 1;
  fetchData();
}

function handleReset() {
  keyword.value = '';
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
const detailRecord = ref<LoginInfo | null>(null);

function openDetail(record: any) {
  detailRecord.value = record;
  detailVisible.value = true;
}

// =========================
// 删除
// =========================
async function handleDelete(record: any) {
  deletingId.value = record.id;
  try {
    await deleteLoginInfo(record.id);
    message.success($t('user.login_page.delete_success'));
    if (records.value.length <= 1 && pageNum.value > 1) {
      pageNum.value -= 1;
    }
    await fetchData();
  } catch (error: any) {
    message.error(error?.message || $t('user.login_page.delete_failed'));
  } finally {
    deletingId.value = null;
  }
}

async function handleBatchDelete() {
  if (selectedRowKeys.value.length === 0) {
    message.warning($t('user.login_page.select_required'));
    return;
  }
  batchDeleting.value = true;
  try {
    await deleteBatchLoginInfo(selectedRowKeys.value);
    message.success($t('user.login_page.batch_delete_success'));
    if (records.value.length <= selectedRowKeys.value.length && pageNum.value > 1) {
      pageNum.value -= 1;
    }
    await fetchData();
  } catch (error: any) {
    message.error(error?.message || $t('user.login_page.batch_delete_failed'));
  } finally {
    batchDeleting.value = false;
  }
}
</script>

<template>
  <Page :auto-content-height="true">
    <div class="mx-auto w-full">
      <!-- 加载中：骨架屏（仅首次加载） -->
      <Card
        v-if="loading && records.length === 0"
        :title="$t('user.login_page.title')"
        :styles="{ body: { padding: '24px' } }"
      >
        <Skeleton :active="true" :paragraph="{ rows: 8 }" />
      </Card>

      <!-- 加载失败 -->
      <Card
        v-else-if="errorMsg && records.length === 0"
        :title="$t('user.login_page.title')"
        :styles="{ body: { padding: '24px' } }"
      >
        <Result
          status="error"
          :title="$t('user.login_page.error_title')"
          :sub-title="errorMsg"
        >
          <template #extra>
            <Button type="primary" @click="fetchData">
              {{ $t('user.login_page.retry') }}
            </Button>
          </template>
        </Result>
      </Card>

      <!-- 列表 -->
      <Card
        v-else
        :title="$t('user.login_page.title')"
        :styles="{ body: { padding: '0' } }"
      >
        <template #extra>
          <Button @click="handleSearch">
            {{ $t('user.login_page.refresh') }}
          </Button>
        </template>

        <div
          class="flex flex-wrap items-center justify-between gap-2 border-b border-border px-4 py-3"
        >
          <div class="flex flex-wrap items-center gap-2">
            <Input
              v-model:value="keyword"
              :placeholder="$t('user.login_page.search_placeholder')"
              allow-clear
              class="w-64"
              @pressEnter="handleSearch"
            >
              <template #prefix>
                <Search class="size-4 text-muted-foreground" />
              </template>
            </Input>
            <Button type="primary" @click="handleSearch">
              {{ $t('user.login_page.search') }}
            </Button>
            <Button @click="handleReset">
              {{ $t('user.login_page.reset') }}
            </Button>
          </div>
          <div class="flex items-center gap-2">
            <span
              v-if="selectedRowKeys.length > 0"
              class="text-xs text-muted-foreground"
            >
              {{
                $t('user.login_page.selected', {
                  count: selectedRowKeys.length,
                })
              }}
            </span>
            <Popconfirm
              :title="$t('user.login_page.batch_delete_confirm_title')"
              :description="
                $t('user.login_page.batch_delete_confirm', {
                  count: selectedRowKeys.length,
                })
              "
              :ok-text="$t('user.login_page.delete')"
              :cancel-text="$t('user.login_page.cancel')"
              :ok-button-props="{ danger: true }"
              :disabled="selectedRowKeys.length === 0"
              @confirm="handleBatchDelete"
            >
              <Button
                danger
                :loading="batchDeleting"
                :disabled="selectedRowKeys.length === 0"
              >
                {{ $t('user.login_page.batch_delete') }}
              </Button>
            </Popconfirm>
          </div>
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
              `${t} ${$t('user.login_page.total_suffix')}`,
            onChange: handlePageChange,
            onShowSizeChange: handlePageSizeChange,
          }"
          :row-selection="rowSelection"
          :scroll="{ x: 1450 }"
          size="small"
          row-key="id"
        >
          <template #bodyCell="{ column, record }">
            <!-- 用户ID -->
            <template v-if="column.key === 'userId'">
              <span class="tabular-nums">{{ displayValue(record.userId) }}</span>
            </template>

            <!-- 用户名 -->
            <template v-else-if="column.key === 'username'">
              <span>{{ displayValue(record.username) }}</span>
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
                  {{ $t('user.login_page.detail') }}
                </Button>
                <Popconfirm
                  :title="$t('user.login_page.delete_confirm_title')"
                  :description="$t('user.login_page.delete_confirm')"
                  :ok-text="$t('user.login_page.delete')"
                  :cancel-text="$t('user.login_page.cancel')"
                  :ok-button-props="{ danger: true }"
                  @confirm="handleDelete(record)"
                >
                  <Button
                    type="link"
                    size="small"
                    danger
                    :loading="deletingId === record.id"
                  >
                    {{ $t('user.login_page.delete') }}
                  </Button>
                </Popconfirm>
              </div>
            </template>
          </template>

          <template #emptyText>
            <span class="text-sm text-muted-foreground">
              {{ $t('user.login_page.empty') }}
            </span>
          </template>
        </Table>
      </Card>
    </div>

    <!-- 详情弹窗 -->
    <Modal
      v-model:open="detailVisible"
      :title="$t('user.login_page.detail_title')"
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
          <DescriptionsItem :label="$t('user.login_page.fields.id')">
            {{ detailRecord.id }}
          </DescriptionsItem>
          <DescriptionsItem :label="$t('user.login_page.fields.userId')">
            {{ displayValue(detailRecord.userId) }}
          </DescriptionsItem>
          <DescriptionsItem :label="$t('user.login_page.fields.username')">
            {{ displayValue(detailRecord.username) }}
          </DescriptionsItem>
          <DescriptionsItem :label="$t('user.login_page.fields.ipaddr')">
            <code class="text-xs">{{ displayValue(detailRecord.ipaddr) }}</code>
          </DescriptionsItem>
          <DescriptionsItem
            :label="$t('user.login_page.fields.loginLocation')"
            :span="2"
          >
            {{ displayValue(detailRecord.loginLocation) }}
          </DescriptionsItem>
          <DescriptionsItem :label="$t('user.login_page.fields.browser')">
            {{ displayValue(detailRecord.browser) }}
          </DescriptionsItem>
          <DescriptionsItem :label="$t('user.login_page.fields.os')">
            {{ displayValue(detailRecord.os) }}
          </DescriptionsItem>
          <DescriptionsItem :label="$t('user.login_page.fields.status')">
            <Tag :color="statusInfo(detailRecord.status).color">
              {{ statusInfo(detailRecord.status).text }}
            </Tag>
          </DescriptionsItem>
          <DescriptionsItem :label="$t('user.login_page.fields.loginTime')">
            {{ formatTime(detailRecord.loginTime) }}
          </DescriptionsItem>
          <DescriptionsItem
            :label="$t('user.login_page.fields.msg')"
            :span="2"
          >
            {{ displayValue(detailRecord.msg) }}
          </DescriptionsItem>
        </Descriptions>
      </template>
    </Modal>
  </Page>
</template>
