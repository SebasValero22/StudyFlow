Set WshShell = CreateObject("WScript.Shell")
WshShell.CurrentDirectory = "D:\Personal projects\Full StudyFLow\pwa\src-tauri"
WshShell.Run """D:\Personal projects\Full StudyFLow\pwa\src-tauri\target\release\studyflow.exe""", 0, False
