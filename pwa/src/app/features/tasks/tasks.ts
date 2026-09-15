import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DragDropModule, CdkDragDrop, moveItemInArray, transferArrayItem } from '@angular/cdk/drag-drop';
import { ApiService, Task, LifeArea } from '../../core/services/api';

@Component({
  selector: 'app-tasks',
  standalone: true,
  imports: [CommonModule, FormsModule, DragDropModule],
  templateUrl: './tasks.html',
  styleUrls: ['./tasks.scss']
})
export class TasksComponent implements OnInit {
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

  constructor(private api: ApiService, private cdr: ChangeDetectorRef) {}

  ngOnInit() {
    this.loadData();
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
      error: (err) => this.error = 'Failed to load tasks'
    });
    this.api.getInboxTasks().subscribe({
      next: (inbox) => {
        this.backlogTasks = inbox;
        this.cdr.detectChanges();
      }
    });
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
        // Skip backlog, it's loaded from getInboxTasks()
        continue;
      }
      const days = this.getDaysRemainingNumeric(t.due_date);
      if (days <= 0) this.todayTasks.push(t);
      else if (days === 1) this.tomorrowTasks.push(t);
      else this.laterTasks.push(t);
    }
  }

  addTask() {
    if (!this.newTask.title || !this.newTask.lifeAreaId) return;
    this.api.addTask(this.newTask).subscribe({
      next: () => {
        this.loadData();
        this.resetNewTask();
      },
      error: (err) => this.error = 'Failed to add task'
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
      error: (err) => this.error = 'Failed to update task'
    });
  }

  toggleComplete(task: Task) {
    if (!task.taskId) return;
    task.isCompleted = !task.isCompleted;
    this.api.updateTask(task.taskId, task).subscribe({
      next: () => this.organizeTasks(),
      error: (err) => {
        task.isCompleted = !task.isCompleted;
        this.error = 'Failed to update task';
      }
    });
  }

  deleteTask(id: number | undefined) {
    if (!id) return;
    this.api.deleteTask(id).subscribe({
      next: () => this.loadData(),
      error: (err) => this.error = 'Failed to delete task'
    });
  }

  get filteredTasks() {
    if (!this.selectedSubjectId) return this.tasks;
    return this.tasks.filter(t => t.lifeAreaId === +this.selectedSubjectId!);
  }

  onFilterChange() {
    this.organizeTasks();
  }

  getSubjectColor(lifeAreaId?: number | null) {
    if (!lifeAreaId) return '#ccc';
    return this.subjects.find(s => s.lifeAreaId === lifeAreaId)?.color || '#ccc';
  }

  getDaysRemainingNumeric(dateStr: string): number {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const due = new Date(dateStr);
    const diffTime = due.getTime() - today.getTime();
    return Math.ceil(diffTime / (1000 * 60 * 60 * 24));
  }

  getDaysRemaining(dateStr: string): string {
    if (!dateStr) return 'Someday';
    const diffDays = this.getDaysRemainingNumeric(dateStr);
    if (diffDays < 0) return 'Overdue';
    if (diffDays === 0) return 'Today';
    if (diffDays === 1) return 'Tomorrow';
    return `${diffDays} days left`;
  }

  resetNewTask() {
    this.newTask = {
      title: '',
      description: '',
      start_date: new Date().toISOString().split('T')[0],
      due_date: new Date().toISOString().split('T')[0],
      isCompleted: false,
      priority: 'MEDIUM',
      lifeAreaId: 0
    };
  }

  drop(event: CdkDragDrop<Task[]>, targetColumn: string) {
    if (event.previousContainer === event.container) {
      moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
    } else {
      transferArrayItem(
        event.previousContainer.data,
        event.container.data,
        event.previousIndex,
        event.currentIndex,
      );
      
      const task = event.container.data[event.currentIndex];
      
      if (targetColumn === 'completed') {
        task.isCompleted = true;
      } else {
        task.isCompleted = false;
        if (targetColumn === 'backlog') {
          task.due_date = '';
        } else if (targetColumn === 'today') {
          task.due_date = new Date().toISOString().split('T')[0];
        } else if (targetColumn === 'tomorrow') {
          const tmr = new Date();
          tmr.setDate(tmr.getDate() + 1);
          task.due_date = tmr.toISOString().split('T')[0];
        }
        if (targetColumn === 'later' && !task.due_date) {
           const later = new Date();
           later.setDate(later.getDate() + 2);
           task.due_date = later.toISOString().split('T')[0];
        }
      }
      
      if (task.taskId) {
        this.api.updateTask(task.taskId, task).subscribe();
      }
    }
    
    // Reorder across columns sequentially
    const allTasks = [...this.backlogTasks, ...this.todayTasks, ...this.tomorrowTasks, ...this.laterTasks, ...this.completedTasks];
    const reorderRequests: {taskId: number, orderIndex: number}[] = [];
    allTasks.forEach((t, i) => {
        t.orderIndex = i;
        if (t.taskId) {
            reorderRequests.push({taskId: t.taskId, orderIndex: i});
        }
    });
    
    if (reorderRequests.length > 0) {
        this.api.reorderTasks(reorderRequests).subscribe();
    }
  }

  autoPlanWeek() {
    this.loading = true;
    this.api.autoScheduleNyra().subscribe({
      next: (res) => {
        alert(res.message + ' Tasks updated: ' + res.tasksUpdated);
        this.loadData();
        this.loading = false;
      },
      error: (err) => {
        alert('Failed to auto-plan week.');
        this.loading = false;
      }
    });
  }
}
