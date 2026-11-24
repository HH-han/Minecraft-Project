import { usePermissionStore } from '@/stores/permissionStore'

/**
 * 按钮级权限指令：
 *   v-perm="'sys:user:add'"
 *   v-perm.any="['sys:user:add', 'sys:user:edit']"
 * 无权限时直接移除元素。
 */
export const perm = {
  mounted(el, binding) {
    const { value, modifiers } = binding
    if (!value) return
    const store = usePermissionStore()
    const codes = Array.isArray(value) ? value : [value]
    if (!codes.length) return
    const allowed = modifiers.any ? store.hasAnyPerm(codes) : codes.every((code) => store.hasPerm(code))
    if (!allowed) {
      el.parentNode?.removeChild(el)
    }
  }
}

export default perm
