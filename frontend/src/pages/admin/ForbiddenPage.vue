<template>
  <div class="forbidden-page">
    <div class="forbidden-card">
      <p class="forbidden-code">403</p>
      <h1 class="forbidden-title">没有访问权限</h1>
      <p class="forbidden-desc">你没有操作此功能所需的权限，请联系超级管理员。</p>
      <router-link to="/admin" class="forbidden-back">返回总览面板</router-link>
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped>
.forbidden-page {
  min-height: 60vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
}

.forbidden-card {
  text-align: center;
  max-width: 400px;
}

.forbidden-code {
  font-size: 80px;
  font-weight: 900;
  color: var(--lc-border);
  line-height: 1;
  margin: 0 0 8px;
  letter-spacing: -.04em;
}

.forbidden-title {
  font-size: 22px;
  font-weight: 800;
  color: var(--lc-text);
  margin: 0 0 10px;
}

.forbidden-desc {
  font-size: 14px;
  color: var(--lc-muted-light);
  margin: 0 0 24px;
  line-height: 1.6;
}

.forbidden-back {
  display: inline-block;
  padding: 10px 22px;
  background: var(--lc-blue-light);
  border: 1px solid var(--lc-blue-border);
  border-radius: 10px;
  color: var(--lc-blue);
  font-weight: 700;
  font-size: 14px;
  text-decoration: none;
  transition: all .18s ease;
}

.forbidden-back:hover {
  background: var(--lc-border);
  border-color: var(--lc-blue-border);
  color: var(--lc-blue-dark);
}
</style>
