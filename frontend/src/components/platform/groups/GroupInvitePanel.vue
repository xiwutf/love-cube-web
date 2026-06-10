<template>
  <section v-if="visible" class="group-invite-panel">
    <div class="panel-head">
      <h3>邀请成员</h3>
      <p class="panel-sub">分享邀请码或链接，成员验证通过后可加入团体</p>
    </div>
    <p v-if="flash" class="panel-flash">{{ flash }}</p>
    <div v-if="loading" class="panel-loading">加载邀请信息…</div>
    <template v-else-if="inviteCode">
      <div class="code-row">
        <span class="code-label">邀请码</span>
        <strong class="code-value">{{ inviteCode }}</strong>
        <button type="button" class="btn-copy" @click="copyText(inviteCode, '邀请码已复制')">复制</button>
      </div>
      <div class="link-row">
        <span class="code-label">邀请链接</span>
        <p class="link-value">{{ shareUrl }}</p>
        <button type="button" class="btn-copy" @click="copyText(shareUrl, '链接已复制')">复制链接</button>
      </div>
      <div class="panel-actions">
        <button type="button" class="btn-secondary" :disabled="refreshing" @click="refreshCode">
          {{ refreshing ? '更新中…' : '重新生成邀请码' }}
        </button>
      </div>
      <p class="panel-hint">重新生成后，旧邀请码与链接将失效</p>
    </template>
    <p v-else-if="error" class="panel-error">{{ error }}</p>
  </section>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import {
  fetchGroupInviteInfo,
  isNumericPlatformGroupId,
  refreshGroupInviteCode
} from '@/api/groups.js'
import { usePlatformPath } from '@/composables/usePlatformPath.js'

const props = defineProps({
  groupId: { type: [String, Number], required: true },
  joinModeKey: { type: String, default: '' },
  managed: { type: Boolean, default: false }
})

const { groupsPath } = usePlatformPath()

const loading = ref(false)
const refreshing = ref(false)
const inviteCode = ref('')
const error = ref('')
const flash = ref('')

const visible = computed(() =>
  props.managed && props.joinModeKey === 'invite' && isNumericPlatformGroupId(props.groupId)
)

const shareUrl = computed(() => {
  if (!inviteCode.value || !props.groupId) return ''
  const path = groupsPath(String(props.groupId))
  const q = `?invite=${encodeURIComponent(inviteCode.value)}`
  if (typeof window === 'undefined') return `${path}${q}`
  return `${window.location.origin}/#${path}${q}`
})

function showFlash(text) {
  flash.value = text
  window.setTimeout(() => { flash.value = '' }, 2200)
}

async function loadInviteInfo() {
  if (!visible.value || !props.groupId) return
  loading.value = true
  error.value = ''
  try {
    const data = await fetchGroupInviteInfo(props.groupId)
    inviteCode.value = String(data?.inviteCode || '').trim()
  } catch (e) {
    inviteCode.value = ''
    error.value = e.message || '邀请信息加载失败'
  } finally {
    loading.value = false
  }
}

async function refreshCode() {
  if (refreshing.value) return
  if (!window.confirm('重新生成邀请码？旧链接将失效。')) return
  refreshing.value = true
  try {
    const data = await refreshGroupInviteCode(props.groupId)
    inviteCode.value = String(data?.inviteCode || '').trim()
    showFlash(data?.message || '邀请码已更新')
  } catch (e) {
    showFlash(e.message || '更新失败')
  } finally {
    refreshing.value = false
  }
}

async function copyText(text, okMsg) {
  if (!text) return
  try {
    await navigator.clipboard.writeText(text)
    showFlash(okMsg)
  } catch {
    showFlash('复制失败，请手动复制')
  }
}

watch(
  () => [props.groupId, props.managed, props.joinModeKey],
  () => loadInviteInfo(),
  { immediate: true }
)

onMounted(loadInviteInfo)
</script>

<style scoped>
.group-invite-panel {
  margin: 12px 0;
  padding: 14px 16px;
  border-radius: 12px;
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
}

.panel-head h3 {
  margin: 0;
  font-size: 15px;
  color: #14532d;
}

.panel-sub {
  margin: 6px 0 0;
  font-size: 12px;
  color: #166534;
}

.panel-flash {
  margin: 8px 0 0;
  font-size: 12px;
  color: #15803d;
}

.panel-loading,
.panel-error {
  margin: 10px 0 0;
  font-size: 13px;
  color: #64748b;
}

.panel-error {
  color: #b91c1c;
}

.code-row,
.link-row {
  margin-top: 12px;
}

.code-label {
  display: block;
  font-size: 12px;
  color: #166534;
  margin-bottom: 4px;
}

.code-value {
  font-size: 22px;
  letter-spacing: 0.12em;
  color: #15803d;
}

.link-value {
  margin: 0 0 8px;
  font-size: 12px;
  color: #334155;
  word-break: break-all;
  line-height: 1.5;
}

.btn-copy {
  margin-top: 6px;
  padding: 6px 12px;
  border-radius: 8px;
  border: 1px solid #86efac;
  background: #fff;
  color: #15803d;
  font-size: 13px;
  cursor: pointer;
}

.panel-actions {
  margin-top: 12px;
}

.btn-secondary {
  padding: 8px 14px;
  border-radius: 8px;
  border: 1px solid #cbd5e1;
  background: #fff;
  color: #334155;
  font-size: 13px;
  cursor: pointer;
}

.panel-hint {
  margin: 8px 0 0;
  font-size: 11px;
  color: #64748b;
}
</style>
