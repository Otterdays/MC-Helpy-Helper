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
echo Done. Mod JAR^(s^) in build\libs\
dir /b "build\libs\*.jar" 2>nul
exit /b 0
