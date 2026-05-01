@echo off
setlocal
cd /d "%~dp0"

echo Building Fabric mod JAR...
call gradlew.bat build
if errorlevel 1 (
    echo.
    echo Build failed.
    exit /b 1
)

echo.
echo Done. Outputs in BUILT\libs\
echo Use the main mod (no "-sources" in the name) for Modrinth/mods folder.
dir /b "BUILT\libs\*.jar" 2>nul
exit /b 0
