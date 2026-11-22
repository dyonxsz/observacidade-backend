package br.com.observacidade.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import br.com.observacidade.dto.AssistenteEstatisticasDTO;
import br.com.observacidade.dto.AssistenteRequestDTO;
import br.com.observacidade.dto.AssistenteResponseDTO;
import br.com.observacidade.dto.InteracaoAssistenteDTO;
import br.com.observacidade.security.UsuarioAutenticado;
import br.com.observacidade.service.AssistenteService;
import jakarta.validation.Valid;

/**
 * Assistente virtual (FAQ + IA).
 *
 * Pode ser usado pelo cidadão e pelo administrador: as perguntas são
 * registradas no histórico do próprio usuário autenticado.
 */
@RestController
@RequestMapping("/assistente")
@CrossOrigin("*")
public class AssistenteController {

    private final AssistenteService service;
    private final UsuarioAutenticado usuarioAutenticado;

    public AssistenteController(AssistenteService service, UsuarioAutenticado usuarioAutenticado) {
        this.service = service;
        this.usuarioAutenticado = usuarioAutenticado;
    }

    /** Perguntas sugeridas para começar a conversa. */
    @GetMapping("/sugestoes")
    public ResponseEntity<List<String>> sugestoes() {
        return ResponseEntity.ok(service.sugestoes());
    }

    /** Informa se a camada de IA está ativa (sem expor a chave). */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        return ResponseEntity.ok(Map.of(
                "iaDisponivel", service.iaDisponivel(),
                "motor", service.iaProvedor()));
    }

    /** Envia uma pergunta e recebe a resposta. */
    @PostMapping("/perguntas")
    public ResponseEntity<AssistenteResponseDTO> perguntar(
            @Valid @RequestBody AssistenteRequestDTO dto) {

        return ResponseEntity.ok(service.perguntar(dto, usuarioAutenticado.email()));
    }

    /** Últimas perguntas do usuário autenticado. */
    @GetMapping("/historico")
    public ResponseEntity<List<InteracaoAssistenteDTO>> historico() {
        return ResponseEntity.ok(service.historico(usuarioAutenticado.email()));
    }

    /** Métricas de uso do assistente (apenas administrador). */
    @GetMapping("/estatisticas")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AssistenteEstatisticasDTO> estatisticas() {
        return ResponseEntity.ok(service.estatisticas());
    }
}
