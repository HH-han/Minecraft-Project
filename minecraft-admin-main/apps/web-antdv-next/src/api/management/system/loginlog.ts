import request from '#/utils/request';

/**
 * 登录日志管理接口
 * 对应后端 LoginLogController: /api/login-log
 */

export interface LoginLogQueryParams {
  /** 页码，从 1 开始 */
  pageNum?: number;
  /** 每页数量 */
  pageSize?: number;
  /** 关键词（用户名 / 账号 / IP） */
  keyword?: string;
  /** 登录状态：1-成功，0-失败 */
  status?: string;
  /** 开始时间 yyyy-MM-dd HH:mm:ss */
  startTime?: string;
  /** 结束时间 yyyy-MM-dd HH:mm:ss */
  endTime?: string;
}

export interface LoginLog {
  id: number;
  userId?: null | number;
  username?: null | string;
  account?: null | string;
  ipaddr?: null | string;
  loginLocation?: null | string;
  browser?: null | string;
  os?: null | string;
  status?: null | string;
  msg?: null | string;
  loginTime?: null | string;
}

export interface LoginLogStatistics {
  total: number;
  success: number;
  fail: number;
  uniqueUsers: number;
}

export interface LoginLogPage {
  list: LoginLog[];
  total: number;
  statistics: LoginLogStatistics;
}

export interface BlockIPParams {
  /** 需要封禁的 IP 地址 */
  ip: string;
  /** 封禁时长（小时） */
  duration: number;
  /** 封禁原因 */
  reason?: string;
}

/** 分页查询登录日志 */
export async function getLoginLogList(
  params: LoginLogQueryParams,
): Promise<LoginLogPage> {
  return request.get('/login-log/list', { params });
}

/** 查询登录日志详情 */
export async function getLoginLogDetail(
  id: number | string,
): Promise<LoginLog> {
  return request.get(`/login-log/${id}`);
}

/** 封禁 IP 地址 */
export async function blockIPAddress(params: BlockIPParams): Promise<void> {
  return request.post('/login-log/block-ip', null, { params });
}
