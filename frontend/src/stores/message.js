import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useMessageStore = defineStore('message', () => {
  const unreadChat         = ref(0)
  const unreadInteract     = ref(0)
  const unreadVisitor      = ref(0)
  const unreadNotification = ref(0)

  const totalUnread = computed(() =>
    unreadChat.value + unreadInteract.value + unreadVisitor.value + unreadNotification.value
  )

  function setUnread({ chat = 0, interact = 0, visitor = 0, notification = 0 }) {
    unreadChat.value         = chat
    unreadInteract.value     = interact
    unreadVisitor.value      = visitor
    unreadNotification.value = notification
  }

  function setUnreadNotification(n) { unreadNotification.value = n }

  function clearChat()         { unreadChat.value         = 0 }
  function clearInteract()     { unreadInteract.value     = 0 }
  function clearVisitor()      { unreadVisitor.value      = 0 }
  function clearNotification() { unreadNotification.value = 0 }

  return {
    unreadChat, unreadInteract, unreadVisitor, unreadNotification,
    totalUnread,
    setUnread, setUnreadNotification,
    clearChat, clearInteract, clearVisitor, clearNotification,
  }
})
