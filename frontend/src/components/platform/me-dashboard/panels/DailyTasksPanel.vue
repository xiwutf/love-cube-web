<template>
  <section class="card" aria-label="今日任务">
    <header class="head">
      <div class="title">
        <h2>今日任务</h2>
        <span>每日 0 点刷新</span>
      </div>
      <div class="right">已完成 <b>{{ completed }}</b>/<b>{{ total }}</b></div>
    </header>

    <div class="table-head">
      <span>任务</span>
      <span>奖励</span>
      <span>进度</span>
      <span>状态</span>
      <span>操作</span>
    </div>

    <div class="list">
      <div v-for="task in tasks" :key="task.title" class="row" :class="statusKey(task)">
        <div class="name">
          <span class="dot" :class="{ ok: task.done }" aria-hidden="true">{{ task.done ? '✓' : '' }}</span>
          <strong>{{ task.title }}</strong>
        </div>

        <div class="reward">+{{ task.exp }} 经验</div>

        <div class="progress">
          <div class="track">
            <span :style="{ width: `${Math.min(100, (task.current / task.total) * 100)}%` }"></span>
          </div>
          <small>{{ task.current }}/{{ task.total }}</small>
        </div>

        <div class="status">
          <span class="status-badge" :class="`s-${statusKey(task)}`">{{ statusLabel(task) }}</span>
        </div>

        <router-link class="action" :class="{ primary: !task.done }" :to="task.to">
          {{ task.done ? '已完成' : '去完成' }}
        </router-link>
      </div>
    </div>
  </section>
</template>

<script setup>
defineProps({
  tasks: { type: Array, required: true },
  completed: { type: Number, required: true },
  total: { type: Number, required: true },
})

function statusKey(task) {
  if (task.done) return 'done'
  if (task.current > 0) return 'active'
  return 'idle'
}

function statusLabel(task) {
  if (task.done) return '已完成'
  if (task.current > 0) return '进行中'
  return '未开始'
}
</script>

<style scoped>
.card {
  background: #ffffff;
  border: 1px solid #e7ebf2;
  border-radius: 16px;
  box-shadow: 0 6px 18px rgba(15, 23, 42, 0.04);
  padding: 18px 18px 14px;
}

.head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(229, 231, 235, 0.8);
}

.title {
  display: flex;
  align-items: baseline;
  gap: 10px;
  flex-wrap: wrap;
}

.title h2 {
  margin: 0;
  font-size: 16px;
  font-weight: 900;
  color: #0f172a;
}

.title span {
  color: #64748b;
  font-size: 12px;
  font-weight: 600;
}

.right {
  color: #64748b;
  font-size: 13px;
  font-weight: 700;
  white-space: nowrap;
}

.right b {
  color: #0f172a;
}

.table-head {
  margin-top: 10px;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 80px 160px 72px 76px;
  gap: 12px;
  padding: 0 8px 6px;
  color: #8d9ab0;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.02em;
}

.list {
  padding: 2px 0;
}

.row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 80px 160px 72px 76px;
  gap: 12px;
  align-items: center;
  height: 64px;
  padding: 0 8px;
  border-radius: 12px;
  transition: background 0.12s;
}

.row + .row {
  margin-top: 2px;
}

.row:hover {
  background: rgba(79, 70, 229, 0.035);
}

.row.done {
  opacity: 0.82;
}

.name {
  display: grid;
  grid-template-columns: 22px minmax(0, 1fr);
  gap: 10px;
  align-items: center;
  min-width: 0;
}

.name strong {
  display: block;
  font-size: 13px;
  font-weight: 700;
  color: #0f172a;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.row.done .name strong {
  color: #94a3b8;
  text-decoration: line-through;
}

.dot {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: 1.5px solid rgba(203, 213, 225, 0.9);
  display: grid;
  place-items: center;
  color: #ffffff;
  background: #ffffff;
  font-weight: 900;
  font-size: 11px;
  flex: 0 0 auto;
}

.dot.ok {
  background: #4f46e5;
  border-color: #4f46e5;
}

.reward {
  color: #64748b;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
}

.progress {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.track {
  height: 7px;
  border-radius: 999px;
  overflow: hidden;
  background: rgba(229, 231, 235, 0.9);
}

.track span {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: #4f46e5;
  transition: width 0.3s ease;
}

.progress small {
  font-size: 11px;
  color: #94a3b8;
  font-weight: 600;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 24px;
  padding: 0 8px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 700;
  white-space: nowrap;
}

.s-done   { color: #059669; background: rgba(5, 150, 105, 0.10); }
.s-active { color: #2563eb; background: rgba(37, 99, 235, 0.10); }
.s-idle   { color: #94a3b8; background: #f1f5f9; }

.action {
  justify-self: end;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 30px;
  padding: 0 12px;
  border-radius: 10px;
  border: 1px solid rgba(229, 231, 235, 0.9);
  color: #64748b;
  text-decoration: none;
  font-size: 12px;
  font-weight: 700;
  white-space: nowrap;
  transition: background 0.12s;
}

.action:hover {
  background: #f8fafc;
}

.action.primary {
  border-color: rgba(79, 70, 229, 0.18);
  color: #4f46e5;
  background: rgba(79, 70, 229, 0.07);
}

.action.primary:hover {
  background: rgba(79, 70, 229, 0.12);
}
</style>
