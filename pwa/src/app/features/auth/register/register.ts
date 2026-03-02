import { Component, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ApiService, UserRegisterDTO } from '../../../core/services/api';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './register.html',
  styleUrl: './register.scss'
})
export class RegisterComponent {
  userData: UserRegisterDTO = {
    name: '',
    email: '',
    password: ''
  };
  errorMessage = signal('');
  loading = signal(false);

  constructor(private apiService: ApiService, private router: Router) {}

  onSubmit() {
    if (!this.userData.name || !this.userData.email || !this.userData.password) {
      this.errorMessage.set('Please fill in all fields');
      return;
    }

    this.loading.set(true);
    this.errorMessage.set('');

    this.apiService.register(this.userData).subscribe({
      next: () => {
        this.router.navigate(['/overview']);
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set('Error during registration. Please try again.');
        console.error('Registration error:', err);
      }
    });
  }
}
