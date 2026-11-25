/**
 * 轻量国际化方案
 * - 语言包位于 /locales，key 采用扁平命名（如 home.titleLine1）
 * - t() 读取响应式 locale，模板中直接调用即可随语言切换刷新
 * - 语言持久化到本地，并同步 H5/App 的导航栏标题、TabBar 文案
 */
import { ref, watch, onMounted } from 'vue'
import zhCN from '@/locales/zh-CN'
import enUS from '@/locales/en-US'

const STORAGE_KEY = 'app-locale'
const SUPPORTED = ['zh-CN', 'en-US']
const dictionaries = {
	'zh-CN': zhCN,
	'en-US': enUS
}

export const locale = ref('zh-CN')

export function t(key, params) {
	const dict = dictionaries[locale.value] || dictionaries['zh-CN']
	let text = dict[key]
	if (text === undefined) text = dictionaries['zh-CN'][key]
	if (text === undefined) return key

	if (params) {
		text = String(text).replace(/\{(\w+)\}/g, (match, name) => {
			return Object.prototype.hasOwnProperty.call(params, name) ? params[name] : match
		})
	}
	return text
}

export function getLocale() {
	return locale.value
}

export function getLocaleLabel(value = locale.value) {
	return value === 'en-US' ? t('settings.languageValueEn') : t('settings.languageValueZh')
}

function applyHtmlLang() {
	if (typeof document !== 'undefined' && document.documentElement) {
		document.documentElement.setAttribute('lang', locale.value === 'en-US' ? 'en' : 'zh-CN')
	}
}

export function applyTabBarLocale() {
	const labels = [
		t('tabbar.home'),
		t('tabbar.discover'),
		t('tabbar.community'),
		t('tabbar.news'),
		t('tabbar.profile')
	]
	labels.forEach((text, index) => {
		try {
			uni.setTabBarItem({ index, text, fail: () => {} })
		} catch (e) {
			// 非 TabBar 环境忽略
		}
	})
}

export function setLocale(next) {
	const target = SUPPORTED.includes(next) ? next : 'zh-CN'
	locale.value = target
	uni.setStorageSync(STORAGE_KEY, target)
	applyHtmlLang()
	applyTabBarLocale()
	return target
}

export function toggleLocale() {
	return setLocale(locale.value === 'zh-CN' ? 'en-US' : 'zh-CN')
}

export function initLocale() {
	const saved = uni.getStorageSync(STORAGE_KEY)
	if (SUPPORTED.includes(saved)) {
		locale.value = saved
	}
	applyHtmlLang()
	applyTabBarLocale()
	// TabBar 可能尚未挂载完成，稍后重试一次
	setTimeout(applyTabBarLocale, 500)
	return locale.value
}

/**
 * 页面标题同步：在 setup 中调用，语言切换时自动更新当前页导航栏标题
 */
export function usePageTitle(key) {
	const apply = () => {
		try {
			uni.setNavigationBarTitle({ title: t(key) })
		} catch (e) {
			// 忽略不支持动态标题的平台
		}
	}
	onMounted(apply)
	watch(locale, apply)
	return apply
}

export function useI18n() {
	return {
		t,
		locale,
		setLocale,
		toggleLocale
	}
}
