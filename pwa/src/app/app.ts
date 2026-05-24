import { Component, signal, computed } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { ApiService } from './core/services/api';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {
  protected readonly title = signal('studyflow-pwa');
  
  constructor(private apiService: ApiService) {}

  isLoggedIn() {
    return this.apiService.isLoggedIn();
  }

  getCurrentUser() {
    return this.apiService.getCurrentUser();
  }

  logout() {
    this.apiService.logout();
  }
}
