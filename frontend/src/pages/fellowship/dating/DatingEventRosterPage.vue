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

      <div class="card-grid">
        <article
          v-for="item in currentList"
          :key="item.participantKey || item.userId || item.guestParticipantId"
          class="roster-card"
          @click="openCard(item)"
        >
          <div class="card-cover">
            <img
              v-if="item.photo"
              :src="photoUrl(item.photo)"
              alt=""
              class="cover-img"
              loading="lazy"
            />
            <div v-else class="cover-placeholder">
              <span class="placeholder-text">{{ badgeInitial(item.badgeLabel) }}</span>
            </div>
            <span v-if="item.liked" class="liked-badge" aria-label="已喜欢">
              <van-icon name="like" />
            </span>
          </div>
          <div class="card-body">
            <p class="badge">{{ item.badgeLabel }}</p>
            <p v-if="item.profileCompleted" class="meta-line">
              <span v-if="item.age">{{ item.age }} 岁</span>
              <span v-if="item.city">{{ item.age ? ' · ' : '' }}{{ item.city }}</span>
            </p>
            <p v-if="item.profileCompleted && item.occupation" class="meta-sub">{{ item.occupation }}</p>
            <p v-else-if="!item.profileCompleted" class="meta-line muted">资料完善中</p>
          </div>
        </article>
        <van-empty v-if="!currentList.length" class="empty-state" description="暂无参与者" />
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
import { resolveUploadUrl } from '@/utils/image.js'

const route = useRoute()
const router = useRouter()
const eventId = computed(() => route.params.eventId)

const loading = ref(true)
const activeTab = ref('male')
const roster = ref({ male: [], female: [] })

const currentList = computed(() => roster.value[activeTab.value] || [])

function badgeInitial(label) {
  const text = String(label || '').trim()
  return text ? text.slice(-2) : '—'
}

function photoUrl(url) {
  return resolveUploadUrl(url)
}

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
  background: #f8fafc;
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

.card-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  padding: 12px 16px 24px;
}

.roster-card {
  background: #fff;
  border-radius: 14px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(15, 23, 42, 0.06);
  cursor: pointer;
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}

.roster-card:active {
  transform: scale(0.98);
}

.card-cover {
  position: relative;
  aspect-ratio: 3 / 4;
  background: linear-gradient(160deg, #fff0f5 0%, #ffe4ec 100%);
}

.cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.placeholder-text {
  font-size: 28px;
  font-weight: 800;
  color: #ff5f84;
  letter-spacing: 0.04em;
}

.liked-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.92);
  color: #ff5f84;
  font-size: 14px;
  box-shadow: 0 2px 8px rgba(255, 95, 132, 0.25);
}

.card-body {
  padding: 10px 12px 12px;
}

.badge {
  margin: 0;
  font-size: 16px;
  font-weight: 800;
  color: #ff5f84;
}

.meta-line {
  margin: 6px 0 0;
  font-size: 12px;
  line-height: 1.45;
  color: #475569;
}

.meta-sub {
  margin: 2px 0 0;
  font-size: 12px;
  line-height: 1.45;
  color: #64748b;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.meta-line.muted {
  color: #94a3b8;
}

.empty-state {
  grid-column: 1 / -1;
  padding: 24px 0;
}
</style>
