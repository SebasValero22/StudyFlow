-- V8__Sync_Tombstones.sql

-- Add updated_at and deleted_at to Tasks
ALTER TABLE Tasks
ADD COLUMN updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
ADD COLUMN deletedAt TIMESTAMP NULL;

-- Add updated_at and deleted_at to FocusSessions
ALTER TABLE FocusSessions
ADD COLUMN updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
ADD COLUMN deletedAt TIMESTAMP NULL;

-- Add updated_at and deleted_at to LifeAreas
ALTER TABLE LifeAreas
ADD COLUMN updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
ADD COLUMN deletedAt TIMESTAMP NULL;

-- Add updated_at and deleted_at to Projects
ALTER TABLE Projects
ADD COLUMN updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
ADD COLUMN deletedAt TIMESTAMP NULL;

-- Add updated_at and deleted_at to VoiceJournal
ALTER TABLE VoiceJournal
ADD COLUMN updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
ADD COLUMN deletedAt TIMESTAMP NULL;

-- Create UserSettings table for syncing themes/timer preferences
CREATE TABLE UserSettings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    userId INT NOT NULL,
    settingKey VARCHAR(255) NOT NULL,
    settingValue TEXT,
    updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (userId) REFERENCES Users(userId) ON DELETE CASCADE,
    UNIQUE KEY unique_user_setting (userId, settingKey)
);
