import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  Requirement,
  RequirementCreateRequest,
  RequirementUpdateRequest
} from '../models/requirement.model';
import { PageResponse } from '../models/page-response.model';

@Injectable({ providedIn: 'root' })
export class RequirementService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/api/requirements`;

  getAll(page = 0, size = 20): Observable<PageResponse<Requirement>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<PageResponse<Requirement>>(this.base, { params });
  }

  getById(id: string): Observable<Requirement> {
    return this.http.get<Requirement>(`${this.base}/${id}`);
  }

  getByProject(projectId: string, page = 0, size = 50): Observable<PageResponse<Requirement>> {
    const params = new HttpParams()
      .set('projectId', projectId)
      .set('page', page)
      .set('size', size);
    return this.http.get<PageResponse<Requirement>>(this.base, { params });
  }

  create(req: RequirementCreateRequest): Observable<Requirement> {
    return this.http.post<Requirement>(this.base, req);
  }

  update(id: string, req: RequirementUpdateRequest): Observable<Requirement> {
    return this.http.put<Requirement>(`${this.base}/${id}`, req);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
