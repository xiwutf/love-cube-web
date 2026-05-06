<template>
  <div class="dynamic-page">
    <div class="top-bar"><span class="top-title">动态</span></div>

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list v-model:loading="loading" :finished="noMore" finished-text="没有更多动态了" @load="load">
        <div v-for="item in list" :key="item.id" class="dynamic-item">
          <div class="dynamic-header" @click="goUserProfile(item)">
            <van-image round width="40" height="40" :src="getAvatar(item)" fit="cover">
              <template #error><div class="avatar-fallback">{{ getDisplayName(item).slice(0, 1) }}</div></template>
            </van-image>
            <div class="dynamic-user">
              <p class="dynamic-name">{{ getDisplayName(item) }}</p>
              <p class="dynamic-time">{{ formatTime(item.createdAt) }}</p>
            </div>
            <van-icon name="ellipsis" size="18" color="#ccc" @click.stop="showMenu(item)" />
          </div>

          <p class="dynamic-content">{{ item.content }}</p>

          <div
            v-if="getImages(item).length"
            class="dynamic-imgs"
            :class="`grid-${Math.min(getImages(item).length, 3)}`"
          >
            <van-image
              v-for="(img, i) in getImages(item).slice(0, 9)"
              :key="`${item.id}-${img}-${i}`"
              :src="getImageSrc(item, img, i)"
              width="100%"
              height="100%"
              fit="cover"
              radius="6"
              @error="onImageError(item, img, i)"
              @load="onImageLoad(item, img, i)"
              @click="previewImgs(getImages(item), i)"
            >
              <template #error>
                <div class="img-fallback" @click.stop="retryImage(item, img, i)">
                  {{ isImageFailed(item, img, i) ? '图片加载失败，点此重试' : '图片加载失败' }}
                </div>
              </template>
            </van-image>
          </div>

          <div class="dynamic-footer">
            <div class="like-btn" :class="{ liked: item.isLiked }" @click="toggleLike(item)">
              <van-icon :name="item.isLiked ? 'like' : 'like-o'" />
              <span>{{ item.likeCount || 0 }}</span>
            </div>
            <div class="comment-btn" @click="onComment(item)">
              <van-icon name="chat-o" />
              <span>{{ item.commentCount || 0 }}</span>
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
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showImagePreview, showConfirmDialog } from 'vant'
import { showActionSheet } from '@/utils/vantActionSheet.js'
import AppTabBar from '@/components/AppTabBar.vue'
import { getDynamics, likeDynamic, unlikeDynamic, deleteDynamic } from '@/api/dynamic.js'
import { formatTime } from '@/utils/format.js'
import { getAvatar, toFullUrl } from '@/utils/image.js'
import { storage } from '@/utils/storage.js'
import { useReport } from '@/composables/useReport.js'

const router = useRouter()
const myId = storage.get('userId')
const { openReport } = useReport()
const list = ref([])
const loading = ref(false)
const noMore = ref(false)
const refreshing = ref(false)
const likePending = ref(new Set())
const imageRetrySeed = reactive({})
const imageFailed = reactive({})
let page = 1
const PAGE_SIZE = 10

async function load() {
  if (noMore.value) return
  loading.value = true
  try {
    const data = await getDynamics(page, PAGE_SIZE)
    const items = Array.isArray(data) ? data : (data?.list || data?.items || data?.records || data?.content || [])
    list.value.push(...items)
    if (items.length < PAGE_SIZE || data?.hasNext === false) noMore.value = true
    else page++
  } catch (e) {
    noMore.value = true
    showToast({ message: e?.message || '动态加载失败', type: 'fail' })
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
  const id = item?.id
  if (!id || likePending.value.has(id)) return
  likePending.value.add(id)
  try {
    if (item.isLiked) {
      await unlikeDynamic(item.id)
      item.isLiked = false
      item.likeCount = Math.max(0, (item.likeCount || 0) - 1)
    } else {
      await likeDynamic(item.id)
      item.isLiked = true
      item.likeCount = (item.likeCount || 0) + 1
    }
  } catch (e) {
    showToast({ message: e.message || '操作失败', type: 'fail' })
  } finally {
    likePending.value.delete(id)
  }
}

function previewImgs(imgs, startIndex) {
  showImagePreview({ images: imgs.map(toFullUrl), startPosition: startIndex })
}

function getDisplayName(item) {
  return item?.nickname || item?.username || item?.name || '用户'
}

function getImages(item) {
  if (!item) return []
  if (Array.isArray(item.images)) return item.images.filter(Boolean)
  if (Array.isArray(item.imageUrls)) return item.imageUrls.filter(Boolean)
  if (Array.isArray(item.image_urls)) return item.image_urls.filter(Boolean)
  if (typeof item.imageUrls === 'string') {
    try {
      const parsed = JSON.parse(item.imageUrls)
      return Array.isArray(parsed) ? parsed.filter(Boolean) : []
    } catch {
      return item.imageUrls.split(',').map((s) => s.trim()).filter(Boolean)
    }
  }
  if (typeof item.image_urls === 'string') {
    try {
      const parsed = JSON.parse(item.image_urls)
      return Array.isArray(parsed) ? parsed.filter(Boolean) : []
    } catch {
      return item.image_urls.split(',').map((s) => s.trim()).filter(Boolean)
    }
  }
  return []
}

function goUserProfile(item) {
  const uid = item?.userId || item?.userid
  if (!uid) return
  router.push(`/fellowship/user-profile/${uid}`)
}

function onComment(item) {
  const count = Number(item?.commentCount || 0)
  showToast({
    message: count > 0 ? '联谊动态暂不支持评论详情查看' : '联谊动态暂不支持发表评论',
    type: 'text'
  })
}

function imageKey(item, img, index) {
  return `${item?.id || 'x'}-${index}-${String(img || '')}`
}

function getImageSrc(item, img, index) {
  const key = imageKey(item, img, index)
  const full = toFullUrl(img)
  const seed = Number(imageRetrySeed[key] || 0)
  if (!seed) return full
  return `${full}${full.includes('?') ? '&' : '?'}_retry=${seed}`
}

function onImageError(item, img, index) {
  imageFailed[imageKey(item, img, index)] = true
}

function onImageLoad(item, img, index) {
  imageFailed[imageKey(item, img, index)] = false
}

function isImageFailed(item, img, index) {
  return Boolean(imageFailed[imageKey(item, img, index)])
}

function retryImage(item, img, index) {
  const key = imageKey(item, img, index)
  imageRetrySeed[key] = Number(imageRetrySeed[key] || 0) + 1
  imageFailed[key] = false
}

async function showMenu(item) {
  const isMine = String(item.userId) === String(myId)

  if (isMine) {
    try {
      await showConfirmDialog({ title: '删除动态', message: '确认删除这条动态吗？' })
      await deleteDynamic(item.id)
      list.value = list.value.filter((d) => d.id !== item.id)
      showToast({ message: '已删除', type: 'success' })
    } catch (e) {
      if (e?.message) showToast({ message: e.message, type: 'fail' })
    }
    return
  }

  try {
    const action = await showActionSheet({
      actions: [{ name: '举报该动态', color: '#ee0a24' }],
    })
    if (action.name === '举报该动态') {
      await openReport({
        targetType: 'DYNAMIC',
        targetId: String(item.id),
        targetUserId: item.userId,
      })
    }
  } catch {
    // dismissed
  }
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

.dynamic-footer { display: flex; justify-content: flex-end; gap: 14px; }
.like-btn {
  display: flex; align-items: center; gap: 4px;
  font-size: 13px; color: #999; cursor: pointer; padding: 4px 8px;
}
.like-btn.liked { color: #ff6b8a; }
.comment-btn {
  display: flex; align-items: center; gap: 4px;
  font-size: 13px; color: #999; padding: 4px 8px; cursor: pointer;
}

.avatar-fallback {
  width: 40px; height: 40px; border-radius: 50%;
  background: linear-gradient(135deg, #ff6b8a, #ffb3c1);
  display: flex; align-items: center; justify-content: center;
  font-size: 14px; color: #fff; font-weight: 700;
}
.img-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f3f4f6;
  color: #9ca3af;
  font-size: 12px;
}
</style>

