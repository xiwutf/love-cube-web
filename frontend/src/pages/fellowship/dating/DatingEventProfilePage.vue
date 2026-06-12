<template>
  <div class="dating-profile-page">
    <NavBar title="本场活动资料" left-arrow @click-left="router.back()" />

    <div v-if="loading" class="loading-wrap"><van-loading size="24" /></div>

    <van-form v-else @submit="handleSave">
      <van-cell-group inset>
        <van-field name="genderSide" label="本场编号性别">
          <template #input>
            <van-radio-group v-model="form.genderSide" direction="horizontal">
              <van-radio name="MALE">男</van-radio>
              <van-radio name="FEMALE">女</van-radio>
            </van-radio-group>
          </template>
        </van-field>
        <van-field v-model.number="form.age" type="digit" label="年龄" placeholder="请输入年龄" />
        <van-field v-model.number="form.heightCm" type="digit" label="身高(cm)" placeholder="例如 172" />
        <van-field v-model="form.city" label="城市" placeholder="例如 保定" />
        <van-field v-model="form.occupation" label="职业" placeholder="例如 产品经理" />
        <van-field v-model="form.education" label="学历" placeholder="例如 本科" />
        <van-field
          v-model="tagsText"
          label="兴趣标签"
          placeholder="多个标签用逗号分隔"
        />
        <van-field
          v-model="form.selfIntro"
          rows="3"
          autosize
          type="textarea"
          maxlength="500"
          show-word-limit
          label="自我介绍"
          placeholder="用一两句话介绍自己"
        />
        <van-field
          v-model="form.partnerRequirements"
          rows="2"
          autosize
          type="textarea"
          maxlength="300"
          show-word-limit
          label="择偶要求"
          placeholder="可选"
        />
        <van-field
          v-model="form.idealPartnerDesc"
          rows="2"
          autosize
          type="textarea"
          maxlength="300"
          show-word-limit
          label="理想对象描述"
          placeholder="可选"
        />
      </van-cell-group>

      <p class="hint">以上资料仅在本场联谊活动中展示，不会修改你的平台个人资料。</p>

      <div class="actions">
        <van-button round block type="primary" native-type="submit" :loading="saving" color="#ff5f84">
          保存并生成介绍卡
        </van-button>
      </div>
    </van-form>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import NavBar from '@/components/NavBar.vue'
import { fetchDatingProfile, saveDatingProfile } from '@/api/datingEvent.js'

const route = useRoute()
const router = useRouter()
const eventId = computed(() => route.params.eventId)

const loading = ref(true)
const saving = ref(false)
const form = reactive({
  genderSide: 'MALE',
  age: null,
  heightCm: null,
  city: '',
  occupation: '',
  education: '',
  interestTags: [],
  selfIntro: '',
  partnerRequirements: '',
  idealPartnerDesc: ''
})

const tagsText = computed({
  get() {
    return Array.isArray(form.interestTags) ? form.interestTags.join('，') : ''
  },
  set(value) {
    form.interestTags = String(value || '')
      .split(/[,，]/)
      .map((s) => s.trim())
      .filter(Boolean)
  }
})

async function load() {
  loading.value = true
  try {
    const data = await fetchDatingProfile(eventId.value)
    Object.assign(form, {
      genderSide: data.genderSide || 'MALE',
      age: data.age ?? null,
      heightCm: data.heightCm ?? null,
      city: data.city || '',
      occupation: data.occupation || '',
      education: data.education || '',
      interestTags: Array.isArray(data.interestTags) ? data.interestTags : [],
      selfIntro: data.selfIntro || '',
      partnerRequirements: data.partnerRequirements || '',
      idealPartnerDesc: data.idealPartnerDesc || ''
    })
  } catch (e) {
    showToast(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

async function handleSave() {
  if (!form.age || !form.city?.trim() || !form.selfIntro?.trim()) {
    showToast('请至少填写年龄、城市和自我介绍')
    return
  }
  saving.value = true
  try {
    await saveDatingProfile(eventId.value, { ...form })
    showToast({ message: '已保存', type: 'success' })
    router.replace(`/fellowship/events/${eventId.value}/dating/my-card`)
  } catch (e) {
    showToast(e.message || '保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.dating-profile-page {
  min-height: 100vh;
  background: #f8fafc;
  padding-bottom: 24px;
}

.loading-wrap {
  padding: 48px;
  text-align: center;
}

.hint {
  margin: 12px 16px 0;
  font-size: 12px;
  color: #64748b;
  line-height: 1.6;
}

.actions {
  margin: 20px 16px 0;
}
</style>
