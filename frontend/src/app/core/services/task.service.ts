import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Task, TaskCreateRequest, TaskStatus, TaskUpdateRequest } from '../models/task.model';
import { PageResponse } from '../models/page-response.model';

@Injectable({ providedIn: 'root' })
export class TaskService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/api/tasks`;

  getAll(page = 0, size = 20): Observable<PageResponse<Task>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<PageResponse<Task>>(this.base, { params });
  }

  getById(id: string): Observable<Task> {
    return this.http.get<Task>(`${this.base}/${id}`);
  }

  getByProject(projectId: string, page = 0, size = 50): Observable<PageResponse<Task>> {
    const params = new HttpParams()
      .set('projectId', projectId)
      .set('page', page)
      .set('size', size);
    return this.http.get<PageResponse<Task>>(this.base, { params });
  }

  getBySprint(sprintId: string): Observable<Task[]> {
    return this.http.get<Task[]>(`${environment.apiUrl}/api/sprints/${sprintId}/tasks`);
  }

  create(req: TaskCreateRequest): Observable<Task> {
    return this.http.post<Task>(this.base, req);
  }

  update(id: string, req: TaskUpdateRequest): Observable<Task> {
    return this.http.put<Task>(`${this.base}/${id}`, req);
  }

  updateStatus(id: string, status: TaskStatus): Observable<Task> {
    return this.http.patch<Task>(`${this.base}/${id}/status`, { status });
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
