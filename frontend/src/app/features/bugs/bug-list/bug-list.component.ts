import { Component, OnInit, ViewChild, inject, signal } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatPaginatorModule, MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatSortModule, MatSort } from '@angular/material/sort';
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

import { BugService } from '../../../core/services/bug.service';
import { Bug, BugSeverity, BugStatus } from '../../../core/models/bug.model';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-bug-list',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
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
  templateUrl: './bug-list.component.html'
})
export class BugListComponent implements OnInit {
  private readonly bugService = inject(BugService);
  private readonly router = inject(Router);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  private readonly translate = inject(TranslateService);

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  readonly displayedColumns = ['bugNo', 'title', 'projectName', 'severity', 'priority', 'status', 'assigneeName', 'actions'];
  readonly dataSource = new MatTableDataSource<Bug>();
  readonly isLoading = signal(false);
  readonly totalElements = signal(0);
  readonly pageSize = signal(20);
  readonly pageIndex = signal(0);

  readonly severityFilter = new FormControl<BugSeverity | ''>('');
  readonly statusFilter = new FormControl<BugStatus | ''>('');

  readonly severities: BugSeverity[] = ['TRIVIAL', 'MINOR', 'MAJOR', 'CRITICAL', 'BLOCKER'];
  readonly statuses: BugStatus[] = ['NEW', 'CONFIRMED', 'IN_PROGRESS', 'FIXED', 'VERIFIED', 'CLOSED', 'REOPENED', 'WONT_FIX'];

  ngOnInit(): void {
    this.loadBugs();
  }

  loadBugs(): void {
    this.isLoading.set(true);
    this.bugService.getAll(this.pageIndex(), this.pageSize()).subscribe({
      next: (resp) => {
        let filtered = resp.content;
        const sevVal = this.severityFilter.value;
        const statVal = this.statusFilter.value;
        if (sevVal) filtered = filtered.filter((b) => b.severity === sevVal);
        if (statVal) filtered = filtered.filter((b) => b.status === statVal);
        this.dataSource.data = filtered;
        this.totalElements.set(resp.totalElements);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
        this.showError('BUG.LOAD_ERROR');
      }
    });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.loadBugs();
  }

  applyFilters(): void {
    this.pageIndex.set(0);
    this.loadBugs();
  }

  resetFilters(): void {
    this.severityFilter.setValue('');
    this.statusFilter.setValue('');
    this.loadBugs();
  }

  navigateToCreate(): void {
    this.router.navigate(['/bugs/new']);
  }

  navigateToEdit(id: string, event: Event): void {
    event.stopPropagation();
    this.router.navigate(['/bugs', id, 'edit']);
  }

  confirmDelete(bug: Bug, event: Event): void {
    event.stopPropagation();
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '400px',
      data: { title: 'COMMON.CONFIRM_DELETE', message: 'COMMON.CONFIRM_DELETE_MSG' }
    });
    dialogRef.afterClosed().subscribe((confirmed: boolean) => {
      if (confirmed) {
        this.isLoading.set(true);
        this.bugService.delete(bug.id).subscribe({
          next: () => {
            this.isLoading.set(false);
            this.showSuccess('BUG.DELETE_SUCCESS');
            this.loadBugs();
          },
          error: () => {
            this.isLoading.set(false);
            this.showError('BUG.DELETE_ERROR');
          }
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
