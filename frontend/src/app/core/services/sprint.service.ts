import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Sprint, SprintCreateRequest } from '../models/sprint.model';
import { PageResponse } from '../models/page-response.model';

@Injectable({ providedIn: 'root' })
export class SprintService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/api/sprints`;

  getAll(page = 0, size = 20): Observable<PageResponse<Sprint>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<PageResponse<Sprint>>(this.base, { params });
  }

  getById(id: string): Observable<Sprint> {
    return this.http.get<Sprint>(`${this.base}/${id}`);
  }

  getByProject(projectId: string): Observable<Sprint[]> {
    const params = new HttpParams().set('projectId', projectId);
    return this.http.get<Sprint[]>(this.base, { params });
  }

  create(req: SprintCreateRequest): Observable<Sprint> {
    return this.http.post<Sprint>(this.base, req);
  }

  update(id: string, req: Partial<SprintCreateRequest>): Observable<Sprint> {
    return this.http.put<Sprint>(`${this.base}/${id}`, req);
  }

  activate(id: string): Observable<Sprint> {
    return this.http.post<Sprint>(`${this.base}/${id}/activate`, {});
  }

  complete(id: string): Observable<Sprint> {
    return this.http.post<Sprint>(`${this.base}/${id}/complete`, {});
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
