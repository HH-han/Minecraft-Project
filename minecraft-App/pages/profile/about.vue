<template>
  <div class="about-container">
    <div class="about-header">
      <h1>{{ t('page.about') }}</h1>
    </div>

    <div class="about-content">
      <div class="about-logo">
        <img src="https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=travel%20app%20logo%20modern%20design&image_size=square" alt="logo">
      </div>

      <div class="about-info">
        <h2>{{ t('about.appName') }}</h2>
        <p class="version">{{ t('about.version') }}</p>
        <p class="description">{{ t('about.description') }}</p>
      </div>

      <div class="about-section">
        <h3>{{ t('about.features') }}</h3>
        <ul class="feature-list">
          <li>• {{ t('about.f1') }}</li>
          <li>• {{ t('about.f2') }}</li>
          <li>• {{ t('about.f3') }}</li>
          <li>• {{ t('about.f4') }}</li>
          <li>• {{ t('about.f5') }}</li>
          <li>• {{ t('about.f6') }}</li>
        </ul>
      </div>

      <div class="about-section">
        <h3>{{ t('about.contact') }}</h3>
        <p>{{ t('about.email') }}</p>
        <p>{{ t('about.phone') }}</p>
        <p>{{ t('about.address') }}</p>
      </div>

      <div class="about-section">
        <h3>{{ t('about.agreement') }}</h3>
        <p><a href="#">{{ t('about.terms') }}</a></p>
        <p><a href="#">{{ t('about.privacyPolicy') }}</a></p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { t, usePageTitle } from '@/utils/i18n'

usePageTitle('page.about')
</script>

<style scoped>
.about-container {
  min-height: 100vh;
  background-color: var(--background-color);
  padding: 20px;
}

.about-header {
  margin-bottom: 20px;
}

.about-header h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: var(--text-color);
}

.about-content {
  background-color: var(--card-background);
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.about-logo {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
}

.about-logo img {
  width: 100px;
  height: 100px;
  border-radius: 20px;
}

.about-info {
  text-align: center;
  margin-bottom: 30px;
}

.about-info h2 {
  margin: 0 0 8px 0;
  font-size: 20px;
  font-weight: 600;
  color: var(--text-color);
}

.version {
  margin: 0 0 16px 0;
  font-size: 14px;
  color: var(--text-secondary-color);
}

.description {
  margin: 0;
  font-size: 14px;
  line-height: 1.5;
  color: var(--text-color);
  text-align: left;
}

.about-section {
  margin-bottom: 24px;
  padding-top: 20px;
  border-top: 1px solid var(--border-color);
}

.about-section h3 {
  margin: 0 0 12px 0;
  font-size: 16px;
  font-weight: 500;
  color: var(--text-color);
}

.feature-list {
  margin: 0;
  padding-left: 20px;
  font-size: 14px;
  line-height: 1.6;
  color: var(--text-color);
}

.about-section p {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: var(--text-color);
}

.about-section a {
  color: var(--primary-color);
  text-decoration: none;
  transition: all 0.3s ease;
}

.about-section a:hover {
  text-decoration: underline;
}
</style>