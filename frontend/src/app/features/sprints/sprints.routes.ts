import { Routes } from '@angular/router';

export const SPRINT_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./sprint-list/sprint-list.component').then((m) => m.SprintListComponent)
  },
  {
    path: 'new',
    loadComponent: () =>
      import('./sprint-form/sprint-form.component').then((m) => m.SprintFormComponent)
  },
  {
    path: ':id/edit',
    loadComponent: () =>
      import('./sprint-form/sprint-form.component').then((m) => m.SprintFormComponent)
  },
  {
    path: ':id',
    loadComponent: () =>
      import('./sprint-detail/sprint-detail.component').then((m) => m.SprintDetailComponent)
  }
];
