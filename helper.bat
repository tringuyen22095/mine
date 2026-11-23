@echo off
setlocal EnableExtensions EnableDelayedExpansion

cd /d "%~dp0"

set "RESULT_FILE=%TEMP%\mvn-build-helper-%RANDOM%.tmp"
set "MVN_HELPER_RESULT=%RESULT_FILE%"
set "EXIT_CODE=1"

rem Arrow-key menu implemented by PowerShell.
rem This command intentionally stays on one line.
powershell.exe -NoLogo -NoProfile -ExecutionPolicy Bypass -Command "$options=@('Enable thread? (--threads 8)','Debug? (--debug)','Run in batch? (-B)','Skip test? (-DskipTests=true)','Run in offline mode? (--offline)','Quiet? (--quiet)','Write log? (--log-file build.log)'); $selected=New-Object bool[] $options.Count; $index=0; $done=$false; $oldCursor=[Console]::CursorVisible; [Console]::CursorVisible=$false; while(-not $done) { Clear-Host; Write-Host '=========================================='; Write-Host '             Maven Build Helper'; Write-Host '=========================================='; Write-Host 'Use Up/Down to move, Space to select, Enter to continue'; Write-Host ''; for($n=0; $n -lt $options.Count; $n++) { $pointer=' '; if($n -eq $index) {$pointer='>'}; $mark=' '; if($selected[$n]) {$mark='X'}; Write-Host ('{0} [{1}] {2}' -f $pointer,$mark,$options[$n]) }; $key=[Console]::ReadKey($true); switch($key.Key) { UpArrow {if($index -gt 0) {$index--}}; DownArrow {if($index -lt ($options.Count - 1)) {$index++}}; Spacebar {$selected[$index]= -not $selected[$index]}; Enter {$done=$true} } }; [Console]::CursorVisible=$oldCursor; Clear-Host; do {$answer=(Read-Host 'Would you like to build exact module? (y/n)').Trim().ToLowerInvariant()} until($answer -eq 'y' -or $answer -eq 'n'); $module=''; if($answer -eq 'y') {$module=Read-Host 'input service name'}; $flags=''; for($j=0; $j -lt $selected.Count; $j++) { if($selected[$j]) {$flags+='1'} else {$flags+='0'} }; [IO.File]::WriteAllText($env:MVN_HELPER_RESULT,($flags+'~'+$module))"

if errorlevel 1 (
    echo.
    echo Failed to start the selection menu.
    goto finish
)

if not exist "%RESULT_FILE%" (
    echo.
    echo No selection was received.
    goto finish
)

set "RESULT_LINE="
set /p "RESULT_LINE="<"%RESULT_FILE%"

set "FLAGS="
set "MODULE="

for /f "tokens=1,* delims=~" %%A in ("!RESULT_LINE!") do (
    set "FLAGS=%%A"
    set "MODULE=%%B"
)

rem Required base command.
set "CMD=mvn clean install"

if "!FLAGS:~0,1!"=="1" set "CMD=!CMD! --threads 8"
if "!FLAGS:~1,1!"=="1" set "CMD=!CMD! --debug"
if "!FLAGS:~2,1!"=="1" set "CMD=!CMD! -B"
if "!FLAGS:~3,1!"=="1" set "CMD=!CMD! -DskipTests=true"
if "!FLAGS:~4,1!"=="1" set "CMD=!CMD! --offline"
if "!FLAGS:~5,1!"=="1" set "CMD=!CMD! --quiet"
if "!FLAGS:~6,1!"=="1" set "CMD=!CMD! --log-file build.log"

rem -pl selects a specific Maven module.
if defined MODULE set "CMD=!CMD! -pl "!MODULE!""

echo.
echo ==========================================
echo Final command:
echo.
echo !CMD!
echo.
echo ==========================================
echo.

echo Running command...
call !CMD!
set "EXIT_CODE=!ERRORLEVEL!"

echo.
if "!EXIT_CODE!"=="0" (
    echo Build completed successfully.
) else (
    echo Build failed with exit code !EXIT_CODE!.
)

:finish
if exist "%RESULT_FILE%" del /q "%RESULT_FILE%" >nul 2>&1

echo.
echo --Press to continue--
pause >nul

exit /b !EXIT_CODE!