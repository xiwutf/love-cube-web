<template>
  <section class="growth-record-page">
    <div class="record-shell">
      <header class="page-head">
        <div>
          <p class="crumb">个人中心 / 成长记录</p>
          <h1>成长记录</h1>
          <p class="desc">查看等级进度、经验来源和最近达成的成长里程碑。</p>
        </div>
        <router-link class="back-link" to="/pc/platform/me">返回个人中心</router-link>
      </header>

      <div class="summary-grid">
        <section class="card level-card">
          <div class="level-top">
            <span class="level-badge">Lv.2</span>
            <div class="level-meta">
              <strong>新手创作者</strong>
              <span>再获得 180 经验升级到 Lv.3</span>
            </div>
          </div>
          <div class="progress-track">
            <span style="width: 40%"></span>
          </div>
          <p class="progress-text">120 / 300</p>
        </section>

        <section class="card stats-card">
          <div>
            <span>今日获得</span>
            <strong>+7</strong>
          </div>
          <div>
            <span>本周获得</span>
            <strong>+36</strong>
          </div>
          <div>
            <span>累计经验</span>
            <strong>120</strong>
          </div>
        </section>
      </div>

      <div class="content-grid">
        <section class="card">
          <header class="card-head">
            <h2>经验来源</h2>
          </header>
          <ul class="source-list">
            <li>
              <span>发布内容</span>
              <strong>+10 经验</strong>
            </li>
            <li>
              <span>每日心声</span>
              <strong>+5 经验</strong>
            </li>
            <li>
              <span>创建团体</span>
              <strong>+20 经验</strong>
            </li>
            <li>
              <span>获得点赞</span>
              <strong>+2 经验</strong>
            </li>
          </ul>
        </section>

        <section class="card">
          <header class="card-head">
            <h2>最近成长动态</h2>
          </header>
          <ul class="timeline">
            <li>
              <span>今天</span>
              <p>完成任务「发布每日心声」，获得 +5 经验。</p>
            </li>
            <li>
              <span>昨天</span>
              <p>获得 1 次点赞，获得 +2 经验。</p>
            </li>
            <li>
              <span>2 天前</span>
              <p>发布新内容，获得 +10 经验。</p>
            </li>
          </ul>
        </section>
      </div>
    </div>
  </section>
</template>

<style scoped>
.growth-record-page {
  background: #f6f7fb;
  min-height: calc(100vh - 60px);
}

.record-shell {
  width: min(100% - 64px, 1720px);
  margin: 0 auto;
  padding: 20px 0 28px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
}

.crumb {
  margin: 0;
  font-size: 12px;
  color: #64748b;
}

.page-head h1 {
  margin: 4px 0 0;
  font-size: 30px;
  line-height: 1.2;
}

.desc {
  margin: 8px 0 0;
  color: #64748b;
  font-size: 14px;
}

.back-link {
  text-decoration: none;
  color: #4f46e5;
  font-weight: 700;
  font-size: 14px;
}

.summary-grid {
  display: grid;
  grid-template-columns: 1.2fr 1fr;
  gap: 16px;
}

.content-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.card {
  background: #fff;
  border: 1px solid #e7ebf2;
  border-radius: 12px;
  padding: 18px 20px;
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.055);
}

.level-top {
  display: flex;
  align-items: center;
  gap: 14px;
}

.level-badge {
  display: inline-grid;
  place-items: center;
  width: 74px;
  height: 74px;
  clip-path: polygon(25% 5%, 75% 5%, 100% 50%, 75% 95%, 25% 95%, 0 50%);
  background: linear-gradient(135deg, #4f46e5, #7c3aed);
  color: #fff;
  font-weight: 900;
}

.level-meta strong {
  display: block;
  font-size: 18px;
}

.level-meta span {
  display: block;
  margin-top: 4px;
  color: #64748b;
  font-size: 13px;
}

.progress-track {
  margin-top: 14px;
  height: 8px;
  border-radius: 999px;
  background: #e5e7eb;
  overflow: hidden;
}

.progress-track span {
  display: block;
  height: 100%;
  background: linear-gradient(90deg, #6d5dfb, #4f46e5);
}

.progress-text {
  margin: 8px 0 0;
  color: #64748b;
  font-size: 13px;
  font-weight: 700;
}

.stats-card {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.stats-card div {
  border: 1px solid #eef2f7;
  border-radius: 10px;
  padding: 12px;
  text-align: center;
}

.stats-card span {
  color: #64748b;
  font-size: 12px;
}

.stats-card strong {
  display: block;
  margin-top: 6px;
  font-size: 24px;
  color: #0f172a;
}

.card-head h2 {
  margin: 0 0 12px;
  font-size: 16px;
}

.source-list,
.timeline {
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  gap: 10px;
}

.source-list li {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border: 1px solid #eef2f7;
  border-radius: 10px;
  padding: 10px 12px;
}

.source-list span {
  color: #334155;
}

.source-list strong {
  color: #4f46e5;
}

.timeline li {
  border-left: 3px solid #c7d2fe;
  padding: 4px 0 4px 10px;
}

.timeline span {
  color: #64748b;
  font-size: 12px;
}

.timeline p {
  margin: 4px 0 0;
  color: #0f172a;
}

@media (max-width: 980px) {
  .record-shell {
    width: min(100% - 32px, 980px);
  }

  .summary-grid,
  .content-grid {
    grid-template-columns: 1fr;
  }
}
</style>
