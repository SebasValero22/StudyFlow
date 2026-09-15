-- V6: Calendar feeds - professional calendars flow INTO the scheduler.
-- Read-only ICS subscriptions (works with Outlook, Google secret iCal address,
-- Fastmail, Nextcloud...) without OAuth for v1.

CREATE TABLE CalendarFeeds (
    feedId        INT PRIMARY KEY AUTO_INCREMENT,
    userId        INT NOT NULL,
    name          VARCHAR(100) NOT NULL,
    url           VARCHAR(500) NOT NULL,
    enabled       BOOLEAN DEFAULT TRUE,
    lastSyncedAt  DATETIME NULL,
    FOREIGN KEY (userId) REFERENCES Users(userId) ON DELETE CASCADE
);

CREATE TABLE BusyBlocks (
    blockId  INT PRIMARY KEY AUTO_INCREMENT,
    feedId   INT NOT NULL,
    uid      VARCHAR(255),
    startAt  DATETIME NOT NULL,
    endAt    DATETIME NOT NULL,
    summary  VARCHAR(255),
    FOREIGN KEY (feedId) REFERENCES CalendarFeeds(feedId) ON DELETE CASCADE
);

CREATE INDEX idx_blocks_feed_start ON BusyBlocks(feedId, startAt);
