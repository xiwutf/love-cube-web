# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Love Cube is a website with a Vue 3 frontend (Vite) and Spring Boot 3 backend. The site has two layers:

1. **Website shell** — a full-width website homepage at `/` (WebsiteHome.vue)
2. **Fellowship module** — a mobile-constrained (max 480px) H5 social/dating app under `/fellowship/*` (migrated from WeChat Mini Program)

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
./mvnw spring-boot:run     # Dev server on port 8090 (Linux/Mac)
mvnw.cmd spring-boot:run   # Dev server on port 8090 (Windows)
./mvnw clean package       # Build JAR
```

## Architecture

### Frontend (`frontend/src/`)

**Two-layer layout**: `App.vue` applies `.fellowship-shell` class (max-width 480px, centered) when `route.path.startsWith('/fellowship')`; website pages are full-width. The `@f` Vite alias points to `src/modules/fellowship/`.

**Fellowship module** (`modules/fellowship/`): Self-contained — each subdirectory mirrors `src/` structure:
- `api/` — 10 files using `@f/api/request.js` (401 redirects to `/fellowship/login`)
- `stores/` — `user.js`, `message.js` (store IDs prefixed `fellowship-*` to avoid collisions)
- `composables/` — `useWebSocket.js`, `useImageUpload.js`, `useInfiniteScroll.js`
- `components/` — `NavBar.vue`, `AppTabBar.vue`, `SwipeCard.vue`, `UserCard.vue`
- `pages/` — 14 pages: `home`, `login`, `welcome`, `search`, `match`, `message`, `personal`, `settings`, `user-profile`, `chat`, `dynamic`, `vip`, `profile-edit`, `newcomers`, `landing`
- `utils/` — copies of `src/utils/*` so the module is self-contained; all imports use `@f/`

**API layer**: `api/request.js` — Axios instance injecting `Authorization: Bearer {token}` from localStorage. The fellowship copy redirects 401 to `/fellowship/login`; the root copy redirects to `/login`.

**Routing** (`router/index.js`): Vue Router 4 in hash mode. Routes: `/` → WebsiteHome; `/fellowship` → fellowship landing; `/fellowship/login`, `/fellowship/welcome` → public; all other `/fellowship/*` routes require auth (guard redirects to `/fellowship/login`).

**State**: Pinia. Fellowship module uses its own `fellowship-user` and `fellowship-message` stores to avoid collision with any future website-level stores.

**Real-time chat** (`modules/fellowship/composables/useWebSocket.js`): Native WebSocket at `ws://{host}/ws/chat/{userId}`. Exponential backoff (max 5 attempts, 10 s cap), 30 s heartbeat.

**UI**: Vant 4 (mobile-first) inside fellowship shell; plain CSS for the website homepage. No Tailwind.

### Backend (`backend/src/main/java/com/lovecube/backend/`)

**Auth**: JWT-based stateless auth (jjwt HS256, 10-day expiry). `SecurityConfig` handles token validation; `AuthController` provides H5 login/register endpoints that differ from the original WeChat login flow.

**Data**: Spring Data JPA with Hibernate against Aliyun RDS MySQL (`ddl-auto: none` — schema is pre-created, no migrations). MyBatis is also available for custom queries.

**WebSocket**: Spring WebSocket endpoint at `/admin/ws/chat/{userId}` for real-time chat.

**File uploads**: Aliyun OSS via `UploadController`; disabled locally (`application-dev.yml`).

**API docs**: Swagger UI at `http://localhost:8090/admin/swagger-ui.html` when running locally.

### Frontend ↔ Backend

- Dev: Vite proxy forwards `/admin` → `http://localhost:8090/admin` (set in `vite.config.js`)
- Prod: Direct call to `http://xifg.com.cn:8090/admin` (set in `.env.production`)
- All backend routes are prefixed `/admin`

### Environment files

| File | Purpose |
|------|---------|
| `frontend/.env.development` | `VITE_API_BASE_URL=/admin` (uses Vite proxy) |
| `frontend/.env.production` | `VITE_API_BASE_URL=http://xifg.com.cn:8090/admin` |
| `backend/src/main/resources/application.yml` | Main config: port, JWT, DB, WebSocket |
| `backend/src/main/resources/application-dev.yml` | Local overrides: OSS disabled, local base URL |

## Key Conventions

- **Composition API only** — all Vue components use `<script setup>`.
- **No server-side routing** — hash mode SPA; Nginx serves static files without fallback rewrite.
- **WeChat APIs replaced** — any historical `wx.*` call has an H5 equivalent in `utils/storage.js` or native browser APIs; do not reintroduce `wx.*`.
- **Remote DB** — backend always connects to Aliyun RDS; no local DB setup needed.
- **Redis present but disabled** — `application.yml` has Redis config but caching/session is turned off; do not assume Redis is available.
