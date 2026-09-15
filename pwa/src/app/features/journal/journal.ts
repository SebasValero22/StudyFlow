import { Component, OnInit, OnDestroy, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MarkdownModule } from 'ngx-markdown';
import { ApiService, VoiceJournal } from '../../core/services/api';

type RecorderState = 'idle' | 'recording' | 'processing';

@Component({
  selector: 'app-journal',
  standalone: true,
  imports: [CommonModule, FormsModule, MarkdownModule],
  templateUrl: './journal.html',
  styleUrls: ['./journal.scss']
})
export class JournalComponent implements OnInit, OnDestroy {
  entries = signal<VoiceJournal[]>([]);
  loading = signal(false);
  error = signal('');
  recorderState = signal<RecorderState>('idle');
  elapsedSeconds = signal(0);
  latestEntry = signal<VoiceJournal | null>(null);
  textDraft = signal('');
  promoted = signal<Set<string>>(new Set());

  readonly maxDuration = 120; // hard cap: 2 minutes per voice note
  private mediaRecorder: MediaRecorder | null = null;
  private stream: MediaStream | null = null;
  private audioChunks: Blob[] = [];
  private ticker: ReturnType<typeof setInterval> | null = null;

  readonly canSubmitText = computed(() =>
    this.textDraft().trim().length > 0 && this.recorderState() === 'idle'
  );

  constructor(private api: ApiService) {}

  /** Extract Nyra's "- [ ]" action items from a cleaned transcript. */
  parseActions(transcript?: string): string[] {
    if (!transcript) return [];
    return transcript
      .split('\n')
      .map((line) => line.trim().match(/^-\s*\[\s*\]\s*(.+)$/)?.[1]?.trim() ?? '')
      .filter((text) => text.length > 0);
  }

  promoteAction(entryId: number | undefined, text: string) {
    if (!entryId || this.promoted().has(text)) return;
    this.api.promoteJournalAction(entryId, text).subscribe({
      next: () => {
        this.promoted.update((set) => new Set(set).add(text));
      },
      error: () => this.error.set('Could not create task from action item')
    });
  }


  ngOnInit() {
    this.loadHistory();
  }

  loadHistory() {
    this.loading.set(true);
    this.api.getJournal().subscribe({
      next: (entries) => {
        this.entries.set(entries);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Failed to load journal history');
        this.loading.set(false);
      }
    });
  }

  /** Release mic + timers even if the user navigates away mid-recording. */
  ngOnDestroy() {
    if (this.ticker) clearInterval(this.ticker);
    if (this.mediaRecorder && this.mediaRecorder.state === 'recording') {
      this.mediaRecorder.onstop = null;
      this.mediaRecorder.stop();
    }
    this.stream?.getTracks().forEach((t) => t.stop());
    this.stream = null;
  }

  async toggleRecording() {
    if (this.recorderState() === 'recording') {
      this.stopRecording();
      return;
    }

    try {
      this.stream = await navigator.mediaDevices.getUserMedia({ audio: true });
    } catch {
      this.error.set('Microphone access denied. Check browser permissions.');
      return;
    }

    this.audioChunks = [];
    this.mediaRecorder = new MediaRecorder(this.stream);
    this.mediaRecorder.ondataavailable = (e) => this.audioChunks.push(e.data);
    this.mediaRecorder.onstop = () => this.handleRecordingComplete();

    this.mediaRecorder.start();
    this.recorderState.set('recording');
    this.error.set('');
    this.elapsedSeconds.set(0);
    this.ticker = setInterval(() => {
      const next = this.elapsedSeconds() + 1;
      this.elapsedSeconds.set(next);
      if (next >= this.maxDuration) this.stopRecording();
    }, 1000);
  }

  stopRecording() {
    if (this.ticker) clearInterval(this.ticker);
    this.ticker = null;
    this.mediaRecorder?.stop();
  }

  private handleRecordingComplete() {
    this.stream?.getTracks().forEach((t) => t.stop());
    this.stream = null;

    if (!this.audioChunks.length || this.audioChunks.every((c) => c.size === 0)) {
      this.error.set('Recording was empty. Try again.');
      this.recorderState.set('idle');
      return;
    }

    const blob = new Blob(this.audioChunks, { type: this.mediaRecorder?.mimeType || 'audio/webm' });
    this.recorderState.set('processing');

    const form = new FormData();
    form.append('audio', blob, 'voice-note.webm');
    form.append('durationSeconds', String(this.elapsedSeconds()));

    this.api.ingestJournal(form).subscribe({
      next: (entry) => {
        this.latestEntry.set(entry);
        this.entries.update((list) => [entry, ...list]);
        this.recorderState.set('idle');
        this.elapsedSeconds.set(0);
      },
      error: () => {
        this.error.set('Failed to process your voice note. Try again.');
        this.recorderState.set('idle');
      }
    });
  }

  submitTextEntry() {
    const text = this.textDraft().trim();
    if (!text || this.recorderState() !== 'idle') return;

    this.recorderState.set('processing');
    const form = new FormData();
    form.append('text', text);

    this.api.ingestJournal(form).subscribe({
      next: (entry) => {
        this.latestEntry.set(entry);
        this.entries.update((list) => [entry, ...list]);
        this.textDraft.set('');
        this.recorderState.set('idle');
      },
      error: () => {
        this.error.set('Failed to save your entry. Try again.');
        this.recorderState.set('idle');
      }
    });
  }

  deleteEntry(id?: number) {
    if (!id) return;
    this.api.deleteJournalEntry(id).subscribe({
      next: () => {
        this.entries.update((list) => list.filter((e) => e.entryId !== id));
        if (this.latestEntry()?.entryId === id) this.latestEntry.set(null);
      },
      error: () => this.error.set('Failed to delete entry')
    });
  }

  moodClass(mood?: string | null): string {
    return mood ? `mood-${mood.toLowerCase()}` : 'mood-unknown';
  }

  formatDate(iso?: string): string {
    return iso ? new Date(iso).toLocaleString() : '';
  }
}
