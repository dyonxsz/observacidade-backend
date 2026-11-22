package br.com.observacidade.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import br.com.observacidade.dto.*;
import br.com.observacidade.security.UsuarioAutenticado;
import br.com.observacidade.service.AuthService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
@CrossOrigin("*")
public class AuthController {

    private final AuthService authService;
    private final UsuarioAutenticado usuarioAutenticado;

    public AuthController(AuthService authService, UsuarioAutenticado usuarioAutenticado) {
        this.authService = authService;
        this.usuarioAutenticado = usuarioAutenticado;
    }

    @PostMapping("/cadastro")
    public ResponseEntity<LoginResponseDTO> cadastro(@Valid @RequestBody CadastroRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.cadastrar(dto));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        return ResponseEntity.ok(authService.login(dto));
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> me() {
        return ResponseEntity.ok(authService.perfil(usuarioAutenticado.email()));
    }

    @PutMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> atualizarMe(@Valid @RequestBody UsuarioUpdateDTO dto) {
        return ResponseEntity.ok(authService.atualizarPerfil(usuarioAutenticado.email(), dto));
    }

    @PutMapping("/me/senha")
    public ResponseEntity<Map<String, String>> alterarSenha(@Valid @RequestBody AlterarSenhaDTO dto) {
        authService.alterarSenha(usuarioAutenticado.email(), dto);
        return ResponseEntity.ok(Map.of("mensagem", "Senha alterada com sucesso"));
    }
}
