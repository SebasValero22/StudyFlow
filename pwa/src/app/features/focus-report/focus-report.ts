import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService, FocusReport } from '../../core/services/api';

@Component({
  selector: 'app-focus-report',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './focus-report.html',
  styleUrls: ['./focus-report.scss']
})
export class FocusReportComponent implements OnInit {
  private api = inject(ApiService);
  
  fromDate: string = '';
  toDate: string = '';
  
  report: FocusReport | null = null;
  loading: boolean = false;
  errorMessage: string = '';

  // Heatmap properties
  heatmapWeeks: number[] = [];
  heatmapDays: number[] = [0, 1, 2, 3, 4, 5, 6]; // Sun to Sat
  heatmapData: Map<string, number> = new Map();
  maxFocusSeconds: number = 0;

  ngOnInit() {
    // Default to last 30 days
    const end = new Date();
    const start = new Date();
    start.setDate(end.getDate() - 30);

    this.toDate = end.toISOString().split('T')[0];
    this.fromDate = start.toISOString().split('T')[0];

    this.loadReport();
  }

  loadReport() {
    this.loading = true;
    this.errorMessage = '';
    this.api.getFocusReport(this.fromDate, this.toDate).subscribe({
      next: (data) => {
        this.report = data;
        this.processHeatmapData();
        this.loading = false;
      },
      error: (err) => {
        console.error('Failed to load focus report', err);
        this.errorMessage = 'Could not load focus data. The cloud API may still be warming up.';
        this.loading = false;
      }
    });
  }

  processHeatmapData() {
    if (!this.report) return;
    
    this.heatmapData.clear();
    this.maxFocusSeconds = 0;
    
    const byDay = this.report.byDay || [];
    for (const entry of byDay) {
      this.heatmapData.set(entry.date, entry.seconds);
      if (entry.seconds > this.maxFocusSeconds) {
        this.maxFocusSeconds = entry.seconds;
      }
    }

    // Generate weeks array based on the date range
    const start = new Date(this.fromDate);
    const end = new Date(this.toDate);
    
    // Adjust start to beginning of the week (Sunday)
    start.setDate(start.getDate() - start.getDay());
    
    const weeks = [];
    let current = new Date(start);
    while (current <= end || current.getDay() !== 0) {
      if (current.getDay() === 0) {
        weeks.push(new Date(current));
      }
      current.setDate(current.getDate() + 1);
    }
    
    this.heatmapWeeks = Array(weeks.length).fill(0).map((_, i) => i);
  }

  getDateForCell(weekIndex: number, dayIndex: number): string {
    const start = new Date(this.fromDate);
    start.setDate(start.getDate() - start.getDay());
    
    const date = new Date(start);
    date.setDate(date.getDate() + (weekIndex * 7) + dayIndex);
    return date.toISOString().split('T')[0];
  }

  getSecondsForCell(weekIndex: number, dayIndex: number): number {
    const dateStr = this.getDateForCell(weekIndex, dayIndex);
    return this.heatmapData.get(dateStr) || 0;
  }
  
  getOpacityForCell(seconds: number): number {
    if (!seconds || !this.maxFocusSeconds) return 0.1;
    return Math.max(0.2, seconds / this.maxFocusSeconds);
  }
  
  getHours(seconds: number): string {
    return (seconds / 3600).toFixed(1);
  }

  getDailyAverage(): string {
    if (!this.report || !this.report.byDay || this.report.byDay.length === 0) return '0.0';
    const totalDays = this.report.byDay.length;
    return ((this.report.totalFocusSeconds || 0) / totalDays / 3600).toFixed(1);
  }

  exportCsv() {
    const token = localStorage.getItem('studyflow-jwt') || '';
    const url = `https://studyflow-nuux.onrender.com/api/focus/timesheet.csv?from=${this.fromDate}&to=${this.toDate}`;
    
    fetch(url, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    .then(res => res.blob())
    .then(blob => {
      const a = document.createElement('a');
      a.href = window.URL.createObjectURL(blob);
      a.download = `focus_timesheet_${this.fromDate}_to_${this.toDate}.csv`;
      a.click();
    })
    .catch(err => console.error('Error downloading CSV', err));
  }
}
