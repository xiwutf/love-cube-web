<template>
  <section class="admin-page">
    <section class="platform-card">
      <h1 class="platform-title">公告管理</h1>
      <p class="platform-subtitle">PC 端支持完整编辑与发布流程；手机端提供查看能力。</p>

      <div class="admin-toolbar admin-desktop-only">
        <input v-model="draft.title" class="admin-input" placeholder="公告标题" />
        <input v-model="draft.summary" class="admin-input" placeholder="摘要" />
        <button type="button" class="admin-btn primary" @click="create">新增公告</button>
      </div>
      <textarea v-model="draft.content" class="admin-textarea admin-desktop-only" placeholder="公告内容" />

      <p class="admin-mobile-note admin-mobile-only">手机端仅支持查看公告状态与更新时间，编辑发布请在 PC 端完成。</p>
    </section>

    <section class="admin-table-wrap admin-desktop-only">
      <table class="admin-table">
        <thead>
          <tr>
            <th>标题</th>
            <th>摘要</th>
            <th>状态</th>
            <th>内容编辑</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in state.announcements" :key="item.id">
            <td>{{ item.title }}</td>
            <td>{{ item.summary }}</td>
            <td><span class="admin-tag" :class="item.status">{{ item.status }}</span></td>
            <td><textarea v-model="item.content" class="admin-textarea" /></td>
            <td>
              <div class="admin-cell-actions">
                <button class="admin-btn" type="button" @click="save(item)">保存</button>
                <button class="admin-btn" type="button" @click="toggle(item)">{{ item.status === 'published' ? '下架' : '发布' }}</button>
                <span class="admin-row-meta">{{ item.updatedAt }}</span>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </section>

    <div class="admin-list admin-mobile-only">
      <article v-for="item in state.announcements" :key="item.id" class="admin-row">
        <div class="admin-row-head">
          <strong>{{ item.title }}</strong>
          <span class="admin-tag" :class="item.status">{{ item.status }}</span>
        </div>
        <p class="admin-row-meta">{{ item.summary }}</p>
        <p class="admin-row-meta">更新于 {{ item.updatedAt }}</p>
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
