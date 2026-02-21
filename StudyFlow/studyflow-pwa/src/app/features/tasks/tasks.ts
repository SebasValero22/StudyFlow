import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService, Task, Subject } from '../../core/services/api';

@Component({
  selector: 'app-tasks',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './tasks.html',
  styleUrls: ['./tasks.scss']
})
export class TasksComponent implements OnInit {
  tasks: Task[] = [];
  subjects: Subject[] = [];
  selectedSubjectId: number | null = null;
  loading = false;
  error = '';
  isEditing = false;

  newTask: Task = {
    title: '',
    description: '',
    start_date: new Date().toISOString().split('T')[0],
    due_date: new Date().toISOString().split('T')[0],
    isCompleted: false,
    priority: 'MEDIUM',
    subjectId: 0
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
        this.cdr.detectChanges();
      },
      error: (err) => this.error = 'Failed to load tasks'
    });
  }

  addTask() {
    if (!this.newTask.title || !this.newTask.subjectId) return;
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
      next: () => {},
      error: (err) => {
        task.isCompleted = !task.isCompleted; // Revert on error
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
    return this.tasks.filter(t => t.subjectId === +this.selectedSubjectId!);
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

  resetNewTask() {
    this.newTask = {
      title: '',
      description: '',
      start_date: new Date().toISOString().split('T')[0],
      due_date: new Date().toISOString().split('T')[0],
      isCompleted: false,
      priority: 'MEDIUM',
      subjectId: 0
    };
  }
}
