# Dev Seed 数据说明

## 适用环境
- 开发环境（`dev`）
- 内测/预发布环境（非生产）

不建议在生产环境直接执行。本目录脚本用于快速构造接近真实业务的数据分布。

## 文件清单
- `dev-seed.sql`：插入冷启动数据（幂等）
- `dev-seed-rollback.sql`：回滚本次 seed 数据

## 数据范围
- 平台公告：5 条（`announcements`）
- 文章：10 条（`articles`）
- 活动：8 条（`events`）
- 首页配置（Banner 位）：3 条（`home_configs`）
- 联谊用户：15 个（`users`）
- 联谊资料：15 条（`fellowship_profile`）
- 用户照片：每人 2 张（`user_photos`，共约 30 条）

## 幂等与识别策略
- 内容表使用固定 `id`：
  - 公告：`dev-seed-announcement-*`
  - 文章：`dev-seed-article-*`
  - 活动：`dev-seed-event-*`
- 用户使用固定 `openid`：`dev_seed_openid_*`
- 首页配置使用固定 `config_key`：`dev_seed_home_banner_*`
- 回滚脚本仅按上述标识删除，不影响真实业务数据。

## 执行方式
在目标库执行（MySQL）：

```bash
mysql -h <host> -P <port> -u <user> -p <database> < backend/src/main/resources/db/seed/dev-seed.sql
```

## 回滚方式
在同一数据库执行：

```bash
mysql -h <host> -P <port> -u <user> -p <database> < backend/src/main/resources/db/seed/dev-seed-rollback.sql
```

## 注意事项
- 请先确认数据库已执行项目 Flyway migration。
- 脚本为幂等写法，可重复执行，不会重复插入同一条 seed 记录。
- 建议执行前备份目标库（尤其内测环境）。
- 当前脚本未向 `banners` 表插入数据：该表在实体中存在，但当前仓库 Flyway/schema 未提供建表迁移，使用 `home_configs` 作为首页 Banner 配置落点。

