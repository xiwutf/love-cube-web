<template>
  <section class="featured-page">
    <section class="featured-hero">
      <div class="hero-copy">
        <p class="hero-kicker">Featured Content</p>
        <h1>精选内容资讯</h1>
        <p>发现有价值的内容，获取实用技巧与最新资讯，丰富你的社交生活</p>
      </div>
      <div class="hero-visual" aria-hidden="true">
        <img :src="heroImage" alt="" />
      </div>
    </section>

    <section class="featured-shell">
      <div class="featured-main">
        <div class="toolbar">
          <div class="category-tabs" aria-label="内容分类">
            <button
              v-for="cat in categories"
              :key="cat.value"
              class="category-tab"
              :class="{ active: activeCategory === cat.value }"
              type="button"
              @click="setCategory(cat.value)"
            >
              {{ cat.label }}
            </button>
          </div>

          <select v-model="sortMode" class="sort-select" aria-label="内容排序">
            <option value="latest">最新发布</option>
            <option value="hot">热度优先</option>
            <option value="liked">点赞最多</option>
          </select>
        </div>

        <div v-if="featuredLead" class="content-layout">
          <router-link :to="`/articles/${featuredLead.id}`" class="article-card article-card-lead">
            <div class="article-cover">
              <img :src="featuredLead.coverUrl" :alt="featuredLead.title" loading="lazy" />
              <span class="article-label label-hot">置顶</span>
              <span class="article-label label-category">{{ featuredLead.sourceLabel }}</span>
            </div>
            <div class="article-body">
              <h2>{{ featuredLead.title }}</h2>
              <p>{{ featuredLead.summary }}</p>
              <div class="article-meta">
                <span>{{ featuredLead.author }}</span>
                <span>{{ featuredLead.reads }}阅读</span>
                <span>{{ featuredLead.likes }}</span>
                <time :datetime="featuredLead.date">{{ featuredLead.date }}</time>
              </div>
            </div>
          </router-link>

          <router-link
            v-for="item in secondaryFeatured"
            :key="item.id"
            :to="`/articles/${item.id}`"
            class="article-card article-card-compact"
          >
            <div class="article-cover">
              <img :src="item.coverUrl" :alt="item.title" loading="lazy" />
              <span class="article-label" :class="item.labelClass">{{ item.sourceLabel }}</span>
            </div>
            <div class="article-body">
              <h3>{{ item.title }}</h3>
              <p>{{ item.summary }}</p>
              <div class="article-meta">
                <span>{{ item.author }}</span>
                <span>{{ item.reads }}阅读</span>
                <time :datetime="item.date">{{ item.date }}</time>
              </div>
            </div>
          </router-link>
        </div>

        <div class="article-grid">
          <router-link
            v-for="item in visibleList"
            :key="item.id"
            :to="`/articles/${item.id}`"
            class="article-card article-card-small"
          >
            <div class="article-cover">
              <img :src="item.coverUrl" :alt="item.title" loading="lazy" />
              <span class="article-label" :class="item.labelClass">{{ item.sourceLabel }}</span>
            </div>
            <div class="article-body">
              <h3>{{ item.title }}</h3>
              <p>{{ item.summary }}</p>
              <div class="article-meta">
                <span>{{ item.author }}</span>
                <span>{{ item.reads }}阅读</span>
                <span>{{ item.likes }}</span>
                <time :datetime="item.date">{{ item.date }}</time>
              </div>
            </div>
          </router-link>
        </div>

        <article v-if="!loading && !filteredList.length" class="empty-state">
          <h3>暂无内容</h3>
          <p>当前筛选条件下还没有已发布文章，换个分类再看看。</p>
        </article>

        <div v-if="hasMore" class="load-more-wrap">
          <button type="button" class="load-more-btn" @click="page++">查看更多内容</button>
        </div>

        <section class="content-policy">
          <div class="policy-icon" aria-hidden="true">♥</div>
          <div>
            <h2>内容规范</h2>
            <p>我们鼓励优质内容分享，严禁发布违法违规、低俗色情、恶意攻击等不良信息，共同维护良好的社区环境。</p>
          </div>
          <router-link to="/policies/content-policy">查看内容规范</router-link>
        </section>
      </div>

      <aside class="featured-aside">
        <section class="aside-card">
          <div class="aside-head">
            <h2>热门话题</h2>
            <router-link to="/articles">查看发布</router-link>
          </div>
          <ol class="topic-list">
            <li v-for="topic in topics" :key="topic.name">
              <span class="topic-rank">{{ topic.rank }}</span>
              <span class="topic-name"># {{ topic.name }}</span>
              <span class="topic-heat">{{ topic.heat }}热度</span>
            </li>
          </ol>
        </section>

        <section class="share-card">
          <div>
            <h2>分享你的故事</h2>
            <p>记录生活、分享美好，结识更多有趣的人</p>
            <router-link to="/articles">进入内容中心</router-link>
          </div>
          <img :src="shareImage" alt="" loading="lazy" />
        </section>

        <section class="aside-card">
          <div class="aside-head">
            <h2>推荐作者</h2>
            <router-link to="/articles">查看更多</router-link>
          </div>
          <div class="author-list">
            <article v-for="author in authors" :key="author.name" class="author-item">
              <span class="author-avatar" :class="author.tone">{{ author.initial }}</span>
              <div>
                <h3>{{ author.name }} <em>{{ author.badge }}</em></h3>
                <p>{{ author.desc }}</p>
              </div>
              <button type="button">关注</button>
            </article>
          </div>
        </section>
      </aside>
    </section>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { fetchArticles } from '@/api/platformContent.js'
import heroImage from '@/assets/首页首屏右侧大图.png'
import leadImage from '@/assets/联谊专区.png'
import eventImage from '@/assets/活动模块.png'
import aiImage from '@/assets/AI工具模块卡片.png'
import announcementImage from '@/assets/公告模块卡片.png'
import ctaImage from '@/assets/底部横幅 CTA.png'
import moduleImage from '@/assets/未来扩展模块.png'

const PAGE_SIZE = 4
const route = useRoute()
const loading = ref(false)
const allItems = ref([])
const activeCategory = ref('all')
const sortMode = ref('latest')
const page = ref(1)

const categories = [
  { label: '全部', value: 'all' },
  { label: '平台资讯', value: '平台资讯' },
  { label: '活动指南', value: '活动指南' },
  { label: '活动中心', value: '活动中心' },
  { label: '平台攻略', value: '平台攻略' },
  { label: 'AI工具', value: 'AI工具' },
  { label: '本地服务', value: '本地服务' }
]

const fallbackArticles = [
  {
    id: 'social-icebreaker',
    title: '平台账号安全升级指南',
    summary: '从登录保护、通知设置到密码策略，三步提升账号安全。',
    category: '平台资讯',
    author: 'Love Cube官方',
    reads: '2.4k',
    likes: 128,
    date: '2026-04-28',
    coverUrl: leadImage,
    hot: 128
  },
  {
    id: 'may-event-guide',
    title: '五一平台活动参与指南',
    summary: '活动亮点、报名方式、注意事项一文看懂',
    category: '活动指南',
    author: '活动小助手',
    reads: '1.8k',
    likes: 86,
    date: '2026-04-26',
    coverUrl: eventImage,
    hot: 86
  },
  {
    id: 'healthy-relationship',
    title: '内容互动礼仪与社区规范',
    summary: '帮助你在平台内高质量表达与互动，减少沟通误解。',
    category: '平台资讯',
    author: '心语心理',
    reads: '1.3k',
    likes: 136,
    date: '2026-04-25',
    coverUrl: ctaImage,
    hot: 136
  },
  {
    id: 'city-cafe-map',
    title: '城市生活服务精选清单',
    summary: '覆盖餐饮、运动、学习等场景的一站式服务推荐。',
    category: '本地服务',
    author: '生活探索家',
    reads: '1.2k',
    likes: 74,
    date: '2026-04-24',
    coverUrl: announcementImage,
    hot: 74
  },
  {
    id: 'love-cube-tips',
    title: 'Love Cube 使用技巧大全',
    summary: '功能使用指南，玩转平台不迷路',
    category: '平台攻略',
    author: '官方小助手',
    reads: '2.7k',
    likes: 102,
    date: '2026-04-23',
    coverUrl: moduleImage,
    hot: 102
  },
  {
    id: 'ai-social-helper',
    title: 'AI 助手：你的社交好帮手',
    summary: '内容推荐、话题建议、表达辅助',
    category: 'AI工具',
    author: 'AI小智',
    reads: '1.6k',
    likes: 98,
    date: '2026-04-22',
    coverUrl: aiImage,
    hot: 98
  },
  {
    id: 'weekend-city-plan',
    title: '本周活动中心热门主题',
    summary: '快速了解近期平台活动中心最受关注的专题。',
    category: '活动中心',
    author: '城市向导',
    reads: '1.9k',
    likes: 95,
    date: '2026-04-21',
    coverUrl: eventImage,
    hot: 95
  }
]

const topics = [
  { rank: 1, name: '五一联谊活动', heat: '12.4k' },
  { rank: 2, name: '如何快速破冰', heat: '8.7k' },
  { rank: 3, name: '恋爱心理学', heat: '6.9k' },
  { rank: 4, name: '城市约会圣地', heat: '5.3k' },
  { rank: 5, name: '社交技巧分享', heat: '4.8k' }
]

const authors = [
  { name: 'Love Cube官方', badge: '官方', desc: '内容创作者与策展', initial: 'L', tone: 'pink' },
  { name: '心语心理', badge: '达人', desc: '心理学研究者，情感专栏作者', initial: '心', tone: 'blue' },
  { name: '生活探索家', badge: '达人', desc: '热爱生活，分享美好日常', initial: '生', tone: 'green' },
  { name: '活动小助手', badge: '官方', desc: '活动策划专家', initial: '活', tone: 'violet' }
]

const shareImage = ctaImage

const normalizedItems = computed(() => allItems.value.map((item, index) => {
  const fallback = fallbackArticles[index % fallbackArticles.length]
  const category = item.category || item.tag || fallback.category
  return {
    id: item.id || fallback.id,
    title: item.title || fallback.title,
    summary: item.summary || fallback.summary,
    category,
    sourceLabel: sourceLabelByCategory(category),
    author: item.authorName || item.author || fallback.author,
    reads: formatCount(item.viewCount || item.views || fallback.reads),
    likes: item.likeCount || item.likes || fallback.likes,
    date: formatDate(item.publishedAt || item.createdAt || item.date || fallback.date),
    coverUrl: item.coverUrl || item.cover || fallback.coverUrl,
    hot: Number(item.viewCount || item.likes || fallback.hot || 0),
    labelClass: labelClass(category)
  }
}))

const filteredList = computed(() => {
  const keyword = String(route.query.keyword || '').trim().toLowerCase()
  let list = normalizedItems.value

  if (activeCategory.value !== 'all') {
    list = list.filter(item => item.category === activeCategory.value)
  }

  if (keyword) {
    list = list.filter(item =>
      item.title.toLowerCase().includes(keyword) ||
      item.summary.toLowerCase().includes(keyword) ||
      item.category.toLowerCase().includes(keyword)
    )
  }

  return [...list].sort((a, b) => {
    if (sortMode.value === 'hot') return b.hot - a.hot
    if (sortMode.value === 'liked') return Number(b.likes) - Number(a.likes)
    return new Date(b.date) - new Date(a.date)
  })
})

const featuredLead = computed(() => filteredList.value[0])
const secondaryFeatured = computed(() => filteredList.value.slice(1, 3))
const visibleList = computed(() => filteredList.value.slice(3, 3 + page.value * PAGE_SIZE))
const hasMore = computed(() => filteredList.value.length > 3 + page.value * PAGE_SIZE)

function setCategory(value) {
  activeCategory.value = value
  page.value = 1
}

function formatCount(value) {
  if (typeof value === 'string') return value
  const count = Number(value || 0)
  if (count >= 10000) return `${(count / 10000).toFixed(1)}w`
  if (count >= 1000) return `${(count / 1000).toFixed(1)}k`
  return String(count || '1.2k')
}

function formatDate(value) {
  if (!value) return '2026-04-28'
  return String(value).slice(0, 10)
}

function labelClass(category) {
  const map = {
    平台资讯: 'label-platform',
    活动指南: 'label-event',
    活动中心: 'label-event',
    平台攻略: 'label-platform',
    AI工具: 'label-ai',
    本地服务: 'label-life'
  }
  return map[category] || 'label-platform'
}

function sourceLabelByCategory(category) {
  const mapping = {
    平台资讯: '[平台资讯]',
    活动指南: '[活动中心]',
    活动中心: '[活动中心]',
    平台攻略: '[平台资讯]',
    AI工具: '[AI工具]',
    本地服务: '[本地服务]'
  }
  return mapping[category] || '[平台资讯]'
}

onMounted(async () => {
  loading.value = true
  try {
    const data = await fetchArticles({ status: 'published' })
    allItems.value = Array.isArray(data) && data.length ? data : fallbackArticles
  } catch {
    allItems.value = fallbackArticles
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.featured-page {
  background: #f7f9fd;
  padding-bottom: 36px;
}

.featured-hero {
  position: relative;
  overflow: hidden;
  min-height: 268px;
  display: grid;
  grid-template-columns: minmax(0, 1.08fr) minmax(380px, 620px);
  align-items: center;
  gap: clamp(24px, 3vw, 44px);
  max-width: 1920px;
  margin: 0 auto;
  padding: 42px max(24px, calc((100vw - 1780px) / 2 + 24px));
  background:
    linear-gradient(90deg, rgba(248, 251, 255, 0.98) 0%, rgba(238, 244, 255, 0.88) 58%, rgba(255, 242, 248, 0.92) 100%);
  border-bottom: 1px solid #e6edf7;
}

.featured-hero::before,
.featured-hero::after {
  content: '';
  position: absolute;
  pointer-events: none;
  border-radius: 50%;
  filter: blur(2px);
}

.featured-hero::before {
  width: 320px;
  height: 320px;
  right: -120px;
  top: -140px;
  background: radial-gradient(circle, rgba(124, 92, 255, 0.16) 0%, rgba(124, 92, 255, 0) 72%);
}

.featured-hero::after {
  width: 280px;
  height: 280px;
  left: -140px;
  bottom: -150px;
  background: radial-gradient(circle, rgba(245, 72, 120, 0.16) 0%, rgba(245, 72, 120, 0) 74%);
}

.hero-copy {
  position: relative;
  z-index: 1;
  max-width: 840px;
}

.hero-kicker {
  margin: 0 0 10px;
  color: #5b6ff7;
  font-size: 13px;
  font-weight: 800;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.hero-copy h1 {
  margin: 0;
  color: #0f172a;
  font-size: clamp(38px, 3.4vw, 52px);
  line-height: 1.15;
  font-weight: 900;
}

.hero-copy p:last-child {
  margin: 14px 0 0;
  color: #526173;
  font-size: 18px;
  line-height: 1.7;
}

.hero-visual {
  position: relative;
  z-index: 1;
  justify-self: center;
  width: min(100%, 560px);
  aspect-ratio: 2 / 1;
}

.hero-visual img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.featured-shell {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 380px;
  gap: clamp(22px, 2.4vw, 34px);
  width: min(1880px, calc(100% - 40px));
  margin: 30px auto 0;
}

.featured-main {
  min-width: 0;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  margin-bottom: 24px;
}

.category-tabs {
  display: flex;
  gap: 10px;
  flex-wrap: nowrap;
  min-width: 0;
  overflow-x: auto;
  padding-bottom: 4px;
  scrollbar-width: thin;
}

.category-tab,
.sort-select,
.load-more-btn {
  min-height: 38px;
  border: 1px solid #e5ebf3;
  border-radius: 8px;
  background: #fff;
  color: #48576b;
  font-size: 14px;
  font-weight: 700;
}

.category-tab {
  flex: 0 0 auto;
  padding: 0 16px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.category-tab:hover,
.category-tab.active {
  color: #fff;
  border-color: #ff5f8f;
  background: linear-gradient(135deg, #ff668f, #f54878);
  box-shadow: 0 10px 24px rgba(245, 72, 120, 0.22);
}

.sort-select {
  flex: 0 0 118px;
  padding: 0 12px;
  outline: none;
}

.content-layout {
  display: grid;
  grid-template-columns: minmax(0, 1.34fr) repeat(2, minmax(280px, 0.82fr));
  gap: 20px;
}

.article-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(250px, 1fr));
  gap: 20px;
  margin-top: 24px;
}

.article-card {
  min-width: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  color: inherit;
  text-decoration: none;
  border: 1px solid #e5ebf3;
  border-radius: 10px;
  background: #fff;
  box-shadow: 0 14px 36px rgba(15, 23, 42, 0.07);
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.article-card:hover {
  transform: translateY(-2px);
  border-color: #ffc6d7;
  box-shadow: 0 16px 34px rgba(15, 23, 42, 0.1);
}

.article-card-lead {
  grid-row: span 2;
  box-shadow: 0 20px 46px rgba(15, 23, 42, 0.12);
}

.article-card-lead:hover {
  transform: translateY(-4px);
  border-color: #ffb8ce;
  box-shadow: 0 24px 54px rgba(15, 23, 42, 0.16);
}

.article-card-compact {
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.08);
}

.article-cover {
  position: relative;
  aspect-ratio: 16 / 9;
  overflow: hidden;
  background: #eaf0fb;
}

.article-card-lead .article-cover {
  aspect-ratio: 16 / 6.6;
}

.article-cover img {
  width: 100%;
  height: 100%;
  display: block;
  object-fit: cover;
}

.article-label {
  position: absolute;
  top: 12px;
  left: 12px;
  display: inline-flex;
  align-items: center;
  min-height: 24px;
  padding: 0 12px;
  border-radius: 999px;
  color: #fff;
  font-size: 12px;
  font-weight: 800;
  background: #5b6ff7;
}

.label-category {
  top: auto;
  bottom: 12px;
  background: #f54878;
}

.label-hot,
.label-social {
  background: #f54878;
}

.label-event,
.label-life {
  background: #f59e0b;
}

.label-psychology,
.label-platform {
  background: #7c5cff;
}

.label-city {
  background: #ef6b3f;
}

.label-ai {
  background: #29b980;
}

.article-body {
  display: flex;
  flex: 1;
  flex-direction: column;
  padding: 20px;
}

.article-body h2,
.article-body h3 {
  margin: 0;
  color: #122033;
  line-height: 1.35;
  font-weight: 900;
}

.article-body h2 {
  font-size: 22px;
}

.article-body h3 {
  font-size: 17px;
}

.article-body p {
  margin: 10px 0 0;
  color: #64748b;
  font-size: 14px;
  line-height: 1.7;
}

.article-meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: auto;
  padding-top: 16px;
  color: #8896aa;
  font-size: 12px;
}

.featured-aside {
  display: grid;
  gap: 18px;
  align-content: start;
}

.aside-card,
.share-card,
.content-policy,
.empty-state {
  border: 1px solid #e5ebf3;
  border-radius: 10px;
  background: #fff;
  box-shadow: 0 14px 36px rgba(15, 23, 42, 0.07);
}

.aside-card {
  padding: 20px;
}

.aside-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.aside-head h2,
.share-card h2,
.content-policy h2,
.empty-state h3 {
  margin: 0;
  color: #122033;
  font-size: 18px;
  font-weight: 900;
}

.aside-head a,
.content-policy a {
  color: #5b6ff7;
  font-size: 13px;
  font-weight: 800;
  text-decoration: none;
}

.topic-list {
  display: grid;
  gap: 14px;
  margin: 16px 0 0;
  padding: 0;
  list-style: none;
}

.topic-list li {
  display: grid;
  grid-template-columns: 24px minmax(0, 1fr) auto;
  align-items: center;
  gap: 10px;
  color: #334155;
  font-size: 14px;
  font-weight: 800;
}

.topic-rank {
  display: grid;
  place-items: center;
  width: 20px;
  height: 20px;
  border-radius: 5px;
  background: #edf2f7;
  color: #64748b;
  font-size: 12px;
}

.topic-list li:nth-child(-n + 3) .topic-rank {
  color: #fff;
  background: #ff6d92;
}

.topic-heat {
  color: #8b98aa;
  font-size: 12px;
  font-weight: 700;
}

.share-card {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 96px;
  align-items: center;
  gap: 14px;
  padding: 20px;
  background:
    linear-gradient(135deg, #fff7fa 0%, #fff 52%, #ffe6ee 100%);
  border-color: #ffd5e2;
}

.share-card p {
  margin: 8px 0 16px;
  color: #8b6474;
  font-size: 13px;
  line-height: 1.6;
}

.share-card a {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 34px;
  padding: 0 18px;
  border-radius: 999px;
  color: #fff;
  background: #f54878;
  font-size: 13px;
  font-weight: 800;
  text-decoration: none;
}

.share-card img {
  width: 96px;
  height: 96px;
  object-fit: contain;
}

.author-list {
  display: grid;
  gap: 14px;
  margin-top: 14px;
}

.author-item {
  display: grid;
  grid-template-columns: 40px minmax(0, 1fr) auto;
  align-items: center;
  gap: 12px;
}

.author-avatar {
  display: grid;
  place-items: center;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  color: #fff;
  font-size: 14px;
  font-weight: 900;
}

.author-avatar.pink {
  background: #f54878;
}

.author-avatar.blue {
  background: #68a1ff;
}

.author-avatar.green {
  background: #36c083;
}

.author-avatar.violet {
  background: #a66dff;
}

.author-item h3 {
  margin: 0;
  color: #122033;
  font-size: 14px;
  font-weight: 900;
}

.author-item em {
  margin-left: 4px;
  padding: 1px 6px;
  border-radius: 999px;
  background: #fff0f5;
  color: #f54878;
  font-size: 11px;
  font-style: normal;
}

.author-item p {
  margin: 4px 0 0;
  color: #7c8a9d;
  font-size: 12px;
}

.author-item button {
  min-height: 30px;
  padding: 0 14px;
  border: 1px solid #ffd4e0;
  border-radius: 999px;
  color: #f54878;
  background: #fff7fa;
  font-size: 12px;
  font-weight: 800;
  cursor: pointer;
}

.content-policy {
  display: grid;
  grid-template-columns: 46px minmax(0, 1fr) auto;
  align-items: center;
  gap: 18px;
  margin-top: 28px;
  padding: 22px;
  border-color: #ffd5e2;
  background: linear-gradient(135deg, #fff7fa 0%, #fff 70%);
}

.policy-icon {
  display: grid;
  place-items: center;
  width: 46px;
  height: 46px;
  border-radius: 50%;
  color: #fff;
  background: #ff6d92;
  font-size: 20px;
}

.content-policy p,
.empty-state p {
  margin: 8px 0 0;
  color: #64748b;
  font-size: 14px;
  line-height: 1.7;
}

.load-more-wrap {
  display: flex;
  justify-content: center;
  margin-top: 22px;
}

.load-more-btn {
  padding: 0 28px;
  cursor: pointer;
}

.load-more-btn:hover {
  color: #f54878;
  border-color: #ffc6d7;
}

.empty-state {
  margin-top: 24px;
  padding: 28px;
}

@media (max-width: 1440px) {
  .featured-hero {
    min-height: 236px;
    grid-template-columns: minmax(0, 1fr) minmax(320px, 500px);
    padding: 34px max(20px, calc((100vw - 1320px) / 2 + 20px));
  }

  .featured-hero::before {
    width: 260px;
    height: 260px;
    right: -120px;
    top: -130px;
  }

  .featured-hero::after {
    width: 230px;
    height: 230px;
    left: -120px;
    bottom: -140px;
  }

  .hero-copy h1 {
    font-size: clamp(34px, 3.1vw, 44px);
  }

  .hero-copy p:last-child {
    font-size: 16px;
  }

  .featured-shell {
    grid-template-columns: minmax(0, 1fr) 320px;
    width: min(1400px, calc(100% - 32px));
    gap: 24px;
    margin-top: 24px;
  }

  .content-layout {
    grid-template-columns: minmax(0, 1.16fr) repeat(2, minmax(240px, 0.72fr));
    gap: 16px;
  }

  .article-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 16px;
  }
}

@media (max-width: 1199px) {
  .featured-shell {
    grid-template-columns: 1fr;
    width: calc(100% - 32px);
  }

  .featured-aside {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .content-layout {
    grid-template-columns: minmax(0, 1fr) minmax(240px, 0.85fr);
  }

  .article-card-lead {
    grid-row: auto;
    grid-column: 1 / -1;
  }

  .article-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 767px) {
  .featured-hero {
    grid-template-columns: 1fr;
    min-height: auto;
    gap: 14px;
    margin-top: 0;
    padding: 24px 16px 22px;
  }

  .featured-hero::before,
  .featured-hero::after {
    display: none;
  }

  .hero-copy h1 {
    font-size: 32px;
  }

  .hero-copy p:last-child {
    font-size: 15px;
    line-height: 1.65;
  }

  .hero-visual {
    width: 100%;
  }

  .featured-shell {
    width: calc(100% - 24px);
    margin-top: 14px;
    gap: 18px;
  }

  .toolbar {
    align-items: stretch;
    flex-direction: column;
  }

  .category-tabs {
    display: flex;
    gap: 8px;
    overflow-x: auto;
    padding-bottom: 2px;
  }

  .category-tab {
    flex: 0 0 auto;
    min-height: 36px;
    padding: 0 10px;
  }

  .sort-select {
    flex-basis: auto;
    width: 100%;
  }

  .content-layout,
  .article-grid,
  .featured-aside {
    grid-template-columns: 1fr;
  }

  .article-body {
    padding: 16px;
  }

  .content-policy {
    grid-template-columns: 1fr;
  }
}
</style>
