<template>
  <div class="platform-page home-page">
    <section class="platform-card platform-soft-card home-hero">
      <p class="platform-kicker">Love Cube Platform</p>
      <h1 class="platform-title home-title">让官网成为信任入口，让联谊成为真实连接</h1>
      <p class="platform-subtitle">平台层负责信息与规则传达，联谊层专注移动端互动。先了解规则，再进入联谊模块，体验更完整。</p>
      <div class="platform-actions">
        <router-link to="/fellowship-intro" class="platform-btn platform-btn-primary">查看联谊介绍</router-link>
        <router-link to="/about" class="platform-btn platform-btn-ghost">了解平台定位</router-link>
      </div>
      <div class="hero-stats">
        <div class="hero-stat"><strong>公告 / 资讯</strong><span>平台规则与动态统一更新</span></div>
        <div class="hero-stat"><strong>活动日历</strong><span>线上线下活动持续发布</span></div>
        <div class="hero-stat"><strong>联谊入口</strong><span>先看说明，再进入移动端模块</span></div>
      </div>
    </section>

    <section class="platform-block">
      <div class="platform-section-head">
        <h2 class="platform-section-title">公告区</h2>
        <router-link to="/announcements" class="platform-link">查看全部</router-link>
      </div>
      <div class="platform-grid-3">
        <router-link
          v-for="item in publishedAnnouncements"
          :key="item.id"
          :to="`/announcements/${item.id}`"
          class="platform-card platform-item-link"
        >
          <p class="platform-meta">{{ item.date }}</p>
          <h3 class="platform-heading">{{ item.title }}</h3>
          <p class="platform-text">{{ item.summary }}</p>
        </router-link>
      </div>
    </section>

    <section class="platform-block">
      <div class="platform-section-head">
        <h2 class="platform-section-title">资讯区</h2>
        <router-link to="/articles" class="platform-link">查看全部</router-link>
      </div>
      <div class="platform-grid-3">
        <router-link
          v-for="item in publishedArticles"
          :key="item.id"
          :to="`/articles/${item.id}`"
          class="platform-card platform-item-link"
        >
          <p class="platform-meta">{{ item.tag }}</p>
          <h3 class="platform-heading">{{ item.title }}</h3>
          <p class="platform-text">{{ item.summary }}</p>
        </router-link>
      </div>
    </section>

    <section class="platform-block">
      <div class="platform-section-head">
        <h2 class="platform-section-title">活动区</h2>
        <router-link to="/events" class="platform-link">查看全部</router-link>
      </div>
      <div class="platform-grid-3">
        <router-link
          v-for="item in publishedEvents"
          :key="item.id"
          :to="`/events/${item.id}`"
          class="platform-card platform-item-link"
        >
          <p class="platform-meta">{{ item.time }}</p>
          <h3 class="platform-heading">{{ item.title }}</h3>
          <p class="platform-text">{{ item.summary }}</p>
        </router-link>
      </div>
    </section>

    <section class="platform-block">
      <div class="platform-section-head">
        <h2 class="platform-section-title">模块入口</h2>
      </div>
      <div class="platform-grid-2">
        <article class="platform-card">
          <h3 class="platform-heading">平台官网模块</h3>
          <p class="platform-text">集中展示运营公告、平台资讯、活动日历与组织介绍，作为统一入口。</p>
          <router-link to="/announcements" class="platform-link">查看平台内容</router-link>
        </article>
        <article class="platform-card">
          <h3 class="platform-heading"> 联谊模块</h3>
          <p class="platform-text">保留原移动端 UI 与交互，不改业务体验；从联谊介绍页进入更清晰。</p>
          <router-link to="/fellowship-intro" class="platform-link">前往联谊介绍页</router-link>
        </article>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { usePlatformState } from '@/mock/platformState.js'

const { state } = usePlatformState()

const publishedAnnouncements = computed(() => state.announcements.filter((item) => item.status === 'published').slice(0, 3))
const publishedArticles = computed(() => state.articles.filter((item) => item.status === 'published').slice(0, 3))
const publishedEvents = computed(() => state.events.filter((item) => item.status === 'published').slice(0, 3))
</script>

<style scoped>
.home-hero {
  position: relative;
  overflow: hidden;
}

.home-title {
  max-width: 16ch;
}

.hero-stats {
  margin-top: 22px;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
}

.hero-stat {
  background: rgba(255, 255, 255, 0.86);
  border: 1px solid #f7d7df;
  border-radius: 12px;
  padding: 12px;
  display: grid;
  gap: 4px;
}

.hero-stat strong {
  color: #223046;
  font-size: 14px;
}

.hero-stat span {
  color: #5d6b80;
  font-size: 12px;
  line-height: 1.5;
}

@media (max-width: 1023px) {
  .hero-stats {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 767px) {
  .hero-stats {
    grid-template-columns: 1fr;
    margin-top: 14px;
  }

  .hero-stat {
    padding: 10px;
  }
}
</style>
