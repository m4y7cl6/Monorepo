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

import { TaskService } from '../../../core/services/task.service';
import { ProjectService } from '../../../core/services/project.service';
import { SprintService } from '../../../core/services/sprint.service';
import { UserService } from '../../../core/services/user.service';
import { TaskType, TaskPriority, TaskStatus } from '../../../core/models/task.model';
import { Project } from '../../../core/models/project.model';
import { Sprint } from '../../../core/models/sprint.model';
import { User } from '../../../core/models/user.model';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';

@Component({
  selector: 'app-task-form',
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
  templateUrl: './task-form.component.html'
})
export class TaskFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly taskService = inject(TaskService);
  private readonly projectService = inject(ProjectService);
  private readonly sprintService = inject(SprintService);
  private readonly userService = inject(UserService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly snackBar = inject(MatSnackBar);
  private readonly translate = inject(TranslateService);

  readonly isLoading = signal(false);
  readonly isEditMode = signal(false);
  private taskId: string | null = null;

  readonly projects = signal<Project[]>([]);
  readonly sprints = signal<Sprint[]>([]);
  readonly users = signal<User[]>([]);

  readonly taskTypes: TaskType[] = ['FEATURE', 'IMPROVEMENT', 'TECHNICAL_DEBT', 'RESEARCH'];
  readonly priorities: TaskPriority[] = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];
  readonly statuses: TaskStatus[] = ['BACKLOG', 'TODO', 'IN_PROGRESS', 'IN_REVIEW', 'DONE', 'CANCELLED'];

  readonly form = this.fb.group({
    taskNo: ['', Validators.required],
    projectId: ['', Validators.required],
    sprintId: [''],
    title: ['', Validators.required],
    description: [''],
    taskType: ['FEATURE' as TaskType, Validators.required],
    priority: ['MEDIUM' as TaskPriority, Validators.required],
    status: ['BACKLOG' as TaskStatus, Validators.required],
    assigneeId: [''],
    estimateHours: [null as number | null],
    dueDate: [null as Date | null]
  });

  ngOnInit(): void {
    this.loadProjects();
    this.loadUsers();
    this.taskId = this.route.snapshot.paramMap.get('id');
    if (this.taskId) {
      this.isEditMode.set(true);
      this.loadTask(this.taskId);
    }

    this.form.controls.projectId.valueChanges.subscribe((projectId) => {
      if (projectId) {
        this.loadSprints(projectId);
      } else {
        this.sprints.set([]);
      }
    });
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

  private loadSprints(projectId: string): void {
    this.sprintService.getByProject(projectId).subscribe({
      next: (sprints) => this.sprints.set(sprints)
    });
  }

  private loadTask(id: string): void {
    this.isLoading.set(true);
    this.taskService.getById(id).subscribe({
      next: (task) => {
        this.form.patchValue({
          taskNo: task.taskNo,
          projectId: task.projectId,
          sprintId: task.sprintId ?? '',
          title: task.title,
          description: task.description ?? '',
          taskType: task.taskType,
          priority: task.priority,
          status: task.status,
          assigneeId: task.assigneeId ?? '',
          estimateHours: task.estimateHours ?? null,
          dueDate: task.dueDate ? new Date(task.dueDate) : null
        });
        if (task.projectId) {
          this.loadSprints(task.projectId);
        }
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
        this.showError('TASK.LOAD_ERROR');
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
      taskNo: raw.taskNo!,
      projectId: raw.projectId!,
      sprintId: raw.sprintId || undefined,
      title: raw.title!,
      description: raw.description || undefined,
      taskType: raw.taskType!,
      priority: raw.priority!,
      status: raw.status!,
      assigneeId: raw.assigneeId || undefined,
      estimateHours: raw.estimateHours ?? undefined,
      dueDate: raw.dueDate ? this.formatDate(raw.dueDate) : undefined
    };

    this.isLoading.set(true);
    const req$ = this.isEditMode()
      ? this.taskService.update(this.taskId!, payload)
      : this.taskService.create(payload);

    req$.subscribe({
      next: (task) => {
        this.isLoading.set(false);
        const key = this.isEditMode() ? 'TASK.UPDATE_SUCCESS' : 'TASK.CREATE_SUCCESS';
        this.showSuccess(key);
        this.router.navigate(['/tasks', task.id]);
      },
      error: () => {
        this.isLoading.set(false);
        const key = this.isEditMode() ? 'TASK.UPDATE_ERROR' : 'TASK.CREATE_ERROR';
        this.showError(key);
      }
    });
  }

  onCancel(): void {
    this.router.navigate(['/tasks']);
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
