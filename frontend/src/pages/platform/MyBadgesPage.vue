<template>
  <section class="sp-page">
    <header class="sp-head">
      <button type="button" class="sp-back" aria-label="返回" @click="$router.back()">‹</button>
      <h1 class="sp-title">我的徽章</h1>
    </header>

    <div class="sp-body">
      <p v-if="loading" class="sp-status">加载中...</p>
      <template v-else>
        <!-- 已获得 -->
        <div v-if="unlockedBadges.length" class="sp-card">
          <div class="sp-card-title">已获得 ({{ unlockedBadges.length }})</div>
          <div class="badge-grid">
            <div v-for="b in unlockedBadges" :key="b.code" class="badge-item unlocked">
              <div class="badge-icon">{{ b.icon }}</div>
              <div class="badge-name">{{ b.name }}</div>
              <div class="badge-desc">{{ b.description }}</div>
            </div>
          </div>
        </div>

        <!-- 未获得 -->
        <div v-if="lockedBadges.length" class="sp-card">
          <div class="sp-card-title">未获得 ({{ lockedBadges.length }})</div>
          <div class="badge-grid">
            <div v-for="b in lockedBadges" :key="b.code" class="badge-item locked">
              <div class="badge-icon locked-icon">🔒</div>
              <div class="badge-name">{{ b.name }}</div>
              <div class="badge-desc">{{ b.condition }}</div>
            </div>
          </div>
        </div>

        <p v-if="!unlockedBadges.length && !lockedBadges.length" class="sp-status sp-empty">
          暂无徽章数据
        </p>
      </template>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { getMyGrowth, getMyBadges } from '@/api/growth.js'

const loading = ref(false)
const allBadges = ref([])

const defaultBadges = [
  { code: 'FIRST_POST', name: '初次发声', icon: '🌱', description: '发布第一条动态', condition: '发布第一条动态', unlocked: false, progress: 0, conditionValue: 1 },
  { code: 'DAILY_7', name: '坚持七天', icon: '🔥', description: '连续签到 7 天', condition: '连续签到 7 天', unlocked: false, progress: 0, conditionValue: 7 },
  { code: 'SHARE_10', name: '分享达人', icon: '🎤', description: '累计分享 10 次', condition: '累计分享 10 次', unlocked: false, progress: 0, conditionValue: 10 },
  { code: 'LIKE_50', name: '暖心使者', icon: '💗', description: '获得 50 次鼓励', condition: '累计获得 50 次鼓励', unlocked: false, progress: 0, conditionValue: 50 },
  { code: 'LV_5', name: '成长先锋', icon: '⭐', description: '达到 5 级', condition: '升至平台 5 级', unlocked: false, progress: 0, conditionValue: 5 }
]

const unlockedBadges = computed(() => allBadges.value.filter(b => b.unlocked))
const lockedBadges = computed(() => allBadges.value.filter(b => !b.unlocked))

onMounted(async () => {
  loading.value = true
  try {
    const [badgeRes, growthRes] = await Promise.allSettled([getMyBadges(), getMyGrowth()])
    if (badgeRes.status === 'fulfilled' && Array.isArray(badgeRes.value) && badgeRes.value.length) {
      allBadges.value = badgeRes.value
    } else if (growthRes.status === 'fulfilled' && Array.isArray(growthRes.value?.badges) && growthRes.value.badges.length) {
      allBadges.value = growthRes.value.badges
    } else {
      allBadges.value = defaultBadges
    }
  } catch {
    allBadges.value = defaultBadges
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.sp-page { min-height: 100vh; background: #f6f7fb; color: #0f172a; }

.sp-head {
  position: sticky; top: 0; z-index: 10;
  display: flex; align-items: center;
  background: #fff; border-bottom: 1px solid #eef0f4;
}
.sp-back {
  width: 48px; height: 52px; flex: 0 0 auto;
  display: grid; place-items: center;
  border: 0; background: none; font-size: 22px; color: #4f46e5; cursor: pointer;
}
.sp-title { flex: 1; margin: 0; font-size: 17px; font-weight: 800; }

.sp-body {
  padding: 16px 14px calc(80px + env(safe-area-inset-bottom));
  max-width: 680px; margin: 0 auto;
}
.sp-status { text-align: center; padding: 40px 0; color: #94a3b8; font-size: 14px; }
.sp-empty::before { display: block; font-size: 32px; margin-bottom: 10px; content: "🏅"; }

.sp-card {
  background: #fff; border: 1px solid #eef0f4; border-radius: 18px;
  box-shadow: 0 3px 12px rgba(15,23,42,0.04); margin-bottom: 14px; padding: 16px;
}
.sp-card-title { font-size: 15px; font-weight: 800; margin-bottom: 14px; color: #0f172a; }

.badge-grid {
  display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px;
}

.badge-item {
  display: flex; flex-direction: column; align-items: center; gap: 6px;
  padding: 14px 8px; border-radius: 14px; border: 1px solid #eef0f4; text-align: center;
}
.badge-item.unlocked { background: linear-gradient(145deg, #f5f3ff, #fff); border-color: #e0d9ff; }
.badge-item.locked { background: #f8fafc; opacity: 0.6; }

.badge-icon { font-size: 28px; line-height: 1; }
.locked-icon { font-size: 22px; }
.badge-name { font-size: 12px; font-weight: 700; color: #0f172a; }
.badge-desc { font-size: 10px; color: #94a3b8; line-height: 1.3; }

@media (min-width: 480px) {
  .badge-grid { grid-template-columns: repeat(4, 1fr); }
}
@media (max-width: 767px) {
  :global(.platform-header), :global(.platform-footer), :global(.co-creation-toolbar) {
    display: none !important;
  }
}
@media (min-width: 768px) {
  .sp-body { padding-top: 24px; padding-bottom: 48px; }
  .badge-grid { grid-template-columns: repeat(5, 1fr); }
}
</style>
