package br.com.observacidade.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Cliente da IA generativa (opcional).
 *
 * O assistente funciona 100% sem isto: a base de FAQ responde sozinha.
 * Basta definir a chave para as respostas passarem a ser redigidas pelo modelo:
 *
 *   GEMINI_API_KEY=...            (padrão, provedor gemini)
 *   OPENAI_API_KEY=...            (provedor openai)
 *   ASSISTENTE_LLM_PROVIDER=openai|gemini
 */
@Component
public class AssistenteLlmClient {

    private static final Logger log = LoggerFactory.getLogger(AssistenteLlmClient.class);

    private final ObjectMapper mapper;
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Value("${assistente.llm.habilitado:true}")
    private boolean habilitado;

    @Value("${assistente.llm.provider:gemini}")
    private String provider;

    @Value("${assistente.llm.chave:}")
    private String chave;

    @Value("${assistente.llm.modelo:}")
    private String modelo;

    @Value("${assistente.llm.url:}")
    private String url;

    @Value("${assistente.llm.timeout-segundos:20}")
    private int timeoutSegundos;

    public AssistenteLlmClient(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public boolean disponivel() {
        return habilitado && chave != null && !chave.isBlank();
    }

    public String provedor() {
        String valor = (provider == null || provider.isBlank()) ? "gemini" : provider.trim();
        return valor.toLowerCase();
    }

    public Optional<String> responder(String instrucoes, String pergunta) {
        if (!disponivel()) return Optional.empty();

        try {
            boolean openai = "openai".equals(provedor()) || "compativel".equals(provedor());

            HttpRequest requisicao = openai
                    ? requisicaoOpenAi(instrucoes, pergunta)
                    : requisicaoGemini(instrucoes, pergunta);

            HttpResponse<String> resposta = http.send(requisicao,
                    HttpResponse.BodyHandlers.ofString());

            if (resposta.statusCode() < 200 || resposta.statusCode() >= 300) {
                log.warn("IA respondeu {}: {}", resposta.statusCode(), resumir(resposta.body()));
                return Optional.empty();
            }

            JsonNode raiz = mapper.readTree(resposta.body());
            String texto = openai ? extrairOpenAi(raiz) : extrairGemini(raiz);

            if (texto == null || texto.isBlank()) {
                log.warn("IA respondeu sem texto utilizável.");
                return Optional.empty();
            }

            return Optional.of(texto.trim());

        } catch (Exception e) {
            log.warn("Falha ao consultar a IA ({}): {}", provedor(), e.getMessage());
            return Optional.empty();
        }
    }

    // ── Gemini ───────────────────────────────────────────────────────────────

    private HttpRequest requisicaoGemini(String instrucoes, String pergunta) throws Exception {
        String modeloFinal = (modelo == null || modelo.isBlank()) ? "gemini-2.0-flash" : modelo.trim();
        String endereco = (url == null || url.isBlank())
                ? "https://generativelanguage.googleapis.com/v1beta/models/" + modeloFinal + ":generateContent"
                : url.trim();

        ObjectNode corpo = mapper.createObjectNode();

        ObjectNode sistema = corpo.putObject("systemInstruction");
        sistema.putArray("parts").addObject().put("text", instrucoes);

        ArrayNode contents = corpo.putArray("contents");
        ObjectNode conteudo = contents.addObject();
        conteudo.put("role", "user");
        conteudo.putArray("parts").addObject().put("text", pergunta);

        ObjectNode config = corpo.putObject("generationConfig");
        config.put("temperature", 0.3);
        config.put("maxOutputTokens", 500);

        return HttpRequest.newBuilder()
                .uri(URI.create(endereco))
                .timeout(Duration.ofSeconds(timeoutSegundos))
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", chave.trim())
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(corpo)))
                .build();
    }

    private String extrairGemini(JsonNode raiz) {
        JsonNode partes = raiz.path("candidates").path(0).path("content").path("parts");
        if (!partes.isArray()) return null;

        StringBuilder texto = new StringBuilder();
        for (JsonNode parte : partes) {
            texto.append(parte.path("text").asText(""));
        }
        return texto.toString();
    }

    // ── OpenAI / compatível ──────────────────────────────────────────────────

    private HttpRequest requisicaoOpenAi(String instrucoes, String pergunta) throws Exception {
        String modeloFinal = (modelo == null || modelo.isBlank()) ? "gpt-4o-mini" : modelo.trim();
        String endereco = (url == null || url.isBlank())
                ? "https://api.openai.com/v1/chat/completions"
                : url.trim();

        ObjectNode corpo = mapper.createObjectNode();
        corpo.put("model", modeloFinal);
        corpo.put("temperature", 0.3);
        corpo.put("max_tokens", 500);

        ArrayNode mensagens = corpo.putArray("messages");
        mensagens.addObject().put("role", "system").put("content", instrucoes);
        mensagens.addObject().put("role", "user").put("content", pergunta);

        return HttpRequest.newBuilder()
                .uri(URI.create(endereco))
                .timeout(Duration.ofSeconds(timeoutSegundos))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + chave.trim())
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(corpo)))
                .build();
    }

    private String extrairOpenAi(JsonNode raiz) {
        return raiz.path("choices").path(0).path("message").path("content").asText(null);
    }

    private String resumir(String corpo) {
        if (corpo == null) return "";
        return corpo.length() > 300 ? corpo.substring(0, 300) + "..." : corpo;
    }
}
