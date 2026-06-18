package br.com.observacidade.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import br.com.observacidade.dto.*;
import br.com.observacidade.entity.*;
import br.com.observacidade.repository.*;

@RestController
@RequestMapping("/suporte")
@CrossOrigin("*")
public class SuporteController {

    private final SuporteRepository suporteRepository;
    private final UsuarioRepository usuarioRepository;
    private final NotificacaoRepository notificacaoRepository;

    public SuporteController(
            SuporteRepository suporteRepository,
            UsuarioRepository usuarioRepository,
            NotificacaoRepository notificacaoRepository) {
        this.suporteRepository = suporteRepository;
        this.usuarioRepository = usuarioRepository;
        this.notificacaoRepository = notificacaoRepository;
    }

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody SuporteRequestDTO dto, Principal principal) {
        if (dto.getAssunto() == null || dto.getAssunto().isBlank()) {
            return ResponseEntity.badRequest().body("Assunto é obrigatório");
        }

        if (dto.getMensagem() == null || dto.getMensagem().isBlank()) {
            return ResponseEntity.badRequest().body("Mensagem é obrigatória");
        }

        Usuario usuario = usuarioRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Suporte suporte = new Suporte();
        suporte.setAssunto(dto.getAssunto());
        suporte.setMensagem(dto.getMensagem());
        suporte.setUsuario(usuario);

        suporteRepository.save(suporte);

        Notificacao n = new Notificacao();
        n.setUsuario(usuario);
        n.setTipo("SUPORTE");
        n.setTitulo("Mensagem enviada ao suporte");
        n.setMensagem("Recebemos sua solicitação sobre: " + dto.getAssunto());
        notificacaoRepository.save(n);

        return ResponseEntity.status(HttpStatus.CREATED).body(new SuporteResponseDTO(suporte));
    }

    @GetMapping("/meus")
    public ResponseEntity<List<SuporteResponseDTO>> meus(Principal principal) {
        List<SuporteResponseDTO> resposta = suporteRepository
                .findByUsuarioEmailOrderByDataCriacaoDesc(principal.getName())
                .stream()
                .map(SuporteResponseDTO::new)
                .toList();

        return ResponseEntity.ok(resposta);
    }

    @GetMapping
    public ResponseEntity<List<SuporteResponseDTO>> listar() {
        List<SuporteResponseDTO> resposta = suporteRepository
                .findAll()
                .stream()
                .map(SuporteResponseDTO::new)
                .toList();

        return ResponseEntity.ok(resposta);
    }
}
