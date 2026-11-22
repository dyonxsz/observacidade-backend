package br.com.observacidade.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SuporteUpdateDTO {

    @NotBlank(message = "Resposta é obrigatória")
    @Size(max = 4000, message = "A resposta deve ter no máximo 4000 caracteres")
    private String resposta;

    /** ABERTO, EM_ANDAMENTO, RESPONDIDO ou FECHADO. Padrão: RESPONDIDO. */
    private String status;

    public String getResposta() { return resposta; }
    public void setResposta(String resposta) { this.resposta = resposta; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
