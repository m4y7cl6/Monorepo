import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

import { DashboardService } from '../../core/services/dashboard.service';
import { TaskService } from '../../core/services/task.service';
import { DashboardSummaryDto } from '../../core/models/dashboard.model';
import { Task } from '../../core/models/task.model';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header.component';
import { StatusBadgeComponent } from '../../shared/components/status-badge/status-badge.component';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    MatCardModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatTableModule,
    MatSnackBarModule,
    TranslateModule,
    PageHeaderComponent,
    StatusBadgeComponent
  ],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  private readonly dashboardService = inject(DashboardService);
  private readonly taskService = inject(TaskService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly translate = inject(TranslateService);

  readonly isLoading = signal(false);
  readonly summary = signal<DashboardSummaryDto | null>(null);
  readonly recentTasks = signal<Task[]>([]);
  readonly recentTasksSource = new MatTableDataSource<Task>();

  readonly recentColumns = ['taskNo', 'title', 'status', 'priority', 'assigneeName'];

  readonly taskStatusChartData = computed(() => {
    const s = this.summary();
    if (!s) return [];
    const total = s.totalTasks || 1;
    return [
      { label: 'STATUS.DONE',        color: '#4caf50', value: s.doneTasks,       pct: Math.round((s.doneTasks / total) * 100) },
      { label: 'STATUS.IN_PROGRESS', color: '#ff9800', value: s.inProgressTasks, pct: Math.round((s.inProgressTasks / total) * 100) },
      { label: 'STATUS.REVIEW',      color: '#9c27b0', value: s.reviewTasks,     pct: Math.round((s.reviewTasks / total) * 100) },
      { label: 'STATUS.TESTING',     color: '#00bcd4', value: s.testingTasks,    pct: Math.round((s.testingTasks / total) * 100) },
      { label: 'STATUS.TODO',        color: '#2196f3', value: s.openTasks,       pct: Math.round((s.openTasks / total) * 100) },
      { label: 'STATUS.BACKLOG',     color: '#9e9e9e', value: s.backlogTasks,    pct: Math.round((s.backlogTasks / total) * 100) },
    ];
  });

  readonly taskDonutGradient = computed(() => {
    const s = this.summary();
    if (!s) return 'conic-gradient(#e0e0e0 0% 100%)';
    const total = s.totalTasks || 1;
    const segments: string[] = [];
    let pos = 0;
    const slices = [
      { color: '#4caf50', count: s.doneTasks },
      { color: '#ff9800', count: s.inProgressTasks },
      { color: '#9c27b0', count: s.reviewTasks },
      { color: '#00bcd4', count: s.testingTasks },
      { color: '#2196f3', count: s.openTasks },
      { color: '#9e9e9e', count: s.backlogTasks },
    ];
    for (const slice of slices) {
      const pct = (slice.count / total) * 100;
      if (pct > 0) {
        segments.push(`${slice.color} ${pos}% ${pos + pct}%`);
        pos += pct;
      }
    }
    return segments.length ? `conic-gradient(${segments.join(', ')})` : 'conic-gradient(#e0e0e0 0% 100%)';
  });

  readonly projectBarData = computed(() => {
    const s = this.summary();
    if (!s) return [];
    const total = s.totalProjects || 1;
    return [
      { label: 'STATUS.PLANNING', color: '#9c27b0', value: 0, pct: 0 },
      { label: 'STATUS.DEVELOPMENT', color: '#2196f3', value: s.activeProjects, pct: Math.round((s.activeProjects / total) * 100) },
      { label: 'STATUS.CLOSED', color: '#607d8b', value: s.totalProjects - s.activeProjects, pct: Math.round(((s.totalProjects - s.activeProjects) / total) * 100) }
    ];
  });

  readonly bugPieGradient = computed(() => {
    const s = this.summary();
    if (!s) return 'conic-gradient(#e0e0e0 0% 100%)';
    const total = s.totalBugs || 1;
    const openPct = (s.openBugs / total) * 100;
    const criticalPct = (s.criticalBugs / total) * 100;
    const closedPct = Math.max(0, 100 - openPct - criticalPct);
    let pos = 0;
    const segments: string[] = [];
    if (criticalPct > 0) {
      segments.push(`#f44336 ${pos}% ${pos + criticalPct}%`);
      pos += criticalPct;
    }
    if (openPct > 0) {
      segments.push(`#ff9800 ${pos}% ${pos + openPct}%`);
      pos += openPct;
    }
    if (closedPct > 0) {
      segments.push(`#4caf50 ${pos}% 100%`);
    }
    return segments.length ? `conic-gradient(${segments.join(', ')})` : 'conic-gradient(#e0e0e0 0% 100%)';
  });

  ngOnInit(): void {
    this.loadSummary();
    this.loadRecentTasks();
  }

  private loadSummary(): void {
    this.isLoading.set(true);
    this.dashboardService.getSummary().subscribe({
      next: (data) => {
        this.summary.set(data);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
        this.translate.get('COMMON.LOADING').subscribe((msg) => {
          this.snackBar.open(msg, '', { duration: 4000, panelClass: ['snack-error'] });
        });
      }
    });
  }

  private loadRecentTasks(): void {
    this.taskService.getAll(0, 5).subscribe({
      next: (resp) => {
        this.recentTasks.set(resp.content);
        this.recentTasksSource.data = resp.content;
      }
    });
  }
}
