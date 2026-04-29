<template>
  <section class="hero-portal">
    <div class="hero-copy">
      <p class="portal-kicker">LOVE CUBE PLATFORM</p>
      <h1>一个持续进化的多功能连接平台</h1>
      <p class="hero-subtitle">聚合内容资讯、活动服务、社交连接、本地服务与 AI 工具能力，为不同场景提供统一入口。</p>
      <div class="hero-actions">
        <router-link class="portal-btn portal-btn-primary" to="/modules">探索模块</router-link>
        <router-link class="portal-btn portal-btn-light" to="/fellowship-intro">了解 LoveCube</router-link>
      </div>
      <dl class="hero-stats">
        <div v-for="item in stats" :key="item.label">
          <dt>{{ item.value }}</dt>
          <dd>{{ item.label }}</dd>
        </div>
      </dl>
    </div>

    <div class="hero-visual" aria-hidden="true">
      <img :src="heroImage" alt="">
      <div class="visual-float visual-float-chat">聊</div>
      <div class="visual-bubble visual-bubble-heart">♡</div>
      <div class="visual-bubble visual-bubble-check">✓</div>
    </div>
  </section>
</template>

<script setup>
import heroImage from '@/assets/首页首屏右侧大图.webp'

defineProps({
  stats: {
    type: Array,
    required: true
  }
})
</script>

<style scoped>
.hero-portal {
  position: relative;
  display: grid;
  grid-template-columns: minmax(380px, 0.78fr) minmax(560px, 1.22fr);
  gap: var(--lc-space-4);
  align-items: center;
  min-height: 460px;
  margin-top: var(--lc-space-3);
  padding: 46px 42px;
  border: 1px solid rgba(191, 219, 254, 0.9);
  border-radius: var(--lc-radius-sm);
  background:
    linear-gradient(90deg, rgba(239, 246, 255, 0.98) 0%, rgba(245, 249, 255, 0.8) 42%, rgba(219, 234, 254, 0.56) 100%);
  box-shadow: 0 18px 56px rgba(37, 99, 235, 0.12);
  overflow: hidden;
}

.hero-portal::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(90deg, rgba(255, 255, 255, 0.82) 0%, rgba(255, 255, 255, 0.35) 40%, transparent 62%);
  pointer-events: none;
  z-index: 1;
}

.hero-copy {
  position: relative;
  z-index: 2;
}

.portal-kicker {
  margin: 0 0 var(--lc-space-4);
  color: var(--lc-blue);
  font-size: 12px;
  font-weight: 900;
  letter-spacing: 0.12em;
}

.hero-copy h1 {
  max-width: 560px;
  margin: 0;
  color: var(--lc-text);
  font-size: clamp(42px, 4vw, 62px);
  font-weight: 950;
  line-height: 1.16;
}

.hero-subtitle {
  max-width: 530px;
  margin: var(--lc-space-5) 0 0;
  color: var(--lc-muted);
  font-size: 18px;
  font-weight: 700;
  line-height: 1.72;
}

.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: var(--lc-space-3);
  margin-top: var(--lc-space-8);
}

.portal-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 130px;
  min-height: 44px;
  padding: 0 var(--lc-space-6);
  border: 1px solid transparent;
  border-radius: var(--lc-radius-sm);
  font-weight: 900;
  text-decoration: none;
  transition: var(--lc-transition);
}

.portal-btn:hover {
  transform: translateY(-2px);
}

.portal-btn-primary {
  color: var(--lc-surface);
  background: linear-gradient(135deg, var(--lc-blue), #7c3aed);
  box-shadow: var(--lc-shadow-blue);
}

.portal-btn-light {
  color: var(--lc-blue-dark);
  background: var(--lc-surface);
  border-color: var(--lc-blue-border);
}

.hero-stats {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 0;
  max-width: 500px;
  margin: var(--lc-space-8) 0 0;
}

.hero-stats div {
  padding-right: var(--lc-space-5);
  border-right: 1px solid rgba(148, 163, 184, 0.28);
}

.hero-stats div:last-child {
  border-right: 0;
}

.hero-stats dt {
  color: var(--lc-blue);
  font-size: 25px;
  font-weight: 950;
  line-height: 1;
}

.hero-stats dd {
  margin: 6px 0 0;
  color: var(--lc-muted);
  font-size: var(--lc-text-sm);
  font-weight: 800;
}

.hero-visual {
  position: absolute;
  inset: 0;
  z-index: 0;
}

.hero-visual img {
  position: absolute;
  right: 0;
  top: 0;
  width: 74%;
  height: 100%;
  object-fit: cover;
  object-position: center right;
}

.visual-bubble {
  position: absolute;
  display: grid;
  place-items: center;
  width: 54px;
  height: 54px;
  border-radius: 50%;
  background: var(--lc-surface);
  box-shadow: var(--lc-shadow);
  font-size: 24px;
  font-weight: 900;
}

.visual-float {
  position: absolute;
  z-index: 3;
  display: grid;
  place-items: center;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  color: var(--lc-blue);
  background: rgba(255, 255, 255, 0.86);
  box-shadow: var(--lc-shadow-sm);
  font-weight: 950;
}

.visual-float-chat {
  left: 36%;
  top: 24%;
}

.visual-bubble-heart {
  z-index: 3;
  right: 10%;
  top: 21%;
  color: var(--lc-pink);
}

.visual-bubble-check {
  z-index: 3;
  right: 3%;
  bottom: 33%;
  color: var(--lc-green);
}

@media (max-width: 1080px) {
  .hero-portal {
    grid-template-columns: 1fr;
    padding: var(--lc-space-8);
  }

  .hero-portal::before {
    background: linear-gradient(180deg, rgba(255, 255, 255, 0.92) 0%, rgba(255, 255, 255, 0.78) 58%, rgba(255, 255, 255, 0.22) 100%);
  }

  .hero-visual img {
    width: 100%;
    opacity: 0.34;
  }
}

@media (max-width: 680px) {
  .hero-portal {
    gap: var(--lc-space-6);
    min-height: 0;
    padding: var(--lc-space-6) var(--lc-space-4);
  }

  .hero-copy h1 {
    font-size: 36px;
  }

  .hero-actions,
  .portal-btn {
    width: 100%;
  }

  .hero-stats {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: var(--lc-space-4);
  }

  .hero-stats div {
    border-right: 0;
    padding-right: 0;
  }

  .hero-visual {
    display: none;
  }
}
</style>
