import request from '#/utils/request';

export interface SysRole {
  id: number;
  tenantId?: number;
  parentId?: number;
  code: string;
  name: string;
  dataScope?: number;
  builtIn?: number;
  sort?: number;
  status?: number;
  remark?: string;
  createTime?: string;
}

export interface SysPermission {
  id: number;
  parentId: number;
  code: string;
  name: string;
  type: number;
  path?: string;
  component?: string;
  icon?: string;
  method?: string;
  apiPath?: string;
  sort?: number;
  visible?: number;
  status?: number;
  remark?: string;
  children?: SysPermission[];
}

export interface SysUser {
  id: number;
  account: string;
  username: string;
  nickname?: string;
  email?: string;
  status?: number;
  superAdmin?: boolean;
}

export interface PageData<T> {
  records: T[];
  total: number;
  pageNum: number;
  pageSize: number;
}

export interface PageQuery {
  pageNum?: number;
  pageSize?: number;
  keyword?: string;
  status?: number | null;
}

/* ---------------- 角色 ---------------- */
export function getRolePage(params: PageQuery) {
  return request.get<PageData<SysRole>>('/system/role/list', { params });
}
export function getAllRoles() {
  return request.get<SysRole[]>('/system/role/all');
}
export function createRole(data: Partial<SysRole>) {
  return request.post<SysRole>('/system/role', data);
}
export function updateRole(id: number, data: Partial<SysRole>) {
  return request.put<SysRole>(`/system/role/${id}`, data);
}
export function deleteRole(id: number) {
  return request.delete<void>(`/system/role/${id}`);
}
export function getRolePermissions(id: number) {
  return request.get<number[]>(`/system/role/${id}/permissions`);
}
export function assignRolePermissions(id: number, permissionIds: number[]) {
  return request.put<void>(`/system/role/${id}/permissions`, permissionIds);
}

/* ---------------- 权限 ---------------- */
export function getPermissionTree() {
  return request.get<SysPermission[]>('/system/permission/tree');
}
export function createPermission(data: Partial<SysPermission>) {
  return request.post<SysPermission>('/system/permission', data);
}
export function updatePermission(id: number, data: Partial<SysPermission>) {
  return request.put<SysPermission>(`/system/permission/${id}`, data);
}
export function deletePermission(id: number) {
  return request.delete<void>(`/system/permission/${id}`);
}
export function scanPermissions() {
  return request.post<{
    missingInDb: string[];
    missingInCode: string[];
    unprotected: string[];
  }>('/system/permission/scan');
}

/* ---------------- 用户授权 ---------------- */
export function getUserPage(params: PageQuery) {
  return request.get<PageData<SysUser>>('/system/user/page', { params });
}
export function getUserRoles(userId: number) {
  return request.get<number[]>(`/system/user/${userId}/roles`);
}
export function assignUserRoles(userId: number, roleIds: number[]) {
  return request.put<void>(`/system/user/${userId}/roles`, roleIds);
}
export function updateUserStatus(userId: number, status: number) {
  return request.put<void>(`/system/user/${userId}/status`, null, {
    params: { status },
  });
}
