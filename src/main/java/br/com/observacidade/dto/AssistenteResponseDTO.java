package br.com.observacidade.dto;

import java.time.LocalDateTime;
import java.util.List;

public class AssistenteResponseDTO {

    private String resposta;
    /** LLM = respondida pela IA; FAQ = base de conhecimento; SEM_RESPOSTA = não encontrada. */
    private String origem;
    private Double pontuacao;
    private String faqRelacionada;
    private List<String> sugestoes;
    private LocalDateTime dataHora;

    public AssistenteResponseDTO() {
    }

    public AssistenteResponseDTO(String resposta, String origem, Double pontuacao,
            String faqRelacionada, List<String> sugestoes, LocalDateTime dataHora) {
        this.resposta = resposta;
        this.origem = origem;
        this.pontuacao = pontuacao;
        this.faqRelacionada = faqRelacionada;
        this.sugestoes = sugestoes;
        this.dataHora = dataHora;
    }

    public String getResposta() { return resposta; }
    public void setResposta(String resposta) { this.resposta = resposta; }

    public String getOrigem() { return origem; }
    public void setOrigem(String origem) { this.origem = origem; }

    public Double getPontuacao() { return pontuacao; }
    public void setPontuacao(Double pontuacao) { this.pontuacao = pontuacao; }

    public String getFaqRelacionada() { return faqRelacionada; }
    public void setFaqRelacionada(String faqRelacionada) { this.faqRelacionada = faqRelacionada; }

    public List<String> getSugestoes() { return sugestoes; }
    public void setSugestoes(List<String> sugestoes) { this.sugestoes = sugestoes; }

    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }
}
