<template>
  <div class="cover-upload-field">
    <div v-if="modelValue" class="cover-preview-wrap">
      <img :src="modelValue" alt="封面图预览" class="cover-preview" />
      <div class="cover-actions">
        <label class="admin-btn cover-upload-btn" :class="{ disabled: disabled || uploading }">
          {{ uploading ? '上传中...' : changeLabel }}
          <input
            type="file"
            class="cover-file-input"
            accept=".jpg,.jpeg,.png,.webp,image/jpeg,image/png,image/webp"
            :disabled="disabled || uploading"
            @change="onFileChange"
          />
        </label>
        <button v-if="showRemove" type="button" class="admin-btn danger" :disabled="disabled || uploading" @click="clearImage">
          删除
        </button>
      </div>
    </div>

    <div v-else class="cover-empty">
      <label class="cover-placeholder" :class="{ disabled: disabled || uploading }">
        {{ uploading ? '上传中...' : emptyLabel }}
        <input
          type="file"
          class="cover-file-input"
          accept=".jpg,.jpeg,.png,.webp,image/jpeg,image/png,image/webp"
          :disabled="disabled || uploading"
          @change="onFileChange"
        />
      </label>
      <span class="cover-tip">{{ tip }}</span>
    </div>

    <button v-if="allowManualInput" type="button" class="cover-advanced-toggle" @click="advancedOpen = !advancedOpen">
      {{ advancedOpen ? '收起手动 URL 输入' : '高级：手动输入 URL' }}
    </button>

    <div v-if="allowManualInput && advancedOpen" class="cover-advanced">
      <input
        :value="modelValue || ''"
        class="admin-input"
        :placeholder="placeholder"
        :disabled="disabled"
        @input="onManualInput"
      />
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { showToast } from 'vant'
import { uploadImage } from '@/api/upload.js'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  placeholder: {
    type: String,
    default: '封面图 URL（选填）'
  },
  disabled: {
    type: Boolean,
    default: false
  },
  allowManualInput: {
    type: Boolean,
    default: true
  },
  showRemove: {
    type: Boolean,
    default: true
  },
  emptyLabel: {
    type: String,
    default: '上传封面图'
  },
  changeLabel: {
    type: String,
    default: '更换图片'
  },
  tip: {
    type: String,
    default: '支持 jpg/png/webp，建议小于 5MB'
  }
})

const emit = defineEmits(['update:modelValue'])

const uploading = ref(false)
const advancedOpen = ref(false)

const ALLOWED_TYPES = new Set(['image/jpeg', 'image/png', 'image/webp'])
const MAX_FILE_SIZE = 5 * 1024 * 1024

function onManualInput(event) {
  emit('update:modelValue', event.target.value)
}

function clearImage() {
  emit('update:modelValue', '')
}

function validateFile(file) {
  if (!file) return false

  const isAllowedType = ALLOWED_TYPES.has(file.type)
  if (!isAllowedType) {
    showToast({ type: 'fail', message: '仅支持 jpg、png、webp 格式' })
    return false
  }

  if (file.size > MAX_FILE_SIZE) {
    showToast({ type: 'fail', message: '图片大小不能超过 5MB' })
    return false
  }
  return true
}

function extractUrl(response) {
  if (!response) return ''
  if (typeof response === 'string') return response
  if (typeof response.url === 'string') return response.url
  if (typeof response.data === 'string') return response.data
  if (typeof response.data?.url === 'string') return response.data.url
  if (typeof response.result?.url === 'string') return response.result.url
  return ''
}

async function onFileChange(event) {
  const [file] = event.target.files || []
  event.target.value = ''

  if (!validateFile(file)) return

  uploading.value = true
  try {
    const result = await uploadImage(file)
    const url = extractUrl(result)
    if (!url) {
      throw new Error('上传成功但未返回图片地址')
    }
    emit('update:modelValue', url)
    showToast({ type: 'success', message: '封面图上传成功' })
  } catch (error) {
    showToast({ type: 'fail', message: error.message || '上传失败，请稍后重试' })
  } finally {
    uploading.value = false
  }
}
</script>

<style scoped>
.cover-upload-field {
  display: grid;
  gap: 8px;
  min-width: 220px;
}

.cover-preview-wrap {
  display: grid;
  gap: 8px;
}

.cover-preview {
  width: 100%;
  max-width: 220px;
  aspect-ratio: 16 / 9;
  border-radius: 8px;
  border: 1px solid var(--lc-border);
  object-fit: cover;
  background: var(--lc-bg);
}

.cover-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.cover-empty {
  display: grid;
  gap: 6px;
}

.cover-placeholder {
  display: grid;
  place-items: center;
  width: min(100%, 240px);
  aspect-ratio: 16 / 9;
  border: 1px dashed var(--lc-border);
  border-radius: 8px;
  color: var(--lc-muted);
  background: var(--lc-bg);
  font-weight: 900;
  cursor: pointer;
}

.cover-placeholder.disabled {
  cursor: not-allowed;
  opacity: 0.56;
  pointer-events: none;
}

.cover-tip {
  color: var(--lc-muted);
  font-size: 12px;
}

.cover-file-input {
  display: none;
}

.cover-upload-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 36px;
  cursor: pointer;
}

.cover-upload-btn.disabled {
  opacity: 0.56;
  cursor: not-allowed;
  pointer-events: none;
}

.cover-advanced-toggle {
  border: 0;
  background: transparent;
  color: var(--lc-blue);
  font-size: 12px;
  font-weight: 600;
  padding: 0;
  text-align: left;
  cursor: pointer;
}

.cover-advanced .admin-input {
  width: 100%;
}
</style>
