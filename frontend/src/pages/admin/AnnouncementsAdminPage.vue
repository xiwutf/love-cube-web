<template>
  <section class="admin-page">
    <section class="platform-card">
      <h1 class="platform-title">公告管理</h1>
      <div class="admin-toolbar">
        <input v-model="draft.title" class="admin-input" placeholder="公告标题" />
        <input v-model="draft.summary" class="admin-input" placeholder="摘要" />
        <button type="button" class="admin-btn primary" @click="create">新增公告</button>
      </div>
      <textarea v-model="draft.content" class="admin-textarea" placeholder="公告内容" />
    </section>

    <div class="admin-list">
      <article v-for="item in state.announcements" :key="item.id" class="admin-row">
        <div class="admin-row-head">
          <strong>{{ item.title }}</strong>
          <span class="admin-tag" :class="item.status">{{ item.status }}</span>
        </div>
        <p>{{ item.summary }}</p>
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

const draft = reactive({ title: '', summary: '', content: '' })

function create() {
  if (!draft.title || !draft.summary) return
  createContent('announcements', { ...draft, date: new Date().toISOString().slice(0, 10) })
  draft.title = ''
  draft.summary = ''
  draft.content = ''
  showToast({ message: '公告已创建', type: 'success' })
}

function save(item) {
  updateContent('announcements', item)
  showToast({ message: '公告已保存', type: 'success' })
}

function toggle(item) {
  toggleContentStatus('announcements', item.id)
  showToast({ message: '状态已更新', type: 'success' })
}
</script>
