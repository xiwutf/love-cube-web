<template>
  <main class="pc-play-hub">
    <header class="pc-play-head">
      <h1>成长玩法</h1>
      <p>签到、任务、心声、互助与本地服务 — PC 端快捷入口</p>
    </header>

    <section class="pc-play-grid">
      <router-link
        v-for="item in items"
        :key="item.to"
        :to="item.to"
        class="pc-play-card"
        :class="item.tone"
      >
        <span class="icon" aria-hidden="true">{{ item.icon }}</span>
        <span class="title">{{ item.title }}</span>
        <span class="sub">{{ item.sub }}</span>
      </router-link>
    </section>

    <p class="pc-play-tip">
      联谊匹配、VIP、活动签到请使用顶部导航进入联谊模块，或访问
      <router-link to="/fellowship/discover">联谊发现</router-link>。
    </p>
  </main>
</template>

<script setup>
import { PC_EVENTS, pcPath } from '@/constants/pcPaths.js'

const items = [
  { to: pcPath('tasks'), icon: '✓', title: '今日任务', sub: '新人包 · 每周挑战', tone: 'violet' },
  { to: pcPath('checkin'), icon: '☀', title: '每日签到', sub: '连续登录 streak', tone: 'orange' },
  { to: pcPath('positive-share'), icon: '♡', title: '每日心声', sub: '话题 · 周榜', tone: 'pink' },
  { to: pcPath('help'), icon: '⇄', title: '互助广场', sub: '达人榜 · 发需求', tone: 'blue' },
  { to: pcPath('groups'), icon: '◎', title: '团体广场', sub: '发现 · 加入小组', tone: 'green' },
  { to: pcPath('local'), icon: '⌂', title: '本地服务', sub: '招聘二手租房', tone: 'amber' },
  { to: pcPath('topics'), icon: '💬', title: '话题广场', sub: '兴趣讨论', tone: 'cyan' },
  { to: pcPath('messages'), icon: '✉', title: '消息中心', sub: '通知与互动', tone: 'green' },
  { to: '/fellowship/invite', icon: '◎', title: '邀请好友', sub: '有效邀请奖励', tone: 'green' },
  { to: '/fellowship/event-signups', icon: '▣', title: '活动报名', sub: '签到 · 互评', tone: 'indigo' },
  { to: PC_EVENTS, icon: '▣', title: '活动中心', sub: '平台活动列表', tone: 'orange' }
]
</script>

<style scoped>
.pc-play-hub {
  max-width: 1100px;
  margin: 0 auto;
  padding: 32px 24px 48px;
}

.pc-play-head h1 {
  margin: 0;
  font-size: 28px;
  font-weight: 800;
  color: var(--lc-text, #1a2236);
}

.pc-play-head p {
  margin: 8px 0 0;
  font-size: 15px;
  color: var(--lc-subtle, #6b7280);
}

.pc-play-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-top: 28px;
}

.pc-play-card {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 22px 20px;
  min-height: 130px;
  border-radius: 16px;
  text-decoration: none;
  background: var(--lc-surface, #fff);
  border: 1px solid var(--lc-soft, #e8ecf4);
  box-shadow: 0 10px 28px rgba(15, 23, 42, 0.05);
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}

.pc-play-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 14px 32px rgba(15, 23, 42, 0.08);
}

.pc-play-card .icon {
  font-size: 26px;
}

.pc-play-card .title {
  font-size: 17px;
  font-weight: 700;
  color: var(--lc-text, #1a2236);
}

.pc-play-card .sub {
  font-size: 13px;
  color: var(--lc-subtle, #8898aa);
}

.pc-play-card.violet { border-color: #ddd6fe; background: linear-gradient(145deg, #fff, #f5f3ff); }
.pc-play-card.orange { border-color: #fed7aa; background: linear-gradient(145deg, #fff, #fff7ed); }
.pc-play-card.pink { border-color: #fbcfe8; background: linear-gradient(145deg, #fff, #fff1f5); }
.pc-play-card.blue { border-color: #bfdbfe; background: linear-gradient(145deg, #fff, #eff6ff); }
.pc-play-card.green { border-color: #bbf7d0; background: linear-gradient(145deg, #fff, #ecfdf5); }
.pc-play-card.indigo { border-color: #c7d2fe; background: linear-gradient(145deg, #fff, #eef2ff); }
.pc-play-card.amber { border-color: #fde68a; background: linear-gradient(145deg, #fff, #fffbeb); }
.pc-play-card.cyan { border-color: #a5f3fc; background: linear-gradient(145deg, #fff, #ecfeff); }

.pc-play-tip {
  margin-top: 28px;
  font-size: 14px;
  color: var(--lc-subtle, #6b7280);
  line-height: 1.6;
}

.pc-play-tip a {
  color: var(--lc-blue, #2563eb);
}

@media (max-width: 980px) {
  .pc-play-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
