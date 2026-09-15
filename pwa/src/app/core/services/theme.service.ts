import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

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

  setTheme(themeName: string) {
    if (themeName === 'light') {
      document.documentElement.removeAttribute('data-theme');
    } else {
      document.documentElement.setAttribute('data-theme', themeName);
    }
    localStorage.setItem('studyflow-theme', themeName);
    this.activeTheme.next(themeName);
  }

  getCurrentTheme(): string {
    return this.activeTheme.value;
  }
}
