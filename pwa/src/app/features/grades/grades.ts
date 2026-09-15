import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService, Grade, LifeArea } from '../../core/services/api';

@Component({
  selector: 'app-grades',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './grades.html',
  styleUrls: ['./grades.scss']
})
export class GradesComponent implements OnInit {
  grades: Grade[] = [];
  subjects: LifeArea[] = [];
  selectedLifeAreaId: number | null = null;
  loading = false;
  error = '';
  isEditing = false;

  newGrade: Grade = {
    score: 0,
    weight: 100
  };

  constructor(private api: ApiService, private cdr: ChangeDetectorRef) {}

  ngOnInit() {
    this.loadData();
  }

  loadData() {
    // Independent loading
    this.api.getSubjects().subscribe(subs => {
      this.subjects = subs;
      this.cdr.detectChanges();
    });
    
    this.api.getGrades().subscribe({
      next: (grades) => {
        this.grades = grades;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.error = 'Failed to load grades';
      }
    });
  }

  addGrade() {
    if (this.newGrade.score < 0 || !this.selectedLifeAreaId) return;
    const payload: Grade = { ...this.newGrade, lifeArea: { lifeAreaId: this.selectedLifeAreaId } };
    this.api.addGrade(payload).subscribe({
      next: () => {
        this.loadData();
        this.resetNewGrade();
      },
      error: (err) => this.error = 'Failed to add grade'
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
      error: (err) => this.error = 'Failed to update grade'
    });
  }

  deleteGrade(id: number) {
    this.api.deleteGrade(id).subscribe({
      next: () => this.loadData(),
      error: (err) => this.error = 'Failed to delete grade'
    });
  }

  resetNewGrade() {
    this.newGrade = { score: 0, weight: 100 };
    this.selectedLifeAreaId = null;
  }

  calculateGPA() {
    if (this.grades.length === 0) return 0;
    let totalScore = 0;
    let totalWeight = 0;
    this.grades.forEach(g => {
      totalScore += (g.score * (g.weight / 100));
      totalWeight += (g.weight / 100);
    });
    return totalWeight > 0 ? (totalScore / totalWeight).toFixed(2) : 0;
  }
}
