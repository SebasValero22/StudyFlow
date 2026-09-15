-- V4: Task ownership - every task belongs to exactly one user.
-- Backfill: all pre-existing data belonged to the first registered account.

ALTER TABLE Tasks ADD COLUMN userId INT NULL;

UPDATE Tasks
SET userId = (SELECT MIN(u.userId) FROM Users u)
WHERE userId IS NULL;

ALTER TABLE Tasks MODIFY userId INT NOT NULL;

ALTER TABLE Tasks ADD CONSTRAINT fk_tasks_user
    FOREIGN KEY (userId) REFERENCES Users(userId) ON DELETE CASCADE;

CREATE INDEX idx_tasks_userId ON Tasks(userId);
