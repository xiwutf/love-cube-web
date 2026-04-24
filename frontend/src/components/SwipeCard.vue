<template>
  <div
    ref="cardEl"
    class="swipe-card"
    :style="cardStyle"
    @touchstart.prevent="onTouchStart"
    @touchmove.prevent="onTouchMove"
    @touchend="onTouchEnd"
  >
    <!-- 图片 -->
    <van-image :src="user.avatar" width="100%" height="100%" fit="cover" class="card-img">
      <template #error><div class="card-placeholder">{{ (user.nickname||'?')[0] }}</div></template>
    </van-image>

    <!-- LIKE / NOPE 标签 -->
    <div class="stamp like-stamp" :style="{ opacity: likeOpacity }">LIKE 💚</div>
    <div class="stamp nope-stamp" :style="{ opacity: nopeOpacity }">NOPE 💔</div>
    <div class="stamp super-stamp" :style="{ opacity: superOpacity }">SUPER ⭐</div>

    <!-- 用户信息 -->
    <div class="card-info">
      <h3 class="card-name">{{ user.nickname }} <span class="card-age">{{ user.age }}</span></h3>
      <p class="card-meta">{{ user.location }} · {{ user.occupation }}</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  user: { type: Object, required: true }
})

const emit = defineEmits(['like', 'dislike', 'superlike'])

const THRESHOLD  = 80   // px - 触发 like/dislike 的阈值
const THRESHOLD_Y = -80  // px - 触发 superlike 的阈值

let startX = 0, startY = 0
const deltaX = ref(0)
const deltaY = ref(0)
const isDragging = ref(false)
const isFlying   = ref(false)

const cardStyle = computed(() => {
  if (isFlying.value) return {}  // 飞出时由 flyOut 动画控制
  const rotate = deltaX.value / 12
  return {
    transform: `translateX(${deltaX.value}px) translateY(${deltaY.value}px) rotate(${rotate}deg)`,
    transition: isDragging.value ? 'none' : 'transform 0.3s ease',
    cursor: 'grab',
  }
})

const likeOpacity  = computed(() => Math.max(0, Math.min(1,  deltaX.value / THRESHOLD)))
const nopeOpacity  = computed(() => Math.max(0, Math.min(1, -deltaX.value / THRESHOLD)))
const superOpacity = computed(() => Math.max(0, Math.min(1, -deltaY.value / Math.abs(THRESHOLD_Y))))

function onTouchStart(e) {
  if (isFlying.value) return
  const t = e.touches[0]
  startX = t.clientX
  startY = t.clientY
  isDragging.value = true
}

function onTouchMove(e) {
  if (!isDragging.value) return
  const t  = e.touches[0]
  deltaX.value = t.clientX - startX
  deltaY.value = t.clientY - startY
}

function onTouchEnd() {
  isDragging.value = false
  if (deltaY.value < THRESHOLD_Y && Math.abs(deltaX.value) < THRESHOLD) {
    flyOut(0, -window.innerHeight, 'superlike')
  } else if (deltaX.value > THRESHOLD) {
    flyOut(window.innerWidth * 1.5, deltaX.value / 5, 'like')
  } else if (deltaX.value < -THRESHOLD) {
    flyOut(-window.innerWidth * 1.5, deltaX.value / 5, 'dislike')
  } else {
    // 回弹
    deltaX.value = 0
    deltaY.value = 0
  }
}

function flyOut(tx, ty, action) {
  isFlying.value = true
  const rotate = action === 'like' ? 30 : action === 'dislike' ? -30 : 0
  // 用 CSS transition 飞出
  const el = document.querySelector('.swipe-card')
  if (el) {
    el.style.transition = 'transform 0.4s ease, opacity 0.4s ease'
    el.style.transform  = `translateX(${tx}px) translateY(${ty}px) rotate(${rotate}deg)`
    el.style.opacity    = '0'
  }
  setTimeout(() => emit(action), 400)
}

// 允许父组件调用（按钮触发）
defineExpose({
  triggerLike:      () => flyOut(window.innerWidth * 1.5, 50, 'like'),
  triggerDislike:   () => flyOut(-window.innerWidth * 1.5, 50, 'dislike'),
  triggerSuperlike: () => flyOut(0, -window.innerHeight, 'superlike'),
})
</script>

<style scoped>
.swipe-card {
  position: absolute; inset: 0;
  border-radius: 16px; overflow: hidden;
  box-shadow: 0 8px 30px rgba(0,0,0,.15);
  user-select: none; touch-action: none;
}
.card-img { display: block; width: 100%; height: 100%; }
.card-placeholder {
  width: 100%; height: 100%;
  background: linear-gradient(135deg, #FFE8EE, #FFB3C1);
  display: flex; align-items: center; justify-content: center;
  font-size: 80px; color: #fff; font-weight: 700;
}

.stamp {
  position: absolute; top: 30px; padding: 8px 20px;
  border: 4px solid; border-radius: 8px;
  font-size: 28px; font-weight: 900; transform: rotate(-15deg);
  pointer-events: none;
}
.like-stamp  { left: 20px;  border-color: #4CAF50; color: #4CAF50; transform: rotate(-15deg); }
.nope-stamp  { right: 20px; border-color: #ee0a24; color: #ee0a24; transform: rotate(15deg); }
.super-stamp {
  left: 50%; transform: translateX(-50%) rotate(0deg);
  border-color: #FF6B8A; color: #FF6B8A; top: 60px;
}

.card-info {
  position: absolute; bottom: 0; left: 0; right: 0;
  padding: 40px 20px 20px;
  background: linear-gradient(transparent, rgba(0,0,0,.6));
  color: #fff;
}
.card-name { font-size: 22px; font-weight: 700; margin-bottom: 4px; }
.card-age  { font-size: 18px; font-weight: 400; margin-left: 8px; }
.card-meta { font-size: 13px; opacity: .85; }
</style>
