<template>
  <section class="platform-page fellowship-intro-page">
    <section class="intro-hero">
      <div class="intro-hero-bg-placeholder" />
      <div class="intro-hero-overlay">
        <p class="platform-kicker intro-kicker">Love Cube Fellowship</p>
        <h1 class="intro-title">
          遇见对的人<br />
          遇见更好的生活
        </h1>
        <p class="intro-subtitle">为五象新区青年打造的真实可靠联谊交友平台</p>
        <div class="platform-actions intro-actions">
          <router-link to="/fellowship" class="platform-btn platform-btn-primary intro-primary-btn">立即注册</router-link>
          <router-link to="/about" class="platform-btn platform-btn-ghost intro-ghost-btn">了解更多</router-link>
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
        <span class="intro-feature-icon">◻</span>
        <h3 class="intro-feature-title">完善资料</h3>
        <p class="intro-feature-text">填写真实资料，提升匹配成功率</p>
      </article>
      <article class="intro-feature-card">
        <span class="intro-feature-icon">◻</span>
        <h3 class="intro-feature-title">智能匹配</h3>
        <p class="intro-feature-text">算法推荐携手，遇见合适的 TA</p>
      </article>
      <article class="intro-feature-card">
        <span class="intro-feature-icon">◻</span>
        <h3 class="intro-feature-title">精彩活动</h3>
        <p class="intro-feature-text">丰富联谊活动，线上线下相结合</p>
      </article>
      <article class="intro-feature-card">
        <span class="intro-feature-icon">◻</span>
        <h3 class="intro-feature-title">安全沟通</h3>
        <p class="intro-feature-text">平台消息沟通，保护双方隐私</p>
      </article>
    </section>
  </section>
</template>

<style scoped>
.fellowship-intro-page {
  width: 100%;
  max-width: none;
  margin: 12px 0 26px;
  padding: 0 24px;
}

.intro-hero {
  position: relative;
  min-height: 520px;
  border-radius: 28px;
  overflow: hidden;
  border: 1px solid #f1d7e1;
  box-shadow: 0 20px 46px rgba(15, 23, 42, 0.1);
  background: #f9e5ed;
}

.intro-hero-bg-placeholder {
  position: absolute;
  inset: 0;
  background:
    linear-gradient(90deg, rgba(255, 255, 255, 0.86) 0%, rgba(255, 255, 255, 0.58) 34%, rgba(255, 255, 255, 0.1) 60%, rgba(255, 255, 255, 0) 75%),
    linear-gradient(0deg, rgba(255, 220, 232, 0.3) 0%, rgba(255, 220, 232, 0) 42%),
    url('@/assets/platform/fellowship-hero.png');
  background-position: left center, center, center;
  background-size: cover, cover, cover;
  background-repeat: no-repeat;
}

.intro-hero-overlay {
  position: relative;
  z-index: 2;
  padding: 44px 40px 54px;
  max-width: 560px;
}

.intro-kicker {
  color: #ff4f83;
  font-size: 12px;
  letter-spacing: 0.14em;
  font-weight: 700;
}

.intro-title {
  margin-top: 12px;
  font-size: clamp(56px, 4.8vw, 82px);
  line-height: 1.1;
  letter-spacing: -0.03em;
  color: #1f2538;
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
  font-weight: 800;
}

.intro-subtitle {
  margin-top: 14px;
  font-size: 18px;
  line-height: 1.5;
  color: #21293f;
  max-width: 24ch;
  letter-spacing: 0;
  font-weight: 600;
}

.intro-actions {
  margin-top: 28px;
}

.intro-primary-btn,
.intro-ghost-btn {
  min-width: 154px;
  height: 46px;
  border-radius: 999px;
  font-size: 16px;
  font-weight: 700;
}

.intro-primary-btn {
  box-shadow: 0 12px 22px rgba(255, 95, 132, 0.34);
}

.intro-ghost-btn {
  background: rgba(255, 255, 255, 0.95);
  border-color: #f5cad7;
  color: #364053;
}

.intro-slogan-side {
  position: absolute;
  z-index: 2;
  right: 54px;
  top: 60px;
  text-align: right;
  color: rgba(255, 255, 255, 0.95);
  font-size: 46px;
  font-family: 'KaiTi', 'STKaiti', serif;
  font-weight: 500;
  line-height: 1.3;
  letter-spacing: 0.03em;
  text-shadow: 0 7px 20px rgba(62, 26, 44, 0.25);
}

.intro-hero-badges {
  position: absolute;
  z-index: 2;
  left: 32px;
  right: 32px;
  bottom: 22px;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
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
  letter-spacing: -0.01em;
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

.intro-feature-icon {
  width: 34px;
  height: 34px;
  border-radius: 10px;
  display: grid;
  place-items: center;
  color: #ff4f83;
  background: #ffe8ef;
  border: 1px dashed #ff9cb8;
  font-size: 14px;
  font-weight: 700;
}

.intro-feature-title {
  margin-top: 8px;
  font-size: 28px;
  color: #1f2a44;
  font-weight: 800;
  letter-spacing: -0.01em;
}

.intro-feature-text {
  margin-top: 8px;
  font-size: 16px;
  color: #5d687f;
  line-height: 1.5;
}

@media (max-width: 1023px) {
  .fellowship-intro-page {
    padding: 0 16px;
  }

  .intro-hero {
    min-height: 460px;
  }

  .intro-hero-overlay {
    padding: 32px 24px 42px;
  }

  .intro-title {
    font-size: 54px;
  }

  .intro-subtitle {
    font-size: 17px;
  }

  .intro-slogan-side {
    right: 24px;
    top: 42px;
    font-size: 32px;
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
    padding: 0;
  }

  .intro-feature-card {
    box-shadow: 0 8px 22px rgba(15, 23, 42, 0.06);
  }
}

@media (max-width: 767px) {
  .fellowship-intro-page {
    margin-top: 8px;
    padding: 0 12px;
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
  }

  .intro-primary-btn,
  .intro-ghost-btn {
    min-width: 0;
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
