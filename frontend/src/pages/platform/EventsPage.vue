<template>
  <component :is="CurrentView" />
</template>

<script setup>
import { computed } from 'vue'
import { useDevice } from '@/composables/useDevice.js'
import Desktop from '@/components/platform/events/EventsPage.desktop.vue'
import Mobile from '@/components/platform/events/EventsPage.mobile.vue'

const { isMobile } = useDevice()
const CurrentView = computed(() => (isMobile.value ? Mobile : Desktop))
</script>
