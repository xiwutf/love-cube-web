<template>
  <section class="events-page">
    <section class="events-hero" aria-labelledby="events-title">
      <div class="hero-copy">
        <h1 id="events-title">活动中心<span aria-hidden="true">✓</span></h1>
        <p class="hero-lead">发现精彩活动，结识志同道合的朋友，丰富你的生活体验</p>

        <div class="stats-grid" aria-label="活动数据概览">
          <article v-for="stat in stats" :key="stat.label" class="stat-card">
            <span class="stat-icon" :class="stat.tone" aria-hidden="true">{{ stat.icon }}</span>
            <span class="stat-value">{{ stat.value }}</span>
            <span class="stat-label">{{ stat.label }}</span>
            <span class="stat-note">{{ stat.note }}</span>
          </article>
        </div>
      </div>

      <div class="hero-art" aria-hidden="true">
        <div class="balloon balloon-pink"></div>
        <div class="balloon balloon-violet"></div>
        <div class="calendar-stand">
          <div class="calendar-page">
            <div class="calendar-rings">
              <i></i>
              <i></i>
            </div>
            <div class="calendar-header"></div>
            <div class="calendar-grid">
              <i v-for="n in 25" :key="n" :class="{ marked: n === 15 }"></i>
            </div>
          </div>
          <div class="gift-box"></div>
        </div>
      </div>
    </section>

    <section class="events-shell">
      <div class="events-main">
        <div class="filter-panel">
          <nav class="tabs" aria-label="活动分类">
            <button
              v-for="tab in categoryTabs"
              :key="tab.value"
              type="button"
              :class="{ active: activeCategory === tab.value }"
              @click="setCategory(tab.value)"
            >
              {{ tab.label }}
            </button>
          </nav>

          <button
            type="button"
            class="filter-toggle"
            :aria-expanded="filtersExpanded ? 'true' : 'false'"
            @click="filtersExpanded = !filtersExpanded"
          >
            <span>筛选条件</span>
            <i :class="{ expanded: filtersExpanded }" aria-hidden="true">⌄</i>
          </button>

          <div class="filters-wrap" :class="{ 'is-collapsed': !filtersExpanded }">
            <div class="filters">
              <label>
                <span>城市</span>
                <select v-model="cityFilter">
                  <option value="">全部城市</option>
                  <option v-for="city in cityOptions" :key="city" :value="city">{{ city }}</option>
                </select>
              </label>
              <label>
                <span>时间</span>
                <select v-model="timeFilter">
                  <option value="">全部时间</option>
                  <option value="week">本周活动</option>
                  <option value="month">本月活动</option>
                  <option value="future">即将开始</option>
                </select>
              </label>
              <label>
                <span>类型</span>
                <select v-model="typeFilter">
                  <option value="">全部类型</option>
                  <option v-for="type in typeOptions" :key="type" :value="type">{{ type }}</option>
                </select>
              </label>
              <label>
                <span>排序</span>
                <select v-model="sortMode">
                  <option value="default">默认排序</option>
                  <option value="time">按时间最近</option>
                  <option value="signup">按报名人数</option>
                </select>
              </label>
              <label class="check-filter">
                <input v-model="availableOnly" type="checkbox">
                <span>仅看可报</span>
              </label>
              <div class="view-toggle" aria-label="视图切换">
                <button type="button" class="active" aria-label="网格视图">▦</button>
                <button type="button" aria-label="列表视图">☰</button>
              </div>
            </div>
          </div>
        </div>

        <section class="activity-section" aria-labelledby="activity-list-title">
          <h2 id="activity-list-title">活动列表</h2>
          <div class="activity-grid">
            <router-link
              v-for="item in visibleList"
              :key="item.id"
              :to="eventsPath(item.id)"
              class="activity-card"
            >
              <div class="activity-cover">
                <img :src="item.coverUrl" :alt="item.title" loading="lazy">
                <span class="cover-badge" :class="item.tone">{{ item.category }}</span>
              </div>
              <div class="activity-body">
                <h3>{{ item.title }}</h3>
                <div class="chips">
                  <span v-for="tag in item.tags" :key="tag">{{ tag }}</span>
                </div>
                <p class="meta-row">● {{ formatEventTime(item.time) }} · 📍 {{ item.location }}</p>
                <div class="card-foot">
                  <strong>{{ item.signupCount }} 人已报名</strong>
                </div>
              </div>
            </router-link>

            <article v-if="!loading && !visibleList.length" class="empty-card">
              <h3>暂无符合条件的活动</h3>
              <p>换一个筛选条件试试，新的活动也会持续补充</p>
            </article>
          </div>
        </section>

        <button v-if="hasMore" type="button" class="load-more" @click="page += 1">加载更多活动</button>
      </div>

      <aside class="events-aside">
        <section class="side-card">
          <div class="side-head">
            <h2>热门活动</h2>
            <router-link :to="eventsPath()">查看更多</router-link>
          </div>
          <router-link v-for="item in hotActivities" :key="item.id" :to="eventsPath(item.id)" class="hot-item">
            <img :src="item.coverUrl" :alt="item.title" loading="lazy">
            <span>
              <strong>{{ item.title }}</strong>
              <small>{{ formatShortTime(item.time) }} · 报名 {{ item.signupCount }} 人</small>
            </span>
            <em :class="item.tone">{{ item.hotLabel }}</em>
          </router-link>
        </section>

        <section class="side-card publish-card">
          <div>
            <h2>快速发布活动</h2>
            <p>分享你的活动，吸引更多伙伴参与</p>
            <router-link to="/admin/events" class="publish-btn">发布新活动</router-link>
          </div>
          <div class="mini-calendar" aria-hidden="true"></div>
        </section>
      </aside>
    </section>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { fetchEvents, fetchEventsStats } from '@/api/platformContent.js'
import { usePlatformPath } from '@/composables/usePlatformPath.js'

const { eventsPath } = usePlatformPath()

const PAGE_SIZE = 10
const activeCategory = ref('')
const cityFilter = ref('')
const timeFilter = ref('')
const typeFilter = ref('')
const sortMode = ref('default')
const availableOnly = ref(false)
const filtersExpanded = ref(true)
const loading = ref(false)
const allItems = ref([])
const page = ref(1)

const EVENT_TONES = ['pink', 'blue', 'green', 'violet', 'amber']

const categoryTabs = [
  { label: '全部活动', value: '' },
  { label: '线下活动', value: '线下活动' },
  { label: '线上活动', value: '线上活动' },
  { label: '兴趣小组', value: '兴趣小组' },
  { label: '主题派对', value: '主题派对' },
  { label: '公益活动', value: '公益活动' }
]

const eventsApiStats = ref({ totalEvents: 0, totalSignups: 0, citiesCount: 0, activeEvents: 0 })

const stats = computed(() => [
  { icon: '总', value: eventsApiStats.value.totalEvents || allItems.value.length, label: '全部活动', note: `进行中 ${activeCount.value} 场`, tone: 'pink' },
  { icon: '报', value: eventsApiStats.value.totalSignups || signupTotal.value, label: '已报名', note: '累计报名人数', tone: 'blue' },
  { icon: '城', value: eventsApiStats.value.citiesCount || cityOptions.value.length, label: '城市覆盖', note: '活动举办城市', tone: 'green' }
])

const normalizedItems = computed(() => allItems.value.map((item, index) => ({
  id: item.id,
  title: item.title || '',
  category: item.category || '',
  type: item.type || item.category || '',
  time: item.eventTime || item.time || '',
  location: item.location || '',
  city: item.city || parseCity(item.location) || '',
  signupCount: Number(item.signupCount ?? 0),
  capacity: Number(item.capacity ?? 0),
  coverUrl: item.coverUrl || '',
  tags: item.tags || [],
  tone: EVENT_TONES[index % EVENT_TONES.length],
  hotLabel: item.recommended ? '推荐' : ''
})))

const cityOptions = computed(() => [...new Set(normalizedItems.value.map(item => item.city).filter(Boolean))])
const typeOptions = computed(() => [...new Set(normalizedItems.value.map(item => item.type).filter(Boolean))])
const signupTotal = computed(() => normalizedItems.value.reduce((sum, item) => sum + item.signupCount, 0))
const activeCount = computed(() => normalizedItems.value.filter(item => new Date(item.time.replace(' ', 'T')) >= new Date()).length)

const filteredList = computed(() => {
  const now = new Date()
  const sevenDays = 7 * 24 * 60 * 60 * 1000
  const thirtyDays = 30 * 24 * 60 * 60 * 1000
  let list = normalizedItems.value.filter(item => {
    const eventDate = new Date(item.time.replace(' ', 'T'))
    const delta = eventDate.getTime() - now.getTime()
    if (activeCategory.value && item.category !== activeCategory.value) return false
    if (cityFilter.value && item.city !== cityFilter.value) return false
    if (typeFilter.value && item.type !== typeFilter.value) return false
    if (availableOnly.value && item.signupCount >= item.capacity) return false
    if (timeFilter.value === 'week' && (delta < 0 || delta > sevenDays)) return false
    if (timeFilter.value === 'month' && (delta < 0 || delta > thirtyDays)) return false
    if (timeFilter.value === 'future' && delta < 0) return false
    return true
  })

  if (sortMode.value === 'time') {
    list = [...list].sort((a, b) => new Date(a.time.replace(' ', 'T')) - new Date(b.time.replace(' ', 'T')))
  }
  if (sortMode.value === 'signup') {
    list = [...list].sort((a, b) => b.signupCount - a.signupCount)
  }
  return list
})

const visibleList = computed(() => filteredList.value.slice(0, page.value * PAGE_SIZE))
const hasMore = computed(() => filteredList.value.length > visibleList.value.length)
const hotActivities = computed(() => [...normalizedItems.value].sort((a, b) => b.signupCount - a.signupCount).slice(0, 4))

function setCategory(value) {
  activeCategory.value = value
  page.value = 1
}

function parseCity(location = '') {
  if (location.includes('上海')) return '上海'
  if (location.includes('线上')) return '线上'
  return ''
}

function formatEventTime(value) {
  if (!value) return '时间待定'
  const [date, time = ''] = String(value).split(' ')
  const eventDate = new Date(String(value).replace(' ', 'T'))
  const week = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'][eventDate.getDay()] || ''
  return `${date.slice(5)} ${time.slice(0, 5)} ${week}`
}

function formatShortTime(value) {
  if (!value) return '待定'
  const [date, time = ''] = String(value).split(' ')
  return `${date.slice(5)} ${time.slice(0, 5)}`
}

onMounted(async () => {
  if (window.matchMedia('(max-width: 640px)').matches) {
    filtersExpanded.value = false
  }

  loading.value = true
  const [eventsRes, statsRes] = await Promise.allSettled([
    fetchEvents({ status: 'published' }),
    fetchEventsStats()
  ])

  if (eventsRes.status === 'fulfilled') {
    allItems.value = Array.isArray(eventsRes.value) ? eventsRes.value : []
  } else {
    allItems.value = []
  }

  if (statsRes.status === 'fulfilled' && statsRes.value) {
    eventsApiStats.value = statsRes.value
  }

  loading.value = false
})
</script>

<style scoped>
.events-page {
  box-sizing: border-box;
  width: 100%;
  max-width: 100%;
  min-height: 100vh;
  padding: 32px clamp(20px, 5vw, 92px) 0;
  background:
    linear-gradient(115deg, rgba(244, 248, 255, 0.94), rgba(255, 247, 251, 0.94) 52%, rgba(245, 247, 255, 0.94)),
    var(--lc-bg);
}

.events-hero {
  min-height: 340px;
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(340px, 0.72fr);
  align-items: center;
  gap: var(--lc-space-12);
}

.hero-copy h1 {
  margin: 0;
  color: var(--lc-text);
  font-size: clamp(40px, 4vw, 60px);
  line-height: 1.05;
  letter-spacing: 0;
}

.hero-copy h1 span {
  margin-left: var(--lc-space-2);
  color: var(--lc-pink);
  font-size: 28px;
  vertical-align: top;
}

.hero-copy p {
  margin: var(--lc-space-4) 0 0;
  color: var(--lc-muted);
  font-size: var(--lc-text-md);
  font-weight: 700;
}

.stats-grid {
  margin-top: var(--lc-space-8);
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: var(--lc-space-4);
}

.stat-card {
  min-height: 112px;
  display: grid;
  grid-template-columns: 56px 1fr;
  align-items: center;
  column-gap: var(--lc-space-3);
  padding: var(--lc-space-5);
  border: 1px solid rgba(226, 232, 240, 0.76);
  border-radius: var(--lc-radius-sm);
  background: rgba(255, 255, 255, 0.76);
  box-shadow: var(--lc-shadow-sm);
  backdrop-filter: none;
}

.stat-icon {
  grid-row: span 3;
  width: 48px;
  height: 48px;
  display: grid;
  place-items: center;
  border-radius: var(--lc-radius-sm);
  font-size: 22px;
  font-weight: 800;
}

.stat-icon.pink,
.cover-badge.pink,
.hot-item em.pink {
  color: var(--lc-pink);
  background: var(--lc-pink-light);
}

.stat-icon.blue,
.cover-badge.blue,
.hot-item em.blue {
  color: var(--lc-blue);
  background: var(--lc-blue-light);
}

.stat-icon.green,
.cover-badge.green,
.hot-item em.green {
  color: var(--lc-green);
  background: var(--lc-green-light);
}

.stat-icon.amber {
  color: var(--lc-amber);
  background: var(--lc-amber-light);
}

.cover-badge.violet,
.hot-item em.violet {
  color: var(--lc-purple);
  background: #f3e8ff;
}

.stat-value {
  color: var(--lc-text);
  font-size: 24px;
  font-weight: 900;
}

.stat-label {
  color: var(--lc-muted);
  font-size: var(--lc-text-sm);
  font-weight: 800;
}

.stat-note {
  color: var(--lc-subtle);
  font-size: var(--lc-text-xs);
  font-weight: 700;
}

.hero-art {
  position: relative;
  min-height: 300px;
}

.calendar-stand {
  position: absolute;
  right: 5%;
  bottom: 20px;
  width: min(420px, 90%);
  height: 190px;
  border-radius: 18px;
  background: linear-gradient(180deg, #b6a3ff, #8b72ff);
  box-shadow: 0 34px 62px rgba(124, 58, 237, 0.28);
}

.calendar-page {
  position: absolute;
  left: 72px;
  top: -74px;
  width: 230px;
  height: 210px;
  border-radius: 10px;
  background: #fff8fc;
  border: 4px solid #ff8bb3;
  transform: rotate(3deg);
  box-shadow: 18px 20px 0 #6f5be8;
}

.calendar-rings {
  position: absolute;
  top: -26px;
  left: 34px;
  right: 34px;
  display: flex;
  justify-content: space-between;
}

.calendar-rings i {
  width: 18px;
  height: 42px;
  border: 5px solid #6371c9;
  border-bottom: 0;
  border-radius: 999px 999px 0 0;
}

.calendar-header {
  height: 38px;
  border-radius: 5px 5px 0 0;
  background: linear-gradient(90deg, #ff6e9f, #ff9cc1);
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 10px;
  padding: 24px 28px;
}

.calendar-grid i {
  height: 14px;
  border: 2px solid #ff9cc1;
  border-radius: 2px;
}

.calendar-grid i.marked {
  position: relative;
  background: var(--lc-pink);
}

.calendar-grid i.marked::after {
  content: '*';
  position: absolute;
  right: -18px;
  top: -12px;
  color: var(--lc-pink);
  font-size: 28px;
}

.balloon {
  position: absolute;
  border-radius: 999px;
  box-shadow: inset -12px -12px 24px rgba(15, 23, 42, 0.12);
}

.balloon::after {
  content: '';
  position: absolute;
  left: 50%;
  top: 100%;
  width: 2px;
  height: 118px;
  background: rgba(100, 116, 139, 0.46);
}

.balloon-pink {
  right: 62%;
  top: 22px;
  width: 72px;
  height: 92px;
  background: linear-gradient(145deg, #ff91b5, var(--lc-pink));
}

.balloon-violet {
  right: 74%;
  top: 96px;
  width: 54px;
  height: 68px;
  background: linear-gradient(145deg, #b8a5ff, var(--lc-purple));
}

.gift-box {
  position: absolute;
  right: 40px;
  bottom: 26px;
  width: 72px;
  height: 72px;
  border-radius: 8px;
  background:
    linear-gradient(90deg, transparent 40%, var(--lc-purple) 40% 56%, transparent 56%),
    linear-gradient(0deg, transparent 42%, var(--lc-purple) 42% 58%, transparent 58%),
    linear-gradient(145deg, #ff94b8, #ffbdd1);
}

.events-shell {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 340px;
  gap: var(--lc-space-6);
  align-items: start;
  margin-top: var(--lc-space-4);
}

.events-main,
.side-card {
  border: 1px solid rgba(226, 232, 240, 0.78);
  border-radius: var(--lc-radius);
  background: rgba(255, 255, 255, 0.86);
  box-shadow: var(--lc-shadow-sm);
  backdrop-filter: none;
}

.filter-panel {
  border-bottom: 1px solid var(--lc-border);
  padding: 0 var(--lc-space-5) var(--lc-space-5);
}

.tabs {
  display: flex;
  gap: var(--lc-space-6);
  min-height: 62px;
  align-items: center;
  flex-wrap: nowrap;
  overflow-x: auto;
  overflow-y: hidden;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: thin;
  padding-bottom: 2px;
  margin-inline: calc(-1 * var(--lc-space-5));
  padding-inline: var(--lc-space-5);
}

.tabs button {
  position: relative;
  flex-shrink: 0;
  border: 0;
  background: transparent;
  color: var(--lc-muted);
  font-size: var(--lc-text-sm);
  font-weight: 800;
  white-space: nowrap;
  cursor: pointer;
}

.tabs button.active {
  color: var(--lc-pink);
}

.tabs button.active::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: -20px;
  height: 2px;
  border-radius: 999px;
  background: var(--lc-pink);
}

.filters {
  display: grid;
  grid-template-columns: repeat(4, minmax(130px, 1fr)) auto auto;
  gap: var(--lc-space-3);
  align-items: center;
}

.filter-toggle {
  display: none;
}

.filters-wrap {
  overflow: visible;
}

.filters label {
  position: relative;
}

.filters label > span:not(.check-filter span) {
  position: absolute;
  width: 1px;
  height: 1px;
  overflow: hidden;
  clip-path: inset(50%);
}

.filters select {
  width: 100%;
  height: 40px;
  border: 1px solid var(--lc-border);
  border-radius: var(--lc-radius-xs);
  background: var(--lc-surface);
  color: var(--lc-muted);
  padding: 0 var(--lc-space-4);
  font-weight: 700;
  outline: 0;
}

.check-filter {
  display: inline-flex;
  align-items: center;
  gap: var(--lc-space-2);
  color: var(--lc-muted);
  font-size: var(--lc-text-sm);
  font-weight: 800;
  white-space: nowrap;
}

.check-filter input {
  width: 16px;
  height: 16px;
  accent-color: var(--lc-pink);
}

.view-toggle {
  display: inline-flex;
  justify-content: flex-end;
  gap: var(--lc-space-2);
}

.view-toggle button {
  width: 38px;
  height: 38px;
  border: 1px solid var(--lc-border);
  border-radius: var(--lc-radius-xs);
  background: var(--lc-surface);
  color: var(--lc-muted);
  font-size: 18px;
  cursor: pointer;
}

.view-toggle button.active {
  color: var(--lc-pink);
  border-color: var(--lc-pink-border);
  background: var(--lc-pink-light);
}

.activity-section {
  padding: var(--lc-space-5);
}

.activity-section h2,
.side-card h2 {
  margin: 0;
  color: var(--lc-text);
  font-size: var(--lc-text-lg);
}

.activity-grid {
  margin-top: var(--lc-space-4);
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: var(--lc-space-5);
}

.activity-card {
  overflow: hidden;
  border: 1px solid var(--lc-border);
  border-radius: var(--lc-radius-sm);
  background: var(--lc-surface);
  color: inherit;
  text-decoration: none;
  box-shadow: 0 8px 22px rgba(15, 23, 42, 0.06);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.activity-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--lc-shadow);
}

.activity-cover {
  position: relative;
  aspect-ratio: 16 / 8.8;
  overflow: hidden;
}

.activity-cover img {
  width: 100%;
  height: 100%;
  display: block;
  object-fit: cover;
}

.cover-badge {
  position: absolute;
  left: var(--lc-space-3);
  top: var(--lc-space-3);
  border-radius: 999px;
  padding: 4px 10px;
  font-size: var(--lc-text-xs);
  font-weight: 900;
}

.activity-body {
  padding: var(--lc-space-4);
}

.activity-body h3 {
  margin: 0;
  color: var(--lc-text);
  font-size: var(--lc-text-md);
  line-height: 1.35;
}

.chips {
  margin-top: var(--lc-space-3);
  display: flex;
  gap: var(--lc-space-2);
  flex-wrap: wrap;
}

.chips span {
  border-radius: 999px;
  background: var(--lc-soft);
  color: var(--lc-muted);
  padding: 4px 8px;
  font-size: var(--lc-text-xs);
  font-weight: 800;
}

.meta-row {
  margin: var(--lc-space-3) 0 0;
  color: var(--lc-muted);
  font-size: var(--lc-text-xs);
  font-weight: 700;
  line-height: 1.7;
}

.card-foot {
  margin-top: var(--lc-space-4);
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: var(--lc-space-3);
}

.card-foot strong {
  color: var(--lc-pink);
  font-size: var(--lc-text-xs);
  white-space: nowrap;
}

.load-more {
  width: calc(100% - 40px);
  margin: 0 var(--lc-space-5) var(--lc-space-5);
  height: 42px;
  border: 0;
  border-radius: var(--lc-radius-xs);
  background: var(--lc-bg);
  color: var(--lc-muted);
  font-weight: 800;
  cursor: pointer;
}

.events-aside {
  display: grid;
  gap: var(--lc-space-6);
}

.side-card {
  padding: var(--lc-space-5);
}

.side-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--lc-space-3);
}

.side-head a {
  color: var(--lc-muted);
  font-size: var(--lc-text-sm);
  font-weight: 800;
  text-decoration: none;
}

.hot-item {
  margin-top: var(--lc-space-4);
  display: grid;
  grid-template-columns: 64px 1fr auto;
  gap: var(--lc-space-3);
  align-items: center;
  color: inherit;
  text-decoration: none;
}

.hot-item img {
  width: 64px;
  height: 52px;
  border-radius: var(--lc-radius-xs);
  object-fit: cover;
}

.hot-item strong {
  display: block;
  color: var(--lc-text);
  font-size: var(--lc-text-sm);
}

.hot-item small {
  display: block;
  margin-top: 6px;
  color: var(--lc-subtle);
  font-size: var(--lc-text-xs);
  font-weight: 700;
}

.hot-item em {
  min-width: 42px;
  border-radius: 999px;
  padding: 5px 8px;
  font-size: var(--lc-text-xs);
  font-style: normal;
  font-weight: 900;
  text-align: center;
}

.publish-card {
  min-height: 148px;
  display: grid;
  grid-template-columns: 1fr 92px;
  gap: var(--lc-space-4);
  align-items: center;
}

.publish-card p {
  margin: var(--lc-space-3) 0 0;
  color: var(--lc-muted);
  font-size: var(--lc-text-sm);
  font-weight: 700;
}

.publish-btn {
  margin-top: var(--lc-space-5);
  min-height: 38px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  padding: 0 var(--lc-space-4);
  background: linear-gradient(135deg, #ff5f84, var(--lc-pink));
  color: var(--lc-surface);
  font-size: var(--lc-text-sm);
  font-weight: 900;
  text-decoration: none;
  box-shadow: 0 10px 20px rgba(236, 72, 153, 0.24);
}

.mini-calendar {
  height: 88px;
  border-radius: var(--lc-radius-sm);
  background:
    radial-gradient(circle at 72% 70%, var(--lc-pink) 0 10px, transparent 11px),
    linear-gradient(90deg, transparent 45%, #ff8bb3 45% 57%, transparent 57%),
    linear-gradient(180deg, #ffd6e3 0 28%, #fff7fb 28%);
  box-shadow: inset 0 -10px 0 rgba(236, 72, 153, 0.08);
}

.empty-card {
  grid-column: 1 / -1;
  border: 1px dashed var(--lc-border);
  border-radius: var(--lc-radius-sm);
  padding: var(--lc-space-8);
  text-align: center;
}

.empty-card h3 {
  margin: 0;
  color: var(--lc-text);
}

.empty-card p {
  color: var(--lc-muted);
}

@media (max-width: 1280px) {
  .stats-grid,
  .activity-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .events-shell {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 900px) {
  .events-hero {
    grid-template-columns: 1fr;
    min-height: 0;
    gap: var(--lc-space-4);
  }

  .hero-art {
    display: none;
  }

  .filters {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .events-page {
    padding: 16px 12px 0;
  }

  .hero-copy h1 {
    margin-top: var(--lc-space-2);
    font-size: clamp(1.35rem, 6.2vw, 1.65rem);
    line-height: 1.15;
  }

  .hero-copy h1 span {
    font-size: 1rem;
    margin-left: 4px;
  }

  .hero-lead {
    margin-top: var(--lc-space-2);
    font-size: 12px;
    line-height: 1.45;
    font-weight: 600;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  /* 数据概览：单行紧凑条，避免多块大卡占满首屏 */
  .stats-grid {
    display: flex;
    flex-wrap: nowrap;
    margin-top: 10px;
    padding: 8px 6px;
    gap: 0;
    border: 1px solid rgba(226, 232, 240, 0.76);
    border-radius: var(--lc-radius-sm);
    background: rgba(255, 255, 255, 0.82);
  }

  .stat-card {
    flex: 1 1 0;
    min-width: 0;
    min-height: 0;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    text-align: center;
    padding: 6px 2px;
    gap: 2px;
    border: 0;
    box-shadow: none;
    background: transparent;
  }

  .stat-card:not(:first-child) {
    border-left: 1px solid rgba(226, 232, 240, 0.9);
  }

  .stat-icon {
    grid-row: auto;
    width: 26px;
    height: 26px;
    font-size: 12px;
    font-weight: 800;
  }

  .stat-value {
    font-size: 15px;
    font-weight: 900;
    line-height: 1.15;
  }

  .stat-label {
    font-size: 10px;
    font-weight: 700;
    line-height: 1.2;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    max-width: 100%;
  }

  .stat-note {
    display: none;
  }

  .activity-card,
  .side-card {
    box-shadow: none;
  }

  .activity-grid,
  .filters {
    grid-template-columns: 1fr;
  }

  .tabs {
    gap: var(--lc-space-4);
    scroll-snap-type: x proximity;
  }

  .tabs button {
    scroll-snap-align: start;
  }

  .filter-toggle {
    width: 100%;
    height: 40px;
    margin-top: 4px;
    display: inline-flex;
    align-items: center;
    justify-content: space-between;
    border: 1px solid var(--lc-border);
    border-radius: var(--lc-radius-xs);
    background: var(--lc-surface);
    color: var(--lc-text);
    padding: 0 12px;
    font-size: var(--lc-text-sm);
    font-weight: 800;
  }

  .filter-toggle i {
    font-style: normal;
    transition: transform 0.2s ease;
  }

  .filter-toggle i.expanded {
    transform: rotate(180deg);
  }

  .filters-wrap {
    max-height: 420px;
    overflow: hidden;
    transition: max-height 0.22s ease, opacity 0.22s ease;
  }

  .filters-wrap.is-collapsed {
    max-height: 0;
    opacity: 0;
  }

  .hot-item {
    grid-template-columns: 58px 1fr;
  }

  .hot-item em {
    grid-column: 2;
    width: max-content;
  }

  .publish-card {
    grid-template-columns: 1fr;
  }
}
</style>

