<template>
  <div class="dating-roster-page">
    <NavBar title="参与者花名册" left-arrow @click-left="router.back()" />

    <div v-if="loading" class="loading-wrap"><van-loading size="24" /></div>
    <section v-else>
      <p class="hint">仅展示本场已签到且已分配编号的参与者</p>
      <van-tabs v-model:active="activeTab" color="#ff5f84" shrink>
        <van-tab title="男嘉宾" name="male" />
        <van-tab title="女嘉宾" name="female" />
      </van-tabs>

      <div class="list">
        <article
          v-for="item in currentList"
          :key="item.participantKey || item.userId || item.guestParticipantId"
          class="roster-item"
          @click="openCard(item)"
        >
          <p class="badge">
            {{ item.badgeLabel }}
            <van-icon v-if="item.liked" name="like" class="liked-icon" color="#ff5f84" />
          </p>
          <div class="meta">
            <p v-if="item.profileCompleted" class="line">
              {{ item.age ? `${item.age} 岁` : '' }}
              <span v-if="item.city"> · {{ item.city }}</span>
              <span v-if="item.occupation"> · {{ item.occupation }}</span>
            </p>
            <p v-else class="line muted">资料完善中</p>
          </div>
          <van-icon name="arrow" class="arrow" />
        </article>
        <van-empty v-if="!currentList.length" description="暂无参与者" />
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import NavBar from '@/components/NavBar.vue'
import { fetchDatingRoster } from '@/api/datingEvent.js'

const route = useRoute()
const router = useRouter()
const eventId = computed(() => route.params.eventId)

const loading = ref(true)
const activeTab = ref('male')
const roster = ref({ male: [], female: [] })

const currentList = computed(() => roster.value[activeTab.value] || [])

async function load() {
  loading.value = true
  try {
    roster.value = await fetchDatingRoster(eventId.value)
  } catch (e) {
    showToast(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

function openCard(item) {
  if (!item.profileCompleted) {
    showToast('对方尚未完善本场活动资料')
    return
  }
  const key = item.participantKey || `REGISTERED:${item.userId}`
  router.push(`/fellowship/events/${eventId.value}/dating/card/${encodeURIComponent(key)}`)
}

onMounted(load)
</script>

<style scoped>
.dating-roster-page {
  min-height: 100vh;
  background: #fff;
}

.loading-wrap {
  padding: 48px;
  text-align: center;
}

.hint {
  margin: 0;
  padding: 12px 16px 0;
  font-size: 12px;
  color: #94a3b8;
}

.list {
  padding: 8px 16px 24px;
}

.roster-item {
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 12px;
  align-items: center;
  padding: 14px 0;
  border-bottom: 1px solid #f1f5f9;
}

.badge {
  margin: 0;
  min-width: 56px;
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 18px;
  font-weight: 800;
  color: #ff5f84;
}

.liked-icon {
  font-size: 14px;
}

.line {
  margin: 0;
  font-size: 14px;
  color: #334155;
}

.line.muted {
  color: #94a3b8;
}

.arrow {
  color: #cbd5e1;
}
</style>
