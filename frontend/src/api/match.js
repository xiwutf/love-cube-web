import request from './request.js'

function unwrapList(res) {
  if (Array.isArray(res)) return res
  if (Array.isArray(res?.data)) return res.data
  return []
}

export async function getMatchList(params = {}) {
  const query = {}
  if (params?.gender === 'male') query.gender = 1
  if (params?.gender === 'female') query.gender = 2
  if (Array.isArray(params?.ageRange) && params.ageRange.length === 2) {
    query.minAge = params.ageRange[0]
    query.maxAge = params.ageRange[1]
  }
  if (params?.region) query.location = params.region
  if (params?.verifiedOnly) query.verifiedOnly = true
  if (params?.page) query.page = params.page
  if (params?.size) query.size = params.size
  const data = await request.get('/matches/list', { params: query })
  if (params?.page || params?.size) {
    return {
      list: unwrapList(data),
      total: Number(data?.total || 0),
      page: Number(data?.page || query.page || 1),
      size: Number(data?.size || query.size || 20),
      hasMore: Boolean(data?.hasMore)
    }
  }
  return unwrapList(data)
}

export async function getOppositeGenderUsers(params = {}) {
  const query = {
    includeActed: true,
    page: params?.page || 1,
    size: params?.size || 20
  }
  if (Array.isArray(params?.ageRange) && params.ageRange.length === 2) {
    query.minAge = params.ageRange[0]
    query.maxAge = params.ageRange[1]
  }
  if (params?.region) query.location = params.region
  if (params?.verifiedOnly) query.verifiedOnly = true
  const data = await request.get('/matches/list', { params: query })
  return {
    list: unwrapList(data),
    total: Number(data?.total || 0),
    page: Number(data?.page || query.page),
    size: Number(data?.size || query.size),
    hasMore: Boolean(data?.hasMore)
  }
}

export async function likeUser(userId) {
  return request.post(`/interactions/like/${userId}`)
}

export async function superlikeUser(userId) {
  return request.post(`/interactions/superlike/${userId}`)
}

export async function dislikeUser(userId) {
  try {
    return await request.post(`/interactions/dislike/${userId}`)
  } catch {
    return { skipped: true, matched: false }
  }
}

export async function getLikeStatus(userId) {
  try {
    return await request.get(`/interactions/like-status/${userId}`)
  } catch {
    return { isLiked: false }
  }
}

export async function filterMatches(data = {}) {
  const payload = {
    ageRange: data?.ageRange || undefined,
    region: data?.region || undefined
  }
  if (data?.gender === 'male') payload.gender = 1
  if (data?.gender === 'female') payload.gender = 2

  const res = await request.post('/matches/filter', payload)
  return unwrapList(res)
}
