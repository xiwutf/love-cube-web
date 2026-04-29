<template>
  <section class="admin-page">
    <section class="platform-card">
      <h1 class="platform-title">新建团体</h1>
      <p class="platform-subtitle">填写团体基本信息，创建后可在编辑页继续完善</p>
    </section>

    <section class="platform-card form-card">
      <div class="form-grid">
        <label class="form-field required">
          <span class="field-label">团体名称</span>
          <input v-model="form.name" class="admin-input" placeholder="请输入团体名称" maxlength="100">
        </label>

        <label class="form-field">
          <span class="field-label">分类</span>
          <select v-model="form.category" class="admin-input">
            <option value="">请选择分类</option>
            <option v-for="cat in categories" :key="cat" :value="cat">{{ cat }}</option>
          </select>
        </label>

        <label class="form-field">
          <span class="field-label">加入方式</span>
          <select v-model="form.joinType" class="admin-input">
            <option value="approval">申请加入（管理员审核）</option>
            <option value="open">自由加入（无需审核）</option>
          </select>
        </label>

        <label class="form-field">
          <span class="field-label">状态</span>
          <select v-model="form.status" class="admin-input">
            <option value="active">正常</option>
            <option value="inactive">停用</option>
          </select>
        </label>

        <label class="form-field">
          <span class="field-label">封面图片地址</span>
          <input v-model="form.coverUrl" class="admin-input" placeholder="https://...">
        </label>

        <label class="form-field">
          <span class="field-label">置顶</span>
          <label class="admin-check">
            <input type="checkbox" v-model="form.pinned"> 置顶显示
          </label>
        </label>

        <label class="form-field full-width">
          <span class="field-label">团体简介</span>
          <textarea v-model="form.description" class="admin-textarea" placeholder="介绍团体的目标、活动方式等..." rows="5"></textarea>
        </label>
      </div>

      <div class="form-actions">
        <router-link to="/admin/platform/groups" class="admin-btn">取消</router-link>
        <button type="button" class="admin-btn primary" :disabled="saving || !form.name.trim()" @click="submit">
          {{ saving ? '创建中...' : '创建团体' }}
        </button>
      </div>
    </section>
  </section>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { createAdminGroup } from '@/api/groups.js'

const router = useRouter()
const saving = ref(false)

const categories = ['祈祷小组', '青年团体', '家庭小组', '读经小组', '公益服务', '其他']

const form = reactive({
  name: '',
  description: '',
  category: '',
  coverUrl: '',
  joinType: 'approval',
  status: 'active',
  pinned: false
})

async function submit() {
  if (!form.name.trim()) return
  saving.value = true
  try {
    const created = await createAdminGroup({ ...form })
    showToast({ message: '团体已创建', type: 'success' })
    router.push(`/admin/platform/groups/${created.id}/edit`)
  } catch (e) {
    showToast({ message: e.message || '创建失败', type: 'fail' })
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.form-card {
  margin-top: 16px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-field.full-width {
  grid-column: 1 / -1;
}

.form-field.required .field-label::after {
  content: ' *';
  color: #e84f73;
}

.field-label {
  font-size: 13px;
  font-weight: 700;
  color: #475569;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 28px;
  padding-top: 20px;
  border-top: 1px solid #e2e8f0;
}

.form-actions a {
  text-decoration: none;
}

.admin-check {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  color: #475569;
  cursor: pointer;
}

.admin-check input[type='checkbox'] {
  accent-color: #e84f73;
}

@media (max-width: 640px) {
  .form-grid { grid-template-columns: 1fr; }
}
</style>
