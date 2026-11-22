import request from '#/utils/request';

/**
 * 滑块验证码管理接口
 * 对应后端 CaptchaAdminController: /api/admin/captcha
 */

export interface CaptchaRecordQueryParams {
  /** 页码，从 1 开始 */
  page?: number;
  /** 每页数量 */
  size?: number;
  /** 验证结果：1-通过，0-失败 */
  passed?: number;
  /** 关键词（traceId / imageId / 客户端IP） */
  keyword?: string;
  /** 开始时间 yyyy-MM-dd HH:mm:ss */
  startTime?: string;
  /** 结束时间 yyyy-MM-dd HH:mm:ss */
  endTime?: string;
}

export interface CaptchaRecord {
  id: number;
  traceId?: null | string;
  imageId?: null | string;
  clientIp?: null | string;
  userAgent?: null | string;
  sliderOffset?: null | number;
  targetOffset?: null | number;
  isPassed?: null | number;
  costTime?: null | number;
  createTime?: null | string;
}

export interface CaptchaStatistics {
  total: number;
  passed: number;
  failed: number;
  passRate: number;
  avgCostTime: number;
  todayTotal: number;
  todayPassed: number;
  todayFailed: number;
}

export interface CaptchaRecordPage {
  records: CaptchaRecord[];
  total: number;
  pageNum: number;
  pageSize: number;
  pages: number;
}

/** 分页查询滑块验证记录 */
export async function getCaptchaRecordList(
  params: CaptchaRecordQueryParams,
): Promise<CaptchaRecordPage> {
  return request.get('/admin/captcha/records', { params });
}

/** 查询滑块验证数据概览 */
export async function getCaptchaStatistics(): Promise<CaptchaStatistics> {
  return request.get('/admin/captcha/statistics');
}
