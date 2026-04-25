<template>
  <section class="platform-page">
    <section class="platform-card platform-soft-card">
      <p class="platform-kicker">About Platform</p>
      <h1 class="platform-title">关于我们</h1>
      <p class="platform-subtitle">Love Cube 采用“平台官网 + 联谊子应用”双层架构，兼顾信息展示与移动端互动体验。</p>
    </section>

    <div class="platform-grid-3">
      <article class="platform-card">
        <h3 class="platform-heading">平台定位</h3>
        <p class="platform-text">平台官网负责公告、资讯与活动发布，为用户提供可信入口与规则说明。</p>
      </article>
      <article class="platform-card">
        <h3 class="platform-heading">联谊模块</h3>
        <p class="platform-text">联谊功能统一在 <code>/fellowship/*</code>，保留原移动端 UI，不影响官网视觉语言。</p>
      </article>
      <article class="platform-card">
        <h3 class="platform-heading">运营原则</h3>
        <p class="platform-text">坚持真实、尊重、安全，强调双向同意沟通与用户隐私保护。</p>
      </article>
    </div>

    <div class="platform-actions">
      <router-link to="/fellowship-intro" class="platform-btn platform-btn-primary">先看联谊介绍，再进入模块</router-link>
    </div>
  </section>
</template>

<style scoped>
code {
  background: #f1f5f9;
  padding: 2px 6px;
  border-radius: 6px;
}
</style>
