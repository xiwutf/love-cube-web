<template>
  <div class="ccd-overlay" @click.self="$emit('continue')">
    <div class="ccd-card">
      <div class="ccd-header">
        <span class="ccd-icon">!</span>
        <h3 class="ccd-title">建议使用更通用的表达方式</h3>
      </div>

      <p class="ccd-desc">
        检测到以下词汇可能不适合平台使用：
        <span v-for="w in hitWords" :key="w" class="ccd-hit-word">{{ w }}</span>
      </p>

      <div v-if="suggestion" class="ccd-suggestion">
        <p class="ccd-suggestion-label">建议文案</p>
        <p class="ccd-suggestion-text">{{ suggestion }}</p>
      </div>

      <div class="ccd-actions">
        <button class="ccd-btn ccd-btn-primary" @click="$emit('use-suggestion')">
          使用建议
        </button>
        <button class="ccd-btn ccd-btn-ghost" @click="$emit('continue')">
          继续发布
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  suggestion: { type: String, default: '' },
  hitWords:   { type: Array,  default: () => [] }
})
defineEmits(['use-suggestion', 'continue'])
</script>

<style scoped>
.ccd-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: var(--lc-z-modal, 1000);
}

.ccd-card {
  background: #fff;
  border-radius: 12px;
  padding: 28px 32px;
  width: 440px;
  max-width: calc(100vw - 32px);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.16);
}

.ccd-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
}

.ccd-icon {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #f59e0b;
  color: #fff;
  font-weight: 700;
  font-size: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.ccd-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
}

.ccd-desc {
  font-size: 14px;
  color: #555;
  line-height: 1.6;
  margin-bottom: 14px;
}

.ccd-hit-word {
  display: inline-block;
  margin: 0 4px;
  padding: 1px 7px;
  background: #fff7ed;
  color: #c2410c;
  border: 1px solid #fed7aa;
  border-radius: 4px;
  font-size: 13px;
}

.ccd-suggestion {
  background: #f8fafb;
  border-left: 3px solid var(--lc-blue, #4f86f7);
  padding: 12px 14px;
  border-radius: 0 6px 6px 0;
  margin-bottom: 20px;
}

.ccd-suggestion-label {
  font-size: 12px;
  color: #888;
  margin: 0 0 6px;
}

.ccd-suggestion-text {
  font-size: 14px;
  color: #333;
  margin: 0;
  line-height: 1.6;
  white-space: pre-wrap;
}

.ccd-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.ccd-btn {
  padding: 8px 22px;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  border: none;
  transition: opacity 0.15s;
}
.ccd-btn:hover { opacity: 0.85; }

.ccd-btn-primary {
  background: var(--lc-blue, #4f86f7);
  color: #fff;
}

.ccd-btn-ghost {
  background: #f3f4f6;
  color: #555;
}
</style>
