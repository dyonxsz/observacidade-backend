package br.com.observacidade.dto;

import java.time.LocalDateTime;
import br.com.observacidade.entity.Suporte;

public class SuporteResponseDTO {
    private Long id;
    private String assunto;
    private String mensagem;
    private String status;
    private LocalDateTime dataCriacao;
    private UsuarioResponseDTO usuario;

    public SuporteResponseDTO(Suporte s) {
        this.id = s.getId();
        this.assunto = s.getAssunto();
        this.mensagem = s.getMensagem();
        this.status = s.getStatus();
        this.dataCriacao = s.getDataCriacao();
        if (s.getUsuario() != null) this.usuario = new UsuarioResponseDTO(s.getUsuario());
    }

    public Long getId() { return id; }
    public String getAssunto() { return assunto; }
    public String getMensagem() { return mensagem; }
    public String getStatus() { return status; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public UsuarioResponseDTO getUsuario() { return usuario; }
}
