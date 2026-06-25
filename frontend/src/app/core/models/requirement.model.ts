export type RequirementStatus =
  | 'DRAFT'
  | 'REVIEWING'
  | 'APPROVED'
  | 'REJECTED'
  | 'IMPLEMENTED';

export type RequirementPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';

export interface Requirement {
  id: string;
  reqNo: string;
  projectId: string;
  projectName?: string;
  title: string;
  description?: string;
  priority: RequirementPriority;
  status: RequirementStatus;
  createdAt: string;
  updatedAt: string;
}

export interface RequirementCreateRequest {
  reqNo: string;
  projectId: string;
  title: string;
  description?: string;
  priority: RequirementPriority;
  status: RequirementStatus;
}

export interface RequirementUpdateRequest extends Partial<RequirementCreateRequest> {}
