package br.com.observacidade.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import br.com.observacidade.entity.Usuario;
import br.com.observacidade.exception.AcessoNegadoException;
import br.com.observacidade.exception.RecursoNaoEncontradoException;
import br.com.observacidade.repository.UsuarioRepository;

/**
 * Acesso ao usuário autenticado na requisição atual.
 */
@Component
public class UsuarioAutenticado {

    private final UsuarioRepository usuarioRepository;

    public UsuarioAutenticado(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public String email() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            throw new AcessoNegadoException("Usuário não autenticado");
        }

        return auth.getName();
    }

    public Usuario usuario() {
        return usuarioRepository.findByEmailIgnoreCase(email())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));
    }

    public boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }
}
