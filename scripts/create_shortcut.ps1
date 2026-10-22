$wshShell = New-Object -ComObject WScript.Shell
$desktopDir = [System.Environment]::GetFolderPath([System.Environment+SpecialFolder]::Desktop)
$shortcutPath = Join-Path $desktopDir "StudyFlow.lnk"

$exePath = "D:\Personal projects\Full StudyFLow\pwa\src-tauri\target\release\studyflow.exe"
$workDir = "D:\Personal projects\Full StudyFLow\pwa"
$icoPath = "D:\Personal projects\Full StudyFLow\pwa\src-tauri\icons\icon.ico"

$shortcut = $wshShell.CreateShortcut($shortcutPath)
$shortcut.TargetPath = $exePath
$shortcut.WorkingDirectory = $workDir
$shortcut.Description = "StudyFlow - Featherweight Academic Life OS (Tauri v2 + Rust)"
if (Test-Path $icoPath) {
    $shortcut.IconLocation = "$icoPath, 0"
}
$shortcut.Save()

Write-Host "Updated shortcut at $shortcutPath -> $exePath"
