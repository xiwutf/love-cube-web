# Content Semantic Governance P0 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在动态发布、评论发布、团体介绍编辑三个入口前加入语义风险检测，命中敏感词时弹出柔和提示并提供替换建议，不强制拦截。

**Architecture:** 后端新建 SensitiveWordService（词表+检测）、SemanticNormalizeService（替换建议）、ContentRiskService（编排+日志），通过 POST /api/content/check 对外暴露；前端新建 useContentCheck composable 封装请求与对话框状态，ContentCheckDialog.vue 渲染提示弹层，在三个发布入口前插入检测钩子。

**Tech Stack:** Spring Boot 3 / Spring Data JPA / Flyway（后端）；Vue 3 Composition API、plain CSS（无 Vant，PC 平台层）

---

## File Map

| 操作 | 文件 |
|------|------|
| CREATE | `backend/.../db/migration/V64__content_risk_log.sql` |
| CREATE | `backend/.../entity/ContentRiskLog.java` |
| CREATE | `backend/.../repository/ContentRiskLogRepository.java` |
| CREATE | `backend/.../services/SensitiveWordService.java` |
| CREATE | `backend/.../services/SemanticNormalizeService.java` |
| CREATE | `backend/.../services/ContentRiskService.java` |
| CREATE | `backend/.../controllers/ContentCheckController.java` |
| CREATE | `frontend/src/api/content.js` |
| CREATE | `frontend/src/composables/useContentCheck.js` |
| CREATE | `frontend/src/components/common/ContentCheckDialog.vue` |
| MODIFY | `frontend/src/components/platform/groups/GroupDetail.desktop.vue` |
| MODIFY | `frontend/src/pages/admin/GroupEditAdminPage.vue` |

Base package path: `backend/src/main/java/com/lovecube/backend/`

---

## Task 1: Flyway Migration — content_risk_log 表

**Files:**
- Create: `backend/src/main/resources/db/migration/V64__content_risk_log.sql`

- [ ] **Step 1: 创建 migration 文件**

```sql
CREATE TABLE content_risk_log (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    original_text TEXT        NOT NULL,
    suggested_text TEXT,
    hit_words   VARCHAR(500),
    risk_level  VARCHAR(20)  NOT NULL,
    user_id     BIGINT,
    context     VARCHAR(100),
    created_at  DATETIME     NOT NULL,
    INDEX idx_risk_level (risk_level),
    INDEX idx_user_id    (user_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

- [ ] **Step 2: 启动后端，确认 Flyway 执行成功**

运行：`./mvnw spring-boot:run`（或 `mvnw.cmd spring-boot:run`）
期望：控制台打印 `Successfully applied 1 migration to schema` 且无 `FlywayException`

- [ ] **Step 3: 提交**

```bash
git add backend/src/main/resources/db/migration/V64__content_risk_log.sql
git commit -m "feat: add content_risk_log migration V64"
```

---

## Task 2: ContentRiskLog Entity + Repository

**Files:**
- Create: `backend/src/main/java/com/lovecube/backend/entity/ContentRiskLog.java`
- Create: `backend/src/main/java/com/lovecube/backend/repository/ContentRiskLogRepository.java`

- [ ] **Step 1: 创建 Entity**

```java
package com.lovecube.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "content_risk_log")
public class ContentRiskLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_text", columnDefinition = "TEXT", nullable = false)
    private String originalText;

    @Column(name = "suggested_text", columnDefinition = "TEXT")
    private String suggestedText;

    @Column(name = "hit_words", length = 500)
    private String hitWords;

    @Column(name = "risk_level", length = 20, nullable = false)
    private String riskLevel;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "context", length = 100)
    private String context;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
```

- [ ] **Step 2: 创建 Repository**

```java
package com.lovecube.backend.repository;

import com.lovecube.backend.entity.ContentRiskLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContentRiskLogRepository extends JpaRepository<ContentRiskLog, Long> {
}
```

- [ ] **Step 3: 提交**

```bash
git add backend/src/main/java/com/lovecube/backend/entity/ContentRiskLog.java
git add backend/src/main/java/com/lovecube/backend/repository/ContentRiskLogRepository.java
git commit -m "feat: add ContentRiskLog entity and repository"
```

---

## Task 3: SensitiveWordService — 词表与检测

**Files:**
- Create: `backend/src/main/java/com/lovecube/backend/services/SensitiveWordService.java`

- [ ] **Step 1: 创建 SensitiveWordService**

```java
package com.lovecube.backend.services;

import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SensitiveWordService {

    // 有序 Map，确保替换时长词优先匹配
    private static final Map<String, String> WORD_MAP = new LinkedHashMap<>();

    static {
        WORD_MAP.put("弟兄姊妹", "伙伴");
        WORD_MAP.put("灵命成长", "个人成长");
        WORD_MAP.put("代祷清单", "心愿清单");
        WORD_MAP.put("信仰小组", "兴趣小组");
        WORD_MAP.put("祷告",    "祝福");
        WORD_MAP.put("祈祷",    "祝福");
        WORD_MAP.put("教会",    "团体");
        WORD_MAP.put("团契",    "小组");
        WORD_MAP.put("灵修",    "成长记录");
        WORD_MAP.put("见证",    "经历分享");
        WORD_MAP.put("代祷",    "互助支持");
        WORD_MAP.put("查经",    "学习小组");
        WORD_MAP.put("主内",    "圈子内");
        WORD_MAP.put("牧养",    "陪伴");
        WORD_MAP.put("敬拜",    "音乐活动");
        WORD_MAP.put("福音",    "好消息");
        WORD_MAP.put("蒙恩",    "收获");
        WORD_MAP.put("认罪",    "反思");
    }

    public Map<String, String> getWordMap() {
        return Collections.unmodifiableMap(WORD_MAP);
    }

    /** 返回文本中命中的所有敏感词（去重）。 */
    public List<String> detectHits(String text) {
        if (text == null || text.isBlank()) return Collections.emptyList();
        return WORD_MAP.keySet().stream()
                .filter(text::contains)
                .collect(Collectors.toList());
    }

    /** 0 → low，1-2 → medium，3+ → high */
    public String calculateRiskLevel(int hitCount) {
        if (hitCount == 0) return "low";
        if (hitCount <= 2)  return "medium";
        return "high";
    }
}
```

- [ ] **Step 2: 确认编译通过**

运行：`./mvnw compile -q`
期望：无编译错误

- [ ] **Step 3: 提交**

```bash
git add backend/src/main/java/com/lovecube/backend/services/SensitiveWordService.java
git commit -m "feat: add SensitiveWordService with semantic word map"
```

---

## Task 4: SemanticNormalizeService — 替换建议

**Files:**
- Create: `backend/src/main/java/com/lovecube/backend/services/SemanticNormalizeService.java`

- [ ] **Step 1: 创建 SemanticNormalizeService**

```java
package com.lovecube.backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class SemanticNormalizeService {

    @Autowired
    private SensitiveWordService sensitiveWordService;

    /**
     * 将文本中所有敏感词替换为中性表达，返回建议文案。
     * 若无命中词则返回原文。
     */
    public String normalize(String text) {
        if (text == null || text.isBlank()) return text;
        String result = text;
        for (Map.Entry<String, String> entry : sensitiveWordService.getWordMap().entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }
        return result;
    }
}
```

- [ ] **Step 2: 确认编译通过**

运行：`./mvnw compile -q`
期望：无编译错误

- [ ] **Step 3: 提交**

```bash
git add backend/src/main/java/com/lovecube/backend/services/SemanticNormalizeService.java
git commit -m "feat: add SemanticNormalizeService for text normalization"
```

---

## Task 5: ContentRiskService — 编排 + 日志

**Files:**
- Create: `backend/src/main/java/com/lovecube/backend/services/ContentRiskService.java`

- [ ] **Step 1: 创建 ContentRiskService**

```java
package com.lovecube.backend.services;

import com.lovecube.backend.entity.ContentRiskLog;
import com.lovecube.backend.repository.ContentRiskLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class ContentRiskService {

    @Autowired
    private SensitiveWordService sensitiveWordService;

    @Autowired
    private SemanticNormalizeService semanticNormalizeService;

    @Autowired
    private ContentRiskLogRepository logRepository;

    /**
     * 检测文本风险，记录日志（仅命中时），返回检测结果 Map。
     *
     * @param text    待检测文本
     * @param userId  发起请求的用户 ID，未登录时为 null
     * @param context 调用场景标识，如 "group-post"、"comment"、"group-info"
     */
    public Map<String, Object> check(String text, Long userId, String context) {
        List<String> hits = sensitiveWordService.detectHits(text);
        String riskLevel = sensitiveWordService.calculateRiskLevel(hits.size());
        String suggestion = hits.isEmpty() ? "" : semanticNormalizeService.normalize(text);

        if (!hits.isEmpty()) {
            ContentRiskLog log = new ContentRiskLog();
            log.setOriginalText(text);
            log.setSuggestedText(suggestion);
            log.setHitWords(String.join(",", hits));
            log.setRiskLevel(riskLevel);
            log.setUserId(userId);
            log.setContext(context);
            logRepository.save(log);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("riskLevel",    riskLevel);
        result.put("hitWords",     hits);
        result.put("suggestion",   suggestion);
        result.put("allowPublish", true);
        return result;
    }
}
```

- [ ] **Step 2: 确认编译通过**

运行：`./mvnw compile -q`
期望：无编译错误

- [ ] **Step 3: 提交**

```bash
git add backend/src/main/java/com/lovecube/backend/services/ContentRiskService.java
git commit -m "feat: add ContentRiskService with logging"
```

---

## Task 6: ContentCheckController — POST /api/content/check

**Files:**
- Create: `backend/src/main/java/com/lovecube/backend/controllers/ContentCheckController.java`

- [ ] **Step 1: 创建 Controller**

```java
package com.lovecube.backend.controllers;

import com.lovecube.backend.services.AdminAuthService;
import com.lovecube.backend.services.ContentRiskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/content")
public class ContentCheckController {

    @Autowired
    private ContentRiskService contentRiskService;

    @Autowired
    private AdminAuthService adminAuthService;

    @PostMapping("/check")
    public ResponseEntity<?> check(
            @RequestBody Map<String, Object> request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        String text = (String) request.get("text");
        if (text == null || text.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "text is required"));
        }

        Long userId = null;
        try {
            if (authHeader != null && !authHeader.isBlank()) {
                userId = adminAuthService.requireUser(authHeader).getId();
            }
        } catch (Exception ignored) {
            // 未登录或 token 失效，仍允许检测，userId 记为 null
        }

        String context = (String) request.getOrDefault("context", "content-check");
        Map<String, Object> result = contentRiskService.check(text, userId, context);
        return ResponseEntity.ok(result);
    }
}
```

- [ ] **Step 2: 启动后端，用 curl 验证接口**

```bash
curl -s -X POST http://localhost:8090/admin/api/content/check \
  -H "Content-Type: application/json" \
  -d '{"text":"今天在教会祷告，弟兄姊妹都来了"}' | python -m json.tool
```

期望响应：
```json
{
  "riskLevel": "high",
  "hitWords": ["弟兄姊妹", "教会", "祷告"],
  "suggestion": "今天在团体祝福，伙伴都来了",
  "allowPublish": true
}
```

再测试无敏感词：
```bash
curl -s -X POST http://localhost:8090/admin/api/content/check \
  -H "Content-Type: application/json" \
  -d '{"text":"今天天气很好，一起去打球吧"}' | python -m json.tool
```

期望：`"riskLevel": "low"`，`"hitWords": []`

- [ ] **Step 3: 提交**

```bash
git add backend/src/main/java/com/lovecube/backend/controllers/ContentCheckController.java
git commit -m "feat: add POST /api/content/check endpoint"
```

---

## Task 7: 前端 API 模块 + Composable

**Files:**
- Create: `frontend/src/api/content.js`
- Create: `frontend/src/composables/useContentCheck.js`

- [ ] **Step 1: 创建 content.js**

```javascript
// frontend/src/api/content.js
import request from './request.js'

export function checkContent(text, context = 'content-check') {
  return request.post('/content/check', { text, context })
}
```

- [ ] **Step 2: 创建 useContentCheck.js**

```javascript
// frontend/src/composables/useContentCheck.js
import { reactive } from 'vue'
import { checkContent } from '@/api/content.js'

export function useContentCheck() {
  const state = reactive({
    show: false,
    suggestion: '',
    hitWords: [],
    riskLevel: '',
    checking: false
  })

  let resolveCallback = null

  /**
   * 检测文本，若低风险直接返回 { ok: true }；
   * 若中/高风险，显示对话框并返回一个 Promise，
   * 用户点击 [使用建议] → resolves { ok: true, suggestion: '...' }
   * 用户点击 [继续发布] → resolves { ok: true, suggestion: null }
   */
  async function check(text, context) {
    if (!text || !text.trim()) return { ok: true }
    state.checking = true
    try {
      const result = await checkContent(text, context)
      if (result.riskLevel === 'low') return { ok: true }
      state.suggestion = result.suggestion || ''
      state.hitWords = result.hitWords || []
      state.riskLevel = result.riskLevel
      state.show = true
      return new Promise(resolve => {
        resolveCallback = resolve
      })
    } catch {
      // 检测失败不阻断发布
      return { ok: true }
    } finally {
      state.checking = false
    }
  }

  function applySuggestion() {
    const suggestion = state.suggestion
    _close()
    resolveCallback?.({ ok: true, suggestion })
    resolveCallback = null
  }

  function continueAnyway() {
    _close()
    resolveCallback?.({ ok: true, suggestion: null })
    resolveCallback = null
  }

  function _close() {
    state.show = false
    state.suggestion = ''
    state.hitWords = []
    state.riskLevel = ''
  }

  return { state, check, applySuggestion, continueAnyway }
}
```

- [ ] **Step 3: 提交**

```bash
git add frontend/src/api/content.js frontend/src/composables/useContentCheck.js
git commit -m "feat: add content check API and useContentCheck composable"
```

---

## Task 8: ContentCheckDialog 组件

**Files:**
- Create: `frontend/src/components/common/ContentCheckDialog.vue`

> 此组件属于 PC 平台层，**不使用 Vant**，使用 plain CSS。

- [ ] **Step 1: 创建 ContentCheckDialog.vue**

```vue
<template>
  <div class="ccd-overlay" @click.self="$emit('continue')">
    <div class="ccd-card">
      <div class="ccd-header">
        <span class="ccd-icon">!</span>
        <h3 class="ccd-title">建议使用更通用的表达方式</h3>
      </div>

      <p class="ccd-desc">
        检测到以下词汇可能不适合平台使用：
        <span v-for="w in hitWords" :key="w" class="ccd-hit-word">{{ w }}</span>
      </p>

      <div v-if="suggestion" class="ccd-suggestion">
        <p class="ccd-suggestion-label">建议文案</p>
        <p class="ccd-suggestion-text">{{ suggestion }}</p>
      </div>

      <div class="ccd-actions">
        <button class="ccd-btn ccd-btn-primary" @click="$emit('use-suggestion')">
          使用建议
        </button>
        <button class="ccd-btn ccd-btn-ghost" @click="$emit('continue')">
          继续发布
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  suggestion: { type: String, default: '' },
  hitWords:   { type: Array,  default: () => [] }
})
defineEmits(['use-suggestion', 'continue'])
</script>

<style scoped>
.ccd-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: var(--z-modal, 1000);
}

.ccd-card {
  background: #fff;
  border-radius: 12px;
  padding: 28px 32px;
  width: 440px;
  max-width: calc(100vw - 32px);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.16);
}

.ccd-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
}

.ccd-icon {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #f59e0b;
  color: #fff;
  font-weight: 700;
  font-size: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.ccd-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
}

.ccd-desc {
  font-size: 14px;
  color: #555;
  line-height: 1.6;
  margin-bottom: 14px;
}

.ccd-hit-word {
  display: inline-block;
  margin: 0 4px;
  padding: 1px 7px;
  background: #fff7ed;
  color: #c2410c;
  border: 1px solid #fed7aa;
  border-radius: 4px;
  font-size: 13px;
}

.ccd-suggestion {
  background: #f8fafb;
  border-left: 3px solid var(--lc-blue, #4f86f7);
  padding: 12px 14px;
  border-radius: 0 6px 6px 0;
  margin-bottom: 20px;
}

.ccd-suggestion-label {
  font-size: 12px;
  color: #888;
  margin: 0 0 6px;
}

.ccd-suggestion-text {
  font-size: 14px;
  color: #333;
  margin: 0;
  line-height: 1.6;
  white-space: pre-wrap;
}

.ccd-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.ccd-btn {
  padding: 8px 22px;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  border: none;
  transition: opacity 0.15s;
}
.ccd-btn:hover { opacity: 0.85; }

.ccd-btn-primary {
  background: var(--lc-blue, #4f86f7);
  color: #fff;
}

.ccd-btn-ghost {
  background: #f3f4f6;
  color: #555;
}
</style>
```

- [ ] **Step 2: 提交**

```bash
git add frontend/src/components/common/ContentCheckDialog.vue
git commit -m "feat: add ContentCheckDialog component for content risk advisory"
```

---

## Task 9: 接入 GroupDetail.desktop.vue — 动态发布

**Files:**
- Modify: `frontend/src/components/platform/groups/GroupDetail.desktop.vue`

需要修改的四处位置：
1. 添加 import（`<script setup>` 顶部）
2. 添加 composable 实例化
3. 修改 `submitPost()`（L1356）
4. 修改 `submitQuickPost()`（L1378）
5. 在 template 中添加 `<ContentCheckDialog>`

- [ ] **Step 1: 在 `<script setup>` 顶部已有 import 区域添加两行 import**

在文件的 import 区域（`import { useUserStore }` 附近），追加：

```javascript
import { useContentCheck } from '@/composables/useContentCheck.js'
import ContentCheckDialog from '@/components/common/ContentCheckDialog.vue'
```

- [ ] **Step 2: 在 stores 实例化之后（`const userStore = useUserStore()` 行之后）添加 composable**

```javascript
const contentCheck = useContentCheck()
```

- [ ] **Step 3: 替换 `submitPost` 函数（原 L1356-L1376）**

旧代码：
```javascript
async function submitPost() {
  const text = postForm.content.trim()
  const imgs = postForm.imageUrls.trim()
  if (!text && !imgs) {
    flashMessage('请填写文字或图片链接', 'error')
    return
  }
  posting.value = true
  try {
    await createGroupPost(group.value.id, { content: text, imageUrls: imgs || undefined })
    postForm.content = ''
    postForm.imageUrls = ''
    await loadPosts()
    await loadDetail()
    flashMessage('动态发布成功')
  } catch (error) {
    flashMessage(error.message || '动态发布失败', 'error')
  } finally {
    posting.value = false
  }
}
```

新代码：
```javascript
async function submitPost() {
  const text = postForm.content.trim()
  const imgs = postForm.imageUrls.trim()
  if (!text && !imgs) {
    flashMessage('请填写文字或图片链接', 'error')
    return
  }
  if (text) {
    const checkResult = await contentCheck.check(text, 'group-post')
    if (!checkResult.ok) return
    if (checkResult.suggestion) postForm.content = checkResult.suggestion
  }
  posting.value = true
  try {
    await createGroupPost(group.value.id, {
      content: postForm.content.trim(),
      imageUrls: postForm.imageUrls.trim() || undefined
    })
    postForm.content = ''
    postForm.imageUrls = ''
    await loadPosts()
    await loadDetail()
    flashMessage('动态发布成功')
  } catch (error) {
    flashMessage(error.message || '动态发布失败', 'error')
  } finally {
    posting.value = false
  }
}
```

- [ ] **Step 4: 替换 `submitQuickPost` 函数（原 L1378-L1393）**

旧代码：
```javascript
async function submitQuickPost() {
  const text = quickPostContent.value.trim()
  if (!text || quickPosting.value) return
  quickPosting.value = true
  try {
    await createGroupPost(group.value.id, { content: text })
    quickPostContent.value = ''
    await loadPosts()
    await loadDetail()
    flashMessage('动态发布成功')
  } catch (error) {
    flashMessage(error.message || '动态发布失败', 'error')
  } finally {
    quickPosting.value = false
  }
}
```

新代码：
```javascript
async function submitQuickPost() {
  const text = quickPostContent.value.trim()
  if (!text || quickPosting.value) return
  const checkResult = await contentCheck.check(text, 'group-post')
  if (!checkResult.ok) return
  const finalText = checkResult.suggestion || text
  quickPosting.value = true
  try {
    await createGroupPost(group.value.id, { content: finalText })
    quickPostContent.value = ''
    await loadPosts()
    await loadDetail()
    flashMessage('动态发布成功')
  } catch (error) {
    flashMessage(error.message || '动态发布失败', 'error')
  } finally {
    quickPosting.value = false
  }
}
```

- [ ] **Step 5: 在 template 最外层容器的末尾（`</div>` 闭合前）添加 dialog**

找到模板最外层 `<div class="group-detail-page">` 或类似的根节点，在其闭合标签前追加：

```html
<ContentCheckDialog
  v-if="contentCheck.state.show"
  :suggestion="contentCheck.state.suggestion"
  :hit-words="contentCheck.state.hitWords"
  @use-suggestion="contentCheck.applySuggestion"
  @continue="contentCheck.continueAnyway"
/>
```

- [ ] **Step 6: 在浏览器验证**

1. 启动前端 `npm run dev`，打开任意一个团体详情页
2. 在动态发布输入框输入含敏感词的内容，如"在教会祷告"
3. 点击发布，期望弹出 ContentCheckDialog，显示命中词和建议文案
4. 点击「继续发布」，期望正常发布原文
5. 再次输入含敏感词内容，点击「使用建议」，期望表单内容更新为建议文案并成功发布

- [ ] **Step 7: 提交**

```bash
git add frontend/src/components/platform/groups/GroupDetail.desktop.vue
git commit -m "feat: hook content check into group post submission"
```

---

## Task 10: 接入 GroupDetail.desktop.vue — 评论发布

**Files:**
- Modify: `frontend/src/components/platform/groups/GroupDetail.desktop.vue`

- [ ] **Step 1: 替换 `submitComment` 函数（原 L1480-L1496）**

旧代码：
```javascript
async function submitComment(post) {
  const key = post.id
  const text = (commentDraft[key] || '').trim()
  if (!text) return
  commentPosting[key] = true
  try {
    await createGroupPostComment(group.value.id, post.id, { content: text })
    commentDraft[key] = ''
    await loadCommentsForPost(post.id)
    await loadPosts()
    flashMessage('评论已发布')
  } catch (error) {
    flashMessage(error.message || '评论失败', 'error')
  } finally {
    commentPosting[key] = false
  }
}
```

新代码：
```javascript
async function submitComment(post) {
  const key = post.id
  const text = (commentDraft[key] || '').trim()
  if (!text) return
  const checkResult = await contentCheck.check(text, 'comment')
  if (!checkResult.ok) return
  const finalText = checkResult.suggestion || text
  commentPosting[key] = true
  try {
    await createGroupPostComment(group.value.id, post.id, { content: finalText })
    commentDraft[key] = ''
    await loadCommentsForPost(post.id)
    await loadPosts()
    flashMessage('评论已发布')
  } catch (error) {
    flashMessage(error.message || '评论失败', 'error')
  } finally {
    commentPosting[key] = false
  }
}
```

- [ ] **Step 2: 在浏览器验证**

1. 展开某条动态的评论区
2. 输入含敏感词的评论，如"为你代祷"
3. 点击发送，期望弹出风险提示对话框
4. 点击「继续发布」，评论正常提交

- [ ] **Step 3: 提交**

```bash
git add frontend/src/components/platform/groups/GroupDetail.desktop.vue
git commit -m "feat: hook content check into comment submission"
```

---

## Task 11: 接入 GroupEditAdminPage.vue — 团体介绍编辑

**Files:**
- Modify: `frontend/src/pages/admin/GroupEditAdminPage.vue`

- [ ] **Step 1: 在 `<script setup>` 的 import 区域添加**

```javascript
import { useContentCheck } from '@/composables/useContentCheck.js'
import ContentCheckDialog from '@/components/common/ContentCheckDialog.vue'
```

- [ ] **Step 2: 在 `<script setup>` 内 stores 实例化之后添加**

```javascript
const contentCheck = useContentCheck()
```

- [ ] **Step 3: 替换 `saveInfo` 函数（原 L535-L557）**

旧代码：
```javascript
async function saveInfo() {
  if (!form.name) {
    flash('团体名称不能为空', 'error')
    return
  }
  saving.value = true
  try {
    await updateAdminGroup(groupId, {
      name: form.name,
      category: form.category,
      description: form.description,
      coverUrl: form.coverUrl,
      joinType: form.joinType,
      status: form.status
    })
    await loadDetail()
    flash('基础信息已保存')
  } catch (err) {
    flash(err.message || '保存失败', 'error')
  } finally {
    saving.value = false
  }
}
```

新代码：
```javascript
async function saveInfo() {
  if (!form.name) {
    flash('团体名称不能为空', 'error')
    return
  }
  if (form.description) {
    const checkResult = await contentCheck.check(form.description, 'group-info')
    if (!checkResult.ok) return
    if (checkResult.suggestion) form.description = checkResult.suggestion
  }
  saving.value = true
  try {
    await updateAdminGroup(groupId, {
      name: form.name,
      category: form.category,
      description: form.description,
      coverUrl: form.coverUrl,
      joinType: form.joinType,
      status: form.status
    })
    await loadDetail()
    flash('基础信息已保存')
  } catch (err) {
    flash(err.message || '保存失败', 'error')
  } finally {
    saving.value = false
  }
}
```

- [ ] **Step 4: 在 template 末尾（根节点闭合前）添加 dialog**

```html
<ContentCheckDialog
  v-if="contentCheck.state.show"
  :suggestion="contentCheck.state.suggestion"
  :hit-words="contentCheck.state.hitWords"
  @use-suggestion="contentCheck.applySuggestion"
  @continue="contentCheck.continueAnyway"
/>
```

- [ ] **Step 5: 在浏览器验证**

1. 进入后台任意团体编辑页
2. 在「团体简介」文本框输入含敏感词内容，如"这是一个灵修小组，欢迎弟兄姊妹"
3. 点击保存，期望弹出风险提示
4. 点击「使用建议」，简介字段更新为建议文案，再次点击保存成功

- [ ] **Step 6: 提交**

```bash
git add frontend/src/pages/admin/GroupEditAdminPage.vue
git commit -m "feat: hook content check into group description editing"
```

---

## 自检清单

### 规格覆盖

| 需求项 | 对应 Task |
|--------|-----------|
| SensitiveWordService | Task 3 |
| SemanticNormalizeService | Task 4 |
| ContentRiskService | Task 5 |
| POST /api/content/check | Task 6 |
| riskLevel / hitWords / suggestion / allowPublish 响应格式 | Task 6 |
| 动态发布检测 | Task 9 |
| 评论发布检测 | Task 10 |
| 团体介绍编辑检测 | Task 11 |
| medium 时弹出提示对话框 | Task 8 + 9 |
| [使用建议] / [继续发布] 两个按钮 | Task 8 |
| 不强制拦截 | useContentCheck resolves { ok: true } 均允许发布 |
| content_risk_log 日志记录 | Task 1 + 5 |
| 不影响现有业务 | 所有新代码为 additive，原有逻辑完整保留 |

### 接口名称一致性

- `contentCheck.check(text, context)` 在 Task 9、10、11 中均使用此签名 ✓
- `contentCheck.applySuggestion()` / `contentCheck.continueAnyway()` 在 Task 8 的 emit handler 和 Task 9-11 的绑定中名称一致 ✓
- `ContentCheckDialog` emit 事件：`use-suggestion`、`continue` 与 composable 绑定一致 ✓
