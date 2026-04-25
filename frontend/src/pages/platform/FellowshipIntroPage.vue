<template>
  <section class="platform-page fellowship-intro-page">
    <section class="intro-hero">
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
    </section>

    <section class="intro-feature-section">
      <article class="intro-feature-card">
        <h3 class="intro-feature-title">完善资料</h3>
        <p class="intro-feature-text">填写真实资料，提升匹配成功率</p>
      </article>
      <article class="intro-feature-card">
        <h3 class="intro-feature-title">智能匹配</h3>
        <p class="intro-feature-text">算法推荐携手，遇见合适的 TA</p>
      </article>
      <article class="intro-feature-card">
        <h3 class="intro-feature-title">精彩活动</h3>
        <p class="intro-feature-text">丰富联谊活动，线上线下相结合</p>
      </article>
      <article class="intro-feature-card">
        <h3 class="intro-feature-title">安全沟通</h3>
        <p class="intro-feature-text">平台消息沟通，保护双方隐私</p>
      </article>
    </section>
  </section>
</template>

<style scoped>
.fellowship-intro-page {
  margin-top: 20px;
}

.intro-hero {
  position: relative;
  min-height: 450px;
  border-radius: 28px;
  overflow: hidden;
  border: 1px solid #f6d5de;
  box-shadow: 0 20px 46px rgba(15, 23, 42, 0.1);
  background:
    linear-gradient(96deg, rgba(255, 255, 255, 0.92) 0%, rgba(255, 255, 255, 0.3) 46%, rgba(255, 255, 255, 0.08) 100%),
    radial-gradient(circle at 75% 28%, rgba(255, 196, 214, 0.72) 0%, rgba(255, 196, 214, 0) 52%),
    linear-gradient(135deg, #fef4f7 0%, #f7d8e4 45%, #f0bfd4 100%);
}

.intro-hero-overlay {
  padding: 54px 54px 60px;
  max-width: 620px;
}

.intro-kicker {
  color: #ff4f83;
}

.intro-title {
  margin-top: 12px;
  font-size: clamp(42px, 4.8vw, 68px);
  line-height: 1.12;
  letter-spacing: -0.02em;
  color: #252734;
}

.intro-subtitle {
  margin-top: 16px;
  font-size: 30px;
  line-height: 1.5;
  color: #2c3342;
  max-width: 32ch;
}

.intro-actions {
  margin-top: 30px;
}

.intro-primary-btn,
.intro-ghost-btn {
  min-width: 138px;
}

.intro-feature-section {
  margin-top: -34px;
  position: relative;
  z-index: 2;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
  padding: 0 14px 10px;
}

.intro-feature-card {
  background: #fff;
  border: 1px solid #f2d7e1;
  border-radius: 16px;
  box-shadow: 0 10px 26px rgba(15, 23, 42, 0.07);
  padding: 18px 16px;
}

.intro-feature-title {
  font-size: 22px;
  color: #1f2a44;
  font-weight: 700;
}

.intro-feature-text {
  margin-top: 10px;
  font-size: 16px;
  color: #5c677d;
  line-height: 1.6;
}

@media (max-width: 1023px) {
  .intro-hero {
    min-height: 390px;
  }

  .intro-hero-overlay {
    padding: 34px 24px 42px;
  }

  .intro-title {
    font-size: 48px;
  }

  .intro-subtitle {
    font-size: 22px;
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
  }

  .intro-hero {
    min-height: 320px;
    border-radius: 18px;
  }

  .intro-hero-overlay {
    padding: 24px 16px 24px;
    max-width: 100%;
  }

  .intro-title {
    font-size: 34px;
  }

  .intro-subtitle {
    margin-top: 10px;
    font-size: 16px;
    max-width: 100%;
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
    font-size: 18px;
  }

  .intro-feature-text {
    margin-top: 6px;
    font-size: 14px;
  }
}
</style>
