import { Component, OnInit, ViewChild, inject, signal } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatPaginatorModule, MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatSortModule, MatSort } from '@angular/material/sort';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatCardModule } from '@angular/material/card';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

import { TaskService } from '../../../core/services/task.service';
import { ProjectService } from '../../../core/services/project.service';
import { UserService } from '../../../core/services/user.service';
import { Task, TaskStatus } from '../../../core/models/task.model';
import { Project } from '../../../core/models/project.model';
import { User } from '../../../core/models/user.model';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-task-list',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatSelectModule,
    MatTooltipModule,
    MatDialogModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
    MatCardModule,
    TranslateModule,
    StatusBadgeComponent,
    PageHeaderComponent
  ],
  templateUrl: './task-list.component.html'
})
export class TaskListComponent implements OnInit {
  private readonly taskService = inject(TaskService);
  private readonly projectService = inject(ProjectService);
  private readonly userService = inject(UserService);
  private readonly router = inject(Router);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  private readonly translate = inject(TranslateService);

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  readonly displayedColumns = [
    'taskNo', 'title', 'projectName', 'sprintName', 'taskType',
    'priority', 'status', 'assigneeName', 'dueDate', 'actions'
  ];
  readonly dataSource = new MatTableDataSource<Task>();
  readonly isLoading = signal(false);
  readonly totalElements = signal(0);
  readonly pageSize = signal(20);
  readonly pageIndex = signal(0);

  readonly projects = signal<Project[]>([]);
  readonly users = signal<User[]>([]);

  readonly statusFilter = new FormControl<TaskStatus | ''>('');
  readonly projectFilter = new FormControl<string>('');
  readonly assigneeFilter = new FormControl<string>('');

  readonly taskStatuses: TaskStatus[] = ['BACKLOG', 'TODO', 'IN_PROGRESS', 'IN_REVIEW', 'DONE', 'CANCELLED'];

  ngOnInit(): void {
    this.loadProjects();
    this.loadUsers();
    this.loadTasks();
  }

  private loadProjects(): void {
    this.projectService.getAll(0, 100).subscribe({
      next: (resp) => this.projects.set(resp.content)
    });
  }

  private loadUsers(): void {
    this.userService.getAll().subscribe({
      next: (resp) => this.users.set(resp.content)
    });
  }

  loadTasks(): void {
    this.isLoading.set(true);
    this.taskService.getAll(this.pageIndex(), this.pageSize()).subscribe({
      next: (resp) => {
        let filtered = resp.content;
        const statusVal = this.statusFilter.value;
        const projectVal = this.projectFilter.value;
        const assigneeVal = this.assigneeFilter.value;
        if (statusVal) filtered = filtered.filter((t) => t.status === statusVal);
        if (projectVal) filtered = filtered.filter((t) => t.projectId === projectVal);
        if (assigneeVal) filtered = filtered.filter((t) => t.assigneeId === assigneeVal);
        this.dataSource.data = filtered;
        this.totalElements.set(resp.totalElements);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
        this.showError('TASK.LOAD_ERROR');
      }
    });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.loadTasks();
  }

  applyFilters(): void {
    this.pageIndex.set(0);
    this.loadTasks();
  }

  resetFilters(): void {
    this.statusFilter.setValue('');
    this.projectFilter.setValue('');
    this.assigneeFilter.setValue('');
    this.loadTasks();
  }

  navigateToCreate(): void {
    this.router.navigate(['/tasks/new']);
  }

  navigateToDetail(id: string): void {
    this.router.navigate(['/tasks', id]);
  }

  navigateToEdit(id: string, event: Event): void {
    event.stopPropagation();
    this.router.navigate(['/tasks', id, 'edit']);
  }

  confirmDelete(task: Task, event: Event): void {
    event.stopPropagation();
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '400px',
      data: { title: 'COMMON.CONFIRM_DELETE', message: 'COMMON.CONFIRM_DELETE_MSG' }
    });
    dialogRef.afterClosed().subscribe((confirmed: boolean) => {
      if (confirmed) {
        this.isLoading.set(true);
        this.taskService.delete(task.id).subscribe({
          next: () => {
            this.isLoading.set(false);
            this.showSuccess('TASK.DELETE_SUCCESS');
            this.loadTasks();
          },
          error: () => {
            this.isLoading.set(false);
            this.showError('TASK.DELETE_ERROR');
          }
        });
      }
    });
  }

  private showSuccess(key: string): void {
    this.translate.get(key).subscribe((msg) => {
      this.snackBar.open(msg, '', { duration: 3000 });
    });
  }

  private showError(key: string): void {
    this.translate.get(key).subscribe((msg) => {
      this.snackBar.open(msg, '', { duration: 4000 });
    });
  }
}
