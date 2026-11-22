' Abre a API do Observa Cidade em segundo plano, sem janela.
' Coloque um atalho deste arquivo em:
'   %APPDATA%\Microsoft\Windows\Start Menu\Programs\Startup
' Assim o servidor sobe sozinho quando o Windows iniciar.

Dim fso, shell, pasta, script
Set fso = CreateObject("Scripting.FileSystemObject")
Set shell = CreateObject("WScript.Shell")

pasta = fso.GetParentFolderName(WScript.ScriptFullName)
script = pasta & "\iniciar-servidor.ps1"

shell.Run "powershell.exe -NoProfile -ExecutionPolicy Bypass -WindowStyle Hidden -File """ & script & """", 0, False
