package br.com.observacidade.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;

/**
 * Item da base de conhecimento (FAQ) usada pelo assistente virtual.
 */
@Entity
@Table(name = "faq_assistente")
public class FaqAssistente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 300)
    private String pergunta;

    @Lob
    @Column(nullable = false)
    private String resposta;

    @Column(length = 60)
    private String categoria;

    /** Termos extras (separados por vírgula) para melhorar a busca. */
    @Column(length = 500)
    private String palavrasChave;

    private Boolean ativo = true;

    private LocalDateTime dataCriacao;

    @PrePersist
    public void prePersist() {
        if (dataCriacao == null) dataCriacao = LocalDateTime.now();
        if (ativo == null) ativo = true;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPergunta() { return pergunta; }
    public void setPergunta(String pergunta) { this.pergunta = pergunta; }

    public String getResposta() { return resposta; }
    public void setResposta(String resposta) { this.resposta = resposta; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getPalavrasChave() { return palavrasChave; }
    public void setPalavrasChave(String palavrasChave) { this.palavrasChave = palavrasChave; }

    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }

    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
}
