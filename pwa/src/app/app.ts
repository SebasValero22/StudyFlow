import { Component, signal, OnInit, OnDestroy, ViewChild, ElementRef } from '@angular/core';
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
  @ViewChild('hoverVideo') hoverVideoRef?: ElementRef<HTMLVideoElement>;
  @ViewChild('splashVideo') splashVideoRef?: ElementRef<HTMLVideoElement>;

  protected readonly title = signal('studyflow-pwa');
  currentTheme$: Observable<string>;

  sidebarPosition = signal<'left' | 'right'>('left');
  isSidebarCollapsed = signal<boolean>(false);
  showAudioDock = signal<boolean>(true);
  customPfp = signal<string | null>(null);

  // Intro splash and hover animations
  showStartupSplash = signal<boolean>(false);
  isSplashFading = signal<boolean>(false);
  isBrandHovered = signal<boolean>(false);

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
    this.showAudioDock.set(localStorage.getItem('studyflow-show-audio-dock') !== 'false');
    this.customPfp.set(localStorage.getItem('studyflow-custom-pfp'));

    // Purge stale service workers in native desktop/Tauri shells
    if (typeof window !== 'undefined' && 'serviceWorker' in navigator && (window.location.hostname === 'tauri.localhost' || !!(window as any).__TAURI_INTERNALS__ || !!(window as any).__TAURI__)) {
      navigator.serviceWorker.getRegistrations().then(regs => {
        for (const reg of regs) {
          reg.unregister();
        }
      });
      if ('caches' in window) {
        caches.keys().then(keys => {
          for (const key of keys) {
            caches.delete(key);
          }
        });
      }
    }

    // Check if startup splash should play (once per session)
    if (typeof window !== 'undefined' && !sessionStorage.getItem('studyflow-splash-played')) {
      this.showStartupSplash.set(true);
      sessionStorage.setItem('studyflow-splash-played', 'true');
    }

    // Event listeners for real-time reactivity from Settings
    const onPos = (e: any) => this.sidebarPosition.set(e.detail);
    const onPfp = (e: any) => this.customPfp.set(e.detail);
    const onAudio = (e: any) => this.showAudioDock.set(e.detail);
    const onReplay = () => this.replaySplash();

    window.addEventListener('studyflow-sidebar-pos-changed', onPos);
    window.addEventListener('studyflow-pfp-changed', onPfp);
    window.addEventListener('studyflow-audio-dock-toggle', onAudio);
    window.addEventListener('studyflow-replay-splash', onReplay);

    this.eventListeners.push(
      () => window.removeEventListener('studyflow-sidebar-pos-changed', onPos),
      () => window.removeEventListener('studyflow-pfp-changed', onPfp),
      () => window.removeEventListener('studyflow-audio-dock-toggle', onAudio),
      () => window.removeEventListener('studyflow-replay-splash', onReplay)
    );
  }

  dismissSplash() {
    this.isSplashFading.set(true);
    setTimeout(() => {
      this.showStartupSplash.set(false);
      this.isSplashFading.set(false);
    }, 380);
  }

  replaySplash() {
    this.isSplashFading.set(false);
    this.showStartupSplash.set(true);
    setTimeout(() => {
      if (this.splashVideoRef?.nativeElement) {
        this.splashVideoRef.nativeElement.currentTime = 0;
        this.splashVideoRef.nativeElement.play().catch(() => {});
      }
    }, 50);
  }

  onBrandHover(hovered: boolean) {
    this.isBrandHovered.set(hovered);
    if (hovered && this.hoverVideoRef?.nativeElement) {
      this.hoverVideoRef.nativeElement.currentTime = 0;
      this.hoverVideoRef.nativeElement.play().catch(() => {});
    } else if (this.hoverVideoRef?.nativeElement) {
      this.hoverVideoRef.nativeElement.pause();
    }
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
