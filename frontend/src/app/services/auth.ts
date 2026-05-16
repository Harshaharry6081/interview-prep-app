import { Injectable, NgZone } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { Router } from '@angular/router';

export interface AuthResponse {
  token: string;
  username: string;
  email: string;
  picture?: string;
}

// Extend window to include the Google GIS types
declare global {
  interface Window {
    google: {
      accounts: {
        id: {
          initialize: (config: object) => void;
          renderButton: (parent: HTMLElement, config: object) => void;
          prompt: () => void;
        };
      };
    };
  }
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private apiUrl = '/api/auth';

  // !! REPLACE THIS with your actual Google Client ID !!
  readonly GOOGLE_CLIENT_ID = '965213755424-ig93q0pk9uiui11t045jc02o6pfd3v0t.apps.googleusercontent.com';

  constructor(
    private http: HttpClient,
    private router: Router,
    private ngZone: NgZone
  ) {}

  /**
   * Called after Google GIS delivers a credential (ID token).
   * We forward it to our backend to verify and get our own JWT.
   */
  loginWithGoogle(idToken: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/google`, { idToken }).pipe(
      tap(res => this.saveSession(res))
    );
  }

  /**
   * Test Login for Development/Hackathon without Google OAuth.
   */
  loginWithTestUser(username: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/test-login`, { username, password }).pipe(
      tap(res => this.saveSession(res))
    );
  }

  /**
   * Initialise Google Identity Services and render the button into a given element.
   */
  initGoogleSignIn(buttonElement: HTMLElement, onSuccess: (idToken: string) => void) {
    const tryInit = () => {
      if (window.google?.accounts?.id) {
        window.google.accounts.id.initialize({
          client_id: this.GOOGLE_CLIENT_ID,
          callback: (response: { credential: string }) => {
            this.ngZone.run(() => onSuccess(response.credential));
          },
          auto_select: false,
          cancel_on_tap_outside: true,
        });
        window.google.accounts.id.renderButton(buttonElement, {
          theme: 'outline',
          size: 'large',
          shape: 'rectangular',
          logo_alignment: 'left',
          width: '320',
          text: 'signin_with',
        });
      } else {
        // GIS script still loading — retry in 300ms
        setTimeout(tryInit, 300);
      }
    };
    tryInit();
  }

  private saveSession(res: AuthResponse) {
    localStorage.setItem('jwt_token', res.token);
    localStorage.setItem('username', res.username);
    localStorage.setItem('email', res.email);
    if (res.picture) localStorage.setItem('picture', res.picture);
  }

  logout() {
    localStorage.clear();
    this.router.navigate(['/login']);
  }

  getToken(): string | null { return localStorage.getItem('jwt_token'); }
  isLoggedIn(): boolean    { return !!this.getToken(); }
  getUsername(): string    { return localStorage.getItem('username') || 'User'; }
  getPicture(): string     { return localStorage.getItem('picture') || ''; }
}
