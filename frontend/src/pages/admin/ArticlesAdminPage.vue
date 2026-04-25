<template>
  <section class="admin-page">
    <section class="platform-card">
      <h1 class="platform-title">资讯管理</h1>
      <div class="admin-toolbar">
        <input v-model="draft.title" class="admin-input" placeholder="资讯标题" />
        <input v-model="draft.tag" class="admin-input" placeholder="标签" />
        <button type="button" class="admin-btn primary" @click="create">新增资讯</button>
      </div>
      <textarea v-model="draft.content" class="admin-textarea" placeholder="资讯内容" />
    </section>

    <div class="admin-list">
      <article v-for="item in state.articles" :key="item.id" class="admin-row">
        <div class="admin-row-head">
          <strong>{{ item.title }}</strong>
          <span class="admin-tag" :class="item.status">{{ item.status }}</span>
        </div>
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
const draft = reactive({ title: '', summary: '', content: '', tag: '平台资讯' })

function create() {
  if (!draft.title || !draft.summary) return
  createContent('articles', { ...draft })
  draft.title = ''
  draft.summary = ''
  draft.content = ''
  showToast({ message: '资讯已创建', type: 'success' })
}

function save(item) {
  updateContent('articles', item)
  showToast({ message: '资讯已保存', type: 'success' })
}

function toggle(item) {
  toggleContentStatus('articles', item.id)
  showToast({ message: '状态已更新', type: 'success' })
}
</script>
