# StudyFlow 🎓

> **Modern Academic Life OS & Deep Work Focus Engine**  
> *Local-first, offline-ready, multi-platform companion built for students and lifelong learners.*

---

## 🌟 Overview

**StudyFlow** is a comprehensive academic and productivity ecosystem that unifies task management, deep work timer mechanics, Markdown note-taking, and academic grade analytics into a single synchronized experience.

Whether you are studying at your desk on **Windows/Linux**, reviewing on your **Android** phone between lectures, or checking deadlines on the **Web PWA**, your data stays instantly accessible, even completely offline.

---

## 🚀 Live Production Services

| Component | Platform / Host | URL |
|---|---|---|
| **Web PWA** | Netlify | [https://studyflow-pwa.netlify.app](https://studyflow-pwa.netlify.app) |
| **Backend API** | Render | [https://studyflow-nuux.onrender.com](https://studyflow-nuux.onrender.com) |
| **API Health Ping** | Render | [https://studyflow-nuux.onrender.com/api/public/ping](https://studyflow-nuux.onrender.com/api/public/ping) |
| **Cloud Database** | Aiven Cloud | MySQL 8.x with SSL encryption & Flyway migrations |
| **Source Code** | GitHub | [https://github.com/SebasValero22/StudyFlow](https://github.com/SebasValero22/StudyFlow) |

---

## ⚡ Key Features at a Glance

### 1. ⚡ Quick Capture & Ingestion
- **Global Hotkey (`Ctrl + Space`)**: Invoke quick capture instantly from anywhere in your operating system without switching windows.
- **Natural Language Task Creation**: Input tasks rapidly with automatic date, priority, and life area tagging.
- **Linux Modal Capture**: Integrated with Rofi, Wofi, and Fuzzel on Linux/Hyprland.

### 2. ⏱️ Animedoro & Pomodoro Focus Engine
- **Flexible Timing Modes**:
  - **Animedoro**: 40 minutes of deep focus + 20 minutes to watch an episode of anime or take a relaxed break.
  - **Pomodoro**: Traditional 25 minutes of focus + 5 minutes short break (15m long break after 4 cycles).
  - **Custom Mode**: Set any interval configuration you prefer.
- **Audio Synthesis Chimes**: Procedural, non-jarring audio notifications when sessions start or end (no external audio codecs required).
- **Auto-Advance**: Optional automatic transitions between focus and break intervals.
- **Timesheet Tracking**: Logs focus sessions against specific tasks and exports to CSV.

### 3. 🗄️ Local-First & Background Sync
- **Offline SQLite Engine**: Desktop data is stored locally in SQLite (`studyflow.db`). Full access even when disconnected from the internet.
- **Delta Sync Engine**: Automatically pushes local changes and pulls cloud updates with ISO timestamp cursors every 60 seconds.
- **Conflict Surfacing**: If a remote cloud change overwrites an unsaved local edit, a prominent `⚠️ Sync Conflict` badge alerts you on the dashboard with a side-by-side review dialog.
- **Push Validation**: Safeguards against invalid rows stalling sync queues.

### 4. 📝 Markdown Notes Vault
- **Obsidian-Style Notebook**: Stores your lecture notes as pure `.md` files in your local filesystem (`~/StudyFlow/vault` by default).
- **Frontmatter & Tags**: Organize topics with YAML frontmatter, tags, and categories.
- **Full-Text Search & Live Preview**: Real-time rendering of Markdown, math notation, and lists with zero cloud vendor lock-in.

### 5. 📊 Visual Analytics & Focus Heatmap
- **Activity Heatmap (PWA)**: GitHub-style 52-week activity grid displaying daily focus session volume.
- **Progress Tracking**: Daily averages, total hours focused, and session count cards.
- **Timesheet Export**: One-click download of your focus logs as `.csv` for timesheet or habit analysis.

### 6. 📚 Academic Life OS
- **Subject Color System**: Every subject has a signature hex color that automatically themes your task list, calendar, and timer cards.
- **Weighted Grade Calculator**: Calculate exact academic term standings based on syllabus exam and coursework weights.
- **Exam Countdown & Milestones**: Track approaching exams with urgency indicators.
- **Life Areas**: Categorize responsibilities across *Study*, *Work*, *Health*, and *Personal*.

### 7. 🪟 Windows Desktop Integration
- **1-Click Launcher**: Double-click the `StudyFlow` desktop shortcut or root `.vbs`/`.bat` script to start without terminal windows.
- **Persistent Session**: "Remember Me" keeps you logged in across restarts. Boots straight into your dashboard.
- **System Tray Mode**: Closing the window hides the app to the Windows notification tray. Consumes negligible CPU/RAM while staying ready for `Ctrl + Space` capture.

### 8. 🐧 Linux / Omarchy Native Daemon
- **Rust Service (`studyflowd`)**: Headless daemon communicating via Unix domain socket (`$XDG_RUNTIME_DIR/studyflow.sock`).
- **CLI (`studyflow-cli`)**: Control timers, view state, and ingest notes from your terminal.
- **Status Bar Integration**: Native JSON stream modules for **Waybar** and **Quickshell** with colorized animations.

---

## 🧭 New User Guide: Getting Started with StudyFlow

### Step 1: Launching the App
- **Windows Desktop**:
  - Double-click the **StudyFlow** shortcut on your Desktop (or run `StudyFlow.vbs` in the project root).
  - No command prompts will appear; the app opens directly.
- **Web Browser**:
  - Visit [https://studyflow-pwa.netlify.app](https://studyflow-pwa.netlify.app) on any phone, tablet, or browser.
  - Click **Install App** in the browser address bar to install it as a standalone PWA.

### Step 2: Login or Create an Account
- **Instant Demo Account**:
  - **Email**: `admin@mail.com`
  - **Password**: `123`
- Or click **Register** to create your own private workspace. Your credentials will be remembered on the device.

### Step 3: Add Your Subjects & Assign Colors
1. Navigate to **Subjects** in the sidebar.
2. Click **+ Add Subject**.
3. Give it a name (e.g. *Physics II*, *Software Engineering*) and pick a distinct color.
4. All tasks, exams, and timer sessions associated with this subject will inherit this color across every device.

### Step 4: Rapidly Capture Tasks (`Ctrl + Space`)
1. Press `Ctrl + Space` anywhere in Windows (even while browsing or reading a PDF).
2. Type what you need to do: e.g., *"Finish lab report by Friday for Physics"*.
3. Press **Enter**. The task appears in your task list ready to track.

### Step 5: Start an Animedoro Session
1. Head to **Focus Timer** (or press the timer icon on your task card).
2. Choose your preset (**Animedoro 40/20** or **Pomodoro 25/5**).
3. Hit **Start**.
4. When focus time expires, StudyFlow plays a pleasant chime notifying you it is time for your break.

### Step 6: Take Notes in the Vault
1. Click **Notes** in the dashboard.
2. Create Markdown notes for each lecture or project.
3. Your notes are saved as standard `.md` files directly on your computer, fully compatible with Obsidian, VS Code, and Logseq.

### Step 7: Hide to Tray When Done
- Click the **`X`** in the top right corner.
- StudyFlow minimizes to your system tray in the background.
- Whenever inspiration strikes, tap `Ctrl + Space` or click the tray icon to pop it right back up.

---

## ⌨️ Global Shortcuts Cheat Sheet

| Shortcut | Scope | Description |
|---|---|---|
| `Ctrl + Space` | Desktop Global | Open Nyra Quick Capture popup from any app |
| `Click Tray Icon` | Windows System Tray | Show / Restore StudyFlow window |
| `Super + Shift + S` | Linux (Hyprland) | Toggle StudyFlow scratchpad window |
| `Super + P` | Linux (Hyprland) | Toggle focus timer (Start / Pause / Resume) |
| `Super + Alt + C` | Linux (Hyprland) | Launch Rofi/Wofi Quick Capture modal |

---

## 📁 Repository Structure

```text
StudyFlow/
├── android/                 # Native Android Application (Java 21, Retrofit, OkHttp Bearer)
│   ├── app/src/main/        # UI Activities, Adapters, SQLite cache, and Services
│   └── keystore/            # Release signing keystore
├── api/                     # Spring Boot 4.0.2 REST API (Java 21, Spring Security, JWT)
│   ├── src/main/java/       # Controllers, Services, Entities, DTOs, Security
│   ├── src/main/resources/  # application.properties, Flyway migrations (V1 -> V10)
│   └── Dockerfile           # Production container for Render deployment
├── assets/                  # High-res logos, banners, and Windows .ico app icon
├── database/                # Standalone SQL schema scripts and documentation
├── desktop/                 # JavaFX 21 Desktop Client (OpenJFX, SQLite, Tray, GlobalHook)
│   ├── src/main/java/       # Local-first Sync Engine, Controllers, Services, Notes Vault
│   ├── src/main/resources/  # FXML templates, CSS themes (Light & Dark), Icons
│   └── pom.xml              # Shaded uber-jar build configuration
├── linux/                   # Linux / Omarchy Native Workspace (Rust 2021)
│   ├── studyflowd/          # Headless background timer and API daemon
│   ├── studyflow-cli/       # CLI utility, Waybar streamer, and Rofi modal
│   └── packaging/           # systemd user service, PKGBUILD, Waybar module
├── pwa/                     # Progressive Web App (Angular 19+, SCSS, Standalone Components)
│   ├── src/app/features/    # Timer, Focus Heatmap Analytics, Tasks, Exams, Subjects
│   └── src/app/core/        # Guards, Auth interceptors, LocalStorage cache
├── scripts/                 # Utility scripts (Shortcut creator, ICO generator)
├── StudyFlow.bat            # Quick Windows batch execution script
├── StudyFlow.vbs            # Windowless 1-click execution script
└── GEMINI.md                # System instructions, database configs, and architectural rules
```

---

## 🛠️ Verification & Test Suites

All modules have comprehensive automated test suites and pass cleanly:

```bash
# Backend Spring Boot API (19/19 tests)
cd api
./mvnw clean test

# Desktop Client (15/15 tests + Uber-JAR package)
cd desktop
./mvnw clean test package -DskipTests

# Angular PWA (15/15 tests)
cd pwa
ng test --watch=false

# Linux Rust Workspace
cd linux
cargo test --all
```

---

## 📄 License & Credits

- **Author:** Sebastián Valero  
- **Project:** Final Degree Project (*Ciclo Superior de Desarrollo de Aplicaciones Multiplataforma*) — IES Camp de Morvedre  
- **License:** MIT License. Free to use, adapt, and build upon.
