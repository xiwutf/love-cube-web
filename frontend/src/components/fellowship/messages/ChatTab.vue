<template>
  <van-pull-refresh v-model="refreshingLocal" @refresh="emit('refresh')">
    <div class="tab-content">
      <van-list v-model:loading="loadingLocal" :finished="true" finished-text="">
      <template v-for="item in list" :key="item.userId">
        <van-swipe-cell v-if="!item.matchedOnly" class="chat-cell">
          <div class="chat-item" @click="emit('chat', item)">
            <div class="chat-avatar-wrap">
              <van-image round width="50" height="50" :src="item.avatar" fit="cover">
                <template #error>
                  <div class="avatar-fb">{{ (item.nickname || '?')[0] }}</div>
                </template>
              </van-image>
              <span v-if="item.unread" class="unread-dot">{{ item.unread > 99 ? '99+' : item.unread }}</span>
            </div>
            <div class="chat-info">
              <div class="chat-row">
                <span class="chat-name">{{ item.nickname }}</span>
                <span class="chat-time">{{ formatTime(item.lastTime) }}</span>
              </div>
              <p class="chat-last">{{ item.lastMessage || '暂无消息' }}</p>
            </div>
          </div>
          <template #right>
            <van-button square type="danger" text="删除" style="height: 100%" @click="emit('delete-chat', item)" />
          </template>
        </van-swipe-cell>

        <div v-else class="chat-item chat-item--matched" @click="emit('chat', item)">
          <div class="chat-avatar-wrap">
            <van-image round width="50" height="50" :src="item.avatar" fit="cover">
              <template #error>
                <div class="avatar-fb">{{ (item.nickname || '?')[0] }}</div>
              </template>
            </van-image>
            <span class="match-badge">新配对</span>
          </div>
          <div class="chat-info">
            <div class="chat-row">
              <span class="chat-name">{{ item.nickname }}</span>
              <span class="chat-time">{{ formatTime(item.lastTime) }}</span>
            </div>
            <p class="chat-last chat-last--hint">{{ item.lastMessage || '配对成功，打个招呼吧' }}</p>
          </div>
        </div>
      </template>

        <div v-if="!loading && !list.length" class="empty-wrap">
          <van-empty description="暂无聊天消息" image-size="70" />
          <van-button round type="primary" size="small" color="#ff5f84" @click="emit('discover')">
            去看看推荐用户吧
          </van-button>
        </div>
      </van-list>
    </div>
  </van-pull-refresh>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  list: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  refreshing: { type: Boolean, default: false },
  formatTime: { type: Function, required: true }
})

const emit = defineEmits(['refresh', 'chat', 'delete-chat', 'discover', 'update:loading', 'update:refreshing'])

const loadingLocal = computed({
  get: () => props.loading,
  set: (value) => emit('update:loading', value)
})

const refreshingLocal = computed({
  get: () => props.refreshing,
  set: (value) => emit('update:refreshing', value)
})
</script>
