# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Love Cube is a Vue 3 + Spring Boot 3 platform with **three distinct layers** that must never be mixed:

| Layer | Routes | Layout | UI |
|-------|--------|--------|----|
| Platform (website) | `/` and `/platform/*` | `PlatformLayout.vue` — full-width | Plain CSS only, no Vant |
| Fellowship (H5 app) | `/fellowship/*` | `FellowshipLayout.vue` — max-width 480px, centered | Vant 4 |
| Admin (back-office) | `/admin/*` | `AdminLayout.vue` — sidebar + main | Plain CSS + Vant for toasts |

## Commands

### Frontend (`frontend/`)
```bash
npm install        # Install dependencies
npm run dev        # Vite dev server on port 5173
npm run build      # Production build → dist/
npm run preview    # Preview production build
```

### Backend (`backend/`)
```bash
mvnw.cmd spring-boot:run   # Dev server on port 8090 (Windows)
./mvnw spring-boot:run     # Dev server on port 8090 (Linux/Mac)
./mvnw clean package       # Build JAR
```

## Architecture

### Frontend (`frontend/src/`)

**Routing** (`router/index.js`): Vue Router 4 in hash mode. Three route modules:
- `platform.routes.js` — wraps the platform layer under `PlatformLayout`
- `fellowship.routes.js` — wraps fellowship pages under `FellowshipLayout`; pages needing auth carry `meta: { requiresAuth: true }`
- `admin.routes.js` — wraps admin pages under `AdminLayout`; all children carry `meta: { requiresAdmin: true }`

**File placement rules:**
- Platform pages → `src/pages/platform/{Name}Page.vue`
- Fellowship pages → `src/pages/{feature}/index.vue` (registered in `fellowship.routes.js`)
- Admin pages → `src/pages/admin/{Name}AdminPage.vue`
- `src/modules/fellowship/pages/` is **legacy** — never add new pages there
- API files → `src/api/{feature}.js`, one file per feature domain

**API layer** (`src/api/request.js`): `baseURL = ${VITE_API_BASE_URL}/api`. In dev (`VITE_API_BASE_URL=/admin`) this resolves to `/admin/api`; Vite proxy forwards `/admin/*` → `http://localhost:8090`. Call paths are relative: `request.get('/groups')` → `/admin/api/groups`. Admin calls use `request.get('/admin/groups')` → `/admin/api/admin/groups`.

**`@f` alias** points to `src/modules/fellowship/` — used only inside the fellowship module for self-contained imports.

**State**: Pinia. Fellowship stores use IDs prefixed `fellowship-*` to avoid collision with platform stores.

**Real-time chat** (`modules/fellowship/composables/useWebSocket.js`): Native WebSocket at `ws://{host}/ws/chat/{userId}`. Exponential backoff (max 5 attempts, 10 s cap), 30 s heartbeat.

**CSS conventions**: `<style scoped>` on every component. Colors use CSS variables from `assets/styles/tokens.css` (`--lc-pink`, `--lc-blue`, etc.). No Tailwind, no hardcoded hex colors in platform/admin pages.

### Platform module — key pages and their routes

New pages are added as children of the platform route with path `platform/{feature}` (the wrapping layout is at `/`, so the full URL becomes `/#/platform/{feature}`):

```
/platform/positive-share   → PositiveSharePage.vue
/platform/groups           → GroupsPage.vue
/platform/groups/:id       → GroupDetailPage.vue  (also handles /members and /feed tabs)
```

Older content pages (`/events`, `/articles`, `/announcements`) do not use the `platform/` prefix — this is intentional legacy inconsistency.

### Groups module (`/platform/groups`)

Four backend tables: `platform_groups`, `group_members`, `group_posts`, `group_join_requests`.

Public API (controller: `GroupController`, `@RequestMapping("/api/groups")`):
- `GET /groups` — list active groups
- `GET /groups/:id` — detail + join status for the current user
- `GET /groups/:id/members` / `/posts` — members and posts
- `POST /groups/:id/join` — direct join (open) or create a pending join request (approval)
- `DELETE /groups/:id/leave`

Admin API (controller: `AdminGroupController`, `@RequestMapping("/api/admin/groups")`):
- Full CRUD on groups
- `GET /admin/groups/:id/join-requests?status=pending` — list requests
- `PATCH /admin/groups/:id/join-requests/:reqId/approve` / `/reject`
- `POST /admin/groups/:id/posts` — publish announcement or post
- `DELETE /admin/groups/:id/members/:userId`

### Backend (`backend/src/main/java/com/lovecube/backend/`)

**Controller path convention**: `@RequestMapping` prefix is `/api/{resource}` for public endpoints and `/api/admin/{resource}` for admin-only endpoints. **Never add `/admin` inside `@RequestMapping`** — the Spring context-path is `/admin` (set in `application.yml`), so the full external path is already `/admin/api/...`.

**Auth**: JWT-based stateless (jjwt HS256, 10-day expiry). Call `adminAuthService.requireUser(authHeader)` to get the current user; call `adminAuthService.requireAdmin(authHeader)` for admin-only endpoints.

**Data**: Spring Data JPA with Hibernate against Aliyun RDS MySQL (`ddl-auto: none`). Schema managed by **Flyway** (`db/migration/V*.sql`, baseline V13). Current highest migration: **V25**. `schema-platform.sql` is NOT auto-executed — reference only.

**Flyway rules**: Never modify an already-executed migration file (breaks checksum). Always create a new `V{N+1}__description.sql`. Files run exactly once, so no need for `IF NOT EXISTS` guards.

**WebSocket**: Spring WebSocket endpoint at `/ws/chat/{userId}` (full external path `/admin/ws/chat/{userId}`).

**File uploads**: Aliyun OSS via `UploadController`; disabled locally (`application-dev.yml`).

**API docs**: Swagger UI at `http://localhost:8090/admin/swagger-ui.html` when running locally.

### Frontend ↔ Backend

- Dev: Vite proxy forwards `/admin` → `http://localhost:8090/admin`
- Prod: Direct call to `http://xifg.com.cn:8090/admin`
- Backend context-path: `/admin` — all Spring endpoints are reached at `/admin/...`

### Environment files

| File | Purpose |
|------|---------|
| `frontend/.env.development` | `VITE_API_BASE_URL=/admin` (uses Vite proxy) |
| `frontend/.env.production` | `VITE_API_BASE_URL=http://xifg.com.cn:8090/admin` |
| `backend/src/main/resources/application.yml` | Port, JWT secret, DB, context-path |
| `backend/src/main/resources/application-dev.yml` | OSS disabled, local base URL override |

### Deployment

GitHub Actions (`.github/workflows/deploy.yml`) deploys to `lovecube.xifg.com.cn` on push to `main`. Backend has a separate workflow (`deploy-backend.yml`). Secrets required: `SERVER_HOST`, `SERVER_USER`, `SSH_PRIVATE_KEY`, optionally `RELOAD_NGINX`.

## Key Conventions

- **Composition API only** — all Vue components use `<script setup>`, no exceptions.
- **No server-side routing** — hash mode SPA; Nginx serves static files without fallback rewrite.
- **Layer isolation** — platform pages must not import fellowship components or use Vant; fellowship pages must not be placed in `src/pages/platform/`.
- **WeChat APIs replaced** — any historical `wx.*` call has an H5 equivalent in `utils/storage.js` or native browser APIs; do not reintroduce `wx.*`.
- **Remote DB** — backend always connects to Aliyun RDS; no local DB setup needed.
- **Redis present but disabled** — `application.yml` has Redis config but caching/session is turned off; do not assume Redis is available.
- **All source files** must be UTF-8 (no BOM). Database tables must use `utf8mb4`.
- **Admin layout nav** — when adding a new admin page, register it in both `navItems` and `sectionMap` in `AdminLayout.vue`.

## Further Reference

- `AGENTS.md` — decision guide for matching task types to the correct files and layers
- `DEVGUIDE.md` — detailed coding patterns, API response formats, fellowship-specific patterns, and a new-feature checklist
