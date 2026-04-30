<template>
  <section class="card" aria-label="成长等级">
    <header class="head">
      <h2>成长等级</h2>
      <router-link class="more" to="/pc/platform/growth-record">成长记录 ></router-link>
    </header>

    <div class="body">
      <div class="level-row">
        <div class="badge">Lv.{{ growthLevel.level }}</div>
        <div class="medal">
          <span>徽章</span>
          <strong>{{ growthLevel.name }}</strong>
        </div>
      </div>
      <div class="content">
        <strong class="lv-name">成长进度 {{ growthProgress }}</strong>
        <p class="hint">再获得 {{ growthLevel.nextExp - growthLevel.currentExp }} 经验可升级 Lv.{{ growthLevel.level + 1 }}</p>
        <div class="progress">
          <span :style="{ width: growthProgress }"></span>
        </div>
        <div class="meta">{{ growthLevel.currentExp }} / {{ growthLevel.nextExp }}</div>
      </div>
    </div>

    <ul v-if="!compact" class="sources" aria-label="经验来源">
      <li v-for="item in growthLevel.sources" :key="item.label">
        <span>{{ item.label }}</span>
        <strong>+{{ item.exp }} 经验</strong>
      </li>
    </ul>
  </section>
</template>

<script setup>
defineProps({
  growthLevel: { type: Object, required: true },
  growthProgress: { type: String, required: true },
  compact: { type: Boolean, default: false },
})
</script>

<style scoped>
.card {
  background: #ffffff;
  border: 1px solid #e7ebf2;
  border-radius: 12px;
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.055);
  padding: 22px 24px;
  min-height: 272px;
  box-sizing: border-box;
}

.head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 14px;
}

.head h2 {
  margin: 0;
  font-size: 15px;
  font-weight: 900;
  color: #0f172a;
}

.more {
  color: #64748b;
  font-size: 12px;
  font-weight: 700;
  text-decoration: none;
  white-space: nowrap;
}

.body {
  display: flex;
  min-height: 176px;
  flex-direction: column;
  align-items: stretch;
  justify-content: center;
  gap: 16px;
}

.badge {
  position: relative;
  width: 84px;
  height: 84px;
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, #4f46e5, #7c3aed);
  color: #ffffff;
  font-weight: 900;
  font-size: 21px;
  clip-path: polygon(25% 5%, 75% 5%, 100% 50%, 75% 95%, 25% 95%, 0 50%);
  filter: drop-shadow(0 12px 20px rgba(79, 70, 229, 0.28));
}

.level-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.medal {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 8px 12px;
  border-radius: 10px;
  background: linear-gradient(135deg, #fff7ed, #fef3c7);
  border: 1px solid #fde68a;
  box-shadow: 0 6px 14px rgba(245, 158, 11, 0.12);
}

.medal span {
  font-size: 11px;
  color: #b45309;
  font-weight: 700;
}

.medal strong {
  font-size: 13px;
  color: #92400e;
  font-weight: 800;
  white-space: nowrap;
}

.content {
  min-width: 0;
  text-align: center;
}

.lv-name {
  display: block;
  font-size: 14px;
  font-weight: 900;
  color: #0f172a;
}

.hint {
  margin: 5px 0 0;
  font-size: 11px;
  color: #64748b;
  line-height: 1.45;
}

.progress {
  margin-top: 10px;
  height: 7px;
  border-radius: 999px;
  overflow: hidden;
  background: rgba(229, 231, 235, 0.9);
}

.progress span {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, #6d5dfb, #4f46e5);
  transition: width 0.4s ease;
}

.meta {
  margin-top: 5px;
  font-size: 11px;
  color: #94a3b8;
  font-weight: 700;
  text-align: center;
}

.sources {
  margin: 14px 0 0;
  padding: 14px 0 0;
  list-style: none;
  display: grid;
  gap: 10px;
  border-top: 1px solid rgba(229, 231, 235, 0.8);
}

.sources li {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  color: #64748b;
  font-size: 13px;
}

.sources strong {
  color: #64748b;
  font-weight: 800;
}
</style>
