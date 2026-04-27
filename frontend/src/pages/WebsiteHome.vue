<template>
  <div class="home-wrap">
    <section class="hero-section">
      <div class="hc hero-grid">
        <div class="hero-copy">
          <p class="eyebrow">LOVE CUBE PLATFORM</p>
          <h1>Love Cube 多功能连接平台</h1>
          <p class="hero-subtitle">
            从联谊交友、活动参与、内容资讯到更多本地服务，逐步连接真实的人、信息与服务。
          </p>
          <div class="hero-actions">
            <router-link to="/modules" class="btn btn-primary">立即体验平台</router-link>
            <router-link to="/fellowship" class="btn btn-secondary">进入联谊交友</router-link>
          </div>
          <p class="hero-guidance">
            当前重点开放：联谊交友模块，支持资料认证、私信互动、举报治理。
          </p>
        </div>

        <div class="hero-panel">
          <p class="panel-label">平台当前能力</p>
          <div class="hero-tags">
            <div v-for="item in heroTags" :key="item.title" class="hero-tag">
              <span class="tag-dot" :style="{ background: item.color }"></span>
              <div>
                <strong>{{ item.title }}</strong>
                <span>{{ item.desc }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="section">
      <div class="hc feature-card">
        <div class="feature-main">
          <p class="eyebrow">CURRENT FOCUS</p>
          <h2>当前重点：联谊交友</h2>
          <p>
            以真实资料、认证审核、私信互动和举报治理为基础，提供更可靠的联谊体验。
          </p>
          <router-link to="/fellowship" class="btn btn-primary">进入联谊模块</router-link>
        </div>
        <div class="feature-points">
          <div v-for="item in fellowshipCapabilities" :key="item.title" class="mini-card">
            <span class="icon-box" :style="{ color: item.color, background: item.bg }">
              <svg viewBox="0 0 24 24" fill="currentColor" width="22" height="22">
                <path :d="item.path" />
              </svg>
            </span>
            <strong>{{ item.title }}</strong>
          </div>
        </div>
      </div>
    </section>

    <section class="section section-muted">
      <div class="hc">
        <SectionHead
          title="平台模块"
          desc="当前首页保留核心入口，帮助新用户快速理解并开始体验。"
        />
        <div class="module-grid">
          <component
            :is="item.to ? 'router-link' : 'div'"
            v-for="item in platformModules"
            :key="item.title"
            :to="item.to || undefined"
            class="module-card"
            :class="{ 'module-card--planned': item.status === 'planned' }"
          >
            <span class="icon-box" :style="{ color: item.color, background: item.bg }">
              <svg viewBox="0 0 24 24" fill="currentColor" width="22" height="22">
                <path :d="item.path" />
              </svg>
            </span>
            <h3>{{ item.title }}</h3>
            <p>{{ item.desc }}</p>
            <span class="module-cta">{{ item.status === 'planned' ? '敬请期待' : '立即进入' }}</span>
          </component>
        </div>
        <div class="section-action">
          <router-link to="/modules" class="text-link">查看全部模块</router-link>
        </div>
      </div>
    </section>

    <section class="section">
      <div class="hc">
        <SectionHead
          title="平台最新动态"
          desc="公告、资讯和活动合并呈现，减少信息分散。"
        />
        <div class="updates-card">
          <div class="tabs" role="tablist" aria-label="平台最新动态">
            <button
              v-for="tab in updateTabs"
              :key="tab.key"
              type="button"
              class="tab-btn"
              :class="{ active: activeTab === tab.key }"
              @click="activeTab = tab.key"
            >
              {{ tab.label }}
            </button>
          </div>

          <div class="updates-list">
            <component
              :is="item.to ? 'router-link' : 'div'"
              v-for="item in activeUpdates"
              :key="item.key"
              :to="item.to || undefined"
              class="update-row"
              :class="{ 'update-row--empty': !item.to }"
            >
              <span class="update-date">{{ item.date }}</span>
              <div class="update-body">
                <h3>{{ item.title }}</h3>
                <p>{{ item.summary }}</p>
              </div>
            </component>
          </div>
        </div>
      </div>
    </section>

    <section class="section section-muted">
      <div class="hc">
        <SectionHead
          title="平台基础能力"
          desc="为联谊、活动、内容和后续本地服务提供统一底座。"
        />
        <div class="capability-grid">
          <div v-for="item in platformCapabilities" :key="item.title" class="capability-card">
            <span class="icon-box" :style="{ color: item.color, background: item.bg }">
              <svg viewBox="0 0 24 24" fill="currentColor" width="22" height="22">
                <path :d="item.path" />
              </svg>
            </span>
            <h3>{{ item.title }}</h3>
            <p>{{ item.desc }}</p>
          </div>
        </div>
      </div>
    </section>

    <section class="cta-section">
      <div class="hc cta-card">
        <div>
          <p class="eyebrow">NEXT STEP</p>
          <h2>从一个模块开始，逐步扩展成平台</h2>
        </div>
        <div class="cta-actions">
          <router-link to="/modules" class="btn btn-primary">进入模块中心</router-link>
          <router-link to="/account" class="btn btn-secondary btn-secondary-dark">进入个人中心</router-link>
        </div>
        <div class="cta-foot">
          <router-link to="/login">已有账号？立即登录</router-link>
          <router-link to="/login">新用户？3分钟完成注册</router-link>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, defineComponent, h, onMounted, ref } from 'vue'
import { fetchAnnouncements, fetchArticles, fetchEvents } from '@/api/platformContent.js'

const announcements = ref([])
const articles = ref([])
const events = ref([])
const activeTab = ref('announcements')

const SectionHead = defineComponent({
  name: 'SectionHead',
  props: {
    title: { type: String, required: true },
    desc: { type: String, required: true }
  },
  setup(props) {
    return () => h('div', { class: 'section-head' }, [
      h('h2', props.title),
      h('p', props.desc)
    ])
  }
})

function formatDate(value) {
  if (!value) return '暂无日期'
  return String(value).replace('T', ' ').slice(0, 10)
}

function normalizeList(list, type) {
  const emptyText = {
    announcements: '暂无公告',
    articles: '暂无资讯',
    events: '暂无活动'
  }

  const pathMap = {
    announcements: 'announcements',
    articles: 'articles',
    events: 'events'
  }

  const source = list.slice(0, 3).map((item) => ({
    key: `${type}-${item.id}`,
    title: item.title || emptyText[type],
    summary: item.summary || item.description || item.location || '更多内容请进入详情查看。',
    date: formatDate(item.publishDate || item.date || item.time || item.startTime),
    to: `/${pathMap[type]}/${item.id}`
  }))

  if (source.length) return source

  return [{
    key: `${type}-empty`,
    title: emptyText[type],
    summary: '内容更新后将在这里展示。',
    date: '--',
    to: ''
  }]
}

onMounted(async () => {
  const [ann, art, ev] = await Promise.allSettled([
    fetchAnnouncements({ status: 'published' }),
    fetchArticles({ status: 'published' }),
    fetchEvents({ status: 'published' })
  ])

  if (ann.status === 'fulfilled') announcements.value = ann.value || []
  if (art.status === 'fulfilled') articles.value = art.value || []
  if (ev.status === 'fulfilled') events.value = ev.value || []
})

const updateTabs = [
  { key: 'announcements', label: '公告' },
  { key: 'articles', label: '资讯' },
  { key: 'events', label: '活动' }
]

const activeUpdates = computed(() => {
  const map = {
    announcements: normalizeList(announcements.value, 'announcements'),
    articles: normalizeList(articles.value, 'articles'),
    events: normalizeList(events.value, 'events')
  }
  return map[activeTab.value]
})

const PATHS = {
  heart: 'M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z',
  calendar: 'M17 12h-5v5h5v-5zM16 1v2H8V1H6v2H5c-1.11 0-1.99.9-1.99 2L3 19c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2h-1V1h-2zm3 18H5V8h14v11z',
  bell: 'M12 22c1.1 0 2-.9 2-2h-4c0 1.1.9 2 2 2zm6-6v-5c0-3.07-1.64-5.64-4.5-6.32V4c0-.83-.67-1.5-1.5-1.5s-1.5.67-1.5 1.5v.68C7.63 5.36 6 7.92 6 11v5l-2 2v1h16v-1l-2-2z',
  article: 'M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-5 14H7v-2h7v2zm3-4H7v-2h10v2zm0-4H7V7h10v2z',
  shield: 'M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4zm-2 16l-4-4 1.41-1.41L10 14.17l6.59-6.59L18 9l-8 8z',
  message: 'M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2z',
  person: 'M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z',
  spark: 'M19 9l1.25-2.75L23 5l-2.75-1.25L19 1l-1.25 2.75L15 5l2.75 1.25L19 9zm-7.5.5L9 4 6.5 9.5 1 12l5.5 2.5L9 20l2.5-5.5L17 12l-5.5-2.5z'
}

const heroTags = [
  { title: '联谊交友', desc: '当前重点模块', color: '#f45b7a' },
  { title: '活动内容', desc: '运营信息入口', color: '#2563eb' },
  { title: '平台治理', desc: '认证与安全机制', color: '#059669' }
]

const fellowshipCapabilities = [
  { title: '资料认证', path: PATHS.person, color: '#7c3aed', bg: '#f5f3ff' },
  { title: '智能匹配', path: PATHS.spark, color: '#2563eb', bg: '#eff6ff' },
  { title: '私信互动', path: PATHS.message, color: '#f45b7a', bg: '#fff1f4' },
  { title: '举报黑名单', path: PATHS.shield, color: '#059669', bg: '#ecfdf5' }
]

const platformModules = [
  { title: '联谊交友', desc: '真实资料、匹配互动与安全治理。', to: '/fellowship', path: PATHS.heart, color: '#f45b7a', bg: '#fff1f4', status: 'active' },
  { title: '活动中心', desc: '活动发布、报名参与和现场连接。', to: '/events', path: PATHS.calendar, color: '#2563eb', bg: '#eff6ff', status: 'active' },
  { title: '内容资讯', desc: '精选文章、使用指南与平台内容。', to: '/articles', path: PATHS.article, color: '#059669', bg: '#ecfdf5', status: 'active' },
  { title: '公告通知', desc: '平台公告、规则说明和重要通知。', to: '/announcements', path: PATHS.bell, color: '#0891b2', bg: '#ecfeff', status: 'active' }
]

const platformCapabilities = [
  { title: '统一账号体系', desc: '统一登录、个人资料和跨模块账号能力。', path: PATHS.person, color: '#7c3aed', bg: '#f5f3ff' },
  { title: '内容与活动运营', desc: '支持公告、文章、活动等运营内容统一发布。', path: PATHS.article, color: '#059669', bg: '#ecfdf5' },
  { title: '通知与消息中心', desc: '承接站内通知、私信互动和重要消息提醒。', path: PATHS.message, color: '#2563eb', bg: '#eff6ff' },
  { title: '举报、黑名单与认证治理', desc: '用认证审核和治理机制维护平台秩序。', path: PATHS.shield, color: '#f45b7a', bg: '#fff1f4' }
]
</script>

<style scoped>
.home-wrap {
  --primary: #1677ff;
  --primary-dark: #0f5ad6;
  --accent: #36cfc9;
  --bg: #f3f6fb;
  --card: #ffffff;
  --border: #d9e1ec;
  --text: #0f1f3d;
  --muted: #556987;
  --muted-light: #8da1bf;
  --radius-card: 10px;
  --radius-tile: 8px;
  --shadow-card: 0 12px 28px rgba(9, 24, 48, 0.06);
  --shadow-card-hover: 0 18px 36px rgba(9, 24, 48, 0.12);
  background: var(--bg);
  color: var(--text);
}

.hc {
  width: calc(100% - 48px);
  max-width: none;
  margin: 0 auto;
}

.section {
  padding: 72px 0;
}

.section-muted {
  background: #f8fbff;
  border-block: 1px solid var(--border);
}

.section-head {
  max-width: 700px;
  margin-bottom: 32px;
}

.section-head h2,
.feature-main h2,
.cta-card h2 {
  margin: 0;
  font-size: clamp(28px, 3vw, 42px);
  line-height: 1.18;
  font-weight: 900;
  letter-spacing: 0;
}

.section-head p,
.feature-main p {
  margin: 12px 0 0;
  color: var(--muted);
  font-size: 17px;
  line-height: 1.8;
}

.eyebrow {
  margin: 0 0 14px;
  color: #6ea8ff;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.16em;
}

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 50px;
  padding: 0 28px;
  border-radius: 6px;
  font-size: 16px;
  font-weight: 800;
  text-decoration: none;
  transition: transform 0.16s ease, box-shadow 0.16s ease, border-color 0.16s ease;
}

.btn:hover {
  transform: translateY(-1px);
}

.btn-primary {
  background: linear-gradient(135deg, var(--primary), #1b8bff);
  color: #fff;
  box-shadow: 0 10px 24px rgba(22, 119, 255, 0.28);
}

.btn-secondary {
  background: #ffffff;
  border: 1px solid #c7d3e4;
  color: #1b3354;
}

.btn-secondary-dark {
  background: #ffffff;
  border-color: #c7d3e4;
  color: #1b3354;
}

.text-link {
  display: inline-flex;
  align-items: center;
  min-height: 44px;
  padding: 0 20px;
  border: 1px solid #cbd5e1;
  border-radius: 6px;
  color: #0f5ad6;
  background: #f7fbff;
  font-weight: 800;
  text-decoration: none;
}

.hero-section {
  margin: 18px clamp(12px, 2vw, 28px) 0;
  border-radius: 18px;
  overflow: hidden;
  background:
    radial-gradient(circle at 84% 18%, rgba(22, 119, 255, 0.18), transparent 34%),
    radial-gradient(circle at 96% 10%, rgba(244, 91, 122, 0.14), transparent 30%),
    linear-gradient(140deg, #f7faff 0%, #eef5ff 55%, #eaf2ff 100%);
  border: 1px solid #d8e4f5;
  padding: 88px 0 84px;
}

.hero-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.3fr) minmax(360px, 460px);
  gap: 72px;
  align-items: center;
}

.hero-copy h1 {
  max-width: 760px;
  margin: 0;
  font-size: clamp(42px, 5.6vw, 74px);
  line-height: 1.08;
  font-weight: 900;
  letter-spacing: 0;
  color: #12274a;
  text-wrap: balance;
}

.hero-subtitle {
  max-width: 660px;
  margin: 24px 0 0;
  color: #3b567e;
  font-size: 20px;
  line-height: 1.85;
}

.hero-guidance {
  margin: 16px 0 0;
  color: #2a5a9b;
  font-size: 15px;
  line-height: 1.6;
  font-weight: 700;
}

.hero-actions,
.cta-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  margin-top: 34px;
}

.hero-panel,
.feature-card,
.updates-card,
.cta-card {
  background: var(--card);
  border: 1px solid var(--border);
  border-radius: var(--radius-card);
  box-shadow: var(--shadow-card);
}

.hero-panel {
  padding: 28px;
  background: rgba(255, 255, 255, 0.88);
  border: 1px solid #d3e0f2;
  backdrop-filter: blur(6px);
}

.panel-label {
  margin: 0 0 18px;
  color: #4d6488;
  font-size: 14px;
  font-weight: 800;
}

.hero-tags {
  display: grid;
  gap: 14px;
}

.hero-tag {
  display: flex;
  gap: 14px;
  align-items: center;
  min-height: 78px;
  padding: 18px;
  border: 1px solid #d7e4f6;
  border-radius: var(--radius-tile);
  background: rgba(246, 250, 255, 0.9);
}

.tag-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex: 0 0 auto;
}

.hero-tag strong,
.mini-card strong {
  display: block;
  font-size: 18px;
  font-weight: 900;
}

.hero-tag span:not(.tag-dot) {
  display: block;
  margin-top: 4px;
  color: #5b7195;
  font-size: 14px;
}

.hero-tag strong {
  color: #12274a;
}

.feature-card {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1.2fr);
  gap: 48px;
  align-items: center;
  padding: 48px;
}

.feature-points,
.module-grid,
.capability-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 18px;
}

.feature-points {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.mini-card,
.module-card,
.capability-card {
  background: #fff;
  border: 1px solid var(--border);
  border-radius: var(--radius-tile);
  padding: 22px;
}

.mini-card {
  min-height: 142px;
}

.icon-box {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  margin-bottom: 18px;
  border-radius: 12px;
}

.module-card,
.capability-card {
  min-height: 220px;
  color: inherit;
  text-decoration: none;
  transition: transform 0.16s ease, box-shadow 0.16s ease, border-color 0.16s ease;
}

.module-card:hover {
  transform: translateY(-2px);
  border-color: #9fc4ff;
  box-shadow: var(--shadow-card-hover);
}

.module-card--planned {
  opacity: 0.78;
}

.module-card h3,
.capability-card h3 {
  margin: 0 0 10px;
  font-size: 20px;
  font-weight: 900;
}

.module-card p,
.capability-card p {
  margin: 0;
  color: var(--muted);
  font-size: 15px;
  line-height: 1.75;
}

.module-cta {
  display: inline-flex;
  margin-top: 14px;
  font-size: 13px;
  font-weight: 800;
  color: #0f5ad6;
}

.module-card--planned .module-cta {
  color: #64748b;
}

.section-action {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

.updates-card {
  overflow: hidden;
  background: #fff;
}

.tabs {
  display: flex;
  gap: 8px;
  padding: 18px;
  border-bottom: 1px solid var(--border);
  background: #f7fbff;
}

.tab-btn {
  min-width: 86px;
  min-height: 40px;
  border: 1px solid transparent;
  border-radius: 8px;
  background: transparent;
  color: var(--muted);
  font-size: 15px;
  font-weight: 800;
  cursor: pointer;
}

.tab-btn.active {
  background: #fff;
  color: #0f5ad6;
  border-color: #bfd7ff;
  box-shadow: 0 6px 16px rgba(15, 23, 42, 0.06);
}

.updates-list {
  padding: 8px 34px 12px;
}

.update-row {
  display: grid;
  grid-template-columns: 120px minmax(0, 1fr);
  gap: 24px;
  padding: 24px 0;
  color: inherit;
  text-decoration: none;
  border-bottom: 1px solid var(--border);
}

.update-row:last-child {
  border-bottom: 0;
}

.update-row:not(.update-row--empty):hover h3 {
  color: var(--primary);
}

.update-date {
  color: var(--muted-light);
  font-size: 14px;
  font-weight: 800;
}

.update-body h3 {
  margin: 0 0 8px;
  font-size: 18px;
  line-height: 1.45;
  font-weight: 900;
  transition: color 0.16s ease;
}

.update-body p {
  display: -webkit-box;
  margin: 0;
  overflow: hidden;
  color: var(--muted);
  font-size: 15px;
  line-height: 1.7;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.cta-section {
  margin: 18px clamp(12px, 2vw, 28px) 24px;
  border-radius: 18px;
  overflow: hidden;
  padding: 72px 0;
  background: linear-gradient(135deg, #edf4ff 0%, #f6f8ff 60%, #fff5f7 100%);
  border: 1px solid #d8e4f5;
}

.cta-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 32px;
  padding: 46px 48px;
  background: rgba(255, 255, 255, 0.86);
  border-color: #d4e0f1;
  flex-wrap: wrap;
}

.cta-card h2 {
  color: #12274a;
}

.cta-card .eyebrow {
  color: #5f7ca6;
}

.cta-actions {
  margin-top: 0;
  flex: 0 0 auto;
}

.cta-foot {
  width: 100%;
  display: flex;
  gap: 18px;
  flex-wrap: wrap;
  margin-top: 6px;
}

.cta-foot a {
  color: #5f7393;
  font-size: 14px;
  font-weight: 700;
  text-decoration: none;
}

.cta-foot a:hover {
  color: #e84f73;
}

@media (max-width: 1199px) {
  .hc {
    width: calc(100% - 32px);
  }

  .module-grid,
  .capability-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 900px) {
  .hero-grid,
  .feature-card {
    grid-template-columns: 1fr;
    gap: 36px;
  }

  .hero-panel {
    max-width: 620px;
    padding: 22px;
  }
}

@media (min-width: 1360px) {
  .feature-card {
    padding: 54px;
  }

  .module-grid,
  .capability-grid {
    gap: 22px;
  }
}

@media (max-width: 767px) {
  .hc {
    width: calc(100% - 24px);
  }

  .hero-section {
    margin: 12px 12px 0;
    padding: 58px 0 52px;
  }

  .section,
  .cta-section {
    padding: 56px 0;
  }

  .cta-section {
    margin: 12px 12px 16px;
  }

  .hero-copy h1 {
    font-size: clamp(36px, 11vw, 52px);
  }

  .hero-subtitle,
  .section-head p,
  .feature-main p {
    font-size: 16px;
  }

  .hero-actions,
  .cta-actions {
    flex-direction: column;
  }

  .btn,
  .text-link {
    width: 100%;
  }

  .feature-card,
  .hero-panel,
  .cta-card {
    padding: 24px;
  }

  .feature-points,
  .module-grid,
  .capability-grid {
    grid-template-columns: 1fr;
  }

  .mini-card,
  .module-card,
  .capability-card {
    min-height: auto;
  }

  .tabs {
    padding: 12px;
  }

  .tab-btn {
    flex: 1;
    min-width: 0;
  }

  .updates-list {
    padding: 0 18px;
  }

  .update-row {
    grid-template-columns: 1fr;
    gap: 8px;
    padding: 20px 0;
  }

  .cta-card {
    align-items: stretch;
    flex-direction: column;
  }

  .cta-foot {
    flex-direction: column;
    gap: 10px;
  }
}
</style>
