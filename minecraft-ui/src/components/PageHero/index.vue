<template>
    <!-- 统一页面头部：标题 + 副标题 + 搜索区（Hotel / Scenicspot / Souvenir / Food 共用） -->
    <header class="page-hero">
        <div class="page-hero__decor" aria-hidden="true">
            <span class="page-hero__blob page-hero__blob--primary"></span>
            <span class="page-hero__blob page-hero__blob--accent"></span>
            <span class="page-hero__sheen"></span>
        </div>

        <div class="page-hero__inner">
            <h1 class="page-hero__title">{{ title }}</h1>
            <p v-if="subtitle" class="page-hero__subtitle">{{ subtitle }}</p>

            <form class="page-hero__search" role="search" @submit.prevent="handleSubmit">
                <div class="page-hero__fields">
                    <!-- 搜索字段：input / CalendarPicker 等 -->
                    <slot />
                </div>
                <button class="page-hero__submit" type="submit">
                    <svg viewBox="0 0 24 24" aria-hidden="true">
                        <circle cx="11" cy="11" r="7" />
                        <path d="M20 20l-3.6-3.6" />
                    </svg>
                    <span>{{ $t('recommendHero.search') }}</span>
                </button>
            </form>

            <div v-if="$slots.chips" class="page-hero__chips">
                <slot name="chips" />
            </div>
        </div>
    </header>
</template>

<script setup>
defineProps({
    /** 主标题 */
    title: {
        type: String,
        required: true
    },
    /** 副标题，为空时不渲染 */
    subtitle: {
        type: String,
        default: ''
    }
});

const emit = defineEmits(['search']);

// 表单提交（回车 / 点击搜索按钮）统一抛给页面处理
const handleSubmit = () => emit('search');
</script>

<style scoped>
.page-hero {
    position: relative;
    margin: 24px 0;
    padding: 40px 24px 36px;
    border-radius: 20px;
    overflow: hidden;
    isolation: isolate;
    background: var(--theme-bg-surface);
    border: 1px solid var(--theme-border);
    box-shadow: var(--theme-shadow-sm);
    color: var(--theme-text-1);
}

/* ---------- 背景装饰 ---------- */
.page-hero__decor {
    position: absolute;
    inset: 0;
    z-index: 0;
    pointer-events: none;
}

.page-hero__blob {
    position: absolute;
    border-radius: 50%;
    filter: blur(64px);
}

.page-hero__blob--primary {
    width: 46%;
    height: 200%;
    top: -110%;
    left: -10%;
    background: var(--theme-primary);
    opacity: 0.28;
}

.page-hero__blob--accent {
    width: 42%;
    height: 190%;
    right: -10%;
    bottom: -120%;
    background: var(--theme-accent-2);
    opacity: 0.24;
}

.page-hero__sheen {
    position: absolute;
    inset: 0;
    background: linear-gradient(180deg, var(--theme-bg-glass-strong) 0%, transparent 72%);
}

/* ---------- 内容区 ---------- */
.page-hero__inner {
    position: relative;
    z-index: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
}

.page-hero__title {
    margin: 0;
    font-size: clamp(1.6rem, 2.6vw, 2.4rem);
    font-weight: 800;
    line-height: 1.25;
    letter-spacing: 0.02em;
    text-align: center;
    background: var(--theme-gradient);
    -webkit-background-clip: text;
    background-clip: text;
    color: transparent;
}

.page-hero__subtitle {
    margin: 10px 0 0;
    font-size: clamp(0.9rem, 1.2vw, 1.05rem);
    line-height: 1.6;
    text-align: center;
    color: var(--theme-text-2);
}

/* ---------- 搜索区 ---------- */
.page-hero__search {
    display: flex;
    align-items: stretch;
    gap: 10px;
    width: min(920px, 100%);
    margin-top: 24px;
    padding: 10px;
    border-radius: 18px;
    background: var(--theme-bg-glass-strong);
    border: 1px solid var(--theme-border);
    box-shadow: var(--theme-shadow-md);
    -webkit-backdrop-filter: blur(12px);
    backdrop-filter: blur(12px);
    transition: border-color 0.25s ease, box-shadow 0.25s ease;
}

.page-hero__search:focus-within {
    border-color: var(--theme-primary);
    box-shadow: var(--theme-shadow-md), var(--theme-ring);
}

.page-hero__fields {
    display: flex;
    flex: 1 1 auto;
    flex-wrap: wrap;
    gap: 10px;
    min-width: 0;
}

/* 文本 / 数字输入框（来自页面插槽） */
.page-hero__fields :slotted(input) {
    flex: 1 1 170px;
    min-width: 0;
    height: 46px;
    padding: 0 16px;
    border: 1px solid var(--theme-border);
    border-radius: 10px;
    background: var(--theme-bg-surface);
    color: var(--theme-text-1);
    font-family: inherit;
    font-size: 15px;
    outline: none;
    transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.page-hero__fields :slotted(input)::placeholder {
    color: var(--theme-text-3);
}

.page-hero__fields :slotted(input):focus {
    border-color: var(--theme-primary);
    box-shadow: var(--theme-ring);
}

.page-hero__fields :slotted(.calendar-picker) {
    flex: 1 1 170px;
    min-width: 0;
}

.page-hero__submit {
    flex: 0 0 auto;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    height: 46px;
    padding: 0 24px;
    border: 0;
    border-radius: 10px;
    background: var(--theme-gradient);
    color: var(--theme-primary-text);
    font-family: inherit;
    font-size: 15px;
    font-weight: 600;
    letter-spacing: 0.02em;
    cursor: pointer;
    transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.page-hero__submit svg {
    width: 17px;
    height: 17px;
    fill: none;
    stroke: currentColor;
    stroke-width: 2;
    stroke-linecap: round;
}

.page-hero__submit:hover {
    box-shadow: var(--theme-shadow-md);
    transform: translateY(-1px);
}

.page-hero__submit:active {
    transform: translateY(0) scale(0.98);
}

.page-hero__submit:focus-visible {
    outline: 2px solid var(--theme-primary);
    outline-offset: 2px;
}

/* ---------- 快捷筛选（可选插槽） ---------- */
.page-hero__chips {
    display: flex;
    flex-wrap: wrap;
    justify-content: center;
    gap: 8px;
    margin-top: 14px;
}

.page-hero__chips :slotted(button) {
    padding: 7px 16px;
    border: 1px solid var(--theme-border);
    border-radius: 999px;
    background: var(--theme-bg-surface);
    color: var(--theme-text-2);
    font-family: inherit;
    font-size: 13px;
    cursor: pointer;
    transition: color 0.2s ease, border-color 0.2s ease, transform 0.2s ease;
}

.page-hero__chips :slotted(button):hover {
    color: var(--theme-primary);
    border-color: var(--theme-primary-soft);
    transform: translateY(-1px);
}

.page-hero__chips :slotted(button.active) {
    background: var(--theme-gradient);
    border-color: transparent;
    color: var(--theme-primary-text);
    font-weight: 600;
}

/* ---------- 响应式 ---------- */
@media (max-width: 768px) {
    .page-hero {
        margin: 12px 0 16px;
        padding: 24px 14px 20px;
        border-radius: 16px;
    }

    .page-hero__subtitle {
        margin-top: 6px;
        font-size: 0.9rem;
    }

    .page-hero__search {
        flex-direction: column;
        gap: 8px;
        margin-top: 16px;
        padding: 8px;
        border-radius: 14px;
    }

    .page-hero__fields {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
        gap: 8px;
    }

    .page-hero__fields :slotted(input) {
        flex: 0 0 auto;
        width: 100%;
        height: 44px;
        padding: 0 14px;
        font-size: 14px;
    }

    .page-hero__fields :slotted(.calendar-picker) {
        flex: 0 0 auto;
        width: 100%;
    }

    .page-hero__submit {
        width: 100%;
        height: 44px;
        padding: 0 18px;
        font-size: 14px;
    }

    .page-hero__chips {
        flex-wrap: nowrap;
        justify-content: flex-start;
        overflow-x: auto;
        padding-bottom: 2px;
    }

    .page-hero__chips :slotted(button) {
        flex: 0 0 auto;
        padding: 6px 14px;
        white-space: nowrap;
        font-size: 12px;
    }
}
</style>
