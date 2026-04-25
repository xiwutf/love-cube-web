<template>
  <section class="platform-page">
    <router-link to="/events" class="platform-backlink">← 返回活动列表</router-link>

    <article v-if="item && item.status === 'published'" class="platform-card platform-block">
      <p class="platform-meta">{{ item.time }} · {{ item.location }}</p>
      <h1 class="platform-title">{{ item.title }}</h1>
      <p class="platform-subtitle">{{ item.summary }}</p>
      <p class="platform-text">{{ item.content }}</p>
      <p class="platform-text">当前报名人数（占位）：{{ item.signupCount || 0 }}</p>
      <div class="platform-actions">
        <router-link to="/fellowship-intro" class="platform-btn platform-btn-primary">查看联谊模块说明</router-link>
      </div>
    </article>

    <div v-else class="platform-card platform-empty">
      <h2 class="platform-heading">未找到活动</h2>
      <p class="platform-text">该活动可能已结束，或链接无效。</p>
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { usePlatformState } from '@/mock/platformState.js'

const route = useRoute()
const { state } = usePlatformState()
const item = computed(() => state.events.find((entry) => entry.id === route.params.id))
</script>
