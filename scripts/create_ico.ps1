Add-Type -AssemblyName System.Drawing
$pngPath = "D:\Personal projects\Full StudyFLow\assets\StudyFlowIcon.png"
$icoPath = "D:\Personal projects\Full StudyFLow\assets\StudyFlowIcon.ico"

$src = [System.Drawing.Bitmap]::FromFile($pngPath)
$resized = New-Object System.Drawing.Bitmap $src, (New-Object System.Drawing.Size 64, 64)
$hIcon = $resized.GetHicon()
$icon = [System.Drawing.Icon]::FromHandle($hIcon)
$fs = New-Object System.IO.FileStream $icoPath, ([System.IO.FileMode]::Create)
$icon.Save($fs)
$fs.Close()
$icon.Dispose()
$resized.Dispose()
$src.Dispose()
Write-Host "ICO successfully generated at $icoPath"
