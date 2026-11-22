package br.com.observacidade.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import br.com.observacidade.entity.Usuario;
import br.com.observacidade.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    public JwtAuthFilter(JwtService jwtService, UsuarioRepository usuarioRepository) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String token = extrairToken(request);

        if (token == null) {
            chain.doFilter(request, response);
            return;
        }

        try {
            String email = jwtService.extrairEmail(token);

            Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email).orElse(null);

            if (usuario != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        usuario.getEmail(),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRole())));

                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (Exception ex) {
            // Token inválido/expirado: segue sem autenticar. Rotas protegidas retornam 401
            // pelo AuthenticationEntryPoint, e rotas públicas continuam funcionando.
            SecurityContextHolder.clearContext();
        }

        chain.doFilter(request, response);
    }

    /**
     * O token vem no header Authorization normalmente. A exceção é a imagem da
     * ocorrência: o componente &lt;Image&gt; do React Native não consegue enviar o
     * header em todos os casos, então aceitamos também {@code ?token=} nesse GET.
     */
    private String extrairToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7).trim();
            return token.isEmpty() ? null : token;
        }

        boolean imagemDaOcorrencia = "GET".equalsIgnoreCase(request.getMethod())
                && request.getRequestURI().matches("^/ocorrencias/\\d+/imagem/?$");

        if (imagemDaOcorrencia) {
            String parametro = request.getParameter("token");
            if (parametro != null && !parametro.isBlank()) {
                return parametro.trim();
            }
        }

        return null;
    }
}
