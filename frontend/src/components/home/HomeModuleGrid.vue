<template>
  <section class="portal-section module-section">
    <header class="section-head">
      <div>
        <h2>核心模块</h2>
        <p>第一次来可先看：活动中心、用户中心，也可以寻找自己的团队</p>
      </div>
    </header>
    <div class="module-tags" aria-label="模块清单">
      <span v-for="item in modules" :key="`tag-${item.title}`">{{ item.title }}</span>
    </div>

    <div class="module-grid">
      <router-link
        v-for="item in modules"
        :key="item.title"
        :to="item.to"
        class="module-card"
        :class="`tone-${item.tone}`"
      >
        <span class="module-icon">{{ item.icon }}</span>
        <div>
          <strong>{{ item.title }}</strong>
          <p>{{ item.desc }}</p>
        </div>
        <em aria-hidden="true">→</em>
      </router-link>
    </div>
  </section>
</template>

<script setup>
defineProps({
  modules: {
    type: Array,
    required: true
  }
})
</script>

<style scoped>
.portal-section {
  margin-top: var(--lc-space-5);
  padding: var(--lc-space-6);
  border: 1px solid var(--lc-border);
  border-radius: var(--lc-radius);
  background: var(--lc-surface);
  box-shadow: var(--lc-shadow-sm);
}

.section-head {
  display: flex;
  justify-content: space-between;
  gap: var(--lc-space-4);
  margin-bottom: var(--lc-space-5);
}

.section-head h2 {
  margin: 0;
  color: var(--lc-text);
  font-size: 22px;
  line-height: 1.25;
}

.section-head p {
  margin: 6px 0 0;
  color: var(--lc-muted);
  line-height: 1.6;
}

.module-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: var(--lc-space-4);
}

.module-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin: -6px 0 var(--lc-space-4);
}

.module-tags span {
  padding: 4px 10px;
  border: 1px solid var(--lc-blue-border);
  border-radius: 999px;
  color: var(--lc-blue);
  background: var(--lc-blue-light);
  font-size: 12px;
  font-weight: 700;
}

.module-card {
  display: grid;
  grid-template-columns: 70px minmax(0, 1fr) 36px;
  gap: var(--lc-space-4);
  align-items: center;
  min-height: 112px;
  padding: 22px;
  border: 1px solid var(--lc-border);
  border-radius: var(--lc-radius-sm);
  color: inherit;
  text-decoration: none;
  background: var(--lc-surface);
  transition: var(--lc-transition);
}

.module-card:hover {
  border-color: var(--tone-border);
  box-shadow: var(--lc-shadow);
  transform: translateY(-3px);
}

.module-icon {
  display: grid;
  place-items: center;
  width: 62px;
  height: 62px;
  border-radius: 18px;
  color: var(--lc-surface);
  background: linear-gradient(135deg, var(--tone), var(--tone-end));
  font-size: 25px;
  font-weight: 950;
}

.module-card strong {
  color: var(--lc-text);
  font-size: 18px;
  line-height: 1.35;
}

.module-card p {
  white-space: pre-line;
  margin: 6px 0 0;
  color: var(--lc-muted);
  line-height: 1.65;
}

.module-card em {
  display: grid;
  place-items: center;
  width: 34px;
  height: 34px;
  border-radius: 50%;
  color: var(--tone);
  background: var(--tone-soft);
  font-style: normal;
  font-weight: 950;
}

.tone-blue { --tone: var(--lc-blue); --tone-end: #60a5fa; --tone-soft: var(--lc-blue-light); --tone-border: var(--lc-blue-border); }
.tone-pink { --tone: var(--lc-pink); --tone-end: #fb7185; --tone-soft: var(--lc-pink-light); --tone-border: var(--lc-pink-border); }
.tone-orange { --tone: #f97316; --tone-end: #fbbf24; --tone-soft: #fff7ed; --tone-border: #fed7aa; }
.tone-violet { --tone: #7c3aed; --tone-end: #a78bfa; --tone-soft: #f5f3ff; --tone-border: #ddd6fe; }
.tone-green { --tone: var(--lc-green); --tone-end: #34d399; --tone-soft: var(--lc-green-light); --tone-border: #bbf7d0; }

@media (max-width: 1080px) {
  .module-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .portal-section {
    padding: 16px;
    border-radius: 18px;
  }

  .module-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 10px;
  }

  .module-tags {
    gap: 6px;
    margin-bottom: 12px;
  }

  .module-card {
    grid-template-columns: 1fr;
    align-items: start;
    gap: 8px;
    min-height: 122px;
    padding: 14px 12px;
  }

  .module-icon {
    width: 40px;
    height: 40px;
    border-radius: 12px;
    font-size: 18px;
  }

  .module-card strong {
    display: block;
    font-size: 18px;
    line-height: 1.25;
  }

  .module-card p {
    margin-top: 2px;
    font-size: 12px;
    line-height: 1.45;
  }

  .module-card em {
    justify-self: end;
    width: 24px;
    height: 24px;
    font-size: 12px;
  }
}

@media (max-width: 390px) {
  .section-head h2 {
    font-size: 20px;
  }

  .section-head p {
    font-size: 12px;
  }

  .module-card {
    min-height: 116px;
    padding: 12px 10px;
  }
}
</style>
