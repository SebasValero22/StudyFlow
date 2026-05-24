import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';

export interface User {
  id?: number;
  userName: string;
  email: string;
}

export interface UserLoginDTO {
  email: string;
  password?: string;
}

export interface UserRegisterDTO {
  name: string;
  email: string;
  password?: string;
}

export interface UserResponseDTO {
  id: number;
  userName: string;
  email: string;
}

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
  private currentUser = signal<UserResponseDTO | null>(null);

  constructor(private http: HttpClient) {
    const savedUser = localStorage.getItem('user');
    if (savedUser) {
      try {
        const userObj = JSON.parse(savedUser);
        // Map from backend response keys (userId, name) if they exist
        const mappedUser: UserResponseDTO = {
          id: userObj.id !== undefined ? userObj.id : userObj.userId,
          userName: userObj.userName !== undefined ? userObj.userName : userObj.name,
          email: userObj.email
        };
        
        // If it was in the backend format, update localStorage immediately to standard client format
        if (userObj.userId !== undefined || userObj.name !== undefined) {
          localStorage.setItem('user', JSON.stringify(mappedUser));
        }
        
        this.currentUser.set(mappedUser);
      } catch (e) {
        console.error('Error parsing saved user:', e);
      }
    }
  }

  // Auth
  login(credentials: UserLoginDTO): Observable<UserResponseDTO> {
    return this.http.post<any>(`${`${this.baseUrl}/users/login`}`, credentials).pipe(
      map(res => {
        const user: UserResponseDTO = {
          id: res.userId,
          userName: res.name,
          email: res.email
        };
        this.currentUser.set(user);
        localStorage.setItem('user', JSON.stringify(user));
        return user;
      })
    );
  }

  register(userData: UserRegisterDTO): Observable<UserResponseDTO> {
    return this.http.post<any>(`${`${this.baseUrl}/users/register`}`, userData).pipe(
      map(res => {
        const user: UserResponseDTO = {
          id: res.userId,
          userName: res.name,
          email: res.email
        };
        this.currentUser.set(user);
        localStorage.setItem('user', JSON.stringify(user));
        return user;
      })
    );
  }

  logout() {
    this.currentUser.set(null);
    localStorage.removeItem('user');
  }

  isLoggedIn() {
    return !!this.currentUser();
  }

  getCurrentUser() {
    return this.currentUser();
  }

  // Subjects
  getSubjects(): Observable<Subject[]> {
    const userId = this.currentUser()?.id || 1;
    return this.http.get<Subject[]>(`${this.baseUrl}/subjects?userId=${userId}`);
  }
  addSubject(subject: Subject): Observable<Subject> {
    const userId = this.currentUser()?.id || 1;
    subject.userId = userId;
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
    const userId = this.currentUser()?.id || 1;
    return this.http.get<Task[]>(`${this.baseUrl}/tasks?userId=${userId}`);
  }
  addTask(task: Task): Observable<Task> {
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
    const userId = this.currentUser()?.id || 1;
    return this.http.get<Exam[]>(`${this.baseUrl}/exams?userId=${userId}`);
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
    const userId = this.currentUser()?.id || 1;
    return this.http.get<any[]>(`${this.baseUrl}/grades?userId=${userId}`).pipe(
      map(grades => grades.map(g => ({
        gradeId: g.gradeId,
        score: g.score,
        weight: g.weight,
        subjectId: g.subject ? g.subject.subjectId : 0
      })))
    );
  }
  addGrade(grade: Grade): Observable<Grade> {
    const payload = {
      score: grade.score,
      weight: grade.weight,
      subject: {
        subjectId: Number(grade.subjectId)
      }
    };
    return this.http.post<any>(`${this.baseUrl}/grades`, payload).pipe(
      map(g => ({
        gradeId: g.gradeId,
        score: g.score,
        weight: g.weight,
        subjectId: g.subject ? g.subject.subjectId : 0
      }))
    );
  }
  updateGrade(id: number, grade: Grade): Observable<Grade> {
    const payload = {
      gradeId: grade.gradeId,
      score: grade.score,
      weight: grade.weight,
      subject: {
        subjectId: Number(grade.subjectId)
      }
    };
    return this.http.put<any>(`${this.baseUrl}/grades/${id}`, payload).pipe(
      map(g => ({
        gradeId: g.gradeId,
        score: g.score,
        weight: g.weight,
        subjectId: g.subject ? g.subject.subjectId : 0
      }))
    );
  }
  deleteGrade(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/grades/${id}`);
  }
}
