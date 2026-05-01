<template>
  <section class="sp-page">
    <header class="sp-head">
      <button type="button" class="sp-back" aria-label="返回" @click="$router.back()">‹</button>
      <h1 class="sp-title">隐私设置</h1>
    </header>

    <div class="sp-body">
      <!-- 资料展示 -->
      <div class="sp-card">
        <div class="sp-card-title">资料展示范围</div>
        <div v-for="item in profileItems" :key="item.key" class="pv-row">
          <div class="pv-row-left">
            <div class="pv-row-name">{{ item.label }}</div>
            <div class="pv-row-sub">{{ item.desc }}</div>
          </div>
          <select v-model="settings[item.key]" class="pv-select">
            <option value="all">所有人</option>
            <option value="friends">好友可见</option>
            <option value="private">仅自己</option>
          </select>
        </div>
      </div>

      <!-- 互动设置 -->
      <div class="sp-card">
        <div class="sp-card-title">互动设置</div>
        <div v-for="item in interactItems" :key="item.key" class="pv-switch-row">
          <div>
            <div class="pv-row-name">{{ item.label }}</div>
            <div class="pv-row-sub">{{ item.desc }}</div>
          </div>
          <button
            type="button"
            class="pv-switch"
            :class="{ on: settings[item.key] }"
            @click="settings[item.key] = !settings[item.key]"
          >
            <span class="pv-switch-thumb"></span>
          </button>
        </div>
      </div>

      <!-- 数据与账号 -->
      <div class="sp-card">
        <div class="sp-card-title">数据与账号</div>
        <button type="button" class="pv-link-row">
          <div>
            <div class="pv-row-name">黑名单管理</div>
            <div class="pv-row-sub">管理屏蔽的用户</div>
          </div>
          <span class="pv-arrow">›</span>
        </button>
        <button type="button" class="pv-link-row">
          <div>
            <div class="pv-row-name">清空浏览历史</div>
            <div class="pv-row-sub">删除本地浏览记录</div>
          </div>
          <span class="pv-arrow">›</span>
        </button>
      </div>

      <p v-if="saved" class="pv-saved">已保存</p>
      <button type="button" class="sp-primary-btn" @click="handleSave">保存设置</button>
    </div>
  </section>
</template>

<script setup>
import { reactive, ref } from 'vue'

const saved = ref(false)

const settings = reactive({
  showProfile: 'all',
  showPosts: 'all',
  showFollows: 'friends',
  allowComment: true,
  allowMention: true,
  showOnline: false
})

const profileItems = [
  { key: 'showProfile', label: '个人资料', desc: '谁可以查看你的资料详情' },
  { key: 'showPosts', label: '我的动态', desc: '谁可以查看你发布的动态' },
  { key: 'showFollows', label: '关注列表', desc: '谁可以查看你的关注与粉丝' }
]

const interactItems = [
  { key: 'allowComment', label: '允许评论', desc: '允许他人评论你的动态' },
  { key: 'allowMention', label: '允许被@', desc: '允许他人在内容中提及你' },
  { key: 'showOnline', label: '显示在线状态', desc: '向他人展示你的在线状态' }
]

function handleSave() {
  saved.value = true
  setTimeout(() => { saved.value = false }, 2000)
}
</script>

<style scoped>
.sp-page { min-height: 100vh; background: var(--lc-bg); color: var(--lc-text); }

.sp-head {
  position: sticky; top: 0; z-index: 10;
  display: flex; align-items: center;
  background: var(--lc-surface); border-bottom: 1px solid var(--lc-soft-alt);
}
.sp-back {
  width: 48px; height: 52px; flex: 0 0 auto;
  display: grid; place-items: center;
  border: 0; background: none; font-size: 22px; color: var(--lc-indigo); cursor: pointer;
}
.sp-title { flex: 1; margin: 0; font-size: 17px; font-weight: 800; }

.sp-body {
  padding: 16px 14px calc(80px + env(safe-area-inset-bottom));
  max-width: 680px; margin: 0 auto;
}
.sp-card {
  background: var(--lc-surface); border: 1px solid var(--lc-soft-alt); border-radius: 18px;
  box-shadow: 0 3px 12px rgba(15,23,42,0.04); margin-bottom: 14px; padding: 16px;
}
.sp-card-title { font-size: 15px; font-weight: 800; margin-bottom: 14px; color: var(--lc-text); }

.pv-row {
  display: flex; align-items: center; justify-content: space-between; gap: 12px;
  padding: 11px 0; border-bottom: 1px solid var(--lc-soft);
}
.pv-row:last-child { border-bottom: 0; }
.pv-row-left { flex: 1; min-width: 0; }
.pv-row-name { font-size: 14px; font-weight: 600; color: var(--lc-text-deep); }
.pv-row-sub { font-size: 12px; color: var(--lc-subtle); margin-top: 2px; }

.pv-select {
  flex: 0 0 auto; border: 1px solid var(--lc-soft-alt); border-radius: 8px;
  padding: 6px 10px; font-size: 12px; color: var(--lc-slate); background: var(--lc-bg);
  outline: none; cursor: pointer;
}

.pv-switch-row {
  display: flex; align-items: center; justify-content: space-between; gap: 12px;
  padding: 12px 0; border-bottom: 1px solid var(--lc-soft);
}
.pv-switch-row:last-child { border-bottom: 0; }

.pv-switch {
  position: relative; flex: 0 0 auto;
  width: 48px; height: 28px; border-radius: 999px; border: 0;
  background: var(--lc-border); cursor: pointer; transition: background 0.2s;
}
.pv-switch.on { background: var(--lc-violet); }
.pv-switch-thumb {
  position: absolute; top: 3px; left: 3px;
  width: 22px; height: 22px; border-radius: 50%; background: var(--lc-surface);
  box-shadow: 0 1px 4px rgba(0,0,0,0.2); transition: left 0.2s;
}
.pv-switch.on .pv-switch-thumb { left: 23px; }

.pv-link-row {
  display: flex; align-items: center; justify-content: space-between; gap: 12px;
  width: 100%; padding: 12px 0; border-bottom: 1px solid var(--lc-soft); border-left: 0; border-right: 0; border-top: 0;
  background: none; cursor: pointer; text-align: left;
}
.pv-link-row:last-child { border-bottom: 0; }
.pv-arrow { font-size: 20px; color: var(--lc-border); }

.pv-saved { text-align: center; font-size: 13px; color: var(--lc-emerald); margin-bottom: 8px; }
.sp-primary-btn {
  display: block; width: 100%; padding: 14px; border: 0; border-radius: 14px;
  background: linear-gradient(135deg, var(--lc-violet), var(--lc-indigo)); color: var(--lc-surface);
  font-size: 15px; font-weight: 800; cursor: pointer;
}

@media (max-width: 767px) {
  :global(.platform-header), :global(.platform-footer), :global(.co-creation-toolbar) {
    display: none !important;
  }
}
@media (min-width: 768px) {
  .sp-body { padding-top: 24px; padding-bottom: 48px; }
}
</style>
