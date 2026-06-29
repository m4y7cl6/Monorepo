# ProjectHub Enterprise — API 文件

**Base URL**：`http://localhost:8080`
**認證**：Bearer Token（Keycloak JWT）
**內容類型**：`application/json`（檔案上傳除外，使用 `multipart/form-data`）

---

## 認證說明

本系統使用 Keycloak 作為 Identity Provider，採 OIDC Authorization Code Flow（前端）。

### 取得 Access Token（開發 / 測試用）

```bash
curl -X POST http://localhost:8180/realms/projecthub/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=projecthub-frontend" \
  -d "grant_type=password" \
  -d "username=admin" \
  -d "password=admin123"
```

回應中的 `access_token` 即為 JWT，有效期 3600 秒。

### 攜帶 Token 呼叫 API

```bash
curl http://localhost:8080/api/projects \
  -H "Authorization: Bearer <access_token>"
```

### 角色說明

| 角色 | 說明 |
|---|---|
| ADMIN | 系統管理員，擁有所有操作權限 |
| PM | 專案管理員，可建立/修改專案、Sprint、需求 |
| LEADER | 團隊領導，可管理任務與 Sprint |
| DEVELOPER | 開發人員，可建立/更新任務、填報工時 |
| TESTER | 測試人員，可回報 Bug、更新狀態 |
| VIEWER | 唯讀，只可查看資料 |

---

## 共用回應格式

### 分頁回應（PageResponse）

支援分頁的 endpoint 回傳以下結構：

```json
{
  "content": [...],
  "totalElements": 42,
  "totalPages": 3,
  "page": 0,
  "size": 20,
  "first": true,
  "last": false
}
```

### 分頁查詢參數

| 參數 | 預設值 | 說明 |
|---|---|---|
| `page` | `0` | 頁碼（0-based） |
| `size` | `20` | 每頁筆數 |
| `sortBy` | 各 endpoint 不同 | 排序欄位（如 `createdAt`、`uploadedAt`） |
| `direction` | `desc` | 排序方向：`asc` 或 `desc` |

### 錯誤回應（RFC 9457 ProblemDetail）

```json
{
  "type": "https://projecthub.local/errors/not-found",
  "title": "Resource Not Found",
  "status": 404,
  "detail": "Project with id 550e8400-e29b-41d4-a716-446655440000 not found",
  "timestamp": "2026-06-29T12:00:00"
}
```

常見 HTTP 狀態碼：

| 狀態碼 | 情境 |
|---|---|
| `200 OK` | 查詢、更新成功 |
| `201 Created` | 新增成功 |
| `204 No Content` | 刪除成功 |
| `400 Bad Request` | 輸入驗證失敗（`@Valid`） |
| `401 Unauthorized` | 未攜帶或 Token 無效 |
| `403 Forbidden` | 角色權限不足 |
| `404 Not Found` | 資源不存在或已軟刪除 |
| `409 Conflict` | 業務規則衝突（如重複代碼、Sprint 狀態錯誤） |

---

## Projects

Base path：`/api/projects`

| 方法 | 路徑 | 說明 | 所需角色 |
|---|---|---|---|
| `GET` | `/api/projects` | 列出所有專案（分頁） | 已登入 |
| `GET` | `/api/projects/{id}` | 取得單一專案 | 已登入 |
| `POST` | `/api/projects` | 建立新專案 | ADMIN、PM |
| `PUT` | `/api/projects/{id}` | 更新專案 | ADMIN、PM |
| `DELETE` | `/api/projects/{id}` | 軟刪除專案 | ADMIN |

### GET /api/projects

**查詢參數**：`page`、`size`、`sortBy`（預設 `createdAt`）、`direction`

**回應範例**：

```json
{
  "content": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "code": "PROJ-001",
      "name": "專案名稱",
      "description": "專案描述",
      "status": "ACTIVE",
      "startDate": "2026-01-01",
      "endDate": "2026-12-31",
      "createdAt": "2026-01-01T00:00:00"
    }
  ],
  "totalElements": 10,
  "totalPages": 1,
  "page": 0,
  "size": 20,
  "first": true,
  "last": true
}
```

### POST /api/projects

**Request Body**：

```json
{
  "code": "PROJ-001",
  "name": "新專案",
  "description": "專案描述",
  "status": "PLANNING",
  "startDate": "2026-07-01",
  "endDate": "2026-12-31",
  "clientName": "客戶名稱"
}
```

`status` 可選值：`PLANNING`、`ACTIVE`、`ON_HOLD`、`COMPLETED`、`CANCELLED`、`ARCHIVED`

---

## Tasks

| 方法 | 路徑 | 說明 | 所需角色 |
|---|---|---|---|
| `GET` | `/api/tasks` | 列出所有任務（分頁） | 已登入 |
| `GET` | `/api/tasks/{id}` | 取得單一任務 | 已登入 |
| `GET` | `/api/projects/{projectId}/tasks` | 列出專案下的任務（分頁） | 已登入 |
| `GET` | `/api/sprints/{sprintId}/tasks` | 列出 Sprint 下的任務（List） | 已登入 |
| `POST` | `/api/tasks` | 建立新任務 | ADMIN、PM、LEADER、DEVELOPER |
| `PUT` | `/api/tasks/{id}` | 更新任務 | ADMIN、PM、LEADER、DEVELOPER |
| `PATCH` | `/api/tasks/{id}/status` | 更新任務狀態（Kanban 拖曳用） | 已登入 |
| `DELETE` | `/api/tasks/{id}` | 軟刪除任務 | ADMIN、PM、LEADER |

### PATCH /api/tasks/{id}/status

**查詢參數**：`status`（必填）

`status` 可選值：`BACKLOG`、`TODO`、`IN_PROGRESS`、`IN_REVIEW`、`TESTING`、`DONE`

**範例**：

```bash
PATCH /api/tasks/550e8400-e29b-41d4-a716-446655440001/status?status=IN_PROGRESS
```

### POST /api/tasks — Request Body

```json
{
  "title": "實作登入頁面",
  "description": "使用 Keycloak OIDC 實作登入",
  "type": "TASK",
  "priority": "HIGH",
  "status": "BACKLOG",
  "projectId": "550e8400-e29b-41d4-a716-446655440000",
  "sprintId": "550e8400-e29b-41d4-a716-446655440010",
  "assigneeId": "550e8400-e29b-41d4-a716-446655440020",
  "estimatedHours": 8,
  "dueDate": "2026-07-15"
}
```

`type` 可選值：`EPIC`、`STORY`、`TASK`、`BUG`、`SPIKE`
`priority` 可選值：`CRITICAL`、`HIGH`、`MEDIUM`、`LOW`

---

## Sprints

| 方法 | 路徑 | 說明 | 所需角色 |
|---|---|---|---|
| `GET` | `/api/sprints` | 列出所有 Sprint（List） | 已登入 |
| `GET` | `/api/sprints/{id}` | 取得單一 Sprint | 已登入 |
| `GET` | `/api/projects/{projectId}/sprints` | 列出專案下的 Sprint（List） | 已登入 |
| `POST` | `/api/sprints` | 建立新 Sprint | ADMIN、PM、LEADER |
| `PUT` | `/api/sprints/{id}` | 更新 Sprint | ADMIN、PM、LEADER |
| `POST` | `/api/sprints/{id}/activate` | 啟動 Sprint（PLANNED → ACTIVE） | ADMIN、PM、LEADER |
| `POST` | `/api/sprints/{id}/complete` | 完成 Sprint（ACTIVE → COMPLETED） | ADMIN、PM、LEADER |
| `DELETE` | `/api/sprints/{id}` | 刪除 Sprint | ADMIN、PM |

### Sprint Lifecycle

```
PLANNED  →  (activate)  →  ACTIVE  →  (complete)  →  COMPLETED
```

業務規則：同一專案下同一時間只能有一個 ACTIVE Sprint，嘗試 activate 時若已有 ACTIVE Sprint 將回傳 `409 Conflict`。

### POST /api/sprints — Request Body

```json
{
  "name": "Sprint 1",
  "goal": "完成登入與基礎 CRUD",
  "projectId": "550e8400-e29b-41d4-a716-446655440000",
  "startDate": "2026-07-01",
  "endDate": "2026-07-14"
}
```

---

## Worklogs

Base path：`/api/worklogs`

| 方法 | 路徑 | 說明 | 所需角色 |
|---|---|---|---|
| `GET` | `/api/worklogs` | 列出所有工時記錄（List） | 已登入 |
| `GET` | `/api/worklogs/{id}` | 取得單一工時記錄 | 已登入 |
| `GET` | `/api/worklogs/task/{taskId}` | 列出特定任務的工時記錄 | 已登入 |
| `GET` | `/api/worklogs/user/{userId}` | 列出特定用戶的工時記錄 | 已登入 |
| `GET` | `/api/worklogs/range` | 依日期區間查詢 | 已登入 |
| `POST` | `/api/worklogs` | 新增工時記錄 | 已登入 |
| `PUT` | `/api/worklogs/{id}` | 更新工時記錄 | 已登入 |
| `DELETE` | `/api/worklogs/{id}` | 刪除工時記錄 | 已登入 |

### GET /api/worklogs/range

**查詢參數**：

| 參數 | 格式 | 說明 |
|---|---|---|
| `startDate` | `yyyy-MM-dd` | 起始日期（含） |
| `endDate` | `yyyy-MM-dd` | 結束日期（含） |

**範例**：

```bash
GET /api/worklogs/range?startDate=2026-07-01&endDate=2026-07-31
```

### POST /api/worklogs — Request Body

```json
{
  "taskId": "550e8400-e29b-41d4-a716-446655440001",
  "userId": "550e8400-e29b-41d4-a716-446655440020",
  "hoursSpent": 4.5,
  "workDate": "2026-07-01",
  "description": "實作 API endpoint"
}
```

---

## Bugs

| 方法 | 路徑 | 說明 | 所需角色 |
|---|---|---|---|
| `GET` | `/api/bugs` | 列出所有 Bug（分頁） | 已登入 |
| `GET` | `/api/bugs/{id}` | 取得單一 Bug | 已登入 |
| `GET` | `/api/projects/{projectId}/bugs` | 列出專案下的 Bug（List） | 已登入 |
| `POST` | `/api/bugs` | 回報新 Bug | 已登入 |
| `PUT` | `/api/bugs/{id}` | 更新 Bug 內容 | 已登入 |
| `PATCH` | `/api/bugs/{id}/status` | 更新 Bug 狀態 | 已登入 |
| `DELETE` | `/api/bugs/{id}` | 軟刪除 Bug | ADMIN、PM、LEADER |

### PATCH /api/bugs/{id}/status

**查詢參數**：`status`（必填）

**範例**：

```bash
PATCH /api/bugs/550e8400-e29b-41d4-a716-446655440030/status?status=IN_PROGRESS
```

### POST /api/bugs — Request Body

```json
{
  "title": "登入頁面在 Safari 顯示異常",
  "description": "詳細重現步驟",
  "severity": "HIGH",
  "priority": "HIGH",
  "status": "OPEN",
  "projectId": "550e8400-e29b-41d4-a716-446655440000",
  "assigneeId": "550e8400-e29b-41d4-a716-446655440020",
  "relatedTaskId": "550e8400-e29b-41d4-a716-446655440001"
}
```

---

## Requirements

| 方法 | 路徑 | 說明 | 所需角色 |
|---|---|---|---|
| `GET` | `/api/requirements` | 列出所有需求（分頁） | 已登入 |
| `GET` | `/api/requirements/{id}` | 取得單一需求 | 已登入 |
| `GET` | `/api/projects/{projectId}/requirements` | 列出專案下的需求（List） | 已登入 |
| `POST` | `/api/requirements` | 建立新需求 | ADMIN、PM、LEADER |
| `PUT` | `/api/requirements/{id}` | 更新需求 | ADMIN、PM、LEADER |
| `PATCH` | `/api/requirements/{id}/status` | 更新需求狀態 | ADMIN、PM、LEADER |
| `DELETE` | `/api/requirements/{id}` | 軟刪除需求 | ADMIN、PM |

### PATCH /api/requirements/{id}/status

**查詢參數**：`status`（必填）

`status` 可選值視 `RequirementStatus` enum 定義。

### POST /api/requirements — Request Body

```json
{
  "title": "使用者需能透過 SSO 登入",
  "description": "整合 Keycloak OIDC，支援 Google / Microsoft 帳號",
  "projectId": "550e8400-e29b-41d4-a716-446655440000",
  "priority": "HIGH",
  "status": "DRAFT"
}
```

---

## Users

Base path：`/api/users`

| 方法 | 路徑 | 說明 | 所需角色 |
|---|---|---|---|
| `GET` | `/api/users` | 列出所有啟用中的用戶（List） | 已登入 |
| `GET` | `/api/users/{id}` | 取得單一用戶 | 已登入 |
| `GET` | `/api/users/me` | 取得當前登入用戶資料（JWT upsert） | 已登入 |
| `DELETE` | `/api/users/{id}` | 軟刪除用戶 | ADMIN |

### GET /api/users/me

前端初始化時呼叫此 endpoint，後端從 JWT claims 取出用戶資訊並在資料庫 upsert（首次登入自動建立本地用戶記錄）。

**回應範例**：

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440020",
  "keycloakId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "username": "dev.user",
  "email": "dev@projecthub.local",
  "firstName": "Dev",
  "lastName": "User",
  "role": "DEVELOPER",
  "createdAt": "2026-01-01T00:00:00"
}
```

---

## Dashboard

Base path：`/api/dashboard`

| 方法 | 路徑 | 說明 | 所需角色 |
|---|---|---|---|
| `GET` | `/api/dashboard/summary` | 取得儀表板摘要統計 | 已登入 |

### GET /api/dashboard/summary — 回應範例

```json
{
  "totalProjects": 5,
  "activeProjects": 3,
  "activeTasks": 24,
  "openBugs": 7,
  "hoursThisWeek": 120.5,
  "tasksByStatus": {
    "BACKLOG": 10,
    "TODO": 5,
    "IN_PROGRESS": 6,
    "IN_REVIEW": 2,
    "TESTING": 1,
    "DONE": 30
  },
  "bugsBySeverity": {
    "CRITICAL": 1,
    "HIGH": 3,
    "MEDIUM": 2,
    "LOW": 1
  }
}
```

---

## Documents

Base path：`/api/documents`

| 方法 | 路徑 | 說明 | 所需角色 |
|---|---|---|---|
| `GET` | `/api/documents` | 列出所有文件（分頁） | 已登入 |
| `GET` | `/api/documents/{id}` | 取得文件 metadata | 已登入 |
| `POST` | `/api/documents/upload` | 上傳檔案至 MinIO | 已登入 |
| `DELETE` | `/api/documents/{id}` | 軟刪除文件並從 MinIO 移除 | 已登入 |

### POST /api/documents/upload

**Content-Type**：`multipart/form-data`

| 參數 | 類型 | 必填 | 說明 |
|---|---|---|---|
| `file` | `MultipartFile` | 是 | 上傳的檔案 |
| `uploadedById` | `UUID` | 是 | 上傳者的用戶 ID |
| `projectId` | `UUID` | 否 | 關聯的專案 ID |
| `folderName` | `String` | 否 | MinIO 中的資料夾名稱（用於分類） |

**curl 範例**：

```bash
curl -X POST http://localhost:8080/api/documents/upload \
  -H "Authorization: Bearer <token>" \
  -F "file=@/path/to/document.pdf" \
  -F "uploadedById=550e8400-e29b-41d4-a716-446655440020" \
  -F "projectId=550e8400-e29b-41d4-a716-446655440000" \
  -F "folderName=specifications"
```

**回應範例**：

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440040",
  "fileName": "document.pdf",
  "contentType": "application/pdf",
  "fileSize": 204800,
  "minioObjectKey": "specifications/document.pdf",
  "projectId": "550e8400-e29b-41d4-a716-446655440000",
  "uploadedById": "550e8400-e29b-41d4-a716-446655440020",
  "uploadedAt": "2026-06-29T12:00:00"
}
```

---

## 完整 Endpoint 速查表

| 模組 | 方法 | 路徑 | 最低權限 |
|---|---|---|---|
| Projects | GET | `/api/projects` | 已登入 |
| Projects | GET | `/api/projects/{id}` | 已登入 |
| Projects | POST | `/api/projects` | PM |
| Projects | PUT | `/api/projects/{id}` | PM |
| Projects | DELETE | `/api/projects/{id}` | ADMIN |
| Tasks | GET | `/api/tasks` | 已登入 |
| Tasks | GET | `/api/tasks/{id}` | 已登入 |
| Tasks | GET | `/api/projects/{projectId}/tasks` | 已登入 |
| Tasks | GET | `/api/sprints/{sprintId}/tasks` | 已登入 |
| Tasks | POST | `/api/tasks` | DEVELOPER |
| Tasks | PUT | `/api/tasks/{id}` | DEVELOPER |
| Tasks | PATCH | `/api/tasks/{id}/status` | 已登入 |
| Tasks | DELETE | `/api/tasks/{id}` | LEADER |
| Sprints | GET | `/api/sprints` | 已登入 |
| Sprints | GET | `/api/sprints/{id}` | 已登入 |
| Sprints | GET | `/api/projects/{projectId}/sprints` | 已登入 |
| Sprints | POST | `/api/sprints` | LEADER |
| Sprints | PUT | `/api/sprints/{id}` | LEADER |
| Sprints | POST | `/api/sprints/{id}/activate` | LEADER |
| Sprints | POST | `/api/sprints/{id}/complete` | LEADER |
| Sprints | DELETE | `/api/sprints/{id}` | PM |
| Worklogs | GET | `/api/worklogs` | 已登入 |
| Worklogs | GET | `/api/worklogs/{id}` | 已登入 |
| Worklogs | GET | `/api/worklogs/task/{taskId}` | 已登入 |
| Worklogs | GET | `/api/worklogs/user/{userId}` | 已登入 |
| Worklogs | GET | `/api/worklogs/range` | 已登入 |
| Worklogs | POST | `/api/worklogs` | 已登入 |
| Worklogs | PUT | `/api/worklogs/{id}` | 已登入 |
| Worklogs | DELETE | `/api/worklogs/{id}` | 已登入 |
| Bugs | GET | `/api/bugs` | 已登入 |
| Bugs | GET | `/api/bugs/{id}` | 已登入 |
| Bugs | GET | `/api/projects/{projectId}/bugs` | 已登入 |
| Bugs | POST | `/api/bugs` | 已登入 |
| Bugs | PUT | `/api/bugs/{id}` | 已登入 |
| Bugs | PATCH | `/api/bugs/{id}/status` | 已登入 |
| Bugs | DELETE | `/api/bugs/{id}` | LEADER |
| Requirements | GET | `/api/requirements` | 已登入 |
| Requirements | GET | `/api/requirements/{id}` | 已登入 |
| Requirements | GET | `/api/projects/{projectId}/requirements` | 已登入 |
| Requirements | POST | `/api/requirements` | LEADER |
| Requirements | PUT | `/api/requirements/{id}` | LEADER |
| Requirements | PATCH | `/api/requirements/{id}/status` | LEADER |
| Requirements | DELETE | `/api/requirements/{id}` | PM |
| Users | GET | `/api/users` | 已登入 |
| Users | GET | `/api/users/{id}` | 已登入 |
| Users | GET | `/api/users/me` | 已登入 |
| Users | DELETE | `/api/users/{id}` | ADMIN |
| Dashboard | GET | `/api/dashboard/summary` | 已登入 |
| Documents | GET | `/api/documents` | 已登入 |
| Documents | GET | `/api/documents/{id}` | 已登入 |
| Documents | POST | `/api/documents/upload` | 已登入 |
| Documents | DELETE | `/api/documents/{id}` | 已登入 |
