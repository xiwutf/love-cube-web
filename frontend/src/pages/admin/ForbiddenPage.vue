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
  color: #e2e8f0;
  line-height: 1;
  margin: 0 0 8px;
  letter-spacing: -.04em;
}

.forbidden-title {
  font-size: 22px;
  font-weight: 800;
  color: #0f172a;
  margin: 0 0 10px;
}

.forbidden-desc {
  font-size: 14px;
  color: #64748b;
  margin: 0 0 24px;
  line-height: 1.6;
}

.forbidden-back {
  display: inline-block;
  padding: 10px 22px;
  background: #eff6ff;
  border: 1px solid #bfdbfe;
  border-radius: 10px;
  color: #1d4ed8;
  font-weight: 700;
  font-size: 14px;
  text-decoration: none;
  transition: all .18s ease;
}

.forbidden-back:hover {
  background: #dbeafe;
  border-color: #93c5fd;
  color: #1e3a8a;
}
</style>
