/**
 * 主题管理
 * 说明：通过 <html data-theme="dark"> 驱动 App.vue 中的设计令牌切换，
 * 并同步原生导航栏颜色。默认跟随系统外观，用户手动切换后持久化。
 */
const THEME_STORAGE_KEY = 'app-theme'
const NAV_COLORS = {
	light: { frontColor: '#000000', backgroundColor: '#FFFDF8' },
	dark: { frontColor: '#ffffff', backgroundColor: '#0F1512' }
}

export function getTheme() {
	const saved = uni.getStorageSync(THEME_STORAGE_KEY)
	return saved === 'dark' ? 'dark' : 'light'
}

export function applyTheme(theme) {
	const next = theme === 'dark' ? 'dark' : 'light'

	// H5 / WebView 环境：切换根节点主题标识
	if (typeof document !== 'undefined' && document.documentElement) {
		document.documentElement.setAttribute('data-theme', next)
	}

	try {
		const colors = NAV_COLORS[next]
		uni.setNavigationBarColor({
			frontColor: colors.frontColor,
			backgroundColor: colors.backgroundColor,
			fail: () => {}
		})
	} catch (e) {
		// 部分平台不支持动态设置导航栏，忽略即可
	}

	return next
}

export function setTheme(theme) {
	const next = applyTheme(theme)
	uni.setStorageSync(THEME_STORAGE_KEY, next)
	return next
}

export function toggleTheme() {
	return setTheme(getTheme() === 'dark' ? 'light' : 'dark')
}

export function initTheme() {
	const saved = uni.getStorageSync(THEME_STORAGE_KEY)
	if (saved === 'dark' || saved === 'light') {
		return applyTheme(saved)
	}

	// 首次进入跟随系统外观
	let theme = 'light'
	if (typeof window !== 'undefined' && window.matchMedia) {
		theme = window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
	}
	return applyTheme(theme)
}
