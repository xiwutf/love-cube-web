<template>
  <section class="season-page">
    <header class="season-hero platform-card">
      <div>
        <h1 class="platform-title">团体赛季榜</h1>
        <p class="platform-subtitle">
          季度积分：打卡 +2、任务 +5、活动 +10。各团体通过成员活跃累积赛季积分。
        </p>
        <p v-if="meta.seasonTitle" class="season-range">
          {{ meta.seasonTitle }}
          <template v-if="meta.startDate && meta.endDate">（{{ meta.startDate }} 至 {{ meta.endDate }}）</template>
        </p>
      </div>
      <router-link :to="groupsPath()" class="platform-btn">返回团体大厅</router-link>
    </header>

    <div v-if="loading" class="season-state">加载中…</div>
    <div v-else-if="error" class="season-state err">
      <p>{{ error }}</p>
      <button type="button" class="platform-btn" @click="load">重试</button>
    </div>

    <template v-else>
      <section v-if="rules.length" class="rules-card platform-card">
        <h2>积分规则</h2>
        <ul>
          <li v-for="rule in rules" :key="rule.label">
            <strong>{{ rule.label }} ×{{ rule.points }}</strong>
            <span>{{ rule.description }}</span>
          </li>
        </ul>
      </section>

      <section class="rank-card platform-card">
        <h2>季度排行榜</h2>
        <table class="rank-table">
          <thead>
            <tr>
              <th>排名</th>
              <th>团体</th>
              <th>积分</th>
              <th>打卡</th>
              <th>任务</th>
              <th>活动</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in items" :key="row.groupId">
              <td>#{{ row.rank }}</td>
              <td>
                <router-link :to="groupsPath(String(row.groupId))">{{ row.groupName }}</router-link>
              </td>
              <td><strong>{{ row.score }}</strong></td>
              <td>{{ row.checkinCount ?? 0 }}</td>
              <td>{{ row.taskCount ?? 0 }}</td>
              <td>{{ row.activityCount ?? 0 }}</td>
            </tr>
          </tbody>
        </table>
        <p v-if="!items.length" class="season-state">暂无排行数据</p>
        <div v-if="totalPages > 1" class="pager">
          <button type="button" class="platform-btn" :disabled="page <= 1" @click="goPage(page - 1)">上一页</button>
          <span>{{ page }} / {{ totalPages }}</span>
          <button type="button" class="platform-btn" :disabled="page >= totalPages" @click="goPage(page + 1)">下一页</button>
        </div>
      </section>
    </template>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { fetchGroupSeasonRankings } from '@/api/groups.js'
import { usePlatformPath } from '@/composables/usePlatformPath.js'

const { groupsPath } = usePlatformPath()

const loading = ref(true)
const error = ref('')
const items = ref([])
const page = ref(1)
const pageSize = 20
const total = ref(0)
const meta = reactive({
  seasonTitle: '',
  startDate: '',
  endDate: ''
})
const rules = ref([])

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize)))

async function load() {
  loading.value = true
  error.value = ''
  try {
    const res = await fetchGroupSeasonRankings({ page: page.value, size: pageSize })
    items.value = Array.isArray(res?.items) ? res.items : []
    total.value = Number(res?.total ?? items.value.length)
    meta.seasonTitle = res?.seasonTitle || ''
    meta.startDate = res?.startDate || ''
    meta.endDate = res?.endDate || ''
    rules.value = Array.isArray(res?.scoringRules) ? res.scoringRules : []
  } catch (e) {
    error.value = e?.message || '加载失败'
    items.value = []
  } finally {
    loading.value = false
  }
}

function goPage(p) {
  page.value = Math.min(Math.max(1, p), totalPages.value)
  load()
}

onMounted(load)
</script>

<style scoped>
.season-page {
  display: flex;
  flex-direction: column;
  gap: var(--lc-space-4);
  padding-bottom: var(--lc-space-6);
}

.season-hero {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--lc-space-3);
}

.season-range {
  margin: var(--lc-space-2) 0 0;
  font-size: var(--lc-text-sm);
  color: var(--lc-text-muted);
}

.season-state {
  text-align: center;
  padding: var(--lc-space-6);
  color: var(--lc-text-muted);
}

.season-state.err {
  color: var(--lc-danger, #c0392b);
}

.rules-card ul {
  margin: 0;
  padding: 0;
  list-style: none;
  display: grid;
  gap: var(--lc-space-2);
}

.rules-card li {
  display: flex;
  flex-direction: column;
  gap: 2px;
  font-size: var(--lc-text-sm);
}

.rules-card span {
  color: var(--lc-text-muted);
}

.rank-table {
  width: 100%;
  border-collapse: collapse;
  font-size: var(--lc-text-sm);
}

.rank-table th,
.rank-table td {
  padding: var(--lc-space-2) var(--lc-space-3);
  border-bottom: 1px solid var(--lc-border);
  text-align: left;
}

.rank-table a {
  color: var(--lc-blue);
  text-decoration: none;
}

.pager {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--lc-space-3);
  margin-top: var(--lc-space-4);
}

@media (max-width: 720px) {
  .rank-table thead {
    display: none;
  }

  .rank-table tr {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 4px 12px;
    padding: var(--lc-space-3) 0;
    border-bottom: 1px solid var(--lc-border);
  }

  .rank-table td {
    border: none;
    padding: 2px 0;
  }
}
</style>
