<template>
  <main class="platform-home">
    <HomeHero title="Love Cube 平台" subtitle="连接内容、团体与真实互动" @browse="goContent" @publish="goPublish" />
    <HomeNotice :notice="platformNotices[0]" />
    <HomeQuickEntry :entries="quickEntries" @go="goQuick" />
    <HomeFeatured :items="featured" @open="openDetail" />
    <HomeActivityBanner text="运营活动位：本周活动报名进行中，欢迎参与。" />
    <section class="local-recommend platform-card">
      <div class="local-head">
        <div>
          <h3>本地推荐</h3>
          <p>发现附近的人、活动、圈子和实用信息</p>
        </div>
        <button type="button" class="platform-btn" @click="goLocal">查看更多</button>
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
        <div>
          <h3>最近更新</h3>
          <p>你可以快速查看近期改动与待处理事项</p>
        </div>
        <div class="updates-actions">
          <button type="button" class="platform-btn is-ghost" @click="goChangelog">查看全部</button>
          <button v-if="isAdmin" type="button" class="platform-btn" @click="goPendingUpdates">快速处理</button>
        </div>
      </div>
      <ul v-if="homeUpdates.length" class="updates-list">
        <li v-for="item in homeUpdates" :key="`${item.version || 'v'}-${item.title}`">
          <strong>{{ item.title }}</strong>
          <span>{{ item.version || '最新' }} {{ item.date || '' }}</span>
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
function goPendingUpdates(){ router.push('/platform/pending-updates') }
function goQuick(type){
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
.platform-home{width:min(100% - 24px,560px);margin:16px auto;display:grid;gap:12px;padding-bottom:calc(72px + env(safe-area-inset-bottom));}
.local-recommend { border: 1px solid var(--lc-blue-border); background: linear-gradient(135deg, var(--lc-surface), var(--lc-blue-light)); }
.local-head { display: flex; justify-content: space-between; align-items: center; gap: 12px; }
.local-head h3 { margin: 0; font-size: 18px; color: var(--lc-text); }
.local-head p { margin: 6px 0 0; color: var(--lc-muted); font-size: 13px; }
.local-list { margin-top: 10px; display: grid; gap: 8px; }
.local-item { width: 100%; text-align: left; padding: 8px 10px; border-radius: 10px; background: var(--lc-surface); border: 1px solid var(--lc-border); display: grid; gap: 2px; cursor: pointer; }
.local-item strong { font-size: 14px; color: var(--lc-text); }
.local-item span { font-size: 12px; color: var(--lc-muted); }
.local-item:hover { border-color: var(--lc-blue-border); background: var(--lc-blue-light); }
.local-empty-tip { margin: 10px 0 0; font-size: 13px; color: var(--lc-subtle); }
.updates-card { border: 1px solid var(--lc-border); }
.updates-head { display: flex; justify-content: space-between; gap: 10px; align-items: center; }
.updates-head h3 { margin: 0; font-size: 18px; color: var(--lc-text); }
.updates-head p { margin: 6px 0 0; font-size: 13px; color: var(--lc-muted); }
.updates-actions { display: flex; gap: 8px; align-items: center; }
.updates-list { list-style: none; margin: 10px 0 0; padding: 0; display: grid; gap: 8px; }
.updates-list li { display: grid; gap: 2px; padding: 8px 10px; border: 1px solid var(--lc-border); border-radius: 10px; background: var(--lc-surface); }
.updates-list strong { font-size: 14px; color: var(--lc-text); }
.updates-list span { font-size: 12px; color: var(--lc-subtle); }
.platform-btn.is-ghost { background: var(--lc-surface); color: var(--lc-text); border: 1px solid var(--lc-border); box-shadow: none; }
</style>
