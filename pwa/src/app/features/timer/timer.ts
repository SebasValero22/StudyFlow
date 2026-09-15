import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService, Task } from '../../core/services/api';

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
  isRunning: boolean = false;
  isFocusMode: boolean = true;
  private timerInterval: any;

  // AniList State
  animeList: Anime[] = [];
  loadingAnime: boolean = false;

  constructor(private api: ApiService) {}

  ngOnInit() {
    this.api.getTasks().subscribe(t => {
      this.tasks = t.filter(task => !task.isCompleted);
    });
    this.fetchAnimeList();
  }

  get formattedTime(): string {
    const m = this.minutes < 10 ? '0' + this.minutes : this.minutes;
    const s = this.seconds < 10 ? '0' + this.seconds : this.seconds;
    return `${m}:${s}`;
  }

  toggleTimer() {
    if (!this.selectedTaskId && this.isFocusMode) {
      alert('Please select a task to focus on first!');
      return;
    }

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
      }, 1000);
    } else {
      clearInterval(this.timerInterval);
    }
  }

  handleTimerComplete() {
    clearInterval(this.timerInterval);
    this.isRunning = false;

    // Synth chime: no binary asset to lose, works offline, zero latency
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

    // Persist the session -> timesheets & analytics (fire-and-forget)
    const durationSeconds = this.isFocusMode ? 2400 : 1200;
    this.api.recordFocusSession({
      taskId: this.selectedTaskId,
      durationSeconds,
      mode: this.isFocusMode ? 'FOCUS' : 'BREAK',
      startedAt: new Date(Date.now() - durationSeconds * 1000).toISOString().slice(0, 19),
      completed: true
    }).subscribe({ error: () => console.warn('Focus session could not be saved') });

    if (this.isFocusMode) {
      this.isFocusMode = false;
      this.minutes = 20; // Reward time
      this.seconds = 0;
      alert('Focus complete! Time for a 20-minute anime break!');
    } else {
      this.isFocusMode = true;
      this.minutes = 40; // Focus time
      this.seconds = 0;
      alert('Break is over! Back to work.');
    }
  }

  resetTimer() {
    clearInterval(this.timerInterval);
    this.isRunning = false;
    this.isFocusMode = true;
    this.minutes = 40;
    this.seconds = 0;
  }

  /** Never leave a ticking interval behind on navigation. */
  ngOnDestroy() {
    clearInterval(this.timerInterval);
  }

  async fetchAnimeList() {
    this.loadingAnime = true;
    // Replace "sebas" with actual username if needed, or parameterize
    const query = `
      query {
        Page {
          mediaList(userName: "sebas", status: CURRENT, type: ANIME) {
            media {
              id
              title {
                romaji
                english
              }
              coverImage {
                large
              }
              episodes
            }
          }
        }
      }
    `;

    try {
      const response = await fetch('https://graphql.anilist.co', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json',
        },
        body: JSON.stringify({ query })
      });
      const json = await response.json();
      if (json.data && json.data.Page && json.data.Page.mediaList) {
        this.animeList = json.data.Page.mediaList.map((item: any) => item.media);
      }
    } catch (e) {
      console.error('Failed to fetch from AniList', e);
    } finally {
      this.loadingAnime = false;
    }
  }
}
