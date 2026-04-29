<template>
  <main class="portal-home">
    <div class="portal-shell">
      <HomeNoticeBar :notice="notice" />
      <HomeHeroPortal :stats="heroStats" />
      <HomeModuleGrid :modules="coreModules" />
      <HomeOfficialPanel :official-items="officialItems" :changelog-items="changelogItems" />
      <HomeFeaturedContent :items="featuredItems" />
      <div class="portal-split portal-split-accent">
        <HomeFellowshipBanner />
        <HomeActivityPanel :events="activityItems" :stats="activityStats" />
      </div>
      <div class="portal-split">
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

const fallbackEvents = [
  {
    id: 'fallback-event-1',
    title: '五月主题共创开放日',
    summary: '围绕内容、活动、社交连接与 AI 工具，一起讨论平台下一步。',
    eventTime: '2026-05-05',
    status: '报名中'
  },
  {
    id: 'fallback-event-2',
    title: '线上轻分享：如何写下好建议',
    summary: '把灵感变成可迭代的功能需求，让平台更贴近真实场景。',
    eventTime: '2026-05-12',
    status: '即将开始'
  }
]

const fallbackChangelog = [
  { version: 'v1.4.0', title: '优化移动端体验，修复已知问题', date: '2026-04-29' },
  { version: 'v1.3.0', title: '新增问卷调查功能，支持匿名填写', date: '2026-04-26' },
  { version: 'v1.2.0', title: '上线每日心声板块，记录生活点滴', date: '2026-04-20' },
  { version: 'v1.1.0', title: '优化登录流程，提升系统稳定性', date: '2026-04-15' },
  { version: 'v1.0.0', title: '平台正式上线，基础功能发布', date: '2026-04-01' }
]

const coreModules = [
  { title: '平台中心', desc: '资讯、文章、动态\n精彩内容尽在这里', to: '/modules', icon: '⚙', tone: 'pink' },
  { title: 'LoveCube', desc: '联谊交友专区\n真诚相遇，认真开始', to: '/fellowship-intro', icon: '♥', tone: 'pink' },
  { title: '活动中心', desc: '线上线下活动\n发现有趣的人和事', to: '/events', icon: '♔', tone: 'orange' },
  { title: 'AI 工具', desc: '智能助手、效率工具\n让生活更高效', to: '/modules', icon: '✣', tone: 'violet' },
  { title: '用户中心', desc: '个人资料、消息、动态\n管理你的多端空间', to: '/me', icon: '●', tone: 'blue' },
  { title: '官方动态', desc: '公告、更新日志\n了解平台最新进展', to: '/announcements', icon: '▤', tone: 'green' }
]

const notice = computed(() => {
  const configured = homeConfig.value?.notice || homeConfig.value?.announcement
  const first = announcements.value[0] || fallbackAnnouncements[0]
  return {
    title: configured?.title || first.title,
    text: configured?.text || configured?.summary || first.summary || '平台持续升级中，欢迎提出你的建议。',
    to: first.id && !String(first.id).startsWith('fallback') ? `/announcements/${first.id}` : '/announcements'
  }
})

const heroStats = computed(() => [
  {
    label: '注册用户',
    value: formatStat(platformStats.value.userCount, '126+')
  },
  {
    label: '内容发布',
    value: formatStat(platformStats.value.dynamicsCount || platformStats.value.articleViewCount, '58+')
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

const officialItems = computed(() => {
  const source = announcements.value.length ? announcements.value : fallbackAnnouncements
  return source.slice(0, 5).map((item, index) => ({
    id: item.id || `announcement-${index}`,
    title: item.title || '平台公告',
    date: formatDate(item.publishDate || item.createdAt),
    tag: index === 0 ? '置顶' : '公告',
    to: item.id && !String(item.id).startsWith('fallback') ? `/announcements/${item.id}` : '/announcements'
  }))
})

const changelogItems = computed(() => {
  const configured = Array.isArray(homeConfig.value?.changelog) ? homeConfig.value.changelog : []
  return (configured.length ? configured : fallbackChangelog).slice(0, 5)
})

const featuredItems = computed(() => {
  const source = articles.value.length ? articles.value : fallbackArticles
  return source.slice(0, 4).map((item, index) => ({
    id: item.id || `article-${index}`,
    title: item.title || '精选内容',
    summary: item.summary || item.description || '更多精选内容正在持续更新。',
    category: item.category || ['平台资讯', '生活随笔', '每日心声', '用户故事'][index] || '精选内容',
    meta: [item.authorName || '平台编辑', item.viewCount ? `${item.viewCount}阅读` : '', formatDate(item.publishDate || item.createdAt)].filter(Boolean).join(' · '),
    to: item.id && !String(item.id).startsWith('fallback') ? `/articles/${item.id}` : '/articles',
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
    to: item.id && !String(item.id).startsWith('fallback') ? `/events/${item.id}` : '/events'
  }))
})

const activityStats = computed(() => [
  { value: events.value.length || 3, label: '进行中' },
  { value: platformStats.value.eventSignupCount || 5, label: '即将开始' },
  { value: 12, label: '已结束' }
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
  events.value = ev.status === 'fulfilled' ? unwrapList(ev.value) : fallbackEvents
})
</script>

<style scoped>
.portal-home {
  background:
    linear-gradient(180deg, #f3f7ff 0, var(--lc-bg) 520px, var(--lc-bg) 100%);
  color: var(--lc-text);
}

.portal-shell {
  width: min(100% - 32px, 1720px);
  margin: 0 auto;
  padding: var(--lc-space-4) 0 var(--lc-space-6);
}

.portal-split {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  gap: var(--lc-space-5);
  margin-top: var(--lc-space-5);
}

.portal-split-accent {
  align-items: stretch;
}

@media (max-width: 980px) {
  .portal-shell {
    width: min(100% - 28px, 720px);
    padding-top: var(--lc-space-3);
  }

  .portal-split {
    grid-template-columns: 1fr;
    gap: var(--lc-space-4);
  }
}

@media (max-width: 560px) {
  .portal-shell {
    width: min(100% - 20px, 420px);
    padding-bottom: var(--lc-space-10);
  }
}
</style>
