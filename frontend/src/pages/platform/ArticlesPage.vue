<template>
  <component :is="CurrentView" />
</template>

<script setup>
import { computed } from 'vue'
import { useDevice } from '@/composables/useDevice.js'
import Desktop from '@/components/platform/articles/ArticlesPage.desktop.vue'
import Mobile from '@/components/platform/articles/ArticlesPage.mobile.vue'

const { isMobile } = useDevice()
const CurrentView = computed(() => (isMobile.value ? Mobile : Desktop))
</script>
