<template>
  <div class="fellowship-profile-edit-page">
    <NavBar title="编辑找对象资料" />

    <button
      v-if="verifyBannerVisible"
      type="button"
      class="verify-banner"
      @click="router.push('/fellowship/verify')"
    >
      <van-icon name="passed" class="verify-banner-icon" />
      <span class="verify-banner-text">您已通过认证 · 个人资料展示认证标识</span>
      <van-icon name="arrow" class="verify-banner-arrow" />
    </button>

    <van-form class="form-wrap" @submit="handleSubmit">
      <van-cell-group inset>
        <van-field v-model="form.nickname" label="昵称" placeholder="请输入昵称（最多20字）" maxlength="20" />
        <van-field
          :model-value="genderLabel"
          label="性别"
          placeholder="请选择性别"
          readonly
          is-link
          @click="showGenderPicker = true"
        />
        <van-field v-model="form.birthYear" label="出生年份" type="number" placeholder="例如 1998" />
        <van-field
          :model-value="cityLabel"
          label="城市"
          placeholder="请选择城市"
          readonly
          is-link
          @click="showCityPicker = true"
        />
        <van-field v-model="form.occupation" label="职业" placeholder="请输入职业" />
        <van-field
          :model-value="educationLabel"
          label="学历"
          placeholder="请选择学历"
          readonly
          is-link
          @click="showEducationPicker = true"
        />
        <van-field v-model="form.height" label="身高(cm)" type="number" placeholder="例如 170" />
        <van-field v-model="form.bio" label="自我介绍" type="textarea" rows="2" autosize />
        <van-field v-model="form.intention" label="交友意向" type="textarea" rows="2" autosize />
        <van-field v-model="form.tags" label="标签" placeholder="多个标签用逗号分隔" />
        <van-field v-model="form.avatarUrl" label="头像 URL" placeholder="请输入头像地址" />
      </van-cell-group>

      <div class="btn-wrap">
        <van-button round block type="primary" native-type="submit" :loading="store.saving" :disabled="store.saving">
          保存资料
        </van-button>
      </div>
    </van-form>

    <van-popup v-model:show="showGenderPicker" position="bottom" round>
      <van-picker :columns="genderOptions" @confirm="onGenderConfirm" @cancel="showGenderPicker = false" />
    </van-popup>

    <van-popup v-model:show="showEducationPicker" position="bottom" round>
      <van-picker :columns="educationOptions" @confirm="onEducationConfirm" @cancel="showEducationPicker = false" />
    </van-popup>

    <van-popup v-model:show="showCityPicker" position="bottom" round>
      <van-area
        v-model="cityCode"
        title="选择省市"
        :columns-num="2"
        :area-list="areaList"
        :columns-placeholder="['请选择', '请选择']"
        @confirm="onCityConfirm"
        @cancel="showCityPicker = false"
      />
    </van-popup>
  </div>
</template>

<script setup>
import { areaList } from '@vant/area-data'
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import NavBar from '@/components/NavBar.vue'
import { useFellowshipProfileStore } from '@/stores/fellowshipProfile.js'
import { userHasVerificationBadge } from '@/utils/displayFields.js'

const router = useRouter()
const store = useFellowshipProfileStore()

const verifyBannerVisible = computed(() => userHasVerificationBadge(store.profile ?? {}))

const form = reactive({
  nickname: '',
  gender: '',
  birthYear: '',
  city: '',
  occupation: '',
  education: '',
  height: '',
  bio: '',
  intention: '',
  tags: '',
  avatarUrl: ''
})

const showGenderPicker = ref(false)
const showEducationPicker = ref(false)
const showCityPicker = ref(false)
const cityCode = ref('')

const genderOptions = [
  { text: '男', value: 'male' },
  { text: '女', value: 'female' }
]

const educationOptions = [
  { text: '高中及以下', value: '高中及以下' },
  { text: '大专', value: '大专' },
  { text: '本科', value: '本科' },
  { text: '硕士', value: '硕士' },
  { text: '博士', value: '博士' }
]

const genderLabelMap = {
  male: '男',
  female: '女',
  男: '男',
  女: '女'
}

const genderLabel = computed(() => genderLabelMap[form.gender] || form.gender || '')
const educationLabel = computed(() => form.education || '')
const cityLabel = computed(() => form.city || '')

onMounted(async () => {
  try {
    const data = await store.fetchProfile()
    form.nickname = data.nickname || ''
    form.gender = data.gender || ''
    form.birthYear = data.birthYear || ''
    form.city = data.city || ''
    form.occupation = data.occupation || ''
    form.education = data.education || ''
    form.height = data.height || ''
    form.bio = data.bio || ''
    form.intention = data.intention || ''
    form.tags = data.tags || ''
    form.avatarUrl = data.avatarUrl || ''
    cityCode.value = findCityCodeByName(form.city)
  } catch (e) {
    showToast({ message: e?.message || '资料加载失败，请稍后重试', type: 'fail' })
  }
})

function onGenderConfirm({ selectedValues }) {
  form.gender = selectedValues[0]
  showGenderPicker.value = false
}

function onEducationConfirm({ selectedValues }) {
  form.education = selectedValues[0]
  showEducationPicker.value = false
}

function onCityConfirm({ selectedOptions, selectedValues }) {
  const provinceName = selectedOptions?.[0]?.text || ''
  const cityName = selectedOptions?.[1]?.text || ''
  form.city = [provinceName, cityName].filter(Boolean).join(' ')
  cityCode.value = selectedValues?.[1] || selectedValues?.[0] || ''
  showCityPicker.value = false
}

function findCityCodeByName(cityText) {
  if (!cityText) return ''
  const normalized = String(cityText).replace(/\s+/g, ' ').trim()
  const cityList = areaList.city_list || {}
  const provinceList = areaList.province_list || {}

  for (const [code, name] of Object.entries(cityList)) {
    if (normalized === name || normalized.endsWith(` ${name}`)) {
      return code
    }
  }

  for (const [code, name] of Object.entries(provinceList)) {
    if (normalized === name) return code
  }

  return ''
}

async function handleSubmit() {
  const nickname = String(form.nickname || '').trim()
  if (nickname.length > 20) {
    showToast({ message: '昵称最多 20 个字符', type: 'fail' })
    return
  }
  try {
    await store.saveProfile({
      nickname,
      gender: form.gender,
      birthYear: form.birthYear ? Number(form.birthYear) : null,
      city: form.city,
      occupation: form.occupation,
      education: form.education,
      height: form.height ? Number(form.height) : null,
      bio: form.bio,
      intention: form.intention,
      tags: form.tags,
      avatarUrl: form.avatarUrl
    })
    await store.fetchProfile(true)
    await store.fetchCompletion(true)
    showToast({ message: '保存成功', type: 'success' })
    router.replace('/fellowship/profile')
  } catch (e) {
    console.error('[profile/edit] save failed:', e)
    showToast({ message: e?.response?.data?.message || e?.message || '保存失败，请稍后重试', type: 'fail' })
  }
}
</script>

<style scoped>
.fellowship-profile-edit-page { min-height: 100vh; background: #f8f8f8; }

.verify-banner {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 10px 12px 0;
  padding: 10px 12px;
  border: none;
  border-radius: 12px;
  width: calc(100% - 24px);
  box-sizing: border-box;
  text-align: left;
  background: linear-gradient(135deg, #ecfdf5 0%, #d1fae5 45%, #e0f2fe 100%);
  color: #065f46;
  font-size: 13px;
  font-weight: 600;
  box-shadow: 0 4px 14px rgba(6, 95, 70, 0.12);
}

.verify-banner-icon {
  font-size: 18px;
  flex-shrink: 0;
}

.verify-banner-text {
  flex: 1;
  min-width: 0;
  line-height: 1.35;
}

.verify-banner-arrow {
  font-size: 14px;
  color: #047857;
  flex-shrink: 0;
}

.form-wrap { padding: 12px 0 24px; }
.btn-wrap { margin: 20px 16px 0; }
</style>


