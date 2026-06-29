# Changelog

All notable changes to ProjectHub Enterprise will be documented in this file.

格式遵循 [Keep a Changelog](https://keepachangelog.com/zh-TW/1.0.0/)，版本號遵循 [Semantic Versioning](https://semver.org/)。

---

## [1.0.0] - 2026-06-29

### Phase 4 — Docker Compose 與 Production Hardening

#### Added
- **docker-compose.yml**：整合 postgres、keycloak、minio、backend、frontend 五個服務，統一以 `projecthub-net` bridge network 互聯
- **backend/Dockerfile**：Maven multi-stage build（`maven:3.9-eclipse-temurin-21` 建置 → `eclipse-temurin:21-jre-alpine` 執行），最小化映像檔體積
- **frontend/Dockerfile**：Node multi-stage build（`node:20-alpine` 建置 Angular → `nginx:1.27-alpine` 服務靜態資源）
- **frontend/nginx.conf**：SPA routing（`try_files` fallback）、`/api/` reverse proxy 至 backend、X-Frame-Options / X-Content-Type-Options 等 security headers、gzip 壓縮、靜態資源長期快取
- **infra/keycloak/realm-export.json**：自動匯入 `projecthub` realm，定義 6 個角色（ADMIN、PM、LEADER、DEVELOPER、TESTER、VIEWER）、3 個預設帳號（admin / pm.user / dev.user）、2 個 client（projecthub-frontend public PKCE、projecthub-backend bearer-only）
- **infra/postgres/init.sql**：Container 首次啟動時自動建立 `keycloak` schema，供 Keycloak 持久化使用
- **backend/src/main/resources/application-prod.yml**：Production profile，包含 HikariCP 連線池調整、JSON 格式結構化日誌、Prometheus metrics 端點
- **.gitignore**：排除 `target/`、`node_modules/`、`postgres_data/`、`minio_data/` 等建置與資料目錄
- **Healthcheck 串聯**：postgres → keycloak → backend → frontend 的 `depends_on` + `healthcheck` 確保有序啟動

---

### Phase 3 — Dashboard、Kanban Board、Sprint Management

#### Added
- **Dashboard 頁面**：四張摘要卡片（Total Projects、Active Tasks、Open Bugs、Hours This Week），資料來源為 `GET /api/dashboard/summary`；純 CSS 實作圓餅圖與橫條圖，無第三方圖表函式庫依賴
- **Kanban Board**：使用 Angular CDK drag-and-drop，六欄位（BACKLOG、TODO、IN_PROGRESS、IN_REVIEW、TESTING、DONE），拖曳後即時呼叫 `PATCH /api/tasks/{id}/status` 更新後端狀態
- **Sprint Management**：Sprint 列表、新增/編輯表單、詳細頁面，支援 activate（PLANNED → ACTIVE）與 complete（ACTIVE → COMPLETED）lifecycle 操作；詳細頁顯示速度圖（Velocity）與任務完成率 progress bar
- **i18n 擴充**：於 `zh-TW.json` 及 `en.json` 新增 DASHBOARD、KANBAN、SPRINT 鍵值群組
- **Sidebar 更新**：新增 Dashboard、Kanban、Sprint 三個導覽項目；應用程式預設首頁路由改為 `/dashboard`

---

### Phase 2 — Angular 20 Frontend

#### Added
- **Angular 20 架構**：全 standalone component，feature 模組採 lazy-loaded routes，無 NgModule
- **Keycloak 整合**：keycloak-angular OIDC login flow，`APP_INITIALIZER` 初始化 Keycloak adapter，`AuthGuard` 保護需認證路由，HTTP interceptor 自動附加 Bearer token 至所有 `/api/*` 請求
- **Angular Material 20 UI**：`MainLayoutComponent`（responsive sidenav）、`SidebarComponent`、`TopbarComponent`，支援手機收合側邊欄
- **語言切換**：右上角切換繁體中文（預設）/ English，使用 `@ngx-translate/core`，偏好以 `localStorage` 持久化
- **i18n 資源檔**：`src/assets/i18n/zh-TW.json` 與 `en.json`，涵蓋所有 UI 文字
- **Core models**：`Project`、`Task`、`Sprint`、`Worklog`、`Bug`、`Requirement`、`User`、`PageResponse<T>`、`DashboardSummary` TypeScript 介面
- **API services**：`ProjectService`、`TaskService`、`SprintService`、`WorklogService`、`BugService`、`RequirementService`、`UserService`、`DashboardService`，對應各 backend endpoint
- **Shared 元件**：`PageHeaderComponent`、`ConfirmDialogComponent`（Material Dialog）、`StatusBadgeComponent`、`TaskStatusPipe`
- **Feature CRUD 頁面**：
  - Projects：列表（分頁表格）、新增/編輯表單、詳細頁（含 Tasks / Sprints / Requirements / Bugs / Documents 分頁）
  - Tasks：列表（分頁表格）、新增/編輯表單、詳細頁（含 Worklogs 子列表）
  - Bugs：列表（分頁表格）、新增/編輯表單
  - Requirements：列表（分頁表格）、新增/編輯表單
  - Worklogs：列表（日期區間篩選）、新增/編輯表單
- **proxy.conf.json**：開發模式下 `/api` 代理至 `http://localhost:8080`，避免 CORS 問題

---

### Phase 1 — Spring Boot Backend Foundation

#### Added
- **Spring Boot 3.5 + Java 21** Maven 專案，群組 `com.projecthub`
- **JPA Entities**：`User`、`Project`、`Task`、`Sprint`、`Worklog`、`Requirement`、`Bug`、`Document`，全部使用 UUID 主鍵，含 `createdAt`/`updatedAt` 審計欄位與軟刪除 `deleted` flag
- **Enum 定義**：
  - `UserRole`：ADMIN、PM、LEADER、DEVELOPER、TESTER、VIEWER
  - `ProjectStatus`：PLANNING、ACTIVE、ON_HOLD、COMPLETED、CANCELLED、ARCHIVED
  - `TaskType`：EPIC、STORY、TASK、BUG、SPIKE
  - `TaskPriority`：CRITICAL、HIGH、MEDIUM、LOW
  - `TaskStatus`：BACKLOG、TODO、IN_PROGRESS、IN_REVIEW、TESTING、DONE
  - `BugStatus`、`BugSeverity`、`RequirementStatus` 等
- **Flyway migrations**：
  - `V1__init_schema.sql`：建立所有資料表、外鍵、索引
  - `V2__seed_data.sql`：植入範例資料（2 專案、5 用戶、10 任務、3 Sprint）
- **MapStruct mappers**：Entity ↔ DTO 自動映射，DTO 層分 request（CreateRequest / UpdateRequest）與 response（Dto）
- **Service 層**：軟刪除（`deleted = true`）、Spring Data 分頁、業務規則驗證（如不允許兩個同時 ACTIVE 的 Sprint）
- **REST Controllers**：`/api/projects`、`/api/tasks`、`/api/sprints`、`/api/worklogs`、`/api/bugs`、`/api/requirements`、`/api/users`、`/api/dashboard`、`/api/documents`
- **Spring Security**：OAuth2 Resource Server，Keycloak JWT converter 從 `realm_access.roles` 提取角色，`@PreAuthorize` 細粒度方法層級權限控制
- **SpringDoc OpenAPI 3**：Swagger UI 位於 `/swagger-ui.html`，支援 Bearer Token 認證測試
- **MinIO 整合**：`DocumentService` 透過 MinIO Java SDK 處理檔案上傳/刪除，物件儲存於 `projecthub` bucket
- **RFC 9457 ProblemDetail**：全域 `@ControllerAdvice` 統一例外處理，回傳標準化錯誤格式

---

[1.0.0]: https://github.com/m4y7cl6/Monorepo/releases/tag/v1.0.0
