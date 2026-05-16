import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ChatMessage {
  role: string;
  content: string;
  timestamp: string;
}

export interface InterviewSession {
  id: string;
  userId: string;
  topic: string;
  transcript: ChatMessage[];
  startedAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class InterviewService {
  private apiUrl = '/api/interview';

  constructor(private http: HttpClient) { }

  startSession(userId: string, topic: string): Observable<InterviewSession> {
    return this.http.post<InterviewSession>(`${this.apiUrl}/start?userId=${userId}&topic=${topic}`, {});
  }

  sendMessage(sessionId: string, message: string): Observable<InterviewSession> {
    return this.http.post<InterviewSession>(`${this.apiUrl}/${sessionId}/message`, message);
  }
}
