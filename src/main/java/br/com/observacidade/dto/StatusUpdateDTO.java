package br.com.observacidade.dto;

import br.com.observacidade.enums.StatusOcorrencia;

public class StatusUpdateDTO {
    private StatusOcorrencia status;

    public StatusOcorrencia getStatus() { return status; }
    public void setStatus(StatusOcorrencia status) { this.status = status; }
}
