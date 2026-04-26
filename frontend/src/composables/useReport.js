import { showToast } from 'vant'
import { showActionSheet } from '@/utils/vantActionSheet.js'
import { submitReport } from '@/api/fellowship.js'

const REASONS = [
  { name: '骚扰',     value: '骚扰' },
  { name: '虚假资料', value: '虚假资料' },
  { name: '广告推销', value: '广告推销' },
  { name: '诈骗',     value: '诈骗' },
  { name: '色情内容', value: '色情内容' },
  { name: '其他',     value: '其他' },
]

export function useReport() {
  async function openReport({ targetType = 'USER', targetId = null, targetUserId = null } = {}) {
    // Step 1: pick reason
    let reasonType
    try {
      const r = await showActionSheet({ title: '举报原因', actions: REASONS })
      reasonType = r.value
    } catch {
      return false // cancelled
    }

    // Step 2: choose report-only vs report+block
    const finalActions = [
      { name: '举报 + 拉黑该用户', color: '#ee0a24', alsoBlock: true },
      { name: '仅举报',            color: '#333',     alsoBlock: false },
    ]
    let alsoBlock = false
    try {
      const a = await showActionSheet({
        title: `原因：${reasonType}，确认提交？`,
        actions: finalActions,
      })
      alsoBlock = !!a.alsoBlock
    } catch {
      return false // cancelled
    }

    // Step 3: submit
    try {
      await submitReport({ targetType, targetId, targetUserId, reasonType, alsoBlock })
      showToast({ message: '举报已提交，感谢反馈', type: 'success' })
      return true
    } catch (e) {
      showToast({ message: e.message || '提交失败，请重试', type: 'fail' })
      return false
    }
  }

  return { openReport }
}
