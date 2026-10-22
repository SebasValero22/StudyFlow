use anyhow::{Context, Result};
use rusqlite::Connection;
use std::fs;
use std::path::PathBuf;

pub fn get_db_path() -> Result<PathBuf> {
    let base_dir = dirs::home_dir()
        .or_else(dirs::data_dir)
        .context("Could not determine user directory")?;
    let vault_dir = base_dir.join("StudyFlow").join("vault");
    fs::create_dir_all(&vault_dir)?;
    Ok(vault_dir.join("studyflow.db"))
}

pub fn get_connection() -> Result<Connection> {
    let path = get_db_path()?;
    let conn = Connection::open(path)?;
    Ok(conn)
}

pub fn init_schema(conn: &Connection) -> Result<()> {
    conn.execute_batch(
        "CREATE TABLE IF NOT EXISTS LifeAreas (
            lifeAreaId INTEGER PRIMARY KEY,
            name TEXT NOT NULL,
            color TEXT,
            active INTEGER DEFAULT 1,
            academicYear TEXT,
            updatedAt TEXT,
            deletedAt TEXT,
            syncStatus TEXT DEFAULT 'SYNCED',
            clientUuid TEXT
        );

        CREATE TABLE IF NOT EXISTS Tasks (
            taskId INTEGER PRIMARY KEY,
            title TEXT NOT NULL,
            description TEXT,
            isCompleted INTEGER DEFAULT 0,
            isHabit INTEGER DEFAULT 0,
            priority TEXT,
            startDate TEXT,
            dueDate TEXT,
            lifeAreaId INTEGER,
            projectId INTEGER,
            orderIndex INTEGER DEFAULT 0,
            updatedAt TEXT,
            deletedAt TEXT,
            syncStatus TEXT DEFAULT 'SYNCED',
            clientUuid TEXT,
            trigger TEXT,
            microStep TEXT,
            energyBucket TEXT
        );

        CREATE TABLE IF NOT EXISTS FocusSessions (
            sessionId INTEGER PRIMARY KEY,
            taskId INTEGER,
            startedAt TEXT,
            endedAt TEXT,
            durationSeconds INTEGER,
            mode TEXT,
            completed INTEGER DEFAULT 1,
            updatedAt TEXT,
            deletedAt TEXT,
            syncStatus TEXT DEFAULT 'SYNCED',
            clientUuid TEXT
        );

        CREATE TABLE IF NOT EXISTS Exams (
            examId INTEGER PRIMARY KEY,
            subjectId INTEGER,
            nameExam TEXT NOT NULL,
            examType TEXT,
            examDate TEXT,
            classroom TEXT,
            updatedAt TEXT,
            deletedAt TEXT,
            syncStatus TEXT DEFAULT 'SYNCED',
            clientUuid TEXT
        );

        CREATE TABLE IF NOT EXISTS Grades (
            gradeId INTEGER PRIMARY KEY,
            subjectId INTEGER,
            concept TEXT,
            score REAL,
            weight REAL,
            gradeDate TEXT,
            updatedAt TEXT,
            deletedAt TEXT,
            syncStatus TEXT DEFAULT 'SYNCED',
            clientUuid TEXT
        );

        CREATE TABLE IF NOT EXISTS Reminders (
            reminderId INTEGER PRIMARY KEY,
            message TEXT,
            dateTime TEXT,
            taskId INTEGER,
            examId INTEGER,
            updatedAt TEXT,
            deletedAt TEXT,
            syncStatus TEXT DEFAULT 'SYNCED',
            clientUuid TEXT
        );

        CREATE TABLE IF NOT EXISTS UserSettings (
            settingKey TEXT PRIMARY KEY,
            settingValue TEXT,
            updatedAt TEXT,
            syncStatus TEXT DEFAULT 'SYNCED'
        );

        CREATE TABLE IF NOT EXISTS SyncConflicts (
            conflictId INTEGER PRIMARY KEY AUTOINCREMENT,
            entityType TEXT NOT NULL,
            entityId INTEGER NOT NULL,
            localSummary TEXT,
            remoteSummary TEXT,
            localUpdatedAt TEXT,
            remoteUpdatedAt TEXT,
            detectedAt TEXT,
            dismissed INTEGER DEFAULT 0
        );"
    )?;

    Ok(())
}

pub fn next_local_id(conn: &Connection, table: &str, pk_col: &str) -> Result<i32> {
    let sql = format!("SELECT MIN({}) FROM {} WHERE {} < 0", pk_col, table, pk_col);
    let mut stmt = conn.prepare(&sql)?;
    let min_val: Option<i32> = stmt.query_row([], |row| row.get(0)).unwrap_or(None);
    match min_val {
        Some(m) if m < 0 => Ok(m - 1),
        _ => Ok(-1),
    }
}

pub fn wipe_local_data(conn: &Connection) -> Result<()> {
    conn.execute_batch(
        "DELETE FROM LifeAreas;
         DELETE FROM Tasks;
         DELETE FROM FocusSessions;
         DELETE FROM Exams;
         DELETE FROM Grades;
         DELETE FROM Reminders;
         DELETE FROM UserSettings;
         DELETE FROM SyncConflicts;"
    )?;
    Ok(())
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_schema_init_and_negative_ids() {
        let conn = Connection::open_in_memory().unwrap();
        init_schema(&conn).unwrap();

        let id1 = next_local_id(&conn, "Tasks", "taskId").unwrap();
        assert_eq!(id1, -1);

        conn.execute(
            "INSERT INTO Tasks (taskId, title, syncStatus) VALUES (?1, ?2, 'PENDING_SYNC')",
            (-1, "Offline task"),
        ).unwrap();

        let id2 = next_local_id(&conn, "Tasks", "taskId").unwrap();
        assert_eq!(id2, -2);
    }
}
