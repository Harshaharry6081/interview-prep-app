import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth';

export interface Project { id: string; name: string; description: string; }
export interface Folder  { id: string; name: string; projectId: string; parentFolderId: string | null; }

@Injectable({ providedIn: 'root' })
export class ProjectService {
  private baseUrl = '/api';

  constructor(private http: HttpClient, private auth: AuthService) {}

  private headers() {
    return { headers: new HttpHeaders({ Authorization: `Bearer ${this.auth.getToken()}` }) };
  }

  getProjects(): Observable<Project[]> {
    return this.http.get<Project[]>(`${this.baseUrl}/projects`, this.headers());
  }

  createProject(name: string, description: string): Observable<Project> {
    return this.http.post<Project>(`${this.baseUrl}/projects`, { name, description }, this.headers());
  }

  getFolders(projectId: string): Observable<Folder[]> {
    return this.http.get<Folder[]>(`${this.baseUrl}/folders/project/${projectId}`, this.headers());
  }

  createFolder(name: string, projectId: string, parentFolderId?: string): Observable<Folder> {
    return this.http.post<Folder>(`${this.baseUrl}/folders`, { name, projectId, parentFolderId }, this.headers());
  }
}
