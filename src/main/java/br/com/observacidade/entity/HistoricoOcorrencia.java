package br.com.observacidade.entity;

import java.time.LocalDateTime;

import br.com.observacidade.enums.StatusOcorrencia;
import jakarta.persistence.*;

/**
 * Linha do tempo de cada ocorrência (quem mudou o status, quando e por quê).
 */
@Entity
@Table(name = "historico_ocorrencias")
public class HistoricoOcorrencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ocorrencia_id", nullable = false)
    private Long ocorrenciaId;

    @Enumerated(EnumType.STRING)
    private StatusOcorrencia statusAnterior;

    @Enumerated(EnumType.STRING)
    private StatusOcorrencia statusNovo;

    @Lob
    private String observacao;

    @Column(name = "usuario_id")
    private Long usuarioId;

    private String usuarioNome;

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

    public StatusOcorrencia getStatusAnterior() { return statusAnterior; }
    public void setStatusAnterior(StatusOcorrencia statusAnterior) { this.statusAnterior = statusAnterior; }

    public StatusOcorrencia getStatusNovo() { return statusNovo; }
    public void setStatusNovo(StatusOcorrencia statusNovo) { this.statusNovo = statusNovo; }

    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public String getUsuarioNome() { return usuarioNome; }
    public void setUsuarioNome(String usuarioNome) { this.usuarioNome = usuarioNome; }

    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
}
