@echo off
echo ========================================
echo    Spring Boot Backend (Local Setup)
echo ========================================

echo.
echo [1/3] Checking Java version...
java -version
if %errorlevel% neq 0 (
    echo ERROR: Java is not installed or not in PATH
    echo Please install JDK 21 and set JAVA_HOME
    pause
    exit /b 1
)

echo.
echo [2/3] Checking PostgreSQL connection...
echo Please ensure PostgreSQL is running and database 'fit_db' exists
echo Database config: localhost:5432/fit_db (postgres/12341234)
echo.

echo [3/3] Starting Spring Boot application...
echo Application will be available at: http://localhost:8080
echo.
echo Press Ctrl+C to stop the application
echo.
./mvnw.cmd spring-boot:run

pause 