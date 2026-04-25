<template>
  <div class="privacy-page">
    <NavBar title="隐私设置" />

    <van-cell-group inset>
      <van-cell title="隐藏年龄">
        <template #right-icon>
          <van-switch v-model="settings.hideAge" size="22" @change="save" />
        </template>
      </van-cell>
      <van-cell title="隐藏城市">
        <template #right-icon>
          <van-switch v-model="settings.hideLocation" size="22" @change="save" />
        </template>
      </van-cell>
      <van-cell title="允许被推荐">
        <template #right-icon>
          <van-switch v-model="settings.allowRecommend" size="22" @change="save" />
        </template>
      </van-cell>
      <van-cell title="允许陌生人发消息">
        <template #right-icon>
          <van-switch v-model="settings.allowStrangerMessage" size="22" @change="save" />
        </template>
      </van-cell>
    </van-cell-group>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { showToast } from 'vant'
import NavBar from '@/components/NavBar.vue'

const STORAGE_KEY = 'fellowship-privacy-settings'

const defaults = {
  hideAge: false,
  hideLocation: false,
  allowRecommend: true,
  allowStrangerMessage: true
}

const settings = reactive(loadSettings())

function loadSettings() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    return raw ? { ...defaults, ...JSON.parse(raw) } : { ...defaults }
  } catch {
    return { ...defaults }
  }
}

function save() {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(settings))
  showToast({ type: 'success', message: '已保存' })
}
</script>

<style scoped>
.privacy-page {
  min-height: 100vh;
  background: #f8f9fb;
  padding-top: 6px;
}
</style>

