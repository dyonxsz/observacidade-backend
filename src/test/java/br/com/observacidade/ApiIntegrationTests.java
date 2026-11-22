package br.com.observacidade;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiIntegrationTests {

    private static final String IMAGEM_1PX_PNG =
            "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // =========================
    // Autenticação
    // =========================

    @Test
    void cadastroLoginEPerfilFuncionam() throws Exception {
        String email = email("cidadao");

        MvcResult cadastro = mockMvc.perform(post("/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("nome", "Cidadão Teste", "email", email, "senha", "senha123"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andReturn();

        String token = token(cadastro);

        mockMvc.perform(get("/auth/me").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.senha").doesNotExist());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "senha", "senha123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "senha", "errada"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void cadastroRecusaEmailDuplicadoEInvalido() throws Exception {
        String email = email("duplicado");

        cadastrar(email);

        mockMvc.perform(post("/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("nome", "Outro", "email", email.toUpperCase(), "senha", "senha123"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("E-mail já cadastrado"));

        mockMvc.perform(post("/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("nome", "Outro", "email", "email-invalido", "senha", "senha123"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.email").exists());
    }

    @Test
    void rotaProtegidaSemTokenRetorna401EmJson() throws Exception {
        mockMvc.perform(get("/ocorrencias"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.mensagem").isNotEmpty());
    }

    @Test
    void usuarioPodeAtualizarPerfilEAlterarSenha() throws Exception {
        String email = email("perfil");
        String token = cadastrar(email);

        mockMvc.perform(put("/auth/me")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("nome", "Nome Atualizado", "cidade", "São Paulo", "telefone", "11999999999"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Nome Atualizado"))
                .andExpect(jsonPath("$.cidade").value("São Paulo"));

        mockMvc.perform(put("/auth/me/senha")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("senhaAtual", "senha123", "novaSenha", "novaSenha123"))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "senha", "novaSenha123"))))
                .andExpect(status().isOk());
    }

    // =========================
    // Ocorrências
    // =========================

    @Test
    void cicloCompletoDeOcorrenciaIncluiImagemHistoricoEFiltros() throws Exception {
        String email = email("ocorrencia");
        String token = cadastrar(email);

        MvcResult criada = mockMvc.perform(post("/ocorrencias")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(ocorrenciaPayload())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.protocolo").value(startsWith("OC-")))
                .andExpect(jsonPath("$.status").value("RECEBIDO"))
                .andExpect(jsonPath("$.statusLabel").value("Recebida"))
                .andExpect(jsonPath("$.categoriaLabel").value("Infraestrutura / buracos"))
                .andExpect(jsonPath("$.latitude").value(-23.55))
                .andExpect(jsonPath("$.temImagem").value(true))
                .andExpect(jsonPath("$.historico", hasSize(1)))
                .andReturn();

        long id = id(criada);

        mockMvc.perform(get("/ocorrencias/{id}", id).header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalComentarios").value(0))
                .andExpect(jsonPath("$.imagemUrl").value("/ocorrencias/" + id + "/imagem"))
                .andExpect(jsonPath("$.historico[0].statusNovo").value("RECEBIDO"));

        mockMvc.perform(get("/ocorrencias/{id}", id)
                        .param("incluirImagem", "true")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imagemBase64").isNotEmpty());

        mockMvc.perform(get("/ocorrencias/{id}/imagem", id).header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/png"));

        mockMvc.perform(get("/ocorrencias/minhas").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(id))
                .andExpect(jsonPath("$.content[0].temImagem").value(true));

        mockMvc.perform(get("/ocorrencias")
                        .header("Authorization", bearer(token))
                        .param("status", "RECEBIDO")
                        .param("categoria", "INFRAESTRUTURA")
                        .param("bairro", "Centro")
                        .param("busca", "buraco"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(id));

        mockMvc.perform(get("/ocorrencias/estatisticas").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.porStatus.RECEBIDO").value(greaterThanOrEqualTo(1)));

        // notificação de ocorrência registrada
        mockMvc.perform(get("/notificacoes").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].titulo", hasItem("Ocorrência registrada")));

        mockMvc.perform(get("/notificacoes/contador").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.naoLidas").value(greaterThanOrEqualTo(1)));
    }

    @Test
    void usuarioNaoPodeAlterarOcorrenciaDeOutro() throws Exception {
        String tokenDono = cadastrar(email("dono"));
        String tokenOutro = cadastrar(email("outro"));

        long id = criarOcorrencia(tokenDono);

        Map<String, Object> alteracao = ocorrenciaPayload();
        alteracao.put("descricao", "Tentativa de alteração indevida");

        mockMvc.perform(put("/ocorrencias/{id}", id)
                        .header("Authorization", bearer(tokenOutro))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(alteracao)))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/ocorrencias/{id}", id)
                        .header("Authorization", bearer(tokenOutro)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/ocorrencias/{id}", id).header("Authorization", bearer(tokenDono)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").value("Buraco na rua principal"));
    }

    @Test
    void somenteAdminAlteraStatusEUsuarioRecebeNotificacao() throws Exception {
        String email = email("status");
        String token = cadastrar(email);
        long id = criarOcorrencia(token);

        // usuário comum não pode alterar status
        mockMvc.perform(put("/ocorrencias/{id}/status", id)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("status", "EM_ANDAMENTO"))))
                .andExpect(status().isForbidden());

        String admin = adminToken();

        mockMvc.perform(put("/ocorrencias/{id}/status", id)
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("status", "EM_ANALISE", "observacao", "Equipe técnica acionada"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_ANALISE"))
                .andExpect(jsonPath("$.statusLabel").value("Em análise"))
                .andExpect(jsonPath("$.historico", hasSize(2)))
                .andExpect(jsonPath("$.historico[1].observacao").value("Equipe técnica acionada"));

        mockMvc.perform(put("/ocorrencias/{id}/status", id)
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("status", "EM_ANDAMENTO"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_ANDAMENTO"))
                .andExpect(jsonPath("$.statusLabel").value("Em andamento"));

        mockMvc.perform(get("/notificacoes").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].tipo", hasItem("EM_ANDAMENTO")));
    }

    @Test
    void comentariosNotificamDonoEPodemSerExcluidosPeloAutor() throws Exception {
        String emailDono = email("dono-comentario");
        String tokenDono = cadastrar(emailDono);
        String tokenOutro = cadastrar(email("comentarista"));

        long id = criarOcorrencia(tokenDono);

        MvcResult comentario = mockMvc.perform(post("/ocorrencias/{id}/comentarios", id)
                        .header("Authorization", bearer(tokenOutro))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("texto", "Também vi esse problema"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.texto").value("Também vi esse problema"))
                .andReturn();

        long comentarioId = objectMapper.readTree(comentario.getResponse()
                .getContentAsString(StandardCharsets.UTF_8)).get("id").asLong();

        mockMvc.perform(get("/notificacoes").header("Authorization", bearer(tokenDono)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].tipo", hasItem("COMENTARIO")));

        mockMvc.perform(get("/ocorrencias/{id}", id).header("Authorization", bearer(tokenDono)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalComentarios").value(1));

        // o dono da ocorrência não é o autor do comentário: não pode excluir
        mockMvc.perform(delete("/ocorrencias/{id}/comentarios/{comentarioId}", id, comentarioId)
                        .header("Authorization", bearer(tokenDono)))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/ocorrencias/{id}/comentarios/{comentarioId}", id, comentarioId)
                        .header("Authorization", bearer(tokenOutro)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/ocorrencias/{id}/comentarios", id).header("Authorization", bearer(tokenDono)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void uploadMultipartEAtualizacaoDeOcorrenciaFuncionam() throws Exception {
        String token = cadastrar(email("upload"));
        long id = criarOcorrencia(token);

        org.springframework.mock.web.MockMultipartFile arquivo =
                new org.springframework.mock.web.MockMultipartFile(
                        "arquivo", "foto.png", "image/png",
                        java.util.Base64.getDecoder().decode(IMAGEM_1PX_PNG.split(",")[1]));

        mockMvc.perform(multipart("/ocorrencias/{id}/imagem", id)
                        .file(arquivo)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contentType").value("image/png"))
                .andExpect(jsonPath("$.imagemBase64").isNotEmpty());

        Map<String, Object> payload = ocorrenciaPayload();
        payload.put("descricao", "Descrição corrigida");
        payload.put("urgencia", "MEDIA");

        mockMvc.perform(put("/ocorrencias/{id}", id)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").value("Descrição corrigida"))
                .andExpect(jsonPath("$.urgencia").value("MEDIA"))
                .andExpect(jsonPath("$.temImagem").value(true));

        mockMvc.perform(delete("/ocorrencias/{id}", id)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/ocorrencias/{id}", id).header("Authorization", bearer(token)))
                .andExpect(status().isNotFound());
    }

    @Test
    void validacaoDeOcorrenciaRetornaCampos() throws Exception {
        String token = cadastrar(email("validacao"));

        mockMvc.perform(post("/ocorrencias")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("descricao", "abc", "urgencia", "ALTA"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.categoria").exists())
                .andExpect(jsonPath("$.campos.descricao").exists());

        mockMvc.perform(post("/ocorrencias")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("categoria", "INEXISTENTE", "descricao", "Teste válido", "urgencia", "ALTA"))))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/ocorrencias").param("status", "INVALIDO").header("Authorization", bearer(token)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value(containsString("status")));

        mockMvc.perform(get("/ocorrencias/abc").header("Authorization", bearer(token)))
                .andExpect(status().isBadRequest());
    }

    // =========================
    // Suporte
    // =========================

    @Test
    void suporteAceitaChamadoERespostaDoAdmin() throws Exception {
        String token = cadastrar(email("suporte"));

        MvcResult criado = mockMvc.perform(post("/suporte")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("assunto", "Dúvida", "mensagem", "Como acompanhar minha ocorrência?"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ABERTO"))
                .andReturn();

        long id = objectMapper.readTree(criado.getResponse()
                .getContentAsString(StandardCharsets.UTF_8)).get("id").asLong();

        mockMvc.perform(get("/suporte").header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/suporte").header("Authorization", bearer(adminToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].id", hasItem((int) id)));

        mockMvc.perform(put("/suporte/{id}", id)
                        .header("Authorization", bearer(adminToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("resposta", "Pelo menu Minhas ocorrências.", "status", "RESPONDIDO"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESPONDIDO"))
                .andExpect(jsonPath("$.resposta").value("Pelo menu Minhas ocorrências."))
                .andExpect(jsonPath("$.respondidoPor").value("Administrador"));

        mockMvc.perform(get("/suporte/meus").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].resposta").value("Pelo menu Minhas ocorrências."));

        mockMvc.perform(get("/notificacoes").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].titulo", hasItem("Resposta do suporte")));
    }

    // =========================
    // Administração de usuários
    // =========================

    @Test
    void adminGerenciaUsuariosERotasSaoProtegidas() throws Exception {
        String email = email("gerencia");
        String token = cadastrar(email);

        mockMvc.perform(get("/usuarios").header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());

        String admin = adminToken();

        MvcResult lista = mockMvc.perform(get("/usuarios").header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andReturn();

        long id = -1;
        for (JsonNode node : objectMapper.readTree(lista.getResponse()
                .getContentAsString(StandardCharsets.UTF_8)).get("content")) {
            if (email.equalsIgnoreCase(node.get("email").asText())) {
                id = node.get("id").asLong();
            }
        }
        org.junit.jupiter.api.Assertions.assertTrue(id > 0);

        mockMvc.perform(put("/usuarios/{id}/admin", id).header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));

        // o mesmo token passa a ter acesso de admin, pois a role é lida do banco
        mockMvc.perform(get("/usuarios").header("Authorization", bearer(token)))
                .andExpect(status().isOk());

        mockMvc.perform(put("/usuarios/{id}/user", id).header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("USER"));

        // admin não pode remover o próprio acesso
        MvcResult adminMe = mockMvc.perform(get("/auth/me").header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andReturn();

        long adminId = objectMapper.readTree(adminMe.getResponse()
                .getContentAsString(StandardCharsets.UTF_8)).get("id").asLong();

        mockMvc.perform(put("/usuarios/{id}/user", adminId).header("Authorization", bearer(admin)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void notificacoesPodemSerMarcadasComoLidas() throws Exception {        String token = cadastrar(email("notificacao"));
        criarOcorrencia(token);

        mockMvc.perform(put("/notificacoes/marcar-lidas").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.marcadas").value(greaterThanOrEqualTo(1)));

        mockMvc.perform(get("/notificacoes").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        mockMvc.perform(get("/notificacoes/todas").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].lida", hasItem(true)));
    }

    @Test
    void adminPodeExcluirUsuarioComDadosVinculados() throws Exception {
        String email = email("excluir");
        String token = cadastrar(email);
        long ocorrenciaId = criarOcorrencia(token);

        mockMvc.perform(post("/ocorrencias/{id}/comentarios", ocorrenciaId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("texto", "Comentário do próprio autor"))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/suporte")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("assunto", "Teste", "mensagem", "Chamado do usuário"))))
                .andExpect(status().isCreated());

        MvcResult me = mockMvc.perform(get("/auth/me").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn();

        long usuarioId = objectMapper.readTree(me.getResponse()
                .getContentAsString(StandardCharsets.UTF_8)).get("id").asLong();

        mockMvc.perform(delete("/usuarios/{id}", usuarioId)
                        .header("Authorization", bearer(adminToken())))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/usuarios/{id}", usuarioId)
                        .header("Authorization", bearer(adminToken())))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/ocorrencias/{id}", ocorrenciaId)
                        .header("Authorization", bearer(adminToken())))
                .andExpect(status().isNotFound());
    }

    @Test
    void dominiosSaoPublicos() throws Exception {
        mockMvc.perform(get("/dominios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categorias", hasSize(greaterThanOrEqualTo(10))))
                .andExpect(jsonPath("$.status[*].valor", hasItem("EM_ANDAMENTO")));
    }

    // =========================
    // Helpers
    // =========================

    private String cadastrar(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("nome", "Usuário Teste", "email", email, "senha", "senha123"))))
                .andExpect(status().isCreated())
                .andReturn();

        return token(result);
    }

    private String adminToken() throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", "admin@teste.com", "senha", "admin123"))))
                .andExpect(status().isOk())
                .andReturn();

        return token(result);
    }

    private long criarOcorrencia(String token) throws Exception {
        MvcResult result = mockMvc.perform(post("/ocorrencias")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(ocorrenciaPayload())))
                .andExpect(status().isCreated())
                .andReturn();

        return id(result);
    }

    private Map<String, Object> ocorrenciaPayload() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("categoria", "INFRAESTRUTURA");
        payload.put("descricao", "Buraco na rua principal");
        payload.put("rua", "Rua das Flores");
        payload.put("bairro", "Centro");
        payload.put("referencia", "Próximo à escola");
        payload.put("latitude", -23.55);
        payload.put("longitude", -46.63);
        payload.put("urgencia", "ALTA");
        payload.put("imagemBase64", IMAGEM_1PX_PNG);
        return payload;
    }

    private String token(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse()
                .getContentAsString(StandardCharsets.UTF_8)).get("token").asText();
    }

    private long id(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse()
                .getContentAsString(StandardCharsets.UTF_8)).get("id").asLong();
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String email(String prefixo) {
        return prefixo + "-" + UUID.randomUUID() + "@teste.com";
    }
}
