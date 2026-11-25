import request from '@/utils/request'

/**
 * 收藏（微信风格）相关接口
 */

// 收藏分类统计
export const getFavoriteStats = (userId) => {
  return request.get('/public/favorite/stats', { userId })
}

// 最近收藏
export const getRecentFavorites = (userId, limit = 8) => {
  return request.get('/public/favorite/recent', { userId, limit })
}

// 收藏分页
export const getFavoriteList = (userId, params = {}) => {
  return request.get('/public/favorite/list', { userId, ...params })
}
