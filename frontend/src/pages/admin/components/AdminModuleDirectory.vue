<template>
  <section class="module-directory platform-card" aria-labelledby="module-directory-title">
    <header class="dir-head">
      <div>
        <h2 id="module-directory-title" class="dir-title">功能地图</h2>
        <p v-if="directoryGroups.length" class="dir-lead">
          按业务分区，与左侧菜单一致。您没有权限的入口不会出现在这里。
        </p>
        <p v-else class="dir-lead dir-lead--muted">
          当前账号除总览外暂无其他管理模块；如需开通权限请联系超级管理员。
        </p>
      </div>
    </header>

    <div class="dir-groups">
      <section v-for="group in directoryGroups" :key="group.id" class="dir-group">
        <div class="dir-group-head">
          <h3 class="dir-group-title">{{ group.label }}</h3>
          <p class="dir-group-hint">{{ group.hint }}</p>
        </div>
        <div class="dir-cards">
          <router-link
            v-for="item in group.items"
            :key="item.to"
            v-slot="{ isActive, isExactActive, href, navigate }"
            :to="item.to"
            custom
          >
            <a
              :href="href"
              class="dir-card"
              :class="{ 'is-active': item.to === '/admin' ? isExactActive : isActive }"
              @click="navigate"
            >
              <span class="dir-card-name">{{ item.label }}</span>
              <span class="dir-card-blurb">{{ item.blurb }}</span>
            </a>
          </router-link>
        </div>
      </section>
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue'
import { useUserStore } from '@/stores/user.js'
import { ADMIN_NAV_GROUPS, filterAdminNavGroups } from '@/constants/adminNavigation.js'

const userStore = useUserStore()

const directoryGroups = computed(() => {
  const filtered = filterAdminNavGroups(ADMIN_NAV_GROUPS, userStore.hasPermission)
  return filtered.filter(g => g.id !== 'overview')
})
</script>

<style scoped>
.module-directory {
  padding: var(--lc-space-6);
  border: 1px solid var(--lc-border);
  isolation: isolate;
}

.module-directory,
.module-directory * {
  text-shadow: none !important;
  filter: none !important;
  transform: none !important;
}

.dir-head {
  margin-bottom: var(--lc-space-5);
}

.dir-title {
  margin: 0;
  font-size: clamp(1.2rem, 1.4vw, 1.45rem);
  font-weight: 700;
  color: var(--lc-text);
  letter-spacing: 0;
  line-height: 1.45;
}

.dir-lead {
  margin: var(--lc-space-2) 0 0;
  font-size: 14px;
  line-height: 1.8;
  color: var(--lc-muted);
  max-width: 52rem;
  font-weight: 400;
}

.dir-lead--muted {
  color: var(--lc-subtle);
}

.dir-groups {
  display: flex;
  flex-direction: column;
  gap: var(--lc-space-6);
}

.dir-group-head {
  margin-bottom: var(--lc-space-3);
  position: relative;
  z-index: 2;
  background: var(--lc-bg);
}

.dir-group-title {
  margin: 0;
  font-size: 15px;
  font-weight: 700;
  color: var(--lc-text-deep);
  line-height: 1.5;
}

.dir-group-hint {
  margin: 6px 0 0;
  font-size: 13px;
  line-height: 1.5;
  color: var(--lc-subtle);
  max-width: 48rem;
}

.dir-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: var(--lc-space-3);
  margin-top: 8px;
  position: relative;
  z-index: 1;
  clear: both;
}

.dir-card {
  display: block;
  padding: 14px 14px 16px;
  border-radius: var(--lc-radius-sm);
  border: 1px solid var(--lc-border);
  background: var(--lc-surface);
  text-decoration: none;
  color: var(--lc-text);
  box-shadow: var(--lc-shadow-sm);
  transition: border-color 0.18s ease, box-shadow 0.18s ease, background 0.18s ease;
  min-height: 92px;
  position: static;
}

.dir-card:hover {
  border-color: var(--lc-blue-border);
  background: var(--lc-blue-light);
  box-shadow: var(--lc-shadow-sm);
}

.dir-card.is-active {
  border-color: var(--lc-blue);
  background: var(--lc-blue-light);
  box-shadow: inset 0 0 0 1px var(--lc-blue-border);
}

.dir-card-name {
  display: block;
  width: 100%;
  font-size: 14px;
  font-weight: 700;
  line-height: 1.3;
  color: var(--lc-text);
  white-space: normal;
  overflow-wrap: anywhere;
  word-break: break-word;
}

.dir-card-blurb {
  display: block;
  width: 100%;
  margin-top: 6px;
  font-size: 12px;
  line-height: 1.45;
  color: var(--lc-muted);
  white-space: normal;
  overflow-wrap: anywhere;
  word-break: break-word;
}

@media (max-width: 600px) {
  .dir-cards {
    grid-template-columns: 1fr;
  }
}
</style>
