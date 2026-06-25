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
import { MatIconModule } from '@angular/material/icon';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

import { ProjectService } from '../../../core/services/project.service';
import { ProjectStatus } from '../../../core/models/project.model';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';

@Component({
  selector: 'app-project-form',
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
  templateUrl: './project-form.component.html'
})
export class ProjectFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly projectService = inject(ProjectService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly snackBar = inject(MatSnackBar);
  private readonly translate = inject(TranslateService);

  readonly isLoading = signal(false);
  readonly isEditMode = signal(false);
  private projectId: string | null = null;

  readonly statuses: ProjectStatus[] = [
    'PLANNING',
    'DEVELOPMENT',
    'TESTING',
    'UAT',
    'PRODUCTION',
    'CLOSED'
  ];

  readonly form = this.fb.group({
    code: ['', [Validators.required, Validators.maxLength(20)]],
    name: ['', [Validators.required, Validators.maxLength(100)]],
    description: [''],
    customer: [''],
    startDate: [null as Date | null],
    endDate: [null as Date | null],
    status: ['PLANNING' as ProjectStatus, Validators.required]
  });

  ngOnInit(): void {
    this.projectId = this.route.snapshot.paramMap.get('id');
    if (this.projectId) {
      this.isEditMode.set(true);
      this.loadProject(this.projectId);
    }
  }

  private loadProject(id: string): void {
    this.isLoading.set(true);
    this.projectService.getById(id).subscribe({
      next: (project) => {
        this.form.patchValue({
          code: project.code,
          name: project.name,
          description: project.description ?? '',
          customer: project.customer ?? '',
          startDate: project.startDate ? new Date(project.startDate) : null,
          endDate: project.endDate ? new Date(project.endDate) : null,
          status: project.status
        });
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
        this.showError('PROJECT.LOAD_ERROR');
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
      code: raw.code!,
      name: raw.name!,
      description: raw.description || undefined,
      customer: raw.customer || undefined,
      startDate: raw.startDate ? this.formatDate(raw.startDate) : undefined,
      endDate: raw.endDate ? this.formatDate(raw.endDate) : undefined,
      status: raw.status!
    };

    this.isLoading.set(true);
    const request$ = this.isEditMode()
      ? this.projectService.update(this.projectId!, payload)
      : this.projectService.create(payload);

    request$.subscribe({
      next: (project) => {
        this.isLoading.set(false);
        const key = this.isEditMode() ? 'PROJECT.UPDATE_SUCCESS' : 'PROJECT.CREATE_SUCCESS';
        this.showSuccess(key);
        this.router.navigate(['/projects', project.id]);
      },
      error: () => {
        this.isLoading.set(false);
        const key = this.isEditMode() ? 'PROJECT.UPDATE_ERROR' : 'PROJECT.CREATE_ERROR';
        this.showError(key);
      }
    });
  }

  onCancel(): void {
    this.router.navigate(['/projects']);
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
