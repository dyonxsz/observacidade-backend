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

    @Value("${GEMINI_API_KEY:}")
    private String geminiChave;

    @Value("${OPENAI_API_KEY:}")
    private String openaiChave;

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
        return habilitado && !chaveAtiva().isBlank();
    }

    /** Chave efetiva: a configurada no assistente ou a variável do provedor. */
    private String chaveAtiva() {
        if (chave != null && !chave.isBlank()) return chave.trim();

        boolean openai = "openai".equals(provedor()) || "compativel".equals(provedor());
        String daVez = openai ? openaiChave : geminiChave;

        if (daVez != null && !daVez.isBlank()) return daVez.trim();

        // Se só a outra variável estiver definida, usa ela (evita exigir configurar o provedor).
        String alternativa = openai ? geminiChave : openaiChave;
        return alternativa == null ? "" : alternativa.trim();
    }

    public String provedor() {
        String valor = (provider == null || provider.isBlank()) ? "auto" : provider.trim().toLowerCase();

        if ("auto".equals(valor)) {
            return temTexto(openaiChave) && !temTexto(geminiChave) ? "openai" : "gemini";
        }

        return valor;
    }

    private static boolean temTexto(String valor) {
        return valor != null && !valor.isBlank();
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
        // A API de Interactions é a recomendada pelo Google em 2026 e é bem mais
        // rápida que o generateContent para os modelos Gemini 3.x.
        String modeloFinal = (modelo == null || modelo.isBlank())
                ? "gemini-3.5-flash-lite"
                : modelo.trim();
        String endereco = (url == null || url.isBlank())
                ? "https://generativelanguage.googleapis.com/v1beta/interactions"
                : url.trim();

        ObjectNode corpo = mapper.createObjectNode();
        corpo.put("model", modeloFinal);
        corpo.put("input", pergunta);
        corpo.put("store", false);
        corpo.put("system_instruction", instrucoes);

        ObjectNode config = corpo.putObject("generation_config");
        config.put("thinking_level", "low");
        config.put("temperature", 0.3);
        config.put("max_output_tokens", 600);

        return HttpRequest.newBuilder()
                .uri(URI.create(endereco))
                .timeout(Duration.ofSeconds(timeoutSegundos))
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", chaveAtiva())
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(corpo)))
                .build();
    }

    private String extrairGemini(JsonNode raiz) {
        JsonNode passos = raiz.path("steps");
        if (!passos.isArray()) return null;

        StringBuilder texto = new StringBuilder();
        for (JsonNode passo : passos) {
            if (!"model_output".equals(passo.path("type").asText())) continue;

            for (JsonNode parte : passo.path("content")) {
                if ("text".equals(parte.path("type").asText())) {
                    texto.append(parte.path("text").asText(""));
                }
            }
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
                .header("Authorization", "Bearer " + chaveAtiva())
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
