import { Routes } from '@angular/router';
import { Login } from './components/login/login';
import { Dashboard } from './components/dashboard/dashboard';
import { Spaces } from './components/spaces/spaces';
import { ResourceUpload } from './components/resource-upload/resource-upload';
import { MockInterview } from './components/mock-interview/mock-interview';
import { FeedbackPanel } from './components/feedback-panel/feedback-panel';
import { authGuard } from './guards/auth-guard';

export const routes: Routes = [
  { path: '',          redirectTo: 'spaces', pathMatch: 'full' },
  { path: 'login',     component: Login },
  { path: 'dashboard', component: Dashboard,    canActivate: [authGuard] },
  { path: 'spaces',    component: Spaces,        canActivate: [authGuard] },
  { path: 'upload',    component: ResourceUpload, canActivate: [authGuard] },
  { path: 'interview', component: MockInterview,  canActivate: [authGuard] },
  { path: 'feedback',  component: FeedbackPanel,  canActivate: [authGuard] },
  { path: '**',        redirectTo: 'login' }
];
