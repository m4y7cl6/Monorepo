import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

import { RequirementService } from '../../../core/services/requirement.service';
import { ProjectService } from '../../../core/services/project.service';
import { RequirementPriority, RequirementStatus } from '../../../core/models/requirement.model';
import { Project } from '../../../core/models/project.model';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';

@Component({
  selector: 'app-requirement-form',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatCardModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    TranslateModule,
    PageHeaderComponent
  ],
  templateUrl: './requirement-form.component.html'
})
export class RequirementFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly requirementService = inject(RequirementService);
  private readonly projectService = inject(ProjectService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly snackBar = inject(MatSnackBar);
  private readonly translate = inject(TranslateService);

  readonly isLoading = signal(false);
  readonly isEditMode = signal(false);
  private reqId: string | null = null;

  readonly projects = signal<Project[]>([]);

  readonly priorities: RequirementPriority[] = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];
  readonly statuses: RequirementStatus[] = ['DRAFT', 'REVIEWING', 'APPROVED', 'REJECTED', 'IMPLEMENTED'];

  readonly form = this.fb.group({
    reqNo: ['', Validators.required],
    projectId: ['', Validators.required],
    title: ['', Validators.required],
    description: [''],
    priority: ['MEDIUM' as RequirementPriority, Validators.required],
    status: ['DRAFT' as RequirementStatus, Validators.required]
  });

  ngOnInit(): void {
    this.loadProjects();
    this.reqId = this.route.snapshot.paramMap.get('id');
    if (this.reqId) {
      this.isEditMode.set(true);
      this.loadRequirement(this.reqId);
    }
  }

  private loadProjects(): void {
    this.projectService.getAll(0, 100).subscribe({
      next: (resp) => this.projects.set(resp.content)
    });
  }

  private loadRequirement(id: string): void {
    this.isLoading.set(true);
    this.requirementService.getById(id).subscribe({
      next: (req) => {
        this.form.patchValue({
          reqNo: req.reqNo,
          projectId: req.projectId,
          title: req.title,
          description: req.description ?? '',
          priority: req.priority,
          status: req.status
        });
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
        this.showError('REQUIREMENT.LOAD_ERROR');
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
      reqNo: raw.reqNo!,
      projectId: raw.projectId!,
      title: raw.title!,
      description: raw.description || undefined,
      priority: raw.priority!,
      status: raw.status!
    };

    this.isLoading.set(true);
    const req$ = this.isEditMode()
      ? this.requirementService.update(this.reqId!, payload)
      : this.requirementService.create(payload);

    req$.subscribe({
      next: () => {
        this.isLoading.set(false);
        const key = this.isEditMode() ? 'REQUIREMENT.UPDATE_SUCCESS' : 'REQUIREMENT.CREATE_SUCCESS';
        this.showSuccess(key);
        this.router.navigate(['/requirements']);
      },
      error: () => {
        this.isLoading.set(false);
        const key = this.isEditMode() ? 'REQUIREMENT.UPDATE_ERROR' : 'REQUIREMENT.CREATE_ERROR';
        this.showError(key);
      }
    });
  }

  onCancel(): void {
    this.router.navigate(['/requirements']);
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
