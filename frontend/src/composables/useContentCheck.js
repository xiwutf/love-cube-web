// frontend/src/composables/useContentCheck.js
import { reactive } from 'vue'
import { checkContent } from '@/api/content.js'

export function useContentCheck() {
  const state = reactive({
    show: false,
    suggestion: '',
    hitWords: [],
    riskLevel: '',
    checking: false
  })

  let resolveCallback = null

  /**
   * 检测文本，若低风险直接返回 { ok: true }；
   * 若中/高风险，显示对话框并返回一个 Promise，
   * 用户点击 [使用建议] → resolves { ok: true, suggestion: '...' }
   * 用户点击 [继续发布] → resolves { ok: true, suggestion: null }
   */
  async function check(text, context) {
    if (!text || !text.trim()) return { ok: true }
    state.checking = true
    try {
      const result = await checkContent(text, context)
      if (result.riskLevel === 'low') {
        state.checking = false
        return { ok: true }
      }
      state.suggestion = result.suggestion || ''
      state.hitWords = result.hitWords || []
      state.riskLevel = result.riskLevel
      state.show = true
      return new Promise(resolve => {
        resolveCallback = resolve
      })
    } catch {
      // 检测失败不阻断发布
      state.checking = false
      return { ok: true }
    }
  }

  function applySuggestion() {
    const suggestion = state.suggestion
    _close()
    resolveCallback?.({ ok: true, suggestion })
    resolveCallback = null
  }

  function continueAnyway() {
    _close()
    resolveCallback?.({ ok: true, suggestion: null })
    resolveCallback = null
  }

  function _close() {
    state.show = false
    state.checking = false
    state.suggestion = ''
    state.hitWords = []
    state.riskLevel = ''
  }

  return { state, check, applySuggestion, continueAnyway }
}
