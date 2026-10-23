import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ThemeService, CustomThemeColors, DEFAULT_CUSTOM_THEME } from '../../core/services/theme.service';
import { ApiService } from '../../core/services/api';

export type SettingsTab = 'appearance' | 'profile' | 'voice' | 'timer' | 'sync';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './settings.html',
  styleUrls: ['./settings.scss']
})
export class SettingsComponent implements OnInit {
  activeTab = signal<SettingsTab>('appearance');

  // Appearance
  currentTheme = signal<string>('light');
  themes = [
    { id: 'light', name: 'StudyFlow Signature (Light)', icon: '☀️' },
    { id: 'catppuccin', name: 'Catppuccin (Mocha Dark)', icon: '☕' },
    { id: 'cyberpunk', name: 'Midnight Tokyo (Cyberpunk)', icon: '🌃' },
    { id: 'lofi-cafe', name: 'Matcha Morning (Gruvbox)', icon: '🍵' },
    { id: 'oled', name: 'OLED Pure Dark', icon: '🌑' },
    { id: 'custom', name: 'Custom Theme (Builder)', icon: '🎨' }
  ];

  customColors: CustomThemeColors = { ...DEFAULT_CUSTOM_THEME };
  showCustomBuilder = signal(false);

  // Layout
  sidebarPosition = signal<'left' | 'right'>('left');
  compactDensity = signal<boolean>(false);

  // Profile & Mode
  customPfp = signal<string | null>(null);
  displayName = signal<string>('');
  appMode = signal<'study' | 'work'>('study');

  // Voice Journal
  voiceRecordingMode = signal<'both' | 'transcribe_only' | 'audio_only'>('both');
  maxRecordingDuration = signal<number>(120);

  // Timer
  timerMode = signal<'pomodoro' | 'animedoro' | 'custom'>('animedoro');
  focusMinutes = signal<number>(40);
  breakMinutes = signal<number>(20);
  chimeSound = signal<boolean>(true);

  // Sync
  syncStatus = signal<string>('Ready');
  saveNotification = signal<string>('');

  constructor(
    private themeService: ThemeService,
    private apiService: ApiService
  ) {}

  ngOnInit() {
    this.themeService.activeTheme$.subscribe(t => {
      this.currentTheme.set(t);
      this.showCustomBuilder.set(t === 'custom');
    });

    this.customColors = this.themeService.getCustomThemeColors();

    // Load saved settings
    this.sidebarPosition.set((localStorage.getItem('studyflow-sidebar-pos') as 'left' | 'right') || 'left');
    this.compactDensity.set(localStorage.getItem('studyflow-compact-density') === 'true');
    this.customPfp.set(localStorage.getItem('studyflow-custom-pfp'));
    
    const user = this.apiService.getCurrentUser();
    this.displayName.set(user?.name || localStorage.getItem('studyflow-display-name') || '');
    this.appMode.set((localStorage.getItem('studyflow-app-mode') as 'study' | 'work') || 'study');

    this.voiceRecordingMode.set(
      (localStorage.getItem('studyflow-voice-mode') as 'both' | 'transcribe_only' | 'audio_only') || 'both'
    );
    this.maxRecordingDuration.set(
      parseInt(localStorage.getItem('studyflow-max-recording') || '120', 10)
    );

    this.timerMode.set(
      (localStorage.getItem('studyflow-timer-mode') as 'pomodoro' | 'animedoro' | 'custom') || 'animedoro'
    );
    this.focusMinutes.set(parseInt(localStorage.getItem('studyflow-focus-mins') || '40', 10));
    this.breakMinutes.set(parseInt(localStorage.getItem('studyflow-break-mins') || '20', 10));
    this.chimeSound.set(localStorage.getItem('studyflow-chime-sound') !== 'false');
  }

  setTab(tab: SettingsTab) {
    this.activeTab.set(tab);
  }

  setTheme(themeId: string) {
    this.themeService.setTheme(themeId);
    this.showCustomBuilder.set(themeId === 'custom');
    this.flashNotification('Theme updated');
  }

  saveCustomColors() {
    this.themeService.saveCustomTheme(this.customColors);
    this.setTheme('custom');
    this.flashNotification('Custom theme colors saved and applied!');
  }

  resetCustomColors() {
    this.customColors = { ...DEFAULT_CUSTOM_THEME };
    this.saveCustomColors();
  }

  setSidebarPosition(pos: 'left' | 'right') {
    this.sidebarPosition.set(pos);
    localStorage.setItem('studyflow-sidebar-pos', pos);
    window.dispatchEvent(new CustomEvent('studyflow-sidebar-pos-changed', { detail: pos }));
    this.flashNotification(`Sidebar placed on the ${pos}`);
  }

  toggleCompactDensity() {
    const next = !this.compactDensity();
    this.compactDensity.set(next);
    localStorage.setItem('studyflow-compact-density', String(next));
    if (next) {
      document.documentElement.classList.add('compact-density');
    } else {
      document.documentElement.classList.remove('compact-density');
    }
  }

  setAppMode(mode: 'study' | 'work') {
    this.appMode.set(mode);
    localStorage.setItem('studyflow-app-mode', mode);
    window.dispatchEvent(new CustomEvent('studyflow-mode-changed', { detail: mode }));
    this.flashNotification(`Switched to ${mode === 'study' ? '🎓 Study Mode' : '💼 Work Mode'}`);
  }

  setVoiceMode(mode: 'both' | 'transcribe_only' | 'audio_only') {
    this.voiceRecordingMode.set(mode);
    localStorage.setItem('studyflow-voice-mode', mode);
    this.flashNotification('Voice journal recording mode saved');
  }

  setMaxDuration(secs: number) {
    this.maxRecordingDuration.set(secs);
    localStorage.setItem('studyflow-max-recording', String(secs));
  }

  setTimerPreset(preset: 'pomodoro' | 'animedoro' | 'custom') {
    this.timerMode.set(preset);
    if (preset === 'pomodoro') {
      this.focusMinutes.set(25);
      this.breakMinutes.set(5);
    } else if (preset === 'animedoro') {
      this.focusMinutes.set(40);
      this.breakMinutes.set(20);
    }
    localStorage.setItem('studyflow-timer-mode', preset);
    localStorage.setItem('studyflow-focus-mins', String(this.focusMinutes()));
    localStorage.setItem('studyflow-break-mins', String(this.breakMinutes()));
    this.flashNotification(`Timer preset set to ${preset}`);
  }

  onTimerDurationsChanged() {
    localStorage.setItem('studyflow-timer-mode', 'custom');
    this.timerMode.set('custom');
    localStorage.setItem('studyflow-focus-mins', String(this.focusMinutes()));
    localStorage.setItem('studyflow-break-mins', String(this.breakMinutes()));
  }

  toggleChimeSound() {
    const next = !this.chimeSound();
    this.chimeSound.set(next);
    localStorage.setItem('studyflow-chime-sound', String(next));
  }

  onPfpSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      const file = input.files[0];
      if (file.size > 2 * 1024 * 1024) {
        alert('Profile picture must be under 2MB.');
        return;
      }
      const reader = new FileReader();
      reader.onload = (e) => {
        const base64 = e.target?.result as string;
        this.customPfp.set(base64);
        localStorage.setItem('studyflow-custom-pfp', base64);
        window.dispatchEvent(new CustomEvent('studyflow-pfp-changed', { detail: base64 }));
        this.flashNotification('Profile picture updated!');
      };
      reader.readAsDataURL(file);
    }
  }

  removePfp() {
    this.customPfp.set(null);
    localStorage.removeItem('studyflow-custom-pfp');
    window.dispatchEvent(new CustomEvent('studyflow-pfp-changed', { detail: null }));
    this.flashNotification('Profile picture reset to default avatar');
  }

  saveDisplayName() {
    localStorage.setItem('studyflow-display-name', this.displayName());
    this.flashNotification('Display name saved');
  }

  triggerSyncNow() {
    this.syncStatus.set('Syncing...');
    this.apiService.triggerSync().then(() => {
      this.syncStatus.set('Synced just now ✓');
      this.flashNotification('Personal Cloud Sync complete');
    }).catch(() => {
      this.syncStatus.set('Sync completed (Offline cache active)');
    });
  }

  exportDataJson() {
    const data = {
      user: this.apiService.getCurrentUser(),
      appMode: this.appMode(),
      theme: this.currentTheme(),
      customTheme: this.customColors,
      exportedAt: new Date().toISOString()
    };
    const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' });
    const a = document.createElement('a');
    a.href = URL.createObjectURL(blob);
    a.download = `studyflow_backup_${new Date().toISOString().split('T')[0]}.json`;
    a.click();
  }

  private flashNotification(msg: string) {
    this.saveNotification.set(msg);
    setTimeout(() => {
      if (this.saveNotification() === msg) {
        this.saveNotification.set('');
      }
    }, 3000);
  }
}
