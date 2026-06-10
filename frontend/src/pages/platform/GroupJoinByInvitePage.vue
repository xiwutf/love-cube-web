<template>
  <section class="join-invite-page">
    <header class="page-head">
      <h1>邀请加入团体</h1>
      <p>输入团长分享的邀请码，验证后即可加入</p>
    </header>

    <form class="code-form" @submit.prevent="handleLookup">
      <label for="invite-code">邀请码</label>
      <input
        id="invite-code"
        v-model.trim="inviteCode"
        type="text"
        maxlength="16"
        placeholder="例如：AB12CD34"
        autocomplete="off"
      />
      <button type="submit" class="btn-primary" :disabled="loading || !inviteCode">
        {{ loading ? '查询中…' : '查看团体' }}
      </button>
    </form>

    <p v-if="error" class="form-error">{{ error }}</p>

    <article v-if="preview" class="preview-card">
      <img v-if="preview.coverUrl" :src="preview.coverUrl" :alt="preview.name" class="preview-cover">
      <h2>{{ preview.name }}</h2>
      <p class="preview-meta">{{ preview.region || '未设置地区' }} · {{ preview.memberCount }} 人</p>
      <p class="preview-desc">{{ preview.description || '暂无简介' }}</p>
      <button type="button" class="btn-primary" @click="goJoin">
        前往加入
      </button>
    </article>
  </section>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { fetchGroupByInviteCode } from '@/api/groups.js'
import { usePlatformPath } from '@/composables/usePlatformPath.js'

const route = useRoute()
const router = useRouter()
const { groupsPath } = usePlatformPath()

const inviteCode = ref(String(route.query.code || '').trim().toUpperCase())
const loading = ref(false)
const error = ref('')
const preview = ref(null)

async function handleLookup() {
  const code = inviteCode.value.trim().toUpperCase()
  if (!code) return
  loading.value = true
  error.value = ''
  preview.value = null
  try {
    preview.value = await fetchGroupByInviteCode(code)
  } catch (e) {
    error.value = e.message || '邀请码无效'
  } finally {
    loading.value = false
  }
}

function goJoin() {
  if (!preview.value?.id || !inviteCode.value) return
  router.push({
    path: groupsPath(String(preview.value.id)),
    query: { invite: inviteCode.value.trim().toUpperCase() }
  })
}

watch(
  () => route.query.code,
  (code) => {
    if (code) {
      inviteCode.value = String(code).trim().toUpperCase()
      handleLookup()
    }
  },
  { immediate: true }
)
</script>

<style scoped>
.join-invite-page {
  max-width: 560px;
  margin: 0 auto;
  padding: 24px 16px 48px;
}

.page-head h1 {
  margin: 0;
  font-size: 22px;
  color: #111827;
}

.page-head p {
  margin: 8px 0 0;
  font-size: 14px;
  color: #64748b;
}

.code-form {
  margin-top: 20px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.code-form label {
  font-size: 13px;
  color: #334155;
}

.code-form input {
  padding: 12px 14px;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  font-size: 16px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.btn-primary {
  padding: 12px 16px;
  border: none;
  border-radius: 10px;
  background: var(--lc-pink, #ff5f84);
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.form-error {
  margin-top: 12px;
  color: #b91c1c;
  font-size: 13px;
}

.preview-card {
  margin-top: 20px;
  padding: 16px;
  border-radius: 14px;
  border: 1px solid #e2e8f0;
  background: #fff;
}

.preview-cover {
  width: 100%;
  height: 140px;
  object-fit: cover;
  border-radius: 10px;
  margin-bottom: 12px;
}

.preview-card h2 {
  margin: 0;
  font-size: 18px;
}

.preview-meta {
  margin: 6px 0 0;
  font-size: 13px;
  color: #64748b;
}

.preview-desc {
  margin: 10px 0 16px;
  font-size: 14px;
  color: #334155;
  line-height: 1.5;
}
</style>
