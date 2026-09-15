import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { firstValueFrom } from 'rxjs';
import { ApiService } from '../../services/api';

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
  feedback = signal('');

  private mediaRecorder: MediaRecorder | null = null;
  private stream: MediaStream | null = null;
  private audioChunks: BlobPart[] = [];

  constructor(private apiService: ApiService) {}

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
        // Release the microphone immediately
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
          this.feedback.set(task?.title ? `Captured "${task.title}" to your inbox` : 'Added to your inbox');
        } catch (error) {
          console.error('Error sending audio to Nyra:', error);
          this.feedback.set('Nyra could not process that. Try again.');
        } finally {
          setTimeout(() => this.feedback.set(''), 4000);
          this.isProcessing.set(false);
        }
      };

      this.mediaRecorder.start();
      this.isRecording.set(true);
      this.feedback.set('');
    } catch (error) {
      console.error('Could not start recording:', error);
      this.feedback.set('Microphone access is required.');
    }
  }

  private stopRecording() {
    if (this.mediaRecorder && this.isRecording()) {
      this.mediaRecorder.stop();
      this.isRecording.set(false);
    }
  }
}
