<template>
  <main class="page">
    <header class="search"><input v-model.trim="keyword" placeholder="搜索内容、活动、攻略"></header>
    <ContentTabs v-model="activeType" :tabs="contentTypes" />
    <ContentFilterBar v-model="activeSort" :sorts="PLATFORM_SORTS" />
    <ContentList v-if="filtered.length" :items="filtered" @like="handleLike" @comment="handleComment" />
    <ContentEmpty v-else @publish="goPublish" />
    <button class="fab" type="button" @click="goPublish">发布</button>
  </main>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import ContentTabs from '@/components/platform/content/ContentTabs.vue'
import ContentFilterBar from '@/components/platform/content/ContentFilterBar.vue'
import ContentList from '@/components/platform/content/ContentList.vue'
import ContentEmpty from '@/components/platform/content/ContentEmpty.vue'
import {
  fetchAnnouncements,
  fetchArticles,
  fetchEvents,
  fetchPlatformContentFeed
} from '@/api/platformContent.js'
import {
  commentPositiveShare,
  fetchPositiveShares,
  likePositiveShare,
  unlikePositiveShare
} from '@/api/positiveShare.js'
import { getLocalResources } from '@/api/localResources.js'
import { PLATFORM_CONTENT_TYPES, PLATFORM_SORTS } from '@/mock/platformContent.js'
import { userAvatarUrlFromApi } from '@/utils/displayFields.js'

const route = useRoute()
const router = useRouter()
const keyword = ref('')
const activeType = ref('local')
const activeSort = ref('latest')
const contentList = ref([])
const contentTypes = [{ key: 'local', label: '本地资源' }, ...PLATFORM_CONTENT_TYPES]

watch(() => route.query.type, (type) => {
  if (typeof type === 'string' && contentTypes.some(item => item.key === type)) activeType.value = type
}, { immediate: true })

const filtered = computed(() => {
  let list = contentList.value.filter(item => activeType.value === 'all' || item.type === activeType.value)
  if (keyword.value) {
    const q = keyword.value.toLowerCase()
    list = list.filter(item => item.title.toLowerCase().includes(q) || item.summary.toLowerCase().includes(q))
  }
  if (activeSort.value === 'hot') list = [...list].sort((a,b)=>(b.likeCount+b.commentCount)-(a.likeCount+a.commentCount))
  if (activeSort.value === 'recommend') list = [...list].sort((a,b)=>Number(b.pinned)-Number(a.pinned))
  if (activeSort.value === 'latest') list = [...list].sort((a,b)=>String(b.createdAt).localeCompare(String(a.createdAt)))
  return list
})

function goPublish(){ router.push('/platform/positive-share') }

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

/** 聚合接口体：兼容 { announcements, ... } 或 { data: { ... } } */
function unwrapContentFeedBody(raw) {
  if (!raw || typeof raw !== 'object' || Array.isArray(raw)) return null
  const inner = raw.data && typeof raw.data === 'object' && !Array.isArray(raw.data) ? raw.data : raw
  if (
    inner &&
    typeof inner === 'object' &&
    ('announcements' in inner || 'articles' in inner || 'events' in inner || 'positiveShares' in inner)
  ) {
    return inner
  }
  return null
}

function mergeNormalizedFromFeedBody(body) {
  const announcements = unwrapList(body.announcements).map(normalizeAnnouncement)
  const articles = unwrapList(body.articles).map(normalizeArticle)
  const events = unwrapList(body.events).map(normalizeEvent)
  const moods = unwrapList(body.positiveShares).map(normalizePositiveShare)
  return [...announcements, ...articles, ...events, ...moods]
}

async function loadContentListLegacy() {
  const [localRes, annRes, artRes, eventRes, moodRes] = await Promise.allSettled([
    getLocalResources({ type: 'all' }),
    fetchAnnouncements({ status: 'published', limit: 30 }),
    fetchArticles({ status: 'published', limit: 30 }),
    fetchEvents({ status: 'published', limit: 30 }),
    fetchPositiveShares({ tab: 'latest', pageNum: 1, pageSize: 50 })
  ])
  const locals = localRes.status === 'fulfilled' ? unwrapList(localRes.value).map(normalizeLocalResource) : []
  const announcements = annRes.status === 'fulfilled' ? unwrapList(annRes.value).map(normalizeAnnouncement) : []
  const articles = artRes.status === 'fulfilled' ? unwrapList(artRes.value).map(normalizeArticle) : []
  const events = eventRes.status === 'fulfilled' ? unwrapList(eventRes.value).map(normalizeEvent) : []
  const moods = moodRes.status === 'fulfilled' ? unwrapList(moodRes.value).map(normalizePositiveShare) : []
  return [...locals, ...announcements, ...articles, ...events, ...moods]
}

function resolveTypeFromArticle(item) {
  const category = String(item?.category || item?.tag || '').toLowerCase()
  const summary = `${item?.title || ''} ${item?.summary || ''}`.toLowerCase()
  if (category.includes('ai') || summary.includes('ai')) return 'ai'
  if (category.includes('攻略') || summary.includes('攻略')) return 'guide'
  if (category.includes('心声') || summary.includes('心声')) return 'mood'
  if (category.includes('动态') || summary.includes('动态')) return 'dynamic'
  if (category.includes('活动') || summary.includes('活动')) return 'event'
  return 'guide'
}

function normalizeArticle(item) {
  return {
    id: item.id ?? `article-${Math.random()}`,
    title: item.title || '未命名内容',
    summary: item.summary || item.description || '',
    type: resolveTypeFromArticle(item),
    cover: item.coverUrl || item.cover || '',
    images: [],
    tags: item.tag ? [item.tag] : [],
    authorName: item.authorName || item.author || '平台编辑',
    avatar: userAvatarUrlFromApi(item),
    createdAt: String(item.publishedAt || item.createdAt || '').slice(0, 10),
    likeCount: Number(item.likeCount || item.likes || 0),
    commentCount: Number(item.commentCount || 0),
    pinned: Boolean(item.pinned)
  }
}

function normalizeAnnouncement(item) {
  return {
    id: item.id ?? `announcement-${Math.random()}`,
    title: item.title || '平台公告',
    summary: item.summary || item.content || '',
    type: 'dynamic',
    cover: item.coverUrl || item.cover || '',
    images: [],
    tags: ['平台公告'],
    authorName: item.authorName || item.publisher || '平台运营',
    avatar: userAvatarUrlFromApi(item),
    createdAt: String(item.publishDate || item.createdAt || '').slice(0, 10),
    likeCount: Number(item.likeCount || 0),
    commentCount: Number(item.commentCount || 0),
    pinned: Boolean(item.pinned)
  }
}

function normalizeEvent(item) {
  return {
    id: item.id ?? `event-${Math.random()}`,
    title: item.title || '平台活动',
    summary: item.summary || item.description || '',
    type: 'event',
    cover: item.coverUrl || item.cover || '',
    images: [],
    tags: ['活动'],
    authorName: item.authorName || item.organizer || '活动中心',
    avatar: userAvatarUrlFromApi(item),
    createdAt: String(item.eventTime || item.startTime || item.createdAt || '').slice(0, 10),
    likeCount: Number(item.likeCount || 0),
    commentCount: Number(item.commentCount || 0),
    pinned: Boolean(item.pinned)
  }
}

function normalizePositiveShare(item) {
  const resolvedName =
    item.nickname ||
    item.nickName ||
    item.userNickname ||
    item.username ||
    item.userName ||
    item.accountName ||
    item.authorName ||
    item.author ||
    '用户'

  return {
    id: item.id ?? `mood-${Math.random()}`,
    title: item.title || '每日心声',
    summary: item.content || item.summary || '',
    type: 'mood',
    cover: item.coverUrl || item.cover || '',
    images: Array.isArray(item.images) ? item.images : [],
    tags: item.category ? [item.category] : [],
    authorName: resolvedName,
    avatar: userAvatarUrlFromApi(item),
    createdAt: String(item.createdAt || '').slice(0, 10),
    likeCount: Number(item.encourageCount || item.likeCount || 0),
    commentCount: Number(item.commentCount || 0),
    pinned: Boolean(item.pinned),
    liked: Boolean(item.liked),
    source: 'mood'
  }
}

function normalizeLocalResource(item) {
  return {
    id: item.id ?? `local-${Math.random()}`,
    title: item.title || '本地资源',
    summary: item.summary || '暂无简介',
    type: 'local',
    cover: item.coverUrl || '',
    images: [],
    tags: [item.type || '本地资源', item.location || '同城'],
    authorName: '本地推荐',
    avatar: '',
    createdAt: String(item.updatedAt || item.createdAt || '').slice(0, 10),
    likeCount: Number(item.heat || 0),
    commentCount: Number(item.interestCount || 0),
    pinned: Number(item.heat || 0) >= 20
  }
}

async function handleLike(item) {
  if (!item || item.source !== 'mood') return
  try {
    const res = item.liked ? await unlikePositiveShare(item.id) : await likePositiveShare(item.id)
    item.liked = Boolean(res?.liked ?? !item.liked)
    item.likeCount = Number(res?.encourageCount ?? item.likeCount ?? 0)
  } catch {
    // keep UI stable on failure
  }
}

async function handleComment(item) {
  if (!item || item.source !== 'mood') return
  const content = window.prompt('请输入评论内容')
  const text = String(content || '').trim()
  if (!text) return
  try {
    const res = await commentPositiveShare(item.id, { content: text })
    item.commentCount = Number(res?.commentCount ?? item.commentCount ?? 0)
  } catch {
    // keep UI stable on failure
  }
}

onMounted(async () => {
  try {
    const [localRaw, raw] = await Promise.all([
      getLocalResources({ type: 'all' }).catch(() => []),
      fetchPlatformContentFeed({ limit: 30 })
    ])
    const localItems = unwrapList(localRaw).map(normalizeLocalResource)
    const body = unwrapContentFeedBody(raw)
    if (body) {
      contentList.value = [...localItems, ...mergeNormalizedFromFeedBody(body)]
      return
    }
  } catch {
    // 未部署 content-feed 或网络错误：回退四条接口（与改聚合接口前行为一致）
  }
  contentList.value = await loadContentListLegacy()
})
</script>

<style scoped>
.page{width:min(100% - 24px,560px);margin:16px auto;display:grid;gap:12px;padding-bottom:calc(92px + env(safe-area-inset-bottom));}
.search input{width:100%;height:40px;border:1px solid var(--lc-border);border-radius:10px;padding:0 12px;background:var(--lc-surface)}
.fab{position:fixed;right:16px;bottom:calc(74px + env(safe-area-inset-bottom));height:44px;padding:0 16px;border:0;border-radius:22px;background:var(--lc-blue);color:var(--lc-surface);font-weight:700;box-shadow:var(--lc-shadow-blue);}
</style>
