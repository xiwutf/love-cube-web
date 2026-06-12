/**
 * 轻量验收：两个通知渠道设置入口的路由与 API 挂载是否齐全（无浏览器）。
 */
import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const errors = []

function read(rel) {
  return fs.readFileSync(path.join(root, rel), 'utf8')
}

function assertContains(haystack, needle, label) {
  if (!haystack.includes(needle)) {
    errors.push(`${label}: 缺少 ${needle}`)
  }
}

// 平台层路由
const platformRoutes = read('src/router/modules/platform.routes.js')
assertContains(platformRoutes, "path: 'me/notification-settings'", 'platform.routes')
assertContains(platformRoutes, 'MyNotificationChannelSettingsPage.vue', 'platform.routes component')

// 联谊层路由
const fellowshipRoutes = read('src/router/modules/fellowship.routes.js')
assertContains(fellowshipRoutes, "path: 'notification-settings'", 'fellowship.routes')

// API 三件套
const api = read('src/api/notificationChannelPrefs.js')
;['/notification-channel-prefs', 'put(', 'post('].forEach((n) => assertContains(api, n, 'notificationChannelPrefs.js'))

// 平台页：读/存/测
const platformPage = read('src/pages/platform/MyNotificationChannelSettingsPage.vue')
;['getNotificationChannelPrefs', 'putNotificationChannelPrefs', 'postNotificationChannelTest'].forEach((n) =>
  assertContains(platformPage, n, 'MyNotificationChannelSettingsPage')
)
assertContains(platformPage, '开启 PushPlus 前请填写 Token', 'platform page token validation')

// 联谊页：读/存/测
const fellowshipPage = read('src/pages/notification-settings/index.vue')
;['getNotificationChannelPrefs', 'putNotificationChannelPrefs', 'postNotificationChannelTest'].forEach((n) =>
  assertContains(fellowshipPage, n, 'fellowship notification-settings')
)
assertContains(fellowshipPage, '开启 PushPlus 前请填写 Token', 'fellowship page token validation')

// 不接公众号/服务号/企业微信（外发实现仅 PushPlus + 邮件）
const dispatchSrc = fs.readFileSync(
  path.join(root, '../backend/src/main/java/com/lovecube/backend/services/NotificationDispatchService.java'),
  'utf8'
)
if (/mp\.weixin\.qq\.com|企业微信|服务号/.test(dispatchSrc)) {
  errors.push('NotificationDispatchService: 不应包含微信公众号/服务号/企业微信外发逻辑')
}

if (errors.length) {
  console.error('[verify-notification-channel] FAILED')
  errors.forEach((e) => console.error(' -', e))
  process.exit(1)
}

console.log('[verify-notification-channel] OK: 双入口路由、API 与 Token 校验齐全')
