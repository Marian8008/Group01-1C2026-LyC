@echo off
setlocal

set "DOSBOX=C:\Program Files (x86)\DOSBox-0.74-3\DOSBox.exe"
set "ASMDIR=%~dp0target\asm"

if not exist "%DOSBOX%" (
    echo ERROR: No se encontro DOSBox en "%DOSBOX%"
    exit /b 1
)

if not exist "%ASMDIR%\run.bat" (
    echo ERROR: No se encontro "%ASMDIR%\run.bat"
    exit /b 1
)

"%DOSBOX%" -c "mount c \"%ASMDIR%\"" -c "c:" -c "run.bat" -c "pause"

endlocal
