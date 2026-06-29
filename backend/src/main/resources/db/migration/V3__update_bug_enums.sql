-- Remap BugSeverity: old backend values → frontend/domain values
UPDATE bugs SET severity = 'TRIVIAL'  WHERE severity = 'LOW';
UPDATE bugs SET severity = 'MINOR'    WHERE severity = 'MEDIUM';
UPDATE bugs SET severity = 'MAJOR'    WHERE severity = 'HIGH';
-- CRITICAL stays as CRITICAL

-- Remap BugStatus: old backend values → frontend/domain values
UPDATE bugs SET status = 'NEW'      WHERE status = 'OPEN';
UPDATE bugs SET status = 'FIXED'    WHERE status = 'RESOLVED';
UPDATE bugs SET status = 'WONT_FIX' WHERE status = 'REJECTED';
-- IN_PROGRESS and CLOSED stay the same
