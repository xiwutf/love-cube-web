<template>
  <van-pull-refresh v-model="refreshingLocal" @refresh="emit('refresh')">
    <div class="tab-content">
      <van-list v-model:loading="loadingLocal" :finished="true" finished-text="">
        <div
          v-for="item in list"
          :key="item.id"
          class="interact-item"
          role="button"
          tabindex="0"
          @click="emit('profile', item)"
        >
          <van-image round width="46" height="46" :src="getAvatar(fromUser(item))" fit="cover">
            <template #error>
              <div class="avatar-fb size46">{{ initial(item) }}</div>
            </template>
          </van-image>
          <div class="interact-info">
            <div class="interact-row">
              <span class="interact-name">{{ name(item) }}</span>
              <span class="interact-time">{{ formatTime(item.createdAt || item.time) }}</span>
            </div>
            <p class="interact-sub">
              <span class="interact-action">{{ label(item.type) }}</span>
              <span v-if="preview(item)" class="interact-preview">{{ preview(item) }}</span>
            </p>
          </div>
          <div class="interact-type-icon">{{ icon(item.type) }}</div>
          <van-icon name="arrow" class="interact-chevron" />
        </div>
        <van-empty v-if="!loading && !list.length" description="暂无互动消息" image-size="70" />
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
  fromUser: { type: Function, required: true },
  initial: { type: Function, required: true },
  name: { type: Function, required: true },
  label: { type: Function, required: true },
  preview: { type: Function, required: true },
  icon: { type: Function, required: true }
})

const emit = defineEmits(['refresh', 'profile', 'update:loading', 'update:refreshing'])

const loadingLocal = computed({ get: () => props.loading, set: (value) => emit('update:loading', value) })
const refreshingLocal = computed({ get: () => props.refreshing, set: (value) => emit('update:refreshing', value) })
</script>
