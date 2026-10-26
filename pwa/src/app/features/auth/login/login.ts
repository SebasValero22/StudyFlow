import { Component, signal, OnInit, OnDestroy } from '@angular/core';
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
export class LoginComponent implements OnInit, OnDestroy {
  credentials: UserLoginDTO = {
    email: '',
    password: ''
  };
  errorMessage = signal('');
  loading = signal(false);
  loadingNotice = signal('');
  loadingSeconds = signal(0);
  private timerRef: any;

  constructor(private apiService: ApiService, private router: Router) {}

  ngOnInit() {
    this.apiService.prewarmServer();
  }

  ngOnDestroy() {
    if (this.timerRef) {
      clearInterval(this.timerRef);
    }
  }

  onSubmit() {
    if (!this.credentials.email || !this.credentials.password) {
      this.errorMessage.set('Please fill in all fields');
      return;
    }

    this.loading.set(true);
    this.errorMessage.set('');
    this.loadingSeconds.set(0);
    this.loadingNotice.set('');

    if (this.timerRef) clearInterval(this.timerRef);
    this.timerRef = setInterval(() => {
      this.loadingSeconds.update(s => s + 1);
      if (this.loadingSeconds() >= 3) {
        this.loadingNotice.set(
          `☕ Waking up cloud server (${this.loadingSeconds()}s)... Render free instances spin down when idle. Almost ready!`
        );
      }
    }, 1000);

    this.apiService.login(this.credentials).subscribe({
      next: () => {
        clearInterval(this.timerRef);
        this.loading.set(false);
        this.loadingNotice.set('');
        this.router.navigate(['/overview']);
      },
      error: (err) => {
        clearInterval(this.timerRef);
        this.loading.set(false);
        this.loadingNotice.set('');
        if (err.status === 0 || err.status === 504) {
          this.errorMessage.set('Server waking up from sleep. Now ready — please click Login once more.');
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
