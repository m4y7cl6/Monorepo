# ProjectHub Enterprise

## AI-Native Project Management Platform

Version: 1.0

---

# 專案目標

建立一套企業級專案管理平台（ProjectHub Enterprise），整合：

* 專案管理
* 任務管理
* Kanban Board
* Sprint 管理
* 工時管理
* 文件管理
* 需求管理
* Bug Tracking
* Dashboard
* Keycloak SSO

目標是成為 Jira + Confluence + Redmine 的整合替代方案。

---

# 技術架構

## Frontend

* Angular 20+
* Angular Material
* TypeScript
* RxJS
* NgRx（選用）
* ApexCharts
* ngx-drag-drop

---

## Backend

* Java 21
* Spring Boot 3.5+
* Spring Security
* Spring Data JPA
* Spring Validation
* Spring Actuator
* OpenAPI / Swagger

---

## Authentication

Keycloak

需求：

* OIDC Login
* Role Mapping
* JWT Authentication
* SSO

---

## Database

PostgreSQL 17+

---

## File Storage

MinIO

需求：

* PDF
* DOCX
* XLSX
* Image

---

## Container

Docker Compose

未來需支援：

* Kubernetes
* OpenShift

---

# 系統模組

## 1. User Management

### Entity

User

欄位：

* id
* username
* email
* displayName
* role
* status

### Roles

* ADMIN
* PM
* LEADER
* DEVELOPER
* TESTER
* VIEWER

---

## 2. Project Management

### Project

欄位：

* id
* code
* name
* description
* customer
* startDate
* endDate
* status
* createdAt
* updatedAt

### Status

* PLANNING
* DEVELOPMENT
* TESTING
* UAT
* PRODUCTION
* CLOSED

---

## 3. Task Management

### Task

欄位：

* id
* taskNo
* projectId
* title
* description
* taskType
* priority
* status
* assigneeId
* reporterId
* estimateHours
* actualHours
* dueDate

### Task Type

* EPIC
* STORY
* TASK
* BUG
* SPIKE

### Priority

* LOW
* MEDIUM
* HIGH
* CRITICAL

### Status

* BACKLOG
* TODO
* IN_PROGRESS
* REVIEW
* TESTING
* DONE

---

## 4. Kanban Board

功能：

* 拖曳任務
* 即時更新狀態
* 過濾條件
* 搜尋

欄位：

BACKLOG

TODO

IN_PROGRESS

REVIEW

TESTING

DONE

---

## 5. Sprint Management

### Sprint

欄位：

* id
* projectId
* sprintName
* startDate
* endDate
* goal

功能：

* 建立 Sprint
* 關閉 Sprint
* Sprint Report
* Velocity Report

---

## 6. Worklog

### Worklog

欄位：

* id
* taskId
* userId
* workDate
* hours
* description

功能：

* 填報工時
* 查詢工時
* 匯出Excel

---

## 7. Requirement Management

### Requirement

欄位：

* id
* reqNo
* title
* description
* priority
* status

功能：

* Requirement → Task 關聯
* Traceability Matrix

---

## 8. Bug Tracking

### Bug

欄位：

* id
* bugNo
* title
* description
* severity
* priority
* status

### Severity

* LOW
* MEDIUM
* HIGH
* CRITICAL

---

## 9. Document Center

### Folder

* id
* name

### Document

* id
* fileName
* filePath
* fileSize
* uploadedBy
* uploadedAt

功能：

* 上傳
* 下載
* 預覽
* 版本管理

---

## 10. Dashboard

首頁統計：

### Project Summary

* Total Projects
* Active Projects
* Closed Projects

### Task Summary

* Todo
* In Progress
* Done

### Bug Summary

* Open
* Closed

### Worklog Summary

* Daily
* Weekly
* Monthly

圖表：

* Pie Chart
* Bar Chart
* Line Chart

---

# REST API 規範

## Project

GET /api/projects

GET /api/projects/{id}

POST /api/projects

PUT /api/projects/{id}

DELETE /api/projects/{id}

---

## Task

GET /api/tasks

GET /api/tasks/{id}

POST /api/tasks

PUT /api/tasks/{id}

DELETE /api/tasks/{id}

---

## Sprint

GET /api/sprints

POST /api/sprints

PUT /api/sprints/{id}

DELETE /api/sprints/{id}

---

## Worklog

GET /api/worklogs

POST /api/worklogs

PUT /api/worklogs/{id}

DELETE /api/worklogs/{id}

---

# Database Design

建立完整：

* JPA Entity
* Repository
* Service
* Controller
* DTO
* Mapper

需支援：

* Flyway Migration
* Audit Columns
* Soft Delete

---

# 非功能需求

## Security

* JWT
* Keycloak
* RBAC

---

## Performance

* 支援 500+ 同時使用者
* 分頁查詢
* 索引最佳化

---

## Logging

* JSON Log
* ELK Stack Ready

---

## Monitoring

* Spring Actuator
* Prometheus
* Grafana

---

# Docker Compose

需產生：

services:

* frontend
* backend
* postgres
* keycloak
* minio

---

# Claude Code Implementation Requirement

請直接產生完整可執行專案：

Backend:

* Spring Boot
* Maven
* Java 21

Frontend:

* Angular 20
* Angular Material

Database:

* PostgreSQL

Authentication:

* Keycloak

Container:

* Docker Compose

要求：

1. 可直接 docker compose up 啟動
2. Backend 完整 CRUD
3. Frontend 完整 CRUD 畫面
4. Swagger 文件
5. Flyway Migration
6. Keycloak Login
7. MinIO Upload
8. Dashboard 統計頁
9. Kanban Board
10. Sprint Management

產出需符合 Production Ready 等級架構。
