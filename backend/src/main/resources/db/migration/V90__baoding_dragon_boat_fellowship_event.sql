-- 河北保定 · 端午节联谊活动（演示：活动发布 → 站内通知 → 报名 → 我的活动）

INSERT INTO events (
  id, title, summary, content, location, event_time, signup_count, status,
  category, cover_url, pinned, recommended, view_count, checkin_code, created_at, updated_at
)
VALUES (
  'event-baoding-dragonboat-2026',
  '河北保定 · 端午节联谊活动',
  '端午节当天下午，保定主城区线下联谊：破冰互动、分组交流与自由结识。已完成资料完善与实名认证的用户优先报名。',
  '【活动亮点】\n· 端午主题破冰：猜灯谜、组队包粽子体验（轻量参与）\n· 分组轮换交流：每组 15 分钟，便于认识更多伙伴\n· 现场签到核验，活动结束支持互评反馈\n\n【时间】2026 年 6 月 19 日（周四）14:00 集合，预计 17:30 结束\n【地点】河北省保定市莲池区滨河公园南门广场（爱情广场一侧草坪区）\n【人数】限额 40 人，报满即止\n【报名】平台活动页一键报名；活动开始前将收到站内提醒\n【签到】现场出示报名记录，输入 6 位签到码完成签到\n\n欢迎保定及周边城市伙伴参加，一起度过轻松愉快的端午下午。',
  '河北省保定市莲池区滨河公园南门广场',
  '2026-06-19 14:00:00',
  0,
  'published',
  '线下联谊',
  'https://images.unsplash.com/photo-1529156069898-49953e39b3ac?auto=format&fit=crop&w=1200&q=80',
  1,
  1,
  0,
  'BD5268',
  NOW(),
  NOW()
)
ON DUPLICATE KEY UPDATE
  title = VALUES(title),
  summary = VALUES(summary),
  content = VALUES(content),
  location = VALUES(location),
  event_time = VALUES(event_time),
  status = VALUES(status),
  category = VALUES(category),
  cover_url = VALUES(cover_url),
  pinned = VALUES(pinned),
  recommended = VALUES(recommended),
  checkin_code = VALUES(checkin_code),
  updated_at = NOW();

INSERT INTO announcements (
  id, title, summary, content, status, publish_date, category, cover_url,
  pinned, recommended, popup_enabled, view_count, created_at, updated_at
)
VALUES (
  'announcement-baoding-dragonboat-2026',
  '【活动通知】河北保定端午节联谊活动开启报名',
  '6 月 19 日（端午）保定滨河公园线下联谊，破冰互动 + 分组交流，限额 40 人。',
  '平台现已开放「河北保定 · 端午节联谊活动」报名。\n\n活动时间：2026-06-19 14:00\n活动地点：保定市莲池区滨河公园南门广场\n适合人群：已完成基础资料、希望拓展同城社交圈的伙伴\n\n点击活动详情页即可报名；报名成功后可在「我的活动」查看进度，活动开始前会收到提醒通知。',
  'published',
  NOW(),
  '活动公告',
  'https://images.unsplash.com/photo-1515169067868-5387ec356754?auto=format&fit=crop&w=1200&q=80',
  1,
  1,
  1,
  0,
  NOW(),
  NOW()
)
ON DUPLICATE KEY UPDATE
  title = VALUES(title),
  summary = VALUES(summary),
  content = VALUES(content),
  status = VALUES(status),
  publish_date = VALUES(publish_date),
  pinned = VALUES(pinned),
  recommended = VALUES(recommended),
  popup_enabled = VALUES(popup_enabled),
  updated_at = NOW();

-- 启动后由 SeededEventNotificationBootstrap 读取并发送一次站内广播
INSERT INTO home_configs (config_key, config_value, config_group, sort_order, enabled)
VALUES (
  'event-baoding-dragonboat-2026',
  '{"eventId":"event-baoding-dragonboat-2026","announcementId":"announcement-baoding-dragonboat-2026"}',
  'notify_bootstrap',
  0,
  1
)
ON DUPLICATE KEY UPDATE enabled = 1;
