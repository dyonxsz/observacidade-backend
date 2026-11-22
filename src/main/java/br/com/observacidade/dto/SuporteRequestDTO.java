package br.com.observacidade.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SuporteRequestDTO {

    @NotBlank(message = "Assunto é obrigatório")
    @Size(max = 150, message = "O assunto deve ter no máximo 150 caracteres")
    private String assunto;

    @NotBlank(message = "Mensagem é obrigatória")
    @Size(max = 4000, message = "A mensagem deve ter no máximo 4000 caracteres")
    private String mensagem;

    public String getAssunto() { return assunto; }
    public void setAssunto(String assunto) { this.assunto = assunto; }

    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }
}
