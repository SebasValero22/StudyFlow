import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DragDropModule, CdkDragDrop, moveItemInArray, transferArrayItem } from '@angular/cdk/drag-drop';
import { ApiService, Task, LifeArea } from '../../core/services/api';
import { ModeService } from '../../core/services/mode.service';

@Component({
  selector: 'app-tasks',
  standalone: true,
  imports: [CommonModule, FormsModule, DragDropModule],
  templateUrl: './tasks.html',
  styleUrls: ['./tasks.scss']
})
export class TasksComponent implements OnInit, OnDestroy {
  tasks: Task[] = [];
  subjects: LifeArea[] = [];
  selectedSubjectId: number | null = null;
  loading = false;
  error = '';
  isEditing = false;

  backlogTasks: Task[] = [];
  todayTasks: Task[] = [];
  tomorrowTasks: Task[] = [];
  laterTasks: Task[] = [];
  completedTasks: Task[] = [];

  newTask: Task = {
    title: '',
    description: '',
    start_date: new Date().toISOString().split('T')[0],
    due_date: new Date().toISOString().split('T')[0],
    isCompleted: false,
    priority: 'MEDIUM',
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
    this.api.getTasks().subscribe({
      next: (tasks) => {
        this.tasks = tasks;
        this.organizeTasks();
        this.cdr.detectChanges();
      },
      error: () => this.error = 'Failed to load tasks'
    });
    this.api.getInboxTasks().subscribe({
      next: (inbox) => {
        this.backlogTasks = inbox;
        this.cdr.detectChanges();
      }
    });
  }

  get filteredTasks(): Task[] {
    if (!this.selectedSubjectId) return this.tasks;
    return this.tasks.filter(t => t.lifeAreaId === this.selectedSubjectId);
  }

  onFilterChange() {
    this.organizeTasks();
  }

  organizeTasks() {
    this.backlogTasks = [];
    this.todayTasks = [];
    this.tomorrowTasks = [];
    this.laterTasks = [];
    this.completedTasks = [];

    const sorted = [...this.filteredTasks].sort((a,b) => (a.orderIndex || 0) - (b.orderIndex || 0));

    for (const t of sorted) {
      if (t.isCompleted) {
        this.completedTasks.push(t);
        continue;
      }
      if (!t.due_date) {
        this.backlogTasks.push(t);
        continue;
      }
      const days = this.getDaysRemainingNumeric(t.due_date);
      if (days <= 0) this.todayTasks.push(t);
      else if (days === 1) this.tomorrowTasks.push(t);
      else this.laterTasks.push(t);
    }
  }

  getDaysRemainingNumeric(dueDateStr: string): number {
    const today = new Date();
    today.setHours(0,0,0,0);
    const due = new Date(dueDateStr);
    due.setHours(0,0,0,0);
    const diff = due.getTime() - today.getTime();
    return Math.ceil(diff / (1000 * 60 * 60 * 24));
  }

  openCreateModal(columnType: 'today' | 'tomorrow' | 'someday' = 'today') {
    if (typeof window !== 'undefined') {
      window.dispatchEvent(new CustomEvent('open-studyflow-command-modal', { detail: { tab: 'task' } }));
    }
  }

  addTask() {
    if (!this.newTask.title) return;
    this.api.addTask(this.newTask).subscribe({
      next: () => {
        this.loadData();
        this.resetNewTask();
        this.isEditing = false;
      },
      error: () => this.error = 'Failed to add task'
    });
  }

  editTask(task: Task) {
    this.isEditing = true;
    this.newTask = { ...task };
  }

  cancelEdit() {
    this.isEditing = false;
    this.resetNewTask();
  }

  updateTask() {
    if (!this.newTask.taskId) return;
    this.api.updateTask(this.newTask.taskId, this.newTask).subscribe({
      next: () => {
        this.isEditing = false;
        this.loadData();
        this.resetNewTask();
      },
      error: () => this.error = 'Failed to update task'
    });
  }

  deleteTask(id: number | undefined) {
    if (!id) return;
    this.api.deleteTask(id).subscribe({
      next: () => this.loadData(),
      error: () => this.error = 'Failed to delete task'
    });
  }

  resetNewTask() {
    this.newTask = {
      title: '',
      description: '',
      start_date: new Date().toISOString().split('T')[0],
      due_date: new Date().toISOString().split('T')[0],
      isCompleted: false,
      priority: 'MEDIUM',
      lifeAreaId: this.subjects.length > 0 ? (this.subjects[0].lifeAreaId || 0) : 0
    };
  }

  autoPlanWeek() {
    const today = new Date().toISOString().split('T')[0];
    const tomorrowDate = new Date();
    tomorrowDate.setDate(tomorrowDate.getDate() + 1);
    const tomorrow = tomorrowDate.toISOString().split('T')[0];

    this.backlogTasks.forEach(task => {
      if (task.taskId) {
        const newDue = task.priority === 'HIGH' ? today : tomorrow;
        this.api.updateTask(task.taskId, { ...task, due_date: newDue }).subscribe({
          next: () => this.loadData()
        });
      }
    });
  }

  drop(event: CdkDragDrop<Task[]>) {
    if (event.previousContainer === event.container) {
      moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
      this.persistOrder(event.container.data);
    } else {
      transferArrayItem(
        event.previousContainer.data,
        event.container.data,
        event.previousIndex,
        event.currentIndex,
      );

      const movedTask = event.container.data[event.currentIndex];
      const targetListId = event.container.id;

      let newDueDate: string | null = null;
      let newIsCompleted = false;

      const today = new Date();
      const todayStr = today.toISOString().split('T')[0];

      const tomorrow = new Date();
      tomorrow.setDate(today.getDate() + 1);
      const tomorrowStr = tomorrow.toISOString().split('T')[0];

      const later = new Date();
      later.setDate(today.getDate() + 3);
      const laterStr = later.toISOString().split('T')[0];

      if (targetListId.includes('today')) {
        newDueDate = todayStr;
      } else if (targetListId.includes('tomorrow')) {
        newDueDate = tomorrowStr;
      } else if (targetListId.includes('later')) {
        newDueDate = laterStr;
      } else if (targetListId.includes('completed')) {
        newIsCompleted = true;
      } else if (targetListId.includes('backlog')) {
        newDueDate = null;
      }

      movedTask.due_date = newDueDate;
      movedTask.isCompleted = newIsCompleted;

      if (movedTask.taskId) {
        this.api.updateTask(movedTask.taskId, movedTask).subscribe({
          next: () => this.persistOrder(event.container.data)
        });
      }
    }
  }

  private persistOrder(tasks: Task[]) {
    const reorders = tasks
      .filter(t => t.taskId != null)
      .map((t, idx) => ({
        taskId: t.taskId!,
        orderIndex: idx
      }));

    if (reorders.length > 0) {
      this.api.reorderTasks(reorders).subscribe();
    }
  }
}
