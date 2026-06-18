package br.com.observacidade.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import br.com.observacidade.dto.*;
import br.com.observacidade.entity.*;
import br.com.observacidade.repository.*;

@RestController
@RequestMapping("/ocorrencias/{ocorrenciaId}/comentarios")
@CrossOrigin("*")
public class ComentarioController {

    private final ComentarioRepository comentarioRepository;
    private final OcorrenciaRepository ocorrenciaRepository;
    private final UsuarioRepository usuarioRepository;
    private final NotificacaoRepository notificacaoRepository;

    public ComentarioController(
            ComentarioRepository comentarioRepository,
            OcorrenciaRepository ocorrenciaRepository,
            UsuarioRepository usuarioRepository,
            NotificacaoRepository notificacaoRepository) {
        this.comentarioRepository = comentarioRepository;
        this.ocorrenciaRepository = ocorrenciaRepository;
        this.usuarioRepository = usuarioRepository;
        this.notificacaoRepository = notificacaoRepository;
    }

    @GetMapping
    public ResponseEntity<List<ComentarioResponseDTO>> listar(@PathVariable Long ocorrenciaId) {
        List<ComentarioResponseDTO> resposta = comentarioRepository
                .findByOcorrenciaIdOrderByDataCriacaoDesc(ocorrenciaId)
                .stream()
                .map(ComentarioResponseDTO::new)
                .toList();

        return ResponseEntity.ok(resposta);
    }

    @PostMapping
    public ResponseEntity<?> criar(
            @PathVariable Long ocorrenciaId,
            @RequestBody ComentarioRequestDTO dto,
            Principal principal) {

        if (dto.getTexto() == null || dto.getTexto().isBlank()) {
            return ResponseEntity.badRequest().body("Comentário não pode ser vazio");
        }

        Ocorrencia ocorrencia = ocorrenciaRepository.findById(ocorrenciaId)
                .orElseThrow(() -> new RuntimeException("Ocorrência não encontrada"));

        Usuario usuario = usuarioRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Comentario comentario = new Comentario();
        comentario.setTexto(dto.getTexto());
        comentario.setOcorrencia(ocorrencia);
        comentario.setUsuario(usuario);

        comentarioRepository.save(comentario);

        if (ocorrencia.getUsuario() != null) {
            Notificacao n = new Notificacao();
            n.setUsuario(ocorrencia.getUsuario());
            n.setOcorrencia(ocorrencia);
            n.setTipo("COMENTARIO");
            n.setTitulo("Novo comentário na ocorrência");
            n.setMensagem(usuario.getNome() + " comentou: " + dto.getTexto());
            notificacaoRepository.save(n);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(new ComentarioResponseDTO(comentario));
    }
}
