rem Windows script to rebuild the whole project and redeploy to local tomcat

rem CATALINA_HOME needs to be set to run this

set start=%time%
call mvn -f ./web/pom.xml clean package
if errorlevel 1 (
    echo Failure Reason Given is %errorlevel%
    exit /b %errorlevel%
)

rmdir /s /q %CATALINA_HOME%\webapps\submit
copy .\web\target\submit.war %CATALINA_HOME%\webapps\
call %CATALINA_HOME%\bin\startup.bat
set end=%time%
echo start time %start%
echo end time %end%
