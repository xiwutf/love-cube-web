<template>
  <div class="fellowship-profile-page">
    <NavBar title="找对象资料" />

    <div v-if="store.loading" class="loading-wrap">
      <van-loading color="#ff6b8a" />
    </div>

    <template v-else>
      <div class="cert-card">
        <div class="cert-card-head">
          <span class="cert-title">认证标识</span>
          <button v-if="!isCertified" type="button" class="cert-cta" @click="router.push('/fellowship/verify')">
            去认证
          </button>
        </div>
        <div v-if="isCertified" class="cert-main">
          <span class="cert-badge-primary">
            <van-icon name="passed" class="cert-icon" />
            已认证用户
          </span>
          <span v-if="profile.photoVerified" class="cert-chip">真人</span>
          <span v-if="profile.realnameVerified" class="cert-chip">实名</span>
        </div>
        <p v-else class="cert-muted">完成真人或实名认证后，将在此处展示徽章，资料可信度更高。</p>
      </div>

      <div class="profile-card">
        <div class="row"><span>昵称</span><span>{{ profile.nickname || '-' }}</span></div>
        <div class="row"><span>性别</span><span>{{ profile.gender || '-' }}</span></div>
        <div class="row"><span>年龄</span><span>{{ profile.age || '-' }}</span></div>
        <div class="row"><span>城市</span><span>{{ profile.city || '-' }}</span></div>
        <div class="row"><span>职业</span><span>{{ profile.occupation || '-' }}</span></div>
        <div class="row"><span>学历</span><span>{{ profile.education || '-' }}</span></div>
        <div class="row"><span>身高</span><span>{{ profile.height ? `${profile.height}cm` : '-' }}</span></div>
        <div class="row"><span>交友意向</span><span>{{ profile.intention || '-' }}</span></div>
        <div class="row">
          <span>认证状态</span>
          <span class="verify-status-val" :class="verificationSummaryTone">{{ verificationSummary }}</span>
        </div>
        <div class="row"><span>资料状态</span><span>{{ profile.profileStatus || 'INCOMPLETE' }}</span></div>
      </div>

      <div class="completion-card">
        <div class="completion-header">
          <span>资料完整度</span>
          <strong>{{ completion.percent || 0 }}%</strong>
        </div>
        <van-progress :percentage="completion.percent || 0" color="#ff6b8a" stroke-width="8" />
        <p v-if="completion.missingFields?.length" class="missing">
          缺少字段：{{ completion.missingFields.join('、') }}
        </p>
      </div>

      <div class="action-wrap">
        <van-button round block type="primary" @click="router.push('/fellowship/profile/edit')">
          编辑资料
        </van-button>
      </div>
    </template>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import NavBar from '@/components/NavBar.vue'
import { useFellowshipProfileStore } from '@/stores/fellowshipProfile.js'
import { userHasVerificationBadge } from '@/utils/displayFields.js'

const router = useRouter()
const store = useFellowshipProfileStore()
const profile = computed(() => store.profile || {})
const completion = computed(() => store.completion || { percent: 0, missingFields: [] })

const isCertified = computed(() => userHasVerificationBadge(profile.value))

const verificationSummary = computed(() => {
  if (isCertified.value) {
    const parts = []
    if (profile.value.photoVerified) parts.push('真人')
    if (profile.value.realnameVerified) parts.push('实名')
    if (parts.length) return `已认证（${parts.join(' · ')}）`
    return '已认证'
  }
  const s = String(profile.value.verificationStatus || 'none').toLowerCase()
  if (s === 'pending') return '审核中'
  if (s === 'rejected') return '未通过'
  return '未认证'
})

const verificationSummaryTone = computed(() => {
  if (isCertified.value) return 'is-ok'
  const s = String(profile.value.verificationStatus || 'none').toLowerCase()
  if (s === 'pending') return 'is-warn'
  if (s === 'rejected') return 'is-bad'
  return 'is-muted'
})

onMounted(async () => {
  await Promise.all([store.fetchProfile(true), store.fetchCompletion(true)])
})
</script>

<style scoped>
.fellowship-profile-page { min-height: 100vh; background: #f8f8f8; padding-bottom: 24px; }
.loading-wrap { display: flex; justify-content: center; padding-top: 100px; }

.cert-card {
  margin: 12px;
  background: linear-gradient(135deg, #fff8fb 0%, #ffffff 55%, #f3f6ff 100%);
  border-radius: 14px;
  padding: 14px 16px;
  border: 1px solid rgba(255, 107, 138, 0.22);
  box-shadow: 0 8px 22px rgba(255, 107, 138, 0.08);
}

.cert-card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.cert-title {
  font-size: 15px;
  font-weight: 700;
  color: #2a2d3a;
}

.cert-cta {
  border: none;
  background: linear-gradient(135deg, #ff6b8a, #ff8fb8);
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  padding: 5px 12px;
  border-radius: 999px;
}

.cert-main {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.cert-badge-primary {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  font-weight: 800;
  color: #fff;
  padding: 6px 12px 6px 8px;
  border-radius: 999px;
  background: linear-gradient(135deg, #0d9f6e 0%, #3ecf9a 100%);
  box-shadow: 0 4px 12px rgba(14, 142, 102, 0.25);
}

.cert-icon {
  font-size: 16px;
}

.cert-chip {
  font-size: 11px;
  font-weight: 700;
  color: #276749;
  background: #e6ffef;
  border: 1px solid #9ae6b4;
  padding: 3px 8px;
  border-radius: 999px;
}

.cert-muted {
  margin: 0;
  font-size: 12px;
  color: #7a7f92;
  line-height: 1.5;
}

.profile-card, .completion-card {
  margin: 12px;
  background: #fff;
  border-radius: 12px;
  padding: 14px;
}
.row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f2f2f2;
  font-size: 14px;
}
.row:last-child { border-bottom: none; }

.verify-status-val {
  font-weight: 600;
}
.verify-status-val.is-ok { color: #276749; }
.verify-status-val.is-warn { color: #b45309; }
.verify-status-val.is-bad { color: #b91c1c; }
.verify-status-val.is-muted { color: #6b7280; }

.completion-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  font-size: 14px;
}
.missing {
  margin-top: 10px;
  font-size: 12px;
  color: #888;
}
.action-wrap { margin: 16px 12px; }
</style>
