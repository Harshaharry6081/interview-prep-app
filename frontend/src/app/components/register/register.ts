import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

/**
 * Register component is no longer used — Google Sign-In
 * automatically handles account creation on first login.
 * This stub exists only to prevent build errors from any
 * stale imports; the /register route has been removed.
 */
@Component({
  selector: 'app-register',
  standalone: true,
  imports: [RouterLink],
  template: `
    <div style="min-height:100vh;display:flex;align-items:center;justify-content:center;flex-direction:column;gap:1rem;">
      <h2>No manual registration needed.</h2>
      <a routerLink="/login" style="color:#818cf8;">← Back to Sign In</a>
    </div>
  `
})
export class Register {}
