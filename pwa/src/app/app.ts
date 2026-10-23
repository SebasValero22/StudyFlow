import { Component, signal, OnInit, OnDestroy, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { Observable } from 'rxjs';
import { ApiService } from './core/services/api';

import { NyraVoiceButton } from './core/components/nyra-voice-button/nyra-voice-button';
import { AudioPlayerComponent } from './shared/components/audio-player/audio-player';
import { ThemeService } from './core/services/theme.service';
import { ModeService } from './core/services/mode.service';
import { CommandModalComponent } from './shared/components/command-modal/command-modal';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    RouterOutlet, 
    RouterLink, 
    RouterLinkActive, 
    NyraVoiceButton, 
    AudioPlayerComponent, 
    CommandModalComponent,
    CommonModule
  ],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App implements OnInit, OnDestroy {
  @ViewChild(CommandModalComponent) commandModal?: CommandModalComponent;

  protected readonly title = signal('studyflow-pwa');
  currentTheme$: Observable<string>;

  sidebarPosition = signal<'left' | 'right'>('left');
  isSidebarCollapsed = signal<boolean>(false);
  customPfp = signal<string | null>(null);

  private eventListeners: (() => void)[] = [];

  constructor(
    private apiService: ApiService, 
    private themeService: ThemeService,
    public modeService: ModeService
  ) {
    this.currentTheme$ = this.themeService.activeTheme$;
  }

  ngOnInit() {
    this.sidebarPosition.set((localStorage.getItem('studyflow-sidebar-pos') as 'left' | 'right') || 'left');
    this.isSidebarCollapsed.set(localStorage.getItem('studyflow-sidebar-collapsed') === 'true');
    this.customPfp.set(localStorage.getItem('studyflow-custom-pfp'));

    // Event listeners for real-time reactivity from Settings
    const onPos = (e: any) => this.sidebarPosition.set(e.detail);
    const onPfp = (e: any) => this.customPfp.set(e.detail);

    window.addEventListener('studyflow-sidebar-pos-changed', onPos);
    window.addEventListener('studyflow-pfp-changed', onPfp);

    this.eventListeners.push(
      () => window.removeEventListener('studyflow-sidebar-pos-changed', onPos),
      () => window.removeEventListener('studyflow-pfp-changed', onPfp)
    );
  }

  ngOnDestroy() {
    this.eventListeners.forEach(fn => fn());
  }

  toggleSidebar() {
    const next = !this.isSidebarCollapsed();
    this.isSidebarCollapsed.set(next);
    localStorage.setItem('studyflow-sidebar-collapsed', String(next));
  }

  toggleAppMode(event?: Event) {
    if (event) event.stopPropagation();
    this.modeService.toggleMode();
  }

  openQuickCreate(tab: any = 'task') {
    if (this.commandModal) {
      this.commandModal.open(tab);
    } else if (typeof window !== 'undefined') {
      window.dispatchEvent(new CustomEvent('open-studyflow-command-modal', { detail: { tab } }));
    }
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
