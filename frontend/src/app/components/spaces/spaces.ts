import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { InterviewService, InterviewGroup } from '../../services/interview';

@Component({
  selector: 'app-spaces',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './spaces.html',
  styleUrl: './spaces.scss'
})
export class Spaces implements OnInit {
  groups: InterviewGroup[] = [];
  selectedGroup: InterviewGroup | null = null;
  selectedDifficulty = 'MEDIUM';
  loading = false;
  error = '';

  difficulties = [
    { value: 'EASY', label: 'Easy', icon: '🟢', desc: 'Fundamentals & basics' },
    { value: 'MEDIUM', label: 'Medium', icon: '🟡', desc: 'Mid-level concepts' },
    { value: 'HARD', label: 'Hard', icon: '🔴', desc: 'Advanced & expert level' }
  ];

  constructor(private interviewService: InterviewService, private router: Router) {}

  ngOnInit() {
    this.interviewService.getGroups().subscribe({
      next: (groups) => this.groups = groups,
      error: () => this.error = 'Failed to load interview groups.'
    });
  }

  selectGroup(group: InterviewGroup) {
    this.selectedGroup = group;
  }

  startInterview() {
    if (!this.selectedGroup) return;
    this.router.navigate(['/interview'], {
      queryParams: {
        group: this.selectedGroup.id,
        topic: this.selectedGroup.title,
        difficulty: this.selectedDifficulty
      }
    });
  }

  dismiss() {
    this.selectedGroup = null;
  }
}
