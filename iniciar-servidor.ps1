<#
    Sobe a API do Observa Cidade em segundo plano (sem janela), usando o JAR já compilado.

    - Se a porta já estiver em uso, não faz nada.
    - Se o JAR não existir, compila antes com o Maven Wrapper.
    - Logs em .\logs\servidor.log e .\logs\servidor-erros.log

    Uso:
        .\iniciar-servidor.ps1              # porta 8080
        .\iniciar-servidor.ps1 -Port 9090   # outra porta
#>
param(
    [int]$Port = 8080
)

$ErrorActionPreference = 'Stop'
$raiz = $PSScriptRoot
Set-Location $raiz

$logs = Join-Path $raiz 'logs'
New-Item -ItemType Directory -Force -Path $logs | Out-Null

$emUso = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue
if ($emUso) {
    Write-Output "A API já está rodando na porta $Port (PID $($emUso.OwningProcess | Select-Object -First 1))."
    exit 0
}

$jar = Get-ChildItem (Join-Path $raiz 'target') -Filter 'observacidade-*.jar' -ErrorAction SilentlyContinue |
    Where-Object { $_.Name -notlike '*.original' } |
    Sort-Object LastWriteTime -Descending |
    Select-Object -First 1

if (-not $jar) {
    Write-Output 'JAR não encontrado; compilando (pode levar alguns minutos)...'
    & (Join-Path $raiz 'mvnw.cmd') -q -DskipTests package
    $jar = Get-ChildItem (Join-Path $raiz 'target') -Filter 'observacidade-*.jar' |
        Where-Object { $_.Name -notlike '*.original' } |
        Sort-Object LastWriteTime -Descending |
        Select-Object -First 1

    if (-not $jar) {
        throw 'Não foi possível compilar o JAR da API.'
    }
}

$java = (Get-Command java -ErrorAction SilentlyContinue).Source
if (-not $java) {
    throw 'Java não encontrado no PATH. Instale o JDK 17 ou superior.'
}

$saida = Join-Path $logs 'servidor.log'
$erros = Join-Path $logs 'servidor-erros.log'

Start-Process -FilePath $java `
    -ArgumentList @('-jar', "`"$($jar.FullName)`"", '--spring.profiles.active=dev', "--server.port=$Port") `
    -WorkingDirectory $raiz `
    -WindowStyle Hidden `
    -RedirectStandardOutput $saida `
    -RedirectStandardError $erros

Write-Output "Iniciando a API na porta $Port ($($jar.Name))..."

for ($i = 0; $i -lt 60; $i++) {
    Start-Sleep -Seconds 1
    try {
        $resposta = Invoke-WebRequest -UseBasicParsing "http://127.0.0.1:$Port/health" -TimeoutSec 2
        if ($resposta.Content -match 'UP') {
            Write-Output "API no ar: http://127.0.0.1:$Port/health"
            exit 0
        }
    } catch {
        # ainda subindo
    }
}

Write-Output "A API não respondeu em 60s. Confira $erros"
exit 1
