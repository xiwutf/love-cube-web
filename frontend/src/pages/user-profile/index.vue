<template>
  <div class="user-profile-page">
    <NavBar title="TA的资料" />

    <div v-if="loading" class="page-loading">
      <van-loading type="spinner" color="#FF6B8A" />
    </div>

    <template v-else-if="user">
      <!-- 头部 -->
      <div class="profile-header">
        <van-image round width="80" height="80" :src="user.avatar" fit="cover">
          <template #error>
            <div class="avatar-fallback">{{ (user.nickname || '?')[0] }}</div>
          </template>
        </van-image>
        <div class="header-info">
          <h2 class="nickname">{{ user.nickname }}</h2>
          <div class="tags">
            <van-tag v-if="user.gender"        plain type="primary">{{ user.gender }}</van-tag>
            <van-tag v-if="user.age"           plain type="success">{{ user.age }}岁</van-tag>
            <van-tag v-if="user.constellation" plain type="warning">{{ user.constellation }}</van-tag>
          </div>
          <p class="signature">{{ user.signature || '这个人很神秘～' }}</p>
        </div>
      </div>

      <!-- 基本信息 -->
      <van-cell-group inset title="基本信息" class="info-group">
        <van-cell v-if="user.location"   title="所在地" :value="user.location"   />
        <van-cell v-if="user.occupation" title="职业"   :value="user.occupation" />
        <van-cell v-if="user.height"     title="身高"   :value="user.height + 'cm'" />
      </van-cell-group>

      <!-- 生活照 -->
      <div v-if="user.photos?.length" class="photos-section">
        <p class="section-title">生活照</p>
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

      <!-- 操作区 -->
      <div class="actions">
        <van-button round plain type="primary" icon="like-o" @click="handleLike">
          喜欢
        </van-button>
        <van-button round type="primary" icon="chat-o" @click="handleChat">
          打招呼
        </van-button>
      </div>
    </template>

    <van-empty v-else description="用户不存在" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast, showImagePreview } from 'vant'
import NavBar from '@/components/NavBar.vue'
import { normalizeUser } from '@/utils/normalizeUser.js'
import { toFullUrl } from '@/utils/image.js'
import request from '@/api/request.js'
import { likeUser } from '@/api/match.js'
import { storage } from '@/utils/storage.js'

const route  = useRoute()
const router = useRouter()
const loading = ref(true)
const user    = ref(null)
const liked   = ref(false)

onMounted(async () => {
  try {
    const data = await request.get(`/users/${route.params.id}`)
    user.value = normalizeUser(data)
  } catch {
    showToast({ message: '加载失败', type: 'fail' })
  } finally {
    loading.value = false
  }
})

async function handleLike() {
  if (liked.value) return
  try {
    await likeUser(route.params.id)
    liked.value = true
    showToast({ message: '已喜欢 ❤️', type: 'success' })
  } catch (e) {
    showToast({ message: e.message, type: 'fail' })
  }
}

function handleChat() {
  const myId = storage.get('userId')
  if (!myId) { router.push('/login'); return }
  router.push(`/chat/${route.params.id}`)
}

function previewPhotos(startIndex) {
  showImagePreview({ images: user.value.photos.map(toFullUrl), startPosition: startIndex })
}
</script>

<style scoped>
.user-profile-page { min-height: 100vh; background: #f8f8f8; padding-bottom: 100px; }
.page-loading { display: flex; justify-content: center; padding-top: 120px; }

.profile-header {
  display: flex; gap: 16px; padding: 20px 16px;
  background: #fff; margin-bottom: 8px;
}
.avatar-fallback {
  width: 80px; height: 80px; border-radius: 50%;
  background: linear-gradient(135deg, #FF6B8A, #FFB3C1);
  display: flex; align-items: center; justify-content: center;
  font-size: 28px; color: #fff; font-weight: 700;
}
.header-info { flex: 1; }
.nickname { font-size: 18px; font-weight: 700; color: #333; margin-bottom: 6px; }
.tags { display: flex; gap: 6px; flex-wrap: wrap; margin-bottom: 8px; }
.signature { font-size: 13px; color: #999; line-height: 1.5; }

.info-group { margin-bottom: 8px; }

.photos-section { background: #fff; padding: 14px 16px; margin-bottom: 8px; }
.section-title  { font-size: 15px; font-weight: 600; color: #333; margin-bottom: 10px; }
.photos-grid {
  display: grid; grid-template-columns: repeat(3, 1fr); gap: 6px;
}

.actions {
  position: fixed; bottom: 0; left: 50%; transform: translateX(-50%);
  width: 100%; max-width: 480px;
  display: flex; gap: 12px; padding: 12px 16px;
  background: #fff; box-shadow: 0 -2px 8px rgba(0,0,0,.06);
}
.actions .van-button { flex: 1; }
</style>
