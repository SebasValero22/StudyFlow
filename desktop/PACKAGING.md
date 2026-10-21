# StudyFlow Desktop — Packaging & Downloadables

Plain `mvn package` builds the cross-platform fat jar (`target/studyflow-desktop-*.jar`,
`java -jar` works anywhere with a Java 21+ runtime). Native installers come from
the jpackage profiles below — **always run jpackage on the target OS**.

## Windows (.exe)

Prerequisite: [WiX Toolset v3](https://wixtoolset.org/) on PATH (jpackage needs it
for `exe`/`msi`; without WiX use the `app-image` fallback).

```powershell
cd desktop
.\mvnw.cmd -Ppkg-win clean package javafx:jpackage
```

Output: `target/jpackage/StudyFlow-1.0.0.exe` (Start-menu + desktop shortcuts included).
No-WiX fallback: temporarily change the profile's `--type exe` to `app-image`
and ship the resulting `StudyFlow/` folder with its bundled runtime.

## Linux / Omarchy (.deb or portable dir)

Prerequisites: JDK 21+, `fakeroot` + `dpkg-deb` for the `deb` type
(`sudo pacman -S jdk-openjdk fakeroot dpkg` — dpkg is AUR-only; otherwise use
the app-image profile which needs nothing extra).

```bash
cd desktop
./mvnw -Ppkg-linux-deb clean package javafx:jpackage   # → target/jpackage/studyflow_1.0.0-1_amd64.deb
# or portable, no root needed:
./mvnw -Ppkg-linux-app clean package javafx:jpackage   # → target/jpackage/StudyFlow/bin/studyflow
```

Install: `sudo pacman -U studyflow_1.0.0-1_amd64.deb` or copy the app-image dir
to `~/apps` / `/opt` and link the launcher into `~/.local/bin`.

> Note: jpackage bundles its own Java runtime, so the .exe/.deb run even on
> machines without Java installed.

## What the user gets on first run

1. Login screen → sign in with the seeded account (`admin@mail.com` / `123`)
   or register a new one.
2. Settings (👤 → all sections): Focus Timer presets & durations, your API URL
   with a Test button, Light/Dark theme, notes vault folder.
3. Notes vault auto-creates at `~/StudyFlow/vault` (`%USERPROFILE%` on Windows).
