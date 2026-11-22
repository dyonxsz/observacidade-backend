package br.com.observacidade.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AssistenteRequestDTO {

    @NotBlank(message = "Pergunta é obrigatória")
    @Size(max = 500, message = "A pergunta deve ter no máximo 500 caracteres")
    private String pergunta;

    public String getPergunta() { return pergunta; }
    public void setPergunta(String pergunta) { this.pergunta = pergunta; }
}
