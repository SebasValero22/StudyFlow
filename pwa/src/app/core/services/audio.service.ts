import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export interface AudioStream {
  id: string;
  name: string;
  url: string;
}

@Injectable({
  providedIn: 'root'
})
export class AudioService {
  private audio: HTMLAudioElement;
  
  public streams: AudioStream[] = [
    { id: 'lofi', name: 'Lofi Girl Radio', url: 'https://streams.ilovemusic.de/iloveradio17.mp3' },
    { id: 'rain', name: 'Rain Ambience', url: 'https://cdn.pixabay.com/audio/2021/08/04/audio_3d162a057d.mp3' },
    { id: 'cafe', name: 'Cafe Noise', url: 'https://cdn.pixabay.com/audio/2022/02/10/audio_5594b2a472.mp3' }
  ];

  private currentStreamSubject = new BehaviorSubject<AudioStream | null>(null);
  public currentStream$ = this.currentStreamSubject.asObservable();

  private isPlayingSubject = new BehaviorSubject<boolean>(false);
  public isPlaying$ = this.isPlayingSubject.asObservable();

  private volumeSubject = new BehaviorSubject<number>(0.5);
  public volume$ = this.volumeSubject.asObservable();

  constructor() {
    this.audio = new Audio();
    this.audio.loop = true;
    this.audio.volume = 0.5;

    this.audio.addEventListener('play', () => this.isPlayingSubject.next(true));
    this.audio.addEventListener('pause', () => this.isPlayingSubject.next(false));
  }

  playStream(streamId: string) {
    const stream = this.streams.find(s => s.id === streamId);
    if (!stream) return;

    if (this.audio.src !== stream.url) {
      this.audio.src = stream.url;
      this.currentStreamSubject.next(stream);
    }
    
    this.audio.play().catch(e => console.error('Audio playback failed:', e));
  }

  pause() {
    this.audio.pause();
  }

  togglePlay() {
    if (this.audio.paused) {
      if (!this.audio.src) {
        this.playStream(this.streams[0].id);
      } else {
        this.audio.play();
      }
    } else {
      this.audio.pause();
    }
  }

  setVolume(volume: number) {
    this.audio.volume = volume;
    this.volumeSubject.next(volume);
  }
}
