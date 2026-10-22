Set WshShell = CreateObject("WScript.Shell")
WshShell.CurrentDirectory = "D:\Personal projects\Full StudyFLow\desktop"
WshShell.Run "javaw -jar ""D:\Personal projects\Full StudyFLow\desktop\target\studyflow-desktop-1.0-SNAPSHOT.jar""", 0, False
