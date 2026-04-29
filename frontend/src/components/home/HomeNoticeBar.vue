<template>
  <section class="notice-bar" aria-label="公告通知">
    <div class="notice-icon" aria-hidden="true">!</div>
    <p><strong>公告</strong><span>{{ notice.title }}</span><em>{{ notice.text }}</em></p>
    <router-link :to="notice.to || '/announcements'">查看全部公告 →</router-link>
  </section>
</template>

<script setup>
defineProps({
  notice: {
    type: Object,
    required: true
  }
})
</script>

<style scoped>
.notice-bar {
  display: flex;
  align-items: center;
  gap: var(--lc-space-3);
  min-height: 48px;
  padding: 0 var(--lc-space-5);
  border: 1px solid rgba(191, 219, 254, 0.7);
  border-radius: var(--lc-radius-sm);
  background: rgba(255, 255, 255, 0.88);
  box-shadow: var(--lc-shadow-sm);
}

.notice-icon {
  display: grid;
  place-items: center;
  width: 28px;
  height: 28px;
  flex: 0 0 auto;
  border-radius: 50%;
  color: var(--lc-surface);
  background: linear-gradient(135deg, var(--lc-blue), #8b5cf6);
  font-weight: 900;
}

.notice-bar p {
  display: flex;
  min-width: 0;
  flex: 1;
  align-items: center;
  gap: var(--lc-space-2);
  margin: 0;
  color: var(--lc-muted);
  line-height: 1.5;
}

.notice-bar strong {
  color: var(--lc-blue);
  font-size: var(--lc-text-sm);
}

.notice-bar span {
  color: var(--lc-text);
  font-weight: 800;
}

.notice-bar em {
  overflow: hidden;
  color: var(--lc-muted);
  font-style: normal;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notice-bar a {
  flex: 0 0 auto;
  color: var(--lc-blue);
  font-size: var(--lc-text-sm);
  font-weight: 900;
  text-decoration: none;
}

@media (max-width: 720px) {
  .notice-bar {
    min-height: 40px;
    margin: 8px 0 6px;
    padding: 0 10px;
    border-radius: 12px;
  }

  .notice-bar p {
    align-items: center;
    gap: 4px;
  }

  .notice-bar strong,
  .notice-bar span {
    font-size: 12px;
  }

  .notice-bar em {
    display: none;
  }

  .notice-bar span {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .notice-bar a {
    display: inline-flex;
    color: var(--lc-blue);
    font-size: 0;
  }

  .notice-bar a::after {
    content: '›';
    font-size: 18px;
    line-height: 1;
  }
}
</style>
