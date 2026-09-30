@echo off
echo ========================================================
echo   INICIANDO CONVITE CASAMENTO BACKEND - SPRING BOOT
echo ========================================================
echo.

if not exist target\convite-backend-0.0.1-SNAPSHOT.jar (
    echo [ERRO] O arquivo JAR executavel nao foi encontrado.
    echo Gerando novo pacote JAR com Maven...
    call mvn clean package -DskipTests
)

echo Iniciando o servidor Spring Boot na porta 3001...
echo Conectando ao MongoDB Atlas...
echo.
java -jar target\convite-backend-0.0.1-SNAPSHOT.jar
pause
