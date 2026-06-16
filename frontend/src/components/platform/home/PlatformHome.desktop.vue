<template>
  <main class="platform-home">
    <HomeHero title="Love Cube 平台" subtitle="连接内容、团体与真实互动" @browse="goContent" @publish="goPublish" />
    <HomeNotice :notice="platformNotices[0]" />
    <HomeQuickEntry :entries="quickEntries" featured-type="play" @go="goQuick" />
    <HomeFeatured :items="featured" @open="openDetail" />
    <HomeActivityBanner text="运营活动位：本周活动报名进行中，欢迎参与。" />
    <section class="local-recommend platform-card">
      <div class="local-head">
        <div class="section-head-copy">
          <h3>本地推荐</h3>
          <p>发现附近的人、活动、圈子和实用信息</p>
        </div>
        <button type="button" class="home-action-btn" @click="goLocal">查看更多</button>
      </div>
      <div v-if="localRecommendations.length" class="local-list">
        <button
          v-for="item in localRecommendations"
          :key="item.id"
          type="button"
          class="local-item"
          @click="goLocal"
        >
          <strong>{{ item.title }}</strong>
          <span>{{ item.location || '地点待补充' }} · 热度 {{ item.heat || 0 }}</span>
        </button>
      </div>
      <p v-else class="local-empty-tip">暂无本地推荐。</p>
    </section>
    <section class="updates-card platform-card">
      <div class="updates-head">
        <div class="section-head-copy">
          <h3>最近更新</h3>
          <p>你可以快速查看近期改动与待处理事项</p>
        </div>
        <div class="updates-actions">
          <button type="button" class="home-action-btn is-ghost" @click="goChangelog">查看全部</button>
          <button v-if="isAdmin" type="button" class="home-action-btn is-primary" @click="goPendingUpdates">快速处理</button>
        </div>
      </div>
      <ul v-if="homeUpdates.length" class="updates-list">
        <li v-for="item in homeUpdates" :key="`${item.version || 'v'}-${item.title}`">
          <div class="updates-item-main">
            <strong>{{ item.title }}</strong>
            <span>{{ item.version || '最新' }} {{ item.date || '' }}</span>
          </div>
          <p v-if="changelogPreview(item.detail)" class="updates-item-detail">
            {{ changelogPreview(item.detail) }}
          </p>
        </li>
      </ul>
      <p v-else class="local-empty-tip">暂无更新日志。</p>
    </section>
    <HomeFeedPreview :items="latest" @more="goContent" />
  </main>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import HomeHero from '@/components/platform/home/HomeHero.vue'
import HomeNotice from '@/components/platform/home/HomeNotice.vue'
import HomeQuickEntry from '@/components/platform/home/HomeQuickEntry.vue'
import HomeFeatured from '@/components/platform/home/HomeFeatured.vue'
import HomeActivityBanner from '@/components/platform/home/HomeActivityBanner.vue'
import HomeFeedPreview from '@/components/platform/home/HomeFeedPreview.vue'
import { platformNotices, platformContentList } from '@/mock/platformContent.js'
import { getLocalResources } from '@/api/localResources.js'
import { fetchHomeConfig } from '@/api/platformContent.js'
import { useUserStore } from '@/stores/user.js'

const router = useRouter()
const userStore = useUserStore()
const { isAdmin } = storeToRefs(userStore)
const quickEntries = [
  { label: '全部功能', type: 'play' },
  { label: '每日心声', type: 'mood' },
  { label: '平台公告', type: 'dynamic' },
  { label: '活动中心', type: 'event' },
  { label: 'AI工具', type: 'ai' },
  { label: '联谊', type: 'match' }
]
const featured = computed(() => platformContentList.slice(0, 4))
const latest = computed(() => platformContentList.slice(0, 5))
const localRecommendations = ref([])
const homeUpdates = ref([])

function goContent(){ router.push('/platform/content') }
function goPublish(){ router.push('/platform/positive-share') }
function goLocal(){ router.push('/platform/local') }
function goChangelog(){ router.push('/platform/changelog') }
function changelogPreview(detail) {
  const text = String(detail || '').trim()
  if (!text) return ''
  return text.split('\n').find((line) => line.trim())?.trim() || ''
}
function goPendingUpdates(){ router.push('/platform/pending-updates') }
function goQuick(type){
  if (type === 'play') {
    router.push('/platform/play')
    return
  }
  if (type === 'match') {
    router.push('/fellowship')
    return
  }
  router.push({ path: '/platform/content', query: { type } })
}
function openDetail(item){ if (item?.id) router.push('/platform/content') }

async function loadLocalRecommendations() {
  try {
    const data = await getLocalResources()
    const list = Array.isArray(data) ? data : []
    localRecommendations.value = list
      .sort((a, b) => Number(b?.heat || 0) - Number(a?.heat || 0))
      .slice(0, 3)
  } catch {
    localRecommendations.value = []
  }
}

async function loadHomeUpdates() {
  try {
    const data = await fetchHomeConfig()
    const rows = Array.isArray(data?.changelog) ? data.changelog : []
    homeUpdates.value = rows.slice(0, 3)
  } catch {
    homeUpdates.value = []
  }
}

onMounted(async () => {
  await Promise.all([loadLocalRecommendations(), loadHomeUpdates()])
})
</script>

<style scoped>
.platform-home {
  width: min(100% - 24px, 560px);
  margin: 16px auto;
  display: grid;
  gap: 12px;
  padding-bottom: calc(72px + env(safe-area-inset-bottom, 0px));
  box-sizing: border-box;
}

.local-recommend {
  border: 1px solid var(--lc-blue-border);
  background: linear-gradient(135deg, var(--lc-surface), var(--lc-blue-light));
}

.local-head,
.updates-head {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px 12px;
}

.section-head-copy {
  flex: 1 1 200px;
  min-width: 0;
}

.local-head h3,
.updates-head h3 {
  margin: 0;
  font-size: 18px;
  color: var(--lc-text);
  line-height: 1.3;
}

.local-head p,
.updates-head p {
  margin: 6px 0 0;
  color: var(--lc-muted);
  font-size: 13px;
  line-height: 1.45;
}

.home-action-btn {
  flex: 0 0 auto;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 36px;
  padding: 0 14px;
  border-radius: 10px;
  border: 1px solid var(--lc-blue-border);
  background: var(--lc-surface);
  color: var(--lc-blue);
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  white-space: nowrap;
  -webkit-tap-highlight-color: transparent;
}

.home-action-btn.is-ghost {
  background: var(--lc-surface);
  color: var(--lc-text);
  border-color: var(--lc-border);
}

.home-action-btn.is-primary {
  background: var(--lc-blue);
  color: var(--lc-surface);
  border-color: var(--lc-blue);
}

.updates-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.local-list {
  margin-top: 10px;
  display: grid;
  gap: 8px;
}

.local-item {
  width: 100%;
  text-align: left;
  padding: 10px 12px;
  border-radius: 10px;
  background: var(--lc-surface);
  border: 1px solid var(--lc-border);
  display: grid;
  gap: 4px;
  cursor: pointer;
}

.local-item strong {
  font-size: 14px;
  color: var(--lc-text);
  line-height: 1.35;
  overflow-wrap: anywhere;
}

.local-item span {
  font-size: 12px;
  color: var(--lc-muted);
  line-height: 1.4;
  overflow-wrap: anywhere;
}

.local-item:hover {
  border-color: var(--lc-blue-border);
  background: var(--lc-blue-light);
}

.local-empty-tip {
  margin: 10px 0 0;
  font-size: 13px;
  color: var(--lc-subtle);
}

.updates-card {
  border: 1px solid var(--lc-border);
}

.updates-list {
  list-style: none;
  margin: 10px 0 0;
  padding: 0;
  display: grid;
  gap: 8px;
}

.updates-list li {
  display: grid;
  gap: 4px;
  padding: 10px 12px;
  border: 1px solid var(--lc-border);
  border-radius: 10px;
  background: var(--lc-surface);
}

.updates-item-main {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  gap: 4px 10px;
  align-items: baseline;
}

.updates-list strong {
  flex: 1 1 160px;
  min-width: 0;
  font-size: 14px;
  color: var(--lc-text);
  line-height: 1.35;
  overflow-wrap: anywhere;
}

.updates-list span {
  flex: 0 0 auto;
  font-size: 12px;
  color: var(--lc-subtle);
  line-height: 1.35;
}

.updates-item-detail {
  margin: 0;
  font-size: 12px;
  line-height: 1.5;
  color: var(--lc-muted);
  overflow-wrap: anywhere;
}

@media (max-width: 480px) {
  .platform-home {
    width: calc(100% - 16px);
    margin: 12px auto;
    gap: 10px;
  }

  .local-head h3,
  .updates-head h3 {
    font-size: 17px;
  }

  .local-head,
  .updates-head {
    flex-direction: column;
    align-items: stretch;
  }

  .home-action-btn {
    width: 100%;
    min-height: 40px;
  }

  .updates-actions {
    display: grid;
    grid-template-columns: 1fr 1fr;
    width: 100%;
  }

  .updates-actions .home-action-btn:only-child {
    grid-column: 1 / -1;
  }
}

@media (max-width: 360px) {
  .updates-actions {
    grid-template-columns: 1fr;
  }
}
</style>
