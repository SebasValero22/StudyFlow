$wshShell = New-Object -ComObject WScript.Shell
$desktopDir = [System.Environment]::GetFolderPath([System.Environment+SpecialFolder]::Desktop)
$shortcutPath = Join-Path $desktopDir "StudyFlow.lnk"

$javaw = "C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot\bin\javaw.exe"
if (-not (Test-Path $javaw)) {
    $found = (Get-Command javaw.exe -ErrorAction SilentlyContinue)
    if ($found) { $javaw = $found.Source }
}

$jarPath = "D:\Personal projects\Full StudyFLow\desktop\target\studyflow-desktop-1.0-SNAPSHOT.jar"
$workDir = "D:\Personal projects\Full StudyFLow\desktop"
$icoPath = "D:\Personal projects\Full StudyFLow\assets\StudyFlowIcon.ico"

$shortcut = $wshShell.CreateShortcut($shortcutPath)
$shortcut.TargetPath = $javaw
$shortcut.Arguments = "-jar `"$jarPath`""
$shortcut.WorkingDirectory = $workDir
$shortcut.Description = "StudyFlow - Personal Productivity & Academic Life OS"
if (Test-Path $icoPath) {
    $shortcut.IconLocation = "$icoPath, 0"
}
$shortcut.Save()

Write-Host "Created shortcut at $shortcutPath"
