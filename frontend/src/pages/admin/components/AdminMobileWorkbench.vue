<template>
  <section class="mobile-workbench">
    <header class="mobile-workbench-header">
      <div>
        <h2>后台管理</h2>
        <p>今日概览</p>
      </div>
      <button type="button" class="header-action" :disabled="loading" aria-label="刷新统计" @click="$emit('refresh')">
        {{ loading ? '...' : '刷新' }}
      </button>
    </header>

    <section class="overview-grid" aria-label="数据概览">
      <article v-for="item in overviewCards" :key="item.label" class="overview-card">
        <strong>{{ item.value }}</strong>
        <span>{{ item.label }}</span>
        <p>{{ item.hint }}</p>
      </article>
    </section>

    <section class="quick-actions" aria-label="常用操作">
      <router-link v-for="action in quickActions" :key="action.to" :to="action.to" class="quick-action">
        <span class="entry-icon" aria-hidden="true">{{ action.icon }}</span>
        <span>{{ action.label }}</span>
      </router-link>
    </section>

    <section id="admin-mobile-todo" class="todo-card" aria-label="待办提醒">
      <div class="section-heading">
        <h3>待办提醒</h3>
        <span>{{ totalTodo }} 项</span>
      </div>
      <router-link v-for="item in todoItems" :key="item.label" :to="item.to" class="todo-row">
        <span class="todo-name">{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
      </router-link>
    </section>

    <section class="module-groups" aria-label="模块分组">
      <article v-for="group in moduleGroups" :key="group.title" class="module-card">
        <div class="section-heading">
          <h3>{{ group.title }}</h3>
        </div>
        <div class="module-entry-list">
          <router-link v-for="entry in group.items" :key="entry.label" :to="entry.to" class="module-entry">
            <span class="entry-icon" aria-hidden="true">{{ entry.icon }}</span>
            <span class="entry-copy">
              <strong>{{ entry.label }}</strong>
              <small>{{ entry.hint }}</small>
            </span>
          </router-link>
        </div>
      </article>
    </section>

    <div v-if="error" class="mobile-error">
      <span>{{ error }}</span>
      <button type="button" @click="$emit('retry')">重试</button>
    </div>

    <nav class="mobile-bottom-nav" aria-label="后台移动端导航">
      <router-link to="/admin" class="bottom-nav-item is-active">
        <span aria-hidden="true">□</span>
        <b>工作台</b>
      </router-link>
      <a href="#admin-mobile-todo" class="bottom-nav-item">
        <span aria-hidden="true">!</span>
        <b>待办</b>
      </a>
      <router-link to="/admin/users" class="bottom-nav-item">
        <span aria-hidden="true">○</span>
        <b>我的</b>
      </router-link>
    </nav>
  </section>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  stats: {
    type: Object,
    required: true
  },
  loading: {
    type: Boolean,
    default: false
  },
  error: {
    type: String,
    default: ''
  }
})

defineEmits(['refresh', 'retry'])

function valueOf(groupKey, key) {
  const group = props.stats?.[groupKey]
  const raw = group?.[key] ?? props.stats?.[key] ?? 0
  const number = Number(raw)
  return Number.isFinite(number) ? number : 0
}

const pendingReview = computed(() =>
  valueOf('fellowshipData', 'pendingVerifications') +
  valueOf('helpAndShareData', 'positiveSharesPending') +
  valueOf('helpAndShareData', 'helpRequestsPending')
)

const pendingFeedback = computed(() =>
  valueOf('feedbackData', 'pendingFeedbacks') || valueOf('pendingFeedbacks', 'pendingFeedbacks')
)

const todayPublished = computed(() =>
  valueOf('contentData', 'todayPublishedContent') ||
  valueOf('contentData', 'contentPublishedToday') ||
  valueOf('helpAndShareData', 'positiveSharesToday') +
    valueOf('engagementData', 'dynamicsToday') +
    valueOf('communityData', 'groupPostsToday')
)

const overviewCards = computed(() => [
  { label: '待审核内容', value: pendingReview.value, hint: '内容与资料' },
  { label: '待处理反馈', value: pendingFeedback.value, hint: '用户反馈' },
  { label: '今日新增用户', value: valueOf('userData', 'todayNewUsers'), hint: '新注册' },
  { label: '今日发布内容', value: todayPublished.value, hint: '公开内容' }
])

const quickActions = [
  { label: '发布平台公告', to: '/admin/announcements', icon: '+' },
  { label: '审核内容', to: '/admin/positive-shares', icon: '✓' },
  { label: '处理反馈', to: '/admin/feedbacks', icon: '!' },
  { label: '资源管理', to: '/admin/local-resources', icon: '□' }
]

const todoItems = computed(() => [
  { label: '内容待审核', value: pendingReview.value, to: '/admin/positive-shares' },
  { label: '用户反馈待处理', value: pendingFeedback.value, to: '/admin/feedbacks' },
  {
    label: '敏感内容待检查',
    value: valueOf('fellowshipData', 'pendingReports') || valueOf('governanceData', 'todayReports'),
    to: '/admin/reports'
  },
  {
    label: '平台公告可更新',
    value: valueOf('contentData', 'totalAnnouncements'),
    to: '/admin/announcements'
  }
])

const totalTodo = computed(() =>
  todoItems.value.reduce((sum, item) => sum + Number(item.value || 0), 0)
)

const moduleGroups = [
  {
    title: '内容管理',
    items: [
      { label: '平台公告', hint: '发布与下架', to: '/admin/announcements', icon: '告' },
      { label: '本地资源', hint: '资源维护', to: '/admin/local-resources', icon: '资' },
      { label: '心声投稿', hint: '内容审核', to: '/admin/positive-shares', icon: '动' },
      { label: '联谊动态', hint: '点赞明细', to: '/admin/fellowship-dynamics', icon: '谊' },
      { label: '敏感词管理', hint: '举报检查', to: '/admin/reports', icon: '敏' }
    ]
  },
  {
    title: '用户运营',
    items: [
      { label: '用户管理', hint: '账号状态', to: '/admin/users', icon: '用' },
      { label: '反馈管理', hint: '跟进处理', to: '/admin/feedbacks', icon: '馈' },
      { label: '成长体系', hint: '邀请记录', to: '/admin/invites', icon: '长' },
      { label: '消息通知', hint: '通知数据', to: '/admin/analytics', icon: '信' }
    ]
  },
  {
    title: '平台模块',
    items: [
      { label: '团体管理', hint: '全站团体', to: '/admin/platform/groups', icon: '团' },
      { label: '互助管理', hint: '求助审核', to: '/admin/help-requests', icon: '助' },
      { label: '联谊管理', hint: '资料审核', to: '/admin/verifications', icon: '联' }
    ]
  },
  {
    title: '系统设置',
    items: [
      { label: '权限管理', hint: '后台用户', to: '/admin/users', icon: '权' },
      { label: '配置管理', hint: '模块开关', to: '/admin/modules', icon: '配' },
      { label: '数据统计', hint: '访问分析', to: '/admin/analytics', icon: '数' }
    ]
  }
]
</script>

<style scoped>
.mobile-workbench {
  min-height: calc(100vh - 57px);
  padding: 0 0 72px;
  background: var(--lc-soft);
  color: var(--lc-text);
  overflow-x: hidden;
}

.mobile-workbench-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 4px 2px 10px;
}

.mobile-workbench-header h2 {
  margin: 0;
  font-size: 18px;
  line-height: 1.25;
  font-weight: 800;
}

.mobile-workbench-header p {
  margin: 3px 0 0;
  font-size: 12px;
  color: var(--lc-muted-light);
}

.header-action {
  min-width: 54px;
  height: 34px;
  border: 1px solid var(--lc-border);
  border-radius: 10px;
  background: var(--lc-surface);
  color: var(--lc-blue);
  font-size: 12px;
  font-weight: 700;
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.overview-card,
.todo-card,
.module-card {
  border: 1px solid var(--lc-border);
  border-radius: 14px;
  background: var(--lc-surface);
  box-shadow: var(--lc-shadow-sm);
}

.overview-card {
  min-width: 0;
  padding: 12px;
}

.overview-card strong {
  display: block;
  font-size: 22px;
  line-height: 1;
  color: var(--lc-text);
  font-weight: 850;
  font-variant-numeric: tabular-nums;
}

.overview-card span {
  display: block;
  margin-top: 8px;
  font-size: 13px;
  font-weight: 700;
  color: var(--lc-slate);
}

.overview-card p {
  margin: 4px 0 0;
  font-size: 12px;
  color: var(--lc-subtle);
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 8px;
  margin-top: 12px;
}

.quick-action {
  min-width: 0;
  min-height: 64px;
  border: 1px solid var(--lc-border);
  border-radius: 14px;
  background: var(--lc-surface);
  color: var(--lc-text);
  text-decoration: none;
  display: grid;
  place-items: center;
  align-content: center;
  gap: 6px;
  font-size: 12px;
  font-weight: 700;
}

.entry-icon {
  width: 24px;
  height: 24px;
  border-radius: 8px;
  display: inline-grid;
  place-items: center;
  background: var(--lc-blue-light);
  color: var(--lc-blue-mid);
  font-size: 12px;
  font-weight: 800;
  flex: 0 0 auto;
}

.todo-card,
.module-groups {
  margin-top: 12px;
}

.todo-card {
  padding: 12px;
}

.section-heading {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.section-heading h3 {
  margin: 0;
  font-size: 15px;
  line-height: 1.25;
  font-weight: 800;
  color: var(--lc-text);
}

.section-heading span {
  font-size: 12px;
  color: var(--lc-muted-light);
  font-weight: 700;
}

.todo-row {
  margin-top: 10px;
  min-height: 38px;
  border-top: 1px solid var(--lc-border);
  color: var(--lc-text);
  text-decoration: none;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.todo-row:first-of-type {
  margin-top: 8px;
}

.todo-name {
  min-width: 0;
  font-size: 13px;
  color: var(--lc-slate);
  font-weight: 700;
}

.todo-row strong {
  font-size: 15px;
  color: var(--lc-blue-mid);
  font-variant-numeric: tabular-nums;
}

.module-groups {
  display: grid;
  gap: 12px;
}

.module-card {
  padding: 12px;
}

.module-entry-list {
  display: grid;
  gap: 8px;
  margin-top: 12px;
}

.module-entry {
  min-width: 0;
  min-height: 52px;
  border: 1px solid var(--lc-border);
  border-radius: 12px;
  background: var(--lc-bg);
  color: var(--lc-text);
  text-decoration: none;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
}

.entry-copy {
  min-width: 0;
  display: grid;
  gap: 2px;
}

.entry-copy strong {
  font-size: 13px;
  color: var(--lc-text);
  font-weight: 800;
}

.entry-copy small {
  font-size: 12px;
  color: var(--lc-muted-light);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.mobile-error {
  margin-top: 12px;
  padding: 10px 12px;
  border-radius: 12px;
  background: var(--lc-red-light);
  color: var(--lc-red);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  font-size: 12px;
}

.mobile-error button {
  border: 0;
  border-radius: 8px;
  background: var(--lc-surface);
  color: var(--lc-red);
  font-size: 12px;
  font-weight: 800;
  padding: 6px 9px;
}

.mobile-bottom-nav {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 70;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  padding: 6px 16px max(6px, env(safe-area-inset-bottom, 0px));
  border-top: 1px solid var(--lc-border);
  background: var(--lc-surface);
  box-shadow: 0 -8px 24px rgba(15, 23, 42, 0.06);
}

.bottom-nav-item {
  min-width: 0;
  color: var(--lc-muted-light);
  text-decoration: none;
  display: grid;
  justify-items: center;
  gap: 2px;
  font-size: 11px;
  font-weight: 700;
}

.bottom-nav-item span {
  font-size: 16px;
  line-height: 1;
}

.bottom-nav-item b {
  font-size: 11px;
}

.bottom-nav-item.is-active {
  color: var(--lc-blue);
}

@media (max-width: 360px) {
  .quick-actions {
    gap: 6px;
  }

  .quick-action {
    font-size: 11px;
  }
}
</style>
