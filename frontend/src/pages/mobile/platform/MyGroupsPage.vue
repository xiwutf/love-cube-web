<template>
  <div class="my-groups-m">
    <header class="head">
      <router-link to="/m/platform" class="back" aria-label="返回玩法中心">‹</router-link>
      <h1>我的团体</h1>
    </header>
    <MyGroupsPage class="body" />
  </div>
</template>

<script setup>
import MyGroupsPage from '@/pages/platform/MyGroupsPage.vue'
</script>

<style scoped>
.my-groups-m {
  min-height: 100vh;
  background: var(--lc-bg, #f4f5fb);
}

.head {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 14px 0;
  background: #fff;
  border-bottom: 1px solid var(--lc-soft, #e8ecf4);
}

.back {
  display: grid;
  place-items: center;
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: #fff;
  border: 1px solid var(--lc-soft, #e8ecf4);
  text-decoration: none;
  color: var(--lc-indigo, #4f46e5);
  font-size: 22px;
}

.head h1 {
  margin: 0 0 12px;
  font-size: 18px;
  font-weight: 800;
}

.body :deep(.page-head h1) {
  display: none;
}

.body :deep(.page-head) {
  padding-top: 8px;
}
</style>
