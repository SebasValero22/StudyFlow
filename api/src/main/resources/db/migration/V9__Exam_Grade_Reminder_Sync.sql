-- V9__Exam_Grade_Reminder_Sync.sql
-- Sync columns for exams, grades, reminders + idempotency clientUuid on core tables.

ALTER TABLE Exams
    ADD COLUMN userId INT NULL,
    ADD COLUMN updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    ADD COLUMN deletedAt TIMESTAMP NULL,
    ADD COLUMN clientUuid VARCHAR(36) NULL;

ALTER TABLE Grades
    ADD COLUMN userId INT NULL,
    ADD COLUMN updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    ADD COLUMN deletedAt TIMESTAMP NULL,
    ADD COLUMN clientUuid VARCHAR(36) NULL;

ALTER TABLE Reminders
    ADD COLUMN userId INT NULL,
    ADD COLUMN updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    ADD COLUMN deletedAt TIMESTAMP NULL,
    ADD COLUMN clientUuid VARCHAR(36) NULL;

-- Backfill ownership from LifeArea / Tasks so existing rows sync correctly.
UPDATE Exams e
    INNER JOIN LifeAreas la ON e.lifeAreaId = la.lifeAreaId
SET e.userId = la.userId
WHERE e.userId IS NULL;

UPDATE Grades g
    INNER JOIN LifeAreas la ON g.lifeAreaId = la.lifeAreaId
SET g.userId = la.userId
WHERE g.userId IS NULL;

UPDATE Reminders r
    INNER JOIN Tasks t ON r.taskId = t.taskId
SET r.userId = t.userId
WHERE r.userId IS NULL AND r.taskId IS NOT NULL;

UPDATE Reminders r
    INNER JOIN Exams e ON r.examId = e.examId
SET r.userId = e.userId
WHERE r.userId IS NULL AND r.examId IS NOT NULL AND e.userId IS NOT NULL;

ALTER TABLE Tasks
    ADD COLUMN clientUuid VARCHAR(36) NULL;

ALTER TABLE LifeAreas
    ADD COLUMN clientUuid VARCHAR(36) NULL;

ALTER TABLE FocusSessions
    ADD COLUMN clientUuid VARCHAR(36) NULL;
