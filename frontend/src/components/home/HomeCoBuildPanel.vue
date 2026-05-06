<template>
  <section class="cobuild-panel">
    <header>
      <p>共建平台</p>
      <h2>说出你的梦想功能。只要合法合规、有价值，我会持续迭代这个平台。</h2>
    </header>

    <div class="cobuild-actions">
      <router-link to="/feedback">提交建议</router-link>
      <router-link to="/modules">查看规划</router-link>
    </div>

    <div class="cobuild-steps">
      <span><b>1</b>提交建议</span>
      <span><b>2</b>反馈问题</span>
      <span><b>3</b>功能投票</span>
      <span><b>4</b>加入共建</span>
    </div>
  </section>
</template>

<style scoped>
.cobuild-panel {
  display: grid;
  gap: var(--lc-space-6);
  padding: var(--lc-space-8);
  border: 1px solid var(--lc-border);
  border-radius: var(--lc-radius);
  background: var(--lc-surface);
  box-shadow: var(--lc-shadow-sm);
}

.cobuild-panel header p {
  margin: 0;
  color: var(--lc-blue);
  font-size: var(--lc-text-xl);
  font-weight: 950;
}

.cobuild-panel h2 {
  max-width: 760px;
  margin: var(--lc-space-3) 0 0;
  color: var(--lc-text);
  font-size: clamp(24px, 2vw, 32px);
  line-height: 1.45;
}

.cobuild-actions,
.cobuild-steps {
  display: flex;
  flex-wrap: wrap;
  gap: var(--lc-space-3);
}

.cobuild-actions a {
  display: inline-flex;
  align-items: center;
  min-height: 42px;
  padding: 0 var(--lc-space-5);
  border-radius: 999px;
  color: var(--lc-blue-dark);
  background: var(--lc-blue-light);
  font-weight: 950;
  text-decoration: none;
}

.cobuild-actions a:first-child {
  color: var(--lc-surface);
  background: linear-gradient(135deg, var(--lc-blue), #7c3aed);
}

.cobuild-steps span {
  display: inline-flex;
  align-items: center;
  gap: var(--lc-space-2);
  color: var(--lc-muted);
  font-weight: 800;
}

.cobuild-steps b {
  display: grid;
  place-items: center;
  width: 28px;
  height: 28px;
  border-radius: 8px;
  color: var(--lc-surface);
  background: var(--lc-blue);
}

@media (max-width: 680px) {
  .cobuild-panel {
    padding: var(--lc-space-5);
  }
}
</style>
