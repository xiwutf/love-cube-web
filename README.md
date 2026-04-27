# Love Cube

双层结构项目：官网平台层（PC/移动端响应式）+ 联谊子应用层（H5，`/fellowship/*`）。

## 技术栈

| 层 | 技术 |
|----|------|
| 前端 | Vue 3 + Vite，Vant 4（交友层），纯 CSS（平台层） |
| 后端 | Spring Boot 3，Spring Data JPA，Flyway |
| 数据库 | Aliyun RDS MySQL（远程，无需本地搭建） |
| 实时通信 | 原生 WebSocket |
| 文件存储 | Aliyun OSS（本地开发时已禁用） |

## 本地开发

```bash
# 前端（http://localhost:5173）
cd frontend && npm install && npm run dev

# 后端（http://localhost:8090）
cd backend && mvnw.cmd spring-boot:run   # Windows
cd backend && ./mvnw spring-boot:run     # Mac/Linux
```

后端直连 Aliyun RDS，无需本地数据库。Redis 已禁用（`store-type: none`）。

前端通过 Vite proxy 转发 `/admin` → `http://localhost:8090/admin`，无需额外配置跨域。

## 部署

GitHub Actions 自动部署（`.github/workflows/deploy.yml`）：
- 触发：push 到 `main` 分支，或手动触发
- 目标：`lovecube.xifg.com.cn` → `/var/www/lovecube`

需在 GitHub 仓库 Secrets 中配置：

| Secret | 说明 |
|--------|------|
| `SERVER_HOST` | 服务器地址 |
| `SERVER_USER` | SSH 用户名 |
| `SSH_PRIVATE_KEY` | SSH 私钥 |
| `RELOAD_NGINX` | 填 `true` 时自动 reload Nginx（可选） |

## 关键文档

| 文档 | 用途 |
|------|------|
| `CLAUDE.md` | 架构详情（Claude Code 自动加载） |
| `AGENTS.md` | AI 任务执行入口手册 |
| `DEVGUIDE.md` | 开发规范与代码模式 |
