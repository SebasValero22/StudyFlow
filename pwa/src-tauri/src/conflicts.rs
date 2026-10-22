use anyhow::Result;
use rusqlite::Connection;
use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct SyncConflict {
    pub conflict_id: i32,
    pub entity_type: String,
    pub entity_id: i32,
    pub local_summary: Option<String>,
    pub remote_summary: Option<String>,
    pub local_updated_at: Option<String>,
    pub remote_updated_at: Option<String>,
    pub detected_at: String,
    pub dismissed: bool,
}

pub fn list_conflicts(conn: &Connection) -> Result<Vec<SyncConflict>> {
    let mut stmt = conn.prepare(
        "SELECT conflictId, entityType, entityId, localSummary, remoteSummary,
                localUpdatedAt, remoteUpdatedAt, detectedAt, dismissed
         FROM SyncConflicts
         WHERE dismissed = 0
         ORDER BY conflictId DESC
         LIMIT 50"
    )?;

    let rows = stmt.query_map([], |row| {
        Ok(SyncConflict {
            conflict_id: row.get(0)?,
            entity_type: row.get(1)?,
            entity_id: row.get(2)?,
            local_summary: row.get(3)?,
            remote_summary: row.get(4)?,
            local_updated_at: row.get(5)?,
            remote_updated_at: row.get(6)?,
            detected_at: row.get(7)?,
            dismissed: row.get::<_, i32>(8)? != 0,
        })
    })?;

    let mut list = Vec::new();
    for r in rows {
        list.push(r?);
    }
    Ok(list)
}

pub fn active_conflicts_count(conn: &Connection) -> Result<i32> {
    let count: i32 = conn.query_row(
        "SELECT COUNT(*) FROM SyncConflicts WHERE dismissed = 0",
        [],
        |row| row.get(0),
    ).unwrap_or(0);
    Ok(count)
}

pub fn record_conflict(
    conn: &Connection,
    entity_type: &str,
    entity_id: i32,
    local_summary: Option<&str>,
    remote_summary: Option<&str>,
    local_updated_at: Option<&str>,
    remote_updated_at: Option<&str>,
) -> Result<()> {
    let now = chrono::Utc::now().to_rfc3339();
    conn.execute(
        "INSERT INTO SyncConflicts (entityType, entityId, localSummary, remoteSummary,
                                    localUpdatedAt, remoteUpdatedAt, detectedAt, dismissed)
         VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, 0)",
        rusqlite::params![
            entity_type,
            entity_id,
            local_summary,
            remote_summary,
            local_updated_at,
            remote_updated_at,
            now
        ],
    )?;
    Ok(())
}

pub fn dismiss_all(conn: &Connection) -> Result<()> {
    conn.execute("UPDATE SyncConflicts SET dismissed = 1 WHERE dismissed = 0", [])?;
    Ok(())
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::db::init_schema;

    #[test]
    fn test_conflict_lifecycle() {
        let conn = Connection::open_in_memory().unwrap();
        init_schema(&conn).unwrap();

        assert_eq!(active_conflicts_count(&conn).unwrap(), 0);

        record_conflict(
            &conn,
            "Tasks",
            42,
            Some("My local task"),
            Some("Remote task edit"),
            Some("2026-10-22T10:00:00Z"),
            Some("2026-10-22T10:05:00Z"),
        ).unwrap();

        assert_eq!(active_conflicts_count(&conn).unwrap(), 1);
        let list = list_conflicts(&conn).unwrap();
        assert_eq!(list.len(), 1);
        assert_eq!(list[0].entity_type, "Tasks");
        assert_eq!(list[0].entity_id, 42);

        dismiss_all(&conn).unwrap();
        assert_eq!(active_conflicts_count(&conn).unwrap(), 0);
    }
}
