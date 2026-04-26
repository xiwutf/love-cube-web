<template>
  <section class="admin-page">
    <section class="platform-card admin-hero">
      <p class="platform-kicker">Overview</p>
      <h1 class="platform-title">后台运营总览</h1>
      <p class="platform-subtitle">
        点击任意卡片即可跳转到对应模块，直接处理公告、用户、审核与举报等任务。
      </p>
    </section>

    <section class="admin-kpi-grid">
      <router-link
        v-for="item in kpiCards"
        :key="item.to"
        :to="item.to"
        class="platform-card admin-kpi-card card-link"
        :class="{ alert: item.alert }"
      >
        <p class="admin-kpi-label">{{ item.label }}</p>
        <p class="admin-kpi-value" :class="{ loading }">{{ loading ? '...' : item.value }}</p>
        <p class="admin-kpi-tip">{{ item.tip }}</p>
        <p class="admin-kpi-action">点击进入</p>
      </router-link>
    </section>

    <section class="admin-quick-grid">
      <router-link
        v-for="item in quickCards"
        :key="item.to"
        :to="item.to"
        class="platform-card admin-quick-card card-link"
      >
        <p class="admin-quick-title">{{ item.title }}</p>
        <p class="admin-quick-desc">{{ item.desc }}</p>
        <p class="admin-kpi-action">查看详情</p>
      </router-link>
    </section>

    <div v-if="error" class="admin-error">
      <p>{{ error }}</p>
      <button class="admin-btn" @click="load">重新加载</button>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { getAdminStats } from '@/api/adminContent.js'

const loading = ref(true)
const error = ref('')
const stats = ref({
  totalUsers: 0,
  totalAnnouncements: 0,
  pendingVerifications: 0,
  pendingReports: 0
})

const kpiCards = computed(() => [
  {
    label: '用户总量',
    value: stats.value.totalUsers,
    tip: '全站注册用户',
    to: '/admin/users',
    alert: false
  },
  {
    label: '公告总数',
    value: stats.value.totalAnnouncements,
    tip: '内容发布与维护',
    to: '/admin/announcements',
    alert: false
  },
  {
    label: '待认证审核',
    value: stats.value.pendingVerifications,
    tip: '请及时处理实名申请',
    to: '/admin/verifications',
    alert: stats.value.pendingVerifications > 0
  },
  {
    label: '待举报处理',
    value: stats.value.pendingReports,
    tip: '建议优先处理高风险内容',
    to: '/admin/reports',
    alert: stats.value.pendingReports > 0
  }
])

const quickCards = [
  { title: '公告管理', desc: '发布、下架与编辑公告', to: '/admin/announcements' },
  { title: '资讯管理', desc: '维护资讯内容和标签', to: '/admin/articles' },
  { title: '活动管理', desc: '管理活动状态和详情', to: '/admin/events' },
  { title: '用户管理', desc: '调整用户角色与状态', to: '/admin/users' },
  { title: '邀请记录', desc: '查看邀请关系与来源', to: '/admin/invites' },
  { title: '认证审核', desc: '处理实名认证申请', to: '/admin/verifications' }
]

async function load() {
  loading.value = true
  error.value = ''
  try {
    stats.value = await getAdminStats()
  } catch (e) {
    error.value = e.message || '统计信息加载失败'
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.admin-hero {
  margin-bottom: 14px;
}

.admin-kpi-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.admin-kpi-card {
  padding: 18px;
  min-height: 182px;
}

.card-link {
  display: flex;
  flex-direction: column;
  width: 100%;
  min-height: 100%;
  text-decoration: none;
  color: #1c2940;
  transition: transform 0.18s ease, box-shadow 0.22s ease, border-color 0.22s ease;
}

.card-link:hover {
  transform: translateY(-2px);
  box-shadow: 0 14px 30px rgba(18, 32, 57, 0.12);
  border-color: #ffb5c7;
}

.admin-kpi-label {
  margin: 0;
  font-size: 13px;
  color: #6c7c94;
  font-weight: 700;
  letter-spacing: 0.04em;
}

.admin-kpi-value {
  margin: 10px 0 6px;
  font-size: 34px;
  line-height: 1;
  font-weight: 800;
  color: #16253f;
}

.admin-kpi-value.loading {
  color: #c0cad8;
}

.admin-kpi-tip {
  margin: 0;
  font-size: 12px;
  color: #8191a8;
  line-height: 1.6;
}

.admin-kpi-action {
  margin: 12px 0 0;
  font-size: 12px;
  color: #d9486d;
  font-weight: 700;
}

.admin-kpi-card.alert {
  border-color: #ffd3dd;
  box-shadow: 0 12px 24px rgba(232, 79, 115, 0.13);
}

.admin-kpi-card.alert .admin-kpi-value {
  color: #d23e65;
}

.admin-quick-grid {
  margin-top: 12px;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.admin-quick-card {
  padding: 16px;
  min-height: 132px;
}

.admin-quick-title {
  margin: 0;
  font-size: 17px;
  font-weight: 700;
  color: #182741;
}

.admin-quick-desc {
  margin: 8px 0 0;
  font-size: 13px;
  line-height: 1.6;
  color: #667892;
  flex: 1;
}

@media (max-width: 1200px) {
  .admin-kpi-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .admin-quick-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 767px) {
  .admin-kpi-grid,
  .admin-quick-grid {
    grid-template-columns: 1fr;
  }

  .admin-kpi-card,
  .admin-quick-card {
    padding: 14px;
  }

  .admin-kpi-value {
    font-size: 30px;
  }
}
</style>
