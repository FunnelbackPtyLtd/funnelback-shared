Set fs = CreateObject("Scripting.FilesystemObject")

If WScript.Arguments.Count > 2 Then
    Set file = fs.CreateTextFile(WScript.Arguments.Item(2), True)
    file.WriteLine("Started")
    file.Close
End If

WScript.Sleep WScript.Arguments.Item(0) * 1000
WScript.Echo fs.OpenTextFile(WScript.Arguments.Item(1),1,false).ReadAll


