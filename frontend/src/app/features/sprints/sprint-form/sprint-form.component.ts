import { Component, OnInit, inject, signal } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
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
import { MatIconModule } from '@angular/material/icon';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

import { SprintService } from '../../../core/services/sprint.service';
import { ProjectService } from '../../../core/services/project.service';
import { SprintStatus } from '../../../core/models/sprint.model';
import { Project } from '../../../core/models/project.model';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';

function endAfterStartValidator(): ValidatorFn {
  return (group: AbstractControl): ValidationErrors | null => {
    const start = group.get('startDate')?.value;
    const end = group.get('endDate')?.value;
    if (start && end && new Date(end) <= new Date(start)) {
      return { endBeforeStart: true };
    }
    return null;
  };
}

@Component({
  selector: 'app-sprint-form',
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
    MatIconModule,
    TranslateModule,
    PageHeaderComponent
  ],
  templateUrl: './sprint-form.component.html'
})
export class SprintFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly sprintService = inject(SprintService);
  private readonly projectService = inject(ProjectService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly snackBar = inject(MatSnackBar);
  private readonly translate = inject(TranslateService);

  readonly isLoading = signal(false);
  readonly isEditMode = signal(false);
  readonly projects = signal<Project[]>([]);
  private sprintId: string | null = null;

  readonly statuses: SprintStatus[] = ['PLANNED', 'ACTIVE', 'COMPLETED', 'CANCELLED'];

  readonly form = this.fb.group(
    {
      projectId: ['', Validators.required],
      name: ['', [Validators.required, Validators.maxLength(100)]],
      startDate: [null as Date | null],
      endDate: [null as Date | null],
      goal: [''],
      status: ['PLANNED' as SprintStatus, Validators.required]
    },
    { validators: endAfterStartValidator() }
  );

  ngOnInit(): void {
    this.loadProjects();
    this.sprintId = this.route.snapshot.paramMap.get('id');
    if (this.sprintId) {
      this.isEditMode.set(true);
      this.loadSprint(this.sprintId);
    }
  }

  private loadProjects(): void {
    this.projectService.getAll(0, 100).subscribe({
      next: (resp) => this.projects.set(resp.content),
      error: () => this.showError('PROJECT.LOAD_ERROR')
    });
  }

  private loadSprint(id: string): void {
    this.isLoading.set(true);
    this.sprintService.getById(id).subscribe({
      next: (sprint) => {
        this.form.patchValue({
          projectId: sprint.projectId,
          name: sprint.name,
          startDate: sprint.startDate ? new Date(sprint.startDate) : null,
          endDate: sprint.endDate ? new Date(sprint.endDate) : null,
          goal: sprint.goal ?? '',
          status: sprint.status
        });
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
        this.showError('SPRINT.LOAD_ERROR');
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
      projectId: raw.projectId!,
      name: raw.name!,
      startDate: raw.startDate ? this.formatDate(raw.startDate) : undefined,
      endDate: raw.endDate ? this.formatDate(raw.endDate) : undefined,
      goal: raw.goal || undefined,
      status: raw.status!
    };

    this.isLoading.set(true);
    const request$ = this.isEditMode()
      ? this.sprintService.update(this.sprintId!, payload)
      : this.sprintService.create(payload);

    request$.subscribe({
      next: (sprint) => {
        this.isLoading.set(false);
        const key = this.isEditMode() ? 'SPRINT.UPDATE_SUCCESS' : 'SPRINT.CREATE_SUCCESS';
        this.showSuccess(key);
        this.router.navigate(['/sprints', sprint.id]);
      },
      error: () => {
        this.isLoading.set(false);
        const key = this.isEditMode() ? 'SPRINT.UPDATE_ERROR' : 'SPRINT.CREATE_ERROR';
        this.showError(key);
      }
    });
  }

  onCancel(): void {
    this.router.navigate(['/sprints']);
  }

  private formatDate(date: Date): string {
    return date.toISOString().split('T')[0];
  }

  private showSuccess(key: string): void {
    this.translate.get(key).subscribe((msg) => {
      this.snackBar.open(msg, '', { duration: 3000, panelClass: ['snack-success'] });
    });
  }

  private showError(key: string): void {
    this.translate.get(key).subscribe((msg) => {
      this.snackBar.open(msg, '', { duration: 4000, panelClass: ['snack-error'] });
    });
  }
}
