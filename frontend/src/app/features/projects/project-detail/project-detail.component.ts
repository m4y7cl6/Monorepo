import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTableModule } from '@angular/material/table';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatTooltipModule } from '@angular/material/tooltip';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

import { ProjectService } from '../../../core/services/project.service';
import { TaskService } from '../../../core/services/task.service';
import { BugService } from '../../../core/services/bug.service';
import { RequirementService } from '../../../core/services/requirement.service';
import { Project } from '../../../core/models/project.model';
import { Task } from '../../../core/models/task.model';
import { Bug } from '../../../core/models/bug.model';
import { Requirement } from '../../../core/models/requirement.model';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-project-detail',
  standalone: true,
  imports: [
    RouterLink,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatTabsModule,
    MatTableModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatDialogModule,
    MatTooltipModule,
    TranslateModule,
    StatusBadgeComponent,
    PageHeaderComponent
  ],
  templateUrl: './project-detail.component.html'
})
export class ProjectDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly projectService = inject(ProjectService);
  private readonly taskService = inject(TaskService);
  private readonly bugService = inject(BugService);
  private readonly requirementService = inject(RequirementService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  private readonly translate = inject(TranslateService);

  readonly project = signal<Project | null>(null);
  readonly tasks = signal<Task[]>([]);
  readonly bugs = signal<Bug[]>([]);
  readonly requirements = signal<Requirement[]>([]);
  readonly isLoading = signal(false);

  readonly taskColumns = ['taskNo', 'title', 'priority', 'status', 'assigneeName', 'actions'];
  readonly bugColumns = ['bugNo', 'title', 'severity', 'status', 'assigneeName', 'actions'];
  readonly reqColumns = ['reqNo', 'title', 'priority', 'status', 'actions'];

  private projectId = '';

  ngOnInit(): void {
    this.projectId = this.route.snapshot.paramMap.get('id') ?? '';
    this.loadProject();
    this.loadTasks();
    this.loadBugs();
    this.loadRequirements();
  }

  private loadProject(): void {
    this.isLoading.set(true);
    this.projectService.getById(this.projectId).subscribe({
      next: (p) => {
        this.project.set(p);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
        this.showError('PROJECT.LOAD_ERROR');
      }
    });
  }

  private loadTasks(): void {
    this.taskService.getByProject(this.projectId).subscribe({
      next: (resp) => this.tasks.set(resp.content),
      error: () => this.showError('TASK.LOAD_ERROR')
    });
  }

  private loadBugs(): void {
    this.bugService.getByProject(this.projectId).subscribe({
      next: (resp) => this.bugs.set(resp.content),
      error: () => this.showError('BUG.LOAD_ERROR')
    });
  }

  private loadRequirements(): void {
    this.requirementService.getByProject(this.projectId).subscribe({
      next: (resp) => this.requirements.set(resp.content),
      error: () => this.showError('REQUIREMENT.LOAD_ERROR')
    });
  }

  navigateToEdit(): void {
    this.router.navigate(['/projects', this.projectId, 'edit']);
  }

  navigateToTask(id: string): void {
    this.router.navigate(['/tasks', id]);
  }

  navigateToBug(id: string): void {
    this.router.navigate(['/bugs', id]);
  }

  confirmDeleteProject(): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '400px',
      data: { title: 'COMMON.CONFIRM_DELETE', message: 'COMMON.CONFIRM_DELETE_MSG' }
    });
    dialogRef.afterClosed().subscribe((confirmed: boolean) => {
      if (confirmed) {
        this.projectService.delete(this.projectId).subscribe({
          next: () => {
            this.showSuccess('PROJECT.DELETE_SUCCESS');
            this.router.navigate(['/projects']);
          },
          error: () => this.showError('PROJECT.DELETE_ERROR')
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
