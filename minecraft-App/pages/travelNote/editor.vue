<template>
  <view class="note-editor">
    <text class="page-title">{{ isEdit ? t('noteEditor.editTitle') : t('noteEditor.createTitle') }}</text>

    <view class="field">
      <text class="label">{{ t('noteEditor.title') }}</text>
      <input v-model="form.title" class="input" maxlength="120" :placeholder="t('noteEditor.titlePlaceholder')" />
    </view>

    <view class="field">
      <text class="label">{{ t('noteEditor.summary') }}</text>
      <textarea v-model="form.summary" class="textarea" maxlength="300" :placeholder="t('noteEditor.summaryPlaceholder')" />
    </view>

    <view class="field-row">
      <view class="field field--half">
        <text class="label">{{ t('noteEditor.destination') }}</text>
        <input v-model="form.destination" class="input" maxlength="100" :placeholder="t('noteEditor.destinationPlaceholder')" />
      </view>
      <view class="field field--half">
        <text class="label">{{ t('noteEditor.date') }}</text>
        <picker mode="date" :value="form.travelDate" @change="onDateChange">
          <view class="input picker-value">{{ form.travelDate || t('noteEditor.selectDate') }}</view>
        </picker>
      </view>
    </view>

    <view class="field-row">
      <view class="field field--half">
        <text class="label">{{ t('noteEditor.days') }}</text>
        <input v-model.number="form.travelDays" class="input" type="number" placeholder="1" />
      </view>
      <view class="field field--half">
        <text class="label">{{ t('noteEditor.cost') }}</text>
        <input v-model.number="form.cost" class="input" type="digit" placeholder="0" />
      </view>
    </view>

    <view class="field">
      <text class="label">{{ t('noteEditor.images') }}</text>
      <view class="image-list">
        <view v-for="(img, idx) in images" :key="idx" class="image-item">
          <image class="image-thumb" :src="img" mode="aspectFill" />
          <view class="image-remove" @click="removeImage(idx)">×</view>
        </view>
        <view v-if="images.length < 18" class="image-add" @click="chooseImages">
          <text class="image-add-icon">+</text>
        </view>
      </view>
    </view>

    <view class="field">
      <text class="label">{{ t('noteEditor.content') }}</text>
      <textarea
        v-model="content"
        class="textarea textarea--large"
        maxlength="5000"
        :placeholder="t('noteEditor.contentPlaceholder')"
      />
    </view>

    <view class="field">
      <text class="label">{{ t('noteEditor.tags') }}</text>
      <input v-model="tagText" class="input" :placeholder="t('noteEditor.tagsPlaceholder')" />
    </view>

    <view class="submit-bar">
      <button class="btn btn--ghost" :disabled="saving" @click="save(0)">{{ t('noteEditor.saveDraft') }}</button>
      <button class="btn btn--ghost" :disabled="saving" @click="save(5)">{{ t('noteEditor.setPrivate') }}</button>
      <button class="btn btn--primary" :disabled="saving" @click="save(1)">{{ t('noteEditor.submit') }}</button>
    </view>
  </view>
</template>

<script>
import { createNote, getNoteDetail, updateNote, uploadNoteImage } from '@/api/travelNote'
import { t } from '@/utils/i18n'

export default {
  data() {
    return {
      editId: null,
      saving: false,
      content: '',
      tagText: '',
      images: [],
      form: {
        title: '',
        summary: '',
        destination: '',
        travelDate: '',
        travelDays: 1,
        cost: null
      }
    }
  },
  computed: {
    isEdit() {
      return !!this.editId
    }
  },
  onLoad(options) {
    if (!uni.getStorageSync('token')) {
      uni.showToast({ title: t('common.loginRequired'), icon: 'none' })
      setTimeout(() => uni.navigateBack(), 800)
      return
    }
    if (options.id) {
      this.editId = Number(options.id)
      this.loadForEdit()
    }
  },
  onShow() {
    uni.setNavigationBarTitle({ title: t('page.noteEditor') })
  },
  methods: {
    t,
    async loadForEdit() {
      const res = await getNoteDetail(this.editId)
      if (res.code !== 200 || !res.data) {
        uni.showToast({ title: res.message || t('noteEditor.notFound'), icon: 'none' })
        setTimeout(() => uni.navigateBack(), 800)
        return
      }
      const d = res.data
      this.form.title = d.title || ''
      this.form.summary = d.summary || ''
      this.form.destination = d.destination || ''
      this.form.travelDate = d.travelDate || ''
      this.form.travelDays = d.travelDays || 1
      this.form.cost = d.cost ? Number(d.cost) : null
      this.content = (d.content || '').replace(/<br\s*\/?>/gi, '\n').replace(/<\/?p>/gi, '')
      this.tagText = (d.tags || []).join(',')
      this.images = (d.mediaList || []).filter((m) => m.mediaType === 1).map((m) => m.url)
    },
    onDateChange(e) {
      this.form.travelDate = e.detail.value
    },
    chooseImages() {
      uni.chooseImage({
        count: 9,
        success: async (res) => {
          for (const path of res.tempFilePaths) {
            try {
              const up = await uploadNoteImage(path)
              if (up.code === 200 && up.data) {
                this.images.push(up.data)
              } else {
                uni.showToast({ title: up.message || t('noteEditor.uploadFailed'), icon: 'none' })
              }
            } catch (e) {
              console.error('图片上传失败', e)
            }
          }
        }
      })
    },
    removeImage(idx) {
      this.images.splice(idx, 1)
    },
    buildPayload(status) {
      const tags = this.tagText
        .split(/[,，\s]+/)
        .map((t) => t.trim().replace(/^#/, ''))
        .filter(Boolean)
        .slice(0, 10)
      return {
        title: this.form.title.trim(),
        summary: this.form.summary,
        destination: this.form.destination,
        travelDate: this.form.travelDate || null,
        travelDays: Number(this.form.travelDays) || 1,
        cost: this.form.cost ? Number(this.form.cost) : null,
        coverUrl: this.images[0] || null,
        contentType: 1,
        content: this.content ? this.content.replace(/\n/g, '<br/>') : '',
        status,
        tags,
        mediaList: this.images.map((url, idx) => ({ mediaType: 1, url, sort: idx }))
      }
    },
    async save(status) {
      if (!this.form.title.trim()) {
        uni.showToast({ title: t('noteEditor.titleRequired'), icon: 'none' })
        return
      }
      if (status === 1 && !this.content.trim()) {
        uni.showToast({ title: t('noteEditor.contentRequired'), icon: 'none' })
        return
      }
      this.saving = true
      uni.showLoading({ title: t('noteEditor.submitting') })
      try {
        const payload = this.buildPayload(status)
        const res = this.isEdit ? await updateNote(this.editId, payload) : await createNote(payload)
        uni.hideLoading()
        if (res.code === 200) {
          uni.showToast({
            title: status === 0 ? t('noteEditor.draftSaved') : status === 5 ? t('noteEditor.privateSet') : t('noteEditor.submitted'),
            icon: 'none'
          })
          setTimeout(() => uni.redirectTo({ url: '/pages/travelNote/mine' }), 900)
        } else {
          uni.showToast({ title: res.message || t('noteEditor.saveFailed'), icon: 'none' })
        }
      } catch (e) {
        uni.hideLoading()
        uni.showToast({ title: t('noteEditor.saveFailed'), icon: 'none' })
      } finally {
        this.saving = false
      }
    }
  }
}
</script>

<style scoped>
.note-editor {
  min-height: 100vh;
  background-color: var(--background-color, #F7F4EE);
  padding: 24rpx 24rpx 200rpx;
}

.page-title {
  display: block;
  font-size: 36rpx;
  font-weight: 700;
  color: var(--text-color, #1f2329);
  margin-bottom: 24rpx;
}

.field {
  margin-bottom: 24rpx;
}

.field-row {
  display: flex;
  gap: 20rpx;
}

.field--half {
  flex: 1;
}

.label {
  display: block;
  font-size: 26rpx;
  color: var(--text-secondary-color, #7d7e80);
  margin-bottom: 12rpx;
}

.input {
  width: 100%;
  height: 84rpx;
  padding: 0 24rpx;
  background-color: var(--card-background);
  border-radius: 12rpx;
  font-size: 28rpx;
  box-sizing: border-box;
}

.picker-value {
  display: flex;
  align-items: center;
}

.textarea {
  width: 100%;
  min-height: 140rpx;
  padding: 20rpx 24rpx;
  background-color: var(--card-background);
  border-radius: 12rpx;
  font-size: 28rpx;
  box-sizing: border-box;
}

.textarea--large {
  min-height: 360rpx;
}

.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.image-item {
  position: relative;
  width: 180rpx;
  height: 180rpx;
}

.image-thumb {
  width: 100%;
  height: 100%;
  border-radius: 12rpx;
  background-color: #eef2f8;
}

.image-remove {
  position: absolute;
  top: -12rpx;
  right: -12rpx;
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
  background-color: rgba(0, 0, 0, 0.6);
  color: #fff;
  font-size: 28rpx;
  text-align: center;
  line-height: 40rpx;
}

.image-add {
  width: 180rpx;
  height: 180rpx;
  border-radius: 12rpx;
  background-color: var(--card-background);
  border: 2rpx dashed #cfd6e4;
  display: flex;
  align-items: center;
  justify-content: center;
}

.image-add-icon {
  font-size: 56rpx;
  color: #a0a4ab;
}

.submit-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  gap: 16rpx;
  padding: 20rpx 24rpx 40rpx;
  background-color: var(--card-background);
  box-shadow: 0 -4rpx 16rpx rgba(0, 0, 0, 0.06);
}

.btn {
  flex: 1;
  height: 84rpx;
  line-height: 84rpx;
  border-radius: 42rpx;
  font-size: 28rpx;
  border: none;
}

.btn--ghost {
  background-color: #f0f4fa;
  color: var(--text-color, #1f2329);
}

.btn--primary {
  background-color: var(--primary-color, #3a80d2);
  color: #fff;
}
</style>
