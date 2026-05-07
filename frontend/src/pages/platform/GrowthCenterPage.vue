<template>
  <section class="growth-page">
    <header class="growth-head">
      <button type="button" class="growth-back" aria-label="返回" @click="$router.back()">‹</button>
      <h1 class="growth-title">成长中心</h1>
    </header>

    <div class="growth-body">
      <p v-if="loading" class="growth-status">加载中...</p>
      <template v-else>
        <article class="growth-card">
          <h2>身份卡片</h2>
          <div class="identity-row">
            <span>等级 Lv.{{ growth.level }}</span>
            <span>阶段 {{ growth.stage || 'normal' }}</span>
          </div>
          <p class="total">总贡献 {{ growth.totalContribution }}</p>
        </article>

        <article class="growth-card">
          <h2>四维贡献</h2>
          <div class="grid">
            <div class="item"><label>内容</label><strong>{{ growth.contribContent }}</strong></div>
            <div class="item"><label>组织</label><strong>{{ growth.contribOrg }}</strong></div>
            <div class="item"><label>传播</label><strong>{{ growth.contribSpread }}</strong></div>
            <div class="item"><label>城市</label><strong>{{ growth.contribCity }}</strong></div>
          </div>
        </article>

        <article class="growth-card">
          <h2>成长进度</h2>
          <p class="progress">
            当前 Lv.{{ growth.level }}，下一等级 Lv.{{ growth.nextLevel }}（阈值 {{ growth.nextLevelThreshold }}）
          </p>
          <p class="progress">还差 {{ growth.contributionToNextLevel }} 贡献值升级</p>
        </article>

        <article class="growth-card">
          <h2>贡献流水</h2>
          <ul v-if="contributions.length" class="list">
            <li v-for="log in contributions" :key="log.id">
              <div>
                <strong>{{ log.eventType }}</strong>
                <p>{{ formatTime(log.occurredAt) }}</p>
              </div>
              <span class="delta">+{{ log.deltaFinal }}</span>
            </li>
          </ul>
          <p v-else class="empty">暂无贡献记录</p>
        </article>

        <article class="growth-card">
          <h2>成就占位</h2>
          <p v-if="achievements.length">已解锁 {{ achievements.length }} 项成就</p>
          <p v-else class="empty">暂无成就，继续参与互动解锁</p>
        </article>

        <article class="growth-card">
          <h2>里程碑动态</h2>
          <ul v-if="milestones.length" class="list">
            <li v-for="event in milestones" :key="event.id">
              <div>
                <strong>{{ event.eventType }}</strong>
                <p>{{ formatTime(event.occurredAt) }}</p>
              </div>
              <span class="delta">{{ event.settleStatus }}</span>
            </li>
          </ul>
          <p v-else class="empty">暂无里程碑动态</p>
        </article>
      </template>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import {
  getGrowthAchievements,
  getGrowthCenter,
  getGrowthContributions,
  getGrowthEvents
} from '@/api/growth.js'

const loading = ref(false)
const growth = ref({
  level: 1,
  stage: 'normal',
  totalContribution: 0,
  contribContent: 0,
  contribOrg: 0,
  contribSpread: 0,
  contribCity: 0,
  nextLevel: 2,
  nextLevelThreshold: 50,
  contributionToNextLevel: 50
})
const contributions = ref([])
const achievements = ref([])
const milestones = ref([])

function formatTime(value) {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

onMounted(async () => {
  loading.value = true
  try {
    const [centerRes, contributionRes, achievementRes, eventRes] = await Promise.allSettled([
      getGrowthCenter(),
      getGrowthContributions(20),
      getGrowthAchievements(20),
      getGrowthEvents(20)
    ])
    if (centerRes.status === 'fulfilled' && centerRes.value) {
      growth.value = { ...growth.value, ...centerRes.value }
    }
    contributions.value = contributionRes.status === 'fulfilled' && Array.isArray(contributionRes.value)
      ? contributionRes.value
      : []
    achievements.value = achievementRes.status === 'fulfilled' && Array.isArray(achievementRes.value)
      ? achievementRes.value
      : []
    const events = eventRes.status === 'fulfilled' && Array.isArray(eventRes.value) ? eventRes.value : []
    milestones.value = events.filter(item => ['GROUP_CREATED', 'LEVEL_UP', 'ACHIEVEMENT_UNLOCKED'].includes(item.eventType))
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.growth-page { min-height: 100vh; background: var(--lc-bg); color: var(--lc-text); }
.growth-head { display: flex; align-items: center; background: var(--lc-surface); border-bottom: 1px solid var(--lc-soft-alt); }
.growth-back { width: 48px; height: 48px; border: 0; background: transparent; font-size: 22px; color: var(--lc-indigo); }
.growth-title { margin: 0; font-size: 17px; font-weight: 800; }
.growth-body { max-width: 720px; margin: 0 auto; padding: 14px 12px calc(90px + env(safe-area-inset-bottom)); }
.growth-status { text-align: center; padding: 36px 0; color: var(--lc-subtle); }
.growth-card { background: var(--lc-surface); border: 1px solid var(--lc-soft-alt); border-radius: 14px; padding: 14px; margin-bottom: 12px; }
.growth-card h2 { margin: 0 0 10px; font-size: 15px; }
.identity-row { display: flex; justify-content: space-between; font-size: 14px; }
.total { margin: 8px 0 0; font-weight: 700; }
.grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 8px; }
.item { background: var(--lc-bg); border-radius: 10px; padding: 10px; display: flex; justify-content: space-between; }
.item label { color: var(--lc-subtle); font-size: 13px; }
.progress { margin: 6px 0; font-size: 13px; color: var(--lc-slate); }
.list { list-style: none; padding: 0; margin: 0; }
.list li { display: flex; justify-content: space-between; align-items: center; padding: 8px 0; border-bottom: 1px solid var(--lc-soft); }
.list li:last-child { border-bottom: 0; }
.list strong { font-size: 13px; }
.list p { margin: 3px 0 0; color: var(--lc-subtle); font-size: 12px; }
.delta { color: var(--lc-violet); font-weight: 700; }
.empty { margin: 0; font-size: 13px; color: var(--lc-subtle); }
</style>
