# Pitch — Observa Cidade (23/11)

Roteiro curto para a apresentação: o que falar, em que ordem, e o que mostrar na tela.

## 1. Abertura (30 s)

> "Hoje, quando um cidadão vê um buraco na rua, um poste apagado ou lixo acumulado, ele não
> tem um canal direto com a prefeitura. O Observa Cidade resolve isso: o cidadão registra a
> ocorrência com foto e localização pelo celular, e o gestor acompanha, atualiza o status e
> responde pelo mesmo sistema."

- Problema: comunicação fragmentada (telefone, rede social, papel) e falta de retorno ao cidadão.
- Solução: app mobile + API + painel do administrador + assistente virtual.
- Público: cidadão e equipe da prefeitura.

## 2. Demonstração (3–4 min)

Ordem sugerida (tudo já logado, evite digitar senha na hora):

1. **Cidadão — registrar**
   - Tela inicial → **Nova ocorrência**
   - Escolher categoria (ex.: Iluminação), escrever descrição
   - Tirar/escolher foto e confirmar o GPS
   - Enviar → aparece no feed com a foto
2. **Cidadão — acompanhar**
   - Abrir a ocorrência → mostrar status, histórico e comentários
   - Mostrar que só o dono vê as opções de **editar/excluir**
3. **Administrador**
   - Login como admin → painel com estatísticas
   - Abrir a ocorrência recém-criada → mudar status para **Em andamento**
   - Responder por comentário
4. **Notificação**
   - Voltar ao cidadão → sino com aviso da mudança de status
5. **Assistente (o diferencial novo)**
   - Tocar no ícone 💬 no topo
   - Perguntar: *"Como registro uma ocorrência com foto?"*
   - Mostrar a resposta e o selo **Base de conhecimento**
   - Perguntar algo fora da base (*"qual a capital da Austrália?"*) → ele assume que não sabe
     e indica o suporte
6. **Swagger** (se sobrar tempo)
   - `http://localhost:8080/swagger-ui.html` — API documentada e testável

## 3. Arquitetura (1 min)

```
App Expo / React Native (cidadão e admin)
        │  HTTPS + JWT
        ▼
API Spring Boot 3.5 (Java 17)
  ├── Spring Security + JWT (ROLE_ADMIN)
  ├── Ocorrências, histórico, comentários, notificações, suporte
  ├── Imagens em tabela própria (acesso autenticado)
  └── Assistente: FAQ + TF-IDF/cosseno + LLM opcional
        │
        ▼
MySQL 8 (produção) ou H2 (dev)
```

- O app descobre sozinho o endereço da API (IP local do Expo, endereço embutido ou túnel).
- Swagger/OpenAPI para demonstrar os endpoints.
- Testes automatizados: integração (16) + roteiro E2E (29) + assistente (14).

## 4. Agente de IA (1–2 min)

- **Base curada**: 20 perguntas frequentes sobre o app (foto, GPS, status, permissões,
  suporte, conta, privacidade).
- **Busca**: normalização de acentos/plural + TF-IDF e similaridade de cosseno, em Java puro.
  Não depende de serviço externo nem de banco vetorial.
- **LLM opcional**: com chave Gemini/OpenAI, os trechos encontrados viram contexto e o modelo
  redige a resposta (RAG). Sem chave, a base responde sozinha — **fallback automático**.
- **Aprendizado**: toda pergunta é registrada com origem (`LLM`, `FAQ`, `SEM_RESPOSTA`) e
  pontuação; o admin vê as perguntas sem resposta para melhorar a base.
- **Segurança**: não pede senha nem dados pessoais; instruções proíbem inventar prazos,
  telefones e políticas.

Frase para o professor:

> "O agente não é um chat genérico: ele é ancorado numa base de conhecimento do domínio e
> funciona mesmo sem internet/API de IA. A IA é uma camada de redação, não uma dependência."

## 5. Segurança e privacidade (30 s)

- Senhas com BCrypt; autenticação stateless por JWT.
- Cidadão só edita/exclui as **próprias** ocorrências.
- Rotas de administração exigem `ROLE_ADMIN`.
- Fotos exigem token para serem abertas.
- Credenciais de admin não aparecem na tela de login.

## 6. Diferenciais (30 s)

- Funciona ponta a ponta: app, API, banco, painel e assistente.
- Roda sem instalar banco (perfil `dev` com H2) e também com MySQL/Docker.
- Acesso de qualquer lugar por túnel (Cloudflare/Expo), sem DNS próprio.
- Assistente híbrido (FAQ + IA) com fallback e métricas.
- Documentação e testes no repositório.

## 7. Encerramento e roadmap (30 s)

> "O próximo passo é integrar com a secretaria responsável (roteamento por categoria), autenticar
> com a conta gov.br e usar as métricas do assistente para ampliar a base de conhecimento."

- Integração com sistemas da prefeitura.
- Notificações push nativas.
- Mapa com agrupamento de ocorrências por bairro.
- Painel web público de transparência.

## Perguntas prováveis

**"Por que FAQ e não banco vetorial?"**
> Para ~20 perguntas do domínio, TF-IDF/cosseno resolve com precisão e custo zero. A arquitetura
> está isolada em `MotorBuscaFaq`; trocar por embeddings é substituir essa classe, sem mexer no
> restante.

**"E se a IA errar?"**
> Ela recebe instruções para responder só com o contexto, sem inventar prazos ou telefones.
> Se não houver contexto suficiente, o próprio serviço devolve a mensagem de "não encontrado" e
> orienta o suporte. Além disso, a resposta da base continua disponível como fallback.

**"O que acontece se a internet cair?"**
> O assistente cai para a base local (se a IA estiver configurada) e o app avisa quando não
> consegue falar com o servidor. As ocorrências exigem conexão, como qualquer app cliente-servidor.

**"Como o app acha o servidor?"**
> Ele testa em paralelo o IP do Expo, o endereço embutido e o túnel público, e usa o primeiro que
> responder. Também é possível informar manualmente em **login → Servidor**.

**"Isso está no GitHub?"**
> Sim: backend em `dyonxsz/observacidade-backend` e app em `ruaandsx/alerta_cidade`, com README,
> Swagger e instruções de execução.
