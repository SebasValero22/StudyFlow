import { Routes } from '@angular/router';
import { OverviewComponent } from './features/overview/overview';
import { TasksComponent } from './features/tasks/tasks';
import { ExamsComponent } from './features/exams/exams';
import { SubjectsComponent } from './features/subjects/subjects';
import { GradesComponent } from './features/grades/grades';
import { ProfileComponent } from './features/profile/profile';
import { SettingsComponent } from './features/settings/settings';

export const routes: Routes = [
  { path: '', redirectTo: 'overview', pathMatch: 'full' },
  { path: 'overview', component: OverviewComponent },
  { path: 'tasks', component: TasksComponent },
  { path: 'exams', component: ExamsComponent },
  { path: 'subjects', component: SubjectsComponent },
  { path: 'grades', component: GradesComponent },
  { path: 'profile', component: ProfileComponent },
  { path: 'settings', component: SettingsComponent }
];
