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
        this.errorMessage.set('Invalid email or password');
        console.error('Login error:', err);
      }
    });
  }
}
