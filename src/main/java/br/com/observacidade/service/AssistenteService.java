package br.com.observacidade.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import br.com.observacidade.dto.AssistenteEstatisticasDTO;
import br.com.observacidade.dto.AssistenteRequestDTO;
import br.com.observacidade.dto.AssistenteResponseDTO;
import br.com.observacidade.dto.InteracaoAssistenteDTO;
import br.com.observacidade.entity.FaqAssistente;
import br.com.observacidade.entity.InteracaoAssistente;
import br.com.observacidade.entity.Usuario;
import br.com.observacidade.repository.FaqAssistenteRepository;
import br.com.observacidade.repository.InteracaoAssistenteRepository;
import br.com.observacidade.repository.UsuarioRepository;

/**
 * Assistente virtual do Observa Cidade.
 *
 * Fluxo da resposta:
 *  1. busca a pergunta na base de conhecimento (TF-IDF/cosseno);
 *  2. se houver modelo configurado, pede a redação para a IA usando os
 *     trechos encontrados como contexto (RAG simples);
 *  3. se a IA não estiver disponível ou falhar, devolve a resposta da base;
 *  4. se nada for encontrado, orienta a abrir um chamado no suporte.
 */
@Service
public class AssistenteService {

    private static final Logger log = LoggerFactory.getLogger(AssistenteService.class);

    public static final String ORIGEM_LLM = "LLM";
    public static final String ORIGEM_FAQ = "FAQ";
    public static final String ORIGEM_VAZIA = "SEM_RESPOSTA";

    private static final String MENSAGEM_SEM_RESPOSTA =
            "Não encontrei essa informação na minha base de conhecimento. "
                    + "Tente reformular a pergunta com outras palavras ou abra um chamado em "
                    + "\"Ajuda e Suporte\", que a equipe responde por lá.";

    private final FaqAssistenteRepository faqRepository;
    private final InteracaoAssistenteRepository interacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final AssistenteLlmClient llm;

    @Value("${assistente.faq.pontuacao-minima:0.18}")
    private double pontuacaoMinima;

    @Value("${assistente.faq.limite-sugestoes:4}")
    private int limiteSugestoes;

    private volatile MotorBuscaFaq motor;
    private volatile int tamanhoMotor = -1;

    public AssistenteService(
            FaqAssistenteRepository faqRepository,
            InteracaoAssistenteRepository interacaoRepository,
            UsuarioRepository usuarioRepository,
            AssistenteLlmClient llm) {
        this.faqRepository = faqRepository;
        this.interacaoRepository = interacaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.llm = llm;
    }

    public AssistenteResponseDTO perguntar(AssistenteRequestDTO dto, String email) {
        String pergunta = dto.getPergunta() == null ? "" : dto.getPergunta().trim();

        MotorBuscaFaq motorAtual = motor();
        List<MotorBuscaFaq.Achado> achados = motorAtual.buscar(pergunta, 3);

        MotorBuscaFaq.Achado melhor = achados.isEmpty() ? null : achados.get(0);
        double pontuacao = melhor == null ? 0 : melhor.pontuacao();

        // Só considera "relacionada" quando a similaridade passa do limite;
        // caso contrário seria enganoso exibir um assunto aleatório.
        MotorBuscaFaq.Achado confiavel = (melhor != null && pontuacao >= pontuacaoMinima) ? melhor : null;

        String resposta;
        String origem;

        Optional<String> respostaIa = llm.disponivel()
                ? llm.responder(instrucoes(achados), pergunta)
                : Optional.empty();

        if (respostaIa.isPresent()) {
            resposta = respostaIa.get();
            origem = ORIGEM_LLM;
        } else if (melhor != null && pontuacao >= pontuacaoMinima) {
            resposta = melhor.resposta();
            origem = ORIGEM_FAQ;
        } else {
            resposta = MENSAGEM_SEM_RESPOSTA;
            origem = ORIGEM_VAZIA;
        }

        registrar(email, pergunta, resposta, origem, pontuacao, confiavel);

        return new AssistenteResponseDTO(
                resposta,
                origem,
                arredondar(pontuacao),
                confiavel == null ? null : confiavel.pergunta(),
                sugestoes(achados, confiavel),
                LocalDateTime.now());
    }

    /** Sugestões iniciais exibidas na tela do assistente. */
    public List<String> sugestoes() {
        List<String> perguntas = new ArrayList<>();
        for (FaqAssistente faq : faqRepository.findByAtivoTrueOrderByIdAsc()) {
            perguntas.add(faq.getPergunta());
            if (perguntas.size() >= limiteSugestoes) break;
        }
        return perguntas;
    }

    public List<InteracaoAssistenteDTO> historico(String email) {
        Optional<Usuario> usuario = usuarioRepository.findByEmailIgnoreCase(email);
        if (usuario.isEmpty()) return List.of();

        return interacaoRepository.findTop20ByUsuarioOrderByDataHoraDesc(usuario.get()).stream()
                .map(i -> new InteracaoAssistenteDTO(
                        i.getPergunta(), i.getResposta(), i.getOrigem(), i.getDataHora()))
                .toList();
    }

    public AssistenteEstatisticasDTO estatisticas() {
        Map<String, Long> porOrigem = new LinkedHashMap<>();
        porOrigem.put(ORIGEM_LLM, interacaoRepository.countByOrigem(ORIGEM_LLM));
        porOrigem.put(ORIGEM_FAQ, interacaoRepository.countByOrigem(ORIGEM_FAQ));
        porOrigem.put(ORIGEM_VAZIA, interacaoRepository.countByOrigem(ORIGEM_VAZIA));

        List<String> semResposta = interacaoRepository.findTop5ByOrigemOrderByDataHoraDesc(ORIGEM_VAZIA)
                .stream()
                .map(InteracaoAssistente::getPergunta)
                .toList();

        return new AssistenteEstatisticasDTO(interacaoRepository.count(), porOrigem, semResposta);
    }

    public boolean iaDisponivel() {
        return llm.disponivel();
    }

    public String iaProvedor() {
        return llm.disponivel() ? llm.provedor() : "base-faq";
    }

    // ── apoio ────────────────────────────────────────────────────────────────

    private MotorBuscaFaq motor() {
        List<FaqAssistente> faqs = faqRepository.findByAtivoTrueOrderByIdAsc();

        if (motor == null || tamanhoMotor != faqs.size()) {
            motor = new MotorBuscaFaq(faqs);
            tamanhoMotor = faqs.size();
            log.info("Base de conhecimento do assistente carregada: {} perguntas", faqs.size());
        }

        return motor;
    }

    private List<String> sugestoes(List<MotorBuscaFaq.Achado> achados, MotorBuscaFaq.Achado respondida) {
        Set<String> respostas = new LinkedHashSet<>();
        String jaUsada = respondida == null ? null : respondida.pergunta();

        // Depois de responder, sugere assuntos próximos (pulando o que já foi usado).
        for (int i = 1; i < achados.size(); i++) {
            if (!achados.get(i).pergunta().equals(jaUsada)) {
                respostas.add(achados.get(i).pergunta());
            }
        }

        for (String pergunta : sugestoes()) {
            if (!pergunta.equals(jaUsada)) {
                respostas.add(pergunta);
            }
        }

        List<String> lista = new ArrayList<>(respostas);
        return lista.size() > limiteSugestoes ? lista.subList(0, limiteSugestoes) : lista;
    }

    private String instrucoes(List<MotorBuscaFaq.Achado> achados) {
        StringBuilder texto = new StringBuilder();

        texto.append("Você é o assistente virtual do aplicativo Observa Cidade, um canal de ")
                .append("registro de ocorrências urbanas (buracos, iluminação, lixo, alagamento, etc).\n")
                .append("Regras:\n")
                .append("- Responda em português do Brasil, direto e curto (no máximo 4 frases).\n")
                .append("- Use o CONTEXTO abaixo como fonte principal.\n")
                .append("- Se o contexto não tiver a resposta, diga que não sabe e sugira a tela ")
                .append("\"Ajuda e Suporte\".\n")
                .append("- Nunca invente prazos, telefones, valores ou políticas.\n")
                .append("- Nunca peça senha nem dados pessoais.\n")
                .append("- Não use markdown; escreva texto simples.\n\n");

        if (achados.isEmpty()) {
            texto.append("CONTEXTO: (vazio)\n");
        } else {
            texto.append("CONTEXTO:\n");
            int numero = 1;
            for (MotorBuscaFaq.Achado achado : achados) {
                texto.append(numero++).append(") ").append(achado.pergunta()).append("\n")
                        .append("   ").append(achado.resposta()).append("\n");
            }
        }

        return texto.toString();
    }

    private void registrar(String email, String pergunta, String resposta, String origem,
            double pontuacao, MotorBuscaFaq.Achado relacionada) {

        try {
            InteracaoAssistente interacao = new InteracaoAssistente();
            interacao.setPergunta(pergunta);
            interacao.setResposta(resposta);
            interacao.setOrigem(origem);
            interacao.setPontuacao(arredondar(pontuacao));
            interacao.setFaqRelacionada(relacionada == null ? null : relacionada.pergunta());
            interacao.setDataHora(LocalDateTime.now());

            if (email != null && !email.isBlank()) {
                usuarioRepository.findByEmailIgnoreCase(email).ifPresent(interacao::setUsuario);
            }

            interacaoRepository.save(interacao);

        } catch (Exception e) {
            // Nunca derrubar a resposta do assistente por causa do histórico.
            log.warn("Não foi possível registrar a interação do assistente: {}", e.getMessage());
        }
    }

    private double arredondar(double valor) {
        return Math.round(valor * 1000.0) / 1000.0;
    }
}
