import request from './request.js'

export function fetchMyGroupActivitySignups() {
  return request.get('/platform/my-activities/group-signups')
}

export function fetchMyActivityProposalsAll() {
  return request.get('/platform/my-activities/proposals')
}

export function fetchMyHostedActivityCount() {
  return request.get('/platform/my-activities/hosted-count')
}
