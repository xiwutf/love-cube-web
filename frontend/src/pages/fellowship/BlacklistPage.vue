<template>
  <div class="blacklist-page">
    <NavBar title="黑名单" />
    <section class="content">
      <template v-if="list.length">
        <article v-for="item in list" :key="item.userId" class="row">
          <div>
            <p class="name">{{ item.nickname || `用户${item.userId}` }}</p>
            <p class="meta">ID {{ item.userId }}</p>
          </div>
          <van-button size="small" plain type="primary" @click="remove(item.userId)">解除拉黑</van-button>
        </article>
      </template>
      <van-empty v-else description="黑名单为空" />
    </section>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { showToast } from 'vant'
import NavBar from '@/components/NavBar.vue'

const KEY = 'fellowship-blacklist'
const list = ref(load())

function load() {
  try {
    return JSON.parse(localStorage.getItem(KEY) || '[]')
  } catch {
    return []
  }
}

function remove(userId) {
  list.value = list.value.filter((item) => String(item.userId) !== String(userId))
  localStorage.setItem(KEY, JSON.stringify(list.value))
  showToast({ type: 'success', message: '已解除' })
}
</script>

<style scoped>
.blacklist-page {
  min-height: 100vh;
  background: #f7f9fc;
}

.content {
  padding: 12px;
}

.row {
  background: #fff;
  border-radius: 14px;
  padding: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.name {
  margin: 0;
  font-size: 15px;
  color: #111827;
}

.meta {
  margin: 4px 0 0;
  font-size: 12px;
  color: #9ca3af;
}
</style>

