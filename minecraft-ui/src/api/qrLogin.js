import request from '@/utils/request'

/**
 * 生成扫码登录二维码
 * @param {Object} [data] 预留的客户端信息
 */
export const generateQr = (data = {}) => request.post('/auth/qr/generate', data, { silent: true })

/**
 * 轮询二维码状态（WebSocket 兜底）
 * @param {string} ticket
 */
export const getQrStatus = (ticket) => request.get(`/auth/qr/status/${ticket}`, { silent: true })

/**
 * 消费 ticket 换取 Web JWT
 * @param {string} ticket
 */
export const consumeQr = (ticket) => request.post('/auth/qr/consume', { ticket }, { silent: true })
