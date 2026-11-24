<script lang="ts" setup>
import type { TableColumnsType } from 'antdv-next';

import type { SysPermission, SysRole, SysUser } from '#/api/management/system/rbac';

import { computed, onMounted, reactive, ref } from 'vue';

import { Page } from '@vben/common-ui';
import { $t } from '@vben/locales';

import {
  Button,
  Card,
  Checkbox,
  CheckboxGroup,
  Input,
  InputNumber,
  message,
  Modal,
  Select,
  Space,
  Switch,
  Table,
  Tag,
  Tree,
} from 'antdv-next';

import {
  assignRolePermissions,
  assignUserRoles,
  createPermission,
  createRole,
  deletePermission,
  deleteRole,
  getAllRoles,
  getPermissionTree,
  getRolePage,
  getRolePermissions,
  getUserPage,
  getUserRoles,
  scanPermissions,
  updatePermission,
  updateRole,
  updateUserStatus,
} from '#/api/management/system/rbac';

defineOptions({ name: 'SystemRbac' });

type TabKey = 'permission' | 'role' | 'user';
const activeTab = ref<TabKey>('role');

const scopeOptions = computed(() => [
  { label: $t('system.rbac.scope_all'), value: 1 },
  { label: $t('system.rbac.scope_dept'), value: 2 },
  { label: $t('system.rbac.scope_dept_child'), value: 3 },
  { label: $t('system.rbac.scope_self'), value: 4 },
  { label: $t('system.rbac.scope_custom'), value: 5 },
]);

const typeOptions = computed(() => [
  { label: $t('system.rbac.type_dir'), value: 1 },
  { label: $t('system.rbac.type_menu'), value: 2 },
  { label: $t('system.rbac.type_button'), value: 3 },
  { label: $t('system.rbac.type_api'), value: 4 },
  { label: $t('system.rbac.type_data'), value: 5 },
]);

/* ==================== 角色 ==================== */
const roleLoading = ref(false);
const roles = ref<SysRole[]>([]);
const roleTotal = ref(0);
const roleQuery = reactive<any>({ pageNum: 1, pageSize: 10, keyword: '' });

const roleColumns: TableColumnsType = [
  { title: $t('system.rbac.name'), dataIndex: 'name', key: 'name', width: 160 },
  { title: $t('system.rbac.code'), dataIndex: 'code', key: 'code', width: 170 },
  { title: $t('system.rbac.data_scope'), dataIndex: 'dataScope', key: 'dataScope', width: 150 },
  { title: $t('system.rbac.built_in'), dataIndex: 'builtIn', key: 'builtIn', width: 110 },
  { title: $t('system.rbac.status'), dataIndex: 'status', key: 'status', width: 100 },
  { title: $t('system.rbac.actions'), key: 'action', width: 240, fixed: 'right' },
];

const loadRoles = async () => {
  roleLoading.value = true;
  try {
    const data = await getRolePage(roleQuery);
    roles.value = data?.records || [];
    roleTotal.value = Number(data?.total || 0);
  } finally {
    roleLoading.value = false;
  }
};

const roleFormVisible = ref(false);
const roleForm = reactive<any>({});
const openRoleForm = (row?: any) => {
  Object.assign(roleForm, {
    id: null,
    code: '',
    name: '',
    dataScope: 4,
    sort: 0,
    status: 1,
    remark: '',
    ...row,
  });
  roleFormVisible.value = true;
};

const submitRole = async () => {
  if (!roleForm.code || !roleForm.name) {
    message.warning($t('system.rbac.required'));
    return;
  }
  if (roleForm.id) {
    await updateRole(roleForm.id, roleForm);
  } else {
    await createRole(roleForm);
  }
  message.success($t('system.rbac.saved'));
  roleFormVisible.value = false;
  loadRoles();
};

const removeRole = async (row: any) => {
  await deleteRole(row.id);
  message.success($t('system.rbac.saved'));
  loadRoles();
};

/* ---------- 角色授权 ---------- */
const permTree = ref<SysPermission[]>([]);
const permTreeVisible = ref(false);
const permTreeLoading = ref(false);
const checkedPermIds = ref<number[]>([]);
const currentRole = ref<SysRole | null>(null);

const leafIds = (nodes: SysPermission[], result: Set<number> = new Set()) => {
  nodes.forEach((node) => {
    if (node.children?.length) leafIds(node.children, result);
    else result.add(node.id);
  });
  return result;
};

const openRolePermission = async (row: any) => {
  currentRole.value = row;
  permTreeVisible.value = true;
  permTreeLoading.value = true;
  try {
    const [tree, owned] = await Promise.all([
      getPermissionTree(),
      getRolePermissions(row.id),
    ]);
    permTree.value = tree || [];
    const leaves = leafIds(permTree.value);
    checkedPermIds.value = (owned || []).filter((id) => leaves.has(id));
  } finally {
    permTreeLoading.value = false;
  }
};

const submitRolePermission = async () => {
  await assignRolePermissions(currentRole.value!.id, checkedPermIds.value);
  message.success($t('system.rbac.saved'));
  permTreeVisible.value = false;
};

/* ==================== 权限 ==================== */
const permissionLoading = ref(false);
const permissionTree = ref<SysPermission[]>([]);
const permissionColumns: TableColumnsType = [
  { title: $t('system.rbac.name'), dataIndex: 'name', key: 'name', width: 220 },
  { title: $t('system.rbac.code'), dataIndex: 'code', key: 'code', width: 230 },
  { title: $t('system.rbac.type'), dataIndex: 'type', key: 'type', width: 100 },
  { title: $t('system.rbac.path'), dataIndex: 'path', key: 'path', width: 200 },
  { title: $t('system.rbac.status'), dataIndex: 'status', key: 'status', width: 90 },
  { title: $t('system.rbac.actions'), key: 'action', width: 250, fixed: 'right' },
];

const loadPermissionTree = async () => {
  permissionLoading.value = true;
  try {
    permissionTree.value = (await getPermissionTree()) || [];
  } finally {
    permissionLoading.value = false;
  }
};

const permissionFormVisible = ref(false);
const permissionForm = reactive<any>({});
const openPermissionForm = (parent?: any, row?: any) => {
  Object.assign(permissionForm, {
    id: null,
    parentId: parent?.id ?? 0,
    code: '',
    name: '',
    type: parent ? 3 : 1,
    path: '',
    component: '',
    icon: '',
    method: '',
    apiPath: '',
    sort: 0,
    visible: 1,
    status: 1,
    remark: '',
    ...row,
  });
  permissionFormVisible.value = true;
};

const submitPermission = async () => {
  if (!permissionForm.code || !permissionForm.name) {
    message.warning($t('system.rbac.required'));
    return;
  }
  if (permissionForm.id) {
    await updatePermission(permissionForm.id, permissionForm);
  } else {
    await createPermission(permissionForm);
  }
  message.success($t('system.rbac.saved'));
  permissionFormVisible.value = false;
  loadPermissionTree();
};

const removePermission = async (row: any) => {
  await deletePermission(row.id);
  message.success($t('system.rbac.saved'));
  loadPermissionTree();
};

const scanVisible = ref(false);
const scanLoading = ref(false);
const scanResult = ref<any>({ missingInDb: [], missingInCode: [], unprotected: [] });
const runScan = async () => {
  scanVisible.value = true;
  scanLoading.value = true;
  try {
    scanResult.value = (await scanPermissions()) || scanResult.value;
  } finally {
    scanLoading.value = false;
  }
};

/* ==================== 用户授权 ==================== */
const userLoading = ref(false);
const users = ref<SysUser[]>([]);
const userTotal = ref(0);
const userQuery = reactive<any>({ pageNum: 1, pageSize: 10, keyword: '' });

const userColumns: TableColumnsType = [
  { title: $t('system.rbac.account'), dataIndex: 'account', key: 'account', width: 150 },
  { title: $t('system.rbac.username'), dataIndex: 'username', key: 'username', width: 150 },
  { title: $t('system.rbac.nickname'), dataIndex: 'nickname', key: 'nickname', width: 130 },
  { title: $t('system.rbac.email'), dataIndex: 'email', key: 'email', width: 210 },
  { title: $t('system.rbac.status'), dataIndex: 'status', key: 'status', width: 110 },
  { title: $t('system.rbac.actions'), key: 'action', width: 150, fixed: 'right' },
];

const loadUsers = async () => {
  userLoading.value = true;
  try {
    const data = await getUserPage(userQuery);
    users.value = data?.records || [];
    userTotal.value = Number(data?.total || 0);
  } finally {
    userLoading.value = false;
  }
};

const roleOptions = ref<SysRole[]>([]);
const userRoleVisible = ref(false);
const userRoleLoading = ref(false);
const selectedRoleIds = ref<number[]>([]);
const currentUser = ref<SysUser | null>(null);

const openUserRole = async (row: any) => {
  currentUser.value = row;
  userRoleVisible.value = true;
  userRoleLoading.value = true;
  try {
    const [all, owned] = await Promise.all([
      getAllRoles(),
      getUserRoles(row.id),
    ]);
    roleOptions.value = all || [];
    selectedRoleIds.value = owned || [];
  } finally {
    userRoleLoading.value = false;
  }
};

const submitUserRole = async () => {
  await assignUserRoles(currentUser.value!.id, selectedRoleIds.value);
  message.success($t('system.rbac.saved'));
  userRoleVisible.value = false;
};

const toggleUserStatus = async (row: any, checked: boolean) => {
  await updateUserStatus(row.id, checked ? 1 : 0);
  message.success($t('system.rbac.saved'));
  loadUsers();
};

const scopeLabel = (value: number) =>
  scopeOptions.value.find((item) => item.value === value)?.label || '-';
const typeLabel = (value: number) =>
  typeOptions.value.find((item) => item.value === value)?.label || '-';

const switchTab = (tab: TabKey) => {
  activeTab.value = tab;
  if (tab === 'role' && !roles.value.length) loadRoles();
  if (tab === 'permission' && !permissionTree.value.length) loadPermissionTree();
  if (tab === 'user' && !users.value.length) loadUsers();
};

onMounted(loadRoles);
</script>

<template>
  <Page>
    <Card>
      <div class="mb-3 flex flex-wrap items-center gap-2">
        <Button :type="activeTab === 'role' ? 'primary' : 'default'" @click="switchTab('role')">
          {{ $t('system.rbac.tab_role') }}
        </Button>
        <Button :type="activeTab === 'permission' ? 'primary' : 'default'" @click="switchTab('permission')">
          {{ $t('system.rbac.tab_permission') }}
        </Button>
        <Button :type="activeTab === 'user' ? 'primary' : 'default'" @click="switchTab('user')">
          {{ $t('system.rbac.tab_user') }}
        </Button>
      </div>

      <!-- ==================== 角色 ==================== -->
      <div v-show="activeTab === 'role'">
        <div class="mb-3 flex flex-wrap items-center gap-2">
          <Input
            v-model:value="roleQuery.keyword"
            allow-clear
            :placeholder="$t('system.rbac.search')"
            style="width: 240px"
            @press-enter="roleQuery.pageNum = 1; loadRoles()"
          />
          <Button type="primary" @click="roleQuery.pageNum = 1; loadRoles()">
            {{ $t('system.rbac.search') }}
          </Button>
          <Button @click="roleQuery.keyword = ''; roleQuery.pageNum = 1; loadRoles()">
            {{ $t('system.rbac.reset') }}
          </Button>
          <Button type="primary" @click="openRoleForm()">+ {{ $t('system.rbac.add') }}</Button>
        </div>
        <Table
          :columns="roleColumns"
          :data-source="roles"
          :loading="roleLoading"
          :pagination="{
            current: roleQuery.pageNum,
            pageSize: roleQuery.pageSize,
            total: roleTotal,
            showSizeChanger: true,
            onChange: (p: number, s: number) => { roleQuery.pageNum = p; roleQuery.pageSize = s; loadRoles(); },
          }"
          :scroll="{ x: 1000 }"
          row-key="id"
          size="small"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'dataScope'">{{ scopeLabel(record.dataScope) }}</template>
            <template v-else-if="column.key === 'builtIn'">
              <Tag v-if="record.builtIn === 1" color="orange">{{ $t('system.rbac.built_in') }}</Tag>
              <span v-else>-</span>
            </template>
            <template v-else-if="column.key === 'status'">
              <Tag :color="record.status === 1 ? 'green' : 'default'">
                {{ record.status === 1 ? $t('system.rbac.enabled') : $t('system.rbac.disabled') }}
              </Tag>
            </template>
            <template v-else-if="column.key === 'action'">
              <Space>
                <Button size="small" type="link" @click="openRoleForm(record)">
                  {{ $t('system.rbac.edit') }}
                </Button>
                <Button size="small" type="link" @click="openRolePermission(record)">
                  {{ $t('system.rbac.assign') }}
                </Button>
                <Button
                  size="small"
                  danger
                  type="link"
                  :disabled="record.builtIn === 1"
                  @click="removeRole(record)"
                >
                  {{ $t('system.rbac.delete') }}
                </Button>
              </Space>
            </template>
          </template>
        </Table>
      </div>

      <!-- ==================== 权限 ==================== -->
      <div v-show="activeTab === 'permission'">
        <div class="mb-3 flex flex-wrap items-center gap-2">
          <Button type="primary" @click="openPermissionForm()">+ {{ $t('system.rbac.add') }}</Button>
          <Button @click="runScan">{{ $t('system.rbac.scan') }}</Button>
          <Button @click="loadPermissionTree">{{ $t('system.rbac.refresh') }}</Button>
        </div>
        <Table
          :columns="permissionColumns"
          :data-source="permissionTree"
          :loading="permissionLoading"
          :pagination="false"
          :default-expand-all-rows="true"
          :scroll="{ x: 1100 }"
          row-key="id"
          size="small"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'type'">
              <Tag>{{ typeLabel(record.type) }}</Tag>
            </template>
            <template v-else-if="column.key === 'status'">
              <Tag :color="record.status === 1 ? 'green' : 'default'">
                {{ record.status === 1 ? $t('system.rbac.enabled') : $t('system.rbac.disabled') }}
              </Tag>
            </template>
            <template v-else-if="column.key === 'action'">
              <Space>
                <Button size="small" type="link" @click="openPermissionForm(record)">
                  {{ $t('system.rbac.add_child') }}
                </Button>
                <Button size="small" type="link" @click="openPermissionForm(undefined, record)">
                  {{ $t('system.rbac.edit') }}
                </Button>
                <Button size="small" danger type="link" @click="removePermission(record)">
                  {{ $t('system.rbac.delete') }}
                </Button>
              </Space>
            </template>
          </template>
        </Table>
      </div>

      <!-- ==================== 用户授权 ==================== -->
      <div v-show="activeTab === 'user'">
        <div class="mb-3 flex flex-wrap items-center gap-2">
          <Input
            v-model:value="userQuery.keyword"
            allow-clear
            :placeholder="$t('system.rbac.search')"
            style="width: 240px"
            @press-enter="userQuery.pageNum = 1; loadUsers()"
          />
          <Button type="primary" @click="userQuery.pageNum = 1; loadUsers()">
            {{ $t('system.rbac.search') }}
          </Button>
          <Button @click="userQuery.keyword = ''; userQuery.pageNum = 1; loadUsers()">
            {{ $t('system.rbac.reset') }}
          </Button>
        </div>
        <Table
          :columns="userColumns"
          :data-source="users"
          :loading="userLoading"
          :pagination="{
            current: userQuery.pageNum,
            pageSize: userQuery.pageSize,
            total: userTotal,
            showSizeChanger: true,
            onChange: (p: number, s: number) => { userQuery.pageNum = p; userQuery.pageSize = s; loadUsers(); },
          }"
          :scroll="{ x: 1000 }"
          row-key="id"
          size="small"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'status'">
              <Switch
                :checked="record.status === 1"
                size="small"
                @change="(val: any) => toggleUserStatus(record, !!val)"
              />
            </template>
            <template v-else-if="column.key === 'action'">
              <Button size="small" type="link" @click="openUserRole(record)">
                {{ $t('system.rbac.assign_role') }}
              </Button>
            </template>
          </template>
        </Table>
      </div>
    </Card>

    <!-- 角色表单 -->
    <Modal
      v-model:open="roleFormVisible"
      :title="roleForm.id ? $t('system.rbac.edit') : $t('system.rbac.add')"
      @ok="submitRole"
    >
      <div class="flex flex-col gap-3">
        <Input v-model:value="roleForm.name" :placeholder="$t('system.rbac.name')" />
        <Input v-model:value="roleForm.code" :disabled="!!roleForm.id" :placeholder="$t('system.rbac.code')" />
        <Select
          v-model:value="roleForm.dataScope"
          :options="scopeOptions"
          :placeholder="$t('system.rbac.data_scope')"
        />
        <InputNumber v-model:value="roleForm.sort" :min="0" style="width: 100%" />
        <Input v-model:value="roleForm.remark" :placeholder="$t('system.rbac.remark')" />
      </div>
    </Modal>

    <!-- 角色授权 -->
    <Modal
      v-model:open="permTreeVisible"
      :title="$t('system.rbac.assign')"
      :width="640"
      :confirm-loading="permTreeLoading"
      @ok="submitRolePermission"
    >
      <Tree
        v-model:checked-keys="checkedPermIds"
        :tree-data="permTree"
        checkable
        default-expand-all
        :field-names="{ title: 'name', key: 'id', children: 'children' }"
      />
    </Modal>

    <!-- 权限表单 -->
    <Modal
      v-model:open="permissionFormVisible"
      :title="permissionForm.id ? $t('system.rbac.edit') : $t('system.rbac.add')"
      :width="620"
      @ok="submitPermission"
    >
      <div class="flex flex-col gap-3">
        <Input v-model:value="permissionForm.name" :placeholder="$t('system.rbac.name')" />
        <Input v-model:value="permissionForm.code" placeholder="module:resource:action" />
        <Select v-model:value="permissionForm.type" :options="typeOptions" />
        <Input v-model:value="permissionForm.path" :placeholder="$t('system.rbac.path')" />
        <Input v-model:value="permissionForm.component" :placeholder="$t('system.rbac.component')" />
        <Input v-model:value="permissionForm.apiPath" :placeholder="$t('system.rbac.api_path')" />
        <InputNumber v-model:value="permissionForm.sort" :min="0" style="width: 100%" />
      </div>
    </Modal>

    <!-- 扫描结果 -->
    <Modal v-model:open="scanVisible" :title="$t('system.rbac.scan')" :width="680" :footer="null">
      <div v-if="scanLoading" class="py-6 text-center">{{ $t('system.rbac.loading') }}</div>
      <div v-else class="flex flex-col gap-4">
        <div>
          <div class="mb-2 font-medium">{{ $t('system.rbac.missing_in_db') }}</div>
          <Space wrap>
            <Tag v-for="code in scanResult.missingInDb" :key="code" color="red">{{ code }}</Tag>
            <span v-if="!scanResult.missingInDb?.length">-</span>
          </Space>
        </div>
        <div>
          <div class="mb-2 font-medium">{{ $t('system.rbac.missing_in_code') }}</div>
          <Space wrap>
            <Tag v-for="code in scanResult.missingInCode" :key="code" color="orange">{{ code }}</Tag>
            <span v-if="!scanResult.missingInCode?.length">-</span>
          </Space>
        </div>
        <div>
          <div class="mb-2 font-medium">{{ $t('system.rbac.unprotected') }}</div>
          <Space wrap>
            <Tag v-for="code in scanResult.unprotected" :key="code">{{ code }}</Tag>
            <span v-if="!scanResult.unprotected?.length">-</span>
          </Space>
        </div>
      </div>
    </Modal>

    <!-- 用户角色 -->
    <Modal
      v-model:open="userRoleVisible"
      :title="$t('system.rbac.assign_role')"
      :confirm-loading="userRoleLoading"
      @ok="submitUserRole"
    >
      <CheckboxGroup v-model:value="selectedRoleIds" class="flex flex-col gap-2">
        <Checkbox v-for="role in roleOptions" :key="role.id" :value="role.id">
          {{ role.name }}
          <span class="ml-2 text-xs opacity-60">{{ role.code }}</span>
        </Checkbox>
      </CheckboxGroup>
    </Modal>
  </Page>
</template>
