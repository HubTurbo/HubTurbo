@REM  ----------------------
@REM |rbar (rebuild and run)|
@REM  ----------------------
@REM Script to clean the build/libs directory, build the project with gradle (after a git pull, maybe)
@REM and then run the newly built HubTurbo. User information, such as cached repos and boards, will stay
@REM the same during the process.

@echo off

REM An endlocal only affects the most recent setlocal, so we can nest setlocals. This ensures local variables
REM do not persist within the cmd window running the script.
setlocal

REM This gets the name of the HubTurbo .jar file in build/libs. The expected format is HubTurbo-X.X.X-all.jar, but
REM any file that fits the format HubTurbo-(*).jar will be accepted.
for /r build\libs %%i in (*.jar) do @set versionLine=%%~ni
REM Get the chromedriver file name. Expecting chromedriver_X-X.exe, but chromedriver(*).exe is good enough.
for /r build\libs %%i in (chromedriver*.exe) do @set chromedriverName=%%~ni
REM Based on the .jar file name, close relevant processes.
echo ^> Closing all javaw processes with window title format: HubTurbo V%versionLine:~9,5%*.
taskkill /fi "Windowtitle eq HubTurbo V%versionLine:~9,5%*" /im "javaw.exe"
echo(
echo ^> Closing all java processes with window title format: HubTurbo V%versionLine:~9,5%*.
taskkill /fi "Windowtitle eq HubTurbo V%versionLine:~9,5%*" /im "java.exe"
echo(
REM Wait for existing chromedriver processes to close.
set chromedriverPid=""
for /f "tokens=2" %%a in ('tasklist^|find /i "%chromedriverName%.exe"') do set chromedriverPid=%%a
if not ("%chromedriverPid%"=="") (
    echo ^> Waiting for all chromedriver processes with name %chromedriverName%.exe to close.
    echo(
    :chromedriverWaitLoop
    tasklist|find " %chromedriverPid% " >nul
    if not errorlevel 1 (
        timeout /t 1 >nul
        goto :chromedriverWaitLoop
    )
)

REM rbar-ex.txt tells xcopy to avoid backing up any .jar and .exe files.
REM (we avoid copying HubTurbo .jar and the chromedriver executable)
REM It is used as the exclusion list when xcopy backs up the user cache.
REM The temporary backup location of the user cache is the folder rbar.
echo ^> Backing up your files. Any jar or exe files will not be backed up.
echo .jar>>rbar-ex.txt
echo .exe>>rbar-ex.txt
xcopy build\libs rbar /i /s /exclude:rbar-ex.txt
del rbar-ex.txt
echo(

echo ^> Starting Gradle build. This will take a while...
echo(
call gradlew clean shadowJar --no-daemon
echo(

REM After rebuilding the project, we move the backup cache back to build/libs
REM and then delete the temporary rbar folder.
echo ^> Moving your files back to build/libs.
xcopy rbar build\libs /i /s
rmdir /S /Q rbar

REM We find the new version number in our build.gradle file by getting the
REM line containing it, and then calling a substring operation on the line to
REM get the version number. Note that the length of the version number changes
REM e.g. 3.1.0 vs 3.10.0, so we have to find the string length to get the correct
REM substring.
for /F "delims=" %%a in ('findstr /rc:"^    version" build.gradle') do @set versionLine=%%a
call:strLen versionLine versionLineLength
set startIndex=15
set /a versionStrLength=%versionLineLength%-%startIndex%-1
setlocal EnableDelayedExpansion
set versionStr=!versionLine:~%startIndex%,%versionStrlength%!
endlocal & set versionNumber=%versionStr%

REM We use the new version number to run the correct (newly built) HubTurbo .jar file.
cd build\libs
echo(
echo ^> Starting HubTurbo V%versionNumber%. You can close this cmd window once HubTurbo begins launching.
start "" javaw -jar HubTurbo-%versionNumber%-all.jar
cd ..\..
endlocal
exit /B


REM  -----------------------
REM |Finds length of string.|
REM  -----------------------
REM Mechanism: Start with a length value of 0, then flip bits when appropriate, starting from the largest one.
REM Similar to binary search.
:strLen inputStr lengthOfStr
setlocal EnableDelayedExpansion
set "inputString=#!%~1!"
set "curr=0"
for /l %%p in (12, -1, 0) do (
    REM Bitwise OR of "curr" with 2^a, which starts at 4024 (2^12), then 2048 (2^11),...until 1 (2^0).
    set /a "curr|=1<<%%p"
    REM Not an actual loop, the "for" is used to inject the value of "curr" into the substring operation.
    REM The if-statement means, if curr does not point to a valid character in the inputString i.e. curr is still larger
    REM than the length of inputString, then perform a bitwise AND on curr with -2^a to erase the effect of the current loop's
    REM bitwise OR operation.
    for %%b in (!curr!) do if "!inputString:~%%b,1!"=="" set /a "curr&=~1<<%%p"
)
endlocal & set %~2=%curr%