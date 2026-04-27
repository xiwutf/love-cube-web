# ⚠️ 废弃目录 — 请勿在此新增代码

该目录是 Fellowship 模块的历史遗留结构，已于 2026-04 启动平台化重构，逐步废弃。

## 迁移去向

| 旧路径 | 新路径 |
|--------|--------|
| `modules/fellowship/pages/` | `src/pages/fellowship/` |
| `modules/fellowship/components/` | `src/components/fellowship/` |
| `modules/fellowship/api/` | `src/api/` |
| `modules/fellowship/stores/` | `src/stores/` |
| `modules/fellowship/composables/` | `src/composables/` |
| `modules/fellowship/utils/` | `src/utils/` |

## 注意事项

- 所有新功能统一放到 `src/pages/fellowship/` 和 `src/components/fellowship/`
- 路由定义统一放到 `src/router/modules/fellowship.routes.js`
- 此目录暂时保留，待所有引用清理完毕后删除
- 如发现仍有代码从此目录 import，请迁移到对应新路径
