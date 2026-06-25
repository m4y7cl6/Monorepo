export interface Worklog {
  id: string;
  taskId: string;
  taskNo?: string;
  taskTitle?: string;
  userId: string;
  userName?: string;
  workDate: string;
  hours: number;
  description?: string;
  createdAt: string;
  updatedAt: string;
}

export interface WorklogCreateRequest {
  taskId: string;
  workDate: string;
  hours: number;
  description?: string;
}

export interface WorklogUpdateRequest extends Partial<WorklogCreateRequest> {}
