package br.com.observacidade.dto;

import java.time.LocalDateTime;

import br.com.observacidade.entity.Ocorrencia;
import br.com.observacidade.enums.*;

public class OcorrenciaResponseDTO {
    private Long id;
    private Categoria categoria;
    private String descricao;
    private String rua;
    private String bairro;
    private String referencia;
    private Urgencia urgencia;
    private StatusOcorrencia status;
    private LocalDateTime dataCriacao;
    private UsuarioResponseDTO usuario;
    private String imagemBase64;

    public OcorrenciaResponseDTO(Ocorrencia ocorrencia) {
        this.id = ocorrencia.getId();
        this.categoria = ocorrencia.getCategoria();
        this.descricao = ocorrencia.getDescricao();
        this.rua = ocorrencia.getRua();
        this.bairro = ocorrencia.getBairro();
        this.referencia = ocorrencia.getReferencia();
        this.urgencia = ocorrencia.getUrgencia();
        this.status = ocorrencia.getStatus();
        this.dataCriacao = ocorrencia.getDataCriacao();
        this.imagemBase64 = ocorrencia.getImagemBase64();

        if (ocorrencia.getUsuario() != null) {
            this.usuario = new UsuarioResponseDTO(ocorrencia.getUsuario());
        }
    }

    public Long getId() { return id; }
    public Categoria getCategoria() { return categoria; }
    public String getDescricao() { return descricao; }
    public String getRua() { return rua; }
    public String getBairro() { return bairro; }
    public String getReferencia() { return referencia; }
    public Urgencia getUrgencia() { return urgencia; }
    public StatusOcorrencia getStatus() { return status; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public UsuarioResponseDTO getUsuario() { return usuario; }
    public String getImagemBase64() { return imagemBase64; }
}
