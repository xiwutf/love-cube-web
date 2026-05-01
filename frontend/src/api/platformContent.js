import request from './request.js'

/** 平台内容页：公告 + 资讯 + 活动 + 每日心声（一次请求） */
export function fetchPlatformContentFeed(params = { limit: 30 }) {
  return request.get('/platform/content-feed', { params })
}

export function fetchAnnouncements(params = { status: 'published' }) {
  return request.get('/announcements', { params })
}

export function fetchAnnouncementDetail(id) {
  return request.get(`/announcements/${id}`)
}

export function fetchArticles(params = { status: 'published' }) {
  return request.get('/articles', { params })
}

export function fetchArticleDetail(id) {
  return request.get(`/articles/${id}`)
}

export function submitArticle(payload) {
  return request.post('/articles/submissions', payload)
}

export function fetchEvents(params = { status: 'published' }) {
  return request.get('/events', { params })
}

export function fetchEventDetail(id) {
  return request.get(`/events/${id}`)
}

export function signupEvent(id) {
  return request.post(`/events/${id}/signup`)
}

export function getMyEventSignups() {
  return request.get('/events/my-signups')
}

export function fetchHomeConfig() {
  return request.get('/home/config')
}

export function fetchHotTopics(limit = 5) {
  return request.get('/articles/hot-topics', { params: { limit } })
}

export function fetchRecommendedAuthors(limit = 4) {
  return request.get('/users/recommended-authors', { params: { limit } })
}

export function fetchEventsStats() {
  return request.get('/events/stats')
}

export function fetchModulesStats() {
  return request.get('/modules/stats')
}

export function fetchPlatformStats() {
  return request.get('/platform/stats')
}
