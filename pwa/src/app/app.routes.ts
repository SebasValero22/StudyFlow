import { Routes, Router } from '@angular/router';
import { inject } from '@angular/core';
import { ApiService } from './core/services/api';
import { OverviewComponent } from './features/overview/overview';
import { TasksComponent } from './features/tasks/tasks';
import { ExamsComponent } from './features/exams/exams';
import { SubjectsComponent } from './features/subjects/subjects';
import { GradesComponent } from './features/grades/grades';
import { ProfileComponent } from './features/profile/profile';
import { SettingsComponent } from './features/settings/settings';
import { LoginComponent } from './features/auth/login/login';
import { RegisterComponent } from './features/auth/register/register';

const authGuard = () => {
  const apiService = inject(ApiService);
  const router = inject(Router);
  if (apiService.isLoggedIn()) {
    return true;
  }
  return router.parseUrl('/login');
};

const redirectIfLoggedIn = () => {
  const apiService = inject(ApiService);
  const router = inject(Router);
  if (apiService.isLoggedIn()) {
    return router.parseUrl('/overview');
  }
  return true;
};

export const routes: Routes = [
  { path: '', redirectTo: 'overview', pathMatch: 'full' },
  { path: 'login', component: LoginComponent, canActivate: [redirectIfLoggedIn] },
  { path: 'register', component: RegisterComponent, canActivate: [redirectIfLoggedIn] },
  { path: 'overview', component: OverviewComponent, canActivate: [authGuard] },
  { path: 'tasks', component: TasksComponent, canActivate: [authGuard] },
  { path: 'exams', component: ExamsComponent, canActivate: [authGuard] },
  { path: 'subjects', component: SubjectsComponent, canActivate: [authGuard] },
  { path: 'grades', component: GradesComponent, canActivate: [authGuard] },
  { path: 'profile', component: ProfileComponent, canActivate: [authGuard] },
  { path: 'settings', component: SettingsComponent, canActivate: [authGuard] }
];
