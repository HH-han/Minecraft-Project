import request from '#/utils/request';

/**
 * 登录信息管理接口
 * 对应后端 LoginInfoController: /api/public/user/logininfo
 */

export interface LoginInfoQueryParams {
  /** 页码，从 1 开始 */
  page?: number;
  /** 每页数量 */
  pageSize?: number;
  /** 关键词（ID / 用户ID / 用户名 / IP） */
  keyword?: string;
}

export interface LoginInfo {
  id: number;
  userId?: null | number;
  username?: null | string;
  ipaddr?: null | string;
  loginLocation?: null | string;
  browser?: null | string;
  os?: null | string;
  status?: null | string;
  msg?: null | string;
  loginTime?: null | string;
}

export interface LoginInfoPage {
  records: LoginInfo[];
  total: number;
  size: number;
  current: number;
  pages: number;
}

/** 分页查询登录信息 */
export async function getLoginInfoList(
  params: LoginInfoQueryParams,
): Promise<LoginInfoPage> {
  return request.get('/public/user/logininfo', { params });
}

/** 删除登录信息 */
export async function deleteLoginInfo(
  id: number | string,
): Promise<boolean> {
  return request.delete(`/public/user/logininfo/${id}`);
}

/** 批量删除登录信息 */
export async function deleteBatchLoginInfo(
  ids: Array<number | string>,
): Promise<boolean> {
  return request.delete('/public/user/logininfo/batch', {
    params: { ids: ids.join(',') },
  });
}
