import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Worklog, WorklogCreateRequest, WorklogUpdateRequest } from '../models/worklog.model';
import { PageResponse } from '../models/page-response.model';

@Injectable({ providedIn: 'root' })
export class WorklogService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/api/worklogs`;

  getAll(page = 0, size = 20): Observable<PageResponse<Worklog>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<PageResponse<Worklog>>(this.base, { params });
  }

  getById(id: string): Observable<Worklog> {
    return this.http.get<Worklog>(`${this.base}/${id}`);
  }

  getByTask(taskId: string): Observable<Worklog[]> {
    const params = new HttpParams().set('taskId', taskId);
    return this.http.get<Worklog[]>(this.base, { params });
  }

  getByUser(userId: string): Observable<Worklog[]> {
    const params = new HttpParams().set('userId', userId);
    return this.http.get<Worklog[]>(this.base, { params });
  }

  getByDateRange(startDate: string, endDate: string, page = 0, size = 20): Observable<PageResponse<Worklog>> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate)
      .set('page', page)
      .set('size', size);
    return this.http.get<PageResponse<Worklog>>(this.base, { params });
  }

  create(req: WorklogCreateRequest): Observable<Worklog> {
    return this.http.post<Worklog>(this.base, req);
  }

  update(id: string, req: WorklogUpdateRequest): Observable<Worklog> {
    return this.http.put<Worklog>(`${this.base}/${id}`, req);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
