-- V7: Project collaboration. Projects become shareable client-work spaces
-- with OWNER / EDITOR / VIEWER roles.

CREATE TABLE ProjectMembers (
    memberId  INT PRIMARY KEY AUTO_INCREMENT,
    projectId INT NOT NULL,
    userId    INT NOT NULL,
    role      VARCHAR(10) NOT NULL CHECK (role IN ('OWNER','EDITOR','VIEWER')),
    UNIQUE KEY uq_member (projectId, userId),
    FOREIGN KEY (projectId) REFERENCES Projects(projectId) ON DELETE CASCADE,
    FOREIGN KEY (userId) REFERENCES Users(userId) ON DELETE CASCADE
);

CREATE INDEX idx_members_user ON ProjectMembers(userId);
