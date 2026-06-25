import { Routes } from '@angular/router';

export const REQUIREMENT_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./requirement-list/requirement-list.component').then(
        (m) => m.RequirementListComponent
      )
  },
  {
    path: 'new',
    loadComponent: () =>
      import('./requirement-form/requirement-form.component').then(
        (m) => m.RequirementFormComponent
      )
  },
  {
    path: ':id/edit',
    loadComponent: () =>
      import('./requirement-form/requirement-form.component').then(
        (m) => m.RequirementFormComponent
      )
  }
];
