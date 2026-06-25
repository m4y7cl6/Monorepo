# ProjectHub Enterprise

企業級專案管理平台 — Jira + Confluence + Redmine 整合替代方案

## 功能模組

| 模組 | 說明 |
|---|---|
| 儀表板 | 統計摘要卡片 + CSS 圖表（專案/任務/問題分佈）|
| 專案管理 | CRUD、狀態追蹤、客戶管理 |
| 任務管理 | EPIC/STORY/TASK/BUG/SPIKE，優先序，指派人 |
| Kanban 看板 | 拖曳更新狀態，6 欄位（BACKLOG→DONE）|
| Sprint 管理 | 建立/啟動/完成 Sprint，速度報告 |
| 問題回報 | Bug 追蹤，嚴重度/優先序 |
| 需求管理 | 需求→任務關聯 |
| 工時管理 | 填報工時，日期區間查詢 |
| 文件中心 | MinIO 檔案上傳/下載 |

## 技術架構

```
frontend/   Angular 20 + Material + ngx-translate (zh-TW / EN)
backend/    Spring Boot 3.5 + Java 21 + Spring Security OAuth2
infra/      Keycloak realm 匯入設定
```

## 快速啟動

```bash
# 一鍵啟動所有服務
docker compose up -d

# 查看服務狀態
docker compose ps
```

服務啟動後：

| 服務 | URL |
|---|---|
| 前端應用 | http://localhost |
| 後端 API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Keycloak 管理 | http://localhost:8180 (admin / admin123) |
| MinIO 控制台 | http://localhost:9001 (minioadmin / minioadmin123) |

## 預設帳號

| 帳號 | 密碼 | 角色 |
|---|---|---|
| admin | admin123 | ADMIN |
| pm.user | pm123 | PM |
| dev.user | dev123 | DEVELOPER |

## 本地開發

### Backend
```bash
cd backend
# 需要本地 PostgreSQL + Keycloak，或先 docker compose up postgres keycloak -d
mvn spring-boot:run
```

### Frontend
```bash
cd frontend
npm install
npm start   # http://localhost:4200，proxy → http://localhost:8080
```

## 語言切換

點擊右上角 **language** 圖示可切換 **繁體中文 / English**，偏好設定自動儲存至 localStorage。

## API 文件

啟動後訪問 http://localhost:8080/swagger-ui.html，支援 Bearer Token 認證測試。

## 資料庫 Schema

Flyway 自動管理：
- `V1__init_schema.sql` — 建立所有資料表與索引
- `V2__seed_data.sql` — 植入範例資料（2 專案、5 用戶、10 任務、3 Sprint）
