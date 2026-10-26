import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export interface CustomThemeColors {
  bgPrimary: string;
  bgSurface: string;
  textMain: string;
  textMuted: string;
  sidebarBg: string;
  accentPrimary: string;
  timerFocus: string;
  timerReward: string;
}

export const DEFAULT_CUSTOM_THEME: CustomThemeColors = {
  bgPrimary: '#1a1d24',
  bgSurface: '#222630',
  textMain: '#f1f5f9',
  textMuted: '#94a3b8',
  sidebarBg: '#13151b',
  accentPrimary: '#1a7fd4',
  timerFocus: '#ef4444',
  timerReward: '#22c55e'
};

@Injectable({
  providedIn: 'root'
})
export class ThemeService {
  private activeTheme = new BehaviorSubject<string>('light');
  public activeTheme$ = this.activeTheme.asObservable();

  constructor() {
    this.initTheme();
  }

  private initTheme() {
    const savedTheme = localStorage.getItem('studyflow-theme') || 'light';
    this.setTheme(savedTheme);
  }

  setTheme(themeName: string, autoAudio: boolean = true) {
    if (themeName === 'light') {
      document.documentElement.removeAttribute('data-theme');
      this.clearCustomStyles();
    } else if (themeName === 'custom') {
      document.documentElement.setAttribute('data-theme', 'custom');
      this.applyCustomTheme(this.getCustomThemeColors());
    } else {
      document.documentElement.setAttribute('data-theme', themeName);
      this.clearCustomStyles();
    }
    localStorage.setItem('studyflow-theme', themeName);
    this.activeTheme.next(themeName);

    if (autoAudio && typeof window !== 'undefined') {
      const audioMap: Record<string, string> = {
        'soft-paper': 'cafe',
        'porcelain-cyan': 'lofi',
        'nordic-snow': 'classical',
        omarchy: 'rain',
        cyberpunk: 'lofi',
        catppuccin: 'rain',
        'lofi-cafe': 'cafe',
        oled: 'brown'
      };
      if (audioMap[themeName]) {
        window.dispatchEvent(new CustomEvent('studyflow-theme-audio', { detail: audioMap[themeName] }));
      }
    }
  }

  getCurrentTheme(): string {
    return this.activeTheme.value;
  }

  getCustomThemeColors(): CustomThemeColors {
    const saved = localStorage.getItem('studyflow-custom-theme');
    if (saved) {
      try {
        return JSON.parse(saved);
      } catch (e) {
        // fallback
      }
    }
    return { ...DEFAULT_CUSTOM_THEME };
  }

  saveCustomTheme(colors: CustomThemeColors) {
    localStorage.setItem('studyflow-custom-theme', JSON.stringify(colors));
    if (this.getCurrentTheme() === 'custom') {
      this.applyCustomTheme(colors);
    }
  }

  applyCustomTheme(colors: CustomThemeColors) {
    const root = document.documentElement;
    root.style.setProperty('--bg-primary', colors.bgPrimary);
    root.style.setProperty('--bg-surface', colors.bgSurface);
    root.style.setProperty('--bg-surface-hover', this.lightenDarkenColor(colors.bgSurface, 15));
    root.style.setProperty('--text-main', colors.textMain);
    root.style.setProperty('--text-muted', colors.textMuted);
    root.style.setProperty('--sidebar-bg', colors.sidebarBg);
    root.style.setProperty('--sidebar-hover', this.lightenDarkenColor(colors.sidebarBg, 15));
    root.style.setProperty('--sidebar-text', colors.textMain);
    root.style.setProperty('--sidebar-text-muted', colors.textMuted);
    root.style.setProperty('--accent-primary', colors.accentPrimary);
    root.style.setProperty('--accent-contrast', '#ffffff');
    root.style.setProperty('--accent-gradient', `linear-gradient(135deg, ${colors.accentPrimary} 0%, #38bdf8 100%)`);
    root.style.setProperty('--border-color', this.lightenDarkenColor(colors.bgSurface, 25));
    root.style.setProperty('--border-hover', this.lightenDarkenColor(colors.bgSurface, 40));
    root.style.setProperty('--timer-focus', colors.timerFocus);
    root.style.setProperty('--timer-reward', colors.timerReward);
  }

  private clearCustomStyles() {
    const root = document.documentElement;
    const props = [
      '--bg-primary', '--bg-surface', '--bg-surface-hover', '--text-main', '--text-muted',
      '--sidebar-bg', '--sidebar-hover', '--sidebar-text', '--sidebar-text-muted',
      '--accent-primary', '--accent-contrast', '--accent-gradient', '--border-color', '--border-hover',
      '--timer-focus', '--timer-reward'
    ];
    props.forEach(p => root.style.removeProperty(p));
  }

  private lightenDarkenColor(col: string, amt: number): string {
    let usePound = false;
    if (col[0] === '#') {
      col = col.slice(1);
      usePound = true;
    }
    const num = parseInt(col, 16);
    let r = ((num >> 16) & 0xFF) + amt;
    if (r > 255) r = 255; else if (r < 0) r = 0;
    let g = ((num >> 8) & 0xFF) + amt;
    if (g > 255) g = 255; else if (g < 0) g = 0;
    let b = (num & 0xFF) + amt;
    if (b > 255) b = 255; else if (b < 0) b = 0;
    return (usePound ? '#' : '') + ((r << 16) | (g << 8) | b).toString(16).padStart(6, '0');
  }
}
