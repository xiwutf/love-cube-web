<template>
  <van-pull-refresh v-model="refreshingLocal" @refresh="emit('refresh')">
    <div class="tab-content">
      <div class="notif-toolbar" v-if="list.length">
        <span class="notif-count">共 {{ list.length }} 条通知</span>
        <van-button size="mini" plain type="primary" @click="emit('read-all')">全部已读</van-button>
      </div>

      <div v-for="item in list" :key="item.id" class="notif-item" :class="{ unread: !item.isRead }" @click="emit('open', item)">
        <div class="notif-icon">{{ icon(item.type) }}</div>
        <div class="notif-body">
          <p class="notif-title">{{ item.title }}</p>
          <p class="notif-content">{{ item.content }}</p>
          <p class="notif-time">{{ formatTime(item.createdAt) }}</p>
        </div>
        <div v-if="!item.isRead" class="notif-dot" />
      </div>

      <van-empty v-if="!loading && !list.length" description="暂无通知消息" image-size="70" />
    </div>
  </van-pull-refresh>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  list: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  refreshing: { type: Boolean, default: false },
  formatTime: { type: Function, required: true },
  icon: { type: Function, required: true }
})

const emit = defineEmits(['refresh', 'open', 'read-all', 'update:refreshing'])

const refreshingLocal = computed({ get: () => props.refreshing, set: (value) => emit('update:refreshing', value) })
</script>
