@for /r build\libs %%i in (*.jar) do @set versionline=%%~ni
@echo Closing all java and javaw processes with window title format: HubTurbo V%versionline:~9,5%*.
@taskkill /fi "Windowtitle eq HubTurbo V%versionline:~15,5%*" /im "javaw.exe"
@taskkill /fi "Windowtitle eq HubTurbo V%versionline:~15,5%*" /im "java.exe"
@echo(
@echo Backing up your files. Any jar or exe files will not be backed up.
@echo .jar>>rbar-ex.txt
@echo .exe>>rbar-ex.txt
@xcopy build\libs rbar /i /s /exclude:rbar-ex.txt
@del rbar-ex.txt
@echo(
@echo Starting Gradle build. This will take a while...
@echo(
@call gradlew clean shadowJar --no-daemon
@xcopy rbar build\libs /i /s
@rmdir /S /Q rbar
@for /F "delims=" %%a in ('findstr /rc:"^    version" build.gradle') do @set versionline=%%a
@cd build\libs
@echo(
@echo Starting HubTurbo V%versionline:~15,5%. You can close this cmd window once HubTurbo begins launching.
@start "" javaw -jar HubTurbo-%versionline:~15,5%-all.jar
@exit