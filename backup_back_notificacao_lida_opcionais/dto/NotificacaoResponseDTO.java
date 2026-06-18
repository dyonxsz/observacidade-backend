package br.com.observacidade.dto;

import java.time.LocalDateTime;

import br.com.observacidade.entity.Notificacao;

public class NotificacaoResponseDTO {
    private Long id;
    private String tipo;
    private String titulo;
    private String mensagem;
    private LocalDateTime dataCriacao;
    private Long ocorrenciaId;
    private Boolean lida;

    public NotificacaoResponseDTO(Notificacao n) {
        this.id = n.getId();
        this.tipo = n.getTipo();
        this.titulo = n.getTitulo();
        this.mensagem = n.getMensagem();
        this.dataCriacao = n.getDataCriacao();
        this.ocorrenciaId = n.getOcorrencia() != null ? n.getOcorrencia().getId() : null;
        this.lida = n.getLida();
    }

    public Long getId() { return id; }
    public String getTipo() { return tipo; }
    public String getTitulo() { return titulo; }
    public String getMensagem() { return mensagem; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public Long getOcorrenciaId() { return ocorrenciaId; }
}
