<template>
  <main class="platform-home">
    <HomeHero title="Love Cube 平台" subtitle="连接内容、团体与真实互动" @browse="goContent" @publish="goPublish" />
    <HomeNotice :notice="platformNotices[0]" />
    <HomeQuickEntry :entries="quickEntries" @go="goQuick" />
    <HomeFeatured :items="featured" @open="openDetail" />
    <HomeActivityBanner text="运营活动位：本周活动报名进行中，欢迎参与。" />
    <HomeFeedPreview :items="latest" @more="goContent" />
  </main>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import HomeHero from '@/components/platform/home/HomeHero.vue'
import HomeNotice from '@/components/platform/home/HomeNotice.vue'
import HomeQuickEntry from '@/components/platform/home/HomeQuickEntry.vue'
import HomeFeatured from '@/components/platform/home/HomeFeatured.vue'
import HomeActivityBanner from '@/components/platform/home/HomeActivityBanner.vue'
import HomeFeedPreview from '@/components/platform/home/HomeFeedPreview.vue'
import { platformNotices, platformContentList } from '@/mock/platformContent.js'

const router = useRouter()
const quickEntries = [
  { label: '每日心声', type: 'mood' },
  { label: '平台动态', type: 'dynamic' },
  { label: '活动中心', type: 'event' },
  { label: 'AI工具', type: 'ai' }
]
const featured = computed(() => platformContentList.slice(0, 4))
const latest = computed(() => platformContentList.slice(0, 5))

function goContent(){ router.push('/platform/content') }
function goPublish(){ router.push('/platform/publish') }
function goQuick(type){ router.push({ path: '/platform/content', query: { type } }) }
function openDetail(item){ if (item?.id) router.push('/platform/content') }
</script>

<style scoped>
.platform-home{width:min(100% - 24px,560px);margin:16px auto;display:grid;gap:12px;padding-bottom:calc(72px + env(safe-area-inset-bottom));}
</style>
