import { defineStore } from 'pinia'
import { getMe, getMyMenus } from '@/api/system'

/**
 * 权限 Store：统一持有当前登录用户的权限码、角色与菜单。
 * 权限码与后端 @PreAuthorize 使用同一套 module:resource:action 通配符规则。
 */
export const usePermissionStore = defineStore('permission', {
  state: () => ({
    loaded: false,
    loading: null,
    permissions: [],
    roles: [],
    menus: [],
    superAdmin: false,
    dataScope: 4
  }),

  getters: {
    /** 单个权限判断（支持持有码中的 * 通配） */
    hasPerm: (state) => (code) => {
      if (!code) return false
      if (state.superAdmin || state.permissions.includes('*:*:*')) return true
      if (state.permissions.includes(code)) return true
      const required = String(code).split(':')
      return state.permissions.some((owned) => {
        const segments = String(owned).split(':')
        if (segments.length !== required.length) return false
        return segments.every((segment, index) => segment === '*' || segment === required[index])
      })
    },
    /** 任意一个权限命中 */
    hasAnyPerm: (state) => (codes = []) => codes.some((code) => state.hasPerm(code)),
    /** 全部权限命中 */
    hasAllPerm: (state) => (codes = []) => codes.every((code) => state.hasPerm(code)),
    /** 角色判断 */
    hasRole: (state) => (roleCode) => state.roles.includes(roleCode)
  },

  actions: {
    /** 加载当前用户权限（并发调用时复用同一个 Promise） */
    async load(force = false) {
      if (this.loaded && !force) return
      if (this.loading) return this.loading
      this.loading = (async () => {
        try {
          const [meRes, menusRes] = await Promise.all([getMe(), getMyMenus()])
          const me = meRes?.code === 200 ? meRes.data : null
          if (me) {
            this.permissions = me.permissions || []
            this.roles = me.roles || []
            this.superAdmin = !!me.superAdmin
            this.dataScope = me.dataScope ?? 4
          }
          this.menus = menusRes?.code === 200 ? menusRes.data || [] : []
          this.loaded = true
        } finally {
          this.loading = null
        }
      })()
      return this.loading
    },

    /** 清空权限（退出登录/切换账号时调用） */
    reset() {
      this.$reset()
    }
  }
})
