<template>
  <section class="platform-page">
    <router-link to="/about" class="platform-backlink">← 返回关于我们</router-link>

    <article v-if="item" class="platform-card platform-block">
      <p class="platform-kicker">Policy</p>
      <h1 class="platform-title">{{ item.title }}</h1>
      <p class="platform-subtitle">{{ item.intro }}</p>
      <section v-for="section in item.sections" :key="section.heading" class="platform-block">
        <h3 class="platform-heading">{{ section.heading }}</h3>
        <p class="platform-text">{{ section.text }}</p>
      </section>
    </article>

    <div v-else class="platform-card platform-empty">
      <h2 class="platform-heading">未找到页面</h2>
      <p class="platform-text">请从 Footer 重新进入协议说明页面。</p>
    </div>
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
