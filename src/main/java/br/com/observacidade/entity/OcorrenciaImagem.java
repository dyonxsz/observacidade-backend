package br.com.observacidade.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "ocorrencia_imagens")
public class OcorrenciaImagem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ocorrencia_id", nullable = false, unique = true)
    private Long ocorrenciaId;

    @Lob
    private String conteudoBase64;

    private String contentType;

    private Integer tamanhoBytes;

    private LocalDateTime dataCriacao;

    @PrePersist
    public void prePersist() {
        if (dataCriacao == null) {
            dataCriacao = LocalDateTime.now();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOcorrenciaId() { return ocorrenciaId; }
    public void setOcorrenciaId(Long ocorrenciaId) { this.ocorrenciaId = ocorrenciaId; }

    public String getConteudoBase64() { return conteudoBase64; }
    public void setConteudoBase64(String conteudoBase64) { this.conteudoBase64 = conteudoBase64; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public Integer getTamanhoBytes() { return tamanhoBytes; }
    public void setTamanhoBytes(Integer tamanhoBytes) { this.tamanhoBytes = tamanhoBytes; }

    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
}
