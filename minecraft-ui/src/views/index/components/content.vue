<template>
    <div class="content-container">
        <section class="section-container">
            <div class="section-header">
                <h2 class="section-title">{{ currentSection.title }}</h2>
                <span class="section-subtitle">{{ currentSection.subtitle }}</span>
            </div>
            <div class="tab-container" role="tablist">
                <button v-for="section in sections" :key="section.type" class="tab-btn" role="tab"
                    :aria-selected="activeSection === section.type"
                    :class="{ active: activeSection === section.type }" @click="switchSection(section.type)">
                    {{ section.title }}
                </button>
            </div>
        </section>

        <section v-if="currentSection" class="content-section">
            <div v-if="loading[currentSection.type]" class="loading-state">
                <div class="loading-spinner"></div>
                <span class="loading-text">{{ $t('homeSections.common.loading') }}</span>
            </div>

            <div v-else-if="error[currentSection.type]" class="error-state">
                <span class="error-text">{{ error[currentSection.type] }}</span>
                <button class="retry-btn" @click="retryFetch(currentSection.type)">
                    {{ $t('homeSections.common.retry') }}
                </button>
            </div>

            <div v-else-if="!displayedItems.length" class="empty-state">
                <p class="empty-text">{{ $t('homeSections.common.empty') }}</p>
            </div>

            <div v-else class="card-grid">
                <div v-for="(item, index) in displayedItems" :key="item.id" class="card"
                    :class="{ 'card--expanded': isExpanded && index >= DEFAULT_DISPLAY_COUNT }"
                    :style="{ animationDelay: `${Math.min(index, DEFAULT_DISPLAY_COUNT) * 0.05}s` }"
                    @mouseenter="onCardHover(item.id, true)" @mouseleave="onCardHover(item.id, false)">
                    <div class="card-image-wrapper">
                        <img :src="item.coverImage" :alt="getImageAlt(currentSection.type)" class="card-image">
                        <div class="card-overlay"></div>
                        <div class="card-category">{{ getCategory(item, currentSection.type) }}</div>
                    </div>

                    <div class="card-content">
                        <h3 class="card-title">{{ item.name }}</h3>
                        <p class="card-description">{{ excerpt(item.description) }}</p>

                        <div class="card-meta">
                            <span class="card-location">{{ item.city }} · {{ getLocation(item, currentSection.type)
                                }}</span>
                            <span class="card-price">{{ getPrice(item, currentSection.type) }}</span>
                        </div>

                        <div class="card-actions">
                            <button class="primary-cta" @click="handleViewDetail(currentSection.type, item.id)">
                                {{ $t('homeSections.common.viewDetail') }}
                                <svg class="cta-arrow" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                                    stroke-width="2" aria-hidden="true">
                                    <path d="M5 12h14M12 5l7 7-7 7" />
                                </svg>
                            </button>
                            <button class="secondary-cta" :aria-label="$t('homeSections.common.favorite')"
                                @click="toggleFavorite(item.id)">
                                <svg class="heart-icon" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
                                    <path
                                        d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z" />
                                </svg>
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            <div v-if="hasMoreData" class="expand-actions">
                <button class="expand-btn" @click="toggleExpand">
                    <span>{{ isExpanded ? $t('homeSections.common.collapse') : $t('homeSections.common.expandAll') }}</span>
                    <svg class="expand-arrow" :class="{ 'expand-arrow--up': isExpanded }" viewBox="0 0 24 24"
                        fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
                        <polyline points="6 9 12 15 18 9" />
                    </svg>
                </button>
            </div>
        </section>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { getRecommendHotels } from '@/api/hotel.js';
import { getRecommendFoods } from '@/api/food.js';
import { getHotAttractions } from '@/api/attraction.js';
import { useAutoRefresh } from '@/hooks/useAutoRefresh';

const emit = defineEmits(['open-detail']);
const { t } = useI18n();

// 默认展示的数据条数
const DEFAULT_DISPLAY_COUNT = 4;

const activeSection = ref('hotel');
const loadedSections = reactive(new Set());
const isExpanded = ref(false);

// 分类数据（随语言切换响应式更新）
const sections = computed(() => [
    {
        type: 'hotel',
        title: t('homeSections.content.hotel.title'),
        subtitle: t('homeSections.content.hotel.subtitle')
    },
    {
        type: 'food',
        title: t('homeSections.content.food.title'),
        subtitle: t('homeSections.content.food.subtitle')
    },
    {
        type: 'attraction',
        title: t('homeSections.content.attraction.title'),
        subtitle: t('homeSections.content.attraction.subtitle')
    }
]);

const currentSection = computed(() => {
    return sections.value.find(item => item.type === activeSection.value);
});

const hotelList = ref([]);
const foodList = ref([]);
const attractionList = ref([]);

const loading = ref({
    hotel: false,
    food: false,
    attraction: false
});

const error = ref({
    hotel: '',
    food: '',
    attraction: ''
});

const hoveredCards = ref(new Set());

const getItemList = (type) => {
    const lists = {
        hotel: hotelList.value,
        food: foodList.value,
        attraction: attractionList.value
    };
    return lists[type] || [];
};

// 默认仅展示前若干条，展开后显示全部
const displayedItems = computed(() => {
    const list = getItemList(activeSection.value);
    if (isExpanded.value || list.length <= DEFAULT_DISPLAY_COUNT) {
        return list;
    }
    return list.slice(0, DEFAULT_DISPLAY_COUNT);
});

// 当前分类是否存在折叠数据
const hasMoreData = computed(() => {
    return getItemList(activeSection.value).length > DEFAULT_DISPLAY_COUNT;
});

const toggleExpand = () => {
    isExpanded.value = !isExpanded.value;
};

/** 截断简介，避免空描述报错 */
const excerpt = (text) => {
    const value = (text || '').trim();
    return value.length > 100 ? `${value.slice(0, 100)}…` : value;
};

const getImageAlt = (type) => t(`homeSections.common.imageAlt.${type}`);

const getCategory = (item, type) => {
    if (type === 'hotel') {
        return item.starRating
            ? t('homeSections.common.starLevel', { level: item.starRating })
            : t('homeSections.common.featured');
    }
    return item.category || t('homeSections.common.featured');
};

const getLocation = (item, type) => {
    if (type === 'hotel') return item.address || '';
    if (type === 'food') return item.restaurant || '';
    if (type === 'attraction') return item.address || '';
    return '';
};

const getPrice = (item, type) => {
    if (type === 'hotel') return t('homeSections.common.pricePerNight', { price: item.price });
    if (type === 'food') return `¥${item.price}`;
    if (type === 'attraction') return `¥${item.ticketPrice || 0}`;
    return '';
};

const onCardHover = (id, isHovered) => {
    if (isHovered) {
        hoveredCards.value.add(id);
    } else {
        hoveredCards.value.delete(id);
    }
};

const toggleFavorite = (id) => {
    console.log('Toggle favorite:', id);
};

const retryFetch = (type) => {
    const fetchers = {
        hotel: fetchHotels,
        food: fetchFoods,
        attraction: fetchAttractions
    };
    if (fetchers[type]) {
        fetchers[type]();
    }
};

const switchSection = (type) => {
    activeSection.value = type;
    // 切换分类时重置为折叠状态
    isExpanded.value = false;
    if (!loadedSections.has(type)) {
        retryFetch(type);
        loadedSections.add(type);
    }
};

const fetchFailed = (type) =>
    t('homeSections.content.fetchFailed', { type: t(`homeSections.content.${type}.title`) });

const fetchHotels = async () => {
    loading.value.hotel = true;
    error.value.hotel = '';
    try {
        const response = await getRecommendHotels(null, 20);
        if (response.code === 200 && response.data) {
            hotelList.value = response.data;
        } else {
            error.value.hotel = fetchFailed('hotel');
        }
    } catch (err) {
        error.value.hotel = t('homeSections.common.networkError');
        console.error('获取酒店失败:', err);
    } finally {
        loading.value.hotel = false;
    }
};

const fetchFoods = async () => {
    loading.value.food = true;
    error.value.food = '';
    try {
        const response = await getRecommendFoods(null, 20);
        if (response.code === 200 && response.data) {
            foodList.value = response.data;
        } else {
            error.value.food = fetchFailed('food');
        }
    } catch (err) {
        error.value.food = t('homeSections.common.networkError');
        console.error('获取美食失败:', err);
    } finally {
        loading.value.food = false;
    }
};

// 数据变更后自动刷新
useAutoRefresh(fetchHotels);
useAutoRefresh(fetchFoods);

const fetchAttractions = async () => {
    loading.value.attraction = true;
    error.value.attraction = '';
    try {
        const response = await getHotAttractions(20);
        if (response.code === 200 && response.data) {
            attractionList.value = response.data;
        } else {
            error.value.attraction = fetchFailed('attraction');
        }
    } catch (err) {
        error.value.attraction = t('homeSections.common.networkError');
        console.error('获取景点失败:', err);
    } finally {
        loading.value.attraction = false;
    }
};

const handleViewDetail = (type, id) => {
    emit('open-detail', type, id);
};

onMounted(() => {
    switchSection('hotel');
});
</script>

<style scoped>
.content-container {
    width: 100%;
}

/* 标题与分类切换同一行：桌面左右分布，移动端上下堆叠 */
.section-container {
    width: 100%;
    display: flex;
    align-items: flex-end;
    justify-content: space-between;
    gap: 24px;
    flex-wrap: wrap;
    margin-bottom: 28px;
}

.section-header {
    min-width: 0;
}

.section-title {
    font-size: clamp(24px, 2.6vw, 34px);
    font-weight: 700;
    color: var(--theme-text-1);
    margin: 0;
    letter-spacing: -0.03em;
    line-height: 1.2;
}

.section-subtitle {
    display: block;
    font-size: 14.5px;
    color: var(--theme-text-2);
    margin-top: 6px;
    font-weight: 400;
}

/* 紧凑分段控件 */
.tab-container {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    padding: 5px;
    background: var(--theme-bg-surface-2);
    border: 1px solid var(--theme-hairline);
    border-radius: 14px;
}

.tab-btn {
    padding: 9px 18px;
    border: none;
    border-radius: 10px;
    background: transparent;
    font-size: 14px;
    font-weight: 500;
    line-height: 1.2;
    color: var(--theme-text-2);
    white-space: nowrap;
    cursor: pointer;
    transition: color 0.24s ease, background 0.24s ease, box-shadow 0.24s ease;
}

.tab-btn:hover {
    background: var(--theme-bg-hover);
    color: var(--theme-text-1);
}

.tab-btn.active {
    background: var(--theme-gradient);
    color: var(--theme-primary-text);
    box-shadow: var(--theme-shadow-sm);
}

.content-section {
    margin-bottom: 0;
    animation: fadeInUp 0.6s ease-out forwards;
}

@keyframes fadeInUp {
    from {
        opacity: 0;
        transform: translateY(20px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.card-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(min(100%, 290px), 1fr));
    gap: 26px;
}

.card {
    min-width: 0;
    display: flex;
    flex-direction: column;
    background: var(--theme-bg-surface);
    border: 1px solid var(--theme-hairline);
    border-radius: 26px;
    overflow: hidden;
    box-shadow: var(--theme-shadow-md);
    transition: transform 0.5s cubic-bezier(0.22, 1, 0.36, 1), box-shadow 0.5s ease,
        border-color 0.5s ease;
    cursor: pointer;
    opacity: 0;
    animation: fadeInUp 0.5s ease-out forwards;
}

.card:hover {
    transform: translateY(-6px);
    border-color: var(--theme-primary);
    box-shadow: var(--theme-shadow-lg), 0 0 0 1px var(--theme-primary-soft);
}

/* 展开时新增卡片使用更轻量的淡入动画 */
.card--expanded {
    animation: fadeInExpand 0.5s ease-out forwards;
}

@keyframes fadeInExpand {
    from {
        opacity: 0;
        transform: translateY(20px) scale(0.98);
    }

    to {
        opacity: 1;
        transform: translateY(0) scale(1);
    }
}

.expand-actions {
    display: flex;
    justify-content: center;
    margin-top: 40px;
}

.expand-btn {
    display: inline-flex;
    align-items: center;
    gap: 10px;
    padding: 12px 32px;
    background: var(--theme-bg-surface);
    border: 1px solid var(--theme-border);
    border-radius: 980px;
    font-size: 14.5px;
    font-weight: 500;
    color: var(--theme-text-1);
    cursor: pointer;
    transition: all 0.3s var(--theme-ease, ease);
    box-shadow: var(--theme-shadow-sm);
}

.expand-btn:hover {
    background: var(--theme-primary-soft);
    border-color: var(--theme-primary);
    color: var(--theme-primary);
    transform: translateY(-2px);
    box-shadow: var(--theme-shadow-md);
}

.expand-btn:active {
    transform: translateY(0);
}

.expand-arrow {
    width: 18px;
    height: 18px;
    transition: transform 0.3s ease;
}

.expand-arrow--up {
    transform: rotate(180deg);
}

.card-image-wrapper {
    position: relative;
    height: 224px;
    overflow: hidden;
    background: var(--theme-bg-surface-2);
}

.card-image {
    display: block;
    width: 100%;
    height: 100%;
    object-fit: cover;
    color: var(--theme-text-3);
    font-size: 13px;
    transition: transform 0.6s cubic-bezier(0.22, 1, 0.36, 1);
}

.card:hover .card-image {
    transform: scale(1.06);
}

.card-overlay {
    position: absolute;
    inset: 0;
    background: linear-gradient(to bottom, transparent 45%, var(--theme-bg-glass) 100%);
    pointer-events: none;
}

.card-category {
    position: absolute;
    top: 14px;
    right: 14px;
    padding: 6px 12px;
    background: var(--theme-bg-glass-strong);
    border: 1px solid var(--theme-border);
    backdrop-filter: blur(10px);
    -webkit-backdrop-filter: blur(10px);
    border-radius: 980px;
    font-size: 11.5px;
    font-weight: 600;
    color: var(--theme-text-1);
    letter-spacing: 0.04em;
}

.card-content {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    padding: 22px;
}

.card-title {
    font-size: 19px;
    font-weight: 600;
    color: var(--theme-text-1);
    margin: 0 0 10px;
    line-height: 1.35;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
}

.card-description {
    font-size: 14px;
    color: var(--theme-text-2);
    margin: 0 0 18px;
    line-height: 1.65;
    min-height: 46px;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
}

.card-meta {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 12px;
    margin-bottom: 20px;
}

.card-location {
    min-width: 0;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    font-size: 13px;
    color: var(--theme-text-3);
    display: flex;
    align-items: center;
    gap: 4px;
}

.card-price {
    flex: 0 0 auto;
    white-space: nowrap;
    font-size: 18px;
    font-weight: 700;
    background: var(--theme-gradient);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
}

.card-actions {
    display: flex;
    gap: 10px;
    justify-content: space-between;
    margin-top: auto;
}

.primary-cta {
    flex: 1;
    padding: 11px 18px;
    background: var(--theme-gradient);
    color: var(--theme-primary-text);
    border: none;
    border-radius: 12px;
    font-size: 14px;
    font-weight: 500;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    transition: all 0.3s var(--theme-ease, ease);
    box-shadow: var(--theme-shadow-sm);
}

.primary-cta:hover {
    transform: translateY(-2px);
    box-shadow: var(--theme-shadow-md);
}

.primary-cta:active {
    transform: translateY(0);
}

.cta-arrow {
    width: 17px;
    height: 17px;
    transition: transform 0.3s ease;
}

.primary-cta:hover .cta-arrow {
    transform: translateX(4px);
}

.secondary-cta {
    width: 44px;
    height: 44px;
    padding: 0;
    flex: 0 0 44px;
    background: var(--theme-bg-surface-2);
    color: var(--theme-text-3);
    border: none;
    border-radius: 12px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.3s ease;
}

.secondary-cta:hover {
    background: var(--theme-danger-soft);
    color: var(--theme-danger);
    transform: scale(1.05);
}

.heart-icon {
    width: 19px;
    height: 19px;
    transition: transform 0.3s ease;
}

.secondary-cta:hover .heart-icon {
    transform: scale(1.1);
}

.loading-state,
.empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 64px 0;
    background: var(--theme-bg-surface);
    border: 1px dashed var(--theme-border);
    border-radius: 24px;
}

.loading-spinner {
    width: 42px;
    height: 42px;
    border: 3px solid var(--theme-border);
    border-top-color: var(--theme-primary);
    border-radius: 50%;
    animation: spin 1s linear infinite;
}

@keyframes spin {
    to {
        transform: rotate(360deg);
    }
}

.loading-text,
.empty-text {
    margin: 18px 0 0;
    font-size: 14.5px;
    color: var(--theme-text-2);
}

.empty-text {
    margin: 0;
}

.error-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 56px 0;
    background: var(--theme-danger-soft);
    border: 1px solid var(--theme-danger);
    border-radius: 24px;
}

.error-text {
    font-size: 14.5px;
    color: var(--theme-danger);
    margin-bottom: 16px;
}

.retry-btn {
    padding: 11px 28px;
    background: var(--theme-gradient);
    color: var(--theme-primary-text);
    border: none;
    border-radius: 12px;
    font-size: 14px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.3s ease;
}

.retry-btn:hover {
    transform: translateY(-2px);
    box-shadow: var(--theme-shadow-md);
}

@media (max-width: 992px) {
    .section-container {
        align-items: center;
        row-gap: 16px;
    }

    .tab-container {
        width: 100%;
        justify-content: space-between;
    }

    .tab-btn {
        flex: 1;
        padding: 10px 6px;
        text-align: center;
    }
}

@media (max-width: 768px) {
    .section-container {
        margin-bottom: 22px;
    }

    .section-title {
        font-size: 24px;
    }

    .section-subtitle {
        font-size: 13.5px;
    }

    .tab-container {
        gap: 4px;
        padding: 4px;
        border-radius: 12px;
    }

    .tab-btn {
        padding: 9px 4px;
        font-size: 13px;
        border-radius: 9px;
        overflow: hidden;
        text-overflow: ellipsis;
    }

    .card-grid {
        grid-template-columns: minmax(0, 1fr);
        gap: 20px;
    }

    .expand-actions {
        margin-top: 30px;
    }

    .card-image-wrapper {
        height: 200px;
    }

    .card-content {
        padding: 20px;
    }

    .card-title {
        font-size: 18px;
    }

    .card-description {
        font-size: 13.5px;
        min-height: 0;
        margin-bottom: 16px;
    }

    .card-meta {
        margin-bottom: 18px;
    }

    .card-price {
        font-size: 17px;
    }
}

@media (max-width: 375px) {
    .section-title {
        font-size: 22px;
    }

    .tab-btn {
        font-size: 12.5px;
    }

    .card-image-wrapper {
        height: 184px;
    }

    .card-content {
        padding: 18px;
    }

    .card-title {
        font-size: 17px;
    }

    .primary-cta {
        padding: 10px 14px;
        font-size: 13.5px;
    }

    .secondary-cta {
        flex: 0 0 40px;
        width: 40px;
        height: 40px;
    }
}
</style>
