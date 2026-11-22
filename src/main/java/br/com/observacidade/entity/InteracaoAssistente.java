package br.com.observacidade.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;

/**
 * Registro de cada pergunta feita ao assistente.
 * Serve como histórico para o usuário e como métrica de qualidade
 * (quantas foram respondidas pelo FAQ, pela IA ou ficaram sem resposta).
 */
@Entity
@Table(name = "interacoes_assistente")
public class InteracaoAssistente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(nullable = false)
    private String pergunta;

    @Lob
    private String resposta;

    /** LLM = respondida pela IA; FAQ = base de conhecimento; SEM_RESPOSTA = não encontrada. */
    @Column(length = 20)
    private String origem;

    private Double pontuacao;

    @Column(length = 300)
    private String faqRelacionada;

    private LocalDateTime dataHora;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @PrePersist
    public void prePersist() {
        if (dataHora == null) dataHora = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPergunta() { return pergunta; }
    public void setPergunta(String pergunta) { this.pergunta = pergunta; }

    public String getResposta() { return resposta; }
    public void setResposta(String resposta) { this.resposta = resposta; }

    public String getOrigem() { return origem; }
    public void setOrigem(String origem) { this.origem = origem; }

    public Double getPontuacao() { return pontuacao; }
    public void setPontuacao(Double pontuacao) { this.pontuacao = pontuacao; }

    public String getFaqRelacionada() { return faqRelacionada; }
    public void setFaqRelacionada(String faqRelacionada) { this.faqRelacionada = faqRelacionada; }

    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
}
