import { Routes } from '@angular/router';

export const BUG_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./bug-list/bug-list.component').then((m) => m.BugListComponent)
  },
  {
    path: 'new',
    loadComponent: () =>
      import('./bug-form/bug-form.component').then((m) => m.BugFormComponent)
  },
  {
    path: ':id/edit',
    loadComponent: () =>
      import('./bug-form/bug-form.component').then((m) => m.BugFormComponent)
  }
];
