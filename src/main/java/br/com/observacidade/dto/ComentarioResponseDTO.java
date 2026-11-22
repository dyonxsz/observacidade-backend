package br.com.observacidade.dto;

import java.time.LocalDateTime;

import br.com.observacidade.entity.Comentario;

public class ComentarioResponseDTO {
    private Long id;
    private String texto;
    private LocalDateTime dataCriacao;
    private Long ocorrenciaId;
    private UsuarioResponseDTO usuario;

    public ComentarioResponseDTO(Comentario comentario) {
        this.id = comentario.getId();
        this.texto = comentario.getTexto();
        this.dataCriacao = comentario.getDataCriacao();
        this.ocorrenciaId = comentario.getOcorrencia() != null ? comentario.getOcorrencia().getId() : null;

        if (comentario.getUsuario() != null) {
            this.usuario = new UsuarioResponseDTO(comentario.getUsuario());
        }
    }

    public Long getId() { return id; }
    public String getTexto() { return texto; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public Long getOcorrenciaId() { return ocorrenciaId; }
    public UsuarioResponseDTO getUsuario() { return usuario; }
}
