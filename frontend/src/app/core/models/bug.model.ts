export type BugSeverity = 'TRIVIAL' | 'MINOR' | 'MAJOR' | 'CRITICAL' | 'BLOCKER';

export type BugPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';

export type BugStatus =
  | 'NEW'
  | 'CONFIRMED'
  | 'IN_PROGRESS'
  | 'FIXED'
  | 'VERIFIED'
  | 'CLOSED'
  | 'REOPENED'
  | 'WONT_FIX';

export interface Bug {
  id: string;
  bugNo: string;
  projectId: string;
  projectName?: string;
  title: string;
  description?: string;
  severity: BugSeverity;
  priority: BugPriority;
  status: BugStatus;
  assigneeId?: string;
  assigneeName?: string;
  reporterId?: string;
  reporterName?: string;
  dueDate?: string;
  createdAt: string;
  updatedAt: string;
}

export interface BugCreateRequest {
  bugNo: string;
  projectId: string;
  title: string;
  description?: string;
  severity: BugSeverity;
  priority: BugPriority;
  status: BugStatus;
  assigneeId?: string;
  dueDate?: string;
}

export interface BugUpdateRequest extends Partial<BugCreateRequest> {}
