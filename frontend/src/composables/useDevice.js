import { computed, onBeforeUnmount, onMounted, ref } from 'vue'

const MOBILE_WIDTH = 768

export function useDevice() {
  const width = ref(typeof window !== 'undefined' ? window.innerWidth : 1280)

  const onResize = () => {
    width.value = window.innerWidth
  }

  onMounted(() => {
    onResize()
    window.addEventListener('resize', onResize)
  })

  onBeforeUnmount(() => {
    window.removeEventListener('resize', onResize)
  })

  const isMobile = computed(() => width.value < MOBILE_WIDTH)

  return { width, isMobile }
}
