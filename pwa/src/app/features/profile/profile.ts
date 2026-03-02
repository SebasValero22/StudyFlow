import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { ApiService } from '../../core/services/api';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './profile.html',
  styleUrl: './profile.scss',
})
export class ProfileComponent {
  constructor(private apiService: ApiService, private router: Router) {}

  getUser() {
    return this.apiService.getCurrentUser();
  }

  logout() {
    this.apiService.logout();
    this.router.navigate(['/login']);
  }
}
