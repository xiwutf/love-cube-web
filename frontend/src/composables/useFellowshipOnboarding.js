import { computed } from 'vue'
import { storage } from '@/utils/storage.js'

const KEYS = {
  firstSwipe: 'fellowship_onboarding_swiped',
  firstChat: 'fellowship_onboarding_chatted'
}

export function markFellowshipFirstSwipe() {
  storage.set(KEYS.firstSwipe, '1')
}

export function markFellowshipFirstChat() {
  storage.set(KEYS.firstChat, '1')
}

/**
 * @param {{ completionPercent?: number, verified?: boolean }} ctx
 */
export function useFellowshipOnboarding(ctx) {
  const steps = computed(() => {
    const completion = Number(ctx.completionPercent?.value ?? ctx.completionPercent ?? 0)
    const verified = Boolean(ctx.verified?.value ?? ctx.verified)
    const swiped = storage.get(KEYS.firstSwipe) === '1'
    const chatted = storage.get(KEYS.firstChat) === '1'

    return [
      {
        key: 'profile',
        label: '完善资料',
        done: completion >= 80,
        to: '/profile/edit'
      },
      {
        key: 'verify',
        label: '真人认证',
        done: verified,
        to: '/verify'
      },
      {
        key: 'swipe',
        label: '首次滑卡',
        done: swiped,
        to: '/match'
      },
      {
        key: 'chat',
        label: '首次聊天',
        done: chatted,
        to: '/messages'
      }
    ]
  })

  const allDone = computed(() => steps.value.every((s) => s.done))
  const currentStep = computed(() => steps.value.find((s) => !s.done) || null)
  const progressPercent = computed(() =>
    Math.round((steps.value.filter((s) => s.done).length / steps.value.length) * 100)
  )

  return { steps, allDone, currentStep, progressPercent }
}
