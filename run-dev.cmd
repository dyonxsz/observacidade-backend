@echo off
REM Executa a API em modo desenvolvimento (H2, sem MySQL).
REM Uso: run-dev.cmd [porta]
REM Ex.:  run-dev.cmd 8081
cd /d "%~dp0"
if not "%~1"=="" set PORT=%~1
call mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
