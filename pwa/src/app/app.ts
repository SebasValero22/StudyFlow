import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { Observable } from 'rxjs';
import { ApiService } from './core/services/api';

import { NyraVoiceButton } from './core/components/nyra-voice-button/nyra-voice-button';
import { AudioPlayerComponent } from './shared/components/audio-player/audio-player';

import { ThemeService } from './core/services/theme.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, NyraVoiceButton, AudioPlayerComponent, CommonModule],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {
  protected readonly title = signal('studyflow-pwa');
  currentTheme$: Observable<string>;

  constructor(private apiService: ApiService, private themeService: ThemeService) {
    this.currentTheme$ = this.themeService.activeTheme$;
  }

  changeTheme(event: Event) {
    const select = event.target as HTMLSelectElement;
    this.themeService.setTheme(select.value);
  }

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
