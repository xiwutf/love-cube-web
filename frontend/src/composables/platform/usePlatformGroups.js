import { computed, reactive, ref } from 'vue'
import {
  fetchGroupDetail,
  fetchGroupMembers,
  fetchGroups,
  fetchGroupNotices,
  fetchGroupPosts,
  joinGroup,
  unwrapGroupPostsList,
  unwrapPlatformGroupList
} from '@/api/groups.js'

export function usePlatformGroups(groupIdRef = null) {
  const loading = ref(false)
  const detailLoading = ref(false)
  const groups = ref([])
  const total = ref(0)
  const detail = ref(null)
  const members = ref([])
  const posts = ref([])
  const notices = ref([])
  const filters = reactive({ keyword: '', page: 1, pageSize: 8 })

  const totalPages = computed(() => Math.max(1, Math.ceil(total.value / filters.pageSize)))

  async function loadGroups() {
    loading.value = true
    try {
      const res = await fetchGroups({ keyword: filters.keyword || undefined, page: filters.page, pageSize: filters.pageSize })
      const items = unwrapPlatformGroupList(res)
      groups.value = Array.isArray(items) ? items : []
      total.value = Number(res?.total || groups.value.length)
    } finally {
      loading.value = false
    }
  }

  async function loadDetail(id = groupIdRef?.value) {
    if (!id) return
    detailLoading.value = true
    try {
      detail.value = await fetchGroupDetail(id)
      const [mRes, pRes, nRes] = await Promise.allSettled([
        fetchGroupMembers(id, { status: 'approved' }),
        fetchGroupPosts(id, { page: 1, size: 50 }),
        fetchGroupNotices(id)
      ])
      members.value = mRes.status === 'fulfilled' ? (Array.isArray(mRes.value) ? mRes.value : mRes.value?.data || []) : []
      posts.value = pRes.status === 'fulfilled' ? unwrapGroupPostsList(pRes.value) : []
      notices.value = nRes.status === 'fulfilled' ? (Array.isArray(nRes.value) ? nRes.value : nRes.value?.data || []) : []
    } finally {
      detailLoading.value = false
    }
  }

  async function applyJoin(id, { message = '', memberRealName = '' } = {}) {
    await joinGroup(id, { message, memberRealName })
    await loadDetail(id)
  }

  return {
    loading,
    detailLoading,
    groups,
    total,
    totalPages,
    detail,
    members,
    posts,
    notices,
    filters,
    loadGroups,
    loadDetail,
    applyJoin
  }
}
