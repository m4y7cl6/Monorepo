import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Project, ProjectCreateRequest, ProjectUpdateRequest } from '../models/project.model';
import { PageResponse } from '../models/page-response.model';

@Injectable({ providedIn: 'root' })
export class ProjectService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/api/projects`;

  getAll(page = 0, size = 20, name?: string): Observable<PageResponse<Project>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (name) {
      params = params.set('name', name);
    }
    return this.http.get<PageResponse<Project>>(this.base, { params });
  }

  getById(id: string): Observable<Project> {
    return this.http.get<Project>(`${this.base}/${id}`);
  }

  create(req: ProjectCreateRequest): Observable<Project> {
    return this.http.post<Project>(this.base, req);
  }

  update(id: string, req: ProjectUpdateRequest): Observable<Project> {
    return this.http.put<Project>(`${this.base}/${id}`, req);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
