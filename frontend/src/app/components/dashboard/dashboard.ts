import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { InterviewService, InterviewSession } from '../../services/interview';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss'
})
export class Dashboard implements OnInit {
  sessions: InterviewSession[] = [];
  loading = true;
  username = '';

  constructor(
    private interviewService: InterviewService,
    private auth: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    this.username = this.auth.getUsername();
    this.interviewService.getHistory(this.username).subscribe({
      next: (sessions) => {
        this.sessions = sessions;
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  get totalSessions() { return this.sessions.length; }

  get completedSessions() { return this.sessions.filter(s => s.completedAt).length; }

  get averageScore(): number | null {
    const scored = this.sessions.filter(s => s.overallScore != null);
    if (!scored.length) return null;
    const avg = scored.reduce((sum, s) => sum + (s.overallScore || 0), 0) / scored.length;
    return Math.round(avg * 10) / 10;
  }

  get scoresByGroup(): { group: string; avg: number; count: number }[] {
    const map: Record<string, number[]> = {};
    this.sessions.forEach(s => {
      if (s.overallScore != null) {
        if (!map[s.interviewGroup]) map[s.interviewGroup] = [];
        map[s.interviewGroup].push(s.overallScore);
      }
    });
    return Object.entries(map).map(([group, scores]) => ({
      group,
      avg: Math.round((scores.reduce((a, b) => a + b, 0) / scores.length) * 10) / 10,
      count: scores.length
    })).sort((a, b) => b.avg - a.avg);
  }

  get recentSessions() {
    return this.sessions.slice(0, 5);
  }

  startNew() {
    this.router.navigate(['/spaces']);
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '';
    return new Date(dateStr).toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' });
  }

  scoreColor(score: number): string {
    if (score >= 8) return '#4ade80';
    if (score >= 6) return '#facc15';
    return '#f87171';
  }

  groupIcon(group: string): string {
    const icons: Record<string, string> = {
      SYSTEM_DESIGN: '🏗️', BACKEND: '⚙️', FRONTEND: '🖥️',
      DSA: '🧩', DEVOPS: '☁️', HR: '🤝', GENERAL: '💬'
    };
    return icons[group] || '💬';
  }
}
