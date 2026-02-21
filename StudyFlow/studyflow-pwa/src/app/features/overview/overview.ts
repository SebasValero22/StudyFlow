import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule, NgIf, NgFor, NgStyle, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService, Task, Exam, Subject, Grade } from '../../core/services/api';

interface CalendarItem {
  date: string;
  type: 'TASK' | 'EXAM';
  title: string;
  subjectColor: string;
  isCompleted: boolean;
}

interface GroupedItems {
  [date: string]: CalendarItem[];
}

@Component({
  selector: 'app-overview',
  standalone: true,
  imports: [CommonModule, FormsModule, NgIf, NgFor, NgStyle, DatePipe],
  templateUrl: './overview.html',
  styleUrls: ['./overview.scss']
})
export class OverviewComponent implements OnInit {
  subjects: Subject[] = [];
  tasks: Task[] = [];
  exams: Exam[] = [];
  grades: Grade[] = [];
  selectedWeeks: number = 2;
  loading = false;
  error = '';
  
  groupedItems: GroupedItems = {};
  gpa: number = 0;

  constructor(private api: ApiService, private cdr: ChangeDetectorRef) {}

  ngOnInit() {
    this.loadAll();
  }

  loadAll() {
    this.api.getSubjects().subscribe(data => {
      this.subjects = data;
      this.updateCalendar();
      this.cdr.detectChanges();
    });
    this.api.getTasks().subscribe(data => {
      this.tasks = data;
      this.updateCalendar();
      this.cdr.detectChanges();
    });
    this.api.getExams().subscribe(data => {
      this.exams = data;
      this.updateCalendar();
      this.cdr.detectChanges();
    });
    this.api.getGrades().subscribe(data => {
      this.grades = data;
      this.calculateGPA();
      this.cdr.detectChanges();
    });
  }

  calculateGPA() {
    if (this.grades.length === 0) {
      this.gpa = 0;
      return;
    }
    let totalScore = 0;
    let totalWeight = 0;
    this.grades.forEach(g => {
      totalScore += (g.score * (g.weight / 100));
      totalWeight += (g.weight / 100);
    });
    this.gpa = totalWeight > 0 ? +(totalScore / totalWeight).toFixed(2) : 0;
  }

  updateCalendar() {
    const today = new Date();
    today.setHours(0,0,0,0);
    const limitDate = new Date();
    limitDate.setDate(today.getDate() + (this.selectedWeeks * 7));
    limitDate.setHours(23,59,59,999);

    const merged: CalendarItem[] = [];

    // Filter and map incomplete Tasks
    this.tasks.filter(t => !t.isCompleted).forEach(t => {
      const d = new Date(t.due_date);
      if (d >= today && d <= limitDate) {
        merged.push({
          date: t.due_date,
          type: 'TASK',
          title: t.title,
          subjectColor: this.getSubjectColor(t.subjectId),
          isCompleted: t.isCompleted
        });
      }
    });

    // Filter and map incomplete Exams
    this.exams.filter(e => !e.isCompleted).forEach(e => {
      const d = new Date(e.examDate);
      if (d >= today && d <= limitDate) {
        merged.push({
          date: e.examDate,
          type: 'EXAM',
          title: e.nameExam,
          subjectColor: this.getSubjectColor(e.subjectId),
          isCompleted: e.isCompleted
        });
      }
    });

    // Group by date
    const groups: GroupedItems = {};
    merged.sort((a, b) => a.date.localeCompare(b.date)).forEach(item => {
      if (!groups[item.date]) groups[item.date] = [];
      groups[item.date].push(item);
    });

    this.groupedItems = groups;
  }

  getSubjectColor(id: number) {
    return this.subjects.find(s => s.subjectId === id)?.color || '#ccc';
  }

  getDates() {
    return Object.keys(this.groupedItems).sort();
  }
}
