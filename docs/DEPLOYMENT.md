# ProjectHub Enterprise — 部署指南

## 系統需求

| 項目 | 最低要求 | 建議 |
|---|---|---|
| Docker Desktop | 4.x 以上 | 最新穩定版 |
| 記憶體 | 6 GB 可用 | 8 GB 以上 |
| 磁碟空間 | 5 GB（映像檔 + 資料） | 10 GB 以上 |
| 作業系統 | Windows 10/11、macOS 12+、Ubuntu 20.04+ | — |

Keycloak 本身需要約 512 MB–1 GB RAM，Spring Boot JVM 約需 512 MB，請確保 Docker Desktop 記憶體上限設定充足。

---

## 快速啟動

### 首次啟動（全新環境）

```bash
git clone https://github.com/m4y7cl6/Monorepo.git
cd Monorepo
docker compose up -d
```

首次啟動需下載所有映像檔並建置 backend / frontend，預計需要 5–10 分鐘。Keycloak 完全就緒約需 60–90 秒。

### 查看啟動狀態

```bash
# 查看所有容器狀態
docker compose ps

# 即時追蹤所有服務日誌
docker compose logs -f

# 只看 backend 日誌
docker compose logs -f backend
```

### 重建（修改程式碼後）

```bash
# 重建並重新啟動有異動的服務
docker compose up -d --build

# 只重建特定服務
docker compose up -d --build backend
docker compose up -d --build frontend
```

### 停止服務

```bash
# 停止但保留資料
docker compose stop

# 停止並移除容器（資料 volume 保留）
docker compose down
```

### 完整重設（清除所有資料）

```bash
# 停止並移除容器與 volume
docker compose down -v

# 清除持久化資料目錄
rm -rf postgres_data/ minio_data/

# 重新從頭啟動
docker compose up -d
```

**注意**：`down -v` 會移除 Docker named volumes，但 `postgres_data/` 與 `minio_data/` 是 bind mount，需手動刪除。

---

## 服務啟動順序

`docker-compose.yml` 透過 `depends_on` + `healthcheck` 確保正確的啟動順序：

```
postgres (healthcheck: pg_isready)
    │  healthy
    ▼
keycloak (healthcheck: HTTP GET /health/ready)
    │       ← 等待約 60–90 秒，start_period: 90s
    │  healthy
    ▼
backend (healthcheck: GET /actuator/health → "UP")
    │       ← 等待 Keycloak 就緒後 Spring Boot 才能驗證 JWT issuer-uri
    │  healthy
    ▼
frontend (無 healthcheck，backend healthy 後直接啟動)
```

`minio` 與 `minio-init` 獨立並行啟動：

```
minio (healthcheck: curl /minio/health/live)
    │  healthy
    ▼
minio-init (建立 projecthub bucket → 退出)
```

---

## 服務 URL 清單

| 服務 | URL | 說明 |
|---|---|---|
| 前端應用 | http://localhost | Angular SPA 主入口 |
| 後端 API | http://localhost:8080/api | REST API base |
| Swagger UI | http://localhost:8080/swagger-ui.html | API 互動文件，支援 Bearer Token 測試 |
| OpenAPI JSON | http://localhost:8080/api-docs | OpenAPI 3 規格 JSON |
| Actuator Health | http://localhost:8080/actuator/health | Backend 健康檢查 |
| Actuator Metrics | http://localhost:8080/actuator/metrics | Metrics 端點（需認證） |
| Keycloak Admin | http://localhost:8180 | Keycloak 管理控制台 |
| Keycloak Realm | http://localhost:8180/realms/projecthub | Realm OIDC 端點 |
| MinIO API | http://localhost:9000 | MinIO S3-compatible API |
| MinIO Console | http://localhost:9001 | MinIO Web 管理控制台 |

---

## 預設帳號一覽

### Keycloak 應用程式用戶（`projecthub` realm）

| 帳號 | 密碼 | 角色 | Email |
|---|---|---|---|
| `admin` | `admin123` | ADMIN | admin@projecthub.local |
| `pm.user` | `pm123` | PM | pm@projecthub.local |
| `dev.user` | `dev123` | DEVELOPER | dev@projecthub.local |

### Keycloak 管理員

| 帳號 | 密碼 | 用途 |
|---|---|---|
| `admin` | `admin123` | Keycloak Admin Console（http://localhost:8180） |

### PostgreSQL

| 項目 | 值 |
|---|---|
| Host | `localhost:5432`（或容器間使用 `postgres:5432`） |
| Database | `projecthub` |
| 用戶 | `projecthub` |
| 密碼 | `projecthub123` |

### MinIO

| 項目 | 值 |
|---|---|
| Access Key | `minioadmin` |
| Secret Key | `minioadmin123` |
| Bucket | `projecthub` |

---

## 本地開發模式

僅啟動基礎設施服務，backend 與 frontend 在本地以開發模式執行，便於除錯與快速迭代。

### 步驟 1：啟動基礎設施

```bash
cd Monorepo
docker compose up -d postgres keycloak minio minio-init
```

等待 Keycloak 就緒（約 60–90 秒）：

```bash
docker compose logs -f keycloak
# 看到 "Running the server in development mode." 即就緒
```

### 步驟 2：啟動 Backend

```bash
cd backend
# 使用 default profile（指向 localhost 的 keycloak）
mvn spring-boot:run
```

Backend 啟動後訪問 http://localhost:8080/swagger-ui.html 確認正常。

`application.yml` 中的預設 `issuer-uri` 為 `http://keycloak:8080/realms/projecthub`（容器名稱），本地開發需覆寫：

```bash
mvn spring-boot:run \
  -Dspring-boot.run.jvmArguments="-DSPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI=http://localhost:8180/realms/projecthub"
```

### 步驟 3：啟動 Frontend

```bash
cd frontend
npm install
npm start
```

Angular dev server 預設跑在 http://localhost:4200，`proxy.conf.json` 已設定 `/api` 代理至 `http://localhost:8080`。

---

## 監控與觀測

### Spring Boot Actuator

Backend 開放以下 Actuator endpoint：

| Endpoint | URL | 說明 |
|---|---|---|
| Health | `GET /actuator/health` | 服務健康狀態，含 DB / MinIO 子檢查 |
| Info | `GET /actuator/info` | 應用程式資訊 |
| Metrics | `GET /actuator/metrics` | Micrometer metrics 列表 |

`/actuator/health` 的詳細資訊需認證後才顯示（`show-details: when-authorized`）。

### 日誌格式

Production profile 輸出 JSON 結構化日誌，每行格式：

```json
{"timestamp":"2026-06-29 12:00:00.000","level":"INFO","logger":"com.projecthub.service.ProjectService","message":"Project created: PROJ-001"}
```

查看 backend 日誌：

```bash
docker compose logs -f backend
```

---

## 常見問題排除

| 問題 | 可能原因 | 解決方法 |
|---|---|---|
| Keycloak 啟動失敗：`schema "keycloak" does not exist` | `postgres_data/` 有舊資料，`keycloak` schema 未建立 | `docker compose down -v && rm -rf postgres_data/ && docker compose up -d` |
| 前端登入後跳轉失敗：`invalid_redirect_uri` | Keycloak realm 的 redirect URI 設定不符 | 至 Keycloak Admin → Clients → projecthub-frontend → Valid redirect URIs 新增對應 URL |
| 前端白頁（無內容） | Angular build 失敗 | `docker compose logs frontend` 查看 Nginx 與 build 錯誤 |
| Backend 啟動失敗：`Connection refused to keycloak` | Keycloak 尚未通過 healthcheck | 等待 Keycloak 完全就緒（最多 90 秒），或 `docker compose logs keycloak` 確認狀態 |
| MinIO bucket not found | `minio-init` 未執行成功 | `docker compose logs minio-init`；若有錯誤可重跑 `docker compose up minio-init` |
| API 回傳 `401 Unauthorized` | Token 過期或未正確傳遞 | 重新登入取得新 Token；確認 Authorization header 格式為 `Bearer <token>` |
| API 回傳 `403 Forbidden` | 登入用戶角色不足 | 確認帳號對應角色；ADMIN 帳號有完整權限 |
| `docker compose up` 失敗：`port is already in use` | 本機已有服務佔用 80、8080、8180、5432、9000 等 port | 停止衝突服務，或修改 `docker-compose.yml` 的 ports 映射 |
| Backend 日誌顯示 `FlywayException` | DB schema 版本不一致 | 完整重設：`docker compose down -v && rm -rf postgres_data/` |

### 快速診斷指令

```bash
# 查看所有容器狀態與健康
docker compose ps

# 查看特定服務最後 100 行日誌
docker compose logs --tail=100 backend
docker compose logs --tail=100 keycloak

# 進入 postgres 容器確認 schema
docker exec -it projecthub-postgres psql -U projecthub -d projecthub -c "\dn"

# 確認 backend 健康
curl -s http://localhost:8080/actuator/health | python -m json.tool

# 確認 Keycloak realm 存在
curl -s http://localhost:8180/realms/projecthub | python -m json.tool
```
