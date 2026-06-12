-- 用户可见更新日志：通知渠道 v1.5.0、活动能力 v1.6.0
UPDATE home_configs
SET sort_order = sort_order + 2
WHERE config_group = 'changelog'
  AND config_key NOT IN ('v1.5.0', 'v1.6.0');

INSERT INTO home_configs (config_key, config_value, config_group, sort_order, enabled)
VALUES
(
  'v1.6.0',
  '{"version":"v1.6.0","title":"活动能力产品化","date":"2026-06-12","detail":"• 新增「我的活动」页面，集中查看报名、签到、互评与投稿进展\n• 活动详情页支持报名、现场签到与活动结束后互评\n• 活动开始前 24 小时与 2 小时提醒，结束后提醒提交互评\n• 个人工作台恢复邀请二维码","enabled":true,"sortOrder":0}',
  'changelog',
  0,
  1
),
(
  'v1.5.0',
  '{"version":"v1.5.0","title":"通知渠道偏好设置","date":"2026-06-11","detail":"• 消息通知支持按渠道设置偏好（站内、邮件、PushPlus）\n• 通知投递记录便于排查发送状态","enabled":true,"sortOrder":1}',
  'changelog',
  1,
  1
)
ON DUPLICATE KEY UPDATE
  config_value = VALUES(config_value),
  sort_order = VALUES(sort_order),
  enabled = VALUES(enabled);
