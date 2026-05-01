<template>
  <section class="quick-wrap platform-card">
    <header>
      <h3>快捷操作</h3>
      <p>高频动作直达，减少页面切换。</p>
    </header>

    <div class="quick-grid">
      <router-link v-for="item in actions" :key="item.label" :to="item.to" class="quick-btn">
        <span class="icon">{{ item.icon }}</span>
        <span class="text">{{ item.label }}</span>
      </router-link>
    </div>
  </section>
</template>

<script setup>
const actions = [
  { label: '发布公告', to: '/admin/announcements', icon: '📢' },
  { label: '发布文章', to: '/admin/articles', icon: '📝' },
  { label: '创建活动', to: '/admin/events', icon: '🎉' },
  { label: '用户管理', to: '/admin/users', icon: '👤' }
]
</script>

<style scoped>
.quick-wrap {
  border: 1px solid var(--lc-surface);
  border-radius: 12px;
  padding: 20px;
  box-shadow: var(--lc-shadow-sm);
}

h3 {
  margin: 0;
  font-size: 20px;
}

p {
  margin: 8px 0 0;
  color: var(--lc-subtle);
  font-size: 13px;
}

.quick-grid {
  margin-top: 20px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.quick-btn {
  border: 1px solid var(--lc-surface);
  border-radius: 12px;
  min-height: 74px;
  text-decoration: none;
  color: var(--lc-text);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  background: var(--lc-surface);
  box-shadow: var(--lc-shadow-sm);
}

.icon {
  font-size: 18px;
}

.text {
  font-size: 13px;
  font-weight: 600;
}
</style>
