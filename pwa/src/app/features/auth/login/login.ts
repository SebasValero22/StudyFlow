import { Component, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ApiService, UserLoginDTO } from '../../../core/services/api';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.scss'
})
export class LoginComponent {
  credentials: UserLoginDTO = {
    email: '',
    password: ''
  };
  errorMessage = signal('');
  loading = signal(false);

  constructor(private apiService: ApiService, private router: Router) {}

  onSubmit() {
    if (!this.credentials.email || !this.credentials.password) {
      this.errorMessage.set('Please fill in all fields');
      return;
    }

    this.loading.set(true);
    this.errorMessage.set('');

    this.apiService.login(this.credentials).subscribe({
      next: () => {
        this.router.navigate(['/overview']);
      },
      error: (err) => {
        this.loading.set(false);
        if (err.status === 0 || err.status === 504) {
          this.errorMessage.set('Connecting to server (Render free tier waking up). Please try again in a few seconds.');
        } else if (err.status === 401) {
          this.errorMessage.set('Invalid email or password. Use admin@mail.com / 123 or register below.');
        } else {
          this.errorMessage.set(err.error?.message || err.error || 'Login failed. Please check credentials or register.');
        }
        console.error('Login error:', err);
      }
    });
  }
}
