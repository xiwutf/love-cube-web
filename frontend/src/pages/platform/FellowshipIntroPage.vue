<template>
  <section class="fellowship-intro-page">
    <section class="intro-hero">
      <div class="intro-hero-bg" />
      <div class="intro-hero-overlay">
        <p class="intro-kicker">Love Cube Fellowship</p>
        <h1 class="intro-title">
          遇见对的人
          <br />
          遇见更好的生活
        </h1>
        <p class="intro-subtitle">
          为青年打造真实可靠的联谊交友平台，连接真实资料、互动体验与平台治理能力。
        </p>
        <div class="intro-actions">
          <router-link to="/fellowship" class="intro-btn intro-btn-primary">进入联谊模块</router-link>
          <router-link to="/fellowship/profile/edit" class="intro-btn intro-btn-ghost">完善资料</router-link>
          <router-link to="/policies/content-policy" class="intro-btn intro-btn-rule">了解规则</router-link>
        </div>
      </div>

      <aside class="intro-slogan-side">遇见美好<br />从 Love Cube 开始</aside>

      <div class="intro-hero-badges">
        <article class="intro-badge">
          <strong>真实认证</strong>
          <span>严格审核 安心可靠</span>
        </article>
        <article class="intro-badge">
          <strong>优质青年</strong>
          <span>高质量圈层 精准匹配</span>
        </article>
        <article class="intro-badge">
          <strong>安全隐私</strong>
          <span>多重保护 隐私安全</span>
        </article>
      </div>
    </section>

    <section class="intro-feature-section">
      <article class="intro-feature-card">
        <span class="intro-feature-index">01</span>
        <h3 class="intro-feature-title">完善资料</h3>
        <p class="intro-feature-text">填写真实资料，提升匹配成功率，让对的人更快看到你。</p>
      </article>
      <article class="intro-feature-card">
        <span class="intro-feature-index">02</span>
        <h3 class="intro-feature-title">智能匹配</h3>
        <p class="intro-feature-text">基于兴趣和活跃行为推荐，帮助你高效建立连接。</p>
      </article>
      <article class="intro-feature-card">
        <span class="intro-feature-index">03</span>
        <h3 class="intro-feature-title">私信互动</h3>
        <p class="intro-feature-text">支持站内私信沟通，消息提醒清晰，互动链路顺畅。</p>
      </article>
      <article class="intro-feature-card">
        <span class="intro-feature-index">04</span>
        <h3 class="intro-feature-title">举报治理</h3>
        <p class="intro-feature-text">违规行为可举报与拉黑，持续维护健康交友环境。</p>
      </article>
    </section>
  </section>
</template>

<style scoped>
.fellowship-intro-page {
  width: calc(100% - 48px);
  margin: 12px auto 30px;
}

.intro-hero {
  position: relative;
  min-height: 560px;
  border-radius: 30px;
  overflow: hidden;
  border: 1px solid #f1d7e1;
  box-shadow: 0 24px 52px rgba(15, 23, 42, 0.12);
  background: #f9e5ed;
}

.intro-hero-bg {
  position: absolute;
  inset: 0;
  background:
    linear-gradient(90deg, rgba(255, 255, 255, 0.9) 0%, rgba(255, 255, 255, 0.62) 34%, rgba(255, 255, 255, 0.12) 60%, rgba(255, 255, 255, 0) 75%),
    linear-gradient(0deg, rgba(255, 220, 232, 0.32) 0%, rgba(255, 220, 232, 0) 42%),
    url('@/assets/platform/fellowship-hero.png');
  background-position: left center, center, center;
  background-size: cover, cover, cover;
  background-repeat: no-repeat;
}

.intro-hero-overlay {
  position: relative;
  z-index: 2;
  padding: 52px 48px 58px;
  max-width: 640px;
}

.intro-kicker {
  margin: 0;
  color: #ff4f83;
  font-size: 12px;
  letter-spacing: 0.14em;
  font-weight: 700;
  text-transform: uppercase;
}

.intro-title {
  margin-top: 14px;
  font-size: clamp(58px, 5vw, 84px);
  line-height: 1.08;
  letter-spacing: 0;
  color: #1f2538;
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
  font-weight: 900;
}

.intro-subtitle {
  margin-top: 16px;
  font-size: 19px;
  line-height: 1.65;
  color: #21293f;
  max-width: 28ch;
  font-weight: 600;
}

.intro-actions {
  margin-top: 30px;
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.intro-btn {
  min-height: 46px;
  padding: 0 20px;
  border-radius: 999px;
  font-size: 15px;
  font-weight: 700;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 1px solid transparent;
}

.intro-btn-primary {
  color: #fff;
  background: linear-gradient(135deg, #ff5f84, #e84f73);
  box-shadow: 0 12px 24px rgba(255, 95, 132, 0.34);
}

.intro-btn-ghost {
  background: rgba(255, 255, 255, 0.96);
  border-color: #f3c9d6;
  color: #364053;
}

.intro-btn-rule {
  background: rgba(37, 99, 235, 0.92);
  color: #fff;
}

.intro-slogan-side {
  position: absolute;
  z-index: 2;
  right: 56px;
  top: 64px;
  text-align: right;
  color: rgba(255, 255, 255, 0.96);
  font-size: 48px;
  font-family: 'KaiTi', 'STKaiti', serif;
  font-weight: 500;
  line-height: 1.32;
  letter-spacing: 0.03em;
  text-shadow: 0 7px 20px rgba(62, 26, 44, 0.25);
}

.intro-hero-badges {
  position: absolute;
  z-index: 2;
  left: 32px;
  right: 32px;
  bottom: 24px;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.intro-badge {
  background: rgba(255, 255, 255, 0.92);
  border-radius: 14px;
  border: 1px solid #f4dbe4;
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.08);
  padding: 14px 16px;
}

.intro-badge strong {
  display: block;
  font-size: 24px;
  color: #1f2a44;
}

.intro-badge span {
  display: block;
  margin-top: 6px;
  font-size: 14px;
  color: #5f6a80;
}

.intro-feature-section {
  margin-top: 18px;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.intro-feature-card {
  background: #fff;
  border: 1px solid #f2d7e1;
  border-radius: 16px;
  box-shadow: 0 10px 26px rgba(15, 23, 42, 0.07);
  padding: 18px 16px 20px;
}

.intro-feature-index {
  width: 34px;
  height: 34px;
  border-radius: 10px;
  display: grid;
  place-items: center;
  color: #ff4f83;
  background: #ffe8ef;
  font-size: 13px;
  font-weight: 800;
}

.intro-feature-title {
  margin-top: 8px;
  font-size: 28px;
  color: #1f2a44;
  font-weight: 800;
  letter-spacing: 0;
}

.intro-feature-text {
  margin-top: 8px;
  font-size: 16px;
  color: #5d687f;
  line-height: 1.5;
}

@media (max-width: 1023px) {
  .fellowship-intro-page {
    width: calc(100% - 32px);
  }

  .intro-hero {
    min-height: 480px;
  }

  .intro-hero-overlay {
    padding: 34px 24px 42px;
  }

  .intro-title {
    font-size: clamp(44px, 8vw, 60px);
  }

  .intro-subtitle {
    font-size: 17px;
  }

  .intro-slogan-side {
    right: 24px;
    top: 42px;
    font-size: 34px;
  }

  .intro-hero-badges {
    left: 16px;
    right: 16px;
    bottom: 14px;
  }

  .intro-badge strong {
    font-size: 18px;
  }

  .intro-badge span {
    font-size: 12px;
  }

  .intro-feature-section {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    margin-top: 14px;
  }
}

@media (max-width: 767px) {
  .fellowship-intro-page {
    width: calc(100% - 24px);
    margin-top: 8px;
  }

  .intro-hero {
    min-height: 420px;
    border-radius: 18px;
  }

  .intro-hero-overlay {
    padding: 24px 16px 24px;
    max-width: 100%;
  }

  .intro-title {
    font-size: 38px;
    line-height: 1.1;
  }

  .intro-subtitle {
    margin-top: 10px;
    font-size: 15px;
    max-width: 100%;
  }

  .intro-slogan-side {
    right: 16px;
    top: 16px;
    font-size: 22px;
    opacity: 0.85;
  }

  .intro-hero-badges {
    grid-template-columns: 1fr;
    bottom: 12px;
    gap: 8px;
  }

  .intro-badge {
    padding: 10px 12px;
  }

  .intro-actions {
    margin-top: 20px;
    flex-direction: column;
  }

  .intro-btn {
    width: 100%;
  }

  .intro-feature-section {
    grid-template-columns: 1fr;
    gap: 10px;
  }

  .intro-feature-card {
    border-radius: 14px;
    padding: 14px;
  }

  .intro-feature-title {
    font-size: 20px;
  }

  .intro-feature-text {
    margin-top: 6px;
    font-size: 14px;
  }
}
</style>
