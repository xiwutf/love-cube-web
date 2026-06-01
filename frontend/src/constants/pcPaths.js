/** PC 新架构静态路径（PcLayout、始终处于 /pc 壳内的页面使用） */
export const PC_HOME = '/pc/platform'
export const PC_PLAY = '/pc/platform/play'
export const PC_ME = '/pc/platform/me'
export const PC_EVENTS = '/pc/events'
export const PC_FELLOWSHIP = '/fellowship/discover'

export function pcPath(segment = '') {
  const s = String(segment || '').replace(/^\//, '')
  return s ? `${PC_HOME}/${s}` : PC_HOME
}

export function pcEventsPath(id) {
  const eid = id != null && id !== '' ? String(id) : ''
  return eid ? `${PC_EVENTS}/${eid}` : PC_EVENTS
}
