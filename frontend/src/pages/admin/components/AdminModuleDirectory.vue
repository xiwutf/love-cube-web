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
              <span class="dir-card-icon" aria-hidden="true">{{ item.icon }}</span>
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
}

.dir-head {
  margin-bottom: var(--lc-space-5);
}

.dir-title {
  margin: 0;
  font-size: clamp(1.2rem, 1.4vw, 1.45rem);
  font-weight: 800;
  color: var(--lc-text);
  letter-spacing: 0.02em;
  line-height: 1.25;
}

.dir-lead {
  margin: var(--lc-space-2) 0 0;
  font-size: 14px;
  line-height: 1.55;
  color: var(--lc-muted);
  max-width: 52rem;
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
}

.dir-group-title {
  margin: 0;
  font-size: 15px;
  font-weight: 800;
  color: var(--lc-text-deep);
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
}

.dir-card {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 6px;
  padding: 14px 14px 16px;
  border-radius: var(--lc-radius-sm);
  border: 1px solid var(--lc-border);
  background: var(--lc-surface);
  text-decoration: none;
  color: var(--lc-text);
  box-shadow: var(--lc-shadow-sm);
  transition: border-color 0.18s ease, box-shadow 0.18s ease, background 0.18s ease;
  min-height: 108px;
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

.dir-card-icon {
  font-size: 14px;
  color: var(--lc-subtle);
  line-height: 1;
}

.dir-card.is-active .dir-card-icon {
  color: var(--lc-blue);
}

.dir-card-name {
  font-size: 14px;
  font-weight: 700;
  line-height: 1.3;
}

.dir-card-blurb {
  font-size: 12px;
  line-height: 1.45;
  color: var(--lc-muted);
}

@media (max-width: 600px) {
  .dir-cards {
    grid-template-columns: 1fr;
  }
}
</style>
