import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDividerModule } from '@angular/material/divider';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

import { TaskService } from '../../../core/services/task.service';
import { WorklogService } from '../../../core/services/worklog.service';
import { Task } from '../../../core/models/task.model';
import { Worklog } from '../../../core/models/worklog.model';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-task-detail',
  standalone: true,
  imports: [
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatTableModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatDialogModule,
    MatTooltipModule,
    MatDividerModule,
    TranslateModule,
    StatusBadgeComponent,
    PageHeaderComponent
  ],
  templateUrl: './task-detail.component.html'
})
export class TaskDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly taskService = inject(TaskService);
  private readonly worklogService = inject(WorklogService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  private readonly translate = inject(TranslateService);

  readonly task = signal<Task | null>(null);
  readonly worklogs = signal<Worklog[]>([]);
  readonly isLoading = signal(false);

  readonly worklogColumns = ['workDate', 'userName', 'hours', 'description', 'actions'];

  private taskId = '';

  ngOnInit(): void {
    this.taskId = this.route.snapshot.paramMap.get('id') ?? '';
    this.loadTask();
    this.loadWorklogs();
  }

  private loadTask(): void {
    this.isLoading.set(true);
    this.taskService.getById(this.taskId).subscribe({
      next: (t) => {
        this.task.set(t);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
        this.showError('TASK.LOAD_ERROR');
      }
    });
  }

  private loadWorklogs(): void {
    this.worklogService.getByTask(this.taskId).subscribe({
      next: (logs) => this.worklogs.set(logs),
      error: () => this.showError('WORKLOG.LOAD_ERROR')
    });
  }

  navigateToEdit(): void {
    this.router.navigate(['/tasks', this.taskId, 'edit']);
  }

  navigateToNewWorklog(): void {
    this.router.navigate(['/worklogs/new'], { queryParams: { taskId: this.taskId } });
  }

  confirmDeleteTask(): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '400px',
      data: { title: 'COMMON.CONFIRM_DELETE', message: 'COMMON.CONFIRM_DELETE_MSG' }
    });
    dialogRef.afterClosed().subscribe((confirmed: boolean) => {
      if (confirmed) {
        this.taskService.delete(this.taskId).subscribe({
          next: () => {
            this.showSuccess('TASK.DELETE_SUCCESS');
            this.router.navigate(['/tasks']);
          },
          error: () => this.showError('TASK.DELETE_ERROR')
        });
      }
    });
  }

  confirmDeleteWorklog(id: string): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '400px',
      data: { title: 'COMMON.CONFIRM_DELETE', message: 'COMMON.CONFIRM_DELETE_MSG' }
    });
    dialogRef.afterClosed().subscribe((confirmed: boolean) => {
      if (confirmed) {
        this.worklogService.delete(id).subscribe({
          next: () => {
            this.showSuccess('WORKLOG.DELETE_SUCCESS');
            this.loadWorklogs();
          },
          error: () => this.showError('WORKLOG.DELETE_ERROR')
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
