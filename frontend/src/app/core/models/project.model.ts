export type ProjectStatus =
  | 'PLANNING'
  | 'DEVELOPMENT'
  | 'TESTING'
  | 'UAT'
  | 'PRODUCTION'
  | 'CLOSED';

export interface Project {
  id: string;
  code: string;
  name: string;
  description?: string;
  customer?: string;
  startDate?: string;
  endDate?: string;
  status: ProjectStatus;
  createdAt: string;
  updatedAt: string;
}

export interface ProjectCreateRequest {
  code: string;
  name: string;
  description?: string;
  customer?: string;
  startDate?: string;
  endDate?: string;
  status: ProjectStatus;
}

export interface ProjectUpdateRequest extends Partial<ProjectCreateRequest> {}
