import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService, Exam, LifeArea } from '../../core/services/api';
import { ModeService } from '../../core/services/mode.service';

@Component({
  selector: 'app-exams',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './exams.html',
  styleUrls: ['./exams.scss']
})
export class ExamsComponent implements OnInit, OnDestroy {
  exams: Exam[] = [];
  subjects: LifeArea[] = [];
  selectedSubjectId: number | null = null;
  loading = false;
  error = '';
  isEditing = false;

  newExam: Exam = {
    nameExam: '',
    examType: 'EXAM',
    examDate: new Date().toISOString().split('T')[0],
    classroom: '',
    lifeAreaId: 0
  };

  private entityListener?: () => void;

  constructor(
    private api: ApiService, 
    public modeService: ModeService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.loadData();

    const onEntity = () => this.loadData();
    if (typeof window !== 'undefined') {
      window.addEventListener('studyflow-entity-created', onEntity);
      this.entityListener = () => window.removeEventListener('studyflow-entity-created', onEntity);
    }
  }

  ngOnDestroy() {
    if (this.entityListener) {
      this.entityListener();
    }
  }

  loadData() {
    this.api.getSubjects().subscribe(subs => {
      this.subjects = subs;
      this.cdr.detectChanges();
    });
    this.api.getExams().subscribe({
      next: (exams) => {
        this.exams = exams;
        this.cdr.detectChanges();
      },
      error: () => this.error = 'Failed to load milestones'
    });
  }

  openCreateModal() {
    if (typeof window !== 'undefined') {
      window.dispatchEvent(new CustomEvent('open-studyflow-command-modal', { detail: { tab: 'exam' } }));
    }
  }

  editExam(exam: Exam) {
    this.isEditing = true;
    this.newExam = { ...exam };
  }

  cancelEdit() {
    this.isEditing = false;
    this.resetNewExam();
  }

  updateExam() {
    if (!this.newExam.examId) return;
    this.api.updateExam(this.newExam.examId, this.newExam).subscribe({
      next: () => {
        this.isEditing = false;
        this.loadData();
        this.resetNewExam();
      },
      error: () => this.error = 'Failed to update'
    });
  }

  deleteExam(id: number | undefined) {
    if (!id) return;
    if (!confirm(`Delete this ${this.modeService.terms().exam.toLowerCase()}?`)) return;
    this.api.deleteExam(id).subscribe({
      next: () => this.loadData(),
      error: () => this.error = 'Failed to delete'
    });
  }

  get filteredExams() {
    if (!this.selectedSubjectId) return this.exams;
    return this.exams.filter(e => e.lifeAreaId === +this.selectedSubjectId!);
  }

  getSubjectColor(lifeAreaId?: number | null) {
    if (!lifeAreaId) return 'var(--accent-primary)';
    return this.subjects.find(s => s.lifeAreaId === lifeAreaId)?.color || 'var(--accent-primary)';
  }

  getDaysRemaining(dateStr: string): string {
    const today = new Date();
    today.setHours(0,0,0,0);
    const examDate = new Date(dateStr);
    examDate.setHours(0,0,0,0);
    
    const diffTime = examDate.getTime() - today.getTime();
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    
    if (diffDays < 0) return `${Math.abs(diffDays)} days ago (Past)`;
    if (diffDays === 0) return 'Today!';
    if (diffDays === 1) return 'Tomorrow!';
    return `${diffDays} days left`;
  }

  isUrgent(dateStr: string): boolean {
    const today = new Date();
    today.setHours(0,0,0,0);
    const examDate = new Date(dateStr);
    examDate.setHours(0,0,0,0);
    const diffTime = examDate.getTime() - today.getTime();
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    return diffDays >= 0 && diffDays <= 3;
  }

  resetNewExam() {
    this.newExam = {
      nameExam: '',
      examType: 'EXAM',
      examDate: new Date().toISOString().split('T')[0],
      classroom: '',
      lifeAreaId: this.subjects.length > 0 ? (this.subjects[0].lifeAreaId || 0) : 0
    };
  }
}
