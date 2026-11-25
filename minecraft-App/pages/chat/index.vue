<template>
  <div class="app-page app-page--plain chat-page">
    <div class="shell">
      <header class="simple-head">
        <div class="page-eyebrow">{{ t('chat.eyebrow') }}</div>
        <h1 class="page-title">{{ t('chat.title') }}</h1>
        <p class="page-sub">{{ t('chat.sub') }}</p>
      </header>

      <div class="chat-messages">
        <div v-for="item in messages" :key="item.id" class="message-item" :class="item.role">
          <div v-if="item.role === 'bot'" class="message-avatar">
            <img :src="botAvatar" alt="客服头像">
          </div>
          <div class="message-content">
            <div class="bubble">
              <p v-for="(key, index) in item.lineKeys" :key="'k' + index">{{ t(key) }}</p>
              <p v-for="(line, index) in item.lines" :key="'l' + index">{{ line }}</p>
            </div>
            <span class="message-time">{{ item.time }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 输入栏 -->
    <div class="chat-input-bar">
      <div class="chat-input">
        <input v-model="message" type="text" :placeholder="t('chat.placeholder')" confirm-type="send" @confirm="sendMessage" />
        <button class="send-btn" @click="sendMessage">
          <uni-icons type="paperplane-filled" size="17" color="#FFFFFF" />
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import UniIcons from '@/uni_modules/uni-icons/components/uni-icons/uni-icons.vue'
import { t, usePageTitle } from '@/utils/i18n'

usePageTitle('page.chat')

const botAvatar = 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=customer%20service%20robot%20avatar&image_size=square'

const currentTime = () => {
  const now = new Date()
  return `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`
}

const messages = ref([
  {
    id: 1,
    role: 'bot',
    lineKeys: ['chat.welcome'],
    time: '10:00'
  },
  {
    id: 2,
    role: 'user',
    lineKeys: ['chat.exampleUser'],
    time: '10:01'
  },
  {
    id: 3,
    role: 'bot',
    lineKeys: ['chat.replyIntro', 'chat.step1', 'chat.step2', 'chat.step3', 'chat.step4'],
    time: '10:02'
  }
])

const message = ref('')

const scrollToBottom = () => {
  setTimeout(() => {
    uni.pageScrollTo({ scrollTop: 99999, duration: 200 })
  }, 60)
}

const sendMessage = () => {
  const text = message.value.trim()
  if (!text) return

  messages.value.push({
    id: Date.now(),
    role: 'user',
    lines: [text],
    time: currentTime()
  })
  message.value = ''
  scrollToBottom()

  setTimeout(() => {
    messages.value.push({
      id: Date.now() + 1,
      role: 'bot',
      lineKeys: ['chat.autoReply'],
      time: currentTime()
    })
    scrollToBottom()
  }, 800)
}
</script>

<style scoped>
.chat-page {
  padding-bottom: calc(96px + env(safe-area-inset-bottom));
}

.simple-head {
  padding: 6px 2px 0;
  margin-bottom: 16px;
}

.page-eyebrow {
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 1.6px;
  color: var(--primary-color);
}

.page-title {
  margin-top: 7px;
  font-size: 26px;
  font-weight: 800;
  letter-spacing: -0.5px;
  color: var(--text-color);
}

.page-sub {
  margin-top: 5px;
  font-size: 13px;
  color: var(--text-secondary-color);
}

.chat-messages {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message-item {
  display: flex;
  align-items: flex-start;
  gap: 9px;
}

.message-item.user {
  flex-direction: row-reverse;
}

.message-avatar {
  flex: 0 0 36px;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  overflow: hidden;
  background: var(--surface-2);
  box-shadow: 0 0 0 1px var(--border-color);
}

.message-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.message-content {
  max-width: 76%;
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.message-item.user .message-content {
  align-items: flex-end;
}

.bubble {
  padding: 11px 14px;
  border-radius: 16px 16px 16px 4px;
  background: var(--card-background);
  border: 1px solid var(--card-border);
  box-shadow: var(--shadow-sm);
  font-size: 13px;
  line-height: 1.65;
  color: var(--text-color);
}

.bubble p + p {
  margin-top: 4px;
}

.message-item.user .bubble {
  border-radius: 16px 16px 4px 16px;
  background: var(--primary-color);
  border-color: transparent;
  color: #fff;
  box-shadow: var(--shadow-brand);
}

.message-time {
  font-size: 10px;
  color: var(--text-tertiary-color);
}

.chat-input-bar {
  position: fixed;
  left: 50%;
  bottom: 0;
  z-index: 80;
  transform: translateX(-50%);
  width: 100%;
  max-width: 1200px;
  padding: 10px 16px calc(10px + env(safe-area-inset-bottom));
  background: var(--glass-background);
  -webkit-backdrop-filter: blur(20px);
  backdrop-filter: blur(20px);
  border-top: 1px solid var(--card-border);
}

.chat-input {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 6px 6px 16px;
  border-radius: var(--radius-pill);
  background: var(--card-background);
  border: 1px solid var(--border-color);
}

.chat-input input {
  flex: 1;
  min-width: 0;
  height: 38px;
  border: none;
  outline: none;
  font-size: 14px;
  color: var(--text-color);
}

.chat-input input::placeholder {
  color: var(--text-tertiary-color);
}

.send-btn {
  flex: 0 0 auto;
  width: 38px;
  height: 38px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: var(--primary-color);
  box-shadow: var(--shadow-brand);
  transition: transform 0.2s var(--ease-spring);
}

.send-btn:active {
  transform: scale(0.92);
}

@media (min-width: 768px) {
  .page-title {
    font-size: 32px;
  }

  .message-content {
    max-width: 60%;
  }

  .chat-input-bar {
    width: min(100%, 1200px);
    border-radius: 20px 20px 0 0;
    padding: 12px 28px calc(12px + env(safe-area-inset-bottom));
  }
}
</style>
