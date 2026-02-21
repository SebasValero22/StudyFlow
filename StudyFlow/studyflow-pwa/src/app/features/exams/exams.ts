import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService, Exam, Subject } from '../../core/services/api';

@Component({
  selector: 'app-exams',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './exams.html',
  styleUrls: ['./exams.scss']
})
export class ExamsComponent implements OnInit {
  exams: Exam[] = [];
  subjects: Subject[] = [];
  selectedSubjectId: number | null = null;
  loading = false;
  error = '';
  isEditing = false;

  newExam: Exam = {
    nameExam: '',
    examType: 'EXAM',
    examDate: new Date().toISOString().split('T')[0],
    classroom: '',
    subjectId: 0,
    isCompleted: false
  };

  constructor(private api: ApiService, private cdr: ChangeDetectorRef) {}

  ngOnInit() {
    this.loadData();
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
      error: (err) => this.error = 'Failed to load exams'
    });
  }

  addExam() {
    if (!this.newExam.nameExam || !this.newExam.subjectId) return;
    this.api.addExam(this.newExam).subscribe({
      next: () => {
        this.loadData();
        this.resetNewExam();
      },
      error: (err) => this.error = 'Failed to add exam'
    });
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
      error: (err) => this.error = 'Failed to update exam'
    });
  }

  toggleComplete(exam: Exam) {
    if (!exam.examId) return;
    this.api.completeExam(exam.examId).subscribe({
      next: (updated) => {
        exam.isCompleted = updated.isCompleted;
      },
      error: (err) => this.error = 'Failed to update exam'
    });
  }

  deleteExam(id: number | undefined) {
    if (!id) return;
    this.api.deleteExam(id).subscribe({
      next: () => this.loadData(),
      error: (err) => this.error = 'Failed to delete exam'
    });
  }

  get filteredExams() {
    if (!this.selectedSubjectId) return this.exams;
    return this.exams.filter(e => e.subjectId === +this.selectedSubjectId!);
  }

  getSubjectColor(subjectId: number) {
    return this.subjects.find(s => s.subjectId === subjectId)?.color || '#ccc';
  }

  getDaysRemaining(dateStr: string): string {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const due = new Date(dateStr);
    const diffTime = due.getTime() - today.getTime();
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    
    if (diffDays < 0) return 'Overdue';
    if (diffDays === 0) return 'Today';
    if (diffDays === 1) return 'Tomorrow';
    return `${diffDays} days left`;
  }

  resetNewExam() {
    this.newExam = {
      nameExam: '',
      examType: 'EXAM',
      examDate: new Date().toISOString().split('T')[0],
      classroom: '',
      subjectId: 0,
      isCompleted: false
    };
  }
}
