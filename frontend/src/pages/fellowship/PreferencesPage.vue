<template>
  <div class="preferences-page">
    <NavBar title="择偶条件" />

    <van-form class="form-wrap" @submit="handleSave">
      <van-cell-group inset>
        <van-field
          :model-value="`${form.minAge} - ${form.maxAge} 宀乣`"
          label="年龄范围"
          readonly
          is-link
          @click="showAgePicker = true"
        />
        <van-field
          :model-value="genderLabel"
          label="性别偏好"
          readonly
          is-link
          @click="showGenderPicker = true"
        />
        <van-field
          :model-value="form.city"
          label="城市偏好"
          placeholder="请输入期望城市"
          clearable
          @update:model-value="(val) => (form.city = val)"
        />
        <van-field
          :model-value="form.education"
          label="学历偏好"
          readonly
          is-link
          @click="showEducationPicker = true"
        />
        <van-field
          :model-value="form.marriagePlan"
          label="濠氭亱瑙勫垝"
          readonly
          is-link
          @click="showPlanPicker = true"
        />
        <van-field
          :model-value="form.note"
          label="补充说明"
          type="textarea"
          rows="2"
          autosize
          placeholder="可补充个人偏好与沟通方式"
          @update:model-value="(val) => (form.note = val)"
        />
      </van-cell-group>

      <div class="action-wrap">
        <van-button round block type="primary" native-type="submit">保存择偶条件</van-button>
      </div>
    </van-form>

    <van-popup v-model:show="showGenderPicker" position="bottom" round>
      <van-picker :columns="genderOptions" @confirm="onGenderConfirm" @cancel="showGenderPicker = false" />
    </van-popup>

    <van-popup v-model:show="showEducationPicker" position="bottom" round>
      <van-picker :columns="educationOptions" @confirm="onEducationConfirm" @cancel="showEducationPicker = false" />
    </van-popup>

    <van-popup v-model:show="showPlanPicker" position="bottom" round>
      <van-picker :columns="planOptions" @confirm="onPlanConfirm" @cancel="showPlanPicker = false" />
    </van-popup>

    <van-popup v-model:show="showAgePicker" position="bottom" round>
      <div class="age-popup">
        <h3>选择年龄范围</h3>
        <p>{{ form.minAge }} - {{ form.maxAge }} 宀</p>
        <van-slider v-model="ageRange" range :min="18" :max="60" bar-height="4" active-color="#ff5f84" />
        <van-button round block type="primary" @click="confirmAgeRange">确定</van-button>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import NavBar from '@/components/NavBar.vue'

const STORAGE_KEY = 'fellowship-preferences'
const router = useRouter()

const saved = JSON.parse(localStorage.getItem(STORAGE_KEY) || '{}')
const form = reactive({
  minAge: Number(saved.minAge || 22),
  maxAge: Number(saved.maxAge || 32),
  gender: saved.gender || 'female',
  city: saved.city || '',
  education: saved.education || '涓嶉檺',
  marriagePlan: saved.marriagePlan || '丢年内考虑',
  note: saved.note || ''
})

const ageRange = ref([form.minAge, form.maxAge])
const showAgePicker = ref(false)
const showGenderPicker = ref(false)
const showEducationPicker = ref(false)
const showPlanPicker = ref(false)

const genderOptions = [
  { text: '涓嶉檺', value: 'any' },
  { text: '男生', value: 'male' },
  { text: '濂崇敓', value: 'female' }
]
const educationOptions = [
  { text: '涓嶉檺', value: '涓嶉檺' },
  { text: '大专及以上', value: '大专及以上' },
  { text: '本科及以上', value: '本科及以上' },
  { text: '硕士及以上', value: '硕士及以上' }
]
const planOptions = [
  { text: '半年内虑', value: '半年内虑' },
  { text: '丢年内考虑', value: '丢年内考虑' },
  { text: '两年内虑', value: '两年内虑' },
  { text: '先了解再决定', value: '先了解再决定' }
]

const genderLabel = computed(() => {
  const map = { any: '涓嶉檺', male: '男生', female: '濂崇敓' }
  return map[form.gender] || '涓嶉檺'
})

function onGenderConfirm({ selectedValues }) {
  form.gender = selectedValues[0]
  showGenderPicker.value = false
}

function onEducationConfirm({ selectedValues }) {
  form.education = selectedValues[0]
  showEducationPicker.value = false
}

function onPlanConfirm({ selectedValues }) {
  form.marriagePlan = selectedValues[0]
  showPlanPicker.value = false
}

function confirmAgeRange() {
  form.minAge = Number(ageRange.value[0])
  form.maxAge = Number(ageRange.value[1])
  showAgePicker.value = false
}

function handleSave() {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(form))
  showToast({ type: 'success', message: '择偶条件已保存' })
  router.back()
}
</script>

<style scoped>
.preferences-page {
  min-height: 100vh;
  background: #f8f8f8;
}

.form-wrap {
  padding: 12px 0 24px;
}

.action-wrap {
  margin: 20px 16px 0;
}

.age-popup {
  padding: 16px;
}

.age-popup h3 {
  margin: 0;
  font-size: 16px;
}

.age-popup p {
  margin: 10px 0;
  color: #666;
  font-size: 13px;
}
</style>

