package br.com.observacidade.dto;

import br.com.observacidade.entity.Usuario;

public class LoginResponseDTO {
    private String token;
    private Long id;
    private String nome;
    private String email;
    private String role;

    public LoginResponseDTO(String token, Long id, String nome, String email, String role) {
        this.token = token;
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.role = role;
    }

    public LoginResponseDTO(String token, Usuario usuario) {
        this.token = token;
        this.id = usuario.getId();
        this.nome = usuario.getNome();
        this.email = usuario.getEmail();
        this.role = usuario.getRole();
    }

    public String getToken() { return token; }
    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
}
