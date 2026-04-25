<template>
  <section class="admin-page">
    <section class="platform-card">
      <h1 class="platform-title">用户管理</h1>
      <p class="platform-subtitle">PC 端完整查看与状态管理；手机端保留轻量封禁操作。</p>
    </section>

    <section class="admin-table-wrap admin-desktop-only">
      <table class="admin-table">
        <thead>
          <tr>
            <th>用户</th>
            <th>手机号</th>
            <th>角色</th>
            <th>认证</th>
            <th>状态</th>
            <th>注册时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in state.users" :key="item.id">
            <td>{{ item.username }}</td>
            <td>{{ item.phone || '无手机号' }}</td>
            <td>{{ item.role }}</td>
            <td>{{ item.verificationStatus }}</td>
            <td><span class="admin-tag" :class="item.status">{{ item.status }}</span></td>
            <td>{{ item.createdAt }}</td>
            <td>
              <div class="admin-cell-actions">
                <button class="admin-btn" type="button" @click="setStatus(item, 'active')">解封</button>
                <button class="admin-btn" type="button" @click="setStatus(item, 'banned')">封禁</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </section>

    <div class="admin-list admin-mobile-only">
      <article v-for="item in state.users" :key="item.id" class="admin-row">
        <div class="admin-row-head">
          <strong>{{ item.username }}</strong>
          <span class="admin-tag" :class="item.status">{{ item.status }}</span>
        </div>
        <p class="admin-row-meta">{{ item.phone || '无手机号' }} · {{ item.role }}</p>
        <p class="admin-row-meta">认证：{{ item.verificationStatus }} · 注册：{{ item.createdAt }}</p>
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
