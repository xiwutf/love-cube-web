<template>
  <div class="landing-page">
    <div class="content">
      <p class="eyebrow">Love Cube Fellowship</p>
      <h1 class="title">更可靠的联谊交友体验</h1>
      <p class="slogan">从完善资料到弢始匹配，3 分钟即可进入互动状</p>
      <div class="actions">
        <router-link to="/fellowship/discover" class="btn-primary">立即弢始匹</router-link>
        <router-link to="/fellowship/profile/edit" class="btn-ghost">完善我的资料</router-link>
      </div>
      <p class="hint">新用户建议先完善资料并完成真人认证，推荐效果会更好</p>

      <div class="quick-grid">
        <router-link to="/fellowship/discover" class="quick-item">推荐对象</router-link>
        <router-link to="/fellowship/messages?tab=interact" class="quick-item">打招呼消</router-link>
        <router-link to="/fellowship/messages?tab=visitor" class="quick-item">璋佺湅杩囨垜</router-link>
        <router-link to="/fellowship/my-likes" class="quick-item">我的喜欢</router-link>
        <router-link to="/fellowship/preferences" class="quick-item">择偶条件</router-link>
        <router-link to="/fellowship/verify" class="quick-item">真人/实名认证</router-link>
        <router-link to="/fellowship/blacklist" class="quick-item">黑名</router-link>
        <router-link to="/fellowship/profile/edit" class="quick-item">生活照管</router-link>
      </div>
    </div>
    <div class="footer-link">
      <router-link to="/">返回平台首页</router-link>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { storage } from '@/utils/storage.js'

const router = useRouter()
if (storage.get('token')) {
  router.replace('/fellowship/discover')
}
</script>

<style scoped>
.landing-page {
  min-height: 100vh;
  min-height: 100dvh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(160deg, #fff0f3 0%, #ffffff 60%);
  padding: calc(24px + env(safe-area-inset-top)) 20px calc(24px + env(safe-area-inset-bottom));
  position: relative;
}

.content {
  width: min(100%, 520px);
  text-align: center;
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.eyebrow {
  margin: 0;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.12em;
  color: #e84f73;
  text-transform: uppercase;
}

.title {
  margin: 14px 0 10px;
  font-size: clamp(32px, 8vw, 44px);
  line-height: 1.2;
  font-weight: 900;
  color: #1f2a44;
}

.slogan {
  font-size: 15px;
  color: #64748b;
  margin: 0;
  line-height: 1.7;
}

.actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  justify-content: center;
  margin-top: 28px;
}

.btn-primary,
.btn-ghost {
  min-height: 46px;
  padding: 0 24px;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 700;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.btn-primary {
  background: #ff5f84;
  color: #fff;
  box-shadow: 0 10px 20px rgba(255, 95, 132, 0.25);
}

.btn-ghost {
  border: 1px solid #ffc6d4;
  color: #d94870;
  background: #fff;
}

.hint {
  margin: 16px 0 0;
  font-size: 13px;
  color: #64748b;
}

.quick-grid {
  width: 100%;
  margin-top: 16px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.quick-item {
  min-height: 40px;
  border-radius: 10px;
  border: 1px solid #ffd5df;
  background: #fff;
  color: #d94870;
  font-size: 13px;
  font-weight: 600;
  text-decoration: none;
  display: flex;
  align-items: center;
  justify-content: center;
}

.footer-link {
  position: absolute;
  bottom: 24px;
  font-size: 13px;
}

.footer-link a {
  color: #94a3b8;
  text-decoration: none;
}

@media (max-width: 767px) {
  .landing-page {
    justify-content: flex-start;
    padding: calc(16px + env(safe-area-inset-top)) 16px calc(20px + env(safe-area-inset-bottom));
  }

  .content {
    width: 100%;
    justify-content: flex-start;
    padding-bottom: 12px;
  }

  .actions {
    width: 100%;
    flex-direction: column;
  }

  .btn-primary,
  .btn-ghost {
    width: 100%;
  }

  .footer-link {
    position: static;
    margin-top: 10px;
  }
}

@media (max-width: 375px) {
  .landing-page {
    padding: calc(12px + env(safe-area-inset-top)) 12px calc(16px + env(safe-area-inset-bottom));
  }

  .title {
    margin-top: 10px;
    font-size: 28px;
    line-height: 1.25;
  }

  .slogan {
    font-size: 14px;
    line-height: 1.55;
  }

  .actions {
    margin-top: 16px;
    gap: 8px;
  }

  .btn-primary,
  .btn-ghost {
    min-height: 42px;
    font-size: 14px;
  }

  .quick-grid {
    grid-template-columns: 1fr;
    gap: 8px;
  }

  .quick-item {
    min-height: 38px;
    font-size: 12px;
  }
}

@media (max-width: 360px) {
  .landing-page {
    padding: calc(10px + env(safe-area-inset-top)) 10px calc(12px + env(safe-area-inset-bottom));
  }

  .eyebrow {
    font-size: 11px;
    letter-spacing: 0.08em;
  }

  .title {
    margin: 8px 0;
    font-size: 24px;
    line-height: 1.3;
  }

  .hint {
    margin-top: 12px;
    font-size: 12px;
  }

  .btn-primary,
  .btn-ghost {
    min-height: 40px;
    font-size: 13px;
  }
}
</style>

