package br.com.observacidade.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import br.com.observacidade.dto.*;
import br.com.observacidade.security.UsuarioAutenticado;
import br.com.observacidade.service.ComentarioService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/ocorrencias/{ocorrenciaId}/comentarios")
@CrossOrigin("*")
public class ComentarioController {

    private final ComentarioService comentarioService;
    private final UsuarioAutenticado usuarioAutenticado;

    public ComentarioController(ComentarioService comentarioService, UsuarioAutenticado usuarioAutenticado) {
        this.comentarioService = comentarioService;
        this.usuarioAutenticado = usuarioAutenticado;
    }

    @GetMapping
    public ResponseEntity<List<ComentarioResponseDTO>> listar(@PathVariable Long ocorrenciaId) {
        return ResponseEntity.ok(comentarioService.listar(ocorrenciaId));
    }

    @PostMapping
    public ResponseEntity<ComentarioResponseDTO> criar(
            @PathVariable Long ocorrenciaId,
            @Valid @RequestBody ComentarioRequestDTO dto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(comentarioService.criar(ocorrenciaId, dto, usuarioAutenticado.email()));
    }

    @DeleteMapping("/{comentarioId}")
    public ResponseEntity<Void> deletar(
            @PathVariable Long ocorrenciaId,
            @PathVariable Long comentarioId) {

        comentarioService.deletar(ocorrenciaId, comentarioId, usuarioAutenticado.email());
        return ResponseEntity.noContent().build();
    }
}
