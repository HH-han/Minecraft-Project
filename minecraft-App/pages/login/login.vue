<template>
  <view class="auth-page">
    <view class="auth-shell">
      <!-- 品牌区（PC） -->
      <view class="brand-panel">
        <view class="brand-pattern brand-pattern--1"></view>
        <view class="brand-pattern brand-pattern--2"></view>
        <view class="brand-inner">
          <view class="brand-logo">
            <uni-icons type="map-pin-ellipse" size="24" color="#FFFDF8" />
          </view>
          <text class="brand-name">Minecraft</text>
          <text class="brand-tagline">{{ t('auth.brandTagline') }}</text>

          <view class="brand-points">
            <view v-for="key in brandPoints" :key="key" class="brand-point">
              <view class="point-dot">
                <uni-icons type="checkmarkempty" size="11" color="#1E5B48" />
              </view>
              <text class="point-text">{{ t(key) }}</text>
            </view>
          </view>
        </view>
        <text class="brand-copyright">© 2026 Minecraft Travel</text>
      </view>

      <!-- 表单区 -->
      <view class="auth-card">
        <view class="mobile-brand">
          <view class="mobile-logo">
            <uni-icons type="map-pin-ellipse" size="20" color="#1E5B48" />
          </view>
          <view class="mobile-brand__text">
            <text class="mobile-name">Minecraft</text>
            <text class="mobile-tagline">{{ t('auth.brandTagline') }}</text>
          </view>
        </view>

        <view class="card-head">
          <text class="card-title">{{ t('auth.loginTitle') }}</text>
          <text class="card-sub">{{ t('auth.loginSubtitle') }}</text>
        </view>

        <view class="field">
          <text class="field-label">{{ t('auth.accountLabel') }}</text>
          <view class="input-wrap" :class="{ 'is-error': !!errors.account }">
            <uni-icons class="field-icon" type="person" size="17" color="currentColor" />
            <input v-model="formData.account" class="field-input"
              :placeholder="t('auth.accountPlaceholder')" confirm-type="next" />
          </view>
          <text v-if="errors.account" class="error-text">{{ errors.account }}</text>
        </view>

        <view class="field">
          <text class="field-label">{{ t('auth.passwordLabel') }}</text>
          <view class="input-wrap" :class="{ 'is-error': !!errors.password }">
            <uni-icons class="field-icon" type="locked" size="17" color="currentColor" />
            <input v-model="formData.password" class="field-input" :type="showPassword ? 'text' : 'password'"
              :placeholder="t('auth.passwordPlaceholder')" confirm-type="done" @confirm="submitForm" />
            <view class="eye-btn" :aria-label="showPassword ? t('auth.hidePassword') : t('auth.showPassword')"
              @click="showPassword = !showPassword">
              <uni-icons :type="showPassword ? 'eye-filled' : 'eye-slash'" size="17" color="currentColor" />
            </view>
          </view>
          <text v-if="errors.password" class="error-text">{{ errors.password }}</text>
        </view>

        <view class="form-row">
          <view class="remember" @click="remember = !remember">
            <view class="checkbox" :class="{ 'is-checked': remember }">
              <uni-icons v-if="remember" type="checkmarkempty" size="12" color="#FFFFFF" />
            </view>
            <text class="remember-text">{{ t('auth.remember') }}</text>
          </view>
          <text class="text-link" @click="forgotPassword">{{ t('auth.forgot') }}</text>
        </view>

        <button class="submit-btn" :loading="loading" :disabled="loading" @click="submitForm">
          {{ t('auth.login') }}
        </button>

        <view class="switch-row">
          <text class="switch-text">{{ t('auth.noAccount') }}</text>
          <text class="text-link" @click="goRegister">{{ t('auth.registerNow') }}</text>
        </view>

        <text class="legal">{{ t('auth.legal') }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { login } from '../../api/auth';
import { t, usePageTitle } from '@/utils/i18n';
import UniIcons from '@/uni_modules/uni-icons/components/uni-icons/uni-icons.vue';

usePageTitle('page.login');

const brandPoints = ['auth.brandPoint1', 'auth.brandPoint2', 'auth.brandPoint3'];

const loading = ref(false);
const remember = ref(false);
const showPassword = ref(false);
let redirectUrl = '';

onLoad((options) => {
  if (options?.redirect) {
    try {
      redirectUrl = decodeURIComponent(options.redirect);
    } catch (error) {
      redirectUrl = '';
    }
  }
});

const formData = reactive({
  account: '',
  password: ''
});

const errors = reactive({
  account: '',
  password: ''
});

const validateForm = () => {
  let isValid = true;

  // 重置错误信息
  errors.account = '';
  errors.password = '';

  // 验证账号
  if (!formData.account.trim()) {
    errors.account = t('auth.accountRequired');
    isValid = false;
  }

  // 验证密码
  if (!formData.password.trim()) {
    errors.password = t('auth.passwordRequired');
    isValid = false;
  }

  return isValid;
};

const submitForm = async () => {
  if (validateForm()) {
    loading.value = true;
    try {
      const response = await login(formData);
      if (response.code === 200) {
        // 存储登录信息
        uni.setStorageSync('token', response.data.token);
        uni.setStorageSync('userInfo', response.data);

        uni.showToast({
          title: t('auth.loginSuccess'),
          icon: 'success'
        });

        if (redirectUrl) {
          uni.redirectTo({
            url: redirectUrl,
            fail: () => {
              uni.switchTab({ url: '/pages/index/index' });
            }
          });
        } else {
          uni.switchTab({
            url: '/pages/index/index'
          });
        }
      } else {
        uni.showToast({
          title: response.message || t('auth.loginFailed'),
          icon: 'none'
        });
      }
    } catch (error) {
      uni.showToast({
        title: t('auth.networkError'),
        icon: 'none'
      });
    } finally {
      loading.value = false;
    }
  }
};

const forgotPassword = () => {
  uni.showToast({
    title: t('auth.forgotNotReady'),
    icon: 'none'
  });
};

const goRegister = () => {
  uni.navigateTo({
    url: '/pages/login/register'
  });
};
</script>

<style scoped>
/* ============ 页面底壳 ============ */
.auth-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  padding: 28px 16px calc(28px + env(safe-area-inset-bottom));
  background-color: var(--bg-color);
  background-image:
    radial-gradient(900px 520px at 12% -6%, rgba(30, 91, 72, 0.12), transparent 62%),
    radial-gradient(760px 520px at 104% 104%, rgba(196, 97, 60, 0.1), transparent 58%);
  background-repeat: no-repeat;
}

.auth-shell {
  width: 100%;
  max-width: 460px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border-radius: 24px;
  background: var(--card-background);
  border: 1px solid var(--card-border);
  box-shadow: var(--shadow-lg);
  animation: authFloatUp 0.55s var(--ease-out) both;
}

@keyframes authFloatUp {
  from {
    opacity: 0;
    transform: translateY(18px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* ============ 品牌区（PC 显示） ============ */
.brand-panel {
  position: relative;
  display: none;
  overflow: hidden;
  padding: 44px 40px 36px;
  background: linear-gradient(158deg, #235F4B 0%, #1A4C3D 52%, #113528 100%);
  color: #FFFDF8;
}

.brand-pattern {
  position: absolute;
  border-radius: 50%;
  border: 1px solid rgba(255, 253, 248, 0.14);
  pointer-events: none;
}

.brand-pattern--1 {
  width: 300px;
  height: 300px;
  right: -120px;
  top: -110px;
  background: radial-gradient(circle, rgba(255, 253, 248, 0.14), transparent 66%);
  border: none;
}

.brand-pattern--2 {
  width: 220px;
  height: 220px;
  left: -90px;
  bottom: -80px;
}

.brand-inner {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.brand-logo {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 15px;
  background: rgba(255, 253, 248, 0.14);
  border: 1px solid rgba(255, 253, 248, 0.24);
  -webkit-backdrop-filter: blur(10px);
  backdrop-filter: blur(10px);
}

.brand-name {
  margin-top: 22px;
  font-size: 30px;
  font-weight: 800;
  letter-spacing: 0.4px;
  line-height: 1.1;
}

.brand-tagline {
  margin-top: 10px;
  font-size: 13.5px;
  line-height: 1.6;
  color: rgba(255, 253, 248, 0.72);
}

.brand-points {
  display: flex;
  flex-direction: column;
  gap: 14px;
  margin-top: auto;
  padding-top: 36px;
}

.brand-point {
  display: flex;
  align-items: center;
  gap: 10px;
}

.point-dot {
  flex: 0 0 auto;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: rgba(255, 253, 248, 0.92);
}

.point-text {
  font-size: 13px;
  color: rgba(255, 253, 248, 0.86);
}

.brand-copyright {
  position: relative;
  z-index: 1;
  margin-top: 28px;
  font-size: 10.5px;
  letter-spacing: 0.4px;
  color: rgba(255, 253, 248, 0.42);
}

/* ============ 表单区 ============ */
.auth-card {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 30px 22px 26px;
  background: var(--card-background);
}

.mobile-brand {
  display: flex;
  align-items: center;
  gap: 11px;
  margin-bottom: 22px;
}

.mobile-logo {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 13px;
  background: var(--primary-soft);
}

.mobile-brand__text {
  display: flex;
  flex-direction: column;
}

.mobile-name {
  font-size: 17px;
  font-weight: 800;
  letter-spacing: 0.2px;
  color: var(--text-color);
}

.mobile-tagline {
  margin-top: 2px;
  font-size: 11px;
  color: var(--text-tertiary-color);
}

.card-head {
  display: flex;
  flex-direction: column;
}

.card-title {
  font-size: 25px;
  font-weight: 800;
  letter-spacing: -0.4px;
  color: var(--text-color);
}

.card-sub {
  margin-top: 6px;
  font-size: 13px;
  color: var(--text-secondary-color);
}

/* ============ 表单控件 ============ */
.field {
  display: flex;
  flex-direction: column;
  margin-top: 18px;
}

.field-label {
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.2px;
  color: var(--text-secondary-color);
}

.input-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
  height: 50px;
  margin-top: 8px;
  padding: 0 14px;
  border-radius: 14px;
  background: var(--surface-2);
  border: 1px solid var(--border-color);
  transition: border-color 0.25s var(--ease-out), box-shadow 0.25s var(--ease-out),
    background-color 0.25s var(--ease-out);
}

.input-wrap:focus-within {
  background: var(--card-background);
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px var(--primary-soft);
}

.input-wrap.is-error {
  border-color: #C0392B;
  box-shadow: 0 0 0 3px rgba(192, 57, 43, 0.1);
}

.field-icon {
  flex: 0 0 auto;
  display: flex;
  align-items: center;
  color: var(--text-tertiary-color);
  transition: color 0.25s var(--ease-out);
}

.input-wrap:focus-within .field-icon {
  color: var(--primary-color);
}

.field-input {
  flex: 1;
  min-width: 0;
  height: 100%;
  border: none;
  outline: none;
  background: transparent;
  font-size: 14.5px;
  color: var(--text-color);
}

.field-input::placeholder {
  color: var(--text-tertiary-color);
}

.field-input :deep(.uni-input-input) {
  font-size: 14.5px;
  color: var(--text-color);
  background: transparent;
}

.field-input :deep(.uni-input-placeholder) {
  color: var(--text-tertiary-color);
}

.eye-btn {
  flex: 0 0 auto;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: -6px;
  border-radius: 50%;
  color: var(--text-tertiary-color);
  transition: color 0.2s var(--ease-out), background-color 0.2s var(--ease-out);
}

.eye-btn:hover {
  color: var(--primary-color);
  background: var(--primary-soft);
}

.error-text {
  margin-top: 6px;
  font-size: 11px;
  line-height: 1.4;
  color: #C0392B;
}

/* ============ 记住我 / 链接 ============ */
.form-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 16px;
}

.remember {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.checkbox {
  width: 18px;
  height: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 5px;
  border: 1.5px solid var(--border-strong);
  background: var(--card-background);
  transition: all 0.2s var(--ease-out);
}

.checkbox.is-checked {
  background: var(--primary-color);
  border-color: var(--primary-color);
  box-shadow: 0 4px 10px rgba(30, 91, 72, 0.24);
}

.remember-text {
  font-size: 12.5px;
  color: var(--text-secondary-color);
}

.text-link {
  font-size: 12.5px;
  font-weight: 600;
  color: var(--primary-color);
  transition: opacity 0.2s var(--ease-out);
}

.text-link:hover {
  opacity: 0.75;
}

/* ============ 提交按钮 ============ */
.submit-btn {
  width: 100%;
  height: 52px;
  line-height: 52px;
  margin-top: 22px;
  border: none;
  border-radius: 14px;
  background: linear-gradient(135deg, #235F4B 0%, #1E5B48 55%, #2F7A55 100%);
  color: #FFFDF8;
  font-size: 15px;
  font-weight: 700;
  letter-spacing: 0.6px;
  box-shadow: var(--shadow-brand);
  transition: transform 0.25s var(--ease-out), box-shadow 0.25s var(--ease-out), opacity 0.25s var(--ease-out);
}

.submit-btn::after {
  border: none;
}

.submit-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 14px 28px rgba(30, 91, 72, 0.3);
}

.submit-btn:active {
  transform: scale(0.99);
}

.submit-btn[disabled] {
  opacity: 0.7;
}

/* ============ 底部切换 / 条款 ============ */
.switch-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  margin-top: 20px;
}

.switch-text {
  font-size: 13px;
  color: var(--text-secondary-color);
}

.legal {
  margin-top: 22px;
  font-size: 10.5px;
  line-height: 1.65;
  text-align: center;
  color: var(--text-tertiary-color);
}

/* ============ PC：左右分栏 ============ */
@media (min-width: 900px) {
  .auth-page {
    justify-content: center;
    padding: 40px;
  }

  .auth-shell {
    max-width: 1040px;
    min-height: 620px;
    display: grid;
    grid-template-columns: 1.02fr 0.98fr;
    border-radius: 28px;
  }

  .brand-panel {
    display: flex;
    flex-direction: column;
    justify-content: space-between;
  }

  .auth-card {
    padding: 52px 54px;
  }

  .mobile-brand {
    display: none;
  }

  .card-title {
    font-size: 28px;
  }
}
</style>
