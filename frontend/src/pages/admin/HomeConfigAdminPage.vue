<template>
  <div class="home-config-admin">
    <header class="hc-head">
      <h2>首页配置</h2>
      <p>配置首页 Hero、模块封面和能力图片，所有图片通过上传按钮管理，无需手动填 URL。</p>
    </header>

    <section class="hc-panel">
      <div class="panel-head">
        <h3>Hero 配置</h3>
      </div>
      <div class="hero-form">
        <label class="field">
          <span>标题</span>
          <input v-model="form.hero.title" class="admin-input" type="text">
        </label>
        <label class="field">
          <span>副标题</span>
          <textarea v-model="form.hero.subtitle" class="admin-textarea" rows="3"></textarea>
        </label>
        <div class="grid-2">
          <label class="field">
            <span>主按钮文字</span>
            <input v-model="form.hero.primaryText" class="admin-input" type="text">
          </label>
          <label class="field">
            <span>主按钮链接</span>
            <input v-model="form.hero.primaryLink" class="admin-input" type="text" placeholder="/modules">
          </label>
          <label class="field">
            <span>次按钮文字</span>
            <input v-model="form.hero.secondaryText" class="admin-input" type="text">
          </label>
          <label class="field">
            <span>次按钮链接</span>
            <input v-model="form.hero.secondaryLink" class="admin-input" type="text" placeholder="/articles">
          </label>
        </div>
        <div class="field">
          <span>Hero 图片</span>
          <CoverUploadField v-model="form.hero.imageUrl" :allow-manual-input="false" />
        </div>
      </div>
    </section>

    <section class="hc-panel">
      <div class="panel-head">
        <h3>模块图片配置</h3>
      </div>
      <div class="list-grid">
        <article v-for="item in form.modules" :key="item.moduleKey" class="item-card">
          <div class="item-head">
            <strong>{{ item.title || item.moduleKey }}</strong>
            <label class="inline-check">
              <input v-model="item.enabled" type="checkbox">
              展示
            </label>
          </div>
          <label class="field">
            <span>模块名称</span>
            <input v-model="item.title" class="admin-input" type="text">
          </label>
          <label class="field">
            <span>模块说明</span>
            <textarea v-model="item.desc" class="admin-textarea" rows="2"></textarea>
          </label>
          <div class="grid-2">
            <label class="field">
              <span>排序</span>
              <input v-model.number="item.sortOrder" class="admin-input" type="number" min="0">
            </label>
            <label class="field">
              <span>状态</span>
              <select v-model="item.status" class="admin-input">
                <option value="active">已开放</option>
                <option value="planned">规划中</option>
              </select>
            </label>
          </div>
          <div class="field">
            <span>模块封面图</span>
            <CoverUploadField v-model="item.coverUrl" :allow-manual-input="false" />
          </div>
        </article>
      </div>
    </section>

    <section class="hc-panel">
      <div class="panel-head">
        <h3>首页能力图配置</h3>
      </div>
      <div class="list-grid list-grid-ability">
        <article v-for="item in form.abilities" :key="item.abilityKey" class="item-card">
          <div class="item-head">
            <strong>{{ item.title || item.abilityKey }}</strong>
            <label class="inline-check">
              <input v-model="item.enabled" type="checkbox">
              展示
            </label>
          </div>
          <label class="field">
            <span>能力名称</span>
            <input v-model="item.title" class="admin-input" type="text">
          </label>
          <label class="field">
            <span>能力说明</span>
            <textarea v-model="item.desc" class="admin-textarea" rows="2"></textarea>
          </label>
          <label class="field">
            <span>排序</span>
            <input v-model.number="item.sortOrder" class="admin-input" type="number" min="0">
          </label>
          <div class="field">
            <span>能力图片</span>
            <CoverUploadField v-model="item.imageUrl" :allow-manual-input="false" />
          </div>
        </article>
      </div>
    </section>

    <section class="hc-panel">
      <div class="panel-head">
        <h3>平台基础能力背景图</h3>
      </div>
      <div class="field">
        <span>宽幅背景图 / 插画</span>
        <CoverUploadField v-model="form.foundation.imageUrl" :allow-manual-input="false" />
      </div>
    </section>

    <div class="actions">
      <button type="button" class="admin-btn" :disabled="loading" @click="loadConfig">
        {{ loading ? '加载中...' : '重新加载' }}
      </button>
      <button type="button" class="admin-btn primary" :disabled="saving" @click="saveConfig">
        {{ saving ? '保存中...' : '保存首页配置' }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { showToast } from 'vant'
import CoverUploadField from '@/components/admin/CoverUploadField.vue'
import { getAdminHomeConfig, saveAdminHomeConfig } from '@/api/adminContent.js'

const loading = ref(false)
const saving = ref(false)
const form = reactive({
  hero: {
    title: '',
    subtitle: '',
    primaryText: '',
    primaryLink: '',
    secondaryText: '',
    secondaryLink: '',
    imageUrl: ''
  },
  modules: [],
  abilities: [],
  foundation: {
    imageUrl: ''
  }
})

function applyConfig(data) {
  form.hero = { ...form.hero, ...(data?.hero || {}) }
  form.modules = Array.isArray(data?.modules) ? data.modules.map(item => ({ ...item })) : []
  form.abilities = Array.isArray(data?.abilities) ? data.abilities.map(item => ({ ...item })) : []
  form.foundation = { ...form.foundation, ...(data?.foundation || {}) }
}

async function loadConfig() {
  loading.value = true
  try {
    const data = await getAdminHomeConfig()
    applyConfig(data)
  } catch (error) {
    showToast({ type: 'fail', message: error.message || '加载首页配置失败' })
  } finally {
    loading.value = false
  }
}

async function saveConfig() {
  saving.value = true
  try {
    const payload = {
      hero: form.hero,
      modules: form.modules,
      abilities: form.abilities,
      foundation: form.foundation
    }
    const result = await saveAdminHomeConfig(payload)
    applyConfig(result)
    showToast({ type: 'success', message: '首页配置已保存' })
  } catch (error) {
    showToast({ type: 'fail', message: error.message || '保存首页配置失败' })
  } finally {
    saving.value = false
  }
}

loadConfig()
</script>

<style scoped>
.home-config-admin {
  display: grid;
  gap: 16px;
  padding: 8px 0 20px;
}

.hc-head h2 {
  margin: 0;
  font-size: 24px;
  color: #122039;
}

.hc-head p {
  margin: 8px 0 0;
  color: #64748b;
}

.hc-panel {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  padding: 18px;
  display: grid;
  gap: 14px;
}

.panel-head h3 {
  margin: 0;
  font-size: 18px;
  color: #122039;
}

.hero-form,
.field {
  display: grid;
  gap: 8px;
}

.field > span {
  font-size: 13px;
  color: #475569;
  font-weight: 700;
}

.grid-2 {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.list-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.list-grid-ability {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.item-card {
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 14px;
  display: grid;
  gap: 10px;
  background: #f8fafc;
}

.item-head {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  align-items: center;
}

.inline-check {
  display: inline-flex;
  gap: 6px;
  font-size: 12px;
  color: #475569;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

@media (max-width: 1023px) {
  .list-grid,
  .list-grid-ability,
  .grid-2 {
    grid-template-columns: 1fr;
  }
}
</style>
