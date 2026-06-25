export interface DashboardSummaryDto {
  totalProjects: number;
  activeProjects: number;
  totalTasks: number;
  openTasks: number;
  inProgressTasks: number;
  doneTasks: number;
  totalBugs: number;
  openBugs: number;
  criticalBugs: number;
  totalRequirements: number;
  approvedRequirements: number;
  totalWorklogs: number;
  totalHoursLogged: number;
}
