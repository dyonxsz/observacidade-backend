<#
    Abre um endereço público (https) para a API do Observa Cidade, sem mexer no roteador.

    Usa o Cloudflare Quick Tunnel: serve para testar e para uso imediato. O endereço
    muda cada vez que o túnel é reiniciado. Para um endereço fixo, veja
    iniciar-funil.ps1 (Tailscale).

    Uso:
        .\iniciar-tunel.ps1
        .\iniciar-tunel.ps1 -Port 8080

    O endereço gerado aparece no console e é salvo em .\logs\tunel-url.txt
#>
param(
    [int]$Port = 8080
)

$ErrorActionPreference = 'Stop'
$raiz = $PSScriptRoot
$logs = Join-Path $raiz 'logs'
New-Item -ItemType Directory -Force -Path $logs | Out-Null

$cloudflared = (Get-Command cloudflared -ErrorAction SilentlyContinue).Source
if (-not $cloudflared) {
    foreach ($caminho in @(
            'C:\Program Files (x86)\cloudflared\cloudflared.exe',
            'C:\Program Files\cloudflared\cloudflared.exe')) {
        if (Test-Path $caminho) { $cloudflared = $caminho; break }
    }
}

if (-not $cloudflared) {
    throw 'cloudflared não encontrado. Instale com: winget install Cloudflare.cloudflared'
}

$saida = Join-Path $logs 'tunel.log'
$arquivoUrl = Join-Path $logs 'tunel-url.txt'

$processo = Start-Process -FilePath $cloudflared `
    -ArgumentList @('tunnel', '--url', "http://127.0.0.1:$Port", '--no-autoupdate') `
    -WorkingDirectory $raiz `
    -WindowStyle Hidden `
    -RedirectStandardError $saida `
    -RedirectStandardOutput (Join-Path $logs 'tunel-saida.log') `
    -PassThru

Write-Output "Abrindo túnel para a porta $Port (PID $($processo.Id))..."

$endereco = $null

for ($i = 0; $i -lt 40; $i++) {
    Start-Sleep -Seconds 1
    if (Test-Path $saida) {
        $texto = Get-Content $saida -Raw -ErrorAction SilentlyContinue
        $achado = [regex]::Match($texto, 'https://[a-z0-9-]+\.trycloudflare\.com')

        if ($achado.Success) {
            $endereco = $achado.Value
            break
        }
    }
}

if (-not $endereco) {
    Write-Output "Não consegui obter o endereço. Veja $saida"
    exit 1
}

Set-Content -Path $arquivoUrl -Value $endereco -Encoding ASCII

# Mantém o app apontando para o endereço público atual: é o que permite
# usar o celular fora do Wi-Fi (dados móveis / outra cidade).
try {
    & (Join-Path $raiz 'sincronizar-app.ps1') -Url $endereco | ForEach-Object { Write-Output $_ }
} catch {
    Write-Output "Não consegui atualizar o app.json: $($_.Exception.Message)"
}

Write-Output ''
Write-Output "Endereço público: $endereco"
Write-Output "Confira em: $endereco/health"
Write-Output 'No app, esse endereço é usado automaticamente quando o celular está fora do Wi-Fi.'
Write-Output 'Se precisar informar na mão: login -> Servidor.'
Write-Output ''
Write-Output 'Atenção: esse endereço muda quando o túnel reiniciar.'
Write-Output 'Para um endereço fixo use .\iniciar-funil.ps1 (Tailscale).'
