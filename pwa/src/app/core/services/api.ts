import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap, map } from 'rxjs';

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
  userId: number;
  name: string;
  email: string;
}

export interface AuthResponseDTO {
  token: string;
  user: UserResponseDTO;
}

export interface LifeArea {
  lifeAreaId?: number;
  userId: number;
  name: string;
  color: string;
  academicYear: string;
  active: boolean;
}

export interface Task {
  taskId?: number;
  title: string;
  description?: string | null;
  priority?: string;
  isCompleted: boolean;
  orderIndex?: number;
  start_date?: string | null;
  due_date?: string | null;
  lifeAreaId?: number | null;
  lifeAreaName?: string | null;
  lifeAreaColor?: string | null;
}

export interface Exam {
  examId?: number;
  nameExam: string;
  examType: string;
  examDate: string;
  classroom?: string;
  lifeAreaId?: number | null;
  lifeAreaName?: string | null;
  lifeAreaColor?: string | null;
}

export interface Grade {
  gradeId?: number;
  concept?: string | null;
  score: number;
  weight: number;
  gradeDate?: string | null;
  lifeArea?: Partial<LifeArea> | null;
}

export interface VoiceJournal {
  entryId?: number;
  userId?: number;
  taskId?: number | null;
  title?: string;
  moodTag?: 'GREAT' | 'GOOD' | 'NEUTRAL' | 'ROUGH' | 'TERRIBLE' | null;
  rawTranscript?: string;
  transcript?: string;
  audioPath?: string | null;
  durationSeconds?: number | null;
  createdAt?: string;
}

export interface FocusReport {
  totalFocusSeconds: number;
  sessionCount: number;
  byDay: { date: string; seconds: number }[];
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
      this.currentUser.set(JSON.parse(savedUser));
    }
  }

  // Auth
  login(credentials: UserLoginDTO): Observable<UserResponseDTO> {
    return this.http.post<AuthResponseDTO>(`${this.baseUrl}/auth/login`, credentials).pipe(
      tap(res => this.storeSession(res)),
      map(res => res.user)
    );
  }

  register(userData: UserRegisterDTO): Observable<UserResponseDTO> {
    return this.http.post<AuthResponseDTO>(`${this.baseUrl}/auth/register`, userData).pipe(
      tap(res => this.storeSession(res)),
      map(res => res.user)
    );
  }

  logout() {
    this.currentUser.set(null);
    localStorage.removeItem('user');
    localStorage.removeItem('studyflow-jwt');
  }

  isLoggedIn() {
    // Token is the source of truth: the 401 interceptor may have cleared it
    // behind our back, so don't trust the in-memory signal alone.
    return !!this.currentUser() && !!localStorage.getItem('studyflow-jwt');
  }

  getCurrentUser() {
    return this.currentUser();
  }

  private storeSession(res: AuthResponseDTO) {
    localStorage.setItem('studyflow-jwt', res.token);
    localStorage.setItem('user', JSON.stringify(res.user));
    this.currentUser.set(res.user);
  }

  // Life Areas
  getSubjects(): Observable<LifeArea[]> {
    return this.http.get<LifeArea[]>(`${this.baseUrl}/subjects`);
  }
  addSubject(lifeArea: LifeArea): Observable<LifeArea> {
    return this.http.post<LifeArea>(`${this.baseUrl}/subjects`, lifeArea);
  }
  updateSubject(id: number, lifeArea: LifeArea): Observable<LifeArea> {
    return this.http.put<LifeArea>(`${this.baseUrl}/subjects/${id}`, lifeArea);
  }
  deleteSubject(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/subjects/${id}`);
  }

  // Tasks
  getTasks(): Observable<Task[]> {
    return this.http.get<Task[]>(`${this.baseUrl}/tasks`);
  }
  getInboxTasks(): Observable<Task[]> {
    return this.http.get<Task[]>(`${this.baseUrl}/tasks/inbox`);
  }
  addTask(task: Task): Observable<Task> {
    // In Angular, we send the fields as defined in the interface
    return this.http.post<Task>(`${this.baseUrl}/tasks`, task);
  }
  updateTask(id: number, task: Task): Observable<Task> {
    return this.http.put<Task>(`${this.baseUrl}/tasks/${id}`, task);
  }
  reorderTasks(reorderRequests: {taskId: number, orderIndex: number}[]): Observable<any> {
    return this.http.patch(`${this.baseUrl}/tasks/reorder`, reorderRequests);
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

  // Nyra Integration
  sendToNyra(input: string | Blob): Observable<Task> {
    const formData = new FormData();
    
    if (typeof input === 'string') {
      formData.append('text', input);
    } else {
      formData.append('audio', input, 'voice_capture.webm');
    }

    // Our backend expects /api/nyra/ingest
    // Using this.baseUrl which is currently hardcoded or environment based
    // Need to make sure this points to the Spring Boot instance
    return this.http.post<Task>(`${this.baseUrl}/nyra/ingest`, formData);
  }

  autoScheduleNyra(): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/nyra/auto-schedule`, {});
  }

  // Voice Journal
  ingestJournal(form: FormData): Observable<VoiceJournal> {
    return this.http.post<VoiceJournal>(`${this.baseUrl}/journal/ingest`, form);
  }
  getJournal(): Observable<VoiceJournal[]> {
    return this.http.get<VoiceJournal[]>(`${this.baseUrl}/journal`);
  }
  deleteJournalEntry(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/journal/${id}`);
  }
  promoteJournalAction(entryId: number, text: string): Observable<Task> {
    return this.http.post<Task>(`${this.baseUrl}/journal/${entryId}/actions`, { text });
  }

  // Focus sessions (Animedoro -> timesheets)
  recordFocusSession(payload: {
    taskId?: number | null;
    durationSeconds: number;
    mode: 'FOCUS' | 'BREAK';
    startedAt?: string;
    completed?: boolean;
  }): Observable<any> {
    return this.http.post(`${this.baseUrl}/focus/sessions`, payload);
  }
  getFocusReport(from?: string, to?: string): Observable<FocusReport> {
    const params = from && to ? `?from=${from}&to=${to}` : '';
    return this.http.get<FocusReport>(`${this.baseUrl}/focus/report${params}`);
  }
}
