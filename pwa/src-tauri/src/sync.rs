use anyhow::{bail, Context, Result};
use rusqlite::{params, Connection};
use serde_json::{json, Value};
use std::collections::{HashMap, HashSet};
use std::sync::Arc;
use tokio::sync::Mutex;

pub const PROTOCOL_VERSION: i32 = 2;

#[derive(Clone)]
pub struct SyncEngine {
    pub api_url: String,
    pub token: Arc<Mutex<Option<String>>>,
    pub user_id: Arc<Mutex<Option<i32>>>,
    pub client: reqwest::Client,
}

impl SyncEngine {
    pub fn new(api_url: Option<String>) -> Self {
        Self {
            api_url: api_url.unwrap_or_else(|| "https://studyflow-nuux.onrender.com/api".to_string()),
            token: Arc::new(Mutex::new(None)),
            user_id: Arc::new(Mutex::new(None)),
            client: reqwest::Client::new(),
        }
    }

    pub async fn set_session(&self, token: Option<String>, user_id: Option<i32>) {
        let mut t = self.token.lock().await;
        *t = token;
        let mut u = self.user_id.lock().await;
        *u = user_id;
    }

    pub async fn sync(&self, conn: &mut Connection) -> Result<()> {
        let token = {
            let t = self.token.lock().await;
            t.clone()
        };
        let user_id = {
            let u = self.user_id.lock().await;
            *u
        };

        let token = match token {
            Some(t) if !t.trim().is_empty() => t,
            _ => return Ok(()),
        };

        let user_id = match user_id {
            Some(u) if u > 0 => u,
            _ => return Ok(()),
        };

        self.push_dirty_rows(conn, &token).await?;
        self.pull_since_cursor(conn, &token, user_id).await?;
        Ok(())
    }

    pub async fn push_dirty_rows(&self, conn: &mut Connection, token: &str) -> Result<()> {
        let life_areas = load_pending_life_areas(conn)?;
        let tasks = load_pending_tasks(conn)?;
        let sessions = load_pending_focus_sessions(conn)?;
        let exams = load_pending_exams(conn)?;
        let grades = load_pending_grades(conn)?;
        let reminders = load_pending_reminders(conn)?;
        let settings = load_pending_settings(conn)?;

        if life_areas.is_empty()
            && tasks.is_empty()
            && sessions.is_empty()
            && exams.is_empty()
            && grades.is_empty()
            && reminders.is_empty()
            && settings.is_empty()
        {
            return Ok(());
        }

        let mut payload = json!({
            "protocolVersion": PROTOCOL_VERSION
        });

        if !life_areas.is_empty() {
            payload["lifeAreas"] = json!(life_areas);
        }
        if !tasks.is_empty() {
            payload["tasks"] = json!(tasks);
        }
        if !sessions.is_empty() {
            payload["focusSessions"] = json!(sessions);
        }
        if !exams.is_empty() {
            payload["exams"] = json!(exams);
        }
        if !grades.is_empty() {
            payload["grades"] = json!(grades);
        }
        if !reminders.is_empty() {
            payload["reminders"] = json!(reminders);
        }
        if !settings.is_empty() {
            payload["settings"] = json!(settings);
        }

        let res = self
            .client
            .post(format!("{}/sync/push", self.api_url))
            .header("Authorization", format!("Bearer {}", token))
            .json(&payload)
            .send()
            .await
            .context("Failed to send sync push request")?;

        if res.status().as_u16() == 401 {
            bail!("Sync push unauthorized: token expired or invalid");
        }
        if !res.status().is_success() {
            bail!("Sync push failed with HTTP status: {}", res.status());
        }

        let body: Value = res.json().await.context("Failed to parse push response")?;
        let mut id_map = HashMap::new();
        if let Some(map) = body.get("idMap").and_then(|v| v.as_object()) {
            for (k, v) in map {
                if let Some(new_id) = v.as_i64() {
                    id_map.insert(k.clone(), new_id as i32);
                }
            }
        }

        let mut rejected_keys = HashSet::new();
        if let Some(rej) = body.get("rejected").and_then(|v| v.as_object()) {
            for k in rej.keys() {
                rejected_keys.insert(k.clone());
            }
        }

        apply_id_map(conn, &id_map)?;
        mark_pushed_rows_synced(
            conn,
            &life_areas,
            &tasks,
            &sessions,
            &exams,
            &grades,
            &reminders,
            &settings,
            &rejected_keys,
        )?;

        Ok(())
    }

    pub async fn pull_since_cursor(
        &self,
        conn: &mut Connection,
        token: &str,
        user_id: i32,
    ) -> Result<()> {
        let cursor_key = format!("sync.cursor.{}", user_id);
        let cursor: Option<String> = conn
            .query_row(
                "SELECT settingValue FROM UserSettings WHERE settingKey = ?1",
                params![cursor_key],
                |row| row.get(0),
            )
            .ok();

        let mut url = format!("{}/sync/pull", self.api_url);
        if let Some(ref c) = cursor {
            if !c.trim().is_empty() {
                let encoded = urlencoding::encode(c);
                url.push_str(&format!("?since={}", encoded));
            }
        }

        let res = self
            .client
            .get(&url)
            .header("Authorization", format!("Bearer {}", token))
            .send()
            .await
            .context("Failed to send sync pull request")?;

        if res.status().as_u16() == 401 {
            bail!("Sync pull unauthorized: token expired or invalid");
        }
        if !res.status().is_success() {
            bail!("Sync pull failed with HTTP status: {}", res.status());
        }

        let body: Value = res.json().await.context("Failed to parse pull response")?;

        merge_life_areas(conn, body.get("lifeAreas").and_then(|v| v.as_array()))?;
        merge_tasks(conn, body.get("tasks").and_then(|v| v.as_array()))?;
        merge_focus_sessions(conn, body.get("focusSessions").and_then(|v| v.as_array()))?;
        merge_exams(conn, body.get("exams").and_then(|v| v.as_array()))?;
        merge_grades(conn, body.get("grades").and_then(|v| v.as_array()))?;
        merge_reminders(conn, body.get("reminders").and_then(|v| v.as_array()))?;
        merge_settings(conn, body.get("settings").and_then(|v| v.as_array()))?;

        if let Some(server_time) = body.get("serverTime").and_then(|v| v.as_str()) {
            let now = chrono::Utc::now().to_rfc3339();
            conn.execute(
                "INSERT INTO UserSettings (settingKey, settingValue, updatedAt, syncStatus)
                 VALUES (?1, ?2, ?3, 'SYNCED')
                 ON CONFLICT(settingKey) DO UPDATE SET settingValue = ?2, updatedAt = ?3, syncStatus = 'SYNCED'",
                params![cursor_key, server_time, now],
            )?;
        }

        Ok(())
    }
}

// ---------------------------------------------------------------------------
// Helpers: URL encoding fallback
// ---------------------------------------------------------------------------
mod urlencoding {
    pub fn encode(s: &str) -> String {
        let mut encoded = String::new();
        for byte in s.bytes() {
            match byte {
                b'A'..=b'Z' | b'a'..=b'z' | b'0'..=b'9' | b'-' | b'_' | b'.' | b'~' => {
                    encoded.push(byte as char);
                }
                _ => {
                    encoded.push_str(&format!("%{:02X}", byte));
                }
            }
        }
        encoded
    }
}

// ---------------------------------------------------------------------------
// Push Outbox Loaders
// ---------------------------------------------------------------------------

pub fn load_pending_life_areas(conn: &Connection) -> Result<Vec<Value>> {
    let mut stmt = conn.prepare(
        "SELECT lifeAreaId, name, color, active, academicYear, updatedAt, deletedAt, clientUuid
         FROM LifeAreas WHERE syncStatus = 'PENDING_SYNC'",
    )?;
    let rows = stmt.query_map([], |row| {
        let id: i32 = row.get(0)?;
        let mut obj = json!({
            "localId": id,
            "name": row.get::<_, String>(1)?,
            "color": row.get::<_, Option<String>>(2)?,
            "active": row.get::<_, Option<i32>>(3)?.unwrap_or(1) != 0,
            "academicYear": row.get::<_, Option<String>>(4)?,
            "updatedAt": row.get::<_, Option<String>>(5)?,
            "deletedAt": row.get::<_, Option<String>>(6)?,
            "clientUuid": row.get::<_, Option<String>>(7)?
        });
        if id > 0 {
            obj["lifeAreaId"] = json!(id);
        }
        Ok(obj)
    })?;

    let mut out = Vec::new();
    for r in rows {
        out.push(r?);
    }
    Ok(out)
}

pub fn load_pending_tasks(conn: &Connection) -> Result<Vec<Value>> {
    let mut stmt = conn.prepare(
        "SELECT taskId, title, description, isCompleted, isHabit, priority,
                startDate, dueDate, lifeAreaId, projectId, orderIndex, updatedAt, deletedAt, clientUuid
         FROM Tasks WHERE syncStatus = 'PENDING_SYNC'",
    )?;
    let rows = stmt.query_map([], |row| {
        let id: i32 = row.get(0)?;
        let mut obj = json!({
            "localId": id,
            "title": row.get::<_, String>(1)?,
            "description": row.get::<_, Option<String>>(2)?,
            "isCompleted": row.get::<_, Option<i32>>(3)?.unwrap_or(0) == 1,
            "isHabit": row.get::<_, Option<i32>>(4)?.unwrap_or(0) == 1,
            "priority": row.get::<_, Option<String>>(5)?,
            "startDate": row.get::<_, Option<String>>(6)?,
            "dueDate": row.get::<_, Option<String>>(7)?,
            "lifeAreaId": row.get::<_, Option<i32>>(8)?,
            "projectId": row.get::<_, Option<i32>>(9)?,
            "orderIndex": row.get::<_, Option<i32>>(10)?.unwrap_or(0),
            "updatedAt": row.get::<_, Option<String>>(11)?,
            "deletedAt": row.get::<_, Option<String>>(12)?,
            "clientUuid": row.get::<_, Option<String>>(13)?
        });
        if id > 0 {
            obj["taskId"] = json!(id);
        }
        Ok(obj)
    })?;

    let mut out = Vec::new();
    for r in rows {
        out.push(r?);
    }
    Ok(out)
}

pub fn load_pending_focus_sessions(conn: &Connection) -> Result<Vec<Value>> {
    let mut stmt = conn.prepare(
        "SELECT sessionId, taskId, startedAt, endedAt, durationSeconds, mode, completed,
                updatedAt, deletedAt, clientUuid
         FROM FocusSessions WHERE syncStatus = 'PENDING_SYNC'",
    )?;
    let rows = stmt.query_map([], |row| {
        let id: i32 = row.get(0)?;
        let mut obj = json!({
            "localId": id,
            "taskId": row.get::<_, Option<i32>>(1)?,
            "startedAt": row.get::<_, Option<String>>(2)?,
            "endedAt": row.get::<_, Option<String>>(3)?,
            "durationSeconds": row.get::<_, Option<i32>>(4)?.unwrap_or(0),
            "mode": row.get::<_, Option<String>>(5)?,
            "completed": row.get::<_, Option<i32>>(6)?.unwrap_or(1) == 1,
            "updatedAt": row.get::<_, Option<String>>(7)?,
            "deletedAt": row.get::<_, Option<String>>(8)?,
            "clientUuid": row.get::<_, Option<String>>(9)?
        });
        if id > 0 {
            obj["sessionId"] = json!(id);
        }
        Ok(obj)
    })?;

    let mut out = Vec::new();
    for r in rows {
        out.push(r?);
    }
    Ok(out)
}

pub fn load_pending_exams(conn: &Connection) -> Result<Vec<Value>> {
    let mut stmt = conn.prepare(
        "SELECT examId, subjectId, nameExam, examType, examDate, classroom,
                updatedAt, deletedAt, clientUuid
         FROM Exams WHERE syncStatus = 'PENDING_SYNC'",
    )?;
    let rows = stmt.query_map([], |row| {
        let id: i32 = row.get(0)?;
        let mut obj = json!({
            "localId": id,
            "lifeAreaId": row.get::<_, Option<i32>>(1)?,
            "nameExam": row.get::<_, String>(2)?,
            "examType": row.get::<_, Option<String>>(3)?,
            "examDate": row.get::<_, Option<String>>(4)?,
            "classroom": row.get::<_, Option<String>>(5)?,
            "updatedAt": row.get::<_, Option<String>>(6)?,
            "deletedAt": row.get::<_, Option<String>>(7)?,
            "clientUuid": row.get::<_, Option<String>>(8)?
        });
        if id > 0 {
            obj["examId"] = json!(id);
        }
        Ok(obj)
    })?;

    let mut out = Vec::new();
    for r in rows {
        out.push(r?);
    }
    Ok(out)
}

pub fn load_pending_grades(conn: &Connection) -> Result<Vec<Value>> {
    let mut stmt = conn.prepare(
        "SELECT gradeId, subjectId, concept, score, weight, gradeDate,
                updatedAt, deletedAt, clientUuid
         FROM Grades WHERE syncStatus = 'PENDING_SYNC'",
    )?;
    let rows = stmt.query_map([], |row| {
        let id: i32 = row.get(0)?;
        let mut obj = json!({
            "localId": id,
            "lifeAreaId": row.get::<_, Option<i32>>(1)?,
            "concept": row.get::<_, Option<String>>(2)?,
            "score": row.get::<_, Option<f64>>(3)?,
            "weight": row.get::<_, Option<f64>>(4)?,
            "gradeDate": row.get::<_, Option<String>>(5)?,
            "updatedAt": row.get::<_, Option<String>>(6)?,
            "deletedAt": row.get::<_, Option<String>>(7)?,
            "clientUuid": row.get::<_, Option<String>>(8)?
        });
        if id > 0 {
            obj["gradeId"] = json!(id);
        }
        Ok(obj)
    })?;

    let mut out = Vec::new();
    for r in rows {
        out.push(r?);
    }
    Ok(out)
}

pub fn load_pending_reminders(conn: &Connection) -> Result<Vec<Value>> {
    let mut stmt = conn.prepare(
        "SELECT reminderId, message, dateTime, taskId, examId,
                updatedAt, deletedAt, clientUuid
         FROM Reminders WHERE syncStatus = 'PENDING_SYNC'",
    )?;
    let rows = stmt.query_map([], |row| {
        let id: i32 = row.get(0)?;
        let mut obj = json!({
            "localId": id,
            "message": row.get::<_, Option<String>>(1)?,
            "dateTime": row.get::<_, Option<String>>(2)?,
            "taskId": row.get::<_, Option<i32>>(3)?,
            "examId": row.get::<_, Option<i32>>(4)?,
            "updatedAt": row.get::<_, Option<String>>(5)?,
            "deletedAt": row.get::<_, Option<String>>(6)?,
            "clientUuid": row.get::<_, Option<String>>(7)?
        });
        if id > 0 {
            obj["reminderId"] = json!(id);
        }
        Ok(obj)
    })?;

    let mut out = Vec::new();
    for r in rows {
        out.push(r?);
    }
    Ok(out)
}

pub fn load_pending_settings(conn: &Connection) -> Result<Vec<Value>> {
    let mut stmt = conn.prepare(
        "SELECT settingKey, settingValue, updatedAt
         FROM UserSettings WHERE syncStatus = 'PENDING_SYNC'",
    )?;
    let rows = stmt.query_map([], |row| {
        Ok(json!({
            "settingKey": row.get::<_, String>(0)?,
            "settingValue": row.get::<_, Option<String>>(1)?,
            "updatedAt": row.get::<_, Option<String>>(2)?
        }))
    })?;

    let mut out = Vec::new();
    for r in rows {
        out.push(r?);
    }
    Ok(out)
}

// ---------------------------------------------------------------------------
// ID Mapping & Post-Push Mark Synced
// ---------------------------------------------------------------------------

pub fn apply_id_map(conn: &Connection, id_map: &HashMap<String, i32>) -> Result<()> {
    if id_map.is_empty() {
        return Ok(());
    }

    for (k, &new_id) in id_map {
        let parts: Vec<&str> = k.split(':').collect();
        if parts.len() != 2 {
            continue;
        }
        let entity = parts[0];
        let old_id: i32 = match parts[1].parse() {
            Ok(id) => id,
            Err(_) => continue,
        };
        if old_id == new_id || old_id >= 0 {
            continue;
        }

        match entity {
            "lifeArea" => {
                conn.execute(
                    "UPDATE LifeAreas SET lifeAreaId = ?1 WHERE lifeAreaId = ?2",
                    params![new_id, old_id],
                )?;
                conn.execute(
                    "UPDATE Tasks SET lifeAreaId = ?1 WHERE lifeAreaId = ?2",
                    params![new_id, old_id],
                )?;
                conn.execute(
                    "UPDATE Exams SET subjectId = ?1 WHERE subjectId = ?2",
                    params![new_id, old_id],
                )?;
                conn.execute(
                    "UPDATE Grades SET subjectId = ?1 WHERE subjectId = ?2",
                    params![new_id, old_id],
                )?;
            }
            "task" => {
                conn.execute(
                    "UPDATE Tasks SET taskId = ?1 WHERE taskId = ?2",
                    params![new_id, old_id],
                )?;
                conn.execute(
                    "UPDATE FocusSessions SET taskId = ?1 WHERE taskId = ?2",
                    params![new_id, old_id],
                )?;
                conn.execute(
                    "UPDATE Reminders SET taskId = ?1 WHERE taskId = ?2",
                    params![new_id, old_id],
                )?;
            }
            "focusSession" => {
                conn.execute(
                    "UPDATE FocusSessions SET sessionId = ?1 WHERE sessionId = ?2",
                    params![new_id, old_id],
                )?;
            }
            "exam" => {
                conn.execute(
                    "UPDATE Exams SET examId = ?1 WHERE examId = ?2",
                    params![new_id, old_id],
                )?;
                conn.execute(
                    "UPDATE Reminders SET examId = ?1 WHERE examId = ?2",
                    params![new_id, old_id],
                )?;
            }
            "grade" => {
                conn.execute(
                    "UPDATE Grades SET gradeId = ?1 WHERE gradeId = ?2",
                    params![new_id, old_id],
                )?;
            }
            "reminder" => {
                conn.execute(
                    "UPDATE Reminders SET reminderId = ?1 WHERE reminderId = ?2",
                    params![new_id, old_id],
                )?;
            }
            _ => {}
        }
    }

    Ok(())
}

fn mark_pushed_rows_synced(
    conn: &Connection,
    life_areas: &[Value],
    tasks: &[Value],
    sessions: &[Value],
    exams: &[Value],
    grades: &[Value],
    reminders: &[Value],
    settings: &[Value],
    rejected: &HashSet<String>,
) -> Result<()> {
    for r in life_areas {
        if let Some(id) = r.get("localId").and_then(|v| v.as_i64()) {
            let key = format!("lifeArea:{}", id);
            if !rejected.contains(&key) {
                conn.execute(
                    "UPDATE LifeAreas SET syncStatus = 'SYNCED' WHERE lifeAreaId = ?1",
                    params![id],
                )?;
            }
        }
    }

    for r in tasks {
        if let Some(id) = r.get("localId").and_then(|v| v.as_i64()) {
            let key = format!("task:{}", id);
            if !rejected.contains(&key) {
                conn.execute(
                    "UPDATE Tasks SET syncStatus = 'SYNCED' WHERE taskId = ?1",
                    params![id],
                )?;
            }
        }
    }

    for r in sessions {
        if let Some(id) = r.get("localId").and_then(|v| v.as_i64()) {
            let key = format!("focusSession:{}", id);
            if !rejected.contains(&key) {
                conn.execute(
                    "UPDATE FocusSessions SET syncStatus = 'SYNCED' WHERE sessionId = ?1",
                    params![id],
                )?;
            }
        }
    }

    for r in exams {
        if let Some(id) = r.get("localId").and_then(|v| v.as_i64()) {
            let key = format!("exam:{}", id);
            if !rejected.contains(&key) {
                conn.execute(
                    "UPDATE Exams SET syncStatus = 'SYNCED' WHERE examId = ?1",
                    params![id],
                )?;
            }
        }
    }

    for r in grades {
        if let Some(id) = r.get("localId").and_then(|v| v.as_i64()) {
            let key = format!("grade:{}", id);
            if !rejected.contains(&key) {
                conn.execute(
                    "UPDATE Grades SET syncStatus = 'SYNCED' WHERE gradeId = ?1",
                    params![id],
                )?;
            }
        }
    }

    for r in reminders {
        if let Some(id) = r.get("localId").and_then(|v| v.as_i64()) {
            let key = format!("reminder:{}", id);
            if !rejected.contains(&key) {
                conn.execute(
                    "UPDATE Reminders SET syncStatus = 'SYNCED' WHERE reminderId = ?1",
                    params![id],
                )?;
            }
        }
    }

    for r in settings {
        if let Some(k) = r.get("settingKey").and_then(|v| v.as_str()) {
            let key = format!("setting:{}", k);
            if !rejected.contains(&key) {
                conn.execute(
                    "UPDATE UserSettings SET syncStatus = 'SYNCED' WHERE settingKey = ?1",
                    params![k],
                )?;
            }
        }
    }

    Ok(())
}

// ---------------------------------------------------------------------------
// Conflict & LWW Evaluation
// ---------------------------------------------------------------------------

fn should_apply_remote(
    conn: &Connection,
    table: &str,
    pk_col: &str,
    pk_val: i32,
    entity_type: &str,
    summary_col: &str,
    remote_updated_at: Option<&str>,
    remote_summary: Option<&str>,
) -> Result<bool> {
    let sql = format!(
        "SELECT updatedAt, syncStatus, {} FROM {} WHERE {} = ?1",
        summary_col, table, pk_col
    );
    let mut stmt = conn.prepare(&sql)?;
    let local_row = stmt
        .query_row(params![pk_val], |row| {
            Ok((
                row.get::<_, Option<String>>(0)?,
                row.get::<_, Option<String>>(1)?,
                row.get::<_, Option<String>>(2)?,
            ))
        })
        .ok();

    match local_row {
        None => Ok(true), // New row from remote
        Some((local_updated_at, local_sync_status, local_summary)) => {
            let is_dirty = local_sync_status.as_deref() == Some("PENDING_SYNC");

            let remote_newer = match (local_updated_at.as_deref(), remote_updated_at) {
                (Some(loc), Some(rem)) => rem >= loc,
                (None, Some(_)) => true,
                _ => true,
            };

            if is_dirty && remote_newer {
                // Record conflict when overwriting dirty local data
                let _ = crate::conflicts::record_conflict(
                    conn,
                    entity_type,
                    pk_val,
                    local_summary.as_deref(),
                    remote_summary,
                    local_updated_at.as_deref(),
                    remote_updated_at,
                );
            }

            if is_dirty && !remote_newer {
                // Local dirty row is newer than remote; preserve local
                Ok(false)
            } else {
                Ok(true)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Merge Upserts
// ---------------------------------------------------------------------------

fn merge_life_areas(conn: &Connection, list: Option<&Vec<Value>>) -> Result<()> {
    let list = match list {
        Some(l) => l,
        None => return Ok(()),
    };

    for row in list {
        let id = match row.get("lifeAreaId").and_then(|v| v.as_i64()) {
            Some(i) => i as i32,
            None => continue,
        };
        let name = row.get("name").and_then(|v| v.as_str()).unwrap_or("");
        let updated_at = row.get("updatedAt").and_then(|v| v.as_str());

        if !should_apply_remote(conn, "LifeAreas", "lifeAreaId", id, "LifeArea", "name", updated_at, Some(name))? {
            continue;
        }

        conn.execute(
            "INSERT INTO LifeAreas (lifeAreaId, name, color, active, academicYear, updatedAt, deletedAt, syncStatus, clientUuid)
             VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, 'SYNCED', ?8)
             ON CONFLICT(lifeAreaId) DO UPDATE SET
                name = ?2, color = ?3, active = ?4, academicYear = ?5,
                updatedAt = ?6, deletedAt = ?7, syncStatus = 'SYNCED', clientUuid = ?8",
            params![
                id,
                name,
                row.get("color").and_then(|v| v.as_str()),
                row.get("active").and_then(|v| v.as_bool()).map(|b| if b { 1 } else { 0 }).unwrap_or(1),
                row.get("academicYear").and_then(|v| v.as_str()),
                updated_at,
                row.get("deletedAt").and_then(|v| v.as_str()),
                row.get("clientUuid").and_then(|v| v.as_str()),
            ],
        )?;
    }
    Ok(())
}

fn merge_tasks(conn: &Connection, list: Option<&Vec<Value>>) -> Result<()> {
    let list = match list {
        Some(l) => l,
        None => return Ok(()),
    };

    for row in list {
        let id = match row.get("taskId").and_then(|v| v.as_i64()) {
            Some(i) => i as i32,
            None => continue,
        };
        let title = row.get("title").and_then(|v| v.as_str()).unwrap_or("");
        let updated_at = row.get("updatedAt").and_then(|v| v.as_str());

        if !should_apply_remote(conn, "Tasks", "taskId", id, "Task", "title", updated_at, Some(title))? {
            continue;
        }

        conn.execute(
            "INSERT INTO Tasks (taskId, title, description, isCompleted, isHabit, priority,
                                startDate, dueDate, lifeAreaId, projectId, orderIndex,
                                updatedAt, deletedAt, syncStatus, clientUuid)
             VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, ?10, ?11, ?12, ?13, 'SYNCED', ?14)
             ON CONFLICT(taskId) DO UPDATE SET
                title = ?2, description = ?3, isCompleted = ?4, isHabit = ?5, priority = ?6,
                startDate = ?7, dueDate = ?8, lifeAreaId = ?9, projectId = ?10, orderIndex = ?11,
                updatedAt = ?12, deletedAt = ?13, syncStatus = 'SYNCED', clientUuid = ?14",
            params![
                id,
                title,
                row.get("description").and_then(|v| v.as_str()),
                row.get("isCompleted").and_then(|v| v.as_bool()).map(|b| if b { 1 } else { 0 }).unwrap_or(0),
                row.get("isHabit").and_then(|v| v.as_bool()).map(|b| if b { 1 } else { 0 }).unwrap_or(0),
                row.get("priority").and_then(|v| v.as_str()),
                row.get("startDate").and_then(|v| v.as_str()),
                row.get("dueDate").and_then(|v| v.as_str()),
                row.get("lifeAreaId").and_then(|v| v.as_i64()).map(|i| i as i32),
                row.get("projectId").and_then(|v| v.as_i64()).map(|i| i as i32),
                row.get("orderIndex").and_then(|v| v.as_i64()).map(|i| i as i32).unwrap_or(0),
                updated_at,
                row.get("deletedAt").and_then(|v| v.as_str()),
                row.get("clientUuid").and_then(|v| v.as_str()),
            ],
        )?;
    }
    Ok(())
}

fn merge_focus_sessions(conn: &Connection, list: Option<&Vec<Value>>) -> Result<()> {
    let list = match list {
        Some(l) => l,
        None => return Ok(()),
    };

    for row in list {
        let id = match row.get("sessionId").and_then(|v| v.as_i64()) {
            Some(i) => i as i32,
            None => continue,
        };
        let started_at = row.get("startedAt").and_then(|v| v.as_str());
        let updated_at = row.get("updatedAt").and_then(|v| v.as_str());

        if !should_apply_remote(conn, "FocusSessions", "sessionId", id, "FocusSession", "startedAt", updated_at, started_at)? {
            continue;
        }

        conn.execute(
            "INSERT INTO FocusSessions (sessionId, taskId, startedAt, endedAt, durationSeconds, mode, completed,
                                        updatedAt, deletedAt, syncStatus, clientUuid)
             VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, 'SYNCED', ?10)
             ON CONFLICT(sessionId) DO UPDATE SET
                taskId = ?2, startedAt = ?3, endedAt = ?4, durationSeconds = ?5, mode = ?6,
                completed = ?7, updatedAt = ?8, deletedAt = ?9, syncStatus = 'SYNCED', clientUuid = ?10",
            params![
                id,
                row.get("taskId").and_then(|v| v.as_i64()).map(|i| i as i32),
                started_at,
                row.get("endedAt").and_then(|v| v.as_str()),
                row.get("durationSeconds").and_then(|v| v.as_i64()).map(|i| i as i32).unwrap_or(0),
                row.get("mode").and_then(|v| v.as_str()),
                row.get("completed").and_then(|v| v.as_bool()).map(|b| if b { 1 } else { 0 }).unwrap_or(1),
                updated_at,
                row.get("deletedAt").and_then(|v| v.as_str()),
                row.get("clientUuid").and_then(|v| v.as_str()),
            ],
        )?;
    }
    Ok(())
}

fn merge_exams(conn: &Connection, list: Option<&Vec<Value>>) -> Result<()> {
    let list = match list {
        Some(l) => l,
        None => return Ok(()),
    };

    for row in list {
        let id = match row.get("examId").and_then(|v| v.as_i64()) {
            Some(i) => i as i32,
            None => continue,
        };
        let name_exam = row.get("nameExam").and_then(|v| v.as_str()).unwrap_or("");
        let updated_at = row.get("updatedAt").and_then(|v| v.as_str());

        if !should_apply_remote(conn, "Exams", "examId", id, "Exam", "nameExam", updated_at, Some(name_exam))? {
            continue;
        }

        conn.execute(
            "INSERT INTO Exams (examId, subjectId, nameExam, examType, examDate, classroom,
                                updatedAt, deletedAt, syncStatus, clientUuid)
             VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, 'SYNCED', ?9)
             ON CONFLICT(examId) DO UPDATE SET
                subjectId = ?2, nameExam = ?3, examType = ?4, examDate = ?5,
                classroom = ?6, updatedAt = ?7, deletedAt = ?8, syncStatus = 'SYNCED', clientUuid = ?9",
            params![
                id,
                row.get("lifeAreaId").and_then(|v| v.as_i64()).map(|i| i as i32),
                name_exam,
                row.get("examType").and_then(|v| v.as_str()),
                row.get("examDate").and_then(|v| v.as_str()),
                row.get("classroom").and_then(|v| v.as_str()),
                updated_at,
                row.get("deletedAt").and_then(|v| v.as_str()),
                row.get("clientUuid").and_then(|v| v.as_str()),
            ],
        )?;
    }
    Ok(())
}

fn merge_grades(conn: &Connection, list: Option<&Vec<Value>>) -> Result<()> {
    let list = match list {
        Some(l) => l,
        None => return Ok(()),
    };

    for row in list {
        let id = match row.get("gradeId").and_then(|v| v.as_i64()) {
            Some(i) => i as i32,
            None => continue,
        };
        let concept = row.get("concept").and_then(|v| v.as_str()).unwrap_or("");
        let updated_at = row.get("updatedAt").and_then(|v| v.as_str());

        if !should_apply_remote(conn, "Grades", "gradeId", id, "Grade", "concept", updated_at, Some(concept))? {
            continue;
        }

        conn.execute(
            "INSERT INTO Grades (gradeId, subjectId, concept, score, weight, gradeDate,
                                 updatedAt, deletedAt, syncStatus, clientUuid)
             VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, 'SYNCED', ?9)
             ON CONFLICT(gradeId) DO UPDATE SET
                subjectId = ?2, concept = ?3, score = ?4, weight = ?5,
                gradeDate = ?6, updatedAt = ?7, deletedAt = ?8, syncStatus = 'SYNCED', clientUuid = ?9",
            params![
                id,
                row.get("lifeAreaId").and_then(|v| v.as_i64()).map(|i| i as i32),
                concept,
                row.get("score").and_then(|v| v.as_f64()),
                row.get("weight").and_then(|v| v.as_f64()),
                row.get("gradeDate").and_then(|v| v.as_str()),
                updated_at,
                row.get("deletedAt").and_then(|v| v.as_str()),
                row.get("clientUuid").and_then(|v| v.as_str()),
            ],
        )?;
    }
    Ok(())
}

fn merge_reminders(conn: &Connection, list: Option<&Vec<Value>>) -> Result<()> {
    let list = match list {
        Some(l) => l,
        None => return Ok(()),
    };

    for row in list {
        let id = match row.get("reminderId").and_then(|v| v.as_i64()) {
            Some(i) => i as i32,
            None => continue,
        };
        let message = row.get("message").and_then(|v| v.as_str()).unwrap_or("");
        let updated_at = row.get("updatedAt").and_then(|v| v.as_str());

        if !should_apply_remote(conn, "Reminders", "reminderId", id, "Reminder", "message", updated_at, Some(message))? {
            continue;
        }

        conn.execute(
            "INSERT INTO Reminders (reminderId, message, dateTime, taskId, examId,
                                    updatedAt, deletedAt, syncStatus, clientUuid)
             VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, 'SYNCED', ?8)
             ON CONFLICT(reminderId) DO UPDATE SET
                message = ?2, dateTime = ?3, taskId = ?4, examId = ?5,
                updatedAt = ?6, deletedAt = ?7, syncStatus = 'SYNCED', clientUuid = ?8",
            params![
                id,
                message,
                row.get("dateTime").and_then(|v| v.as_str()),
                row.get("taskId").and_then(|v| v.as_i64()).map(|i| i as i32),
                row.get("examId").and_then(|v| v.as_i64()).map(|i| i as i32),
                updated_at,
                row.get("deletedAt").and_then(|v| v.as_str()),
                row.get("clientUuid").and_then(|v| v.as_str()),
            ],
        )?;
    }
    Ok(())
}

fn merge_settings(conn: &Connection, list: Option<&Vec<Value>>) -> Result<()> {
    let list = match list {
        Some(l) => l,
        None => return Ok(()),
    };

    for row in list {
        let key = match row.get("settingKey").and_then(|v| v.as_str()) {
            Some(k) => k,
            None => continue,
        };
        let val = row.get("settingValue").and_then(|v| v.as_str());
        let updated_at = row.get("updatedAt").and_then(|v| v.as_str());

        conn.execute(
            "INSERT INTO UserSettings (settingKey, settingValue, updatedAt, syncStatus)
             VALUES (?1, ?2, ?3, 'SYNCED')
             ON CONFLICT(settingKey) DO UPDATE SET
                settingValue = ?2, updatedAt = ?3, syncStatus = 'SYNCED'",
            params![key, val, updated_at],
        )?;
    }
    Ok(())
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::db::init_schema;

    #[test]
    fn test_apply_id_map_cascades() {
        let conn = Connection::open_in_memory().unwrap();
        init_schema(&conn).unwrap();

        // Insert local offline lifeArea (-1) and task (-2) pointing to -1
        conn.execute(
            "INSERT INTO LifeAreas (lifeAreaId, name, syncStatus) VALUES (-1, 'Math', 'PENDING_SYNC')",
            [],
        ).unwrap();
        conn.execute(
            "INSERT INTO Tasks (taskId, title, lifeAreaId, syncStatus) VALUES (-2, 'Homework', -1, 'PENDING_SYNC')",
            [],
        ).unwrap();

        let mut id_map = HashMap::new();
        id_map.insert("lifeArea:-1".to_string(), 101);
        id_map.insert("task:-2".to_string(), 202);

        apply_id_map(&conn, &id_map).unwrap();

        // Verify LifeAreas remapped
        let area_id: i32 = conn.query_row(
            "SELECT lifeAreaId FROM LifeAreas WHERE name = 'Math'",
            [],
            |row| row.get(0),
        ).unwrap();
        assert_eq!(area_id, 101);

        // Verify Task remapped and FK remapped
        let (task_id, fk_id): (i32, i32) = conn.query_row(
            "SELECT taskId, lifeAreaId FROM Tasks WHERE title = 'Homework'",
            [],
            |row| Ok((row.get(0)?, row.get(1)?)),
        ).unwrap();
        assert_eq!(task_id, 202);
        assert_eq!(fk_id, 101);
    }

    #[test]
    fn test_lww_and_conflict_detection() {
        let conn = Connection::open_in_memory().unwrap();
        init_schema(&conn).unwrap();

        // Local task is dirty with updatedAt = 2026-10-22T10:00:00Z
        conn.execute(
            "INSERT INTO Tasks (taskId, title, updatedAt, syncStatus) VALUES (50, 'Local edit', '2026-10-22T10:00:00Z', 'PENDING_SYNC')",
            [],
        ).unwrap();

        // Remote has newer updatedAt = 2026-10-22T10:05:00Z
        let should_apply = should_apply_remote(
            &conn,
            "Tasks",
            "taskId",
            50,
            "Task",
            "title",
            Some("2026-10-22T10:05:00Z"),
            Some("Remote edit"),
        ).unwrap();

        assert!(should_apply);

        // Conflict should have been logged in SyncConflicts table!
        let conflict_count: i32 = conn.query_row(
            "SELECT COUNT(*) FROM SyncConflicts WHERE entityId = 50",
            [],
            |row| row.get(0),
        ).unwrap();
        assert_eq!(conflict_count, 1);
    }
}
