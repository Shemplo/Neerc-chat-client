@echo off

:: Fetching version
set "version=%~1"
goto :versionCheck

:versionPrompt
set /p "version=Enter version: "

:versionCheck
if "%version%"=="" goto :versionPrompt

if not exist "chat.neerc-%version%.jar" (
    echo File of %version% version not exists
	goto :versionPrompt
)

echo Starting chat client chat.neerc-%version%.jar
java -cp chat.neerc-%version%.jar;lib/* ru.shemplo.chat.neerc.RunNeercChatClient
pause