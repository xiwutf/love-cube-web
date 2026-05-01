<template>
  <section class="group-detail-page">
    <div v-if="loading.detail" class="loading-state">加载团体详情中...</div>
    <div v-else-if="errors.detail" class="error-state">
      <h2>团体详情加载失败</h2>
      <p>{{ errors.detail }}</p>
      <button type="button" @click="loadDetail">重试</button>
      <router-link to="/platform/groups">返回团体列表</router-link>
    </div>

    <template v-else-if="group">
      <section class="hero-card">
        <div class="hero-cover" :style="{ backgroundImage: `url(${heroImage})` }"></div>
        <div class="hero-body">
          <img class="group-avatar" :src="heroImage" :alt="group.name">
          <div class="hero-main">
            <div class="title-row">
              <h1>{{ group.name }}</h1>
              <span>{{ group.category }}</span>
            </div>
            <p class="meta-line">
              {{ group.region }} · {{ group.memberCount }} 人
              <template v-if="group.postCount != null"> · {{ group.postCount }} 条动态</template>
              · {{ group.joinLabel }}
            </p>
            <p class="group-desc">{{ group.description }}</p>
          </div>
          <div class="hero-actions">
            <button type="button" class="join-btn" :disabled="joinDisabled || joining" @click="applyJoin">
              {{ joining ? '处理中...' : joinButtonText }}
            </button>
            <router-link
              v-if="group.managed"
              class="manage-link"
              :to="`/platform/groups/${group.id}/members`"
            >团体管理</router-link>
          </div>
        </div>
      </section>

      <nav class="tabs" aria-label="团体内容">
        <router-link v-for="tab in tabs" :key="tab.key" :to="tab.to" :class="{ active: activeTab === tab.key }">
          {{ tab.label }}
        </router-link>
      </nav>

      <p v-if="message" class="page-message" :class="{ error: messageType === 'error' }">{{ message }}</p>

      <section class="content-grid">
        <main class="main-panel">
          <template v-if="activeTab === 'home'">
            <section class="panel-card">
              <div class="section-head">
                <h2>最新公告</h2>
                <router-link :to="`/platform/groups/${group.id}/notices`">查看全部</router-link>
              </div>
              <article v-if="latestNotice" class="notice-card compact">
                <strong>{{ latestNotice.title }}</strong>
                <p>{{ latestNotice.content }}</p>
                <time>{{ latestNotice.date }}</time>
              </article>
              <div v-else-if="errors.notices" class="inline-error">{{ errors.notices }}</div>
              <div v-else class="empty-inline">暂无公告</div>
            </section>

            <section class="panel-card">
              <div class="section-head">
                <h2>最新动态</h2>
                <router-link :to="`/platform/groups/${group.id}/posts`">查看全部</router-link>
              </div>
              <PostList :posts="posts.slice(0, 1)" :error="errors.posts" empty-text="暂无动态" />
            </section>

            <section class="home-grid">
              <article class="panel-card info-card">
                <h2>团体资料</h2>
                <InfoList :group="group" />
              </article>
              <article class="panel-card admins-card">
                <h2>管理员</h2>
                <div v-if="admins.length" class="admin-list">
                  <div v-for="admin in admins" :key="admin.userId || admin.name" class="admin-item">
                    <img :src="admin.avatar" :alt="admin.name">
                    <span>{{ admin.name }}</span>
                    <em>{{ admin.roleLabel }}</em>
                  </div>
                </div>
                <div v-else class="empty-inline">暂无管理员信息</div>
              </article>
              <article class="panel-card activity-placeholder">
                <h2>近期活动</h2>
                <div>
                  <strong>活动能力建设中</strong>
                  <p>后续会在这里展示团体近期活动、报名状态与地点时间。</p>
                </div>
              </article>
            </section>
          </template>

          <section v-else-if="activeTab === 'posts'" class="panel-card">
            <div class="section-head">
              <h2>团体动态</h2>
            </div>
            <template v-if="group.isMember">
              <form class="post-form" @submit.prevent="submitPost">
                <textarea v-model.trim="postForm.content" rows="4" maxlength="2000" placeholder="分享团体动态..."></textarea>
                <input v-model.trim="postForm.imageUrls" type="text" placeholder="图片 URL，可用英文逗号分隔（可选）">
                <div class="post-form-foot">
                  <span>至少填写文字或图片链接之一</span>
                  <button type="submit" class="primary-btn" :disabled="posting">{{ posting ? '发布中...' : '发布' }}</button>
                </div>
              </form>
            </template>
            <p v-else class="join-hint">加入团体后可以参与动态互动。</p>
            <p v-if="errors.posts" class="inline-error">{{ errors.posts }}</p>
            <template v-else-if="!posts.length">
              <div class="empty-inline">{{ emptyPostsHint }}</div>
            </template>
            <div v-else class="post-list post-list-feed">
              <article v-for="post in posts" :key="post.id" class="post-card">
                <div class="post-head">
                  <img :src="post.avatar" alt="">
                  <div>
                    <strong>{{ post.author }}</strong>
                    <span>{{ post.time }}</span>
                  </div>
                  <em>{{ post.role }}</em>
                  <button
                    v-if="canDeletePost(post)"
                    type="button"
                    class="post-delete-btn"
                    :disabled="deletingPostId === post.id"
                    @click="deletePost(post)"
                  >{{ deletingPostId === post.id ? '…' : '删除' }}</button>
                </div>
                <p>{{ post.content }}</p>
                <div v-if="post.images.length" class="post-images">
                  <img v-for="img in post.images" :key="img" :src="img" alt="">
                </div>
                <div class="post-actions">
                  <button
                    type="button"
                    :class="{ active: post.likedByMe }"
                    :disabled="!userStore.isLoggedIn || likingPostId === post.id"
                    @click="togglePostLike(post)"
                  >{{ post.likedByMe ? '已赞' : '赞' }} {{ post.likes }}</button>
                  <button type="button" @click="toggleCommentPanel(post)">
                    评论 {{ post.comments }}
                  </button>
                </div>
                <div v-if="expandedPostId === post.id" class="comment-panel">
                  <p v-if="commentsLoading[post.id]" class="comment-status">评论加载中...</p>
                  <ul v-else class="comment-list">
                    <li v-for="c in (feedComments[post.id] || [])" :key="c.id">
                      <img :src="c.avatar" alt="">
                      <div class="comment-body">
                        <strong>{{ c.author }}</strong>
                        <span>{{ c.time }}</span>
                        <p>{{ c.content }}</p>
                      </div>
                      <button
                        v-if="canDeleteComment(c)"
                        type="button"
                        class="comment-del"
                        :disabled="deletingCommentKey === commentKey(post.id, c.id)"
                        @click="deleteComment(post, c)"
                      >删</button>
                    </li>
                    <li v-if="!(feedComments[post.id] || []).length" class="comment-empty">暂无评论</li>
                  </ul>
                  <form v-if="userStore.isLoggedIn" class="comment-form" @submit.prevent="submitComment(post)">
                    <input v-model.trim="commentDraft[post.id]" type="text" maxlength="500" placeholder="写一条评论…">
                    <button type="submit" class="primary-btn small" :disabled="commentPosting[post.id]">
                      {{ commentPosting[post.id] ? '发送中' : '发送' }}
                    </button>
                  </form>
                  <p v-else class="comment-login-hint">登录后即可评论</p>
                </div>
              </article>
            </div>
          </section>

          <section v-else-if="activeTab === 'members'" class="panel-card">
            <div class="section-head member-tools">
              <h2>成员 {{ filteredMembers.length }}</h2>
              <div>
                <input v-model.trim="memberKeyword" type="search" placeholder="搜索成员">
                <select v-model="roleFilter" aria-label="角色筛选">
                  <option value="">全部角色</option>
                  <option value="owner">团体负责人</option>
                  <option value="admin">管理员</option>
                  <option value="member">成员</option>
                </select>
              </div>
            </div>
            <div v-if="errors.members" class="inline-error">{{ errors.members }}</div>
            <div v-else-if="filteredMembers.length" class="member-list">
              <article v-for="member in filteredMembers" :key="member.id || member.userId" class="member-item">
                <img :src="member.avatar" :alt="member.name">
                <div>
                  <strong>{{ member.name }}</strong>
                  <span>{{ member.joinedAt || '未记录加入时间' }}</span>
                  <p v-if="member.status === 'pending' && member.applyReason">申请说明：{{ member.applyReason }}</p>
                </div>
                <em>{{ member.roleLabel }}</em>
                <b :class="member.status">{{ member.statusLabel }}</b>
                <div v-if="canAuditMember(member)" class="member-actions">
                  <button type="button" :disabled="moderatingMemberId === member.id" @click="approvePendingMember(member)">通过</button>
                  <button type="button" :disabled="moderatingMemberId === member.id" @click="rejectPendingMember(member)">拒绝</button>
                </div>
                <button
                  v-else-if="canRemoveMember(member)"
                  type="button"
                  class="remove-member-btn"
                  :disabled="moderatingMemberId === member.id"
                  @click="removeMember(member)"
                >移除</button>
              </article>
            </div>
            <div v-else class="empty-inline">暂无成员</div>
          </section>

          <section v-else-if="activeTab === 'notices'" class="panel-card">
            <div class="section-head">
              <h2>团体公告</h2>
              <button type="button" class="primary-btn small">发布公告</button>
            </div>
            <div v-if="errors.notices" class="inline-error">{{ errors.notices }}</div>
            <div v-else-if="notices.length" class="notice-list">
              <article v-for="notice in notices" :key="notice.id" class="notice-card">
                <button type="button" @click="toggleNotice(notice.id)">
                  <span>{{ notice.title }}</span>
                  <time>{{ notice.date }}</time>
                </button>
                <p v-if="expandedNoticeId === notice.id">{{ notice.content }}</p>
              </article>
            </div>
            <div v-else class="empty-inline">暂无公告</div>
          </section>

          <section v-else class="panel-card info-card">
            <h2>团体资料</h2>
            <InfoList :group="group" />
          </section>
        </main>

        <aside class="side-panel">
          <section v-if="activeTab !== 'profile'" class="side-card">
            <h2>团体资料</h2>
            <InfoList :group="group" compact />
          </section>
          <section class="side-card">
            <h2>成员概况</h2>
            <p>{{ group.memberCount }} 位成员 · {{ admins.length }} 位管理员</p>
            <router-link :to="`/platform/groups/${group.id}/members`">查看成员列表</router-link>
          </section>
        </aside>
      </section>
    </template>
  </section>
</template>

<script setup>
import { computed, defineComponent, h, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  approveMember,
  createGroupPost,
  createGroupPostComment,
  deletePlatformGroupPost,
  deletePlatformGroupPostComment,
  fetchGroupDetail,
  fetchGroupMembers,
  fetchGroupNotices,
  fetchGroupPostComments,
  fetchGroupPosts,
  joinGroup,
  unwrapGroupPostsList,
  rejectMember,
  removeGroupMember,
  togglePlatformGroupPostLike
} from '@/api/groups.js'
import { useUserStore } from '@/stores/user.js'

const DEFAULT_COVER = 'https://images.unsplash.com/photo-1507692049790-de58290a4334?auto=format&fit=crop&w=1400&q=80'
const DEFAULT_AVATAR = 'https://api.dicebear.com/7.x/initials/svg?seed=LC&backgroundColor=eff6ff,fdf2f8,eef2ff'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const group = ref(null)
const rawMembers = ref([])
const rawPosts = ref([])
const rawNotices = ref([])
const memberKeyword = ref('')
const roleFilter = ref('')
const expandedNoticeId = ref(null)
const joining = ref(false)
const posting = ref(false)
const moderatingMemberId = ref(null)
const message = ref('')
const messageType = ref('success')

const loading = reactive({ detail: false, members: false, posts: false, notices: false })
const errors = reactive({ detail: '', members: '', posts: '', notices: '' })
const postForm = reactive({ content: '', imageUrls: '' })
const expandedPostId = ref(null)
const feedComments = reactive({})
const commentsLoading = reactive({})
const commentDraft = reactive({})
const commentPosting = reactive({})
const likingPostId = ref(null)
const deletingPostId = ref(null)
const deletingCommentKey = ref('')

const activeTab = computed(() => {
  if (route.path.endsWith('/posts')) return 'posts'
  if (route.path.endsWith('/members')) return 'members'
  if (route.path.endsWith('/notices')) return 'notices'
  if (route.path.endsWith('/profile')) return 'profile'
  return 'home'
})

const tabs = computed(() => {
  const id = group.value?.id || route.params.id
  return [
    { key: 'home', label: '首页', to: `/platform/groups/${id}` },
    { key: 'posts', label: '动态', to: `/platform/groups/${id}/posts` },
    { key: 'members', label: '成员', to: `/platform/groups/${id}/members` },
    { key: 'notices', label: '公告', to: `/platform/groups/${id}/notices` },
    { key: 'profile', label: '资料', to: `/platform/groups/${id}/profile` }
  ]
})

const heroImage = computed(() => group.value?.coverUrl || DEFAULT_COVER)
const latestNotice = computed(() => notices.value[0] || group.value?.latestNotice || null)
const joinModeKey = computed(() => {
  const g = group.value
  if (!g) return 'audit'
  return g.joinModeKey || (g.joinMode === 'free' ? 'open' : g.joinMode === 'invite' ? 'invite' : 'audit')
})

const joinDisabled = computed(() => {
  if (!group.value) return true
  if (group.value.isMember || group.value.hasPendingRequest) return true
  if (joinModeKey.value === 'invite') return true
  return false
})

const joinButtonText = computed(() => {
  if (!userStore.isLoggedIn) return '登录后加入'
  if (group.value?.isMember) return '已加入'
  if (group.value?.hasPendingRequest) return '审核中'
  if (joinModeKey.value === 'invite') return '仅限邀请'
  if (joinModeKey.value === 'open') return '加入团体'
  return '申请加入'
})

const admins = computed(() => normalizeAdmins(group.value?.admins || []))
const posts = computed(() => rawPosts.value.map(normalizePost))
const notices = computed(() => rawNotices.value.map(normalizeNotice))
const members = computed(() => rawMembers.value.map(normalizeMember))

const filteredMembers = computed(() => {
  const keyword = memberKeyword.value.toLowerCase()
  return members.value.filter((member) => {
    const byKeyword = !keyword || member.name.toLowerCase().includes(keyword)
    const byRole = !roleFilter.value || member.role === roleFilter.value
    return byKeyword && byRole
  })
})

const emptyPostsHint = computed(() => {
  if (group.value?.isMember) return '这个团体还没有动态，来发布第一条吧。'
  return '这个团体还没有动态。'
})

async function loadDetail() {
  loading.detail = true
  errors.detail = ''
  try {
    const data = await fetchGroupDetail(route.params.id)
    group.value = normalizeGroup(data)
  } catch (error) {
    group.value = null
    errors.detail = error.message || '无法连接 /api/platform/groups/{id}'
  } finally {
    loading.detail = false
  }
}

async function loadMembers() {
  loading.members = true
  errors.members = ''
  try {
    const status = group.value?.managed ? 'all' : 'approved'
    rawMembers.value = unwrapList(await fetchGroupMembers(group.value.id, { status }))
  } catch (error) {
    rawMembers.value = []
    errors.members = error.message || '成员接口加载失败'
  } finally {
    loading.members = false
  }
}

async function loadPosts() {
  loading.posts = true
  errors.posts = ''
  try {
    const res = await fetchGroupPosts(group.value.id, { page: 1, size: 80 })
    rawPosts.value = unwrapGroupPostsList(res)
  } catch (error) {
    rawPosts.value = []
    errors.posts = error.message || '动态接口加载失败'
  } finally {
    loading.posts = false
  }
}

async function loadNotices() {
  loading.notices = true
  errors.notices = ''
  try {
    rawNotices.value = unwrapList(await fetchGroupNotices(group.value.id))
  } catch (error) {
    rawNotices.value = []
    errors.notices = error.message || '公告接口加载失败'
  } finally {
    loading.notices = false
  }
}

async function loadRelatedData() {
  if (!group.value?.id) return
  await Promise.all([loadMembers(), loadPosts(), loadNotices()])
}

async function applyJoin() {
  if (joinDisabled.value || joining.value) return
  if (!userStore.isLoggedIn) {
    userStore.setPostLoginRedirect(route.fullPath.replace(/^#/, '') || `/platform/groups/${route.params.id}`)
    router.push('/login')
    return
  }
  if (joinModeKey.value === 'invite') return
  let applyMessage = ''
  if (joinModeKey.value === 'audit') {
    const input = window.prompt('请输入申请验证信息（必填）', '')
    if (input === null) return
    applyMessage = input.trim()
    if (!applyMessage) {
      flashMessage('请填写申请验证信息', 'error')
      return
    }
  }
  joining.value = true
  try {
    const res = await joinGroup(group.value.id, applyMessage)
    group.value.isMember = Boolean(res?.joined)
    group.value.hasPendingRequest = Boolean(res?.pending)
    await loadDetail()
    await loadRelatedData()
    flashMessage(res?.message || '申请已提交')
  } catch (error) {
    flashMessage(error.message || '申请加入失败', 'error')
  } finally {
    joining.value = false
  }
}

async function submitPost() {
  const text = postForm.content.trim()
  const imgs = postForm.imageUrls.trim()
  if (!text && !imgs) {
    flashMessage('请填写文字或图片链接', 'error')
    return
  }
  posting.value = true
  try {
    await createGroupPost(group.value.id, { content: text, imageUrls: imgs || undefined })
    postForm.content = ''
    postForm.imageUrls = ''
    await loadPosts()
    await loadDetail()
    flashMessage('动态发布成功')
  } catch (error) {
    flashMessage(error.message || '动态发布失败', 'error')
  } finally {
    posting.value = false
  }
}

function unwrapCommentItems(res) {
  if (Array.isArray(res?.items)) return res.items
  if (Array.isArray(res?.data?.items)) return res.data.items
  if (Array.isArray(res)) return res
  return []
}

function commentKey(postId, commentId) {
  return `${postId}-${commentId}`
}

function canDeletePost(post) {
  if (!group.value) return false
  if (group.value.managed) return true
  return String(post.userId) === String(userStore.userId)
}

function canDeleteComment(c) {
  if (!group.value) return false
  if (group.value.managed) return true
  return String(c.userId) === String(userStore.userId)
}

async function togglePostLike(post) {
  if (!userStore.isLoggedIn) {
    userStore.setPostLoginRedirect(route.fullPath.replace(/^#/, '') || `/platform/groups/${route.params.id}`)
    router.push('/login')
    return
  }
  const row = rawPosts.value.find((p) => p.id === post.id)
  if (!row) return
  const prevLiked = Boolean(row.likedByMe)
  const prevCount = Number(row.likeCount ?? 0)
  row.likedByMe = !prevLiked
  row.likeCount = prevLiked ? Math.max(0, prevCount - 1) : prevCount + 1
  likingPostId.value = post.id
  try {
    const res = await togglePlatformGroupPostLike(post.id)
    row.likedByMe = Boolean(res?.likedByMe)
    row.likeCount = Number(res?.likeCount ?? row.likeCount)
  } catch (error) {
    row.likedByMe = prevLiked
    row.likeCount = prevCount
    flashMessage(error.message || '点赞失败', 'error')
  } finally {
    likingPostId.value = null
  }
}

async function toggleCommentPanel(post) {
  if (expandedPostId.value === post.id) {
    expandedPostId.value = null
    return
  }
  expandedPostId.value = post.id
  if (!feedComments[post.id]) {
    await loadCommentsForPost(post.id)
  }
}

async function loadCommentsForPost(postId) {
  commentsLoading[postId] = true
  try {
    const res = await fetchGroupPostComments(postId, { page: 1, size: 50 })
    const items = unwrapCommentItems(res)
    feedComments[postId] = items.map(normalizeComment)
  } catch (error) {
    feedComments[postId] = []
    flashMessage(error.message || '评论加载失败', 'error')
  } finally {
    commentsLoading[postId] = false
  }
}

function normalizeComment(item) {
  return {
    id: item.id,
    userId: item.userId,
    author: item.authorName || '成员',
    avatar: item.authorAvatar || DEFAULT_AVATAR,
    content: item.content || '',
    time: formatDateTime(item.createdAt)
  }
}

async function submitComment(post) {
  const key = post.id
  const text = (commentDraft[key] || '').trim()
  if (!text) return
  commentPosting[key] = true
  try {
    await createGroupPostComment(post.id, { content: text })
    commentDraft[key] = ''
    await loadCommentsForPost(post.id)
    await loadPosts()
    flashMessage('评论已发布')
  } catch (error) {
    flashMessage(error.message || '评论失败', 'error')
  } finally {
    commentPosting[key] = false
  }
}

async function deletePost(post) {
  if (!canDeletePost(post) || deletingPostId.value) return
  if (!window.confirm('确定删除这条动态？')) return
  deletingPostId.value = post.id
  try {
    await deletePlatformGroupPost(group.value.id, post.id)
    expandedPostId.value = null
    delete feedComments[post.id]
    await loadPosts()
    await loadDetail()
    flashMessage('已删除动态')
  } catch (error) {
    flashMessage(error.message || '删除失败', 'error')
  } finally {
    deletingPostId.value = null
  }
}

async function deleteComment(post, c) {
  if (!canDeleteComment(c)) return
  const ck = commentKey(post.id, c.id)
  if (deletingCommentKey.value) return
  if (!window.confirm('删除这条评论？')) return
  deletingCommentKey.value = ck
  try {
    await deletePlatformGroupPostComment(post.id, c.id)
    await loadCommentsForPost(post.id)
    await loadPosts()
  } catch (error) {
    flashMessage(error.message || '删除失败', 'error')
  } finally {
    deletingCommentKey.value = ''
  }
}

function canAuditMember(member) {
  return group.value?.managed && member.status === 'pending'
}

function canRemoveMember(member) {
  return (
    group.value?.managed &&
    member.status === 'approved' &&
    member.role !== 'owner'
  )
}

async function removeMember(member) {
  if (!canRemoveMember(member) || moderatingMemberId.value) return
  if (!window.confirm(`确定将「${member.name}」移出团体？`)) return
  moderatingMemberId.value = member.id
  try {
    await removeGroupMember(group.value.id, member.id)
    await loadDetail()
    await loadMembers()
    flashMessage('已移除成员')
  } catch (error) {
    flashMessage(error.message || '移除失败', 'error')
  } finally {
    moderatingMemberId.value = null
  }
}

async function approvePendingMember(member) {
  if (!canAuditMember(member) || moderatingMemberId.value) return
  moderatingMemberId.value = member.id
  try {
    const res = await approveMember(group.value.id, member.id)
    await loadDetail()
    await loadMembers()
    flashMessage(res?.message || '已通过申请')
  } catch (error) {
    flashMessage(error.message || '审核通过失败', 'error')
  } finally {
    moderatingMemberId.value = null
  }
}

async function rejectPendingMember(member) {
  if (!canAuditMember(member) || moderatingMemberId.value) return
  moderatingMemberId.value = member.id
  try {
    const res = await rejectMember(group.value.id, member.id)
    await loadMembers()
    flashMessage(res?.message || '已拒绝申请')
  } catch (error) {
    flashMessage(error.message || '拒绝申请失败', 'error')
  } finally {
    moderatingMemberId.value = null
  }
}

function toggleNotice(id) {
  expandedNoticeId.value = expandedNoticeId.value === id ? null : id
}

function normalizeGroup(item) {
  const jm = item.joinMode
  const key = item.joinModeKey || (jm === 'free' ? 'open' : jm === 'invite' ? 'invite' : 'audit')
  const joinLabel =
    key === 'open' ? '公开加入' : key === 'invite' ? '仅限邀请' : '审核加入'
  return {
    id: item.id || route.params.id,
    name: item.name || '未命名团体',
    description: item.description || '暂无团体简介',
    coverUrl: item.coverUrl || '',
    category: item.category || item.typeName || item.type || '团体',
    region: item.location || item.region || '未设置地区',
    memberCount: Number(item.memberCount || 0),
    joinMode: jm || (key === 'open' ? 'free' : 'audit'),
    joinModeKey: key,
    joinLabel,
    isMember: Boolean(item.isMember),
    managed: Boolean(item.managed),
    hasPendingRequest: Boolean(item.hasPendingRequest),
    createdDate: formatDate(item.createdAt),
    latestNotice: item.latestNotice ? normalizeNotice(item.latestNotice) : null,
    admins: item.admins || [],
    postCount: item.postCount != null ? Number(item.postCount) : null
  }
}

function normalizePost(item) {
  return {
    id: item.id,
    userId: item.userId,
    author: item.authorName || '团体成员',
    role: item.type === 'announcement' ? '公告' : '动态',
    time: formatDateTime(item.createdAt),
    content: item.content || '',
    likes: Number(item.likeCount || 0),
    comments: Number(item.commentCount || 0),
    likedByMe: Boolean(item.likedByMe),
    avatar: item.authorAvatar || DEFAULT_AVATAR,
    images: parseImages(item.imageUrls)
  }
}

function normalizeNotice(item) {
  return {
    id: item.id,
    title: item.title || '未命名公告',
    content: item.content || '',
    date: formatDate(item.createdAt)
  }
}

function normalizeMember(item) {
  const role = item.role || 'member'
  const status = item.status || 'approved'
  return {
    id: item.id,
    userId: item.userId,
    name: item.username || '未命名成员',
    avatar: item.avatarUrl || DEFAULT_AVATAR,
    role,
    roleLabel: role === 'owner' ? '团体负责人' : role === 'admin' ? '管理员' : '成员',
    status,
    statusLabel: status === 'approved' ? '已加入' : status === 'pending' ? '申请中' : status === 'left' ? '已退出' : status,
    joinedAt: formatDate(item.joinedAt),
    applyReason: item.applyReason || ''
  }
}

function normalizeAdmins(items) {
  return items.map((item, index) => ({
    userId: item.userId || index,
    name: item.name || '管理员',
    avatar: item.avatar || DEFAULT_AVATAR,
    roleLabel: item.role === 'owner' ? '团体负责人' : '管理员'
  }))
}

function parseImages(value) {
  if (Array.isArray(value)) return value.map(String).filter(Boolean)
  if (!value) return []
  const s = String(value).trim()
  if (s.startsWith('[')) {
    try {
      const parsed = JSON.parse(s)
      if (Array.isArray(parsed)) return parsed.map(String).filter(Boolean)
    } catch {
      /* ignore */
    }
  }
  return s.split(',').map((item) => item.trim()).filter(Boolean)
}

function unwrapList(res) {
  if (Array.isArray(res)) return res
  if (Array.isArray(res?.data)) return res.data
  return []
}

function flashMessage(text, type = 'success') {
  message.value = text
  messageType.value = type
  window.setTimeout(() => {
    message.value = ''
  }, 2200)
}

function formatDate(value) {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return ''
  return date.toISOString().slice(0, 10)
}

function formatDateTime(value) {
  if (!value) return '刚刚'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return ''
  const pad = (n) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
}

const InfoList = defineComponent({
  props: { group: { type: Object, required: true }, compact: Boolean },
  setup(props) {
    return () => h('dl', { class: props.compact ? 'info-list compact' : 'info-list' }, [
      ['团体类型', props.group.category],
      ['所在地区', props.group.region],
      ['成员数量', `${props.group.memberCount} 人`],
      ['加入方式', props.group.joinLabel],
      ['创建时间', props.group.createdDate || '未记录']
    ].map(([key, value]) => h('div', [h('dt', key), h('dd', value)])))
  }
})

const PostList = defineComponent({
  props: { posts: { type: Array, required: true }, error: String, emptyText: String },
  setup(props) {
    return () => {
      if (props.error) return h('div', { class: 'inline-error' }, props.error)
      if (!props.posts.length) return h('div', { class: 'empty-inline' }, props.emptyText)
      return h('div', { class: 'post-list' }, props.posts.map(post => h('article', { class: 'post-card', key: post.id }, [
        h('div', { class: 'post-head' }, [
          h('img', { src: post.avatar, alt: '' }),
          h('div', [h('strong', post.author), h('span', post.time)]),
          h('em', post.role)
        ]),
        h('p', post.content),
        post.images.length ? h('div', { class: 'post-images' }, post.images.map(image => h('img', { src: image, alt: '', key: image }))) : null,
        h('div', { class: 'post-actions' }, [h('button', '赞 ' + post.likes), h('button', '评论 ' + post.comments)])
      ])))
    }
  }
})

watch(() => route.params.id, async () => {
  await loadDetail()
  await loadRelatedData()
})

onMounted(async () => {
  await loadDetail()
  await loadRelatedData()
})
</script>

<style scoped>
.group-detail-page {
  width: calc(100% - var(--lc-space-8));
  margin: var(--lc-space-4) auto 0;
  padding-bottom: var(--lc-space-12);
}

.loading-state,
.error-state {
  min-height: 420px;
  display: grid;
  place-items: center;
  align-content: center;
  gap: var(--lc-space-3);
  color: var(--lc-muted);
}

.error-state h2 {
  margin: 0;
  color: var(--lc-text);
}

.error-state button,
.error-state a {
  height: 36px;
  border: 1px solid var(--lc-border);
  border-radius: var(--lc-radius-xs);
  padding: 0 var(--lc-space-4);
  color: var(--lc-blue);
  background: var(--lc-surface);
  text-decoration: none;
  font-weight: 900;
}

.hero-card,
.tabs,
.panel-card,
.side-card {
  border: 1px solid var(--lc-border);
  background: var(--lc-surface);
  box-shadow: var(--lc-shadow-sm);
}

.hero-card {
  overflow: hidden;
  border-radius: var(--lc-radius);
}

.hero-cover {
  height: 220px;
  background-size: cover;
  background-position: center;
}

.hero-body {
  display: grid;
  grid-template-columns: 116px minmax(0, 1fr) auto;
  gap: var(--lc-space-5);
  padding: 0 var(--lc-space-6) var(--lc-space-6);
}

.group-avatar {
  width: 116px;
  height: 116px;
  margin-top: -42px;
  border: 4px solid var(--lc-surface);
  border-radius: var(--lc-radius-xs);
  object-fit: cover;
  box-shadow: var(--lc-shadow);
}

.hero-main {
  padding-top: var(--lc-space-4);
}

.title-row {
  display: flex;
  align-items: center;
  gap: var(--lc-space-3);
  min-width: 0;
}

.title-row h1 {
  margin: 0;
  color: var(--lc-text);
  font-size: 30px;
  line-height: 1.15;
}

.title-row span,
.post-head em,
.member-item em,
.member-item b {
  border-radius: 999px;
  padding: 3px var(--lc-space-2);
  color: var(--lc-blue);
  background: var(--lc-blue-light);
  font-size: var(--lc-text-xs);
  font-style: normal;
  font-weight: 900;
}

.meta-line,
.group-desc {
  margin: var(--lc-space-2) 0 0;
  color: var(--lc-muted);
  font-size: var(--lc-text-base);
  line-height: 1.7;
}

.hero-actions {
  display: flex;
  align-items: flex-start;
  gap: var(--lc-space-2);
  padding-top: var(--lc-space-4);
}

.join-btn,
.primary-btn,
.icon-btn {
  height: 36px;
  border-radius: var(--lc-radius-xs);
  font-weight: 900;
  cursor: pointer;
}

.primary-btn {
  border: 0;
  padding: 0 var(--lc-space-4);
  color: var(--lc-surface);
  background: linear-gradient(135deg, var(--lc-pink), var(--lc-blue));
  box-shadow: var(--lc-shadow-blue);
}

.primary-btn.small {
  height: 32px;
  font-size: var(--lc-text-sm);
}

.join-btn,
.icon-btn {
  border: 1px solid var(--lc-border);
  color: var(--lc-muted);
  background: var(--lc-surface);
}

.join-btn {
  min-width: 96px;
  padding: 0 var(--lc-space-4);
}

.join-btn:disabled {
  color: var(--lc-blue);
  background: var(--lc-blue-light);
  cursor: default;
}

.manage-link {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 36px;
  padding: 0 var(--lc-space-4);
  border-radius: var(--lc-radius-xs);
  border: 1px solid var(--lc-blue-border);
  color: var(--lc-blue);
  background: var(--lc-surface);
  font-weight: 900;
  text-decoration: none;
}

.remove-member-btn {
  height: 28px;
  padding: 0 var(--lc-space-2);
  border-radius: var(--lc-radius-xs);
  border: 1px solid var(--lc-border);
  color: var(--lc-red);
  background: var(--lc-surface);
  font-size: var(--lc-text-xs);
  font-weight: 900;
  cursor: pointer;
}

.icon-btn {
  width: 36px;
}

.tabs {
  display: flex;
  gap: var(--lc-space-8);
  overflow-x: auto;
  border-top: 0;
  padding: 0 var(--lc-space-6);
  border-radius: 0 0 var(--lc-radius) var(--lc-radius);
}

.tabs a {
  position: relative;
  flex: 0 0 auto;
  padding: var(--lc-space-4) 0;
  color: var(--lc-muted);
  text-decoration: none;
  font-size: var(--lc-text-base);
  font-weight: 900;
}

.tabs a.active {
  color: var(--lc-blue);
}

.tabs a.active::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: -1px;
  height: 3px;
  border-radius: 999px;
  background: var(--lc-blue);
}

.page-message {
  margin-top: var(--lc-space-3);
  padding: var(--lc-space-2) var(--lc-space-3);
  border-radius: var(--lc-radius-xs);
  color: var(--lc-green);
  background: var(--lc-green-light);
  font-size: var(--lc-text-sm);
  font-weight: 900;
}

.page-message.error,
.inline-error {
  color: var(--lc-red);
  background: var(--lc-red-light);
}

.content-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: var(--lc-space-5);
  margin-top: var(--lc-space-5);
}

.main-panel,
.side-panel,
.post-list,
.notice-list,
.member-list,
.home-grid {
  display: grid;
  gap: var(--lc-space-4);
  align-content: start;
}

.home-grid {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.panel-card,
.side-card {
  border-radius: var(--lc-radius-sm);
  padding: var(--lc-space-5);
}

.panel-card h2,
.side-card h2 {
  margin: 0 0 var(--lc-space-3);
  color: var(--lc-text);
  font-size: var(--lc-text-md);
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--lc-space-3);
  margin-bottom: var(--lc-space-3);
}

.section-head h2 {
  margin: 0;
}

.section-head a,
.side-card a {
  color: var(--lc-blue);
  text-decoration: none;
  font-size: var(--lc-text-sm);
  font-weight: 900;
}

.notice-card,
.post-card,
.member-item {
  border: 1px solid var(--lc-border);
  border-radius: var(--lc-radius-xs);
  background: var(--lc-bg);
}

.notice-card {
  padding: var(--lc-space-4);
}

.notice-card.compact p {
  display: -webkit-box;
  overflow: hidden;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.notice-card button {
  display: flex;
  justify-content: space-between;
  gap: var(--lc-space-4);
  width: 100%;
  border: 0;
  padding: 0;
  color: var(--lc-text);
  background: transparent;
  font: inherit;
  font-weight: 900;
  text-align: left;
  cursor: pointer;
}

.notice-card strong {
  color: var(--lc-text);
}

.notice-card p,
.side-card p,
.activity-placeholder p {
  margin: var(--lc-space-2) 0 0;
  color: var(--lc-muted);
  line-height: 1.7;
}

.notice-card time {
  color: var(--lc-subtle);
  font-size: var(--lc-text-xs);
}

.empty-inline,
.inline-error {
  padding: var(--lc-space-4);
  border-radius: var(--lc-radius-xs);
  color: var(--lc-muted);
  background: var(--lc-bg);
  font-weight: 800;
}

.inline-error {
  color: var(--lc-red);
}

.post-form {
  display: grid;
  gap: var(--lc-space-3);
  margin-bottom: var(--lc-space-4);
}

.post-form textarea,
.post-form input,
.member-tools input,
.member-tools select {
  border: 1px solid var(--lc-border);
  border-radius: var(--lc-radius-xs);
  color: var(--lc-text);
  background: var(--lc-surface);
  font: inherit;
  font-weight: 700;
}

.post-form textarea {
  min-height: 96px;
  padding: var(--lc-space-3);
  resize: vertical;
}

.post-form input,
.member-tools input,
.member-tools select {
  height: 38px;
  padding: 0 var(--lc-space-3);
}

.post-form-foot,
.member-tools > div {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--lc-space-3);
}

.post-form-foot span {
  color: var(--lc-subtle);
  font-size: var(--lc-text-sm);
  font-weight: 800;
}

.post-card {
  padding: var(--lc-space-4);
}

.post-head {
  display: grid;
  grid-template-columns: 42px minmax(0, 1fr) auto auto;
  gap: var(--lc-space-3);
  align-items: center;
}

:deep(.post-list) {
  display: grid;
  gap: var(--lc-space-4);
}

:deep(.post-card) {
  border: 1px solid var(--lc-border);
  border-radius: var(--lc-radius-xs);
  padding: var(--lc-space-4);
  background: var(--lc-bg);
}

:deep(.post-head) {
  display: grid;
  grid-template-columns: 42px minmax(0, 1fr) auto;
  gap: var(--lc-space-3);
  align-items: center;
}

:deep(.post-head img) {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  object-fit: cover;
}

:deep(.post-head strong) {
  display: block;
  color: var(--lc-text);
  font-size: var(--lc-text-base);
}

:deep(.post-head span) {
  display: block;
  margin-top: 2px;
  color: var(--lc-subtle);
  font-size: var(--lc-text-xs);
}

:deep(.post-head em) {
  border-radius: 999px;
  padding: 3px var(--lc-space-2);
  color: var(--lc-blue);
  background: var(--lc-blue-light);
  font-size: var(--lc-text-xs);
  font-style: normal;
  font-weight: 900;
}

:deep(.post-card p) {
  margin: var(--lc-space-3) 0 0;
  color: var(--lc-muted);
  line-height: 1.75;
}

:deep(.post-images) {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: var(--lc-space-2);
  margin-top: var(--lc-space-3);
}

:deep(.post-images img) {
  width: 100%;
  aspect-ratio: 16 / 10;
  border-radius: var(--lc-radius-xs);
  object-fit: cover;
}

:deep(.post-actions) {
  display: flex;
  gap: var(--lc-space-3);
  margin-top: var(--lc-space-3);
}

:deep(.post-actions button) {
  height: 30px;
  border: 1px solid var(--lc-border);
  border-radius: var(--lc-radius-xs);
  color: var(--lc-muted);
  background: var(--lc-surface);
  font-weight: 900;
}

:deep(.info-list) {
  display: grid;
  gap: var(--lc-space-3);
  margin: 0;
}

:deep(.info-list div) {
  display: flex;
  justify-content: space-between;
  gap: var(--lc-space-4);
}

:deep(.info-list dt) {
  color: var(--lc-subtle);
  font-size: var(--lc-text-sm);
}

:deep(.info-list dd) {
  margin: 0;
  color: var(--lc-text);
  font-size: var(--lc-text-sm);
  font-weight: 900;
  text-align: right;
}

.post-head img,
.member-item img,
.admin-item img {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  object-fit: cover;
}

.post-head strong,
.member-item strong,
.admin-item span,
.activity-placeholder strong {
  display: block;
  color: var(--lc-text);
  font-size: var(--lc-text-base);
}

.post-head span,
.member-item span {
  display: block;
  margin-top: 2px;
  color: var(--lc-subtle);
  font-size: var(--lc-text-xs);
}

.post-card p {
  margin: var(--lc-space-3) 0 0;
  color: var(--lc-muted);
  line-height: 1.75;
}

.post-images {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: var(--lc-space-2);
  margin-top: var(--lc-space-3);
}

.post-images img {
  width: 100%;
  aspect-ratio: 16 / 10;
  border-radius: var(--lc-radius-xs);
  object-fit: cover;
}

.post-actions {
  display: flex;
  gap: var(--lc-space-3);
  margin-top: var(--lc-space-3);
}

.post-actions button {
  height: 30px;
  border: 1px solid var(--lc-border);
  border-radius: var(--lc-radius-xs);
  color: var(--lc-muted);
  background: var(--lc-surface);
  font-weight: 900;
}

.post-actions button.active {
  color: var(--lc-blue);
  border-color: var(--lc-blue-border);
  background: var(--lc-blue-light);
}

.join-hint {
  margin: 0 0 var(--lc-space-4);
  padding: var(--lc-space-3);
  border-radius: var(--lc-radius-xs);
  color: var(--lc-muted);
  background: var(--lc-bg);
  font-weight: 800;
}

.post-delete-btn {
  height: 28px;
  padding: 0 var(--lc-space-2);
  border: 1px solid var(--lc-border);
  border-radius: var(--lc-radius-xs);
  color: var(--lc-subtle);
  background: var(--lc-surface);
  font-size: var(--lc-text-xs);
  font-weight: 800;
  cursor: pointer;
}

.comment-panel {
  margin-top: var(--lc-space-3);
  padding: var(--lc-space-3);
  border-radius: var(--lc-radius-xs);
  border: 1px solid var(--lc-border);
  background: var(--lc-surface);
}

.comment-status,
.comment-login-hint {
  margin: 0;
  font-size: var(--lc-text-sm);
  color: var(--lc-subtle);
}

.comment-list {
  list-style: none;
  margin: 0 0 var(--lc-space-3);
  padding: 0;
  display: grid;
  gap: var(--lc-space-2);
}

.comment-list li {
  display: grid;
  grid-template-columns: 32px minmax(0, 1fr) auto;
  gap: var(--lc-space-2);
  align-items: start;
}

.comment-list li img {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
}

.comment-body strong {
  display: block;
  font-size: var(--lc-text-sm);
  color: var(--lc-text);
}

.comment-body span {
  font-size: var(--lc-text-xs);
  color: var(--lc-subtle);
}

.comment-body p {
  margin: 4px 0 0;
  font-size: var(--lc-text-sm);
  color: var(--lc-muted);
  line-height: 1.5;
}

.comment-empty {
  grid-column: 1 / -1;
  color: var(--lc-subtle);
  font-size: var(--lc-text-sm);
}

.comment-form {
  display: flex;
  gap: var(--lc-space-2);
  align-items: center;
}

.comment-form input {
  flex: 1;
  min-width: 0;
  height: 36px;
  padding: 0 var(--lc-space-3);
  border: 1px solid var(--lc-border);
  border-radius: var(--lc-radius-xs);
  font: inherit;
}

.comment-del {
  height: 26px;
  padding: 0 6px;
  border: 0;
  background: none;
  color: var(--lc-subtle);
  font-size: var(--lc-text-xs);
  cursor: pointer;
}

.member-item {
  display: grid;
  grid-template-columns: 42px minmax(0, 1fr) auto auto auto;
  gap: var(--lc-space-3);
  align-items: center;
  padding: var(--lc-space-3);
}

.member-item b.pending {
  color: var(--lc-amber);
  background: var(--lc-amber-light);
}

.member-actions {
  display: flex;
  gap: var(--lc-space-2);
}

.member-actions button {
  height: 30px;
  border: 1px solid var(--lc-border);
  border-radius: var(--lc-radius-xs);
  padding: 0 var(--lc-space-3);
  color: var(--lc-blue);
  background: var(--lc-surface);
  font-weight: 900;
  cursor: pointer;
}

.member-actions button:last-child {
  color: var(--lc-red);
}

.member-actions button:disabled {
  cursor: not-allowed;
  opacity: 0.65;
}

.info-list {
  display: grid;
  gap: var(--lc-space-3);
  margin: 0;
}

.info-list div {
  display: flex;
  justify-content: space-between;
  gap: var(--lc-space-4);
}

.info-list dt {
  color: var(--lc-subtle);
  font-size: var(--lc-text-sm);
}

.info-list dd {
  margin: 0;
  color: var(--lc-text);
  font-size: var(--lc-text-sm);
  font-weight: 900;
  text-align: right;
}

.admin-list {
  display: grid;
  gap: var(--lc-space-3);
}

.admin-item {
  display: grid;
  grid-template-columns: 42px minmax(0, 1fr) auto;
  gap: var(--lc-space-3);
  align-items: center;
}

.admin-item em {
  color: var(--lc-muted);
  font-size: var(--lc-text-xs);
  font-style: normal;
  font-weight: 900;
}

@media (max-width: 1180px) {
  .content-grid {
    grid-template-columns: 1fr;
  }

  .home-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 760px) {
  .group-detail-page {
    width: calc(100% - var(--lc-space-4));
  }

  .hero-body {
    grid-template-columns: 1fr;
    padding: 0 var(--lc-space-4) var(--lc-space-4);
  }

  .hero-actions,
  .section-head,
  .post-form-foot,
  .member-tools > div {
    align-items: stretch;
    flex-direction: column;
  }

  .tabs {
    padding: 0 var(--lc-space-4);
    gap: var(--lc-space-5);
  }

  .post-images {
    grid-template-columns: 1fr;
  }

  .member-item {
    grid-template-columns: 42px minmax(0, 1fr);
  }

  .member-actions {
    grid-column: 2;
  }
}
</style>
