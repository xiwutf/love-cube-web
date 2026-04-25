<template>
  <div class="platform-page home-page">
    <section class="platform-card platform-soft-card home-hero">
      <div class="hero-grid">
        <div>
          <p class="platform-kicker">Love Cube Platform</p>
          <h1 class="platform-title home-title">让每一次连接，都从可信平台开始</h1>
          <p class="platform-subtitle">
            Love Cube 将平台内容运营与联谊互动模块解耦：官网负责公告、资讯、活动与规则治理，联谊模块聚焦真实社交体验，形成完整产品闭环。
          </p>
          <div class="platform-actions">
            <router-link to="/fellowship-intro" class="platform-btn platform-btn-primary">查看联谊说明并进入</router-link>
            <router-link to="/about" class="platform-btn platform-btn-ghost">了解平台定位</router-link>
          </div>
        </div>

        <aside class="hero-aside">
          <article class="hero-highlight">
            <p>平台运行状态</p>
            <h3>稳定运营中</h3>
            <span>公告、资讯、活动内容持续更新</span>
          </article>
          <div class="hero-stats">
            <div class="hero-stat">
              <strong>规则透明</strong>
              <span>入口先看机制说明</span>
            </div>
            <div class="hero-stat">
              <strong>风控保障</strong>
              <span>实名审核 + 举报闭环</span>
            </div>
            <div class="hero-stat">
              <strong>双层产品</strong>
              <span>官网 + 联谊子应用协同</span>
            </div>
          </div>
        </aside>
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
          <p class="platform-meta">{{ formatDate(item.publishDate) }}</p>
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
          <p class="platform-meta">{{ formatDate(item.eventTime, true) }}</p>
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
          <h3 class="platform-heading">联谊模块</h3>
          <p class="platform-text">保留原移动端 UI 与交互，不改业务体验；从联谊介绍页进入更清晰。</p>
          <router-link to="/fellowship-intro" class="platform-link">前往联谊介绍页</router-link>
        </article>
      </div>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { fetchAnnouncements, fetchArticles, fetchEvents } from '@/api/platformContent.js'

const publishedAnnouncements = ref([])
const publishedArticles = ref([])
const publishedEvents = ref([])

function formatDate(value, withTime = false) {
  if (!value) return ''
  const normalized = String(value).replace('T', ' ')
  return withTime ? normalized.slice(0, 16) : normalized.slice(0, 10)
}

onMounted(async () => {
  try {
    const [announcements, articles, events] = await Promise.all([
      fetchAnnouncements({ status: 'published' }),
      fetchArticles({ status: 'published' }),
      fetchEvents({ status: 'published' })
    ])

    publishedAnnouncements.value = (announcements || []).slice(0, 3)
    publishedArticles.value = (articles || []).slice(0, 3)
    publishedEvents.value = (events || []).slice(0, 3)
  } catch {
    publishedAnnouncements.value = []
    publishedArticles.value = []
    publishedEvents.value = []
  }
})
</script>

<style scoped>
.home-hero {
  position: relative;
  overflow: hidden;
}

.hero-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.35fr) minmax(300px, 0.9fr);
  gap: 24px;
  align-items: stretch;
}

.home-title {
  max-width: 13ch;
}

.hero-aside {
  display: grid;
  gap: 12px;
}

.hero-highlight {
  border-radius: 16px;
  border: 1px solid #f6d5de;
  background: rgba(255, 255, 255, 0.92);
  padding: 16px;
}

.hero-highlight p {
  margin: 0;
  font-size: 12px;
  color: #8a6f7d;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.hero-highlight h3 {
  margin-top: 8px;
  font-size: 24px;
  color: #1f2a44;
}

.hero-highlight span {
  margin-top: 7px;
  display: block;
  color: #5d6b80;
  line-height: 1.6;
}

.hero-stats {
  display: grid;
  gap: 10px;
}

.hero-stat {
  background: rgba(255, 255, 255, 0.88);
  border: 1px solid #f7d7df;
  border-radius: 14px;
  padding: 12px 14px;
  display: grid;
  gap: 5px;
}

.hero-stat strong {
  color: #223046;
  font-size: 15px;
}

.hero-stat span {
  color: #5d6b80;
  font-size: 13px;
  line-height: 1.5;
}

@media (min-width: 768px) and (max-width: 1023px) {
  .hero-grid {
    grid-template-columns: 1fr;
  }

  .home-title {
    max-width: 16ch;
  }

  .hero-aside {
    grid-template-columns: 1fr 1fr;
    align-items: stretch;
  }

  .hero-highlight {
    grid-column: 1 / -1;
  }
}

@media (max-width: 767px) {
  .hero-grid {
    grid-template-columns: 1fr;
    gap: 14px;
  }

  .home-title {
    max-width: 100%;
  }

  .hero-highlight,
  .hero-stat {
    border-radius: 12px;
    padding: 11px;
  }

  .hero-highlight h3 {
    font-size: 20px;
  }

  .hero-stat strong {
    font-size: 14px;
  }

  .hero-stat span {
    font-size: 12px;
  }
}
</style>
