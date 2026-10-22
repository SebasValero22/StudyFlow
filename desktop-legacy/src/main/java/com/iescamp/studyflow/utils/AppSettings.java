package com.iescamp.studyflow.utils;

import java.util.prefs.Preferences;

/**
 * Central, persisted user settings for the desktop client.
 * Stored via java.util.prefs (registry on Windows, ~/.java on Linux),
 * so "bring your own cloud" config survives reinstalls without rebuilds.
 */
public class AppSettings {

    public enum TimerPreset { POMODORO, ANIMEDORO, CUSTOM }

    private static final AppSettings INSTANCE = new AppSettings();
    private final Preferences prefs = Preferences.userNodeForPackage(AppSettings.class);

    private AppSettings() {}

    public static AppSettings get() {
        return INSTANCE;
    }

    // ---- Timer ----
    public TimerPreset getTimerPreset() {
        try {
            return TimerPreset.valueOf(prefs.get("timer.preset", TimerPreset.ANIMEDORO.name()));
        } catch (IllegalArgumentException e) {
            return TimerPreset.ANIMEDORO;
        }
    }

    public void setTimerPreset(TimerPreset preset) {
        prefs.put("timer.preset", preset.name());
    }

    public int getFocusMinutes() { return prefs.getInt("timer.focus", 40); }
    public void setFocusMinutes(int v) { prefs.putInt("timer.focus", clamp(v, 1, 240)); }

    public int getShortBreakMinutes() { return prefs.getInt("timer.short", 20); }
    public void setShortBreakMinutes(int v) { prefs.putInt("timer.short", clamp(v, 1, 120)); }

    public int getLongBreakMinutes() { return prefs.getInt("timer.long", 30); }
    public void setLongBreakMinutes(int v) { prefs.putInt("timer.long", clamp(v, 1, 180)); }

    public int getCyclesBeforeLongBreak() { return prefs.getInt("timer.cycles", 4); }
    public void setCyclesBeforeLongBreak(int v) { prefs.putInt("timer.cycles", clamp(v, 1, 12)); }

    public boolean isAutoStartBreaks() { return prefs.getBoolean("timer.autostart.breaks", false); }
    public void setAutoStartBreaks(boolean v) { prefs.putBoolean("timer.autostart.breaks", v); }

    public boolean isAutoStartFocus() { return prefs.getBoolean("timer.autostart.focus", false); }
    public void setAutoStartFocus(boolean v) { prefs.putBoolean("timer.autostart.focus", v); }

    public boolean isSoundEnabled() { return prefs.getBoolean("timer.sound", true); }
    public void setSoundEnabled(boolean v) { prefs.putBoolean("timer.sound", v); }

    /** Applies a preset (also writes the concrete minute values). */
    public void applyPreset(TimerPreset preset) {
        setTimerPreset(preset);
        switch (preset) {
            case POMODORO -> {
                setFocusMinutes(25);
                setShortBreakMinutes(5);
                setLongBreakMinutes(15);
                setCyclesBeforeLongBreak(4);
            }
            case ANIMEDORO -> {
                setFocusMinutes(40);
                setShortBreakMinutes(20);
                setLongBreakMinutes(30);
                setCyclesBeforeLongBreak(4);
            }
            case CUSTOM -> { /* keep current values */ }
        }
    }

    // ---- Appearance ----
    public String getTheme() { return prefs.get("ui.theme", "light"); }
    public void setTheme(String theme) { prefs.put("ui.theme", theme); }

    // ---- Bring your own cloud ----
    /** Blank = use the bundled default API URL. */
    public String getApiUrlOverride() { return prefs.get("cloud.apiUrl", "").trim(); }
    public void setApiUrlOverride(String url) { prefs.put("cloud.apiUrl", url == null ? "" : url.trim()); }

    /** Blank = default vault location (~/StudyFlow/vault). */
    public String getVaultDirOverride() { return prefs.get("notes.vaultDir", "").trim(); }
    public void setVaultDirOverride(String dir) { prefs.put("notes.vaultDir", dir == null ? "" : dir.trim()); }

    // ---- Sync ----
    /**
     * ISO serverTime cursor from the last successful pull for a specific user.
     * Blank = full sync. Scoped per user so account switches never leak cursors.
     */
    public String getSyncCursor(int userId) {
        return prefs.get(syncCursorKey(userId), "").trim();
    }

    public void setSyncCursor(int userId, String iso) {
        prefs.put(syncCursorKey(userId), iso == null ? "" : iso);
    }

    public void clearSyncCursor(int userId) {
        prefs.remove(syncCursorKey(userId));
    }

    private static String syncCursorKey(int userId) {
        return "sync.cursor.u" + userId;
    }

    /** Generic int prefs used by sync (e.g. last logged-in userId). */
    public int getIntPref(String key, int fallback) { return prefs.getInt(key, fallback); }
    public void putIntPref(String key, int value) { prefs.putInt(key, value); }

    // ---- Auth / Session persistence ----
    public String getAuthToken() { return prefs.get("auth.token", "").trim(); }
    public void setAuthToken(String token) { prefs.put("auth.token", token == null ? "" : token.trim()); }
    public void clearAuthToken() { prefs.remove("auth.token"); }

    public String getSavedUserJson() { return prefs.get("auth.userJson", "").trim(); }
    public void setSavedUserJson(String json) { prefs.put("auth.userJson", json == null ? "" : json.trim()); }
    public void clearSavedUserJson() { prefs.remove("auth.userJson"); }

    /** JWT expiry (epoch ms) for the current session; 0 = unknown/no token. */
    public long getTokenExpiresAt() { return prefs.getLong("auth.expiresAt", 0L); }
    public void setTokenExpiresAt(long epochMs) { prefs.putLong("auth.expiresAt", epochMs); }
    public void clearTokenExpiresAt() { prefs.remove("auth.expiresAt"); }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }
}
