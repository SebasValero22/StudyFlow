import { Component, OnInit, OnDestroy, ChangeDetectorRef, signal } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ApiService, Task, Exam, LifeArea, Grade, FocusReport } from '../../core/services/api';
import { ModeService } from '../../core/services/mode.service';

export interface CalendarItem {
  date: string;
  type: 'TASK' | 'EXAM';
  title: string;
  subjectColor: string;
  isCompleted: boolean;
  relativeDays: string;
}

export interface GroupedItems {
  [date: string]: CalendarItem[];
}

export type HeroMetricType = 'focus' | 'momentum' | 'radar' | 'gpa';

@Component({
  selector: 'app-overview',
  standalone: true,
  imports: [CommonModule, FormsModule, DatePipe, RouterLink],
  templateUrl: './overview.html',
  styleUrls: ['./overview.scss']
})
export class OverviewComponent implements OnInit, OnDestroy {
  subjects: LifeArea[] = [];
  tasks: Task[] = [];
  exams: Exam[] = [];
  grades: Grade[] = [];
  focusReport: FocusReport | null = null;

  selectedWeeks: number = 2;
  loading = false;
  error = '';
  
  groupedItems: GroupedItems = {};
  gpa: number = 0;
  velocityPercent: number = 0;
  completedTasksCount: number = 0;
  activeTasksCount: number = 0;

  // Option 4: Modular Hero Widget State
  heroMetric = signal<HeroMetricType>('focus');
  isPrivacyMasked = signal<boolean>(false);

  // Focus Activity Metrics (Option 1 - Default)
  todayFocusMinutes: number = 0;
  dailyGoalMinutes: number = 120; // 2 hours target
  todayFocusPercent: number = 0;
  todayFocusFormatted: string = '0m';
  studyStreakDays: number = 0;

  // Task Momentum Metrics (Option 2)
  todayTasksCompletedCount: number = 0;
  todayTasksTotalCount: number = 0;
  todayMomentumPercent: number = 0;
  highPriorityPendingCount: number = 0;

  // Next Milestone Radar Metrics (Option 3)
  nextMilestone: { title: string; daysLeft: number; date: string; subjectName: string; color: string; type: string } | null = null;

  // Mini Heatmap cells (last 84 days = 12 weeks)
  heatmapCells: { date: string; seconds: number; level: number }[] = [];
  totalFocusHours: string = '0';

  private entityCreatedListener?: () => void;

  constructor(
    private api: ApiService, 
    public modeService: ModeService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    const savedHero = localStorage.getItem('studyflow-hero-metric') as HeroMetricType;
    if (savedHero && ['focus', 'momentum', 'radar', 'gpa'].includes(savedHero)) {
      this.heroMetric.set(savedHero);
    } else {
      this.heroMetric.set('focus'); // Focus Activity is the primary default
    }

    this.isPrivacyMasked.set(localStorage.getItem('studyflow-hero-privacy') === 'true');

    this.loadAll();

    const onEntity = () => this.loadAll();
    if (typeof window !== 'undefined') {
      window.addEventListener('studyflow-entity-created', onEntity);
      this.entityCreatedListener = () => window.removeEventListener('studyflow-entity-created', onEntity);
    }
  }

  ngOnDestroy() {
    if (this.entityCreatedListener) {
      this.entityCreatedListener();
    }
  }

  setHeroMetric(type: HeroMetricType) {
    this.heroMetric.set(type);
    localStorage.setItem('studyflow-hero-metric', type);
  }

  togglePrivacyMask() {
    const next = !this.isPrivacyMasked();
    this.isPrivacyMasked.set(next);
    localStorage.setItem('studyflow-hero-privacy', String(next));
  }

  loadAll() {
    this.loading = true;
    this.api.getSubjects().subscribe(data => {
      this.subjects = data;
      this.updateCalendar();
      this.updateMilestoneRadar();
      this.cdr.detectChanges();
    });

    this.api.getTasks().subscribe(data => {
      this.tasks = data;
      this.activeTasksCount = data.filter(t => !t.isCompleted).length;
      this.completedTasksCount = data.filter(t => t.isCompleted).length;
      const total = this.activeTasksCount + this.completedTasksCount;
      this.velocityPercent = total > 0 ? Math.round((this.completedTasksCount / total) * 100) : 0;
      
      this.calculateMomentumMetrics();
      this.updateCalendar();
      this.cdr.detectChanges();
    });

    this.api.getExams().subscribe(data => {
      this.exams = data;
      this.updateCalendar();
      this.updateMilestoneRadar();
      this.cdr.detectChanges();
    });

    this.api.getGrades().subscribe(data => {
      this.grades = data;
      this.calculateGPA();
      this.cdr.detectChanges();
    });

    this.api.getFocusReport().subscribe({
      next: (report) => {
        this.focusReport = report;
        this.totalFocusHours = (report.totalFocusSeconds / 3600).toFixed(1);
        this.calculateFocusMetrics(report);
        this.buildMiniHeatmap(report.byDay || []);
        this.cdr.detectChanges();
      },
      error: () => {}
    });
  }

  calculateFocusMetrics(report: FocusReport) {
    const todayStr = new Date().toISOString().split('T')[0];
    const todayItem = report.byDay?.find(d => d.date === todayStr);
    const todaySecs = todayItem ? todayItem.seconds : 0;
    this.todayFocusMinutes = Math.round(todaySecs / 60);

    const hrs = Math.floor(this.todayFocusMinutes / 60);
    const mins = this.todayFocusMinutes % 60;
    this.todayFocusFormatted = hrs > 0 ? `${hrs}h ${mins}m` : `${mins}m`;
    this.todayFocusPercent = Math.min(100, Math.round((this.todayFocusMinutes / this.dailyGoalMinutes) * 100));

    // Calculate Streak
    const dayMap = new Map<string, number>();
    (report.byDay || []).forEach(d => dayMap.set(d.date, d.seconds));

    let streak = 0;
    const checkDate = new Date();
    // Check from today or yesterday backwards
    const todayHasStudy = (dayMap.get(todayStr) || 0) > 0;
    if (todayHasStudy) streak = 1;

    for (let i = 1; i <= 60; i++) {
      const d = new Date();
      d.setDate(checkDate.getDate() - i);
      const ds = d.toISOString().split('T')[0];
      if ((dayMap.get(ds) || 0) > 0) {
        streak++;
      } else {
        break;
      }
    }
    this.studyStreakDays = streak;
  }

  calculateMomentumMetrics() {
    const todayStr = new Date().toISOString().split('T')[0];
    const todayTasks = this.tasks.filter(t => t.due_date === todayStr);
    this.todayTasksTotalCount = todayTasks.length;
    this.todayTasksCompletedCount = todayTasks.filter(t => t.isCompleted).length;
    this.todayMomentumPercent = this.todayTasksTotalCount > 0 
      ? Math.round((this.todayTasksCompletedCount / this.todayTasksTotalCount) * 100)
      : (this.completedTasksCount > 0 ? 100 : 0);
    this.highPriorityPendingCount = this.tasks.filter(t => !t.isCompleted && t.priority === 'HIGH').length;
  }

  updateMilestoneRadar() {
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const upcoming = this.exams
      .map(e => {
        const ed = new Date(e.examDate);
        ed.setHours(0, 0, 0, 0);
        const diffDays = Math.ceil((ed.getTime() - today.getTime()) / (1000 * 60 * 60 * 24));
        const sub = this.subjects.find(s => s.lifeAreaId === e.lifeAreaId);
        return {
          title: e.nameExam,
          daysLeft: diffDays,
          date: e.examDate,
          subjectName: sub?.name || 'General',
          color: sub?.color || 'var(--accent-primary)',
          type: e.examType
        };
      })
      .filter(item => item.daysLeft >= 0)
      .sort((a, b) => a.daysLeft - b.daysLeft);

    this.nextMilestone = upcoming.length > 0 ? upcoming[0] : null;
  }

  calculateGPA() {
    if (this.grades.length === 0) {
      this.gpa = 0;
      return;
    }
    let totalScore = 0;
    let totalWeight = 0;
    this.grades.forEach(g => {
      totalScore += (g.score * (g.weight / 100));
      totalWeight += (g.weight / 100);
    });
    this.gpa = totalWeight > 0 ? +(totalScore / totalWeight).toFixed(2) : 0;
  }

  getStandingBadge(): { label: string; class: string } {
    if (this.grades.length === 0) return { label: 'No Grades Yet', class: 'neutral' };
    if (this.gpa >= 8.5) return { label: 'High Honors', class: 'excellent' };
    if (this.gpa >= 7.0) return { label: 'Good Standing', class: 'good' };
    if (this.gpa >= 5.0) return { label: 'Passing Standard', class: 'fair' };
    return { label: 'Needs Focus', class: 'warning' };
  }

  buildMiniHeatmap(byDay: { date: string; seconds: number }[]) {
    const dayMap = new Map<string, number>();
    byDay.forEach(d => dayMap.set(d.date, d.seconds));

    const cells: { date: string; seconds: number; level: number }[] = [];
    const today = new Date();

    // 12 weeks = 84 days
    for (let i = 83; i >= 0; i--) {
      const d = new Date();
      d.setDate(today.getDate() - i);
      const dateStr = d.toISOString().split('T')[0];
      const seconds = dayMap.get(dateStr) || 0;

      let level = 0;
      if (seconds > 7200) level = 4;
      else if (seconds > 3600) level = 3;
      else if (seconds > 1800) level = 2;
      else if (seconds > 0) level = 1;

      cells.push({ date: dateStr, seconds, level });
    }
    this.heatmapCells = cells;
  }

  updateCalendar() {
    const today = new Date();
    today.setHours(0,0,0,0);
    const limitDate = new Date();
    limitDate.setDate(today.getDate() + (this.selectedWeeks * 7));
    limitDate.setHours(23,59,59,999);

    const merged: CalendarItem[] = [];

    // Filter and map incomplete Tasks
    this.tasks.filter(t => !t.isCompleted).forEach(t => {
      const d = t.due_date ? new Date(t.due_date) : null;
      if (d && d >= today && d <= limitDate) {
        merged.push({
          date: t.due_date!,
          type: 'TASK',
          title: t.title,
          subjectColor: t.lifeAreaColor || 'var(--accent-primary)',
          isCompleted: t.isCompleted,
          relativeDays: this.getRelativeDayLabel(d, today)
        });
      }
    });

    // Map all Exams
    this.exams.forEach(e => {
      const d = new Date(e.examDate);
      if (d >= today && d <= limitDate) {
        merged.push({
          date: e.examDate,
          type: 'EXAM',
          title: e.nameExam,
          subjectColor: e.lifeAreaColor || 'var(--brand-blue)',
          isCompleted: false,
          relativeDays: this.getRelativeDayLabel(d, today)
        });
      }
    });

    const groups: GroupedItems = {};
    merged.sort((a, b) => a.date.localeCompare(b.date)).forEach(item => {
      if (!groups[item.date]) groups[item.date] = [];
      groups[item.date].push(item);
    });

    this.groupedItems = groups;
  }

  getRelativeDayLabel(targetDate: Date, today: Date): string {
    const diffTime = targetDate.getTime() - today.getTime();
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    if (diffDays === 0) return 'Today';
    if (diffDays === 1) return 'Tomorrow';
    return `In ${diffDays} days`;
  }

  getDates() {
    return Object.keys(this.groupedItems).sort();
  }
}
