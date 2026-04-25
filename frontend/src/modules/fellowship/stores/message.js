import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useMessageStore = defineStore('fellowship-message', () => {
  const unreadChat     = ref(0)
  const unreadInteract = ref(0)
  const unreadVisitor  = ref(0)

  const totalUnread = computed(() => unreadChat.value + unreadInteract.value + unreadVisitor.value)

  function setUnread({ chat = 0, interact = 0, visitor = 0 }) {
    unreadChat.value     = chat
    unreadInteract.value = interact
    unreadVisitor.value  = visitor
  }

  function clearChat()     { unreadChat.value     = 0 }
  function clearInteract() { unreadInteract.value  = 0 }
  function clearVisitor()  { unreadVisitor.value   = 0 }

  return { unreadChat, unreadInteract, unreadVisitor, totalUnread, setUnread, clearChat, clearInteract, clearVisitor }
})
