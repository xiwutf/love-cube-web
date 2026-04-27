<template>
  <div class="home-wrap">

    <!-- ===== 01 HERO ===== -->
    <section class="hero">
      <div class="container">
        <div class="hero-inner">
          <div class="hero-text">
            <p class="eyebrow">LOVE CUBE PLATFORM</p>
            <h1>Love Cube<br>多功能连接平台</h1>
            <p class="hero-lead">
              聚合内容资讯、活动服务、社交连接、本地服务与 AI 工具能力，为不同场景提供统一入口。
            </p>
            <div class="hero-actions">
              <router-link to="/modules" class="btn btn-primary">进入模块中心</router-link>
              <router-link to="/articles" class="btn btn-ghost">查看平台动态</router-link>
            </div>
            <ul class="hero-checks">
              <li v-for="v in heroChecks" :key="v">
                <span class="check-mark">✓</span>{{ v }}
              </li>
            </ul>
          </div>
          <div class="hero-visual">
            <img
              :src="heroImage"
              alt="Love Cube 首页主视觉"
              loading="eager"
              fetchpriority="high"
              decoding="async"
              @error="onMediaError"
            >
          </div>
        </div>
      </div>
    </section>

    <!-- ===== 02 MODULE MATRIX ===== -->
    <section class="section">
      <div class="container">
        <header class="section-head">
          <div>
            <p class="eyebrow">PRODUCT MATRIX</p>
            <h2>平台模块</h2>
            <p class="section-sub">已开放与规划中的核心能力模块</p>
          </div>
          <router-link to="/modules" class="text-link">查看全部 →</router-link>
        </header>
        <div class="modules-grid">
          <component
            :is="item.to ? 'router-link' : 'div'"
            v-for="item in platformModules"
            :key="item.title"
            :to="item.to || undefined"
            class="module-card"
            :class="{ planned: item.status === 'planned' }"
          >
            <div class="module-icon" :class="item.tone">{{ item.icon }}</div>
            <h3>{{ item.title }}</h3>
            <p>{{ item.desc }}</p>
            <div class="module-footer">
              <span class="status-tag" :class="item.status === 'active' ? 'tag-active' : 'tag-planned'">
                {{ item.status === 'active' ? '已开放' : '规划中' }}
              </span>
              <span class="module-cta">{{ item.status === 'active' ? '立即进入 →' : '查看规划 →' }}</span>
            </div>
          </component>
        </div>
      </div>
    </section>

    <!-- ===== 03 CAPABILITY SYSTEM ===== -->
    <section class="section bg-soft">
      <div class="container">
        <header class="section-head centered">
          <p class="eyebrow">PLATFORM CAPABILITY</p>
          <h2>平台基础能力</h2>
          <p class="section-sub">面向长期运营的基础能力沉淀，支持模块持续扩展与统一治理。</p>
        </header>
        <div class="cap-grid">
          <article v-for="item in capabilities" :key="item.title" class="cap-card">
            <div class="cap-icon">{{ item.icon }}</div>
            <h3>{{ item.title }}</h3>
            <p>{{ item.desc }}</p>
          </article>
        </div>
      </div>
    </section>

    <!-- ===== 04 FELLOWSHIP FOCUS ===== -->
    <section class="section">
      <div class="container">
        <div class="fellowship-panel">
          <div class="fellowship-text">
            <p class="eyebrow eyebrow-pink">CURRENT PRIORITY</p>
            <h2>当前重点完善<br>联谊交友</h2>
            <p class="panel-lead">
              已接入资料认证、互动私信、举报黑名单、通知提醒等能力，持续优化真实、安全、高质量连接体验。
            </p>
            <ul class="feature-list">
              <li>资料展示、实名认证与个人安全边界</li>
              <li>匹配推荐、会话沟通与消息提醒</li>
              <li>举报拉黑、内容审核与平台治理</li>
            </ul>
            <div class="panel-actions">
              <router-link to="/fellowship" class="btn btn-primary">进入联谊模块</router-link>
              <router-link to="/fellowship-intro" class="btn btn-ghost">了解联谊规则</router-link>
            </div>
          </div>
          <div class="fellowship-visual">
            <img
              :src="moduleFellowshipImage"
              alt="联谊交友"
              loading="lazy"
              decoding="async"
              @error="onMediaError"
            >
          </div>
        </div>
      </div>
    </section>

    <!-- ===== 05 LATEST UPDATES ===== -->
    <section class="section">
      <div class="container">
        <header class="section-head">
          <div>
            <p class="eyebrow">UPDATES</p>
            <h2>平台最新动态</h2>
            <p class="section-sub">公告、资讯与活动，按类型快速获取。</p>
          </div>
          <div class="tabs" role="tablist" aria-label="动态类型">
            <button
              v-for="tab in updateTabs"
              :key="tab.key"
              type="button"
              class="tab-btn"
              :class="{ active: activeUpdateTab === tab.key }"
              @click="activeUpdateTab = tab.key"
            >{{ tab.label }}</button>
          </div>
        </header>
        <div class="updates-grid">
          <component
            :is="item.to ? 'router-link' : 'article'"
            v-for="item in activeUpdates.slice(0, 3)"
            :key="item.key"
            :to="item.to || undefined"
            class="update-card"
          >
            <div v-if="item.cover" class="update-thumb">
              <img :src="item.cover" :alt="item.title" loading="lazy" decoding="async" @error="onMediaError">
            </div>
            <div class="update-body">
              <div class="update-meta">
                <span>{{ item.category }}</span>
                <span>{{ item.date }}</span>
              </div>
              <h3>{{ item.title }}</h3>
              <p>{{ item.summary }}</p>
            </div>
            <span class="update-tag">{{ item.tag }}</span>
          </component>
        </div>
      </div>
    </section>

    <!-- ===== 06 VALUE ENDORSEMENT ===== -->
    <section class="section bg-soft">
      <div class="container">
        <div class="stats-row">
          <div v-for="stat in platformStats" :key="stat.label" class="stat-item">
            <strong class="stat-value">{{ stat.value }}</strong>
            <span class="stat-label">{{ stat.label }}</span>
          </div>
        </div>
      </div>
    </section>

    <!-- ===== 07 CTA ===== -->
    <section class="section">
      <div class="container">
        <div class="cta-banner" :style="ctaBgStyle">
          <div class="cta-mask"></div>
          <div class="cta-inner">
            <p class="eyebrow eyebrow-dim">START WITH LOVE CUBE</p>
            <h2>从一个入口开始<br>连接更多场景</h2>
            <p>模块、内容、活动、消息和用户体系统一承载，让平台从单点功能走向持续运营。</p>
            <div class="cta-actions">
              <router-link to="/modules" class="btn btn-white">进入模块中心</router-link>
              <router-link to="/account" class="btn btn-outline-white">进入个人中心</router-link>
            </div>
          </div>
        </div>
      </div>
    </section>

  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { fetchAnnouncements, fetchArticles, fetchEvents } from '@/api/platformContent.js'
import heroImage from '@/assets/首页首屏右侧大图.png'
import moduleFellowshipImage from '@/assets/联谊专区.png'
import moduleEventsImage from '@/assets/活动模块.png'
import moduleArticlesImage from '@/assets/公告模块卡片.png'
import ctaBackgroundImage from '@/assets/底部横幅 CTA.png'

const announcements = ref([])
const articles = ref([])
const events = ref([])
const activeUpdateTab = ref('announcements')

const heroChecks = ['多模块入口', '统一账号体系', '持续扩展能力']

const platformModules = [
  { title: '联谊交友', desc: '资料认证、互动私信、匹配推荐与安全治理。', to: '/fellowship', status: 'active', icon: '联', tone: 'tone-blue' },
  { title: '活动中心', desc: '承载主题活动、线下信息、报名入口与活动动态。', to: '/events', status: 'active', icon: '活', tone: 'tone-cyan' },
  { title: '内容资讯', desc: '沉淀平台文章、精选资讯和长期可读内容。', to: '/articles', status: 'active', icon: '文', tone: 'tone-green' },
  { title: '公告通知', desc: '发布平台规则、版本说明、重要通知和运营公告。', to: '/announcements', status: 'active', icon: '告', tone: 'tone-amber' },
  { title: '本地服务', desc: '规划招聘、二手车、生活服务等本地化场景入口。', to: '', status: 'planned', icon: '服', tone: 'tone-violet' },
  { title: 'AI 工具', desc: '规划智能助手、效率工具、内容生成和辅助决策能力。', to: '', status: 'planned', icon: 'AI', tone: 'tone-rose' }
]

const capabilities = [
  { title: '统一账号体系', desc: '账号、资料、权限与认证状态统一管理，跨模块通用。', icon: '账' },
  { title: '消息通知中心', desc: '面向互动、公告与系统提醒的通知基础设施。', icon: '消' },
  { title: '内容运营能力', desc: '文章、公告、专题内容按发布状态沉淀，支持运营节奏。', icon: '内' },
  { title: '治理与风控', desc: '围绕举报、黑名单和规则说明建立平台安全秩序。', icon: '治' }
]

const platformStats = [
  { value: '4+', label: '已开放核心模块' },
  { value: '多类', label: '内容与活动场景' },
  { value: '统一', label: '账号与体验入口' },
  { value: '持续', label: '迭代升级中' }
]

const updateTabs = [
  { key: 'announcements', label: '公告' },
  { key: 'articles', label: '资讯' },
  { key: 'events', label: '活动' }
]

function formatDate(value) {
  if (!value) return '暂无日期'
  return String(value).replace('T', ' ').slice(0, 10)
}

function createFallbackRows(category, tag, cover = '') {
  return [
    {
      key: `empty-${category}-1`,
      title: `${category}内容更新中`,
      summary: '平台内容会在发布后展示在这里，方便用户快速了解最新进展。',
      date: '--', category, tag, cover, to: ''
    },
    {
      key: `empty-${category}-2`,
      title: '更多平台动态即将发布',
      summary: '后续将围绕模块进展、运营活动和服务说明持续补充。',
      date: '--', category, tag: '预告', cover: '', to: ''
    },
    {
      key: `empty-${category}-3`,
      title: '平台持续迭代更新',
      summary: '保持关注以获取最新平台动态与功能升级信息。',
      date: '--', category, tag: '进行中', cover: '', to: ''
    }
  ]
}

const updateGroups = computed(() => {
  const announcementRows = (announcements.value || []).slice(0, 3).map((item) => ({
    key: `announcement-${item.id}`,
    title: item.title || '平台公告',
    summary: item.summary || item.description || '重要平台信息请进入详情查看。',
    date: formatDate(item.publishDate || item.createdAt),
    category: '公告',
    tag: item.status === 'published' ? '已发布' : '通知',
    cover: item.coverUrl || item.cover || '',
    to: item.id ? `/announcements/${item.id}` : ''
  }))

  const articleRows = (articles.value || []).slice(0, 3).map((item, index) => ({
    key: `article-${item.id}`,
    title: item.title || '平台资讯',
    summary: item.summary || item.description || '内容更新后将在这里展示。',
    date: formatDate(item.publishDate || item.createdAt),
    category: '资讯',
    tag: '精选',
    cover: item.coverUrl || item.cover || (index === 0 ? moduleArticlesImage : ''),
    to: item.id ? `/articles/${item.id}` : ''
  }))

  const eventRows = (events.value || []).slice(0, 3).map((item, index) => ({
    key: `event-${item.id}`,
    title: item.title || '平台活动',
    summary: item.summary || item.description || item.location || '更多活动内容请进入详情查看。',
    date: formatDate(item.publishDate || item.date || item.time || item.startTime),
    category: '活动',
    tag: item.location || '活动',
    cover: item.coverUrl || item.cover || (index === 0 ? moduleEventsImage : ''),
    to: item.id ? `/events/${item.id}` : ''
  }))

  return {
    announcements: announcementRows.length ? announcementRows : createFallbackRows('公告', '通知'),
    articles: articleRows.length ? articleRows : createFallbackRows('资讯', '精选', moduleArticlesImage),
    events: eventRows.length ? eventRows : createFallbackRows('活动', '报名', moduleEventsImage)
  }
})

const activeUpdates = computed(() => updateGroups.value[activeUpdateTab.value] || [])

const ctaBgStyle = {
  backgroundImage: `url(${ctaBackgroundImage})`,
  backgroundSize: 'cover',
  backgroundPosition: 'center'
}

function onMediaError(event) {
  if (event?.target?.classList) event.target.classList.add('is-hidden')
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
</script>

<style scoped>
/* ===== TOKENS ===== */
.home-wrap {
  --c-text:       #0f172a;
  --c-muted:      #64748b;
  --c-soft:       #f1f5f9;
  --c-border:     #e2e8f0;
  --c-blue:       #2563eb;
  --c-blue-dark:  #1e3a8a;
  --c-pink:       #ec4899;
  --c-green:      #16a34a;
  --shadow-sm:    0 1px 3px rgba(15,23,42,.06), 0 4px 12px rgba(15,23,42,.04);
  --shadow-md:    0 4px 16px rgba(15,23,42,.08), 0 12px 32px rgba(15,23,42,.06);
  --shadow-lg:    0 8px 32px rgba(15,23,42,.10), 0 24px 48px rgba(15,23,42,.07);
  --radius:       16px;
  --radius-sm:    10px;
  color: var(--c-text);
  background: #f8fafc;
}

/* ===== CONTAINER ===== */
.container {
  width: calc(100% - 48px);
  margin: 0 auto;
}

/* ===== SECTION ===== */
.section {
  padding: 72px 0;
}

.bg-soft {
  background: var(--c-soft);
}

/* ===== SECTION HEAD ===== */
.section-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 24px;
  margin-bottom: 48px;
}

.section-head.centered {
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.section-head h2 {
  margin: 8px 0 0;
  font-size: clamp(32px, 2.8vw, 40px);
  font-weight: 800;
  line-height: 1.15;
}

.section-sub {
  margin: 10px 0 0;
  font-size: 16px;
  color: var(--c-muted);
  line-height: 1.7;
}

/* ===== EYEBROW ===== */
.eyebrow {
  display: block;
  margin: 0;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: .18em;
  color: var(--c-blue);
}

.eyebrow-pink { color: var(--c-pink); }
.eyebrow-dim  { color: rgba(255,255,255,.65); }

/* ===== BUTTONS ===== */
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 48px;
  padding: 0 28px;
  border-radius: var(--radius-sm);
  font-size: 15px;
  font-weight: 700;
  text-decoration: none;
  border: 1.5px solid transparent;
  cursor: pointer;
  transition: transform .18s ease, box-shadow .18s ease;
}

.btn:hover { transform: translateY(-2px); }

.btn-primary {
  color: #fff;
  background: linear-gradient(135deg, #1d4ed8, #3b82f6);
  box-shadow: 0 8px 24px rgba(37,99,235,.28);
}
.btn-primary:hover { box-shadow: 0 12px 32px rgba(37,99,235,.38); }

.btn-ghost {
  color: var(--c-blue-dark);
  background: #fff;
  border-color: #bfd2f6;
}
.btn-ghost:hover { border-color: var(--c-blue); }

.btn-white {
  color: var(--c-blue-dark);
  background: #fff;
  box-shadow: 0 4px 16px rgba(0,0,0,.14);
}

.btn-outline-white {
  color: #fff;
  background: transparent;
  border-color: rgba(255,255,255,.55);
}
.btn-outline-white:hover { background: rgba(255,255,255,.1); }

.text-link {
  flex-shrink: 0;
  color: var(--c-blue);
  font-size: 14px;
  font-weight: 700;
  text-decoration: none;
  white-space: nowrap;
}

/* ===== 01 HERO ===== */
.hero {
  background: linear-gradient(145deg, #eff6ff 0%, #f8fafc 50%, #eef4ff 100%);
  padding: 88px 0;
  border-bottom: 1px solid #e2e8f0;
}

.hero-inner {
  display: grid;
  grid-template-columns: 55fr 45fr;
  align-items: center;
  gap: 72px;
  min-height: 480px;
}

.hero-text h1 {
  margin: 14px 0 0;
  font-size: clamp(48px, 5vw, 72px);
  font-weight: 900;
  line-height: 1.05;
  letter-spacing: -.02em;
}

.hero-lead {
  margin: 20px 0 0;
  font-size: 18px;
  color: #334e73;
  line-height: 1.85;
  max-width: 560px;
}

.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 32px;
}

.hero-checks {
  display: flex;
  flex-wrap: wrap;
  gap: 12px 28px;
  margin: 32px 0 0;
  padding: 0;
  list-style: none;
}

.hero-checks li {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #334155;
}

.check-mark {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: #dcfce7;
  color: var(--c-green);
  font-size: 12px;
  font-weight: 900;
  flex-shrink: 0;
}

.hero-visual {
  border-radius: var(--radius);
  border: 1px solid #d9e5f7;
  background: #fff;
  box-shadow: var(--shadow-lg);
  overflow: hidden;
  display: flex;
  align-items: center;
  min-height: 400px;
}

.hero-visual img {
  width: 100%;
  max-height: 580px;
  object-fit: contain;
}

/* ===== 02 MODULES GRID ===== */
.modules-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 20px;
}

.module-card {
  display: flex;
  flex-direction: column;
  padding: 26px 24px 22px;
  border-radius: var(--radius);
  border: 1px solid var(--c-border);
  background: #fff;
  box-shadow: var(--shadow-sm);
  color: inherit;
  text-decoration: none;
  transition: transform .2s ease, box-shadow .2s ease, border-color .2s ease;
  min-height: 220px;
}

.module-card:hover {
  transform: translateY(-4px);
  border-color: #93c5fd;
  box-shadow: var(--shadow-md);
}

.module-card.planned {
  background: linear-gradient(180deg, #fff, #f8fafc);
}

.module-card.planned:hover { opacity: 1; }

.module-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 12px;
  font-weight: 900;
  font-size: 16px;
  margin-bottom: 18px;
  flex-shrink: 0;
}

.tone-blue   { color: #1d4ed8; background: #dbeafe; }
.tone-cyan   { color: #047481; background: #cffafe; }
.tone-green  { color: #166534; background: #dcfce7; }
.tone-amber  { color: #92400e; background: #fef3c7; }
.tone-violet { color: #6d28d9; background: #ede9fe; }
.tone-rose   { color: #be123c; background: #ffe4e6; }

.module-card h3 {
  margin: 0 0 8px;
  font-size: 20px;
  font-weight: 700;
}

.module-card p {
  margin: 0;
  font-size: 14px;
  color: var(--c-muted);
  line-height: 1.75;
  flex: 1;
}

.module-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 20px;
}

.status-tag {
  display: inline-flex;
  align-items: center;
  height: 24px;
  padding: 0 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
}

.tag-active  { background: #dbeafe; color: #1e40af; }
.tag-planned { background: #f1f5f9; color: #64748b; }

.module-cta {
  font-size: 13px;
  font-weight: 700;
  color: var(--c-blue);
}

.module-card.planned .module-cta { color: var(--c-muted); }

/* ===== 03 CAPABILITY ===== */
.cap-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 20px;
}

.cap-card {
  padding: 28px 24px;
  border-radius: var(--radius);
  border: 1px solid var(--c-border);
  background: #fff;
  box-shadow: var(--shadow-sm);
}

.cap-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: #eff6ff;
  color: var(--c-blue);
  font-size: 16px;
  font-weight: 900;
  margin-bottom: 18px;
}

.cap-card h3 {
  margin: 0 0 8px;
  font-size: 18px;
  font-weight: 700;
}

.cap-card p {
  margin: 0;
  font-size: 14px;
  color: var(--c-muted);
  line-height: 1.75;
}

/* ===== 04 FELLOWSHIP PANEL ===== */
.fellowship-panel {
  display: grid;
  grid-template-columns: 60fr 40fr;
  border-radius: var(--radius);
  border: 1px solid var(--c-border);
  background: #fff;
  box-shadow: var(--shadow-md);
  overflow: hidden;
}

.fellowship-text {
  padding: 56px 52px;
}

.fellowship-text h2 {
  margin: 14px 0 0;
  font-size: clamp(30px, 2.6vw, 44px);
  font-weight: 800;
  line-height: 1.18;
}

.panel-lead {
  margin: 18px 0 0;
  font-size: 16px;
  color: var(--c-muted);
  line-height: 1.85;
  max-width: 520px;
}

.feature-list {
  margin: 22px 0 0;
  padding: 0;
  list-style: none;
  display: grid;
  gap: 10px;
}

.feature-list li {
  position: relative;
  padding-left: 20px;
  font-size: 15px;
  color: #334155;
  line-height: 1.65;
}

.feature-list li::before {
  content: '';
  position: absolute;
  left: 0;
  top: 9px;
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--c-pink);
}

.panel-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 30px;
}

.fellowship-visual {
  background: linear-gradient(135deg, #fce7f3, #fbcfe8);
  display: flex;
  align-items: stretch;
}

.fellowship-visual img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  min-height: 380px;
}

/* ===== 05 UPDATES ===== */
.tabs {
  display: inline-flex;
  gap: 4px;
  padding: 4px;
  background: #fff;
  border: 1px solid var(--c-border);
  border-radius: var(--radius-sm);
  flex-shrink: 0;
}

.tab-btn {
  height: 36px;
  min-width: 68px;
  padding: 0 16px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 700;
  color: var(--c-muted);
  background: transparent;
  cursor: pointer;
  transition: background .15s, color .15s;
}

.tab-btn.active {
  color: #fff;
  background: var(--c-blue);
}

.updates-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 20px;
}

.update-card {
  display: flex;
  flex-direction: column;
  border-radius: var(--radius);
  border: 1px solid var(--c-border);
  background: #fff;
  box-shadow: var(--shadow-sm);
  color: inherit;
  text-decoration: none;
  overflow: hidden;
  transition: transform .2s ease, box-shadow .2s ease;
}

.update-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-md);
}

.update-thumb {
  aspect-ratio: 16 / 9;
  overflow: hidden;
  background: var(--c-soft);
  flex-shrink: 0;
}

.update-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.update-body {
  flex: 1;
  padding: 20px 20px 0;
}

.update-meta {
  display: flex;
  gap: 12px;
  font-size: 12px;
  font-weight: 700;
  color: #94a3b8;
}

.update-body h3 {
  margin: 10px 0 8px;
  font-size: 18px;
  font-weight: 700;
  line-height: 1.4;
}

.update-body p {
  margin: 0;
  font-size: 14px;
  color: var(--c-muted);
  line-height: 1.75;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.update-tag {
  display: inline-flex;
  align-self: flex-start;
  margin: 16px 20px 20px;
  height: 22px;
  padding: 0 10px;
  border-radius: 999px;
  background: #eff6ff;
  color: #1e40af;
  font-size: 11px;
  font-weight: 800;
}

/* ===== 06 STATS ===== */
.stats-row {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 1px;
  background: var(--c-border);
  border-radius: var(--radius);
  border: 1px solid var(--c-border);
  overflow: hidden;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 44px 24px;
  background: #fff;
  text-align: center;
}

.stat-value {
  font-size: 44px;
  font-weight: 900;
  color: var(--c-blue);
  letter-spacing: -.02em;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: var(--c-muted);
  font-weight: 600;
}

/* ===== 07 CTA ===== */
.cta-banner {
  position: relative;
  border-radius: var(--radius);
  overflow: hidden;
  min-height: 300px;
  display: flex;
  align-items: center;
}

.cta-mask {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(10,25,60,.82) 0%, rgba(29,78,216,.56) 100%);
}

.cta-inner {
  position: relative;
  z-index: 1;
  padding: 64px 72px;
  max-width: 780px;
}

.cta-inner h2 {
  margin: 14px 0 0;
  font-size: clamp(32px, 3vw, 50px);
  font-weight: 900;
  line-height: 1.15;
  color: #fff;
}

.cta-inner p {
  margin: 18px 0 0;
  font-size: 16px;
  color: rgba(255,255,255,.82);
  line-height: 1.8;
}

.cta-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 36px;
}

/* ===== UTIL ===== */
.is-hidden { display: none; }

/* ===== RESPONSIVE ===== */
@media (max-width: 1280px) {
  .cap-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
  .stats-row {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 1100px) {
  .modules-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
  .hero-inner {
    gap: 48px;
  }
}

@media (max-width: 960px) {
  .container {
    width: calc(100% - 32px);
  }
  .hero-inner {
    grid-template-columns: 1fr;
    min-height: auto;
  }
  .hero-lead {
    max-width: 100%;
  }
  .hero-visual {
    min-height: 280px;
  }
  .section-head {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
    margin-bottom: 36px;
  }
  .fellowship-panel {
    grid-template-columns: 1fr;
  }
  .fellowship-visual {
    order: -1;
  }
  .fellowship-visual img {
    min-height: 260px;
  }
  .updates-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .container {
    width: calc(100% - 24px);
  }
  .section {
    padding: 48px 0;
  }
  .hero {
    padding: 52px 0;
  }
  .hero-text h1 {
    font-size: clamp(38px, 10vw, 54px);
  }
  .hero-lead {
    font-size: 16px;
  }
  .hero-checks {
    gap: 10px 20px;
  }
  .modules-grid,
  .cap-grid,
  .updates-grid {
    grid-template-columns: 1fr;
  }
  .stats-row {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
  .fellowship-text {
    padding: 36px 28px;
  }
  .cta-inner {
    padding: 44px 28px;
  }
  .cta-actions,
  .hero-actions,
  .panel-actions {
    flex-direction: column;
  }
  .btn {
    width: 100%;
  }
}
</style>
