-- V5: Focus Sessions - the Animedoro timer becomes a time-tracking product.
-- Every completed timer cycle is persisted; FOCUS sessions feed timesheets.

CREATE TABLE FocusSessions (
    sessionId       INT PRIMARY KEY AUTO_INCREMENT,
    userId          INT NOT NULL,
    taskId          INT NULL,            -- task pinned to the session (Animedoro lock)
    startedAt       DATETIME NOT NULL,
    endedAt         DATETIME NOT NULL,
    durationSeconds INT NOT NULL,
    mode            VARCHAR(10) NOT NULL DEFAULT 'FOCUS' CHECK (mode IN ('FOCUS','BREAK')),
    completed       BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (userId) REFERENCES Users(userId) ON DELETE CASCADE,
    FOREIGN KEY (taskId) REFERENCES Tasks(taskId) ON DELETE SET NULL
);

CREATE INDEX idx_sessions_user_start ON FocusSessions(userId, startedAt);
