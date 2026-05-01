<template>
  <section class="howto platform-card" aria-labelledby="howto-title">
    <h3 id="howto-title" class="howto-title">第一次用后台？</h3>
    <ol class="howto-list">
      <li>在<strong>总览</strong>的「功能地图」里按分区找到要做的事，点卡片进入。</li>
      <li>左侧菜单与地图<strong>完全一致</strong>，习惯用哪边都可以。</li>
      <li>进入具体列表或表单后，可用顶栏<strong>返回</strong>回到上一页。</li>
      <li>若某页提示无权限，说明当前账号没有该模块，需联系超级管理员。</li>
    </ol>
  </section>
</template>

<style scoped>
.howto {
  padding: var(--lc-space-5);
  border: 1px solid var(--lc-border);
}

.howto-title {
  margin: 0 0 var(--lc-space-3);
  font-size: 16px;
  font-weight: 800;
  color: var(--lc-text);
}

.howto-list {
  margin: 0;
  padding-left: 1.25rem;
  font-size: 13px;
  line-height: 1.65;
  color: var(--lc-muted);
}

.howto-list li {
  margin-bottom: 10px;
}

.howto-list li:last-child {
  margin-bottom: 0;
}

.howto-list strong {
  color: var(--lc-text-deep);
  font-weight: 700;
}
</style>
