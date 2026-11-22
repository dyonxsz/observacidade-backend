package br.com.observacidade.dto;

import jakarta.validation.constraints.Size;

public class UsuarioUpdateDTO {

    @Size(min = 3, max = 120, message = "O nome deve ter entre 3 e 120 caracteres")
    private String nome;

    @Size(max = 30, message = "Telefone muito longo")
    private String telefone;

    @Size(max = 10, message = "Data de nascimento inválida")
    private String nascimento;

    @Size(max = 120, message = "Cidade muito longa")
    private String cidade;

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getNascimento() { return nascimento; }
    public void setNascimento(String nascimento) { this.nascimento = nascimento; }

    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }
}
