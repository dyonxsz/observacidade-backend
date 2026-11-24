# Pitch — Observa Cidade (23/11)

Roteiro da apresentação: o que falar, em que ordem e o que mostrar na tela.

## 1. Abertura (30 s)

> "Quando aparece um buraco na rua, um poste apagado ou lixo acumulado, o cidadão não tem
> um canal direto com a prefeitura. Ele liga, posta em rede social ou desiste. O Observa
> Cidade junta isso num app: o cidadão registra a ocorrência com foto e localização, e o
> gestor vê, muda o status e responde no mesmo sistema."

- Problema: o pedido se perde entre telefone, rede social e papel, e o cidadão nunca fica
  sabendo o que aconteceu.
- Solução: app do cidadão, API, painel do administrador e um assistente que tira dúvidas
  sobre o app.
- Público: quem mora na cidade e quem atende no órgão.

## 2. Demonstração (3–4 min)

Ordem sugerida. Deixe tudo já logado para não digitar senha na hora.

1. **Cidadão: registrar**
   - Tela inicial → Nova ocorrência
   - Escolher a categoria (por exemplo, Iluminação) e escrever a descrição
   - Tirar ou escolher a foto e confirmar o GPS
   - Enviar; a ocorrência aparece no feed com a foto
2. **Cidadão: acompanhar**
   - Abrir a ocorrência e mostrar status, histórico e comentários
   - Mostrar que as opções de editar e excluir só aparecem para o dono
3. **Administrador**
   - Entrar como admin e abrir o painel com as estatísticas
   - Abrir a ocorrência que acabou de ser criada e mudar o status para Em andamento
   - Responder por comentário
4. **Notificação**
   - Voltar para o cidadão e mostrar o sino com o aviso da mudança
5. **Assistente**
   - Tocar no ícone de chat no topo
   - Perguntar: "Como registro uma ocorrência com foto?"
   - Mostrar a resposta e o selo "Base de conhecimento"
   - Perguntar algo fora da base ("qual é a capital da Austrália?") e mostrar que ele
     assume que não sabe e indica o suporte
6. **Swagger** (se sobrar tempo)
   - `http://localhost:8080/swagger-ui.html`, com a API documentada e testável

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

- O app descobre o endereço da API sozinho: IP local do Expo, endereço embutido ou túnel.
- Swagger/OpenAPI para mostrar os endpoints.
- Testes: integração (16), roteiro E2E (29) e assistente (14).

## 4. Agente de IA (1–2 min)

- A base tem 20 perguntas sobre o app: foto, GPS, status, permissões, suporte, conta e
  privacidade. Quem mantém é a gente, no `FaqInitializer`.
- A busca normaliza acento e plural, e compara com TF-IDF e cosseno, tudo em Java. Não
  chama serviço de fora nem precisa de banco vetorial.
- Com a chave do Gemini ou da OpenAI, os trechos encontrados viram contexto e o modelo
  escreve a resposta. Sem chave, a base responde sozinha.
- Toda pergunta fica registrada com a origem (LLM, FAQ ou sem resposta) e a pontuação. O
  admin vê o que ficou sem resposta e usa isso para melhorar a base.
- O assistente não pede senha nem dado pessoal. As instruções dizem para não inventar
  prazo, telefone ou política.

Para dizer ao professor:

> "O agente responde a partir da base do projeto e continua funcionando se a API de IA
> cair ou se a chave não estiver configurada. A IA escreve a resposta; o conteúdo vem do
> FAQ."

## 5. Segurança e privacidade (30 s)

- Senhas com BCrypt e autenticação por JWT, sem sessão no servidor.
- O cidadão só edita e exclui as próprias ocorrências.
- As rotas de administração exigem `ROLE_ADMIN`.
- As fotos precisam de token para abrir.
- O login não mostra as credenciais do admin na tela.

## 6. O que ele tem de diferente (30 s)

- Funciona de ponta a ponta: app, API, banco, painel e assistente.
- Roda sem instalar banco (perfil `dev` com H2) e também com MySQL no Docker.
- Dá para acessar de qualquer lugar por túnel (Cloudflare ou Expo), sem DNS próprio.
- O assistente usa FAQ e IA, com fallback e métricas.
- Documentação e testes ficam no repositório.

## 7. Encerramento e próximos passos (30 s)

> "O próximo passo é mandar cada ocorrência para a secretaria responsável pela categoria,
> entrar com a conta gov.br e usar as perguntas sem resposta para crescer a base."

- Integração com os sistemas da prefeitura.
- Notificação push nativa.
- Mapa com as ocorrências agrupadas por bairro.
- Painel público de transparência.

## Perguntas prováveis

**"Por que FAQ e não banco vetorial?"**
> São 20 perguntas de um domínio fechado. TF-IDF com cosseno acerta bem e não custa nada.
> Deixei a busca isolada em `MotorBuscaFaq`; se um dia precisar de embeddings, troco essa
> classe e o resto continua igual.

**"E se a IA errar?"**
> Ela recebe só o contexto do FAQ, e as instruções dizem para não inventar prazo ou
> telefone. Quando o contexto não basta, o próprio serviço devolve "não encontrado" e
> aponta o suporte. A resposta da base fica de reserva.

**"O que acontece se a internet cair?"**
> Se a IA estiver configurada, o assistente volta para a base local. O app avisa quando
> não alcança o servidor. As ocorrências precisam de conexão, como qualquer app
> cliente-servidor.

**"Como o app acha o servidor?"**
> Ele testa ao mesmo tempo o IP do Expo, o endereço embutido e o túnel, e fica com o
> primeiro que responder. Também dá para informar na mão em login → Servidor.

**"Isso está no GitHub?"**
> Sim. O backend fica em `dyonxsz/observacidade-backend` e o app em
> `ruaandsx/alerta_cidade`, com README, Swagger e o passo a passo para rodar.
