<template>
  <div class="home-wrap">

    <!-- ① HERO -->
    <section class="home-hero">
      <div class="hc hero-grid">
        <div class="hero-content">
          <p class="hero-kicker">LOVE CUBE PLATFORM</p>
          <h1 class="hero-title">
            Love Cube<br>
            <span class="hero-accent">多元连接平台</span>
          </h1>
          <p class="hero-sub">
            集社交互动、活动组织、内容资讯、兴趣连接、效率服务于一体的综合平台。
          </p>
          <div class="hero-actions">
            <router-link to="/fellowship-intro" class="btn-primary">立即体验</router-link>
            <router-link to="/about" class="btn-ghost">了解平台</router-link>
          </div>
          <div class="hero-tags">
            <span class="htag">真实认证</span>
            <span class="htag">活动组织</span>
            <span class="htag">内容资讯</span>
            <span class="htag">兴趣社区</span>
          </div>
        </div>

        <div class="hero-panel" aria-hidden="true">
          <div class="hp-titlebar">
            <div class="hp-dots">
              <i class="hp-dot hp-dot--r"></i>
              <i class="hp-dot hp-dot--y"></i>
              <i class="hp-dot hp-dot--g"></i>
            </div>
            <span class="hp-label">Love Cube Platform</span>
          </div>
          <div class="hp-mods">
            <div class="hp-mod" v-for="m in panelModules" :key="m.name" :style="{ '--mc': m.color }">
              <span class="hp-mod-dot"></span>
              <span class="hp-mod-name">{{ m.name }}</span>
              <span v-if="m.coming" class="hp-mod-soon">soon</span>
            </div>
          </div>
          <div class="hp-divider"></div>
          <div class="hp-stats">
            <div class="hp-stat" v-for="s in panelStats" :key="s.label">
              <strong>{{ s.val }}</strong>
              <span>{{ s.label }}</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- ② STATS BAR -->
    <div class="home-stats-bar">
      <div class="hc stats-row">
        <div class="stat-cell" v-for="s in stats" :key="s.label">
          <strong class="stat-num" :style="{ color: s.color }">{{ s.val }}</strong>
          <span class="stat-lbl">{{ s.label }}</span>
        </div>
      </div>
    </div>

    <!-- ③ FEATURE MATRIX -->
    <section class="home-section" id="modules">
      <div class="hc">
        <div class="sec-head">
          <div>
            <p class="sec-kicker">MODULES</p>
            <h2 class="sec-title">平台功能矩阵</h2>
            <p class="sec-sub">六大核心模块，覆盖社交、活动、内容、工具全场景</p>
          </div>
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
            <div class="mod-icon" :style="{ background: m.bg, color: m.color }">
              <svg viewBox="0 0 24 24" fill="currentColor" width="22" height="22">
                <path :d="m.path" />
              </svg>
            </div>
            <h3 class="mod-name">{{ m.title }}</h3>
            <p class="mod-desc">{{ m.desc }}</p>
            <span v-if="m.coming" class="mod-badge">即将上线</span>
            <span v-else class="mod-arrow">›</span>
          </component>
        </div>
      </div>
    </section>

    <!-- ④ PLATFORM UPDATES -->
    <section class="home-section home-section--alt">
      <div class="hc">
        <div class="sec-head">
          <div>
            <p class="sec-kicker">UPDATES</p>
            <h2 class="sec-title">平台动态</h2>
            <p class="sec-sub">公告、新功能上线与运营动态持续同步</p>
          </div>
          <router-link to="/announcements" class="sec-more">查看全部 ›</router-link>
        </div>
        <div class="card-grid-3">
          <router-link
            v-for="item in announcements"
            :key="item.id"
            :to="`/announcements/${item.id}`"
            class="info-card"
          >
            <p class="ic-date">{{ formatDate(item.publishDate || item.date) }}</p>
            <h3 class="ic-title">{{ item.title }}</h3>
            <p class="ic-desc">{{ item.summary }}</p>
          </router-link>
          <template v-if="!announcements.length">
            <div class="info-card info-card--skel" v-for="i in 3" :key="'as' + i"></div>
          </template>
        </div>
      </div>
    </section>

    <!-- ⑤ FEATURED CONTENT -->
    <section class="home-section">
      <div class="hc">
        <div class="sec-head">
          <div>
            <p class="sec-kicker">CONTENT</p>
            <h2 class="sec-title">精选内容</h2>
            <p class="sec-sub">情感成长、沟通技巧、活动指南、平台教程</p>
          </div>
          <router-link to="/articles" class="sec-more">查看全部 ›</router-link>
        </div>
        <div class="card-grid-3">
          <router-link
            v-for="item in articles"
            :key="item.id"
            :to="`/articles/${item.id}`"
            class="info-card info-card--article"
          >
            <span class="ic-tag">{{ item.tag }}</span>
            <h3 class="ic-title">{{ item.title }}</h3>
            <p class="ic-desc">{{ item.summary }}</p>
          </router-link>
          <template v-if="!articles.length">
            <div class="info-card info-card--skel" v-for="i in 3" :key="'rs' + i"></div>
          </template>
        </div>
      </div>
    </section>

    <!-- ⑥ HOT MODULES -->
    <section class="home-section home-section--alt">
      <div class="hc">
        <div class="sec-head">
          <div>
            <p class="sec-kicker">EXPLORE</p>
            <h2 class="sec-title">热门推荐</h2>
          </div>
        </div>
        <div class="hot-grid">
          <router-link to="/fellowship-intro" class="hot-card hot-card--pink">
            <p class="hot-kicker">FELLOWSHIP</p>
            <h3 class="hot-title">联谊社交</h3>
            <p class="hot-desc">真实资料认证，发现高质量连接机会</p>
            <span class="hot-cta">进入联谊 →</span>
          </router-link>
          <router-link to="/events" class="hot-card hot-card--blue">
            <p class="hot-kicker">EVENTS</p>
            <h3 class="hot-title">活动专区</h3>
            <p class="hot-desc">线上线下活动，与有趣的人共同参与</p>
            <span class="hot-cta">浏览活动 →</span>
          </router-link>
          <div class="hot-card hot-card--navy">
            <p class="hot-kicker">COMING SOON</p>
            <h3 class="hot-title">工具专区</h3>
            <p class="hot-desc">实用效率工具，持续建设中，敬请期待</p>
            <span class="hot-cta hot-cta--muted">即将上线</span>
          </div>
        </div>
      </div>
    </section>

    <!-- ⑦ CTA -->
    <section class="home-cta">
      <div class="hc cta-inner">
        <div>
          <h2 class="cta-title">加入 Love Cube，开始多元连接</h2>
          <p class="cta-sub">已有 3,200+ 用户在这里建立了真实的连接</p>
        </div>
        <div class="cta-btns">
          <router-link to="/login" class="btn-primary">立即加入</router-link>
          <router-link to="/about" class="btn-ghost btn-ghost--inv">了解平台</router-link>
        </div>
      </div>
    </section>

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { fetchAnnouncements, fetchArticles } from '@/api/platformContent.js'

const announcements = ref([])
const articles = ref([])

function formatDate(v) {
  if (!v) return ''
  return String(v).replace('T', ' ').slice(0, 10)
}

onMounted(async () => {
  const [ann, art] = await Promise.allSettled([
    fetchAnnouncements({ status: 'published' }),
    fetchArticles({ status: 'published' })
  ])
  if (ann.status === 'fulfilled') announcements.value = (ann.value || []).slice(0, 3)
  if (art.status === 'fulfilled') articles.value = (art.value || []).slice(0, 3)
})

const panelModules = [
  { name: '联谊社交', color: '#ff5f84', coming: false },
  { name: '活动专区', color: '#3b82f6', coming: false },
  { name: '资讯中心', color: '#10b981', coming: false },
  { name: '兴趣社区', color: '#8b5cf6', coming: true },
  { name: '工具服务', color: '#f59e0b', coming: true },
  { name: '会员中心', color: '#ec4899', coming: false }
]

const panelStats = [
  { val: '3,200+', label: '用户' },
  { val: '48', label: '活动' },
  { val: '120+', label: '文章' }
]

const stats = [
  { val: '3,200+', label: '注册用户', color: '#ff5f84' },
  { val: '48场', label: '活动举办', color: '#3b82f6' },
  { val: '1,200+', label: '成功连接', color: '#10b981' },
  { val: '120+', label: '内容文章', color: '#8b5cf6' }
]

const PATHS = {
  heart: 'M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z',
  calendar: 'M17 12h-5v5h5v-5zM16 1v2H8V1H6v2H5c-1.11 0-1.99.9-1.99 2L3 19c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2h-1V1h-2zm3 18H5V8h14v11z',
  article: 'M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-5 14H7v-2h7v2zm3-4H7v-2h10v2zm0-4H7V7h10v2z',
  group: 'M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z',
  build: 'M22.7 19l-9.1-9.1c.9-2.3.4-5-1.5-6.9-2-2-5-2.4-7.4-1.3L9 6 6 9 1.6 4.7C.4 7.1.9 10.1 2.9 12.1c1.9 1.9 4.6 2.4 6.9 1.5l9.1 9.1c.4.4 1 .4 1.4 0l2.3-2.3c.5-.4.5-1.1.1-1.4z',
  shield: 'M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4zm-2 16l-4-4 1.41-1.41L10 14.17l6.59-6.59L18 9l-8 8z'
}

const modules = [
  {
    title: '联谊社交',
    desc: '真实资料认证，高质量社交连接',
    to: '/fellowship-intro',
    path: PATHS.heart,
    bg: '#fff0f4',
    color: '#ff5f84',
    coming: false
  },
  {
    title: '活动专区',
    desc: '线上线下活动组织与报名',
    to: '/events',
    path: PATHS.calendar,
    bg: '#eff6ff',
    color: '#3b82f6',
    coming: false
  },
  {
    title: '资讯中心',
    desc: '平台内容与精选干货文章',
    to: '/articles',
    path: PATHS.article,
    bg: '#f0fdf4',
    color: '#10b981',
    coming: false
  },
  {
    title: '兴趣社区',
    desc: '同城兴趣、爱好圈、交流社群',
    to: null,
    path: PATHS.group,
    bg: '#f5f3ff',
    color: '#8b5cf6',
    coming: true
  },
  {
    title: '工具服务',
    desc: '实用功能模块，效率工具集',
    to: null,
    path: PATHS.build,
    bg: '#fffbeb',
    color: '#f59e0b',
    coming: true
  },
  {
    title: '会员中心',
    desc: '成长权益体系，专属特权服务',
    to: '/account',
    path: PATHS.shield,
    bg: '#fdf2f8',
    color: '#ec4899',
    coming: false
  }
]
</script>

<style scoped>
/* ── Container ── */
.hc {
  width: min(1200px, calc(100% - 48px));
  margin-left: auto;
  margin-right: auto;
}

/* ── Shared buttons ── */
.btn-primary {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 13px 28px;
  border-radius: 999px;
  font-size: 15px;
  font-weight: 700;
  text-decoration: none;
  background: linear-gradient(135deg, #ff5f84, #e84f73);
  color: #fff;
  box-shadow: 0 12px 24px rgba(255, 95, 132, 0.3);
  transition: transform 0.18s, box-shadow 0.18s;
}
.btn-primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 16px 30px rgba(232, 79, 115, 0.38);
}

.btn-ghost {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 13px 28px;
  border-radius: 999px;
  font-size: 15px;
  font-weight: 700;
  text-decoration: none;
  background: #fff;
  color: #334155;
  border: 1.5px solid #e2e8f0;
  transition: border-color 0.18s, color 0.18s;
}
.btn-ghost:hover {
  border-color: #ffd0db;
  color: #ff5f84;
}
.btn-ghost--inv {
  background: rgba(255,255,255,0.15);
  color: #fff;
  border-color: rgba(255,255,255,0.35);
}
.btn-ghost--inv:hover {
  background: rgba(255,255,255,0.25);
  color: #fff;
  border-color: rgba(255,255,255,0.6);
}

/* ── Section shared ── */
.home-section {
  padding: 64px 0;
}
.home-section--alt {
  background: #f8fafc;
}

.sec-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: 36px;
  gap: 16px;
}
.sec-kicker {
  margin: 0;
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.14em;
  color: #ff5f84;
  text-transform: uppercase;
}
.sec-title {
  margin: 6px 0 0;
  font-size: clamp(22px, 2.8vw, 30px);
  font-weight: 800;
  color: #0f172a;
  letter-spacing: -0.02em;
}
.sec-sub {
  margin: 6px 0 0;
  font-size: 14px;
  color: #64748b;
}
.sec-more {
  flex-shrink: 0;
  font-size: 14px;
  font-weight: 700;
  color: #ff5f84;
  text-decoration: none;
  padding-bottom: 2px;
  border-bottom: 1.5px solid transparent;
  transition: border-color 0.15s;
}
.sec-more:hover {
  border-color: #ff5f84;
}

/* ──────────────────────────────── */
/* HERO                             */
/* ──────────────────────────────── */
.home-hero {
  padding: 80px 0 72px;
  background: linear-gradient(160deg, #fff0f4 0%, #f8faff 50%, #f0f7ff 100%);
}

.hero-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 56px;
  align-items: center;
}

.hero-kicker {
  margin: 0;
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.14em;
  color: #ff5f84;
  text-transform: uppercase;
}

.hero-title {
  margin: 14px 0 0;
  font-size: clamp(38px, 4.8vw, 62px);
  font-weight: 800;
  line-height: 1.1;
  letter-spacing: -0.03em;
  color: #0f172a;
}

.hero-accent {
  display: block;
  background: linear-gradient(135deg, #ff5f84 0%, #3b82f6 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.hero-sub {
  margin: 18px 0 0;
  font-size: 16px;
  color: #475569;
  line-height: 1.8;
  max-width: 460px;
}

.hero-actions {
  margin-top: 28px;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.hero-tags {
  margin-top: 24px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.htag {
  font-size: 12px;
  font-weight: 700;
  color: #475569;
  background: rgba(255,255,255,0.85);
  border: 1px solid #e2e8f0;
  border-radius: 999px;
  padding: 5px 12px;
}

/* ── Hero Panel (right side) ── */
.hero-panel {
  background: #0f172a;
  border-radius: 20px;
  padding: 20px;
  box-shadow: 0 32px 64px rgba(15, 23, 42, 0.2), 0 0 0 1px rgba(255,255,255,0.06);
}

.hp-titlebar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
}
.hp-dots {
  display: flex;
  gap: 6px;
}
.hp-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  display: block;
}
.hp-dot--r { background: #ff5f57; }
.hp-dot--y { background: #febc2e; }
.hp-dot--g { background: #28c840; }
.hp-label {
  font-size: 12px;
  color: #475569;
  font-weight: 600;
  letter-spacing: 0.02em;
  flex: 1;
  text-align: center;
  margin-right: 36px;
}

.hp-mods {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  margin-bottom: 20px;
}
.hp-mod {
  background: rgba(255,255,255,0.05);
  border: 1px solid rgba(255,255,255,0.08);
  border-radius: 12px;
  padding: 12px 10px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  position: relative;
}
.hp-mod-dot {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  background: var(--mc);
  opacity: 0.85;
}
.hp-mod-name {
  font-size: 11px;
  color: rgba(255,255,255,0.75);
  font-weight: 600;
  text-align: center;
  line-height: 1.3;
}
.hp-mod-soon {
  position: absolute;
  top: 6px;
  right: 6px;
  font-size: 9px;
  font-weight: 700;
  color: #f59e0b;
  background: rgba(245,158,11,0.15);
  border: 1px solid rgba(245,158,11,0.3);
  border-radius: 4px;
  padding: 1px 4px;
  letter-spacing: 0.04em;
}

.hp-divider {
  height: 1px;
  background: rgba(255,255,255,0.08);
  margin-bottom: 16px;
}

.hp-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 4px;
}
.hp-stat {
  text-align: center;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.hp-stat strong {
  font-size: 15px;
  font-weight: 800;
  color: #fff;
}
.hp-stat span {
  font-size: 11px;
  color: #64748b;
}

/* ──────────────────────────────── */
/* STATS BAR                        */
/* ──────────────────────────────── */
.home-stats-bar {
  background: #fff;
  border-top: 1px solid #e2e8f0;
  border-bottom: 1px solid #e2e8f0;
  padding: 28px 0;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 0;
}

.stat-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 8px 0;
  border-right: 1px solid #e2e8f0;
}
.stat-cell:last-child {
  border-right: none;
}
.stat-num {
  font-size: clamp(22px, 2.4vw, 32px);
  font-weight: 800;
  letter-spacing: -0.02em;
  line-height: 1;
}
.stat-lbl {
  font-size: 13px;
  color: #64748b;
  font-weight: 600;
}

/* ──────────────────────────────── */
/* FEATURE MATRIX                   */
/* ──────────────────────────────── */
.mod-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.mod-card {
  background: #fff;
  border: 1px solid #e8edf5;
  border-radius: 18px;
  padding: 28px 24px;
  text-decoration: none;
  color: inherit;
  display: block;
  position: relative;
  box-shadow: 0 2px 12px rgba(15, 23, 42, 0.04);
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}
.mod-card:not(.mod-card--coming):hover {
  transform: translateY(-3px);
  box-shadow: 0 14px 36px rgba(15, 23, 42, 0.1);
  border-color: #dde3ed;
}
.mod-card--coming {
  opacity: 0.72;
  cursor: default;
}

.mod-icon {
  width: 46px;
  height: 46px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
}
.mod-name {
  font-size: 17px;
  font-weight: 800;
  color: #0f172a;
  margin: 0 0 8px;
}
.mod-desc {
  font-size: 13px;
  color: #64748b;
  line-height: 1.65;
  margin: 0;
}
.mod-badge {
  position: absolute;
  top: 16px;
  right: 16px;
  font-size: 11px;
  font-weight: 700;
  color: #94a3b8;
  background: #f1f5f9;
  border-radius: 999px;
  padding: 3px 8px;
}
.mod-arrow {
  position: absolute;
  top: 20px;
  right: 20px;
  font-size: 18px;
  color: #cbd5e1;
  transition: color 0.15s, transform 0.15s;
}
.mod-card:hover .mod-arrow {
  color: #ff5f84;
  transform: translateX(2px);
}

/* ──────────────────────────────── */
/* INFO CARDS (announcements/articles) */
/* ──────────────────────────────── */
.card-grid-3 {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.info-card {
  background: #fff;
  border: 1px solid #e8edf5;
  border-radius: 16px;
  padding: 24px 20px;
  text-decoration: none;
  color: inherit;
  display: flex;
  flex-direction: column;
  gap: 10px;
  box-shadow: 0 2px 12px rgba(15, 23, 42, 0.04);
  transition: transform 0.18s, box-shadow 0.18s;
}
.info-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 28px rgba(15, 23, 42, 0.09);
}
.info-card--skel {
  min-height: 140px;
  background: linear-gradient(90deg, #f1f5f9 25%, #e8edf5 50%, #f1f5f9 75%);
  background-size: 200% 100%;
  animation: skel 1.6s infinite;
}
@keyframes skel {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

.ic-date {
  margin: 0;
  font-size: 12px;
  color: #94a3b8;
  font-weight: 600;
}
.ic-tag {
  display: inline-block;
  font-size: 11px;
  font-weight: 700;
  color: #3b82f6;
  background: #eff6ff;
  border-radius: 6px;
  padding: 3px 8px;
  align-self: flex-start;
}
.ic-title {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: #0f172a;
  line-height: 1.45;
}
.ic-desc {
  margin: 0;
  font-size: 13px;
  color: #64748b;
  line-height: 1.65;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* ──────────────────────────────── */
/* HOT MODULES                      */
/* ──────────────────────────────── */
.hot-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.hot-card {
  border-radius: 20px;
  padding: 32px 28px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  text-decoration: none;
  color: #fff;
  min-height: 220px;
  position: relative;
  overflow: hidden;
  transition: transform 0.2s ease;
}
.hot-card:hover {
  transform: translateY(-3px);
}
.hot-card::before {
  content: '';
  position: absolute;
  inset: 0;
  background: inherit;
  opacity: 1;
}

.hot-card--pink {
  background: linear-gradient(145deg, #ff5f84 0%, #e84f73 100%);
  box-shadow: 0 20px 48px rgba(255, 95, 132, 0.28);
}
.hot-card--blue {
  background: linear-gradient(145deg, #3b82f6 0%, #1d4ed8 100%);
  box-shadow: 0 20px 48px rgba(59, 130, 246, 0.28);
}
.hot-card--navy {
  background: linear-gradient(145deg, #334155 0%, #1e293b 100%);
  box-shadow: 0 20px 48px rgba(30, 41, 59, 0.28);
  cursor: default;
}

.hot-kicker {
  margin: 0;
  font-size: 10px;
  font-weight: 800;
  letter-spacing: 0.14em;
  color: rgba(255,255,255,0.6);
  text-transform: uppercase;
  position: relative;
}
.hot-title {
  margin: 0;
  font-size: 22px;
  font-weight: 800;
  color: #fff;
  letter-spacing: -0.01em;
  position: relative;
}
.hot-desc {
  margin: 0;
  font-size: 13px;
  color: rgba(255,255,255,0.75);
  line-height: 1.7;
  flex: 1;
  position: relative;
}
.hot-cta {
  font-size: 14px;
  font-weight: 800;
  color: rgba(255,255,255,0.9);
  position: relative;
  align-self: flex-start;
}
.hot-cta--muted {
  color: rgba(255,255,255,0.45);
}

/* ──────────────────────────────── */
/* CTA STRIP                        */
/* ──────────────────────────────── */
.home-cta {
  background: linear-gradient(135deg, #0f172a 0%, #1e3a5f 60%, #0f172a 100%);
  padding: 64px 0;
}

.cta-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 32px;
  flex-wrap: wrap;
}
.cta-title {
  margin: 0;
  font-size: clamp(22px, 2.8vw, 32px);
  font-weight: 800;
  color: #fff;
  letter-spacing: -0.02em;
}
.cta-sub {
  margin: 8px 0 0;
  font-size: 14px;
  color: #94a3b8;
}
.cta-btns {
  display: flex;
  gap: 12px;
  flex-shrink: 0;
  flex-wrap: wrap;
}

/* ──────────────────────────────── */
/* RESPONSIVE                       */
/* ──────────────────────────────── */
@media (max-width: 1023px) {
  .hero-grid {
    grid-template-columns: 1fr;
    gap: 40px;
  }
  .hero-panel {
    display: none;
  }
  .home-hero {
    padding: 52px 0 56px;
  }
  .hero-title {
    font-size: clamp(34px, 5.5vw, 52px);
  }

  .mod-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .hot-grid {
    grid-template-columns: 1fr;
    gap: 14px;
  }

  .cta-inner {
    flex-direction: column;
    align-items: flex-start;
  }
}

@media (max-width: 767px) {
  .hc {
    width: calc(100% - 32px);
  }

  .home-hero {
    padding: 36px 0 40px;
  }

  .hero-actions {
    flex-direction: column;
    gap: 10px;
  }
  .hero-actions .btn-primary,
  .hero-actions .btn-ghost {
    width: 100%;
  }

  .stats-row {
    grid-template-columns: repeat(2, 1fr);
    gap: 0;
  }
  .stat-cell:nth-child(2) {
    border-right: none;
  }
  .stat-cell:nth-child(3) {
    border-top: 1px solid #e2e8f0;
  }
  .stat-cell:nth-child(4) {
    border-top: 1px solid #e2e8f0;
    border-right: none;
  }

  .home-section {
    padding: 40px 0;
  }
  .sec-head {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
    margin-bottom: 24px;
  }

  .mod-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 10px;
  }
  .mod-card {
    padding: 20px 16px;
  }
  .mod-name {
    font-size: 15px;
  }

  .card-grid-3 {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .hot-card {
    min-height: 160px;
    padding: 24px 20px;
  }

  .home-cta {
    padding: 44px 0;
  }
  .cta-btns {
    width: 100%;
  }
  .cta-btns .btn-primary,
  .cta-btns .btn-ghost--inv {
    flex: 1;
  }
}
</style>
