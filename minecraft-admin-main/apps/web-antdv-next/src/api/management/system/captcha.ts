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

// =========================
// 滑块验证图片素材
// =========================

export interface CaptchaImageQueryParams {
  /** 页码，从 1 开始 */
  page?: number;
  /** 每页数量 */
  size?: number;
  /** 文件名关键词 */
  keyword?: string;
}

export interface CaptchaImageItem {
  id: string;
  imageKey?: string;
  fileName?: string;
  /** 相对后端根路径的访问地址 */
  url?: string;
  fileSize?: number;
  width?: number;
  height?: number;
  mimeType?: string;
  md5Hash?: string;
  /** 1-启用，0-禁用 */
  status?: number;
  createTime?: string;
}

export interface CaptchaImagePage {
  records: CaptchaImageItem[];
  total: number;
  pageNum: number;
  pageSize: number;
  pages: number;
}

/** 分页查询滑块验证码图片 */
export async function getCaptchaImageList(
  params: CaptchaImageQueryParams,
): Promise<CaptchaImagePage> {
  return request.get('/admin/captcha/images', { params });
}

/** 上传滑块验证码图片 */
export async function uploadCaptchaImage(file: File): Promise<CaptchaImageItem> {
  const formData = new FormData();
  formData.append('file', file);
  return request.post('/admin/captcha/images', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
}

/** 删除滑块验证码图片 */
export async function deleteCaptchaImage(id: string): Promise<void> {
  return request.delete(`/admin/captcha/images/${id}`);
}
