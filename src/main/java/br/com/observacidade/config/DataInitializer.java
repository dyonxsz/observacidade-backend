package br.com.observacidade.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import br.com.observacidade.entity.Usuario;
import br.com.observacidade.repository.UsuarioRepository;

/**
 * Cria o usuário administrador inicial, caso configurado e ainda inexistente.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:}")
    private String adminEmail;

    @Value("${app.admin.senha:}")
    private String adminSenha;

    public DataInitializer(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (adminEmail == null || adminEmail.isBlank() || adminSenha == null || adminSenha.isBlank()) {
            return;
        }

        String email = adminEmail.trim().toLowerCase();

        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            return;
        }

        Usuario admin = new Usuario();
        admin.setNome("Administrador");
        admin.setEmail(email);
        admin.setSenha(passwordEncoder.encode(adminSenha));
        admin.setRole("ADMIN");

        usuarioRepository.save(admin);
        log.info("Usuário administrador inicial criado: {}", email);
        log.warn("Troque a senha padrão do administrador após o primeiro acesso.");
    }
}
