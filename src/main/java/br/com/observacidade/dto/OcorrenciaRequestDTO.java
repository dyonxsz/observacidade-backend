package br.com.observacidade.dto;

import br.com.observacidade.enums.*;
import jakarta.validation.constraints.*;

public class OcorrenciaRequestDTO {

    @NotNull(message = "Categoria é obrigatória")
    private Categoria categoria;

    @NotBlank(message = "Descrição é obrigatória")
    @Size(min = 5, max = 2000, message = "A descrição deve ter entre 5 e 2000 caracteres")
    private String descricao;

    @Size(max = 200, message = "Rua muito longa")
    private String rua;

    @Size(max = 120, message = "Bairro muito longo")
    private String bairro;

    @Size(max = 255, message = "Ponto de referência muito longo")
    private String referencia;

    @DecimalMin(value = "-90.0", message = "Latitude inválida")
    @DecimalMax(value = "90.0", message = "Latitude inválida")
    private Double latitude;

    @DecimalMin(value = "-180.0", message = "Longitude inválida")
    @DecimalMax(value = "180.0", message = "Longitude inválida")
    private Double longitude;

    @NotNull(message = "Urgência é obrigatória")
    private Urgencia urgencia;

    /** Imagem em base64 (aceita tanto o base64 puro quanto data URL). */
    private String imagemBase64;

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getRua() {
        return rua;
    }

    public void setRua(String rua) {
        this.rua = rua;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Urgencia getUrgencia() {
        return urgencia;
    }

    public void setUrgencia(Urgencia urgencia) {
        this.urgencia = urgencia;
    }

    public String getImagemBase64() {
        return imagemBase64;
    }

    public void setImagemBase64(String imagemBase64) {
        this.imagemBase64 = imagemBase64;
    }
}
