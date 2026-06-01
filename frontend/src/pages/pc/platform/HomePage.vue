<template>
  <main class="portal-home">
    <div class="portal-shell">
      <section class="home-live-feed" aria-label="实时动态">
        <span class="home-live-feed-label">实时动态</span>
        <div class="home-live-feed-track">
          <div class="home-live-feed-list">
            <p
              v-for="(item, index) in homeRollingFeed"
              :key="`home-live-${index}-${item.text}`"
            >
              {{ item.text }}
            </p>
          </div>
        </div>
      </section>
      <HomeNoticeBar class="home-block home-notice" :notice="notice" />
      <HomeHeroPortal
        class="home-block home-hero"
        :stats="heroStats"
        :quick-links="quickActionLinks"
      />
      <HomeModuleGrid class="home-block home-modules" :modules="coreModules" />
      <HomeOfficialPanel
        class="home-block home-official"
        :official-items="officialItems"
        :changelog-items="changelogItems"
        :pending-items="pendingItems"
      />
      <div class="portal-split portal-split-accent home-block home-fellowship">
        <HomeFellowshipBanner />
        <HomeActivityPanel :events="activityItems" :stats="activityStats" />
      </div>
      <HomeFeaturedContent class="home-block home-featured" :items="featuredItems" />
      <div class="portal-split home-cobuild-vision">
        <HomeCoBuildPanel />
        <HomeVisionPanel />
      </div>
    </div>
  </main>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import {
  fetchAnnouncements,
  fetchArticles,
  fetchEvents,
  fetchHomeConfig,
  fetchPlatformStats
} from '@/api/platformContent.js'
import HomeActivityPanel from '@/components/home/HomeActivityPanel.vue'
import HomeCoBuildPanel from '@/components/home/HomeCoBuildPanel.vue'
import HomeFeaturedContent from '@/components/home/HomeFeaturedContent.vue'
import HomeFellowshipBanner from '@/components/home/HomeFellowshipBanner.vue'
import HomeHeroPortal from '@/components/home/HomeHeroPortal.vue'
import HomeModuleGrid from '@/components/home/HomeModuleGrid.vue'
import HomeNoticeBar from '@/components/home/HomeNoticeBar.vue'
import HomeOfficialPanel from '@/components/home/HomeOfficialPanel.vue'
import HomeVisionPanel from '@/components/home/HomeVisionPanel.vue'
import articleCoverImage from '@/assets/公告模块卡片.webp'
import lifeCoverImage from '@/assets/未来扩展模块.webp'
import aiCoverImage from '@/assets/AI工具模块卡片.webp'
import activityCoverImage from '@/assets/活动模块.webp'
import {
  PC_EVENTS,
  PC_HOME,
  PC_ME,
  PC_PLAY,
  pcEventsPath,
  pcPath
} from '@/constants/pcPaths.js'

const announcements = ref([])
const articles = ref([])
const events = ref([])
const homeConfig = ref(null)
const platformStats = ref({
  userCount: 0,
  eventSignupCount: 0,
  dynamicsCount: 0,
  articleViewCount: 0,
  citiesCount: 0
})

const fallbackAnnouncements = [
  {
    id: 'fallback-notice-1',
    title: '用户建议中心已上线，欢迎提出你的梦想功能',
    summary: '平台会持续收集高价值建议，并逐步纳入后续迭代计划。',
    publishDate: '2026-04-29'
  },
  {
    id: 'fallback-notice-2',
    title: '平台门户首页升级为多模块统一入口',
    summary: '内容、活动、工具与连接服务已在首页聚合展示。',
    publishDate: '2026-04-26'
  }
]

const fallbackArticles = [
  {
    id: 'fallback-article-1',
    title: '长期关系中，稳定回应为什么重要',
    summary: '从真实表达、互相理解与持续陪伴三个角度，聊聊关系里的确定感。',
    category: '平台资讯',
    viewCount: 940,
    publishDate: '2026-04-28',
    coverUrl: articleCoverImage
  },
  {
    id: 'fallback-article-2',
    title: '春日漫步：感受生活的小美好',
    summary: '把日常记录下来，也是在给自己建立一个更清晰的生活坐标。',
    category: '生活随笔',
    viewCount: 156,
    publishDate: '2026-04-25',
    coverUrl: lifeCoverImage
  },
  {
    id: 'fallback-article-3',
    title: '今天，你对自己说了什么',
    summary: '每日心声模块帮助用户沉淀想法、记录成长与情绪变化。',
    category: '每日心声',
    viewCount: 870,
    publishDate: '2026-04-22',
    coverUrl: aiCoverImage
  },
  {
    id: 'fallback-article-4',
    title: '在 LoveCube 遇见了志同道合的 Ta',
    summary: 'LoveCube 是平台里的连接专区，服务真实、认真、可靠的互动。',
    category: '用户故事',
    viewCount: 203,
    publishDate: '2026-04-18',
    coverUrl: activityCoverImage
  }
]

const fallbackEvents = []

const fallbackChangelog = [
  { version: 'v1.4.0', title: '优化移动端体验，修复已知问题', date: '2026-04-29' },
  { version: 'v1.3.0', title: '新增问卷调查功能，支持匿名填写', date: '2026-04-26' },
  { version: 'v1.2.0', title: '上线每日心声板块，记录生活点滴', date: '2026-04-20' },
  { version: 'v1.1.0', title: '优化登录流程，提升系统稳定性', date: '2026-04-15' },
  { version: 'v1.0.0', title: '平台正式上线，基础功能发布', date: '2026-04-01' }
]

const fallbackPendingUpdates = [
  {
    id: 'pending-1',
    title: '部分页面加载偏慢',
    detail: '已定位到高峰时段响应波动，正在推进接口与缓存优化。',
    status: '排查中'
  },
  {
    id: 'pending-2',
    title: '活动报名提醒不够及时',
    detail: '计划新增站内消息与提醒频率设置，避免错过关键通知。',
    status: '开发中'
  },
  {
    id: 'pending-3',
    title: '移动端个别机型排版错位',
    detail: '正在补齐兼容性样式，逐步覆盖主流屏幕尺寸。',
    status: '待发布'
  }
]

const coreModules = [
  { title: '平台中心', desc: '资讯、文章、动态\n精彩内容尽在这里', to: PC_HOME, icon: '⚙', tone: 'pink' },
  { title: '联谊', desc: '完善资料，结识志同道合的朋友', to: '/fellowship', icon: '♥', tone: 'pink' },
  { title: '团队', desc: '寻找你的团体\n连接志同道合的伙伴', to: pcPath('groups'), icon: '◎', tone: 'green' },
  { title: '活动中心', desc: '线上线下活动\n发现有趣的人和事', to: PC_EVENTS, icon: '♔', tone: 'orange' },
  { title: 'AI 工具', desc: '智能助手、效率工具\n让生活更高效', to: pcPath('modules'), icon: '✣', tone: 'violet' },
  { title: '用户中心', desc: '个人资料、消息、动态\n管理你的多端空间', to: PC_ME, icon: '●', tone: 'blue' }
]

const notice = computed(() => {
  const configured = homeConfig.value?.notice || homeConfig.value?.announcement
  const first = announcements.value[0] || fallbackAnnouncements[0]
  return {
    title: configured?.title || first.title,
    text: configured?.text || configured?.summary || first.summary || '平台持续升级中，欢迎提出你的建议。',
    to: first.id && !String(first.id).startsWith('fallback')
      ? `/announcements/${first.id}`
      : pcPath('announcements')
  }
})

const heroStats = computed(() => [
  {
    label: '注册用户',
    value: formatStat(platformStats.value.userCount, '126+')
  },
  {
    label: '内容发布',
    value: formatStat(platformStats.value.dynamicsCount, '58+')
  },
  {
    label: '功能模块',
    value: `${coreModules.length}+`
  },
  {
    label: '持续运营中',
    value: '持续'
  }
])

const quickActionLinks = computed(() => [
  { label: '成长玩法', to: PC_PLAY },
  { label: '先看活动', to: PC_EVENTS },
  { label: '查看公告', to: pcPath('announcements') },
  { label: '去看看', to: '/fellowship' },
  { label: '个人中心', to: PC_ME }
])

const heroLiveFeed = computed(() => {
  const source = [
    ...announcements.value.slice(0, 3).map((item) => ({
      text: `公告更新：${item.title || '平台公告'}`
    })),
    ...events.value.slice(0, 3).map((item) => ({
      text: `活动上新：${item.title || '平台活动'}`
    })),
    ...articles.value.slice(0, 2).map((item) => ({
      text: `内容发布：${item.title || '精选内容'}`
    }))
  ]
  if (source.length) return source
  return [
    { text: '新用户指南：从活动中心开始更容易上手' },
    { text: '官方公告已更新，建议先看最近的变化' },
    { text: '内容专区持续更新，欢迎浏览精选文章' }
  ]
})

const homeRollingFeed = computed(() => [...heroLiveFeed.value, ...heroLiveFeed.value])

const officialItems = computed(() => {
  const source = announcements.value.length ? announcements.value : fallbackAnnouncements
  return source.slice(0, 5).map((item, index) => ({
    id: item.id || `announcement-${index}`,
    title: item.title || '平台公告',
    date: formatDate(item.publishDate || item.createdAt),
    tag: index === 0 ? '置顶' : '公告',
    to: item.id && !String(item.id).startsWith('fallback')
      ? `/announcements/${item.id}`
      : pcPath('announcements')
  }))
})

const changelogItems = computed(() => {
  const configured = Array.isArray(homeConfig.value?.changelog) ? homeConfig.value.changelog : []
  return (configured.length ? configured : fallbackChangelog).slice(0, 3)
})

const pendingItems = computed(() => {
  const configured = Array.isArray(homeConfig.value?.pendingUpdates)
    ? homeConfig.value.pendingUpdates
    : []
  return (configured.length ? configured : fallbackPendingUpdates).slice(0, 3)
})

const featuredItems = computed(() => {
  const source = articles.value.length ? articles.value : fallbackArticles
  return source.slice(0, 4).map((item, index) => ({
    id: item.id || `article-${index}`,
    title: item.title || '精选内容',
    summary: item.summary || item.description || '更多精选内容正在持续更新。',
    category: item.category || ['平台资讯', '生活随笔', '每日心声', '用户故事'][index] || '精选内容',
    meta: [item.authorName || '平台编辑', item.viewCount ? `${item.viewCount}阅读` : '', formatDate(item.publishDate || item.createdAt)].filter(Boolean).join(' · '),
    to: item.id && !String(item.id).startsWith('fallback') ? `/articles/${item.id}` : pcPath('articles'),
    tone: ['blue', 'green', 'violet', 'pink'][index % 4],
    cover: item.coverUrl || item.cover || [articleCoverImage, lifeCoverImage, aiCoverImage, activityCoverImage][index % 4]
  }))
})

const activityItems = computed(() => {
  const source = events.value.length ? events.value : fallbackEvents
  return source.slice(0, 2).map((item, index) => ({
    id: item.id || `event-${index}`,
    title: item.title || '平台活动',
    summary: item.summary || item.description || '活动服务持续开放。',
    date: formatDate(item.eventTime || item.startTime || item.createdAt),
    status: item.status || (index === 0 ? '报名中' : '即将开始'),
    to: item.id && !String(item.id).startsWith('fallback') ? pcEventsPath(item.id) : PC_EVENTS
  }))
})

const activityStats = computed(() => [
  { value: events.value.length, label: '进行中' },
  { value: Number(platformStats.value.eventSignupCount || 0), label: '报名人次' },
  { value: Number(platformStats.value.citiesCount || 0), label: '覆盖城市' }
])

function formatStat(value, fallback) {
  const numberValue = Number(value)
  if (!Number.isFinite(numberValue) || numberValue <= 0) return fallback
  return `${numberValue.toLocaleString()}+`
}

function formatDate(value) {
  if (!value) return '刚刚'
  const text = String(value).replace('T', ' ')
  return text.length > 10 ? text.slice(0, 10) : text
}

function unwrapList(res) {
  if (Array.isArray(res)) return res
  if (Array.isArray(res?.data)) return res.data
  if (Array.isArray(res?.data?.list)) return res.data.list
  if (Array.isArray(res?.data?.records)) return res.data.records
  if (Array.isArray(res?.list)) return res.list
  if (Array.isArray(res?.records)) return res.records
  if (Array.isArray(res?.content)) return res.content
  return []
}

onMounted(async () => {
  const [cfg, stats, ann, art, ev] = await Promise.allSettled([
    fetchHomeConfig(),
    fetchPlatformStats(),
    fetchAnnouncements({ status: 'published', limit: 8 }),
    fetchArticles({ status: 'published', limit: 8 }),
    fetchEvents({ status: 'published', limit: 4 })
  ])

  if (cfg.status === 'fulfilled') homeConfig.value = cfg.value || null
  if (stats.status === 'fulfilled' && stats.value) platformStats.value = { ...platformStats.value, ...stats.value }
  announcements.value = ann.status === 'fulfilled' ? unwrapList(ann.value) : fallbackAnnouncements
  articles.value = art.status === 'fulfilled' ? unwrapList(art.value) : fallbackArticles
  events.value = ev.status === 'fulfilled' ? unwrapList(ev.value) : []
})
</script>

<style scoped>
.portal-home {
  overflow-x: hidden;
  background:
    linear-gradient(180deg, #f3f7ff 0, var(--lc-bg) 520px, var(--lc-bg) 100%);
  color: var(--lc-text);
}

.portal-shell {
  width: min(100% - 64px, 1720px);
  margin: 0 auto;
  padding: var(--lc-space-4) 0 var(--lc-space-6);
}

.portal-split {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  gap: var(--lc-space-5);
  margin-top: var(--lc-space-5);
}

.home-live-feed {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: var(--lc-space-5);
  padding: 10px 12px;
  border: 1px solid rgba(148, 163, 184, 0.3);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.9);
}

.home-live-feed-label {
  flex: 0 0 auto;
  color: var(--lc-blue);
  font-size: 12px;
  font-weight: 900;
}

.home-live-feed-track {
  position: relative;
  height: 20px;
  overflow: hidden;
  flex: 1;
}

.home-live-feed-list {
  display: flex;
  flex-direction: column;
  animation: homeTicker 14s linear infinite;
}

.home-live-feed-list p {
  margin: 0;
  color: var(--lc-muted);
  font-size: 13px;
  line-height: 20px;
  white-space: nowrap;
}

@keyframes homeTicker {
  0% {
    transform: translateY(0);
  }
  100% {
    transform: translateY(-50%);
  }
}

.portal-split-accent {
  align-items: stretch;
}

@media (max-width: 980px) {
  .portal-shell {
    width: min(100% - 48px, 960px);
    padding-top: var(--lc-space-3);
  }

  .portal-split {
    grid-template-columns: 1fr;
    gap: var(--lc-space-4);
  }

  .home-live-feed {
    margin-top: var(--lc-space-4);
  }
}
</style>
