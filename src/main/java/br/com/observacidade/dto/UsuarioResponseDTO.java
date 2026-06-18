package br.com.observacidade.dto;

import br.com.observacidade.entity.Usuario;

public class UsuarioResponseDTO {
    private Long id;
    private String nome;
    private String email;
    private String role;
    private String telefone;
    private String nascimento;
    private String cidade;

    public UsuarioResponseDTO(Usuario usuario) {
        this.id = usuario.getId();
        this.nome = usuario.getNome();
        this.email = usuario.getEmail();
        this.role = usuario.getRole();
        this.telefone = usuario.getTelefone();
        this.nascimento = usuario.getNascimento();
        this.cidade = usuario.getCidade();
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getTelefone() { return telefone; }
    public String getNascimento() { return nascimento; }
    public String getCidade() { return cidade; }
}
