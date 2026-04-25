<template>
  <div class="dynamic-page">
    <div class="top-bar"><span class="top-title">动态</span></div>

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list v-model:loading="loading" :finished="noMore" finished-text="没有更多动态了" @load="load">
        <div v-for="item in list" :key="item.id" class="dynamic-item">
          <div class="dynamic-header" @click="router.push(`/fellowship/user-profile/${item.userId}`)">
            <van-image round width="40" height="40" :src="getAvatar(item)" fit="cover">
              <template #error><div class="avatar-fallback">{{ (item.nickname || '?')[0] }}</div></template>
            </van-image>
            <div class="dynamic-user">
              <p class="dynamic-name">{{ item.nickname || '用户' }}</p>
              <p class="dynamic-time">{{ formatTime(item.createdAt) }}</p>
            </div>
            <van-icon name="ellipsis" size="18" color="#ccc" @click.stop="showMenu(item)" />
          </div>

          <p class="dynamic-content">{{ item.content }}</p>

          <div v-if="item.images?.length" class="dynamic-imgs" :class="`grid-${Math.min(item.images.length, 3)}`">
            <van-image
              v-for="(img, i) in item.images.slice(0, 9)"
              :key="i"
              :src="toFullUrl(img)"
              width="100%"
              height="100%"
              fit="cover"
              radius="6"
              @click="previewImgs(item.images, i)"
            />
          </div>

          <div class="dynamic-footer">
            <div class="like-btn" :class="{ liked: item.isLiked }" @click="toggleLike(item)">
              <van-icon :name="item.isLiked ? 'like' : 'like-o'" />
              <span>{{ item.likeCount ?? 0 }}</span>
            </div>
          </div>
        </div>

        <van-empty v-if="!loading && !list.length" description="还没有动态，去发一条吧" image-size="100" />
      </van-list>
    </van-pull-refresh>

    <AppTabBar />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showImagePreview, showConfirmDialog } from 'vant'
import AppTabBar from '@/components/AppTabBar.vue'
import { getDynamics, likeDynamic, unlikeDynamic, deleteDynamic } from '@/api/dynamic.js'
import { formatTime } from '@/utils/format.js'
import { getAvatar, toFullUrl } from '@/utils/image.js'
import { storage } from '@/utils/storage.js'

const router = useRouter()
const myId = storage.get('userId')
const list = ref([])
const loading = ref(false)
const noMore = ref(false)
const refreshing = ref(false)
let page = 1

async function load() {
  if (loading.value || noMore.value) return
  loading.value = true
  try {
    const data = await getDynamics(page, 10)
    const items = Array.isArray(data) ? data : (data?.list ?? data?.content ?? [])
    list.value.push(...items)
    if (items.length < 10) noMore.value = true
    else page++
  } finally {
    loading.value = false
  }
}

async function onRefresh() {
  page = 1
  list.value = []
  noMore.value = false
  await load()
  refreshing.value = false
}

async function toggleLike(item) {
  try {
    if (item.isLiked) {
      await unlikeDynamic(item.id)
      item.isLiked = false
      item.likeCount = Math.max(0, (item.likeCount ?? 1) - 1)
    } else {
      await likeDynamic(item.id)
      item.isLiked = true
      item.likeCount = (item.likeCount ?? 0) + 1
    }
  } catch (e) {
    showToast({ message: e.message || '操作失败', type: 'fail' })
  }
}

function previewImgs(imgs, startIndex) {
  showImagePreview({ images: imgs.map(toFullUrl), startPosition: startIndex })
}

function showMenu(item) {
  const isMine = String(item.userId) === String(myId)

  if (!isMine) {
    showToast({ message: '已收到举报，将尽快核查', type: 'success' })
    return
  }

  showConfirmDialog({
    title: '删除动态',
    message: '确认删除这条动态吗？'
  }).then(async () => {
    try {
      await deleteDynamic(item.id)
      list.value = list.value.filter((d) => d.id !== item.id)
      showToast({ message: '已删除', type: 'success' })
    } catch (e) {
      showToast({ message: e.message || '删除失败', type: 'fail' })
    }
  }).catch(() => {})
}
</script>

<style scoped>
.dynamic-page { min-height: 100vh; background: #f8f8f8; padding-bottom: 60px; }
.top-bar {
  display: flex; align-items: center; height: 48px; padding: 0 16px;
  background: #fff; position: sticky; top: 0; z-index: 10;
  box-shadow: 0 1px 4px rgba(0,0,0,.06);
}
.top-title { font-size: 18px; font-weight: 700; }

.dynamic-item { background: #fff; margin-bottom: 8px; padding: 14px 16px; }
.dynamic-header { display: flex; align-items: center; gap: 10px; margin-bottom: 10px; cursor: pointer; }
.dynamic-user { flex: 1; }
.dynamic-name { font-size: 14px; font-weight: 600; color: #333; }
.dynamic-time { font-size: 11px; color: #bbb; margin-top: 2px; }
.dynamic-content { font-size: 15px; color: #333; line-height: 1.6; margin-bottom: 10px; }

.dynamic-imgs { display: grid; gap: 4px; margin-bottom: 10px; }
.grid-1 { grid-template-columns: 1fr; max-width: 200px; }
.grid-2 { grid-template-columns: repeat(2, 1fr); aspect-ratio: 2/1; }
.grid-3 { grid-template-columns: repeat(3, 1fr); aspect-ratio: 3/1; }

.dynamic-footer { display: flex; justify-content: flex-end; }
.like-btn {
  display: flex; align-items: center; gap: 4px;
  font-size: 13px; color: #999; cursor: pointer; padding: 4px 8px;
}
.like-btn.liked { color: #ff6b8a; }

.avatar-fallback {
  width: 40px; height: 40px; border-radius: 50%;
  background: linear-gradient(135deg, #ff6b8a, #ffb3c1);
  display: flex; align-items: center; justify-content: center;
  font-size: 14px; color: #fff; font-weight: 700;
}
</style>
