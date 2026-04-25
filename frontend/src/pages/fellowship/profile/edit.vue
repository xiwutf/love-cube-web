<template>
  <div class="fellowship-profile-edit-page">
    <NavBar title="编辑联谊资料" />

    <van-form @submit="handleSubmit" class="form-wrap">
      <van-cell-group inset>
        <van-field v-model="form.nickname" label="昵称" placeholder="请输入昵称" />
        <van-field v-model="form.gender" label="性别" placeholder="male/female" />
        <van-field v-model="form.birthYear" label="出生年份" type="number" placeholder="例如 1998" />
        <van-field v-model="form.city" label="城市" placeholder="请输入城市" />
        <van-field v-model="form.occupation" label="职业" placeholder="请输入职业" />
        <van-field v-model="form.education" label="学历" placeholder="请输入学历" />
        <van-field v-model="form.height" label="身高(cm)" type="number" placeholder="例如 170" />
        <van-field v-model="form.bio" label="自我介绍" type="textarea" rows="2" autosize />
        <van-field v-model="form.intention" label="交友意向" type="textarea" rows="2" autosize />
        <van-field v-model="form.tags" label="标签" placeholder="多个标签用逗号分隔" />
        <van-field v-model="form.avatarUrl" label="头像URL" placeholder="请输入头像地址" />
      </van-cell-group>

      <div class="btn-wrap">
        <van-button round block type="primary" native-type="submit" :loading="store.saving">
          保存资料
        </van-button>
      </div>
    </van-form>
  </div>
</template>

<script setup>
import { onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import NavBar from '@/components/NavBar.vue'
import { useFellowshipProfileStore } from '@/stores/fellowshipProfile.js'

const router = useRouter()
const store = useFellowshipProfileStore()

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

onMounted(async () => {
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
})

async function handleSubmit() {
  await store.saveProfile({
    nickname: form.nickname,
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
  await store.fetchCompletion()
  showToast({ message: '保存成功', type: 'success' })
  router.replace('/fellowship/profile')
}
</script>

<style scoped>
.fellowship-profile-edit-page { min-height: 100vh; background: #f8f8f8; }
.form-wrap { padding: 12px 0 24px; }
.btn-wrap { margin: 20px 16px 0; }
</style>

