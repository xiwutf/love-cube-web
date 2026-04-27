<template>
  <div class="app-root">
    <router-view v-slot="{ Component }">
      <Suspense>
        <component :is="Component" />
        <template #fallback>
          <div class="route-fallback" aria-hidden="true"></div>
        </template>
      </Suspense>
    </router-view>
  </div>
</template>

<style scoped>
.app-root {
  min-height: 100vh;
  background: #f4f6fb;
}

.route-fallback {
  min-height: 100vh;
  background: linear-gradient(180deg, #f4f6fb, #eef2f8);
}
</style>
