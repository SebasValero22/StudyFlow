DROP DATABASE IF EXISTS StudyFlow;
CREATE DATABASE StudyFlow;
USE StudyFlow;

-- 1. USER
DROP TABLE IF EXISTS Users;
CREATE TABLE Users (
    userId INT PRIMARY KEY AUTO_INCREMENT,
    userName VARCHAR(50) NOT NULL,
    email VARCHAR(50) UNIQUE NOT NULL,
    passwordHash VARCHAR(100) NOT NULL,
    registrationDate DATE DEFAULT (CURRENT_DATE),
    activeUser BOOLEAN DEFAULT TRUE
);

-- 2. SUBJECT (Asignatura)
DROP TABLE IF EXISTS Subjects;
CREATE TABLE Subjects (
    subjectId INT PRIMARY KEY AUTO_INCREMENT,
    userId INT NOT NULL,
    nameSubject VARCHAR(50) NOT NULL,
    academicYear VARCHAR(20), 
    color VARCHAR(7), -- Stores Hex like #FF5722
    activeSubject BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (userId) REFERENCES Users(userId) ON DELETE CASCADE
);

-- 3. TASK (Tarea)
DROP TABLE IF EXISTS Tasks;
CREATE TABLE Tasks (
    taskId INT PRIMARY KEY AUTO_INCREMENT,
    subjectId INT NOT NULL,
    title VARCHAR(100) NOT NULL,
    descriptionTask TEXT,
    startDate DATE,
    dueDate DATE,
    priority VARCHAR(10) CHECK (priority IN ('HIGH', 'MEDIUM', 'LOW')),
    isCompleted BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (subjectId) REFERENCES Subjects(subjectId) ON DELETE CASCADE
);

-- 4. EXAM (Examen)
DROP TABLE IF EXISTS Exams;
CREATE TABLE Exams (
    examId INT PRIMARY KEY AUTO_INCREMENT,
    subjectId INT NOT NULL,
    nameExam VARCHAR(100) NOT NULL,
    examType VARCHAR(20), 
    examDate DATE,
    classroom VARCHAR(20),
    isCompleted BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (subjectId) REFERENCES Subjects(subjectId) ON DELETE CASCADE
);

-- 5. GRADE (Nota)
DROP TABLE IF EXISTS Grades;
CREATE TABLE Grades (
    gradeId INT PRIMARY KEY AUTO_INCREMENT,
    subjectId INT NOT NULL,
    concept VARCHAR(50), -- e.g., "Math Homework 1"
    score DOUBLE NOT NULL CHECK (score >= 0 AND score <= 10),
    weight DOUBLE NOT NULL, -- Percentage (20%, 50%, etc.)
    gradeDate DATE DEFAULT (CURRENT_DATE),
    FOREIGN KEY (subjectId) REFERENCES Subjects(subjectId) ON DELETE CASCADE
);

-- 6. FILES (Archivo)
DROP TABLE IF EXISTS Files;
CREATE TABLE Files (
    fileId INT PRIMARY KEY AUTO_INCREMENT,
    subjectId INT NOT NULL,
    filename VARCHAR(100) NOT NULL,
    filepath VARCHAR(255) NOT NULL,
    sizeBytes BIGINT,
    mimeType VARCHAR(50),
    FOREIGN KEY (subjectId) REFERENCES Subjects(subjectId) ON DELETE CASCADE
);

-- 7. REMINDER (Recordatorio)
DROP TABLE IF EXISTS Reminders;
CREATE TABLE Reminders (
    reminderId INT PRIMARY KEY AUTO_INCREMENT,
    taskId INT,
    examId INT,
    message VARCHAR(255),
    date_Time DATETIME,
    FOREIGN KEY (taskId) REFERENCES Tasks(taskId) ON DELETE CASCADE,
    FOREIGN KEY (examId) REFERENCES Exams(examId) ON DELETE CASCADE,
    CHECK (taskId IS NOT NULL OR examId IS NOT NULL)
);

-- 8. OPERATION HISTORY (MANDATORY for SGE Grade)
DROP TABLE IF EXISTS OperationHistory ;
CREATE TABLE OperationHistory (
    historyId INT PRIMARY KEY AUTO_INCREMENT,
    userId INT,
    operationDate DATETIME DEFAULT CURRENT_TIMESTAMP,
    actionType VARCHAR(50), -- e.g., "LOGIN", "DELETE_TASK"
    descriptionOH TEXT,
    FOREIGN KEY (userId) REFERENCES Users(userId)
);

-- INSERTS DE PRUEBA (DYNAMISM ADDED)

-- 1. Create a Test User
INSERT INTO Users (userName, email, passwordHash, activeUser) 
VALUES ('sebas', 'sebas@email.com', 'password_sebas', true);

-- 2. Create Subjects (Vibrant Colors)
INSERT INTO Subjects (userId, nameSubject, academicYear, color) VALUES (1, 'Acceso a Datos', '2025-2026', '#e74c3c');
INSERT INTO Subjects (userId, nameSubject, academicYear, color) VALUES (1, 'Desarrollo de Interfaces', '2025-2026', '#3498db');
INSERT INTO Subjects (userId, nameSubject, academicYear, color) VALUES (1, 'Gestión Empresarial', '2025-2026', '#2ecc71');
INSERT INTO Subjects (userId, nameSubject, academicYear, color) VALUES (1, 'Programación Multimedia', '2025-2026', '#f1c40f');
INSERT INTO Subjects (userId, nameSubject, academicYear, color) VALUES (1, 'Inglés Técnico', '2025-2026', '#9b59b6');

-- 3. Create Tasks (Realistic Tasks)
INSERT INTO Tasks (subjectId, title, descriptionTask, dueDate, priority, isCompleted) 
VALUES (1, 'Mapeo Hibernate', 'Realizar el mapeo de todas las entidades del proyecto StudyFlow usando anotaciones JPA.', DATE_ADD(CURRENT_DATE, INTERVAL 2 DAY), 'HIGH', false);
INSERT INTO Tasks (subjectId, title, descriptionTask, dueDate, priority, isCompleted) 
VALUES (2, 'Dashboard Angular', 'Crear la interfaz principal del dashboard usando componentes de Angular Material.', DATE_ADD(CURRENT_DATE, INTERVAL 4 DAY), 'MEDIUM', false);
INSERT INTO Tasks (subjectId, title, descriptionTask, dueDate, priority, isCompleted) 
VALUES (3, 'Informe de Presupuesto', 'Preparar el balance de situación y la cuenta de pérdidas y ganancias.', DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), 'LOW', false);
INSERT INTO Tasks (subjectId, title, descriptionTask, dueDate, priority, isCompleted) 
VALUES (4, 'Permisos Android', 'Implementar la gestión de permisos en tiempo de ejecución para Android 14.', DATE_ADD(CURRENT_DATE, INTERVAL 7 DAY), 'HIGH', false);
INSERT INTO Tasks (subjectId, title, descriptionTask, dueDate, priority, isCompleted) 
VALUES (1, 'Tests Unitarios JUnit', 'Escribir pruebas unitarias para los servicios de la API usando JUnit 5 y Mockito.', DATE_ADD(CURRENT_DATE, INTERVAL 10 DAY), 'MEDIUM', false);

-- 4. Create some Grades (Realistic Scores)
INSERT INTO Grades (subjectId, concept, score, weight) VALUES (1, 'Prueba de Mapeo', 8.75, 20);
INSERT INTO Grades (subjectId, concept, score, weight) VALUES (1, 'Proyecto Hito 1', 9.5, 30);
INSERT INTO Grades (subjectId, concept, score, weight) VALUES (1, 'Examen Parcial', 7.8, 50);

-- 5. Create an Exam (Realistic Exams)
INSERT INTO Exams (subjectId, nameExam, examType, examDate, classroom, isCompleted) 
VALUES (1, 'Examen Final Acceso a Datos', 'Final', DATE_ADD(CURRENT_DATE, INTERVAL 15 DAY), 'Aula 204', false);
INSERT INTO Exams (subjectId, nameExam, examType, examDate, classroom, isCompleted) 
VALUES (2, 'Entrega Proyecto Interfaces', 'Proyecto', DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY), 'Online', false);