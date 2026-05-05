import { reactive } from 'vue'

export function useContentEditor(options = {}) {
  const getItems = options.getItems || (() => [])
  const onSave = options.onSave || (async () => {})

  const editor = reactive({
    visible: false,
    title: '',
    content: '',
    targetId: null
  })

  function openContentEditor(item) {
    editor.visible = true
    editor.title = item?.title || '正文编辑'
    editor.content = item?.content || ''
    editor.targetId = item?.id ?? null
  }

  function closeContentEditor() {
    editor.visible = false
    editor.title = ''
    editor.content = ''
    editor.targetId = null
  }

  function onEditorVisibleChange(visible) {
    if (!visible) closeContentEditor()
  }

  function findTarget() {
    const items = getItems() || []
    return items.find((entry) => entry.id === editor.targetId)
  }

  function applyContentEditor(nextContent = editor.content) {
    const target = findTarget()
    if (!target) return
    target.content = nextContent
    editor.content = nextContent
    closeContentEditor()
  }

  async function saveFromEditor(nextContent = editor.content) {
    const target = findTarget()
    if (!target) return
    target.content = nextContent
    editor.content = nextContent
    await onSave(target, { fromEditor: true, contentLength: String(nextContent || '').length })
    closeContentEditor()
  }

  function previewText(value, maxLength = 80) {
    const text = String(value || '').trim()
    if (!text) return '暂无正文内容'
    return text.length > maxLength ? `${text.slice(0, maxLength)}...` : text
  }

  return {
    editor,
    openContentEditor,
    closeContentEditor,
    onEditorVisibleChange,
    applyContentEditor,
    saveFromEditor,
    previewText
  }
}
