<template>
  <section class="admin-page">
    <section class="platform-card">
      <h1 class="platform-title">认证审核</h1>
      <p class="platform-subtitle">优先保证手机端可审核：通过、驳回、填写原因均可操作。</p>
    </section>

    <section class="admin-table-wrap admin-desktop-only">
      <table class="admin-table">
        <thead>
          <tr>
            <th>申请人</th>
            <th>实名信息</th>
            <th>提交/审核</th>
            <th>状态</th>
            <th>驳回原因</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in state.verifications" :key="item.id">
            <td>{{ resolveName(item.userId) }}</td>
            <td>
              姓名：{{ item.realName }}<br />
              证件：{{ item.idNumber }}
            </td>
            <td>{{ item.submittedAt }}<br />{{ item.reviewedAt || '待审核' }}</td>
            <td><span class="admin-tag" :class="item.status">{{ item.status }}</span></td>
            <td><textarea v-model="rejectMemo[item.id]" class="admin-textarea" placeholder="驳回原因" /></td>
            <td>
              <div class="admin-cell-actions">
                <button class="admin-btn primary" type="button" @click="approve(item)">通过</button>
                <button class="admin-btn" type="button" @click="reject(item)">驳回</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </section>

    <div class="admin-list admin-mobile-only">
      <article v-for="item in state.verifications" :key="item.id" class="admin-row">
        <div class="admin-row-head">
          <strong>申请人：{{ resolveName(item.userId) }}</strong>
          <span class="admin-tag" :class="item.status">{{ item.status }}</span>
        </div>
        <p class="admin-row-meta">姓名：{{ item.realName }} · 证件号：{{ item.idNumber }}</p>
        <p class="admin-row-meta">提交：{{ item.submittedAt }} · 审核：{{ item.reviewedAt || '待审核' }}</p>
        <textarea v-model="rejectMemo[item.id]" class="admin-textarea" placeholder="驳回原因（驳回时填写）" />
        <div class="admin-toolbar">
          <button class="admin-btn primary" type="button" @click="approve(item)">通过</button>
          <button class="admin-btn" type="button" @click="reject(item)">驳回</button>
        </div>
      </article>
    </div>
  </section>
</template>

<script setup>
import { reactive } from 'vue'
import { showToast } from 'vant'
import { usePlatformState } from '@/mock/platformState.js'

const { state, reviewVerification, getUserById } = usePlatformState()
const rejectMemo = reactive({})

function resolveName(userId) {
  return getUserById(userId)?.username || `用户${userId}`
}

function approve(item) {
  reviewVerification(item.id, 'approved')
  showToast({ message: '审核通过', type: 'success' })
}

function reject(item) {
  const reason = rejectMemo[item.id] || '资料不完整，请补充后重新提交。'
  reviewVerification(item.id, 'rejected', reason)
  showToast({ message: '已驳回', type: 'success' })
}
</script>
