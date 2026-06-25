import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Bug, BugCreateRequest, BugStatus, BugUpdateRequest } from '../models/bug.model';
import { PageResponse } from '../models/page-response.model';

@Injectable({ providedIn: 'root' })
export class BugService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/api/bugs`;

  getAll(page = 0, size = 20): Observable<PageResponse<Bug>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<PageResponse<Bug>>(this.base, { params });
  }

  getById(id: string): Observable<Bug> {
    return this.http.get<Bug>(`${this.base}/${id}`);
  }

  getByProject(projectId: string, page = 0, size = 50): Observable<PageResponse<Bug>> {
    const params = new HttpParams()
      .set('projectId', projectId)
      .set('page', page)
      .set('size', size);
    return this.http.get<PageResponse<Bug>>(this.base, { params });
  }

  create(req: BugCreateRequest): Observable<Bug> {
    return this.http.post<Bug>(this.base, req);
  }

  update(id: string, req: BugUpdateRequest): Observable<Bug> {
    return this.http.put<Bug>(`${this.base}/${id}`, req);
  }

  updateStatus(id: string, status: BugStatus): Observable<Bug> {
    return this.http.patch<Bug>(`${this.base}/${id}/status`, { status });
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
