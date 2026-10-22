@echo off
echo ============================================================
echo   PRIMO ART GALLERY - STAFF SCANNER APK BUILDER
echo ============================================================
echo.
cd /d "%~dp0\android-app"
echo Building Debug APK with Gradle...
call "C:\Users\anabi\.gradle\wrapper\dists\gradle-9.3.1-bin\23ovyewtku6u96viwx3xl3oks\gradle-9.3.1\bin\gradle.bat" assembleDebug
if %ERRORLEVEL% EQU 0 (
    copy /y "app\build\outputs\apk\debug\app-debug.apk" "..\PrimoStaffScanner.apk"
    echo.
    echo ============================================================
    echo   BUILD SUCCESSFUL!
    echo   APK Saved to: PrimoStaffScanner.apk
    echo ============================================================
) else (
    echo.
    echo Build failed with error code %ERRORLEVEL%
)
pause
