import { Injectable, signal, computed } from '@angular/core';

export type AppMode = 'study' | 'work';

@Injectable({
  providedIn: 'root'
})
export class ModeService {
  private modeSignal = signal<AppMode>('study');
  public mode = this.modeSignal.asReadonly();

  public isWorkMode = computed(() => this.modeSignal() === 'work');

  // Dynamic Terminology Engine
  public terms = computed(() => {
    const isWork = this.modeSignal() === 'work';
    return {
      mode: this.modeSignal(),
      subject: isWork ? 'Project' : 'Subject',
      subjects: isWork ? 'Projects' : 'Subjects',
      exam: isWork ? 'Milestone' : 'Exam',
      exams: isWork ? 'Deadlines & Releases' : 'Exams',
      grade: isWork ? 'Deliverable' : 'Grade',
      grades: isWork ? 'Deliverables & OKRs' : 'Grades',
      gpaOrVelocity: isWork ? 'Sprint Velocity' : 'Cumulative GPA',
      gpaScaleOrStatus: isWork ? 'Sprint SLA Completion Rate' : 'Weighted Scale (0.00 – 10.00)',
      yearOrCode: isWork ? 'Client / Billing Code' : 'Academic Year',
      typeOrMilestone: isWork ? 'Deployment / Sprint Type' : 'Assessment Type',
      roomOrEnv: isWork ? 'Repo / Environment' : 'Classroom / Lab',
      timerBadge: isWork ? 'Deep Work Tracker' : 'Animedoro Focus Engine',
      taskContext: isWork ? 'Client Project' : 'Subject Area',
      emptyStateAdvice: isWork 
        ? 'No active milestones. Use + Create to set a client deliverable or sprint release.' 
        : 'No upcoming exams. Schedule coursework milestones to track countdowns.'
    };
  });

  constructor() {
    const saved = localStorage.getItem('studyflow-app-mode') as AppMode;
    if (saved === 'work' || saved === 'study') {
      this.modeSignal.set(saved);
    }
  }

  isWorkProject(item: { academicYear?: string | null }): boolean {
    if (!item || !item.academicYear) return false;
    const y = item.academicYear.trim().toUpperCase();
    return y.startsWith('WORK') || y.startsWith('PRJ') || y === 'PROJECT' || y.startsWith('CLIENT');
  }

  toggleMode(): AppMode {
    const next: AppMode = this.modeSignal() === 'study' ? 'work' : 'study';
    this.setMode(next);
    return next;
  }

  setMode(newMode: AppMode) {
    this.modeSignal.set(newMode);
    localStorage.setItem('studyflow-app-mode', newMode);
    if (typeof window !== 'undefined') {
      window.dispatchEvent(new CustomEvent('studyflow-mode-changed', { detail: newMode }));
    }
  }
}
