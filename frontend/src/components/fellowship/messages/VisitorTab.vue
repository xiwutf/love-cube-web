<template>
  <van-pull-refresh v-model="refreshingLocal" @refresh="emit('refresh')">
    <div class="tab-content">
      <van-list v-model:loading="loadingLocal" :finished="true" finished-text="">
        <div
          v-for="item in list"
          :key="item.id"
          class="chat-item visitor-item"
          @click="emit('profile', item)"
        >
          <div class="chat-avatar-wrap">
            <van-image round width="50" height="50" :src="getAvatar(item.visitor)" fit="cover">
              <template #error>
                <div class="avatar-fb">{{ (item.visitor?.nickname || '?')[0] }}</div>
              </template>
            </van-image>
          </div>
          <div class="chat-info">
            <div class="chat-row">
              <span class="chat-name">{{ getVisitorName(item) }}</span>
              <span class="chat-time">{{ formatTime(item.visitTime) }}</span>
            </div>
            <p class="chat-last visitor-label">来查看了你的主页</p>
          </div>
        </div>
        <van-empty v-if="!loading && !list.length" description="暂无访客记录" image-size="70" />
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
  formatTime: { type: Function, required: true },
  getAvatar: { type: Function, required: true },
  getVisitorName: { type: Function, required: true }
})

const emit = defineEmits(['refresh', 'profile', 'update:loading', 'update:refreshing'])

const loadingLocal = computed({ get: () => props.loading, set: (value) => emit('update:loading', value) })
const refreshingLocal = computed({ get: () => props.refreshing, set: (value) => emit('update:refreshing', value) })
</script>
