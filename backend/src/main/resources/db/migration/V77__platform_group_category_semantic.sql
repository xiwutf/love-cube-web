-- 迭代 4：平台团体分类规范化 + 团体文案语义治理

-- 1) platform_group.type 存量映射
UPDATE platform_group SET type = 'growth' WHERE LOWER(type) IN ('study', '学习', '读经');
UPDATE platform_group SET type = 'life' WHERE LOWER(type) IN ('family', '生活', '家庭');
UPDATE platform_group SET type = 'volunteer' WHERE LOWER(type) IN ('service', '志愿服务', '服侍');
UPDATE platform_group SET type = 'interest' WHERE LOWER(type) IN ('church', '社群', '敬拜', 'worship');
UPDATE platform_group SET type = 'region' WHERE LOWER(type) IN ('地区', 'local');

-- 2) platform_groups.category 存量映射（文字编号团体）
UPDATE platform_groups SET category = 'growth' WHERE LOWER(category) IN ('study', '学习', '读经');
UPDATE platform_groups SET category = 'life' WHERE LOWER(category) IN ('family', '生活', '家庭');
UPDATE platform_groups SET category = 'volunteer' WHERE LOWER(category) IN ('service', '志愿服务', '服侍');
UPDATE platform_groups SET category = 'interest' WHERE LOWER(category) IN ('church', '社群', '敬拜', 'worship');
UPDATE platform_groups SET category = 'region' WHERE LOWER(category) IN ('地区', 'local');

-- 3) platform_group 名称与简介语义替换（平台可见文案）
UPDATE platform_group SET name = REPLACE(name, '教会', '团体') WHERE name LIKE '%教会%';
UPDATE platform_group SET name = REPLACE(name, '团契', '团体') WHERE name LIKE '%团契%';
UPDATE platform_group SET name = REPLACE(name, '祷告', '心愿') WHERE name LIKE '%祷告%';
UPDATE platform_group SET name = REPLACE(name, '敬拜', '共读') WHERE name LIKE '%敬拜%';
UPDATE platform_group SET name = REPLACE(name, '福音', '故事分享') WHERE name LIKE '%福音%';

UPDATE platform_group SET description = REPLACE(description, '教会', '团体') WHERE description LIKE '%教会%';
UPDATE platform_group SET description = REPLACE(description, '团契', '团体') WHERE description LIKE '%团契%';
UPDATE platform_group SET description = REPLACE(description, '祷告', '心愿') WHERE description LIKE '%祷告%';
UPDATE platform_group SET description = REPLACE(description, '祈祷', '祝福') WHERE description LIKE '%祈祷%';
UPDATE platform_group SET description = REPLACE(description, '敬拜', '共读') WHERE description LIKE '%敬拜%';
UPDATE platform_group SET description = REPLACE(description, '福音', '故事分享') WHERE description LIKE '%福音%';
UPDATE platform_group SET description = REPLACE(description, '弟兄姊妹', '伙伴') WHERE description LIKE '%弟兄姊妹%';
UPDATE platform_group SET description = REPLACE(description, '查经', '共读') WHERE description LIKE '%查经%';
UPDATE platform_group SET description = REPLACE(description, '牧养', '陪伴') WHERE description LIKE '%牧养%';
UPDATE platform_group SET description = REPLACE(description, '主内', '社群') WHERE description LIKE '%主内%';
UPDATE platform_group SET description = REPLACE(description, '见证', '故事分享') WHERE description LIKE '%见证%';
UPDATE platform_group SET description = REPLACE(description, '灵修', '成长记录') WHERE description LIKE '%灵修%';

-- 4) platform_groups 同步治理
UPDATE platform_groups SET name = REPLACE(name, '教会', '团体') WHERE name LIKE '%教会%';
UPDATE platform_groups SET name = REPLACE(name, '团契', '团体') WHERE name LIKE '%团契%';
UPDATE platform_groups SET name = REPLACE(name, '祷告', '心愿') WHERE name LIKE '%祷告%';

UPDATE platform_groups SET description = REPLACE(description, '教会', '团体') WHERE description LIKE '%教会%';
UPDATE platform_groups SET description = REPLACE(description, '团契', '团体') WHERE description LIKE '%团契%';
UPDATE platform_groups SET description = REPLACE(description, '祷告', '心愿') WHERE description LIKE '%祷告%';
UPDATE platform_groups SET description = REPLACE(description, '祈祷', '祝福') WHERE description LIKE '%祈祷%';
UPDATE platform_groups SET description = REPLACE(description, '敬拜', '共读') WHERE description LIKE '%敬拜%';
UPDATE platform_groups SET description = REPLACE(description, '福音', '故事分享') WHERE description LIKE '%福音%';
UPDATE platform_groups SET description = REPLACE(description, '弟兄姊妹', '伙伴') WHERE description LIKE '%弟兄姊妹%';
UPDATE platform_groups SET description = REPLACE(description, '查经', '共读') WHERE description LIKE '%查经%';
UPDATE platform_groups SET description = REPLACE(description, '牧养', '陪伴') WHERE description LIKE '%牧养%';
UPDATE platform_groups SET description = REPLACE(description, '主内', '社群') WHERE description LIKE '%主内%';
UPDATE platform_groups SET description = REPLACE(description, '见证', '故事分享') WHERE description LIKE '%见证%';
UPDATE platform_groups SET description = REPLACE(description, '灵修', '成长记录') WHERE description LIKE '%灵修%';
