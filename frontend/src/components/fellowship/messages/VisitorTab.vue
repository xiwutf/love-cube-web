<template>
  <van-pull-refresh v-model="refreshingLocal" @refresh="emit('refresh')">
    <div class="tab-content">
      <div v-if="locked && showVipCommerce" class="vip-lock-banner" @click="router.push(fellowshipPath('/vip'))">
        <p>访客信息已隐藏</p>
        <span>开通 VIP 查看谁看过你</span>
      </div>

      <FellowshipInterestFilterBar
        v-if="!locked && list.length"
        v-model="activeFilter"
        :options="INTEREST_FILTER_OPTIONS"
      />

      <p v-if="filterEmptyHint" class="filter-empty-hint">{{ filterEmptyHint }}</p>

      <van-list v-model:loading="loadingLocal" :finished="true" finished-text="">
        <div
          v-for="item in displayList"
          :key="item.id"
          class="chat-item visitor-item"
          :class="{ locked: item.locked || locked }"
          @click="onProfile(item)"
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
            <p class="chat-last visitor-label">
              来查看了你的主页
              <van-tag v-if="item.photoVerified || item.realnameVerified" plain type="primary" size="mini">已认证</van-tag>
            </p>
          </div>
        </div>
        <van-empty v-if="!loading && !displayList.length" description="暂无访客记录" image-size="70" />
      </van-list>
    </div>
  </van-pull-refresh>
</template>

<script setup>
import { computed, toRef } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import FellowshipInterestFilterBar from '@/components/fellowship/FellowshipInterestFilterBar.vue'
import { useFellowshipNavBase } from '@/composables/useFellowshipNavBase.js'
import { useFellowshipInterestFilter, INTEREST_FILTER_OPTIONS } from '@/composables/useFellowshipInterestFilter.js'
import { FELLOWSHIP_VIP_COMMERCE_ENABLED } from '@/constants/fellowshipCommerce.js'

const showVipCommerce = FELLOWSHIP_VIP_COMMERCE_ENABLED

const props = defineProps({
  list: { type: Array, default: () => [] },
  locked: { type: Boolean, default: false },
  loading: { type: Boolean, default: false },
  refreshing: { type: Boolean, default: false },
  formatTime: { type: Function, required: true },
  getAvatar: { type: Function, required: true },
  getVisitorName: { type: Function, required: true }
})

const emit = defineEmits(['refresh', 'profile', 'update:loading', 'update:refreshing'])
const router = useRouter()
const { fellowshipPath } = useFellowshipNavBase()

const listRef = toRef(props, 'list')
const { activeFilter, filteredList, filterEmptyHint } = useFellowshipInterestFilter(listRef)

const displayList = computed(() => (props.locked ? props.list : filteredList.value))

const loadingLocal = computed({ get: () => props.loading, set: (value) => emit('update:loading', value) })
const refreshingLocal = computed({ get: () => props.refreshing, set: (value) => emit('update:refreshing', value) })

function onProfile(item) {
  if (props.locked || item.locked) {
    if (showVipCommerce) {
      showToast('开通 VIP 后可查看访客资料')
      router.push(fellowshipPath('/vip'))
    } else {
      showToast('暂无法查看访客资料')
    }
    return
  }
  emit('profile', item)
}
</script>

<style scoped>
.vip-lock-banner {
  margin: 12px 12px 0;
  padding: 12px 14px;
  border-radius: 12px;
  background: linear-gradient(135deg, #fff7ed, #ffedd5);
  border: 1px solid #fdba74;
}

.vip-lock-banner p {
  margin: 0;
  font-size: 14px;
  font-weight: 700;
  color: #9a3412;
}

.vip-lock-banner span {
  display: block;
  margin-top: 4px;
  font-size: 12px;
  color: #c2410c;
}

.filter-empty-hint {
  margin: 0 16px 8px;
  font-size: 12px;
  color: #94a3b8;
}

.visitor-item.locked {
  opacity: 0.72;
}
</style>
