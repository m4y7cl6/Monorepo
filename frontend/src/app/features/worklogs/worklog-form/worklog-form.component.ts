import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

import { WorklogService } from '../../../core/services/worklog.service';
import { TaskService } from '../../../core/services/task.service';
import { Task } from '../../../core/models/task.model';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';

@Component({
  selector: 'app-worklog-form',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatCardModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    TranslateModule,
    PageHeaderComponent
  ],
  templateUrl: './worklog-form.component.html'
})
export class WorklogFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly worklogService = inject(WorklogService);
  private readonly taskService = inject(TaskService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly snackBar = inject(MatSnackBar);
  private readonly translate = inject(TranslateService);

  readonly isLoading = signal(false);
  readonly isEditMode = signal(false);
  private worklogId: string | null = null;

  readonly tasks = signal<Task[]>([]);

  readonly form = this.fb.group({
    taskId: ['', Validators.required],
    workDate: [null as Date | null, Validators.required],
    hours: [null as number | null, [Validators.required, Validators.min(0.5), Validators.max(24)]],
    description: ['']
  });

  ngOnInit(): void {
    this.loadTasks();
    this.worklogId = this.route.snapshot.paramMap.get('id');
    const taskIdParam = this.route.snapshot.queryParamMap.get('taskId');
    if (taskIdParam) {
      this.form.controls.taskId.setValue(taskIdParam);
    }
    if (this.worklogId) {
      this.isEditMode.set(true);
      this.loadWorklog(this.worklogId);
    }
  }

  private loadTasks(): void {
    this.taskService.getAll(0, 200).subscribe({
      next: (resp) => this.tasks.set(resp.content)
    });
  }

  private loadWorklog(id: string): void {
    this.isLoading.set(true);
    this.worklogService.getById(id).subscribe({
      next: (log) => {
        this.form.patchValue({
          taskId: log.taskId,
          workDate: new Date(log.workDate),
          hours: log.hours,
          description: log.description ?? ''
        });
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
        this.showError('WORKLOG.LOAD_ERROR');
      }
    });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const raw = this.form.getRawValue();
    const payload = {
      taskId: raw.taskId!,
      workDate: this.formatDate(raw.workDate!),
      hours: raw.hours!,
      description: raw.description || undefined
    };

    this.isLoading.set(true);
    const req$ = this.isEditMode()
      ? this.worklogService.update(this.worklogId!, payload)
      : this.worklogService.create(payload);

    req$.subscribe({
      next: () => {
        this.isLoading.set(false);
        const key = this.isEditMode() ? 'WORKLOG.UPDATE_SUCCESS' : 'WORKLOG.CREATE_SUCCESS';
        this.showSuccess(key);
        this.router.navigate(['/worklogs']);
      },
      error: () => {
        this.isLoading.set(false);
        const key = this.isEditMode() ? 'WORKLOG.UPDATE_ERROR' : 'WORKLOG.CREATE_ERROR';
        this.showError(key);
      }
    });
  }

  onCancel(): void {
    this.router.navigate(['/worklogs']);
  }

  private formatDate(date: Date): string {
    return date.toISOString().split('T')[0];
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
