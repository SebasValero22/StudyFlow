import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService, Subject } from '../../core/services/api';

@Component({
  selector: 'app-subjects',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './subjects.html',
  styleUrls: ['./subjects.scss']
})
export class SubjectsComponent implements OnInit {
  subjects: Subject[] = [];
  newSubject: Subject = { 
    nameSubject: '', 
    color: '#3498db', 
    userId: 1,
    academicYear: '2025/2026',
    activeSubject: true
  };
  pantoneColors: string[] = ['#c0392b', '#2980b9', '#27ae60', '#f39c12', '#8e44ad', '#d35400'];
  loading = false;
  error = '';
  isEditing = false;

  constructor(private api: ApiService, private cdr: ChangeDetectorRef) {}

  ngOnInit() {
    this.loadSubjects();
  }

  loadSubjects() {
    this.api.getSubjects().subscribe({
      next: (data) => {
        this.subjects = data;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.error = 'Failed to load subjects';
      }
    });
  }

  addSubject() {
    if (!this.newSubject.nameSubject) return;
    this.api.addSubject(this.newSubject).subscribe({
      next: () => {
        this.loadSubjects();
        this.resetNewSubject();
      },
      error: (err) => this.error = 'Failed to add subject'
    });
  }

  editSubject(subject: Subject) {
    this.isEditing = true;
    this.newSubject = { ...subject };
  }

  cancelEdit() {
    this.isEditing = false;
    this.resetNewSubject();
  }

  updateSubject() {
    if (!this.newSubject.subjectId) return;
    this.api.updateSubject(this.newSubject.subjectId, this.newSubject).subscribe({
      next: () => {
        this.isEditing = false;
        this.loadSubjects();
        this.resetNewSubject();
      },
      error: (err) => this.error = 'Failed to update subject'
    });
  }

  deleteSubject(id: number | undefined) {
    if (!id) return;
    this.api.deleteSubject(id).subscribe({
      next: () => this.loadSubjects(),
      error: (err) => this.error = 'Failed to delete subject'
    });
  }

  resetNewSubject() {
    this.newSubject = { 
      nameSubject: '', 
      color: '#FFB7B2', 
      userId: 1,
      academicYear: '2025/2026',
      activeSubject: true
    };
  }
}
