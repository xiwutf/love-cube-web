<template>
  <main class="home-wrap">
    <section class="hero-shell">
      <div class="hero-copy">
        <p class="home-kicker">LOVE CUBE PLATFORM</p>
        <h1>
          <template v-for="(line, index) in heroTitleLines" :key="`${line}-${index}`">
            {{ line }}<br v-if="index < heroTitleLines.length - 1">
          </template>
        </h1>
        <p class="hero-lead">{{ homepageHero.subtitle }}</p>
        <div class="hero-actions">
          <router-link :to="homepageHero.primaryLink" class="home-btn home-btn-primary">
            {{ homepageHero.primaryText }}
          </router-link>
          <router-link :to="homepageHero.secondaryLink" class="home-btn home-btn-light">
            {{ homepageHero.secondaryText }}
          </router-link>
        </div>
        <div class="hero-proof">
          <span class="avatar-stack">
            <span v-for="item in proofAvatars" :key="item" class="proof-avatar">{{ item }}</span>
          </span>
          <span>已有 <strong>12,345+</strong> 青年在这里遇见美好</span>
        </div>
      </div>

      <div class="hero-visual">
        <img
          :src="heroImageSrc"
          alt="Love Cube 城市连接平台"
          loading="eager"
          fetchpriority="high"
          decoding="async"
          @error="onHeroImageError"
        >
        <div class="hero-float hero-float-chat">聊</div>
        <div class="hero-float hero-float-heart">❤</div>
        <div class="hero-float hero-float-location">●</div>
      </div>

      <aside class="hero-data-card">
        <div v-for="item in heroMetrics" :key="item.label" class="hero-data-item">
          <span>{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
          <i :class="item.tone"></i>
        </div>
      </aside>
    </section>

    <section class="module-strip" aria-label="功能入口">
      <router-link
        v-for="item in platformModules.slice(0, 6)"
        :key="item.moduleKey || item.title"
        :to="item.to || '/modules'"
        class="module-entry"
        :class="item.tone"
      >
        <span class="module-entry-icon">{{ item.icon }}</span>
        <strong>{{ item.title }}</strong>
        <small>{{ item.desc }}</small>
        <em>进入 →</em>
      </router-link>
    </section>

    <section class="content-grid">
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
              @error="onMediaError"
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

      <div class="content-panel">
        <div class="panel-head">
          <h2>模块推荐</h2>
          <router-link to="/modules">查看更多 →</router-link>
        </div>
        <div class="people-grid">
          <article v-for="module in moduleCards" :key="module.title" class="person-card">
            <div class="person-photo" :class="module.tone">
              <span>{{ module.icon }}</span>
              <i></i>
            </div>
            <strong>{{ module.title }}</strong>
            <small>{{ module.meta }}</small>
            <p>{{ module.desc }}</p>
            <router-link :to="module.to">{{ module.actionText }}</router-link>
          </article>
        </div>
      </div>
    </section>

    <section class="stats-band" aria-label="平台数据">
      <article v-for="item in platformStats" :key="item.label" class="stat-card">
        <span class="stat-icon" :class="item.tone">{{ item.icon }}</span>
        <strong>{{ item.value }}</strong>
        <small>{{ item.label }}</small>
        <em>{{ item.delta }}</em>
      </article>
    </section>

    <section class="cta-panel">
      <div>
        <h2>加入 Love Cube，开启美好生活之旅</h2>
        <p>在这里，遇见有趣的人，发现精彩的生活。</p>
      </div>
      <div class="cta-actions">
        <router-link to="/login" class="home-btn home-btn-white">立即注册</router-link>
        <router-link to="/modules" class="home-btn home-btn-outline">了解更多</router-link>
      </div>
      <img :src="ctaBackgroundImage" alt="" loading="lazy" decoding="async" @error="onMediaError">
    </section>
  </main>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { fetchAnnouncements, fetchArticles, fetchEvents, fetchHomeConfig } from '@/api/platformContent.js'
import heroImage from '@/assets/首页首屏右侧大图.png'
import moduleEventsImage from '@/assets/活动模块.png'
import moduleArticlesImage from '@/assets/公告模块卡片.png'
import ctaBackgroundImage from '@/assets/底部横幅 CTA.png'

const announcements = ref([])
const articles = ref([])
const events = ref([])
const homeConfig = ref(null)

const proofAvatars = ['溪', '光', '甜', '风']

const defaultPlatformModules = [
  { moduleKey: 'fellowship', title: '联谊交友', desc: '寻找心动的 TA', to: '/fellowship', status: 'active', icon: '❤', tone: 'tone-pink', sortOrder: 1 },
  { moduleKey: 'dynamic', title: '动态社区', desc: '分享生活，结识朋友', to: '/fellowship/dynamic', status: 'active', icon: '●', tone: 'tone-violet', sortOrder: 2 },
  { moduleKey: 'ai-tools', title: 'AI 工具', desc: '智能工具，提升效率', to: '/modules', status: 'planned', icon: 'AI', tone: 'tone-blue', sortOrder: 3 },
  { moduleKey: 'announcements', title: '公告通知', desc: '最新公告，及时获取', to: '/announcements', status: 'active', icon: '告', tone: 'tone-orange', sortOrder: 4 },
  { moduleKey: 'local-services', title: '本地服务', desc: '便捷生活，触手可及', to: '/modules', status: 'planned', icon: '位', tone: 'tone-green', sortOrder: 5 },
  { moduleKey: 'modules', title: '更多模块', desc: '更多精彩，敬请期待', to: '/modules', status: 'active', icon: '▦', tone: 'tone-purple', sortOrder: 6 }
]

const moduleToneByKey = {
  fellowship: 'tone-pink',
  dynamic: 'tone-violet',
  events: 'tone-blue',
  articles: 'tone-green',
  announcements: 'tone-orange',
  'local-services': 'tone-green',
  'ai-tools': 'tone-blue',
  modules: 'tone-purple'
}

const moduleIconByKey = {
  fellowship: '❤',
  dynamic: '●',
  events: '日',
  articles: '文',
  announcements: '告',
  'local-services': '位',
  'ai-tools': 'AI',
  modules: '▦'
}

const moduleDefaultsByKey = Object.fromEntries(defaultPlatformModules.map(item => [item.moduleKey, item]))

const homepageHero = computed(() => ({
  title: homeConfig.value?.hero?.title || '连接城市青年\n遇见美好生活',
  subtitle: homeConfig.value?.hero?.subtitle || '联谊交友 · AI工具 · 内容资讯 · 本地服务',
  primaryText: homeConfig.value?.hero?.primaryText || '立即加入',
  primaryLink: homeConfig.value?.hero?.primaryLink || '/fellowship',
  secondaryText: homeConfig.value?.hero?.secondaryText || '探索更多',
  secondaryLink: homeConfig.value?.hero?.secondaryLink || '/modules',
  imageUrl: homeConfig.value?.hero?.imageUrl || homeConfig.value?.hero?.image || homeConfig.value?.hero?.coverUrl || homeConfig.value?.hero?.heroImageUrl || ''
}))

const heroTitleLines = computed(() => String(homepageHero.value.title).split(/\n|<br\s*\/?>/i).filter(Boolean))
const heroImageSrc = computed(() => homepageHero.value.imageUrl || heroImage)

const platformModules = computed(() => {
  const configured = Array.isArray(homeConfig.value?.modules) ? homeConfig.value.modules : []
  const source = configured.length ? configured : defaultPlatformModules
  return source
    .filter(item => item.enabled !== false)
    .map((item, index) => normalizeModule(item, index))
    .sort((a, b) => a.sortOrder - b.sortOrder)
})

const heroMetrics = [
  { label: '今日活跃', value: '2,345 人', tone: 'line-blue' },
  { label: '在线用户', value: '1,234 人', tone: 'line-green' },
  { label: '活动报名', value: '3,456 人次', tone: 'line-orange' }
]

const moduleCards = [
  { title: '联谊模块', icon: '联', meta: '交友业务专区', desc: '进入联谊专区处理资料、互动与消息。', tone: 'tone-pink', to: '/fellowship', actionText: '进入模块' },
  { title: '内容资讯', icon: '文', meta: '内容中心', desc: '查看平台精选内容与运营资讯。', tone: 'tone-blue', to: '/articles', actionText: '查看内容' },
  { title: '活动中心', icon: '活', meta: '线上线下活动', desc: '浏览近期活动并完成报名。', tone: 'tone-orange', to: '/events', actionText: '查看活动' },
  { title: '模块中心', icon: '模', meta: '统一入口', desc: '从模块中心进入更多平台能力。', tone: 'tone-violet', to: '/modules', actionText: '前往模块' }
]

const platformStats = [
  { icon: '人', value: '125,678+', label: '注册用户', delta: '较昨日 +2.5%', tone: 'tone-blue' },
  { icon: '活', value: '23,456+', label: '活动报名', delta: '较昨日 +3.2%', tone: 'tone-pink' },
  { icon: '聊', value: '89,123+', label: '动态发布', delta: '较昨日 +1.8%', tone: 'tone-violet' },
  { icon: '眼', value: '456,789+', label: '内容浏览', delta: '较昨日 +4.3%', tone: 'tone-purple' },
  { icon: '楼', value: '128+', label: '合作商家', delta: '较昨日 +2.1%', tone: 'tone-green' },
  { icon: '位', value: '15+', label: '覆盖城市', delta: '较昨日 +1.5%', tone: 'tone-blue' }
]

const allUpdates = computed(() => {
  const rows = [
    ...articles.value.slice(0, 3).map((item, index) => ({
      key: `article-${item.id || index}`,
      title: item.title || '平台资讯',
      summary: item.summary || item.description || '更多精彩内容正在持续更新。',
      category: item.category || '平台资讯',
      sourceLabel: sourceLabelByCategory(item.category || '平台资讯'),
      date: formatDate(item.publishDate || item.createdAt),
      reads: `${item.viewCount || 856}阅读`,
      tag: index === 0 ? '推荐' : '资讯',
      short: 'AI',
      tone: 'tone-violet',
      cover: item.coverUrl || item.cover || (index === 0 ? moduleArticlesImage : ''),
      to: item.id ? `/articles/${item.id}` : '/articles'
    })),
    ...announcements.value.slice(0, 2).map((item, index) => ({
      key: `announcement-${item.id || index}`,
      title: item.title || '平台公告',
      summary: item.summary || item.description || '重要平台信息请进入详情查看。',
      category: '平台公告',
      sourceLabel: '[平台资讯]',
      date: formatDate(item.publishDate || item.createdAt),
      reads: `${item.viewCount || 623}阅读`,
      tag: '公告',
      short: '告',
      tone: 'tone-blue',
      cover: item.coverUrl || item.cover || '',
      to: item.id ? `/announcements/${item.id}` : '/announcements'
    })),
    ...events.value.slice(0, 2).map((item, index) => ({
      key: `event-${item.id || index}`,
      title: item.title || '平台活动',
      summary: item.summary || item.description || '活动报名与现场服务持续开放。',
      category: '活动预告',
      sourceLabel: '[活动中心]',
      date: formatDate(item.eventTime || item.createdAt),
      reads: `${item.viewCount || 1234}阅读`,
      tag: '活动',
      short: '活',
      tone: 'tone-orange',
      cover: item.coverUrl || item.cover || (index === 0 ? moduleEventsImage : ''),
      to: item.id ? `/events/${item.id}` : '/events'
    }))
  ]

  return rows.length ? rows : fallbackUpdates
})

const fallbackUpdates = [
  {
    key: 'fallback-featured',
    title: '周末城市青年主题活动报名中',
    summary: '围绕兴趣社交与城市探索，欢迎报名参与。',
    category: '活动预告',
    sourceLabel: '[活动中心]',
    date: '2小时前',
    reads: '1,234阅读',
    tag: '推荐',
    short: '活',
    tone: 'tone-orange',
    cover: moduleEventsImage,
    to: '/events'
  },
  {
    key: 'fallback-ai',
    title: 'AI工具上新：内容推荐算法升级',
    summary: '平台公告 · 5小时前',
    category: '平台公告',
    sourceLabel: '[平台资讯]',
    date: '5小时前',
    reads: '856阅读',
    tag: '公告',
    short: 'AI',
    tone: 'tone-violet',
    cover: '',
    to: '/announcements'
  },
  {
    key: 'fallback-local',
    title: '本地优质商家推荐',
    summary: '生活服务 · 1天前',
    category: '生活服务',
    sourceLabel: '[本地服务]',
    date: '1天前',
    reads: '623阅读',
    tag: '服务',
    short: '服',
    tone: 'tone-blue',
    cover: '',
    to: '/articles'
  },
  {
    key: 'fallback-love',
    title: '如何在 Love Cube 高效使用平台模块',
    summary: '平台指南 · 2天前',
    category: '平台指南',
    sourceLabel: '[平台资讯]',
    date: '2天前',
    reads: '1,023阅读',
    tag: '攻略',
    short: '爱',
    tone: 'tone-orange',
    cover: '',
    to: '/articles'
  }
]

const featuredUpdate = computed(() => allUpdates.value[0] || fallbackUpdates[0])
const sideUpdates = computed(() => allUpdates.value.slice(1, 4))

function normalizeModule(item, index) {
  const moduleKey = item.moduleKey || item.key || ''
  const fallback = moduleDefaultsByKey[moduleKey] || defaultPlatformModules[index] || {}
  return {
    moduleKey,
    title: item.title || item.name || fallback.title || '',
    desc: item.desc || item.description || fallback.desc || '',
    to: item.to || item.entryRoute || fallback.to || '/modules',
    status: item.status || fallback.status || 'planned',
    icon: item.icon || moduleIconByKey[moduleKey] || fallback.icon || '▦',
    tone: item.tone || moduleToneByKey[moduleKey] || fallback.tone || 'tone-blue',
    sortOrder: Number.isFinite(Number(item.sortOrder)) ? Number(item.sortOrder) : index + 1
  }
}

function formatDate(value) {
  if (!value) return '刚刚'
  const text = String(value).replace('T', ' ')
  return text.length > 10 ? text.slice(0, 10) : text
}

function sourceLabelByCategory(category = '') {
  if (String(category).includes('活动')) return '[活动中心]'
  if (String(category).includes('AI')) return '[AI工具]'
  if (String(category).includes('服务')) return '[本地服务]'
  if (String(category).includes('联谊')) return '[联谊模块]'
  return '[平台资讯]'
}

function onMediaError(event) {
  if (event?.target?.classList) event.target.classList.add('is-hidden')
}

function onHeroImageError(event) {
  const target = event?.target
  if (!target) return
  if (target.dataset?.fallbackApplied !== 'true') {
    target.dataset.fallbackApplied = 'true'
    target.src = heroImage
    return
  }
  onMediaError(event)
}

onMounted(async () => {
  const [ann, art, ev, cfg] = await Promise.allSettled([
    fetchAnnouncements({ status: 'published' }),
    fetchArticles({ status: 'published' }),
    fetchEvents({ status: 'published' }),
    fetchHomeConfig()
  ])
  if (ann.status === 'fulfilled') announcements.value = ann.value || []
  if (art.status === 'fulfilled') articles.value = art.value || []
  if (ev.status === 'fulfilled') events.value = ev.value || []
  if (cfg.status === 'fulfilled') homeConfig.value = cfg.value || null
})
</script>

<style scoped>
.home-wrap {
  width: calc(100% - 48px);
  margin: 0 auto;
  padding: 22px 0 48px;
  color: #111827;
}

.hero-shell {
  position: relative;
  display: grid;
  grid-template-columns: minmax(360px, 0.82fr) minmax(420px, 1.08fr) 220px;
  gap: 28px;
  align-items: center;
  min-height: 360px;
  padding: 44px 42px;
  border: 1px solid #dbe7ff;
  border-radius: 8px;
  background:
    radial-gradient(circle at 62% 36%, rgba(255, 92, 163, 0.2) 0, transparent 24%),
    linear-gradient(135deg, #eef5ff 0%, #f8fbff 52%, #eaf2ff 100%);
  box-shadow: 0 24px 70px rgba(30, 64, 175, 0.14);
  overflow: hidden;
}

.home-kicker {
  margin: 0 0 14px;
  color: #2563eb;
  font-size: 12px;
  font-weight: 900;
  letter-spacing: 0.16em;
}

.hero-copy h1 {
  margin: 0;
  color: #0f172a;
  font-size: clamp(42px, 4.4vw, 66px);
  font-weight: 950;
  line-height: 1.15;
}

.hero-copy h1 :deep(*) {
  letter-spacing: 0;
}

.hero-lead {
  margin: 16px 0 0;
  color: #334155;
  font-size: 18px;
  font-weight: 600;
  line-height: 1.7;
}

.hero-actions,
.cta-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  margin-top: 24px;
}

.home-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 132px;
  height: 44px;
  padding: 0 22px;
  border: 1px solid transparent;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 800;
  text-decoration: none;
  transition: transform 0.18s ease, box-shadow 0.18s ease, border-color 0.18s ease;
}

.home-btn:hover {
  transform: translateY(-2px);
}

.home-btn-primary {
  color: #fff;
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
  box-shadow: 0 14px 30px rgba(37, 99, 235, 0.28);
}

.home-btn-light {
  color: #1e293b;
  background: rgba(255, 255, 255, 0.9);
  border-color: #dbeafe;
}

.home-btn-white {
  color: #2563eb;
  background: #fff;
}

.home-btn-outline {
  color: #fff;
  border-color: rgba(255, 255, 255, 0.62);
}

.hero-proof {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 24px;
  color: #475569;
  font-size: 13px;
}

.hero-proof strong {
  color: #2563eb;
}

.avatar-stack {
  display: flex;
}

.proof-avatar {
  display: grid;
  place-items: center;
  width: 26px;
  height: 26px;
  margin-left: -7px;
  border: 2px solid #fff;
  border-radius: 50%;
  color: #fff;
  background: linear-gradient(135deg, #f472b6, #2563eb);
  font-size: 11px;
  font-weight: 900;
}

.proof-avatar:first-child {
  margin-left: 0;
}

.hero-visual {
  position: relative;
  min-height: 300px;
}

.hero-visual img {
  width: 100%;
  height: 330px;
  object-fit: cover;
  border-radius: 8px;
  box-shadow: 0 18px 50px rgba(59, 130, 246, 0.18);
}

.hero-float {
  position: absolute;
  display: grid;
  place-items: center;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.82);
  box-shadow: 0 12px 28px rgba(59, 130, 246, 0.16);
  font-weight: 900;
}

.hero-float-chat {
  left: 6%;
  top: 10%;
  color: #2563eb;
}

.hero-float-heart {
  right: 12%;
  top: 18%;
  color: #ec4899;
}

.hero-float-location {
  right: 4%;
  bottom: 25%;
  color: #22c55e;
}

.hero-data-card {
  display: grid;
  gap: 18px;
  padding: 24px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.86);
  box-shadow: 0 18px 40px rgba(15, 23, 42, 0.12);
}

.hero-data-item {
  display: grid;
  grid-template-columns: 1fr 54px;
  gap: 4px 10px;
  align-items: end;
}

.hero-data-item span {
  grid-column: 1 / -1;
  color: #475569;
  font-size: 13px;
  font-weight: 700;
}

.hero-data-item strong {
  font-size: 22px;
}

.hero-data-item i {
  width: 54px;
  height: 22px;
  border-radius: 999px;
}

.line-blue { background: linear-gradient(135deg, transparent 20%, #2563eb 22%, transparent 28%, transparent 52%, #2563eb 54%, transparent 60%); }
.line-green { background: linear-gradient(135deg, transparent 20%, #059669 22%, transparent 28%, transparent 52%, #059669 54%, transparent 60%); }
.line-orange { background: linear-gradient(135deg, transparent 20%, #f97316 22%, transparent 28%, transparent 52%, #f97316 54%, transparent 60%); }

.module-strip {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 16px;
  margin-top: 16px;
}

.module-entry {
  display: grid;
  justify-items: center;
  gap: 9px;
  min-height: 178px;
  padding: 22px 14px;
  border: 1px solid rgba(226, 232, 240, 0.9);
  border-radius: 8px;
  color: #0f172a;
  text-align: center;
  text-decoration: none;
  background: #fff;
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}

.module-entry:hover,
.news-row:hover,
.person-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 18px 38px rgba(15, 23, 42, 0.1);
}

.module-entry-icon,
.stat-icon,
.news-row-icon {
  display: grid;
  place-items: center;
  border-radius: 50%;
  font-weight: 900;
}

.module-entry-icon {
  width: 62px;
  height: 62px;
  font-size: 24px;
}

.module-entry strong {
  font-size: 18px;
}

.module-entry small {
  color: #64748b;
  font-size: 13px;
  line-height: 1.5;
}

.module-entry em {
  color: currentColor;
  font-size: 13px;
  font-style: normal;
  font-weight: 900;
}

.tone-pink { --tone: #ec4899; --tone-bg: #fff1f5; color: #be185d; background: linear-gradient(180deg, #fff7fb, #fff); }
.tone-violet { --tone: #6366f1; --tone-bg: #eef2ff; color: #4f46e5; background: linear-gradient(180deg, #f5f3ff, #fff); }
.tone-blue { --tone: #2563eb; --tone-bg: #eff6ff; color: #1d4ed8; background: linear-gradient(180deg, #eff6ff, #fff); }
.tone-orange { --tone: #f97316; --tone-bg: #fff7ed; color: #c2410c; background: linear-gradient(180deg, #fff7ed, #fff); }
.tone-green { --tone: #16a34a; --tone-bg: #f0fdf4; color: #15803d; background: linear-gradient(180deg, #f0fdf4, #fff); }
.tone-purple { --tone: #7c3aed; --tone-bg: #f5f3ff; color: #6d28d9; background: linear-gradient(180deg, #f5f3ff, #fff); }

.module-entry-icon,
.stat-icon,
.news-row-icon {
  background: var(--tone-bg);
  color: var(--tone);
}

.content-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(420px, 0.95fr);
  gap: 22px;
  margin-top: 22px;
}

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
.news-row,
.person-card,
.stat-card {
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

.news-row-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
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

.people-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.person-card {
  display: grid;
  justify-items: center;
  gap: 7px;
  min-height: 242px;
  padding: 14px 12px;
  text-align: center;
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}

.person-photo {
  position: relative;
  display: grid;
  place-items: center;
  width: 90px;
  height: 100px;
  border-radius: 8px;
}

.person-photo span {
  display: grid;
  place-items: center;
  width: 64px;
  height: 64px;
  border-radius: 50%;
  color: #fff;
  background: linear-gradient(135deg, var(--tone), #94a3b8);
  font-size: 24px;
  font-weight: 900;
}

.person-photo i {
  position: absolute;
  right: 12px;
  bottom: 17px;
  width: 9px;
  height: 9px;
  border: 2px solid #fff;
  border-radius: 50%;
  background: #22c55e;
}

.person-card strong {
  color: #0f172a;
  font-size: 15px;
}

.person-card small,
.person-card p {
  color: #64748b;
  font-size: 12px;
  line-height: 1.45;
}

.person-card p {
  margin: 0;
  min-height: 34px;
}

.person-card a {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 74px;
  height: 30px;
  border: 1px solid currentColor;
  border-radius: 8px;
  color: var(--tone);
  font-size: 12px;
  font-weight: 800;
  text-decoration: none;
}

.stats-band {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 1px;
  margin-top: 22px;
  padding: 16px;
  border-radius: 8px;
  background: linear-gradient(135deg, #eef5ff, #f8fbff);
}

.stat-card {
  display: grid;
  grid-template-columns: 46px 1fr;
  gap: 5px 12px;
  align-items: center;
  min-height: 88px;
  padding: 14px;
  box-shadow: none;
}

.stat-icon {
  grid-row: 1 / 4;
  width: 44px;
  height: 44px;
  border-radius: 50%;
}

.stat-card strong {
  color: #0f172a;
  font-size: 19px;
}

.stat-card small {
  color: #475569;
  font-size: 12px;
}

.stat-card em {
  color: #16a34a;
  font-size: 12px;
  font-style: normal;
}

.cta-panel {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  min-height: 142px;
  margin-top: 22px;
  padding: 30px 38px;
  border-radius: 8px;
  color: #fff;
  background: linear-gradient(135deg, #2563eb, #6d5dfc);
  overflow: hidden;
}

.cta-panel h2 {
  margin: 0;
  font-size: clamp(26px, 2.3vw, 34px);
}

.cta-panel p {
  margin: 12px 0 0;
  color: rgba(255, 255, 255, 0.88);
  font-size: 16px;
}

.cta-panel img {
  position: absolute;
  right: 70px;
  bottom: -46px;
  width: 190px;
  opacity: 0.3;
  pointer-events: none;
}

.cta-actions {
  position: relative;
  z-index: 1;
  margin-top: 0;
}

.is-hidden {
  display: none;
}

@media (max-width: 1180px) {
  .hero-shell,
  .content-grid {
    grid-template-columns: 1fr;
  }

  .hero-data-card {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .module-strip,
  .stats-band {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .people-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 760px) {
  .home-wrap {
    width: calc(100% - 24px);
    padding-top: 12px;
  }

  .hero-shell {
    padding: 28px 18px;
  }

  .hero-data-card,
  .module-strip,
  .featured-news,
  .stats-band {
    grid-template-columns: 1fr;
  }

  .people-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .hero-visual img {
    height: 240px;
  }

  .cta-panel {
    align-items: flex-start;
    flex-direction: column;
    padding: 28px 20px;
  }

  .cta-actions,
  .home-btn {
    width: 100%;
  }
}
</style>
