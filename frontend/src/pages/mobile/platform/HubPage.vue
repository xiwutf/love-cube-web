<template>
  <div class="m-hub">
    <header class="m-hub-head">
      <h1>成长玩法</h1>
      <p>签到、任务、心声与互助，移动端一站直达</p>
    </header>

    <section class="m-hub-grid">
      <router-link
        v-for="item in items"
        :key="item.to"
        :to="item.to"
        class="m-hub-card"
        :class="item.tone"
      >
        <span class="m-hub-icon" aria-hidden="true">{{ item.icon }}</span>
        <span class="m-hub-title">{{ item.title }}</span>
        <span class="m-hub-sub">{{ item.sub }}</span>
      </router-link>
    </section>

    <section class="m-hub-tip">
      <p>联谊匹配、VIP、活动签到请从底部 Tab「认识 / 我的」进入。</p>
    </section>
  </div>
</template>

<script setup>
const items = [
  { to: '/m/platform/tasks', icon: '✓', title: '今日任务', sub: '新人包 · 每周挑战', tone: 'violet' },
  { to: '/m/platform/checkin', icon: '☀', title: '每日签到', sub: '连续登录 streak', tone: 'orange' },
  { to: '/m/platform/positive-share', icon: '♡', title: '每日心声', sub: '话题 · 周榜', tone: 'pink' },
  { to: '/m/platform/help', icon: '⇄', title: '互助广场', sub: '达人榜 · 发需求', tone: 'blue' },
  { to: '/m/platform/groups', icon: '◎', title: '团体广场', sub: '发现 · 加入小组', tone: 'green' },
  { to: '/m/platform/local', icon: '⌂', title: '本地服务', sub: '招聘二手租房', tone: 'amber' },
  { to: '/m/platform/topics', icon: '💬', title: '话题广场', sub: '兴趣讨论', tone: 'cyan' },
  { to: '/m/platform/messages', icon: '✉', title: '消息中心', sub: '通知与互动', tone: 'green' },
  { to: '/m/fellowship/ai-tools', icon: '✦', title: 'AI 助手', sub: '润色 · 话术', tone: 'violet' },
  { to: '/m/platform/member', icon: '★', title: '平台会员', sub: '权益升级', tone: 'indigo' },
  { to: '/m/fellowship/invite', icon: '◎', title: '邀请好友', sub: '有效邀请奖励', tone: 'green' },
  { to: '/m/fellowship/event-signups', icon: '▣', title: '活动报名', sub: '签到 · 互评', tone: 'indigo' }
]
</script>

<style scoped>
.m-hub {
  min-height: 100vh;
  padding: 20px 16px 32px;
  background: linear-gradient(180deg, #f8f9ff 0%, #f4f5fb 180px, #f4f5fb 100%);
}

.m-hub-head h1 {
  margin: 0;
  font-size: 22px;
  font-weight: 800;
  color: var(--lc-text, #1a2236);
}

.m-hub-head p {
  margin: 6px 0 0;
  font-size: 13px;
  color: var(--lc-subtle, #6b7280);
  line-height: 1.5;
}

.m-hub-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-top: 20px;
}

.m-hub-card {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 16px 14px;
  border-radius: 16px;
  text-decoration: none;
  background: var(--lc-surface, #fff);
  border: 1px solid var(--lc-soft, #e8ecf4);
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.04);
  min-height: 108px;
}

.m-hub-icon {
  font-size: 22px;
  line-height: 1;
}

.m-hub-title {
  font-size: 15px;
  font-weight: 700;
  color: var(--lc-text, #1a2236);
}

.m-hub-sub {
  font-size: 11px;
  color: var(--lc-subtle, #8898aa);
  line-height: 1.4;
}

.m-hub-card.violet { border-color: #ddd6fe; background: linear-gradient(145deg, #fff, #f5f3ff); }
.m-hub-card.orange { border-color: #fed7aa; background: linear-gradient(145deg, #fff, #fff7ed); }
.m-hub-card.pink { border-color: #fbcfe8; background: linear-gradient(145deg, #fff, #fff1f5); }
.m-hub-card.blue { border-color: #bfdbfe; background: linear-gradient(145deg, #fff, #eff6ff); }
.m-hub-card.green { border-color: #bbf7d0; background: linear-gradient(145deg, #fff, #ecfdf5); }
.m-hub-card.indigo { border-color: #c7d2fe; background: linear-gradient(145deg, #fff, #eef2ff); }
.m-hub-card.amber { border-color: #fde68a; background: linear-gradient(145deg, #fff, #fffbeb); }
.m-hub-card.cyan { border-color: #a5f3fc; background: linear-gradient(145deg, #fff, #ecfeff); }

.m-hub-tip {
  margin-top: 20px;
  padding: 12px 14px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.8);
  border: 1px dashed var(--lc-soft, #d8dee9);
}

.m-hub-tip p {
  margin: 0;
  font-size: 12px;
  color: var(--lc-subtle, #6b7280);
  line-height: 1.55;
}

@media (max-width: 420px) {
  .m-hub-grid {
    gap: 10px;
  }

  .m-hub-card {
    min-height: 100px;
    padding: 14px 12px;
  }
}
</style>
