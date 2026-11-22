package br.com.observacidade.dto;

import java.time.LocalDateTime;

import br.com.observacidade.entity.Usuario;

public class UsuarioResponseDTO {
    private Long id;
    private String nome;
    private String email;
    private String role;
    private String telefone;
    private String nascimento;
    private String cidade;
    private LocalDateTime dataCriacao;

    public UsuarioResponseDTO(Usuario usuario) {
        this.id = usuario.getId();
        this.nome = usuario.getNome();
        this.email = usuario.getEmail();
        this.role = usuario.getRole();
        this.telefone = usuario.getTelefone();
        this.nascimento = usuario.getNascimento();
        this.cidade = usuario.getCidade();
        this.dataCriacao = usuario.getDataCriacao();
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getTelefone() { return telefone; }
    public String getNascimento() { return nascimento; }
    public String getCidade() { return cidade; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
}
