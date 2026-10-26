import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService, LifeArea, Task } from '../../core/services/api';
import { ModeService } from '../../core/services/mode.service';

@Component({
  selector: 'app-subjects',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './subjects.html',
  styleUrls: ['./subjects.scss']
})
export class SubjectsComponent implements OnInit, OnDestroy {
  subjects: LifeArea[] = [];
  tasks: Task[] = [];
  
  newSubject: LifeArea = {
    name: '',
    color: '#10b981',
    userId: 0,
    academicYear: '2025/2026',
    active: true
  };
  
  pantoneColors: string[] = [
    '#10b981', '#3b82f6', '#8b5cf6', '#ec4899', 
    '#f59e0b', '#06b6d4', '#6366f1', '#ef4444'
  ];
  
  loading = false;
  error = '';
  isEditing = false;

  private entityListener?: () => void;

  constructor(
    private api: ApiService, 
    public modeService: ModeService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.newSubject.userId = this.api.getCurrentUser()?.userId ?? 1;
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
    this.api.getSubjects().subscribe({
      next: (data) => {
        this.subjects = data;
        this.cdr.detectChanges();
      },
      error: () => this.error = 'Failed to load data'
    });

    this.api.getTasks().subscribe({
      next: (tasks) => {
        this.tasks = tasks;
        this.cdr.detectChanges();
      }
    });
  }

  filterMode: 'mode' | 'all' = 'mode';

  get displayedSubjects(): LifeArea[] {
    if (this.filterMode === 'all') return this.subjects;
    const isWork = this.modeService.isWorkMode();
    return this.subjects.filter(s => {
      const isProject = this.modeService.isWorkProject(s);
      return isWork ? isProject : !isProject;
    });
  }

  toggleFilterMode() {
    this.filterMode = this.filterMode === 'mode' ? 'all' : 'mode';
  }

  getTaskCount(lifeAreaId?: number): number {
    if (!lifeAreaId) return 0;
    return this.tasks.filter(t => t.lifeAreaId === lifeAreaId && !t.isCompleted).length;
  }

  openCreateModal() {
    if (typeof window !== 'undefined') {
      window.dispatchEvent(new CustomEvent('open-studyflow-command-modal', { detail: { tab: 'subject' } }));
    }
  }

  editSubject(subject: LifeArea) {
    this.isEditing = true;
    this.newSubject = { ...subject };
  }

  cancelEdit() {
    this.isEditing = false;
    this.resetNewSubject();
  }

  updateSubject() {
    if (!this.newSubject.lifeAreaId) return;
    this.api.updateSubject(this.newSubject.lifeAreaId, this.newSubject).subscribe({
      next: () => {
        this.isEditing = false;
        this.loadData();
        this.resetNewSubject();
      },
      error: () => this.error = 'Failed to update'
    });
  }

  deleteSubject(id: number | undefined) {
    if (!id) return;
    if (!confirm(`Are you sure you want to delete this ${this.modeService.terms().subject.toLowerCase()}?`)) return;
    this.api.deleteSubject(id).subscribe({
      next: () => this.loadData(),
      error: () => this.error = 'Failed to delete'
    });
  }

  resetNewSubject() {
    this.newSubject = { 
      name: '', 
      color: '#10b981', 
      userId: this.api.getCurrentUser()?.userId ?? 1,
      academicYear: this.modeService.isWorkMode() ? 'PRJ-2026' : '2025/2026',
      active: true
    };
  }
}
