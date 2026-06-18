package br.com.observacidade.controller;

import java.security.Principal;

import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import br.com.observacidade.dto.*;
import br.com.observacidade.entity.Usuario;
import br.com.observacidade.repository.UsuarioRepository;
import br.com.observacidade.security.JwtService;

@RestController
@RequestMapping("/auth")
@CrossOrigin("*")
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/cadastro")
    public ResponseEntity<?> cadastro(@RequestBody CadastroRequestDTO dto) {
        if (dto.getNome() == null || dto.getNome().isBlank()) {
            return ResponseEntity.badRequest().body("Nome é obrigatório");
        }

        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            return ResponseEntity.badRequest().body("E-mail é obrigatório");
        }

        if (dto.getSenha() == null || dto.getSenha().isBlank()) {
            return ResponseEntity.badRequest().body("Senha é obrigatória");
        }

        if (usuarioRepository.findByEmail(dto.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("E-mail já cadastrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail());
        usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        usuario.setRole("USER");

        usuarioRepository.save(usuario);

        String token = jwtService.gerarToken(usuario);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new LoginResponseDTO(token, usuario));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO dto) {
        Usuario usuario = usuarioRepository.findByEmail(dto.getEmail()).orElse(null);

        if (usuario == null || !passwordEncoder.matches(dto.getSenha(), usuario.getSenha())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("E-mail ou senha inválidos");
        }

        String token = jwtService.gerarToken(usuario);

        return ResponseEntity.ok(new LoginResponseDTO(token, usuario));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Principal principal) {
        Usuario usuario = usuarioRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        return ResponseEntity.ok(new UsuarioResponseDTO(usuario));
    }
    @PutMapping("/me")
    public ResponseEntity<?> atualizarMe(@RequestBody UsuarioUpdateDTO dto, Principal principal) {
        Usuario usuario = usuarioRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (dto.getNome() != null && !dto.getNome().isBlank()) {
            usuario.setNome(dto.getNome());
        }

        usuario.setTelefone(dto.getTelefone());
        usuario.setNascimento(dto.getNascimento());
        usuario.setCidade(dto.getCidade());

        usuarioRepository.save(usuario);

        return ResponseEntity.ok(new UsuarioResponseDTO(usuario));
    }

}
