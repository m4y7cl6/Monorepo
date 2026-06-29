import { Component, OnInit, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
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

import { HttpErrorResponse } from '@angular/common/http';
import { SprintService } from '../../../core/services/sprint.service';
import { ProjectService } from '../../../core/services/project.service';
import { Sprint } from '../../../core/models/sprint.model';
import { Project } from '../../../core/models/project.model';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-sprint-list',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatTableModule,
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
  templateUrl: './sprint-list.component.html'
})
export class SprintListComponent implements OnInit {
  private readonly sprintService = inject(SprintService);
  private readonly projectService = inject(ProjectService);
  private readonly router = inject(Router);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  private readonly translate = inject(TranslateService);

  readonly displayedColumns = ['name', 'startDate', 'endDate', 'goal', 'status', 'actions'];
  readonly dataSource = new MatTableDataSource<Sprint>();
  readonly isLoading = signal(false);
  readonly projects = signal<Project[]>([]);
  readonly projectControl = new FormControl<string | null>(null);

  ngOnInit(): void {
    this.loadProjects();
    this.projectControl.valueChanges.subscribe((projectId) => {
      if (projectId) {
        this.loadSprintsByProject(projectId);
      } else {
        this.loadAllSprints();
      }
    });
    this.loadAllSprints();
  }

  private loadProjects(): void {
    this.projectService.getAll(0, 100).subscribe({
      next: (resp) => this.projects.set(resp.content),
      error: () => this.showError('PROJECT.LOAD_ERROR')
    });
  }

  private loadAllSprints(): void {
    this.isLoading.set(true);
    this.sprintService.getAll(0, 100).subscribe({
      next: (resp) => {
        this.dataSource.data = resp.content;
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
        this.showError('SPRINT.LOAD_ERROR');
      }
    });
  }

  private loadSprintsByProject(projectId: string): void {
    this.isLoading.set(true);
    this.sprintService.getByProject(projectId).subscribe({
      next: (sprints) => {
        this.dataSource.data = sprints;
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
        this.showError('SPRINT.LOAD_ERROR');
      }
    });
  }

  navigateToCreate(): void {
    this.router.navigate(['/sprints/new']);
  }

  navigateToDetail(id: string): void {
    this.router.navigate(['/sprints', id]);
  }

  navigateToEdit(id: string, event: Event): void {
    event.stopPropagation();
    this.router.navigate(['/sprints', id, 'edit']);
  }

  activate(sprint: Sprint, event: Event): void {
    event.stopPropagation();
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '400px',
      data: { title: 'SPRINT.ACTIVATE', message: 'SPRINT.CONFIRM_ACTIVATE' }
    });
    dialogRef.afterClosed().subscribe((confirmed: boolean) => {
      if (confirmed) {
        this.isLoading.set(true);
        this.sprintService.activate(sprint.id).subscribe({
          next: () => {
            this.isLoading.set(false);
            this.showSuccess('SPRINT.ACTIVATE_SUCCESS');
            this.reloadCurrent();
          },
          error: (err: HttpErrorResponse) => {
            this.isLoading.set(false);
            this.showBackendError(err, 'SPRINT.ACTIVATE_ERROR');
          }
        });
      }
    });
  }

  complete(sprint: Sprint, event: Event): void {
    event.stopPropagation();
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '400px',
      data: { title: 'SPRINT.COMPLETE', message: 'SPRINT.CONFIRM_COMPLETE' }
    });
    dialogRef.afterClosed().subscribe((confirmed: boolean) => {
      if (confirmed) {
        this.isLoading.set(true);
        this.sprintService.complete(sprint.id).subscribe({
          next: () => {
            this.isLoading.set(false);
            this.showSuccess('SPRINT.COMPLETE_SUCCESS');
            this.reloadCurrent();
          },
          error: (err: HttpErrorResponse) => {
            this.isLoading.set(false);
            this.showBackendError(err, 'SPRINT.COMPLETE_ERROR');
          }
        });
      }
    });
  }

  confirmDelete(sprint: Sprint, event: Event): void {
    event.stopPropagation();
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '400px',
      data: { title: 'COMMON.CONFIRM_DELETE', message: 'COMMON.CONFIRM_DELETE_MSG' }
    });
    dialogRef.afterClosed().subscribe((confirmed: boolean) => {
      if (confirmed) {
        this.isLoading.set(true);
        this.sprintService.delete(sprint.id).subscribe({
          next: () => {
            this.isLoading.set(false);
            this.showSuccess('COMMON.DELETE');
            this.reloadCurrent();
          },
          error: () => {
            this.isLoading.set(false);
            this.showError('SPRINT.LOAD_ERROR');
          }
        });
      }
    });
  }

  private reloadCurrent(): void {
    const projectId = this.projectControl.value;
    if (projectId) {
      this.loadSprintsByProject(projectId);
    } else {
      this.loadAllSprints();
    }
  }

  private showBackendError(err: HttpErrorResponse, fallbackKey: string): void {
    const errorCode = err.error?.errorCode;
    if (errorCode) {
      const i18nKey = `ERRORS.${errorCode}`;
      this.translate.get(i18nKey).subscribe((msg) => {
        // ngx-translate returns the key itself when no translation is found
        const message = msg !== i18nKey ? msg : (err.error?.detail ?? '');
        this.snackBar.open(message, '', { duration: 5000, panelClass: ['snack-error'] });
      });
    } else if (err.error?.detail) {
      this.snackBar.open(err.error.detail, '', { duration: 5000, panelClass: ['snack-error'] });
    } else {
      this.showError(fallbackKey);
    }
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
