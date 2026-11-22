package br.com.observacidade.dto;

import java.time.LocalDateTime;
import java.util.List;

import br.com.observacidade.entity.Ocorrencia;
import br.com.observacidade.entity.OcorrenciaImagem;
import br.com.observacidade.enums.*;

public class OcorrenciaResponseDTO {
    private Long id;
    private String protocolo;
    private Categoria categoria;
    private String categoriaLabel;
    private String descricao;
    private String rua;
    private String bairro;
    private String referencia;
    private Double latitude;
    private Double longitude;
    private Urgencia urgencia;
    private String urgenciaLabel;
    private StatusOcorrencia status;
    private String statusLabel;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;
    private UsuarioResponseDTO usuario;
    private boolean temImagem;
    private String imagemUrl;
    private String imagemBase64;
    private String imagemContentType;
    private long totalComentarios;
    private List<HistoricoResponseDTO> historico;

    public OcorrenciaResponseDTO(
            Ocorrencia ocorrencia,
            OcorrenciaImagem imagem,
            long totalComentarios,
            List<HistoricoResponseDTO> historico,
            boolean incluirImagemBase64) {

        this.id = ocorrencia.getId();
        this.protocolo = ocorrencia.getProtocolo();
        this.categoria = ocorrencia.getCategoria();
        this.categoriaLabel = ocorrencia.getCategoria() != null ? ocorrencia.getCategoria().getLabel() : null;
        this.descricao = ocorrencia.getDescricao();
        this.rua = ocorrencia.getRua();
        this.bairro = ocorrencia.getBairro();
        this.referencia = ocorrencia.getReferencia();
        this.latitude = ocorrencia.getLatitude();
        this.longitude = ocorrencia.getLongitude();
        this.urgencia = ocorrencia.getUrgencia();
        this.urgenciaLabel = ocorrencia.getUrgencia() != null ? ocorrencia.getUrgencia().getLabel() : null;
        this.status = ocorrencia.getStatus();
        this.statusLabel = ocorrencia.getStatus() != null ? ocorrencia.getStatus().getLabel() : null;
        this.dataCriacao = ocorrencia.getDataCriacao();
        this.dataAtualizacao = ocorrencia.getDataAtualizacao();
        this.totalComentarios = totalComentarios;
        this.historico = historico;

        if (ocorrencia.getUsuario() != null) {
            this.usuario = new UsuarioResponseDTO(ocorrencia.getUsuario());
        }

        if (imagem != null) {
            this.temImagem = true;
            this.imagemUrl = "/ocorrencias/" + ocorrencia.getId() + "/imagem";
            if (incluirImagemBase64) {
                this.imagemBase64 = imagem.getConteudoBase64();
            }
            this.imagemContentType = imagem.getContentType();
        }
    }

    public Long getId() { return id; }
    public String getProtocolo() { return protocolo; }
    public Categoria getCategoria() { return categoria; }
    public String getCategoriaLabel() { return categoriaLabel; }
    public String getDescricao() { return descricao; }
    public String getRua() { return rua; }
    public String getBairro() { return bairro; }
    public String getReferencia() { return referencia; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public Urgencia getUrgencia() { return urgencia; }
    public String getUrgenciaLabel() { return urgenciaLabel; }
    public StatusOcorrencia getStatus() { return status; }
    public String getStatusLabel() { return statusLabel; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public LocalDateTime getDataAtualizacao() { return dataAtualizacao; }
    public UsuarioResponseDTO getUsuario() { return usuario; }
    public boolean isTemImagem() { return temImagem; }
    public String getImagemUrl() { return imagemUrl; }
    public String getImagemBase64() { return imagemBase64; }
    public String getImagemContentType() { return imagemContentType; }
    public long getTotalComentarios() { return totalComentarios; }
    public List<HistoricoResponseDTO> getHistorico() { return historico; }
}
