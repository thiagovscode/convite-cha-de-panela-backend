@echo off
echo ===================================================
echo Iniciando o Convite Cha de Panela (React + Java)
echo ===================================================

echo [1/2] Iniciando Backend em Java (Spring Boot)...
start "Backend - Spring Boot" cmd /c "cd backend && mvnw spring-boot:run"

echo [2/2] Iniciando Frontend em React (Vite)...
start "Frontend - React" cmd /c "npm run dev"

echo Pronto! 
echo O frontend abrira em http://localhost:5173
echo O backend rodara em http://localhost:8080
echo ===================================================
pause
