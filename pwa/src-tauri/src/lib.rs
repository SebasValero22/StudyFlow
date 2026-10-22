pub mod db;
pub mod conflicts;
pub mod timer;
pub mod sound;
pub mod sync;
pub mod commands;

use std::sync::Mutex;
use tauri::{
    menu::{Menu, MenuItem},
    tray::{MouseButton, MouseButtonState, TrayIconBuilder, TrayIconEvent},
    Manager, WindowEvent,
};
use tauri_plugin_global_shortcut::ShortcutState;

#[cfg_attr(mobile, tauri::mobile_entry_point)]
pub fn run() {
    tauri::Builder::default()
        .plugin(tauri_plugin_notification::init())
        .plugin(
            tauri_plugin_global_shortcut::Builder::new()
                .with_shortcut("Ctrl+Space")
                .expect("Failed to parse Ctrl+Space shortcut")
                .with_handler(|app, _shortcut, event| {
                    if event.state() == ShortcutState::Pressed {
                        if let Some(window) = app.get_webview_window("main") {
                            let is_visible = window.is_visible().unwrap_or(false);
                            if is_visible {
                                let _ = window.hide();
                            } else {
                                let _ = window.show();
                                let _ = window.set_focus();
                                let _ = window.unminimize();
                            }
                        }
                    }
                })
                .build(),
        )
        .setup(|app| {
            // Initialize local SQLite database
            let conn = db::get_connection().expect("Failed to connect to SQLite database");
            db::init_schema(&conn).expect("Failed to initialize database schema");

            // Register state
            let app_state = commands::AppState {
                timer: Mutex::new(timer::Timer::new(timer::TimerConfig::default())),
                sync_engine: sync::SyncEngine::new(None),
            };
            app.manage(app_state);

            // Spawn background timer & delta sync loop
            let app_handle = app.handle().clone();
            tauri::async_runtime::spawn(async move {
                let mut interval = tokio::time::interval(std::time::Duration::from_secs(1));
                let mut sync_tick = 0u32;
                loop {
                    interval.tick().await;
                    let state = app_handle.state::<commands::AppState>();

                    // Tick timer
                    let event = {
                        let mut timer = state.timer.lock().unwrap();
                        timer.tick()
                    };

                    if let Some(ev) = event {
                        match ev {
                            timer::TimerEvent::SessionComplete { mode, .. } => {
                                if mode == "FOCUS" {
                                    sound::play_chime(sound::ChimeType::FocusComplete);
                                } else {
                                    sound::play_chime(sound::ChimeType::BreakComplete);
                                }
                            }
                            timer::TimerEvent::PhaseChange { .. } => {
                                sound::play_chime(sound::ChimeType::BreakComplete);
                            }
                            _ => {}
                        }
                    }

                    // Delta sync every 60s
                    sync_tick += 1;
                    if sync_tick >= 60 {
                        sync_tick = 0;
                        if let Ok(mut conn) = db::get_connection() {
                            let _ = state.sync_engine.sync(&mut conn).await;
                        }
                    }
                }
            });

            // Build Tray Menu
            let open_i = MenuItem::with_id(app, "open", "Open StudyFlow", true, None::<&str>)?;
            let quick_i = MenuItem::with_id(app, "quick_capture", "Quick Capture (Ctrl + Space)", true, None::<&str>)?;
            let sep = tauri::menu::PredefinedMenuItem::separator(app)?;
            let quit_i = MenuItem::with_id(app, "quit", "Exit", true, None::<&str>)?;
            let menu = Menu::with_items(app, &[&open_i, &quick_i, &sep, &quit_i])?;

            // Build Tray Icon
            let icon = app.default_window_icon().cloned().expect("App icon not found");
            let _tray = TrayIconBuilder::new()
                .icon(icon)
                .menu(&menu)
                .show_menu_on_left_click(false)
                .tooltip("StudyFlow - Life OS")
                .on_menu_event(|app, event| {
                    match event.id.as_ref() {
                        "open" => {
                            if let Some(window) = app.get_webview_window("main") {
                                let _ = window.show();
                                let _ = window.set_focus();
                                let _ = window.unminimize();
                            }
                        }
                        "quick_capture" => {
                            if let Some(window) = app.get_webview_window("main") {
                                let _ = window.show();
                                let _ = window.set_focus();
                                let _ = window.unminimize();
                            }
                        }
                        "quit" => {
                            app.exit(0);
                        }
                        _ => {}
                    }
                })
                .on_tray_icon_event(|tray, event| {
                    if let TrayIconEvent::Click {
                        button: MouseButton::Left,
                        button_state: MouseButtonState::Up,
                        ..
                    } = event {
                        let app = tray.app_handle();
                        if let Some(window) = app.get_webview_window("main") {
                            let _ = window.show();
                            let _ = window.set_focus();
                            let _ = window.unminimize();
                        }
                    }
                })
                .build(app)?;

            // Prevent window close from exiting the app -> hide to tray instead
            if let Some(window) = app.get_webview_window("main") {
                let w = window.clone();
                window.on_window_event(move |event| {
                    if let WindowEvent::CloseRequested { api, .. } = event {
                        api.prevent_close();
                        let _ = w.hide();
                    }
                });
            }

            Ok(())
        })
        .invoke_handler(tauri::generate_handler![
            commands::get_timer_status,
            commands::timer_start,
            commands::timer_pause,
            commands::timer_resume,
            commands::timer_stop,
            commands::timer_skip,
            commands::play_chime,
            commands::get_sync_conflicts,
            commands::dismiss_sync_conflicts,
            commands::set_auth_session,
            commands::trigger_sync,
        ])
        .run(tauri::generate_context!())
        .expect("error while running StudyFlow");
}
