<template>
  <div class="groups-page">
    <header class="groups-header">
      <div>
        <p class="header-kicker">Love Cube</p>
        <h1>团体</h1>
        <p>寻找属于你的团体，连接更多志同道合的伙伴</p>
      </div>
      <button class="create-btn" type="button" @click="showCreateTip">
        <van-icon name="plus" size="16" />
        创建
      </button>
    </header>

    <section class="search-card">
      <van-search
        v-model="keyword"
        shape="round"
        placeholder="搜索团体名称、关键词..."
        background="transparent"
      />
      <div class="filters">
        <button
          v-for="filter in filters"
          :key="filter.value"
          class="filter-chip"
          :class="{ 'filter-chip--active': activeFilter === filter.value }"
          type="button"
          @click="activeFilter = filter.value"
        >
          {{ filter.label }}
        </button>
      </div>
    </section>

    <section class="hero-card">
      <div>
        <p class="hero-kicker">共同成长</p>
        <h2>创建属于你的团体</h2>
        <p>建立团体、邀请伙伴，一起在爱中成长</p>
        <button class="hero-btn" type="button" @click="showCreateTip">立即创建</button>
      </div>
      <div class="hero-people" aria-hidden="true">
        <span></span>
        <span></span>
        <span></span>
      </div>
    </section>

    <section class="group-tabs">
      <button
        v-for="tab in tabs"
        :key="tab.value"
        class="tab-btn"
        :class="{ 'tab-btn--active': activeTab === tab.value }"
        type="button"
        @click="activeTab = tab.value"
      >
        {{ tab.label }}
      </button>
    </section>

    <main class="group-list">
      <article v-for="group in visibleGroups" :key="group.id" class="group-card">
        <div class="group-cover" :class="group.coverClass">
          <img v-if="group.coverUrl" :src="group.coverUrl" :alt="group.name">
          <van-icon v-else :name="group.icon" size="30" />
        </div>
        <div class="group-info">
          <div class="group-title-row">
            <h2>{{ group.name }}</h2>
            <span>{{ group.type }}</span>
          </div>
          <p class="group-meta">{{ group.location }} · {{ group.members }} 人</p>
          <p class="group-desc">{{ group.description }}</p>
          <div class="group-actions">
            <span v-if="group.joined" class="joined-label">已加入</span>
            <span v-else-if="group.pending" class="joined-label joined-label--pending">申请中</span>
            <button v-else type="button" @click="handleJoin(group)">申请加入</button>
          </div>
        </div>
      </article>
      <van-empty v-if="!loading && !visibleGroups.length" description="暂无匹配团体" image-size="72" />
    </main>

    <section class="side-section">
      <div class="section-head">
        <h2>热门团体</h2>
        <span>查看更多</span>
      </div>
      <div class="hot-list">
        <div v-for="item in hotGroups" :key="item.name" class="hot-item">
          <div class="hot-avatar" :class="item.avatarClass">{{ item.name.slice(0, 1) }}</div>
          <div>
            <p>{{ item.name }}</p>
            <span>{{ item.location }} · {{ item.members }} 人</span>
          </div>
          <button type="button" @click="handleJoin(item)">{{ item.joined ? '已加入' : item.pending ? '申请中' : '申请' }}</button>
        </div>
      </div>
    </section>

    <section class="side-section">
      <div class="section-head">
        <h2>团体动态</h2>
        <span>查看更多</span>
      </div>
      <div class="feed-list">
        <div v-for="feed in feeds" :key="feed.title" class="feed-item">
          <div class="feed-avatar" :class="feed.avatarClass"></div>
          <div>
            <p>
              {{ feed.title }}
              <span>{{ feed.role }}</span>
            </p>
            <small>{{ feed.content }}</small>
          </div>
          <time>{{ feed.time }}</time>
        </div>
      </div>
    </section>

    <section class="feature-grid">
      <div v-for="feature in features" :key="feature.title" class="feature-item">
        <div class="feature-icon">
          <van-icon :name="feature.icon" size="24" />
        </div>
        <div>
          <h3>{{ feature.title }}</h3>
          <p>{{ feature.desc }}</p>
        </div>
      </div>
    </section>

    <AppTabBar />
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { showToast } from 'vant'
import AppTabBar from '@/components/AppTabBar.vue'
import { fetchGroups, joinGroup } from '@/api/groups.js'

const keyword = ref('')
const activeFilter = ref('all')
const activeTab = ref('all')
const loading = ref(false)
const remoteGroups = ref([])

const filters = [
  { label: '全部地区', value: 'all' },
  { label: '同城', value: 'local' },
  { label: '线上', value: 'online' }
]

const tabs = [
  { label: '全部团体', value: 'all' },
  { label: '我加入的', value: 'joined' },
  { label: '申请中', value: 'pending' }
]

const groups = [
  {
    id: 1,
    name: '北京青年团契',
    type: '地区团体',
    location: '北京',
    members: 120,
    description: '我们是一个充满爱与活力的青年团契，欢迎志同道合的朋友加入。',
    status: 'joined',
    joined: true,
    coverClass: 'cover-sunrise',
    icon: 'cross'
  },
  {
    id: 2,
    name: '上海教会团体',
    type: '教会团体',
    location: '上海',
    members: 256,
    description: '以上海教会为依托，发布教会通知和活动信息。',
    status: 'all',
    joined: false,
    coverClass: 'cover-church',
    icon: 'wap-home-o'
  },
  {
    id: 3,
    name: '广州祷告小组',
    type: '小组',
    location: '广州',
    members: 68,
    description: '垂听我们的祷告，一起为彼此祷告吧。',
    status: 'pending',
    joined: false,
    pending: true,
    coverClass: 'cover-pray',
    icon: 'like-o'
  },
  {
    id: 4,
    name: '读经分享小组',
    type: '学习小组',
    location: '全国',
    members: 86,
    description: '一起读经，一起成长，在神的话语中扎根。',
    status: 'all',
    joined: false,
    coverClass: 'cover-book',
    icon: 'notes-o'
  },
  {
    id: 5,
    name: '音乐敬拜团',
    type: '兴趣团体',
    location: '全国',
    members: 45,
    description: '用音乐赞美神，服侍教会，感动更多人认识神。',
    status: 'joined',
    joined: true,
    coverClass: 'cover-music',
    icon: 'music-o'
  },
  {
    id: 6,
    name: '家庭团契小组',
    type: '生活团契',
    location: '深圳',
    members: 32,
    description: '在主里建立美好的家庭关系，分享生活点滴。',
    status: 'all',
    joined: false,
    coverClass: 'cover-family',
    icon: 'friends-o'
  }
]

const hotGroups = [
  { name: '深圳青年团契', location: '深圳', members: 98, joined: true, avatarClass: 'avatar-green' },
  { name: '祷告同行小组', location: '全国', members: 76, joined: false, avatarClass: 'avatar-pink' },
  { name: '周末公益小组', location: '北京', members: 55, joined: false, avatarClass: 'avatar-blue' },
  { name: '婚前辅导小组', location: '全国', members: 42, joined: false, avatarClass: 'avatar-purple' }
]

const feeds = [
  { title: '北京青年团契', role: '管理员', content: '发布了新的活动', time: '2小时前', avatarClass: 'avatar-blue' },
  { title: '读经分享小组', role: '管理员', content: '更新了团体公告', time: '3小时前', avatarClass: 'avatar-purple' },
  { title: '音乐敬拜团', role: '管理员', content: '添加了 3 位新成员', time: '5小时前', avatarClass: 'avatar-pink' }
]

const features = [
  { title: '多元团体', desc: '覆盖信仰、兴趣、生活等多种团体类型', icon: 'friends-o' },
  { title: '共同成长', desc: '在团体中彼此陪伴，分享信仰与生活', icon: 'fire-o' },
  { title: '活动丰富', desc: '团体活动、线上分享、线下聚会更丰富', icon: 'calendar-o' },
  { title: '安全可信', desc: '真实身份认证，打造安全可信的团体环境', icon: 'shield-o' }
]

const displayGroups = computed(() => (remoteGroups.value.length ? remoteGroups.value : groups))

const visibleGroups = computed(() => {
  const term = keyword.value.trim().toLowerCase()
  return displayGroups.value.filter((group) => {
    const matchTab =
      activeTab.value === 'all' ||
      (activeTab.value === 'joined' && group.joined) ||
      (activeTab.value === 'pending' && group.status === 'pending')
    const matchFilter =
      activeFilter.value === 'all' ||
      (activeFilter.value === 'local' && group.location !== '全国') ||
      (activeFilter.value === 'online' && group.location === '全国')
    const matchKeyword =
      !term ||
      group.name.toLowerCase().includes(term) ||
      group.type.toLowerCase().includes(term) ||
      group.description.toLowerCase().includes(term)
    return matchTab && matchFilter && matchKeyword
  })
})

onMounted(() => {
  loadGroups()
})

async function loadGroups() {
  loading.value = true
  try {
    const data = await fetchGroups({ status: 'active' })
    remoteGroups.value = Array.isArray(data) ? data.map(normalizeGroup) : []
  } catch {
    remoteGroups.value = []
  } finally {
    loading.value = false
  }
}

function normalizeGroup(item, index) {
  const coverClass = [
    'cover-sunrise',
    'cover-church',
    'cover-pray',
    'cover-book',
    'cover-music',
    'cover-family'
  ][index % 6]

  return {
    id: item.id,
    name: item.name || '未命名团体',
    type: item.category || '团体',
    location: item.location || '全国',
    members: item.memberCount || 0,
    description: item.description || '欢迎加入这个团体，一起认识伙伴、分享生活与信仰。',
    status: item.hasPendingRequest ? 'pending' : 'all',
    joined: Boolean(item.isMember),
    pending: Boolean(item.hasPendingRequest),
    coverUrl: item.coverUrl || '',
    coverClass,
    icon: 'friends-o'
  }
}

function showCreateTip() {
  showToast('创建团体功能即将开放')
}

async function handleJoin(group) {
  if (group.joined || group.pending) return
  if (!remoteGroups.value.length) {
    showToast(`已提交加入${group.name}的申请`)
    return
  }

  try {
    const res = await joinGroup(group.id)
    showToast(res?.message || '申请已提交')
    group.joined = Boolean(res?.joined)
    group.pending = Boolean(res?.pending)
    group.status = group.pending ? 'pending' : group.status
  } catch (error) {
    showToast({ message: error?.message || '申请失败，请稍后重试', type: 'fail' })
  }
}
</script>

<style scoped>
.groups-page {
  min-height: 100vh;
  padding: 16px 12px 88px;
  background: #f4f6fb;
}

.groups-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.header-kicker {
  margin: 0 0 4px;
  font-size: 11px;
  font-weight: 700;
  color: #ff5f84;
}

.groups-header h1 {
  margin: 0;
  font-size: 26px;
  line-height: 1.2;
  color: #1a2236;
}

.groups-header p:last-child {
  margin: 6px 0 0;
  font-size: 13px;
  line-height: 1.5;
  color: #64748b;
}

.create-btn,
.hero-btn,
.group-actions button,
.hot-item button {
  border: none;
  cursor: pointer;
  font-weight: 700;
}

.create-btn {
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  height: 36px;
  padding: 0 13px;
  border-radius: 12px;
  color: #fff;
  background: linear-gradient(135deg, #ff5f84, #6b6ff6);
  box-shadow: 0 8px 18px rgba(107, 111, 246, 0.24);
}

.search-card,
.hero-card,
.group-card,
.side-section,
.feature-grid {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 2px 14px rgba(15, 23, 42, 0.06);
}

.search-card {
  padding: 4px 4px 12px;
}

.filters,
.group-tabs {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  scrollbar-width: none;
}

.filters {
  padding: 0 8px;
}

.filters::-webkit-scrollbar,
.group-tabs::-webkit-scrollbar {
  display: none;
}

.filter-chip,
.tab-btn {
  flex: 0 0 auto;
  height: 32px;
  padding: 0 13px;
  border: 1px solid #edf0f7;
  border-radius: 999px;
  background: #fff;
  color: #64748b;
  font-size: 12px;
  font-weight: 700;
}

.filter-chip--active,
.tab-btn--active {
  color: #4f63f6;
  border-color: rgba(79, 99, 246, 0.28);
  background: #f2f4ff;
}

.hero-card {
  position: relative;
  overflow: hidden;
  margin-top: 12px;
  min-height: 140px;
  padding: 20px 132px 20px 18px;
  color: #fff;
  background: linear-gradient(135deg, #4f63f6 0%, #8a59ef 58%, #db65c7 100%);
}

.hero-kicker {
  margin: 0 0 5px;
  font-size: 12px;
  font-weight: 800;
  color: rgba(255, 255, 255, 0.84);
}

.hero-card h2 {
  margin: 0;
  font-size: 20px;
  line-height: 1.25;
}

.hero-card p:not(.hero-kicker) {
  margin: 7px 0 14px;
  font-size: 13px;
  line-height: 1.5;
  color: rgba(255, 255, 255, 0.86);
}

.hero-btn {
  height: 32px;
  padding: 0 16px;
  border-radius: 10px;
  color: #4f63f6;
  background: #fff;
}

.hero-people {
  position: absolute;
  right: 13px;
  bottom: 18px;
  width: 112px;
  height: 86px;
}

.hero-people span {
  position: absolute;
  bottom: 0;
  width: 38px;
  height: 70px;
  border-radius: 22px 22px 14px 14px;
  background: linear-gradient(180deg, #ffd8cf 0 32%, #ff8ba5 32% 100%);
  box-shadow: 0 12px 18px rgba(38, 28, 88, 0.16);
}

.hero-people span::before {
  content: '';
  position: absolute;
  top: -14px;
  left: 8px;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: #ffd8cf;
}

.hero-people span:nth-child(1) {
  left: 0;
  transform: rotate(-9deg);
}

.hero-people span:nth-child(2) {
  left: 36px;
  height: 82px;
  background: linear-gradient(180deg, #ffd8cf 0 32%, #6f63ec 32% 100%);
  z-index: 2;
}

.hero-people span:nth-child(3) {
  right: 0;
  transform: rotate(8deg);
}

.group-tabs {
  margin: 12px 0;
}

.group-list {
  display: grid;
  gap: 12px;
}

.group-card {
  display: grid;
  grid-template-columns: 104px minmax(0, 1fr);
  gap: 12px;
  padding: 10px;
}

.group-cover {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 112px;
  border-radius: 12px;
  color: #fff;
  overflow: hidden;
}

.group-cover img {
  display: block;
  width: 100%;
  height: 100%;
  min-height: 112px;
  object-fit: cover;
}

.cover-sunrise {
  background: linear-gradient(145deg, #2a2341, #f59e0b);
}

.cover-church {
  background: linear-gradient(145deg, #cbd5e1, #64748b);
}

.cover-pray {
  background: linear-gradient(145deg, #f8c8b8, #a16207);
}

.cover-book {
  background: linear-gradient(145deg, #3f2f23, #9a7354);
}

.cover-music {
  background: linear-gradient(145deg, #27113f, #e879f9);
}

.cover-family {
  background: linear-gradient(145deg, #84cc16, #fef3c7);
}

.group-title-row {
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
}

.group-title-row h2 {
  margin: 0;
  min-width: 0;
  overflow: hidden;
  color: #1a2236;
  font-size: 16px;
  line-height: 1.35;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.group-title-row span {
  flex-shrink: 0;
  padding: 3px 6px;
  border-radius: 7px;
  color: #6b6ff6;
  background: #eef0ff;
  font-size: 10px;
  font-weight: 700;
}

.group-meta,
.group-desc {
  margin: 6px 0 0;
  color: #64748b;
  font-size: 12px;
  line-height: 1.5;
}

.group-desc {
  display: -webkit-box;
  overflow: hidden;
  min-height: 36px;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.group-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 9px;
}

.group-actions button,
.joined-label,
.hot-item button {
  min-width: 64px;
  height: 30px;
  border-radius: 10px;
  font-size: 12px;
}

.group-actions button,
.hot-item button {
  color: #4f63f6;
  background: #f2f4ff;
}

.joined-label {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #4f63f6;
  background: #eef0ff;
  font-weight: 700;
}

.joined-label--pending {
  color: #b7791f;
  background: #fff7e6;
}

.side-section {
  margin-top: 12px;
  padding: 16px;
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.section-head h2 {
  margin: 0;
  color: #1a2236;
  font-size: 16px;
}

.section-head span {
  color: #4f63f6;
  font-size: 12px;
  font-weight: 700;
}

.hot-list,
.feed-list {
  display: grid;
  gap: 12px;
}

.hot-item,
.feed-item {
  display: grid;
  align-items: center;
  gap: 10px;
}

.hot-item {
  grid-template-columns: 42px minmax(0, 1fr) auto;
}

.hot-avatar,
.feed-avatar {
  border-radius: 50%;
}

.hot-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  color: #fff;
  font-weight: 800;
}

.hot-item p,
.feed-item p {
  margin: 0;
  color: #1a2236;
  font-size: 13px;
  font-weight: 700;
}

.hot-item span,
.feed-item small,
.feed-item time {
  display: block;
  margin-top: 2px;
  color: #8898aa;
  font-size: 11px;
}

.feed-item {
  grid-template-columns: 36px minmax(0, 1fr) auto;
}

.feed-avatar {
  width: 36px;
  height: 36px;
}

.feed-item p span {
  margin-left: 4px;
  padding: 2px 5px;
  border-radius: 6px;
  color: #4f63f6;
  background: #eef0ff;
  font-size: 10px;
}

.feature-grid {
  display: grid;
  gap: 12px;
  margin-top: 12px;
  padding: 16px;
}

.feature-item {
  display: grid;
  grid-template-columns: 48px minmax(0, 1fr);
  gap: 12px;
  align-items: center;
}

.feature-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 16px;
  color: #8a59ef;
  background: #f4edff;
}

.feature-item h3 {
  margin: 0;
  color: #1a2236;
  font-size: 14px;
}

.feature-item p {
  margin: 4px 0 0;
  color: #64748b;
  font-size: 12px;
  line-height: 1.45;
}

.avatar-green {
  background: linear-gradient(135deg, #16a34a, #86efac);
}

.avatar-pink {
  background: linear-gradient(135deg, #ff5f84, #f9a8d4);
}

.avatar-blue {
  background: linear-gradient(135deg, #2563eb, #93c5fd);
}

.avatar-purple {
  background: linear-gradient(135deg, #7c3aed, #c4b5fd);
}

@media (max-width: 360px) {
  .hero-card {
    padding-right: 108px;
  }

  .hero-people {
    right: 4px;
    transform: scale(0.86);
  }

  .group-card {
    grid-template-columns: 88px minmax(0, 1fr);
  }
}
</style>
