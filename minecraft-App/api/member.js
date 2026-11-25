import request from '@/utils/request'

/**
 * 会员中心相关接口
 */

// 会员中心信息（等级 / 成长值 / 签到状态 / 权益）
export const getMemberInfo = () => {
  return request.get('/member/info')
}

// 每日签到
export const signMember = () => {
  return request.post('/member/sign')
}

// 会员等级列表
export const getMemberLevels = () => {
  return request.get('/member/levels')
}

// 会员套餐列表
export const getMemberPackages = () => {
  return request.get('/member/packages')
}

// 我的会员订单
export const getMemberOrders = () => {
  return request.get('/member/orders')
}
