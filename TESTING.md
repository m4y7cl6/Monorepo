# ProjectHub Enterprise — 測試流程規範

## 測試分層架構

```
E2E Tests          ← 瀏覽器操作完整功能（未來 Playwright）
    ↑
Integration Tests  ← Controller + Security + DB（@WebMvcTest / @DataJpaTest）
    ↑
Unit Tests         ← Service / Repository 邏輯（JUnit 5 + Mockito）
```

---

## 後端測試（Spring Boot）

### 執行指令

```bash
cd backend

# 執行所有測試
mvn test

# 只執行單元測試
mvn test -Dgroups="unit"

# 只執行整合測試
mvn test -Dgroups="integration"

# 執行特定 class
mvn test -Dtest=ProjectServiceTest

# 產生測試報告（Surefire）
mvn surefire-report:report
# 報告位置：target/site/surefire-report.html
```

### 測試套件說明

| 層級 | 類別 | 工具 | 說明 |
|---|---|---|---|
| Unit | `*ServiceTest` | JUnit 5 + Mockito | Mock Repository，測試商業邏輯 |
| Unit | `*RepositoryTest` | @DataJpaTest + H2 | 測試 JPQL 查詢 |
| Integration | `*ControllerTest` | @WebMvcTest + MockMvc | 測試 HTTP 層、序列化、權限 |
| Integration | `TestDataFactory` | — | 共用測試資料工廠 |

### 測試環境設定

- DB：H2 in-memory（`application-test.yml`）
- Flyway：停用（改用 JPA `create-drop`）
- JWT：Mock `JwtDecoder`
- MinIO：Mock Bean

### 命名規範

```
methodName_scenario_expectedBehavior()

範例：
getById_whenFound_returnsDto()
getById_whenNotFound_throwsResourceNotFoundException()
create_whenCodeDuplicate_throwsBusinessException()
activate_whenAlreadyActiveSprintExists_throwsBusinessException()
```

### 覆蓋範圍要求

每個功能模組至少涵蓋：
- ✅ 正常流程（Happy Path）
- ✅ 資源不存在（ResourceNotFoundException）
- ✅ 業務規則衝突（BusinessException）
- ✅ 權限驗證（403 Forbidden）
- ✅ 輸入驗證（400 Bad Request）

---

## 前端測試（Angular）

### 執行指令（未來規劃）

```bash
cd frontend

# 單元測試（Karma + Jasmine）
npm test

# E2E 測試（Playwright，尚未實作）
npx playwright test
```

---

## CI / CD 測試流程

### 每次 Push / PR 應執行

```
1. mvn test                    ← 後端所有測試
2. mvn verify                  ← 含整合測試
3. npm run build:prod          ← 前端編譯驗證（無測試也能抓 TS 錯誤）
```

### 測試通過門檻

| 指標 | 最低要求 |
|---|---|
| 後端測試通過率 | 100% |
| Service 層覆蓋率 | ≥ 80% |
| Controller 層覆蓋率 | ≥ 70% |

---

## QA Agent 使用時機

`.claude/agents/qa-agent.md` 定義了 QA Agent，在以下情況應主動委派：

| 時機 | 說明 |
|---|---|
| 新功能完成後 | 請 qa-agent 撰寫對應測試 |
| API 契約變更後 | 請 qa-agent 更新 ControllerTest |
| Bug 修復後 | 請 qa-agent 補充回歸測試案例 |
| Commit 前 | 請 qa-agent 執行測試並回報結果 |

### 呼叫方式

在對話中說：「請用 qa-agent 為 [功能] 補充測試」即可委派。

---

## 測試資料策略

- 使用 `TestDataFactory` 統一建立測試物件
- 不使用真實 DB 資料（避免環境依賴）
- 每個測試互相獨立（不依賴執行順序）
- 使用 `@Transactional` + rollback 確保 DB 狀態隔離

---

## 待實作（Backlog）

- [ ] 前端 Angular 元件單元測試（Jasmine）
- [ ] E2E 測試（Playwright）
- [ ] Contract Testing（Spring Cloud Contract）
- [ ] 效能測試（JMeter / Gatling）— 目標 500+ 同時使用者
- [ ] Security 測試（OWASP ZAP）
