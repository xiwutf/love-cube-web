<template>
  <div class="home-wrap">

    <!-- ① HERO BANNER -->
    <section class="home-banner">
      <div class="hc banner-grid">

        <div class="banner-main">
          <p class="banner-kicker">LOVE CUBE PLATFORM · 综合服务平台</p>
          <h1 class="banner-title">Love Cube<br><span class="banner-title-sub">综合服务平台</span></h1>
          <p class="banner-desc">
            提供内容资讯、活动组织、兴趣连接、工具服务与联谊社交等多场景综合服务。
          </p>
          <div class="banner-actions">
            <router-link to="/fellowship-intro" class="btn-primary">进入平台</router-link>
            <router-link to="/about" class="btn-line">了解更多</router-link>
          </div>
          <div class="banner-metrics">
            <span class="bm-item"><strong>3,200+</strong> 注册用户</span>
            <span class="bm-sep">·</span>
            <span class="bm-item"><strong>48</strong> 场活动</span>
            <span class="bm-sep">·</span>
            <span class="bm-item"><strong>120+</strong> 内容文章</span>
          </div>
        </div>

        <div class="banner-panel">
          <div class="bp-block">
            <div class="bp-head">
              <span class="bp-head-title">平台公告</span>
              <router-link to="/announcements" class="bp-more">查看全部 →</router-link>
            </div>
            <ul class="bp-ann">
              <li v-for="item in topAnnouncements" :key="item.id">
                <router-link :to="`/announcements/${item.id}`" class="bpa-link">
                  <span class="bpa-dot"></span>
                  <span class="bpa-title">{{ item.title }}</span>
                  <span class="bpa-date">{{ formatDate(item.publishDate || item.date) }}</span>
                </router-link>
              </li>
              <li v-if="!topAnnouncements.length" v-for="i in 3" :key="'bp'+i">
                <div class="bpa-skel"></div>
              </li>
            </ul>
          </div>
          <div class="bp-divider"></div>
          <div class="bp-block">
            <div class="bp-head">
              <span class="bp-head-title">近期活动</span>
              <router-link to="/events" class="bp-more">全部 →</router-link>
            </div>
            <ul class="bp-ev">
              <li v-for="item in events.slice(0,3)" :key="item.id" class="bpe-row">
                <span class="bpe-dot"></span>
                <span class="bpe-title">{{ item.title }}</span>
                <span class="bpe-date">{{ (item.time || '').slice(0,10) }}</span>
              </li>
              <li v-if="!events.length" v-for="i in 3" :key="'bpe'+i" class="bpe-row bpe-skel"></li>
            </ul>
          </div>
        </div>

      </div>
    </section>

    <!-- ② QUICK SERVICES -->
    <section class="home-services">
      <div class="hc">
        <div class="svc-grid">
          <component
            :is="s.to ? 'router-link' : 'div'"
            v-for="s in services"
            :key="s.label"
            :to="s.to || undefined"
            class="svc-card"
            :class="{ 'svc-card--muted': !s.to }"
          >
            <span class="svc-icon" :style="{ color: s.color }">
              <svg viewBox="0 0 24 24" fill="currentColor" width="30" height="30">
                <path :d="s.path" />
              </svg>
            </span>
            <span class="svc-label">{{ s.label }}</span>
          </component>
        </div>
      </div>
    </section>

    <!-- ③ NEWS 3-COL -->
    <section class="home-section home-section--bg">
      <div class="hc news-grid">

        <!-- 平台动态 -->
        <div>
          <div class="col-head">
            <h2 class="col-title">平台动态</h2>
            <router-link to="/announcements" class="col-more">更多 →</router-link>
          </div>
          <div class="news-list">
            <router-link
              v-for="item in announcements.slice(0,3)"
              :key="item.id"
              :to="`/announcements/${item.id}`"
              class="news-item"
            >
              <span class="ni-date">{{ formatDate(item.publishDate || item.date) }}</span>
              <div class="ni-body">
                <h3 class="ni-title">{{ item.title }}</h3>
                <p class="ni-desc">{{ item.summary }}</p>
              </div>
            </router-link>
            <div v-if="!announcements.length" v-for="i in 3" :key="'ns'+i" class="news-item news-skel"></div>
          </div>
        </div>

        <!-- 通知公告 -->
        <div>
          <div class="col-head">
            <h2 class="col-title">通知公告</h2>
            <router-link to="/announcements" class="col-more">全部 →</router-link>
          </div>
          <ul class="notice-list">
            <li v-for="item in announcements" :key="'n'+item.id">
              <router-link :to="`/announcements/${item.id}`" class="notice-link">
                <span class="notice-dot"></span>
                <span class="notice-title">{{ item.title }}</span>
                <span class="notice-date">{{ formatDate(item.publishDate || item.date) }}</span>
              </router-link>
            </li>
            <li v-if="!announcements.length" v-for="i in 4" :key="'ni'+i" class="notice-skel"></li>
          </ul>
        </div>

        <!-- 活动日历 -->
        <div>
          <div class="col-head">
            <h2 class="col-title">活动日历</h2>
            <router-link to="/events" class="col-more">全部 →</router-link>
          </div>
          <div class="ev-cal-list">
            <router-link
              v-for="item in events"
              :key="'ev'+item.id"
              :to="`/events/${item.id}`"
              class="ev-cal-item"
            >
              <div class="ec-date-box">
                <span class="ec-month">{{ ecMonth(item.time) }}</span>
                <span class="ec-day">{{ ecDay(item.time) }}</span>
              </div>
              <div class="ec-body">
                <p class="ec-title">{{ item.title }}</p>
                <p class="ec-loc">{{ item.location }}</p>
              </div>
            </router-link>
            <div v-if="!events.length" v-for="i in 4" :key="'ecs'+i" class="ev-cal-item ev-cal-skel"></div>
          </div>
        </div>

      </div>
    </section>

    <!-- ④ MODULES (4-col, 8 modules) -->
    <section class="home-section">
      <div class="hc">
        <div class="sec-intro">
          <h2 class="sec-title">服务模块</h2>
          <p class="sec-sub">围绕内容、活动、工具、社交与用户服务构建的综合入口</p>
        </div>
        <div class="mod-grid">
          <component
            :is="m.to ? 'router-link' : 'div'"
            v-for="m in modules"
            :key="m.title"
            :to="m.to || undefined"
            class="mod-card"
            :class="{ 'mod-card--coming': m.coming }"
          >
            <div class="mod-top">
              <div class="mod-icon" :style="{ color: m.color, background: m.bg }">
                <svg viewBox="0 0 24 24" fill="currentColor" width="22" height="22">
                  <path :d="m.path" />
                </svg>
              </div>
              <span v-if="m.coming" class="mod-badge">即将上线</span>
            </div>
            <h3 class="mod-name">{{ m.title }}</h3>
            <p class="mod-desc">{{ m.desc }}</p>
            <span v-if="!m.coming" class="mod-enter">进入 →</span>
          </component>
        </div>
      </div>
    </section>

    <!-- ⑤ ARTICLES -->
    <section class="home-section home-section--bg">
      <div class="hc">
        <div class="sec-intro">
          <div class="sec-intro-left">
            <h2 class="sec-title">精选内容</h2>
            <p class="sec-sub">情感成长、沟通技巧、活动指南、平台教程</p>
          </div>
          <router-link to="/articles" class="col-more">全部文章 →</router-link>
        </div>
        <div class="art-grid">
          <router-link
            v-if="featuredArticle"
            :to="`/articles/${featuredArticle.id}`"
            class="art-featured"
          >
            <span class="af-tag">{{ featuredArticle.tag }}</span>
            <h3 class="af-title">{{ featuredArticle.title }}</h3>
            <p class="af-desc">{{ featuredArticle.summary }}</p>
            <span class="af-cta">阅读全文 →</span>
          </router-link>
          <div v-else class="art-featured art-featured--skel"></div>

          <div class="art-list">
            <router-link
              v-for="item in articleList"
              :key="item.id"
              :to="`/articles/${item.id}`"
              class="art-row"
            >
              <span class="ar-tag">{{ item.tag }}</span>
              <span class="ar-title">{{ item.title }}</span>
              <span class="ar-arrow">→</span>
            </router-link>
            <div v-if="!articleList.length" v-for="i in 5" :key="'ar'+i" class="art-row art-row--skel"></div>
          </div>
        </div>
      </div>
    </section>

    <!-- ⑥ STATS -->
    <section class="home-stats-bar">
      <div class="hc stats-grid">
        <div v-for="s in stats" :key="s.label" class="stat-cell">
          <strong class="stat-num" :style="{ color: s.color }">{{ s.val }}</strong>
          <span class="stat-lbl">{{ s.label }}</span>
        </div>
      </div>
    </section>

    <!-- ⑦ CTA -->
    <section class="home-cta">
      <div class="hc cta-inner">
        <div>
          <h2 class="cta-title">加入 Love Cube，使用多场景综合服务</h2>
          <p class="cta-sub">注册账号，探索联谊社交、活动报名、内容资讯等全部功能</p>
        </div>
        <div class="cta-btns">
          <router-link to="/login" class="btn-primary">立即注册</router-link>
          <router-link to="/about" class="btn-line btn-line--inv">了解平台</router-link>
        </div>
      </div>
    </section>

  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { fetchAnnouncements, fetchArticles, fetchEvents } from '@/api/platformContent.js'

const announcements = ref([])
const articles = ref([])
const events = ref([])

function formatDate(v) {
  if (!v) return ''
  return String(v).replace('T', ' ').slice(0, 10)
}

function ecMonth(t) {
  if (!t) return '--'
  const m = String(t).slice(5, 7)
  const map = { '01':'Jan','02':'Feb','03':'Mar','04':'Apr','05':'May','06':'Jun','07':'Jul','08':'Aug','09':'Sep','10':'Oct','11':'Nov','12':'Dec' }
  return map[m] || m
}

function ecDay(t) {
  if (!t) return '--'
  return String(t).slice(8, 10)
}

onMounted(async () => {
  const [ann, art, ev] = await Promise.allSettled([
    fetchAnnouncements({ status: 'published' }),
    fetchArticles({ status: 'published' }),
    fetchEvents({ status: 'published' })
  ])
  if (ann.status === 'fulfilled') announcements.value = (ann.value || []).slice(0, 5)
  if (art.status === 'fulfilled') articles.value = (art.value || []).slice(0, 7)
  if (ev.status === 'fulfilled') events.value = (ev.value || []).slice(0, 4)
})

const topAnnouncements = computed(() => announcements.value.slice(0, 3))
const featuredArticle = computed(() => articles.value[0] || null)
const articleList = computed(() => articles.value.slice(1, 7))

const PATHS = {
  heart:    'M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z',
  calendar: 'M17 12h-5v5h5v-5zM16 1v2H8V1H6v2H5c-1.11 0-1.99.9-1.99 2L3 19c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2h-1V1h-2zm3 18H5V8h14v11z',
  bell:     'M12 22c1.1 0 2-.9 2-2h-4c0 1.1.9 2 2 2zm6-6v-5c0-3.07-1.64-5.64-4.5-6.32V4c0-.83-.67-1.5-1.5-1.5s-1.5.67-1.5 1.5v.68C7.63 5.36 6 7.92 6 11v5l-2 2v1h16v-1l-2-2z',
  article:  'M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-5 14H7v-2h7v2zm3-4H7v-2h10v2zm0-4H7V7h10v2z',
  build:    'M22.7 19l-9.1-9.1c.9-2.3.4-5-1.5-6.9-2-2-5-2.4-7.4-1.3L9 6 6 9 1.6 4.7C.4 7.1.9 10.1 2.9 12.1c1.9 1.9 4.6 2.4 6.9 1.5l9.1 9.1c.4.4 1 .4 1.4 0l2.3-2.3c.5-.4.5-1.1.1-1.4z',
  person:   'M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z',
  group:    'M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z',
  shield:   'M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4zm-2 16l-4-4 1.41-1.41L10 14.17l6.59-6.59L18 9l-8 8z',
  info:     'M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z'
}

const services = [
  { label: '联谊社交', path: PATHS.heart,    color: '#f45b7a', to: '/fellowship-intro' },
  { label: '活动报名', path: PATHS.calendar,  color: '#1f4fd8', to: '/events' },
  { label: '平台公告', path: PATHS.bell,      color: '#0891b2', to: '/announcements' },
  { label: '资讯中心', path: PATHS.article,   color: '#059669', to: '/articles' },
  { label: '工具服务', path: PATHS.build,     color: '#9ca3af', to: null },
  { label: '个人中心', path: PATHS.person,    color: '#7c3aed', to: '/account' }
]

const modules = [
  { title: '联谊社交', desc: '真实资料认证，高质量社交连接',      to: '/fellowship-intro', path: PATHS.heart,    bg: '#fff0f4', color: '#f45b7a', coming: false },
  { title: '活动中心', desc: '线上线下活动组织与报名参与',          to: '/events',           path: PATHS.calendar, bg: '#eff6ff', color: '#1f4fd8', coming: false },
  { title: '平台公告', desc: '公告通知与平台制度实时发布',          to: '/announcements',    path: PATHS.bell,     bg: '#ecfeff', color: '#0891b2', coming: false },
  { title: '资讯中心', desc: '精选文章、使用指南与干货内容',         to: '/articles',         path: PATHS.article,  bg: '#f0fdf4', color: '#059669', coming: false },
  { title: '兴趣社区', desc: '同城兴趣、爱好圈、交流社群',          to: null,                path: PATHS.group,    bg: '#f5f3ff', color: '#7c3aed', coming: true  },
  { title: '工具服务', desc: '实用功能模块，效率工具集合',           to: null,                path: PATHS.build,    bg: '#fffbeb', color: '#d97706', coming: true  },
  { title: '个人中心', desc: '账号管理、权益设置与个人信息',         to: '/account',          path: PATHS.person,   bg: '#fdf4ff', color: '#9333ea', coming: false },
  { title: '联谊介绍', desc: '了解联谊规则与平台匹配机制',          to: '/fellowship-intro', path: PATHS.info,     bg: '#fff7ed', color: '#ea580c', coming: false }
]

const stats = [
  { val: '3,200+', label: '注册用户', color: '#1f4fd8' },
  { val: '48场',   label: '活动举办', color: '#059669' },
  { val: '1,200+', label: '成功连接', color: '#f45b7a' },
  { val: '120+',   label: '内容文章', color: '#7c3aed' }
]
</script>

<style scoped>
/* ── Variables ── */
.home-wrap {
  --primary: #1f4fd8;
  --primary-light: #eff3ff;
  --accent: #f45b7a;
  --bg: #f5f7fb;
  --card: #ffffff;
  --border: #e5e7eb;
  --text: #111827;
  --text-2: #374151;
  --text-3: #6b7280;
  --text-4: #9ca3af;
  background: var(--bg);
}

/* ── Container ── */
.hc {
  width: min(1600px, calc(100% - 64px));
  margin-left: auto;
  margin-right: auto;
}

/* ── Shared buttons ── */
.btn-primary {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 56px;
  padding: 0 40px;
  border-radius: 8px;
  font-size: 18px;
  font-weight: 700;
  text-decoration: none;
  background: var(--accent);
  color: #fff;
  box-shadow: 0 4px 16px rgba(244, 91, 122, 0.3);
  transition: opacity 0.15s, transform 0.15s;
}
.btn-primary:hover { opacity: 0.88; transform: translateY(-1px); }

.btn-line {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 56px;
  padding: 0 40px;
  border-radius: 8px;
  font-size: 18px;
  font-weight: 700;
  text-decoration: none;
  background: transparent;
  color: var(--text-2);
  border: 1.5px solid var(--border);
  transition: border-color 0.15s, color 0.15s;
}
.btn-line:hover { border-color: var(--primary); color: var(--primary); }
.btn-line--inv { color: rgba(255,255,255,0.85); border-color: rgba(255,255,255,0.35); }
.btn-line--inv:hover { color: #fff; border-color: rgba(255,255,255,0.7); }

/* ── Section shared ── */
.home-section { padding: 72px 0; }
.home-section--bg { background: var(--bg); }

.col-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  gap: 12px;
}
.col-title {
  font-size: 26px;
  font-weight: 900;
  color: var(--text);
  margin: 0;
  padding-left: 12px;
  border-left: 4px solid var(--primary);
  letter-spacing: -0.01em;
}
.col-more {
  font-size: 14px;
  font-weight: 700;
  color: var(--primary);
  text-decoration: none;
  flex-shrink: 0;
}
.col-more:hover { text-decoration: underline; }

.sec-intro {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: 32px;
  gap: 16px;
}
.sec-intro-left { flex: 1; }
.sec-title {
  font-size: clamp(24px, 2.2vw, 32px);
  font-weight: 900;
  color: var(--text);
  margin: 0 0 6px;
  letter-spacing: -0.02em;
}
.sec-sub { font-size: 16px; color: var(--text-3); margin: 0; }

/* ──────────────────── */
/* BANNER               */
/* ──────────────────── */
.home-banner {
  background: var(--card);
  border-bottom: 1px solid var(--border);
  padding: 80px 0 72px;
}

.banner-grid {
  display: grid;
  grid-template-columns: 1.15fr 0.85fr;
  gap: 64px;
  align-items: start;
}

.banner-kicker {
  margin: 0 0 16px;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.18em;
  color: var(--primary);
  text-transform: uppercase;
}

.banner-title {
  margin: 0 0 20px;
  font-size: clamp(48px, 5.5vw, 72px);
  font-weight: 900;
  line-height: 1.05;
  letter-spacing: -0.03em;
  color: var(--text);
}
.banner-title-sub {
  display: block;
  color: var(--primary);
}

.banner-desc {
  margin: 0 0 36px;
  font-size: 20px;
  color: var(--text-3);
  line-height: 1.8;
  max-width: 600px;
}

.banner-actions {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  margin-bottom: 36px;
}

.banner-metrics {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 17px;
  color: var(--text-3);
  flex-wrap: wrap;
}
.bm-item strong { color: var(--text); font-weight: 900; }
.bm-sep { color: var(--text-4); }

/* Banner info panel */
.banner-panel {
  background: var(--bg);
  border: 1px solid var(--border);
  border-radius: 16px;
  overflow: hidden;
  min-height: 360px;
}
.bp-block { padding: 24px 24px 20px; }
.bp-divider { height: 1px; background: var(--border); }

.bp-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.bp-head-title {
  font-size: 18px;
  font-weight: 800;
  color: var(--text);
  padding-left: 10px;
  border-left: 3px solid var(--primary);
}
.bp-more { font-size: 13px; font-weight: 600; color: var(--primary); text-decoration: none; }
.bp-more:hover { text-decoration: underline; }

.bp-ann { list-style: none; margin: 0; padding: 0; display: flex; flex-direction: column; gap: 0; }
.bpa-link {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 0;
  border-bottom: 1px solid var(--border);
  text-decoration: none;
  color: inherit;
}
.bp-ann li:last-child .bpa-link { border-bottom: none; }
.bpa-dot { width: 6px; height: 6px; border-radius: 50%; background: var(--primary); flex-shrink: 0; }
.bpa-title { flex: 1; font-size: 15px; color: var(--text-2); overflow: hidden; white-space: nowrap; text-overflow: ellipsis; transition: color 0.12s; }
.bpa-link:hover .bpa-title { color: var(--primary); }
.bpa-date { font-size: 13px; color: var(--text-4); flex-shrink: 0; }
.bpa-skel { height: 24px; background: #e5e7eb; border-radius: 4px; animation: skel 1.6s infinite; margin: 8px 0; }

.bp-ev { list-style: none; margin: 0; padding: 0; display: flex; flex-direction: column; gap: 0; }
.bpe-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 13px 0;
  border-bottom: 1px solid var(--border);
}
.bp-ev li:last-child.bpe-row { border-bottom: none; }
.bpe-dot { width: 6px; height: 6px; border-radius: 50%; background: #059669; flex-shrink: 0; }
.bpe-title { flex: 1; font-size: 15px; color: var(--text-2); overflow: hidden; white-space: nowrap; text-overflow: ellipsis; }
.bpe-date { font-size: 13px; color: var(--text-4); flex-shrink: 0; }
.bpe-skel { height: 24px; background: #e5e7eb; border-radius: 4px; animation: skel 1.6s infinite; }

/* ──────────────────── */
/* QUICK SERVICES       */
/* ──────────────────── */
.home-services {
  background: var(--card);
  border-bottom: 1px solid var(--border);
  padding: 0;
}

.svc-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
}

.svc-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  height: 112px;
  text-decoration: none;
  color: inherit;
  border-right: 1px solid var(--border);
  transition: background 0.15s;
  cursor: pointer;
}
.svc-card:last-child { border-right: none; }
.svc-card:hover:not(.svc-card--muted) { background: var(--bg); }
.svc-card--muted { cursor: default; opacity: 0.45; }

.svc-icon { line-height: 0; }
.svc-label { font-size: 17px; font-weight: 700; color: var(--text-2); }
.svc-card--muted .svc-label { color: var(--text-4); }

/* ──────────────────── */
/* NEWS 3-COL           */
/* ──────────────────── */
.news-grid {
  display: grid;
  grid-template-columns: 1.4fr 1fr 1fr;
  gap: 48px;
  align-items: start;
}

.news-list { display: flex; flex-direction: column; }

.news-item {
  display: grid;
  grid-template-columns: 96px 1fr;
  gap: 16px;
  padding: 20px 0;
  border-bottom: 1px solid var(--border);
  text-decoration: none;
  color: inherit;
  align-items: start;
  transition: background 0.1s;
}
.news-item:first-child { border-top: 1px solid var(--border); }
.news-item:hover { background: rgba(31,79,216,0.02); }
.news-skel { height: 88px; border-radius: 6px; background: linear-gradient(90deg, #f1f5f9 25%, #e8edf5 50%, #f1f5f9 75%); background-size: 200% 100%; animation: skel 1.6s infinite; margin: 6px 0; }

.ni-date { font-size: 13px; color: var(--text-4); font-weight: 600; padding-top: 4px; }
.ni-title { font-size: 17px; font-weight: 700; color: var(--text); margin: 0 0 7px; line-height: 1.4; transition: color 0.12s; }
.news-item:hover .ni-title { color: var(--primary); }
.ni-desc { font-size: 15px; color: var(--text-3); line-height: 1.7; margin: 0; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }

.notice-list { list-style: none; margin: 0; padding: 0; }
.notice-list li { border-bottom: 1px solid var(--border); }
.notice-list li:first-child { border-top: 1px solid var(--border); }
.notice-skel { height: 52px; background: linear-gradient(90deg, #f1f5f9 25%, #e8edf5 50%, #f1f5f9 75%); background-size: 200% 100%; animation: skel 1.6s infinite; margin: 4px 0; border-radius: 4px; }

.notice-link {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px 0;
  text-decoration: none;
  color: inherit;
}
.notice-dot { width: 5px; height: 5px; border-radius: 50%; background: var(--primary); flex-shrink: 0; }
.notice-title { flex: 1; font-size: 15px; color: var(--text-2); overflow: hidden; white-space: nowrap; text-overflow: ellipsis; transition: color 0.12s; }
.notice-link:hover .notice-title { color: var(--primary); }
.notice-date { font-size: 13px; color: var(--text-4); flex-shrink: 0; }

.ev-cal-list { display: flex; flex-direction: column; }
.ev-cal-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 0;
  border-bottom: 1px solid var(--border);
  text-decoration: none;
  color: inherit;
  transition: background 0.1s;
}
.ev-cal-item:first-child { border-top: 1px solid var(--border); }
.ev-cal-item:hover .ec-title { color: var(--primary); }
.ev-cal-skel { height: 68px; border-radius: 6px; background: linear-gradient(90deg, #f1f5f9 25%, #e8edf5 50%, #f1f5f9 75%); background-size: 200% 100%; animation: skel 1.6s infinite; margin: 4px 0; }

.ec-date-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 52px;
  height: 52px;
  background: var(--primary-light);
  border-radius: 10px;
  flex-shrink: 0;
}
.ec-month { font-size: 11px; font-weight: 700; color: var(--primary); text-transform: uppercase; line-height: 1; }
.ec-day { font-size: 22px; font-weight: 900; color: var(--primary); line-height: 1.2; }

.ec-title { font-size: 15px; font-weight: 700; color: var(--text); margin: 0 0 4px; transition: color 0.12s; }
.ec-loc { font-size: 13px; color: var(--text-4); margin: 0; }

/* ──────────────────── */
/* MODULES 4-COL        */
/* ──────────────────── */
.mod-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.mod-card {
  background: var(--card);
  border: 1px solid var(--border);
  border-radius: 14px;
  padding: 28px 24px;
  min-height: 220px;
  text-decoration: none;
  color: inherit;
  display: flex;
  flex-direction: column;
  gap: 12px;
  box-shadow: 0 1px 4px rgba(15,23,42,0.06);
  transition: transform 0.18s, box-shadow 0.18s, border-color 0.18s;
}
.mod-card:not(.mod-card--coming):hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 32px rgba(15,23,42,0.1);
  border-color: #c7d3ea;
}
.mod-card--coming { opacity: 0.6; cursor: default; }

.mod-top { display: flex; align-items: flex-start; justify-content: space-between; }
.mod-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.mod-badge { font-size: 11px; font-weight: 700; color: var(--text-4); background: #f1f5f9; border-radius: 6px; padding: 3px 8px; }
.mod-name { font-size: 20px; font-weight: 900; color: var(--text); margin: 0; letter-spacing: -0.01em; }
.mod-desc { font-size: 14px; color: var(--text-3); line-height: 1.7; margin: 0; flex: 1; }
.mod-enter { font-size: 14px; font-weight: 700; color: var(--primary); align-self: flex-start; margin-top: 4px; }

/* ──────────────────── */
/* ARTICLES             */
/* ──────────────────── */
.art-grid {
  display: grid;
  grid-template-columns: 1.2fr 1fr;
  gap: 28px;
  align-items: start;
}

.art-featured {
  background: var(--card);
  border: 1px solid var(--border);
  border-radius: 14px;
  padding: 36px 32px;
  text-decoration: none;
  color: inherit;
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 280px;
  border-left: 5px solid var(--primary);
  box-shadow: 0 1px 4px rgba(15,23,42,0.06);
  transition: box-shadow 0.18s;
}
.art-featured:hover { box-shadow: 0 10px 32px rgba(15,23,42,0.1); }
.art-featured--skel { background: linear-gradient(90deg, #f1f5f9 25%, #e8edf5 50%, #f1f5f9 75%); background-size: 200% 100%; animation: skel 1.6s infinite; }

.af-tag { font-size: 12px; font-weight: 700; color: var(--primary); background: var(--primary-light); padding: 4px 10px; border-radius: 5px; align-self: flex-start; }
.af-title { font-size: 24px; font-weight: 900; color: var(--text); margin: 0; line-height: 1.4; letter-spacing: -0.01em; }
.af-desc { font-size: 16px; color: var(--text-3); line-height: 1.75; margin: 0; flex: 1; }
.af-cta { font-size: 15px; font-weight: 700; color: var(--primary); align-self: flex-start; }

.art-list {
  background: var(--card);
  border: 1px solid var(--border);
  border-radius: 14px;
  overflow: hidden;
}

.art-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 18px 22px;
  border-bottom: 1px solid var(--border);
  text-decoration: none;
  color: inherit;
  transition: background 0.12s;
}
.art-row:last-child { border-bottom: none; }
.art-row:hover { background: var(--bg); }
.art-row--skel { height: 58px; background: linear-gradient(90deg, #f1f5f9 25%, #e8edf5 50%, #f1f5f9 75%); background-size: 200% 100%; animation: skel 1.6s infinite; }

.ar-tag { flex-shrink: 0; font-size: 12px; font-weight: 700; color: var(--primary); background: var(--primary-light); padding: 3px 8px; border-radius: 4px; }
.ar-title { flex: 1; font-size: 15px; color: var(--text-2); overflow: hidden; white-space: nowrap; text-overflow: ellipsis; transition: color 0.12s; }
.art-row:hover .ar-title { color: var(--primary); }
.ar-arrow { font-size: 15px; color: var(--text-4); flex-shrink: 0; }

/* ──────────────────── */
/* STATS BAR            */
/* ──────────────────── */
.home-stats-bar {
  background: var(--card);
  border-top: 1px solid var(--border);
  border-bottom: 1px solid var(--border);
  padding: 48px 0;
}
.stats-grid { display: grid; grid-template-columns: repeat(4, 1fr); }
.stat-cell { display: flex; flex-direction: column; align-items: center; gap: 8px; padding: 8px 0; border-right: 1px solid var(--border); }
.stat-cell:last-child { border-right: none; }
.stat-num { font-size: clamp(32px, 3.2vw, 52px); font-weight: 900; letter-spacing: -0.03em; line-height: 1; }
.stat-lbl { font-size: 16px; color: var(--text-3); font-weight: 600; }

/* ──────────────────── */
/* CTA                  */
/* ──────────────────── */
.home-cta {
  background: linear-gradient(135deg, #0f172a 0%, #1e3a5f 100%);
  padding: 72px 0;
}
.cta-inner { display: flex; align-items: center; justify-content: space-between; gap: 40px; flex-wrap: wrap; }
.cta-title { font-size: clamp(22px, 2.4vw, 34px); font-weight: 900; color: #fff; margin: 0; letter-spacing: -0.02em; }
.cta-sub { font-size: 16px; color: #94a3b8; margin: 10px 0 0; }
.cta-btns { display: flex; gap: 16px; flex-shrink: 0; flex-wrap: wrap; }

/* ──────────────────── */
/* SKELETON             */
/* ──────────────────── */
@keyframes skel {
  0%   { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

/* ──────────────────── */
/* RESPONSIVE           */
/* ──────────────────── */
@media (max-width: 1439px) {
  .hc { width: calc(100% - 64px); }
  .news-grid { gap: 32px; }
  .mod-grid { grid-template-columns: repeat(4, 1fr); gap: 16px; }
}

@media (max-width: 1199px) {
  .hc { width: calc(100% - 48px); }
  .banner-grid { gap: 40px; }
  .mod-grid { grid-template-columns: repeat(2, 1fr); }
  .news-grid { grid-template-columns: 1fr 1fr; }
  .news-grid > div:nth-child(3) { grid-column: 1 / -1; }
  .ev-cal-list { display: grid; grid-template-columns: repeat(2, 1fr); gap: 0 24px; }
}

@media (max-width: 1023px) {
  .banner-grid { grid-template-columns: 1fr; gap: 32px; }
  .banner-desc { font-size: 18px; }
  .home-section { padding: 52px 0; }
  .svc-grid { grid-template-columns: repeat(3, 1fr); }
  .svc-card { height: 96px; }
  .svc-card:nth-child(3) { border-right: none; }
  .svc-card:nth-child(4) { border-top: 1px solid var(--border); }
  .svc-card:nth-child(5) { border-top: 1px solid var(--border); }
  .svc-card:nth-child(6) { border-top: 1px solid var(--border); border-right: none; }
  .news-grid { grid-template-columns: 1fr; gap: 36px; }
  .art-grid { grid-template-columns: 1fr; }
  .cta-inner { flex-direction: column; align-items: flex-start; }
}

@media (max-width: 767px) {
  .hc { width: calc(100% - 32px); }
  .home-banner { padding: 48px 0 40px; }
  .banner-title { font-size: clamp(38px, 9vw, 56px); }
  .banner-desc { font-size: 16px; }
  .banner-actions { flex-direction: column; gap: 10px; }
  .banner-actions .btn-primary,
  .banner-actions .btn-line { width: 100%; }
  .btn-primary, .btn-line { height: 48px; font-size: 16px; padding: 0 28px; }
  .home-section { padding: 40px 0; }
  .col-title { font-size: 20px; }
  .svc-grid { grid-template-columns: repeat(3, 1fr); }
  .svc-card { height: 80px; gap: 6px; }
  .svc-label { font-size: 13px; }
  .mod-grid { grid-template-columns: repeat(2, 1fr); gap: 12px; }
  .mod-card { padding: 20px 16px; min-height: unset; }
  .mod-name { font-size: 16px; }
  .home-stats-bar { padding: 32px 0; }
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
  .stat-cell:nth-child(2) { border-right: none; }
  .stat-cell:nth-child(3) { border-top: 1px solid var(--border); }
  .stat-cell:nth-child(4) { border-top: 1px solid var(--border); border-right: none; }
  .home-cta { padding: 48px 0; }
  .cta-btns { width: 100%; }
  .cta-btns .btn-primary, .cta-btns .btn-line--inv { flex: 1; }
  .ev-cal-list { grid-template-columns: 1fr; }
}
</style>
