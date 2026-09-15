import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ThemeService } from '../../core/services/theme.service';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './settings.html',
  styleUrl: './settings.scss',
})
export class SettingsComponent implements OnInit {
  currentTheme: string = 'light';
  themes = [
    { id: 'light', name: 'Default Light' },
    { id: 'cyberpunk', name: 'Cyberpunk 2077' },
    { id: 'lofi-cafe', name: 'Lofi Cafe (Gruvbox)' },
    { id: 'catppuccin', name: 'Catppuccin Macchiato' }
  ];

  constructor(private themeService: ThemeService) {}

  ngOnInit() {
    this.themeService.activeTheme$.subscribe(theme => {
      this.currentTheme = theme;
    });
  }

  setTheme(themeId: string) {
    this.themeService.setTheme(themeId);
  }
}
