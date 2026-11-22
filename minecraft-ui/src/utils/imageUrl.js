import { apiBaseUrl } from '@/config'

/** 历史数据中后端误写入的物理目录前缀 */
const LEGACY_PHYSICAL_PREFIX = '/www/wwwroot/springboot/upload/images/'
/** 统一的上传资源访问路径 */
const UPLOAD_URL_PREFIX = '/upload/'
/** 历史数据中的本机地址（含旧版 2025 端口） */
const LOCAL_HOST_RE = /^https?:\/\/(?:localhost|127\.0\.0\.1)(?::\d+)?/i

/**
 * 规范化后端返回的图片地址：
 * - 空值 / 'null' / 'undefined' → fallback
 * - data: / blob: / file: 本地地址 → 原样返回
 * - 历史 localhost 绝对地址 → 去掉主机，改为同源
 * - 历史物理目录 → 统一映射为 /upload/
 * - 真正的 http(s) 绝对地址 → 原样返回
 * - 相对路径 → 拼接 apiBaseUrl（生产环境为空，等同源）
 */
export function normalizeImageUrl(url, fallback = '') {
  if (!url) return fallback
  let raw = String(url).trim()
  if (!raw || raw === 'null' || raw === 'undefined') return fallback
  if (/^(data|blob|file):/i.test(raw)) return raw

  raw = raw
    .replace(LOCAL_HOST_RE, '')
    .replace(LEGACY_PHYSICAL_PREFIX, UPLOAD_URL_PREFIX)

  if (/^https?:\/\//i.test(raw)) return raw
  return `${apiBaseUrl}${raw.startsWith('/') ? raw : `/${raw}`}`
}
