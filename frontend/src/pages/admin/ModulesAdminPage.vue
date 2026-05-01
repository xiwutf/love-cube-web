<template>
  <div class="modules-admin">
    <header class="ma-header">
      <div>
        <h2 class="ma-title">模块管理</h2>
        <p class="ma-desc">这里维护的模块会同步影响前台首页“平台模块”和“模块中心”的展示、排序、状态与入口。</p>
      </div>
      <div class="ma-actions">
        <button type="button" class="admin-btn" :disabled="loading" @click="loadModules">
          {{ loading ? '加载中...' : '重新加载' }}
        </button>
        <button type="button" class="admin-btn primary" :disabled="saving" @click="saveModules">
          {{ saving ? '保存中...' : '保存设置' }}
        </button>
      </div>
    </header>

    <div class="ma-table-wrap">
      <table class="ma-table">
        <thead>
          <tr>
            <th>显示</th>
            <th>排序</th>
            <th>模块标识</th>
            <th>模块名称</th>
            <th>状态</th>
            <th>入口路由</th>
            <th>图标</th>
            <th>色调</th>
            <th>说明</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="mod in modules" :key="mod.moduleKey">
            <td>
              <label class="ma-switch">
                <input v-model="mod.enabled" type="checkbox">
                <span>{{ mod.enabled ? '展示' : '' }}</span>
              </label>
            </td>
            <td>
              <input v-model.number="mod.sortOrder" class="ma-input ma-input-sort" type="number" min="0">
            </td>
            <td>
              <code class="ma-key">{{ mod.moduleKey }}</code>
            </td>
            <td>
              <input v-model="mod.title" class="ma-input" type="text">
            </td>
            <td>
              <select v-model="mod.status" class="ma-input">
                <option value="active">已开启</option>
                <option value="planned">规划中</option>
              </select>
            </td>
            <td>
              <input v-model="mod.to" class="ma-input" type="text" placeholder="/fellowship">
            </td>
            <td>
              <input v-model="mod.icon" class="ma-input ma-input-icon" type="text" maxlength="2">
            </td>
            <td>
              <select v-model="mod.tone" class="ma-input">
                <option v-for="tone in toneOptions" :key="tone.value" :value="tone.value">
                  {{ tone.label }}
                </option>
              </select>
            </td>
            <td>
              <textarea v-model="mod.desc" class="ma-textarea" rows="2"></textarea>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <p class="ma-tip">状态为“规划中”的模块前台会显示“即将上线”；关闭展示后前台不再显示该模块。</p>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { showToast } from 'vant'
import { getAdminModuleConfig, saveAdminModuleConfig } from '@/api/adminContent.js'

const loading = ref(false)
const saving = ref(false)
const modules = ref([])

const toneOptions = [
  { value: 'tone-blue', label: '蓝色' },
  { value: 'tone-cyan', label: '青色' },
  { value: 'tone-green', label: '绿色' },
  { value: 'tone-amber', label: '琥珀' },
  { value: 'tone-violet', label: '紫色' },
  { value: 'tone-rose', label: '玫红' }
]

function normalizeModule(item, index) {
  return {
    moduleKey: item.moduleKey || `module-${index + 1}`,
    title: item.title || item.name || '',
    desc: item.desc || item.description || '',
    to: item.to || item.entryRoute || '',
    status: item.status || 'planned',
    enabled: item.enabled !== false,
    sortOrder: Number.isFinite(Number(item.sortOrder)) ? Number(item.sortOrder) : index + 1,
    icon: item.icon || '模',
    tone: item.tone || 'tone-blue',
    coverUrl: item.coverUrl || ''
  }
}

async function loadModules() {
  loading.value = true
  try {
    const rows = await getAdminModuleConfig()
    modules.value = rows.map(normalizeModule)
  } catch (error) {
    showToast({ type: 'fail', message: error.message || '加载模块配置失败' })
  } finally {
    loading.value = false
  }
}

async function saveModules() {
  saving.value = true
  try {
    const payload = modules.value
      .map((item, index) => normalizeModule(item, index))
      .sort((a, b) => a.sortOrder - b.sortOrder)
    const result = await saveAdminModuleConfig(payload)
    modules.value = (result?.modules || payload).map(normalizeModule)
    showToast({ type: 'success', message: '模块配置已保存' })
  } catch (error) {
    showToast({ type: 'fail', message: error.message || '保存模块配置失败' })
  } finally {
    saving.value = false
  }
}

loadModules()
</script>

<style scoped>
.modules-admin {
  display: grid;
  gap: 16px;
  padding: 8px 0 20px;
}

.ma-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 16px;
}

.ma-title {
  font-size: 24px;
  font-weight: 900;
  color: var(--lc-text);
  margin: 0 0 8px;
}

.ma-desc {
  font-size: 14px;
  color: var(--lc-muted-light);
  margin: 0;
  line-height: 1.6;
}

.ma-actions {
  display: flex;
  gap: 10px;
  flex-shrink: 0;
}

.ma-table-wrap {
  overflow-x: auto;
  border: 1px solid var(--lc-border);
  border-radius: 12px;
  background: var(--lc-surface);
}

.ma-table {
  width: 100%;
  min-width: 1180px;
  border-collapse: collapse;
  font-size: 14px;
}

.ma-table th {
  background: var(--lc-bg);
  color: var(--lc-muted-light);
  font-weight: 700;
  font-size: 12px;
  text-align: left;
  padding: 12px;
  border-bottom: 1px solid var(--lc-border);
  white-space: nowrap;
}

.ma-table td {
  padding: 12px;
  border-bottom: 1px solid var(--lc-soft);
  color: var(--lc-slate);
  vertical-align: top;
}

.ma-table tr:last-child td {
  border-bottom: none;
}

.ma-switch {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: var(--lc-muted);
  font-size: 13px;
  font-weight: 700;
  white-space: nowrap;
}

.ma-key {
  display: inline-flex;
  padding: 6px 8px;
  border-radius: 8px;
  background: var(--lc-soft);
  color: var(--lc-muted);
  font-size: 12px;
}

.ma-input,
.ma-textarea {
  width: 100%;
  border: 1px solid var(--lc-border);
  border-radius: 8px;
  background: var(--lc-surface);
  color: var(--lc-text);
  font-size: 13px;
  padding: 8px 10px;
  box-sizing: border-box;
}

.ma-input:focus,
.ma-textarea:focus {
  outline: none;
  border-color: var(--lc-blue-border);
  box-shadow: 0 0 0 3px rgba(37,99,235,.12);
}

.ma-input-sort {
  width: 72px;
}

.ma-input-icon {
  width: 64px;
  text-align: center;
  font-weight: 900;
}

.ma-textarea {
  min-width: 240px;
  resize: vertical;
  line-height: 1.5;
}

.ma-tip {
  margin: 0;
  font-size: 13px;
  color: var(--lc-subtle);
  line-height: 1.6;
}

@media (max-width: 767px) {
  .ma-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .ma-actions {
    width: 100%;
  }
}
</style>




