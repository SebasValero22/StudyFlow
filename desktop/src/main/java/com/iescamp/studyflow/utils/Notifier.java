package com.iescamp.studyflow.utils;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;

/**
 * Cross-platform completion feedback: synthesized chime (no audio assets
 * needed) + best-effort OS notification. Never throws, never blocks the
 * JavaFX thread.
 */
public final class Notifier {

    private Notifier() {}

    /** Short two-tone chime, synthesized in-code. Runs on a daemon thread. */
    public static void chime() {
        if (!AppSettings.get().isSoundEnabled()) {
            return;
        }
        Thread t = new Thread(() -> {
            tone(880.0, 220);
            try { Thread.sleep(90); } catch (InterruptedException ignored) {}
            tone(1174.7, 320);
        }, "studyflow-chime");
        t.setDaemon(true);
        t.start();
    }

    private static void tone(double freqHz, int millis) {
        try {
            float sampleRate = 22050f;
            AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, false);
            try (SourceDataLine line = AudioSystem.getSourceDataLine(format)) {
                line.open(format);
                line.start();
                int samples = (int) (sampleRate * millis / 1000);
                byte[] buf = new byte[samples * 2];
                for (int i = 0; i < samples; i++) {
                    double envelope = Math.min(1.0, i / (sampleRate * 0.02))
                            * (1.0 - (double) i / samples);
                    short s = (short) (Math.sin(2 * Math.PI * freqHz * i / sampleRate)
                            * Short.MAX_VALUE * 0.5 * envelope);
                    buf[2 * i] = (byte) (s & 0xff);
                    buf[2 * i + 1] = (byte) ((s >> 8) & 0xff);
                }
                line.write(buf, 0, buf.length);
                line.drain();
            }
        } catch (Exception e) {
            try {
                java.awt.Toolkit.getDefaultToolkit().beep();
            } catch (Exception ignored) {
            }
        }
    }

    /** Best-effort desktop notification. Falls back silently when unsupported. */
    public static void notify(String title, String message) {
        // Linux native notifications (Omarchy/Hyprland friendly)
        if (System.getProperty("os.name", "").toLowerCase().contains("linux")) {
            try {
                new ProcessBuilder("notify-send", "StudyFlow - " + title, message)
                        .redirectErrorStream(true)
                        .start();
                return;
            } catch (Exception ignored) {
            }
        }
        // Windows/macOS fallback via AWT tray
        try {
            if (SystemTray.isSupported()) {
                SystemTray tray = SystemTray.getSystemTray();
                BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
                TrayIcon icon = new TrayIcon(img, "StudyFlow");
                icon.setImageAutoSize(true);
                tray.add(icon);
                icon.displayMessage("StudyFlow - " + title, message, TrayIcon.MessageType.INFO);
                Thread remover = new Thread(() -> {
                    try { Thread.sleep(6000); } catch (InterruptedException ignored) {}
                    tray.remove(icon);
                }, "studyflow-tray-cleanup");
                remover.setDaemon(true);
                remover.start();
            }
        } catch (Exception ignored) {
        }
    }
}
