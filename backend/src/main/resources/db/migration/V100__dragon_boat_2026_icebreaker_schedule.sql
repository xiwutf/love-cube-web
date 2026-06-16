-- 破冰帖改由 DragonBoat2026CampaignService 在 6/16 定时发布；撤销 V99 迁移中的即时插入

UPDATE dynamics
SET is_deleted = 1,
    updated_at = NOW()
WHERE marker = '[CAMPAIGN|DRAGON_BOAT_2026|ICEBREAKER]'
  AND is_deleted = 0;
