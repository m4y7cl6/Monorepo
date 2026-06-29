# ProjectHub Enterprise — 系統架構

## 整體架構圖

```
使用者瀏覽器
     │
     │  HTTP :80
     ▼
┌─────────────────────┐
│  Nginx (Frontend)   │  Angular 20 SPA 靜態資源
│  projecthub-frontend│
└─────────┬───────────┘
          │  /api/* reverse proxy
          │  HTTP :8080
          ▼
┌─────────────────────┐
│  Spring Boot Backend│  REST API / Business Logic
│  projecthub-backend │  port 8080
└──────┬──────┬───────┘
       │      │      │
       │      │      └─────────────────────────────────┐
       │      │                                         │
       ▼      ▼                                         ▼
┌──────────┐ ┌──────────────────┐             ┌────────────────┐
│PostgreSQL│ │    Keycloak      │             │     MinIO      │
│  :5432   │ │    :8180         │             │    :9000       │
│          │ │  realm:          │             │  bucket:       │
│schema:   │ │  projecthub      │             │  projecthub    │
│  public  │ │                  │             │                │
│  keycloak│ │  6 roles         │             │ 物件儲存       │
└──────────┘ │  3 default users │             └────────────────┘
             └──────────────────┘
```

所有服務透過 Docker bridge network `projecthub-net` 互聯，容器間使用服務名稱（hostname）互相定址。

---

## 服務說明

### Frontend（Nginx + Angular）

| 項目 | 內容 |
|---|---|
| Image | `nginx:1.27-alpine`（multi-stage build，第一階段 `node:20-alpine`） |
| 對外 Port | `80` |
| 職責 | 服務 Angular SPA 靜態資源；`/api/*` 請求反向代理至 backend |
| 特性 | SPA routing（`try_files $uri /index.html`）、gzip 壓縮、static assets 長期快取、security headers |
| 依賴 | backend（healthcheck 通過後才啟動） |

### Backend（Spring Boot）

| 項目 | 內容 |
|---|---|
| Image | `eclipse-temurin:21-jre-alpine`（multi-stage build，第一階段 Maven 建置） |
| 對外 Port | `8080` |
| 職責 | REST API 服務、業務邏輯、JWT 驗證、資料存取、MinIO 檔案操作 |
| Active Profile | `prod`（production profile） |
| 健康檢查 | `GET /actuator/health` |
| 依賴 | postgres（healthy）、keycloak（healthy） |

### PostgreSQL

| 項目 | 內容 |
|---|---|
| Image | `postgres:17-alpine` |
| 對外 Port | `5432` |
| 資料庫 | `projecthub` |
| 用戶 | `projecthub` / `projecthub123` |
| Schema | `public`（應用程式資料）、`keycloak`（Keycloak 持久化） |
| 資料持久化 | `./postgres_data` volume mount |
| 初始化 | `infra/postgres/init.sql` 建立 `keycloak` schema |

### Keycloak

| 項目 | 內容 |
|---|---|
| Image | `quay.io/keycloak/keycloak:25.0` |
| 對外 Port | `8180`（內部 `8080`） |
| Realm | `projecthub` |
| 啟動模式 | `start-dev --import-realm`（開發模式，自動匯入 realm） |
| Realm 設定 | Access Token 有效期 3600 秒、Email 登入、PKCE S256 |
| Clients | `projecthub-frontend`（public, PKCE）、`projecthub-backend`（bearer-only） |
| 依賴 | postgres（healthy） |

### MinIO

| 項目 | 內容 |
|---|---|
| Image | `minio/minio:latest` |
| 對外 Port | `9000`（API）、`9001`（Web Console） |
| Bucket | `projecthub`（匿名 download 權限） |
| 認證 | `minioadmin` / `minioadmin123` |
| 資料持久化 | `./minio_data` volume mount |
| 初始化 | `minio-init` 服務（`minio/mc`）在 MinIO healthy 後建立 bucket |

---

## 認證流程

### OIDC Authorization Code Flow（瀏覽器端）

```
1. 使用者進入 http://localhost
2. Angular 偵測未登入 → keycloak-angular 重導向至 Keycloak 登入頁
   GET http://localhost:8180/realms/projecthub/protocol/openid-connect/auth
       ?client_id=projecthub-frontend
       &redirect_uri=http://localhost/
       &response_type=code
       &code_challenge=<PKCE S256>

3. 用戶輸入帳號密碼 → Keycloak 驗證

4. Keycloak 重導向回 Angular，攜帶 authorization_code

5. Angular 以 code 換取 Access Token
   POST http://localhost:8180/realms/projecthub/protocol/openid-connect/token
       code=<authorization_code>
       code_verifier=<PKCE verifier>

6. Keycloak 回傳 JWT Access Token（含 realm_access.roles）

7. Angular HTTP Interceptor 自動在每次 API 請求加上：
   Authorization: Bearer <access_token>
```

### Backend JWT 驗證

```
API 請求攜帶 Bearer Token
       │
       ▼
Spring Security OAuth2 Resource Server
       │  驗證 JWT 簽章（從 Keycloak JWK endpoint 取公鑰）
       │  驗證 issuer-uri = http://keycloak:8080/realms/projecthub
       │  解析 realm_access.roles → Spring Security Authorities
       ▼
@PreAuthorize 方法層級權限控制
       │  如：hasRole('ADMIN')、isAuthenticated()
       ▼
Controller → Service → Repository
```

---

## 資料庫 Schema 說明

Flyway 管理兩個 migration 版本：
- `V1__init_schema.sql`：建立所有資料表
- `V2__seed_data.sql`：植入範例資料

### 資料表關係

```
users
  │
  ├─ projects (created_by → users)
  │     │
  │     ├─ tasks (project_id, assignee_id → users, sprint_id)
  │     │     │
  │     │     └─ worklogs (task_id, user_id → users)
  │     │
  │     ├─ sprints (project_id)
  │     │     └─ tasks (sprint_id → sprints) [見上]
  │     │
  │     ├─ bugs (project_id, assignee_id → users, related_task_id → tasks)
  │     │
  │     ├─ requirements (project_id)
  │     │
  │     └─ documents (project_id, uploaded_by_id → users)
```

### 各資料表說明

| 資料表 | 主鍵 | 說明 | 軟刪除 |
|---|---|---|---|
| `users` | UUID | 用戶資訊，從 Keycloak JWT upsert | 是 |
| `projects` | UUID | 專案基本資訊與狀態 | 是 |
| `tasks` | UUID | 任務（EPIC/STORY/TASK/BUG/SPIKE）| 是 |
| `sprints` | UUID | Sprint 規劃與 lifecycle 狀態 | 否 |
| `worklogs` | UUID | 工時填報記錄 | 否 |
| `bugs` | UUID | Bug 回報與追蹤 | 是 |
| `requirements` | UUID | 需求管理 | 是 |
| `documents` | UUID | MinIO 物件的 metadata 記錄 | 是 |

所有資料表均有 `created_at`、`updated_at` 審計欄位（UTC 時區）。

---

## 模組依賴關係

### Backend 層次結構

```
Controller 層（HTTP 請求/回應，@PreAuthorize）
       │
       ▼
Service 層（業務邏輯、驗證、軟刪除）
       │
       ├── Repository 層（Spring Data JPA，JPQL 查詢）
       │         │
       │         └── PostgreSQL
       │
       ├── MapStruct Mapper（Entity ↔ DTO 轉換）
       │
       └── MinIO Client（DocumentService 檔案操作）
```

### Frontend 層次結構

```
Route（lazy-loaded feature modules）
       │
       ▼
Page Components（Smart Components，注入 Services）
       │
       ├── Shared Components（PageHeader、ConfirmDialog、StatusBadge）
       │
       └── Services（HTTP 呼叫，透過 Interceptor 附加 Bearer Token）
                 │
                 └── Spring Boot Backend /api/*
```

### Agent 分工（開發期）

| Agent | 職責 |
|---|---|
| `backend-agent` | Spring Boot 程式碼、Entity、Service、Controller |
| `frontend-agent` | Angular 元件、Service、i18n |
| `qa-agent` | 撰寫測試（JUnit 5、Mockito、MockMvc） |
| `doc-agent` | 維護文件（README、API.md、CHANGELOG）|

---

## 技術選型說明

### 為何選 Keycloak

- 開源、企業級 Identity Provider，支援 OIDC / OAuth2 / SAML
- Realm 匯出設定（`realm-export.json`）讓開發環境可自動匯入，無需手動設定
- 未來可擴充 Google、Microsoft SSO 整合，不需修改 backend 程式碼
- Realm roles 直接映射至 Spring Security Authorities，簡化權限管理

### 為何用 Flyway

- 資料庫 migration 版本化管理，確保所有環境 schema 一致
- `validate` 模式（`ddl-auto: validate`）防止程式碼與 DB schema 不同步
- 提供可追蹤的 migration 歷史，便於回溯與審計

### 為何選 MapStruct

- 編譯期生成 mapping 程式碼，無 runtime reflection 效能開銷
- 明確分離 Entity 與 DTO，避免 API 洩漏內部 schema 細節
- 配合 Lombok 可大幅減少樣板程式碼

### 為何選 MinIO

- 相容 AWS S3 API，未來可無縫遷移至 S3 或其他 S3-compatible 儲存
- 自托管，資料不離開本地環境
- Docker 部署簡單，開發與生產環境一致

### 為何用 RFC 9457 ProblemDetail

- 標準化錯誤格式，前端可統一解析
- Spring 6 原生支援，`@ControllerAdvice` 整合簡單
- 包含 `type`、`title`、`status`、`detail` 欄位，錯誤資訊豐富且可機器讀取

### 為何選 Angular CDK Drag-and-Drop（Kanban）

- Angular 官方 Component Dev Kit，無額外第三方依賴
- 整合 Angular 變更偵測，拖曳結束後直接呼叫 service 更新後端
- 支援多 container 間拖曳，符合 Kanban 多欄位需求
