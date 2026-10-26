import { Component, signal, HostListener, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService, Task, LifeArea, Exam, Grade } from '../../../core/services/api';
import { ModeService } from '../../../core/services/mode.service';

export type CreateEntityType = 'task' | 'subject' | 'exam' | 'grade';

@Component({
  selector: 'app-command-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './command-modal.html',
  styleUrls: ['./command-modal.scss']
})
export class CommandModalComponent implements OnInit {
  isOpen = signal<boolean>(false);
  activeTab = signal<CreateEntityType>('task');
  submitting = signal<boolean>(false);
  successToast = signal<string>('');

  subjects = signal<LifeArea[]>([]);

  // Task Form State
  taskTitle = '';
  taskDesc = '';
  taskPriority: 'LOW' | 'MEDIUM' | 'HIGH' = 'MEDIUM';
  taskLifeAreaId: number | null = null;
  taskDueDate = '';

  // Subject/Project Form State
  subjectName = '';
  subjectCode = '';
  subjectColor = '#10b981';
  pantoneColors = [
    '#10b981', '#3b82f6', '#8b5cf6', '#ec4899', 
    '#f59e0b', '#06b6d4', '#6366f1', '#ef4444'
  ];

  // Exam/Milestone Form State
  examName = '';
  examClassroom = '';
  examDate = '';
  examType = 'EXAM';
  examLifeAreaId: number | null = null;

  // Grade/Deliverable Form State
  gradeScore = 8.5;
  gradeWeight = 20;
  gradeLifeAreaId: number | null = null;

  constructor(
    private api: ApiService,
    public modeService: ModeService
  ) {}

  ngOnInit() {
    this.loadSubjects();
    
    // Listen for custom trigger
    if (typeof window !== 'undefined') {
      window.addEventListener('open-studyflow-command-modal', (e: any) => {
        const defaultTab = e?.detail?.tab || 'task';
        this.open(defaultTab);
      });
    }
  }

  loadSubjects() {
    this.api.getSubjects().subscribe({
      next: (subs) => {
        this.subjects.set(subs);
        if (subs.length > 0) {
          if (!this.taskLifeAreaId) this.taskLifeAreaId = subs[0].lifeAreaId || null;
          if (!this.examLifeAreaId) this.examLifeAreaId = subs[0].lifeAreaId || null;
          if (!this.gradeLifeAreaId) this.gradeLifeAreaId = subs[0].lifeAreaId || null;
        }
      }
    });
  }

  @HostListener('window:keydown', ['$event'])
  handleKeyDown(event: KeyboardEvent) {
    // Open on Ctrl + K or Cmd + K
    if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 'k') {
      event.preventDefault();
      this.toggle();
    }
    // Close on Escape
    if (event.key === 'Escape' && this.isOpen()) {
      this.close();
    }
  }

  open(tab: CreateEntityType = 'task') {
    this.activeTab.set(tab);
    this.isOpen.set(true);
    this.loadSubjects();
  }

  close() {
    this.isOpen.set(false);
  }

  toggle() {
    if (this.isOpen()) {
      this.close();
    } else {
      this.open('task');
    }
  }

  setTab(tab: CreateEntityType) {
    this.activeTab.set(tab);
  }

  getActiveTabEntityName(): string {
    const tab = this.activeTab();
    if (tab === 'task') return this.modeService.isWorkMode() ? 'Issue' : 'Task';
    if (tab === 'subject') return this.modeService.terms().subject;
    if (tab === 'exam') return this.modeService.terms().exam;
    if (tab === 'grade') return this.modeService.terms().grade;
    return 'Item';
  }

  // Quick NLP chip parsing from task title
  onTaskTitleChange(val: string) {
    this.taskTitle = val;
    // Simple tag detector
    if (val.includes('!high')) {
      this.taskPriority = 'HIGH';
      this.taskTitle = this.taskTitle.replace('!high', '').trim();
    } else if (val.includes('!low')) {
      this.taskPriority = 'LOW';
      this.taskTitle = this.taskTitle.replace('!low', '').trim();
    }

    if (val.includes('@today')) {
      this.taskDueDate = new Date().toISOString().split('T')[0];
      this.taskTitle = this.taskTitle.replace('@today', '').trim();
    } else if (val.includes('@tomorrow')) {
      const tomorrow = new Date();
      tomorrow.setDate(tomorrow.getDate() + 1);
      this.taskDueDate = tomorrow.toISOString().split('T')[0];
      this.taskTitle = this.taskTitle.replace('@tomorrow', '').trim();
    }
  }

  submit() {
    this.submitting.set(true);
    const tab = this.activeTab();

    if (tab === 'task') {
      if (!this.taskTitle.trim()) return;
      const payload: Partial<Task> = {
        title: this.taskTitle.trim(),
        description: this.taskDesc.trim() || null,
        priority: this.taskPriority,
        isCompleted: false,
        due_date: this.taskDueDate || null,
        lifeAreaId: this.taskLifeAreaId
      };
      this.api.addTask(payload as Task).subscribe({
        next: () => this.handleSuccess('Task created!'),
        error: () => this.submitting.set(false)
      });
    } else if (tab === 'subject') {
      if (!this.subjectName.trim()) return;
      const isWork = this.modeService.isWorkMode();
      const code = this.subjectCode.trim();
      const payload: Partial<LifeArea> = {
        name: this.subjectName.trim(),
        academicYear: isWork 
          ? (code ? (code.startsWith('PRJ') ? code : 'PRJ: ' + code) : 'PRJ: Client Alpha')
          : (code || '2025/2026'),
        color: this.subjectColor,
        active: true
      };
      this.api.addSubject(payload as LifeArea).subscribe({
        next: () => this.handleSuccess(`${this.modeService.terms().subject} created!`),
        error: () => this.submitting.set(false)
      });
    } else if (tab === 'exam') {
      if (!this.examName.trim() || !this.examLifeAreaId) return;
      const payload: Partial<Exam> = {
        nameExam: this.examName.trim(),
        examType: this.examType,
        examDate: this.examDate || new Date().toISOString().split('T')[0],
        classroom: this.examClassroom.trim() || (this.modeService.isWorkMode() ? 'Production / Cloud' : 'Lab A-101'),
        lifeAreaId: this.examLifeAreaId
      };
      this.api.addExam(payload as Exam).subscribe({
        next: () => this.handleSuccess(`${this.modeService.terms().exam} scheduled!`),
        error: () => this.submitting.set(false)
      });
    } else if (tab === 'grade') {
      if (!this.gradeLifeAreaId) return;
      const payload: Grade = {
        score: +this.gradeScore,
        weight: +this.gradeWeight,
        lifeArea: { lifeAreaId: this.gradeLifeAreaId }
      };
      this.api.addGrade(payload).subscribe({
        next: () => this.handleSuccess('Record logged!'),
        error: () => this.submitting.set(false)
      });
    }
  }

  private handleSuccess(msg: string) {
    this.submitting.set(false);
    this.successToast.set(msg);
    if (typeof window !== 'undefined') {
      window.dispatchEvent(new CustomEvent('studyflow-entity-created', { detail: this.activeTab() }));
    }
    setTimeout(() => {
      this.successToast.set('');
      this.close();
      this.resetForms();
    }, 600);
  }

  private resetForms() {
    this.taskTitle = '';
    this.taskDesc = '';
    this.taskDueDate = '';
    this.subjectName = '';
    this.subjectCode = '';
    this.examName = '';
    this.examClassroom = '';
    this.examDate = '';
  }
}
