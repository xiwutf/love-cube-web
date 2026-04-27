<template>
  <section class="events-page">
    <section class="events-hero" aria-labelledby="events-title">
      <div class="hero-copy">
        <router-link to="/" class="back-link">‹ 返回</router-link>
        <h1 id="events-title">活动中心<span aria-hidden="true">✦</span></h1>
        <p>发现精彩活动，结识志同道合的朋友，丰富你的生活体验</p>

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
              <span>仅看可报名</span>
            </label>
            <div class="view-toggle" aria-label="视图切换">
              <button type="button" class="active" aria-label="网格视图">▦</button>
              <button type="button" aria-label="列表视图">☰</button>
            </div>
          </div>
        </div>

        <section class="activity-section" aria-labelledby="activity-list-title">
          <h2 id="activity-list-title">活动列表</h2>
          <div class="activity-grid">
            <router-link
              v-for="item in visibleList"
              :key="item.id"
              :to="`/events/${item.id}`"
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
                <p class="meta-row">◷ {{ formatEventTime(item.time) }}　⌖ {{ item.location }}</p>
                <div class="card-foot">
                  <div class="avatars" aria-hidden="true">
                    <i v-for="avatar in item.avatars" :key="avatar" :style="{ backgroundImage: `url(${avatar})` }"></i>
                  </div>
                  <strong>{{ item.signupCount }} 人已报名</strong>
                </div>
              </div>
            </router-link>

            <article v-if="!loading && !visibleList.length" class="empty-card">
              <h3>暂无匹配活动</h3>
              <p>换一个筛选条件试试，新的活动也会持续补充。</p>
            </article>
          </div>
        </section>

        <button v-if="hasMore" type="button" class="load-more" @click="page += 1">加载更多活动⌄</button>
      </div>

      <aside class="events-aside">
        <section class="side-card">
          <div class="side-head">
            <h2>热门活动</h2>
            <router-link to="/events">查看更多 ›</router-link>
          </div>
          <router-link v-for="item in hotActivities" :key="item.id" :to="`/events/${item.id}`" class="hot-item">
            <img :src="item.coverUrl" :alt="item.title" loading="lazy">
            <span>
              <strong>{{ item.title }}</strong>
              <small>{{ formatShortTime(item.time) }}　报名 {{ item.signupCount }} 人</small>
            </span>
            <em :class="item.tone">{{ item.hotLabel }}</em>
          </router-link>
        </section>

        <section class="side-card publish-card">
          <div>
            <h2>快速发布活动</h2>
            <p>分享你的活动，吸引更多伙伴参与</p>
            <router-link to="/admin/events" class="publish-btn">＋ 发布新活动</router-link>
          </div>
          <div class="mini-calendar" aria-hidden="true"></div>
        </section>
      </aside>
    </section>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { fetchEvents } from '@/api/platformContent.js'

const PAGE_SIZE = 4
const activeCategory = ref('')
const cityFilter = ref('')
const timeFilter = ref('')
const typeFilter = ref('')
const sortMode = ref('default')
const availableOnly = ref(false)
const loading = ref(false)
const allItems = ref([])
const page = ref(1)

const fallbackEvents = [
  {
    id: 'offline-shanghai-meetup',
    title: '线下见面会（上海）',
    category: '线下活动',
    type: '社交联谊',
    time: '2026-05-24 15:00',
    location: '上海市 静安区',
    city: '上海',
    signupCount: 30,
    capacity: 40,
    hotLabel: '火热',
    tone: 'pink',
    coverUrl: 'https://images.unsplash.com/photo-1506157786151-b8491531f063?auto=format&fit=crop&w=900&q=80',
    tags: ['社交联谊', '线下聚会']
  },
  {
    id: 'online-icebreak-night',
    title: '线上破冰夜',
    category: '线上活动',
    type: '互动游戏',
    time: '2026-05-19 19:30',
    location: '线上直播间',
    city: '线上',
    signupCount: 16,
    capacity: 80,
    hotLabel: '推荐',
    tone: 'blue',
    coverUrl: 'https://images.unsplash.com/photo-1511512578047-dfb367046420?auto=format&fit=crop&w=900&q=80',
    tags: ['互动游戏', '线上交流']
  },
  {
    id: 'interest-open-day',
    title: '兴趣小组开放日',
    category: '兴趣小组',
    type: '兴趣社群',
    time: '2026-05-11 20:00',
    location: '线上/线下同步',
    city: '线上',
    signupCount: 23,
    capacity: 60,
    hotLabel: '公益',
    tone: 'green',
    coverUrl: 'https://images.unsplash.com/photo-1529156069898-49953e39b3ac?auto=format&fit=crop&w=900&q=80',
    tags: ['兴趣社群', '线下活动']
  },
  {
    id: 'summer-music-party',
    title: '夏日音乐派对',
    category: '主题派对',
    type: '音乐派对',
    time: '2026-06-01 18:30',
    location: '浦东新区 滨江公园',
    city: '上海',
    signupCount: 45,
    capacity: 100,
    hotLabel: '兴趣',
    tone: 'violet',
    coverUrl: 'https://images.unsplash.com/photo-1501281668745-f7f57925c3b4?auto=format&fit=crop&w=900&q=80',
    tags: ['音乐派对', '主题活动']
  },
  {
    id: 'public-welfare-walk',
    title: '公益徒步行',
    category: '公益活动',
    type: '户外公益',
    time: '2026-05-18 08:30',
    location: '世纪公园',
    city: '上海',
    signupCount: 36,
    capacity: 50,
    hotLabel: '公益',
    tone: 'green',
    coverUrl: 'https://images.unsplash.com/photo-1551632811-561732d1e306?auto=format&fit=crop&w=900&q=80',
    tags: ['公益活动', '户外徒步']
  },
  {
    id: 'photo-walk',
    title: '摄影采风活动',
    category: '兴趣小组',
    type: '摄影采风',
    time: '2026-06-26 14:00',
    location: '苏州河沿线',
    city: '上海',
    signupCount: 22,
    capacity: 36,
    hotLabel: '兴趣',
    tone: 'blue',
    coverUrl: 'https://images.unsplash.com/photo-1492691527719-9d1e07e534b4?auto=format&fit=crop&w=900&q=80',
    tags: ['摄影采风', '城市漫步']
  }
]

const categoryTabs = [
  { label: '全部活动', value: '' },
  { label: '线下活动', value: '线下活动' },
  { label: '线上活动', value: '线上活动' },
  { label: '兴趣小组', value: '兴趣小组' },
  { label: '主题派对', value: '主题派对' },
  { label: '公益活动', value: '公益活动' }
]

const stats = computed(() => [
  { icon: '▣', value: allItems.value.length || 32, label: '全部活动', note: `进行中 ${activeCount.value} 场`, tone: 'pink' },
  { icon: '♟', value: signupTotal.value || 328, label: '已报名', note: '本月参与人数', tone: 'blue' },
  { icon: '●', value: cityOptions.value.length || 18, label: '城市覆盖', note: '活动举办城市', tone: 'green' },
  { icon: '♥', value: '96%', label: '用户满意度', note: '活动好评率', tone: 'amber' }
])

const normalizedItems = computed(() => allItems.value.map((item, index) => {
  const fallback = fallbackEvents[index % fallbackEvents.length]
  return {
    ...fallback,
    ...item,
    id: item.id || fallback.id,
    title: item.title || fallback.title,
    category: item.category || fallback.category,
    type: item.type || item.category || fallback.type,
    time: item.eventTime || item.time || fallback.time,
    location: item.location || fallback.location,
    city: item.city || parseCity(item.location) || fallback.city,
    signupCount: Number(item.signupCount ?? fallback.signupCount),
    capacity: Number(item.capacity ?? fallback.capacity),
    coverUrl: item.coverUrl || fallback.coverUrl,
    tags: item.tags || fallback.tags,
    tone: fallback.tone,
    hotLabel: item.recommended ? '推荐' : fallback.hotLabel,
    avatars: avatarSet(index)
  }
}))

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

function avatarSet(seed) {
  return [0, 1, 2, 3].map(i => `https://i.pravatar.cc/80?img=${(seed * 5 + i + 12) % 60}`)
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
  loading.value = true
  try {
    const data = await fetchEvents({ status: 'published' })
    allItems.value = Array.isArray(data) && data.length ? data : fallbackEvents
  } catch {
    allItems.value = fallbackEvents
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.events-page {
  width: 100%;
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

.back-link {
  display: inline-flex;
  color: var(--lc-muted);
  font-size: var(--lc-text-sm);
  font-weight: 700;
  text-decoration: none;
}

.hero-copy h1 {
  margin: var(--lc-space-6) 0 0;
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
  grid-template-columns: repeat(4, minmax(0, 1fr));
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
  backdrop-filter: blur(14px);
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
  color: #7c3aed;
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
  content: '♥';
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
  background: linear-gradient(145deg, #ff91b5, #ec4899);
}

.balloon-violet {
  right: 74%;
  top: 96px;
  width: 54px;
  height: 68px;
  background: linear-gradient(145deg, #b8a5ff, #7c3aed);
}

.gift-box {
  position: absolute;
  right: 40px;
  bottom: 26px;
  width: 72px;
  height: 72px;
  border-radius: 8px;
  background:
    linear-gradient(90deg, transparent 40%, #7c3aed 40% 56%, transparent 56%),
    linear-gradient(0deg, transparent 42%, #7c3aed 42% 58%, transparent 58%),
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
  backdrop-filter: blur(16px);
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
  overflow-x: auto;
}

.tabs button {
  position: relative;
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

.filters label {
  position: relative;
}

.filters label > span:not(.check-filter span) {
  position: absolute;
  width: 1px;
  height: 1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
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
  justify-content: space-between;
  align-items: center;
  gap: var(--lc-space-3);
}

.avatars {
  display: flex;
}

.avatars i {
  width: 24px;
  height: 24px;
  margin-left: -7px;
  border: 2px solid var(--lc-surface);
  border-radius: 999px;
  background-size: cover;
}

.avatars i:first-child {
  margin-left: 0;
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
  color: #fff;
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

  .stats-grid,
  .activity-grid,
  .filters {
    grid-template-columns: 1fr;
  }

  .tabs {
    gap: var(--lc-space-4);
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
