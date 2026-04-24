<template>
  <div class="search-page">
    <div class="search-bar">
      <van-search
        v-model="keyword"
        placeholder="搜索用户昵称"
        show-action
        @search="doSearch"
        @input="onInput"
        @cancel="router.back()"
      />
    </div>

    <van-list v-model:loading="loading" :finished="noMore" finished-text="没有更多了" @load="loadMore">
      <UserCard
        v-for="u in list"
        :key="u.userId"
        :user="u"
        @click="router.push(`/user-profile/${u.userId}`)"
      />
      <van-empty
        v-if="!loading && searched && !list.length"
        description="未找到相关用户"
        image-size="80"
      />
    </van-list>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import UserCard from '@/components/UserCard.vue'
import { normalizeUser } from '@/utils/normalizeUser.js'
import { debounce } from '@/utils/format.js'
import request from '@/api/request.js'

const router   = useRouter()
const keyword  = ref('')
const list     = ref([])
const loading  = ref(false)
const noMore   = ref(false)
const searched = ref(false)
let   page     = 0

async function fetchSearch(reset = false) {
  if (reset) { page = 0; list.value = []; noMore.value = false }
  if (!keyword.value.trim() || loading.value || noMore.value) return

  loading.value = true
  searched.value = true
  try {
    const data  = await request.get('/search', { params: { keyword: keyword.value, page, size: 10 } })
    const items = (Array.isArray(data) ? data : []).map(normalizeUser)
    list.value.push(...items)
    if (items.length < 10) noMore.value = true
    else page++
  } finally {
    loading.value = false
  }
}

function doSearch() { fetchSearch(true) }

const onInput = debounce(() => { if (keyword.value.trim()) fetchSearch(true) }, 400)

function loadMore() { fetchSearch() }
</script>

<style scoped>
.search-page { min-height: 100vh; background: #f8f8f8; }
.search-bar  { position: sticky; top: 0; z-index: 10; background: #fff; }
</style>
