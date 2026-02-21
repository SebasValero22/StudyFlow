import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Subject {
  subjectId?: number;
  nameSubject: string;
  color: string;
  userId: number;
  academicYear: string;
  activeSubject: boolean;
}

export interface Task {
  taskId?: number;
  title: string;
  description: string;
  start_date: string;
  due_date: string;
  isCompleted: boolean;
  priority: string;
  subjectId: number;
}

export interface Exam {
  examId?: number;
  nameExam: string;
  examType: string;
  examDate: string;
  classroom: string;
  subjectId: number;
  isCompleted: boolean;
}

export interface Grade {
  gradeId?: number;
  score: number;
  weight: number;
  subjectId: number;
}

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private baseUrl = 'https://studyflow-nuux.onrender.com/api';
  constructor(private http: HttpClient) {}

  // Subjects
  getSubjects(): Observable<Subject[]> {
    return this.http.get<Subject[]>(`${this.baseUrl}/subjects`);
  }
  addSubject(subject: Subject): Observable<Subject> {
    return this.http.post<Subject>(`${this.baseUrl}/subjects`, subject);
  }
  updateSubject(id: number, subject: Subject): Observable<Subject> {
    return this.http.put<Subject>(`${this.baseUrl}/subjects/${id}`, subject);
  }
  deleteSubject(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/subjects/${id}`);
  }

  // Tasks
  getTasks(): Observable<Task[]> {
    return this.http.get<Task[]>(`${this.baseUrl}/tasks`);
  }
  addTask(task: Task): Observable<Task> {
    // In Angular, we send the fields as defined in the interface
    return this.http.post<Task>(`${this.baseUrl}/tasks`, task);
  }
  updateTask(id: number, task: Task): Observable<Task> {
    return this.http.put<Task>(`${this.baseUrl}/tasks/${id}`, task);
  }
  deleteTask(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/tasks/${id}`);
  }

  // Exams
  getExams(): Observable<Exam[]> {
    return this.http.get<Exam[]>(`${this.baseUrl}/exams`);
  }
  addExam(exam: Exam): Observable<Exam> {
    return this.http.post<Exam>(`${this.baseUrl}/exams`, exam);
  }
  updateExam(id: number, exam: Exam): Observable<Exam> {
    return this.http.put<Exam>(`${this.baseUrl}/exams/${id}`, exam);
  }
  completeExam(id: number): Observable<Exam> {
    return this.http.patch<Exam>(`${this.baseUrl}/exams/${id}/complete`, {});
  }
  deleteExam(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/exams/${id}`);
  }

  // Grades
  getGrades(): Observable<Grade[]> {
    return this.http.get<Grade[]>(`${this.baseUrl}/grades`);
  }
  addGrade(grade: Grade): Observable<Grade> {
    return this.http.post<Grade>(`${this.baseUrl}/grades`, grade);
  }
  updateGrade(id: number, grade: Grade): Observable<Grade> {
    return this.http.put<Grade>(`${this.baseUrl}/grades/${id}`, grade);
  }
  deleteGrade(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/grades/${id}`);
  }
}
