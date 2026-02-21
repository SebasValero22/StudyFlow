import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService, Grade, Subject } from '../../core/services/api';

@Component({
  selector: 'app-grades',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './grades.html',
  styleUrls: ['./grades.scss']
})
export class GradesComponent implements OnInit {
  grades: Grade[] = [];
  subjects: Subject[] = [];
  loading = false;
  error = '';
  isEditing = false;

  newGrade: Grade = {
    score: 0,
    weight: 100,
    subjectId: 0
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
    if (this.newGrade.score < 0 || !this.newGrade.subjectId) return;
    this.api.addGrade(this.newGrade).subscribe({
      next: () => {
        this.loadData();
        this.newGrade = { score: 0, weight: 100, subjectId: 0 };
      },
      error: (err) => this.error = 'Failed to add grade'
    });
  }

  editGrade(grade: Grade) {
    this.isEditing = true;
    this.newGrade = { ...grade };
  }

  cancelEdit() {
    this.isEditing = false;
    this.newGrade = { score: 0, weight: 100, subjectId: 0 };
  }

  updateGrade() {
    if (!this.newGrade.gradeId) return;
    this.api.updateGrade(this.newGrade.gradeId, this.newGrade).subscribe({
      next: () => {
        this.isEditing = false;
        this.loadData();
        this.newGrade = { score: 0, weight: 100, subjectId: 0 };
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

  getSubjectName(id: number) {
    return this.subjects.find(s => s.subjectId === id)?.nameSubject || 'Unknown';
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
