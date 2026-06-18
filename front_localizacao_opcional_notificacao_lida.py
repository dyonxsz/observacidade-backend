from pathlib import Path
import re
import shutil
import os

ROOT = Path.cwd()
SRC = ROOT / "src"

if not SRC.exists():
    raise SystemExit("ERRO: rode este script na raiz do FRONT-END.")

backup = ROOT / "backup_front_notificacao_lida_opcionais"
if not backup.exists():
    shutil.copytree(SRC, backup)
    print("Backup criado:", backup)

def read(p):
    return p.read_text(encoding="utf-8", errors="replace")

def save(p, t):
    p.write_text(t, encoding="utf-8")
    print("ALTERADO:", p)

def find(name):
    for p in ROOT.rglob(name):
        return p
    return None

# =====================================================
# 1) NewReportScreen: remove obrigatoriedade de endereço/localização
# =====================================================
new_report = find("NewReportScreen.tsx")
if new_report:
    t = read(new_report)

    # Remove qualquer if exigindo endereco/rua/localização/bairro/referencia
    patterns = [
        r"\s*if\s*\(!endereco\.trim\(\)\)\s*\{[\s\S]*?return;\s*\}\s*",
        r"\s*if\s*\(!rua\.trim\(\)\)\s*\{[\s\S]*?return;\s*\}\s*",
        r"\s*if\s*\(!localizacao\.trim\(\)\)\s*\{[\s\S]*?return;\s*\}\s*",
        r"\s*if\s*\(!bairro\.trim\(\)\)\s*\{[\s\S]*?return;\s*\}\s*",
        r"\s*if\s*\(!referencia\.trim\(\)\)\s*\{[\s\S]*?return;\s*\}\s*",
    ]
    for pat in patterns:
        t = re.sub(pat, "\n", t)

    # Payload: rua/bairro/referencia opcionais
    t = re.sub(r"rua:\s*endereco\.trim\(\),", "rua: endereco.trim() || undefined,", t)
    t = re.sub(r"rua:\s*rua\.trim\(\),", "rua: rua.trim() || undefined,", t)
    t = re.sub(r"rua:\s*localizacao\.trim\(\),", "rua: localizacao.trim() || undefined,", t)

    t = re.sub(r"bairro:\s*bairro\.trim\(\)\s*\|\|\s*['\"][^'\"]*['\"],", "bairro: bairro.trim() || undefined,", t)
    t = re.sub(r"bairro:\s*['\"]Não informado['\"],", "bairro: bairro.trim() || undefined,", t)

    t = re.sub(r"referencia:\s*referencia\.trim\(\)\s*\|\|\s*['\"][^'\"]*['\"],", "referencia: referencia.trim() || undefined,", t)
    t = re.sub(r"referencia:\s*['\"]App Mobile['\"],", "referencia: referencia.trim() || undefined,", t)

    # Textos
    t = t.replace("Endereço:", "Endereço (opcional):")
    t = t.replace("Localização:", "Localização (opcional):")
    t = t.replace("Endereço da rua", "Endereço da rua (opcional)")
    t = t.replace("Informe sua localização", "Informe sua localização (opcional)")

    save(new_report, t)
else:
    print("AVISO: NewReportScreen.tsx não encontrado.")

# =====================================================
# 2) NotificationScreen: ao abrir, backend marca como lida.
#    Ao sair e voltar, lista deve vir vazia.
# =====================================================
notification = find("NotificationScreen.tsx")
if notification:
    t = read(notification)

    # Garante recarregar no focus
    if "navigation?.addListener?.('focus'" not in t and 'navigation?.addListener?.("focus"' not in t:
        t = t.replace(
            "useEffect(() => {",
            "useEffect(() => {\n    const unsubscribe = navigation?.addListener?.('focus', carregar);"
        )
        t = t.replace(
            "}, [navigation]);",
            "return unsubscribe;\n  }, [navigation]);"
        )

    save(notification, t)

print("\\nFINALIZADO FRONT.")
print("Localização/endereço deixou de ser obrigatória no app.")
print("A tela de notificações recarrega e o backend marca como lida ao abrir.")
print("Rode: npx expo start -c")

try:
    os.remove(__file__)
except Exception:
    pass
