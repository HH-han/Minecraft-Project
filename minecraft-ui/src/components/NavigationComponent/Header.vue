<template>
  <!-- 导航栏 -->
  <div class="navbar-background">
    <!-- 液态玻璃装饰层：折射渐层 / 流动高光 / 底部亮边 -->
    <div class="navbar-glass" aria-hidden="true">
      <span class="navbar-glass__refraction"></span>
      <span class="navbar-glass__sheen"></span>
      <span class="navbar-glass__edge"></span>
    </div>
    <div class="navbar-background-container">
      <div class="navbar-home">
        <div class="logo" @click="navigateTo('/home')">
          <img v-if="mediaList.images.length > 0" :src="mediaList.images[0].image" :alt="$t('common.brand')">
          <img v-else :src="defaultLogo" :alt="$t('common.brand')">
          <span class="brand-text">{{ $t('common.brand') }}</span>
        </div>
        <nav class="nav-items" :aria-label="$t('header.nav.sectionNav')">
          <div class="center-items">
            <button v-for="navItem in navItems" :key="navItem.path" class="action-btn"
              :class="{ 'nav-item-active': isActiveNav(navItem.path) }" :aria-current="isActiveNav(navItem.path) ? 'page' : null"
              @click="navigateTo(navItem.path)">
              {{ navItem.labelKey ? $t(navItem.labelKey) : navItem.label }}
            </button>
          </div>
          <div class="dropdown">
            <button class="action-btn dropdown-toggle" aria-haspopup="true">
              {{ $t('header.more') }}
              <svg class="dropdown-caret" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                <polyline points="6 9 12 15 18 9" />
              </svg>
            </button>
            <div class="dropdown-menu_Home_2">
              <button v-for="item in moreMenuItems" :key="item.path" class="dropdown-item_action-btn"
                @click="navigateTo(item.path)">{{ item.labelKey ? $t(item.labelKey) : item.label }}</button>
            </div>
          </div>
        </nav>
        <div class="actions">
          <LanguageSwitcher />
          <ThemeSwitching />
          <!-- 微信 -->
          <button class="action-btn-iocn" aria-label="WeChat" @click="openSocialLogin('wechat')">
            <svg t="1737552806004" class="action-btn-iocn-size" viewBox="0 0 1024 1024" version="1.1"
              xmlns="http://www.w3.org/2000/svg" p-id="6242">
              <path
                d="M337.387283 341.82659c-17.757225 0-35.514451 11.83815-35.514451 29.595375s17.757225 29.595376 35.514451 29.595376 29.595376-11.83815 29.595376-29.595376c0-18.49711-11.83815-29.595376-29.595376-29.595375zM577.849711 513.479769c-11.83815 0-22.936416 12.578035-22.936416 23.6763 0 12.578035 11.83815 23.676301 22.936416 23.676301 17.757225 0 29.595376-11.83815 29.595376-23.676301s-11.83815-23.676301-29.595376-23.6763zM501.641618 401.017341c17.757225 0 29.595376-12.578035 29.595376-29.595376 0-17.757225-11.83815-29.595376-29.595376-29.595375s-35.514451 11.83815-35.51445 29.595375 17.757225 29.595376 35.51445 29.595376zM706.589595 513.479769c-11.83815 0-22.936416 12.578035-22.936416 23.6763 0 12.578035 11.83815 23.676301 22.936416 23.676301 17.757225 0 29.595376-11.83815 29.595376-23.676301s-11.83815-23.676301-29.595376-23.6763z"
                fill="#28C445" p-id="6243"></path>
              <path
                d="M510.520231 2.959538C228.624277 2.959538 0 231.583815 0 513.479769s228.624277 510.520231 510.520231 510.520231 510.520231-228.624277 510.520231-510.520231-228.624277-510.520231-510.520231-510.520231zM413.595376 644.439306c-29.595376 0-53.271676-5.919075-81.387284-12.578034l-81.387283 41.433526 22.936416-71.768786c-58.450867-41.433526-93.965318-95.445087-93.965317-159.815029 0-113.202312 105.803468-201.988439 233.803468-201.98844 114.682081 0 216.046243 71.028902 236.023121 166.473989-7.398844-0.739884-14.797688-1.479769-22.196532-1.479769-110.982659 1.479769-198.289017 85.086705-198.289017 188.67052 0 17.017341 2.959538 33.294798 7.398844 49.572255-7.398844 0.739884-15.537572 1.479769-22.936416 1.479768z m346.265896 82.867052l17.757225 59.190752-63.630058-35.514451c-22.936416 5.919075-46.612717 11.83815-70.289017 11.83815-111.722543 0-199.768786-76.947977-199.768786-172.393063-0.739884-94.705202 87.306358-171.653179 198.289017-171.65318 105.803468 0 199.028902 77.687861 199.028902 172.393064 0 53.271676-34.774566 100.624277-81.387283 136.138728z"
                fill="#28C445" p-id="6244"></path>
            </svg>
          </button>
          <!-- QQ -->
          <button class="action-btn-iocn" aria-label="QQ" @click="openSocialLogin('qq')">
            <svg t="1737552871738" class="action-btn-iocn-size" viewBox="0 0 1024 1024" version="1.1"
              xmlns="http://www.w3.org/2000/svg" p-id="7246">
              <path
                d="M512 0C229.003636 0 0 229.003636 0 512s229.003636 512 512 512 512-229.003636 512-512S794.996364 0 512 0z m210.385455 641.396364c-7.447273 9.309091-26.996364-1.861818-41.89091-32.581819-3.723636 13.963636-13.032727 36.305455-34.443636 64.232728 35.374545 8.378182 44.683636 42.821818 33.512727 61.44-8.378182 13.032727-26.996364 24.203636-59.578181 24.203636-58.647273 0-83.781818-15.825455-95.883637-26.996364-1.861818-2.792727-5.585455-3.723636-10.24-3.723636-4.654545 0-7.447273 0.930909-10.24 3.723636-11.170909 11.170909-37.236364 26.996364-95.883636 26.996364-32.581818 0-52.130909-11.170909-59.578182-24.203636-12.101818-18.618182-1.861818-53.061818 33.512727-61.44-20.48-27.927273-29.789091-50.269091-34.443636-64.232728-13.963636 30.72-34.443636 42.821818-41.890909 32.581819-5.585455-8.378182-8.378182-26.065455-7.447273-38.167273 3.723636-46.545455 34.443636-85.643636 53.061818-106.123636-2.792727-5.585455-8.378182-40.029091 14.894546-63.301819v-1.861818c0-92.16 65.163636-158.254545 148.014545-158.254545 81.92 0 148.014545 66.094545 148.014546 158.254545v1.861818c23.272727 23.272727 17.687273 57.716364 14.894545 63.301819 17.687273 20.48 49.338182 59.578182 53.061818 106.123636 0.930909 12.101818-0.930909 29.789091-7.447272 38.167273z"
                fill="#30A5DD" p-id="7247"></path>
            </svg>
          </button>
          <!--微博  -->
          <button class="action-btn-iocn" aria-label="Weibo" @click="openSocialLogin('weibo')">
            <svg t="1737552907646" class="action-btn-iocn-size" viewBox="0 0 1024 1024" version="1.1"
              xmlns="http://www.w3.org/2000/svg" p-id="8263">
              <path
                d="M448.698182 482.210909c-96.814545 4.654545-175.010909 56.785455-175.010909 121.949091s78.196364 114.501818 175.010909 109.847273S623.709091 647.912727 623.709091 582.749091c-0.930909-64.232727-79.127273-105.192727-175.010909-100.538182z m65.163636 164.770909c-29.789091 39.098182-88.436364 57.716364-145.221818 26.065455-26.996364-14.894545-26.065455-43.752727-26.065455-43.752728s-11.170909-92.16 85.643637-103.330909c97.745455-12.101818 115.432727 81.92 85.643636 121.018182z"
                fill="#EA5D5C" p-id="8264"></path>
              <path
                d="M448.698182 584.610909c-6.516364 4.654545-7.447273 13.032727-3.723637 18.618182 2.792727 5.585455 11.170909 6.516364 16.756364 1.861818 5.585455-4.654545 8.378182-13.032727 4.654546-18.618182-2.792727-5.585455-10.24-6.516364-17.687273-1.861818zM403.083636 597.643636c-18.618182 1.861818-30.72 17.687273-30.72 33.512728 0 14.894545 14.894545 26.065455 32.581819 24.203636 17.687273-1.861818 32.581818-15.825455 32.581818-31.650909s-13.963636-27.927273-34.443637-26.065455z"
                fill="#EA5D5C" p-id="8265"></path>
              <path
                d="M512 0C229.003636 0 0 229.003636 0 512s229.003636 512 512 512 512-229.003636 512-512S794.996364 0 512 0z m197.352727 626.501818C669.323636 712.145455 538.065455 754.036364 441.250909 746.589091c-92.16-7.447273-211.316364-38.167273-223.418182-151.738182 0 0-6.516364-51.2 42.821818-117.294545 0 0 70.749091-99.607273 152.669091-128.465455 82.850909-27.927273 92.16 19.549091 92.16 48.407273-4.654545 24.203636-12.101818 38.167273 18.618182 28.858182 0 0 80.989091-38.167273 114.501818-4.654546 26.996364 26.996364 4.654545 65.163636 4.654546 65.163637s-11.170909 12.101818 12.101818 16.756363c21.410909 3.723636 94.021818 37.236364 53.992727 122.88z m-80.058182-236.450909c-8.378182 0-15.825455-7.447273-15.825454-15.825454 0-9.309091 7.447273-15.825455 15.825454-15.825455 0 0 99.607273-18.618182 87.505455 89.367273v1.861818c-0.930909 7.447273-7.447273 13.963636-15.825455 13.963636-9.309091 0-15.825455-7.447273-15.825454-15.825454 0-1.861818 15.825455-73.541818-55.854546-57.716364zM797.789091 493.381818c-2.792727 18.618182-12.101818 11.170909-22.341818 11.170909-13.032727 0-23.272727-16.756364-23.272728-29.789091 0-11.170909 4.654545-22.341818 4.654546-22.341818 0.930909-4.654545 12.101818-34.443636-7.447273-78.196363-35.374545-60.509091-106.123636-60.509091-114.501818-57.716364-8.378182 3.723636-21.410909 5.585455-21.410909 5.585454-13.032727 0-23.272727-10.24-23.272727-23.272727 0-11.170909 7.447273-19.549091 16.756363-22.341818 0 0 0 0.930909 0.930909 0.930909s1.861818 0.930909 1.861819 0.930909c10.24-1.861818 45.614545-4.654545 79.127272 3.723637 62.370909 14.894545 146.152727 83.781818 108.916364 211.316363z"
                fill="#EA5D5C" p-id="8266"></path>
            </svg>
          </button>
          <!-- 系统公告铃铛（未读数红点） -->
          <AnnouncementBell />
          <!-- 登录和注册按钮（未登录时显示） -->
          <template v-if="!isLoggedIn">
            <button class="action-btn action-btn--ghost" @click="LoginName">
              <User class="btn-icon" aria-hidden="true" />
              <span>{{ $t('header.login') }}</span>
            </button>
            <button class="action-btn action-btn--primary" @click="EnrolFirst">
              <CirclePlus class="btn-icon" aria-hidden="true" />
              <span>{{ $t('header.register') }}</span>
            </button>
          </template>

          <!-- 用户头像（登录后显示） -->
          <div class="user-actions" v-else>
            <div class="profilepicture">
              <button class="profilepicture_button" :class="{ 'is-open': isDropdownVisible }" @click="toggleDropdown">
                <div class="profilepicture_img">
                  <img :src="avatarSrc" :alt="$t('header.userMenu.profile')" class="img1" @error="onAvatarError" />
                </div>
              </button>
              <div class="profilepicture-inner" v-show="isDropdownVisible">
                <button v-for="item in filteredUserMenuItems" :key="item.label" :style="item.style" :class="item.class"
                  @click="handleMenuItemClick(item)">
                  {{ item.label }}
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
  <!-- 移动端底部悬浮导航（≤992px 显示）：4 个核心页面 + 「更多」入口 -->
  <nav class="mobile-tabbar" :aria-label="$t('header.nav.menu')">
    <button v-for="tab in mobileTabs" :key="tab.key" class="mobile-tab"
      :class="{ 'is-active': tab.more ? mobileMenuOpen || isMoreActive : isActiveNav(tab.path) }"
      @click.stop="onTabClick(tab)">
      <span class="mobile-tab-icon">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"
          stroke-linejoin="round">
          <path v-for="d in tabIcon(tab)" :key="d" :d="d" />
        </svg>
      </span>
      <span class="mobile-tab-label">{{ $t(tab.labelKey) }}</span>
    </button>
  </nav>

  <!-- 移动端「更多」底部浮层（≤992px 显示，其余页面以宫格展示） -->
  <Transition name="mobile-sheet-fade">
    <div v-if="mobileMenuOpen" class="mobile-sheet-mask" @click="mobileMenuOpen = false"></div>
  </Transition>
  <Transition name="mobile-sheet-slide">
    <div v-if="mobileMenuOpen" class="mobile-sheet" @click.stop>
      <span class="mobile-sheet-handle"></span>
      <div class="mobile-sheet-head">
        <span class="mobile-sheet-title">{{ $t('header.nav.sectionMore') }}</span>
        <button class="mobile-sheet-close" :aria-label="$t('header.nav.menu')" @click="mobileMenuOpen = false">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
            <path d="M18 6 6 18" />
            <path d="m6 6 12 12" />
          </svg>
        </button>
      </div>
      <div class="mobile-sheet-grid">
        <button v-for="item in moreSheetItems" :key="item.path" class="mobile-sheet-tile"
          :class="{ 'nav-item-active': isActiveNav(item.path) }" @click="navigateTo(item.path)">
          <span class="mobile-sheet-iconbox">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"
              stroke-linejoin="round">
              <path v-for="d in navIcon(item.path)" :key="d" :d="d" />
            </svg>
          </span>
          <span class="mobile-sheet-label">{{ item.labelKey ? $t(item.labelKey) : item.label }}</span>
        </button>
      </div>
    </div>
  </Transition>
  <!-- 页面渲染区域：由路由驱动 -->
  <main>
    <div class="content-area">
      <router-view />
    </div>
  </main>

  <!-- 退出登录确认对话框 -->
  <div>
    <Launchlogin v-if="showLogoutConfirm" @confirm="confirmLogout" @cancel="cancelLogout" />
  </div>
  <!-- 返回顶部 -->
  <div>
    <BacktoTop />
  </div>
  
  <!-- 社交平台登录模态框 -->
  <SocialLoginModal 
    :visible="showSocialLoginModal" 
    :platform="currentPlatform"
    @close="closeSocialLoginModal" 
  />
</template>
<script setup>
import { useRoute, useRouter } from 'vue-router';
import { ref, onMounted, onBeforeUnmount, computed, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { User, CirclePlus } from '@element-plus/icons-vue';
import { useI18n } from 'vue-i18n';
// 页面组件
import ThemeSwitching from '@/components/ThemeComponents/ThemeSwitching.vue';
import LanguageSwitcher from '@/components/NavigationComponent/LanguageSwitcher.vue';
import AnnouncementBell from '@/components/AnnouncementComponents/AnnouncementBell.vue';
import Launchlogin from '@/components/PromptComponent/Launchlogin.vue';
import BacktoTop from '@/components/DisplayBox/BacktoTop.vue';
import SocialLoginModal from '@/components/PromptComponent/SocialLoginModal.vue';
// 导航菜单（由路由表 meta 派生，路由即导航的唯一数据源）
import { mainNavMenus, moreNavMenus } from '@/router/routes';
// 接口
import { useAuthStore } from '@/stores/auth';
import { usePermissionStore } from '@/stores/permissionStore';
import { getUserInfo, logout } from '@/api/user';
import { normalizeImageUrl } from '@/utils/imageUrl';

const props = defineProps({
  mediaList: {
    type: Object,
    default: () => ({ images: [] })
  }
});

const defaultAvatar = new URL('@/assets/defaultimage/mrtx.png', import.meta.url).href
const defaultLogo = new URL('@/assets/logo/logo.png', import.meta.url).href
const authStore = useAuthStore();
const permissionStore = usePermissionStore();

const canSee = (menu) => !menu.permission || permissionStore.hasPerm(menu.permission);

// 导航项数据（路由表派生，按权限过滤）
const navItems = computed(() => mainNavMenus.filter(canSee));

// 更多下拉菜单项（路由表派生，按权限过滤）
const moreMenuItems = computed(() => moreNavMenus.filter(canSee));

const { t } = useI18n();

// 用户操作菜单项数据（label 使用 i18n key，随语言切换响应式更新）
const userMenuItems = computed(() => [
  {
    label: t('header.userMenu.profile'),
    path: '/personalcenter',
    style: 'border-radius: 10px 10px 0 0;',
    show: true
  },
  {
    label: t('header.userMenu.community'),
    path: '/privatecommunity',
    style: 'border-radius: 0 0 10px 10px;',
    show: true
  },
  {
    label: t('header.userMenu.chat'),
    path: '/chat',
    show: true,
    class: 'im-button'
  },
  {
    label: t('header.userMenu.logout'),
    action: 'localLogout',
    show: true,
    class: 'logout-button'
  },
]);

const router = useRouter();
const route = useRoute();

/** 判断导航项是否为当前激活路由（含子路径） */
const isActiveNav = (path) => {
  return route.path === path || route.path.startsWith(`${path}/`);
};

/** 统一的导航跳转入口 */
const navigateTo = (path) => {
  if (path === route.path) return;
  router.push(path);
};
// 默认头像

// 页面跳转
const LoginName = () => {
  router.push('/login');
  ElMessage.success(t('header.messages.backToLogin'));
};
const EnrolFirst = () => {
  router.push('/login');
  ElMessage.success(t('header.messages.goRegister'));
};

const openSocialLogin = (platform) => {
  currentPlatform.value = platform;
  showSocialLoginModal.value = true;
};

const closeSocialLoginModal = () => {
  showSocialLoginModal.value = false;
};

const isLoggedIn = ref(false);
const isDropdownVisible = ref(false);
// 移动端「更多」浮层开关
const mobileMenuOpen = ref(false);

// 路由切换后自动收起移动端浮层
watch(() => route.path, () => {
  mobileMenuOpen.value = false;
});

// 点击页面其他区域时收起移动端浮层
const onDocumentClick = () => {
  mobileMenuOpen.value = false;
};
// 视口回到桌面宽度时收起浮层，避免滚动锁残留
const onWindowResize = () => {
  if (window.innerWidth > 992) mobileMenuOpen.value = false;
};

/* ---------- 果冻点击反馈：所有按钮（含子组件内按钮）统一使用 ---------- */
const JELLY_SELECTOR = '.navbar-home button, .mobile-tab, .mobile-sheet-tile, .mobile-sheet-close, .logo';
const JELLY_CLASS = 'is-jelly';

const playJelly = (el) => {
  if (!el || el.disabled) return;
  el.classList.remove(JELLY_CLASS);
  // 强制回流，保证连续快速点击也能重新播放动画
  void el.offsetWidth;
  el.classList.add(JELLY_CLASS);
};

const onJellyPress = (event) => {
  // 鼠标 / 触摸操作时关闭键盘焦点环，避免点击后残留描边
  document.documentElement.classList.remove('kb-nav');
  const el = event.target?.closest?.(JELLY_SELECTOR);
  if (el) playJelly(el);
};

// 键盘触发（Enter / Space）同样播放果冻反馈，并保留焦点环
const onJellyKeyDown = (event) => {
  if (event.key !== 'Enter' && event.key !== ' ') return;
  document.documentElement.classList.add('kb-nav');
  const el = event.target?.closest?.(JELLY_SELECTOR);
  if (el) playJelly(el);
};

const onJellyAnimationEnd = (event) => {
  if (event.animationName === 'jelly-pop') {
    event.target?.classList?.remove(JELLY_CLASS);
  }
};

onMounted(() => {
  document.addEventListener('click', onDocumentClick);
  window.addEventListener('resize', onWindowResize);
  document.addEventListener('pointerdown', onJellyPress, { passive: true });
  document.addEventListener('keydown', onJellyKeyDown);
  document.addEventListener('animationend', onJellyAnimationEnd);
});
onBeforeUnmount(() => {
  document.removeEventListener('click', onDocumentClick);
  window.removeEventListener('resize', onWindowResize);
  document.removeEventListener('pointerdown', onJellyPress);
  document.removeEventListener('keydown', onJellyKeyDown);
  document.removeEventListener('animationend', onJellyAnimationEnd);
  document.documentElement.classList.remove('kb-nav');
  document.body.style.overflow = '';
});

/** 移动端菜单项图标（按路由路径映射，24x24 线性图标） */
const MOBILE_NAV_ICONS = {
  '/home': ['M3 10.5 12 3l9 7.5', 'M5 9.5V21h14V9.5'],
  '/worldtravel': ['M12 21a9 9 0 1 0 0-18 9 9 0 0 0 0 18z', 'M3 12h18', 'M12 3c2.6 2.6 4 5.6 4 9s-1.4 6.4-4 9c-2.6-2.6-4-5.6-4-9s1.4-6.4 4-9z'],
  '/scenicspot': ['M12 21s-7-5.5-7-11a7 7 0 1 1 14 0c0 5.5-7 11-7 11z', 'M12 12.5a2.5 2.5 0 1 0 0-5 2.5 2.5 0 0 0 0 5z'],
  '/hotel': ['M4 21V7l8-4 8 4v14', 'M9 21v-6h6v6', 'M9 10h.01', 'M15 10h.01'],
  '/food': ['M7 2v20', 'M7 2v6a2.5 2.5 0 0 0 5 0V2', 'M17 2c-1.7 1.2-2.5 3.4-2.5 5.5 0 2.1 1 3.5 2.5 3.5V22'],
  '/souvenir': ['M6 7h12l1 14H5L6 7z', 'M9 7a3 3 0 0 1 6 0'],
  '/strategy': ['M4 5.5A2.5 2.5 0 0 1 6.5 3H20v15H6.5A2.5 2.5 0 0 0 4 20.5z', 'M8 8h8', 'M8 12h6'],
  '/cards': ['M2 6h20v13H2z', 'M2 10h20', 'M6 15h4'],
  '/community': ['M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z', 'M8 9h8', 'M8 13h5'],
  '/ticket': ['M3 9V7a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2v2a3 3 0 0 0 0 6v2a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-2a3 3 0 0 0 0-6z', 'M13 5v2', 'M13 11v2', 'M13 17v2'],
  '/travel': ['M3 5h18v16H3z', 'M3 10h18', 'M8 3v4', 'M16 3v4', 'M8 15h3'],
  '/travel-note': ['M5 3h11l3 3v15H5z', 'M9 8h6', 'M9 12h6', 'M9 16h4'],
  '/maps': ['M12 22a10 10 0 1 0 0-20 10 10 0 0 0 0 20z', 'M2 12h20', 'M12 2c3 3 4 6.5 4 10s-1 7-4 10c-3-3-4-6.5-4-10s1-7 4-10z'],
  '/test': ['M9 3h6', 'M10 3v6L4.5 19a2 2 0 0 0 1.8 3h11.4a2 2 0 0 0 1.8-3L14 9V3', 'M7 15h10'],
  '/aboutweb': ['M12 22a10 10 0 1 0 0-20 10 10 0 0 0 0 20z', 'M12 16v-4', 'M12 8h.01'],
};
const DEFAULT_NAV_ICON = ['M4 6h16', 'M4 12h16', 'M4 18h16'];
/** 「更多」入口图标（2x2 宫格） */
const MORE_TAB_ICON = ['M4 4h6v6H4z', 'M14 4h6v6h-6z', 'M4 14h6v6H4z', 'M14 14h6v6h-6z'];
const navIcon = (path) => MOBILE_NAV_ICONS[path] ?? DEFAULT_NAV_ICON;

/** 移动端底部导航固定标签：只保留高频核心页面，其余收进「更多」浮层 */
const mobileTabs = [
  { key: 'home', path: '/home', labelKey: 'header.nav.home' },
  { key: 'destination', path: '/worldtravel', labelKey: 'header.nav.destination' },
  { key: 'scenic', path: '/scenicspot', labelKey: 'header.nav.scenic' },
  { key: 'hotel', path: '/hotel', labelKey: 'header.nav.hotel' },
  { key: 'more', more: true, labelKey: 'header.more' },
];
const mobileTabPaths = mobileTabs.filter((tab) => tab.path).map((tab) => tab.path);
const tabIcon = (tab) => (tab.more ? MORE_TAB_ICON : navIcon(tab.path));

/** 「更多」浮层内容 = 主菜单中未上底部栏的项 + 全部更多菜单项 */
const moreSheetItems = computed(() => [
  ...navItems.value.filter((item) => !mobileTabPaths.includes(item.path)),
  ...moreMenuItems.value,
]);

/** 当前页面属于「更多」中的某一项时，高亮底部「更多」标签 */
const isMoreActive = computed(() => moreSheetItems.value.some((item) => isActiveNav(item.path)));

/** 底部标签点击：核心页面直接跳转，「更多」展开/收起浮层 */
const onTabClick = (tab) => {
  if (tab.more) {
    mobileMenuOpen.value = !mobileMenuOpen.value;
    return;
  }
  mobileMenuOpen.value = false;
  navigateTo(tab.path);
};

// 浮层打开时锁定页面滚动，避免背景跟随滚动
watch(mobileMenuOpen, (open) => {
  document.body.style.overflow = open ? 'hidden' : '';
});
const showLogoutConfirm = ref(false);
const showSocialLoginModal = ref(false);
const currentPlatform = ref('wechat');
const toggleDropdown = () => {
  isDropdownVisible.value = !isDropdownVisible.value;
};
const localLogout = () => {
  // 检查是否有token，没有则直接退出登录状态
  const token = localStorage.getItem('token');
  const tokenTimestamp = localStorage.getItem('tokenTimestamp');
  // 检查token是否存在、是否为anonymousUser或是否超过24小时
  const isTokenExpired = tokenTimestamp && (Date.now() - parseInt(tokenTimestamp) > 24 * 60 * 60 * 1000);
  if (!token || token === 'anonymousUser' || isTokenExpired) {
    // 清除所有登录相关存储
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    localStorage.removeItem('rememberedUsername');
    localStorage.removeItem('tokenTimestamp');
    isLoggedIn.value = false;
    return;
  }

  // 显示退出确认对话框
  showLogoutConfirm.value = true;
};

// 确认退出登录
const confirmLogout = async () => {
  try {
    // 调用API退出登录
    await logout();

    // 清除所有登录相关存储
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    localStorage.removeItem('rememberedUsername');
    localStorage.removeItem('tokenTimestamp');

    // 更新登录状态
    isLoggedIn.value = false;
    // 关闭下拉菜单
    isDropdownVisible.value = false;
    // 关闭确认对话框
    showLogoutConfirm.value = false;

    ElMessage.success(t('header.messages.logoutSuccess'));
  } catch (error) {
    console.error('退出登录失败:', error);
    // 即使API调用失败，也清除本地存储
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    localStorage.removeItem('rememberedUsername');
    localStorage.removeItem('tokenTimestamp');
    isLoggedIn.value = false;
    isDropdownVisible.value = false;
    showLogoutConfirm.value = false;
    ElMessage.success(t('header.messages.loggedOut'));
  }
};

// 取消退出登录
const cancelLogout = () => {
  // 关闭确认对话框
  showLogoutConfirm.value = false;
  console.log('退出操作已取消');
};

// 处理菜单项点击
const handleMenuItemClick = (item) => {
  if (item.path) {
    navigateTo(item.path);
  } else if (item.action) {
    // 根据action名称调用对应的方法
    switch (item.action) {
      case 'AdminLayout':
        AdminLayout();
        break;
      case 'localLogout':
        localLogout();
        break;
      default:
        console.warn(`未知的操作: ${item.action}`);
    }
  }
};

// 用户信息
const userInfo = ref({
  username: '',
  permissions: '',
  avatar: ''
});

// 头像加载失败（如断网、图片地址失效）时回退到默认头像，避免出现浏览器破图占位
const avatarLoadFailed = ref(false);
const avatarSrc = computed(() =>
  !userInfo.value.avatar || avatarLoadFailed.value
    ? defaultAvatar
    : normalizeImageUrl(userInfo.value.avatar)
);
const onAvatarError = () => {
  if (!avatarLoadFailed.value) avatarLoadFailed.value = true;
};
// 头像地址变化后重新尝试加载远程头像
watch(() => userInfo.value.avatar, () => {
  avatarLoadFailed.value = false;
});

// 过滤后的用户菜单项
const filteredUserMenuItems = computed(() => {
  return userMenuItems.value.filter(item => {
    if (typeof item.show === 'function') {
      return item.show();
    }
    return item.show;
  });
});

const loading = ref(false);

// 获取用户信息
const fetchUserInfo = async () => {
  try {
    loading.value = true;
    //检查是否有token
    const token = localStorage.getItem('token');
    if (!token || token === 'anonymousUser') {
      isLoggedIn.value = false;
      return;
    }
    // 先从本地存储获取，优化用户体验
    const localUserStr = localStorage.getItem('user');
    if (localUserStr) {
      try {
        const localUser = JSON.parse(localUserStr);
        userInfo.value = { ...localUser };
        isLoggedIn.value = true;
      } catch (e) {
        console.error('解析本地用户数据失败:', e);
      }
    }

    //从服务器获取最新数据
    const response = await getUserInfo();

    if (response.code === 200) {
      // 拿到最新用户信息后重新尝试加载远程头像（此前可能因断网失败）
      avatarLoadFailed.value = false;
      userInfo.value = response.data;
      localStorage.setItem('user', JSON.stringify(response.data));
      isLoggedIn.value = true;
    } else {
      console.warn('获取用户信息返回非200状态:', response);
    }
  } catch (error) {
    console.error('获取用户信息失败:', error);
    // 检查是否是 401 或 403 错误
    const status = error.response?.status;
    if (status === 401 || status === 403) {
      // 清除无效的登录状态
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      localStorage.removeItem('rememberedUsername');
      localStorage.removeItem('tokenTimestamp');
      isLoggedIn.value = false;
      userInfo.value = {
        username: '',
        permissions: '',
        avatar: ''
      };
    }
  } finally {
    loading.value = false;
  }
};



// 初始化检查登录状态
onMounted(() => {
  isLoggedIn.value = !!localStorage.getItem('token');
  fetchUserInfo();
});
</script>

<style scoped>
@import "@/css/Home/HomeViews.css";

/* ================= Logo 品牌区 ================= */

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
  cursor: pointer;
  user-select: none;
}

.logo img {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  object-fit: cover;
  box-shadow: var(--theme-shadow-sm), 0 0 0 1px var(--theme-border);
  transition: transform 0.35s cubic-bezier(0.25, 0.8, 0.25, 1), box-shadow 0.35s ease;
}

.logo:hover img {
  transform: scale(1.06) rotate(-3deg);
  box-shadow: var(--theme-shadow-md), 0 0 0 1px var(--theme-primary-soft);
}

/* 品牌文字：品牌蓝渐变 + hover 光泽扫过 */
.brand-text {
  position: relative;
  font-size: 22px;
  font-weight: 800;
  letter-spacing: 2px;
  background: var(--theme-gradient);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  color: transparent;
  white-space: nowrap;
  overflow: hidden;
}

.brand-text::after {
  content: '';
  position: absolute;
  top: 0;
  bottom: 0;
  left: -60%;
  width: 40%;
  transition: left 0.6s ease;
  pointer-events: none;
}

.logo:hover .brand-text::after {
  left: 120%;
}

/* ================= 导航栏骨架 ================= */

/* ================= 液态玻璃导航外壳 ================= */

.navbar-background {
  --lg-tint: color-mix(in srgb, var(--theme-bg-app) 26%, transparent);
  --lg-highlight: rgba(255, 255, 255, 0.62);
  --lg-sheen: rgba(255, 255, 255, 0.42);
  --lg-edge: color-mix(in srgb, var(--theme-text-1) 9%, transparent);

  background: var(--lg-tint);
  -webkit-backdrop-filter: blur(26px) saturate(200%);
  backdrop-filter: blur(26px) saturate(200%);
  border-bottom: 1px solid var(--lg-edge);
  box-shadow:
    0 22px 48px -34px rgba(15, 23, 42, 0.55),
    inset 0 1px 0 var(--lg-highlight);
}

/* 暗色 / 游戏主题：压低白色高光，避免玻璃发灰 */
html[data-theme="dark"] .navbar-background,
html[data-theme="game"] .navbar-background {
  --lg-tint: color-mix(in srgb, var(--theme-bg-app) 62%, transparent);
  --lg-highlight: rgba(255, 255, 255, 0.14);
  --lg-sheen: rgba(255, 255, 255, 0.2);
  --lg-edge: rgba(255, 255, 255, 0.08);
  box-shadow:
    0 24px 52px -32px rgba(0, 0, 0, 0.72),
    inset 0 1px 0 var(--lg-highlight);
}

/* ================= 亮色主题文字可读性优化 ================= */

/* 提高玻璃底白度，减少背后图片颜色渗透导致文字发灰 */
html[data-theme="light"] .navbar-background {
  --lg-tint: color-mix(in srgb, var(--theme-bg-app) 16%, transparent);
  --lg-highlight: rgba(255, 255, 255, 0.78);
  --lg-edge: color-mix(in srgb, var(--theme-text-1) 12%, transparent);
}

/* 亮色下降低折射白光强度，避免文字上方泛白 */
html[data-theme="light"] .navbar-glass__refraction {
  opacity: 0.42;
}

/* 全局 body 的白色文字辉光会让深色字边缘发虚，导航区统一清除 */
.navbar-background {
  text-shadow: none;
}

/* 品牌文字：亮色改为实色，避免渐变在浅色玻璃上失去对比 */
html[data-theme="light"] .brand-text {
  background: none;
  -webkit-background-clip: border-box;
  background-clip: border-box;
  -webkit-text-fill-color: currentColor;
  color: var(--theme-text-1);
}

/* 亮色下导航默认使用最深文本色 */
html[data-theme="light"] .nav-items .action-btn {
  color: var(--theme-text-1);
}

/* 亮色选中态：白底胶囊 + 主题色细环 + 加深主色文字 */
html[data-theme="light"] .nav-items .action-btn.nav-item-active {
  color: color-mix(in srgb, var(--theme-primary) 78%, #ffffff);
  background:
    linear-gradient(180deg, #ffffff, color-mix(in srgb, #ffffff 88%, var(--theme-primary)));
  box-shadow:
    inset 0 0 0 1px color-mix(in srgb, var(--theme-primary) 24%, transparent),
    inset 0 1px 0 #ffffff,
    0 8px 18px -12px rgba(15, 23, 42, 0.35);
}

html[data-theme="light"] .nav-items .action-btn.nav-item-active::after {
  background: var(--theme-primary);
  box-shadow:
    0 2px 8px color-mix(in srgb, var(--theme-primary) 42%, transparent),
    0 1px 1px rgba(15, 23, 42, 0.12);
}

/* 装饰层：独立裁剪，不影响下拉面板溢出 */
.navbar-glass {
  position: absolute;
  inset: 0;
  z-index: 0;
  overflow: hidden;
  pointer-events: none;
  border-radius: inherit;
}

/* 折射渐层：左上白光 + 右上主题色光晕 */
.navbar-glass__refraction {
  position: absolute;
  inset: -40% -10% auto -10%;
  height: 220%;
  background:
    radial-gradient(58% 80% at 14% 0%, var(--lg-highlight), transparent 62%),
    radial-gradient(46% 72% at 88% 6%, color-mix(in srgb, var(--theme-primary) 46%, transparent), transparent 66%);
  opacity: 0.6;
  filter: blur(6px);
}

/* 底部亮边：让玻璃边缘出现厚度 */
.navbar-glass__edge {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  opacity: 0.75;
}

@keyframes lg-sheen {

  0%,
  62% {
    transform: rotate(14deg) translateX(0);
    opacity: 0;
  }

  70% {
    opacity: 0.7;
  }

  100% {
    transform: rotate(14deg) translateX(420%);
    opacity: 0;
  }
}

.navbar-background-container {
  position: relative;
  z-index: 1;
  width: 100%;
  padding: 0 clamp(12px, 2.4vw, 28px);
}

/* 三栏网格：品牌 / 主导航（真正居中） / 操作区 */
.navbar-home {
  --jelly-spring: cubic-bezier(0.34, 1.56, 0.64, 1);

  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  align-items: center;
  gap: clamp(12px, 2vw, 30px);
  width: 100%;
  max-width: 1480px;
  min-width: 0;
  height: 70px;
  margin: 0 auto;
  padding: 0;
  font-size: 14px;
  font-weight: 500;
}

/* ================= 主导航 ================= */

.nav-items {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  min-width: 0;
}

.nav-items .action-btn {
  position: relative;
  padding: 9px 14px;
  border-radius: 10px;
  background: transparent;
  color: var(--theme-text-2);
  font-size: 14.5px;
  font-weight: 500;
  letter-spacing: 0;
  box-shadow: none;
  overflow: visible;
  transition: color 0.24s var(--theme-ease, ease), background 0.24s var(--theme-ease, ease),
    transform 0.45s var(--jelly-spring);
}

/* 导航项保持安静：去掉全局按钮的涟漪与扫光层 */
.nav-items .action-btn::before,
.nav-items .action-btn::after {
  display: none;
}

.nav-items .action-btn:hover {
  background: var(--theme-bg-hover);
  color: var(--theme-text-1);
  transform: scale(1.04);
  box-shadow: none;
}

.nav-items .action-btn.nav-item-active {
  color: var(--theme-primary);
  background: color-mix(in srgb, var(--theme-primary) 15%, transparent);
  font-weight: 600;
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.38),
    0 8px 18px -14px var(--theme-primary);
}
.dropdown {
  display: flex;
  align-items: center;
  /* 向下扩展悬停热区，覆盖按钮与下拉面板之间的间隙，避免鼠标经过时菜单收起 */
  padding-bottom: 12px;
  margin-bottom: -12px;
}

.dropdown-toggle {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.dropdown-caret {
  width: 14px;
  height: 14px;
  transition: transform 0.25s var(--theme-ease, ease);
}

.dropdown:hover .dropdown-caret,
.dropdown:focus-within .dropdown-caret {
  transform: rotate(180deg);
}

/* ================= 「更多」下拉 ================= */

.navbar-home .dropdown-menu_Home_2 {
  /* 与扩展后的悬停热区无缝衔接，鼠标可连续移动到选项上 */
  top: 100%;
  left: auto;
  right: 0;
  display: block;
  min-width: 196px;
  padding: 6px;
  border-radius: 16px;
  background: color-mix(in srgb, var(--theme-bg-elevated) 84%, transparent);
  -webkit-backdrop-filter: blur(24px) saturate(180%);
  backdrop-filter: blur(24px) saturate(180%);
  box-shadow: var(--theme-shadow-lg), inset 0 1px 0 var(--lg-highlight);
  opacity: 0;
  visibility: hidden;
  pointer-events: none;
  transform: translateY(8px);
  transition: opacity 0.22s var(--theme-ease, ease), transform 0.22s var(--theme-ease, ease),
    visibility 0.22s var(--theme-ease, ease);
}

.navbar-home .dropdown:hover .dropdown-menu_Home_2,
.navbar-home .dropdown:focus-within .dropdown-menu_Home_2 {
  opacity: 1;
  visibility: visible;
  pointer-events: auto;
  transform: translateY(0);
}

/* 悬浮桥接：填补按钮与面板之间的空隙，避免鼠标移动时菜单收起 */
.navbar-home .dropdown-menu_Home_2::before {
  content: '';
  position: absolute;
  top: -10px;
  left: 0;
  right: 0;
  height: 10px;
}

.navbar-home .dropdown-item_action-btn {
  padding: 10px 14px;
  border-radius: 10px;
  font-size: 14px;
  transition: background 0.2s ease, padding-left 0.2s ease, transform 0.45s var(--jelly-spring);
}

.navbar-home .dropdown-item_action-btn:not(:last-child) {
  border-bottom: none;
}

.navbar-home .dropdown-item_action-btn:hover {
  padding-left: 18px;
  background: var(--theme-bg-hover);
  transform: scale(1.02);
}

/* ================= 右侧操作区 ================= */

.actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 6px;
  min-width: 0;
}

.actions .action-btn-iocn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  padding: 5px;
  border: 1px solid transparent;
  border-radius: 10px;
  background: color-mix(in srgb, var(--theme-bg-app) 32%, transparent);
  box-shadow: inset 0 1px 0 var(--lg-highlight);
  transition: background 0.24s var(--theme-ease, ease), border-color 0.24s var(--theme-ease, ease),
    box-shadow 0.24s var(--theme-ease, ease), transform 0.45s var(--jelly-spring);
}

.actions .action-btn-iocn:hover {
  background: var(--theme-bg-hover);
  border-color: var(--lg-edge);
  box-shadow:
    inset 0 1px 0 var(--lg-highlight),
    0 8px 16px -14px rgba(15, 23, 42, 0.42);
  transform: translateY(-1px) scale(1.08);
}

.actions .action-btn-iocn-size {
  width: 22px;
  height: 22px;
}

/* 登录 / 注册：液态玻璃幽灵 + 渐变主按钮，同高胶囊 */
.actions .action-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
  height: 36px;
  padding: 0 17px;
  border: 1px solid transparent;
  border-radius: 999px;
  font-size: 14px;
  font-weight: 600;
  line-height: 1;
  letter-spacing: 0.01em;
  white-space: nowrap;
  overflow: hidden;
  transition: background 0.24s ease, color 0.24s ease, border-color 0.24s ease,
    box-shadow 0.24s ease, transform 0.45s var(--jelly-spring);
}

.actions .action-btn .btn-icon {
  width: 17px;
  height: 17px;
  flex: none;
  fill: currentColor;
  transition: color 0.24s ease, transform 0.45s var(--jelly-spring);
}

.actions .action-btn span {
  position: relative;
  z-index: 1;
  line-height: 1;
}

/* 去掉全局涟漪；注册按钮的玻璃扫光在下方单独实现 */
.actions .action-btn::after {
  display: none;
}

/* 登录：液态玻璃幽灵按钮 */
.actions .action-btn--ghost {
  background: color-mix(in srgb, var(--theme-bg-app) 45%, transparent);
  border-color: var(--lg-edge);
  color: var(--theme-text-1);
  box-shadow: inset 0 1px 0 var(--lg-highlight);
  -webkit-backdrop-filter: blur(10px) saturate(150%);
  backdrop-filter: blur(10px) saturate(150%);
}

.actions .action-btn--ghost::before {
  display: none;
}

.actions .action-btn--ghost .btn-icon {
  color: var(--theme-text-2);
}

.actions .action-btn--ghost:hover {
  background: var(--theme-bg-hover);
  border-color: var(--theme-border-strong);
  color: var(--theme-text-1);
  box-shadow:
    inset 0 1px 0 var(--lg-highlight),
    0 8px 18px -14px rgba(15, 23, 42, 0.45);
  transform: translateY(-1px) scale(1.03);
}

.actions .action-btn--ghost:hover .btn-icon {
  color: var(--theme-primary);
  transform: scale(1.08);
}

/* 注册：渐变主按钮 + 顶部高光 + 主色光晕 */
.actions .action-btn--primary {
  position: relative;
  margin-left: 2px;
  background-color: var(--theme-primary);
  background-image:
    linear-gradient(180deg, rgba(255, 255, 255, 0.28), rgba(255, 255, 255, 0) 60%),
    var(--theme-gradient);
  color: var(--theme-primary-text);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.42),
    0 10px 22px -12px color-mix(in srgb, var(--theme-primary) 76%, transparent);
}

/* 悬浮时掠过一道玻璃高光 */
.actions .action-btn--primary::before {
  content: '';
  display: block;
  position: absolute;
  inset: 0;
  z-index: 0;
  background: linear-gradient(102deg,
      transparent 38%,
      rgba(255, 255, 255, 0.45) 50%,
      transparent 62%);
  background-size: 260% 100%;
  background-position: 135% 0;
  pointer-events: none;
  transition: background-position 0.75s cubic-bezier(0.22, 1, 0.36, 1);
}

.actions .action-btn--primary:hover::before {
  background-position: -35% 0;
}

.actions .action-btn--primary > * {
  position: relative;
  z-index: 1;
}

.actions .action-btn--primary:hover {
  color: var(--theme-primary-text);
  filter: brightness(1.06);
  transform: translateY(-1px) scale(1.03);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.5),
    0 14px 28px -12px color-mix(in srgb, var(--theme-primary) 82%, transparent);
}

.actions .action-btn--primary:hover .btn-icon {
  transform: scale(1.1);
}

/* 头像：渐变玻璃描边 + 悬停光晕 */
.actions .profilepicture {
  width: 50px;
  height: 50px;
}

.actions .profilepicture_button {
  display: inline-flex;
  padding: 0px;
  border-radius: 50%;
  transition: transform 0.45s var(--jelly-spring), box-shadow 0.32s ease;
}

.actions .profilepicture_button:hover,
.actions .profilepicture_button.is-open {
  transform: scale(1.06);
  box-shadow: 0 0 0 0 var(--theme-bg-app),0 0 0 3px var(--theme-primary);
}

.actions .profilepicture_img {
  width: 46px;
  height: 46px;
  border-radius: 50%;
  overflow: hidden;
  background: var(--theme-bg-app);
}

.actions .profilepicture_img img {
  display: block;
  width: 100%;
  height: 100%;
  border: none;
  border-radius: 50%;
  object-fit: cover;
}

/* 头像放大与描边由按钮统一控制，避免内外两层效果叠加 */
.actions .profilepicture_img img:hover {
  transform: none;
  box-shadow: none;
}

/* 公告铃铛：与头像同一视觉重量的玻璃圆钮 */
.actions :deep(.bell-btn) {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  /* 柔光高光代替 1px 内描边，避免小圆钮出现「双圈」 */
  background:
    radial-gradient(120% 100% at 30% 0%,
      color-mix(in srgb, var(--lg-highlight) 55%, transparent),
      transparent 62%),
    color-mix(in srgb, var(--theme-bg-app) 62%, transparent);
  border: 1px solid var(--lg-edge);
  box-shadow: 0 4px 12px -8px rgba(15, 23, 42, 0.45);
  transition: background 0.24s ease, border-color 0.24s ease, box-shadow 0.24s ease,
    color 0.24s ease, transform 0.45s var(--jelly-spring);
}

/* 与头像一致：悬停/按下只做单层柔和变化，不再叠亮底与重投影 */
.actions :deep(.bell-btn:hover) {
  background:
    radial-gradient(120% 100% at 30% 0%,
      color-mix(in srgb, var(--lg-highlight) 70%, transparent),
      transparent 62%),
    color-mix(in srgb, var(--theme-bg-app) 72%, transparent);
  border-color: var(--theme-border-strong);
  box-shadow: 0 6px 18px -8px color-mix(in srgb, var(--theme-primary) 50%, transparent);
  transform: translateY(-1px) scale(1.05);
}

.actions :deep(.bell-btn:active) {
  background:
    radial-gradient(120% 100% at 30% 0%,
      color-mix(in srgb, var(--lg-highlight) 70%, transparent),
      transparent 62%),
    color-mix(in srgb, var(--theme-bg-app) 78%, transparent);
  transform: scale(0.92);
}

/* 点击/聚焦不再出现蓝色描边 */
.actions :deep(.bell-btn:focus),
.actions :deep(.bell-btn:focus-visible) {
  outline: none;
}

/* 未读角标：断点更小、加玻璃托底描边 */
.actions :deep(.bell-badge .el-badge__content) {
  height: 17px;
  min-width: 17px;
  padding: 0 5px;
  border: 2px solid var(--theme-bg-app);
  border-radius: 999px;
  background-color: var(--theme-danger);
  color: #ffffff;
  font-size: 10px;
  font-weight: 700;
  line-height: 13px;
  box-shadow: 0 2px 6px -2px rgba(15, 23, 42, 0.45);
}

/* 语言切换 / 公告铃铛同样使用果冻反馈 */
.actions :deep(.ls-trigger),
.actions :deep(.bell-btn) {
  transition: transform 0.45s var(--jelly-spring), background 0.24s ease, color 0.24s ease,
    border-color 0.24s ease, box-shadow 0.24s ease;
}

.actions :deep(.ls-trigger:hover),
.actions :deep(.bell-btn:hover) {
  transform: translateY(-1px) scale(1.05);
}

.actions .profilepicture-inner {
  top: calc(100% + 10px);
  width: 176px;
  padding: 6px;
  border-radius: 16px;
  background: color-mix(in srgb, var(--theme-bg-elevated) 84%, transparent);
  -webkit-backdrop-filter: blur(24px) saturate(180%);
  backdrop-filter: blur(24px) saturate(180%);
  box-shadow: var(--theme-shadow-lg), inset 0 1px 0 var(--lg-highlight);
}

.actions .profilepicture-inner button {
  padding: 10px 14px;
  border-radius: 10px;
  background: transparent;
  font-size: 13.5px;
}

.actions .profilepicture-inner button:not(:last-child) {
  border-bottom: none;
}

.actions .profilepicture-inner button:hover {
  padding-left: 18px;
  background: var(--theme-bg-hover);
}

/* 菜单项点击不额外变色，反馈统一交给果冻动画 */
.actions .profilepicture-inner button:active {
  background: var(--theme-bg-hover);
}

/* 交互反馈：去掉点击产生的变色聚焦环与触摸高亮 */
.navbar-home button,
.mobile-tab,
.mobile-sheet-tile,
.mobile-sheet-close {
  -webkit-tap-highlight-color: transparent;
}

.navbar-home button:focus,
.navbar-home button:focus-visible,
.mobile-tab:focus,
.mobile-tab:focus-visible,
.mobile-sheet-tile:focus,
.mobile-sheet-tile:focus-visible,
.mobile-sheet-close:focus,
.mobile-sheet-close:focus-visible {
  outline: none;
}

/* 仅键盘导航时保留焦点环，鼠标点击不再出现 */
html.kb-nav .navbar-home button:focus-visible,
html.kb-nav .mobile-tab:focus-visible,
html.kb-nav .mobile-sheet-tile:focus-visible,
html.kb-nav .mobile-sheet-close:focus-visible {
  outline: 2px solid var(--theme-primary);
  outline-offset: 2px;
}


/* ================= 响应式适配 ================= */

/* 移动端底部导航条与「更多」浮层：默认隐藏，≤992px 显示 */
.mobile-tabbar,
.mobile-sheet-mask,
.mobile-sheet {
  display: none;
}

/* ---- 大屏（≤1400px）：压缩间距 ---- */
@media (max-width: 1400px) {
  .navbar-home {
    gap: 12px;
    padding: 0;
  }

  .nav-items,
  .center-items {
    gap: 2px;
  }

  .actions {
    gap: 4px;
  }
}

/* ---- 中屏（≤1300px）：解除固定最小宽度，流式布局 ---- */
@media (max-width: 1300px) {
  .navbar-home {
    min-width: 0;
    width: 100%;
  }

  /* 次要社交登录先让位，保证主导航与主要操作不挤压 */
  .actions > .action-btn-iocn {
    display: none;
  }
}

/* ---- 窄桌面（≤1180px）：收起品牌文字，保留 Logo ---- */
@media (max-width: 1180px) {
  .brand-text {
    display: none;
  }
}

/* ---- 小桌面（≤1080px）：进一步收紧导航项间距 ---- */
@media (max-width: 1080px) {
  .navbar-home {
    gap: 10px;
  }

  .nav-items .action-btn {
    padding: 9px 9px;
    font-size: 14px;
  }
}

/* ---- 平板/手机（≤992px）：底部悬浮导航替代水平导航 ---- */
@media (max-width: 992px) {
  /* 隐藏水平主导航与「更多」下拉 */
  .nav-items {
    display: none;
  }

  /* logo 与操作区两端分布 */
  .navbar-home {
    justify-content: space-between;
  }

  /* 底部悬浮导航条：毛玻璃 + 主题令牌，居中悬浮于安全区之上 */
  .mobile-tabbar {
    --jelly-spring: cubic-bezier(0.34, 1.56, 0.64, 1);

    position: fixed;
    left: 50%;
    bottom: calc(14px + env(safe-area-inset-bottom));
    transform: translateX(-50%);
    z-index: 110;
    display: flex;
    align-items: stretch;
    gap: 2px;
    width: min(560px, calc(100vw - 24px));
    padding: 6px;
    border: 1px solid color-mix(in srgb, var(--theme-text-1) 9%, transparent);
    border-radius: 22px;
    background: color-mix(in srgb, var(--theme-bg-app) 58%, transparent);
    -webkit-backdrop-filter: blur(26px) saturate(190%);
    backdrop-filter: blur(26px) saturate(190%);
    box-shadow:
      var(--theme-shadow-lg),
      inset 0 1px 0 rgba(255, 255, 255, 0.45);
    animation: mobile-tabbar-in 0.35s cubic-bezier(0.25, 0.8, 0.25, 1) both;
  }

  /* 暗色 / 游戏主题下收敛高光 */
  html[data-theme="dark"] .mobile-tabbar,
  html[data-theme="game"] .mobile-tabbar {
    box-shadow:
      var(--theme-shadow-lg),
      inset 0 1px 0 rgba(255, 255, 255, 0.12);
  }

  @keyframes mobile-tabbar-in {
    from {
      opacity: 0;
      transform: translate(-50%, 16px);
    }

    to {
      opacity: 1;
      transform: translate(-50%, 0);
    }
  }

  .mobile-tab {
    position: relative;
    flex: 1 1 0;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 2px;
    min-width: 0;
    padding: 6px 2px 5px;
    border: none;
    border-radius: 16px;
    background: transparent;
    color: var(--theme-text-3);
    cursor: pointer;
    -webkit-tap-highlight-color: transparent;
    transition: color 0.2s ease, transform 0.45s var(--jelly-spring);
  }

  .mobile-tab-icon {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 38px;
    height: 26px;
    border-radius: 999px;
    transition: background 0.25s ease, color 0.25s ease, transform 0.2s ease;
  }

  .mobile-tab-icon svg {
    width: 20px;
    height: 20px;
  }

  .mobile-tab-label {
    max-width: 100%;
    font-size: 11px;
    font-weight: 500;
    line-height: 1.2;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .mobile-tab.is-active {
    color: var(--theme-primary);
    font-weight: 600;
  }

  .mobile-tab.is-active .mobile-tab-icon {
    background: var(--theme-primary-soft);
    color: var(--theme-primary);
    transform: translateY(-1px);
  }

  /* 当前标签顶部渐变指示条 */
  .mobile-tab.is-active::before {
    content: '';
    position: absolute;
    top: -6px;
    left: 50%;
    transform: translateX(-50%);
    width: 16px;
    height: 3px;
    border-radius: 0 0 3px 3px;
    background: var(--theme-gradient);
  }

  /* 「更多」浮层遮罩 */
  .mobile-sheet-mask {
    display: block;
    position: fixed;
    inset: 0;
    z-index: 130;
    background: var(--theme-overlay);
    -webkit-backdrop-filter: blur(2px);
    backdrop-filter: blur(2px);
  }

  /* 「更多」浮层：底部上滑面板，宫格展示其余入口 */
  .mobile-sheet {
    --jelly-spring: cubic-bezier(0.34, 1.56, 0.64, 1);

    display: block;
    position: fixed;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: 140;
    max-height: 76vh;
    padding: 6px 16px calc(20px + env(safe-area-inset-bottom));
    border-top: 1px solid var(--theme-border);
    border-radius: 24px 24px 0 0;
    background: color-mix(in srgb, var(--theme-bg-elevated) 88%, transparent);
    -webkit-backdrop-filter: blur(28px) saturate(180%);
    backdrop-filter: blur(28px) saturate(180%);
    box-shadow:
      var(--theme-shadow-lg),
      inset 0 1px 0 rgba(255, 255, 255, 0.32);
    overflow-y: auto;
    overscroll-behavior: contain;
  }

  .mobile-sheet-handle {
    display: block;
    width: 38px;
    height: 4px;
    margin: 8px auto 10px;
    border-radius: 999px;
    background: var(--theme-hairline);
  }

  .mobile-sheet-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    padding: 0 2px 12px;
  }

  .mobile-sheet-title {
    font-size: 15px;
    font-weight: 700;
    color: var(--theme-text-1);
  }

  .mobile-sheet-close {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 32px;
    height: 32px;
    border: 1px solid var(--theme-border);
    border-radius: 10px;
    background: var(--theme-bg-muted);
    color: var(--theme-text-2);
    cursor: pointer;
    transition: background 0.2s ease, color 0.2s ease, transform 0.45s var(--jelly-spring);
  }

  .mobile-sheet-close svg {
    width: 16px;
    height: 16px;
  }

  .mobile-sheet-close:hover {
    background: var(--theme-bg-hover);
    color: var(--theme-text-1);
  }

  /* 自适应宫格：窄屏自动降为 3 列，避免文字挤压 */
  .mobile-sheet-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(76px, 1fr));
    gap: 8px;
  }

  .mobile-sheet-tile {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 6px;
    padding: 12px 4px 10px;
    border: 1px solid transparent;
    border-radius: 16px;
    background: var(--theme-bg-surface-2);
    color: var(--theme-text-2);
    font-size: 12px;
    font-weight: 500;
    cursor: pointer;
    -webkit-tap-highlight-color: transparent;
    transition: background 0.2s ease, color 0.2s ease, border-color 0.2s ease,
      transform 0.45s var(--jelly-spring);
  }

  .mobile-sheet-tile:hover,
  .mobile-sheet-tile.nav-item-active {
    border-color: var(--theme-primary);
    background: var(--theme-primary-soft);
    color: var(--theme-primary);
  }

  .mobile-sheet-iconbox {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 36px;
    height: 36px;
    border-radius: 12px;
    background: var(--theme-bg-muted);
    color: var(--theme-primary);
    transition: background 0.2s ease, color 0.2s ease;
  }

  .mobile-sheet-iconbox svg {
    width: 19px;
    height: 19px;
  }

  .mobile-sheet-tile:hover .mobile-sheet-iconbox,
  .mobile-sheet-tile.nav-item-active .mobile-sheet-iconbox {
    background: var(--theme-primary);
    color: var(--theme-primary-text);
  }

  .mobile-sheet-label {
    width: 100%;
    text-align: center;
    line-height: 1.25;
  }

  /* 为底部悬浮导航预留空间，避免遮挡页面内容 */
  main {
    padding-bottom: calc(92px + env(safe-area-inset-bottom));
  }
}

/* 浮层动效：遮罩淡入 + 面板上滑 */
.mobile-sheet-fade-enter-active,
.mobile-sheet-fade-leave-active {
  transition: opacity 0.25s ease;
}

.mobile-sheet-fade-enter-from,
.mobile-sheet-fade-leave-to {
  opacity: 0;
}

.mobile-sheet-slide-enter-active {
  transition: transform 0.32s cubic-bezier(0.25, 0.8, 0.25, 1);
}

.mobile-sheet-slide-leave-active {
  transition: transform 0.24s ease-in;
}

.mobile-sheet-slide-enter-from,
.mobile-sheet-slide-leave-to {
  transform: translateY(100%);
}

/* ---- 手机（≤768px）：压缩顶部栏，操作区控件保留图标形态，避免右侧溢出 ---- */
@media (max-width: 768px) {
  /* 微信 / QQ / 微博 图标按钮 */
  .actions > .action-btn-iocn {
    display: none;
  }

  /* 顶部栏整体收紧 */
  .navbar-home {
    height: 58px;
    padding: 6px 12px;
    gap: 10px;
  }

  .logo {
    gap: 8px;
  }

  .logo img {
    width: 36px;
    height: 36px;
    border-radius: 10px;
  }

  .actions {
    flex: 0 1 auto;
    min-width: 0;
    gap: 6px;
  }

  /* 语言切换：只留地球图标的圆形按钮 */
  .actions :deep(.ls-trigger) {
    justify-content: center;
    width: 34px;
    height: 34px;
    padding: 0;
    gap: 0;
  }

  .actions :deep(.ls-label),
  .actions :deep(.ls-arrow) {
    display: none;
  }

  /* 主题切换：缩小分段与配色按钮，亮/暗/游戏三态仍可直接切换 */
  .actions :deep(.theme-switch),
  .actions :deep(.theme-switch__palette) {
    --ts-item: 24px;
    --ts-pad: 2px;
    --ts-icon: 14px;
  }

  /* 头像与公告铃铛同步缩小，保持同一视觉重量 */
  .profilepicture {
    width: 36px;
    height: 36px;
  }

  .profilepicture_img {
    width: 32px;
    height: 32px;
  }

  /* 头像菜单右对齐，避免贴右侧时弹层溢出视口 */
  .profilepicture-inner {
    top: 48px;
    left: auto;
    right: 0;
    transform: none;
  }
}

/* ---- 小屏手机（≤576px）：隐藏品牌文字，登录/注册只留图标 ---- */
@media (max-width: 576px) {
  .logo span {
    display: none;
  }

  .logo img {
    width: 34px;
    height: 34px;
  }

  /* 未登录时登录/注册按钮压缩为图标按钮 */
  .actions .action-btn span {
    display: none;
  }

  .actions .action-btn {
    padding: 8px;
  }
}

/* ============ 果冻点击反馈：先收缩，再弹性放大回弹 ============ */
@keyframes jelly-pop {
  0% {
    transform: scale(1, 1);
  }

  26% {
    transform: scale(0.88, 0.94);
  }

  46% {
    transform: scale(1.07, 1.02);
  }

  64% {
    transform: scale(0.96, 0.99);
  }

  82% {
    transform: scale(1.02, 1);
  }

  100% {
    transform: scale(1, 1);
  }
}

/* :deep 让语言切换、主题切换、公告铃铛等子组件内部按钮同样生效 */
:deep(.is-jelly) {
  animation: jelly-pop 0.5s cubic-bezier(0.22, 1, 0.36, 1) both;
}

/* ============ 悬浮放大（仅支持悬停的设备） ============ */
@media (hover: hover) and (pointer: fine) {
  .mobile-tab:hover {
    transform: translateY(-1px) scale(1.06);
  }

  .mobile-sheet-tile:hover {
    transform: scale(1.04);
  }

  .mobile-sheet-close:hover {
    transform: scale(1.08);
  }
}

/* ============ 无障碍：尊重系统动效偏好 ============ */
@media (prefers-reduced-motion: reduce) {

  .navbar-glass__sheen,
  .mobile-tabbar {
    animation: none;
  }

  .navbar-glass__sheen {
    opacity: 0;
  }

  :deep(.is-jelly) {
    animation: none;
  }

  .nav-items .action-btn.nav-item-active::after {
    animation: none;
  }

  .nav-items .action-btn:hover,
  .actions .action-btn-iocn:hover,
  .actions .action-btn--ghost:hover,
  .actions .action-btn--primary:hover,
  .actions .profilepicture_button:hover,
  .navbar-home .dropdown-item_action-btn:hover,
  .mobile-tab:hover,
  .mobile-sheet-tile:hover,
  .mobile-sheet-close:hover,
  .actions :deep(.ls-trigger:hover),
  .actions :deep(.bell-btn:hover) {
    transform: none;
  }
}
</style>