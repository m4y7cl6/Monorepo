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

import { BugService } from '../../../core/services/bug.service';
import { ProjectService } from '../../../core/services/project.service';
import { UserService } from '../../../core/services/user.service';
import { BugSeverity, BugPriority, BugStatus } from '../../../core/models/bug.model';
import { Project } from '../../../core/models/project.model';
import { User } from '../../../core/models/user.model';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';

@Component({
  selector: 'app-bug-form',
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
  templateUrl: './bug-form.component.html'
})
export class BugFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly bugService = inject(BugService);
  private readonly projectService = inject(ProjectService);
  private readonly userService = inject(UserService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly snackBar = inject(MatSnackBar);
  private readonly translate = inject(TranslateService);

  readonly isLoading = signal(false);
  readonly isEditMode = signal(false);
  private bugId: string | null = null;

  readonly projects = signal<Project[]>([]);
  readonly users = signal<User[]>([]);

  readonly severities: BugSeverity[] = ['TRIVIAL', 'MINOR', 'MAJOR', 'CRITICAL', 'BLOCKER'];
  readonly priorities: BugPriority[] = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];
  readonly statuses: BugStatus[] = ['NEW', 'CONFIRMED', 'IN_PROGRESS', 'FIXED', 'VERIFIED', 'CLOSED', 'REOPENED', 'WONT_FIX'];

  readonly form = this.fb.group({
    bugNo: ['', Validators.required],
    projectId: ['', Validators.required],
    title: ['', Validators.required],
    description: [''],
    severity: ['MAJOR' as BugSeverity, Validators.required],
    priority: ['HIGH' as BugPriority, Validators.required],
    status: ['NEW' as BugStatus, Validators.required],
    assigneeId: [''],
    dueDate: [null as Date | null]
  });

  ngOnInit(): void {
    this.loadProjects();
    this.loadUsers();
    this.bugId = this.route.snapshot.paramMap.get('id');
    if (this.bugId) {
      this.isEditMode.set(true);
      this.loadBug(this.bugId);
    }
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

  private loadBug(id: string): void {
    this.isLoading.set(true);
    this.bugService.getById(id).subscribe({
      next: (bug) => {
        this.form.patchValue({
          bugNo: bug.bugNo,
          projectId: bug.projectId,
          title: bug.title,
          description: bug.description ?? '',
          severity: bug.severity,
          priority: bug.priority,
          status: bug.status,
          assigneeId: bug.assigneeId ?? '',
          dueDate: bug.dueDate ? new Date(bug.dueDate) : null
        });
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
        this.showError('BUG.LOAD_ERROR');
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
      bugNo: raw.bugNo!,
      projectId: raw.projectId!,
      title: raw.title!,
      description: raw.description || undefined,
      severity: raw.severity!,
      priority: raw.priority!,
      status: raw.status!,
      assigneeId: raw.assigneeId || undefined,
      dueDate: raw.dueDate ? raw.dueDate.toISOString().split('T')[0] : undefined
    };

    this.isLoading.set(true);
    const req$ = this.isEditMode()
      ? this.bugService.update(this.bugId!, payload)
      : this.bugService.create(payload);

    req$.subscribe({
      next: () => {
        this.isLoading.set(false);
        const key = this.isEditMode() ? 'BUG.UPDATE_SUCCESS' : 'BUG.CREATE_SUCCESS';
        this.showSuccess(key);
        this.router.navigate(['/bugs']);
      },
      error: () => {
        this.isLoading.set(false);
        const key = this.isEditMode() ? 'BUG.UPDATE_ERROR' : 'BUG.CREATE_ERROR';
        this.showError(key);
      }
    });
  }

  onCancel(): void {
    this.router.navigate(['/bugs']);
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
