import request from '@/utils/request'

/* ==================== 当前用户权限 ==================== */

export const getMe = () => request.get('/auth/me', { silent: true })

export const getMyMenus = () => request.get('/auth/menus', { silent: true })
