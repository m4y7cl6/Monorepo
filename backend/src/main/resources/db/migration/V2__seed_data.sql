-- ProjectHub Enterprise - Seed Data
-- V2__seed_data.sql

-- Users (5)
INSERT INTO users (id, username, email, display_name, role, status, keycloak_id) VALUES
  ('a1000000-0000-0000-0000-000000000001', 'admin', 'admin@projecthub.com', 'System Admin', 'ADMIN', 'ACTIVE', 'kc-admin-001'),
  ('a1000000-0000-0000-0000-000000000002', 'pm.alice', 'alice@projecthub.com', 'Alice Johnson', 'PM', 'ACTIVE', 'kc-alice-002'),
  ('a1000000-0000-0000-0000-000000000003', 'dev.bob', 'bob@projecthub.com', 'Bob Smith', 'DEVELOPER', 'ACTIVE', 'kc-bob-003'),
  ('a1000000-0000-0000-0000-000000000004', 'dev.carol', 'carol@projecthub.com', 'Carol White', 'DEVELOPER', 'ACTIVE', 'kc-carol-004'),
  ('a1000000-0000-0000-0000-000000000005', 'qa.dave', 'dave@projecthub.com', 'Dave Brown', 'TESTER', 'ACTIVE', 'kc-dave-005');

-- Projects (2)
INSERT INTO projects (id, code, name, description, customer, start_date, end_date, status) VALUES
  ('b1000000-0000-0000-0000-000000000001', 'ALPHA', 'Project Alpha', 'Internal platform modernization initiative', 'Internal', '2026-01-01', '2026-12-31', 'DEVELOPMENT'),
  ('b1000000-0000-0000-0000-000000000002', 'BETA', 'Project Beta', 'Customer-facing e-commerce portal rebuild', 'Acme Corp', '2026-03-01', '2026-09-30', 'PLANNING');

-- Sprints (3)
INSERT INTO sprints (id, project_id, sprint_name, start_date, end_date, goal, status) VALUES
  ('c1000000-0000-0000-0000-000000000001', 'b1000000-0000-0000-0000-000000000001', 'Sprint 1', '2026-01-06', '2026-01-19', 'Set up project foundation and CI/CD pipeline', 'COMPLETED'),
  ('c1000000-0000-0000-0000-000000000002', 'b1000000-0000-0000-0000-000000000001', 'Sprint 2', '2026-01-20', '2026-02-02', 'Implement core authentication and user management', 'ACTIVE'),
  ('c1000000-0000-0000-0000-000000000003', 'b1000000-0000-0000-0000-000000000001', 'Sprint 3', '2026-02-03', '2026-02-16', 'Dashboard and reporting features', 'PLANNED');

-- Tasks (10)
INSERT INTO tasks (id, task_no, project_id, sprint_id, title, description, task_type, priority, status, assignee_id, reporter_id, estimate_hours, actual_hours, due_date) VALUES
  ('d1000000-0000-0000-0000-000000000001', 'ALPHA-001', 'b1000000-0000-0000-0000-000000000001', 'c1000000-0000-0000-0000-000000000001', 'Initialize Spring Boot project structure', 'Set up Maven project with all required dependencies', 'TASK', 'HIGH', 'DONE', 'a1000000-0000-0000-0000-000000000003', 'a1000000-0000-0000-0000-000000000002', 8.00, 7.50, '2026-01-10'),
  ('d1000000-0000-0000-0000-000000000002', 'ALPHA-002', 'b1000000-0000-0000-0000-000000000001', 'c1000000-0000-0000-0000-000000000001', 'Configure Docker and docker-compose', 'Create docker-compose for postgres, keycloak, minio', 'TASK', 'HIGH', 'DONE', 'a1000000-0000-0000-0000-000000000003', 'a1000000-0000-0000-0000-000000000002', 6.00, 8.00, '2026-01-12'),
  ('d1000000-0000-0000-0000-000000000003', 'ALPHA-003', 'b1000000-0000-0000-0000-000000000001', 'c1000000-0000-0000-0000-000000000001', 'Set up Flyway database migrations', 'Initial schema design and migration scripts', 'TASK', 'HIGH', 'DONE', 'a1000000-0000-0000-0000-000000000004', 'a1000000-0000-0000-0000-000000000002', 4.00, 4.00, '2026-01-14'),
  ('d1000000-0000-0000-0000-000000000004', 'ALPHA-004', 'b1000000-0000-0000-0000-000000000001', 'c1000000-0000-0000-0000-000000000002', 'Implement JWT security with Keycloak', 'Configure Spring Security oauth2 resource server', 'STORY', 'CRITICAL', 'IN_PROGRESS', 'a1000000-0000-0000-0000-000000000003', 'a1000000-0000-0000-0000-000000000002', 12.00, 6.00, '2026-01-25'),
  ('d1000000-0000-0000-0000-000000000005', 'ALPHA-005', 'b1000000-0000-0000-0000-000000000001', 'c1000000-0000-0000-0000-000000000002', 'User management CRUD APIs', 'REST endpoints for user create/read/update/delete', 'TASK', 'HIGH', 'TODO', 'a1000000-0000-0000-0000-000000000004', 'a1000000-0000-0000-0000-000000000002', 8.00, NULL, '2026-01-28'),
  ('d1000000-0000-0000-0000-000000000006', 'ALPHA-006', 'b1000000-0000-0000-0000-000000000001', 'c1000000-0000-0000-0000-000000000002', 'Project management CRUD APIs', 'REST endpoints for project lifecycle management', 'TASK', 'HIGH', 'TODO', 'a1000000-0000-0000-0000-000000000003', 'a1000000-0000-0000-0000-000000000002', 10.00, NULL, '2026-01-30'),
  ('d1000000-0000-0000-0000-000000000007', 'ALPHA-007', 'b1000000-0000-0000-0000-000000000001', 'c1000000-0000-0000-0000-000000000002', 'Write unit tests for user service', 'JUnit 5 + Mockito tests for UserService', 'TASK', 'MEDIUM', 'BACKLOG', 'a1000000-0000-0000-0000-000000000005', 'a1000000-0000-0000-0000-000000000002', 6.00, NULL, '2026-02-01'),
  ('d1000000-0000-0000-0000-000000000008', 'ALPHA-008', 'b1000000-0000-0000-0000-000000000001', 'c1000000-0000-0000-0000-000000000003', 'Dashboard summary API', 'Aggregate statistics endpoint for frontend dashboard', 'STORY', 'MEDIUM', 'BACKLOG', 'a1000000-0000-0000-0000-000000000003', 'a1000000-0000-0000-0000-000000000002', 8.00, NULL, '2026-02-10'),
  ('d1000000-0000-0000-0000-000000000009', 'BETA-001', 'b1000000-0000-0000-0000-000000000002', NULL, 'Requirements gathering workshop', 'Facilitate requirements workshop with Acme Corp stakeholders', 'EPIC', 'HIGH', 'IN_PROGRESS', 'a1000000-0000-0000-0000-000000000002', 'a1000000-0000-0000-0000-000000000001', 16.00, 8.00, '2026-03-15'),
  ('d1000000-0000-0000-0000-000000000010', 'BETA-002', 'b1000000-0000-0000-0000-000000000002', NULL, 'Technical architecture design', 'Design system architecture for e-commerce portal', 'TASK', 'HIGH', 'BACKLOG', 'a1000000-0000-0000-0000-000000000003', 'a1000000-0000-0000-0000-000000000002', 20.00, NULL, '2026-03-20');

-- Bugs (5)
INSERT INTO bugs (id, bug_no, project_id, title, description, severity, priority, status, assignee_id, reporter_id) VALUES
  ('e1000000-0000-0000-0000-000000000001', 'BUG-ALPHA-001', 'b1000000-0000-0000-0000-000000000001', 'JWT token not refreshing properly', 'Access token expires but refresh token call returns 401', 'HIGH', 'HIGH', 'OPEN', 'a1000000-0000-0000-0000-000000000003', 'a1000000-0000-0000-0000-000000000005'),
  ('e1000000-0000-0000-0000-000000000002', 'BUG-ALPHA-002', 'b1000000-0000-0000-0000-000000000001', 'Pagination returning incorrect total count', 'Total elements count includes soft-deleted records', 'MEDIUM', 'MEDIUM', 'OPEN', 'a1000000-0000-0000-0000-000000000004', 'a1000000-0000-0000-0000-000000000005'),
  ('e1000000-0000-0000-0000-000000000003', 'BUG-ALPHA-003', 'b1000000-0000-0000-0000-000000000001', 'CORS headers missing on OPTIONS preflight', 'Frontend gets CORS error on preflight for PUT requests', 'HIGH', 'CRITICAL', 'OPEN', 'a1000000-0000-0000-0000-000000000003', 'a1000000-0000-0000-0000-000000000002'),
  ('e1000000-0000-0000-0000-000000000004', 'BUG-ALPHA-004', 'b1000000-0000-0000-0000-000000000001', 'Date fields serialized with timezone offset', 'LocalDate fields including unnecessary offset in JSON output', 'LOW', 'LOW', 'OPEN', 'a1000000-0000-0000-0000-000000000004', 'a1000000-0000-0000-0000-000000000003'),
  ('e1000000-0000-0000-0000-000000000005', 'BUG-BETA-001', 'b1000000-0000-0000-0000-000000000002', 'Requirements import fails for large CSV files', 'CSV import endpoint throws OOM for files over 10MB', 'CRITICAL', 'HIGH', 'OPEN', 'a1000000-0000-0000-0000-000000000004', 'a1000000-0000-0000-0000-000000000005');

-- Requirements (3)
INSERT INTO requirements (id, req_no, project_id, title, description, priority, status) VALUES
  ('f1000000-0000-0000-0000-000000000001', 'REQ-ALPHA-001', 'b1000000-0000-0000-0000-000000000001', 'Role-based access control', 'System must support ADMIN, PM, LEADER, DEVELOPER, TESTER, VIEWER roles with appropriate permissions', 'HIGH', 'APPROVED'),
  ('f1000000-0000-0000-0000-000000000002', 'REQ-ALPHA-002', 'b1000000-0000-0000-0000-000000000001', 'Audit trail for all entity changes', 'All create/update/delete operations must be logged with user and timestamp', 'MEDIUM', 'DRAFT'),
  ('f1000000-0000-0000-0000-000000000003', 'REQ-BETA-001', 'b1000000-0000-0000-0000-000000000002', 'Multi-currency support', 'E-commerce portal must support USD, EUR, GBP, THB currencies', 'HIGH', 'DRAFT');
