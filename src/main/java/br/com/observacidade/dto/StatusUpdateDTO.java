package br.com.observacidade.dto;

import br.com.observacidade.enums.StatusOcorrencia;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class StatusUpdateDTO {

    @NotNull(message = "Status é obrigatório")
    private StatusOcorrencia status;

    @Size(max = 500, message = "Observação muito longa")
    private String observacao;

    public StatusOcorrencia getStatus() { return status; }
    public void setStatus(StatusOcorrencia status) { this.status = status; }

    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
}
