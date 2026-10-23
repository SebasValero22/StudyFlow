import { Component, OnInit, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AudioService, AudioStream } from '../../../core/services/audio.service';

@Component({
  selector: 'app-audio-player',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './audio-player.html',
  styleUrls: ['./audio-player.scss']
})
export class AudioPlayerComponent implements OnInit {
  @Input() isCompact = false;
  
  streams: AudioStream[] = [];
  currentStream: AudioStream | null = null;
  isPlaying = false;
  volume = 0.5;

  constructor(private audioService: AudioService) {}

  ngOnInit() {
    this.streams = this.audioService.streams;
    
    this.audioService.currentStream$.subscribe(stream => {
      this.currentStream = stream;
    });

    this.audioService.isPlaying$.subscribe(playing => {
      this.isPlaying = playing;
    });

    this.audioService.volume$.subscribe(vol => {
      this.volume = vol;
    });
  }

  togglePlay() {
    this.audioService.togglePlay();
  }

  changeStream(event: Event) {
    const select = event.target as HTMLSelectElement;
    this.audioService.playStream(select.value);
  }

  changeVolume(event: Event) {
    const input = event.target as HTMLInputElement;
    this.audioService.setVolume(parseFloat(input.value));
  }
}
