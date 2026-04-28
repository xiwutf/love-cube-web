<template>
  <main class="home-wrap">
    <section class="hero-shell">
      <div class="hero-copy">
        <p class="home-kicker">LOVE CUBE PLATFORM</p>
        <h1>
          <span
            v-for="(line, index) in heroTitleLines"
            :key="`${line}-${index}`"
            class="hero-title-line"
          >
            {{ line }}
          </span>
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
          <span v-if="platformData.userCount > 0">已有 <strong>{{ platformData.userCount.toLocaleString() }}+</strong> 青年在这里遇见美好</span>
          <span v-else>加入 Love Cube，遇见美好</span>
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

    <section ref="belowFoldRef" class="deferred-anchor">
      <section v-if="shouldRenderBelowFold" class="content-grid">
        <HomeNewsPanel
          :featured-update="featuredUpdate"
          :side-updates="sideUpdates"
          @media-error="onMediaError"
        />
        <HomeModuleCardsPanel :module-cards="moduleCards" />
      </section>
      <section v-else class="section-skeleton section-skeleton-grid" aria-hidden="true">
        <div class="skeleton-card"></div>
        <div class="skeleton-card"></div>
      </section>
    </section>

    <section ref="statsRef" class="deferred-anchor">
      <HomeStatsBand v-if="shouldRenderStats" :platform-stats="platformStats" />
      <section v-else class="section-skeleton section-skeleton-stats" aria-hidden="true">
        <div v-for="idx in 3" :key="`stat-skeleton-${idx}`" class="skeleton-chip"></div>
      </section>
    </section>

    <section ref="ctaRef" class="deferred-anchor">
      <HomeCtaPanel
        v-if="shouldRenderCta"
        :cta-background-image="ctaBackgroundImage"
        @media-error="onMediaError"
      />
      <section v-else class="section-skeleton section-skeleton-cta" aria-hidden="true">
        <div class="skeleton-line"></div>
        <div class="skeleton-line short"></div>
      </section>
    </section>
  </main>
</template>

<script setup>
import { computed, defineAsyncComponent, onMounted, ref, watch } from 'vue'
import { fetchAnnouncements, fetchArticles, fetchEvents, fetchHomeConfig, fetchPlatformStats } from '@/api/platformContent.js'
import { useDeferredRender } from '@/composables/useDeferredRender.js'
import heroImage from '@/assets/首页首屏右侧大图.webp'
import moduleEventsImage from '@/assets/活动模块.webp'
import moduleArticlesImage from '@/assets/公告模块卡片.webp'
import ctaBackgroundImage from '@/assets/底部横幅 CTA.webp'

const HomeNewsPanel = defineAsyncComponent(() => import('@/components/platform/home/HomeNewsPanel.vue'))
const HomeModuleCardsPanel = defineAsyncComponent(() => import('@/components/platform/home/HomeModuleCardsPanel.vue'))
const HomeStatsBand = defineAsyncComponent(() => import('@/components/platform/home/HomeStatsBand.vue'))
const HomeCtaPanel = defineAsyncComponent(() => import('@/components/platform/home/HomeCtaPanel.vue'))

const announcements = ref([])
const articles = ref([])
const events = ref([])
const homeConfig = ref(null)
const platformData = ref({ userCount: 0, eventSignupCount: 0, dynamicsCount: 0, articleViewCount: 0, citiesCount: 0 })
const belowFoldLoaded = ref(false)

const { mountRef: belowFoldRef, shouldRender: shouldRenderBelowFold } = useDeferredRender({ rootMargin: '260px 0px' })
const { mountRef: statsRef, shouldRender: shouldRenderStats } = useDeferredRender({ rootMargin: '320px 0px' })
const { mountRef: ctaRef, shouldRender: shouldRenderCta } = useDeferredRender({ rootMargin: '420px 0px' })

const proofAvatars = ['溪', '光', '甜', '风']

const defaultPlatformModules = [
  { moduleKey: 'fellowship', title: '联谊交友', desc: '寻找心动的 TA', to: '/fellowship', status: 'active', icon: '❤', tone: 'tone-pink', sortOrder: 1 },
  { moduleKey: 'positive-share', title: '每日心声', desc: '分享感恩和成长思考', to: '/platform/positive-share', status: 'active', icon: '暖', tone: 'tone-pink', sortOrder: 2 },
  { moduleKey: 'ai-tools', title: 'AI 工具', desc: '智能工具，提升效率', to: '/modules', status: 'planned', icon: 'AI', tone: 'tone-blue', sortOrder: 3 },
  { moduleKey: 'announcements', title: '公告通知', desc: '及时获取重要通知', to: '/announcements', status: 'active', icon: '告', tone: 'tone-orange', sortOrder: 4 },
  { moduleKey: 'local-services', title: '本地服务', desc: '便捷生活，触手可及', to: '/modules', status: 'planned', icon: '位', tone: 'tone-green', sortOrder: 5 },
  { moduleKey: 'modules', title: '更多模块', desc: '更多精彩，敬请期待', to: '/modules', status: 'active', icon: '▦', tone: 'tone-purple', sortOrder: 6 }
]

const moduleToneByKey = {
  fellowship: 'tone-pink',
  'positive-share': 'tone-pink',
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
  'positive-share': '暖',
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

const heroMetrics = computed(() => [
  { label: '注册用户', value: platformData.value.userCount > 0 ? `${platformData.value.userCount.toLocaleString()} 人` : '--', tone: 'line-blue' },
  { label: '活动报名', value: platformData.value.eventSignupCount > 0 ? `${platformData.value.eventSignupCount.toLocaleString()} 人次` : '--', tone: 'line-orange' },
  { label: '内容发布', value: platformData.value.dynamicsCount > 0 ? `${platformData.value.dynamicsCount.toLocaleString()} 条` : '--', tone: 'line-green' }
])

const moduleCards = [
  { title: '联谊模块', icon: '联', meta: '交友业务专区', desc: '进入联谊专区处理资料、互动与消息。', tone: 'tone-pink', to: '/fellowship', actionText: '进入模块' },
  { title: '每日心声', icon: '暖', meta: '平台内容专区', desc: '每天记录感恩、鼓励与成长思考。', tone: 'tone-pink', to: '/platform/positive-share', actionText: '进入分享' },
  { title: '活动中心', icon: '活', meta: '线上线下活动', desc: '浏览近期活动并完成报名。', tone: 'tone-orange', to: '/events', actionText: '查看活动' },
  { title: '模块中心', icon: '模', meta: '统一入口', desc: '从模块中心进入更多平台能力。', tone: 'tone-violet', to: '/modules', actionText: '前往模块' }
]

const platformStats = computed(() => [
  { icon: '人', value: platformData.value.userCount > 0 ? `${platformData.value.userCount.toLocaleString()}+` : '--', label: '注册用户', delta: '', tone: 'tone-blue' },
  { icon: '活', value: platformData.value.eventSignupCount > 0 ? `${platformData.value.eventSignupCount.toLocaleString()}+` : '--', label: '活动报名', delta: '', tone: 'tone-pink' },
  { icon: '聊', value: platformData.value.dynamicsCount > 0 ? `${platformData.value.dynamicsCount.toLocaleString()}+` : '--', label: '动态发布', delta: '', tone: 'tone-violet' },
  { icon: '眼', value: platformData.value.articleViewCount > 0 ? `${platformData.value.articleViewCount.toLocaleString()}+` : '--', label: '内容浏览', delta: '', tone: 'tone-purple' },
  { icon: '位', value: platformData.value.citiesCount > 0 ? `${platformData.value.citiesCount}+` : '--', label: '覆盖城市', delta: '', tone: 'tone-green' }
])

const allUpdates = computed(() => {
  const rows = [
    ...articles.value.slice(0, 3).map((item, index) => ({
      key: `article-${item.id || index}`,
      title: item.title || '平台资讯',
      summary: item.summary || item.description || '更多精彩内容正在持续更新。',
      category: item.category || '平台资讯',
      sourceLabel: sourceLabelByCategory(item.category || '平台资讯'),
      date: formatDate(item.publishDate || item.createdAt),
      reads: item.viewCount > 0 ? `${item.viewCount}阅读` : '',
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
      reads: item.viewCount > 0 ? `${item.viewCount}阅读` : '',
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
      reads: item.viewCount > 0 ? `${item.viewCount}阅读` : '',
      tag: '活动',
      short: '活',
      tone: 'tone-orange',
      cover: item.coverUrl || item.cover || (index === 0 ? moduleEventsImage : ''),
      to: item.id ? `/events/${item.id}` : '/events'
    }))
  ]

  return (rows.length ? rows : fallbackUpdates).slice(0, 3)
})

const fallbackUpdates = [
  {
    key: 'fallback-featured',
    title: 'Love Cube 平台内容持续更新中',
    summary: '平台资讯、活动与公告正在陆续发布，欢迎前往内容中心查看最新动态。',
    category: '平台资讯',
    sourceLabel: '[平台资讯]',
    date: '刚刚',
    reads: '',
    tag: '推荐',
    short: '站',
    tone: 'tone-blue',
    cover: moduleArticlesImage,
    to: '/articles'
  },
  {
    key: 'fallback-announcement',
    title: '欢迎来到 Love Cube',
    summary: '关注平台公告，及时获取最新产品与活动信息。',
    category: '平台公告',
    sourceLabel: '[平台资讯]',
    date: '刚刚',
    reads: '',
    tag: '公告',
    short: '告',
    tone: 'tone-violet',
    cover: '',
    to: '/announcements'
  }
]

const featuredUpdate = computed(() => allUpdates.value[0] || fallbackUpdates[0])
const sideUpdates = computed(() => allUpdates.value.slice(1, 3))

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

watch(shouldRenderBelowFold, (next) => {
  if (next) loadBelowFold()
}, { immediate: true })

async function loadBelowFold() {
  if (belowFoldLoaded.value) return
  belowFoldLoaded.value = true
  const [ann, art, ev] = await Promise.allSettled([
    fetchAnnouncements({ status: 'published', limit: 10 }),
    fetchArticles({ status: 'published', limit: 10 }),
    fetchEvents({ status: 'published', limit: 10 })
  ])
  if (ann.status === 'fulfilled') announcements.value = ann.value || []
  if (art.status === 'fulfilled') articles.value = art.value || []
  if (ev.status === 'fulfilled') events.value = ev.value || []
}

onMounted(async () => {
  const [cfg, stats] = await Promise.allSettled([
    fetchHomeConfig(),
    fetchPlatformStats()
  ])
  if (cfg.status === 'fulfilled') homeConfig.value = cfg.value || null
  if (stats.status === 'fulfilled' && stats.value) platformData.value = stats.value
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

.hero-title-line {
  display: block;
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

.deferred-anchor {
  min-height: 72px;
}

.section-skeleton {
  margin-top: 22px;
}

.section-skeleton-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 22px;
}

.skeleton-card,
.skeleton-chip,
.skeleton-line {
  position: relative;
  overflow: hidden;
  border-radius: 8px;
  background: #eef2f8;
}

.skeleton-card::after,
.skeleton-chip::after,
.skeleton-line::after {
  content: '';
  position: absolute;
  inset: 0;
  transform: translateX(-100%);
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.85), transparent);
  animation: skeleton-slide 1.2s ease infinite;
}

.skeleton-card {
  min-height: 280px;
}

.section-skeleton-stats {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
}

.skeleton-chip {
  height: 86px;
}

.section-skeleton-cta {
  display: grid;
  gap: 10px;
}

.skeleton-line {
  height: 24px;
}

.skeleton-line.short {
  width: 60%;
}

@keyframes skeleton-slide {
  to {
    transform: translateX(100%);
  }
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
    width: calc(100% - 16px);
    padding-top: 10px;
  }

  .hero-shell {
    gap: 16px;
    padding: 20px 14px;
  }

  .hero-data-card,
  .featured-news,
  .stats-band {
    grid-template-columns: 1fr;
  }

  .module-strip {
    grid-template-columns: 1fr;
    gap: 10px;
    margin-top: 12px;
  }

  .hero-shell {
    box-shadow: 0 8px 24px rgba(30, 64, 175, 0.08);
  }

  .home-btn:hover,
  .module-entry:hover,
  .news-row:hover,
  .person-card:hover {
    transform: none;
    box-shadow: none;
  }

  .module-entry {
    grid-template-columns: 50px minmax(0, 1fr);
    justify-items: start;
    align-items: center;
    gap: 3px 10px;
    min-height: 0;
    padding: 14px 12px;
    text-align: left;
  }

  .module-entry-icon {
    grid-row: 1 / 4;
    width: 46px;
    height: 46px;
    font-size: 20px;
  }

  .module-entry strong {
    font-size: 16px;
    line-height: 1.3;
  }

  .module-entry small {
    font-size: 12px;
    line-height: 1.4;
  }

  .module-entry em {
    margin-top: 2px;
    font-size: 12px;
  }

  .people-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .person-card {
    min-height: 180px;
    gap: 6px;
    padding: 12px;
  }

  .person-photo {
    width: 72px;
    height: 80px;
  }

  .person-photo span {
    width: 52px;
    height: 52px;
    font-size: 20px;
  }

  .hero-visual img {
    height: 204px;
    box-shadow: 0 8px 20px rgba(59, 130, 246, 0.1);
  }

  .stats-band {
    margin-top: 16px;
    padding: 10px;
    gap: 8px;
    border-radius: 8px;
  }

  .stat-card {
    min-height: 80px;
    padding: 10px 12px;
    gap: 4px 10px;
  }

  .stat-icon {
    width: 40px;
    height: 40px;
  }

  .stat-card strong {
    font-size: 18px;
  }

  .cta-panel {
    align-items: flex-start;
    flex-direction: column;
    padding: 22px 16px;
  }

  .cta-actions,
  .home-btn {
    width: 100%;
  }

  .section-skeleton-grid,
  .section-skeleton-stats {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 520px) {
  .people-grid {
    grid-template-columns: 1fr;
    gap: 10px;
  }

  .person-card {
    grid-template-columns: 54px minmax(0, 1fr);
    justify-items: start;
    align-items: center;
    gap: 2px 10px;
    min-height: 0;
    padding: 12px;
    text-align: left;
  }

  .person-photo {
    grid-row: 1 / 5;
    width: 54px;
    height: 54px;
    border-radius: 50%;
  }

  .person-photo span {
    width: 54px;
    height: 54px;
    font-size: 22px;
  }

  .person-photo i {
    right: 2px;
    bottom: 2px;
  }

  .person-card strong {
    font-size: 18px;
    line-height: 1.3;
  }

  .person-card small {
    font-size: 13px;
  }

  .person-card p {
    min-height: 0;
    font-size: 13px;
    line-height: 1.45;
  }

  .person-card a {
    margin-top: 4px;
    min-width: 82px;
    height: 32px;
  }

  .stats-band {
    padding: 6px;
    gap: 6px;
  }

  .stat-card {
    padding: 10px;
  }
}
</style>

