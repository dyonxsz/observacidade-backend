package br.com.observacidade.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import br.com.observacidade.dto.NotificacaoResponseDTO;
import br.com.observacidade.entity.Notificacao;
import br.com.observacidade.repository.NotificacaoRepository;

@RestController
@RequestMapping("/notificacoes")
@CrossOrigin("*")
public class NotificacaoController {

    private final NotificacaoRepository repository;

    public NotificacaoController(NotificacaoRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public ResponseEntity<List<NotificacaoResponseDTO>> listar(Principal principal) {
        List<Notificacao> notificacoes = repository
                .findByUsuarioEmailAndLidaFalseOrderByDataCriacaoDesc(principal.getName());

        List<NotificacaoResponseDTO> resposta = notificacoes
                .stream()
                .map(NotificacaoResponseDTO::new)
                .toList();

        return ResponseEntity.ok(resposta);
    }

    @PutMapping("/marcar-lidas")
    public ResponseEntity<Void> marcarComoLidas(Principal principal) {
        List<Notificacao> notificacoes = repository
                .findByUsuarioEmailAndLidaFalseOrderByDataCriacaoDesc(principal.getName());

        notificacoes.forEach(n -> n.setLida(true));
        repository.saveAll(notificacoes);

        return ResponseEntity.noContent().build();
    }
}
