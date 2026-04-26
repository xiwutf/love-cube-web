<template>
  <section class="fi-page">
    <!-- Hero -->
    <section class="fi-hero">
      <div class="fi-hero-bg" />
      <div class="fi-hero-deco fi-hero-deco-1" />
      <div class="fi-hero-deco fi-hero-deco-2" />
      <div class="fi-hero-deco fi-hero-deco-3" />

      <div class="fi-hero-body">
        <p class="fi-kicker">Love Cube Fellowship</p>
        <h1 class="fi-title">
          遇见对的人<br />
          遇见更好的生活
        </h1>
        <p class="fi-subtitle">为五象新区青年打造的真实可靠联谊交友平台</p>
        <div class="fi-actions">
          <router-link to="/fellowship" class="platform-btn fi-btn-primary">立即注册 / 登录</router-link>
          <router-link to="/about" class="platform-btn fi-btn-ghost">了解更多</router-link>
        </div>
      </div>

      <aside class="fi-slogan">
        <span>遇见美好</span>
        <span>从 Love Cube 开始</span>
      </aside>

      <div class="fi-badges">
        <article class="fi-badge">
          <div class="fi-badge-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 12l2 2 4-4M7.835 4.697a3.42 3.42 0 001.946-.806 3.42 3.42 0 014.438 0 3.42 3.42 0 001.946.806 3.42 3.42 0 013.138 3.138 3.42 3.42 0 00.806 1.946 3.42 3.42 0 010 4.438 3.42 3.42 0 00-.806 1.946 3.42 3.42 0 01-3.138 3.138 3.42 3.42 0 00-1.946.806 3.42 3.42 0 01-4.438 0 3.42 3.42 0 00-1.946-.806 3.42 3.42 0 01-3.138-3.138 3.42 3.42 0 00-.806-1.946 3.42 3.42 0 010-4.438 3.42 3.42 0 00.806-1.946 3.42 3.42 0 013.138-3.138z"/></svg>
          </div>
          <strong>真实认证</strong>
          <span>严格审核 安心可靠</span>
        </article>
        <article class="fi-badge">
          <div class="fi-badge-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z"/></svg>
          </div>
          <strong>优质青年</strong>
          <span>高质量圈层 精准匹配</span>
        </article>
        <article class="fi-badge">
          <div class="fi-badge-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"/></svg>
          </div>
          <strong>安全隐私</strong>
          <span>多重保护 隐私安全</span>
        </article>
      </div>
    </section>

    <!-- Features -->
    <section class="fi-features">
      <header class="fi-features-head">
        <p class="platform-kicker">平台功能</p>
        <h2 class="fi-section-title">简单四步，开启缘分之旅</h2>
      </header>

      <div class="fi-feature-grid">
        <article class="fi-feature-card" style="--accent: #ff5f84; --accent-bg: #fff0f4;">
          <div class="fi-feature-num">01</div>
          <div class="fi-feature-icon-wrap">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
              <path d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/>
            </svg>
          </div>
          <h3 class="fi-feature-title">完善资料</h3>
          <p class="fi-feature-text">填写真实资料，提升匹配成功率，让对的人看见真实的你</p>
        </article>

        <article class="fi-feature-card" style="--accent: #a855f7; --accent-bg: #f8f0ff;">
          <div class="fi-feature-num">02</div>
          <div class="fi-feature-icon-wrap">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
              <path d="M13 10V3L4 14h7v7l9-11h-7z"/>
            </svg>
          </div>
          <h3 class="fi-feature-title">智能匹配</h3>
          <p class="fi-feature-text">算法推荐携手，基于兴趣、性格精准推送，遇见合适的 TA</p>
        </article>

        <article class="fi-feature-card" style="--accent: #0ea5e9; --accent-bg: #f0f9ff;">
          <div class="fi-feature-num">03</div>
          <div class="fi-feature-icon-wrap">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
              <path d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"/>
            </svg>
          </div>
          <h3 class="fi-feature-title">精彩活动</h3>
          <p class="fi-feature-text">丰富线上线下联谊活动，让每一次相遇都成为故事的开始</p>
        </article>

        <article class="fi-feature-card" style="--accent: #10b981; --accent-bg: #f0fdf8;">
          <div class="fi-feature-num">04</div>
          <div class="fi-feature-icon-wrap">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
              <path d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"/>
            </svg>
          </div>
          <h3 class="fi-feature-title">安全沟通</h3>
          <p class="fi-feature-text">平台内消息沟通，多重隐私保护，让你放心交流，安心相识</p>
        </article>
      </div>
    </section>
  </section>
</template>

<style scoped>
/* ── Page wrapper ── */
.fi-page {
  width: 100%;
  max-width: none;
  margin: 16px 0 40px;
  padding: 0 28px;
}

/* ── Hero ── */
.fi-hero {
  position: relative;
  min-height: 560px;
  border-radius: 32px;
  overflow: hidden;
  box-shadow: 0 30px 70px rgba(15, 23, 42, 0.14);
}

.fi-hero-bg {
  position: absolute;
  inset: 0;
  background:
    linear-gradient(105deg, rgba(10, 8, 22, 0.82) 0%, rgba(10, 8, 22, 0.55) 38%, rgba(10, 8, 22, 0.15) 60%, transparent 75%),
    linear-gradient(180deg, rgba(255, 95, 132, 0.08) 0%, rgba(168, 85, 247, 0.12) 100%),
    url('@/assets/platform/fellowship-hero.png') center / cover no-repeat;
}

/* Decorative blobs */
.fi-hero-deco {
  position: absolute;
  border-radius: 50%;
  filter: blur(60px);
  pointer-events: none;
}
.fi-hero-deco-1 {
  width: 340px; height: 340px;
  right: -60px; top: -80px;
  background: rgba(255, 95, 132, 0.28);
}
.fi-hero-deco-2 {
  width: 220px; height: 220px;
  right: 180px; bottom: 40px;
  background: rgba(168, 85, 247, 0.2);
}
.fi-hero-deco-3 {
  width: 160px; height: 160px;
  left: 380px; top: 60px;
  background: rgba(14, 165, 233, 0.15);
}

/* Hero content */
.fi-hero-body {
  position: relative;
  z-index: 4;
  padding: 52px 48px 170px;
  max-width: 600px;
}

.fi-kicker {
  margin: 0;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.2em;
  text-transform: uppercase;
  color: rgba(255, 160, 185, 0.9);
}

.fi-title {
  margin-top: 16px;
  font-size: clamp(52px, 4.6vw, 80px);
  line-height: 1.08;
  letter-spacing: -0.03em;
  font-weight: 900;
  color: #fff;
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
  text-shadow: 0 4px 24px rgba(0, 0, 0, 0.3);
}

.fi-subtitle {
  margin-top: 18px;
  font-size: 17px;
  line-height: 1.6;
  color: rgba(255, 255, 255, 0.72);
  max-width: 26ch;
  font-weight: 400;
  letter-spacing: 0.01em;
}

.fi-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 32px;
}

.fi-btn-primary {
  background: linear-gradient(135deg, #ff5f84, #e84f73);
  color: #fff;
  font-size: 15px;
  font-weight: 700;
  padding: 13px 30px;
  border-radius: 999px;
  box-shadow: 0 14px 32px rgba(255, 80, 120, 0.4);
  border: none;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}
.fi-btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 20px 40px rgba(255, 80, 120, 0.5);
}

.fi-btn-ghost {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.28);
  color: rgba(255, 255, 255, 0.9);
  font-size: 15px;
  font-weight: 600;
  padding: 13px 28px;
  border-radius: 999px;
  backdrop-filter: blur(8px);
  transition: background 0.2s ease, border-color 0.2s ease, transform 0.2s ease;
}
.fi-btn-ghost:hover {
  background: rgba(255, 255, 255, 0.18);
  border-color: rgba(255, 255, 255, 0.45);
  transform: translateY(-2px);
}

/* Slogan side */
.fi-slogan {
  position: absolute;
  z-index: 3;
  right: 56px;
  top: 64px;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
  color: rgba(255, 255, 255, 0.88);
  font-family: 'KaiTi', 'STKaiti', serif;
  font-size: 44px;
  font-weight: 500;
  line-height: 1.4;
  letter-spacing: 0.08em;
  text-shadow: 0 6px 22px rgba(0, 0, 0, 0.35);
}
.fi-slogan span {
  display: block;
}

/* Badges */
.fi-badges {
  position: absolute;
  z-index: 5;
  left: 32px;
  right: 32px;
  bottom: 26px;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.fi-badge {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(16px) saturate(180%);
  -webkit-backdrop-filter: blur(16px) saturate(180%);
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: 18px;
  padding: 16px 18px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  transition: background 0.2s;
}
.fi-badge:hover {
  background: rgba(255, 255, 255, 0.16);
}

.fi-badge-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: rgba(255, 95, 132, 0.3);
  display: grid;
  place-items: center;
  margin-bottom: 6px;
}
.fi-badge-icon svg {
  width: 16px;
  height: 16px;
  stroke: rgba(255, 200, 215, 0.95);
}

.fi-badge strong {
  display: block;
  font-size: 20px;
  color: #fff;
  font-weight: 700;
  letter-spacing: -0.01em;
}

.fi-badge span {
  display: block;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.6);
  font-weight: 400;
}

/* ── Features ── */
.fi-features {
  margin-top: 24px;
}

.fi-features-head {
  margin-bottom: 16px;
}

.fi-section-title {
  margin-top: 8px;
  font-size: clamp(22px, 2vw, 30px);
  font-weight: 800;
  color: #1a2236;
  letter-spacing: -0.02em;
}

.fi-feature-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.fi-feature-card {
  position: relative;
  background: #fff;
  border: 1px solid #eef0f6;
  border-radius: 22px;
  padding: 24px 22px 26px;
  overflow: hidden;
  box-shadow: 0 8px 28px rgba(15, 23, 42, 0.06);
  transition: transform 0.25s ease, box-shadow 0.25s ease, border-color 0.25s ease;
}
.fi-feature-card::before {
  content: '';
  position: absolute;
  inset: 0;
  background: var(--accent-bg);
  opacity: 0;
  transition: opacity 0.25s ease;
}
.fi-feature-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 20px 48px rgba(15, 23, 42, 0.1);
  border-color: color-mix(in srgb, var(--accent) 22%, transparent);
}
.fi-feature-card:hover::before {
  opacity: 1;
}

.fi-feature-num {
  position: absolute;
  top: 18px;
  right: 20px;
  font-size: 48px;
  font-weight: 900;
  color: color-mix(in srgb, var(--accent) 8%, transparent);
  line-height: 1;
  letter-spacing: -0.04em;
  pointer-events: none;
  transition: color 0.25s;
}
.fi-feature-card:hover .fi-feature-num {
  color: color-mix(in srgb, var(--accent) 14%, transparent);
}

.fi-feature-icon-wrap {
  position: relative;
  z-index: 1;
  width: 48px;
  height: 48px;
  border-radius: 14px;
  background: var(--accent-bg);
  border: 1px solid color-mix(in srgb, var(--accent) 18%, transparent);
  display: grid;
  place-items: center;
  transition: background 0.25s, transform 0.25s;
}
.fi-feature-card:hover .fi-feature-icon-wrap {
  transform: scale(1.06);
  background: color-mix(in srgb, var(--accent) 14%, white);
}
.fi-feature-icon-wrap svg {
  width: 22px;
  height: 22px;
  stroke: var(--accent);
}

.fi-feature-title {
  position: relative;
  z-index: 1;
  margin-top: 14px;
  font-size: 22px;
  color: #18243a;
  font-weight: 800;
  letter-spacing: -0.01em;
}

.fi-feature-text {
  position: relative;
  z-index: 1;
  margin-top: 8px;
  font-size: 14px;
  color: #64748b;
  line-height: 1.65;
}

/* ── Responsive ── */
@media (max-width: 1199px) {
  .fi-page {
    padding: 0 20px;
  }
  .fi-feature-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
  .fi-slogan {
    font-size: 36px;
    right: 36px;
    top: 52px;
  }
}

@media (max-width: 1023px) {
  .fi-hero {
    min-height: 500px;
  }
  .fi-hero-body {
    padding: 38px 32px 160px;
  }
  .fi-title {
    font-size: 52px;
  }
  .fi-slogan {
    font-size: 28px;
    right: 28px;
    top: 40px;
  }
  .fi-badges {
    left: 24px;
    right: 24px;
    bottom: 18px;
  }
}

@media (max-width: 767px) {
  .fi-page {
    padding: 0 12px;
    margin: 8px 0 28px;
  }
  .fi-hero {
    min-height: 460px;
    border-radius: 22px;
  }
  .fi-hero-body {
    padding: 28px 20px 148px;
    max-width: 100%;
  }
  .fi-title {
    font-size: 40px;
  }
  .fi-subtitle {
    font-size: 15px;
    max-width: 100%;
  }
  .fi-slogan {
    font-size: 22px;
    right: 18px;
    top: 20px;
    opacity: 0.8;
  }
  .fi-badges {
    grid-template-columns: 1fr;
    left: 14px;
    right: 14px;
    bottom: 14px;
    gap: 8px;
  }
  .fi-badge {
    padding: 12px 14px;
    border-radius: 14px;
    flex-direction: row;
    align-items: center;
    gap: 12px;
  }
  .fi-badge-icon {
    margin-bottom: 0;
    flex-shrink: 0;
  }
  .fi-badge strong {
    font-size: 16px;
  }
  .fi-badge span {
    font-size: 12px;
  }
  .fi-actions {
    flex-direction: column;
  }
  .fi-btn-primary,
  .fi-btn-ghost {
    width: 100%;
    justify-content: center;
  }
  .fi-feature-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }
  .fi-feature-card {
    border-radius: 16px;
    padding: 20px 18px 22px;
  }
  .fi-feature-title {
    font-size: 18px;
  }
}
</style>
