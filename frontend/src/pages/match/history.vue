<template>
  <div class="history-page">
    <NavBar title="浏览记录" />
    <van-tabs v-model:active="activeName" color="#FF5F84" title-active-color="#FF5F84" @change="onTabChange">
      <van-tab title="喜欢" name="liked" />
      <van-tab title="跳过" name="skipped" />
      <van-tab title="全部" name="all" />
    </van-tabs>

    <van-list
      :key="activeName"
      v-model:loading="loading"
      :finished="finished"
      finished-text="没有更多了"
      class="history-list"
      @load="onLoad"
    >
      <UserCard
        v-for="item in items"
        :key="itemRowKey(item)"
        :user="item"
        @click="goProfile(item.userId)"
      >
        <template #action>
          <div class="row-right">
            <van-tag :type="tagType(item.interactionType)" plain size="medium">
              {{ typeLabel(item.interactionType) }}
            </van-tag>
            <p v-if="item.actedAt" class="acted-at">{{ formatActedAt(item.actedAt) }}</p>
            <van-button
              v-if="activeName === 'skipped' && item.interactionType === 'skip'"
              size="mini"
              plain
              type="primary"
              :loading="rewindingId === item.userId"
              @click.stop="handleRewind(item)"
            >
              撤回跳过
            </van-button>
          </div>
        </template>
      </UserCard>
      <van-empty v-if="!loading && !items.length" description="暂无记录" />
    </van-list>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import NavBar from '@/components/NavBar.vue'
import UserCard from '@/components/UserCard.vue'
import { getMatchBrowseHistory, rewindSkip } from '@/api/match.js'
import { normalizeUser } from '@/utils/normalizeUser.js'
import { useFellowshipNavBase } from '@/composables/useFellowshipNavBase.js'

const route = useRoute()
const router = useRouter()
const { fellowshipPath } = useFellowshipNavBase()
const activeName = ref(
  route.query.tab === 'skipped' ? 'skipped' : route.query.tab === 'all' ? 'all' : 'liked'
)
const items = ref([])
const loading = ref(false)
const finished = ref(false)
const page = ref(1)
const pageSize = 20
const fetching = ref(false)
const rewindingId = ref(null)

function typeLabel(t) {
  const m = { like: '喜欢', super_like: '超级喜欢', follow: '收藏', skip: '跳过' }
  return m[t] || t || ''
}

function tagType(t) {
  if (t === 'skip') return 'default'
  if (t === 'super_like') return 'danger'
  if (t === 'follow') return 'warning'
  return 'primary'
}

function formatActedAt(raw) {
  if (raw == null) return ''
  try {
    let d
    if (Array.isArray(raw) && raw.length >= 3) {
      const [y, mo, day, h = 0, min = 0, sec = 0] = raw
      d = new Date(y, mo - 1, day, h, min, sec)
    } else if (typeof raw === 'string' || typeof raw === 'number') {
      d = new Date(raw)
    } else {
      return ''
    }
    if (Number.isNaN(d.getTime())) return ''
    return d.toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
  } catch {
    return ''
  }
}

function itemRowKey(item) {
  const t = item?.actedAt != null ? String(item.actedAt) : ''
  return `${item?.userId}-${item?.interactionType}-${t}`
}

function mapRow(row) {
  const u = normalizeUser(row)
  if (!u) return null
  return {
    ...u,
    interactionType: row.interactionType,
    actedAt: row.actedAt
  }
}

async function onLoad() {
  if (finished.value || fetching.value) return
  fetching.value = true
  loading.value = true
  try {
    const res = await getMatchBrowseHistory({
      tab: activeName.value,
      page: page.value,
      size: pageSize
    })
    const next = (res.list || []).map(mapRow).filter(Boolean)
    items.value.push(...next)
    page.value += 1
    finished.value = !res.hasMore || next.length === 0
  } catch (e) {
    showToast({ type: 'fail', message: e?.message || '加载失败' })
    finished.value = true
  } finally {
    fetching.value = false
    loading.value = false
  }
}

function onTabChange(name) {
  items.value = []
  page.value = 1
  finished.value = false
  loading.value = false
  fetching.value = false
  activeName.value = name
}

function goProfile(userId) {
  if (!userId) return
  router.push(fellowshipPath(`/user-profile/${userId}`))
}

async function handleRewind(item) {
  if (!item?.userId || rewindingId.value) return
  rewindingId.value = item.userId
  try {
    await rewindSkip(item.userId)
    items.value = items.value.filter((row) => row.userId !== item.userId)
    showToast({ type: 'success', message: '已撤回跳过，可在认识页再次看到 TA' })
  } catch (e) {
    if (e?.data?.code === 'REWIND_DAILY_LIMIT' || e?.status === 429) {
      showToast({ type: 'fail', message: e?.message || '今日撤回次数已用完' })
      router.push(fellowshipPath('/vip'))
    } else {
      showToast({ type: 'fail', message: e?.message || '撤回失败' })
    }
  } finally {
    rewindingId.value = null
  }
}
</script>

<style scoped>
.history-page {
  min-height: 100vh;
  background: #f8f9fb;
}

.history-list {
  padding-top: 8px;
}

.row-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6px;
  flex-shrink: 0;
}

.acted-at {
  margin: 0;
  font-size: 11px;
  color: #a0abbe;
}
</style>
