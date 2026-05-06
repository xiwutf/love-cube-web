<template>
  <div v-if="showBanner" class="photo-banner" role="status">
    <van-icon name="warning-o" class="photo-banner__icon" />
    <div class="photo-banner__text">
      <p class="photo-banner__title">请上传至少一张生活照</p>
      <p class="photo-banner__desc">
        你已开通联谊，但尚未上传生活照。请尽快补充，否则将无法继续使用推荐、喜欢等联谊功能。
      </p>
    </div>
    <router-link class="photo-banner__link" to="/fellowship/profile/edit">去上传</router-link>
  </div>
</template>

<script setup>
import { computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user.js'

const route = useRoute()
const userStore = useUserStore()

watch(
  () => route.path,
  (path, oldPath) => {
    const wasOnEdit =
      oldPath && (oldPath.includes('/profile/edit') || oldPath.includes('/profile-edit'))
    const nowOnEdit = path.includes('/profile/edit') || path.includes('/profile-edit')
    if (wasOnEdit && !nowOnEdit && userStore.isLoggedIn) {
      userStore.refreshCurrentUser().catch(() => {})
    }
  }
)

const hideOnEditPage = computed(() => {
  const p = route.path || ''
  return p.includes('/profile/edit') || p.includes('/profile-edit')
})

const showBanner = computed(() => {
  if (hideOnEditPage.value) return false
  if (!userStore.isLoggedIn || !userStore.isFellowshipEnabled) return false
  const photos = userStore.userInfo?.photos
  return !Array.isArray(photos) || photos.length === 0
})

onMounted(() => {
  if (userStore.isLoggedIn && userStore.isFellowshipEnabled) {
    userStore.refreshCurrentUser().catch(() => {})
  }
})
</script>

<style scoped>
.photo-banner {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin: 0 auto;
  max-width: 480px;
  padding: 10px 12px;
  background: var(--lc-pink-light, #fce7f3);
  border-bottom: 1px solid var(--lc-pink-border, #fbcfe8);
  box-sizing: border-box;
}

.photo-banner__icon {
  flex-shrink: 0;
  margin-top: 2px;
  font-size: 18px;
  color: var(--lc-pink, #ec4899);
}

.photo-banner__text {
  flex: 1;
  min-width: 0;
}

.photo-banner__title {
  margin: 0 0 4px;
  font-size: 14px;
  font-weight: 700;
  color: var(--lc-text, #0f172a);
}

.photo-banner__desc {
  margin: 0;
  font-size: 12px;
  line-height: 1.45;
  color: var(--lc-muted, #475569);
}

.photo-banner__link {
  flex-shrink: 0;
  align-self: center;
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 600;
  color: #fff;
  background: linear-gradient(135deg, var(--lc-pink, #ec4899), var(--lc-violet, #6d5dfb));
  text-decoration: none;
  white-space: nowrap;
}
</style>
