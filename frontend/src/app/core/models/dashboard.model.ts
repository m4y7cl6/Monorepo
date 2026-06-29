export interface DashboardSummaryDto {
  totalProjects: number;
  activeProjects: number;
  totalTasks: number;
  backlogTasks: number;
  openTasks: number;
  inProgressTasks: number;
  reviewTasks: number;
  testingTasks: number;
  doneTasks: number;
  totalBugs: number;
  openBugs: number;
  criticalBugs: number;
  totalRequirements: number;
  approvedRequirements: number;
  totalWorklogs: number;
  totalHoursLogged: number;
}
