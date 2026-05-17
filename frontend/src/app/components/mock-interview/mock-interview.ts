import { Component, OnInit, OnDestroy, ViewChild, ElementRef, AfterViewChecked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { InterviewService, InterviewSession, AgentFeedback } from '../../services/interview';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-mock-interview',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './mock-interview.html',
  styleUrl: './mock-interview.scss'
})
export class MockInterview implements OnInit, OnDestroy, AfterViewChecked {
  @ViewChild('transcriptEnd') transcriptEnd!: ElementRef;

  session: InterviewSession | null = null;
  userInput = '';
  isLoading = false;
  isStarting = true;
  isSending = false;
  isEnded = false;
  error = '';

  // From route params
  group = '';
  topic = '';
  difficulty = 'MEDIUM';

  // UI state
  showFeedback = false;
  lastFeedbacks: AgentFeedback[] = [];

  private shouldScrollToBottom = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private interviewService: InterviewService,
    private auth: AuthService
  ) {}

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      this.group = params['group'] || 'GENERAL';
      this.topic = params['topic'] || 'General';
      this.difficulty = params['difficulty'] || 'MEDIUM';
      this.startSession();
    });
  }

  ngAfterViewChecked() {
    if (this.shouldScrollToBottom) {
      this.scrollToBottom();
      this.shouldScrollToBottom = false;
    }
  }

  ngOnDestroy() {}

  startSession() {
    this.isStarting = true;
    this.error = '';
    const userId = this.auth.getUsername() || 'anonymous';

    this.interviewService.startSession(userId, this.topic, this.group, this.difficulty).subscribe({
      next: (session) => {
        this.session = session;
        this.isStarting = false;
        this.shouldScrollToBottom = true;
      },
      error: (err) => {
        this.isStarting = false;
        this.error = 'Failed to start interview session. Please try again.';
        console.error(err);
      }
    });
  }

  sendMessage() {
    if (!this.userInput.trim() || !this.session || this.isSending) return;

    const message = this.userInput.trim();
    this.userInput = '';
    this.isSending = true;
    this.showFeedback = false;

    this.interviewService.sendMessage(this.session.id, message).subscribe({
      next: (updated) => {
        this.session = updated;
        this.lastFeedbacks = updated.agentFeedbacks || [];
        this.showFeedback = this.lastFeedbacks.length > 0;
        this.isSending = false;
        this.shouldScrollToBottom = true;
      },
      error: () => {
        this.isSending = false;
        this.error = 'Failed to send message. Please retry.';
      }
    });
  }

  endSession() {
    if (!this.session) return;
    this.isLoading = true;

    this.interviewService.endSession(this.session.id).subscribe({
      next: (ended) => {
        this.session = ended;
        this.isEnded = true;
        this.isLoading = false;
        this.shouldScrollToBottom = true;
      },
      error: () => {
        this.isLoading = false;
        this.error = 'Failed to end session.';
      }
    });
  }

  goToSpaces() {
    this.router.navigate(['/spaces']);
  }

  handleKeyDown(event: KeyboardEvent) {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }

  get aiMessages() {
    return this.session?.transcript.filter(m => m.role === 'AI_INTERVIEWER') || [];
  }

  get overallScore() {
    return this.session?.overallScore ?? null;
  }

  get scoreColor() {
    const s = this.overallScore;
    if (!s) return '#818cf8';
    if (s >= 8) return '#4ade80';
    if (s >= 6) return '#facc15';
    return '#f87171';
  }

  private scrollToBottom() {
    try { this.transcriptEnd?.nativeElement?.scrollIntoView({ behavior: 'smooth' }); } catch {}
  }
}
