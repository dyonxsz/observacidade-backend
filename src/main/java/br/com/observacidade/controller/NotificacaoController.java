package br.com.observacidade.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import br.com.observacidade.dto.NotificacaoResponseDTO;
import br.com.observacidade.security.UsuarioAutenticado;
import br.com.observacidade.service.NotificacaoService;

@RestController
@RequestMapping("/notificacoes")
@CrossOrigin("*")
public class NotificacaoController {

    private final NotificacaoService service;
    private final UsuarioAutenticado usuarioAutenticado;

    public NotificacaoController(NotificacaoService service, UsuarioAutenticado usuarioAutenticado) {
        this.service = service;
        this.usuarioAutenticado = usuarioAutenticado;
    }

    /** Mantido para compatibilidade: lista apenas as notificações não lidas. */
    @GetMapping
    public ResponseEntity<List<NotificacaoResponseDTO>> listarNaoLidas() {
        return ResponseEntity.ok(service.listarNaoLidas(usuarioAutenticado.email()));
    }

    @GetMapping("/todas")
    public ResponseEntity<List<NotificacaoResponseDTO>> listarTodas() {
        return ResponseEntity.ok(service.listarTodas(usuarioAutenticado.email()));
    }

    @GetMapping("/contador")
    public ResponseEntity<Map<String, Long>> contador() {
        return ResponseEntity.ok(Map.of("naoLidas", service.contarNaoLidas(usuarioAutenticado.email())));
    }

    @PutMapping("/{id}/lida")
    public ResponseEntity<NotificacaoResponseDTO> marcarComoLida(@PathVariable Long id) {
        return ResponseEntity.ok(service.marcarComoLida(id, usuarioAutenticado.email()));
    }

    @PutMapping("/marcar-lidas")
    public ResponseEntity<Map<String, Integer>> marcarTodasComoLidas() {
        int total = service.marcarTodasComoLidas(usuarioAutenticado.email());
        return ResponseEntity.ok(Map.of("marcadas", total));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id, usuarioAutenticado.email());
        return ResponseEntity.noContent().build();
    }
}
