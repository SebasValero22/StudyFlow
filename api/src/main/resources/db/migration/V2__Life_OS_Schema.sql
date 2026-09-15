-- V2: Life OS Schema Migration

-- 1. Rename Subjects to LifeAreas
RENAME TABLE Subjects TO LifeAreas;
ALTER TABLE LifeAreas CHANGE subjectId lifeAreaId INT AUTO_INCREMENT;
ALTER TABLE LifeAreas CHANGE nameSubject name VARCHAR(50) NOT NULL;
ALTER TABLE LifeAreas CHANGE activeSubject active BOOLEAN DEFAULT TRUE;

-- 2. Create Projects Table
CREATE TABLE Projects (
    projectId INT PRIMARY KEY AUTO_INCREMENT,
    lifeAreaId INT NOT NULL,
    name VARCHAR(100) NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (lifeAreaId) REFERENCES LifeAreas(lifeAreaId) ON DELETE CASCADE
);

-- 3. Update Tasks Table
-- Drop old unnamed foreign key (MySQL default naming convention)
ALTER TABLE Tasks DROP FOREIGN KEY Tasks_ibfk_1;
ALTER TABLE Tasks CHANGE subjectId lifeAreaId INT NULL;
ALTER TABLE Tasks ADD CONSTRAINT fk_tasks_life_area FOREIGN KEY (lifeAreaId) REFERENCES LifeAreas(lifeAreaId) ON DELETE CASCADE;

-- Add Project and Habit columns
ALTER TABLE Tasks ADD COLUMN projectId INT DEFAULT NULL;
ALTER TABLE Tasks ADD COLUMN isHabit BOOLEAN DEFAULT FALSE;
ALTER TABLE Tasks ADD CONSTRAINT fk_tasks_project FOREIGN KEY (projectId) REFERENCES Projects(projectId) ON DELETE CASCADE;

-- 4. Update Exams Table
ALTER TABLE Exams DROP FOREIGN KEY Exams_ibfk_1;
ALTER TABLE Exams CHANGE subjectId lifeAreaId INT NOT NULL;
ALTER TABLE Exams ADD CONSTRAINT fk_exams_life_area FOREIGN KEY (lifeAreaId) REFERENCES LifeAreas(lifeAreaId) ON DELETE CASCADE;

-- 5. Update Grades Table
ALTER TABLE Grades DROP FOREIGN KEY Grades_ibfk_1;
ALTER TABLE Grades CHANGE subjectId lifeAreaId INT NOT NULL;
ALTER TABLE Grades ADD CONSTRAINT fk_grades_life_area FOREIGN KEY (lifeAreaId) REFERENCES LifeAreas(lifeAreaId) ON DELETE CASCADE;

-- 6. Update Files Table
ALTER TABLE Files DROP FOREIGN KEY Files_ibfk_1;
ALTER TABLE Files CHANGE subjectId lifeAreaId INT NOT NULL;
ALTER TABLE Files ADD CONSTRAINT fk_files_life_area FOREIGN KEY (lifeAreaId) REFERENCES LifeAreas(lifeAreaId) ON DELETE CASCADE;
