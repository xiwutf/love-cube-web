<template>
  <div class="content-panel">
    <div class="panel-head">
      <h2>模块推荐</h2>
      <router-link to="/modules">查看更多 →</router-link>
    </div>
    <div class="people-grid">
      <article v-for="module in moduleCards" :key="module.title" class="person-card">
        <div class="person-photo" :class="module.tone">
          <span>{{ module.icon }}</span>
          <i></i>
        </div>
        <strong>{{ module.title }}</strong>
        <small>{{ module.meta }}</small>
        <p>{{ module.desc }}</p>
        <router-link :to="module.to">{{ module.actionText }}</router-link>
      </article>
    </div>
  </div>
</template>

<script setup>
defineProps({
  moduleCards: {
    type: Array,
    default: () => []
  }
})
</script>

<style scoped>
.content-panel {
  min-width: 0;
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.panel-head h2 {
  margin: 0;
  color: #0f172a;
  font-size: 22px;
}

.panel-head a {
  color: #2563eb;
  font-size: 13px;
  font-weight: 800;
  text-decoration: none;
}

.people-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.person-card {
  display: grid;
  justify-items: center;
  gap: 7px;
  min-height: 206px;
  padding: 14px 12px;
  text-align: center;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.045);
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}

.person-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 18px 38px rgba(15, 23, 42, 0.1);
}

.person-photo {
  position: relative;
  display: grid;
  place-items: center;
  width: 90px;
  height: 100px;
  border-radius: 8px;
}

.person-photo span {
  display: grid;
  place-items: center;
  width: 64px;
  height: 64px;
  border-radius: 50%;
  color: #fff;
  background: linear-gradient(135deg, var(--tone), #94a3b8);
  font-size: 24px;
  font-weight: 900;
}

.person-photo i {
  position: absolute;
  right: 12px;
  bottom: 17px;
  width: 9px;
  height: 9px;
  border: 2px solid #fff;
  border-radius: 50%;
  background: #22c55e;
}

.person-card strong {
  color: #0f172a;
  font-size: 15px;
}

.person-card small,
.person-card p {
  color: #64748b;
  font-size: 12px;
  line-height: 1.45;
}

.person-card p {
  margin: 0;
  min-height: 34px;
}

.person-card a {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 74px;
  height: 30px;
  border: 1px solid currentColor;
  border-radius: 8px;
  color: var(--tone);
  font-size: 12px;
  font-weight: 800;
  text-decoration: none;
}

.tone-pink { --tone: #ec4899; --tone-bg: #fff1f5; color: #be185d; background: linear-gradient(180deg, #fff7fb, #fff); }
.tone-violet { --tone: #6366f1; --tone-bg: #eef2ff; color: #4f46e5; background: linear-gradient(180deg, #f5f3ff, #fff); }
.tone-blue { --tone: #2563eb; --tone-bg: #eff6ff; color: #1d4ed8; background: linear-gradient(180deg, #eff6ff, #fff); }
.tone-orange { --tone: #f97316; --tone-bg: #fff7ed; color: #c2410c; background: linear-gradient(180deg, #fff7ed, #fff); }
.tone-green { --tone: #16a34a; --tone-bg: #f0fdf4; color: #15803d; background: linear-gradient(180deg, #f0fdf4, #fff); }
.tone-purple { --tone: #7c3aed; --tone-bg: #f5f3ff; color: #6d28d9; background: linear-gradient(180deg, #f5f3ff, #fff); }

@media (max-width: 520px) {
  .people-grid {
    grid-template-columns: 1fr;
    gap: 10px;
  }
}
</style>
