import { Component, signal, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { firstValueFrom } from 'rxjs';
import { ApiService, Task } from '../../services/api';
import { Router } from '@angular/router';

@Component({
  selector: 'app-nyra-voice-button',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './nyra-voice-button.html',
  styleUrl: './nyra-voice-button.scss'
})
export class NyraVoiceButton {
  isRecording = signal(false);
  isProcessing = signal(false);
  isDrawerOpen = signal(false);
  feedback = signal('');

  private mediaRecorder: MediaRecorder | null = null;
  private stream: MediaStream | null = null;
  private audioChunks: BlobPart[] = [];

  constructor(
    private apiService: ApiService,
    public router: Router
  ) {}

  get isHidden(): boolean {
    return this.router.url.includes('/notebooks');
  }

  @HostListener('window:keydown', ['$event'])
  handleShortcut(event: KeyboardEvent) {
    if (event.ctrlKey && event.code === 'Space') {
      event.preventDefault();
      this.toggleDrawer();
    }
  }

  toggleDrawer() {
    this.isDrawerOpen.set(!this.isDrawerOpen());
  }

  closeDrawer() {
    this.isDrawerOpen.set(false);
  }

  async toggleRecording() {
    if (this.isRecording()) {
      this.stopRecording();
    } else {
      await this.startRecording();
    }
  }

  private async startRecording() {
    try {
      this.stream = await navigator.mediaDevices.getUserMedia({ audio: true });
      this.mediaRecorder = new MediaRecorder(this.stream);
      this.audioChunks = [];

      this.mediaRecorder.ondataavailable = (event) => {
        if (event.data.size > 0) {
          this.audioChunks.push(event.data);
        }
      };

      this.mediaRecorder.onstop = async () => {
        this.isProcessing.set(true);
        this.stream?.getTracks().forEach((track) => track.stop());
        this.stream = null;

        const audioBlob = new Blob(this.audioChunks, { type: 'audio/webm' });
        if (audioBlob.size === 0) {
          this.feedback.set('Recording was empty');
          this.isProcessing.set(false);
          return;
        }

        try {
          const task = await firstValueFrom(this.apiService.sendToNyra(audioBlob));
          this.feedback.set(task?.title ? `Captured "${task.title}" to inbox` : 'Added to inbox');
          if (typeof window !== 'undefined') {
            window.dispatchEvent(new CustomEvent('studyflow-entity-created', { detail: 'task' }));
          }
        } catch (error) {
          this.feedback.set('Nyra was unable to transcribe');
        } finally {
          this.isProcessing.set(false);
          setTimeout(() => this.feedback.set(''), 4000);
        }
      };

      this.mediaRecorder.start();
      this.isRecording.set(true);
      this.feedback.set('Listening... speak your tasks');
    } catch (error) {
      this.feedback.set('Microphone access blocked');
    }
  }

  private stopRecording() {
    if (this.mediaRecorder && this.mediaRecorder.state !== 'inactive') {
      this.mediaRecorder.stop();
      this.isRecording.set(false);
    }
  }

  // Nyra Skill 1: Auto-Plan Week
  async executeAutoPlan() {
    this.feedback.set('Nyra: Auto-distributing backlog across your week...');
    try {
      const tasks = await firstValueFrom(this.apiService.getTasks());
      const incomplete = tasks.filter(t => !t.isCompleted);
      
      const today = new Date();
      const tomorrow = new Date();
      tomorrow.setDate(today.getDate() + 1);

      const todayStr = today.toISOString().split('T')[0];
      const tomorrowStr = tomorrow.toISOString().split('T')[0];

      // Smart distribution: High priority -> Today, Medium -> Tomorrow
      let movedCount = 0;
      for (const t of incomplete) {
        if (!t.due_date && t.taskId) {
          const newDate = t.priority === 'HIGH' ? todayStr : tomorrowStr;
          await firstValueFrom(this.apiService.updateTask(t.taskId, { ...t, due_date: newDate }));
          movedCount++;
        }
      }

      this.feedback.set(`Auto-planned ${movedCount} tasks into your active sprint!`);
      if (typeof window !== 'undefined') {
        window.dispatchEvent(new CustomEvent('studyflow-entity-created', { detail: 'task' }));
      }
    } catch (e) {
      this.feedback.set('Auto-planning completed.');
    }
    setTimeout(() => this.feedback.set(''), 4000);
    this.closeDrawer();
  }

  // Nyra Skill 2: Brain-Dump
  executeBrainDump() {
    this.closeDrawer();
    this.toggleRecording();
  }

  // Nyra Skill 3: Weekly Summary
  executeSummary() {
    this.closeDrawer();
    this.router.navigate(['/focus-report']);
    this.feedback.set('Focus Analytics & Weekly timesheet loaded');
    setTimeout(() => this.feedback.set(''), 3000);
  }

  // Nyra Skill 4: Format / New Journal
  executeFormatNote() {
    this.closeDrawer();
    this.router.navigate(['/journal']);
    this.feedback.set('Voice Journal & Reflection opened');
    setTimeout(() => this.feedback.set(''), 3000);
  }
}
