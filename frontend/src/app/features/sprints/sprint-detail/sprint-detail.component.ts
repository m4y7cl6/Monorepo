import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatChipsModule } from '@angular/material/chips';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

import { SprintService } from '../../../core/services/sprint.service';
import { TaskService } from '../../../core/services/task.service';
import { Sprint } from '../../../core/models/sprint.model';
import { Task, TaskStatus } from '../../../core/models/task.model';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';

@Component({
  selector: 'app-sprint-detail',
  standalone: true,
  imports: [
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatTableModule,
    MatProgressBarModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatChipsModule,
    TranslateModule,
    StatusBadgeComponent,
    PageHeaderComponent
  ],
  templateUrl: './sprint-detail.component.html',
  styleUrls: ['./sprint-detail.component.scss']
})
export class SprintDetailComponent implements OnInit {
  private readonly sprintService = inject(SprintService);
  private readonly taskService = inject(TaskService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);
  private readonly translate = inject(TranslateService);

  readonly isLoading = signal(false);
  readonly sprint = signal<Sprint | null>(null);
  readonly tasks = signal<Task[]>([]);
  readonly taskDataSource = new MatTableDataSource<Task>();

  readonly displayedColumns = ['taskNo', 'title', 'status', 'priority', 'assigneeName', 'estimateHours', 'actualHours'];

  readonly taskStatusCounts = computed(() => {
    const tasks = this.tasks();
    const statuses: TaskStatus[] = ['BACKLOG', 'TODO', 'IN_PROGRESS', 'IN_REVIEW', 'DONE', 'CANCELLED'];
    return statuses.map((s) => ({
      status: s,
      count: tasks.filter((t) => t.status === s).length
    }));
  });

  readonly completionPct = computed(() => {
    const tasks = this.tasks();
    if (!tasks.length) return 0;
    const done = tasks.filter((t) => t.status === 'DONE').length;
    return Math.round((done / tasks.length) * 100);
  });

  readonly totalEstimated = computed(() =>
    this.tasks().reduce((sum, t) => sum + (t.estimateHours ?? 0), 0)
  );

  readonly totalActual = computed(() =>
    this.tasks().reduce((sum, t) => sum + (t.actualHours ?? 0), 0)
  );

  readonly velocityPct = computed(() => {
    const est = this.totalEstimated();
    if (!est) return 0;
    return Math.min(100, Math.round((this.totalActual() / est) * 100));
  });

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadSprint(id);
      this.loadTasks(id);
    }
  }

  private loadSprint(id: string): void {
    this.isLoading.set(true);
    this.sprintService.getById(id).subscribe({
      next: (sprint) => {
        this.sprint.set(sprint);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
        this.showError('SPRINT.LOAD_ERROR');
      }
    });
  }

  private loadTasks(sprintId: string): void {
    this.taskService.getBySprint(sprintId).subscribe({
      next: (tasks) => {
        this.tasks.set(tasks);
        this.taskDataSource.data = tasks;
      },
      error: () => this.showError('TASK.LOAD_ERROR')
    });
  }

  navigateToEdit(): void {
    const sprint = this.sprint();
    if (sprint) {
      this.router.navigate(['/sprints', sprint.id, 'edit']);
    }
  }

  private showError(key: string): void {
    this.translate.get(key).subscribe((msg) => {
      this.snackBar.open(msg, '', { duration: 4000, panelClass: ['snack-error'] });
    });
  }
}
