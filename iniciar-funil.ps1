<#
    Publica a API do Observa Cidade na internet com um endereço FIXO usando o
    Tailscale Funnel.

    Pré-requisitos (uma única vez):
      1. Instalar o Tailscale no PC:  winget install Tailscale.Tailscale
      2. Entrar na sua conta:         tailscale up
      3. No painel https://login.tailscale.com/admin/dns, ativar "MagicDNS" e
         "HTTPS Certificates".

    Depois disso, este script publica a porta e mostra o endereço permanente
    (https://SEU-PC.SUA-TAILNET.ts.net), que pode ser usado por qualquer pessoa
    com o app, de qualquer rede — basta o PC estar ligado com o servidor rodando.

    Uso:
        .\iniciar-funil.ps1
        .\iniciar-funil.ps1 -Port 8080
#>
param(
    [int]$Port = 8080
)

$ErrorActionPreference = 'Stop'
$raiz = $PSScriptRoot
$logs = Join-Path $raiz 'logs'
New-Item -ItemType Directory -Force -Path $logs | Out-Null

$tailscale = (Get-Command tailscale -ErrorAction SilentlyContinue).Source
if (-not $tailscale) {
    foreach ($caminho in @(
            'C:\Program Files\Tailscale\tailscale.exe',
            'C:\Program Files (x86)\Tailscale\tailscale.exe')) {
        if (Test-Path $caminho) { $tailscale = $caminho; break }
    }
}

if (-not $tailscale) {
    throw 'Tailscale não encontrado. Instale com: winget install Tailscale.Tailscale'
}

$estado = & $tailscale status --json | ConvertFrom-Json

if ($estado.BackendState -ne 'Running') {
    Write-Output "Tailscale não está conectado (estado: $($estado.BackendState))."
    Write-Output 'Rode: tailscale up'
    exit 1
}

$nome = $estado.Self.DNSName.TrimEnd('.')
if (-not $nome) {
    Write-Output 'O Tailscale não retornou o nome DNS da máquina. Verifique o MagicDNS no painel.'
    exit 1
}

Write-Output "Publicando a porta $Port em https://$nome ..."

& $tailscale funnel --bg $Port 2>&1 | ForEach-Object { Write-Output $_ }

$endereco = "https://$nome"
Set-Content -Path (Join-Path $logs 'funil-url.txt') -Value $endereco -Encoding ASCII

Write-Output ''
Write-Output "Endereço fixo: $endereco"
Write-Output "Confira em: $endereco/health"
Write-Output 'No app, use esse endereço em: login → Servidor (ou já vem como padrão no APK).'
