import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth';

export interface ChatMessage {
  role: string;
  content: string;
  timestamp: string;
}

export interface AgentFeedback {
  agentName: string;
  score: number;
  feedback: string;
  suggestions: string;
}

export interface InterviewSession {
  id: string;
  userId: string;
  topic: string;
  interviewGroup: string;
  difficulty: string;
  transcript: ChatMessage[];
  agentFeedbacks: AgentFeedback[];
  overallScore: number;
  finalAssessment: string;
  startedAt: string;
  completedAt: string;
}

export interface InterviewGroup {
  id: string;
  title: string;
  icon: string;
  description: string;
  topics: string[];
  color: string;
}

@Injectable({ providedIn: 'root' })
export class InterviewService {
  private apiUrl = '/api/interview';
  private groupsUrl = '/api/groups';

  constructor(private http: HttpClient, private auth: AuthService) {}

  private get headers(): HttpHeaders {
    const token = this.auth.getToken();
    return new HttpHeaders({ Authorization: `Bearer ${token}` });
  }

  getGroups(): Observable<InterviewGroup[]> {
    return this.http.get<InterviewGroup[]>(this.groupsUrl);
  }

  startSession(userId: string, topic: string, interviewGroup: string, difficulty: string): Observable<InterviewSession> {
    return this.http.post<InterviewSession>(
      `${this.apiUrl}/start?userId=${encodeURIComponent(userId)}&topic=${encodeURIComponent(topic)}&interviewGroup=${interviewGroup}&difficulty=${difficulty}`,
      {},
      { headers: this.headers }
    );
  }

  sendMessage(sessionId: string, message: string): Observable<InterviewSession> {
    return this.http.post<InterviewSession>(
      `${this.apiUrl}/${sessionId}/message`,
      { message },
      { headers: this.headers }
    );
  }

  endSession(sessionId: string): Observable<InterviewSession> {
    return this.http.post<InterviewSession>(
      `${this.apiUrl}/${sessionId}/end`,
      {},
      { headers: this.headers }
    );
  }

  getHistory(userId: string): Observable<InterviewSession[]> {
    return this.http.get<InterviewSession[]>(
      `${this.apiUrl}/history?userId=${encodeURIComponent(userId)}`,
      { headers: this.headers }
    );
  }

  getSession(sessionId: string): Observable<InterviewSession> {
    return this.http.get<InterviewSession>(`${this.apiUrl}/${sessionId}`, { headers: this.headers });
  }
}
