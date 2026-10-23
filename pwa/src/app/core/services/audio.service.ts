import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export interface AudioStream {
  id: string;
  name: string;
  type: 'stream' | 'procedural';
  url?: string;
  icon: string;
}

@Injectable({
  providedIn: 'root'
})
export class AudioService {
  private audio: HTMLAudioElement;
  private noiseContext: AudioContext | null = null;
  private noiseNode: AudioBufferSourceNode | null = null;
  private noiseFilter: BiquadFilterNode | null = null;
  private noiseGain: GainNode | null = null;
  
  public streams: AudioStream[] = [
    { id: 'rain', name: 'Rain Ambience', type: 'procedural', icon: '🌧️' },
    { id: 'brown', name: 'Deep Focus Noise', type: 'procedural', icon: '🌊' },
    { id: 'cafe', name: 'Cafe Murmur', type: 'procedural', icon: '☕' },
    { id: 'lofi', name: 'Lofi Chill Radio', type: 'stream', url: 'https://streams.ilovemusic.de/iloveradio17.mp3', icon: '📻' }
  ];

  private currentStreamSubject = new BehaviorSubject<AudioStream>(this.streams[0]);
  public currentStream$ = this.currentStreamSubject.asObservable();

  private isPlayingSubject = new BehaviorSubject<boolean>(false);
  public isPlaying$ = this.isPlayingSubject.asObservable();

  private volumeSubject = new BehaviorSubject<number>(
    parseFloat(localStorage.getItem('studyflow-audio-volume') || '0.5')
  );
  public volume$ = this.volumeSubject.asObservable();

  constructor() {
    this.audio = new Audio();
    this.audio.loop = true;
    this.audio.volume = this.volumeSubject.value;

    this.audio.addEventListener('play', () => this.isPlayingSubject.next(true));
    this.audio.addEventListener('pause', () => {
      if (this.currentStreamSubject.value.type === 'stream') {
        this.isPlayingSubject.next(false);
      }
    });

    const savedStreamId = localStorage.getItem('studyflow-audio-stream');
    if (savedStreamId) {
      const match = this.streams.find(s => s.id === savedStreamId);
      if (match) this.currentStreamSubject.next(match);
    }

    if (typeof window !== 'undefined') {
      window.addEventListener('studyflow-theme-audio', (e: any) => {
        const streamId = e.detail;
        if (streamId) {
          const stream = this.streams.find(s => s.id === streamId);
          if (stream) {
            this.currentStreamSubject.next(stream);
            localStorage.setItem('studyflow-audio-stream', stream.id);
            if (this.isPlayingSubject.value) {
              this.playStream(streamId);
            }
          }
        }
      });
    }
  }

  playStream(streamId: string) {
    const stream = this.streams.find(s => s.id === streamId);
    if (!stream) return;

    this.stopAll();
    this.currentStreamSubject.next(stream);
    localStorage.setItem('studyflow-audio-stream', stream.id);

    if (stream.type === 'procedural') {
      this.startProceduralNoise(stream.id);
      this.isPlayingSubject.next(true);
    } else if (stream.url) {
      this.audio.src = stream.url;
      this.audio.play().catch(e => {
        console.warn('Audio stream playback blocked or failed:', e);
        this.isPlayingSubject.next(false);
      });
    }
  }

  pause() {
    this.stopAll();
    this.isPlayingSubject.next(false);
  }

  togglePlay() {
    if (this.isPlayingSubject.value) {
      this.pause();
    } else {
      this.playStream(this.currentStreamSubject.value.id);
    }
  }

  setVolume(volume: number) {
    this.audio.volume = volume;
    if (this.noiseGain) {
      this.noiseGain.gain.value = volume;
    }
    this.volumeSubject.next(volume);
    localStorage.setItem('studyflow-audio-volume', String(volume));
  }

  private stopAll() {
    this.audio.pause();
    this.stopProceduralNoise();
  }

  private startProceduralNoise(type: string) {
    try {
      const AudioContextClass = window.AudioContext || (window as any).webkitAudioContext;
      if (!AudioContextClass) return;

      this.noiseContext = new AudioContextClass();
      const sampleRate = this.noiseContext.sampleRate;
      const bufferSize = sampleRate * 4; // 4 seconds loop
      const buffer = this.noiseContext.createBuffer(2, bufferSize, sampleRate);

      for (let ch = 0; ch < 2; ch++) {
        const data = buffer.getChannelData(ch);
        let lastOut = 0.0;
        for (let i = 0; i < bufferSize; i++) {
          const white = Math.random() * 2 - 1;
          if (type === 'brown' || type === 'rain') {
            lastOut = (lastOut + (0.02 * white)) / 1.02;
            data[i] = lastOut * 3.5;
          } else {
            // Warm cafe murmurs / pink acoustic
            const b = 0.995 * lastOut + white * 0.05;
            lastOut = b;
            data[i] = b * 2.0;
          }
        }
      }

      this.noiseNode = this.noiseContext.createBufferSource();
      this.noiseNode.buffer = buffer;
      this.noiseNode.loop = true;

      this.noiseFilter = this.noiseContext.createBiquadFilter();
      if (type === 'rain') {
        this.noiseFilter.type = 'lowpass';
        this.noiseFilter.frequency.value = 1100;
        this.noiseFilter.Q.value = 1.0;
      } else if (type === 'brown') {
        this.noiseFilter.type = 'lowpass';
        this.noiseFilter.frequency.value = 380;
        this.noiseFilter.Q.value = 0.4;
      } else {
        // Cafe: warm acoustic bandpass
        this.noiseFilter.type = 'bandpass';
        this.noiseFilter.frequency.value = 650;
        this.noiseFilter.Q.value = 0.7;
      }

      this.noiseGain = this.noiseContext.createGain();
      this.noiseGain.gain.value = this.volumeSubject.value;

      this.noiseNode.connect(this.noiseFilter);
      this.noiseFilter.connect(this.noiseGain);
      this.noiseGain.connect(this.noiseContext.destination);

      this.noiseNode.start(0);
    } catch (err) {
      console.warn('Failed to start procedural audio context:', err);
    }
  }

  private stopProceduralNoise() {
    try {
      if (this.noiseNode) {
        this.noiseNode.stop();
        this.noiseNode.disconnect();
        this.noiseNode = null;
      }
      if (this.noiseContext && this.noiseContext.state !== 'closed') {
        this.noiseContext.close();
        this.noiseContext = null;
      }
    } catch (e) {
      // ignore
    }
  }
}
