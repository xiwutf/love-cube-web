<template>
  <section class="fellowship-banner">
    <div class="banner-copy">
      <p>LoveCube 联谊专区</p>
      <h2>真诚相遇，认真开始</h2>
      <ul>
        <li>实名认证</li>
        <li>精准匹配</li>
        <li>安全可靠</li>
      </ul>
      <router-link to="/fellowship-intro">进入专区 →</router-link>
    </div>
    <div class="banner-visual" aria-hidden="true">
      <img :src="fellowshipImage" alt="">
    </div>
  </section>
</template>

<script setup>
import fellowshipImage from '@/assets/联谊专区.webp'
</script>

<style scoped>
.fellowship-banner {
  position: relative;
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(220px, 300px);
  gap: var(--lc-space-5);
  min-height: 198px;
  padding: var(--lc-space-8) var(--lc-space-8) var(--lc-space-6);
  border: 1px solid var(--lc-pink-border);
  border-radius: var(--lc-radius);
  background:
    linear-gradient(135deg, rgba(252, 231, 243, 0.9), rgba(255, 255, 255, 0.95));
  box-shadow: var(--lc-shadow-sm);
  overflow: hidden;
}

.banner-copy p {
  margin: 0;
  color: var(--lc-pink);
  font-size: 24px;
  font-weight: 950;
}

.banner-copy h2 {
  margin: var(--lc-space-2) 0 0;
  color: #be185d;
  font-size: 17px;
}

.banner-copy ul {
  display: flex;
  flex-wrap: wrap;
  gap: var(--lc-space-4);
  margin: var(--lc-space-5) 0 0;
  padding: 0;
  list-style: none;
}

.banner-copy li {
  color: var(--lc-muted);
  font-weight: 800;
}

.banner-copy li::before {
  content: '♡';
  margin-right: 6px;
  color: var(--lc-pink);
}

.banner-copy a {
  display: inline-flex;
  align-items: center;
  min-height: 40px;
  margin-top: var(--lc-space-6);
  padding: 0 var(--lc-space-5);
  border-radius: 999px;
  color: var(--lc-surface);
  background: linear-gradient(135deg, var(--lc-pink), #fb7185);
  font-weight: 950;
  text-decoration: none;
}

.banner-visual {
  position: relative;
  min-height: 150px;
}

.banner-visual img {
  position: absolute;
  inset: auto 0 -22px auto;
  width: min(100%, 310px);
  height: 190px;
  object-fit: contain;
}

@media (max-width: 680px) {
  .fellowship-banner {
    grid-template-columns: minmax(0, 1fr) 118px;
    gap: 8px;
    min-height: 172px;
    padding: 16px 14px;
    border-radius: 18px;
  }

  .banner-copy p {
    font-size: 32px;
  }

  .banner-copy h2 {
    font-size: 14px;
  }

  .banner-copy ul {
    margin-top: 10px;
    gap: 8px 12px;
  }

  .banner-copy li {
    font-size: 12px;
  }

  .banner-copy a {
    min-height: 34px;
    margin-top: 12px;
    padding: 0 14px;
    font-size: 12px;
  }

  .banner-visual {
    min-height: 130px;
  }

  .banner-visual img {
    height: 130px;
    inset: auto -8px -8px auto;
  }
}

@media (max-width: 390px) {
  .fellowship-banner {
    grid-template-columns: 1fr 100px;
    min-height: 164px;
    padding: 14px 12px;
  }

  .banner-copy p {
    font-size: 28px;
  }
}
</style>
