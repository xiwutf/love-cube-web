<template>
  <div class="content-panel">
    <div class="panel-head">
      <h2>热门资讯</h2>
      <router-link to="/articles">查看更多 →</router-link>
    </div>
    <div class="featured-news">
      <router-link :to="featuredUpdate.to || '/articles'" class="featured-card">
        <img
          v-if="featuredUpdate.cover"
          :src="featuredUpdate.cover"
          :alt="featuredUpdate.title"
          loading="lazy"
          decoding="async"
          @error="$emit('media-error', $event)"
        >
        <span class="news-badge">{{ featuredUpdate.tag }}</span>
        <span class="news-source">{{ featuredUpdate.sourceLabel }}</span>
        <h3>{{ featuredUpdate.title }}</h3>
        <p>{{ featuredUpdate.summary }}</p>
        <small>{{ featuredUpdate.date }} · {{ featuredUpdate.reads }}</small>
      </router-link>
      <div class="news-list">
        <router-link
          v-for="item in sideUpdates"
          :key="item.key"
          :to="item.to || '/announcements'"
          class="news-row"
        >
          <span class="news-row-icon" :class="item.tone">{{ item.short }}</span>
          <span>
            <strong>{{ item.title }}</strong>
            <small>{{ item.sourceLabel }} · {{ item.date }} · {{ item.reads }}</small>
          </span>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  featuredUpdate: {
    type: Object,
    required: true
  },
  sideUpdates: {
    type: Array,
    default: () => []
  }
})

defineEmits(['media-error'])
</script>

<style scoped>
.content-panel {
  min-width: 0;
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.panel-head h2 {
  margin: 0;
  color: var(--lc-text);
  font-size: 22px;
}

.panel-head a {
  color: var(--lc-blue);
  font-size: 13px;
  font-weight: 800;
  text-decoration: none;
}

.featured-news {
  display: grid;
  grid-template-columns: minmax(260px, 1fr) minmax(260px, 0.95fr);
  gap: 14px;
}

.featured-card,
.news-row {
  border: 1px solid var(--lc-border);
  border-radius: 8px;
  background: var(--lc-surface);
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.045);
}

.featured-card {
  position: relative;
  overflow: hidden;
  color: inherit;
  text-decoration: none;
}

.featured-card img {
  width: 100%;
  height: 160px;
  object-fit: cover;
  background: var(--lc-blue-light);
}

.featured-card h3,
.featured-card p,
.featured-card small {
  margin-left: 18px;
  margin-right: 18px;
}

.featured-card h3 {
  margin-top: 16px;
  margin-bottom: 8px;
  color: var(--lc-text);
  font-size: 18px;
  line-height: 1.45;
}

.featured-card p {
  margin-top: 0;
  color: var(--lc-muted-light);
  font-size: 13px;
  line-height: 1.65;
}

.featured-card small {
  display: block;
  margin-bottom: 16px;
  color: var(--lc-subtle);
}

.news-badge {
  position: absolute;
  left: 14px;
  top: 12px;
  padding: 5px 10px;
  border-radius: 999px;
  color: var(--lc-surface);
  background: var(--lc-purple);
  font-size: 12px;
  font-weight: 900;
}

.news-source {
  position: absolute;
  right: 14px;
  top: 12px;
  padding: 4px 9px;
  border-radius: 999px;
  color: var(--lc-indigo);
  background: var(--lc-indigo-light);
  font-size: 11px;
  font-weight: 800;
}

.news-list {
  display: grid;
  gap: 12px;
}

.news-row {
  display: grid;
  grid-template-columns: 48px 1fr;
  gap: 13px;
  align-items: center;
  min-height: 86px;
  padding: 14px;
  color: inherit;
  text-decoration: none;
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}

.news-row:hover {
  transform: translateY(-3px);
  box-shadow: 0 18px 38px rgba(15, 23, 42, 0.1);
}

.news-row-icon {
  display: grid;
  place-items: center;
  width: 44px;
  height: 44px;
  border-radius: 12px;
  font-weight: 900;
  background: var(--tone-bg);
  color: var(--tone);
}

.news-row strong,
.news-row small {
  display: block;
}

.news-row strong {
  color: var(--lc-text);
  font-size: 15px;
  line-height: 1.45;
}

.news-row small {
  margin-top: 5px;
  color: var(--lc-subtle);
  font-size: 12px;
}

.tone-pink { --tone: var(--lc-pink); --tone-bg: var(--lc-pink-light); }
.tone-violet { --tone: var(--lc-indigo); --tone-bg: var(--lc-indigo-light); }
.tone-blue { --tone: var(--lc-blue); --tone-bg: var(--lc-blue-light); }
.tone-orange { --tone: var(--lc-orange); --tone-bg: var(--lc-orange-light); }
.tone-green { --tone: var(--lc-green); --tone-bg: var(--lc-emerald-light); }
.tone-purple { --tone: var(--lc-purple); --tone-bg: var(--lc-indigo-soft); }

@media (max-width: 760px) {
  .featured-news {
    grid-template-columns: 1fr;
  }
}
</style>
