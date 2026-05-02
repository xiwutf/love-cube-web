<template>
  <section class="card profile-card" aria-label="账号资料">
    <div class="user">
      <div class="avatar-wrap">
        <img v-if="user?.avatar" :src="user.avatar" class="avatar" alt="头像" />
        <div v-else class="avatar avatar-fallback" aria-hidden="true">{{ avatarFallback }}</div>
      </div>
      <div class="meta">
        <div class="name-row">
          <h2 class="name">{{ displayName }}</h2>
          <span class="tag">平台创作者</span>
        </div>
        <div class="sub">
          <span>ID：{{ userIdDisplay }}</span>
          <span class="dot" aria-hidden="true"></span>
          <span>注册地：{{ locationDisplay }}</span>
        </div>
        <div class="invite">
          <span>邀请码：<b>{{ inviteCodeDisplay }}</b></span>
          <button type="button" class="chip-btn" :disabled="!inviteCodeDisplay" @click="$emit('copy-invite')">复制</button>
          <em v-if="copyFeedback" class="feedback" :class="{ 'is-error': copyFeedbackError }">{{ copyFeedback }}</em>
        </div>
      </div>
    </div>

    <div class="stats" aria-label="统计数据">
      <div v-for="item in profileLightStats" :key="item.label" class="stat">
        <small>{{ item.label }}</small>
        <strong>{{ item.value }}</strong>
      </div>
    </div>

    <div class="actions">
      <button type="button" class="btn btn-ghost" @click="$emit('edit')">编辑资料</button>
      <button type="button" class="btn btn-primary" @click="$emit('open-fellowship')">去看看</button>
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue'

defineEmits(['edit', 'copy-invite', 'open-fellowship'])

const props = defineProps({
  user: { type: Object, default: null },
  displayName: { type: String, required: true },
  userIdDisplay: { type: [String, Number], required: true },
  locationDisplay: { type: String, required: true },
  inviteCodeDisplay: { type: String, required: true },
  copyFeedback: { type: String, default: '' },
  copyFeedbackError: { type: Boolean, default: false },
  profileLightStats: { type: Array, required: true },
})

const avatarFallback = computed(() => String(props.displayName || 'L').trim().slice(0, 1).toUpperCase())
</script>

<style scoped>
.profile-card {
  background: var(--lc-surface);
  border: 1px solid var(--lc-soft-alt);
  border-radius: 12px;
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.055);
  overflow: hidden;
  min-height: 272px;
  box-sizing: border-box;
  padding: 30px 36px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  gap: 18px;
}

.user {
  display: flex;
  gap: 20px;
  align-items: center;
}

.avatar-wrap {
  width: 92px;
  height: 92px;
  flex: 0 0 auto;
}

.avatar {
  width: 92px;
  height: 92px;
  border-radius: 50%;
  object-fit: cover;
  display: block;
  box-shadow: 0 12px 24px rgba(79, 70, 229, 0.16);
}

.avatar-fallback {
  display: grid;
  place-items: center;
  color: var(--lc-surface);
  background: linear-gradient(135deg, var(--lc-indigo), var(--lc-violet));
  font-size: 28px;
  font-weight: 800;
}

.name-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
}

.name {
  margin: 0;
  font-size: 25px;
  font-weight: 800;
  color: var(--lc-text);
  line-height: 1.18;
}

.tag {
  padding: 3px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
  color: var(--lc-indigo);
  background: rgba(79, 70, 229, 0.10);
  flex: 0 0 auto;
}

.sub {
  margin-top: 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  color: var(--lc-muted-light);
  font-size: 12px;
}

.dot {
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: var(--lc-border);
  flex: 0 0 auto;
}

.invite {
  margin-top: 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  color: var(--lc-muted-light);
  font-size: 12px;
}

.invite b {
  color: var(--lc-text);
  font-weight: 800;
}

.chip-btn {
  border: 1px solid #e5e7eb;
  background: var(--lc-surface);
  color: var(--lc-text);
  border-radius: 8px;
  padding: 5px 10px;
  font-size: 12px;
  font-weight: 800;
  cursor: pointer;
  flex: 0 0 auto;
}

.chip-primary {
  border-color: rgba(79, 70, 229, 0.22);
  color: var(--lc-indigo);
  background: rgba(79, 70, 229, 0.08);
}

.feedback {
  font-style: normal;
  font-size: 12px;
  color: var(--lc-emerald);
}

.feedback.is-error {
  color: var(--lc-red);
}

.stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  border: 1px solid var(--lc-soft-alt);
  border-radius: 12px;
  overflow: hidden;
}

.stat {
  padding: 14px 12px;
  display: grid;
  gap: 4px;
  text-align: center;
}

.stat + .stat {
  border-left: 1px solid rgba(229, 231, 235, 0.9);
}

.stat strong {
  font-size: 19px;
  font-weight: 900;
  color: var(--lc-text);
  line-height: 1;
}

.stat small {
  color: var(--lc-muted-light);
  font-size: 12px;
}

.actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.btn {
  min-height: 46px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 800;
  cursor: pointer;
}

.btn-ghost {
  border: 1px solid var(--lc-soft-alt);
  background: var(--lc-surface);
  color: var(--lc-text);
}

.btn-primary {
  border: 0;
  background: linear-gradient(135deg, var(--lc-indigo), var(--lc-violet));
  color: var(--lc-surface);
}
</style>
