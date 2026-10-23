import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService, Task } from '../../core/services/api';
import { ModeService } from '../../core/services/mode.service';

interface Anime {
  id: number;
  title: { romaji: string; english: string };
  coverImage: { large: string };
  episodes: number;
}

@Component({
  selector: 'app-timer',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './timer.html',
  styleUrls: ['./timer.scss']
})
export class TimerComponent implements OnInit, OnDestroy {
  tasks: Task[] = [];
  selectedTaskId: number | null = null;
  
  // Timer State
  minutes: number = 40;
  seconds: number = 0;
  totalDurationSeconds: number = 40 * 60;
  isRunning: boolean = false;
  isFocusMode: boolean = true;
  private timerInterval: any;

  // Work Mode Options
  isBillable: boolean = true;
  preset: '40/20' | '50/10' | '25/5' = '40/20';

  // AniList State
  animeList: Anime[] = [];
  loadingAnime: boolean = false;

  constructor(
    private api: ApiService, 
    public modeService: ModeService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.api.getTasks().subscribe(t => {
      this.tasks = t.filter(task => !task.isCompleted);
      if (this.tasks.length > 0 && !this.selectedTaskId) {
        this.selectedTaskId = this.tasks[0].taskId || null;
      }
      this.cdr.detectChanges();
    });

    if (this.modeService.isWorkMode()) {
      this.setPreset('50/10');
    } else {
      this.setPreset('40/20');
      this.fetchAnimeList();
    }
  }

  get selectedTask(): Task | undefined {
    return this.tasks.find(t => t.taskId === this.selectedTaskId);
  }

  get formattedTime(): string {
    const m = this.minutes < 10 ? '0' + this.minutes : this.minutes;
    const s = this.seconds < 10 ? '0' + this.seconds : this.seconds;
    return `${m}:${s}`;
  }

  get progressFraction(): number {
    const remaining = this.minutes * 60 + this.seconds;
    if (this.totalDurationSeconds <= 0) return 0;
    return (this.totalDurationSeconds - remaining) / this.totalDurationSeconds;
  }

  get strokeDashoffset(): number {
    const circumference = 753.98; // 2 * π * 120
    return circumference * (1 - this.progressFraction);
  }

  setPreset(p: '40/20' | '50/10' | '25/5') {
    if (this.isRunning) return;
    this.preset = p;
    if (p === '40/20') {
      this.minutes = this.isFocusMode ? 40 : 20;
    } else if (p === '50/10') {
      this.minutes = this.isFocusMode ? 50 : 10;
    } else {
      this.minutes = this.isFocusMode ? 25 : 5;
    }
    this.seconds = 0;
    this.totalDurationSeconds = this.minutes * 60;
    this.cdr.detectChanges();
  }

  toggleTimer() {
    this.isRunning = !this.isRunning;
    if (this.isRunning) {
      this.timerInterval = setInterval(() => {
        if (this.seconds === 0) {
          if (this.minutes === 0) {
            this.handleTimerComplete();
          } else {
            this.minutes--;
            this.seconds = 59;
          }
        } else {
          this.seconds--;
        }
        this.cdr.detectChanges();
      }, 1000);
    } else {
      clearInterval(this.timerInterval);
    }
  }

  handleTimerComplete() {
    clearInterval(this.timerInterval);
    this.isRunning = false;

    // Synth chime
    try {
      const ctx = new AudioContext();
      const notes = [660, 880];
      notes.forEach((freq, i) => {
        const osc = ctx.createOscillator();
        const gain = ctx.createGain();
        osc.frequency.value = freq;
        osc.type = 'sine';
        gain.gain.setValueAtTime(0.001, ctx.currentTime + i * 0.18);
        gain.gain.exponentialRampToValueAtTime(0.2, ctx.currentTime + i * 0.18 + 0.02);
        gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + i * 0.18 + 0.35);
        osc.connect(gain).connect(ctx.destination);
        osc.start(ctx.currentTime + i * 0.18);
        osc.stop(ctx.currentTime + i * 0.18 + 0.4);
      });
      setTimeout(() => ctx.close(), 1200);
    } catch (e) {
      console.log('Audio unavailable', e);
    }

    if (this.isFocusMode) {
      const durationSeconds = this.totalDurationSeconds;
      this.api.recordFocusSession({
        taskId: this.selectedTaskId,
        durationSeconds,
        mode: 'FOCUS',
        startedAt: new Date(Date.now() - durationSeconds * 1000).toISOString(),
        completed: true
      }).subscribe();
    }

    // Flip focus / break mode
    this.isFocusMode = !this.isFocusMode;
    this.setPreset(this.preset);
    this.cdr.detectChanges();
  }

  resetTimer() {
    clearInterval(this.timerInterval);
    this.isRunning = false;
    this.setPreset(this.preset);
  }

  ngOnDestroy() {
    if (this.timerInterval) {
      clearInterval(this.timerInterval);
    }
  }

  async fetchAnimeList() {
    this.loadingAnime = true;
    const query = `
      query {
        Page(page: 1, perPage: 6) {
          media(sort: TRENDING_DESC, type: ANIME, isAdult: false) {
            id
            title { romaji english }
            coverImage { large }
            episodes
          }
        }
      }
    `;

    try {
      const response = await fetch('https://graphql.anilist.co', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
        body: JSON.stringify({ query })
      });
      const data = await response.json();
      this.animeList = data.data.Page.media;
    } catch (e) {
      console.error('Failed to fetch from AniList', e);
    } finally {
      this.loadingAnime = false;
      this.cdr.detectChanges();
    }
  }
}
