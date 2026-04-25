<template>
  <section class="admin-page">
    <section class="platform-card">
      <p class="platform-kicker">Admin Overview</p>
      <h1 class="platform-title">运营后台总览</h1>
      <p class="platform-subtitle">当前为 mock 运营后台，可完成内容管理、用户管理、认证审核和举报处理流程。</p>
    </section>

    <section class="platform-grid-3 platform-block">
      <article class="platform-card">
        <h3 class="platform-heading">公告</h3>
        <p class="platform-text">总数 {{ state.announcements.length }} 条</p>
      </article>
      <article class="platform-card">
        <h3 class="platform-heading">认证待审</h3>
        <p class="platform-text">{{ pendingVerifications }} 条</p>
      </article>
      <article class="platform-card">
        <h3 class="platform-heading">待处理举报</h3>
        <p class="platform-text">{{ pendingReports }} 条</p>
      </article>
    </section>
  </section>
</template>

<script setup>
import { computed } from 'vue'
import { usePlatformState } from '@/mock/platformState.js'

const { state } = usePlatformState()
const pendingVerifications = computed(() => state.verifications.filter((item) => item.status === 'pending').length)
const pendingReports = computed(() => state.reports.filter((item) => item.status !== 'resolved').length)
</script>
