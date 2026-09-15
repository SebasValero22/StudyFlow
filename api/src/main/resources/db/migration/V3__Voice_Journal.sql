-- V3: Voice Journal (Life OS - reflection layer)
-- Audio bytes live in BYO Google Drive; we store metadata + transcripts only.

CREATE TABLE VoiceJournal (
    entryId         INT PRIMARY KEY AUTO_INCREMENT,
    userId          INT NOT NULL,
    taskId          INT NULL,            -- Optional link to a specific task
    title           VARCHAR(150),
    moodTag         VARCHAR(10) CHECK (moodTag IN ('GREAT', 'GOOD', 'NEUTRAL', 'ROUGH', 'TERRIBLE')), -- Chart.js-friendly, validated at SQL level
    rawTranscript   TEXT,                -- Verbatim STT output
    transcript      TEXT,                -- LLM-cleaned Markdown + action checkboxes
    audioPath       VARCHAR(255),        -- Drive fileId (null = text-only entry)
    durationSeconds INT,
    createdAt       DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (userId) REFERENCES Users(userId) ON DELETE CASCADE,
    FOREIGN KEY (taskId) REFERENCES Tasks(taskId) ON DELETE SET NULL
);
