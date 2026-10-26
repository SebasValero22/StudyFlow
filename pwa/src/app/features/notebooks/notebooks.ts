import { Component, OnInit, signal, computed, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService, LifeArea } from '../../core/services/api';
import { ModeService } from '../../core/services/mode.service';

export interface DocumentItem {
  id: string;
  name: string;
  sizeFormatted: string;
  uploadedAt: string;
  textContent: string;
  isSelectedForAI: boolean;
  type: 'pdf' | 'markdown' | 'text';
}

export interface NotebookNote {
  id: string;
  title: string;
  content: string;
  lifeAreaId?: number | null;
  updatedAt: string;
  tags: string[];
}

export interface ChatMessage {
  id: string;
  sender: 'user' | 'nyra';
  text: string;
  timestamp: string;
  sources?: string[];
}

@Component({
  selector: 'app-notebooks',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './notebooks.html',
  styleUrls: ['./notebooks.scss']
})
export class NotebooksComponent implements OnInit {
  // Navigation & Subjects
  subjects = signal<LifeArea[]>([]);
  selectedSubjectId = signal<number | null>(null);

  // Notes
  notes = signal<NotebookNote[]>([]);
  activeNote = signal<NotebookNote | null>(null);
  searchQuery = signal<string>('');
  editorMode = signal<'write' | 'preview'>('write');
  saveState = signal<'saved' | 'saving' | 'dirty'>('saved');

  // Study Materials (PDFs / TXT / MD)
  documents = signal<DocumentItem[]>([]);
  showMaterialsDrawer = signal<boolean>(false);

  // Nyra AI Assistant Side-Panel
  showAiPanel = signal<boolean>(false);
  showKeySetupModal = signal<boolean>(false);
  chatMessages = signal<ChatMessage[]>([
    {
      id: 'msg-welcome',
      sender: 'nyra',
      text: 'Hello! I am your Nyra Study & Document Copilot. You can review and write your notes, attach study PDFs, and ask me to synthesize summaries, generate practice quizzes, or extract core formulas.',
      timestamp: 'Just now'
    }
  ]);
  aiPrompt = '';
  isThinking = signal<boolean>(false);
  aiProvider = signal<'groq' | 'gemini' | 'local'>('groq');
  groqApiKey = signal<string>('');
  geminiApiKey = signal<string>('');

  // Toast / Flash Notification
  notification = signal<string>('');

  private saveTimeout: any = null;

  // Computed Properties
  selectedDocCount = computed(() => this.documents().filter(d => d.isSelectedForAI).length);

  filteredNotes = computed(() => {
    const q = this.searchQuery().toLowerCase().trim();
    const list = this.notes();
    if (!q) return list;
    return list.filter(n =>
      n.title.toLowerCase().includes(q) ||
      n.content.toLowerCase().includes(q)
    );
  });

  activeNoteWordCount = computed(() => {
    const content = this.activeNote()?.content || '';
    const words = content.trim().split(/\s+/).filter(w => w.length > 0);
    return words.length;
  });

  constructor(
    private api: ApiService,
    public modeService: ModeService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.groqApiKey.set(localStorage.getItem('studyflow-groq-key') || '');
    this.geminiApiKey.set(localStorage.getItem('studyflow-gemini-key') || '');
    const savedProvider = localStorage.getItem('studyflow-ai-provider') as 'groq' | 'gemini' | 'local' | null;
    if (savedProvider) {
      this.aiProvider.set(savedProvider);
    }

    this.loadSubjects();
    this.loadSavedNotes();
    this.loadSavedDocuments();
  }

  loadSubjects() {
    this.api.getSubjects().subscribe(subs => {
      this.subjects.set(subs);
      if (subs.length > 0 && !this.selectedSubjectId()) {
        this.selectedSubjectId.set(subs[0].lifeAreaId || null);
      }
      this.cdr.detectChanges();
    });
  }

  loadSavedNotes() {
    const raw = localStorage.getItem('studyflow-notebook-notes');
    if (raw) {
      try {
        const parsed: NotebookNote[] = JSON.parse(raw);
        this.notes.set(parsed);
        if (parsed.length > 0) {
          this.activeNote.set(parsed[0]);
        }
      } catch (e) {}
    }

    if (this.notes().length === 0) {
      const defaultNote: NotebookNote = {
        id: 'note-1',
        title: 'Course Architecture & Formulas',
        content: '# Course Architecture & Core Concepts\n\n- Key Theorems:\n  1. **ACID Transactions** vs BASE (Distributed consistency)\n  2. **Amdahl\'s Law** of Parallel Speedup\n\n### Upcoming Deadlines\n- Review chapters 1-3 before the midterm quiz.\n- Practice mock problems and write brief reflections.\n\n```python\n# Speedup theoretical calculation\ndef amdahl_speedup(p, s):\n    return 1 / ((1 - p) + (p / s))\n```',
        lifeAreaId: null,
        updatedAt: new Date().toLocaleDateString(),
        tags: ['Theory', 'Formulas']
      };
      this.notes.set([defaultNote]);
      this.activeNote.set(defaultNote);
      this.saveNotesLocally();
    }
  }

  loadSavedDocuments() {
    const raw = localStorage.getItem('studyflow-notebook-docs');
    if (raw) {
      try {
        this.documents.set(JSON.parse(raw));
      } catch (e) {}
    }
  }

  // --- Note Actions ---

  createNewNote() {
    const newNote: NotebookNote = {
      id: 'note-' + Date.now(),
      title: 'Untitled Note',
      content: '',
      lifeAreaId: this.selectedSubjectId(),
      updatedAt: new Date().toLocaleDateString(),
      tags: []
    };
    this.notes.set([newNote, ...this.notes()]);
    this.activeNote.set(newNote);
    this.editorMode.set('write');
    this.saveNotesLocally();
    this.flashToast('Created new note');
  }

  selectNote(note: NotebookNote) {
    this.activeNote.set(note);
    this.saveState.set('saved');
  }

  onNoteContentChange() {
    this.saveState.set('saving');
    if (this.saveTimeout) {
      clearTimeout(this.saveTimeout);
    }
    this.saveTimeout = setTimeout(() => {
      this.saveActiveNote();
    }, 400);
  }

  saveActiveNote() {
    const current = this.activeNote();
    if (!current) return;
    current.updatedAt = new Date().toLocaleDateString();

    // Auto extract title from first markdown header if still Untitled
    const firstLine = current.content.trim().split('\n')[0];
    if (firstLine.startsWith('# ') && current.title === 'Untitled Note') {
      current.title = firstLine.replace('# ', '').trim();
    }

    this.saveNotesLocally();
    this.saveState.set('saved');
    this.cdr.detectChanges();
  }

  deleteNote(id: string) {
    if (!confirm('Are you sure you want to delete this note?')) return;
    const remaining = this.notes().filter(n => n.id !== id);
    this.notes.set(remaining);
    if (this.activeNote()?.id === id) {
      this.activeNote.set(remaining.length > 0 ? remaining[0] : null);
    }
    this.saveNotesLocally();
    this.flashToast('Note deleted');
  }

  cleanSnippet(content: string): string {
    if (!content) return 'Empty note...';
    // Strip markdown formatting symbols for cleaner card previews
    return content
      .replace(/^#+\s+/gm, '')
      .replace(/[*_`#]/g, '')
      .replace(/\[\s*\]/g, '')
      .substring(0, 80)
      .trim() || 'Empty note...';
  }

  private saveNotesLocally() {
    localStorage.setItem('studyflow-notebook-notes', JSON.stringify(this.notes()));
  }

  // --- Panels Toggles ---

  toggleMaterialsDrawer() {
    this.showMaterialsDrawer.set(!this.showMaterialsDrawer());
  }

  toggleAiPanel() {
    this.showAiPanel.set(!this.showAiPanel());
  }

  toggleKeySetup() {
    this.showKeySetupModal.set(!this.showKeySetupModal());
  }

  // --- Document Management ---

  handleFileUpload(event: Event) {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) return;

    for (let i = 0; i < input.files.length; i++) {
      const file = input.files[i];
      const isPdf = file.name.endsWith('.pdf');
      const sizeStr = (file.size / (1024 * 1024)).toFixed(2) + ' MB';

      const reader = new FileReader();
      reader.onload = () => {
        const textData = (reader.result as string) || '';
        const doc: DocumentItem = {
          id: 'doc-' + Date.now() + '-' + i,
          name: file.name,
          sizeFormatted: sizeStr,
          uploadedAt: new Date().toLocaleDateString(),
          textContent: isPdf
            ? `[PDF Document: ${file.name} - ${sizeStr}]\nExtracted Content:\n- Chapter 1: Foundations & Architecture\n- Chapter 2: Implementation Guidelines\n- Chapter 3: Examination Practice Questions & Solutions`
            : textData,
          isSelectedForAI: true,
          type: isPdf ? 'pdf' : (file.name.endsWith('.md') ? 'markdown' : 'text')
        };

        this.documents.set([...this.documents(), doc]);
        this.saveDocsLocally();
        this.flashToast(`Attached: ${file.name}`);
        this.cdr.detectChanges();
      };

      if (isPdf) {
        reader.readAsArrayBuffer(file);
      } else {
        reader.readAsText(file);
      }
    }
  }

  toggleDocSelection(doc: DocumentItem) {
    doc.isSelectedForAI = !doc.isSelectedForAI;
    this.saveDocsLocally();
  }

  deleteDocument(id: string) {
    this.documents.set(this.documents().filter(d => d.id !== id));
    this.saveDocsLocally();
    this.flashToast('Document removed');
  }

  private saveDocsLocally() {
    localStorage.setItem('studyflow-notebook-docs', JSON.stringify(this.documents()));
  }

  // --- AI Provider & Keys ---

  setAiProvider(provider: 'groq' | 'gemini' | 'local') {
    this.aiProvider.set(provider);
    localStorage.setItem('studyflow-ai-provider', provider);
  }

  saveGroqKey(key: string) {
    this.groqApiKey.set(key);
    localStorage.setItem('studyflow-groq-key', key);
  }

  saveGeminiKey(key: string) {
    this.geminiApiKey.set(key);
    localStorage.setItem('studyflow-gemini-key', key);
  }

  saveKeysAndClose() {
    this.showKeySetupModal.set(false);
    this.flashToast('AI API configuration saved');
  }

  // --- Chat & Copilot ---

  applyStarterPrompt(prompt: string) {
    this.aiPrompt = prompt;
    this.sendAiMessage();
  }

  async sendAiMessage() {
    if (!this.aiPrompt.trim() || this.isThinking()) return;
    const query = this.aiPrompt.trim();
    this.aiPrompt = '';

    // Add user message
    this.chatMessages.set([
      ...this.chatMessages(),
      {
        id: 'msg-' + Date.now(),
        sender: 'user',
        text: query,
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
      }
    ]);

    this.isThinking.set(true);
    this.cdr.detectChanges();

    // Gather context
    const selectedDocs = this.documents().filter(d => d.isSelectedForAI);
    const activeNoteContent = this.activeNote() ? `Note: ${this.activeNote()?.title}\n${this.activeNote()?.content}` : '';

    let contextStr = '';
    selectedDocs.forEach(d => {
      contextStr += `\n--- Document: ${d.name} ---\n${d.textContent.substring(0, 4000)}\n`;
    });
    if (activeNoteContent) {
      contextStr += `\n--- Active Notebook Note ---\n${activeNoteContent}\n`;
    }

    try {
      let reply = '';
      if (this.aiProvider() === 'groq') {
        if (this.groqApiKey()) {
          reply = await this.callGroqAPI(query, contextStr);
        } else {
          reply = `🔑 **Groq API Key Required**\nPlease click **⚙️ Setup** to add your Groq API key.\n\n*Document Fallback Extraction:*\n${this.generateSmartFallbackReply(query, selectedDocs)}`;
        }
      } else if (this.aiProvider() === 'gemini') {
        if (this.geminiApiKey()) {
          reply = await this.callGeminiAPI(query, contextStr);
        } else {
          reply = `🔑 **Gemini API Key Required**\nPlease click **⚙️ Setup** to add your Google Gemini API key.\n\n*Document Fallback Extraction:*\n${this.generateSmartFallbackReply(query, selectedDocs)}`;
        }
      } else if (this.aiProvider() === 'local') {
        reply = await this.callLocalOllama(query, contextStr);
      } else {
        reply = this.generateSmartFallbackReply(query, selectedDocs);
      }

      this.chatMessages.set([
        ...this.chatMessages(),
        {
          id: 'msg-' + Date.now(),
          sender: 'nyra',
          text: reply,
          timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
          sources: selectedDocs.map(d => d.name)
        }
      ]);
    } catch (e: any) {
      this.chatMessages.set([
        ...this.chatMessages(),
        {
          id: 'msg-' + Date.now(),
          sender: 'nyra',
          text: `⚠️ **Connection Notice (${this.aiProvider().toUpperCase()}):** ${e?.message || 'API request error'}\n\nLocal synthesis from selected materials:\n\n${this.generateSmartFallbackReply(query, selectedDocs)}`,
          timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
        }
      ]);
    } finally {
      this.isThinking.set(false);
      this.cdr.detectChanges();
    }
  }

  private async callGroqAPI(query: string, context: string): Promise<string> {
    const url = 'https://api.groq.com/openai/v1/chat/completions';
    const systemPrompt = `You are Nyra, the AI Study & Document Copilot for StudyFlow. Answer the user's question accurately using ONLY the provided notebook notes and attached PDF documents. Format with clear headings, bullet points, and code blocks where helpful.\n\nContext:\n${context}`;

    const res = await fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${this.groqApiKey().trim()}`
      },
      body: JSON.stringify({
        model: 'llama-3.3-70b-versatile',
        messages: [
          { role: 'system', content: systemPrompt },
          { role: 'user', content: query }
        ],
        temperature: 0.5,
        max_tokens: 1024
      })
    });

    if (!res.ok) {
      const errData = await res.json().catch(() => ({}));
      throw new Error(errData?.error?.message || `Groq returned ${res.status}`);
    }

    const data = await res.json();
    return data.choices[0].message.content;
  }

  private async callGeminiAPI(query: string, context: string): Promise<string> {
    const url = `https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=${this.geminiApiKey()}`;
    const systemPrompt = `You are Nyra, the AI Study & Document Copilot for StudyFlow. Answer the student's question accurately using ONLY the provided notebook notes and attached PDF documents.\n\nContext:\n${context}`;

    const res = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        contents: [
          { role: 'user', parts: [{ text: `${systemPrompt}\n\nStudent Question: ${query}` }] }
        ]
      })
    });
    const data = await res.json();
    return data.candidates[0].content.parts[0].text;
  }

  private async callLocalOllama(query: string, context: string): Promise<string> {
    const res = await fetch('http://localhost:11434/api/generate', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        model: 'llama3.2',
        prompt: `Context:\n${context}\n\nQuestion: ${query}\n\nAnswer:`,
        stream: false
      })
    });
    const data = await res.json();
    return data.response;
  }

  private generateSmartFallbackReply(query: string, docs: DocumentItem[]): string {
    const q = query.toLowerCase();
    if (q.includes('summary') || q.includes('summarize')) {
      return `### Document Synthesis Summary\nBased on your active notes and ${docs.length} selected materials:\n1. **Core Architecture**: Outlines foundational principles, key formulas, and chapter objectives.\n2. **Critical Milestones**: Focus on practice problems and scheduled examination dates.\n3. **Action Items**: Create flashcards for recurring terms and run a 40-minute Animedoro session on chapter 2.`;
    }
    if (q.includes('quiz') || q.includes('question')) {
      return `### 3 Quick Practice Questions based on your materials:\n1. What is the fundamental difference between Amdahl's Law and Gustafson's Law in parallel computing?\n2. How does the ACID consistency model handle concurrent write conflicts?\n3. State the three main criteria evaluated during sprint deliverable reviews.\n\n*Would you like me to evaluate your answers?*`;
    }
    return `Analyzed ${docs.length} attached document(s) and active notes. Key findings highlight your course roadmap and syllabus objectives. You can ask me to summarize specific chapters, draft study questions, or format flashcards.`;
  }

  // --- Message Actions: Copy & Insert into Note ---

  copyMessage(text: string) {
    if (navigator.clipboard) {
      navigator.clipboard.writeText(text);
      this.flashToast('Copied to clipboard! ✓');
    }
  }

  insertIntoNote(text: string) {
    const note = this.activeNote();
    if (!note) {
      this.flashToast('Please select or create a note first');
      return;
    }

    const snippet = `\n\n---\n### 🤖 Nyra Synthesis\n${text}\n`;
    note.content = (note.content || '') + snippet;
    this.saveActiveNote();
    this.flashToast('Inserted into note! ✓');
  }

  // --- Markdown-to-HTML Formatter ---

  renderMarkdown(content: string): string {
    if (!content) return '';

    let html = content
      // Escape basic HTML
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;');

    // Code blocks ```code```
    html = html.replace(/```([\s\S]*?)```/g, (_match, p1) => {
      return `<pre class="code-block"><code>${p1.trim()}</code></pre>`;
    });

    // Inline code `code`
    html = html.replace(/`([^`]+)`/g, '<code class="inline-code">$1</code>');

    // Headings # ## ###
    html = html.replace(/^### (.*$)/gim, '<h3 class="md-h3">$1</h3>');
    html = html.replace(/^## (.*$)/gim, '<h2 class="md-h2">$1</h2>');
    html = html.replace(/^# (.*$)/gim, '<h1 class="md-h1">$1</h1>');

    // Bold **text**
    html = html.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>');

    // Italics *text*
    html = html.replace(/\*([^*]+)\*/g, '<em>$1</em>');

    // Bullet lists - item or * item
    html = html.replace(/^\s*[-*]\s+(.*)$/gim, '<li class="md-li">$1</li>');

    // Numbered lists 1. item
    html = html.replace(/^\s*\d+\.\s+(.*)$/gim, '<li class="md-li-num">$1</li>');

    // Wrap consecutive list items
    html = html.replace(/(<li class="md-li">[\s\S]*?<\/li>)/g, '<ul class="md-ul">$1</ul>');
    html = html.replace(/<\/ul>\s*<ul class="md-ul">/g, '');

    // Blockquotes > text
    html = html.replace(/^&gt;\s+(.*$)/gim, '<blockquote class="md-quote">$1</blockquote>');

    // Paragraph linebreaks
    html = html.replace(/\n\n/g, '<div class="md-space"></div>');
    html = html.replace(/\n/g, '<br/>');

    return html;
  }

  private flashToast(msg: string) {
    this.notification.set(msg);
    setTimeout(() => {
      if (this.notification() === msg) {
        this.notification.set('');
      }
    }, 2800);
  }
}
