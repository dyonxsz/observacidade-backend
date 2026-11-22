package br.com.observacidade.service;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.observacidade.dto.AlterarSenhaDTO;
import br.com.observacidade.dto.CadastroRequestDTO;
import br.com.observacidade.dto.LoginRequestDTO;
import br.com.observacidade.dto.LoginResponseDTO;
import br.com.observacidade.dto.UsuarioResponseDTO;
import br.com.observacidade.dto.UsuarioUpdateDTO;
import br.com.observacidade.entity.Usuario;
import br.com.observacidade.exception.RecursoNaoEncontradoException;
import br.com.observacidade.exception.RegraNegocioException;
import br.com.observacidade.repository.UsuarioRepository;
import br.com.observacidade.security.JwtService;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public LoginResponseDTO cadastrar(CadastroRequestDTO dto) {
        String email = normalizarEmail(dto.getEmail());

        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new RegraNegocioException("E-mail já cadastrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(dto.getNome().trim());
        usuario.setEmail(email);
        usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        usuario.setTelefone(dto.getTelefone());
        usuario.setCidade(dto.getCidade());
        usuario.setRole("USER");

        usuario = usuarioRepository.save(usuario);

        return new LoginResponseDTO(jwtService.gerarToken(usuario), usuario);
    }

    @Transactional(readOnly = true)
    public LoginResponseDTO login(LoginRequestDTO dto) {
        String email = normalizarEmail(dto.getEmail());

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BadCredentialsException("E-mail ou senha inválidos"));

        if (!passwordEncoder.matches(dto.getSenha(), usuario.getSenha())) {
            throw new BadCredentialsException("E-mail ou senha inválidos");
        }

        return new LoginResponseDTO(jwtService.gerarToken(usuario), usuario);
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO perfil(String email) {
        return new UsuarioResponseDTO(buscar(email));
    }

    @Transactional
    public UsuarioResponseDTO atualizarPerfil(String email, UsuarioUpdateDTO dto) {
        Usuario usuario = buscar(email);

        // Atualização parcial: campos nulos não apagam os dados existentes.
        if (dto.getNome() != null && !dto.getNome().isBlank()) {
            usuario.setNome(dto.getNome().trim());
        }
        if (dto.getTelefone() != null) {
            usuario.setTelefone(dto.getTelefone());
        }
        if (dto.getNascimento() != null) {
            usuario.setNascimento(dto.getNascimento());
        }
        if (dto.getCidade() != null) {
            usuario.setCidade(dto.getCidade());
        }

        return new UsuarioResponseDTO(usuarioRepository.save(usuario));
    }

    @Transactional
    public void alterarSenha(String email, AlterarSenhaDTO dto) {
        Usuario usuario = buscar(email);

        if (!passwordEncoder.matches(dto.getSenhaAtual(), usuario.getSenha())) {
            throw new RegraNegocioException("Senha atual incorreta");
        }

        if (dto.getSenhaAtual().equals(dto.getNovaSenha())) {
            throw new RegraNegocioException("A nova senha deve ser diferente da senha atual");
        }

        usuario.setSenha(passwordEncoder.encode(dto.getNovaSenha()));
        usuarioRepository.save(usuario);
    }

    private Usuario buscar(String email) {
        return usuarioRepository.findByEmailIgnoreCase(normalizarEmail(email))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));
    }

    private String normalizarEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new RegraNegocioException("E-mail é obrigatório");
        }
        return email.trim().toLowerCase();
    }
}
