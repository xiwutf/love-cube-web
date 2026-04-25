<template>
  <section class="admin-page">
    <section class="platform-card">
      <h1 class="platform-title">活动管理</h1>
      <div class="admin-toolbar">
        <input v-model="draft.title" class="admin-input" placeholder="活动名称" />
        <input v-model="draft.time" class="admin-input" placeholder="时间" />
        <input v-model="draft.location" class="admin-input" placeholder="地点" />
        <button type="button" class="admin-btn primary" @click="create">新增活动</button>
      </div>
      <textarea v-model="draft.content" class="admin-textarea" placeholder="活动详情" />
    </section>

    <div class="admin-list">
      <article v-for="item in state.events" :key="item.id" class="admin-row">
        <div class="admin-row-head">
          <strong>{{ item.title }}</strong>
          <span class="admin-tag" :class="item.status">{{ item.status }}</span>
        </div>
        <p class="admin-row-meta">{{ item.time }} · {{ item.location }} · 报名人数 {{ item.signupCount || 0 }}</p>
        <input v-model="item.summary" class="admin-input" />
        <textarea v-model="item.content" class="admin-textarea" />
        <div class="admin-toolbar">
          <button class="admin-btn" type="button" @click="save(item)">保存</button>
          <button class="admin-btn" type="button" @click="toggle(item)">{{ item.status === 'published' ? '下架' : '发布' }}</button>
          <span class="admin-row-meta">更新于 {{ item.updatedAt }}</span>
        </div>
      </article>
    </div>
  </section>
</template>

<script setup>
import { reactive } from 'vue'
import { showToast } from 'vant'
import { usePlatformState } from '@/mock/platformState.js'

const { state, createContent, updateContent, toggleContentStatus } = usePlatformState()
const draft = reactive({ title: '', summary: '', content: '', time: '', location: '', signupCount: 0 })

function create() {
  if (!draft.title || !draft.time) return
  createContent('events', { ...draft })
  draft.title = ''
  draft.summary = ''
  draft.content = ''
  draft.time = ''
  draft.location = ''
  showToast({ message: '活动已创建', type: 'success' })
}

function save(item) {
  updateContent('events', item)
  showToast({ message: '活动已保存', type: 'success' })
}

function toggle(item) {
  toggleContentStatus('events', item.id)
  showToast({ message: '状态已更新', type: 'success' })
}
</script>
