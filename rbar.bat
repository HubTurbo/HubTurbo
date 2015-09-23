@for /F "delims=" %%a in ('findstr /rc:"^    version" build.gradle') do @set versionline=%%a
@echo Closing all java and javaw processes with window title format: HubTurbo V%versionline:~15,5%*.
@taskkill /fi "Windowtitle eq HubTurbo V%versionline:~15,5%*" /im "javaw.exe"
@taskkill /fi "Windowtitle eq HubTurbo V%versionline:~15,5%*" /im "java.exe"
@echo(
@echo Starting Gradle build. This will take a while...
@echo(
@call gradlew clean shadowJar --no-daemon
@for /F "delims=" %%a in ('findstr /rc:"^    version" build.gradle') do @set versionline=%%a
@echo(
@echo Starting HubTurbo V%versionline:~15,5%. You can close this cmd window once HubTurbo begins launching.
@start "" javaw -jar build/libs/HubTurbo-%versionline:~15,5%-all.jar
@exit