<template>
  <section class="admin-page">
    <section class="platform-card">
      <h1 class="platform-title">用户管理</h1>
      <p class="platform-subtitle">查看用户、认证状态与封禁状态。</p>
    </section>

    <div class="admin-list">
      <article v-for="item in state.users" :key="item.id" class="admin-row">
        <div class="admin-row-head">
          <strong>{{ item.username }}（{{ item.phone || '无手机号' }}）</strong>
          <span class="admin-tag" :class="item.status">{{ item.status }}</span>
        </div>
        <p class="admin-row-meta">角色：{{ item.role }} · 认证：{{ item.verificationStatus }} · 注册时间：{{ item.createdAt }}</p>
        <div class="admin-toolbar">
          <button class="admin-btn" type="button" @click="setStatus(item, 'active')">解封</button>
          <button class="admin-btn" type="button" @click="setStatus(item, 'banned')">封禁</button>
        </div>
      </article>
    </div>
  </section>
</template>

<script setup>
import { showToast } from 'vant'
import { usePlatformState } from '@/mock/platformState.js'

const { state, updateUserStatus } = usePlatformState()

function setStatus(item, status) {
  updateUserStatus(item.id, status)
  showToast({ message: status === 'active' ? '用户已解封' : '用户已封禁', type: 'success' })
}
</script>
