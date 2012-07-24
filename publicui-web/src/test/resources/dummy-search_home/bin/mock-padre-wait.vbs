WScript.Sleep WScript.Arguments.Item(0) * 1000
WScript.Echo CreateObject("Scripting.FilesystemObject").OpenTextFile(WScript.Arguments.Item(1),1,false).ReadAll