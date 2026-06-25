import { Routes } from '@angular/router';
import { AuthGuard } from './core/auth/auth.guard';
import { MainLayoutComponent } from './layout/main-layout/main-layout.component';

export const routes: Routes = [
  {
    path: '',
    component: MainLayoutComponent,
    canActivate: [AuthGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      {
        path: 'dashboard',
        loadChildren: () =>
          import('./features/dashboard/dashboard.routes').then((m) => m.DASHBOARD_ROUTES)
      },
      {
        path: 'kanban',
        loadChildren: () =>
          import('./features/kanban/kanban.routes').then((m) => m.KANBAN_ROUTES)
      },
      {
        path: 'sprints',
        loadChildren: () =>
          import('./features/sprints/sprints.routes').then((m) => m.SPRINT_ROUTES)
      },
      {
        path: 'projects',
        loadChildren: () =>
          import('./features/projects/projects.routes').then((m) => m.PROJECT_ROUTES)
      },
      {
        path: 'tasks',
        loadChildren: () =>
          import('./features/tasks/tasks.routes').then((m) => m.TASK_ROUTES)
      },
      {
        path: 'bugs',
        loadChildren: () =>
          import('./features/bugs/bugs.routes').then((m) => m.BUG_ROUTES)
      },
      {
        path: 'requirements',
        loadChildren: () =>
          import('./features/requirements/requirements.routes').then(
            (m) => m.REQUIREMENT_ROUTES
          )
      },
      {
        path: 'worklogs',
        loadChildren: () =>
          import('./features/worklogs/worklogs.routes').then((m) => m.WORKLOG_ROUTES)
      }
    ]
  }
];
