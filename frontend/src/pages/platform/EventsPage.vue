<template>
  <section class="platform-page">
    <section class="platform-card">
      <p class="platform-kicker">Events</p>
      <h1 class="platform-title">平台活动</h1>
      <p class="platform-subtitle">活动页提供报名信息、时间地点与注意事项。</p>
    </section>

    <div class="platform-list">
      <router-link
        v-for="item in list"
        :key="item.id"
        :to="`/events/${item.id}`"
        class="platform-card platform-item-link"
      >
        <p class="platform-meta">{{ item.time }} · {{ item.location }}</p>
        <h3 class="platform-heading">{{ item.title }}</h3>
        <p class="platform-text">{{ item.summary }}（报名 {{ item.signupCount || 0 }} 人）</p>
      </router-link>
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue'
import { usePlatformState } from '@/mock/platformState.js'

const { state } = usePlatformState()
const list = computed(() => state.events.filter((item) => item.status === 'published'))
</script>
