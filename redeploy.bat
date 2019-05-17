rem Windows script to rebuild the whole project and redeploy to local tomcat

IF NOT DEFINED MYSQL_PORT SET MYSQL_PORT=3306
IF NOT DEFINED DB_PASSWORD SET DB_PASSWORD=admin
IF NOT DEFINED CATALINA_HOME SET CATALINA_HOME=C:\apache-tomcat-8.5.34

set start=%time%
call mvn -f ./web/pom.xml -Ddatabase.password=%DB_PASSWORD% -Dupload.location=/ctd2upload/ -Dsubmission.builder.version=v2.1 clean package
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
