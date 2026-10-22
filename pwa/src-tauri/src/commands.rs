use std::sync::Mutex;
use tauri::State;
use crate::conflicts::{dismiss_all, list_conflicts, SyncConflict};
use crate::db::get_connection;
use crate::sound::{play_chime as play_sound, ChimeType};
use crate::sync::SyncEngine;
use crate::timer::{StatusResponse, Timer};

pub struct AppState {
    pub timer: Mutex<Timer>,
    pub sync_engine: SyncEngine,
}

#[tauri::command]
pub fn get_timer_status(state: State<'_, AppState>) -> StatusResponse {
    let timer = state.timer.lock().unwrap();
    timer.status()
}

#[tauri::command]
pub fn timer_start(
    state: State<'_, AppState>,
    task_id: Option<i32>,
    task_title: Option<String>,
    task_color: Option<String>,
) -> StatusResponse {
    let mut timer = state.timer.lock().unwrap();
    timer.start(task_id, task_title, task_color);
    timer.status()
}

#[tauri::command]
pub fn timer_pause(state: State<'_, AppState>) -> StatusResponse {
    let mut timer = state.timer.lock().unwrap();
    timer.pause();
    timer.status()
}

#[tauri::command]
pub fn timer_resume(state: State<'_, AppState>) -> StatusResponse {
    let mut timer = state.timer.lock().unwrap();
    timer.resume();
    timer.status()
}

#[tauri::command]
pub fn timer_stop(state: State<'_, AppState>) -> StatusResponse {
    let mut timer = state.timer.lock().unwrap();
    timer.stop();
    timer.status()
}

#[tauri::command]
pub fn timer_skip(state: State<'_, AppState>) -> StatusResponse {
    let mut timer = state.timer.lock().unwrap();
    let _ = timer.skip();
    timer.status()
}

#[tauri::command]
pub fn play_chime(kind: String) {
    let chime = match kind.to_lowercase().as_str() {
        "focus" | "focuscomplete" => ChimeType::FocusComplete,
        "break" | "breakcomplete" => ChimeType::BreakComplete,
        _ => ChimeType::SessionStart,
    };
    play_sound(chime);
}

#[tauri::command]
pub fn get_sync_conflicts() -> Result<Vec<SyncConflict>, String> {
    let conn = get_connection().map_err(|e| e.to_string())?;
    list_conflicts(&conn).map_err(|e| e.to_string())
}

#[tauri::command]
pub fn dismiss_sync_conflicts() -> Result<(), String> {
    let conn = get_connection().map_err(|e| e.to_string())?;
    dismiss_all(&conn).map_err(|e| e.to_string())
}

#[tauri::command]
pub async fn set_auth_session(
    state: State<'_, AppState>,
    token: Option<String>,
    user_id: Option<i32>,
) -> Result<(), String> {
    state.sync_engine.set_session(token, user_id).await;
    Ok(())
}

#[tauri::command]
pub async fn trigger_sync(state: State<'_, AppState>) -> Result<(), String> {
    let mut conn = get_connection().map_err(|e| e.to_string())?;
    state
        .sync_engine
        .sync(&mut conn)
        .await
        .map_err(|e| e.to_string())
}
