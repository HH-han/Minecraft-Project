<script>
	import { initTheme } from '@/utils/theme'
	import { initLocale } from '@/utils/i18n'

	let firstBackTime = 0
	export default {
		onLaunch: function () {
			// 初始化主题（读取本地持久化配置并应用）
			initTheme()
			// 初始化语言（读取本地持久化配置并同步 TabBar 文案）
			initLocale()
		},
		onShow: function () {
			console.log('App Show')
		},
		onHide: function () {
			console.log('App Hide')
		},
		// #ifdef APP-ANDROID
		onLastPageBackPress: function () {
			if (firstBackTime == 0) {
				uni.showToast({
					title: '再按一次退出应用',
					position: 'bottom',
				})
				firstBackTime = Date.now()
				setTimeout(() => {
					firstBackTime = 0
				}, 2000)
			} else if (Date.now() - firstBackTime < 2000) {
				firstBackTime = Date.now()
				uni.exit()
			}
		},
		// #endif
		onExit: function () {
			console.log('App Exit')
		},
	}
</script>

<style>
	/* ==========================================================
	   云途旅行 · 全局设计系统
	   使用方式：页面统一使用 var(--*) 令牌，禁止再硬编码主色
	   ========================================================== */

	/* ---------- 1. 设计令牌：浅色主题（山野墨绿 · 旅行志） ---------- */
	:root,
	page {
		/* 品牌色：松墨绿 + 陶土橙 + 赭石金，全部为纯色 */
		--primary-color: #1E5B48;
		--primary-dark: #14453A;
		--primary-light: #4F8A74;
		--primary-soft: rgba(30, 91, 72, 0.1);
		--accent-color: #C4613C;
		--accent-soft: rgba(196, 97, 60, 0.12);
		--gold-color: #C08A2E;
		--success-color: #2F7A55;

		/* 背景与表面：米纸白 / 亚麻灰 */
		--bg-color: #F7F4EE;
		--background-color: #F7F4EE;
		--background-soft: #EFE9DD;
		--card-background: #FFFDF8;
		--surface-2: #F4EFE6;
		--glass-background: rgba(255, 253, 248, 0.78);

		/* 文本：墨绿黑 */
		--text-color: #1F2A24;
		--text-secondary-color: #5A645C;
		--text-tertiary-color: #8C948B;
		--text-inverse: #FFFDF8;

		/* 边框与阴影 */
		--border-color: rgba(31, 42, 36, 0.1);
		--border-strong: rgba(31, 42, 36, 0.18);
		--card-border: rgba(31, 42, 36, 0.08);
		--shadow-sm: 0 2px 8px rgba(38, 45, 36, 0.05);
		--shadow-md: 0 10px 26px rgba(38, 45, 36, 0.08);
		--shadow-lg: 0 20px 44px rgba(38, 45, 36, 0.12);
		--shadow-brand: 0 10px 22px rgba(30, 91, 72, 0.24);

		/* 圆角 */
		--radius-xs: 8px;
		--radius-sm: 12px;
		--radius-md: 16px;
		--radius-lg: 20px;
		--radius-xl: 28px;
		--radius-pill: 999px;

		/* 间距 */
		--space-page: 16px;
		--space-card: 16px;

		/* 动效 */
		--ease-out: cubic-bezier(0.16, 1, 0.3, 1);
		--ease-spring: cubic-bezier(0.34, 1.56, 0.64, 1);
	}

	/* ---------- 2. 设计令牌：深色主题 ---------- */
	html[data-theme='dark'],
	page[data-theme='dark'] {
		--primary-color: #3E8A6D;
		--primary-dark: #316F57;
		--primary-light: #6FB79A;
		--primary-soft: rgba(62, 138, 109, 0.16);
		--accent-color: #E0805C;
		--accent-soft: rgba(224, 128, 92, 0.18);
		--gold-color: #D6A94C;
		--success-color: #4FA97E;

		--bg-color: #0F1512;
		--background-color: #0F1512;
		--background-soft: #161D19;
		--card-background: #1A231E;
		--surface-2: #212B25;
		--glass-background: rgba(26, 35, 30, 0.78);

		--text-color: #E9EFE9;
		--text-secondary-color: #A4B0A6;
		--text-tertiary-color: #7A877D;

		--border-color: rgba(233, 239, 233, 0.1);
		--border-strong: rgba(233, 239, 233, 0.2);
		--card-border: rgba(233, 239, 233, 0.08);
		--shadow-sm: 0 2px 10px rgba(0, 0, 0, 0.3);
		--shadow-md: 0 10px 30px rgba(0, 0, 0, 0.42);
		--shadow-lg: 0 20px 50px rgba(0, 0, 0, 0.55);
		--shadow-brand: 0 10px 24px rgba(0, 0, 0, 0.45);
	}

	/* ---------- 3. 基础重置 ---------- */
	html,
	body,
	page,
	uni-page-body {
		background-color: var(--bg-color);
		color: var(--text-color);
	}

	body,
	page {
		font-family: -apple-system, BlinkMacSystemFont, 'PingFang SC', 'HarmonyOS Sans SC',
			'Microsoft YaHei', 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
		-webkit-font-smoothing: antialiased;
		-moz-osx-font-smoothing: grayscale;
		line-height: 1.5;
	}

	* {
		box-sizing: border-box;
	}

	h1,
	h2,
	h3,
	h4,
	p {
		margin: 0;
	}

	img {
		display: block;
		max-width: 100%;
	}

	button {
		font-family: inherit;
		cursor: pointer;
		border: none;
		background: none;
		color: inherit;
	}

	input,
	select,
	textarea {
		font-family: inherit;
		color: var(--text-color);
		background-color: transparent;
	}

	/* PC 端滚动条 */
	::-webkit-scrollbar {
		width: 8px;
		height: 8px;
	}

	::-webkit-scrollbar-thumb {
		background: rgba(120, 140, 170, 0.35);
		border-radius: 8px;
	}

	::-webkit-scrollbar-track {
		background: transparent;
	}

	/* ---------- 4. 页面骨架 ---------- */
	/* 内容容器：移动端通栏，PC 端居中限宽 */
	.shell {
		width: 100%;
		max-width: 1200px;
		margin: 0 auto;
	}

	/* 通用页面容器 */
	.app-page {
		min-height: 100vh;
		background-color: var(--bg-color);
		padding: var(--space-page);
		/* 预留底部 TabBar 空间 + 安全区 */
		padding-bottom: calc(84px + env(safe-area-inset-bottom));
	}

	/* 非 Tab 页面：不需要为 TabBar 留白 */
	.app-page--plain {
		padding-bottom: calc(28px + env(safe-area-inset-bottom));
	}

	/* ---------- 5. 卡片 ---------- */
	.surface-card {
		background: var(--card-background);
		border: 1px solid var(--card-border);
		border-radius: var(--radius-lg);
		box-shadow: var(--shadow-sm);
		transition: transform 0.35s var(--ease-out), box-shadow 0.35s var(--ease-out),
			border-color 0.35s var(--ease-out);
	}

	.surface-card:hover {
		transform: translateY(-4px);
		box-shadow: var(--shadow-md);
		border-color: var(--border-strong);
	}

	.glass-card {
		background: var(--glass-background);
		-webkit-backdrop-filter: blur(18px);
		backdrop-filter: blur(18px);
		border: 1px solid var(--card-border);
		border-radius: var(--radius-lg);
		box-shadow: var(--shadow-sm);
	}

	/* ---------- 6. 区块标题 ---------- */
	.section-head {
		display: flex;
		align-items: flex-end;
		justify-content: space-between;
		gap: 12px;
		margin: 28px 2px 14px;
	}

	.section-title {
		position: relative;
		display: flex;
		align-items: center;
		gap: 8px;
		font-size: 19px;
		font-weight: 700;
		letter-spacing: -0.2px;
		color: var(--text-color);
	}

	.section-title::before {
		content: '';
		width: 3px;
		height: 17px;
		border-radius: 2px;
		background: var(--primary-color);
	}

	.section-sub {
		font-size: 12px;
		color: var(--text-tertiary-color);
		font-weight: 400;
		margin-left: 2px;
	}

	.section-more {
		display: inline-flex;
		align-items: center;
		gap: 2px;
		padding: 6px 12px;
		border-radius: var(--radius-pill);
		background: var(--primary-soft);
		color: var(--primary-color);
		font-size: 12px;
		font-weight: 600;
		transition: all 0.3s var(--ease-out);
		white-space: nowrap;
	}

	.section-more:hover {
		background: var(--primary-color);
		color: #fff;
		transform: translateX(2px);
	}

	/* ---------- 7. 按钮 ---------- */
	.btn {
		display: inline-flex;
		align-items: center;
		justify-content: center;
		gap: 6px;
		padding: 11px 20px;
		border-radius: var(--radius-pill);
		font-size: 14px;
		font-weight: 600;
		line-height: 1;
		transition: all 0.28s var(--ease-out);
		user-select: none;
	}

	.btn:active {
		transform: scale(0.97);
	}

	.btn--primary {
		background: var(--primary-color);
		color: #fff;
		box-shadow: var(--shadow-brand);
	}

	.btn--primary:hover {
		box-shadow: 0 16px 34px rgba(30, 91, 72, 0.3);
		transform: translateY(-1px);
	}

	.btn--ghost {
		background: var(--card-background);
		color: var(--text-color);
		border: 1px solid var(--border-color);
	}

	.btn--soft {
		background: var(--primary-soft);
		color: var(--primary-color);
	}

	.btn--block {
		width: 100%;
	}

	/* ---------- 8. 标签 / 筛选胶囊 ---------- */
	.tag-pill {
		display: inline-flex;
		align-items: center;
		padding: 3px 9px;
		border-radius: var(--radius-pill);
		background: var(--primary-soft);
		color: var(--primary-color);
		font-size: 11px;
		font-weight: 600;
	}

	.chip-row {
		display: flex;
		gap: 8px;
		overflow-x: auto;
		padding: 2px 2px 6px;
		scrollbar-width: none;
		-webkit-overflow-scrolling: touch;
	}

	.chip-row::-webkit-scrollbar {
		display: none;
	}

	.chip {
		flex: 0 0 auto;
		padding: 8px 15px;
		border-radius: var(--radius-pill);
		border: 1px solid var(--border-color);
		background: var(--card-background);
		color: var(--text-secondary-color);
		font-size: 13px;
		font-weight: 500;
		white-space: nowrap;
		transition: all 0.25s var(--ease-out);
	}

	.chip:hover {
		color: var(--primary-color);
		border-color: var(--primary-color);
	}

	.chip.is-active {
		background: var(--primary-color);
		border-color: transparent;
		color: #fff;
		box-shadow: var(--shadow-brand);
	}

	/* ---------- 9. 搜索框 ---------- */
	.search-field {
		display: flex;
		align-items: center;
		gap: 10px;
		padding: 0 16px;
		height: 46px;
		border-radius: var(--radius-pill);
		background: var(--card-background);
		border: 1px solid var(--border-color);
		box-shadow: var(--shadow-sm);
		transition: all 0.25s var(--ease-out);
	}

	.search-field:focus-within {
		border-color: var(--primary-color);
		box-shadow: 0 0 0 4px var(--primary-soft);
	}

	.search-field input {
		flex: 1;
		min-width: 0;
		height: 100%;
		border: none;
		outline: none;
		font-size: 14px;
		color: var(--text-color);
	}

	.search-field input::placeholder {
		color: var(--text-tertiary-color);
	}

	/* ---------- 10. 文本工具 ---------- */
	.text-muted {
		color: var(--text-secondary-color);
	}

	.text-dim {
		color: var(--text-tertiary-color);
	}

	.clamp-1,
	.clamp-2,
	.clamp-3 {
		overflow: hidden;
		display: -webkit-box;
		-webkit-box-orient: vertical;
		word-break: break-all;
	}

	.clamp-1 {
		-webkit-line-clamp: 1;
	}

	.clamp-2 {
		-webkit-line-clamp: 2;
	}

	.clamp-3 {
		-webkit-line-clamp: 3;
	}

	/* ---------- 11. 空状态 / 分页 ---------- */
	.empty-state {
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 10px;
		padding: 56px 20px;
		color: var(--text-tertiary-color);
		font-size: 13px;
		text-align: center;
	}

	.empty-state__icon {
		width: 64px;
		height: 64px;
		border-radius: 50%;
		display: flex;
		align-items: center;
		justify-content: center;
		background: var(--primary-soft);
		font-size: 28px;
	}

	.pager {
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 12px;
		margin: 26px 0 10px;
	}

	.pager-btn {
		padding: 9px 18px;
		border-radius: var(--radius-pill);
		border: 1px solid var(--border-color);
		background: var(--card-background);
		color: var(--text-color);
		font-size: 13px;
		font-weight: 500;
		transition: all 0.25s var(--ease-out);
	}

	.pager-btn:hover:not(:disabled) {
		background: var(--primary-color);
		border-color: var(--primary-color);
		color: #fff;
	}

	.pager-btn:disabled {
		opacity: 0.45;
		cursor: not-allowed;
	}

	.pager-info {
		min-width: 62px;
		text-align: center;
		font-size: 13px;
		font-weight: 600;
		color: var(--text-secondary-color);
		font-variant-numeric: tabular-nums;
	}

	/* ---------- 12. 动效 ---------- */
	@keyframes floatUp {
		from {
			opacity: 0;
			transform: translateY(18px);
		}

		to {
			opacity: 1;
			transform: translateY(0);
		}
	}

	@keyframes skeletonPulse {
		0%,
		100% {
			opacity: 1;
		}

		50% {
			opacity: 0.55;
		}
	}

	.reveal {
		animation: floatUp 0.6s var(--ease-out) both;
	}

	.reveal-delay-1 {
		animation-delay: 0.06s;
	}

	.reveal-delay-2 {
		animation-delay: 0.12s;
	}

	.reveal-delay-3 {
		animation-delay: 0.18s;
	}

	.skeleton {
		background: var(--background-soft);
		animation: skeletonPulse 1.4s ease-in-out infinite;
		border-radius: var(--radius-sm);
	}

	/* ---------- 13. H5 TabBar 质感增强 ---------- */
	.uni-tabbar {
		-webkit-backdrop-filter: blur(20px);
		backdrop-filter: blur(20px);
		box-shadow: 0 -8px 26px rgba(38, 45, 36, 0.08);
		border-top: 1px solid var(--card-border);
	}

	.uni-tabbar__item {
		transition: transform 0.25s var(--ease-spring);
	}

	.uni-tabbar__item:active {
		transform: scale(0.92);
	}

	.uni-tabbar__label {
		font-weight: 500;
	}

	/* 深色主题下同步 H5 原生导航栏与 TabBar */
	html[data-theme='dark'] .uni-page-body {
		background-color: #0F1512;
	}

	html[data-theme='dark'] .uni-page-head {
		background-color: #161D19 !important;
		border-bottom: 1px solid rgba(233, 239, 233, 0.08) !important;
	}

	html[data-theme='dark'] .uni-page-head__title {
		color: #E9EFE9 !important;
	}

	html[data-theme='dark'] .uni-tabbar {
		background-color: rgba(26, 35, 30, 0.92) !important;
		border-top-color: rgba(233, 239, 233, 0.08) !important;
	}

	/* ---------- 14. 响应式：PC 端布局 ---------- */
	@media (min-width: 768px) {
		:root {
			--space-page: 28px;
			--space-card: 20px;
		}

		.app-page {
			padding-bottom: calc(96px + env(safe-area-inset-bottom));
		}

		.section-head {
			margin: 36px 4px 18px;
		}

		.section-title {
			font-size: 22px;
		}
	}

	@media (min-width: 1024px) {
		:root {
			--space-page: 36px;
		}
	}
</style>
