<template>
  <div class="fellowship-layout fellowship-shell">
    <router-view />
    <router-link to="/fellowship-intro" class="back-platform">返回平台官网</router-link>
  </div>
</template>

<style scoped>
.fellowship-layout {
  min-height: 100%;
  position: relative;
}

.back-platform {
  position: fixed;
  right: 12px;
  top: calc(10px + env(safe-area-inset-top));
  z-index: 90;
  font-size: 12px;
  color: #ff5f84;
  text-decoration: none;
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid #ffd4df;
  border-radius: 999px;
  padding: 7px 12px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

@media (min-width: 481px) {
  .back-platform {
    top: auto;
    bottom: 74px;
  }
}

@media (min-width: 768px) {
  .back-platform {
    right: calc((100vw - 480px) / 2 + 14px);
  }
}
</style>
