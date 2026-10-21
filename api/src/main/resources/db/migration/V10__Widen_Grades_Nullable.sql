-- V10__Widen_Grades_Nullable.sql
-- Grade score/weight are optional (desktop allows blank values); NOT NULL
-- would reject synced rows and roll back the whole push batch.
ALTER TABLE Grades MODIFY score DOUBLE NULL;
ALTER TABLE Grades MODIFY weight DOUBLE NULL;
