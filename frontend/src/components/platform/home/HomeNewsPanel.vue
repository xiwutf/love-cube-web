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
  color: #0f172a;
  font-size: 22px;
}

.panel-head a {
  color: #2563eb;
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
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: #fff;
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
  background: #eff6ff;
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
  color: #0f172a;
  font-size: 18px;
  line-height: 1.45;
}

.featured-card p {
  margin-top: 0;
  color: #64748b;
  font-size: 13px;
  line-height: 1.65;
}

.featured-card small {
  display: block;
  margin-bottom: 16px;
  color: #94a3b8;
}

.news-badge {
  position: absolute;
  left: 14px;
  top: 12px;
  padding: 5px 10px;
  border-radius: 999px;
  color: #fff;
  background: #7c3aed;
  font-size: 12px;
  font-weight: 900;
}

.news-source {
  position: absolute;
  right: 14px;
  top: 12px;
  padding: 4px 9px;
  border-radius: 999px;
  color: #5b6ff7;
  background: #eef2ff;
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
}

.news-row strong,
.news-row small {
  display: block;
}

.news-row strong {
  color: #0f172a;
  font-size: 15px;
  line-height: 1.45;
}

.news-row small {
  margin-top: 5px;
  color: #94a3b8;
  font-size: 12px;
}

.tone-pink { --tone: #ec4899; --tone-bg: #fff1f5; }
.tone-violet { --tone: #6366f1; --tone-bg: #eef2ff; }
.tone-blue { --tone: #2563eb; --tone-bg: #eff6ff; }
.tone-orange { --tone: #f97316; --tone-bg: #fff7ed; }
.tone-green { --tone: #16a34a; --tone-bg: #f0fdf4; }
.tone-purple { --tone: #7c3aed; --tone-bg: #f5f3ff; }

.news-row-icon {
  background: var(--tone-bg);
  color: var(--tone);
}

@media (max-width: 760px) {
  .featured-news {
    grid-template-columns: 1fr;
  }
}
</style>
