import { Component, AfterViewInit, ElementRef, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth';

import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.scss'
})
export class Login implements AfterViewInit {
  @ViewChild('googleBtn') googleBtnRef!: ElementRef<HTMLDivElement>;

  errorMsg = '';
  loading = false;
  
  testUsername = 'testuser';
  testPassword = 'password';

  constructor(private auth: AuthService, private router: Router) {}

  ngAfterViewInit() {
    this.auth.initGoogleSignIn(this.googleBtnRef.nativeElement, (idToken) => {
      this.loading = true;
      this.errorMsg = '';
      this.auth.loginWithGoogle(idToken).subscribe({
        next: () => this.router.navigate(['/dashboard']),
        error: () => {
          this.errorMsg = 'Google sign-in failed. Please try again.';
          this.loading = false;
        }
      });
    });
  }

  testLogin() {
    if (!this.testUsername || !this.testPassword) {
      this.errorMsg = 'Please enter username and password';
      return;
    }
    
    this.loading = true;
    this.errorMsg = '';
    
    this.auth.loginWithTestUser(this.testUsername, this.testPassword).subscribe({
      next: () => this.router.navigate(['/dashboard']),
      error: () => {
        this.errorMsg = 'Test sign-in failed. Please try again.';
        this.loading = false;
      }
    });
  }
}
