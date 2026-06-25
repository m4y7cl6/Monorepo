import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { CdkDragDrop, DragDropModule, moveItemInArray, transferArrayItem } from '@angular/cdk/drag-drop';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatButtonModule } from '@angular/material/button';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

import { TaskService } from '../../../core/services/task.service';
import { ProjectService } from '../../../core/services/project.service';
import { Task, TaskStatus } from '../../../core/models/task.model';
import { Project } from '../../../core/models/project.model';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';

interface KanbanColumn {
  status: TaskStatus;
  labelKey: string;
}

@Component({
  selector: 'app-kanban-board',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    DragDropModule,
    MatToolbarModule,
    MatSelectModule,
    MatFormFieldModule,
    MatInputModule,
    MatCardModule,
    MatIconModule,
    MatChipsModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatButtonModule,
    TranslateModule,
    StatusBadgeComponent,
    PageHeaderComponent
  ],
  templateUrl: './kanban-board.component.html',
  styleUrls: ['./kanban-board.component.scss']
})
export class KanbanBoardComponent implements OnInit {
  private readonly taskService = inject(TaskService);
  private readonly projectService = inject(ProjectService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly translate = inject(TranslateService);

  readonly isLoading = signal(false);
  readonly projects = signal<Project[]>([]);
  readonly tasks = signal<Task[]>([]);

  readonly selectedProjectId = signal<string | null>(null);
  readonly filterAssignee = signal('');
  readonly filterType = signal('');

  readonly projectControl = new FormControl<string | null>(null);
  readonly assigneeControl = new FormControl('');
  readonly typeControl = new FormControl('');

  readonly columns: KanbanColumn[] = [
    { status: 'BACKLOG',   labelKey: 'STATUS.BACKLOG' },
    { status: 'TODO',      labelKey: 'STATUS.TODO' },
    { status: 'IN_PROGRESS', labelKey: 'STATUS.IN_PROGRESS' },
    { status: 'IN_REVIEW', labelKey: 'STATUS.IN_REVIEW' },
    { status: 'DONE',      labelKey: 'STATUS.DONE' },
    { status: 'CANCELLED', labelKey: 'STATUS.CANCELLED' }
  ];

  readonly taskTypes = ['FEATURE', 'IMPROVEMENT', 'TECHNICAL_DEBT', 'RESEARCH'];

  readonly filteredTasks = computed(() => {
    let tasks = this.tasks();
    const assignee = this.filterAssignee();
    const type = this.filterType();
    if (assignee) {
      tasks = tasks.filter((t) => t.assigneeName?.toLowerCase().includes(assignee.toLowerCase()));
    }
    if (type) {
      tasks = tasks.filter((t) => t.taskType === type);
    }
    return tasks;
  });

  readonly tasksByStatus = computed(() => {
    const map: Record<string, Task[]> = {};
    for (const col of this.columns) {
      map[col.status] = [];
    }
    for (const task of this.filteredTasks()) {
      if (map[task.status]) {
        map[task.status].push(task);
      }
    }
    return map;
  });

  get connectedLists(): string[] {
    return this.columns.map((c) => `drop-${c.status}`);
  }

  ngOnInit(): void {
    this.loadProjects();
    this.projectControl.valueChanges.subscribe((projectId) => {
      this.selectedProjectId.set(projectId);
      if (projectId) {
        this.loadTasks(projectId);
      } else {
        this.tasks.set([]);
      }
    });
    this.assigneeControl.valueChanges.subscribe((v) => this.filterAssignee.set(v ?? ''));
    this.typeControl.valueChanges.subscribe((v) => this.filterType.set(v ?? ''));
  }

  private loadProjects(): void {
    this.projectService.getAll(0, 100).subscribe({
      next: (resp) => this.projects.set(resp.content),
      error: () => this.showError('PROJECT.LOAD_ERROR')
    });
  }

  private loadTasks(projectId: string): void {
    this.isLoading.set(true);
    this.taskService.getByProject(projectId, 0, 200).subscribe({
      next: (resp) => {
        this.tasks.set(resp.content);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
        this.showError('TASK.LOAD_ERROR');
      }
    });
  }

  onDrop(event: CdkDragDrop<Task[]>, targetStatus: TaskStatus): void {
    if (event.previousContainer === event.container) {
      moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
      return;
    }

    const task = event.previousContainer.data[event.previousIndex];
    transferArrayItem(
      event.previousContainer.data,
      event.container.data,
      event.previousIndex,
      event.currentIndex
    );

    this.taskService.updateStatus(task.id, targetStatus).subscribe({
      next: (updated) => {
        const current = this.tasks();
        const idx = current.findIndex((t) => t.id === updated.id);
        if (idx !== -1) {
          const updated_list = [...current];
          updated_list[idx] = updated;
          this.tasks.set(updated_list);
        }
        this.showSuccess('TASK.UPDATE_SUCCESS');
      },
      error: () => {
        this.showError('TASK.UPDATE_ERROR');
        const projectId = this.selectedProjectId();
        if (projectId) this.loadTasks(projectId);
      }
    });
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
