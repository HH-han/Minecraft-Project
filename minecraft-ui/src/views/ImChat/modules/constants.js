// ImChat 侧边栏共享常量
import defaultAvatarUrl from '@/assets/defaultimage/avatar.png'

// 通过 Vite 资源导入解析出真实地址，避免生产构建后 /src/... 路径失效
export const DEFAULT_AVATAR = defaultAvatarUrl

// 头像缺失或加载失败时回退到默认头像，避免出现浏览器破图占位
export const handleAvatarError = (event) => {
  const img = event?.target
  // 已经是默认头像仍加载失败时不再重复赋值，避免死循环
  if (!img || img.getAttribute('src') === DEFAULT_AVATAR) return
  img.src = DEFAULT_AVATAR
}
