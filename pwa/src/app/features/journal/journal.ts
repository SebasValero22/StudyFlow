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
  liveTranscript = signal<string>('');

  // Audio blob URLs for inline playback
  audioUrlMap = new Map<number, string>();
  latestAudioUrl = signal<string | null>(null);

  maxDuration = 120; // default, loaded from settings
  recordingMode: 'both' | 'transcribe_only' | 'audio_only' = 'both';

  private mediaRecorder: MediaRecorder | null = null;
  private stream: MediaStream | null = null;
  private audioChunks: Blob[] = [];
  private ticker: ReturnType<typeof setInterval> | null = null;
  private speechRecognizer: any = null;

  readonly canSubmitText = computed(() =>
    this.textDraft().trim().length > 0 && this.recorderState() === 'idle'
  );

  constructor(private api: ApiService) {}

  ngOnInit() {
    this.loadSettings();
    this.loadHistory();
  }

  loadSettings() {
    this.maxDuration = parseInt(localStorage.getItem('studyflow-max-recording') || '120', 10);
    this.recordingMode = (localStorage.getItem('studyflow-voice-mode') as any) || 'both';
  }

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

  ngOnDestroy() {
    if (this.ticker) clearInterval(this.ticker);
    this.stopSpeechRecognition();
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
    } catch (err) {
      console.warn('Microphone access issue:', err);
      this.error.set('Microphone access was denied or not detected. Please verify microphone settings.');
      return;
    }

    this.audioChunks = [];
    this.liveTranscript.set('');

    try {
      this.mediaRecorder = new MediaRecorder(this.stream);
    } catch (e) {
      this.error.set('MediaRecorder format not supported on this device.');
      return;
    }

    this.mediaRecorder.ondataavailable = (e) => this.audioChunks.push(e.data);
    this.mediaRecorder.onstop = () => this.handleRecordingComplete();

    this.mediaRecorder.start();
    this.recorderState.set('recording');
    this.error.set('');
    this.elapsedSeconds.set(0);

    // Start native Web Speech recognition in parallel if supported
    this.startSpeechRecognition();

    this.ticker = setInterval(() => {
      const next = this.elapsedSeconds() + 1;
      this.elapsedSeconds.set(next);
      if (next >= this.maxDuration) this.stopRecording();
    }, 1000);
  }

  stopRecording() {
    if (this.ticker) clearInterval(this.ticker);
    this.ticker = null;
    this.stopSpeechRecognition();
    this.mediaRecorder?.stop();
  }

  private startSpeechRecognition() {
    try {
      const SpeechClass = (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition;
      if (SpeechClass) {
        this.speechRecognizer = new SpeechClass();
        this.speechRecognizer.continuous = true;
        this.speechRecognizer.interimResults = true;
        this.speechRecognizer.onresult = (evt: any) => {
          let str = '';
          for (let i = 0; i < evt.results.length; i++) {
            str += evt.results[i][0].transcript + ' ';
          }
          this.liveTranscript.set(str.trim());
        };
        this.speechRecognizer.onerror = () => {};
        this.speechRecognizer.start();
      }
    } catch (e) {
      // Speech recognition optional
    }
  }

  private stopSpeechRecognition() {
    if (this.speechRecognizer) {
      try {
        this.speechRecognizer.stop();
      } catch (e) {}
      this.speechRecognizer = null;
    }
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
    const localAudioUrl = URL.createObjectURL(blob);
    this.latestAudioUrl.set(localAudioUrl);

    this.recorderState.set('processing');

    const form = new FormData();
    form.append('audio', blob, 'voice-note.webm');
    form.append('durationSeconds', String(this.elapsedSeconds()));

    if (this.liveTranscript()) {
      form.append('liveTranscript', this.liveTranscript());
    }

    // Handle recording mode
    if (this.recordingMode === 'audio_only') {
      // Create local voice note directly
      const localEntry: VoiceJournal = {
        entryId: Date.now(),
        title: `Voice Note (${this.elapsedSeconds()}s)`,
        transcript: this.liveTranscript() || `🎙️ Audio recording saved (${this.elapsedSeconds()} seconds).`,
        createdAt: new Date().toISOString(),
        durationSeconds: this.elapsedSeconds()
      };
      this.audioUrlMap.set(localEntry.entryId!, localAudioUrl);
      this.latestEntry.set(localEntry);
      this.entries.update((list) => [localEntry, ...list]);
      this.recorderState.set('idle');
      this.elapsedSeconds.set(0);
      return;
    }

    this.api.ingestJournal(form).subscribe({
      next: (entry) => {
        if (entry.entryId) {
          this.audioUrlMap.set(entry.entryId, localAudioUrl);
        }
        this.latestEntry.set(entry);
        this.entries.update((list) => [entry, ...list]);
        this.recorderState.set('idle');
        this.elapsedSeconds.set(0);
      },
      error: () => {
        // Fallback: If AI is unreachable, save the voice note locally with the live transcript!
        const fallbackEntry: VoiceJournal = {
          entryId: Date.now(),
          title: `Voice Note (${this.elapsedSeconds()}s)`,
          transcript: this.liveTranscript() || `🎙️ Audio note saved (${this.elapsedSeconds()}s). Cloud AI transcription was unavailable.`,
          createdAt: new Date().toISOString(),
          durationSeconds: this.elapsedSeconds()
        };
        this.audioUrlMap.set(fallbackEntry.entryId!, localAudioUrl);
        this.latestEntry.set(fallbackEntry);
        this.entries.update((list) => [fallbackEntry, ...list]);
        this.recorderState.set('idle');
        this.elapsedSeconds.set(0);
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

  getAudioUrl(entryId?: number): string | null {
    if (!entryId) return null;
    return this.audioUrlMap.get(entryId) || null;
  }

  moodClass(mood?: string | null): string {
    return mood ? `mood-${mood.toLowerCase()}` : 'mood-unknown';
  }

  formatDate(iso?: string): string {
    return iso ? new Date(iso).toLocaleString() : '';
  }
}
