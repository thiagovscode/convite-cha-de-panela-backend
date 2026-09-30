@echo off
set ADMIN_RESET_PASSWORD=Balboa@2027#N
set RECEPCAO_RESET_PASSWORD=Recepcao@2027!
echo Iniciando backend para resetar senhas...
java -jar target/convite-backend-0.0.1-SNAPSHOT.jar > resetar-senhas.log 2>&1
