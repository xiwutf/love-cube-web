<template>
  <section class="admin-page">
    <section class="platform-card">
      <h1 class="platform-title">举报处理</h1>
      <p class="platform-subtitle">查看举报内容并更新处理状态与备注。</p>
    </section>

    <div class="admin-list">
      <article v-for="item in state.reports" :key="item.id" class="admin-row">
        <div class="admin-row-head">
          <strong>{{ item.type }}</strong>
          <span class="admin-tag" :class="item.status">{{ item.status }}</span>
        </div>
        <p>举报内容：{{ item.content }}</p>
        <p class="admin-row-meta">举报人：{{ item.reporterId }} · 被举报：{{ item.targetUserId }} · {{ item.createdAt }}</p>
        <select v-model="item.status" class="admin-select">
          <option value="pending">pending</option>
          <option value="processing">processing</option>
          <option value="resolved">resolved</option>
        </select>
        <textarea v-model="item.note" class="admin-textarea" placeholder="处理备注" />
        <div class="admin-toolbar">
          <button class="admin-btn" type="button" @click="save(item)">保存处理结果</button>
        </div>
      </article>
    </div>
  </section>
</template>

<script setup>
import { showToast } from 'vant'
import { usePlatformState } from '@/mock/platformState.js'

const { state, updateReport } = usePlatformState()

function save(item) {
  updateReport(item.id, { status: item.status, note: item.note || '' })
  showToast({ message: '举报处理已更新', type: 'success' })
}
</script>
