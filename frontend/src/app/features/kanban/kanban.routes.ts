import { Routes } from '@angular/router';

export const KANBAN_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./kanban-board/kanban-board.component').then((m) => m.KanbanBoardComponent)
  }
];
