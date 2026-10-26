import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'overview', pathMatch: 'full' },
  { path: 'login', loadComponent: () => import('./features/auth/login/login').then(m => m.LoginComponent) },
  { path: 'register', loadComponent: () => import('./features/auth/register/register').then(m => m.RegisterComponent) },
  { path: 'overview', canActivate: [authGuard], loadComponent: () => import('./features/overview/overview').then(m => m.OverviewComponent) },
  { path: 'tasks', canActivate: [authGuard], loadComponent: () => import('./features/tasks/tasks').then(m => m.TasksComponent) },
  { path: 'exams', canActivate: [authGuard], loadComponent: () => import('./features/exams/exams').then(m => m.ExamsComponent) },
  { path: 'subjects', canActivate: [authGuard], loadComponent: () => import('./features/subjects/subjects').then(m => m.SubjectsComponent) },
  { path: 'grades', canActivate: [authGuard], loadComponent: () => import('./features/grades/grades').then(m => m.GradesComponent) },
  { path: 'profile', canActivate: [authGuard], loadComponent: () => import('./features/profile/profile').then(m => m.ProfileComponent) },
  { path: 'settings', canActivate: [authGuard], loadComponent: () => import('./features/settings/settings').then(m => m.SettingsComponent) },
  { path: 'timer', canActivate: [authGuard], loadComponent: () => import('./features/timer/timer').then(m => m.TimerComponent) },
  { path: 'focus-report', canActivate: [authGuard], loadComponent: () => import('./features/focus-report/focus-report').then(m => m.FocusReportComponent) },
  { path: 'journal', canActivate: [authGuard], loadComponent: () => import('./features/journal/journal').then(m => m.JournalComponent) },
  { path: 'notebooks', canActivate: [authGuard], loadComponent: () => import('./features/notebooks/notebooks').then(m => m.NotebooksComponent) }
];
