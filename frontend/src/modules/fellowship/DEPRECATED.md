DEPRECATED MODULE NOTICE

This `frontend/src/modules/fellowship` tree is kept only for backward compatibility and historical reference.

Active production routes are currently wired to `frontend/src/pages/*` and `frontend/src/components/*`.

Do not add new business logic to this module tree in phase 2.
Any profile-related changes should be implemented in:
- `frontend/src/pages/fellowship/profile/*`
- `frontend/src/pages/fellowship/MePage.vue`
- `frontend/src/pages/profile-edit/index.vue` (legacy-compatible bridge only)
