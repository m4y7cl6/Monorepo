import { Component, OnInit, ViewChild, inject, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatPaginatorModule, MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatSortModule, MatSort } from '@angular/material/sort';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatCardModule } from '@angular/material/card';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

import { WorklogService } from '../../../core/services/worklog.service';
import { Worklog } from '../../../core/models/worklog.model';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-worklog-list',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatTooltipModule,
    MatDialogModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
    MatCardModule,
    TranslateModule,
    PageHeaderComponent
  ],
  templateUrl: './worklog-list.component.html'
})
export class WorklogListComponent implements OnInit {
  private readonly worklogService = inject(WorklogService);
  private readonly router = inject(Router);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  private readonly translate = inject(TranslateService);
  private readonly fb = inject(FormBuilder);

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  readonly displayedColumns = ['taskTitle', 'userName', 'workDate', 'hours', 'description', 'actions'];
  readonly dataSource = new MatTableDataSource<Worklog>();
  readonly isLoading = signal(false);
  readonly totalElements = signal(0);
  readonly pageSize = signal(20);
  readonly pageIndex = signal(0);

  readonly dateRangeForm: FormGroup = this.fb.group({
    start: [null],
    end: [null]
  });

  ngOnInit(): void {
    this.loadWorklogs();
  }

  loadWorklogs(): void {
    this.isLoading.set(true);
    const startVal = this.dateRangeForm.get('start')?.value as Date | null;
    const endVal = this.dateRangeForm.get('end')?.value as Date | null;

    const obs$ =
      startVal && endVal
        ? this.worklogService.getByDateRange(
            this.formatDate(startVal),
            this.formatDate(endVal),
            this.pageIndex(),
            this.pageSize()
          )
        : this.worklogService.getAll(this.pageIndex(), this.pageSize());

    obs$.subscribe({
      next: (resp) => {
        this.dataSource.data = resp.content;
        this.totalElements.set(resp.totalElements);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
        this.showError('WORKLOG.LOAD_ERROR');
      }
    });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.loadWorklogs();
  }

  applyDateFilter(): void {
    this.pageIndex.set(0);
    this.loadWorklogs();
  }

  resetFilter(): void {
    this.dateRangeForm.reset();
    this.loadWorklogs();
  }

  navigateToCreate(): void {
    this.router.navigate(['/worklogs/new']);
  }

  navigateToEdit(id: string, event: Event): void {
    event.stopPropagation();
    this.router.navigate(['/worklogs', id, 'edit']);
  }

  confirmDelete(worklog: Worklog, event: Event): void {
    event.stopPropagation();
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '400px',
      data: { title: 'COMMON.CONFIRM_DELETE', message: 'COMMON.CONFIRM_DELETE_MSG' }
    });
    dialogRef.afterClosed().subscribe((confirmed: boolean) => {
      if (confirmed) {
        this.isLoading.set(true);
        this.worklogService.delete(worklog.id).subscribe({
          next: () => {
            this.isLoading.set(false);
            this.showSuccess('WORKLOG.DELETE_SUCCESS');
            this.loadWorklogs();
          },
          error: () => {
            this.isLoading.set(false);
            this.showError('WORKLOG.DELETE_ERROR');
          }
        });
      }
    });
  }

  private formatDate(date: Date): string {
    return date.toISOString().split('T')[0];
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
