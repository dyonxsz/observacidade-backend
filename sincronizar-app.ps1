#requires -Version 5.1
<#
    Aponta o app para o endereço público atual da API.

    Grava o endereço em app.json -> extra.apiUrlRemota, que é o que faz o app
    funcionar fora do Wi-Fi (dados móveis / outra cidade).

    Uso:
        .\sincronizar-app.ps1                      # usa logs\tunel-url.txt
        .\sincronizar-app.ps1 -Url https://xxx.trycloudflare.com
#>
param(
    [string]$Url
)

$ErrorActionPreference = 'Stop'

$raiz = $PSScriptRoot

if (-not $Url) {
    $arquivo = Join-Path $raiz 'logs\tunel-url.txt'
    if (Test-Path $arquivo) {
        $Url = (Get-Content $arquivo -Raw).Trim()
    }
}

if (-not $Url) {
    Write-Output 'Nenhum endereço público encontrado (logs\tunel-url.txt).'
    exit 1
}

$Url = $Url.TrimEnd('/')

$appJson = Join-Path (Split-Path $raiz -Parent) 'alerta_cidade-main\app.json'
if (-not (Test-Path $appJson)) {
    $appJson = 'C:\Users\dyone\Downloads\alerta_cidade-main\alerta_cidade-main\app.json'
}

if (-not (Test-Path $appJson)) {
    Write-Output "app.json não encontrado: $appJson"
    exit 1
}

$conteudo = Get-Content $appJson -Raw

if ($conteudo -notmatch '"apiUrlRemota"') {
    Write-Output 'app.json não tem extra.apiUrlRemota; nada a fazer.'
    exit 0
}

$novo = [regex]::Replace(
    $conteudo,
    '"apiUrlRemota"\s*:\s*"[^"]*"',
    '"apiUrlRemota": "' + $Url + '"'
)

if ($novo -ne $conteudo) {
    [System.IO.File]::WriteAllText($appJson, $novo, (New-Object System.Text.UTF8Encoding($false)))
    Write-Output "app.json atualizado: extra.apiUrlRemota = $Url"
} else {
    Write-Output "app.json já aponta para $Url"
}
