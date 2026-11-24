# Observa Cidade — Backend

API REST para registro e acompanhamento de ocorrências urbanas (buracos, iluminação, saneamento, lixo, sinalização etc.), aproximando cidadãos e prefeitura.

> **Repositórios do projeto**
> - Backend/API: <https://github.com/dyonxsz/observacidade-backend>
> - App mobile (Expo/React Native): <https://github.com/ruaandsx/alerta_cidade>

## Stack

- Java 17 + Spring Boot 3.5
- Spring Web, Spring Data JPA, Bean Validation
- Spring Security + JWT (stateless)
- MySQL 8 (padrão) ou H2 (perfil `dev`, sem instalar banco)
- Swagger/OpenAPI
- Testes de integração com H2

## O que está funcional

- Cadastro, login, perfil e troca de senha (JWT)
- Ocorrências com categoria, urgência, descrição, endereço, **latitude/longitude (GPS)** e **foto**
- Status: `RECEBIDO` → `EM_ANALISE` → `EM_ANDAMENTO` → `RESOLVIDO`
- Histórico (linha do tempo) de cada mudança de status, com observação e autor
- Comentários do cidadão e da prefeitura, com exclusão pelo autor/admin
- Notificações: ocorrência criada, mudança de status, comentário e resposta do suporte
- Painel do administrador: listar/filtrar ocorrências, alterar status, responder suporte, gerenciar usuários
- Estatísticas por status/categoria/urgência
- Paginação e filtros (status, categoria, urgência, bairro, texto)
- Upload de imagem por **base64** (JSON) ou **multipart/form-data**
- Swagger UI para testar tudo pelo navegador
- Perfil `dev` com H2 persistente para rodar sem MySQL
- Docker Compose (MySQL + API)
- **Assistente virtual** com base de conhecimento (FAQ curada) + busca por similaridade e camada opcional de IA generativa
- 16 testes de integração cobrindo autenticação, permissões, ocorrências, imagens, comentários, suporte, notificações e exclusão de usuário

## Rodar rápido (sem MySQL)

Requer Java 17. Não é necessário ter Maven instalado (o projeto tem o `mvnw`).

**Opção 1 — atalho (Windows):**

```powershell
# Abra a pasta do projeto e execute:
.\run-dev.cmd

# Se a porta 8080 estiver ocupada:
.\run-dev.cmd 8081
```

**Opção 2 — comando Maven:**

```powershell
cd "C:\Users\dyone\Downloads\observacidade-backend-main\observacidade-backend-main"
mvn spring-boot:run "-Dspring-boot.run.profiles=dev"
```

> Importante: execute **dentro da pasta do projeto** (onde está o `pom.xml`). Se rodar em `C:\WINDOWS\system32`, o Maven retorna `No plugin found for prefix 'spring-boot'`.

- API: <http://localhost:8080>
- Health check: <http://localhost:8080/health>
- Swagger: <http://localhost:8080/swagger-ui.html>
- H2 Console: <http://localhost:8080/h2-console> (JDBC URL: `jdbc:h2:file:./data/observacidade`)

## Subir sozinho com o Windows

`iniciar-servidor.ps1` compila (se preciso) e sobe o JAR em segundo plano, sem janela,
gravando logs em `./logs`.

```powershell
.\iniciar-servidor.ps1          # porta 8080
.\iniciar-servidor.ps1 -Port 8081
```

Para subir junto com o Windows, crie um atalho de `iniciar-servidor.vbs` em:

```
%APPDATA%\Microsoft\Windows\Start Menu\Programs\Startup
```

(atalho: `Win+R` → `shell:startup`)

Se o JAR estiver desatualizado, rode antes: `.\mvnw.cmd -DskipTests package`

## Usar o app de qualquer lugar (celular fora da rede do PC)

O PC precisa estar ligado com a API rodando; o celular pode estar em qualquer rede.

**Opção A — endereço fixo com Tailscale (recomendado)**

```powershell
winget install Tailscale.Tailscale
tailscale up                       # entre na sua conta (uma vez)
.\iniciar-funil.ps1                # publica https://SEU-PC.SUA-TAILNET.ts.net
```

Antes, no painel <https://login.tailscale.com/admin/dns>, ative **MagicDNS** e
**HTTPS Certificates**. O endereço `https://SEU-PC.SUA-TAILNET.ts.net` é fixo e
funciona para qualquer pessoa com o app, de qualquer rede.

**Opção B — túnel temporário com Cloudflare (sem conta)**

```powershell
winget install Cloudflare.cloudflared
.\iniciar-tunel.ps1                # gera https://algo-trycloudflare.com
```

Funciona na hora, mas o endereço muda a cada reinício do túnel.

Em ambos os casos, informe o endereço no app em **login → Servidor**. Com HTTPS
não há problema de tráfego não criptografado; o APK também aceita `http://` na
mesma rede (cleartext liberado no build).

Os dados ficam em `./data/` (ignorado pelo Git).

## Rodar com MySQL

1. Crie o banco:

```sql
CREATE DATABASE observa_cidade CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. Configure as variáveis (ou edite `src/main/resources/application.properties`):

| Variável | Padrão | Descrição |
|---|---|---|
| `DB_URL` | `jdbc:mysql://localhost:3306/observa_cidade?...` | URL JDBC |
| `DB_USER` | `root` | Usuário |
| `DB_PASSWORD` | `!Dyon785277` | Senha |
| `DB_DDL_AUTO` | `update` | Estratégia do Hibernate |
| `JWT_SECRET` | valor interno | **Troque em produção** (mín. 32 caracteres) |
| `JWT_EXPIRATION_MS` | `86400000` | Validade do token (24h) |
| `CORS_ALLOWED_ORIGINS` | `*` | Origens permitidas, separadas por vírgula |
| `APP_ADMIN_EMAIL` | `admin@observacidade.com` | Admin criado no primeiro start |
| `APP_ADMIN_SENHA` | `admin123` | Senha do admin (troque!) |
| `UPLOAD_MAX_BYTES` | `5242880` | Tamanho máximo da imagem |
| `GEMINI_API_KEY` / `OPENAI_API_KEY` | vazio | Habilita o assistente com IA (sem chave ele usa só a base de FAQ) |
| `ASSISTENTE_LLM_PROVIDER` | `auto` | Provedor da IA (`gemini`, `openai` ou `auto`, que detecta pela chave) |
| `ASSISTENTE_LLM_MODELO` | `gemini-3.5-flash-lite` / `gpt-4o-mini` | Modelo usado |
| `ASSISTENTE_FAQ_PONTUACAO_MINIMA` | `0.18` | Similaridade mínima para responder pela base |

3. Rode:

```bash
mvn spring-boot:run
```

## Docker

```bash
docker compose up --build
```

Sobe MySQL em `localhost:3306` e a API em `localhost:8080`.

## Acesso inicial

Ao subir pela primeira vez, é criado o administrador:

- E-mail: `admin@observacidade.com`
- Senha: `admin123`

> Altere a senha em `PUT /auth/me/senha` e/ou defina `APP_ADMIN_SENHA` antes de publicar.

## Autenticação

1. `POST /auth/cadastro` ou `POST /auth/login` → retorna `token`.
2. Envie em todas as rotas protegidas:

```
Authorization: Bearer <token>
```

No Swagger, clique em **Authorize** e cole o token.

## Endpoints principais

### Públicos

| Método | Rota | Descrição |
|---|---|---|
| GET | `/` | Informações da API |
| GET | `/health` | Health check |
| POST | `/auth/cadastro` | Cria conta e retorna token |
| POST | `/auth/login` | Login e token |
| GET | `/dominios` | Categorias, urgências, status e status de suporte |

### Autenticados

| Método | Rota | Descrição |
|---|---|---|
| GET/PUT | `/auth/me` | Perfil |
| PUT | `/auth/me/senha` | Troca de senha |
| POST | `/ocorrencias` | Cria ocorrência |
| GET | `/ocorrencias` | Lista paginada + filtros |
| GET | `/ocorrencias/minhas` | Minhas ocorrências |
| GET | `/ocorrencias/{id}` | Detalhe + histórico (`?incluirImagem=true` traz base64) |
| GET | `/ocorrencias/{id}/imagem` | Foto (binário) |
| POST | `/ocorrencias/{id}/imagem` | Envia foto (multipart `arquivo`) |
| PUT | `/ocorrencias/{id}` | Edita (dono ou admin) |
| DELETE | `/ocorrencias/{id}` | Exclui (dono ou admin) |
| GET | `/ocorrencias/estatisticas` | Totais por status/categoria/urgência |
| GET/POST | `/ocorrencias/{id}/comentarios` | Lista/cria comentários |
| DELETE | `/ocorrencias/{id}/comentarios/{comentarioId}` | Exclui comentário (autor/admin) |
| GET | `/notificacoes` | Não lidas |
| GET | `/notificacoes/todas` | Todas |
| GET | `/notificacoes/contador` | Quantidade de não lidas |
| PUT | `/notificacoes/{id}/lida` | Marca uma como lida |
| PUT | `/notificacoes/marcar-lidas` | Marca todas |
| DELETE | `/notificacoes/{id}` | Remove |
| POST | `/suporte` | Abre chamado |
| GET | `/suporte/meus` | Meus chamados |
| GET | `/assistente/sugestoes` | Perguntas sugeridas pelo assistente |
| GET | `/assistente/status` | Motor em uso (base de FAQ ou IA) |
| POST | `/assistente/perguntas` | Envia pergunta e recebe resposta |
| GET | `/assistente/historico` | Últimas perguntas do usuário |

### Admin (`ROLE_ADMIN`)

| Método | Rota | Descrição |
|---|---|---|
| GET | `/usuarios` | Lista usuários (paginado) |
| GET | `/usuarios/{id}` | Detalhe |
| PUT | `/usuarios/{id}/admin` | Promove a admin |
| PUT | `/usuarios/{id}/user` | Remove admin |
| DELETE | `/usuarios/{id}` | Exclui usuário + dados |
| PUT | `/ocorrencias/{id}/status` | Altera status |
| GET | `/suporte` | Lista chamados (filtro `status`) |
| PUT | `/suporte/{id}` | Responde chamado |
| DELETE | `/suporte/{id}` | Exclui chamado |
| GET | `/assistente/estatisticas` | Métricas de uso do assistente |

### Filtros e paginação

```
GET /ocorrencias?page=0&size=20&status=EM_ANDAMENTO&categoria=ILUMINACAO&urgencia=ALTA&bairro=Centro&busca=buraco
```

Resposta:

```json
{
  "content": [...],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

## Exemplos

### Cadastro

```bash
curl -X POST http://localhost:8080/auth/cadastro \
  -H "Content-Type: application/json" \
  -d '{"nome":"Maria Silva","email":"maria@email.com","senha":"senha123"}'
```

### Criar ocorrência com foto base64

```bash
curl -X POST http://localhost:8080/ocorrencias \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "categoria":"INFRAESTRUTURA",
    "descricao":"Buraco grande na rua das Flores",
    "rua":"Rua das Flores",
    "bairro":"Centro",
    "referencia":"Em frente à escola",
    "latitude":-23.5505,
    "longitude":-46.6333,
    "urgencia":"ALTA",
    "imagemBase64":"data:image/jpeg;base64,...."
  }'
```

### Alterar status (admin)

```bash
curl -X PUT http://localhost:8080/ocorrencias/1/status \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"status":"EM_ANDAMENTO","observacao":"Equipe de manutenção acionada"}'
```

Mais exemplos: [`docs/api.http`](docs/api.http).

## Assistente virtual (FAQ + IA)

O assistente aparece no app do cidadão e do administrador. Ele **funciona sem
nenhuma chave de API**: a base de conhecimento curada responde por similaridade
de texto. Quando uma chave de IA está configurada, o modelo redige a resposta
usando os itens encontrados como contexto (RAG simples).

Fluxo de cada pergunta:

1. a pergunta é normalizada (acentos, plural, palavras vazias);
2. `MotorBuscaFaq` calcula TF-IDF + similaridade de cosseno em Java puro,
   sem dependência externa e sem banco vetorial;
3. se a similaridade passa do limite (`0.18` por padrão), os itens são
   enviados como contexto para o LLM (Gemini ou OpenAI);
4. se a IA estiver desligada, falhar ou não houver chave, a resposta da base
   é devolvida diretamente (fallback automático);
5. se nada for encontrado, o assistente orienta abrir um chamado em
   **Ajuda e Suporte**;
6. toda interação é registrada com origem (`LLM`, `FAQ`, `SEM_RESPOSTA`) e
   pontuação, alimentando as estatísticas do administrador.

A base inicial tem 20 perguntas sobre ocorrências, fotos, GPS, status,
permissões, suporte, conta, privacidade e o próprio assistente. Ela é carregada
por `FaqInitializer` de forma idempotente (não duplica ao reiniciar).

Ativar a IA:

```powershell
# Gemini (tem plano gratuito)
$env:GEMINI_API_KEY="sua-chave"
.\iniciar-servidor.ps1

# ou OpenAI
$env:OPENAI_API_KEY="sua-chave"
$env:ASSISTENTE_LLM_PROVIDER="openai"
.\iniciar-servidor.ps1
```

Testar sem o app:

```bash
curl -X POST http://localhost:8080/assistente/perguntas \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"pergunta":"Como registro uma ocorrência com foto?"}'
```

## Testes

```bash
mvn test
```

Usa H2 em memória (`src/test/resources/application-test.properties`), sem precisar do MySQL.

## Estrutura

```
src/main/java/br/com/observacidade
├── config/        CORS, OpenAPI, admin inicial
├── controller/    Rotas REST
├── dto/           Entradas/saídas da API
├── entity/        Entidades JPA
├── enums/         Categorias, urgências, status
├── exception/     Erros padronizados em JSON
├── repository/    Acesso a dados + filtros
├── security/      JWT, Spring Security, usuário atual
└── service/       Regras de negócio
```

## Observações de segurança

- Senhas são armazenadas com BCrypt.
- Rotas de administração exigem `ROLE_ADMIN`.
- Um cidadão só edita/exclui as próprias ocorrências.
- Token inválido/expirado retorna **401 JSON**; falta de permissão retorna **403 JSON**.
- Em produção: troque `JWT_SECRET`, a senha do admin, a senha do banco e restrinja `CORS_ALLOWED_ORIGINS`.

## Migração de fotos antigas

Se o banco já possuía ocorrências com a coluna `ocorrencias.imagem_base64`, na primeira execução as fotos são copiadas automaticamente para a nova tabela `ocorrencia_imagens` e a coluna antiga é limpa. Nenhuma ação manual é necessária.

## Frontend

Este repositório contém apenas o backend. O app (React Native) deve consumir:

- `GET /dominios` para montar os selects;
- `POST /auth/cadastro` e `/auth/login` para autenticação;
- `POST /ocorrencias` com `latitude`/`longitude` e `imagemBase64`;
- `GET /ocorrencias/minhas` e `GET /notificacoes` para acompanhamento;
- `GET /ocorrencias/{id}/imagem` para exibir a foto.
