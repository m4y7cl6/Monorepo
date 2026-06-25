export type TaskType = 'FEATURE' | 'IMPROVEMENT' | 'TECHNICAL_DEBT' | 'RESEARCH';

export type TaskPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';

export type TaskStatus =
  | 'BACKLOG'
  | 'TODO'
  | 'IN_PROGRESS'
  | 'IN_REVIEW'
  | 'DONE'
  | 'CANCELLED';

export interface Task {
  id: string;
  taskNo: string;
  projectId: string;
  projectName?: string;
  sprintId?: string;
  sprintName?: string;
  title: string;
  description?: string;
  taskType: TaskType;
  priority: TaskPriority;
  status: TaskStatus;
  assigneeId?: string;
  assigneeName?: string;
  estimateHours?: number;
  actualHours?: number;
  dueDate?: string;
  createdAt: string;
  updatedAt: string;
}

export interface TaskCreateRequest {
  taskNo: string;
  projectId: string;
  sprintId?: string;
  title: string;
  description?: string;
  taskType: TaskType;
  priority: TaskPriority;
  status: TaskStatus;
  assigneeId?: string;
  estimateHours?: number;
  dueDate?: string;
}

export interface TaskUpdateRequest extends Partial<TaskCreateRequest> {}
