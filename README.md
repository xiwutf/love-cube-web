# Love Cube

Love Cube 是一个双层结构项目：
- 官网平台层（PC/移动端响应式）：公告、资讯、活动、规则与入口页
- 联谊子应用层（`/fellowship/*`）：保留移动端 UI 体验

## GitHub Actions 自动部署

已配置工作流：`/.github/workflows/deploy.yml`

触发方式：
- `push` 到 `main` 分支自动触发
- 支持手动触发（`workflow_dispatch`）

部署目标：
- 域名：`lovecube.xifg.com.cn`
- 目录：`/var/www/lovecube`

### 工作流执行步骤

1. `checkout` 代码
2. 安装 Node.js 18
3. 在 `frontend/` 执行依赖安装
4. 在 `frontend/` 执行 `npm run build`
5. SSH 到服务器并清理部署目录
6. SCP 上传 `frontend/dist/**` 到 `/var/www/lovecube`
7. 可选重载 Nginx

### 需要在 GitHub 仓库配置的 Secrets

必填：
- `SERVER_HOST`：服务器地址（例如 `lovecube.xifg.com.cn`）
- `SERVER_USER`：SSH 用户名
- `SSH_PRIVATE_KEY`：SSH 私钥内容（多行）

可选：
- `RELOAD_NGINX`：填 `true` 时自动执行 Nginx reload

### Nginx reload 说明

工作流中使用：
- `sudo systemctl reload nginx || systemctl reload nginx`

如果部署用户无权限，请为该用户配置免密 `sudo`，或将该步骤保持关闭（不设置 `RELOAD_NGINX=true`）。

## 本地开发

该工作流不会影响本地开发流程。你仍可按原方式在本地运行：

```bash
cd frontend
npm install
npm run dev
```
