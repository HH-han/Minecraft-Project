import request from '../utils/request'

// 登录
export const login = (data) => {
  return request.post('/auth/login', data)
}

// 注册
export const register = (data) => {
  return request.post('/auth/register', data)
}

// 退出登录
export const logout = () => {
  return request.post('/user/logout')
}

// APP 扫码：标记已扫描并获取 Web 端设备信息
export const scanQr = (data) => {
  return request.post('/auth/qr/scan', data)
}

// APP 扫码：获取 Web 端设备信息
export const getQrDetail = (ticket) => {
  return request.get(`/auth/qr/detail/${ticket}`)
}

// APP 扫码：确认登录
export const confirmQr = (data) => {
  return request.post('/auth/qr/confirm', data)
}

// APP 扫码：取消登录
export const cancelQr = (data) => {
  return request.post('/auth/qr/cancel', data)
}
