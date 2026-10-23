import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService, Grade, LifeArea } from '../../core/services/api';
import { ModeService } from '../../core/services/mode.service';

@Component({
  selector: 'app-grades',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './grades.html',
  styleUrls: ['./grades.scss']
})
export class GradesComponent implements OnInit, OnDestroy {
  grades: Grade[] = [];
  subjects: LifeArea[] = [];
  selectedLifeAreaId: number | null = null;
  loading = false;
  error = '';
  isEditing = false;

  newGrade: Grade = {
    score: 8.5,
    weight: 20
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
    
    this.api.getGrades().subscribe({
      next: (grades) => {
        this.grades = grades;
        this.cdr.detectChanges();
      },
      error: () => this.error = 'Failed to load grades'
    });
  }

  openCreateModal() {
    if (typeof window !== 'undefined') {
      window.dispatchEvent(new CustomEvent('open-studyflow-command-modal', { detail: { tab: 'grade' } }));
    }
  }

  addGrade() {
    if (this.newGrade.score < 0 || !this.selectedLifeAreaId) return;
    const payload: Grade = { ...this.newGrade, lifeArea: { lifeAreaId: this.selectedLifeAreaId } };
    this.api.addGrade(payload).subscribe({
      next: () => {
        this.loadData();
        this.resetNewGrade();
      },
      error: () => this.error = 'Failed to add record'
    });
  }

  editGrade(grade: Grade) {
    this.isEditing = true;
    this.newGrade = { ...grade };
    this.selectedLifeAreaId = grade.lifeArea?.lifeAreaId ?? null;
  }

  cancelEdit() {
    this.isEditing = false;
    this.resetNewGrade();
  }

  updateGrade() {
    if (!this.newGrade.gradeId) return;
    const payload: Grade = { ...this.newGrade };
    if (this.selectedLifeAreaId) {
      payload.lifeArea = { lifeAreaId: this.selectedLifeAreaId };
    }
    this.api.updateGrade(this.newGrade.gradeId, payload).subscribe({
      next: () => {
        this.isEditing = false;
        this.loadData();
        this.resetNewGrade();
      },
      error: () => this.error = 'Failed to update record'
    });
  }

  deleteGrade(id: number) {
    if (!confirm('Delete this assessment record?')) return;
    this.api.deleteGrade(id).subscribe({
      next: () => this.loadData(),
      error: () => this.error = 'Failed to delete record'
    });
  }

  resetNewGrade() {
    this.newGrade = { score: 8.5, weight: 20 };
    this.selectedLifeAreaId = null;
  }

  calculateGPA(): number {
    if (this.grades.length === 0) return 0;
    let totalScore = 0;
    let totalWeight = 0;
    this.grades.forEach(g => {
      totalScore += (g.score * (g.weight / 100));
      totalWeight += (g.weight / 100);
    });
    return totalWeight > 0 ? +(totalScore / totalWeight).toFixed(2) : 0;
  }
}
