<template>
  <section class="admin-page">
    <section class="platform-card">
      <h1 class="platform-title">活动管理</h1>
      <p class="platform-subtitle">PC 端支持完整编辑；手机端只做活动查看与状态感知。</p>

      <div class="admin-toolbar admin-desktop-only">
        <input v-model="draft.title" class="admin-input" placeholder="活动名称" />
        <input v-model="draft.time" class="admin-input" placeholder="时间" />
        <input v-model="draft.location" class="admin-input" placeholder="地点" />
        <button type="button" class="admin-btn primary" @click="create">新增活动</button>
      </div>
      <textarea v-model="draft.content" class="admin-textarea admin-desktop-only" placeholder="活动详情" />

      <p class="admin-mobile-note admin-mobile-only">手机端仅支持查看活动状态和报名人数，编辑发布请在 PC 端完成。</p>
    </section>

    <section class="admin-table-wrap admin-desktop-only">
      <table class="admin-table">
        <thead>
          <tr>
            <th>活动</th>
            <th>时间地点</th>
            <th>报名人数</th>
            <th>状态</th>
            <th>摘要</th>
            <th>内容编辑</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in state.events" :key="item.id">
            <td>{{ item.title }}</td>
            <td>{{ item.time }}<br />{{ item.location }}</td>
            <td>{{ item.signupCount || 0 }}</td>
            <td><span class="admin-tag" :class="item.status">{{ item.status }}</span></td>
            <td><input v-model="item.summary" class="admin-input" /></td>
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
      <article v-for="item in state.events" :key="item.id" class="admin-row">
        <div class="admin-row-head">
          <strong>{{ item.title }}</strong>
          <span class="admin-tag" :class="item.status">{{ item.status }}</span>
        </div>
        <p class="admin-row-meta">{{ item.time }} · {{ item.location }}</p>
        <p class="admin-row-meta">报名人数 {{ item.signupCount || 0 }}</p>
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
