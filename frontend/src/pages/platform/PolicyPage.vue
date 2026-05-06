<template>
  <section class="platform-page module-page">
    <div class="detail-nav-row">
      <router-link to="/feedback" class="platform-backlink">返回建议反馈</router-link>
      <router-link to="/me" class="platform-link">前往平台个人中心</router-link>
    </div>

    <article v-if="item" class="platform-card detail-article">
      <p class="platform-kicker">Policy</p>
      <h1 class="detail-title">{{ item.title }}</h1>
      <p class="detail-lead">{{ item.intro }}</p>

      <section v-for="section in item.sections" :key="section.heading" class="policy-section">
        <h3 class="platform-heading">{{ section.heading }}</h3>
        <p class="platform-text">{{ section.text }}</p>
      </section>
    </article>

    <article v-else class="platform-card module-empty">
      <h3 class="platform-heading">政策内容不存</h3>
      <p class="platform-text">请从建议反馈页重新进入，或联系平台管理员</p>
    </article>
  </section>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { usePlatformState } from '@/mock/platformState.js'

const route = useRoute()
const { state } = usePlatformState()
const item = computed(() => state.policyPages.find((entry) => entry.id === route.params.id))
</script>

