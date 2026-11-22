<#
    Executa a API em modo desenvolvimento (H2, sem MySQL).
    Uso: .\run-dev.ps1 [-Port 8081]
#>
param(
    [int]$Port = 0
)

Set-Location $PSScriptRoot

if ($Port -gt 0) {
    $env:PORT = "$Port"
}

.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
