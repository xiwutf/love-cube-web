<template>
  <div class="user-profile-page">
    <NavBar title="TA 的资料">
      <template #right>
        <van-icon name="ellipsis" size="22" color="#666" @click="showMoreMenu" />
      </template>
    </NavBar>

    <div v-if="loading" class="page-loading">
      <van-loading type="spinner" color="#FF6B8A" />
    </div>

    <template v-else-if="user">
      <div class="profile-header">
        <van-image round width="80" height="80" :src="profileAvatarDisplay" fit="cover">
          <template #error>
            <div class="avatar-fallback">{{ (user.nickname || '?')[0] }}</div>
          </template>
        </van-image>
        <div class="header-info">
          <h2 class="nickname">{{ user.nickname }}</h2>
          <div class="verify-badges" v-if="verifyBadges.photoVerified || verifyBadges.realnameVerified">
            <span v-if="verifyBadges.photoVerified" class="vbadge photo">📷 真人</span>
            <span v-if="verifyBadges.realnameVerified" class="vbadge realname">🪪 实名</span>
          </div>
          <div class="tags">
            <van-tag v-if="user.gender" plain type="primary">{{ user.gender }}</van-tag>
            <van-tag v-if="user.age" plain type="success">{{ user.age }}岁</van-tag>
            <van-tag v-if="user.constellation" plain type="warning">{{ user.constellation }}</van-tag>
          </div>
          <p class="signature">{{ user.signature || '这个人很神秘' }}</p>
        </div>
      </div>

      <van-cell-group inset title="基本信息" class="info-group">
        <van-cell v-if="user.location" title="所在地" :value="user.location" />
        <van-cell v-if="user.occupation" title="职业" :value="user.occupation" />
        <van-cell v-if="user.height" title="身高" :value="`${user.height}cm`" />
      </van-cell-group>

      <div v-if="user.photos?.length" class="photos-section">
        <p class="section-title">生活</p>
        <div class="photos-grid">
          <van-image
            v-for="(src, i) in user.photos"
            :key="i"
            :src="toFullUrl(src)"
            width="100%"
            height="110"
            fit="cover"
            radius="8"
            @click="previewPhotos(i)"
          />
        </div>
      </div>

      <div v-if="blockStatus.blockedMe" class="blocked-notice">
        <van-icon name="warning-o" />
        <span>对方已将你拉黑，无法互动</span>
      </div>

      <div class="actions">
        <van-button
          round
          :plain="!liked"
          type="primary"
          icon="like-o"
          :disabled="liked || blockStatus.blockedMe || blockStatus.blockedByMe"
          @click="handleLike"
        >
          {{ liked ? '已喜欢' : '喜欢' }}
        </van-button>
        <van-button
          round
          type="primary"
          icon="chat-o"
          :disabled="blockStatus.blockedMe || blockStatus.blockedByMe"
          @click="handleChat"
        >
          打招呼
        </van-button>
      </div>
    </template>

    <van-empty v-else description="用户不存在" />
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showConfirmDialog, showImagePreview, showToast } from 'vant'
import { showActionSheet } from '@/utils/vantActionSheet.js'
import NavBar from '@/components/NavBar.vue'
import { likeUser, getLikeStatus } from '@/api/match.js'
import { fetchFellowshipUserDetail, blockUser, unblockUser, getBlockStatus } from '@/api/fellowship.js'
import { getVerificationStatus } from '@/api/verification.js'
import { toFullUrl, getAvatar } from '@/utils/image.js'
import { normalizeUser } from '@/utils/normalizeUser.js'
import { storage } from '@/utils/storage.js'
import { useReport } from '@/composables/useReport.js'
import { useFellowshipNavBase } from '@/composables/useFellowshipNavBase.js'

const route = useRoute()
const router = useRouter()
const { fellowshipPath } = useFellowshipNavBase()
const loading = ref(true)
const user = ref(null)
const liked = ref(false)
const blocking = ref(false)
const blockStatus = ref({ blockedByMe: false, blockedMe: false, canMessage: true })
const verifyBadges = ref({ photoVerified: false, realnameVerified: false })
const { openReport } = useReport()

const profileAvatarDisplay = computed(() => getAvatar(user.value))

onMounted(async () => {
  try {
    const [detail, status, bStatus, vStatus] = await Promise.allSettled([
      fetchFellowshipUserDetail(route.params.id),
      getLikeStatus(route.params.id),
      getBlockStatus(route.params.id),
      getVerificationStatus(route.params.id),
    ])
    if (detail.status === 'fulfilled') user.value = normalizeUser(detail.value)
    if (status.status === 'fulfilled') liked.value = !!status.value?.isLiked
    if (bStatus.status === 'fulfilled') blockStatus.value = bStatus.value
    if (vStatus.status === 'fulfilled') verifyBadges.value = vStatus.value || {}
  } catch {
    showToast({ message: '加载失败', type: 'fail' })
  } finally {
    loading.value = false
  }
})

async function handleLike() {
  if (liked.value) return
  try {
    const res = await likeUser(route.params.id)
    liked.value = Boolean(res?.isLiked ?? true)
    if (res?.matched) {
      showToast({ message: '配对成功，去打个招呼吧 🎉', type: 'success' })
      router.push(fellowshipPath(`/chat/${route.params.id}`))
      return
    }
    showToast({ message: liked.value ? '已喜欢 ❤️' : '已取消喜欢', type: 'success' })
  } catch (e) {
    showToast({ message: e.message || '操作失败', type: 'fail' })
  }
}

function handleChat() {
  const myId = storage.get('userId')
  if (!myId) {
    router.push({ path: fellowshipPath('/login'), query: { redirect: route.fullPath } })
    return
  }
  router.push(fellowshipPath(`/chat/${route.params.id}`))
}

async function handleBlock() {
  if (blockStatus.value.blockedByMe) {
    await showConfirmDialog({ title: '解除拉黑', message: '确认解除对该用户的拉黑？' }).catch(() => null)
    blocking.value = true
    try {
      await unblockUser(route.params.id)
      blockStatus.value = { ...blockStatus.value, blockedByMe: false, canMessage: !blockStatus.value.blockedMe }
      showToast({ message: '已解除拉黑', type: 'success' })
    } catch {
      showToast({ message: '操作失败', type: 'fail' })
    } finally {
      blocking.value = false
    }
  } else {
    await showConfirmDialog({ title: '拉黑用户', message: '拉黑后双方将无法互发消息，确认操作？' }).catch(() => null)
    blocking.value = true
    try {
      await blockUser(route.params.id)
      blockStatus.value = { ...blockStatus.value, blockedByMe: true, canMessage: false }
      showToast({ message: '已拉黑', type: 'success' })
    } catch {
      showToast({ message: '操作失败', type: 'fail' })
    } finally {
      blocking.value = false
    }
  }
}

function previewPhotos(startIndex) {
  showImagePreview({ images: user.value.photos.map(toFullUrl), startPosition: startIndex })
}

async function showMoreMenu() {
  const blockLabel = blockStatus.value.blockedByMe ? '解除拉黑' : '拉黑'
  try {
    const action = await showActionSheet({
      actions: [
        { name: '举报该用户', color: '#ee0a24' },
        { name: blockLabel, color: blockStatus.value.blockedByMe ? '#333' : '#ee0a24' },
      ],
    })
    if (action.name === '举报该用户') {
      await openReport({ targetType: 'USER', targetUserId: route.params.id })
    } else {
      await handleBlock()
    }
  } catch {
    // dismissed
  }
}
</script>

<style scoped>
.user-profile-page { min-height: 100vh; background: #f8f8f8; padding-bottom: 100px; }
.page-loading { display: flex; justify-content: center; padding-top: 120px; }
.profile-header { display: flex; gap: 16px; padding: 20px 16px; background: #fff; margin-bottom: 8px; }
.avatar-fallback {
  width: 80px; height: 80px; border-radius: 50%;
  background: linear-gradient(135deg, #ff6b8a, #ffb3c1);
  display: flex; align-items: center; justify-content: center;
  font-size: 28px; color: #fff; font-weight: 700;
}
.header-info { flex: 1; }
.nickname { font-size: 18px; font-weight: 700; color: #333; margin-bottom: 4px; }
.verify-badges { display: flex; gap: 6px; margin-bottom: 6px; flex-wrap: wrap; }
.vbadge {
  font-size: 11px; font-weight: 600; padding: 2px 7px;
  border-radius: 20px; white-space: nowrap;
}
.vbadge.photo    { background: #e8f4ff; color: #1989fa; }
.vbadge.realname { background: #e8f8f0; color: #07c160; }
.tags { display: flex; gap: 6px; flex-wrap: wrap; margin-bottom: 8px; }
.signature { font-size: 13px; color: #999; line-height: 1.5; }
.info-group { margin-bottom: 8px; }
.photos-section { background: #fff; padding: 14px 16px; margin-bottom: 8px; }
.section-title { font-size: 15px; font-weight: 600; color: #333; margin-bottom: 10px; }
.photos-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 6px; }

.blocked-notice {
  display: flex; align-items: center; gap: 6px;
  margin: 8px 16px; padding: 10px 14px;
  background: #fff8f0; border-radius: 10px;
  color: #f77; font-size: 13px;
}

.actions {
  position: fixed; bottom: 0; left: 50%; transform: translateX(-50%);
  width: 100%; max-width: 480px;
  display: flex; gap: 8px; padding: 12px 16px;
  background: #fff; box-shadow: 0 -2px 8px rgba(0,0,0,.06);
}
.actions .van-button { flex: 1; }
</style>


