import { Routes } from '@angular/router';

export const WORKLOG_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./worklog-list/worklog-list.component').then((m) => m.WorklogListComponent)
  },
  {
    path: 'new',
    loadComponent: () =>
      import('./worklog-form/worklog-form.component').then((m) => m.WorklogFormComponent)
  },
  {
    path: ':id/edit',
    loadComponent: () =>
      import('./worklog-form/worklog-form.component').then((m) => m.WorklogFormComponent)
  }
];
