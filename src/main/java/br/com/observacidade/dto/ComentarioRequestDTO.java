package br.com.observacidade.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ComentarioRequestDTO {

    @NotBlank(message = "Comentário não pode ser vazio")
    @Size(max = 1000, message = "O comentário deve ter no máximo 1000 caracteres")
    private String texto;

    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }
}
