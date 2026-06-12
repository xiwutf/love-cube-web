-- 用户向与后台向更新日志分离：changelog_admin 不对用户 API 暴露

UPDATE home_configs
SET config_value = '{"version":"v1.5.0","title":"通知渠道偏好设置","date":"2026-06-11","detail":"• 消息通知支持按渠道设置偏好（站内、邮件、PushPlus）","enabled":true,"sortOrder":1}'
WHERE config_group = 'changelog'
  AND config_key = 'v1.5.0';

INSERT INTO home_configs (config_key, config_value, config_group, sort_order, enabled)
VALUES
(
  'v1.6.0',
  '{"version":"v1.6.0","title":"活动能力产品化（后台）","date":"2026-06-12","detail":"• V87 activity_reminder_log：活动提醒去重\n• ActivityReminderService / Scheduler：24h、2h、互评提醒\n• UserActivityHubService：我的活动聚合 API\n• NotificationCatalog 新增活动提醒类型","enabled":true,"sortOrder":0}',
  'changelog_admin',
  0,
  1
),
(
  'v1.5.0',
  '{"version":"v1.5.0","title":"通知渠道与投递（后台）","date":"2026-06-11","detail":"• V86：user_notification_channel_prefs、notification_dispatch_records\n• NotificationDispatchService 外发与状态记录\n• AsyncConfig 异步投递","enabled":true,"sortOrder":1}',
  'changelog_admin',
  1,
  1
)
ON DUPLICATE KEY UPDATE
  config_value = VALUES(config_value),
  sort_order = VALUES(sort_order),
  enabled = VALUES(enabled);
