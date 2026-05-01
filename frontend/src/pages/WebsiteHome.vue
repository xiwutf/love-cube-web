<template>
  <component :is="CurrentView" />
</template>

<script setup>
import { computed } from 'vue'
import { useDevice } from '@/composables/useDevice.js'
import DesktopHome from '@/pages/pc/platform/HomePage.vue'
import MobileHome from '@/components/platform/home/PlatformHome.mobile.vue'

const { isMobile } = useDevice()
const CurrentView = computed(() => (isMobile.value ? MobileHome : DesktopHome))
</script>
