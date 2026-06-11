import request from './request.js'
import { isLegacyPlatformGroupId } from './groups.js'

function assertNumericGroup(groupId) {
  if (!isLegacyPlatformGroupId(groupId)) {
    throw new Error('当前团体类型不支持打卡营')
  }
}

export function fetchSpaceCampaigns(groupId) {
  assertNumericGroup(groupId)
  return request.get(`/platform/groups/${groupId}/space-campaigns`)
}

export function fetchActiveSpaceCampaign(groupId) {
  assertNumericGroup(groupId)
  return request.get(`/platform/groups/${groupId}/space-campaigns/active`)
}

export function fetchSpaceCampaignDetail(groupId, campaignId) {
  assertNumericGroup(groupId)
  return request.get(`/platform/groups/${groupId}/space-campaigns/${campaignId}`)
}

export function createSpaceCampaign(groupId, payload) {
  assertNumericGroup(groupId)
  return request.post(`/platform/groups/${groupId}/space-campaigns`, payload)
}

export function completeSpaceCampaignDay(groupId, campaignId, dayNumber) {
  assertNumericGroup(groupId)
  return request.post(`/platform/groups/${groupId}/space-campaigns/${campaignId}/days/${dayNumber}/complete`)
}

export function notifySpaceCampaignMembers(groupId, campaignId) {
  assertNumericGroup(groupId)
  return request.post(`/platform/groups/${groupId}/space-campaigns/${campaignId}/notify`)
}
