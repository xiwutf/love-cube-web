<template>
  <section class="featured-section">
    <header class="section-head">
      <div>
        <h2>精选内容</h2>
        <p>发现优质内容，丰富你的每一天</p>
      </div>
      <router-link to="/articles">查看更多 →</router-link>
    </header>

    <div class="featured-grid">
      <router-link
        v-for="item in items"
        :key="item.id"
        :to="item.to"
        class="content-card"
        :class="`tone-${item.tone}`"
      >
        <div class="content-cover" :style="{ backgroundImage: `linear-gradient(180deg, rgba(15, 23, 42, 0.02), rgba(15, 23, 42, 0.1)), url(${item.cover})` }">
          <span>{{ item.category }}</span>
        </div>
        <div class="content-body">
          <h3>{{ item.title }}</h3>
          <p>{{ item.summary }}</p>
          <small>{{ item.meta }}</small>
        </div>
      </router-link>
    </div>
  </section>
</template>

<script setup>
defineProps({
  items: {
    type: Array,
    required: true
  }
})
</script>

<style scoped>
.featured-section {
  margin-top: var(--lc-space-5);
  padding: var(--lc-space-6);
  border: 1px solid var(--lc-border);
  border-radius: var(--lc-radius);
  background: var(--lc-surface);
  box-shadow: var(--lc-shadow-sm);
}

.section-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: var(--lc-space-4);
  margin-bottom: var(--lc-space-5);
}

.section-head h2 {
  margin: 0;
  color: var(--lc-text);
  font-size: 22px;
}

.section-head p {
  margin: 6px 0 0;
  color: var(--lc-muted);
}

.section-head a {
  color: var(--lc-blue);
  font-size: var(--lc-text-sm);
  font-weight: 900;
  text-decoration: none;
}

.featured-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: var(--lc-space-5);
}

.content-card {
  overflow: hidden;
  border: 1px solid var(--lc-border);
  border-radius: var(--lc-radius-sm);
  color: inherit;
  text-decoration: none;
  background: var(--lc-surface);
  box-shadow: 0 10px 28px rgba(15, 23, 42, 0.045);
  transition: var(--lc-transition);
}

.content-card:hover {
  border-color: var(--tone-border);
  box-shadow: var(--lc-shadow);
  transform: translateY(-3px);
}

.content-cover {
  display: flex;
  align-items: flex-start;
  height: 154px;
  padding: var(--lc-space-4);
  background-color: var(--tone-soft);
  background-position: center;
  background-size: cover;
}

.content-cover span {
  padding: 6px 10px;
  border-radius: 999px;
  color: var(--lc-surface);
  background: linear-gradient(135deg, var(--tone), var(--tone-end));
  font-size: var(--lc-text-sm);
  font-weight: 950;
}

.content-body {
  padding: var(--lc-space-4);
}

.content-body h3 {
  margin: 0;
  color: var(--lc-text);
  font-size: 17px;
  line-height: 1.45;
}

.content-body p {
  display: -webkit-box;
  min-height: 48px;
  margin: var(--lc-space-2) 0 0;
  overflow: hidden;
  color: var(--lc-muted);
  line-height: 1.65;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.content-body small {
  display: block;
  margin-top: var(--lc-space-3);
  color: var(--lc-subtle);
  line-height: 1.5;
}

.tone-blue { --tone: var(--lc-blue); --tone-end: #60a5fa; --tone-soft: rgba(239, 246, 255, 0.92); --tone-border: var(--lc-blue-border); }
.tone-green { --tone: var(--lc-green); --tone-end: #34d399; --tone-soft: rgba(220, 252, 231, 0.92); --tone-border: #bbf7d0; }
.tone-violet { --tone: #7c3aed; --tone-end: #a78bfa; --tone-soft: rgba(245, 243, 255, 0.92); --tone-border: #ddd6fe; }
.tone-pink { --tone: var(--lc-pink); --tone-end: #fb7185; --tone-soft: rgba(252, 231, 243, 0.92); --tone-border: var(--lc-pink-border); }

@media (max-width: 1160px) {
  .featured-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 620px) {
  .featured-section {
    padding: 16px 12px;
    border-radius: 18px;
  }

  .section-head {
    align-items: center;
    margin-bottom: 12px;
  }

  .featured-grid {
    display: grid;
    grid-auto-flow: column;
    grid-auto-columns: minmax(160px, 180px);
    gap: 10px;
    overflow-x: auto;
    overflow-y: hidden;
    padding-bottom: 2px;
    scroll-snap-type: x proximity;
  }

  .content-card {
    scroll-snap-align: start;
  }

  .content-cover {
    height: 102px;
    background-size: cover;
  }

  .content-body {
    padding: 10px;
  }

  .content-body h3 {
    display: -webkit-box;
    overflow: hidden;
    font-size: 14px;
    line-height: 1.45;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
  }

  .content-body p {
    display: none;
  }

  .content-body small {
    margin-top: 6px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    font-size: 11px;
  }
}
</style>
