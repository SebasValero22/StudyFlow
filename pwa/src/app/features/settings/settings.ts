import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { forkJoin, firstValueFrom, of, catchError } from 'rxjs';
import { ThemeService, CustomThemeColors, DEFAULT_CUSTOM_THEME } from '../../core/services/theme.service';
import { ApiService } from '../../core/services/api';
import { GDriveService } from '../../core/services/gdrive.service';

export type SettingsTab = 'appearance' | 'profile' | 'ai' | 'voice' | 'timer' | 'sync';

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
    { id: 'omarchy', name: 'Omarchy Emerald Core', tag: 'DHH Flagship', icon: '🌿', preview: ['#090d0b', '#111a14', '#10b981', '#e8fbf2'] },
    { id: 'cyberpunk', name: 'Tokyo Night', tag: 'Modern Cyberpunk', icon: '🌃', preview: ['#1a1b26', '#24283b', '#7aa2f7', '#c0caf5'] },
    { id: 'catppuccin', name: 'Catppuccin Mocha', tag: 'Pastel Dark', icon: '☕', preview: ['#181825', '#1e1e2e', '#cba6f7', '#cdd6f4'] },
    { id: 'lofi-cafe', name: 'Matcha Morning', tag: 'Gruvbox Warm', icon: '🍵', preview: ['#1d2021', '#282828', '#fabd2f', '#ebdbb2'] },
    { id: 'oled', name: 'OLED Monolith', tag: 'Stealth Black', icon: '🌑', preview: ['#000000', '#0a0a0c', '#38bdf8', '#f4f4f5'] },
    { id: 'light', name: 'StudyFlow Pro', tag: 'Signature Light', icon: '☀️', preview: ['#f8fafc', '#ffffff', '#1a7fd4', '#0f172a'] },
    { id: 'custom', name: 'Custom Palette', tag: 'Personalized', icon: '🎨', preview: ['#111827', '#1f2937', '#6366f1', '#f9fafb'] }
  ];

  customColors: CustomThemeColors = { ...DEFAULT_CUSTOM_THEME };
  showCustomBuilder = signal(false);

  // Layout
  sidebarPosition = signal<'left' | 'right'>('left');
  compactDensity = signal<boolean>(false);
  showAudioDock = signal<boolean>(true);

  // AI Brain & Copilot
  aiProvider = signal<'groq' | 'gemini' | 'local'>('groq');
  groqApiKey = signal<string>('');
  geminiApiKey = signal<string>('');
  ollamaUrl = signal<string>('http://localhost:11434');
  ollamaModel = signal<string>('llama3.2');
  showGroqKey = signal<boolean>(false);
  showGeminiKey = signal<boolean>(false);
  aiPingStatus = signal<string>('');
  isPingingAi = signal<boolean>(false);

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

  // Sync & Cloud Storage
  syncStatus = signal<string>('Ready');
  saveNotification = signal<string>('');
  storageProvider = signal<'aiven' | 'gdrive' | 'local'>('aiven');

  constructor(
    private themeService: ThemeService,
    private apiService: ApiService,
    public gdrive: GDriveService
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
    this.showAudioDock.set(localStorage.getItem('studyflow-show-audio-dock') !== 'false');
    this.customPfp.set(localStorage.getItem('studyflow-custom-pfp'));
    const savedProvider = localStorage.getItem('studyflow-storage-provider') as 'aiven' | 'gdrive' | 'local' | null;
    if (savedProvider) {
      this.storageProvider.set(savedProvider);
    }

    this.aiProvider.set((localStorage.getItem('studyflow-ai-provider') as 'groq' | 'gemini' | 'local') || 'groq');
    this.groqApiKey.set(localStorage.getItem('studyflow-groq-key') || '');
    this.geminiApiKey.set(localStorage.getItem('studyflow-gemini-key') || '');
    this.ollamaUrl.set(localStorage.getItem('studyflow-ollama-url') || 'http://localhost:11434');
    this.ollamaModel.set(localStorage.getItem('studyflow-ollama-model') || 'llama3.2');
    
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

  toggleAudioDock() {
    const next = !this.showAudioDock();
    this.showAudioDock.set(next);
    localStorage.setItem('studyflow-show-audio-dock', String(next));
    window.dispatchEvent(new CustomEvent('studyflow-audio-dock-toggle', { detail: next }));
    this.flashNotification(`Ambient audio dock ${next ? 'enabled' : 'hidden'}`);
  }

  setAiProvider(provider: 'groq' | 'gemini' | 'local') {
    this.aiProvider.set(provider);
    localStorage.setItem('studyflow-ai-provider', provider);
    this.flashNotification(`Active AI Engine set to ${provider.toUpperCase()}`);
  }

  saveGroqKey() {
    localStorage.setItem('studyflow-groq-key', this.groqApiKey());
    this.flashNotification('Groq API Key saved successfully! ✓');
  }

  saveGeminiKey() {
    localStorage.setItem('studyflow-gemini-key', this.geminiApiKey());
    this.flashNotification('Gemini API Key saved successfully! ✓');
  }

  saveOllamaConfig() {
    localStorage.setItem('studyflow-ollama-url', this.ollamaUrl());
    localStorage.setItem('studyflow-ollama-model', this.ollamaModel());
    this.flashNotification('Local Ollama endpoint saved');
  }

  async testAiConnection() {
    this.isPingingAi.set(true);
    this.aiPingStatus.set('Testing connection...');
    const start = Date.now();
    try {
      if (this.aiProvider() === 'groq') {
        const key = this.groqApiKey().trim();
        if (!key) throw new Error('Groq API Key is empty. Paste your key above.');
        const res = await fetch('https://api.groq.com/openai/v1/chat/completions', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${key}`
          },
          body: JSON.stringify({
            model: 'llama-3.3-70b-versatile',
            messages: [{ role: 'user', content: 'Say "Nyra Ready" in 2 words.' }],
            max_tokens: 10
          })
        });
        if (!res.ok) throw new Error(`Groq returned status ${res.status}`);
        const data = await res.json();
        const latency = Date.now() - start;
        this.aiPingStatus.set(`● Groq Connected (${latency}ms): "${data.choices[0].message.content.trim()}"`);
      } else if (this.aiProvider() === 'gemini') {
        const key = this.geminiApiKey().trim();
        if (!key) throw new Error('Gemini API Key is empty. Paste your key above.');
        const res = await fetch(`https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=${key}`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            contents: [{ parts: [{ text: 'Say "Nyra Ready" in 2 words.' }] }]
          })
        });
        if (!res.ok) throw new Error(`Gemini returned status ${res.status}`);
        const data = await res.json();
        const latency = Date.now() - start;
        this.aiPingStatus.set(`● Gemini Connected (${latency}ms): "${data.candidates[0].content.parts[0].text.trim()}"`);
      } else {
        const res = await fetch(`${this.ollamaUrl()}/api/tags`);
        if (!res.ok) throw new Error(`Ollama returned status ${res.status}`);
        const latency = Date.now() - start;
        this.aiPingStatus.set(`● Ollama reachable (${latency}ms) at ${this.ollamaUrl()}`);
      }
    } catch (e: any) {
      this.aiPingStatus.set(`❌ Connection Error: ${e.message || 'Failed'}`);
    } finally {
      this.isPingingAi.set(false);
    }
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

  setStorageProvider(provider: 'aiven' | 'gdrive' | 'local') {
    this.storageProvider.set(provider);
    localStorage.setItem('studyflow-storage-provider', provider);
    this.flashNotification(`Storage provider set to ${provider.toUpperCase()}`);
  }

  connectGoogleDrive() {
    this.gdrive.startOAuthLogin();
  }

  disconnectGoogleDrive() {
    this.gdrive.disconnect();
    this.flashNotification('Disconnected Google Drive');
  }

  private async buildCompleteArchive() {
    const [tasks, subjects, exams, grades, journal, focusReport] = await firstValueFrom(
      forkJoin([
        this.apiService.getTasks().pipe(catchError(() => of([]))),
        this.apiService.getSubjects().pipe(catchError(() => of([]))),
        this.apiService.getExams().pipe(catchError(() => of([]))),
        this.apiService.getGrades().pipe(catchError(() => of([]))),
        this.apiService.getJournal().pipe(catchError(() => of([]))),
        this.apiService.getFocusReport().pipe(catchError(() => of(null)))
      ])
    );

    return {
      studyflowArchiveVersion: '2.0',
      exportedAt: new Date().toISOString(),
      profile: {
        user: this.apiService.getCurrentUser(),
        displayName: this.displayName(),
        appMode: this.appMode(),
        theme: this.currentTheme(),
        storageProvider: this.storageProvider(),
        customTheme: this.customColors
      },
      studyTimeline: {
        totalFocusSeconds: focusReport?.totalFocusSeconds || 0,
        totalFocusHours: focusReport ? +(focusReport.totalFocusSeconds / 3600).toFixed(2) : 0,
        sessionCount: focusReport?.sessionCount || 0,
        studyDates: focusReport?.byDay || []
      },
      tasks,
      subjects,
      exams,
      grades,
      journalNotes: journal
    };
  }

  async syncToGoogleDrive() {
    if (!this.gdrive.isConnected()) {
      this.connectGoogleDrive();
      return;
    }
    this.flashNotification('Uploading StudyFlow vault backup to Google Drive AppFolder...');
    try {
      const archive = await this.buildCompleteArchive();
      const success = await this.gdrive.uploadBackupToDrive(archive);
      if (success) {
        this.flashNotification('Google Drive AppFolder backup complete! ✓');
      } else {
        this.flashNotification('Drive backup upload failed. Check credentials/quota.');
      }
    } catch (e) {
      this.flashNotification('Sync to Google Drive failed.');
    }
  }

  async exportDataJson() {
    this.flashNotification('Packaging complete StudyFlow archive...');
    try {
      const archive = await this.buildCompleteArchive();

      const blob = new Blob([JSON.stringify(archive, null, 2)], { type: 'application/json' });
      const a = document.createElement('a');
      a.href = URL.createObjectURL(blob);
      a.download = `studyflow_full_archive_${new Date().toISOString().split('T')[0]}.json`;
      a.click();
      this.flashNotification('Full archive downloaded successfully!');
    } catch (e) {
      this.flashNotification('Export failed');
    }
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
