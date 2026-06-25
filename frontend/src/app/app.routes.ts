import { Routes } from '@angular/router';
import { AuthGuard } from './core/auth/auth.guard';
import { MainLayoutComponent } from './layout/main-layout/main-layout.component';

export const routes: Routes = [
  {
    path: '',
    component: MainLayoutComponent,
    canActivate: [AuthGuard],
    children: [
      { path: '', redirectTo: 'projects', pathMatch: 'full' },
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
