ALTER TABLE growth_campaign_delivery
    ADD COLUMN action_url VARCHAR(512) NULL AFTER template_code;

UPDATE growth_campaign_delivery
SET action_url = CASE template_code
    WHEN 'VERIFY_NOW' THEN '/#/fellowship/verify'
    WHEN 'COMPLETE_PROFILE' THEN '/#/fellowship/me'
    WHEN 'ENABLE_FELLOWSHIP' THEN '/#/fellowship/me'
    WHEN 'UPLOAD_PHOTOS' THEN '/#/fellowship/me'
    WHEN 'CITY_REMINDER' THEN '/#/fellowship/profile/edit'
    WHEN 'NEARLY_COMPLETE' THEN '/#/fellowship/me'
    ELSE NULL
END
WHERE action_url IS NULL;
